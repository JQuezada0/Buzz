package moby.mobyv02;

import android.content.DialogInterface;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import java.io.File;
import java.io.IOException;
import java.sql.Time;

/**
 * Created by quezadjo on 9/23/2015.
 */
public class VideoActivity extends FragmentActivity implements SurfaceHolder.Callback {

    private Camera camera;
    private MediaRecorder mediaRecorder;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private ImageView recordButton;
    private MediaController mediaController;
    private FrameLayout videoFrame;
    private VideoView videoView;
    private int position = 0;
    private int facing = Camera.CameraInfo.CAMERA_FACING_BACK;
    private File file;
    private CircleProgressBar progress;
    private boolean recorderInitializationSuccessful = false;
    private boolean recording = false;
    private boolean stopped = false;
    private boolean startRecording = false;
    private Camera.Parameters parameters;
    private ImageView cancelButton;
    private ImageView continueButton;
    private CountDownTimer timer;
    private int currentCount = 10;
    private TextView count;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_activity);
        surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        recordButton = (ImageView) findViewById(R.id.capture_video_button);
        cancelButton = (ImageView) findViewById(R.id.capture_video_cancel_button);
        continueButton = (ImageView) findViewById(R.id.capture_video_ok_button);
        videoView = (VideoView) findViewById(R.id.camera_video_view);
        videoFrame = (FrameLayout) findViewById(R.id.camera_frame);
        progress = (CircleProgressBar) findViewById(R.id.video_activity_progress);
        count = (TextView) findViewById(R.id.video_count);
         surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        recordButton.setOnClickListener(recordClickListener);
        continueButton.setOnClickListener(continueClickListener);
        initializeTimer();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putInt("position", position);
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        System.out.println("surface created");
            try {
                initializeRecorder(surfaceHolder.getSurface(), surfaceHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (startRecording){
                mediaRecorder.start();
                recordButton.setImageDrawable(ContextCompat.getDrawable(VideoActivity.this, R.drawable.stop_capture_video_button));
                recording = true;
                stopped = false;
            }

    }

    @Override
    protected void onPause(){
        super.onPause();
        System.out.println("onPause");
        if (mediaRecorder != null) {
            System.out.println("Media recorder was not null");
            if (recording){
                mediaRecorder.stop();
            }
            mediaRecorder.reset(); // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
        }
        if (camera != null) {
            System.out.println("Camera was not null");
            camera.stopPreview();
            camera.setPreviewCallback(null);
            surfaceView.getHolder().removeCallback(this);
            camera.lock();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        System.out.println("surface changed");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        System.out.println("surface destroyed");
    }

    private void initializeRecorder(Surface surface, SurfaceHolder holder) throws IOException {
        mediaRecorder = new MediaRecorder();
        initCameraPreview(holder);
        mediaRecorder.setCamera(camera);

        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);

        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);

        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        file = Application.getVideoCacheFile(this);
        mediaRecorder.setOutputFile(file.getAbsolutePath());
        mediaRecorder.setMaxDuration(10000);
        mediaRecorder.setMaxFileSize(10000000);
        mediaRecorder.setPreviewDisplay(surface);

        mediaController = new MediaController(this);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recorderInitializationSuccessful = true;
    }

    private void initCameraPreview(SurfaceHolder holder) throws IOException {
        camera = Camera.open(facing);
        camera.stopPreview();
        parameters = camera.getParameters();
        setOrientation();
        camera.setPreviewDisplay(holder);
        camera.startPreview();
        camera.unlock();
    }

    private void showVideo(){

//        mediaRecorder.stop();
        shutdown();
        videoFrame.removeView(surfaceView);
        if (videoFrame.findViewById(R.id.camera_video_view) == null){
            System.out.println("Cmera frame wasn null");
            videoFrame.addView(videoView, 0);
        }
        videoView.setMediaController(mediaController);
        if (file.exists()){
            videoView.setVideoURI(Uri.fromFile(file));
            videoView.requestFocus();
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    progress.setVisibility(View.GONE);
                    videoView.seekTo(position);
                    if (position == 0) {
                        videoView.start();
                    } else {
                        videoView.pause();
                    }
                }
            });
        } else {
            try {
                showPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void showPreview() throws IOException {


        videoFrame.removeView(videoView);
        if (videoFrame.findViewById(R.id.preview_view) == null){
            videoFrame.addView(surfaceView, 0);
        }
    }

    private void shutdown(){

        if (mediaRecorder != null) {
            mediaRecorder.reset(); // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
        }
        if (camera != null) {
            camera.lock();
            camera.release();
            camera = null;
        }

    }

    private final View.OnClickListener recordClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (!recording){
                if (stopped) {
                    try {
                        count.setVisibility(View.GONE);
                        timer.cancel();
                        initializeTimer();
                        startRecording = true;
                        showPreview();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    startTimer();
                } else {
                    startTimer();
                    mediaRecorder.start();
                    recordButton.setImageDrawable(ContextCompat.getDrawable(VideoActivity.this, R.drawable.stop_capture_video_button));
                    recording = true;
                    stopped = false;
                }

            } else {
                count.setVisibility(View.GONE);
                timer.cancel();
                currentCount = 10;
                initializeTimer();
                continueButton.setVisibility(View.VISIBLE);
                recordButton.setImageDrawable(ContextCompat.getDrawable(VideoActivity.this, R.drawable.capture_video_button));
                recording = false;
                showVideo();
                stopped = true;

            }

        }
    };

    private void setOrientation(){

        Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        if (display.getRotation() == Surface.ROTATION_0) {
            camera.setDisplayOrientation(90);
            if (facing == Camera.CameraInfo.CAMERA_FACING_BACK){
                parameters.setRotation(90);
                mediaRecorder.setOrientationHint(90);
            } else {
                parameters.setRotation(270);
                mediaRecorder.setOrientationHint(270);
            }

        }

        if (display.getRotation() == Surface.ROTATION_90) {
            camera.setDisplayOrientation(0);
            mediaRecorder.setOrientationHint(0);
            if (facing == Camera.CameraInfo.CAMERA_FACING_BACK){
                parameters.setRotation(0);
            } else {
                parameters.setRotation(0);
            }
        }

        if (display.getRotation() == Surface.ROTATION_180) {
            camera.setDisplayOrientation(90);
            if (facing == Camera.CameraInfo.CAMERA_FACING_BACK){
                parameters.setRotation(90);
                mediaRecorder.setOrientationHint(90);
            } else {
                parameters.setRotation(270);
                mediaRecorder.setOrientationHint(270);
            }
        }

        if (display.getRotation() == Surface.ROTATION_270) {
            camera.setDisplayOrientation(180);
            mediaRecorder.setOrientationHint(180);
            if (facing == Camera.CameraInfo.CAMERA_FACING_BACK){
                parameters.setRotation(180);
            } else {
                parameters.setRotation(180);
            }
        }

        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);

        parameters.set("cam_mode", 1 );

    }

    private final View.OnClickListener continueClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            setResult(RESULT_OK);
            finish();

        }
    };

    private void initializeTimer(){
        currentCount = 10;
        count.setText(String.valueOf(currentCount));
        timer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            //    timelyView.animate(currentCount, currentCount-1).start();
                currentCount--;
                count.setText(String.valueOf(currentCount));
            }

            @Override
            public void onFinish() {
            //     timelyView.setVisibility(View.GONE);
                count.setVisibility(View.GONE);
                recordClickListener.onClick(recordButton);
            }

        };

    }

    private void startTimer(){
        count.setVisibility(View.VISIBLE);
//        timelyView.setVisibility(View.VISIBLE);
//        timelyView.animate(0, currentCount);
        timer.start();

    }
}
