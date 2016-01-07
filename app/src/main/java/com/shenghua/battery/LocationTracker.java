package com.shenghua.battery;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by shenghua on 12/30/15.
 */
public class LocationTracker implements LocationListener {

//    private final Context context;

    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;

    Context context;

    Location location = null;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATE = 1;
    private static final long MIN_TIME_BETWEEN_UPDATES = 1000 * 10 * 1;

    protected LocationManager locationManager;

    public LocationTracker(Context context) {
        this.context = context;
        update();
    }

//    @Override
//    public void onCreate() {
//        super.onCreate();
//        update();
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        stopLocationUpdateListening();
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        return START_STICKY;
//    }

    public void update() {

        if (context.getPackageManager().checkPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                context.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
            return;
        };

        try {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager == null)
                return;

            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            this.canGetLocation = isNetworkEnabled || isGPSEnabled;

            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BETWEEN_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATE, this);
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (location == null && isGPSEnabled) {
                //locationManager.requestLocationUpdates(
                //        LocationManager.GPS_PROVIDER,
                //        MIN_TIME_BETWEEN_UPDATES,
                //        MIN_DISTANCE_CHANGE_FOR_UPDATE, this);
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopLocationUpdateListening() {
        if (context.getPackageManager().checkPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                context.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
            return;
        };
        if (locationManager != null)
            locationManager.removeUpdates(LocationTracker.this);
    }

    public Location getLocation() {
        //update();
        return location;
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        //Log.e("location-->", "onLocationChanged: "+location.toString());
        stopLocationUpdateListening();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
//        switch (status) {
//            case LocationProvider.AVAILABLE:
//                Log.d("onStatusChanged", "AVAILABLE");
//                break;
//            case LocationProvider.OUT_OF_SERVICE:
//                Log.d("onStatusChanged", "OUT_OF_SERVICE");
//                // ...
//                break;
//            case LocationProvider.TEMPORARILY_UNAVAILABLE:
//                Log.d("onStatusChanged", "TEMPORARILY_UNAVAILABLE");
//                // ...
//                break;
//        }
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
}
