package moby.mobyv02;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup Container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.feed_fragment, null);
        main = (Main) getActivity();
        postList = (ListView) v.findViewById(R.id.post_list);
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.feed_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.moby_blue);
        adapter = new PostAdapter(new ArrayList<Post>(), main, main);
        eventAdapter = new EventAdapter(new ArrayList<Event>(), main, main);
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

    public void loadPosts(ArrayList<Post> posts, boolean reset){
        adapter.addToFeed(posts);
        postList.setOnScrollListener(this);
        if (reset)
            postList.setAdapter(adapter);
    }

    public void loadEvents(ArrayList<Event> events, boolean reset){
        eventAdapter.addToFeed(events);
        postList.setOnScrollListener(this);
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
        if (postList.getLastVisiblePosition() ==  (totalItemCount - 1)){
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
