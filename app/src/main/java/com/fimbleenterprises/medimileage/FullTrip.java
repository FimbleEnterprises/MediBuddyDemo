package com.fimbleenterprises.medimileage;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.fimbleenterprises.medimileage.Requests.Request;
import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.fimbleenterprises.medimileage.Containers.*;

public class FullTrip implements Parcelable {
    private static final String TAG = "FullTrip";
    private String title;
    private String objective;
    private long dateTime;
    private long tripcode;
    private float distance = 0;
    private String email;
    private long milis;
    public int edited = 0;
    private String gu_username;
    private String ownerid;
    public int isManualTrip = 0;
    public int isSubmitted = 0;
    public boolean isChecked = false;
    private ArrayList<TripEntry> tripEntries = new ArrayList<>();
    public boolean isSeparator = false;
    private float reimbursementRate;
    public String tripEntriesJson;
    public String tripGuid;
    public int userStoppedTrip = 0;

    public static class TripEntries extends ArrayList<TripEntry> {
        public TripEntries(FullTrip trip) {
            MySqlDatasource ds = new MySqlDatasource();
            ArrayList<TripEntry> entries = ds.getAllTripEntries(trip.tripcode);
            for (TripEntry entry : entries) {
                this.add(entry);
            }
        }
    }

    public interface GetTripEntriesCallback {
        public void onRetrieved(ArrayList<TripEntry> entries);
}

    public FullTrip() {

    }

    public FullTrip(long tripcode, String gu_username, String ownerid, String email) {
        this.tripcode = tripcode;
        this.gu_username = gu_username;
        this.ownerid = ownerid;
        this.email = email;
    }

