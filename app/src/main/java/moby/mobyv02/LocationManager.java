package moby.mobyv02;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.plus.Plus;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

/**
 * Created by quezadjo on 9/8/2015.
 */
public class LocationManager implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    private GoogleApiClient googleApiClient;
    private LocationReceivedListener locationReceivedListener;
    private Context context;
    private boolean update = false;

    /**
     * 1. Check if ParseUser is null
     * 2. If Parse User is not null, check the location fields
     * 3. Acquire the location from those fields, and proceed to the next activity. In the background, acquire the new location and update the ParseUser
     * 4. If the ParseUser is null, acquire the location and store it in the SharedPreferences.
     * 5. Upon user creation, update the ParseUser fields with the location
     * 6. Proceed to next activity.
     * 7. Expose only a single public static method to acquire the location. This should NEVER return null. EVER.
     *
     *
     */

    private LocationManager(Context context, LocationReceivedListener locationReceivedListener){
        this.context = context;
        this.locationReceivedListener = locationReceivedListener;
    }

    public static ParseGeoPoint getLocation(){

        ParseGeoPoint location = getFromParseUser();
        return location;

    }

    public static void loadLocation(final Context context, LocationReceivedListener locationReceivedListener){

        System.out.println("loadLocation");
        LocationManager locationManager = new LocationManager(context, locationReceivedListener);
        if (locationManager.checkLocation()){
            locationManager.connect();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Please enable location");
            builder.setPositiveButton("Enable Location", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(0);
                }
            });
            builder.show();
        }
    }

    public static void updateFromSharedPreferences(Context context){

        ParseGeoPoint location = new ParseGeoPoint();
        SharedPreferences preferences = context.getSharedPreferences("location", 0);
        location.setLongitude(Double.valueOf(preferences.getString("longitude", "0")));
        location.setLatitude(Double.valueOf(preferences.getString("latitude", "0")));
        ParseUser user = ParseUser.getCurrentUser();
        user.put("location", location);
        user.saveEventually();
    }

    private static ParseGeoPoint getFromParseUser(){

        ParseUser user = ParseUser.getCurrentUser();
        if (user != null){
            return user.getParseGeoPoint("location");
        } else {
            return null;
        }

    }


    private boolean checkLocation(){
        android.location.LocationManager locationManager = (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)){
            return true;
        } else {

            return false;
        }

    }


    private void setUpdate(boolean b){
        update = b;
    }

    private void connect(){
        System.out.println("connect");
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

    }

    public static void updateLocation(Context c){

        LocationReceivedListener locationReceivedListener = new LocationReceivedListener() {
            @Override
            public void locationReceived() {

            }

            @Override
            public void locationReceived(ParseGeoPoint location) {
                ParseUser user = ParseUser.getCurrentUser();
                user.put("location", location);
                System.out.println("Updated location is " + location.getLatitude() + "; " + location.getLongitude());
                user.saveEventually();
            }
        };
            LocationManager locationManager = new LocationManager(c, locationReceivedListener);
            locationManager.setUpdate(true);
            locationManager.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        while(!googleApiClient.isConnected()){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        locationRequest.setInterval(100);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest,  this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("Connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        System.out.print(connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        while(!googleApiClient.isConnected()){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        SharedPreferences.Editor prefs = context.getSharedPreferences("location", 0).edit();
        prefs.putString("longitude", String.valueOf(location.getLongitude()));
        prefs.putString("latitude", String.valueOf(location.getLatitude()));
        prefs.commit();
        System.out.println("Prefs commited");
        ParseGeoPoint locationGeoPoint = new ParseGeoPoint();
        locationGeoPoint.setLatitude(location.getLatitude());
        locationGeoPoint.setLongitude(location.getLongitude());
        if (!update){
            locationReceivedListener.locationReceived();
        } else {
            locationReceivedListener.locationReceived(locationGeoPoint);
        }
    }
}
