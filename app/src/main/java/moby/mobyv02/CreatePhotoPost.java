package moby.mobyv02;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.leanplum.activities.LeanplumFragmentActivity;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import moby.mobyv02.parse.Post;

/**
 * Created by quezadjo on 9/10/2015.
 */
public class CreatePhotoPost extends LeanplumFragmentActivity {

    private File file;
    private EditText postText;
    private Button postButton;
    private ImageView image;
    private Button takeImage;
    private Button cancelButton;
    private static Uri uri;
    private CircleProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_photo);
        file = Application.getImageCacheFile(this);
        uri = Uri.fromFile(file);
        cancelButton = (Button) findViewById(R.id.cancel_button);
        image = (ImageView) findViewById(R.id.post_photo_image);
        postButton = (Button) findViewById(R.id.create_post_photo_submit_button);
        postText = (EditText) findViewById(R.id.create_post_photo_text);
        takeImage = (Button) findViewById(R.id.create_post_photo_take_image);
        progressBar = (CircleProgressBar) findViewById(R.id.post_progressbar);
        progressBar.setColorSchemeResources(R.color.moby_blue);
        postButton.setOnClickListener(submitClickListener);
        takeImage.setOnClickListener(photoClickListener);
        cancelButton.setOnClickListener(cancelClickListener);

    }

    @Override
    protected void onResume(){
        super.onResume();
        BuzzAnalytics.logScreen(this, BuzzAnalytics.POST_CATEGORY, "createPhotoPost");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);

    }

    private void captureImage(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 100);
        }
    }

    private final View.OnClickListener photoClickListener = new View.OnClickListener(){


        @Override
        public void onClick(View view) {
            captureImage();
        }

    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Intent i = new Intent(this, CropperActivity.class);
            startActivityForResult(i, 200);
        } else if (requestCode == 200 && resultCode == RESULT_OK){
            image.setImageURI(Uri.fromFile(file));
        }

    }

    private final View.OnClickListener cancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            setResult(RESULT_CANCELED);
            finish();

        }
    };

    private final View.OnClickListener submitClickListener = new View.OnClickListener(){


        @Override
        public void onClick(View view) {
            postButton.setEnabled(false);
            postButton.setText("Posting");
            progressBar.setVisibility(View.VISIBLE);
            final ParseGeoPoint location = LocationManager.getLocation();
            try {
                FileInputStream fis = new FileInputStream(file);
                new ParseOperation("Network").uploadImage(file, new ParseOperation.ImageUploadCallback() {
                    @Override
                    public void finished(boolean success, ParseFile file, ParseException e) {

                        final Post post = new Post();
                        post.setText(postText.getText().toString());
                        post.setType("photo");
                        post.setImage(file.getUrl());
                        try {
                            Geocoder geocoder = new Geocoder(CreatePhotoPost.this);
                            Address address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);
                            post.setLocale(address.getLocality());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        ParseGeoPoint l = LocationManager.getLocation();
                        post.setLocation(new ParseGeoPoint(l.getLatitude(), l.getLongitude()));
                        post.setUser(ParseUser.getCurrentUser());
                        new ParseOperation("Network").savePost(post, new ParseOperation.ParseOperationCallback() {
                            @Override
                            public void finished(boolean success, ParseException e) {

                                if (success) {
                                    BuzzAnalytics.logPost(CreatePhotoPost.this, "photo");
                                    Intent result = new Intent();
                                    result.putExtra("post", post.getObjectId());
                                    CreatePhotoPost.this.setResult(RESULT_OK, result);
                                    CreatePhotoPost.this.finish();

                                } else {
                                    Toast.makeText(CreatePhotoPost.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        }, CreatePhotoPost.this);
                    }
                }, CreatePhotoPost.this);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    };


}
