package moby.mobyv02;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.leanplum.Leanplum;
import com.leanplum.activities.LeanplumFragmentActivity;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import moby.mobyv02.parse.Follow;
import moby.mobyv02.parse.Post;

/**
 * Created by quezadjo on 9/10/2015.
 */
public class ProfileActivity extends LeanplumFragmentActivity {

    public static ParseUser user;
    CircleImageView profileImage;
    TextView username;
    Button followButton;
    ListView profilePosts;
    boolean self = false;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        if (BuildConfig.DEBUG) {
            Leanplum.setAppIdForDevelopmentMode("app_fHaR2B7Xb1mamIGfU4z9FXb50eVY5QeHvPURmpXAFio", "dev_DZYELDJSN3ASeJHFHQUuuCuSf2t4uxOJvw5wUAimw6c");
        } else {
            Leanplum.setAppIdForProductionMode("app_fHaR2B7Xb1mamIGfU4z9FXb50eVY5QeHvPURmpXAFio", "prod_Y0Uw1nzvxdrA8sY4ruuMOt2OI84pdudG3GbpCAqbhwY");
        }
        Leanplum.start(this);
        profileImage = (CircleImageView) findViewById(R.id.profile_activity_image);
        username = (TextView) findViewById(R.id.profile_activity_name);
        followButton = (Button) findViewById(R.id.profile_activity_follow_button);
        if (getIntent().getExtras() !=null) {
            self = getIntent().getExtras().getBoolean("self");
            if (self) {
                followButton.setVisibility(View.GONE);
                user = ParseUser.getCurrentUser();
            } else {
                setFollowStatus();
            }
        }
        Application.loadImage(profileImage, user.getString("profileImage"));
        username.setText(user.getString("fullName"));
        followButton.setOnClickListener(followClickListener);
        profilePosts = (ListView) findViewById(R.id.post_list);
        setPosts();
    }

    private void setFollowStatus(){
        ParseQuery<Follow> query = Follow.getQuery();
        query.whereEqualTo("fromUser", ParseUser.getCurrentUser());
        query.whereEqualTo("toUser", user);
        query.fromLocalDatastore();
        try {
            System.out.println(query.getFirst());
            followButton.setSelected(true);
            followButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            followButton.setText("Unfollow");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void setPosts(){
        ParseQuery<Post> query = Post.getQuery();
        query.whereEqualTo("user", user);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> list, ParseException e) {
                PostAdapter adapter = new PostAdapter(new ArrayList<Post>(list), ProfileActivity.this, ProfileActivity.this);
                profilePosts.setAdapter(adapter);
            }
        });
    }

    private final View.OnClickListener followClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (!followButton.isSelected()){

                ParseOperation.createFollow(user, new ParseOperation.ParseOperationCallback() {
                    @Override
                    public void finished(boolean success, ParseException e) {
                        if (success){
                            followButton.setText("Unfollow");
                            followButton.setTextColor(ContextCompat.getColor(ProfileActivity.this, android.R.color.white));
                            followButton.setSelected(true);
                        } else {

                        }
                    }
                }, ProfileActivity.this);

            } else {

                ParseOperation.deleteFollow(user, new ParseOperation.ParseOperationCallback() {
                    @Override
                    public void finished(boolean success, ParseException e) {
                        if (success) {
                            followButton.setText("Follow");
                            followButton.setTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.moby_blue));
                            followButton.setSelected(false);
                        } else {
                            Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                }, ProfileActivity.this);
            }

        }
    };

}
