package com.fimbleenterprises.demobuddy.objects_and_containers;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Territories {

    public ArrayList<Territory> list;

    public Territories() {
        this.list = new ArrayList<>();
    }

    public Territories(String crmResponse) throws JSONException {

        this.list = new ArrayList<>();

        // ArrayList<Territory> territories = new ArrayList<>();
        JSONObject rootObject = new JSONObject(crmResponse);
        JSONArray rootArray = rootObject.getJSONArray("value");
        for (int i = 0; i < rootArray.length(); i++) {
            Territory territory = new Territory(rootArray.getJSONObject(i));
            this.list.add(territory);
        }
    }

    public static class Territory implements Parcelable {
        public String etag;
        public String territoryid;
        public String territoryName;
        public String managerName;
        public String managerId;
        public String repName;
        public String repId;
        public String regionName;
        public String regionId;

        public Territory() { }

        public Territory(JSONObject json) {

            try {
                if (!json.isNull("etag")) {
                    this.etag = (json.getString("etag"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("territoryid")) {
                    this.territoryid = (json.getString("territoryid"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("name")) {
                    this.territoryName = (json.getString("name"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_managerid_valueFormattedValue")) {
                    this.managerName = (json.getString("_managerid_valueFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_managerid_value")) {
                    this.managerId = (json.getString("_managerid_value"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_msus_salesregion_valueFormattedValue")) {
                    this.regionName = (json.getString("_msus_salesregion_valueFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_msus_salesregion_value")) {
                    this.regionId = (json.getString("_msus_salesregion_value"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_new_salesrepresentative_valueFormattedValue")) {
                    this.repName = (json.getString("_new_salesrepresentative_valueFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_new_salesrepresentative_value")) {
                    this.repId = (json.getString("_new_salesrepresentative_value"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public static ArrayList<Territory> createMany(String crmServerResponse) {
            try {
                ArrayList<Territory> territories = new ArrayList<>();
                JSONObject rootObject = new JSONObject(crmServerResponse);
                JSONArray rootArray = rootObject.getJSONArray("value");
                for (int i = 0; i < rootArray.length(); i++) {
                    Territory territory = new Territory(rootArray.getJSONObject(i));
                    territories.add(territory);
                }
                return territories;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public String toString() {
            return this.territoryName + " - " + this.repName;
        }

        protected Territory(Parcel in) {
            etag = in.readString();
            territoryid = in.readString();
            territoryName = in.readString();
            managerName = in.readString();
            managerId = in.readString();
            repName = in.readString();
            repId = in.readString();
            regionName = in.readString();
            regionId = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(etag);
            dest.writeString(territoryid);
            dest.writeString(territoryName);
            dest.writeString(managerName);
            dest.writeString(managerId);
            dest.writeString(repName);
            dest.writeString(repId);
            dest.writeString(regionName);
            dest.writeString(regionId);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<Territory> CREATOR = new Parcelable.Creator<Territory>() {
            @Override
            public Territory createFromParcel(Parcel in) {
                return new Territory(in);
            }

            @Override
            public Territory[] newArray(int size) {
                return new Territory[size];
            }
        };
    }
}