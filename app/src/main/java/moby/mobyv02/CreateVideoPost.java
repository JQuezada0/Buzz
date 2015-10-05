package moby.mobyv02;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import java.io.File;

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

        takeVideoButton.setOnClickListener(videoClickListener);
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
}
