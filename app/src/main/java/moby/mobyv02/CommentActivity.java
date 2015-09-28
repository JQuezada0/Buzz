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
public class CommentActivity extends LeanplumFragmentActivity {

    public static Post currentPost;
    private CircleProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment_activity);
        if (BuildConfig.DEBUG) {
            Leanplum.setAppIdForDevelopmentMode("app_fHaR2B7Xb1mamIGfU4z9FXb50eVY5QeHvPURmpXAFio", "dev_DZYELDJSN3ASeJHFHQUuuCuSf2t4uxOJvw5wUAimw6c");
        } else {
            Leanplum.setAppIdForProductionMode("app_fHaR2B7Xb1mamIGfU4z9FXb50eVY5QeHvPURmpXAFio", "prod_Y0Uw1nzvxdrA8sY4ruuMOt2OI84pdudG3GbpCAqbhwY");
        }
        Leanplum.start(this);
        progressBar = (CircleProgressBar) findViewById(R.id.comments_progressbar);
        progressBar.setColorSchemeResources(R.color.moby_blue);
        setInitialFragment();

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
