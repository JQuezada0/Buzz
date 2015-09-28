package moby.mobyv02;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.parse.SignUpCallback;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.leanplum.Leanplum;
import com.leanplum.activities.LeanplumFragmentActivity;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by quezadjo on 9/11/2015.
 */
public class SelectProfilePictureActivity extends LeanplumFragmentActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_profile_picture);

        if (BuildConfig.DEBUG) {
            Leanplum.setAppIdForDevelopmentMode("app_fHaR2B7Xb1mamIGfU4z9FXb50eVY5QeHvPURmpXAFio", "dev_DZYELDJSN3ASeJHFHQUuuCuSf2t4uxOJvw5wUAimw6c");
        } else {
            Leanplum.setAppIdForProductionMode("app_fHaR2B7Xb1mamIGfU4z9FXb50eVY5QeHvPURmpXAFio", "prod_Y0Uw1nzvxdrA8sY4ruuMOt2OI84pdudG3GbpCAqbhwY");
        }

        Leanplum.start(this);


    }
}
