package com.fimbleenterprises.medimileage;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

public class MileBuddyUpdate {

    public String name;
    public String changelog;
    public String downloadLink;
    public String guid;
    public DateTime releaseDate;
    public double version;

    public MileBuddyUpdate(JSONObject json) {

            try {
                if (!json.isNull("msus_name")) {
                    this.name = (json.getString("msus_name"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_milebuddyupdateid")) {
                    this.guid = (json.getString("msus_milebuddyupdateid"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_changelog")) {
                    this.changelog = (json.getString("msus_changelog"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_download_link")) {
                    this.downloadLink = (json.getString("msus_download_link"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_releasedateFormattedValue")) {
                    this.releaseDate = (new DateTime(json.getString("msus_releasedate")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_version")) {
                    this.version = (json.getDouble("msus_version"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

    }

}
