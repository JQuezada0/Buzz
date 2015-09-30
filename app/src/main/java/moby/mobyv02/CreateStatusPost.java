package moby.mobyv02;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.leanplum.Leanplum;
import com.leanplum.activities.LeanplumFragmentActivity;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import moby.mobyv02.parse.Post;

/**
 * Created by quezadjo on 9/8/2015.
 */
public class CreateStatusPost extends FragmentActivity {

    Toolbar toolbar;
    Button cancelButton;
    TextView characterLimitText;
    Button postButton;
    EditText statusText;
    TextView usernameText;
    int characterLimit = 200;
    CircleProgressBar progressBar;
    Typeface roboto;
    private File file;
    private static final int[] colors = new int[]{R.color.turquois, R.color.emerald, R.color.peterriver, R.color.amethyst, R.color.sunflower, R.color.sunflower, R.color.carrot, R.color.alizarin};

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_status);
        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "image.jpg");
        roboto = Typeface.createFromAsset(getAssets(), "font/Roboto-Bold.ttf");
        characterLimitText = (TextView) findViewById(R.id.post_status_character_limit);
        postButton = (Button) findViewById(R.id.create_post_status_button);
        statusText = (EditText) findViewById(R.id.create_post_status_text);
        usernameText = (TextView) findViewById(R.id.create_status_post_fullname);
        cancelButton = (Button) findViewById(R.id.create_post_status_cancel);
        usernameText.setText(ParseUser.getCurrentUser().getString("fullName"));
        progressBar = (CircleProgressBar) findViewById(R.id.post_progressbar);
        progressBar.setColorSchemeResources(android.R.color.holo_blue_bright);
        setStatusTextWatcher();
        postButton.setOnClickListener(createPostClickListener);
        cancelButton.setOnClickListener(cancelPostClickListener);
    }

    @Override
    protected void onResume(){
        super.onResume();
        BuzzAnalytics.logScreen(this, BuzzAnalytics.POST_CATEGORY, "createStatusPost");
    }

    /**
     * @desc Sets a text watcher for the status EditText field. When the character limit is hit,
     * the listener subtracts one character from the field.
     */



    private void setStatusTextWatcher(){

        statusText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                characterLimit = 200 - s.length();
                characterLimitText.setText(String.valueOf(characterLimit));
                if (characterLimit == 0) {
                    statusText.setText(s.subSequence(0, s.length() - 1));
                }
            }

        });
    }

    final View.OnClickListener createPostClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!statusText.getText().toString().isEmpty()){
                postButton.setEnabled(false);
                postButton.setText("Posting...");
                progressBar.setVisibility(View.VISIBLE);
                Bitmap bm = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bm);
                Paint paint = new Paint();
                paint.setStrokeWidth(5);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(500, 500, 500, 500, paint);
                canvas.drawColor(ContextCompat.getColor(CreateStatusPost.this, R.color.moby_blue));
                TextPaint textPaint = new TextPaint();
                textPaint.setTextSize(50);
                textPaint.setTextAlign(Paint.Align.CENTER);
                textPaint.setColor(Color.WHITE);
                textPaint.setTypeface(roboto);
                String statusString = statusText.getText().toString();
                StaticLayout textLayout = new StaticLayout(statusText.getText().toString(), textPaint, canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 1.0f, false);
                canvas.save();
                canvas.translate((textLayout.getWidth() / 2), 250 - (textLayout.getHeight() / 2));
                textLayout.draw(canvas);
                canvas.restore();
                ParseGeoPoint location = LocationManager.getLocation();
                final Post post = new Post();
                post.setType("status");
                post.setUser(ParseUser.getCurrentUser());
                post.setLocation(new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
                post.setText(statusText.getText().toString());
                try {
                    Geocoder geocoder = new Geocoder(CreateStatusPost.this);
                    Address address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);
                    post.setLocale(address.getLocality());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(Application.getImageCacheFile(CreateStatusPost.this));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                ParseOperation.uploadImage(Application.getImageCacheFile(CreateStatusPost.this), new ParseOperation.ImageUploadCallback() {
                    @Override
                    public void finished(boolean success, ParseFile file, ParseException e) {
                        post.put("image", file.getUrl());
                        ParseOperation.savePost(post, new ParseOperation.ParseOperationCallback() {
                            @Override
                            public void finished(boolean success, ParseException e) {
                                if (e == null) {
                                    Intent result = new Intent();
                                    result.putExtra("post", post.getObjectId());
                                    BuzzAnalytics.logPost(CreateStatusPost.this, "status");
                                    CreateStatusPost.this.setResult(RESULT_OK, result);
                                    CreateStatusPost.this.finish();
                                } else {
                                    Toast.makeText(CreateStatusPost.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, CreateStatusPost.this);
                    }
                }, CreateStatusPost.this);
            } else {
                Toast.makeText(CreateStatusPost.this, "Please enter a status", Toast.LENGTH_SHORT).show();
            }
        }
    };

    final View.OnClickListener cancelPostClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CreateStatusPost.this.finish();
        }
    };

    private int getRandomColor(){
        return colors[new Random().nextInt(colors.length)];
    }

}
