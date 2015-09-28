package moby.mobyv02.parse;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterItem;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import moby.mobyv02.LocationManager;

/**
 * Created by quezadjo on 9/8/2015.
 */

@ParseClassName("Post")
public class Post extends ParseObject implements ClusterItem, Comparable {

    private Marker marker;

    public void setUser(ParseUser user){
        put("user", user);
    }

    public ParseUser getUser(){
        ParseUser user = null;
        user = getParseUser("user");
        return user;
    }

    public void setText(String text){
        put("text", text);
    }

    public String getText(){
        return getString("text");
    }

    public void setLocation(ParseGeoPoint location){
        put("location", location);
    }

    public ParseGeoPoint getLocation(){
        return getParseGeoPoint("location");
    }

    public void setColor(String color){
        put("color", color);
    }

    public String getColor(){
        return getString("color");
    }

    public void setLocale(String locale){
        put("locale", locale);
    }

    public String getLocale(){
        return getString("locale");
    }

    public void setImage(String image){
        put("image", image);
    }

    public String getImage(){
        return getString("image");
    }

    public void setType(String type){
        put("type", type);
    }

    public String getType(){
        return getString("type");
    }

    public void setUpvotes(int i){
        put("upvotes", i);
    }

    public int getUpvotes(){
        return getInt("upvotes");
    }

    public int getHearts(){
        return getInt("hearts");
    }

    public void setComments(int i){
        put("comments", i);
    }

    public int getComments(){
        return getInt("comments");
    }

    public void addComment(){
        setComments(getComments() + 1);
    }

    public void setMarker(Marker marker){
        this.marker = marker;
    }

    public double getLongitude(){
        return getLocation().getLongitude();
    }

    public double getLatitude(){
        return getLocation().getLatitude();
    }

    public Marker getMarker(){
        return marker;
    }

    public static ParseQuery<Post> getQuery() {
        return ParseQuery.getQuery(Post.class);
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(getLatitude(), getLongitude());
    }

    @Override
    public int compareTo(Object b) {
        Post p = (Post) b;
        double aDist = distance(LocationManager.getLocation().getLatitude(), LocationManager.getLocation().getLongitude(), getLatitude(), getLongitude());
        double bDist = distance(LocationManager.getLocation().getLatitude(),  LocationManager.getLocation().getLongitude(), p.getLatitude(), p.getLongitude());
        if (aDist - bDist > 0){
            return 1;
        } else if (aDist - bDist < 0){
            return -1;
        } else {
            return 0;
        }
    }

    private double distance(double fromLat, double fromLon, double toLat, double toLon) {
        double radius = 6378137;   // approximate Earth radius, *in meters*
        double deltaLat = toLat - fromLat;
        double deltaLon = toLon - fromLon;
        double angle = 2 * Math.asin( Math.sqrt(
                Math.pow(Math.sin(deltaLat/2), 2) +
                        Math.cos(fromLat) * Math.cos(toLat) *
                                Math.pow(Math.sin(deltaLon/2), 2) ) );
        return radius * angle;
    }

    public String getFormattedDistance(ParseGeoPoint currentLocation){
        double distance = currentLocation.distanceInKilometersTo(getLocation());
        DecimalFormat f = new DecimalFormat("##.00");
        return f.format(distance) + " km";
    }

    public String getFormattedTime(long time){
        time = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - time);
        String timeElapsed;
        if (time > 60){
            if ( ((time / 60) / 24) >= 1){
                timeElapsed = String.valueOf((time / 60) / 24) + " days";
            }
            else {
                timeElapsed = String.valueOf(time / 60) + " hr";
            }
        } else {
            timeElapsed = String.valueOf(time) + " mi";
        }
        return timeElapsed;
    }

}
