package moby.mobyv02;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import android.widget.TableRow;
import android.widget.Toast;

import com.leanplum.activities.LeanplumFragmentActivity;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import moby.mobyv02.parse.Post;

/**
 * Created by quezadjo on 9/7/2015.
 */
public class Main extends LeanplumFragmentActivity{



    //////////////////////////CONSTANTS////////////////////////////////////////

    private static final int WORLD_TOGGLED = 0;
    private static final int POST_CREATED = 1000;
    private int pageNumber = 0;

    //////////////////////////INITIALIZATIONS//////////////////////////////////
    private int currentToggle = WORLD_TOGGLED;
    public List<Post> posts = new ArrayList<Post>();
    ///////////////////////////////////////////////////////////////////////////


    //////////////////////////DECLARATIONS/////////////////////////////////////

    private Application app;
    private boolean map;
    private boolean feedIsLoading;

        //////////////////////VIEWS///////////////////////

        private ImageView messageButton;
        private CircleProgressBar progressBar;
        private Button followerToggleButton;
        private DrawerLayout drawerLayout;
        private ListView drawerList;
        private Button feedToggle;
        private Button mapToggle;
        private TableRow createStatusPost;
        private TableRow createPhotoPost;
        private TableRow createVideoPost;
        private Toolbar toolbar;
        private ActionBarDrawerToggle actionBarDrawerToggle;
        private MainViewPager viewPager;
        private MainPagerAdapter mainAdapter;

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

