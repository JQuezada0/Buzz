package moby.mobyv02;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.OnMapReadyCallback;
import com.androidmapsextensions.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.ui.IconGenerator;

import de.hdodenhof.circleimageview.CircleImageView;
import moby.mobyv02.parse.Event;

/**
 * Created by Johnil on 10/19/2015.
 */
public class BuzzItemActivity extends AppCompatActivity implements OnMapReadyCallback {

    NetworkImageView image;
    TextView name;
    TextView time;
    TextView text;
    CircleImageView profileImage;
    SupportMapFragment map;
    GoogleMap googleMap;
    public static BuzzItem item;
    private TextView fullEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buzz_item_page);
        name = (TextView) findViewById(R.id.name);
        time = (TextView) findViewById(R.id.time);
        text = (TextView) findViewById(R.id.text);
        image = (NetworkImageView) findViewById(R.id.image);
        profileImage = (CircleImageView) findViewById(R.id.profile_image);
        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        name.setText(item.getName());
        time.setText(item.getFormattedTime().toString());
        text.setText(item.getText());
        if (item.getImage() != null){
           image.setImageUrl(item.getImage(), Application.imageLoader);
        } else if (item.getImage() == null) {
            image.setDefaultImageResId(R.drawable.event_default);
        }
        map.getExtendedMapAsync(this);
        profileImage.setImageResource(R.drawable.person_icon_graybg);
        fullEvent = (TextView) findViewById(R.id.full_event);
        if (item.getType() == null){
            profileImage.setVisibility(View.GONE);
            fullEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(((Event) item).getUrl()));
                    startActivity(i);
                }
            });
        } else {
            setProfileImage();
            fullEvent.setVisibility(View.GONE);
        }
    }

    private void setProfileImage(){
        String image = item.getProfileImage();
        if (image != null){
            Application.imageLoader.get(item.getProfileImage(), new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    if (response.getBitmap() != null){
                        profileImage.setImageBitmap(response.getBitmap());
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                }
            }, 100, 100);
        } else {

        }

    }

    private void setMarker(final GoogleMap map){
        final MarkerOptions options = new MarkerOptions();
        final IconGenerator iconGenerator = new IconGenerator(this);
        iconGenerator.setBackground(new ColorDrawable(0x00000000));
        final View v = getLayoutInflater().inflate(R.layout.map_marker_icon, null);
        final CircleImageView profileImage = (CircleImageView) v.findViewById(R.id.map_marker_image);
        v.findViewById(R.id.map_marker_icon_count).setVisibility(View.GONE);
        options.position(new LatLng(item.getLatitude(), item.getLongitude()));
        String image = this.item.getProfileImage();
        if (image!=null){
            Application.imageLoader.get(item.getProfileImage(), new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    if (response.getBitmap()!= null){
                        profileImage.setImageBitmap(response.getBitmap());
                        iconGenerator.setContentView(v);
                        BitmapDescriptor bd = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon());
                        options.icon(bd);
                        map.addMarker(options);
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }, 100, 100);
        } else {
            Bitmap finalBitmap = BitmapDownSampler.getBitmap(100, 100, getResources(), R.drawable.person_icon_graybg);
            profileImage.setImageBitmap(finalBitmap);
            iconGenerator.setContentView(v);
            BitmapDescriptor bd = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon());
            options.icon(bd);
            map.addMarker(options);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(item.getLatitude(), item.getLongitude()), 15f));
        setMarker(googleMap);
    }
}
