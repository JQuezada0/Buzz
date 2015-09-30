package moby.mobyv02;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.leanplum.Leanplum;
import com.leanplum.activities.LeanplumFragmentActivity;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

/**
 * Created by quezadjo on 9/8/2015.
 */
public class Dispatch_Activity extends FragmentActivity implements LocationReceivedListener{

    Application app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dispatch_activity);
        BuzzAnalytics.logAppOpened(this);

    }


    @Override
    protected void onResume(){
        super.onResume();
        ParseGeoPoint location = LocationManager.getLocation();
        if (location == null){
            LocationManager.loadLocation(this, this);
        } else {
            LocationManager.updateLocation(this);
            Intent intent = new Intent(this, Main.class);
            System.out.println(intent.toUri(0));
            startActivity(intent);
        }
    }

    @Override
    public void locationReceived() {
        Intent intent = new Intent(this, Welcome.class);
        startActivity(intent);
    }

    @Override
    public void locationReceived(ParseGeoPoint location) {
        //Only used when updating location.
    }
}
