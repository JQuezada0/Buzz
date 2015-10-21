package moby.mobyv02;

import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import com.nhaarman.listviewanimations.appearance.simple.SwingLeftInAnimationAdapter;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
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
import moby.mobyv02.parse.Friend;
import moby.mobyv02.parse.Post;

/**
 * Created by quezadjo on 9/10/2015.
 */
public class ProfileActivity extends AppCompatActivity {

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

    private TableRow friendButton;

    private boolean friendStatus = false;
    private boolean received = false;

    private Follow follow;
    private Friend friend;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);
        postAdapter = new PostAdapter(new ArrayList<Post>(), this, this);
        SwingLeftInAnimationAdapter animationAdapter = new SwingLeftInAnimationAdapter(postAdapter);
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
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        friendButton = (TableRow) findViewById(R.id.follow_button);
        followIcon = (ImageView) findViewById(R.id.follow_icon);
        followText = (TextView) findViewById(R.id.follow_text);
        progress = (CircleProgressBar) findViewById(R.id.progress);
        profileFrame = (LinearLayout) findViewById(R.id.profile_layout);
        progress.setColorSchemeResources(R.color.moby_blue);
        animationAdapter.setAbsListView(list);
        list.setAdapter(animationAdapter);
        friendButton.setOnClickListener(followClickListener);
        readBehaviour(getIntent().getExtras());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume(){
        super.onResume();
        BuzzAnalytics.logScreen(this, BuzzAnalytics.PROFILE_CATEGORY, "profile");
    }

    private void initialize(final CircleImageView profileImage){
        getFriendStatus();
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
        loadPosts(postAdapter);

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
                        } else {
                            Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (bundle.getString("user").equals("self")){
            friendButton.setVisibility(View.GONE);
            ProfileActivity.this.user = ParseUser.getCurrentUser();
            initialize(profileImage);
            loadPosts(postAdapter);
        } else {
            String objectId = bundle.getString("user");
            if (ParseUser.getCurrentUser() != null){
                if (objectId.equals(ParseUser.getCurrentUser().getObjectId())){
                    friendButton.setVisibility(View.GONE);
                    ProfileActivity.this.user = ParseUser.getCurrentUser();
                    initialize(profileImage);
                    loadPosts(postAdapter);
                    return;
                }
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


    }

    private void getFriendStatus(){
        List<ParseQuery<Friend>> queries = new ArrayList<ParseQuery<Friend>>();
        ParseQuery<Friend> friendQuery = Friend.getQuery();
        friendQuery.whereEqualTo("to", user);
        friendQuery.whereEqualTo("from", ParseUser.getCurrentUser());
        ParseQuery<Friend> friendQueryFrom = Friend.getQuery();
        friendQueryFrom.whereEqualTo("from", user);
        friendQueryFrom.whereEqualTo("to", ParseUser.getCurrentUser());
        queries.add(friendQuery);
        queries.add(friendQueryFrom);
        ParseQuery<Friend> finalQuery = ParseQuery.or(queries);
        finalQuery.findInBackground(new FindCallback<Friend>() {
            @Override
            public void done(List<Friend> list, ParseException e) {
                System.out.println("Query finished");
                if (e == null) {
                    System.out.println("Size of results is " + list.size());
                    if (list.size() > 0) {
                        setFriendStatus(list.get(0));
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });


    }

    private void setFriendStatus(Friend friend){
        this.friend = friend;
        System.out.println(friend.getObjectId());
        if (!friend.getTo().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())){

            if (!friend.getAccepted() && !friend.getRejected()){

                followText.setText("request sent");
                followIcon.setImageResource(R.drawable.minus_icon);
                friendButton.setBackgroundColor(ContextCompat.getColor(this, R.color.moby_light_blue));
                friendStatus = true;

            } else if (!friend.getAccepted() && friend.getRejected()){

                followText.setText("Add friend");
                followIcon.setImageResource(R.drawable.plus_icon);
                friendStatus = false;

            } else if (friend.getAccepted() && !friend.getRejected()){

                followText.setText("Unfriend");
                followIcon.setImageResource(R.drawable.minus_icon);
                friendButton.setBackgroundColor(ContextCompat.getColor(this, R.color.moby_light_blue));
                friendStatus = true;
            }

        } else if (friend.getTo().getObjectId().equals(ParseUser.getCurrentUser().getObjectId()) && !friend.getAccepted()) {
            received = true;
            followText.setText("Accept friend request");
            followIcon.setImageResource(R.drawable.plus_icon);
            friendButton.setBackgroundColor(ContextCompat.getColor(this, R.color.moby_light_blue));
        } else {
            followText.setText("Unfriend");
            followIcon.setImageResource(R.drawable.minus_icon);
            friendButton.setBackgroundColor(ContextCompat.getColor(this, R.color.moby_light_blue));
            friendStatus = true;
        }

    }

    private void addFriend(){
        Friend friend = new Friend();
        friend.setTo(user);
        friend.setFrom(ParseUser.getCurrentUser());
        friend.setAccepted(false);
        friend.setCancelled(false);
        friend.setRejected(false);
        friend.saveInBackground();
        this.friend = friend;
        followText.setText("Request Sent");
        followIcon.setImageResource(R.drawable.minus_icon);
        friendButton.setBackgroundColor(ContextCompat.getColor(this, R.color.moby_light_blue));
        friendStatus = true;
    }

    private void unFriend(){
        System.out.println("unfriending");
        followText.setText("Add Friend");
        followIcon.setImageResource(R.drawable.plus_icon);
        friendButton.setBackgroundColor(ContextCompat.getColor(this, R.color.moby_blue));
        friend.deleteInBackground();
        friendStatus = false;
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
                    if (list.size() < 100){
                        user.put("posts", list.size());
                        user.saveInBackground();
                        posts.setText(String.valueOf(list.size()));
                    }
                    postAdapter.setFeed(list);
                    progress.setVisibility(View.GONE);
                    profileFrame.setVisibility(View.VISIBLE);
                }

            }
        });

    }

    private final View.OnClickListener followClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!received){

                if (friendStatus){
                    unFriend();
                } else {
                    addFriend();
                }

            } else {

                received = false;
                friend.setAccepted(true);
                friend.saveInBackground();
                followText.setText("Unfriend");
                followIcon.setImageResource(R.drawable.minus_icon);
                friendButton.setBackgroundColor(ContextCompat.getColor(ProfileActivity.this, R.color.moby_light_blue));
                friendStatus = true;

            }


        }
    };

}