        Application.initLeanPlum(this);
        Application.initParseInstallation(getIntent());
        Application.initFacebookLogging(this);
        initialize();
        DrawerAdapter drawerAdapter = new DrawerAdapter(this, this);
        drawerList.setAdapter(drawerAdapter); //Set the adapter for the left drawer
        drawerList.setOnItemClickListener(drawerAdapter.getClickListener());
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer); //Connect the toolbar drawer toggle button to the drawer

        viewPager.setAdapter(mainAdapter); //Set the viewpager adapter. Sets the feed and map into the viewpager

        progressBar.setColorSchemeResources(R.color.moby_blue); //Set the color of the progress circle. Graphic doesn't show without setting color.

        actionBarDrawerToggle.setDrawerIndicatorEnabled(true); //Set the drawer toggle button to show in the actionbar

        actionBarDrawerToggle.syncState(); //Sync toolbar drawer toggle

        drawerLayout.setDrawerListener(actionBarDrawerToggle); //Set the toggle to open the drawer upon clicking on it

        feedToggle.setSelected(true); //Set the default view for the app to the feed view

        feedIsLoading = true; //Set to true to indicate that the feed is currently loading

        setClickListeners();

        loadFeed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == POST_CREATED && resultCode == RESULT_OK) {

            displayPostOnMap(data.getStringExtra("post"));

        }

    }

    public void loadFeed(){
        progressBar.setVisibility(View.VISIBLE);
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("location", LocationManager.getLocation());
        params.put("pageNumber", pageNumber);
        System.out.println("load feed");

/*        ParseOperation.getFeed(pageNumber, new ParseOperation.LoadFeedCallback() {
            @Override
            public void finished(boolean success, final List<Post> posts, ParseException e) {
                if (success) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("Obtained posts");
                            feedFragment.loadPosts(new ArrayList<Post>(posts));
                            Main.this.posts.addAll(posts);
                            progressBar.setVisibility(View.GONE);
                            pageNumber++;
                        }
                    });
                }
            }
        }, this); */

        ParseCloud.callFunctionInBackground("getFeed", params, new FunctionCallback<String>() {
            @Override
            public void done(String s, ParseException e) {
                JSONArray postsJsonArray = null;
                try {
                    postsJsonArray = new JSONArray(s);
                    ArrayList<Post> postPointerList = new ArrayList<Post>();
                    ArrayList<ParseUser> userPointerList = new ArrayList<ParseUser>();
                    ArrayList<String> objectIds = new ArrayList<String>();
                    for (int x = 0; x < postsJsonArray.length(); x++){
                        objectIds.add(postsJsonArray.getJSONObject(x).getString("objectId"));
                        Post object = ParseObject.createWithoutData(Post.class, postsJsonArray.getJSONObject(x).getString("objectId"));
                        postPointerList.add(object);
//                        userPointerList.add(user);
//                        object.setUser(user);
                    }
                    System.out.println("Objectid's length is " + objectIds.size());
                    ParseQuery<Post> query = Post.getQuery();
                    query.whereContainedIn("objectId", objectIds);
                    query.include("user");
                    ArrayList<Post> postsList = new ArrayList<Post>(query.find());
//                    System.out.println(ParseObject.fetchAll(userPointerList).size());
//                    for (Post p : postsList){
//                        p.setUser(p.getUser().fetch());
//                    }
                    System.out.println(postsList.size());
//                    System.out.println(userList.size());
//                    ArrayList<ParseUser> userList = new ArrayList<ParseUser>(ParseUser.fetchAll(userPointerList));
//                    for (int x = 0; x < postsList.size(); x++){
//                        postsList.get(x).setUser(userList.get(x));
//                    }
                    Collections.sort(postsList);
                    feedFragment.loadPosts(postsList);
                    posts.addAll(postsList);
                    progressBar.setVisibility(View.GONE);
                    pageNumber++;
//                    feedFragment.loadPosts(new ArrayList<Post>(ParseObject.fetchAll(postPointerList)));
                } catch (JSONException e1) {
                    e1.printStackTrace();
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }


            }
        });


    }

    private void jsonParseObjectToParseObject(){

    }


    private void initialize(){

        app = (Application) getApplication(); //Get reference to Application class

        ///////////////////BUTTONS/////////////////////////////////////////

        feedToggle = (Button) findViewById(R.id.moby_main_feed_toggle);
        mapToggle = (Button) findViewById(R.id.moby_main_map_toggle);
        followerToggleButton = (Button) findViewById(R.id.follower_toggle_button);

        ////////////////////////////////////////////////////////////////////


        //////////////////VIEW GROUPS////////////////////////////////////////

        progressBar = (CircleProgressBar) findViewById(R.id.main_progressbar);
        createStatusPost = (TableRow) findViewById(R.id.create_status_post);
        createPhotoPost = (TableRow) findViewById(R.id.create_post_photo_button);
        createVideoPost = (TableRow) findViewById(R.id.create_video_post_button);

        toolbar = (Toolbar) findViewById(R.id.moby_main_toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        viewPager = (MainViewPager) findViewById(R.id.main_viewpager);
        drawerList = (ListView) findViewById(R.id.left_drawer);


        ///////////////////////////////////////////////////////////////////////

        //////////////////ADAPTERS/////////////////////////////////////////////

        mainAdapter = new MainPagerAdapter(getSupportFragmentManager());

        ////////////////////////////////////////////////////////////////////////

        //////////////////FRAGMENTS/////////////////////////////////////////////

        feedFragment = (FeedFragment) mainAdapter.getItem(0);
        mapFragment = (MapFragment) mainAdapter.getItem(1);

        ////////////////////////////////////////////////////////////////////////

    }

    private void setClickListeners(){

        feedToggle.setOnClickListener(feedToggleClickListener);
        mapToggle.setOnClickListener(mapToggleClickListener);
        createStatusPost.setOnClickListener(createStatusPostClickListener);
        createPhotoPost.setOnClickListener(createPhotoPostClickListener);
        createVideoPost.setOnClickListener(createVideoPostClickListener);

    }



    private void loadInitialPosts(){

        progressBar.setVisibility(View.VISIBLE);

        loadInitialWorldPosts();

    }

    private void loadInitialWorldPosts(){

        ParseOperation.loadInitialWorldFeed(map, new ParseOperation.LoadFeedCallback() {
            @Override
            public void finished(final boolean success, final List<Post> posts, final ParseException e) {
                progressBar.setVisibility(View.GONE);
                if (success) {
                    if (map && !feedIsLoading) { //Only run if the user is in the map feed, and the feed isn't loading
                        mapFragment.setFeed(posts);
                    } else { //Run if the user is in the feed view
                        feedFragment.setInitialPosts(new ArrayList<Post>(posts));
                    }
                    loadFirstWorldFeed();
                } else {
                    Toast.makeText(Main.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, this);


    }

    private void loadFirstWorldFeed() {

        ParseOperation.loadWorldFeed(map, 0, new ParseOperation.LoadFeedCallback() {
            @Override
            public void finished(boolean success, List<Post> posts, ParseException e) {

                progressBar.setVisibility(View.GONE);

                if (success) {
                    if (map && !feedIsLoading) {
                        mapFragment.setFeed(posts);
                    } else {
                        feedFragment.loadFirstPosts(new ArrayList<Post>(posts));
                        feedIsLoading = false;
                    }
                } else {
                    Toast.makeText(Main.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        }, this);

    }

    public void loadWorldFeed(){

        ParseOperation.loadWorldFeed(map, pageNumber, new ParseOperation.LoadFeedCallback() {
            @Override
            public void finished(boolean success, List<Post> posts, ParseException e) {

                progressBar.setVisibility(View.GONE);
                if (success) {
                    feedFragment.loadPosts(new ArrayList<Post>(posts));
                    pageNumber++;
                } else {
                    Toast.makeText(Main.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        }, this);

    }



    public void toggleFeed(){

        mapFragment.hideFeed();
        mapFragment.clearMap();
        map = false;
        feedToggle.setSelected(true);
        feedToggle.setTextColor(getResources().getColor(android.R.color.white));
        mapToggle.setSelected(false);
        mapToggle.setTextColor(getResources().getColor(R.color.moby_blue));
        viewPager.setCurrentItem(0, true);

    }

      public void toggleMap(){

        map = true;
        feedToggle.setSelected(false);
        feedToggle.setTextColor(getResources().getColor(R.color.moby_blue));
        mapToggle.setSelected(true);
        mapToggle.setTextColor(getResources().getColor(android.R.color.white));
        viewPager.setCurrentItem(1, true);
        mapFragment.setFeed(posts);
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
                mapFragment.animateNewMarker(post);
                progressBar.setVisibility(View.GONE);
            }
        });

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

    private final View.OnClickListener createStatusPostClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            startActivityForResult(new Intent(Main.this, CreateStatusPost.class), POST_CREATED);
        }

    };

    private final View.OnClickListener createPhotoPostClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            startActivityForResult(new Intent(Main.this, CreatePhotoPost.class), POST_CREATED);
        }


    };

    private final View.OnClickListener createVideoPostClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            startActivityForResult(new Intent(Main.this, CreateVideoPost.class), POST_CREATED);
        }


    };


    private final View.OnClickListener mapToggleClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            toggleMap();
        }

    };



    /////////////////////////////////////////////////////////////////////////////////////////

}
