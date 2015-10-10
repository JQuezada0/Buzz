package moby.mobyv02;

import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import moby.mobyv02.parse.Follow;
import moby.mobyv02.parse.Post;

/**
 * Created by quezadjo on 9/10/2015.
 */
public class ProfileActivity extends FragmentActivity {

    private ParseUser user;
    private CircleImageView profileImage;
    private TextView name;
    private TextView locale;
    private TextView aboutMe;
    private TextView friends;
    private TextView followers;
    private TextView following;
    private TextView posts;
    private TextView comments;
    private TextView hearts;
    private TextView postMap;
    private TextView followText;
    private ListView list;
    private ImageView followIcon;
    private PostAdapter postAdapter;
    private CircleProgressBar progress;
    private LinearLayout profileFrame;

    private TableRow followButton;

    private boolean followingUser = false;

    private Follow follow;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);
        postAdapter = new PostAdapter(new ArrayList<Post>(), this, this);
        profileImage = (CircleImageView) findViewById(R.id.image);
        name = (TextView) findViewById(R.id.name);
        locale = (TextView) findViewById(R.id.locale);
        aboutMe = (TextView) findViewById(R.id.about_me);
        friends = (TextView) findViewById(R.id.friends);
        followers = (TextView) findViewById(R.id.followers);
        following = (TextView) findViewById(R.id.following);
        posts = (TextView) findViewById(R.id.posts);
        comments = (TextView) findViewById(R.id.comments);
        hearts = (TextView) findViewById(R.id.hearts);
        postMap = (TextView) findViewById(R.id.post_map);
        list = (ListView) findViewById(R.id.post_list);
        followButton = (TableRow) findViewById(R.id.follow_button);
        followIcon = (ImageView) findViewById(R.id.follow_icon);
        followText = (TextView) findViewById(R.id.follow_text);
        progress = (CircleProgressBar) findViewById(R.id.progress);
        profileFrame = (LinearLayout) findViewById(R.id.profile_layout);
        progress.setColorSchemeResources(R.color.moby_blue);
        list.setAdapter(postAdapter);
        followButton.setOnClickListener(followClickListener);
        readBehaviour(getIntent().getExtras());
    }

    @Override
    protected void onResume(){
        super.onResume();
        BuzzAnalytics.logScreen(this, BuzzAnalytics.PROFILE_CATEGORY, "profile");
    }

    private void initialize(final CircleImageView profileImage){
        setFollowStatus();
        name.setText(user.getString("fullName"));
        aboutMe.setText(user.getString("aboutMe"));
        friends.setText(String.valueOf(user.getInt("friends")));
        followers.setText(String.valueOf(user.getInt("followers")));
        following.setText(String.valueOf(user.getInt("following")));
        posts.setText(String.valueOf(user.getInt("posts")));
        comments.setText(String.valueOf(user.getInt("comments")));
        hearts.setText(String.valueOf(user.getInt("hearts")));
        String profileImageUrl = user.getString("profileImage");
        if (profileImageUrl != null){
            Application.imageLoader.get(user.getString("profileImage"), new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    if (response.getBitmap() != null){
                        profileImage.setImageBitmap(response.getBitmap());
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    profileImage.setImageResource(R.drawable.person_icon_graybg);
                }
            }, 100, 100);
        } else {
            profileImage.setImageResource(R.drawable.person_icon_graybg);
        }


    }

    private void readBehaviour(Bundle bundle){
        System.out.println("Read Behavior");
        if (bundle.getString("com.parse.Data") != null){
            try {
                JSONObject data = new JSONObject(bundle.getString("com.parse.Data"));
                String objectId = data.getString("user");
                ParseUser user = ParseUser.createWithoutData(ParseUser.class, objectId);
                user.fetchInBackground(new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (e == null){
                            ProfileActivity.this.user = user;
                            initialize(profileImage);
                            loadPosts(postAdapter);
                        } else {
                            Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (bundle.getString("user").equals("self")){
            followButton.setVisibility(View.GONE);
            ProfileActivity.this.user = ParseUser.getCurrentUser();
            initialize(profileImage);
            loadPosts(postAdapter);
        } else {
            System.out.println("Other user profile");
            String objectId = bundle.getString("user");
            System.out.println(objectId);
            ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
            userQuery.getInBackground(objectId, new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser parseUser, ParseException e) {
                    System.out.println("Done");
                    if (e == null) {
                        ProfileActivity.this.user = parseUser;
                        initialize(profileImage);
                        loadPosts(postAdapter);
                    } else {
                        Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }


    }

    private void setFollowStatus(){

        ParseQuery<Follow> userQuery = Follow.getQuery();
        userQuery.fromLocalDatastore();
        userQuery.whereEqualTo("fromUser", ParseUser.getCurrentUser());
        userQuery.whereEqualTo("toUser", user);
        userQuery.findInBackground(new FindCallback<Follow>() {
            @Override
            public void done(List<Follow> list, ParseException e) {
                if (e == null){
                    if (list.size() > 0){
                        setFollowing();
                        follow = list.get(0);
                    } else {
                        followingUser = false;
                    }
                } else {
                    if (e != null){
                        Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        BuzzAnalytics.logError(ProfileActivity.this, e.getMessage());
                    } else {
                        Toast.makeText(ProfileActivity.this, "An error has occurred. Please try again later", Toast.LENGTH_LONG).show();
                        BuzzAnalytics.logError(ProfileActivity.this, "Unknown error has occurred during profile load");
                    }

                }
            }
        });

    }

    private void setFollowing(){
        followingUser = true;
        followText.setText("Unfollow");
        followIcon.setImageResource(R.drawable.minus_icon);
        followButton.setBackgroundColor(ContextCompat.getColor(this, R.color.moby_light_blue));

    }

    private void setNotFollowing(){
        followingUser = false;
        followText.setText("Follow");
        followIcon.setImageResource(R.drawable.plus_icon);
        followButton.setBackgroundColor(ContextCompat.getColor(this, R.color.moby_blue));
    }

    private void loadPosts(final PostAdapter postAdapter){
        System.out.println("Load Posts");
        ParseQuery<Post> query = Post.getQuery();
        query.include("user");
        query.orderByDescending("updatedAt");
        query.whereEqualTo("user", user);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> list, ParseException e) {
                if (e != null) {
                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    postAdapter.setFeed(list);
 //                   user.put("posts", list.size());
 //                   user.saveEventually();
                    progress.setVisibility(View.GONE);
                    profileFrame.setVisibility(View.VISIBLE);
                }

            }
        });

    }

    private final View.OnClickListener followClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (followingUser){
                unFollow();
            } else {
                follow();
            }

        }
    };

    private void follow(){
        followers.setText(String.valueOf(Integer.parseInt(followers.getText().toString()) + 1));
        Follow follow = new Follow();
        follow.setTo(user);
        follow.setFrom(ParseUser.getCurrentUser());
        new ParseOperation("Network").createFollow(user, follow, new ParseOperation.CreateFollowCallback() {
            @Override
            public void finished(boolean success, Follow follow, ParseException e) {
                ProfileActivity.this.follow = follow;
            }
        }, this);
        this.follow = follow;
        setFollowing();
    }

    private void unFollow(){
        followers.setText(String.valueOf(Integer.parseInt(followers.getText().toString()) - 1));
        new ParseOperation("Network").deleteFollow(user, follow, new ParseOperation.CreateFollowCallback() {


            @Override
            public void finished(boolean success, Follow follow, ParseException e) {

            }
        }, this);
        setNotFollowing();
    }

}
