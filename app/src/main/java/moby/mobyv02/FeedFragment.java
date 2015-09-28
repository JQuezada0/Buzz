package moby.mobyv02;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import moby.mobyv02.parse.Heart;
import moby.mobyv02.parse.Post;

/**
 * Created by quezadjo on 9/12/2015.
 */
public class FeedFragment extends Fragment implements AbsListView.OnScrollListener {

//    private ImageView favoriteButton;
//    private ImageView commentButton;
//    private TableRow feedNavigation;
//    private ViewPager viewPager;
    private ListView postList;
    private PostAdapter adapter;
//    private PostsAdapter postsAdapter;
    private Post currentPost;
    private Main main;
//    private TextView commentCount;
    private static int lastPosition = 0;
    private boolean maxReached;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup Container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.feed_fragment, null);
        main = (Main) getActivity();
        postList = (ListView) v.findViewById(R.id.post_list);
        adapter = new PostAdapter(new ArrayList<Post>(), main, main);
        postList.setAdapter(adapter);
//        favoriteButton = (ImageView) v.findViewById(R.id.feed_favorite_button).findViewById(R.id.favorite_button);
//        commentButton = (ImageView) v.findViewById(R.id.feed_comment_button).findViewById(R.id.comment_button);
//        commentCount = (TextView) v.findViewById(R.id.feed_comment_button).findViewById(R.id.comment_count);
//        feedNavigation = (TableRow) v.findViewById(R.id.feed_navigation);
//        viewPager = (ViewPager) v.findViewById(R.id.feed_viewpager);
//        favoriteButton.setOnClickListener(favoriteClickListener);
//        commentButton.setOnClickListener(commentClickListener);
//        postsAdapter = new PostsAdapter(getFragmentManager());
//        viewPager.setOnPageChangeListener(pageChangeListener);
//        viewPager.setAdapter(postsAdapter);
//        feedNavigation.setVisibility(View.GONE);
        return v;
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    private void restoreLastPosition(Bundle savedInstanceState){
        if (savedInstanceState != null){
            lastPosition = savedInstanceState.getInt("lastPosition");
        }
    }

    public void setInitialPosts(ArrayList<Post> posts){
        adapter = new PostAdapter(posts, main, main);
        postList.setAdapter(adapter);
    }

    public void loadFirstPosts(ArrayList<Post> posts){
        adapter.setFeed(posts);
    }

    public void loadPosts(ArrayList<Post> posts){
        adapter.addToFeed(posts);
//        postList.setOnScrollListener(this);
        System.out.println(adapter.getCount());
    }

  /*  private final View.OnClickListener favoriteClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            if (postsAdapter.getCount() > 0) {


                if (!favoriteButton.isSelected()) {

                    ParseOperation.createFavorite(postsAdapter.getPost(viewPager.getCurrentItem()), new ParseOperation.ParseOperationCallback() {
                        @Override
                        public void finished(boolean success, ParseException e) {
                        }
                    }, main);
                    favoriteButton.setSelected(true);

                } else {

                    ParseOperation.deleteFavorite(postsAdapter.getPost(viewPager.getCurrentItem()), new ParseOperation.ParseOperationCallback() {
                        @Override
                        public void finished(boolean success, ParseException e) {
                        }
                    }, main);
                    favoriteButton.setSelected(false);
                }

            }
        }

    };
*/
/*    private final View.OnClickListener commentClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (postsAdapter.getCount() > 0) {
                CommentActivity.currentPost = postsAdapter.getPost(viewPager.getCurrentItem());
                startActivity(new Intent(main, CommentActivity.class));

            }
        }
    };*/

/*    private void updateDisplay(int position){
        lastPosition = position;
        currentPost = postsAdapter.getPost(position);
        setCommentCount(currentPost.getComments());
        ParseQuery<Heart> query = Heart.getQuery();
        query.fromLocalDatastore();
        query.whereEqualTo("post", currentPost);
        query.getFirstInBackground(new GetCallback<Heart>() {
            @Override
            public void done(Heart heart, ParseException e) {
                if (e != null) {
                    favoriteButton.setSelected(false);
                } else {
                    favoriteButton.setSelected(true);
                }
            }
        });
    }*/

 /*   private final ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

        @Override
        public void onPageSelected(int position) {

            updateDisplay(position);

        }

        @Override
        public void onPageScrollStateChanged(int state) {}
    };*/

    /* public void setFeed(List<Post> posts, boolean resume, boolean first){
        feedNavigation.setVisibility(View.VISIBLE);
        if (!first){
            postsAdapter.setPosts(posts);
        }else {
            postsAdapter.setFirstPosts(posts);
            lastPosition = 0;
        }
        viewPager.setAdapter(postsAdapter);
        if (postsAdapter.getCount() <= 0){
            resume = false;
        }
        if (resume){
            viewPager.setCurrentItem(lastPosition);
            updateDisplay(lastPosition);
        } else {
            if (postsAdapter.getCount() > 0)
            updateDisplay(0);
        }
    } */


 /**   private void setCommentCount(int i){
        commentCount.setText(String.valueOf(i));
    }
*/
    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putInt("lastPosition", lastPosition);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onScrollStateChanged(AbsListView absListView, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        if (postList.getLastVisiblePosition() ==  (totalItemCount - 1)){
            main.loadFeed();
        }

    }
}
