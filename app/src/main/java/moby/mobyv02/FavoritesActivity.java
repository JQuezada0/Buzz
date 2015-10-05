package moby.mobyv02;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import moby.mobyv02.BuildConfig;
import com.leanplum.Leanplum;
import com.leanplum.activities.LeanplumFragmentActivity;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import moby.mobyv02.parse.Heart;
import moby.mobyv02.parse.Post;

/**
 * Created by quezadjo on 9/11/2015.
 */
public class FavoritesActivity extends LeanplumFragmentActivity {

    private ViewPager viewPager;
    private PostsAdapter adapter;
    private ImageView favoriteButton;
    private ImageView commentButton;
    private TextView commentCount;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites_activity);
        if (BuildConfig.DEBUG) {
            Leanplum.setAppIdForDevelopmentMode("app_fHaR2B7Xb1mamIGfU4z9FXb50eVY5QeHvPURmpXAFio", "dev_DZYELDJSN3ASeJHFHQUuuCuSf2t4uxOJvw5wUAimw6c");
        } else {
            Leanplum.setAppIdForProductionMode("app_fHaR2B7Xb1mamIGfU4z9FXb50eVY5QeHvPURmpXAFio", "prod_Y0Uw1nzvxdrA8sY4ruuMOt2OI84pdudG3GbpCAqbhwY");
        }
        Leanplum.start(this);
        adapter = new PostsAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.favorites_viewpager);
        favoriteButton = (ImageView) findViewById(R.id.favorites_favorite_button).findViewById(R.id.favorite_button);
        commentButton = (ImageView) findViewById(R.id.favorites_comment_button).findViewById(R.id.comment_button);
        commentCount = (TextView) findViewById(R.id.favorites_comment_button).findViewById(R.id.comment_count);
        viewPager.setAdapter(adapter);
        getPosts();
    }

    private void getPosts(){
        ParseQuery<Post> query = Post.getQuery();
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> list, ParseException e) {
                adapter.updatePosts(list);
            }
        });
    }

    private final View.OnClickListener favoriteButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (favoriteButton.isSelected()){

                ParseQuery<Heart> query = Heart.getQuery();
                query.whereEqualTo("user", ParseUser.getCurrentUser());
                query.whereEqualTo("post", adapter.getPost(viewPager.getCurrentItem()));
                query.fromLocalDatastore();
                query.getFirstInBackground(new GetCallback<Heart>() {
                    @Override
                    public void done(Heart heart, ParseException e) {
                        heart.deleteEventually();
                        heart.unpinInBackground();
                    }
                });
                favoriteButton.setSelected(false);

            } else {
                Heart heart = new Heart();
                heart.setPost(adapter.getPost(viewPager.getCurrentItem()));
                heart.setUser(ParseUser.getCurrentUser());
                heart.saveEventually();
                heart.pinInBackground();
                favoriteButton.setSelected(true);
            }

        }
    };

    private void setCommentCount(int i){
        commentCount.setText(String.valueOf(i));
    }

    private final ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

            Post p = adapter.getPost(position);
            setCommentCount(p.getComments());
            ParseQuery<Heart> query = Heart.getQuery();
            query.fromLocalDatastore();
            query.whereEqualTo("post", p);
            query.getFirstInBackground(new GetCallback<Heart>() {
                @Override
                public void done(Heart heart, ParseException e) {
                    if (e!=null){
                        favoriteButton.setSelected(false);
                    } else {
                        favoriteButton.setSelected(true);
                    }
                }
            });

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private final View.OnClickListener commentButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {



        }
    };

}
