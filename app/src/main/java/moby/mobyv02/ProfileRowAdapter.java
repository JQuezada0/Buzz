package moby.mobyv02;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Johnil on 10/8/2015.
 */
public class ProfileRowAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<ParseUser> users = new ArrayList<ParseUser>();

    public ProfileRowAdapter(Context context){
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public void setUsers(List<ParseUser> users){
        this.users.clear();
        this.users.addAll(users);
        notifyDataSetChanged();
    }

    public void addUsers(List<ParseUser> users){
        this.users.addAll(users);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder vh;
        ParseUser user = users.get(position);
        if (convertView == null || convertView.getTag() == null){
            convertView = inflater.inflate(R.layout.small_profile_row, parent, false);
            vh = new ViewHolder();
            vh.profileImage = (CircleImageView) convertView.findViewById(R.id.profile_image);
            vh.profileName = (TextView) convertView.findViewById(R.id.profile_name);
            vh.locale = (TextView) convertView.findViewById(R.id.profile_locale);
            vh.postCount = (TextView) convertView.findViewById(R.id.profile_post_count);
            vh.friendCount = (TextView) convertView.findViewById(R.id.profile_friend_count);
            vh.heartCount = (TextView) convertView.findViewById(R.id.profile_heart_count);
            convertView.setTag(R.string.viewholder_tag, vh);
            convertView.setTag(R.string.user_tag, user);
        } else {
            vh = (ViewHolder) convertView.getTag(R.string.viewholder_tag);
            user = (ParseUser) convertView.getTag(R.string.user_tag);
        }
        vh.profileName.setText(user.getString("fullName"));
        setLocale(context, vh, user.getParseGeoPoint("location"));
        vh.postCount.setText(String.valueOf(user.getInt("posts")) + " Posts");
        vh.friendCount.setText(String.valueOf(user.getInt("friends")) + " Friends");
        vh.heartCount.setText(String.valueOf(user.getInt("hearts")) + " Hearts");
        String profileImageString = user.getString("profileImage");
        if (profileImageString != null){
            Application.imageLoader.get(profileImageString, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    if (response.getBitmap() != null){
                        vh.profileImage.setImageBitmap(response.getBitmap());
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    vh.profileImage.setImageResource(R.drawable.person_icon_graybg);
                }
            } ,100, 100);

        } else {
            vh.profileImage.setImageResource(R.drawable.person_icon_graybg);
        }

        return convertView;
    }

    private class ViewHolder {

        CircleImageView profileImage;
        TextView profileName;
        TextView locale;
        TextView postCount;
        TextView friendCount;
        TextView heartCount;

    }

    private void setLocale(final Context c, final ViewHolder vh, final ParseGeoPoint location){

        if (location == null){
            vh.locale.setText("unknown");
            return;
        }

        new AsyncTask<Void, Void, String>(){


            @Override
            protected String doInBackground(Void... params) {
                return  Application.getLocale(c, location);
            }

            @Override
            protected void onPostExecute(String v){

                vh.locale.setText(v);

            }
        }.execute();

    }
}
