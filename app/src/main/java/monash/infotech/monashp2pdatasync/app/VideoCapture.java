package monash.infotech.monashp2pdatasync.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by john on 2/7/2016.
 */
public class VideoCapture {
    public int REQUEST_VIDEO_CAPTURE;
    VideoView videoView = null;
    private String mCurrentVideoPath = "";
    Activity context;
    String videoPath;
    String currentFile;
    private boolean isNew=false;
    public VideoCapture(Activity context, LinearLayout linearLayout, String videoPath, int itemId,String currentFile) {
        this.currentFile=currentFile;
        this.context = context;
        this.videoPath = Environment.getExternalStorageDirectory().
                getAbsolutePath() + "/monashP2P/video/mp4_" + videoPath + ".mp4";
        videoView = new VideoView(context);
        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 700);
        videoView.setLayoutParams(lparams);
        linearLayout.addView(videoView);
        loadVideo();
        REQUEST_VIDEO_CAPTURE = itemId;
        Button record = new Button(context);
        record.setText("record");
        linearLayout.addView(record);
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakeVideoIntent();
            }
        });

    }


    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takeVideoIntent.resolveActivity(context.getPackageManager()) != null) {
            // Create the File where the photo should go
            File videoFile = null;
            try {
                videoFile = createVideoFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d("Ali", "dispatchTakePictureIntent ");
            }
            // Continue only if the File was successfully created
            if (videoFile != null) {
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(videoFile));
                isNew=true;
                context.startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
            }
        }
    }

    private File createVideoFile() throws IOException {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/monashP2p/video");
        if (!dir.exists()) {
            dir.mkdir();
        }

        File video = File.createTempFile(
                "mp4_" + System.currentTimeMillis(),  /* prefix */
                ".mp4",         /* suffix */
                dir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentVideoPath = video.getAbsolutePath();
        return video;

    }

    public void saveVideo()
    {
        File f = new File(mCurrentVideoPath);
        Uri contentUri = Uri.fromFile(f);
        try {
            File tempImage=new File(mCurrentVideoPath);
            File image=new File(videoPath);

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
        File f = new File(mCurrentVideoPath);
        if(f.exists())
        {
            f.delete();
        }
    }

    public void loadVideo() {
        File tempVideo = new File(mCurrentVideoPath);
        if(tempVideo.exists())
        {
            MediaController mc = new MediaController(context);
            videoView.setMediaController(mc);
            videoView.setVideoURI(Uri.parse(mCurrentVideoPath));
            videoView.requestFocus();
            videoView.start();
        }
        else
        {
            File video=new File(currentFile);
            if(video.exists())
            {
                MediaController mc = new MediaController(context);
                videoView.setMediaController(mc);
                videoView.setVideoURI(Uri.parse(currentFile));
                videoView.requestFocus();
                videoView.start();
            }
        }
    }

    public boolean isNew() {
        return isNew;
    }

    public String getVideoPath() {
        return videoPath;
    }
    public void setVideoPath(String videoPath) {
        this.videoPath = Environment.getExternalStorageDirectory().
                getAbsolutePath() + "/monashP2P/video/mp4_" + videoPath + ".mp4";
    }
}
