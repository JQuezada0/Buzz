package moby.mobyv02.map;

import com.androidmapsextensions.Marker;
import com.google.android.gms.maps.model.LatLng;

import moby.mobyv02.LocationManager;

/**
 * Created by Johnil on 10/7/2015.
 */
public class Point implements Comparable {

    private LatLng location;
    private Marker marker;

    public Point(Marker m){
        this.marker = m;
        this.location = m.getPosition();
    }

    public LatLng getLocation(){
        return location;
    }

    public Marker getMarker(){
        return marker;
    }

    @Override
    public int compareTo(Object b) {
        Point p = (Point) b;
        LatLng l = p.getLocation();
        double aDist = distance(LocationManager.getLocation().getLatitude(), LocationManager.getLocation().getLongitude(), location.latitude, location.longitude);
        double bDist = distance(LocationManager.getLocation().getLatitude(),  LocationManager.getLocation().getLongitude(), l.latitude, l.longitude);
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
