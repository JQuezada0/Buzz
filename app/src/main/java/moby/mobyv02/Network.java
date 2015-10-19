package moby.mobyv02;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import moby.mobyv02.parse.Comment;
import moby.mobyv02.parse.Event;
import moby.mobyv02.parse.Follow;
import moby.mobyv02.parse.Friend;
import moby.mobyv02.parse.Heart;
import moby.mobyv02.parse.Post;
import moby.mobyv02.parse.Upvote;
import moby.mobyv02.ParseOperation;

/**
 * Created by Johnil on 10/9/2015.
 */
public class Network extends IntentService {

    public static ParseOperation parseOperation;

    public Network(){
        super("Network");
    }

    public Network(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        System.out.println("Process started");
        String type = intent.getStringExtra("type");

        if (type.equals("saveParseObject")){

            saveParseObject(parseOperation.parseObject, parseOperation.parseOperationCallback);

        } else if (type.equals("savePost")){

            savePost(parseOperation.post, parseOperation.parseOperationCallback);

        } else if (type.equals("uploadImage")){

            uploadImage(parseOperation.file, parseOperation.imageUploadCallback);

        } else if (type.equals("saveParseFile")){

            saveParseFile(parseOperation.parseFile, parseOperation.parseOperationCallback);

        } else if (type.equals("logIn")){

            logIn(parseOperation.username, parseOperation.password, parseOperation.parseOperationCallback);

        } else if (type.equals("signUp")){

            signUp(parseOperation.parseUser, parseOperation.parseOperationCallback);

        } else if (type.equals("loadInitialWorldFeed")){

            loadInitialWorldFeed(parseOperation.map, parseOperation.loadFeedCallback);

        } else if (type.equals("loadInitialFollowerFeed")){

//                loadInitialFollowerFeed(map, loadFeedCallback);

        } else if (type.equals("loadWorldFeed")){

            loadWorldFeed(parseOperation.map, parseOperation.pageNumber, parseOperation.loadFeedCallback);

        } else if (type.equals("loadFollowerFeed")){

//                loadFollowerFeed(map, pageNumber, loadFeedCallback);

        } else if (type.equals("loadInitialComments")){

            loadInitialComments(parseOperation.loadCommentsCallback);

        } else if (type.equals("loadComments")){

            loadComments(parseOperation.pageNumber, parseOperation.loadCommentsCallback);

        } else if (type.equals("saveComment")){

            createComment(parseOperation.comment, CommentActivity.currentPost, parseOperation.parseOperationCallback);

        } else if (type.equals("createFavorite")){

            createHeart(parseOperation.post, parseOperation.parseOperationCallback);

        } else if (type.equals("deleteFavorite")){

            deleteHeart(parseOperation.post, parseOperation.parseOperationCallback);

        } else if (type.equals("createUpvote")){

            createUpvote(parseOperation.post, parseOperation.parseOperationCallback);

        } else if (type.equals("deleteUpvote")){

            deleteUpvote(parseOperation.post, parseOperation.parseOperationCallback);

        } else if (type.equals("createFollow")){

            createFollow(parseOperation.parseUser, parseOperation.follow, parseOperation.createFollowCallback);

        } else if (type.equals("deleteFollow")){

            deleteFollow(parseOperation.parseUser, parseOperation.follow, parseOperation.createFollowCallback);

        } else if (type.equals("getFeed")){
            getFeed(parseOperation.pageNumber, parseOperation.loadFeedCallback);

        } else if (type.equals("getEvents")){

            getEventsFeed(parseOperation.pageNumber, parseOperation.loadEventsCallback);

        } else if (type.equals("getFollowing")){

            getFollowing(parseOperation.followingCallback);

        } else if (type.equals("getFollowers")){

            getFollowers(parseOperation.followerCallback);

        } else if (type.equals("getDiscovery")){

            getDiscoveryFeed(parseOperation.discoveryCallback);

        } else if (type.equals("getFriends")){

            getFriends(parseOperation.getFriendsCallback);

        } else if (type.equals("getPendingFriends")){

            getPendingFriends(parseOperation.getPendingFriendsCallback);

        } else if (type.equals("getReceivedFriends")){

            getReceivedFriends(parseOperation.getReceivedFriendsCallback);

        }

    }

