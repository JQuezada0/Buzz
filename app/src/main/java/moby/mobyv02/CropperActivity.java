package moby.mobyv02;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import com.theartofdev.edmodo.cropper.CropImageView;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by quezadjo on 9/14/2015.
 */
public class CropperActivity extends FragmentActivity {

    Button cropButton;
    CropImageView cropImageView;
    private File file;


    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.cropperactivity);
        file = Application.getImageCacheFile(this);
        cropImageView = (CropImageView) findViewById(R.id.crop_Image_view);
        cropImageView.setFixedAspectRatio(true);
        cropImageView.setImageUri(Uri.fromFile(file));
        cropButton = (Button) findViewById(R.id.crop_button);
        cropButton.setOnClickListener(cropClickListener);
    }

    private final View.OnClickListener cropClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            try {
                FileOutputStream fos = new FileOutputStream(file);
                cropImageView.getCroppedImage().compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            finish();
        }

    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        cropImageView.setImageBitmap(BitmapScaler.upscaleBitmap(cropImageView, BitmapDownSampler.getBitmap(cropImageView, file)));
    }

}
