package com.fimbleenterprises.medimileage.objects_and_containers;

import android.os.Parcel;
import android.os.Parcelable;

import com.fimbleenterprises.medimileage.Helpers;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MileageUser implements Parcelable {

    private static final String TAG = "MileageUser";

    public String ownerid;
    public String businessunitid;
    public String email;
    public boolean isManager;
    public String fullname;
    public String territoryid;
    public String state;
    public String milebuddyVersion;
    public String positionid;
    public String managed_territories;
    public String address;
    public double realtime_lon;
    public double realtime_lat;
    DateTime lastRealtime;

    public MileageUser() {

    }

    public MileageUser(JSONObject json) {
        try {
            if (!json.isNull("_ownerid_value")) {
                this.ownerid = (json.getString("_ownerid_value"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            if (!json.isNull("a_79740df757a5e81180e8005056a36b9b_msus_last_lon")) {
                this.realtime_lon = (Double.parseDouble(json.getString("a_79740df757a5e81180e8005056a36b9b_msus_last_lon")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            if (!json.isNull("a_79740df757a5e81180e8005056a36b9b_msus_last_lat")) {
                this.realtime_lat = (Double.parseDouble(json.getString("a_79740df757a5e81180e8005056a36b9b_msus_last_lat")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            if (!json.isNull("a_79740df757a5e81180e8005056a36b9b_msus_last_loc_timestamp")) {
                this.lastRealtime = (new DateTime(json.getString("a_79740df757a5e81180e8005056a36b9b_msus_last_loc_timestamp")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            if (!json.isNull("a_79740df757a5e81180e8005056a36b9b_businessunitid")) {
                this.businessunitid = (json.getString("a_79740df757a5e81180e8005056a36b9b_businessunitid"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            if (!json.isNull("a_79740df757a5e81180e8005056a36b9b_msus_milebuddy_version")) {
                this.milebuddyVersion = (json.getString("a_79740df757a5e81180e8005056a36b9b_msus_milebuddy_version"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            if (!json.isNull("a_79740df757a5e81180e8005056a36b9b_internalemailaddress")) {
                this.email = (json.getString("a_79740df757a5e81180e8005056a36b9b_internalemailaddress"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            if (!json.isNull("a_79740df757a5e81180e8005056a36b9b_msus_ismanager")) {
                this.isManager = (json.getBoolean("a_79740df757a5e81180e8005056a36b9b_msus_ismanager"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            if (!json.isNull("a_79740df757a5e81180e8005056a36b9b_fullname")) {
                this.fullname = (json.getString("a_79740df757a5e81180e8005056a36b9b_fullname"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            if (!json.isNull("a_79740df757a5e81180e8005056a36b9b_territoryid")) {
                this.territoryid = (json.getString("a_79740df757a5e81180e8005056a36b9b_territoryid"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            if (!json.isNull("a_79740df757a5e81180e8005056a36b9b_address1_stateorprovince")) {
                this.state = (json.getString("a_79740df757a5e81180e8005056a36b9b_address1_stateorprovince"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            if (!json.isNull("a_79740df757a5e81180e8005056a36b9b_positionid")) {
                this.positionid = (json.getString("a_79740df757a5e81180e8005056a36b9b_positionid"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            if (!json.isNull("a_79740df757a5e81180e8005056a36b9b_msus_medibuddy_managed_territories")) {
                this.managed_territories = (json.getString("a_79740df757a5e81180e8005056a36b9b_msus_medibuddy_managed_territories"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            if (!json.isNull("a_79740df757a5e81180e8005056a36b9b_address1_composite")) {
                this.address = (json.getString("a_79740df757a5e81180e8005056a36b9b_address1_composite"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Makes the user's fullname more explicit
     * @return The user's fullname with a fucking modifier.
     */
    public String fullFuckingName() {
        try {
            String[] splitName = fullname.split(" ");
            if (splitName.length > 1) {
                String newFullname = splitName[0] + " Fucking " + splitName[1];
                return newFullname;
            } else {
                return fullname;
            }
        } catch (Exception e) {
            return fullname;
        }
    }

    public boolean isDriving() {
        try {
            if (lastRealtime == null) {
                return false;
            } else {
                long nowMillies = DateTime.now().getMillis();
                long lastRealtimeMillies = lastRealtime.getMillis();
                long gap = nowMillies - lastRealtimeMillies;
                return Helpers.DatesAndTimes.convertMilisToMinutes(gap) <= 5;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ArrayList<MileageUser> makeMany(JSONArray array) {
        ArrayList<MileageUser> users = new ArrayList<>();
        try {
            for (int i = 0; i < array.length(); i++) {
                users.add(new MileageUser(array.getJSONObject(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }


    protected MileageUser(Parcel in) {
        ownerid = in.readString();
        businessunitid = in.readString();
        email = in.readString();
        isManager = in.readByte() != 0x00;
        fullname = in.readString();
        territoryid = in.readString();
        state = in.readString();
        positionid = in.readString();
        managed_territories = in.readString();
        address = in.readString();
        milebuddyVersion = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ownerid);
        dest.writeString(businessunitid);
        dest.writeString(email);
        dest.writeByte((byte) (isManager ? 0x01 : 0x00));
        dest.writeString(fullname);
        dest.writeString(territoryid);
        dest.writeString(state);
        dest.writeString(positionid);
        dest.writeString(managed_territories);
        dest.writeString(address);
        dest.writeString(milebuddyVersion);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MileageUser> CREATOR = new Parcelable.Creator<MileageUser>() {
        @Override
        public MileageUser createFromParcel(Parcel in) {
            return new MileageUser(in);
        }

        @Override
        public MileageUser[] newArray(int size) {
            return new MileageUser[size];
        }
    };
}