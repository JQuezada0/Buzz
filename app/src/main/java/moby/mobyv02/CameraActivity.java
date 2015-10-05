package moby.mobyv02;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by quezadjo on 9/22/2015.
 */
public class CameraActivity extends FragmentActivity implements Camera.PictureCallback, SurfaceHolder.Callback {

    private Camera camera;
    private ImageView image;
    private SurfaceView surfaceView;
    private ImageView captureButton;
    private byte[] data;
    private boolean isCapturing;
    private ImageView cancelButton;
    private ImageView okButton;
    private ImageView toggleCameraButton;
    private boolean defaultCamera = true;
    private int facing = Camera.CameraInfo.CAMERA_FACING_BACK;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.camera_activity);

        image = (ImageView) findViewById(R.id.camera_image_view);
        image.setVisibility(View.INVISIBLE);
        file = Application.getImageCacheFile(this);
        surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        final SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        captureButton = (ImageView) findViewById(R.id.capture_photo_button);
        cancelButton = (ImageView) findViewById(R.id.capture_photo_cancel_button);
        toggleCameraButton = (ImageView) findViewById(R.id.toggle_camera_button);
        okButton = (ImageView) findViewById(R.id.capture_photo_ok_button);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage();
            }
        });
        cancelButton.setOnClickListener(cancelClickListener);
        okButton.setOnClickListener(continueClickListener);
        toggleCameraButton.setOnClickListener(toggleCameraClickListener);
        isCapturing = true;
    }


    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putBoolean("capturing", isCapturing);
        savedInstanceState.putInt("facing", facing);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isCapturing = savedInstanceState.getBoolean("capturing", data == null);
        facing = savedInstanceState.getInt("facing", Camera.CameraInfo.CAMERA_FACING_BACK);
        if (facing == Camera.CameraInfo.CAMERA_FACING_BACK){
            toggleCameraButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.toggle_front_facing_camera_icon));
        } else {
            toggleCameraButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.toggle_back_facing_camera_icon));
        }
        if (camera == null){
            camera = Camera.open(facing);
            try {
                Camera.Parameters parameters = camera.getParameters();
                setOrientation(parameters);
                camera.setParameters(parameters);
                camera.setPreviewDisplay(surfaceView.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (data != null) {
            setupImageDisplay();
        } else {
            setupImageCapture();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (camera == null) {
            try {
                if (facing == Camera.CameraInfo.CAMERA_FACING_BACK){
                    toggleCameraButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.toggle_front_facing_camera_icon));
                } else {
                    toggleCameraButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.toggle_back_facing_camera_icon));
                }
                camera = Camera.open(facing);
                Camera.Parameters parameters = camera.getParameters();
                setOrientation(parameters);
                camera.setParameters(parameters);
                camera.setPreviewDisplay(surfaceView.getHolder());
                if (isCapturing) {
                    camera.startPreview();
                }
            } catch (Exception e) {
                Toast.makeText(CameraActivity.this, "Unable to open camera.", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            setOrientation(parameters);
            try {
                camera.setParameters(parameters);
                camera.setPreviewDisplay(surfaceHolder);
                if (isCapturing) {
                    camera.startPreview();
                }
            } catch (IOException e) {
                Toast.makeText(CameraActivity.this, "Unable to start camera preview.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }


    private void captureImage() {
        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean b, Camera camera) {
                camera.takePicture(null, null, CameraActivity.this);
            }
        });
    }

    private void setupImageCapture() {
        image.setVisibility(View.INVISIBLE);
        surfaceView.setVisibility(View.VISIBLE);
        camera.startPreview();
    }

    private void setupImageDisplay() {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapDownSampler.getBitmap(image, data);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        image.setImageBitmap(bitmap);
        camera.stopPreview();
        surfaceView.setVisibility(View.INVISIBLE);
        image.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPictureTaken(byte[] bytes, Camera camera) {
        data = bytes;
        setupImageDisplay();
        captureButton.setVisibility(View.GONE);
        okButton.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);
        toggleCameraButton.setVisibility(View.GONE);
    }

    private final View.OnClickListener toggleCameraClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            camera.release();
            Camera.Parameters parameters;
            if (defaultCamera){
                facing = Camera.CameraInfo.CAMERA_FACING_FRONT;
                camera = Camera.open(facing);
                defaultCamera = false;
                toggleCameraButton.setImageDrawable(ContextCompat.getDrawable(CameraActivity.this, R.drawable.toggle_back_facing_camera_icon));

            } else {
                facing = Camera.CameraInfo.CAMERA_FACING_BACK;
                camera = Camera.open(facing);
                defaultCamera = true;
                toggleCameraButton.setImageDrawable(ContextCompat.getDrawable(CameraActivity.this, R.drawable.toggle_front_facing_camera_icon));
            }
            parameters = camera.getParameters();
            setOrientation(parameters);
            try {
                camera.setParameters(parameters);
                camera.setPreviewDisplay(surfaceView.getHolder());
                if (isCapturing) {
                    camera.startPreview();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };

    private void setOrientation(Camera.Parameters parameters){

        Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        if (display.getRotation() == Surface.ROTATION_0) {
            camera.setDisplayOrientation(90);
            if (facing == Camera.CameraInfo.CAMERA_FACING_BACK){
                parameters.setRotation(90);
            } else {
                parameters.setRotation(270);
            }

        }

        if (display.getRotation() == Surface.ROTATION_90) {
            camera.setDisplayOrientation(0);
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
            } else {
                parameters.setRotation(270);
            }
        }

        if (display.getRotation() == Surface.ROTATION_270) {
            camera.setDisplayOrientation(180);
            if (facing == Camera.CameraInfo.CAMERA_FACING_BACK){
                parameters.setRotation(180);
            } else {
                parameters.setRotation(180);
            }
        }

//        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

    }

    private final View.OnClickListener cancelClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            setupImageCapture();
            cancelButton.setVisibility(View.GONE);
            okButton.setVisibility(View.GONE);
            captureButton.setVisibility(View.VISIBLE);
            toggleCameraButton.setVisibility(View.VISIBLE);
        }

    };

    private final View.OnClickListener continueClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {

            setResult(RESULT_OK);
            finish();

        }

    };


}
