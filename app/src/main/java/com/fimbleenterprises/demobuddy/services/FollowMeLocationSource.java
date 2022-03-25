package com.fimbleenterprises.demobuddy.services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.LocationSource;

import androidx.core.app.ActivityCompat;

/* Our custom LocationSource.
 * We register this class to receive location updates from the Location Manager
 * and for that reason we need to also implement the LocationListener interface. */
public class FollowMeLocationSource implements LocationSource, LocationListener {

    public interface OnFollowMeLocationChanged {
        public void onLocationChanged(Location location);
    }

    // Used for blue dot and blue dot ONLY
    private OnLocationChangedListener mListener;
    // Used for anything else that wants to subscribe to location changes
    private OnFollowMeLocationChanged mFollowMeLocationListener;

    private LocationManager locationManager;
    private final Criteria criteria = new Criteria();
    private String bestAvailableProvider;
    Context myContext;
    /* Updates are restricted to one every 10 seconds, and only when
     * movement of more than 10 meters has been detected.*/
    private final int minTime = 1500;     // minimum time interval between location updates, in milliseconds
    private final int minDistance = 1;    // minimum distance between location updates, in meters

    public FollowMeLocationSource(Context context, OnFollowMeLocationChanged onLocationChangedListener) {

        this.myContext = context;

        // Get reference to Location Manager
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // Specify Location Provider criteria
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(true);
        criteria.setSpeedRequired(true);
        criteria.setCostAllowed(true);
        this.mFollowMeLocationListener = onLocationChangedListener;
        getBestAvailableProvider();
    }

    public FollowMeLocationSource(boolean gpsOnly, Context context, OnFollowMeLocationChanged onLocationChangedListener) {

        this.myContext = context;

        // Get reference to Location Manager
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // Specify Location Provider criteria
        if (gpsOnly) {
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
        } else {
            criteria.setAccuracy(Criteria.NO_REQUIREMENT);
        }
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(true);
        criteria.setSpeedRequired(true);
        criteria.setCostAllowed(true);
        this.mFollowMeLocationListener = onLocationChangedListener;
        getBestAvailableProvider();
    }

    private void getBestAvailableProvider() {
        /* The preffered way of specifying the location provider (e.g. GPS, NETWORK) to use
         * is to ask the Location Manager for the one that best satisfies our criteria.
         * By passing the 'true' boolean we ask for the best available (enabled) provider. */
        bestAvailableProvider = locationManager.getBestProvider(criteria, true);
    }

    /* Activates this provider. This provider will notify the supplied listener
     * periodically, until you call deactivate().
     * This method is automatically invoked by enabling my-location layer. */
    @Override
    public void activate(OnLocationChangedListener listener) {
        // We need to keep a reference to my-location layer's listener so we can push forward
        // location updates to it when we receive them from Location Manager.
        mListener = listener;

        // Request location updates from Location Manager
        if (bestAvailableProvider != null) {
            if (ActivityCompat.checkSelfPermission(myContext, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(myContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(bestAvailableProvider, minTime, minDistance, this);
        } else {
            // (Display a message/dialog) No Location Providers currently available.
        }
    }

    /* Deactivates this provider.
     * This method is automatically invoked by disabling my-location layer. */
    @Override
    public void deactivate() {
        // Remove location updates from Location Manager
        locationManager.removeUpdates(this);

        mListener = null;
    }

    @Override
    public void onLocationChanged(Location location) {
        /* This ensures that my-location layer will set the blue dot at the new/received location) */
        mListener.onLocationChanged(location);

        // This custom listener is needed to subscribe to onLocationChanged events by anything other than
        // the map layer that shows the blue dot.  This took fucking forever for me to figure out!
        mFollowMeLocationListener.onLocationChanged(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
