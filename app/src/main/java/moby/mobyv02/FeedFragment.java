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

    private ListView postList;
    private PostAdapter adapter;
    private Main main;
    private static int lastPosition = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup Container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.feed_fragment, null);
        main = (Main) getActivity();
        postList = (ListView) v.findViewById(R.id.post_list);
        adapter = new PostAdapter(new ArrayList<Post>(), main, main);
        postList.setAdapter(adapter);
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
        postList.setOnScrollListener(this);
    }

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
            System.out.println("Load feed");
        }

    }
}
