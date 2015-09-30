package moby.mobyv02;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.leanplum.Leanplum;
import com.leanplum.activities.LeanplumFragmentActivity;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import moby.mobyv02.parse.Comment;
import moby.mobyv02.parse.Post;

/**
 * Created by quezadjo on 9/10/2015.
 */
public class CommentActivity extends FragmentActivity {

    public static Post currentPost;
    private CircleProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment_activity);
        progressBar = (CircleProgressBar) findViewById(R.id.comments_progressbar);
        progressBar.setColorSchemeResources(R.color.moby_blue);
        setInitialFragment();
        BuzzAnalytics.logScreen(this, BuzzAnalytics.COMMENT_CATEGORY, "viewComments");

    }

    private void setInitialFragment(){
        Fragment viewCommentsFragment = new Comment_View_Fragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.comment_activity_frame, viewCommentsFragment);
        transaction.commit();
    }

    public void showProgressBar(){
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar(){
        progressBar.setVisibility(View.GONE);
    }

}
