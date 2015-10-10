package moby.mobyv02.parse;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.concurrent.TimeUnit;

import moby.mobyv02.BuzzItem;
import moby.mobyv02.LocationManager;

/**
 * Created by Johnil on 10/6/2015.
 */
@ParseClassName("Event")
public class Event extends BuzzItem {

    public void setName(String name){
        put("name", name);
    }

    @Override
    public String getProfileImage() {
        return null;
    }

    public String getName(){
        return getString("name");
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

    public void setLocale(String locale){
        put("locale", locale);
    }

    public String getLocale(){
        return getString("locale");
    }

    public void setLocation(ParseGeoPoint location){
        put("location", location);
    }

    public ParseGeoPoint getLocation(){
        return getParseGeoPoint("location");
    }

    @Override
    public long getTime() {
        return 0;
    }

    @Override
    public String getFormattedTime() {
        return null;
    }

    @Override
    public double getLongitude(){
        return getLocation().getLongitude();
    }

    @Override
    public double getLatitude(){
        return getLocation().getLatitude();
    }

    public void setImage(String image){
        put("image", image);
    }

    public void setUrl(String url){
        put("Url", url);
    }

    public String getUrl(){
        return getString("Url");
    }

    public String getFormattedTime(long time){
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

    public String getImage(){
        return getString("image");
    }

    public static ParseQuery<Event> getQuery() {
        return ParseQuery.getQuery(Event.class);
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(getLatitude(), getLongitude());
    }

    @Override
    public int compareTo(Object b) {
        Event e = (Event) b;
        double aDist = distance(LocationManager.getLocation().getLatitude(), LocationManager.getLocation().getLongitude(), getLatitude(), getLongitude());
        double bDist = distance(LocationManager.getLocation().getLatitude(),  LocationManager.getLocation().getLongitude(), e.getLatitude(), e.getLongitude());
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
}
