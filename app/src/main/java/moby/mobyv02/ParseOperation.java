package moby.mobyv02;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.parse.FunctionCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import com.parse.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import moby.mobyv02.parse.Comment;
import moby.mobyv02.parse.Event;
import moby.mobyv02.parse.Heart;
import moby.mobyv02.parse.Follow;
import moby.mobyv02.parse.Post;
import moby.mobyv02.parse.Upvote;

/**
 * Created by quezadjo on 9/15/2015.
 */
public class ParseOperation {

    public static ParseObject parseObject;
    public static ParseUser parseUser;
    public static ParseFile parseFile;
    public static Post post;
    public static ParseOperationCallback parseOperationCallback;
    public static ImageUploadCallback imageUploadCallback;
    public static LoadFeedCallback loadFeedCallback;
    public static LoadEventsCallback loadEventsCallback;
    public static LoadCommentsCallback loadCommentsCallback;
    public static GetUsersCallback getUsersCallback;
    public static CreateFollowCallback createFollowCallback;
    public static GetUsersCallback discoveryCallback;
    public static GetUsersCallback followerCallback;
    public static GetUsersCallback followingCallback;
    public static Activity context;
    public static String username;
    public static String password;
    public static String comment;
    public static Follow follow;
    public static int pageNumber;
    public static boolean map;
    public static File file;

    public ParseOperation(String name){
    }

    public interface ParseOperationCallback {
        void finished(boolean success, ParseException e);
    }

    public interface ImageUploadCallback {
        void finished(boolean success, ParseFile file, ParseException e);
    }

    public interface LoadFeedCallback {
        void finished(boolean success, ArrayList<Post> posts, ParseException e);
    }

    public interface LoadEventsCallback {
        void finished(boolean success, ArrayList<Event> events, ParseException e);
    }

    public interface LoadCommentsCallback {
        void finished(boolean success, List<Comment> comments, ParseException e);
    }

    public interface GetUsersCallback {
        void finished(boolean success, ArrayList<ParseUser> users, ParseException e);
    }

    public interface CreateFollowCallback {
        void finished(boolean success, Follow follow, ParseException e);
    }

    public  void saveParseObject(ParseObject parseObject, ParseOperationCallback callback, Activity activity){
        context = activity;
        ParseOperation.parseObject = parseObject;
        ParseOperation.parseOperationCallback = callback;
        startService("saveParseObject");
    }

    public static void savePost(Post post, ParseOperationCallback callback, Activity activity){
        context = activity;
        ParseOperation.post = post;
        ParseOperation.parseOperationCallback = callback;
        startService("savePost");

    }

    public static void uploadImage(File file, ImageUploadCallback callback, Activity activity){
        context = activity;
        System.out.println("uploadImage called");
        ParseOperation.file = file;
        ParseOperation.imageUploadCallback = callback;
        startService("uploadImage");
    }

    public  void saveParseFile(ParseFile parseFile, ParseOperationCallback callback, Activity activity){
        context = activity;
        ParseOperation.parseFile = parseFile;
        ParseOperation.parseOperationCallback = callback;
        startService("saveParseFile");

    }

    public static void logIn(String username, String password, ParseOperationCallback callback, Activity activity){
        context = activity;
        ParseOperation.username = username;
        ParseOperation.password = password;
        ParseOperation.parseOperationCallback = callback;
        startService("logIn");

    }

    public static void signUp(ParseUser parseUser, ParseOperationCallback callback, Activity activity){

        context = activity;
        ParseOperation.parseUser = parseUser;
        ParseOperation.parseOperationCallback = callback;
        startService("signUp");

    }

    public  void loadInitialWorldFeed(boolean map, LoadFeedCallback callback, Activity activity){
        context = activity;
        ParseOperation.loadFeedCallback = callback;
        startService("loadInitialWorldFeed");
    }

