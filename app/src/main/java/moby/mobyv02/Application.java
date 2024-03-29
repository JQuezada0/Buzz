package moby.mobyv02;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.Environment;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseFacebookUtils;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import io.fabric.sdk.android.Fabric;

import java.io.File;
import java.io.IOException;
import java.util.List;

import moby.mobyv02.parse.Comment;
import moby.mobyv02.parse.Event;
import moby.mobyv02.parse.Friend;
import moby.mobyv02.parse.Heart;
import moby.mobyv02.parse.Follow;
import moby.mobyv02.parse.Post;
import moby.mobyv02.parse.Upvote;

/**
 * Created by quezadjo on 9/8/2015.
 */
public class Application extends android.app.Application {

    public static ImageLoader imageLoader;
    public static AppEventsLogger logger;
    public static File cacheImageFile;
    private static File cacheVideoFile;
    public static final int FACEBOOK_REQUEST_CODE = 5000;
    public static Context context;

    @Override
    public void onCreate()
    {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        context = this;
        Parse.enableLocalDatastore(this);
        ParseObject.registerSubclass(Post.class);
        ParseObject.registerSubclass(Heart.class);
        ParseObject.registerSubclass(Comment.class);
        ParseObject.registerSubclass(Friend.class);
        ParseObject.registerSubclass(Upvote.class);
        ParseObject.registerSubclass(Event.class);
        FacebookSdk.sdkInitialize(this, FACEBOOK_REQUEST_CODE);
        Parse.initialize(this, getString(R.string.parse_application_id), getString(R.string.parse_client_key));
        ParseInstallation.getCurrentInstallation().saveInBackground();
        ParseFacebookUtils.initialize(this, FACEBOOK_REQUEST_CODE);
        BuzzAnalytics.initialize(this, this.getResources());
        logger = AppEventsLogger.newLogger(this);
        imageLoader = new ImageLoader(Volley.newRequestQueue(this), new ImageLoader.ImageCache() {

            private final android.support.v4.util.LruCache<String, Bitmap> cache = new android.support.v4.util.LruCache<String, Bitmap>(20);

            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        });

    }

    public static void reloadImageCache(Context c){
        imageLoader = new ImageLoader(Volley.newRequestQueue(c), new ImageLoader.ImageCache() {

            private final android.support.v4.util.LruCache<String, Bitmap> cache = new android.support.v4.util.LruCache<String, Bitmap>(20);

            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        });
    }

    /**
     * @desc getter for the global locationManager
     * @return LocationManager
     */

    @Override
    protected void attachBaseContext (Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static void initParseInstallation(Intent intent){
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        if (ParseUser.getCurrentUser() == null){
            installation.put("user", "anon");
        } else {
            installation.put("user", ParseUser.getCurrentUser());
        }
        installation.saveInBackground();
        ParseAnalytics.trackAppOpenedInBackground(intent);
    }

    public static File getImageCacheFile(Context context){

        if (cacheImageFile != null){
            return cacheImageFile;
        } else {
            cacheImageFile = new File(Environment.getExternalStorageDirectory(), "image.png");
            return cacheImageFile;
        }

    }

    public static File getVideoCacheFile(Context context){

        if (cacheVideoFile != null){
            return cacheVideoFile;
        } else {
            cacheVideoFile = new File(Environment.getExternalStorageDirectory(), "video.3gp");
            return cacheVideoFile;
        }

    }

    public static void loadImage(final ImageView image, String url){
        if (url != null){
            imageLoader.get(url, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    if (response.getBitmap() != null)
                        image.setImageBitmap(response.getBitmap());
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }, image.getWidth(), image.getHeight());
        } else {
            image.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.person_icon_graybg));
        }
    }

    public void updateUserPostCount(ParseUser user){
        ParseQuery<Post> query = Post.getQuery();

    }

    public static String getLocale(Context context, ParseGeoPoint location){
        if (location == null){
            return "";
        }
        try {
            Geocoder geocoder = new Geocoder(context);
            List<Address> addresses;
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses.size() == 0)
                return "";
            return addresses.get(0).getLocality();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

}
