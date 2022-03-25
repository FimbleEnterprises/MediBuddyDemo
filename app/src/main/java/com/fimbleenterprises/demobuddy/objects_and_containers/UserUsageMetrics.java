package com.fimbleenterprises.demobuddy.objects_and_containers;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserUsageMetrics {

    public ArrayList<UserUsageMetric> list = new ArrayList<>();

    public UserUsageMetrics(String serverJson) {
        try {
            JSONObject root = new JSONObject(serverJson);
            JSONArray rootArray = root.getJSONArray("value");
            for (int i = 0; i < rootArray.length(); i++) {
                this.list.add(new UserUsageMetric(rootArray.getJSONObject(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class UserUsageMetric extends CrmEntities.CrmEntity {

        public DateTime msus_last_accessed_milebuddy;
        public String systemuserid;
        public DateTime msus_last_generated_receipt;
        public DateTime msus_last_opened_settings;
        public String fullname;
        public String msus_milebuddy_version;
        public DateTime msus_milebuddy_last_accessed_territory_changer;
        public DateTime msus_last_used_milebuddy;
        public DateTime msus_milebuddy_last_accessed_territory_data;
        public boolean msus_use_trip_minder;
        public String msus_trip_minder_value;
        public DateTime msus_last_accessed_other_user_trips;
        public DateTime msus_last_accessed_account_data;
        public DateTime msus_last_synced_mileage;
        public DateTime msus_last_viewed_mileage_stats;
        public DateTime msus_last_accessed_search;
        public DateTime msus_last_opened_ticket;
        public DateTime msus_last_opened_opportunity;
        public DateTime msus_last_created_note;

        public UserUsageMetric(JSONObject json) {

            this.logicalname = "systemuser";
            this.pluralname = "systemusers";

            try {
                if (!json.isNull("fullname")) {
                    this.fullname = (json.getString("fullname"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
               if (!json.isNull("msus_last_created_note")) {
                   this.msus_last_created_note = (new DateTime(json.getString("msus_last_created_note")));
               }
            } catch (JSONException e) {
               e.printStackTrace();
            }

            try {
               if (!json.isNull("msus_last_opened_opportunity")) {
                   this.msus_last_opened_opportunity = (new DateTime(json.getString("msus_last_opened_opportunity")));
               }
            } catch (JSONException e) {
               e.printStackTrace();
            }

            try {
               if (!json.isNull("msus_last_opened_ticket")) {
                   this.msus_last_opened_ticket = (new DateTime(json.getString("msus_last_opened_ticket")));
               }
            } catch (JSONException e) {
               e.printStackTrace();
            }

            try {
               if (!json.isNull("msus_last_accessed_search")) {
                   this.msus_last_accessed_search = (new DateTime(json.getString("msus_last_accessed_search")));
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
               if (!json.isNull("msus_last_synced_mileage")) {
                   this.msus_last_synced_mileage = (new DateTime(json.getString("msus_last_synced_mileage")));
               }
            } catch (JSONException e) {
               e.printStackTrace();
            }

            try {
               if (!json.isNull("msus_last_accessed_account_data")) {
                   this.msus_last_accessed_account_data = (new DateTime(json.getString("msus_last_accessed_account_data")));
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
                if (!json.isNull("msus_trip_minder_value")) {
                    this.msus_trip_minder_value = (json.getString("msus_trip_minder_value"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                if (!json.isNull("msus_use_trip_minder")) {
                    this.msus_use_trip_minder = (json.getBoolean("msus_use_trip_minder"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
               if (!json.isNull("msus_milebuddy_last_accessed_territory_data")) {
                   this.msus_milebuddy_last_accessed_territory_data = (new DateTime(json.getString("msus_milebuddy_last_accessed_territory_data")));
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
               if (!json.isNull("msus_milebuddy_last_accessed_territory_changer")) {
                   this.msus_milebuddy_last_accessed_territory_changer = (new DateTime(json.getString("msus_milebuddy_last_accessed_territory_changer")));
               }
            } catch (JSONException e) {
               e.printStackTrace();
            }

            try {
                if (!json.isNull("msus_milebuddy_version")) {
                    this.msus_milebuddy_version = (json.getString("msus_milebuddy_version"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
               if (!json.isNull("msus_last_generated_receipt")) {
                   this.msus_last_generated_receipt = (new DateTime(json.getString("msus_last_generated_receipt")));
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
                if (!json.isNull("etag")) {
                    this.etag = (json.getString("etag"));
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
                if (!json.isNull("systemuserid")) {
                    this.systemuserid = (json.getString("systemuserid"));
                    this.entityid = this.systemuserid;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public BasicObjects.BasicObject toBasicObject() {
            return new BasicObjects.BasicObject(fullname, msus_milebuddy_version, this);
        }

    }

}
