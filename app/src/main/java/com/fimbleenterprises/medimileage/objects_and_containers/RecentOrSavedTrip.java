package com.fimbleenterprises.medimileage.objects_and_containers;

import android.os.Parcel;
import android.os.Parcelable;

import com.fimbleenterprises.medimileage.MySqlDatasource;

import java.util.ArrayList;

/**
 * Used to allow the user to create favorite manual trips or recall recently created manual trips.
 */
public class RecentOrSavedTrip implements Parcelable {
    private static final String TAG = "RecentOrSavedTrip";
    public double id;
    public String name;
    public float distanceInMiles;
    public double fromLat;
    public double fromLon;
    public double toLat;
    public double toLon;

    /**
     * Converts an arraylist of RecentOrSavedTrip objects to a BasicObjects object.
     * @param recentOrSavedTrips The arraylist to convert.
     * @return A BasicObjects object.
     */
    public static BasicObjects toBasicObjects(ArrayList<RecentOrSavedTrip> recentOrSavedTrips) {
        BasicObjects objects = new BasicObjects();
        for (RecentOrSavedTrip trip : recentOrSavedTrips) {
            objects.list.add(new BasicObjects.BasicObject(trip.name, trip.distanceInMiles + " miles", trip));
        }
        return objects;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.id);
        dest.writeString(this.name);
        dest.writeFloat(this.distanceInMiles);
        dest.writeDouble(this.fromLat);
        dest.writeDouble(this.fromLon);
        dest.writeDouble(this.toLat);
        dest.writeDouble(this.toLon);
    }

    public RecentOrSavedTrip() {
    }

    protected RecentOrSavedTrip(Parcel in) {
        this.id = in.readDouble();
        this.name = in.readString();
        this.distanceInMiles = in.readFloat();
        this.fromLat = in.readDouble();
        this.fromLon = in.readDouble();
        this.toLat = in.readDouble();
        this.toLon = in.readDouble();
    }

    public static final Parcelable.Creator<RecentOrSavedTrip> CREATOR = new Parcelable.Creator<RecentOrSavedTrip>() {
        @Override
        public RecentOrSavedTrip createFromParcel(Parcel source) {
            return new RecentOrSavedTrip(source);
        }

        @Override
        public RecentOrSavedTrip[] newArray(int size) {
            return new RecentOrSavedTrip[size];
        }
    };

    public boolean delete() {
        try {
            return new MySqlDatasource().deleteRecentOrSavedtrip(this.id);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean save() {
        return new MySqlDatasource().updateRecentOrSavedTrip(this);
    }
}
