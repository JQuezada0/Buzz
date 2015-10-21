package moby.mobyv02;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.androidmapsextensions.ClusterOptions;
import com.androidmapsextensions.ClusterOptionsProvider;
import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.MarkerOptions;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.PointTarget;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.androidmapsextensions.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.androidmapsextensions.Marker;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.ui.IconGenerator;
import com.nineoldandroids.animation.Animator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import moby.mobyv02.layout.GestureFrameLayout;
import moby.mobyv02.map.MapTree;
import moby.mobyv02.parse.Event;
import moby.mobyv02.parse.Post;

/**
 * Created by quezadjo on 9/10/2015.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap map;
    SupportMapFragment mapFragment;
    public static MapFragment currentMapFragment;
    private ViewPager viewPager;
    private Main main;
    private PostsAdapter postsAdapter;
    public List<BuzzItem> currentPosts = new ArrayList<BuzzItem>();
    public ArrayList<Event> currentEvents = new ArrayList<Event>();
    private GestureFrameLayout feedFrame;
    private LayoutInflater inflater;
    private int lastPosition = 0;
    private MapTree mapTree;
    private FrameLayout mapFrameLayout;
    private View successDialog;
    private Button successDialogContinueButton;
    private ImageView darkOverlay;
    private View temporaryMarker;
    private boolean eventMode = false;
    private IconGenerator iconGenerator;
    private ArrayList<Marker> displayedMarkers = new ArrayList<Marker>();
    private int MARKER_WIDTH_DP = 78;
    private int MARKER_HEIGHT_DP = 90;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        main = (Main) getActivity();
        currentMapFragment = this;
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
        postsAdapter = new PostsAdapter(getFragmentManager());
        viewPager.addOnPageChangeListener(pageChangeListener);
        mapFragment.getExtendedMapAsync(this);
        iconGenerator = new IconGenerator(main);
        iconGenerator.setBackground(new ColorDrawable(0x00000000));
        MapAdapter.googleMap = v;
        successDialog = v.findViewById(R.id.success_dialog);
        setGestureDetector();
        Display display = main.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return v;
    }

    private void setGestureDetector(){
        feedFrame.setGestureDetector(new FeedGestureDetector());
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(LocationManager.getLocation().getLatitude(), LocationManager.getLocation().getLongitude()), 10.0f));
        final ClusteringSettings clusteringSettings = new ClusteringSettings();
        clusteringSettings.addMarkersDynamically(true);
        clusteringSettings.enabled(true);
        clusteringSettings.clusterOptionsProvider(new ClusterIconGenerator());
        map.setClustering(clusteringSettings);
        updateMap();
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                hideFeed();
            }
        });
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), map.getCameraPosition().zoom));
                updateDisplayedMarkers();
                reorderClusters(marker);
                reorganizeAdapter();
                postsAdapter.setPosts(currentPosts);
                viewPager.setAdapter(postsAdapter);
                feedFrame.setVisibility(View.VISIBLE);
                updateMarker(marker);
                if (main.getTutorial()){
                    showCaseViewStepFour();
                }
                return false;
            }
        });

    }

    private void updateMarker(Marker m){
        Marker marker = m;
        if (m.isCluster()){
            marker = m.getMarkers().get(0);
        }
        Object[] data = marker.getData();
        BuzzItem bi = (BuzzItem) data[0];
        updateMarker(bi);
    }

    private void updateMarker(BuzzItem item){
        Marker[] markers = findMarker(item);
        Object[] data = markers[0].getData();
        Marker mainMarker = markers[1];
        Bitmap image= (Bitmap) data[1];
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mainMarker.getPosition(), map.getCameraPosition().zoom));
        final View v = inflater.inflate(R.layout.map_marker_icon, null);
        final CircleImageView profileImage = (CircleImageView) v.findViewById(R.id.map_marker_image);
        final TextView count = (TextView) v.findViewById(R.id.map_marker_icon_count_text);
        if (mainMarker.isCluster()){
            count.setText(String.valueOf(mainMarker.getMarkers().size()));
        } else {
            v.findViewById(R.id.map_marker_icon_count).setVisibility(View.GONE);
        }
        profileImage.setImageBitmap(image);
        iconGenerator.setContentView(v);
        mainMarker.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()));
    }

    private Marker[] findMarker(BuzzItem item){
        for (int x = 0; x < displayedMarkers.size(); x++){
            Marker marker = displayedMarkers.get(x);
            if (marker.isCluster()){
                List<Marker> markers = marker.getMarkers();
                for (Marker m : markers){
                    Object[] data = m.getData();
                    BuzzItem bi = (BuzzItem) data[0];
                    if (bi.equals(item)){
                        return new Marker[]{bi.getMarker(), marker};
                    }
                }
            } else {
                Object[] data = (Object[]) marker.getData();
                BuzzItem bi = (BuzzItem) data[0];
                if (bi.equals(item)){
                    return new Marker[]{bi.getMarker(), marker};
                }
            }
        }
        return null;
    }

    private void reorganizeAdapter(){
        currentPosts.clear();
        for (Marker m : displayedMarkers){
            if (m.isCluster()){
                List<Marker> markers = m.getMarkers();
                for (int x = 0; x < markers.size(); x++){
                    Object[] data = (Object[]) markers.get(x).getData();
                    currentPosts.add((BuzzItem) data[0]);
                }
            } else {
                Object[] data = (Object[]) m.getData();
                currentPosts.add((BuzzItem) data[0]);
            }

        }
        System.out.println(currentPosts.size() + " Is the final size of the reordered posts");
    }

    private void reorderClusters(Marker startingMarker){
        ArrayList<moby.mobyv02.map.Point> points = new ArrayList<moby.mobyv02.map.Point>();
        for (Marker m : displayedMarkers){
            points.add(new moby.mobyv02.map.Point(m));
        }
        Collections.sort(points);
        displayedMarkers.clear();
        for (moby.mobyv02.map.Point point : points){
            displayedMarkers.add(point.getMarker());
        }
        displayedMarkers.remove(startingMarker);
        displayedMarkers.add(0, startingMarker);
    }

    public void updateDisplayedMarkers(){
        displayedMarkers.clear();
        List<Marker> displayedMarkers = map.getDisplayedMarkers();
        LatLngBounds screenBounds = map.getProjection().getVisibleRegion().latLngBounds;
        for (int x = 0; x < displayedMarkers.size(); x++){
            if (screenBounds.contains(displayedMarkers.get(x).getPosition())){
                MapFragment.this.displayedMarkers.add(displayedMarkers.get(x));
            }
        }
    }

    public void updateMap(){
        if (map!= null){
            map.clear();
            for (final BuzzItem bi : currentPosts){
                final View v = inflater.inflate(R.layout.map_marker_icon, null);
                final CircleImageView profileImage = (CircleImageView) v.findViewById(R.id.map_marker_image);
                v.findViewById(R.id.map_marker_icon_count).setVisibility(View.GONE);
                String image = bi.getProfileImage();
                final MarkerOptions options = new MarkerOptions();
                options.position(new LatLng(bi.getLatitude(), bi.getLongitude()));
                if (image!=null){
                    Application.imageLoader.get(bi.getProfileImage(), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                            if (response.getBitmap()!= null){
                                profileImage.setImageBitmap(response.getBitmap());
                                iconGenerator.setContentView(v);
                                Bitmap finalBitmap = response.getBitmap();
                                BitmapDescriptor bd = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon());
                                options.icon(bd);
                                Marker marker = map.addMarker(options);
                                bi.setMarker(marker);
                                Object[] data = new Object[]{bi, finalBitmap};
                                marker.setData(data);
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }, 100, 100);
                } else {
                    Bitmap finalBitmap = BitmapDownSampler.getBitmap(100, 100, main.getResources(), R.drawable.person_icon_graybg);
                    profileImage.setImageBitmap(finalBitmap);
                    iconGenerator.setContentView(v);
                    BitmapDescriptor bd = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon());
                    options.icon(bd);
                    Marker marker = map.addMarker(options);
                    bi.setMarker(marker);
                    Object[] data = new Object[]{bi, finalBitmap};
                    marker.setData(data);
                }
            }
        }
    }

    public void setFeed(List<BuzzItem> posts){
        currentPosts.clear();
        currentPosts.addAll(posts);
        eventMode = false;
        updateMap();
    }

    public void setEvents(List<Event> events){
        currentEvents.clear();
        currentEvents.addAll(events);
        eventMode = true;
        updateMap();
    }

    public void showCaseViewStepThree(){
        ShowcaseView showcaseView = main.getShowcaseView();
        Display display = main.getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        Point p = map.getProjection().toScreenLocation(new LatLng(LocationManager.getLocation().getLatitude(), LocationManager.getLocation().getLongitude()));
        p.y += main.getMainViewPager().getHeight() / 4;
        Target target = new PointTarget(p);
        showcaseView.setTarget(target);
        showcaseView.setContentTitle("Map posts");
        showcaseView.setContentText("Tap here to view posts from your map");
    }

    public void showCaseViewStepFour(){

        ShowcaseView showcaseView = main.getShowcaseView();
        Target target = new ViewTarget(feedFrame);
        showcaseView.setTarget(target);
        showcaseView.setContentTitle("Hide posts");
        showcaseView.setContentText("Swipe down to hide the posts");
        showcaseView.setShouldCentreText(true);
    }

    public void markerClusterClicked(BuzzItem post){
        if (mapTree != null){
            mapTree.resetAllNodes();
        }
//        mapTree = MapTree.createTree(main, clusterManager, clusterRenderer, map, new ArrayList<BuzzItem>(currentPosts), post);
        postsAdapter.setPosts(mapTree.getNewPosts());
        viewPager.setAdapter(postsAdapter);
        feedFrame.setVisibility(View.VISIBLE);
        lastPosition = 0;
    }

    public void markerClusterItemClicked(BuzzItem post){
        if (mapTree != null){
            mapTree.resetAllNodes();
        }
//        mapTree = MapTree.createTree(main, clusterManager, clusterRenderer, map, new ArrayList<BuzzItem>(currentPosts), post);
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

            updateMarker(currentPosts.get(position));
            postsAdapter.getCurrentPostView().hideMediaControls();
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
                if (main.getTutorial()){
                //   main.toggleFeed();
                    main.showcaseViewStepFour();
                }
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

    }

    public void hideFeed(){
        if (feedFrame!= null)
            feedFrame.setVisibility(View.GONE);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        return (int)((dp * displayMetrics.density) + 0.5);
    }

    public void animateNewMarker(final Post post){
        String imageUrl = null;
        View v = inflater.inflate(R.layout.map_marker_icon, null);
        if (inflater == null){
            inflater = LayoutInflater.from(main);
        }
        darkOverlay.setVisibility(View.VISIBLE);
        final CircleImageView profileImage = (CircleImageView) v.findViewById(R.id.map_marker_image);
        v.findViewById(R.id.map_marker_icon_count).setVisibility(View.GONE);
        if(post.getUser() != null) {
            imageUrl = post.getUser().getString("profileImage");
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(LocationManager.getLocation().getLatitude()+0.012, LocationManager.getLocation().getLongitude()), 12.0f));
        mapFrameLayout.addView(v);
        Projection p = map.getProjection();
        Point point = p.toScreenLocation(new LatLng(LocationManager.getLocation().getLatitude(), LocationManager.getLocation().getLongitude()));
        //Set the X and Y coordinates of the marker but offset the height and width because it pins the markers top left corner to the point you set
        v.setX(point.x - dpToPx(MARKER_WIDTH_DP/2));
        v.setY(point.y - dpToPx(MARKER_HEIGHT_DP));
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
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(LocationManager.getLocation().getLatitude()+0.0225, LocationManager.getLocation().getLongitude()), 12.0f));
        successDialog.setVisibility(View.VISIBLE);
        showSuccessDialog();
        darkOverlay.setVisibility(View.VISIBLE);
        View v = inflater.inflate(R.layout.map_marker_icon, null);
        final CircleImageView profileImage = (CircleImageView) v.findViewById(R.id.map_marker_image);
        v.findViewById(R.id.map_marker_icon_count).setVisibility(View.GONE);
        String imageUrl = post.getUser().getString("profileImage");
        mapFrameLayout.addView(v);
        Projection p = map.getProjection();
        Point point = p.toScreenLocation(new LatLng(LocationManager.getLocation().getLatitude(), LocationManager.getLocation().getLongitude()));
        //Set the X and Y coordinates of the marker but offset the height and width because it pins the markers top left corner to the point you set
        v.setX(point.x - dpToPx(MARKER_WIDTH_DP/2));
        v.setY(point.y - dpToPx(MARKER_HEIGHT_DP));
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
            setFeed(new ArrayList<BuzzItem>(main.posts));
        }
    };

    private void showSuccessDialog(){
        successDialog.setVisibility(View.VISIBLE);
    }

    private void hideSuccessDialog(){
        successDialog.setVisibility(View.GONE);
    }

    private class ClusterIconGenerator implements ClusterOptionsProvider {

        @Override
        public ClusterOptions getClusterOptions(List<Marker> markers) {
            final View v = inflater.inflate(R.layout.map_marker_icon, null);
            final CircleImageView profileImage = (CircleImageView) v.findViewById(R.id.map_marker_image);
            final TextView count = (TextView) v.findViewById(R.id.map_marker_icon_count_text);
            count.setText(String.valueOf(markers.size()));
            final ClusterOptions clusterOptions = new ClusterOptions();
            Object[] data = (Object[]) markers.get(0).getData();
            Bitmap bm = (Bitmap) data[1];
            profileImage.setImageBitmap(bm);
            iconGenerator.setContentView(v);
            clusterOptions.icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()));
            return clusterOptions;
        }
    }


}
