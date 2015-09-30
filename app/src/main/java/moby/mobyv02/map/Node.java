package moby.mobyv02.map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import moby.mobyv02.Application;
import moby.mobyv02.ClusterRenderer;
import moby.mobyv02.R;
import moby.mobyv02.parse.Post;

/**
 * Created by quezadjo on 9/20/2015.
 */
public class Node {

    private ArrayList<Post> posts = new ArrayList<Post>();
    private Marker marker;
    private int position = 0;
    private final MapTree tree;
    private final IconGenerator iconGenerator;
    private final int index;
    private final LayoutInflater inflater;

    public Node(MapTree tree, int index){
        this.tree = tree;
        this.index = index;
        iconGenerator = new IconGenerator(tree.getContext());
        inflater = LayoutInflater.from(tree.getContext());
    }

    public void traverseForward(){
        if (forwardTraversalPossible()){
            position++;
            updateMarker();
        }
    }

    public void traverseBackward(){
        if (backwardsTraversalPossible()){
            position--;
            updateMarker();
        }
    }

    public boolean forwardTraversalPossible() {
        if (position == (posts.size() - 1)) {
            return false;
        }
        return true;
    }

    public boolean backwardsTraversalPossible() {
        if (position == 0){
            return false;
        }
        return true;
    }

    public Post getCurrentPost(){
        return posts.get(position);
    }

    public List<Post> getPosts(){
        return posts;
    }

    public void updateMarker(){
        if (!tree.getCreation()) {
            System.out.println("Update marker");
            Post post = getCurrentPost();
            iconGenerator.setBackground(new ColorDrawable(0x00000000));
            String image = post.getUser().getString("profileImage");
            if (image!=null){
                Application.imageLoader.get(post.getUser().getString("profileImage"), new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                        Bitmap bm = response.getBitmap();
                        if (bm != null) {
                            updateMarkerImage(bm);
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse.statusCode == 404){
                            updateMarkerImage(tree.missingProfileImage);
                        }
                    }
                });
            } else {
                updateMarkerImage(BitmapFactory.decodeResource(tree.getContext().getResources(), R.drawable.person_icon_graybg));
            }

        }

    }

    public Node createFromPost(Post post){
        Cluster<Post> cluster = isFromCluster(post);
        if (cluster != null){
            posts = new ArrayList<Post>(cluster.getItems());
        }else {
            posts.clear();
            posts.add(post);
            position = 0;
            marker = findMarkerFromPost(post);
        }
        updateMarker();
        return this;
    }

    public Cluster<Post> isFromCluster(Post post){
        ClusterManager<Post> clusterManager = tree.getClusterManager();
        ClusterRenderer clusterRenderer = tree.getClusterRenderer();
        ArrayList<Marker> clusterMarkers = new ArrayList<Marker>(clusterManager.getClusterMarkerCollection().getMarkers());
        ArrayList<Post> clusterPosts;
        for (Marker marker : clusterMarkers){
            Cluster<Post> currentCluster = clusterRenderer.getCluster(marker);
            clusterPosts = new ArrayList<Post>(currentCluster.getItems());
            for (Post p : clusterPosts){
                if (p.getObjectId().equals(post.getObjectId())){
                    this.marker = marker;
                    return currentCluster;
                }
            }
        }
        return null;
    }

    public Marker findMarkerFromPost(Post post){

        ClusterManager<Post> clusterManager = tree.getClusterManager();
        ClusterRenderer clusterRenderer = tree.getClusterRenderer();
        ArrayList<Marker> clusterMarkers = new ArrayList<Marker>(clusterManager.getMarkerCollection().getMarkers());
        for (Marker marker : clusterMarkers){
            Post p = clusterRenderer.getClusterItem(marker);
            if (p.getObjectId().equals(post.getObjectId())){
                return marker;
            }
        }
        return null;

    }

    public void setPosition(int position){
        this.position = position;
        exitNode();
    }

    public Marker getMarker(){
        return marker;
    }

    public void exitNode(){

        Post post = posts.get(0);
        iconGenerator.setBackground(new ColorDrawable(0x00000000));
        String profileImage = post.getUser().getString("profileImage");
        if (profileImage == null){
            updateMarkerImageOnExit(tree.missingProfileImage);
        } else {
            Application.imageLoader.get(post.getUser().getString("profileImage"), new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    Bitmap bm = response.getBitmap();
                    if (bm != null) {
                        updateMarkerImageOnExit(bm);
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error.networkResponse.statusCode == 404) {
                        updateMarkerImageOnExit(tree.missingProfileImage);
                    }
                }
            }, 80, 80);

        }


    }


    private void updateMarkerImage(Bitmap bm){

        View markerIcon = inflater.inflate(R.layout.map_marker_icon, null);
        CircleImageView profileImage = (CircleImageView) markerIcon.findViewById(R.id.map_marker_image);
        TextView count = (TextView) markerIcon.findViewById(R.id.map_marker_icon_count_text);
        profileImage.setImageBitmap(bm);
        if (posts.size() < 2) {
            markerIcon.findViewById(R.id.map_marker_icon_count).setVisibility(View.GONE);
        } else {
            count.setText(String.valueOf(posts.size()));
        }
        iconGenerator.setContentView(markerIcon);
        Bitmap markerIconBitmap = iconGenerator.makeIcon();
        Double width = markerIconBitmap.getWidth() * 1.3;
        Double height = markerIconBitmap.getHeight() * 1.3;
        markerIconBitmap = Bitmap.createScaledBitmap(markerIconBitmap, width.intValue(), height.intValue(), false);
        try {
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(markerIconBitmap));
    } catch (IllegalArgumentException e) {
        System.out.println("Caugh exception " + e.getMessage());
    }
    }

    private void updateMarkerImageOnExit(Bitmap bm){

            View markerIcon = inflater.inflate(R.layout.map_marker_icon, null);
            CircleImageView profileImage = (CircleImageView) markerIcon.findViewById(R.id.map_marker_image);
            TextView count = (TextView) markerIcon.findViewById(R.id.map_marker_icon_count_text);
            profileImage.setImageBitmap(bm);
            if (posts.size() < 2) {
                markerIcon.findViewById(R.id.map_marker_icon_count).setVisibility(View.GONE);
            } else {
                count.setText(String.valueOf(posts.size()));
            }
            iconGenerator.setContentView(markerIcon);
        try {
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()));
        } catch (IllegalArgumentException e) {
            System.out.println("Caugh exception " + e.getMessage());
        }

    }
}
