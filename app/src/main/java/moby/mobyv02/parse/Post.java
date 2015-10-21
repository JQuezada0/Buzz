package moby.mobyv02.parse;

import com.google.android.gms.maps.model.LatLng;
import com.androidmapsextensions.Marker;
import com.google.maps.android.clustering.ClusterItem;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import moby.mobyv02.BuzzItem;
import moby.mobyv02.LocationManager;

/**
 * Created by quezadjo on 9/8/2015.
 */

@ParseClassName("Post")
public class Post extends BuzzItem {

    private Marker marker;

    public void setUser(ParseUser user){
        put("user", user);
    }

    public ParseUser getUser(){
        return getParseUser("user");
    }

    public void setText(String text){
        put("text", text);
    }

    public String getText(){
        return getString("text");
    }

    @Override
    public int getHeartCount() {
        return 0;
    }

    @Override
    public int getCommentCount() {
        return 0;
    }

    public void setLocation(ParseGeoPoint location){
        put("location", location);
    }

    public ParseGeoPoint getLocation(){
        return getParseGeoPoint("location");
    }

    @Override
    public long getTime() {
        return getCreatedAt().getTime();
    }

    @Override
    public Date getFormattedTime() {
        return new Date(getTime());
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

    public void setProfileImage(String profileImage){
        put("profileImage", profileImage);
    }

    @Override
    public String getProfileImage() {
        return getUser().getString("profileImage");
    }

    @Override
    public String getName() {
        return getUser().getString("fullName");
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

    public void setVideo(String video){ put("video", video); }

    public String getVideo() {return getString("video"); }

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

    @Override
    public double getLongitude(){
        return getLocation().getLongitude();
    }

    @Override
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
        time = getTime();
        time = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - time);
        String timeElapsed;
        if (time > 60){
            if ( ((time / 60) / 24) >= 1){
                timeElapsed = String.valueOf((time / 60) / 24) + "d";
            }
            else {
                timeElapsed = String.valueOf(time / 60) + "h";
            }
        } else {
            timeElapsed = String.valueOf(time) + "m";
        }
        return timeElapsed;
    }

    @Override
    public String getFormattedDate() {
        Date d = getFormattedTime();
        String s = "";
        int year = d.getYear();
        int month = d.getMonth();
        int day = d.getDay();
        Calendar c = Calendar.getInstance();
        c.set(year, month, day, d.getHours(), d.getMinutes());
        s += getDayOfWeek(c.get(Calendar.DAY_OF_WEEK)) + ", ";
        s += getMonth(c.get(Calendar.MONTH)) + " ";
        s += c.get(Calendar.DAY_OF_MONTH) + ", ";
        s += d.getHours() + ":";
        s += d.getMinutes();
        return s;
    }

    private String getDayOfWeek(int i){

        switch (i){
            case 1:
                return "Sun";
            case 2:
                return "Mon";
            case 3:
                return "Tues";
            case 4:
                return "Wed";
            case 5:
                return "Thurs";
            case 6:
                return "Fri";
            case 7:
                return "Sat";
            default:
                return "";
        }

    }

    private String getMonth(int i){
       switch (i){
           case 0:
               return "Jan";
           case 1:
               return "Feb";
           case 2:
               return "Mar";
           case 3:
               return "Apr";
           case 4:
               return "May";
           case 5:
               return "June";
           case 6:
               return "July";
           case 7:
               return "August";
           case 8:
               return "September";
           case 9:
               return "October";
           case 10:
               return "November";
           case 11:
               return "December";
           default:
               return "";
       }
    }

    public void fetchUser() throws ParseException {

        setUser(getUser().fetch());

    }

    @Override
    public LatLng getPosition() {
        return new LatLng(getLatitude(), getLongitude());
    }
}
