package pandelis.ga_testnavig;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.Toast;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Gallery_Frag.OnFragmentInteractionListener {

    // Activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public boolean onclick;


    // directory name to store captured images and videos
    private static final String IMAGE_DIRECTORY_NAME = "Hello Camera1";

    public Uri fileUri; // file url to store image/video
    public static   String mCurrentPath;

    FirebaseDatabase fbdatabase;
    DatabaseReference fbref;
    private BroadcastReceiver broadcastreceriver;
    private  static  String camera_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //start 1st fragment
        Gallery_Frag gallery_frag = Gallery_Frag.newInstance(mCurrentPath);
        FragmentManager manager= getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.relativelayout_for_fragment,gallery_frag,gallery_frag.getTag()).commit();
        // Highlight the selected item has been done by NavigationView

       // String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        fbdatabase=FirebaseDatabase.getInstance();
        fbref=fbdatabase.getReference(id);

        if(! runtime_permissions());
        //call gps_service to enable gps

        Intent i =new Intent(getApplicationContext(),GPS_SERVICE.class);
        startService(i);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastreceriver!=null){
            unregisterReceiver(broadcastreceriver);
        }
        Intent i = new Intent(getApplicationContext(),GPS_SERVICE.class);
        stopService(i);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.camera_button) {
            Toast.makeText(this, "Camera", Toast.LENGTH_SHORT).show();
            captureImage();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_gallery) {
            // Handle the camera action
            Gallery_Frag gallery_frag = Gallery_Frag.newInstance(camera_state);
            FragmentManager manager= getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment,gallery_frag,gallery_frag.getTag()).commit();
            // Highlight the selected item has been done by NavigationView

        } else if (id == R.id.nav_temp) {

            Statistics statistics = new Statistics();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, statistics, statistics.getTag()).commit();

        }  else if (id == R.id.nav_share) {

            Language language = new Language();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, language, language.getTag()).commitAllowingStateLoss();

        } else if (id == R.id.nav_maps) {
            Intent intent= new Intent(MainActivity.this,Map_Activity.class);
            startActivity(intent);

        } else if (id == R.id.nav_info) {
            Info_Frag info_frag = new Info_Frag();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, info_frag, info_frag.getTag()).commit();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(String data) {
        Toast.makeText(this,data,Toast.LENGTH_SHORT).show();
    }

    //camera
    /**
     * Checking device has camera hardware or not
     * */
    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
    /**
     * Capturing Camera Image will launch camera app request image capture
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
            }

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
              // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
               // get the file url
//        Toast.makeText(getApplicationContext(),
//                "onRestoreInstanceState", Toast.LENGTH_SHORT)
//                .show();
        fileUri = savedInstanceState.getParcelable("file_uri");
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * Receiving activity result method will be called after closing the camera
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                camera_state="true";
                //when return from camera app and gps has a location , we get all the values
                //from the sensors and store to firebase
                if(broadcastreceriver ==null){
                    broadcastreceriver= new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {

                            String key = mCurrentPath;
                            String Longitude = " "+intent.getExtras().get("long");
                            String Latitude= " "+intent.getExtras().get("coordinates");
                            String temperature=" "+intent.getExtras().get("temperature");
                            String humidity=" "+intent.getExtras().get("hu");
                            String speed=" "+intent.getExtras().get("speed");

                            Double value = Double.parseDouble(Longitude);
                            Double value2 = Double.parseDouble(Latitude);
                            Double value3 = Double.parseDouble(temperature);
                            Double value4 = Double.parseDouble(humidity);
                            Double value5 = Double.parseDouble(speed);

                            LocationVar locationVar = new LocationVar();
                            locationVar.setkey(key);
                            locationVar.setLati(value2);
                            locationVar.setLong(value);
                            locationVar.setTemp(value3);
                            locationVar.setHumi(value4);
                            locationVar.setSpeed(value5);
                            //add to firebase
                            fbref.child(key).setValue(locationVar);

                        }
                    };
                    registerReceiver(broadcastreceriver,new IntentFilter("location update"));
                }

            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image /
     */
    private  static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + timeStamp + ".jpg");
            mCurrentPath = timeStamp;
        }
        else {
            return null;
        }
        return mediaFile;
    }

    private boolean runtime_permissions() {
        if(Build.VERSION.SDK_INT >=23 && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){

            //100 request perimision code
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},100);
            return  true;
        }
        return  false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==100){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] ==PackageManager.PERMISSION_GRANTED) {

            }else{
                runtime_permissions();
            }
        }
    }


}
