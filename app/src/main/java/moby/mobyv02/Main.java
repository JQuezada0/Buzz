package moby.mobyv02;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.leanplum.activities.LeanplumFragmentActivity;
import com.nineoldandroids.animation.Animator;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import moby.mobyv02.parse.Event;
import moby.mobyv02.parse.Post;

/**
 * Created by quezadjo on 9/7/2015.
 */
public class Main extends FragmentActivity {



    //////////////////////////CONSTANTS////////////////////////////////////////

    private static final int WORLD_TOGGLED = 0;
    private static final int POST_CREATED = 1000;
    private int pageNumber = 0;
    private int currentView = 0;
    private boolean eventMode = false;

    //////////////////////////INITIALIZATIONS//////////////////////////////////
    private int currentToggle = WORLD_TOGGLED;
    public List<Post> posts = new ArrayList<Post>();
    public ArrayList<Event> events = new ArrayList<Event>();
    private boolean firstTime = true;
    private boolean tutorial = false;
    ///////////////////////////////////////////////////////////////////////////


    //////////////////////////DECLARATIONS/////////////////////////////////////

    private boolean map;
    private boolean feedIsLoading;
    private ShowcaseView showcaseView;

        //////////////////////VIEWS///////////////////////

        private ImageView messageButton;
        private CircleProgressBar progressBar;
        private Button followerToggleButton;
        private DrawerLayout drawerLayout;
        private ListView drawerList;
        private Button feedToggle;
        private Button mapToggle;
        private TableRow createPostBar;
        private ListView createPostList;
        private Toolbar toolbar;
        private ActionBarDrawerToggle actionBarDrawerToggle;
        private MainViewPager viewPager;
        private MainPagerAdapter mainAdapter;
        private boolean postListOpen = false;
        private CircleImageView profileImage;
        private TextView profileName;

        ////////////////////////////////////////////////////


        //////////////////FRAGMENTS/////////////////////////

        private FeedFragment feedFragment;
        private MapFragment mapFragment;

        ////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////


