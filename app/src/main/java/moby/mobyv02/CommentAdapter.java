package moby.mobyv02;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import moby.mobyv02.parse.Comment;

/**
 * Created by quezadjo on 9/10/2015.
 */
public class CommentAdapter extends BaseAdapter {

    List<Comment> comments = new ArrayList<Comment>();
    LayoutInflater inflater;

    public CommentAdapter(Context c){
        inflater = LayoutInflater.from(c);
    }

    public void updateComments(List<Comment> comments){
        this.comments.addAll(comments);
        notifyDataSetChanged();
    }

    public void setCommentsFirst(List<Comment> comments){
        if (comments.size() >=20) {
            for (int x = 0; x < 20; x++) {
                this.comments.set(x, comments.get(x));
            }
            updateComments(comments.subList(20, comments.size()));
        } else {
            updateComments(comments);
        }
    }

    @Override
    public int getCount() {
        return comments.size();
    }

    @Override
    public Object getItem(int i) {
        return comments.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder vh;
        Comment comment = comments.get(i);
        if (convertView == null || convertView.getTag() == null){
            convertView = inflater.inflate(R.layout.comment_view_layout, null);
            vh = new ViewHolder();
            vh.profileImage = (CircleImageView) convertView.findViewById(R.id.comment_view_layout_profile_image);
            vh.username = (TextView) convertView.findViewById(R.id.comment_view_layout_username);
            vh.text = (TextView) convertView.findViewById(R.id.comment_view_layout_text);
            convertView.setTag(vh);
        }else {
            vh = (ViewHolder) convertView.getTag();
        }
        vh.profileImage.setImageUrl(comment.getUser().getString("profileImage"), Application.imageLoader);
        vh.username.setText(comment.getUser().getUsername());
        vh.text.setText(comment.getText());
        System.out.println("getView on Comments called");
        return convertView;
    }

    private class ViewHolder {

        CircleImageView profileImage;
        TextView username;
        TextView text;

    }
}
