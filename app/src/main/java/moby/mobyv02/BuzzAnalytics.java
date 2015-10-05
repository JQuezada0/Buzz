package moby.mobyv02;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

import com.facebook.appevents.AppEventsLogger;
import com.leanplum.Leanplum;
import com.parse.ParseUser;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;

/**
 * Created by quezadjo on 9/30/2015.
 */
public class BuzzAnalytics {

    private static AppEventsLogger facebookAppEventsLogger;
    public static String ONBOARDING_CATEGORY = "onboarding";
    public static String MAIN_CATEGORY = "main";
    public static String POST_CATEGORY = "post";
    public static String COMMENT_CATEGORY = "comment";
    public static String PROFILE_CATEGORY = "profile";

    public static void initialize(Context context, Resources res){
        initializeAnalytics(context, res);
        initializeUser(context);
        initializeFacebookAppEventsLogger(context);
    }

    private static void initializeAnalytics(Context context, Resources res){
        Analytics analytics = new Analytics.Builder(context, res.getString(R.string.analytics_write_key)).logLevel(Analytics.LogLevel.VERBOSE).build();
        Analytics.setSingletonInstance(analytics);
    }

    private static void initializeUser(Context context){
        if (ParseUser.getCurrentUser() != null){
            Analytics.with(context).identify(ParseUser.getCurrentUser().getObjectId());
        }
    }

    private static void initializeFacebookAppEventsLogger(Context context){
        AppEventsLogger.activateApp(context);
        facebookAppEventsLogger = AppEventsLogger.newLogger(context);
    }

    public static void logLogin(Context context, String method, boolean newUser){
        Analytics.with(context).alias(ParseUser.getCurrentUser().getObjectId());
        Analytics.with(context).identify(ParseUser.getCurrentUser().getObjectId());
        Properties properties = new Properties();
        properties.put("method", method);
        properties.put("newUser", newUser);
        Analytics.with(context).track("userLogin", properties);
    }

    public static void logScreen(Context context, String category, String screen){
        Properties properties = new Properties();
        properties.put("screen", screen);
        Analytics.with(context).screen(category, screen, properties);

    }

    public static void logAppOpened(Context context){
        boolean newUser = ParseUser.getCurrentUser() == null ? true : false;
        Properties properties = new Properties();
        properties.put("newUser", newUser);
        Analytics.with(context).track("appOpened", properties);
    }

    public static void logPost(Context context, String type){
        Properties properties = new Properties();
        properties.put("type", type);
        Analytics.with(context).track("postCreated", properties);
    }

    public static void logComment(Context context){
        Properties properties = new Properties();
        properties.put("user", ParseUser.getCurrentUser().getObjectId());
        Analytics.with(context).track("commentCreated", properties);
    }

    public static void logHeart(Context context){
        Properties properties = new Properties();
        properties.put("user", ParseUser.getCurrentUser().getObjectId());
        Analytics.with(context).track("heartCreated", properties);
    }

    public static void logFollow(Context context){
        Properties properties = new Properties();
        properties.put("user", ParseUser.getCurrentUser().getObjectId());
        Analytics.with(context).track("followCreated", properties);
    }

    public static void logFacebookClick(Context context){
        Properties properties = new Properties();

    }

}
