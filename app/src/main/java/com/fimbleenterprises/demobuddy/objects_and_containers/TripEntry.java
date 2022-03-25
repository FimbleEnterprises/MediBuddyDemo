package com.fimbleenterprises.demobuddy.objects_and_containers;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.fimbleenterprises.demobuddy.Helpers;
import com.google.android.gms.maps.model.LatLng;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Represents a point in time and space during a mileage trip.
 * <br/><br/>
 * It is worth mentioning that this class is <b>not a representation of a CRM entity</b>. This object
 * will be one of many in a TripEntries arraylist that will be serialized to JSON and added to the
 * msus_trip_entries_json field of the msus_fulltrip entity.
 */
public class TripEntry implements Parcelable {
    private static final String TAG = "TripEntry";

    private long ID;
    private long dateTime;
    private long tripcode;
    private float distance;
    private long milis; // Do not fix this typo! Fixing would breaking existing trips on the server.
    private String guid;
    private double lattitude; // Do not fix this typo! Fixing would breaking existing trips on the server.
    private double longitude;
    private float speed;
    // Needed for the CRM javascript map drawing
    private String pause_flag = "";

    public TripEntry() { }

    public static ArrayList<TripEntry> parseCrmTripEntries(String crmJson) {
        ArrayList<TripEntry> entries = new ArrayList<>();
        String rawJson = crmJson.replace("\\", "");
        
        /*
        "ID": 0,
		"dateTime": 1587659248002,
		"distance": 0.0,
		"lattitude": 44.8801593,
		"longitude": -93.2008443,
		"milis": 1587659248002,
		"pause_flag": "",
		"speed": 0.0,
		"tripcode": 1587659248002
         */
        try {
            JSONArray array = new JSONArray(rawJson);
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.getJSONObject(i);
                TripEntry tripEntry = new TripEntry();
                try {
                    if (!json.isNull("datetime")) {
                        tripEntry.dateTime = (json.getLong("datetime"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("distance")) {
                        tripEntry.distance = (json.getLong("distance"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("lattitude")) {
                        tripEntry.lattitude = (json.getDouble("lattitude"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("longitude")) {
                        tripEntry.longitude = (json.getDouble("longitude"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("milis")) {
                        tripEntry.milis = (json.getLong("milis"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("pause_flag")) {
                        tripEntry.pause_flag = (json.getString("pause_flag"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("speed")) {
                        tripEntry.speed = (json.getLong("speed"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("tripcode")) {
                        tripEntry.tripcode = (json.getLong("tripcode"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                entries.add(tripEntry);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        Log.i(TAG, "parseCrmTripEntries : " + entries.size() + " entries were added.");

        return entries;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public DateTime getDateTime() {
        return new DateTime(dateTime);
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime.getMillis();
    }

    public void setDateTime(String dateTime) {
        this.dateTime = DateTime.parse(dateTime).getMillis();
    }

    public long getTripcode() {
        return tripcode;
    }

    public void setTripcode(long tripcode) {
        this.tripcode = tripcode;
    }

    public double getLat() {
        return lattitude;
    }

    public double getLng() {
        return longitude;
    }

    public LatLng getLatLng() {
        return new LatLng(lattitude, longitude);
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public double getLattitude() {
        return lattitude;
    }

    public void setLatitude(double lattitude) {
        this.lattitude = lattitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getMph() {
        return Helpers.Geo.getSpeedInMph((float)getSpeed(),2);
    }

    public String getSpeedInMph(boolean appendMph) {
        return Helpers.Geo.getSpeedInMph((float) speed, appendMph, 0);
    }

    public long getMilis() {
        return milis;
    }

    public void setMilis(long milis) {
        this.milis = milis;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;

    }

    public double distanceTo(LatLng position) {
        Location sourcePos = new Location("SOURCE");
        sourcePos.setLatitude(this.lattitude);
        sourcePos.setLongitude(this.longitude);

        Location targetPos = new Location("DEST");
        targetPos.setLatitude(position.latitude);
        targetPos.setLongitude(position.longitude);

        double meters = sourcePos.distanceTo(targetPos);

        return meters;
    }

    public Location makeLocation() {
        Location location = new Location("GPS");
        location.setSpeed(this.speed);
        location.setLatitude(this.getLattitude());
        location.setLongitude(this.getLongitude());
        location.setTime(this.milis);
        return location;
    }

/*    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public String getLocationJson() {
        Gson gson = new Gson();

        return gson.toJson(this.location);
    }

    public void setLocation(String gson) {
        Gson gson1 = new Gson();
        this.location = gson1.fromJson(gson, Location.class);
    }*/

    @Override
    public String toString() {
        return
                "Tripcode:" + this.tripcode + ", " +
                        "Speed:" + this.speed + ", " +
                        "Date:" + this.dateTime + ", " +
                        "Lat:" + this.lattitude + ", " +
                        "Lng:" + this.longitude + ", " +
                        "Distance:" + this.distance;
    }

    protected TripEntry(Parcel in) {
        ID = in.readLong();
        dateTime = in.readLong();
        tripcode = in.readLong();
        distance = in.readFloat();
        milis = in.readLong();
        guid = in.readString();
        speed = in.readFloat();
        lattitude = in.readDouble();
        longitude = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(ID);
        dest.writeLong(dateTime);
        dest.writeLong(tripcode);
        dest.writeFloat(distance);
        dest.writeLong(milis);
        dest.writeString(guid);
        dest.writeFloat(speed);
        dest.writeDouble(lattitude);
        dest.writeDouble(longitude);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<TripEntry> CREATOR = new Parcelable.Creator<TripEntry>() {
        @Override
        public TripEntry createFromParcel(Parcel in) {
            return new TripEntry(in);
        }

        @Override
        public TripEntry[] newArray(int size) {
            return new TripEntry[size];
        }
    };
}