package moby.mobyv02;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.multidex.MultiDex;
import android.util.LruCache;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.leanplum.Leanplum;
import com.leanplum.LeanplumApplication;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;

import io.fabric.sdk.android.Fabric;

import java.io.File;

import moby.mobyv02.parse.Comment;
import moby.mobyv02.parse.Heart;
import moby.mobyv02.parse.Follow;
import moby.mobyv02.parse.Post;
import moby.mobyv02.parse.Upvote;

/**
 * Created by quezadjo on 9/8/2015.
 */
public class Application extends LeanplumApplication {

    public static ImageLoader imageLoader;
    public static AppEventsLogger logger;
    public static File cacheImageFile;
    public static final int FACEBOOK_REQUEST_CODE = 5000;
    private static Context context;

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
        ParseObject.registerSubclass(Follow.class);
        ParseObject.registerSubclass(Upvote.class);
        FacebookSdk.sdkInitialize(this, FACEBOOK_REQUEST_CODE);
        Parse.initialize(this, getString(R.string.parse_application_id), getString(R.string.parse_client_key));
        ParseInstallation.getCurrentInstallation().saveInBackground();
        ParseFacebookUtils.initialize(this, FACEBOOK_REQUEST_CODE);
        logger = AppEventsLogger.newLogger(this);
        logger.logEvent("Moby opened");
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

    /**
     * @desc getter for the global locationManager
     * @return LocationManager
     */

    @Override
    protected void attachBaseContext (Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static void initLeanPlum(Activity activity){
        if (BuildConfig.DEBUG) {
            Leanplum.setAppIdForDevelopmentMode("app_fHaR2B7Xb1mamIGfU4z9FXb50eVY5QeHvPURmpXAFio", "dev_DZYELDJSN3ASeJHFHQUuuCuSf2t4uxOJvw5wUAimw6c");
        } else {
            Leanplum.setAppIdForProductionMode("app_fHaR2B7Xb1mamIGfU4z9FXb50eVY5QeHvPURmpXAFio", "prod_Y0Uw1nzvxdrA8sY4ruuMOt2OI84pdudG3GbpCAqbhwY");
        }
        Leanplum.start(activity);
    }

    public static void initParseInstallation(Intent intent){
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("user", ParseUser.getCurrentUser());
        installation.saveInBackground();
        ParseAnalytics.trackAppOpenedInBackground(intent);
    }

    public static void initFacebookLogging(Context context){
        AppEventsLogger.activateApp(context);
    }

    public static File getImageCacheFile(Context context){

        if (cacheImageFile != null){
            return cacheImageFile;
        } else {
            cacheImageFile = new File(Environment.getExternalStorageDirectory(), "image.png");
            return cacheImageFile;
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
            });
        } else {
            image.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.person_icon_graybg));
        }
    }

}