    public static ArrayList<FullTrip> createTripsFromCrmJson(String crmJson, boolean skipEntryParsing) {

        ArrayList<FullTrip> trips = new ArrayList<>();

        try {
            JSONObject root = new JSONObject(crmJson);
            JSONArray values = root.getJSONArray("value");

            for (int i = 0; i < values.length(); i++) {
                JSONObject json = values.getJSONObject(i);
                FullTrip trip = new FullTrip();

                try {
                    if (!json.isNull("msus_dt_tripdate")) {
                        trip.dateTime = new DateTime(json.getString("msus_dt_tripdate")).getMillis();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_edited")) {
                        trip.setIsEdited(json.getBoolean("msus_edited"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_reimbursement_rate")) {
                        trip.reimbursementRate = (float) (json.getDouble("msus_reimbursement_rate"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_trip_entries_json")) {
                        trip.tripEntriesJson = (json.getString("msus_trip_entries_json"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_is_manual")) {
                        trip.setIsManualTrip(json.getBoolean("msus_is_manual"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_tripcode")) {
                        trip.tripcode = (long) (Double.parseDouble(json.getString("msus_tripcode")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_totaldistance")) {
                        double miles = json.getDouble("msus_totaldistance");
                        float fmiles = Float.parseFloat(Double.toString(miles));
                        trip.distance = Helpers.Geo.convertMilesToMeters(fmiles, 2);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_fulltripid")) {
                        trip.tripGuid = (json.getString("msus_fulltripid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_ownerid_value")) {
                        trip.ownerid = (json.getString("_ownerid_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_name")) {
                        trip.title = (json.getString("msus_name"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_is_submitted")) {
                        trip.setIsSubmitted(json.getBoolean("msus_is_submitted"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (! skipEntryParsing) {
                    trip.tripEntries = TripEntry.parseCrmTripEntries(trip.tripEntriesJson);
                }
                trips.add(trip);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return trips;
    }

    public static ArrayList<FullTrip> createTripsFromCrmJson(String crmJson) {
        return createTripsFromCrmJson(crmJson, false);
    }

    private String toGson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public String getSafeTripEntriesJson() {

        if (tripEntriesJson.length() > 1048576) {
            while (tripEntriesJson.length() > 1048576) {
                Log.d(TAG, "getSafeTripEntriesJson JSON IS TOO LONG! (" + tripEntriesJson.length() + ")");
                reduceJson(tripEntriesJson);
                Log.d(TAG, "getSafeTripEntriesJson JSON reduced to:" + tripEntriesJson.length());
            }
            Log.d(TAG, "getSafeTripEntriesJson Returning JSON @ length: " + tripEntriesJson.length());
            return tripEntriesJson;
        } else {
            return tripEntriesJson;
        }
    }

    private void reduceJson(String data) {
        int oSize = tripEntriesJson.length();
        Gson gson = new Gson();
        ArrayList<TripEntry> entries = gson.fromJson(tripEntriesJson, ArrayList.class);
        ArrayList<TripEntry> newList = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            if (Helpers.Numbers.isEven(i)) {
                newList.add(entries.get(i));
            }
        }
        String newJson = gson.toJson(newList);
        int nSize = newJson.length();
        Log.i(TAG, "getSafeTripEntriesJson oSize=" + oSize + " nSize=" + nSize);
        tripEntriesJson = newJson;
    }

    public Request packageForCrm() {
        try {
            Gson gson = new Gson();
            this.tripEntriesJson = gson.toJson(new TripEntries(this));

            EntityContainer container = new EntityContainer();
            container.entityFields.add(new EntityField("msus_name", this.getTitle()));
            container.entityFields.add(new EntityField("msus_tripcode", Long.toString(this.getTripcode())));
            container.entityFields.add(new EntityField("msus_dt_tripdate", Helpers.DatesAndTimes.getPrettyDateAndTime(this.getDateTime())));
            container.entityFields.add(new EntityField("msus_reimbursement_rate", Float.toString(this.getReimbursementRate())));
            container.entityFields.add(new EntityField("msus_reimbursement", Float.toString(this.calculateReimbursement())));
            container.entityFields.add(new EntityField("msus_totaldistance", Float.toString(getDistanceInMiles())));
            container.entityFields.add(new EntityField("msus_trip_duration", Float.toString(this.getDurationInMinutes())));
            container.entityFields.add(new EntityField("msus_is_manual", Boolean.toString(this.getIsManualTrip())));
            container.entityFields.add(new EntityField("msus_edited", Boolean.toString(this.getIsEdited())));
            container.entityFields.add(new EntityField("msus_trip_entries_json", getSafeTripEntriesJson()));
            container.entityFields.add(new EntityField("msus_is_submitted", Boolean.toString(true)));
            container.entityFields.add(new EntityField("msus_user_stopped_trip", Boolean.toString(getUserStoppedTrip())));

            Request request = new Request();
            request.function = Request.Function.CREATE.name();
            request.arguments.add(new Requests.Argument("entity", "msus_fulltrip"));
            request.arguments.add(new Requests.Argument("as_userid", MediUser.getMe().systemuserid));
            request.arguments.add(new Requests.Argument("container", container.toJson()));

            return request;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks if a trip is currently running and if so, has the same tripcode as this trip.
     * @return True if the trip is the currently active trip.
     */
    public boolean getIsRunning() {

        if (MyLocationService.isRunning) {
            return (MyLocationService.fullTrip.getTripcode() == this.getTripcode());
        } else {
            return false;
        }
    }

    public String calculatePrettyReimbursement() {
        return Helpers.Numbers.convertToCurrency((float) (getReimbursementRate() * getDistanceInMiles()));
    }

    public float calculateReimbursement() {
        String strReimbursement = Helpers.Numbers.convertToCurrency(getReimbursementRate()
                * getDistanceInMiles()).replace("$","").replace(",","");
        return Float.parseFloat(strReimbursement);
    }

    public float getReimbursementRate() {
        if (reimbursementRate == 0) {
            MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());
            reimbursementRate = options.getReimbursementRate();
        }
        return reimbursementRate;
    }

    public void setReimbursementRate(float rate) {
        this.reimbursementRate = rate;
    }

    public void setDefaultTitle() {
        DateTime now = DateTime.now();
        this.setTitle(Helpers.DatesAndTimes.getPrettyDateAndTime(now));
    }

    public void setIsManualTrip(boolean isManualTrip) {
        this.isManualTrip = (isManualTrip) ? 1:0;
    }

    public boolean getIsManualTrip() {
        return this.isManualTrip == 1;
    }

    public boolean getUserStoppedTrip() {
        return this.userStoppedTrip == 1;
    }

    public void setUserStoppedTrip(boolean val) {
        this.userStoppedTrip = (val ? 1 : 0);
    }

    public String getTitle() {
        return title;
    }

    public void setTripGuid(String guid) {
        this.tripGuid = guid;
    }

    public String getTripGuid() {
        return this.tripGuid;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public DateTime getDateTime() {
        return new DateTime(dateTime);
    }

    public String getPrettyDateTime() {
        return Helpers.DatesAndTimes.getPrettyDateAndTime(this.getDateTime());
    }

    public String getPrettyDate() {
        return Helpers.DatesAndTimes.getPrettyDate(this.getDateTime());
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

    public void setIsSubmitted(boolean value) {
        this.isSubmitted = (value) ? 1:0;
    }

    public boolean getIsSubmitted() {
        return isSubmitted == 1;
    }

    public int getEntryCount() {
        if (this.tripEntries == null) {
            this.tripEntries = new ArrayList<>();
        }
        return this.tripEntries.size();
    }

    public boolean save() {

        MySqlDatasource ds = new MySqlDatasource();

        if (ds.fullTripExists(this.getTripcode())) {
            Log.i(TAG, "save : Trip was updated.");
            return ds.updateFulltrip(this);
        } else {
            Log.i(TAG, "save : Trip was created.");
            return ds.createFullTrip(this);
        }
    }

    public float getDistance() {
        return distance;
    }

    public float getAvgSpeedInMeters() {
        if (this.tripEntries.size() == 0) {
            this.getTripEntries();
        }

        float total = 0;
        float size = this.tripEntries.size();

        for (TripEntry entry : this.tripEntries) {
            total += entry.getSpeed();
        }

        float avg = total / size;
        return avg;
    }

    public float getAvgSpeedInMph() {
        float meters = getAvgSpeedInMeters();
        return Helpers.Geo.getSpeedInMph(meters,2);
    }

    public float getTopSpeedInMeters() {
        if (this.tripEntries.size() == 0) {
            this.getTripEntries();
        }

        float topSpeed = 0;

        for (TripEntry entry : this.tripEntries) {
            if (entry.getSpeed() > topSpeed) {
                topSpeed = entry.getSpeed();
            }
        }

        return topSpeed;
    }

    public float getTopSpeedInMph() {
        float meters = getTopSpeedInMeters();
        return Helpers.Geo.getSpeedInMph(meters,2);
    }

    @SuppressLint("StaticFieldLeak")
    public ArrayList<TripEntry> getTripEntries() {
        MySqlDatasource datasource = new MySqlDatasource();
        ArrayList<TripEntry> entries = datasource.getAllTripEntries(getTripcode());
        this.tripEntries = entries;

        // Since the json isn't stored in the db we need to populate it as often as possible.
        if (entries.size() > 0) {
            Gson gson = new Gson();
            this.tripEntriesJson = gson.toJson(entries);
        }
        return this.tripEntries;
    }

    public float getDistanceInMiles() {
        return Helpers.Geo.convertMetersToMiles(getDistance(),2);
    }

    /**
     * Subtracts the last entry's milliseconds value from the first and divides by 60000 to arrive at minutes between the two.
     * @return The time in minutes between the first and last trip entries.
     */
    public long getDurationInMinutes() {
        if (this.tripEntries.size() == 0) {
            this.tripEntries = getTripEntries();
        }
        long start = tripEntries.get(0).getMilis();
        long end = tripEntries.get(tripEntries.size() - 1).getMilis();
        long minutes = (end - start) / 60000;
        return minutes;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getMilis() {
        return milis;
    }

    public void setMilis(long milis) {
        this.milis = milis;
    }

    public boolean getIsEdited() {
        return edited == 1;
    }

    public void setIsEdited(boolean edited) {
        this.edited = (edited == true) ? 1 : 0;
    }

    public String getGu_username() {
        return gu_username;
    }

    public void setGu_username(String gu_username) {
        this.gu_username = gu_username;
    }

    public String getOwnerid() {
        return ownerid;
    }

    public void setOwnerid(String ownerid) {
        this.ownerid = ownerid;
    }

    @Override
    public String toString() {
        return "Tripcode: " + this.tripcode + " Distance: " + this.getDistanceInMiles() + " miles";
    }

    protected FullTrip(Parcel in) {
        title = in.readString();
        objective = in.readString();
        dateTime = in.readLong();
        tripcode = in.readLong();
        distance = in.readFloat();
        email = in.readString();
        milis = in.readLong();
        edited = in.readInt();
        gu_username = in.readString();
        ownerid = in.readString();
        isManualTrip = in.readInt();
        userStoppedTrip = in.readInt();
        if (in.readByte() == 0x01) {
            tripEntries = new ArrayList<TripEntry>();
            in.readList(tripEntries, TripEntry.class.getClassLoader());
        } else {
            tripEntries = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(objective);
        dest.writeLong(dateTime);
        dest.writeLong(tripcode);
        dest.writeFloat(distance);
        dest.writeString(email);
        dest.writeLong(milis);
        dest.writeInt(edited);
        dest.writeString(gu_username);
        dest.writeString(ownerid);
        dest.writeInt(isManualTrip);
        dest.writeInt(userStoppedTrip);
        if (tripEntries == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(tripEntries);
        }
    }

    @SuppressWarnings("unused")
    public static final Creator<FullTrip> CREATOR = new Creator<FullTrip>() {
        @Override
        public FullTrip createFromParcel(Parcel in) {
            return new FullTrip(in);
        }

        @Override
        public FullTrip[] newArray(int size) {
            return new FullTrip[size];
        }
    };
}