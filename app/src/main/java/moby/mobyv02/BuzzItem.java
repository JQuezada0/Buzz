package moby.mobyv02;

import com.androidmapsextensions.Marker;
import com.google.maps.android.clustering.ClusterItem;
import com.parse.Parse;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

/**
 * Created by Johnil on 10/6/2015.
 */
public abstract class BuzzItem extends ParseObject implements ClusterItem, Comparable {

    private Marker marker;

    public abstract String getProfileImage();

    public abstract String getName();

    public abstract String getLocale();

    public abstract ParseGeoPoint getLocation();

    public abstract long getTime();

    public abstract Date getFormattedTime();

    public abstract String getText();

    public abstract int getHeartCount();

    public abstract int getCommentCount();

    public abstract double getLatitude();

    public abstract double getLongitude();

    public abstract ParseUser getUser();

    public abstract String getType();

    public abstract String getImage();

    public abstract String getFormattedDistance(ParseGeoPoint location);

    public abstract String getFormattedTime(long time);

    public abstract String getFormattedDate();

    public abstract String getVideo();

    public void setMarker(Marker marker){
        this.marker = marker;
    }

    public Marker getMarker(){
        return marker;
    }

}
