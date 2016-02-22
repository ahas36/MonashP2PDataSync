package monash.infotech.monashp2pdatasync.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;


/**
 * Created by john on 2/6/2016.
 */
public class ImageCapture {
    ImageView imageView=null;
    public  int REQUEST_TAKE_PHOTO;
    private  String mCurrentPhotoPath="";
    Activity context;
    String imagePath;
    private boolean isNew=false;
    String currentFile;
    public ImageCapture(Activity context,LinearLayout linearLayout, String imagePath,int itemId,String currentFile)
    {
        this.currentFile=currentFile;
        this.context=context;
        this.imagePath=  Environment.getExternalStorageDirectory().
                getAbsolutePath() + "/monashP2P/image/JPEG_"+imagePath+".jpg";
        imageView=new ImageView(context);
        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 700);
        imageView.setLayoutParams(lparams);
        linearLayout.addView(imageView);
        loadImage();
        REQUEST_TAKE_PHOTO=itemId;
        Button takeImage=new Button(context);
        takeImage.setText("take Image");
        linearLayout.addView(takeImage);
        takeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

    }
    public  void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d("Ali", "dispatchTakePictureIntent ");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                isNew=true;
                context.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }
    private  File createImageFile() throws IOException {

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/monashP2p/image");
        if(!dir.exists())
        {
            dir.mkdir();
        }

        File image = File.createTempFile(
                "JPEG_" + System.currentTimeMillis(),  /* prefix */
                ".jpg",         /* suffix */
                dir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void loadImage()
    {

        File tempImage = new File(mCurrentPhotoPath);
        if(tempImage.exists())
        {
            imageView.setImageBitmap(
                    decodeSampledBitmapFromFile(mCurrentPhotoPath, 100, 100));
        }
        else
        {
            File image=new File(currentFile);
            if(image.exists())
            {
                imageView.setImageBitmap(
                        decodeSampledBitmapFromFile(currentFile, 100, 100));
            }
        }
    }

    public void saveImage()
    {
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        try {
            File tempImage=new File(mCurrentPhotoPath);
            File image=new File(imagePath);

            if (tempImage.exists()) {
                FileChannel src = new FileInputStream(tempImage).getChannel();
                FileChannel dst = new FileOutputStream(image).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }
            tempImage.delete();
        } catch (Exception e) {

        }
    }
    public void clear()
    {
        File f = new File(mCurrentPhotoPath);
        if(f.exists())
        {
            f.delete();
        }
    }

    public boolean isNew() {
        return isNew;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath)
    {
        this.imagePath=Environment.getExternalStorageDirectory().
                getAbsolutePath() + "/monashP2P/image/JPEG_"+imagePath+".jpg";
    }

    public static Bitmap decodeSampledBitmapFromFile(String fileName,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileName, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(fileName, options);
    }
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
