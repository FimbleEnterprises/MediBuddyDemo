package com.fimbleenterprises.medimileage;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import static com.fimbleenterprises.medimileage.MySettingsHelper.MILEBUDDY_UPDATE_JSON;

public class MileBuddyUpdate {

    public static final String TAG = "MileBuddyUpdate";

    MySettingsHelper options;

    // public String name;
    public String changelog;
    public String downloadLink;
    public String guid;
    public DateTime releaseDate;
    public double version;
    public String json;

    public MileBuddyUpdate() {
        try {
            this.options = new MySettingsHelper(MyApp.getAppContext());
        } catch (Exception e) { }
    }
    public MileBuddyUpdate(JSONObject json) {

        try {
            options = new MySettingsHelper(MyApp.getAppContext());
        } catch (Exception e) { }

        this.json = json.toString();

            /*try {
                if (!json.isNull("msus_name")) {
                    this.name = (json.getString("msus_name"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }*/
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

    public boolean existsLocally() {
        File file = null;
        if (options != null) {
            file = new File(Helpers.Files.getAppDownloadDirectory() + File.separator +
                    "MileBuddy " + this.version);

        }
        return file != null && file.exists();
    }

    public void deleteLocally() {
        SharedPreferences prefs = options.getSharedPrefs();
        prefs.edit().remove(MILEBUDDY_UPDATE_JSON).commit();
        File file = new File(Helpers.Files.getAppDownloadDirectory() + File.separator +
                "MileBuddy " + this.version);
        if (file.exists()) {
            file.delete();
        }
        Log.i(TAG, "deleteLocally: Deleted local update");
    }

    public static void deleteAllLocallyAvailableUpdates() {
        int i = 0;
        MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());
        SharedPreferences prefs = options.getSharedPrefs();
        prefs.edit().remove(MILEBUDDY_UPDATE_JSON).commit();
        File[] files = Helpers.Files.getAppDirectory().listFiles();
        for (File file : files) {
            if (file.getName().endsWith(".apk")) {
                file.delete();
                i++;
            }
        }
        Log.i(TAG, "deleteLocally: Deleted " + i + " local updates");
    }

}
