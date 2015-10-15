package moby.mobyv02;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.parse.GetCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import moby.mobyv02.parse.Post;

/**
 * Created by quezadjo on 9/8/2015.
 */
public class PostsAdapter extends FragmentStatePagerAdapter {

    ArrayList<BuzzItem> posts = new ArrayList<BuzzItem>();
    private Post_View_Fragment currentPostView;

    public PostsAdapter(FragmentManager fm) {
        super(fm);
    }

    public void updatePosts(List<BuzzItem> posts){
        this.posts.addAll(posts);
        notifyDataSetChanged();
    }

    public void setPosts(List<BuzzItem> posts){
        this.posts.clear();
        updatePosts(posts);
    }

    public void setFirstPosts(List<Post> posts){
        this.posts.clear();
        this.posts.addAll(posts);
        notifyDataSetChanged();
    }

    public Void updatePost(final int position){

        Post.getQuery().getInBackground(posts.get(position).getObjectId(), new GetCallback<Post>() {
            @Override
            public void done(Post post, ParseException e) {
                posts.set(position, post);
            }
        });
        return null;
    }

    public int getPosition(Post p){
        return posts.indexOf(p);
    }

    public BuzzItem getPost(int position){
        return posts.get(position);
    }

    public List<BuzzItem> getPosts(){
        return posts;
    }

    @Override
    public Fragment getItem(final int position) {
        Post_View_Fragment f = new Post_View_Fragment();
        currentPostView = f;
        f.item = posts.get(position);
        final Callable<Void> updatePost = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                return updatePost(position);
            }
        };
        f.updatePost = updatePost;
        return f;
    }

    @Override
    public int getCount() {
        return posts.size();
    }

    public Post_View_Fragment getCurrentPostView(){
        return currentPostView;
    }

}
