package pandelis.ga_testnavig;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ActivityImage extends AppCompatActivity {


    //get path of device
    String ExternalStorageDirectoryPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    //declare varialbles
    File targetDirector;
    PhotoViewAttacher mAttacher;
    String targetPath;
    ImageView imageView;
    TextView Date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        //declare ImageButton and imageview
        ImageButton closeX;
        imageView = (ImageView) findViewById(R.id.SingleView);
        Date=(TextView)findViewById(R.id.date);

        //Get file name of image from another activity
        Bundle b = this.getIntent().getExtras();
        String path=b.getString("str1");

        //check if containis ".jpg" in order to have the right path
        if(path.contains(".jpg")) {
             targetPath = path;
        }else {
             targetPath = ExternalStorageDirectoryPath + "/Pictures/Hello Camera1/" + path + ".jpg";
        }

        targetDirector = new File(targetPath);
        BitmapFactory.Options options = new BitmapFactory.Options();
        // downsizing image as it throws OutOfMemory Exception for larger  images
        options.inSampleSize = 2;

        final Bitmap bitmap = BitmapFactory.decodeFile(targetDirector.getPath(),options);

        //Rotate thumbnail in google maps
        BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(imageView);
        Bitmap bitmap1 = bitmapWorkerTask.getRotatedBitmap(targetDirector.getPath(), bitmap);

        //Set date that the picture has taken
        Date date = new Date(targetDirector.lastModified());
        String time = new SimpleDateFormat("dd/MM/yyyy").format(date);
        Date.setText(time);
        // Set the Picture displayed
        imageView.setImageBitmap(bitmap1);
        // Attach a PhotoViewAttacher, which takes care of all of the zooming functionality.
        mAttacher = new PhotoViewAttacher(imageView);
        //Image Button to close preview
        closeX=(ImageButton) findViewById(R.id.btn_close);
        closeX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
