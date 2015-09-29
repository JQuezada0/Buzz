package moby.mobyv02;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.leanplum.Leanplum;
import com.parse.FindCallback;
import com.parse.ParseCloud;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import com.parse.ParseException;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import moby.mobyv02.parse.Comment;
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
    private static LoadCommentsCallback loadCommentsCallback;
    private static String filePath;
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
        void finished(boolean success, List<Post> posts, ParseException e);
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

    public static void uploadImage(String file, ImageUploadCallback callback, Activity activity){
        context = activity;
        System.out.println("uploadImage called");
        ParseOperation.filePath = file;
        ParseOperation.imageUploadCallback = callback;
        startService("uploadImage");
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
                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put("user", user.getObjectId());
                        params.put("method", "Buzz");
                        Leanplum.track("login", params);
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
                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put("user", parseUser.getObjectId());
                        params.put("method", "Buzz");
                        Leanplum.track("registration", params);
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
                        callback.finished(true, results, null);
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
                        callback.finished(true, posts, null);
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
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("post", post.getObjectId());
                Leanplum.track("Hearts", params);
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
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("post", post.getObjectId());
                Leanplum.track("Comments", params);
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
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("post", post.getObjectId());
                params.put("type", "upvote");
                Leanplum.track("Upvotes", params);
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
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("post", post.getObjectId());
                params.put("type", "downvote");
                Leanplum.track("Upvotes", params);
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
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("post", user.getObjectId());
                params.put("type", "follow");
                Leanplum.track("Follows", params);
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
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("post", user.getObjectId());
                params.put("type", "unfollow");
                Leanplum.track("Follows", params);
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
            try {
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("pageNumber", pageNumber);
                params.put("location", LocationManager.getLocation());
                String postsJsonString = ParseCloud.callFunction("getPost", params);
                JSONArray postsJsonArray = new JSONArray(postsJsonString);
                ArrayList<Post> postPointerList = new ArrayList<Post>();
                ArrayList<ParseUser> userPointerList = new ArrayList<ParseUser>();
                ArrayList<ParseUser> userList = new ArrayList<ParseUser>();
                for (int x = 0; x < postsJsonArray.length(); x++){
                    Post object = ParseObject.createWithoutData(Post.class, postsJsonArray.getJSONObject(x).getString("objectId"));
                    ParseUser user = ParseUser.createWithoutData(ParseUser.class, postsJsonArray.getJSONObject(x).getJSONObject("user").getString("objectId"));
                    postPointerList.add(object);
                    userPointerList.add(user);
                }
                ParseQuery<Post> query = Post.getQuery();
                query.whereContainedIn("objectId", postPointerList);
                ArrayList<Post> postList = new ArrayList<Post>(query.find());
                System.out.println(postList.size());
                callback.finished(true, postList, null);
                //ParseUser.fetchAll(userPointerList);
                for (int x = 0; x < postList.size(); x++){
                    postList.get(x).setUser(userList.get(x));
                }

            } catch (ParseException e) {
                e.printStackTrace();
                callback.finished(false, null, e);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


    }

}
