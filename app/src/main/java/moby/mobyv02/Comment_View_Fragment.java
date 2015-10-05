package moby.mobyv02;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;

import com.parse.ParseException;

import java.util.List;

import moby.mobyv02.parse.Comment;

/**
 * Created by quezadjo on 9/10/2015.
 */
public class Comment_View_Fragment extends Fragment implements AbsListView.OnScrollListener {

    ListView commentsList;
    Button addCommentButton;
    Button backButton;
    CommentActivity commentActivity;
    private int pageNumber = 1;
    private int maxComments = CommentActivity.currentPost.getComments();
    private CommentAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.comment_view_fragment, null);
        commentActivity = (CommentActivity) getActivity();
        commentsList = (ListView) v.findViewById(R.id.comment_view_fragment_list);
        addCommentButton = (Button) v.findViewById(R.id.comment_view_fragment_addcomment_button);
        backButton = (Button) v.findViewById(R.id.cancel_button);
        addCommentButton.setOnClickListener(addCommentClickListener);
        backButton.setOnClickListener(backClickListener);
        adapter = new CommentAdapter(getActivity());
        commentsList.setAdapter(adapter);
        loadInitialComments();
        return v;

    }

    private void loadInitialComments(){

        ParseOperation.loadInitialComments(new ParseOperation.LoadCommentsCallback() {
            @Override
            public void finished(boolean success, List<Comment> comments, ParseException e) {

                if (success) {
                    adapter.updateComments(comments);
                    loadFirstComments();
                } else {
                    commentActivity.hideProgressBar();
                }


            }
        }, commentActivity);

    }

    private void loadFirstComments(){
        if (adapter.getCount() < maxComments){
            ParseOperation.loadComments(0, new ParseOperation.LoadCommentsCallback() {
                @Override
                public void finished(boolean success, List<Comment> comments, ParseException e) {
                    commentActivity.hideProgressBar();
                    if (success){
                        adapter.setCommentsFirst(comments);
                        commentsList.setOnScrollListener(Comment_View_Fragment.this);
                    } else {
                    }
                }
            }, commentActivity);
        } else {
            commentActivity.hideProgressBar();
        }

    }

    private void loadComments(){

        commentActivity.showProgressBar();
        if (adapter.getCount() < maxComments){
            ParseOperation.loadComments(pageNumber, new ParseOperation.LoadCommentsCallback() {
                @Override
                public void finished(boolean success, List<Comment> comments, ParseException e) {
                    commentActivity.hideProgressBar();
                    if (success)
                        adapter.setCommentsFirst(comments);

                }
            }, commentActivity);
        }

    }

    private final View.OnClickListener addCommentClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Comment_Add_Comment_Fragment addCommentFragment = new Comment_Add_Comment_Fragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.comment_activity_frame, addCommentFragment);
            transaction.addToBackStack("add-comment");
            transaction.commit();
            BuzzAnalytics.logScreen(commentActivity, BuzzAnalytics.COMMENT_CATEGORY, "addComment");
        }
    };

    private final View.OnClickListener backClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            getActivity().finish();
        }
    };

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        if (totalItemCount < maxComments){

            int lastVisiblePosition = commentsList.getLastVisiblePosition();

            if (lastVisiblePosition == (totalItemCount - 1)){

                loadComments();

            }

        }

    }
}
