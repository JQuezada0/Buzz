package moby.mobyv02;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.nhaarman.listviewanimations.appearance.simple.SwingLeftInAnimationAdapter;

import java.util.ArrayList;

import moby.mobyv02.parse.Event;
import moby.mobyv02.parse.Post;

/**
 * Created by quezadjo on 9/12/2015.
 */
public class FeedFragment extends Fragment implements AbsListView.OnScrollListener, SwipeRefreshLayout.OnRefreshListener {

    private ListView postList;
    private PostAdapter adapter;
    private EventAdapter eventAdapter;
    private Main main;
    private static int lastPosition = 0;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SwingLeftInAnimationAdapter animationAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        main = (Main) getActivity();
        adapter = new PostAdapter(new ArrayList<Post>(), main, main);
        animationAdapter = new SwingLeftInAnimationAdapter(adapter);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup Container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.feed_fragment, null);
        postList = (ListView) v.findViewById(R.id.post_list);
        animationAdapter.setAbsListView(postList);
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.feed_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.moby_blue);
        eventAdapter = new EventAdapter(new ArrayList<Event>(), main, main);
        postList.setAdapter(animationAdapter);
        postList.setOnScrollListener(this);
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

    public void loadPosts(ArrayList<Post> posts, boolean reset){
        if (reset){
            adapter.setFeed(posts);
        } else {
            adapter.addToFeed(posts);
        }
    }

    public void loadEvents(ArrayList<Event> events, boolean reset){
        eventAdapter.addToFeed(events);
        if (reset)
            postList.setAdapter(eventAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putInt("lastPosition", lastPosition);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onScrollStateChanged(AbsListView absListView, int scrollState) {
        adapter.hideMediaControls();
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        adapter.hideMediaControls();
        if ((postList.getLastVisiblePosition() ==  (totalItemCount - 1)) && totalItemCount > 10){
            main.loadFeed(false);
        }

    }

    @Override
    public void onRefresh() {

        main.refreshFeed();

    }

    public void refreshFinished(){

        swipeRefreshLayout.setRefreshing(false);

    }
}
