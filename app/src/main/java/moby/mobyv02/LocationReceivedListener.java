package moby.mobyv02;

import com.parse.ParseGeoPoint;

/**
 * Created by quezadjo on 9/8/2015.
 */
public interface LocationReceivedListener {

    void locationReceived();

    void locationReceived(ParseGeoPoint location);

}