    /**
     * @desc Called when the activity is created. All Initializations occur here.
     * @param savedInstanceState If a bundle was saved in onSaveInstance, the bundle is received here during activity recreation.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Application.initParseInstallation(getIntent());
        initialize();
        DrawerAdapter drawerAdapter = new DrawerAdapter(this, this);
        drawerList.setAdapter(drawerAdapter); //Set the adapter for the left drawer
        drawerList.setOnItemClickListener(drawerAdapter.getClickListener());
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer); //Connect the toolbar drawer toggle button to the drawer

        viewPager.setAdapter(mainAdapter); //Set the viewpager adapter. Sets the feed and map into the viewpager

        viewPager.addOnPageChangeListener(pageChangeListener);

        progressBar.setColorSchemeResources(R.color.moby_blue); //Set the color of the progress circle. Graphic doesn't show without setting color.

        actionBarDrawerToggle.setDrawerIndicatorEnabled(true); //Set the drawer toggle button to show in the actionbar

        actionBarDrawerToggle.syncState(); //Sync toolbar drawer toggle

        drawerLayout.setDrawerListener(actionBarDrawerToggle); //Set the toggle to open the drawer upon clicking on it

        viewPager.setCurrentItem(1);

        map = true;
        feedToggle.setSelected(false);
        feedToggle.setTextColor(getResources().getColor(R.color.moby_blue));
        mapToggle.setSelected(true);
        mapToggle.setTextColor(getResources().getColor(android.R.color.white));

        feedIsLoading = true; //Set to true to indicate that the feed is currently loading

        setClickListeners();

        setPostBarInfo();

        loadFeed(true);

        firstTime = getSharedPreferences("user", 0).getBoolean("firstTime", true);

    }

    @Override
    protected void onResume(){
        super.onResume();
        if (currentView == 0){
            BuzzAnalytics.logScreen(Main.this, BuzzAnalytics.MAIN_CATEGORY, "feed");
        } else{
            BuzzAnalytics.logScreen(Main.this, BuzzAnalytics.MAIN_CATEGORY, "map");
        }
        postListOpen = false;
        createPostList.setVisibility(View.GONE);
    }

    private void showcaseViewStepOne(){

        Target target = new ViewTarget(R.id.moby_main_feed_toggle, this);
        tutorial = true;
        firstTime = false;
        getSharedPreferences("user", 0).edit().putBoolean("firstTime", firstTime).apply();
        showcaseView = new ShowcaseView.Builder(this)
                .setTarget(target)
                .setContentTitle("Feed toggle")
                .setContentText("Tap here to switch to your feed")
                .setStyle(R.style.CustomShowcaseTheme)
                .build();
        showcaseView.show();
        showcaseView.hideButton();
    }

    private void showcaseViewStepTwo(){
        Target target = new ViewTarget(R.id.moby_main_map_toggle, this);
        showcaseView.setTarget(target);
        showcaseView.setContentTitle("Map Toggle");
        showcaseView.setContentText("Tap here to switch back to your map");

    }

    public void showcaseViewStepFour(){


        drawerLayout.openDrawer(Gravity.LEFT);
        showcaseView.setTarget(new ViewTarget(drawerList.getChildAt(3)));
        showcaseView.setContentTitle("Find new friends");
        showcaseView.setContentText("Go into the discovery section to find new friends. ");
    }

    public ShowcaseView getShowcaseView(){
        return showcaseView;
    }

    public boolean getTutorial(){
        return tutorial;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == POST_CREATED && resultCode == RESULT_OK) {

            displayPostOnMap(data.getStringExtra("post"));

        }

    }

    public ViewPager getMainViewPager(){
        return viewPager;
    }

    private void setPostBarInfo(){
        profileName.setText("What's going on, " + ParseUser.getCurrentUser().getString("fullName") + "?");
        final String profileImage = ParseUser.getCurrentUser().getString("profileImage");
        if (profileImage != null){
            Application.imageLoader.get(profileImage, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    Bitmap bm = response.getBitmap();
                    if (bm != null){
                        Main.this.profileImage.setImageBitmap(bm);
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    Main.this.profileImage.setImageDrawable(ContextCompat.getDrawable(Main.this, R.drawable.person_icon_graybg));
                }
            }, 100, 100);
        } else {
            Main.this.profileImage.setImageDrawable(ContextCompat.getDrawable(Main.this, R.drawable.person_icon_graybg));
        }
    }

    public void loadFeed(final boolean reset){
        progressBar.setVisibility(View.VISIBLE);
        new ParseOperation("Network").getFeed(pageNumber, new ParseOperation.LoadFeedCallback() {
            @Override
            public void finished(boolean success, ArrayList<Post> posts, ParseException e) {
                if (e == null && posts != null) {
                    feedFragment.loadPosts(posts, reset);
                    Main.this.posts.addAll(posts);
                    progressBar.setVisibility(View.GONE);
                    pageNumber++;
                    mapFragment.setFeed(Main.this.posts);
                    if (firstTime){
                        showcaseViewStepOne();
                    }
                } else {
                    if (e != null) {
                        Toast.makeText(Main.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        BuzzAnalytics.logError(Main.this, e.getMessage());
                    } else {
                        Toast.makeText(Main.this, "An error has occured. Please try again later.", Toast.LENGTH_LONG).show();
                        BuzzAnalytics.logError(Main.this, "Unknown error loading feed");
                    }
                }

            }
        }, this);
    }

    public void loadEventsFeed(final boolean reset){

        new ParseOperation("Network").getEventsFeed(0, new ParseOperation.LoadEventsCallback() {
            @Override
            public void finished(boolean success, ArrayList<Event> events, ParseException e) {
                progressBar.setVisibility(View.GONE);
                feedFragment.loadEvents(events, reset);
                Main.this.events.addAll(events);
                pageNumber++;
            }
        }, this);

    }

    private void initialize(){

        ///////////////////BUTTONS/////////////////////////////////////////

        feedToggle = (Button) findViewById(R.id.moby_main_feed_toggle);
        mapToggle = (Button) findViewById(R.id.moby_main_map_toggle);
        followerToggleButton = (Button) findViewById(R.id.follower_toggle_button);

        ////////////////////////////////////////////////////////////////////


        //////////////////VIEW GROUPS////////////////////////////////////////

        progressBar = (CircleProgressBar) findViewById(R.id.main_progressbar);
        createPostBar = (TableRow) findViewById(R.id.create_post_bar);
        createPostList = (ListView) findViewById(R.id.post_bar_list);
        createPostList.setAdapter(new PostBarListAdapter());
        createPostList.setOnItemClickListener(postListItemClickListener);
        toolbar = (Toolbar) findViewById(R.id.moby_main_toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        viewPager = (MainViewPager) findViewById(R.id.main_viewpager);
        drawerList = (ListView) findViewById(R.id.left_drawer);
        profileImage = (CircleImageView) findViewById(R.id.main_profile_image);
        profileName = (TextView) findViewById(R.id.main_name);


        ///////////////////////////////////////////////////////////////////////

        //////////////////ADAPTERS/////////////////////////////////////////////

        mainAdapter = new MainPagerAdapter(getSupportFragmentManager());

        ////////////////////////////////////////////////////////////////////////

        //////////////////FRAGMENTS/////////////////////////////////////////////

        feedFragment = (FeedFragment) mainAdapter.getItem(0);
        mapFragment = (MapFragment) mainAdapter.getItem(1);

        ////////////////////////////////////////////////////////////////////////

    }

    private void setClickListeners() {

        feedToggle.setOnClickListener(feedToggleClickListener);
        mapToggle.setOnClickListener(mapToggleClickListener);
        createPostBar.setOnClickListener(postBarClickListener);

    }

    public void toggleFeed(){

        mapFragment.hideFeed();
        map = false;
        feedToggle.setSelected(true);
        feedToggle.setTextColor(getResources().getColor(android.R.color.white));
        mapToggle.setSelected(false);
        mapToggle.setTextColor(getResources().getColor(R.color.moby_blue));
        viewPager.setCurrentItem(0, true);
        if (eventMode == true){
            loadFeed(true);
        }
        eventMode = false;
        if (tutorial){
            showcaseViewStepTwo();
        }
    }

      public void toggleMap(){

        map = true;
        feedToggle.setSelected(false);
        feedToggle.setTextColor(getResources().getColor(R.color.moby_blue));
        mapToggle.setSelected(true);
        mapToggle.setTextColor(getResources().getColor(android.R.color.white));
        viewPager.setCurrentItem(1, true);
          if (!eventMode){
              System.out.println(posts.size());
              mapFragment.setFeed(posts);
          }
        if (tutorial){
            mapFragment.showCaseViewStepThree();
        }
    }

    private void displayPostOnMap(String objectId){

        progressBar.setVisibility(View.VISIBLE);
        map = true;
        feedToggle.setSelected(false);
        feedToggle.setTextColor(getResources().getColor(R.color.moby_blue));
        mapToggle.setSelected(true);
        mapToggle.setTextColor(getResources().getColor(android.R.color.white));
        viewPager.setCurrentItem(1, true);
        ParseQuery<Post> postQuery = Post.getQuery();
        postQuery.getInBackground(objectId, new GetCallback<Post>() {
            @Override
            public void done(Post post, ParseException e) {
                mapFragment.animateNewMarkerOnPost(post);
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    public void refreshFeed(){
        pageNumber = 0;
        new ParseOperation("Network").getFeed(pageNumber, new ParseOperation.LoadFeedCallback() {
            @Override
            public void finished(boolean success, ArrayList<Post> posts, ParseException e) {
                if (e == null){
                    feedFragment.loadPosts(posts, true);
                    Main.this.posts.clear();
                    Main.this.posts.addAll(posts);
                    pageNumber++;
                }

            }
        }, this);
        feedFragment.refreshFinished();
    }

    public void closeDrawer(){
        drawerLayout.closeDrawers();
    }

    ////////////////////////////CLICK LISTENERS/////////////////////////////////////////////

    final View.OnClickListener feedToggleClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            toggleFeed();
        }
    };


    private final View.OnClickListener mapToggleClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            toggleMap();
        }

    };

    private final View.OnClickListener postBarClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!postListOpen){
                createPostList.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeInDown)
                        .duration(250)
                        .playOn(createPostList);
                postListOpen = true;
                viewPager.setCurrentItem(1);
                Post post = new Post();
                ParseGeoPoint point = new ParseGeoPoint();
                point.setLongitude(LocationManager.getLocation().getLongitude());
                point.setLatitude(LocationManager.getLocation().getLatitude());
                post.setLocation(point);
                post.setUser(ParseUser.getCurrentUser());
                mapFragment.animateNewMarker(post);
                map = true;
                feedToggle.setSelected(false);
                feedToggle.setTextColor(getResources().getColor(R.color.moby_blue));
                mapToggle.setSelected(true);
                mapToggle.setTextColor(getResources().getColor(android.R.color.white));
            } else {
                YoYo.with(Techniques.FadeOutUp)
                        .duration(250).withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                        createPostList.setVisibility(View.GONE);
                        postListOpen = false;
                        mapFragment.returnMapToNormal();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).playOn(createPostList);
            }

        }
    };



    /////////////////////////////////////////////////////////////////////////////////////////


    private final ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {



        }

        @Override
        public void onPageSelected(int position) {
            currentView = position;
            switch (position){
                case 0:
                    BuzzAnalytics.logScreen(Main.this, BuzzAnalytics.MAIN_CATEGORY, "feed");
                    break;
                case 1:
                    BuzzAnalytics.logScreen(Main.this, BuzzAnalytics.MAIN_CATEGORY, "map");
                    break;
            }

        }

        @Override
        public void onPageScrollStateChanged(int state) {



        }
    };

    private class PostBarListAdapter extends BaseAdapter {

        String[] list = new String[]{"Status", "Photo", "Video"};

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Object getItem(int i) {
            return list[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            view = Main.this.getLayoutInflater().inflate(R.layout.drawer_item, null);
            TextView text = (TextView) view.findViewById(R.id.drawer_item_text);
            text.setText(list[position]);
            ImageView image = (ImageView) view.findViewById(R.id.drawer_item_image);
            switch(position){
                case 0:
                    image.setImageDrawable(ContextCompat.getDrawable(Main.this, R.drawable.post_status_icon));
                    break;
                case 1:
                    image.setImageDrawable(ContextCompat.getDrawable(Main.this, R.drawable.post_photo_icon));
                    break;
                case 2:
                    image.setImageDrawable(ContextCompat.getDrawable(Main.this, R.drawable.post_video_icon));
                    break;
            }
            return view;
        }
    }

    private AdapterView.OnItemClickListener postListItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            mapFragment.returnMapToNormal();
            switch (position){
                case 0:
                    startActivityForResult(new Intent(Main.this, CreateStatusPost.class), POST_CREATED);
                    break;
                case 1:
                    startActivityForResult(new Intent(Main.this, CreatePhotoPost.class), POST_CREATED);
                    break;
                case 2:
                    startActivityForResult(new Intent(Main.this, CreateVideoPost.class), POST_CREATED);
                    break;
            }
        }
    };

    public void toggleEvents(){
        progressBar.setVisibility(View.VISIBLE);
        loadEventsFeed(true);
        eventMode = true;
    }

}
