package moby.mobyv02;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import moby.mobyv02.layout.SquareImageView;
import moby.mobyv02.parse.Heart;
import moby.mobyv02.parse.Post;
import moby.mobyv02.parse.Upvote;

/**
 * Created by quezadjo on 9/25/2015.
 */
public class PostAdapter extends BaseAdapter {

    private final ArrayList<Post> posts = new ArrayList<Post>();
    private final LayoutInflater inflater;
    private final Context context;
    private Activity activity;

    public PostAdapter(ArrayList<Post> posts, Context context, Activity activity){
        this.posts.addAll(posts);
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.activity = activity;
    }


    public void setFeed(ArrayList<Post> posts){
        this.posts.clear();
        this.posts.addAll(posts);
        notifyDataSetChanged();
    }

    public void updatePost(final int position){

        Post.getQuery().getInBackground(posts.get(position).getObjectId(), new GetCallback<Post>() {
            @Override
            public void done(Post post, ParseException e) {
                posts.set(position, post);
            }
        });
    }

    public void addToFeed(ArrayList<Post> posts){
        this.posts.addAll(posts);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (posts.size() > 0) {
            return posts.size() + 1;
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return posts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        if (position == posts.size()){
            View v = inflater.inflate(R.layout.loading_fragment, null);
            CircleProgressBar circleProgressBar = (CircleProgressBar) v.findViewById(R.id.login_progressbar);
            circleProgressBar.setColorSchemeResources(R.color.moby_blue);
            return v;
        }
        final Post post = posts.get(position);
        final ViewHolder vh;
        final ParseUser user = post.getUser();
        if (convertView == null || convertView.getTag() == null){
            convertView = inflater.inflate(R.layout.feed_post_layout, null);
            vh = new ViewHolder();
            vh.name = (TextView) convertView.findViewById(R.id.post_name);
            vh.locale = (TextView) convertView.findViewById(R.id.post_locale);
            vh.distance = (TextView) convertView.findViewById(R.id.post_distance);
            vh.time = (TextView) convertView.findViewById(R.id.post_time);
            vh.heartCount = (TextView) convertView.findViewById(R.id.post_heart_count);
            vh.commentCount = (TextView) convertView.findViewById(R.id.post_comment_count);
            vh.profileImage = (CircleImageView) convertView.findViewById(R.id.post_profile_image);
            vh.heartButton = (TableRow) convertView.findViewById(R.id.heart_button);
            vh.commentButton = (TableRow) convertView.findViewById(R.id.comment_button);
            vh.chatButton = (TableRow) convertView.findViewById(R.id.chat_button);
            vh.postText = (TextView) convertView.findViewById(R.id.post_text);
            vh.postImage = (NetworkImageView) convertView.findViewById(R.id.post_image);
            vh.profileButton = (TableRow) convertView.findViewById(R.id.post_profile_button);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        vh.name.setText(user.getString("fullName"));
        vh.locale.setText(post.getLocale());
        vh.distance.setText(post.getFormattedDistance(LocationManager.getLocation()));
        if (post.getCreatedAt() == null){
            Date d = (Date) post.get("createdDate");
            vh.time.setText(post.getFormattedTime(d.getTime()));
        } else {
            vh.time.setText(post.getFormattedTime(post.getCreatedAt().getTime()));
        }
        if (post.getHearts() > 0) {
            vh.commentCount.setVisibility(View.VISIBLE);
            vh.heartCount.setText(post.getHearts() + " hearts");
        } else {
            vh.heartCount.setVisibility(View.GONE);
        }
        if (post.getComments() > 0){
            vh.commentCount.setVisibility(View.VISIBLE);
            vh.commentCount.setText(post.getComments() + " comments");
        } else {
            vh.commentCount.setVisibility(View.GONE);
        }

        Application.loadImage(vh.profileImage, user.getString("profileImage"));
        String type = post.getType();
        if (type.equals("status")){
            vh.postText.setText(post.getText());
            vh.postImage.setVisibility(View.GONE);
        } else if (type.equals("photo")){
            vh.postImage.setVisibility(View.VISIBLE);
            vh.postImage.setImageUrl(post.getString("image"), Application.imageLoader);
            vh.postText.setText(post.getText());
        }
        ParseQuery<Heart> query = Heart.getQuery();
        query.whereEqualTo("post", post);
        query.fromLocalDatastore();
        try {
            query.getFirst();
            vh.heartButton.setSelected(true);
        } catch (ParseException e) {
            vh.heartButton.setSelected(false);
        }
        vh.heartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!vh.heartButton.isSelected()){
                    vh.heartCount.setVisibility(View.VISIBLE);
                    if ((post.getHearts() + 1) == 1){
                        vh.heartCount.setText(String.valueOf(post.getHearts() + 1) + " heart");
                    } else {
                        vh.heartCount.setText(String.valueOf(post.getHearts() + 1) + " hearts");
                    }
                    vh.heartButton.setSelected(true);
                    ParseOperation.createHeart(post, new ParseOperation.ParseOperationCallback() {
                        @Override
                        public void finished(boolean success, ParseException e) {
                            updatePost(position);
                        }
                    }, activity);
                } else {
                    if ((post.getHearts() - 1) < 1){
                        vh.heartCount.setVisibility(View.GONE);
                    }
                    if ((post.getHearts() - 1) == 1){
                        vh.heartCount.setText(String.valueOf(post.getHearts() - 1) + " heart");
                    } else {
                        vh.heartCount.setText(String.valueOf(post.getHearts() - 1) + " hearts");
                    }
                    vh.heartButton.setSelected(false);
                    ParseOperation.deleteHeart(post, new ParseOperation.ParseOperationCallback() {
                        @Override
                        public void finished(boolean success, ParseException e) {
                            updatePost(position);
                        }
                    }, activity);
                }

            }
        });
        vh.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommentActivity.currentPost = posts.get(position);
                Intent i = new Intent(context, CommentActivity.class);
                context.startActivity(i);
            }
        });
        vh.profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Is clicekd");
                Intent i = new Intent(context, ProfileActivity.class);
                ProfileActivity.user = user;
                context.startActivity(i);
            }
        });
        return convertView;
    }

    private class ViewHolder {

        TextView name;
        TextView locale;
        TextView distance;
        TextView time;
        TextView heartCount;
        TextView commentCount;
        TextView postText;
        NetworkImageView postImage;
        CircleImageView profileImage;
        TableRow heartButton;
        TableRow commentButton;
        TableRow chatButton;
        TableRow profileButton;
    }

    private class ImageListener implements ImageLoader.ImageListener {

        private CircleImageView cv;
        private Resources resources;

        public ImageListener(CircleImageView v, Context context){
            cv = v;
            resources = context.getResources();
        }


        @Override
        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
            Bitmap bm = response.getBitmap();
            if (bm != null){
                cv.setImageBitmap(bm);
            }
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            System.out.println("Volley error " + error.networkResponse.statusCode);
            cv.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.person_icon_graybg));
        }
    }
}
