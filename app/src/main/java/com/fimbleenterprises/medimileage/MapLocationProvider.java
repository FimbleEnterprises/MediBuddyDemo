package com.fimbleenterprises.medimileage;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.LocationSource;

import androidx.core.app.ActivityCompat;

public class MapLocationProvider implements LocationSource, LocationListener {
    private OnLocationChangedListener listener;
    private LocationManager locationManager;
    public boolean isActivated = false;
    public boolean useOnlyGPS = false;

    public MapLocationProvider(Context context, boolean useOnlyGPS) {
        this.useOnlyGPS = useOnlyGPS;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        Log.e("PROVIDER", "PROVIDER IS ACTIVATED YO!");
        this.listener = listener;
        LocationProvider gpsProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
        if (gpsProvider != null) {
            if (ActivityCompat.checkSelfPermission(MyApp.getAppContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(MyApp.getAppContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            locationManager.requestLocationUpdates(gpsProvider.getName(), 0,
                    1, this);
        }

        if (this.useOnlyGPS == false) {
        	LocationProvider networkProvider = locationManager.getProvider(LocationManager.NETWORK_PROVIDER);;
            if(networkProvider != null) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 60 * 5, 0, this);
            }
        }
        
        isActivated = true;
        
    }

    @Override
    public void deactivate()
    {
    	Log.e("PROVIDER", "PROVIDER IS DEACTIVATED!");
        locationManager.removeUpdates(this);
        isActivated = false;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        if(listener != null)
        {
            listener.onLocationChanged(location);
        }
    }

    @Override
    public void onProviderDisabled(String provider)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        // TODO Auto-generated method stub

    }
}
