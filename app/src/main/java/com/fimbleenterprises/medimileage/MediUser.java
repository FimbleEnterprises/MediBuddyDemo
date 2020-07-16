package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MediUser {

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
    public boolean isMe = true;

    public MediUser(com.fimbleenterprises.medimileage.RestResponse restresponse) {

        JSONArray array = null;
        try {
            array = new JSONArray(restresponse.value);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (!array.isNull(0)){
           try {
               JSONObject json = array.getJSONObject(0);
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
    }

    public MediUser() {

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

    public static MediUser getMe(Context context) {
        MySqlDatasource ds = new MySqlDatasource(context);
        return ds.getMe();
    }

    public static MediUser getMe() {
        MySqlDatasource ds = new MySqlDatasource(MyApp.getAppContext());
        return ds.getMe();
    }

}
