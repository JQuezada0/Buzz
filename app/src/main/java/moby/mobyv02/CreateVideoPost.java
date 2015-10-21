package moby.mobyv02;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import moby.mobyv02.parse.Post;

/**
 * Created by quezadjo on 9/24/2015.
 */
public class CreateVideoPost extends FragmentActivity {

    private Button takeVideoButton;
    private File file;
    private EditText postText;
    private Button postButton;
    private ImageView image;
    private Button cancelButton;
    private static Uri uri;
    private CircleProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_video);
        cancelButton = (Button) findViewById(R.id.create_post_video_cancel);
        image = (ImageView) findViewById(R.id.post_video_image);
        postButton = (Button) findViewById(R.id.create_post_video_submit_button);
        postText = (EditText) findViewById(R.id.create_post_video_text);
        takeVideoButton = (Button) findViewById(R.id.create_video_post_take_video);
        progressBar = (CircleProgressBar) findViewById(R.id.post_progressbar);
        progressBar.setColorSchemeResources(R.color.moby_blue);

        postButton.setOnClickListener(postClickListener);
        takeVideoButton.setOnClickListener(videoClickListener);
        file = Application.getVideoCacheFile(this);
        cancelButton.setOnClickListener(cancelPostClickListener);
    }

    private void captureVideo(){
        Intent takePictureIntent = new Intent(this, VideoActivity.class);
        startActivityForResult(takePictureIntent, 100);
    }

    private final View.OnClickListener videoClickListener = new View.OnClickListener(){


        @Override
        public void onClick(View view) {
            captureVideo();
        }

    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 100 && resultCode == RESULT_OK) {

            if (file.exists()){
                Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Images.Thumbnails.MINI_KIND);
                image.setImageBitmap(thumbnail);
            }

        }

    }

    private final View.OnClickListener postClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (file.exists()){
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setShowProgressText(true);
                try {
                    final ParseFile parseFile = new ParseFile(IOUtils.toByteArray(new FileInputStream(file)));
                    parseFile.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            final Post post = new Post();
                            post.setText(postText.getText().toString());
                            post.setType("video");
                            post.setVideo(parseFile.getUrl());
                            try {
                                Geocoder geocoder = new Geocoder(CreateVideoPost.this);
                                ParseGeoPoint location = LocationManager.getLocation();
                                Address address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);
                                post.setLocale(address.getLocality());
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            ParseGeoPoint l = LocationManager.getLocation();
                            post.setLocation(new ParseGeoPoint(l.getLatitude(), l.getLongitude()));
                            post.setUser(ParseUser.getCurrentUser());
                            progressBar.setShowProgressText(false);
                            new ParseOperation("Network").savePost(post, new ParseOperation.ParseOperationCallback() {
                                @Override
                                public void finished(boolean success, ParseException e) {
                                    if (success) {
                                        Intent result = new Intent();
                                        result.putExtra("post", post.getObjectId());
                                        CreateVideoPost.this.setResult(RESULT_OK, result);
                                        CreateVideoPost.this.finish();
                                    } else {
                                        Toast.makeText(CreateVideoPost.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            }, CreateVideoPost.this);

                        }
                    }, new ProgressCallback() {
                        @Override
                        public void done(Integer progress) {
                            progressBar.setProgress(progress);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    final View.OnClickListener cancelPostClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CreateVideoPost.this.finish();
        }
    };

}
