package moby.mobyv02;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import moby.mobyv02.parse.Post;

/**
 * Created by quezadjo on 9/27/2015.
 */
public class ParseDecoder {

    private final JSONObject data;
    private String className;

    public ParseDecoder(JSONObject po) throws JSONException {

        data = po;
//        className = po.getString("className");

    }

    public ParseObject decode(){


        return null;
    }

    public Post decodePost() throws JSONException, ParseException {

        Post post = new Post();
        Iterator<String> keys = data.keys();
        while (keys.hasNext()){
            String key = keys.next();
            post.put(key, data.get(key));
        }
        post.put("createdDate", DateParser.parse(data.getJSONObject("createdAt").getString("iso")));
        post.put("updatedAt", DateParser.parse(data.getJSONObject("updatedAt").getString("iso")));
        post.setLocation(getLocation(data.getJSONObject("location")));
        post.put("user", decodeUser());
        return post;
    }

    private ParseGeoPoint getLocation(JSONObject data) throws JSONException {
        ParseGeoPoint location = new ParseGeoPoint();
        location.setLatitude(data.getDouble("latitude"));
        location.setLongitude(data.getDouble("longitude"));
        return location;
    }

    public ParseUser decodeUser() throws JSONException, ParseException {

        ParseUser user = new ParseUser();
        Iterator<String> keys = data.keys();
        while (keys.hasNext()){
            String key = keys.next();
            user.put(key, data.get(key));
            System.out.println(key + " : " + data.get(key));
        }
        user.put("createdAt", DateParser.parse(data.getJSONObject("updatedAt").getString("iso")));
        user.put("updatedAt", DateParser.parse(data.getJSONObject("createdAt").getString("iso")));
        return user;
    }

}
