package com.fimbleenterprises.demobuddy.objects_and_containers;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

public class LocationContainer implements Parcelable {

    public float speed;
    public float bearing;
    public double longitude;
    public double lattitude;
    public double altitude;
    public float accuracy;
    public float distance;
    public FullTrip fullTrip;
    public TripEntry tripEntry;

    public LocationContainer(Location location, FullTrip fullTrip, TripEntry tripEntry) {
        this.speed = location.getSpeed();
        this.bearing = location.getBearing();
        this.longitude = location.getLongitude();
        this.lattitude = location.getLatitude();
        this.altitude = location.getAltitude();
        this.accuracy = location.getAccuracy();
        this.tripEntry = tripEntry;
        this.fullTrip = fullTrip;
    }


    protected LocationContainer(Parcel in) {
        speed = in.readFloat();
        bearing = in.readFloat();
        longitude = in.readDouble();
        lattitude = in.readDouble();
        altitude = in.readDouble();
        accuracy = in.readFloat();
        distance = in.readFloat();
        fullTrip = (FullTrip) in.readValue(FullTrip.class.getClassLoader());
        tripEntry = (TripEntry) in.readValue(TripEntry.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(speed);
        dest.writeFloat(bearing);
        dest.writeDouble(longitude);
        dest.writeDouble(lattitude);
        dest.writeDouble(altitude);
        dest.writeFloat(accuracy);
        dest.writeFloat(distance);
        dest.writeValue(fullTrip);
        dest.writeValue(tripEntry);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<LocationContainer> CREATOR = new Parcelable.Creator<LocationContainer>() {
        @Override
        public LocationContainer createFromParcel(Parcel in) {
            return new LocationContainer(in);
        }

        @Override
        public LocationContainer[] newArray(int size) {
            return new LocationContainer[size];
        }
    };
}
