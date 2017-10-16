package pandelis.ga_testnavig;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import static android.R.attr.path;

/**
 * Created by PadPad on 3/8/2016.
 */

public class ImageAdapter extends BaseAdapter{
    private Context mContext;
    ArrayList<String> imageList = new ArrayList<String>();
    private  Bitmap placeholerBitmap;
    private  static LruCache<String,Bitmap> mMemoryCache;
    private  int width,height;

    //Handling concurrency, when the images is loaded ,with fast scroll,
    //we don`t want to lose the correct order from images
    public  static class AsyncDrawable extends BitmapDrawable {

        final WeakReference<BitmapWorkerTask> taskReference;

        public AsyncDrawable(Resources resources, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask){

            super(resources,bitmap);
            taskReference=new WeakReference(bitmapWorkerTask);
        }
        public BitmapWorkerTask getBitmapWorkerTask(){
            return taskReference.get();
        }
    }

    private static LayoutInflater inflater = null;

    public ImageAdapter(Context c) {
        mContext = c;
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemorySize=(int) Runtime.getRuntime().maxMemory()/1024;
        // Use 1/6 of the available memory for this memory cache.
        final int cacheSize = maxMemorySize/6;

        //initialize cache ,mMemoryCache hold all the bitmaps in cache
        mMemoryCache = new LruCache<String,Bitmap>(cacheSize){

            //Override sizeOf, to size the cache in different units.
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount()/1024;
            }
        };
    }

    //How many items are in the data set represented by this Adapter.
    public int getCount() {
       // return imageList.size();
        int i=0;

        String ExternalStorageDirectoryPath = Environment
                .getExternalStorageDirectory()
                .getAbsolutePath();

        String targetPath = ExternalStorageDirectoryPath + "/Pictures/Hello Camera1";

        //sort the images to the latest has been taken
        File targetDirector = new File(targetPath);
        if(targetDirector.listFiles()==null){
            Toast.makeText(mContext,"You have not yet captured a photo",Toast.LENGTH_SHORT).show();
        }
        else if(targetDirector.listFiles()!=null) {

            File[] files = targetDirector.listFiles();
            File[] sort = sortFilesToLatest(files);

            //add the paths of the file to the arraylist imagelist
            for (File file : sort) {
                imageList.add(file.getAbsolutePath());
                i++;
            }
        }
        //return the number of the images t
        return i;
    }
    //Array Sort Images to the latest
    private  File[] sortFilesToLatest(File[] fileImageDir){
        //File[] files= fileImageDir.listFiles();
        Arrays.sort(fileImageDir, new Comparator<File>() {
            @Override
            public int compare(File file, File t1) {
                return Long.valueOf(t1.lastModified()).compareTo(file.lastModified());
            }
        });

        return fileImageDir;
    }


    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView imageView;
        if (convertView == null) {
            //check orientation and the screen size
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(metrics);
            //calculate the size of screen in inch
            double density = metrics.density * 160;
            double x = Math.pow(metrics.widthPixels / density, 2);
            double y = Math.pow(metrics.heightPixels / density, 2);
            double screenInches = Math.sqrt(x + y);

            int rotation = windowManager.getDefaultDisplay().getRotation();
            //change the size of thumbnail in depends on the screen size
            if(screenInches<=3.9){

                if(rotation == 0){
                    width=230;
                    height=200;
                }else {
                    width=200;
                    height=200;
                }
            }else if(screenInches<=4.6){
                if(rotation == 0){
                    width=330;
                    height=210;
                }else {
                    width=310;
                    height=200;
                }
            }else if(screenInches<=7){
                if(rotation == 0){
                    width=500;
                    height=300;
                }else {
                    width=450;
                    height=200;
                }
            }
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(width,height));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(5, 5, 5, 5);
        } else {
            imageView = (ImageView) convertView;
        }

        Bitmap bitmap = getBitmapFromMemoryCache(imageList.get(position));
        //if bitmap is null call async task to load bitmap from storage,otherwise load bitmap to imageview
        if(bitmap!=null){
            imageView.setImageBitmap(bitmap);
        }
        else if(cheBitmapWorkerTask(imageList.get(position),imageView)){

            BitmapWorkerTask bitmapWorkerTask= new BitmapWorkerTask(imageView);
            AsyncDrawable asyncDrawable = new AsyncDrawable(imageView.getResources(),
                    placeholerBitmap,bitmapWorkerTask);

            imageView.setImageDrawable(asyncDrawable);
            bitmapWorkerTask.execute(imageList.get(position));
        }

        return imageView;

    }

    //The cheBitmapWorkerTask method  checks if another running task is already associated with the ImageView.
    // If so, it attempts to cancel the previous task
    public static boolean cheBitmapWorkerTask(String imagesFile, ImageView imageView){

        BitmapWorkerTask bitmapWorkerTask= getBitmapWorkerTask(imageView);
        if(bitmapWorkerTask != null){

            final  String workerFile = bitmapWorkerTask.getImagefile();
            //workerFile differs from the new data
            if(workerFile !=null){
                if (workerFile !=imagesFile){
                    // Cancel previous task
                    bitmapWorkerTask.cancel(true);
                }else {
                    //bitmapworkertask file is the same as the imageview we are expecting
                    //so do nothing
                    return  false;
                }
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return  true;
    }

    //getBitmapWorkerTask(), is used above to retrieve the task associated with a particular ImageView:
    public  static BitmapWorkerTask getBitmapWorkerTask (ImageView imageView){
        Drawable drawable= imageView.getDrawable();
        if(drawable instanceof AsyncDrawable){

            AsyncDrawable asyncDrawable=(AsyncDrawable)drawable;
            return  asyncDrawable.getBitmapWorkerTask();
        }
        return null;
    }
    //setter and getter to access memory cache from our activities
    public  static Bitmap getBitmapFromMemoryCache(String key){
        return mMemoryCache.get(key);
    }

    public static  void  setBitmapToMemoryCache(String key,Bitmap bitmap){
        if(getBitmapFromMemoryCache(key)== null ){
            mMemoryCache.put(key,bitmap);
        }
    }
}
