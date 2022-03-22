package com.fimbleenterprises.medimileage.objects_and_containers;

import android.content.SharedPreferences;
import android.util.Log;

import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.MyApp;
import com.fimbleenterprises.medimileage.MyPreferencesHelper;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import static com.fimbleenterprises.medimileage.MyPreferencesHelper.MEDIBUDDY_UPDATE_JSON;

public class MediBuddyUpdate {

    public static final String TAG = "MileBuddyUpdate";

    MyPreferencesHelper options;

    // public String name;
    public String changelog;
    public String downloadLink;
    public String guid;
    public DateTime releaseDate;
    public double version;
    public String json;

    public MediBuddyUpdate() {
        try {
            this.options = new MyPreferencesHelper(MyApp.getAppContext());
        } catch (Exception e) { }
    }
    public MediBuddyUpdate(JSONObject json) {

        try {
            options = new MyPreferencesHelper(MyApp.getAppContext());
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
            file = new File(Helpers.Files.AppUpdates.getDirectory() + File.separator +
                    "MileBuddy " + this.version);

        }
        return file != null && file.exists();
    }

    public void deleteLocally() {
        SharedPreferences prefs = options.getSharedPrefs();
        prefs.edit().remove(MEDIBUDDY_UPDATE_JSON).commit();
        File file = new File(Helpers.Files.AppUpdates.getDirectory() + File.separator +
                "MileBuddy " + this.version);
        if (file.exists()) {
            file.delete();
        }
        Log.i(TAG, "deleteLocally: Deleted local update");
    }

    public static void deleteAllLocallyAvailableUpdates() {
        int i = 0;
        MyPreferencesHelper options = new MyPreferencesHelper(MyApp.getAppContext());
        SharedPreferences prefs = options.getSharedPrefs();
        prefs.edit().remove(MEDIBUDDY_UPDATE_JSON).commit();
        File[] files = Helpers.Files.AppUpdates.getFiles();
        for (File file : files) {
            if (! file.isDirectory() && file.getName().endsWith(".apk")) {
                file.delete();
                i++;
            }
        }
        Log.i(TAG, "deleteLocally: Deleted " + i + " local updates");
    }

}
