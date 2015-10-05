package moby.mobyv02;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import moby.mobyv02.map.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.parse.ParseUser;

import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import moby.mobyv02.parse.Post;

/**
 * Created by quezadjo on 9/10/2015.
 */
public class ClusterRenderer extends DefaultClusterRenderer<Post> implements ClusterManager.OnClusterClickListener<Post>, ClusterManager.OnClusterItemClickListener<Post>{

    private IconGenerator iconGenerator;
    private final Context context;
    private final LayoutInflater inflater;
    private MapFragment mapFragment;
    private GoogleMap map;
    private Projection projection;

    public ClusterRenderer(Context context, GoogleMap map, ClusterManager<Post> clusterManager, MapFragment m) {
        super(context, map, clusterManager);
        iconGenerator = new IconGenerator(context);
        this.context = context;
        inflater = LayoutInflater.from(context);
        mapFragment = m;
        this.map = map;
        iconGenerator.setBackground(new ColorDrawable(0x00000000));
    }

    @Override
    protected void onClusterItemRendered(final Post clusterItem, final Marker marker){

        try {
            View markerIcon = inflater.inflate(moby.mobyv02.R.layout.map_marker_icon, null);
            loadImageAsync(clusterItem.getUser(), marker, iconGenerator, markerIcon, clusterItem, false, 0);
        } catch (IllegalArgumentException e) {
            System.out.println("Caught exception " + e.getMessage());
        }


    }

    @Override
    protected void onClusterRendered(final Cluster<Post> cluster, final Marker marker){
        iconGenerator.setBackground(new ColorDrawable(0x00000000));
        final Post[] parr = new Post[cluster.getSize()];
        cluster.getItems().toArray(parr);
        Post firstPost = parr[0];
        try {
            View markerIcon = inflater.inflate(moby.mobyv02.R.layout.map_marker_icon, null);
            loadImageAsync(firstPost.getUser(), marker, iconGenerator, markerIcon, firstPost, true, cluster.getSize());
        } catch (IllegalArgumentException e) {
            System.out.println("Caught exception " + e.getMessage());
        }

    }


    @Override
    public boolean onClusterClick(Cluster<Post> cluster) {
        final Post[] posts = new Post[cluster.getSize()];
        cluster.getItems().toArray(posts);
        mapFragment.markerClusterClicked(getMarker(cluster), posts[0], cluster.getSize());
        return false;
    }

    @Override
    public boolean onClusterItemClick(Post post) {

        mapFragment.markerClusterItemClicked(post.getMarker(), post);
        return false;
    }

    private void loadImageAsync(ParseUser user, final Marker marker, IconGenerator generator, final View v, final Post post, boolean cluster, int count){

        final CircleImageView profileImage = (CircleImageView) v.findViewById(moby.mobyv02.R.id.map_marker_image);
        TextView countText = (TextView) v.findViewById(moby.mobyv02.R.id.map_marker_icon_count_text);
        String imageUrl = user.getString("profileImage");
        if (!cluster) {
            v.findViewById(moby.mobyv02.R.id.map_marker_icon_count).setVisibility(View.GONE);
        } else {
            countText.setText(String.valueOf(count));
        }
        if (imageUrl == null){
            profileImage.setImageDrawable(ContextCompat.getDrawable(context, moby.mobyv02.R.drawable.person_icon_graybg));
            iconGenerator.setContentView(v);
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()));
        } else {
            Application.imageLoader.get(imageUrl, new ImageLoader.ImageListener() {

                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    projection = map.getProjection();
                    LatLngBounds bounds = projection.getVisibleRegion().latLngBounds;
                    if (response.getBitmap() != null) {
                        try {
                            profileImage.setImageBitmap(response.getBitmap());
                            iconGenerator.setContentView(v);
                            Bitmap bm = iconGenerator.makeIcon();
                            BitmapDescriptor bd = BitmapDescriptorFactory.fromBitmap(bm);
                            marker.setIcon(bd);
                            if (post != null)
                                post.setMarker(marker);
                        } catch (IllegalArgumentException e) {
                            System.out.println("Caught exception " + e.getMessage());
                        }
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }, 80, 80);
        }

    }

    @Override
    public void onClustersChanged(Set<? extends Cluster<Post>> clusters){
        super.onClustersChanged(clusters);
    }

}
