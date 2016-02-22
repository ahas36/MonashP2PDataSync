package monash.infotech.monashp2pdatasync.app;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import monash.infotech.monashp2pdatasync.R;

/**
 * Created by john on 2/6/2016.
 */
public class AudioRecorder {
    private MediaRecorder myRecorder;
    private MediaPlayer myPlayer;
    private String outputFile = null;
    private String recorded = null;
    private ImageButton recordBtn;
    private ImageButton playBtn;
    private boolean isPlaying=false;
    private boolean isNew = false;
    String currentFile;

    public AudioRecorder(Context context, LinearLayout layout, String itemId, String currentFile) {
        this.currentFile = currentFile;
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/monashP2p/sound");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        recorded = Environment.getExternalStorageDirectory().
                getAbsolutePath() + "/monashP2P/sound/sound_" + itemId + ".3gpp";
        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout temp = new LinearLayout(context);
        temp.setLayoutParams(lparams);
        temp.setOrientation(LinearLayout.HORIZONTAL);
        temp.setBackgroundResource(R.drawable.border);
        layout.addView(temp);

        recordBtn = new ImageButton(context);
        recordBtn.setLayoutParams(new LinearLayout.LayoutParams(256, 256));
        recordBtn.setBackgroundResource(R.drawable.recordbtn);
        temp.addView(recordBtn);
        recordBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_OUTSIDE:
                    case MotionEvent.ACTION_CANCEL:
                        stop();
                        v.setPressed(false);
                        // Stop action ...
                        break;
                }
                return false;
            }
        });

        playBtn = new ImageButton(context);
        playBtn.setLayoutParams(new LinearLayout.LayoutParams(256, 256));
        playBtn.setBackgroundResource(R.drawable.playbtn);
        if ( new File(currentFile).exists()) {
            playBtn.setEnabled(true);
        } else {
            playBtn.setEnabled(false);
        }
        temp.addView(playBtn);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(isPlaying)
                {
                    stopPlay();
                }
                else
                {
                    play();
                }

            }
        });
    }

    public void start() {
        try {
            clear();
            outputFile = Environment.getExternalStorageDirectory().
                    getAbsolutePath() + "/monashP2p/sound/sound_" + System.currentTimeMillis() + ".3gpp";
            myRecorder = new MediaRecorder();
            myRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            myRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            myRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
            myRecorder.setOutputFile(outputFile);
            myRecorder.prepare();
            myRecorder.start();
        } catch (IllegalStateException e) {
            // start:it is called before prepare()
            // prepare: it is called after start() or before setOutputFormat()
            e.printStackTrace();
        } catch (IOException e) {
            // prepare() fails
            e.printStackTrace();
        }

    }

    public void stop() {
        try {
            myRecorder.stop();
            myRecorder.release();
            myRecorder = null;
            isNew = true;
            playBtn.setEnabled(true);

        } catch (IllegalStateException e) {
            //  it is called before start()
            e.printStackTrace();
        } catch (RuntimeException e) {
            // no valid audio/video data has been received
            e.printStackTrace();
        }
    }

    public void play() {
        try {
            isPlaying=true;
            myPlayer = new MediaPlayer();
            if (outputFile!=null && new File(outputFile).exists()) {
                myPlayer.setDataSource(outputFile);
            } else {
                myPlayer.setDataSource(currentFile);
            }
            myPlayer.prepare();
            myPlayer.start();
            playBtn.setBackgroundResource(R.drawable.stop);
            myPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playBtn.setBackgroundResource(R.drawable.playbtn);
                    isPlaying = false;
                }
            });

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void stopPlay() {
        try {
            if (myPlayer != null) {
                myPlayer.stop();
                myPlayer.release();
                myPlayer = null;
                playBtn.setBackgroundResource(R.drawable.playbtn);
                isPlaying=false;

            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void clear() {
        if(outputFile==null)
            return;
        File tempSound = new File(outputFile);
        if (tempSound.exists()) {
            tempSound.delete();
        }
    }

    public void save() {
        try {

            File tempSound = new File(outputFile);
            File inputSound = new File(recorded);

            if (tempSound.exists()) {
                FileChannel src = new FileInputStream(tempSound).getChannel();
                FileChannel dst = new FileOutputStream(inputSound).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }
            tempSound.delete();
        } catch (Exception e) {

        }
    }

    public boolean isNew() {
        return isNew;
    }

    public String getRecorded() {
        return recorded;
    }
    public void setRecorded(String itemId) {
        recorded = Environment.getExternalStorageDirectory().
                getAbsolutePath() + "/monashP2P/sound/sound_" + itemId + ".3gpp";
    }
}