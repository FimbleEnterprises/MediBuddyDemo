package com.fimbleenterprises.medimileage.objects_and_containers;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.fimbleenterprises.medimileage.Crm;
import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.MyApp;
import com.fimbleenterprises.medimileage.MyInterfaces;
import com.fimbleenterprises.medimileage.MySqlDatasource;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MediUser implements Parcelable {

    // USed to cache the user object representing the actual user so we don't have to build
    // a user from a saved JSON string from preferences.
    //private static Obj_MediUser currentUser;

    private static final String TAG = "MediUser";

    public String etag;
    public String territoryname;
    public String territoryid;
    public String jobtitle;
    public String jobtitleid;
    public String systemuserid;
    public String managername;
    public String milebuddyVersion;
    public String managerid;
    public String email;
    public String fullname;
    public String address;
    public double latitude;
    public double longitude;
    public String title;
    public String domainname;
    public String mobilephone;
    public boolean pushonallorders;
    public boolean pushonorder;
    public String msus_medibuddy_managed_territories;
    public ArrayList<String> managedTerritories = new ArrayList<>();
    public String businessunitid;
    public String businessunitname;
    public String managerbusinessunitid;
    public String managerbusinessunitname;
    public String salesregionid;
    public String salesregionname;
    public boolean isMe = true;

    // Mark these as transient so that Gson will ignore them.
    public transient DateTime msus_last_generated_receipt;
    public transient DateTime msus_last_viewed_mileage_stats;
    public transient DateTime msus_last_accessed_milebuddy;
    public transient DateTime msus_last_used_milebuddy;
    public transient DateTime msus_last_opened_settings;
    public transient DateTime msus_last_synced_mileage;
    public transient DateTime msus_milebuddy_last_accessed_territory_changer;
    public transient DateTime msus_last_accessed_other_user_trips;

    public MediUser(JSONObject json) {

        try {
            try {
                if (!json.isNull("msus_last_generated_receipt")) {
                    this.msus_last_generated_receipt = (new DateTime(json.getString("msus_last_generated_receipt")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_milebuddy_version")) {
                    this.milebuddyVersion = (json.getString("msus_milebuddy_version"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_last_viewed_mileage_stats")) {
                    this.msus_last_viewed_mileage_stats = (new DateTime(json.getString("msus_last_viewed_mileage_stats")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_last_accessed_milebuddy")) {
                    this.msus_last_accessed_milebuddy = (new DateTime(json.getString("msus_last_accessed_milebuddy")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_last_accessed_other_user_trips")) {
                    this.msus_last_accessed_other_user_trips = (new DateTime(json.getString("msus_last_accessed_other_user_trips")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_milebuddy_last_accessed_territory_changer")) {
                    this.msus_milebuddy_last_accessed_territory_changer = (new DateTime(json.getString("msus_milebuddy_last_accessed_territory_changer")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_last_synced_mileage")) {
                    this.msus_last_synced_mileage = (new DateTime(json.getString("msus_last_synced_mileage")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_last_opened_settings")) {
                    this.msus_last_opened_settings = (new DateTime(json.getString("msus_last_opened_settings")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_last_used_milebuddy")) {
                    this.msus_last_used_milebuddy = (new DateTime(json.getString("msus_last_used_milebuddy")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("etag")) {
                    this.etag = (json.getString("etag"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_territoryid_valueFormattedValue")) {
                    this.territoryname = (json.getString("_territoryid_valueFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_territoryid_value")) {
                    this.territoryid = (json.getString("_territoryid_value"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_positionid_valueFormattedValue")) {
                    this.jobtitle = (json.getString("_positionid_valueFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_positionid_value")) {
                    this.jobtitleid = (json.getString("_positionid_value"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("systemuserid")) {
                    this.systemuserid = (json.getString("systemuserid"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("address1_latitude")) {
                    this.latitude = (json.getDouble("address1_latitude"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("address1_longitude")) {
                    this.longitude = (json.getDouble("address1_longitude"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_parentsystemuserid_valueFormattedValue")) {
                    this.managername = (json.getString("_parentsystemuserid_valueFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("address1_composite")) {
                    this.address = (json.getString("address1_composite"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_parentsystemuserid_value")) {
                    this.managerid = (json.getString("_parentsystemuserid_value"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("internalemailaddress")) {
                    this.email = (json.getString("internalemailaddress"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("fullname")) {
                    this.fullname = (json.getString("fullname"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("title")) {
                    this.title = (json.getString("title"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("domainname")) {
                    this.domainname = (json.getString("domainname"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("mobilephone")) {
                    this.mobilephone = (json.getString("mobilephone"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_msus_user_salesregion_value")) {
                    this.salesregionid = (json.getString("_msus_user_salesregion_value"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_msus_user_salesregion_valueFormattedValue")) {
                    this.salesregionname = (json.getString("_msus_user_salesregion_valueFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_push_onallorders")) {
                    this.pushonallorders = (json.getBoolean("msus_push_onallorders"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_push_onorder")) {
                    this.pushonorder = (json.getBoolean("msus_push_onorder"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_medibuddy_managed_territories")) {
                    this.msus_medibuddy_managed_territories = (json.getString("msus_medibuddy_managed_territories"));
                    String[] terrs = this.msus_medibuddy_managed_territories.split(",");
                    for (int i = 0; i < terrs.length; i++) {
                        managedTerritories.add(terrs[i]);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_businessunitid_valueFormattedValue")) {
                    this.businessunitname = (json.getString("_businessunitid_valueFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_businessunitid_value")) {
                    this.businessunitid = (json.getString("_businessunitid_value"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MediUser() {

    }

    public static MediUser createOne(RestResponse restresponse) {

        JSONArray array = null;
        try {
            array = new JSONArray(restresponse.value);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (!array.isNull(0)){
            try {
                return new MediUser( array.getJSONObject(0));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    public static MediUser createOne(String crmResponse) {
        RestResponse restResponse = new RestResponse(crmResponse);
        return MediUser.createOne(restResponse);
    }

    public static ArrayList<MediUser> createMany(RestResponse crmResponse) {
        ArrayList<MediUser> users = new ArrayList<>();
        try {
            JSONArray rootArray = new JSONArray(crmResponse.value);
            for (int i = 0; i < rootArray.length(); i++) {
                MediUser user = new MediUser(rootArray.getJSONObject(i));
                users.add(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    public static MediUser fromGson(String gson) {
        return new Gson().fromJson(gson, MediUser.class);
    }

    public boolean hasAddress() {
        return address != null;
    }

    public boolean hasGeoLoc() {
        return (latitude != 0d && longitude != 0d);
    }

    public String toGson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public boolean save(Context context) {
        MySqlDatasource datasource = new MySqlDatasource(context);
        return datasource.saveUser(this);
    }

    public static void deleteUsers() {
        MySqlDatasource ds = new MySqlDatasource();
        ds.deleteMe();
    }

    /**
     * Returns the current user as saved in the local database.
     */
    public static MediUser getMe(Context context) {
        MySqlDatasource ds = new MySqlDatasource(context);
        return ds.getMe();
    }

    /**
     * Returns the current user as saved in the local database.
     */
    public static MediUser getMe() {
        MySqlDatasource ds = new MySqlDatasource(MyApp.getAppContext());
        return ds.getMe();
    }

    public boolean isAdmin() {
        if (this.msus_medibuddy_managed_territories == null) {
            return false;
        }
        return this.msus_medibuddy_managed_territories.toLowerCase().equals("all");
    }

    /**
     * Builds a basic Territory object using this object's territoryid and territoryname properties.
     * @return A Territory object using this object's territory properties
     */
    public Territory getTerritory() {
        Territory territory = new Territory();
        territory.territoryName = territoryname;
        territory.territoryid = territoryid;
        return territory;
    }

    public static void updateCrmWithMyMileBuddyVersion(Context context, final MyInterfaces.CrmRequestListener callback) {
        EntityContainers.EntityContainer container = new EntityContainers.EntityContainer();
        container.entityFields.add(new EntityContainers.EntityField("msus_milebuddy_version",
                Float.toString(Helpers.Application.getAppVersion(context))));
        Requests.Request request = new Requests.Request(Requests.Request.Function.UPDATE);
        request.arguments.add(new Requests.Argument("entityid", MediUser.getMe().systemuserid));
        request.arguments.add(new Requests.Argument("entityname", "systemuser"));
        request.arguments.add(new Requests.Argument("containers", container.toJson()));
        request.arguments.add(new Requests.Argument("asuser", MediUser.getMe().systemuserid));

        Crm crm = new Crm();
        crm.makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                callback.onComplete(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                callback.onFail(error.getLocalizedMessage());
            }
        });
    }

    public static boolean isLoggedIn() {
        return MediUser.getMe() != null;
    }

    /**
     * Rudimentary evaluation that compares this object's systemuserid to the supplied systemuserid.
     * @param systemuserid The guid to compare to.
     * @return True if they match.
     */
    public boolean isMe(String systemuserid) {

        // Don't fuck with nulls
        if (this.systemuserid == null && systemuserid == null) {
            return false;
        }

        return this.systemuserid.equals(systemuserid);
    }

    protected MediUser(Parcel in) {
        etag = in.readString();
        territoryname = in.readString();
        territoryid = in.readString();
        jobtitle = in.readString();
        jobtitleid = in.readString();
        systemuserid = in.readString();
        managername = in.readString();
        managerid = in.readString();
        email = in.readString();
        fullname = in.readString();
        address = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        title = in.readString();
        domainname = in.readString();
        mobilephone = in.readString();
        pushonallorders = in.readByte() != 0x00;
        pushonorder = in.readByte() != 0x00;
        msus_medibuddy_managed_territories = in.readString();
        if (in.readByte() == 0x01) {
            managedTerritories = new ArrayList<String>();
            in.readList(managedTerritories, String.class.getClassLoader());
        } else {
            managedTerritories = null;
        }
        businessunitid = in.readString();
        businessunitname = in.readString();
        managerbusinessunitid = in.readString();
        managerbusinessunitname = in.readString();
        salesregionid = in.readString();
        salesregionname = in.readString();
        isMe = in.readByte() != 0x00;
        msus_last_generated_receipt = (DateTime) in.readValue(DateTime.class.getClassLoader());
        msus_last_viewed_mileage_stats = (DateTime) in.readValue(DateTime.class.getClassLoader());
        msus_last_accessed_milebuddy = (DateTime) in.readValue(DateTime.class.getClassLoader());
        msus_last_used_milebuddy = (DateTime) in.readValue(DateTime.class.getClassLoader());
        msus_last_opened_settings = (DateTime) in.readValue(DateTime.class.getClassLoader());
        msus_last_synced_mileage = (DateTime) in.readValue(DateTime.class.getClassLoader());
        msus_milebuddy_last_accessed_territory_changer = (DateTime) in.readValue(DateTime.class.getClassLoader());
        msus_last_accessed_other_user_trips = (DateTime) in.readValue(DateTime.class.getClassLoader());
        milebuddyVersion = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(etag);
        dest.writeString(territoryname);
        dest.writeString(territoryid);
        dest.writeString(jobtitle);
        dest.writeString(jobtitleid);
        dest.writeString(systemuserid);
        dest.writeString(managername);
        dest.writeString(managerid);
        dest.writeString(email);
        dest.writeString(fullname);
        dest.writeString(address);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(title);
        dest.writeString(domainname);
        dest.writeString(mobilephone);
        dest.writeByte((byte) (pushonallorders ? 0x01 : 0x00));
        dest.writeByte((byte) (pushonorder ? 0x01 : 0x00));
        dest.writeString(msus_medibuddy_managed_territories);
        if (managedTerritories == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(managedTerritories);
        }
        dest.writeString(businessunitid);
        dest.writeString(businessunitname);
        dest.writeString(managerbusinessunitid);
        dest.writeString(managerbusinessunitname);
        dest.writeString(salesregionid);
        dest.writeString(salesregionname);
        dest.writeByte((byte) (isMe ? 0x01 : 0x00));
        dest.writeValue(msus_last_generated_receipt);
        dest.writeValue(msus_last_viewed_mileage_stats);
        dest.writeValue(msus_last_accessed_milebuddy);
        dest.writeValue(msus_last_used_milebuddy);
        dest.writeValue(msus_last_opened_settings);
        dest.writeValue(msus_last_synced_mileage);
        dest.writeValue(msus_milebuddy_last_accessed_territory_changer);
        dest.writeValue(msus_last_accessed_other_user_trips);
        dest.writeString(milebuddyVersion);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MediUser> CREATOR = new Parcelable.Creator<MediUser>() {
        @Override
        public MediUser createFromParcel(Parcel in) {
            return new MediUser(in);
        }

        @Override
        public MediUser[] newArray(int size) {
            return new MediUser[size];
        }
    };

    @Override
    public String toString() {
        return this.fullname + ", " + this.systemuserid;
    }
}