    private void saveParseObject(ParseObject parseObject, final ParseOperation.ParseOperationCallback callback){
        try {

            parseObject.save();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(true, null);
                }
            });


        } catch (final com.parse.ParseException e) {
            e.printStackTrace();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(false, e);
                }
            });
        }

    }

    private void savePost(Post post, final ParseOperation.ParseOperationCallback callback){
        try {

            post.save();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(true, null);
                }
            });
        } catch (final com.parse.ParseException e) {
            e.printStackTrace();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(false, e);
                }
            });

        }

    }

    private void uploadImage(File file, final ParseOperation.ImageUploadCallback callback){

        Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        final ParseFile parseFile = new ParseFile("image.jpg", baos.toByteArray());
        try {
            parseFile.save();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(true, parseFile, null);
                }
            });
        } catch (final ParseException e) {
            e.printStackTrace();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(false, parseFile, e);
                }
            });
        }

    }

    private void uploadImage(byte[] file, final ParseOperation.ImageUploadCallback callback){

        final ParseFile parseFile = new ParseFile("image.jpg", file);
        try {
            parseFile.save();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(true, parseFile, null);
                }
            });
        } catch (final ParseException e) {
            e.printStackTrace();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(false, parseFile, e);
                }
            });
        }

    }

    private void saveParseFile(ParseFile parseFile, final ParseOperation.ParseOperationCallback callback){
        try {

            parseFile.save();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(true, null);
                }
            });
        } catch (final ParseException e) {
            e.printStackTrace();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(false, e);
                }
            });
        }
    }

    private void logIn(String username, String password, final ParseOperation.ParseOperationCallback callback){

        try {
            final ParseUser user = ParseUser.logIn(username, password);
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(true, null);
                }
            });
        } catch (final ParseException e) {
            e.printStackTrace();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(false, e);
                }
            });
        }

    }

    private void signUp(final ParseUser parseUser, final ParseOperation.ParseOperationCallback callback){
        try {

            parseUser.signUp();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(true, null);
                }
            });
        } catch (final ParseException e) {
            e.printStackTrace();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(false, e);
                }
            });
        }

    }

    private void loadInitialWorldFeed(boolean map, final ParseOperation.LoadFeedCallback callback){

        ParseQuery<Post> query = Post.getQuery();
        ParseGeoPoint location = LocationManager.getLocation();
        query.include("user");
        query.whereNear("location", new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
        query.whereWithinMiles("location", new ParseGeoPoint(location.getLatitude(), location.getLongitude()), 5);
        query.setLimit(10);
        try {
            final List<Post> results = query.find();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //                 callback.finished(true, results, null);
                }
            });
        } catch (final ParseException e) {
            e.printStackTrace();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(false, null, e);
                }
            });
        }
    }

    private void loadWorldFeed(boolean map, int pageNumber, final ParseOperation.LoadFeedCallback callback){

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
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //             callback.finished(true, posts, null);
                }
            });
        } catch (final ParseException e) {
            e.printStackTrace();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //             callback.finished(false, null, e);
                }
            });
        }

    }

    private void loadInitialComments(final ParseOperation.LoadCommentsCallback callback){

        ParseQuery<Comment> query = Comment.getQuery();
        query.whereEqualTo("post", CommentActivity.currentPost);
        query.include("user");
        query.setLimit(20);
        try {
            final List<Comment> comments = query.find();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(true, comments, null);
                }
            });
        } catch (final ParseException e) {
            e.printStackTrace();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(false, null, e);
                }
            });
        }

    }

    private void loadComments(int pageNumber,final ParseOperation.LoadCommentsCallback callback){

        ParseQuery<Comment> query = Comment.getQuery();
        query.whereEqualTo("post", CommentActivity.currentPost);
        query.include("user");
        query.setLimit(100);
        query.setSkip(100 * pageNumber);
        try {
            final List<Comment> comments = query.find();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(true, comments, null);
                }
            });
        } catch (final ParseException e) {
            e.printStackTrace();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(false, null, e);
                }
            });
        }

    }

    private void createHeart(Post post, final ParseOperation.ParseOperationCallback callback){

        Heart heart = new Heart();
        heart.setPost(post);
        heart.setUser(ParseUser.getCurrentUser());
        try {
            heart.save();
            heart.pin();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(true, null);
                }
            });
        } catch (final ParseException e) {
            e.printStackTrace();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(false, e);
                }
            });
        }

    }

    private void deleteHeart(Post post, final ParseOperation.ParseOperationCallback callback){

        ParseQuery<Heart> query = Heart.getQuery();
        query.whereEqualTo("post", post);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.fromLocalDatastore();
        try {
            Heart heart = query.getFirst();
            heart.delete();
            heart.unpin();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(true, null);
                }
            });
        } catch (final ParseException e) {
            e.printStackTrace();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(false, e);
                }
            });
        }

    }

    private void createComment(String text, Post post, final ParseOperation.ParseOperationCallback callback){

        Comment comment = new Comment();
        comment.setUser(ParseUser.getCurrentUser());
        comment.setPost(post);
        comment.setText(text);
        post.setComments(post.getComments() + 1);
        post.saveEventually();
        try {
            comment.save();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(true, null);
                }
            });
        } catch (final ParseException e) {
            e.printStackTrace();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(false, e);
                }
            });
        }


    }

    private void createUpvote(Post post, final ParseOperation.ParseOperationCallback callback){

        Upvote upvote = new Upvote();
        upvote.setPost(post);
        upvote.setUser(ParseUser.getCurrentUser());
        try {
            upvote.save();
            upvote.pin();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(true, null);
                }
            });
        } catch (final ParseException e) {
            e.printStackTrace();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(false, e);
                }
            });
        }

    }

    private void deleteUpvote(Post post, final ParseOperation.ParseOperationCallback callback){

        ParseQuery<Upvote> query = Upvote.getQuery();
        query.whereEqualTo("post", post);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        try {
            Upvote upvote = query.getFirst();
            upvote.delete();
            upvote.unpin();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(true, null);
                }
            });
        } catch (final ParseException e) {
            e.printStackTrace();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(false, e);
                }
            });
        }

    }

    private void createFollow(ParseUser user, final Follow follow, final ParseOperation.CreateFollowCallback callback) {
        try {
            follow.save();
            follow.pin();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(true, follow, null);
                }
            });
        } catch (final ParseException e) {
            e.printStackTrace();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(false, null, e);
                }
            });
        }

    }

    private void deleteFollow(ParseUser user, final Follow follow, final ParseOperation.CreateFollowCallback callback) {
        try {
            follow.delete();
            follow.unpin();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(true, follow, null);
                }
            });
        } catch (final ParseException e) {
            e.printStackTrace();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(false, null, e);
                }
            });
        }

    }



    private void getFeed(int pageNumber, final ParseOperation.LoadFeedCallback callback){
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("location", LocationManager.getLocation());
        params.put("pageNumber", pageNumber);
        ParseCloud.callFunctionInBackground("getFeed", params, new FunctionCallback<String>() {
            @Override
            public void done(String s, final ParseException e) {
                if (e != null) {
                    parseOperation.context.runOnUiThread(new Runnable() {
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
                        parseOperation.context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.finished(true, postsList, null);
                            }
                        });
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    } catch (final ParseException e1) {
                        e1.printStackTrace();
                        parseOperation.context.runOnUiThread(new Runnable() {
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

    private void getEventsFeed(int pageNumber, final ParseOperation.LoadEventsCallback callback){
        System.out.println("getEventsFeed");
        String urlString = parseOperation.context.getString(R.string.eventbrite_url);
        String sortParam = "sort_by=distance";
        String latParam = "location.latitude=" + LocationManager.getLocation().getLatitude();
        String lonParam = "location.longitude=" + LocationManager.getLocation().getLongitude();
        String expansionParam = "expand=venue";
        String tokenParam = "token=" + parseOperation.context.getString(R.string.eventbrite_token);
        urlString+= sortParam + "&" + latParam + "&" + lonParam + "&" + expansionParam + "&" + tokenParam;
        URL url;
        HttpURLConnection connection;
        try {
            System.out.println(urlString);
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
                event.setLocation(location);
                JSONObject start = eventObject.getJSONObject("start");
                String startTime = start.getString("local");
                System.out.println(DateParser.parse(startTime));
                event.setTime(DateParser.parse(startTime).getTime());
                eventsList.add(event);
            }

            parseOperation.context.runOnUiThread(new Runnable() {
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
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
    }

    private void getFollowers(final ParseOperation.GetUsersCallback callback) {
        ParseQuery<Follow> userQuery = Follow.getQuery();
        userQuery.whereEqualTo("toUser", ParseUser.getCurrentUser());
        userQuery.include("fromUser");
        try {
            List<Follow> follows = userQuery.find();
            final ArrayList<ParseUser> users = new ArrayList<ParseUser>();
            for (Follow f : follows){
                users.add(f.getFrom());
            }
            Comparator<ParseUser> comparator = new Comparator<ParseUser>(){

                @Override
                public int compare(ParseUser lhs, ParseUser rhs) {
                    ParseGeoPoint userALocation = lhs.getParseGeoPoint("location");
                    ParseGeoPoint userBLocation = rhs.getParseGeoPoint("location");
                    double lat1 = userALocation.getLatitude();
                    double lon1 = userALocation.getLongitude();
                    double lat2 = userBLocation.getLatitude();
                    double lon2 = userBLocation.getLongitude();
                    double aDist = userALocation.distanceInMilesTo(LocationManager.getLocation());
                    double bDist = userBLocation.distanceInMilesTo(LocationManager.getLocation());
                    if (aDist - bDist > 0){
                        return 1;
                    } else if (aDist - bDist < 0){
                        return -1;
                    } else {
                        return 0;
                    }
                }
            };
            Collections.sort(users, comparator);
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(true, users, null);
                }
            });
        } catch (final ParseException e) {
            e.printStackTrace();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(false, null, e);
                }
            });
        }
    }

    private void getFollowing(final ParseOperation.GetUsersCallback callback) {
        ParseQuery<Follow> userQuery = Follow.getQuery();
        userQuery.whereEqualTo("fromUser", ParseUser.getCurrentUser());
        userQuery.include("toUser");
        try {
            List<Follow> follows = userQuery.find();
            final ArrayList<ParseUser> users = new ArrayList<ParseUser>();
            for (Follow f : follows){
                users.add(f.getTo());
            }
            Comparator<ParseUser> comparator = new Comparator<ParseUser>(){

                @Override
                public int compare(ParseUser lhs, ParseUser rhs) {
                    ParseGeoPoint userALocation = lhs.getParseGeoPoint("location");
                    ParseGeoPoint userBLocation = rhs.getParseGeoPoint("location");
                    double lat1 = userALocation.getLatitude();
                    double lon1 = userALocation.getLongitude();
                    double lat2 = userBLocation.getLatitude();
                    double lon2 = userBLocation.getLongitude();
                    double aDist = userALocation.distanceInMilesTo(LocationManager.getLocation());
                    double bDist = userBLocation.distanceInMilesTo(LocationManager.getLocation());
                    if (aDist - bDist > 0){
                        return 1;
                    } else if (aDist - bDist < 0){
                        return -1;
                    } else {
                        return 0;
                    }
                }
            };
            Collections.sort(users, comparator);
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(true, users, null);
                }
            });
        } catch (final ParseException e) {
            e.printStackTrace();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(false, null, e);
                }
            });
        }
    }

    private void getDiscoveryFeed(final ParseOperation.GetUsersCallback callback){

        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.addDescendingOrder("posts");
        userQuery.addDescendingOrder("hearts");
        userQuery.addDescendingOrder("comments");
        userQuery.addDescendingOrder("followers");

        userQuery.whereNotEqualTo("instagram", true);
        try {
            final ArrayList<ParseUser> users = new ArrayList<>(userQuery.find());
            Comparator<ParseUser> comparator = new Comparator<ParseUser>(){

                @Override
                public int compare(ParseUser lhs, ParseUser rhs) {
                    ParseGeoPoint userALocation = lhs.getParseGeoPoint("location");
                    ParseGeoPoint userBLocation = rhs.getParseGeoPoint("location");
                    if (userALocation == null){
                        return 1;
                    }
                    if (userBLocation == null){
                        return -1;
                    }
                    if (userALocation == null && userBLocation == null){
                        return 0;
                    }
                    double aDist = userALocation.distanceInMilesTo(LocationManager.getLocation());
                    double bDist = userBLocation.distanceInMilesTo(LocationManager.getLocation());
                    if (aDist - bDist > 0){
                        return 1;
                    } else if (aDist - bDist < 0){
                        return -1;
                    } else {
                        return 0;
                    }
                }
            };
            Collections.sort(users, comparator);
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(true, users, null);
                }
            });
        } catch (final ParseException e) {
            e.printStackTrace();
            parseOperation.context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.finished(false, null, e);
                }
            });

        }
    }

    private void getFriends(final ParseOperation.GetFriendsCallback callback){
        List<ParseQuery<Friend>> queries = new ArrayList<ParseQuery<Friend>>();
        ParseQuery<Friend> friendQuery = Friend.getQuery();
        friendQuery.whereEqualTo("from", ParseUser.getCurrentUser());
        friendQuery.whereEqualTo("accepted", true);
        ParseQuery<Friend> friendQueryTo = Friend.getQuery();
        friendQueryTo.whereEqualTo("to", ParseUser.getCurrentUser());
        friendQueryTo.whereEqualTo("accepted", true);
        queries.add(friendQuery);
        queries.add(friendQueryTo);
        ParseQuery<Friend> finalQuery = ParseQuery.or(queries);
        finalQuery.include("to");
        finalQuery.include("from");
        finalQuery.findInBackground(new FindCallback<Friend>() {
            @Override
            public void done(final List<Friend> list, final ParseException e) {
                if (e == null) {

                    parseOperation.context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.finished(true, list, e);
                        }
                    });

                } else {
                    parseOperation.context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.finished(false, null, e);
                        }
                    });
                }
            }
        });
    }

    private void getPendingFriends(final ParseOperation.GetFriendsCallback callback){

        ParseQuery<Friend> friendQuery = Friend.getQuery();
        friendQuery.whereEqualTo("from", ParseUser.getCurrentUser());
        friendQuery.whereEqualTo("accepted", false);
        friendQuery.whereEqualTo("rejected", false);
        friendQuery.include("to");
        friendQuery.findInBackground(new FindCallback<Friend>() {
            @Override
            public void done(final List<Friend> list, final ParseException e) {
                if (e == null){

                    parseOperation.context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.finished(true, list, e);
                        }
                    });

                } else {
                    parseOperation.context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.finished(false, null, e);
                        }
                    });
                }
            }
        });
    }

    private void getReceivedFriends(final ParseOperation.GetFriendsCallback callback){

        ParseQuery<Friend> friendQuery = Friend.getQuery();
        friendQuery.whereEqualTo("to", ParseUser.getCurrentUser());
        friendQuery.whereEqualTo("accepted", false);
        friendQuery.whereEqualTo("rejected", false);
        friendQuery.include("from");
        friendQuery.findInBackground(new FindCallback<Friend>() {
            @Override
            public void done(final List<Friend> list, final ParseException e) {
                if (e == null){

                    parseOperation.context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.finished(true, list, e);
                        }
                    });

                } else {
                    parseOperation.context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.finished(false, null, e);
                        }
                    });
                }
            }
        });
    }


}