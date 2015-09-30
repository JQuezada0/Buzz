package moby.mobyv02;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.ui.IconGenerator;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import moby.mobyv02.layout.GestureFrameLayout;
import moby.mobyv02.map.LatLngInterpolator;
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
    private List<Post> currentPosts = new ArrayList<Post>();
    private GestureFrameLayout feedFrame;
    private IconGenerator iconGenerator;
    private LayoutInflater inflater;
    ClusterRenderer clusterRenderer;
    private int lastPosition = 0;
    private MapTree mapTree;
    private ImageView fireworkAnimationImage;
    private AnimationDrawable fireworkAnimation;
    private Projection projection;
    private FrameLayout mapFrameLayout;
    private View successDialog;
    private int width;
    private int height;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        main = (Main) getActivity();
        currentMapFragment = this;
        if (MapAdapter.googleMap == null) {
            this.inflater = inflater;
            View v = inflater.inflate(R.layout.map, null);
            fireworkAnimationImage = (ImageView) v.findViewById(R.id.firework_animation);
            fireworkAnimation = (AnimationDrawable) fireworkAnimationImage.getBackground();
            mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            feedFrame = (GestureFrameLayout) v.findViewById(R.id.map_feed_frame);
            mapFrameLayout = (FrameLayout) v.findViewById(R.id.map_frame_layout);
            viewPager = (ViewPager) v.findViewById(R.id.map_viewpager);
            mapFrameLayout = (FrameLayout) v.findViewById(R.id.map_frame_layout);
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
            width = size.x;
            height = size.y;
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


    }

    public void updateMap(){
        map.clear();
        clusterManager.clearItems();
        clusterManager.addItems(currentPosts);
        clusterManager.cluster();
        System.out.println("Map should be updated");
        System.out.println(currentPosts.size());
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
        if (ParseUser.getCurrentUser() == null)
        System.out.println(ParseUser.getCurrentUser() + " this is parse user");
   //     markerClusterItemClicked(null, post);
//        successDialog.setVisibility(View.VISIBLE);
//        ObjectAnimator animator = ObjectAnimator.ofFloat(successDialog, "Y", 0 - successDialog.getHeight(), (height/2) - (successDialog.getHeight() / 2));
//        animator.setDuration(1000);
//        animator.start();
        Application.imageLoader.get(ParseUser.getCurrentUser().getString("profileImage"), new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                Bitmap bm = response.getBitmap();
                if (bm != null) {
                    IconGenerator iconGenerator = new IconGenerator(main);
                    iconGenerator.setBackground(new ColorDrawable(0x00000000));
                    View v = LayoutInflater.from(main).inflate(R.layout.map_marker_icon, null);
                    v.findViewById(R.id.map_marker_icon_count).setVisibility(View.GONE);
                    final CircleImageView profileImage = (CircleImageView) v.findViewById(R.id.map_marker_image);
                    profileImage.setImageBitmap(bm);
                    iconGenerator.setContentView(v);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(new LatLng(post.getLatitude(), post.getLongitude()));
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()));
                    Marker marker = map.addMarker(markerOptions);
//                    animateMarker(marker, iconGenerator.makeIcon());
                    int valuesAmount = new Double(3000 / 16).intValue();
                    Float[] interpolateValues = new Float[valuesAmount];
                    int halfwayPoint = new Double(interpolateValues.length / 2).intValue();
                    for (int x = 0; x < interpolateValues.length; x++){
                        if (x == 0){
                            interpolateValues[0] = 5f;
                        } else if (x == halfwayPoint){
                            interpolateValues[halfwayPoint] = 2.5f;
                        } else if (x == (interpolateValues.length - 1)){
                            interpolateValues[interpolateValues.length - 1] = 1f;
                        } else {
                            interpolateValues[x] = null;
                        }
                    }
                    interpolateValues = moby.mobyv02.Interpolator.Interpolate(interpolateValues, "linear");
                    for (int x = 0; x < interpolateValues.length; x++){
                        System.out.println(interpolateValues[x]);
                    }
//                    fireworkAnimationImage.setVisibility(View.VISIBLE);
//                    fireworkAnimation.start();
                }

            }
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    private void animateMarker(final Marker marker, final Bitmap bm, Float[] scale){

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 3000;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;
            double markerMultiplier;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);
                Double newWidth = bm.getWidth() * markerMultiplier;
                Double newHeight = bm.getHeight() * markerMultiplier;

                marker.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bm, newWidth.intValue(), newHeight.intValue(), false)));

                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });

    }

    static void animateMarkerToGB(final Marker marker, final LatLng finalPosition, final LatLngInterpolator latLngInterpolator, final Bitmap bm) {
        final LatLng startPosition = marker.getPosition();
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 3000;
        final double initialMultiplier = 2;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;
            double markerMultiplier;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);
                Double newWidth = bm.getWidth() * markerMultiplier;
                Double newHeight = bm.getWidth() * markerMultiplier;

//                marker.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bm, bm.getWidth()*markerMultiplier, bm.getHeight()*markerMultiplier, false)));

                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }




}
