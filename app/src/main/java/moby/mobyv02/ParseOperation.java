package moby.mobyv02;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.parse.FunctionCallback;
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

    private static ParseObject parseObject;
    private static ParseUser parseUser;
    private static ParseFile parseFile;
    private static Post post;
    private static ParseOperationCallback parseOperationCallback;
    private static ImageUploadCallback imageUploadCallback;
    private static LoadFeedCallback loadFeedCallback;
    private static LoadEventsCallback loadEventsCallback;
    private static LoadCommentsCallback loadCommentsCallback;
    private static Activity context;
    private static String username;
    private static String password;
    private static String comment;
    private static int pageNumber;
    private static boolean map;
    private static File file;

    public ParseOperation(String name){

    }

    public ParseOperation(){

    }


    public static interface ParseOperationCallback {
        void finished(boolean success, ParseException e);
    }

    public static interface ImageUploadCallback {
        void finished(boolean success, ParseFile file, ParseException e);
    }

    public static interface LoadFeedCallback {
        void finished(boolean success, ArrayList<Post> posts, ParseException e);
    }

    public static interface LoadEventsCallback {
        void finished(boolean success, ArrayList<Event> events, ParseException e);
    }

    public static interface LoadCommentsCallback {
        void finished(boolean success, List<Comment> comments, ParseException e);
    }

    public static void saveParseObject(ParseObject parseObject, ParseOperationCallback callback, Activity activity){
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

    public static void saveParseFile(ParseFile parseFile, ParseOperationCallback callback, Activity activity){
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

    public static void loadInitialWorldFeed(boolean map, LoadFeedCallback callback, Activity activity){
        context = activity;
        ParseOperation.loadFeedCallback = callback;
        startService("loadInitialWorldFeed");
    }

    public static void loadInitialFollowerFeed(boolean map, LoadFeedCallback callback, Activity activity){
        context = activity;
        ParseOperation.loadFeedCallback = callback;
        startService("loadInitialFollowerFeed");
    }

    public static void loadWorldFeed(boolean map, int pageNumber, LoadFeedCallback callback, Activity activity){
        context = activity;
        ParseOperation.loadFeedCallback = callback;
        ParseOperation.pageNumber = pageNumber;
        startService("loadWorldFeed");

    }

    public static void loadFollowerFeed(boolean map, int pageNumber, LoadFeedCallback callback, Activity activity){
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

    public static void createUpvote(Post post, ParseOperationCallback callback, Activity activity){
        context = activity;
        ParseOperation.post = post;
        ParseOperation.parseOperationCallback = callback;
        startService("createUpvote");
    }

    public static void deleteUpvote(Post post, ParseOperationCallback callback, Activity activity){
        context = activity;
        ParseOperation.post = post;
        ParseOperation.parseOperationCallback = callback;
        startService("deleteUpvote");
    }

    public static void createFollow(ParseUser user, ParseOperationCallback callback, Activity activity){
        context = activity;
        ParseOperation.parseUser = user;
        ParseOperation.parseOperationCallback = callback;
        startService("createFollow");
    }

    public static void deleteFollow(ParseUser user, ParseOperationCallback callback, Activity activity){
        context = activity;
        ParseOperation.parseUser = user;
        ParseOperation.parseOperationCallback = callback;
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


    private static void startService(String type){
        Intent i = new Intent(context, Network.class);
        i.putExtra("type", type);
        context.startService(i);
    }


    public static class Network extends IntentService {


        public Network(){
            super("Network");
        }

        public Network(String name) {
            super(name);
        }



        @Override
        protected void onHandleIntent(Intent intent) {

            System.out.println("Handle Intent");
            String type = intent.getStringExtra("type");

            if (type.equals("saveParseObject")){

                saveParseObject(parseObject, parseOperationCallback);

            } else if (type.equals("savePost")){

                savePost(post, parseOperationCallback);

            } else if (type.equals("uploadImage")){

                uploadImage(file, imageUploadCallback);

            } else if (type.equals("saveParseFile")){

                saveParseFile(parseFile, parseOperationCallback);

            } else if (type.equals("logIn")){

                logIn(username, password, parseOperationCallback);

            } else if (type.equals("signUp")){

                signUp(parseUser, parseOperationCallback);

            } else if (type.equals("loadInitialWorldFeed")){

                loadInitialWorldFeed(map, loadFeedCallback);

            } else if (type.equals("loadInitialFollowerFeed")){

//                loadInitialFollowerFeed(map, loadFeedCallback);

            } else if (type.equals("loadWorldFeed")){

                loadWorldFeed(map, pageNumber, loadFeedCallback);

            } else if (type.equals("loadFollowerFeed")){

//                loadFollowerFeed(map, pageNumber, loadFeedCallback);

            } else if (type.equals("loadInitialComments")){

                loadInitialComments(loadCommentsCallback);

            } else if (type.equals("loadComments")){

                loadComments(pageNumber, loadCommentsCallback);

            } else if (type.equals("saveComment")){

                createComment(comment, CommentActivity.currentPost, parseOperationCallback);

            } else if (type.equals("createFavorite")){

                createHeart(post, parseOperationCallback);

            } else if (type.equals("deleteFavorite")){

                deleteHeart(post, parseOperationCallback);

            } else if (type.equals("createUpvote")){

                createUpvote(post, parseOperationCallback);

            } else if (type.equals("deleteUpvote")){

                deleteUpvote(post, parseOperationCallback);

            } else if (type.equals("createFollow")){

                createFollow(parseUser, parseOperationCallback);

            } else if (type.equals("deleteFollow")){

                deleteFollow(parseUser, parseOperationCallback);

            } else if (type.equals("getFeed")){
                getFeed(pageNumber, loadFeedCallback);

            } else if (type.equals("getEvents")){
                getEventsFeed(pageNumber, loadEventsCallback);
            }

        }

        private void saveParseObject(ParseObject parseObject, final ParseOperationCallback callback){
            try {

                parseObject.save();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(true, null);
                    }
                });


            } catch (final com.parse.ParseException e) {
                e.printStackTrace();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(false, e);
                    }
                });
            }

        }

        private void savePost(Post post, final ParseOperationCallback callback){
            try {

                post.save();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(true, null);
                    }
                });
            } catch (final com.parse.ParseException e) {
                e.printStackTrace();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(false, e);
                    }
                });

            }

        }

        private void uploadImage(File file, final ImageUploadCallback callback){

            Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            final ParseFile parseFile = new ParseFile("image.jpg", baos.toByteArray());
            try {
                parseFile.save();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(true, parseFile, null);
                    }
                });
            } catch (final ParseException e) {
                e.printStackTrace();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(false, parseFile, e);
                    }
                });
            }

        }

        private void uploadImage(byte[] file, final ImageUploadCallback callback){

            final ParseFile parseFile = new ParseFile("image.jpg", file);
            try {
                parseFile.save();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(true, parseFile, null);
                    }
                });
            } catch (final ParseException e) {
                e.printStackTrace();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(false, parseFile, e);
                    }
                });
            }

        }

        private void saveParseFile(ParseFile parseFile, final ParseOperationCallback callback){
            try {

                parseFile.save();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(true, null);
                    }
                });
            } catch (final ParseException e) {
                e.printStackTrace();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(false, e);
                    }
                });
            }
        }

        private void logIn(String username, String password, final ParseOperationCallback callback){

            try {
                final ParseUser user = ParseUser.logIn(username, password);
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(true, null);
                    }
                });
            } catch (final ParseException e) {
                e.printStackTrace();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(false, e);
                    }
                });
            }

        }

        private void signUp(final ParseUser parseUser, final ParseOperationCallback callback){
            try {

                parseUser.signUp();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(true, null);
                    }
                });
            } catch (final ParseException e) {
                e.printStackTrace();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(false, e);
                    }
                });
            }

        }

        private void loadInitialWorldFeed(boolean map, final LoadFeedCallback callback){

            ParseQuery<Post> query = Post.getQuery();
            ParseGeoPoint location = LocationManager.getLocation();
            query.include("user");
            query.whereNear("location", new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
            query.whereWithinMiles("location", new ParseGeoPoint(location.getLatitude(), location.getLongitude()), 5);
            query.setLimit(10);
            try {
                final List<Post> results = query.find();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
       //                 callback.finished(true, results, null);
                    }
                });
            } catch (final ParseException e) {
                e.printStackTrace();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(false, null, e);
                    }
                });
            }
        }

        private void loadWorldFeed(boolean map, int pageNumber, final LoadFeedCallback callback){

            ParseQuery<Post> query = Post.getQuery();
            ParseGeoPoint location = LocationManager.getLocation();
            query.include("user");
 //           query.whereNear("location", new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
            if (!map) {
                query.setLimit(300);
            } else {
                query.setLimit(300);
            }
        //    query.whereWithinMiles("location", new ParseGeoPoint(21.0000, 78.0000), 3900);
            query.setSkip(300 * pageNumber);
            try {
                final List<Post> posts = query.find();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
           //             callback.finished(true, posts, null);
                    }
                });
            } catch (final ParseException e) {
                e.printStackTrace();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
           //             callback.finished(false, null, e);
                    }
                });
            }

        }

        private void loadInitialComments(final LoadCommentsCallback callback){

            ParseQuery<Comment> query = Comment.getQuery();
            query.whereEqualTo("post", CommentActivity.currentPost);
            query.include("user");
            query.setLimit(20);
            try {
                final List<Comment> comments = query.find();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(true, comments, null);
                    }
                });
            } catch (final ParseException e) {
                e.printStackTrace();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(false, null, e);
                    }
                });
            }

        }

        private void loadComments(int pageNumber,final LoadCommentsCallback callback){

            ParseQuery<Comment> query = Comment.getQuery();
            query.whereEqualTo("post", CommentActivity.currentPost);
            query.include("user");
            query.setLimit(100);
            query.setSkip(100 * pageNumber);
            try {
                final List<Comment> comments = query.find();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(true, comments, null);
                    }
                });
            } catch (final ParseException e) {
                e.printStackTrace();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(false, null, e);
                    }
                });
            }

        }

        private void createHeart(Post post, final ParseOperationCallback callback){

            Heart heart = new Heart();
            heart.setPost(post);
            heart.setUser(ParseUser.getCurrentUser());
            try {
                heart.save();
                heart.pin();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(true, null);
                    }
                });
            } catch (final ParseException e) {
                e.printStackTrace();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(false, e);
                    }
                });
            }

        }

        private void deleteHeart(Post post, final ParseOperationCallback callback){

            ParseQuery<Heart> query = Heart.getQuery();
            query.whereEqualTo("post", post);
            query.whereEqualTo("user", ParseUser.getCurrentUser());
            query.fromLocalDatastore();
            try {
                Heart heart = query.getFirst();
                heart.delete();
                heart.unpin();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(true, null);
                    }
                });
            } catch (final ParseException e) {
                e.printStackTrace();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(false, e);
                    }
                });
            }

        }

        private void createComment(String text, Post post, final ParseOperationCallback callback){

            Comment comment = new Comment();
            comment.setUser(ParseUser.getCurrentUser());
            comment.setPost(post);
            comment.setText(text);
            post.setComments(post.getComments() + 1);
            post.saveEventually();
            try {
                comment.save();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(true, null);
                    }
                });
            } catch (final ParseException e) {
                e.printStackTrace();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(false, e);
                    }
                });
            }


        }

        private void createUpvote(Post post, final ParseOperationCallback callback){

            Upvote upvote = new Upvote();
            upvote.setPost(post);
            upvote.setUser(ParseUser.getCurrentUser());
            try {
                upvote.save();
                upvote.pin();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(true, null);
                    }
                });
            } catch (final ParseException e) {
                e.printStackTrace();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(false, e);
                    }
                });
            }

        }

        private void deleteUpvote(Post post, final ParseOperationCallback callback){

            ParseQuery<Upvote> query = Upvote.getQuery();
            query.whereEqualTo("post", post);
            query.whereEqualTo("user", ParseUser.getCurrentUser());
            try {
                Upvote upvote = query.getFirst();
                upvote.delete();
                upvote.unpin();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(true, null);
                    }
                });
            } catch (final ParseException e) {
                e.printStackTrace();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(false, e);
                    }
                });
            }

        }

        private void createFollow(ParseUser user, final ParseOperationCallback callback) {

            Follow follow = new Follow();
            follow.setTo(user);
            follow.setFrom(ParseUser.getCurrentUser());
            try {
                follow.save();
                follow.pin();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(true, null);
                    }
                });
            } catch (final ParseException e) {
                e.printStackTrace();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(false, e);
                    }
                });
            }

        }

        private void deleteFollow(ParseUser user, final ParseOperationCallback callback) {

            ParseQuery<Follow> query = Follow.getQuery();
            query.whereEqualTo("fromUser", ParseUser.getCurrentUser());
            query.whereEqualTo("toUser", user);
            query.fromLocalDatastore();
            try {
                Follow follow = query.getFirst();
                follow.delete();
                follow.unpin();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(true, null);
                    }
                });
            } catch (final ParseException e) {
                e.printStackTrace();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(false, e);
                    }
                });
            }

        }

        private void getFeed(int pageNumber, final LoadFeedCallback callback){
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("location", LocationManager.getLocation());
            params.put("pageNumber", pageNumber);
            ParseCloud.callFunctionInBackground("getFeed", params, new FunctionCallback<String>() {
                @Override
                public void done(String s, final ParseException e) {
                    if (e != null) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.finished(false, null, e);
                            }
                        });
                    } else {
                        JSONArray postsJsonArray = null;
                        try {
                            postsJsonArray = new JSONArray(s);
                            ArrayList<Post> postPointerList = new ArrayList<Post>();
                            ArrayList<ParseUser> userPointerList = new ArrayList<ParseUser>();
                            ArrayList<String> objectIds = new ArrayList<String>();
                            for (int x = 0; x < postsJsonArray.length(); x++) {
                                objectIds.add(postsJsonArray.getJSONObject(x).getString("objectId"));
                                Post object = ParseObject.createWithoutData(Post.class, postsJsonArray.getJSONObject(x).getString("objectId"));
                                postPointerList.add(object);
                            }
                            System.out.println("Objectid's length is " + objectIds.size());
                            ParseQuery<Post> query = Post.getQuery();
                            query.whereContainedIn("objectId", objectIds);
                            query.include("user");
                            final ArrayList<Post> postsList = new ArrayList<Post>(query.find());
                            System.out.println(postsList.size());
                            Collections.sort(postsList);
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.finished(true, postsList, null);
                                }
                            });
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        } catch (final ParseException e1) {
                            e1.printStackTrace();
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.finished(false, null, e1);
                                }
                            });
                        }
                    }


                }
            });

        }

        private void getEventsFeed(int pageNumber, final LoadEventsCallback callback){
            System.out.println("getEventsFeed");
            String urlString = context.getString(R.string.eventbrite_url);
            String sortParam = "sort_by=distance";
            String latParam = "location.latitude=" + LocationManager.getLocation().getLatitude();
            String lonParam = "location.longitude=" + LocationManager.getLocation().getLongitude();
            String expansionParam = "expand=venue";
            String tokenParam = "token=" + context.getString(R.string.eventbrite_token);
            urlString+= sortParam + "&" + latParam + "&" + lonParam + "&" + expansionParam + "&" + tokenParam;
            URL url;
            HttpURLConnection connection;
            try {
                url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                InputStream is = connection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                String responseString = response.toString();
                JSONObject responseObj = new JSONObject(responseString);
                JSONArray events = responseObj.getJSONArray("events");
                final ArrayList<Event> eventsList = new ArrayList<Event>();
                for (int x = 0; x < events.length(); x++){
                    Event event = new Event();
                    JSONObject eventObject = events.getJSONObject(x);
                    event.setName(eventObject.getJSONObject("name").getString("text"));
                    event.setText(eventObject.getJSONObject("description").getString("text"));
                    event.setUrl(eventObject.getString("url"));
                    if (!eventObject.isNull("logo"))
                    event.setImage(eventObject.getJSONObject("logo").getString("url"));
                    JSONObject venue = eventObject.getJSONObject("venue");
                    event.setLocale(venue.getJSONObject("address").getString("city") + ", " + venue.getJSONObject("address").getString("region"));
                    ParseGeoPoint location = new ParseGeoPoint();
                    location.setLatitude(venue.getJSONObject("address").getDouble("latitude"));
                    location.setLongitude(venue.getJSONObject("address").getDouble("longitude"));
                    eventsList.add(event);
                }

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.finished(true, eventsList, null);
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }

}
