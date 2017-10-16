package pandelis.ga_testnavig;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Image;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by PadPad on 10/8/2016.
 */

//  we use AsyncTask to execute some work in a background thread and publish the results back on the UI thread
public class BitmapWorkerTask extends AsyncTask<String,Void,Bitmap> {

    private  String mImagefile;

    WeakReference<ImageView> imageViewWeakReference;

    public BitmapWorkerTask(ImageView imageView){
        // Use a WeakReference to ensure the ImageView can be the garbage collected
        imageViewWeakReference= new WeakReference<ImageView>(imageView);
    }
    // Decode image in background and cache image.
    @Override
    protected Bitmap doInBackground(String... params) {

        mImagefile= params[0];
        //load image from storage
        Bitmap bitmap = decodeSampledBitmapFromUri(mImagefile,200,200);
        //rotate bitmap to the correct orientantion
        Bitmap rotatedBitmap = getRotatedBitmap(params[0],bitmap);
        //set bitmap to memory cache
        ImageAdapter.setBitmapToMemoryCache(mImagefile,rotatedBitmap);

        return  rotatedBitmap;
   }
    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected  void onPostExecute(Bitmap bitmap){

        //BitmapWorkerTask  checks if the task is cancelled and if the current task matches the one associated with the ImageView:
        if(isCancelled()){
            bitmap=null;
        }
        if(bitmap !=null && imageViewWeakReference!=null){
            ImageView imageview = imageViewWeakReference.get();
            BitmapWorkerTask bitmapWorkerTask = ImageAdapter.getBitmapWorkerTask(imageview);
            if (this== bitmapWorkerTask && imageview!=null){
                imageview.setImageBitmap(bitmap);
            }
        }
    }

    //decodes image and scales it to reduce memory consumption
    public Bitmap decodeSampledBitmapFromUri(String path, int reqWidth, int reqHeight) {

        Bitmap bm = null;
        //decode image size
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(path, options);

        return bm;
    }

    public int calculateInSampleSize(

            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        //Find the correct scale value. It should be the power of 2
        if(height> reqHeight || width>reqWidth){

            final int halfheight = height/2;
            final int halfwidth=width/2;

            while((halfheight/inSampleSize) > reqHeight && (halfwidth/inSampleSize)>reqWidth){

                inSampleSize *=2;
            }
        }
        return inSampleSize;
    }

    public  String getImagefile(){

        return  mImagefile;
    }
    //check bitmap orientation and rotate to correct orientation
    public Bitmap getRotatedBitmap(String path, Bitmap bitmap){
        Bitmap rotatedBitmap = null;
        Matrix m = new Matrix();
        ExifInterface exif = null;
        int orientation = 1;

        try {
            if(path!=null){
                // Getting Exif information of the file
                exif = new ExifInterface(path);
            }
            if(exif!=null){
                //check current orientation of image
                orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
                switch(orientation){
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        m.preRotate(270);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        m.preRotate(90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        m.preRotate(180);
                        break;
                }
                // Rotates the image to vertical orientation
                rotatedBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),m,true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotatedBitmap;
    }
}
