package com.fimbleenterprises.demobuddy.objects_and_containers;

import android.location.Location;

import com.fimbleenterprises.demobuddy.MyPreferencesHelper;
import com.google.android.gms.maps.model.LatLng;

import org.joda.time.DateTime;

public class SavedParkingSpot {

    public double lat;
    public double lon;
    public long dateTimeIsMs;
    public double altitude;

    public SavedParkingSpot(Location location) {
        this.lat = location.getLatitude();
        this.lon = location.getLongitude();
        this.dateTimeIsMs = DateTime.now().getMillis();
        this.altitude = location.getAltitude();
    }

    public SavedParkingSpot(LatLng position) {
        this.lat = position.latitude;
        this.lon = position.longitude;
        this.dateTimeIsMs = DateTime.now().getMillis();
        this.altitude = 0;
    }

    public DateTime getDate() {
        return new DateTime(this.dateTimeIsMs);
    }

    public Location toLocation() {
        Location location = new Location("GPS");
        location.setLongitude(this.lon);
        location.setLatitude(this.lat);
        location.setTime(this.dateTimeIsMs);
        location.setAltitude(this.altitude);
        return location;
    }

    public LatLng toPosition() {
        return new LatLng(this.lat, this.lon);
    }

    public void save() {
        new MyPreferencesHelper().setParkingSpot(this);
    }

    public static SavedParkingSpot getSaved() {
        return new MyPreferencesHelper().getParkingSpot();
    }

}
