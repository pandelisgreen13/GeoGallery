package pandelis.ga_testnavig;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;

/**
 * Created by PadPad on 20/9/2016.
 */

public class Map_Activity extends AppCompatActivity implements GoogleMap.OnInfoWindowClickListener,OnMapReadyCallback {

    private GoogleMap mMap;
    FirebaseDatabase fbdatabase;
    DatabaseReference fbref;
    LocationVar locationVar;
    File targetDirector;
    Double string,humi ;
    Double string2,temp ;
    String path;
    int i = 0;
    private Double[] toppings;
    String ExternalStorageDirectoryPath = Environment
            .getExternalStorageDirectory()
            .getAbsolutePath();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //declare Firebase and get the unique id from the device ,to read from the database
        String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        fbdatabase = FirebaseDatabase.getInstance();
        fbref = fbdatabase.getReference(id);

    }



    @Override
    public void onBackPressed() {
        finish();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        //map goes to Athens
        LatLng athens = new LatLng(37.981947, 23.727127);
        mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(athens, 11));
        mMap.setOnInfoWindowClickListener(this);
        //check from Firebase the data
        fbref.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                locationVar = dataSnapshot.getValue(LocationVar.class);
                String targetPath = ExternalStorageDirectoryPath + "/Pictures/Hello Camera1/" +locationVar.getKey() + ".jpg";
                targetDirector = new File(targetPath);

                //check if the picture has been deleted
                if(targetDirector.exists()) {
                //retrieve values from firebase
                    string = locationVar.getLati();
                    string2 = locationVar.getLong();
                    path = locationVar.getKey();
                    humi = locationVar.getHumi();
                    temp = locationVar.getTemp();
                    //add a marker on the map
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(string2, string))
                            .title(path)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            .snippet(getResources().getString(R.string.humi)+":" + humi.toString() + "%" + "\n" + getResources().getString(R.string.temp)+":" + temp.toString() + "C"));
                }else{
                    //delete from the firebase the child if the picture has been deleted
                    fbref.child(locationVar.getKey()).removeValue();
                }
                //if marker is clicked ,open Info Window
                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                @Override
                public View getInfoWindow(Marker marker) {

                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View v =getLayoutInflater().inflate(R.layout.info_window,null);

                          TextView tv_long= (TextView)v.findViewById(R.id.longt);

                            String targetPath = ExternalStorageDirectoryPath + "/Pictures/Hello Camera1/" + marker.getTitle() + ".jpg";


                            targetDirector = new File(targetPath);

                            ImageView imageView = (ImageView) v.findViewById(R.id.imageView);

                            BitmapFactory.Options options = new BitmapFactory.Options();
                            // downsizing image as it throws OutOfMemory Exception for larger
                                // images
                            options.inSampleSize = 8;

                            final Bitmap bitmap = BitmapFactory.decodeFile(targetDirector.getPath(),options);

                            //Rotate thumbnail in google maps
                            BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(imageView);
                            Bitmap bitmap1 = bitmapWorkerTask.getRotatedBitmap(targetDirector.getPath(), bitmap);

                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            imageView.setImageBitmap(bitmap1);

                            tv_long.setText(marker.getSnippet());

                    return v;
                }
            });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        Intent intent = new Intent(this, ActivityImage.class);
        intent.putExtra("str1", marker.getTitle());
        startActivity(intent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        //getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // user can change the type of the map

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.map_type_hybrid:
                if (mMap != null) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    return true;
                }
            case R.id.map_type_none:
                if (mMap != null) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                    return true;
                }
            case R.id.map_type_normal:
                if (mMap != null) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    return true;
                }
            case R.id.map_type_satellite:
                if (mMap != null) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    return true;
                }
            case R.id.map_type_terrain:
                if (mMap != null) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }
}
