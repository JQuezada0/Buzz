package moby.mobyv02;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.concurrent.Callable;

import de.hdodenhof.circleimageview.CircleImageView;
import moby.mobyv02.layout.SquareVideoView;
import moby.mobyv02.parse.Event;
import moby.mobyv02.parse.Post;
import moby.mobyv02.parse.Upvote;

/**
 * Created by quezadjo on 9/9/2015.
 */
public class Post_View_Fragment extends Fragment {

    private ParseUser user;
    public BuzzItem item;
    public Callable<Void> updatePost;
    private CircleImageView profilePicture;
    private TextView username;
    private TextView locale;
    private TextView distance;
    private TextView timeStamp;
    private LinearLayout profileButton;
    private FrameLayout frame;
    private Application app;
    private Activity main;
    private SquareVideoView video;
    private MediaController mediaController;
    private FrameLayout buzzItemFrame;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle savedInstanceState){

        View v = null;
        main = getActivity();
        app = (Application) main.getApplication();
        if (item.getType() != null){
            user = item.getUser();
            if (item.getType().equals("status")){

                v = inflater.inflate(R.layout.post_status_layout, null);
                profilePicture = (CircleImageView) v.findViewById(R.id.post_profile_image);
                username = (TextView) v.findViewById(R.id.post_name);
                locale = (TextView) v.findViewById(R.id.post_locale);
                distance = (TextView) v.findViewById(R.id.post_distance);
                timeStamp = (TextView) v.findViewById(R.id.post_time);
                profileButton = (LinearLayout) v.findViewById(R.id.post_profile_button);
                profileButton.setOnClickListener(profileButtonClickListener);
                profilePicture.setOnClickListener(profileButtonClickListener);
                buzzItemFrame = (FrameLayout) v.findViewById(R.id.buzz_item_frame);
                TextView text = (TextView) v.findViewById(R.id.post_status_layout_text);
                text.setText(item.getText());
                text.setOnClickListener(buzzItemClickListener);
            } else if (item.getType().equals("photo")){

                v = inflater.inflate(R.layout.post_photo_layout, null);
                TextView text = (TextView) v.findViewById(R.id.post_photo_layout_text);
                final NetworkImageView image = (NetworkImageView) v.findViewById(R.id.post_photo_layout_image);
                image.setImageUrl(item.getImage(), Application.imageLoader);
                text.setText(item.getText());

                profilePicture = (CircleImageView) v.findViewById(R.id.post_profile_image);
                username = (TextView) v.findViewById(R.id.post_name);
                locale = (TextView) v.findViewById(R.id.post_locale);
                distance = (TextView) v.findViewById(R.id.post_distance);
                timeStamp = (TextView) v.findViewById(R.id.post_time);
                profileButton = (LinearLayout) v.findViewById(R.id.post_profile_button);
                profileButton.setOnClickListener(profileButtonClickListener);
                profilePicture.setOnClickListener(profileButtonClickListener);
                frame = (FrameLayout) v.findViewById(R.id.post_photo_layout_frame);
                buzzItemFrame = (FrameLayout) v.findViewById(R.id.buzz_item_frame);
            } else if (item.getType().equals("video")){

                v = inflater.inflate(R.layout.post_video_layout, null);
                TextView text = (TextView) v.findViewById(R.id.post_photo_layout_text);
                video = (SquareVideoView) v.findViewById(R.id.post_video_layout_video);
                text.setText(item.getText());
                mediaController = new MediaController(main);
                mediaController.setAnchorView(video);
                video.setMediaController(mediaController);
                video.setVideoURI(Uri.parse(item.getVideo()));
                profilePicture = (CircleImageView) v.findViewById(R.id.post_profile_image);
                username = (TextView) v.findViewById(R.id.post_name);
                locale = (TextView) v.findViewById(R.id.post_locale);
                distance = (TextView) v.findViewById(R.id.post_distance);
                timeStamp = (TextView) v.findViewById(R.id.post_time);
                buzzItemFrame = (FrameLayout) v.findViewById(R.id.buzz_item_frame);
                profileButton = (LinearLayout) v.findViewById(R.id.post_profile_button);
                profileButton.setOnClickListener(profileButtonClickListener);
                profilePicture.setOnClickListener(profileButtonClickListener);
            }
            setUserInfo();
        } else {
            v = inflater.inflate(R.layout.event_layout, null);
            username = (TextView) v.findViewById(R.id.event_name);
            locale = (TextView) v.findViewById(R.id.event_locale);
            distance = (TextView) v.findViewById(R.id.event_distance);
            timeStamp = (TextView) v.findViewById(R.id.event_time);
            final NetworkImageView image = (NetworkImageView) v.findViewById(R.id.event_image);
            final TextView text = (TextView) v.findViewById(R.id.event_text);
            username.setText(item.getName());
            locale.setText(item.getLocale());
            distance.setText(item.getFormattedDistance(LocationManager.getLocation()));
            timeStamp.setText(item.getFormattedTime(0));
            buzzItemFrame = (FrameLayout) v.findViewById(R.id.buzz_item_frame);
            if (item.getImage() != null){
                image.setImageUrl(item.getImage(), Application.imageLoader);
            } else {
                image.setDefaultImageResId(R.drawable.event_default);
            }
            text.setText(item.getText());
        }
        buzzItemFrame.setOnClickListener(buzzItemClickListener);
        return v;
    }

    public void hideMediaControls(){
        if (mediaController != null){
            mediaController.hide();
        }
    }

    private final View.OnClickListener buzzItemClickListener = new View.OnClickListener(){


        @Override
        public void onClick(View v) {
            BuzzItemActivity.item = item;
            Intent i = new Intent(main, BuzzItemActivity.class);
            main.startActivity(i);
        }
    };

    private void setUserInfo(){
        if (user.getString("profileImage") == null){
            Bitmap bm = BitmapFactory.decodeResource(main.getResources(), R.drawable.person_icon_graybg);
            Palette.Builder p = Palette.from(bm);
            Palette pl = p.generate();
            int defaultColor = ContextCompat.getColor(app, android.R.color.black);
            if (frame!=null)
                frame.setBackgroundColor(pl.getMutedColor(defaultColor));
            profilePicture.setImageBitmap(bm);
        } else {
            Application.imageLoader.get(user.getString("profileImage"), new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    Bitmap bm = response.getBitmap();
                    if (bm != null) {
                        Palette.Builder p = Palette.from(bm);
                        Palette pl = p.generate();
                        int defaultColor = ContextCompat.getColor(app, android.R.color.black);
                        if (frame != null)
                            frame.setBackgroundColor(pl.getMutedColor(defaultColor));
                        profilePicture.setImageBitmap(bm);

                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
        }
        username.setText(user.getString("fullName"));
        locale.setText(item.getLocale());
        double distanceAwayKm = item.getLocation().distanceInKilometersTo(new ParseGeoPoint(LocationManager.getLocation().getLatitude(), LocationManager.getLocation().getLongitude()));
        DecimalFormat f = new DecimalFormat("##.00");
        String distanceAway = String.valueOf(f.format(distanceAwayKm)) + " km";
        distance.setText(distanceAway);

    }

    private final View.OnClickListener profileButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent i = new Intent(getActivity(), ProfileActivity.class);
            i.putExtra("user", user.getObjectId());
            startActivity(i);
        }
    };

}
