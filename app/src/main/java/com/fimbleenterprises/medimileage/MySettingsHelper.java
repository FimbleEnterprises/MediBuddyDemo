package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;

public class MySettingsHelper {

    private static final String TAG = "MySettingsHelper";
    public static final String DB_PATH = "DB_PATH";
    public static final String CACHED_USERNAME = "CACHED_USERNAME";
    public static final String CACHED_PASSWORD = "CACHED_PASSWORD";
    public static final String SHOW_GOOGLE_POLY = "SHOW_GOOGLE_POLY";
    public static final String SHOW_GOOGLE_SUGGESTED = "SHOW_GOOGLE_SUGGESTED";
    public static final String REIMBURSEMENT_RATE = "REIMBURSEMENT_RATE";
    public static final String LAST_PAGE = "LAST_PAGE";
    public static final String AUTH_SHOWING = "AUTH_SHOWING";
    public static final String SUBMIT_ON_END = "SUBMIT_ON_END";
    public static final String TRIP_MINDER = "TRIP_MINDER";
    public static final String TRIP_MINDER_INTERVAL = "TRIP_MINDER_INTERVAL";
    public static final String CONFIRM_END = "CONFIRM_END";
    public static final String CHECK_FOR_UPDATES = "CHECK_FOR_UPDATES";
    public static final String UPDATE_AVAILABLE = "UPDATE_AVAILABLE";
    public static final String UPDATE_FILENAME = "UPDATE_FILENAME";
    public static final String UPDATE_VERSION = "UPDATE_VERSION";
    public static final String UPDATE_CHANGELOG = "UPDATE_CHANGELOG";
    public static final String NAME_TRIP_ON_START = "NAME_TRIP_ON_START";

    Context context;
    SharedPreferences prefs;

    public MySettingsHelper(Context context) {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getDbPath() {
        return prefs.getString(DB_PATH, null);
    }

    public void setDbPath(String path) {
        prefs.edit().putString(DB_PATH, path).commit();
    }

    public String getCachedUsername() {
        return prefs.getString(CACHED_USERNAME, "");
    }

    public void setCachedUsername(String username) {
        prefs.edit().putString(CACHED_USERNAME, username).commit();
    }

    public String getCachedPassword() {
        return prefs.getString(CACHED_PASSWORD, "");
    }

    public void setCachedPassword(String password) {
        prefs.edit().putString(CACHED_PASSWORD, password).commit();
    }

    public void clearCachedCredentials() {
        prefs.edit().putString(CACHED_USERNAME, null).commit();
        prefs.edit().putString(CACHED_PASSWORD, null).commit();
    }

    public boolean hasCachedCredentials() {
        return ((getCachedUsername() != null && getCachedUsername().length() > 0)
                && (getCachedPassword() != null && getCachedPassword().length() > 0));
    }

    public boolean getShowHelp_GoogleSuggestedDistance() {
        return prefs.getBoolean(SHOW_GOOGLE_SUGGESTED, false);
    }

    public void setShowHelp_GoogleSuggestedDistance(boolean checked) {
        prefs.edit().putBoolean(SHOW_GOOGLE_SUGGESTED, checked).commit();
    }

    public void setReimbursementRate(float rate) {
        prefs.edit().putFloat(REIMBURSEMENT_RATE, rate).commit();
        Log.i(TAG, "setReimbursementRate : " + prefs.getFloat(REIMBURSEMENT_RATE, 0f));
    }

    public float getReimbursementRate() {
        float val = prefs.getFloat(REIMBURSEMENT_RATE, 0);
        return val;
    }

    public String getPrettyReimbursementRate() {
        float val = prefs.getFloat(REIMBURSEMENT_RATE, 0);
        return "$" + val;
    }

    public void setLastPage(int lastPage) {
        prefs.edit().putInt(LAST_PAGE, 0).commit();
    }

    public int getLastPage() {
        return prefs.getInt(LAST_PAGE, 0);
    }

    public void logout() {
        prefs.edit().remove(CACHED_USERNAME).commit();
        prefs.edit().remove(CACHED_PASSWORD).commit();
    }

    public boolean authenticateFragIsVisible() {
        return prefs.getBoolean(AUTH_SHOWING, false);
    }

    public void authenticateFragIsVisible(boolean isShowing) {
        prefs.edit().putBoolean(AUTH_SHOWING, isShowing).commit();
    }

    public boolean getAutosubmitOnTripEnd() {
        return prefs.getBoolean(SUBMIT_ON_END, true);
    }

    public void setAutosubmitOnTripEnd(boolean isShowing) {
        prefs.edit().putBoolean(SUBMIT_ON_END, isShowing).commit();
    }

    public boolean getTripEndReminder() {
        return prefs.getBoolean(TRIP_MINDER, true);
    }

    public void setTripEndReminder(boolean val) {
        prefs.edit().putBoolean(TRIP_MINDER, val).commit();
    }

    public int getTripMinderIntervalMillis() {
        String val = prefs.getString(TRIP_MINDER_INTERVAL, "150000");
        return (Integer.parseInt(val));
    }

    public void setTripMinderIntervalMillis(int millis) {
        prefs.edit().putString(TRIP_MINDER_INTERVAL, Integer.toString(millis)).commit();
    }

    public boolean getConfirmTripEnd() {
        return prefs.getBoolean(CONFIRM_END, true);
    }

    public void setConfirmTripEnd(boolean val) {
        prefs.edit().putBoolean(CONFIRM_END, val).commit();
    }

    public boolean getCheckForUpdates() {
        return prefs.getBoolean(CHECK_FOR_UPDATES, true);
    }

    public void setCheckForUpdates(boolean val) {
        prefs.edit().putBoolean(CHECK_FOR_UPDATES, val).commit();
    }

    public boolean updateIsAvailable() {
        return prefs.getBoolean(UPDATE_AVAILABLE, false);
    }

    public void updateIsAvailable(boolean val) {
        prefs.edit().putBoolean(UPDATE_AVAILABLE, val).commit();
    }

    public File getUpdateFile() {
        try {
            File file = new File(prefs.getString(UPDATE_FILENAME, null));
            if (file.exists()) {
                Log.i(TAG, "getUpdateFile:" + file.getAbsolutePath());
                return file;
            } else {
                Log.i(TAG, "getUpdateFile: Does not exist");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setUpdateFile(File file) {
        if (file == null) {
            prefs.edit().putString(UPDATE_FILENAME, null).commit();
            Log.i(TAG, "setUpdateFile: null");
            return;
        }

        prefs.edit().putString(UPDATE_FILENAME, file.getAbsolutePath()).commit();
        Log.i(TAG, "setUpdateFile:" + file.getAbsolutePath());
    }

    public float getUpdateVersion() {
        Log.i(TAG, "getUpdateVersion " + prefs.getFloat(UPDATE_VERSION, 1.0f));
        return prefs.getFloat(UPDATE_VERSION, 1.0f);
    }

    public void setUpdateVersion(float version) {
        prefs.edit().putFloat(UPDATE_VERSION, version).commit();
        Log.i(TAG, "getUpdateVersion " + version);
    }

    public void setUpdateChangelog(String changelog) {
        prefs.edit().putString(UPDATE_CHANGELOG, changelog).commit();
    }

    public String getUpdateChangelog() {
        return prefs.getString(UPDATE_CHANGELOG, null);
    }

    public boolean getNameTripOnStart() {
        return prefs.getBoolean(NAME_TRIP_ON_START, true);
    }

    public void setNameTripOnStart(boolean val) {
        prefs.edit().putBoolean(NAME_TRIP_ON_START, val).commit();
    }
}

























































