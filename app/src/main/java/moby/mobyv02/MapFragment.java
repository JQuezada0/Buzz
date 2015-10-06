package moby.mobyv02;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.ui.IconGenerator;
import com.nineoldandroids.animation.Animator;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import moby.mobyv02.layout.GestureFrameLayout;
import moby.mobyv02.map.MapTree;
import moby.mobyv02.parse.Post;

/**
 * Created by quezadjo on 9/10/2015.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap map;
    private ClusterManager<Post> clusterManager;
    SupportMapFragment mapFragment;
    public static MapFragment currentMapFragment;
    private ViewPager viewPager;
    private Main main;
    private PostsAdapter postsAdapter;
    public List<Post> currentPosts = new ArrayList<Post>();
    private GestureFrameLayout feedFrame;
    private IconGenerator iconGenerator;
    private LayoutInflater inflater;
    ClusterRenderer clusterRenderer;
    private int lastPosition = 0;
    private MapTree mapTree;
    private ImageView fireworkAnimationImage;
    private Projection projection;
    private FrameLayout mapFrameLayout;
    private View successDialog;
    private Button successDialogContinueButton;
    private ImageView darkOverlay;
    private View temporaryMarker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        main = (Main) getActivity();
        currentMapFragment = this;
        if (MapAdapter.googleMap == null) {
            this.inflater = inflater;
            View v = inflater.inflate(R.layout.map, null);
            this.inflater = inflater;
            mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            feedFrame = (GestureFrameLayout) v.findViewById(R.id.map_feed_frame);
            mapFrameLayout = (FrameLayout) v.findViewById(R.id.map_frame_layout);
            viewPager = (ViewPager) v.findViewById(R.id.map_viewpager);
            mapFrameLayout = (FrameLayout) v.findViewById(R.id.map_frame_layout);
            successDialogContinueButton = (Button) v.findViewById(R.id.success_dialog_continue_button);
            successDialogContinueButton.setOnClickListener(continueOnClickListener);
            darkOverlay = (ImageView) v.findViewById(R.id.map_dark_overlay);
            feedFrame.setVisibility(View.GONE);
            postsAdapter = new PostsAdapter(getFragmentManager());
            viewPager.addOnPageChangeListener(pageChangeListener);
            mapFragment.getMapAsync(this);
            MapAdapter.googleMap = v;
            successDialog = v.findViewById(R.id.success_dialog);
            setGestureDetector();
            iconGenerator = new IconGenerator(main);
            Display display = main.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            return v;
        } else {
            return MapAdapter.googleMap;
        }
    }

    private void setGestureDetector(){
        feedFrame.setGestureDetector(new FeedGestureDetector());
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(LocationManager.getLocation().getLatitude(), LocationManager.getLocation().getLongitude()), 10.0f));
        clusterManager = new ClusterManager<Post>(getContext(), googleMap);
        clusterRenderer = new ClusterRenderer(getContext(), googleMap, clusterManager, this);
        clusterManager.setRenderer(clusterRenderer);
        googleMap.setOnCameraChangeListener(clusterManager);
        googleMap.setInfoWindowAdapter(clusterManager.getMarkerManager());
        clusterManager.setOnClusterClickListener(clusterRenderer);
        clusterManager.setOnClusterItemClickListener(clusterRenderer);
        clusterManager.getClusterMarkerCollection().setOnInfoWindowAdapter(new MapWindowAdapter(main));
        clusterManager.getMarkerCollection().setOnInfoWindowAdapter(new MapWindowAdapter(main));
        googleMap.setOnMarkerClickListener(clusterManager);
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                hideFeed();
            }
        });

    }

    public void updateMap(){
        if (map!= null){
            map.clear();
            clusterManager.clearItems();
            clusterManager.addItems(currentPosts);
            clusterManager.cluster();
        }
    }

    public void clearMap(){

    }

    public void setFeed(List<Post> posts){
        currentPosts.clear();
        currentPosts.addAll(posts);
        updateMap();
    }

    public void markerClusterClicked(Marker marker, Post post, int clusterSize){
        if (mapTree != null){
            mapTree.resetAllNodes();
        }
        mapTree = MapTree.createTree(main, clusterManager, clusterRenderer, map, new ArrayList<Post>(currentPosts), post);
        postsAdapter.setPosts(mapTree.getNewPosts());
        viewPager.setAdapter(postsAdapter);
        feedFrame.setVisibility(View.VISIBLE);
        lastPosition = 0;
    }

    public void markerClusterItemClicked(Marker marker, Post post){
        if (mapTree != null){
            mapTree.resetAllNodes();
        }
        mapTree = MapTree.createTree(main, clusterManager, clusterRenderer, map, new ArrayList<Post>(currentPosts), post);
        postsAdapter.setPosts(mapTree.getNewPosts());
        viewPager.setAdapter(postsAdapter);
        feedFrame.setVisibility(View.VISIBLE);
        lastPosition = 0;
    }

    private final ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (position > lastPosition) {
                mapTree.traverseForward();
            } else {
                    mapTree.traverseBackward();
            }
            lastPosition = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private class FeedGestureDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            if (velocityY > 2000){
                feedFrame.setVisibility(View.GONE);
            }
            return true;
        }

    }

    public class MapWindowAdapter extends InfoWindowAdapter {
        private Context context = null;

        public MapWindowAdapter(Context c) {
            super(c);
            context = c;
        }


        @Override
        public View getInfoContents(Marker marker) {
            View v = ((Activity) context).getLayoutInflater().inflate(R.layout.empty_info_window, null);
            return v;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }
    }

    public void hideFeed(){
        if (feedFrame!= null)
            feedFrame.setVisibility(View.GONE);
    }

    public void animateNewMarker(final Post post){
        View v = inflater.inflate(R.layout.map_marker_icon, null);
        darkOverlay.setVisibility(View.VISIBLE);
        final CircleImageView profileImage = (CircleImageView) v.findViewById(R.id.map_marker_image);
        v.findViewById(R.id.map_marker_icon_count).setVisibility(View.GONE);
        String imageUrl = post.getUser().getString("profileImage");
        mapFrameLayout.addView(v);
        Projection p = map.getProjection();
        Point point = p.toScreenLocation(new LatLng(post.getLatitude(), post.getLongitude()));
        v.setX(point.x);
        v.setY(point.y);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(post.getLatitude(), post.getLongitude()), 10.0f));
        map.getUiSettings().setAllGesturesEnabled(false);
        temporaryMarker = v;
        if (imageUrl == null){
            profileImage.setImageResource(R.drawable.person_icon_graybg);
        } else {
            Application.imageLoader.get(imageUrl, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    Bitmap bm = response.getBitmap();
                    if (bm != null){
                        profileImage.setImageBitmap(bm);
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    profileImage.setImageResource(R.drawable.person_icon_graybg);
                }
            }, 100, 100);
        }
    }

    public void animateNewMarkerOnPost(final Post post){
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(post.getLatitude(), post.getLongitude()), 10.0f));
        successDialog.setVisibility(View.VISIBLE);
        showSuccessDialog();
        darkOverlay.setVisibility(View.VISIBLE);
        View v = inflater.inflate(R.layout.map_marker_icon, null);
        final CircleImageView profileImage = (CircleImageView) v.findViewById(R.id.map_marker_image);
        v.findViewById(R.id.map_marker_icon_count).setVisibility(View.GONE);
        String imageUrl = post.getUser().getString("profileImage");
        mapFrameLayout.addView(v);
        Projection p = map.getProjection();
        Point point = p.toScreenLocation(new LatLng(post.getLatitude(), post.getLongitude()));
        v.setX(point.x);
        v.setY(point.y);
        map.getUiSettings().setAllGesturesEnabled(false);
        temporaryMarker = v;
        if (imageUrl == null){
            profileImage.setImageResource(R.drawable.person_icon_graybg);
        } else {
            Application.imageLoader.get(imageUrl, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    Bitmap bm = response.getBitmap();
                    if (bm != null){
                        profileImage.setImageBitmap(bm);
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    profileImage.setImageResource(R.drawable.person_icon_graybg);
                }
            }, 100, 100);
        }
    }

    public void returnMapToNormal(){
        darkOverlay.setVisibility(View.GONE);
        map.getUiSettings().setAllGesturesEnabled(true);
        mapFrameLayout.removeView(temporaryMarker);
    }

    private View.OnClickListener continueOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            hideSuccessDialog();
            returnMapToNormal();
            setFeed(main.posts);
        }
    };

    private void showSuccessDialog(){
        YoYo.with(Techniques.FlipInX)
                .duration(250)
                .playOn(successDialog);
    }

    private void hideSuccessDialog(){
        YoYo.with(Techniques.FlipOutX)
                .duration(250)
                .playOn(successDialog);
    }


}
