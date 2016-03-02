package moby.mobyv02;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;

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
        loadPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION, 0);
    }

    private void loadPermissions(String perm,int requestCode) {
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
                ActivityCompat.requestPermissions(this, new String[]{perm, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, requestCode);
            } else {
                finished();
            }
        } else {
            finished();
        }
    }


    @Override
    protected void onResume(){
        super.onResume();
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

    private void finished(){
        ParseGeoPoint location = LocationManager.getLocation();
        SharedPreferences prefs = getSharedPreferences("location", 0);
        if (!prefs.contains("latitude")){
            System.out.println("Load location");
            LocationManager.loadLocation(this, this);
        } else {
            System.out.println("Location already saved");
            if (ParseUser.getCurrentUser() != null){
                LocationManager.updateLocation(this);
            }
            Intent intent = new Intent(this, Main.class);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        System.out.println("onRequestPermissionResult");
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    finished();
                }
                else{
                    System.out.println("Not granted");
                    loadPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION, 0);
                }
                return;
            }

        }

    }
}
