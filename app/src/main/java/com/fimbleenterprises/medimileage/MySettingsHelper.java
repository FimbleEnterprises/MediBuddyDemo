package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.rpc.Help;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.json.JSONObject;

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
    public static final String NAME_TRIP_ON_START = "NAME_TRIP_ON_START";
    public static final String DEBUG_MODE = "DEBUG_MODE";
    public static final String MAP_MODE = "MAP_MODE";
    public static final String MILEBUDDY_UPDATE_JSON = "MILEBUDDY_UPDATE_JSON";
    public static final String FCM_TOKEN = "FCM_TOKEN";
    public static final String LAST_UPDATED_ACT_ADDYS = "LAST_UPDATED_ACT_ADDYS";
    public static final String LAST_UPDATED_USER_ADDYS = "LAST_UPDATED_USER_ADDYS";
    public static final String RECEIPT_FORMATS = "RECEIPT_FORMATS";
    public static final String LAST_TRIP_AUTO_KILLED = "lasttripautokilled";
    public static final String ALL_ADDRESSES_JSON = "ALL_ADDRESSES_JSON";
    public static final String DISTANCE_THRESHOLD = "DISTANCE_THRESHOLD";
    public static final String EXPLICIT_MODE = "EXPLICIT_MODE";
    public static final String SET_DEFAULTS = "SET_DEFAULTS";
    public static final String SERVER_BASE_URL = "SERVER_BASE_URL";
    public static final String OPPORTUNITIES_JSON = "OPPORTUNITIES_JSON";
    public static final String SHOW_OPPORTUNITY_MGR = "SHOW_OPPORTUNITY_MGR";

    public static final String RECEIPT_FORMAT_PNG = ".png";
    public static final String RECEIPT_FORMAT_JPEG = ".jpeg";
    public static final String RECEIPT_FORMAT_TXT = ".txt";

    Context context;
    SharedPreferences prefs;

    public MySettingsHelper(Context context) {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public SharedPreferences getSharedPrefs() {
        return prefs;
    }

    public void setDefaults() {
        prefs.edit().clear().commit();
    }

    public void setServerBaseUrl(String url) {
        prefs.edit().putString(SERVER_BASE_URL, url).commit();
    }

    public String getServerBaseUrl() {
        return prefs.getString(SERVER_BASE_URL, context.getString(R.string.base_server_url));
    }

    public boolean isExplicitMode() {
        return prefs.getBoolean(EXPLICIT_MODE, false);
    }

    public boolean showOpportunityOptions() {
        return prefs.getBoolean(SHOW_OPPORTUNITY_MGR, true);
    }

    public void setShowOpportunityMgr(Boolean value) {
        prefs.edit().putBoolean(SHOW_OPPORTUNITY_MGR, value).commit();
    }

    public void isExplicitMode(boolean value) {
        prefs.edit().putBoolean(EXPLICIT_MODE, value).commit();
    }

    public boolean getShouldUpdateUserAddys() {
        try {
            DateTime now = DateTime.now();
            DateTime lastUpdated = new DateTime(prefs.getString(LAST_UPDATED_USER_ADDYS, null));
            return Days.daysBetween(lastUpdated, now).getDays() >= UserAddresses.ADDYS_VALID_FOR_X_DAYS;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public boolean getShouldUpdateActAddys() {
        try {
            DateTime now = DateTime.now();
            DateTime lastUpdated = new DateTime(prefs.getString(LAST_UPDATED_ACT_ADDYS, null));
            return Days.daysBetween(lastUpdated, now).getDays() >= AccountAddresses.ADDYS_VALID_FOR_X_DAYS;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public double getDistanceThreshold() {
        String dist = prefs.getString(DISTANCE_THRESHOLD, "1609");
        return Double.parseDouble(dist);
    }

    public double getDistanceThresholdInMiles() {
        double dist = Double.parseDouble(prefs.getString(DISTANCE_THRESHOLD, "1609"));
        return Helpers.Geo.convertMetersToMiles(dist, 1);
    }

    public CrmEntities.CrmAddresses getAllSavedCrmAddresses() {
        String json = prefs.getString(ALL_ADDRESSES_JSON, null);
        if (json != null) {
            return new Gson().fromJson(json, CrmEntities.CrmAddresses.class);
        } else {
            return null;
        }
    }

    public void saveAllCrmAddresses(CrmEntities.CrmAddresses addresses) {
        prefs.edit().putString(ALL_ADDRESSES_JSON, addresses.toGson()).commit();
    }

    public boolean hasSavedAddresses() {
        return (getAllSavedCrmAddresses() != null);
    }

    public boolean hasSavedOpportunities() {
        return (getSavedOpportunities() != null);
    }

    public boolean addressIsSaved(CrmEntities.CrmAddresses.CrmAddress address) {
        CrmEntities.CrmAddresses addresses = this.getAllSavedCrmAddresses();
        for (CrmEntities.CrmAddresses.CrmAddress addy : addresses.list) {
            if (addy.accountid.equals(address.accountid)) {
                return true;
            }
        }
        return false;
    }

    public int countSavedAddresses() {
        CrmEntities.CrmAddresses addresses = getAllSavedCrmAddresses();
        return (addresses != null ? addresses.list.size() : 0);
    }

    /**
     * Returns the extension of the generated receipt file (.png, .jpeg or .txt)
     * @return The extension of the generated receipt file.
     */
    public String getReceiptFormat() {
        return prefs.getString(RECEIPT_FORMATS, ".png");
    }

    /**
     * Sets the generated receipt file's format and extension (.png, .jpeg or .txt)
     * @param receiptFormat "A string representing an appropriate extension (.png, .jpeg or .txt)
     */
    public void setReceiptFormat(String receiptFormat) {
        prefs.edit().putString(RECEIPT_FORMATS, receiptFormat).commit();
    }

    /**
     * Sets the default receipt format to .png
     */
    public void setDefaultReceiptFormat() {
        prefs.edit().putString(RECEIPT_FORMATS, RECEIPT_FORMAT_PNG).commit();
    }

    public void updateLastUpdatedUserAddys() {
        prefs.edit().putString(LAST_UPDATED_USER_ADDYS, DateTime.now().toString()).commit();
    }

    public void updateLastUpdatedActAddys() {
        prefs.edit().putString(LAST_UPDATED_ACT_ADDYS, DateTime.now().toString()).commit();
    }

    public String getFcmToken() {
        return prefs.getString(FCM_TOKEN, null);
    }

    public void setFcmToken(String token) {
        prefs.edit().putString(FCM_TOKEN, token).commit();
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

    public CrmEntities.Opportunities getSavedOpportunities() {
        return CrmEntities.Opportunities.fromGson(prefs.getString(OPPORTUNITIES_JSON, null));
    }

    public void saveOpportunities(CrmEntities.Opportunities opportunities) {
        prefs.edit().putString(OPPORTUNITIES_JSON, opportunities.toGson()).commit();
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

    public boolean lastTripAutoKilled() {
        boolean value = prefs.getBoolean(LAST_TRIP_AUTO_KILLED, false);
        Log.i(TAG, "lastTripAutoKilled: " + value);
        return value;
    }

    public void lastTripAutoKilled(boolean value) {
        Log.i(TAG, "setLastTripAutoKilled: " + value);
        prefs.edit().putBoolean(LAST_TRIP_AUTO_KILLED, value).commit();
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

    public void setMapMode(int mapMode) {
        prefs.edit().putInt(MAP_MODE, mapMode).commit();
    }

    public int getMapMode() {
        return prefs.getInt(MAP_MODE, 1);
    }

    public int getTripMinderIntervalMillis() {
        String val = prefs.getString(TRIP_MINDER_INTERVAL, "150000");
        return (Integer.parseInt(val));
    }

    public void setTripMinderIntervalMillis(int millis) {
        prefs.edit().putString(TRIP_MINDER_INTERVAL, Integer.toString(millis)).commit();
    }

    public boolean getConfirmTripEnd() {
        return prefs.getBoolean(CONFIRM_END, false);
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

    /**
     * Checks if the preferences has a saved update populated on a previous update check.  It also
     * checks if the file has already been downloaded and exists.  If either is false it clears the
     * preferences with respect to the update entirely.
     * @return True if update information is saved into preferences and the file has already been
     * downloaded and exists.
     */
    public boolean updateIsAvailableLocally() {

        MileBuddyUpdate update = null;

        String val = prefs.getString(MILEBUDDY_UPDATE_JSON, null);
        if (val != null) {
            try {
                JSONObject json = new JSONObject(prefs.getString(MILEBUDDY_UPDATE_JSON, null));
                if (json != null) {
                    update = new MileBuddyUpdate(json);
                }
            } catch (Exception e) {
                Log.w(TAG, "updateIsAvailableLocally: Failed checking for a local update!");
                e.printStackTrace();
            }
        }

        if (update == null) {
            Log.w(TAG, "updateIsAvailableLocally: No local update was found on the device.");
            clearMileBuddyUpdate();
            return false;
        } else if (update.version > Helpers.Application.getAppVersion(MyApp.getAppContext())) {
            Log.i(TAG, "updateIsAvailableLocally Preferences says a local update is available.  " +
                    "Checking if the actual file exists...");
            File file = new File(Helpers.Files.getAppDownloadDirectory().getPath(), update.version + ".apk");
            if (file.exists()) {
                Log.i(TAG, "updateIsAvailableLocally File is available!");
                return true;
            } else {
                Log.w(TAG, "updateIsAvailableLocally: File does not exist!  Clearing preferences " +
                        "of an available local update!");
                clearMileBuddyUpdate();
                return false;
            }
        } else {
            Log.w(TAG, "updateIsAvailableLocally: No local update was found.  Clearing all things " +
                    "update related.  Update will be downloaded if available next time it is checked.");
            clearMileBuddyUpdate();
            return false;
        }
    }

    public MileBuddyUpdate getMileBuddyUpdate() {
        MileBuddyUpdate update = null;
        try {
            update = new MileBuddyUpdate(new JSONObject(prefs.getString(MILEBUDDY_UPDATE_JSON, null)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return update;
    }

    public void setMilebuddyUpdate(String update) {
        if (update == null) {
            prefs.edit().remove(MILEBUDDY_UPDATE_JSON).commit();
        } else {
            prefs.edit().putString(MILEBUDDY_UPDATE_JSON, update).commit();
        }
    }

    public void clearMileBuddyUpdate() {
        prefs.edit().remove(MILEBUDDY_UPDATE_JSON).commit();
    }

    public boolean getNameTripOnStart() {
        return prefs.getBoolean(NAME_TRIP_ON_START, false);
    }

    public void setNameTripOnStart(boolean val) {
        prefs.edit().putBoolean(NAME_TRIP_ON_START, val).commit();
    }

    public void setDebugMode(boolean val) {
        prefs.edit().putBoolean(DEBUG_MODE, val).commit();
    }

    public boolean getDebugMode() {
        return prefs.getBoolean(DEBUG_MODE, false);
    }
}

























































