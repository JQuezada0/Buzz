package moby.mobyv02;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseSession;
import com.parse.ParseUser;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by quezadjo on 9/10/2015.
 */
public class DrawerAdapter extends BaseAdapter {

    private Context c;
    private String[] items = new String[]{ParseUser.getCurrentUser().getString("fullName"), "News Feed", "Map", "Discovery", "Friends"};
    private Main main;

    public DrawerAdapter(Context c, Main m){
        this.c = c;
        main = m;
    }

    public AdapterView.OnItemClickListener getClickListener(){
        return itemClickListener;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int i) {
        return items[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(c).inflate(R.layout.drawer_item, null);
        TextView tv = (TextView) view.findViewById(R.id.drawer_item_text);
        ImageView iv = (ImageView) view.findViewById(R.id.drawer_item_image);
        final CircleImageView cv = (CircleImageView) view.findViewById(R.id.drawer_item_profile);
        if (i == 0){
            tv.setText(items[0]);
            iv.setVisibility(View.GONE);
            cv.setVisibility(View.VISIBLE);
            String profileImage = ParseUser.getCurrentUser().getString("profileImage");
            if (profileImage == null){
                cv.setImageDrawable(ContextCompat.getDrawable(c, R.drawable.person_icon_graybg));
            } else {
                Application.imageLoader.get(profileImage, new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                        Bitmap bm = response.getBitmap();
                        if (bm != null){
                            cv.setImageBitmap(bm);
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        cv.setImageDrawable(ContextCompat.getDrawable(c, R.drawable.person_icon_graybg));
                    }
                }, 100, 100);
            }
            return view;
        }
        tv.setText(items[i]);
        switch (i){
            case 1:
                iv.setImageDrawable(ContextCompat.getDrawable(c, R.drawable.moby_feed_icon));
                break;
            case 2:
                iv.setImageDrawable(ContextCompat.getDrawable(c, R.drawable.moby_map_icon));
                break;
            case 3:
                iv.setImageDrawable(ContextCompat.getDrawable(c, R.drawable.moby_people_icon));
                break;
            case 4:
                iv.setImageDrawable(ContextCompat.getDrawable(c, R.drawable.people_icon));
                break;
            case 5:
                iv.setImageDrawable(ContextCompat.getDrawable(c, R.drawable.nearby_events_icon));
                break;
        }
        return view;
    }


    private final View.OnClickListener deleteAccountClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            ParseUser.logOut();
            ParseQuery<ParseSession> query = ParseQuery.getQuery(ParseSession.class);
            query.whereEqualTo("user", ParseUser.getCurrentUser());
            query.getFirstInBackground(new GetCallback<ParseSession>() {
                @Override
                public void done(ParseSession parseSession, ParseException e) {
                    if (e==null){
                        parseSession.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null){
                                    System.exit(0);
                                } else {
                                    System.out.println(e.getMessage() + " Error logging out");
                                }
                            }
                        });
                    } else {
                        System.out.println(e.getMessage());
                    }

                }
            });
        }
    };

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener(){


        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            switch (position){

                case 1:
                    main.closeDrawer();
                    main.toggleFeed();
                    break;
                case 2:
                    main.closeDrawer();
                    main.toggleMap();
                    break;
                case 0:
                    main.closeDrawer();
                    Intent i = new Intent(DrawerAdapter.this.c, ProfileActivity.class);
                    i.putExtra("user", "self");
                    DrawerAdapter.this.c.startActivity(i);
                    break;
                case 3:
                    main.closeDrawer();
                    Intent peopleIntent = new Intent(DrawerAdapter.this.c, PeopleActivity.class);
                    DrawerAdapter.this.c.startActivity(peopleIntent);
                    break;
                case 4:
                    main.closeDrawer();
                    Intent friendIntent = new Intent(DrawerAdapter.this.c, FriendsActivity.class);
                    DrawerAdapter.this.c.startActivity(friendIntent);
                    break;
            }
        }
    };
}