    public  void loadInitialFollowerFeed(boolean map, LoadFeedCallback callback, Activity activity){
        context = activity;
        ParseOperation.loadFeedCallback = callback;
        startService("loadInitialFollowerFeed");
    }

    public  void loadWorldFeed(boolean map, int pageNumber, LoadFeedCallback callback, Activity activity){
        context = activity;
        ParseOperation.loadFeedCallback = callback;
        ParseOperation.pageNumber = pageNumber;
        startService("loadWorldFeed");

    }

    public  void loadFollowerFeed(boolean map, int pageNumber, LoadFeedCallback callback, Activity activity){
        context = activity;
        ParseOperation.loadFeedCallback = callback;
        ParseOperation.pageNumber = pageNumber;
        startService("loadFollowerFeed");

    }

    public static void loadInitialComments(LoadCommentsCallback callback, Activity activity){
        context = activity;
        ParseOperation.loadCommentsCallback = callback;
        startService("loadInitialComments");
    }

    public static void loadComments(int pageNumber, LoadCommentsCallback callback, Activity activity){
        context = activity;
        ParseOperation.loadCommentsCallback = callback;
        ParseOperation.pageNumber = pageNumber;
        startService("loadComments");
    }

    public static void createComment(String commentText, ParseOperationCallback callback, Activity activity){
        context = activity;
        ParseOperation.comment = commentText;
        ParseOperation.parseOperationCallback = callback;
        startService("saveComment");
    }

    public static void createHeart(Post post, ParseOperationCallback callback, Activity activity){
        context = activity;
        ParseOperation.post = post;
        ParseOperation.parseOperationCallback = callback;
        startService("createFavorite");
    }

    public static void deleteHeart(Post post, ParseOperationCallback callback, Activity activity){
        context = activity;
        ParseOperation.post = post;
        ParseOperation.parseOperationCallback = callback;
        startService("deleteFavorite");

    }

    public  void createUpvote(Post post, ParseOperationCallback callback, Activity activity){
        context = activity;
        ParseOperation.post = post;
        ParseOperation.parseOperationCallback = callback;
        startService("createUpvote");
    }

    public  void deleteUpvote(Post post, ParseOperationCallback callback, Activity activity){
        context = activity;
        ParseOperation.post = post;
        ParseOperation.parseOperationCallback = callback;
        startService("deleteUpvote");
    }

    public static void createFollow(ParseUser user, Follow follow, CreateFollowCallback callback, Activity activity){
        context = activity;
        ParseOperation.parseUser = user;
        ParseOperation.createFollowCallback = callback;
        ParseOperation.follow = follow;
        startService("createFollow");
    }

    public static void deleteFollow(ParseUser user, Follow follow, CreateFollowCallback callback, Activity activity){
        context = activity;
        ParseOperation.parseUser = user;
        ParseOperation.createFollowCallback = callback;
        ParseOperation.follow = follow;
        startService("deleteFollow");
    }

    public static void getFeed(int pageNumber, LoadFeedCallback callback, Activity activity){
        context = activity;
        loadFeedCallback = callback;
        ParseOperation.pageNumber = pageNumber;
        startService("getFeed");
    }

    public static void getEventsFeed(int pageNumber, LoadEventsCallback callback, Activity activity){
        context = activity;
        loadEventsCallback = callback;
        ParseOperation.pageNumber = pageNumber;
        startService("getEvents");
    }

    public static void getFollowers(GetUsersCallback callback, Activity activity){
        context = activity;
        followerCallback = callback;
        startService("getFollowers");
    }

    public static void getFollowing(GetUsersCallback callback, Activity activity){
        context = activity;
        followingCallback = callback;
        startService("getFollowing");
    }

    public static void getDiscovery(GetUsersCallback callback, Activity activity){
        context = activity;
        discoveryCallback = callback;
        startService("getDiscovery");
    }

    private static void startService(String type){
        System.out.println("Start service called");
        Intent i = new Intent(context, Network.class);
        i.putExtra("type", type);
        context.startService(i);
    }

}
