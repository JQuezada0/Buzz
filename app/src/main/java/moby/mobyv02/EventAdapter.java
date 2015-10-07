package moby.mobyv02;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.VideoView;

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
import moby.mobyv02.parse.Event;
import moby.mobyv02.parse.Heart;
import moby.mobyv02.parse.Event;

/**
 * Created by quezadjo on 9/25/2015.
 */
public class EventAdapter extends BaseAdapter {

    private final ArrayList<Event> events = new ArrayList<Event>();
    private final LayoutInflater inflater;
    private final Context context;
    private Activity activity;

    public EventAdapter(ArrayList<Event> events, Context context, Activity activity){
        this.events.addAll(events);
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.activity = activity;
    }


    public void setFeed(ArrayList<Event> events){
        this.events.clear();
        this.events.addAll(events);
        notifyDataSetChanged();
    }

    public void updateEvent(final int position){

        Event.getQuery().getInBackground(events.get(position).getObjectId(), new GetCallback<Event>() {
            @Override
            public void done(Event event, ParseException e) {
                events.set(position, event);
            }
        });
    }

    public void addToFeed(ArrayList<Event> events){
        if (events != null){
            this.events.addAll(events);
            notifyDataSetChanged();
        }

    }

    @Override
    public int getCount() {
        if (events.size() > 0) {
            return events.size() + 1;
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return events.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        if (position == events.size()){
            View v = inflater.inflate(R.layout.loading_fragment, null);
            CircleProgressBar circleProgressBar = (CircleProgressBar) v.findViewById(R.id.login_progressbar);
            circleProgressBar.setColorSchemeResources(R.color.moby_blue);
            return v;
        }
        Event event = events.get(position);
        ViewHolder vh;
        if (convertView == null || convertView.getTag() == null){
            convertView = inflater.inflate(R.layout.feed_event_layout, null);
            vh = new ViewHolder();
            vh.name = (TextView) convertView.findViewById(R.id.event_name);
            vh.locale = (TextView) convertView.findViewById(R.id.event_locale);
            vh.distance = (TextView) convertView.findViewById(R.id.event_distance);
            vh.time = (TextView) convertView.findViewById(R.id.event_time);
            vh.heartButton = (TableRow) convertView.findViewById(R.id.heart_button);
            vh.commentButton = (TableRow) convertView.findViewById(R.id.comment_button);
            vh.chatButton = (TableRow) convertView.findViewById(R.id.chat_button);
            vh.eventText = (TextView) convertView.findViewById(R.id.event_text);
            vh.eventImage = (NetworkImageView) convertView.findViewById(R.id.event_image);
            vh.profileButton = (TableRow) convertView.findViewById(R.id.event_button);
            convertView.setTag(R.string.viewholder_tag, vh);
            convertView.setTag(R.string.event_tag, event);
        } else {
            vh = (ViewHolder) convertView.getTag(R.string.viewholder_tag);
            event = (Event) convertView.getTag(R.string.event_tag);
        }
        vh.name.setText(event.getName());
        vh.eventText.setText(event.getText());
        vh.locale.setText(event.getLocale());
        if (event.getImage() != null)
            vh.eventImage.setImageUrl(event.getImage(), Application.imageLoader);
        return convertView;
    }

    private void setOnPreparedListener(final ViewHolder vh){
        vh.video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                vh.progressBar.setVisibility(View.GONE);
                vh.video.seekTo(100);
            }
        });
    }

    private class ViewHolder {

        TextView name;
        TextView locale;
        TextView distance;
        TextView time;
        TextView eventText;
        NetworkImageView eventImage;
        CircleImageView profileImage;
        TableRow heartButton;
        TableRow commentButton;
        TableRow chatButton;
        TableRow profileButton;
        FrameLayout videoFrame;
        VideoView video;
        CircleProgressBar progressBar;
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
