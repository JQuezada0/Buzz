package moby.mobyv02;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

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
            if (ParseUser.getCurrentUser() != null){
                LocationManager.updateLocation(this);
            }
            Intent intent = new Intent(this, Main.class);
            startActivity(intent);
        }
    }

    @Override
    public void locationReceived() {
        SharedPreferences prefs = getSharedPreferences("user", 0);
        
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
    }

    @Override
    public void locationReceived(ParseGeoPoint location) {

    }
}
