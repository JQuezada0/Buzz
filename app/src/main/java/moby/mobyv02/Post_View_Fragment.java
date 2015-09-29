package moby.mobyv02;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.leanplum.Leanplum;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import de.hdodenhof.circleimageview.CircleImageView;
import moby.mobyv02.parse.Post;
import moby.mobyv02.parse.Upvote;

/**
 * Created by quezadjo on 9/9/2015.
 */
public class Post_View_Fragment extends Fragment {

    private ParseUser user;
    public Post post;
    public Callable<Void> updatePost;
    private CircleImageView profilePicture;
    private TextView username;
    private TextView locale;
    private TextView distance;
    private TextView timeStamp;
//    private TextView upvotes;
    private LinearLayout profileButton;
//    private FrameLayout upvoteButton;
//    private ImageView upvoteIcon;
    private TextView usernameSmall;
    private FrameLayout frame;
    private Application app;
    private Activity main;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle savedInstanceState){

        View v = null;
        main = getActivity();
        app = (Application) main.getApplication();
        user = post.getUser();
        if (post.getType().equals("status")){

            v = inflater.inflate(R.layout.post_status_layout, null);
            profilePicture = (CircleImageView) v.findViewById(R.id.post_profile_image);
            username = (TextView) v.findViewById(R.id.post_name);
//            usernameSmall = (TextView) v.findViewById(R.id.post_status_layout_username_small);
            locale = (TextView) v.findViewById(R.id.post_locale);
            distance = (TextView) v.findViewById(R.id.post_distance);
            timeStamp = (TextView) v.findViewById(R.id.post_time);
//            upvotes = (TextView) v.findViewById(R.id.post_status_layout_upvote_count);
            profileButton = (LinearLayout) v.findViewById(R.id.post_profile_button);
//            upvoteButton = (FrameLayout) v.findViewById(R.id.post_status_layout_upvote_button);
            profileButton.setOnClickListener(profileButtonClickListener);
            profilePicture.setOnClickListener(profileButtonClickListener);
//            upvoteIcon = (ImageView) v.findViewById(R.id.post_status_layout_upvote_icon);
//            upvoteButton.setOnClickListener(upvoteButtonClickListener);
            TextView text = (TextView) v.findViewById(R.id.post_status_layout_text);
            text.setText(post.getText());
            setUpvoteStatus();


        } else if (post.getType().equals("photo")){

            v = inflater.inflate(R.layout.post_photo_layout, null);
            TextView text = (TextView) v.findViewById(R.id.post_photo_layout_text);
            final NetworkImageView image = (NetworkImageView) v.findViewById(R.id.post_photo_layout_image);
            image.setImageUrl(post.getImage(), Application.imageLoader);
            text.setText(post.getText());

            profilePicture = (CircleImageView) v.findViewById(R.id.post_profile_image);
            username = (TextView) v.findViewById(R.id.post_name);
//            usernameSmall = (TextView) v.findViewById(R.id.post_photo_layout_username_small);
            locale = (TextView) v.findViewById(R.id.post_locale);
            distance = (TextView) v.findViewById(R.id.post_distance);
            timeStamp = (TextView) v.findViewById(R.id.post_time);
//            upvotes = (TextView) v.findViewById(R.id.post_photo_layout_upvote_count);
            profileButton = (LinearLayout) v.findViewById(R.id.post_profile_button);
//            upvoteButton = (FrameLayout) v.findViewById(R.id.post_photo_layout_upvote_button);
            profileButton.setOnClickListener(profileButtonClickListener);
            profilePicture.setOnClickListener(profileButtonClickListener);
//            upvoteIcon = (ImageView) v.findViewById(R.id.post_photo_layout_upvote_icon);
//            upvoteButton.setOnClickListener(upvoteButtonClickListener);
            setUpvoteStatus();
            frame = (FrameLayout) v.findViewById(R.id.post_photo_layout_frame);
        }
        setUserInfo();
        return v;
    }

    private void setUpvoteStatus(){

//        upvotes.setText(String.valueOf(post.getUpvotes()));
        ParseQuery<Upvote> query = Upvote.getQuery();
        query.whereEqualTo("post", post);
        query.fromLocalDatastore();
        try {
            Upvote upvote = query.getFirst();
//            upvoteIcon.setSelected(true);
        } catch (ParseException e) {

        }

    }

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
//        usernameSmall.setText(user.getUsername());
        locale.setText(post.getLocale());
//        timeStamp.setText(Application.getTimeElapsed(post.getCreatedAt().getTime()));
        double distanceAwayKm = post.getLocation().distanceInKilometersTo(new ParseGeoPoint(LocationManager.getLocation().getLatitude(), LocationManager.getLocation().getLongitude()));
        DecimalFormat f = new DecimalFormat("##.00");
        String distanceAway = String.valueOf(f.format(distanceAwayKm)) + " km";
        distance.setText(distanceAway);

    }

    private final View.OnClickListener profileButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ProfileActivity.user = post.getUser();
            Intent i = new Intent(getActivity(), ProfileActivity.class);
            i.putExtra("self", false);
            startActivity(i);
        }
    };

/*    private final View.OnClickListener upvoteButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (upvoteIcon.isSelected()){
                ParseOperation.deleteUpvote(post, new ParseOperation.ParseOperationCallback() {
                    @Override
                    public void finished(boolean success, ParseException e) {
                        if (success) {
                            Post.getQuery().getInBackground(post.getObjectId(), new GetCallback<Post>() {
                                @Override
                                public void done(Post post, ParseException e) {
                                    try {
                                        updatePost.call();
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                }, main);
                upvotes.setText(String.valueOf(Integer.parseInt(upvotes.getText().toString()) - 1));
                upvoteIcon.setSelected(false);
            } else {
                ParseOperation.createUpvote(post, new ParseOperation.ParseOperationCallback() {
                    @Override
                    public void finished(boolean success, ParseException e) {
                        if (success) {
                            Post.getQuery().getInBackground(post.getObjectId(), new GetCallback<Post>() {
                                @Override
                                public void done(Post post, ParseException e) {
                                    try {
                                        updatePost.call();
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                }, main);
                upvoteIcon.setSelected(true);
                upvotes.setText(String.valueOf(Integer.parseInt(upvotes.getText().toString()) + 1));
            }

        }
    }; */

}
