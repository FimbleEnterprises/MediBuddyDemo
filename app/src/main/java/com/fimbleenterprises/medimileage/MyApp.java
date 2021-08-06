package com.fimbleenterprises.medimileage;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fimbleenterprises.medimileage.activities.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.Random;

public class MyApp extends Application {

    private static final String TAG = "MyApp";

    public static final int PERMISSION_MAKE_RECEIPT = 0;
    public static final int PERMISSION_START_TRIP = 1;
    public static final int PERMISSION_MAKE_TRIP = 2;
    public static final int PERMISSION_UPDATE = 3;
    public static final int PERMISSION_FLOATY = 4;

    public static class AppVisibilityState {
        public boolean isVisible = false;
        public String callingActivity;

        public AppVisibilityState(boolean isVisible, String callingActivity) {
            this.isVisible = isVisible;
            this.callingActivity = callingActivity;
        }
    }

    /**
     * This is (an attempt) to track whether the app is in the foreground or background.  It is labor
     * intensive to track this as it requires all activities that can take the foreground to set this
     * flag in from either its onStop() or onStart() methods.  Labor intensive but not ridiculously so.
     */
    private static AppVisibilityState appVisibilityState = new AppVisibilityState(true, " ! INITIAL INSTANTIATION ! ");
    private static Context mContext;

    public static enum LocationPermissionResult {
        FULL, PARTIAL, NONE
    }

    /**
     * Call this from anywhere to determine if the app (in any form) is actually in the foreground.
     * Assuming devs have been diligent in updating the isVisible flag within their activity's onStart()
     * and onStop() methods this should be reliable.
     * @return Boolean.
     */
    public static AppVisibilityState isIsVisible() {
        Log.w(TAG, "-= isIsVisible: App in foreground: " + appVisibilityState.isVisible
                + " Set by: " + appVisibilityState.callingActivity + " =-");
        return appVisibilityState;
    }

    /**
     * Sets the private static isVisible flag which is used to globally determine if the app is
     * actually in the foreground or the background.
     */
    public static void setIsVisible(boolean val, Activity callingActivity) {
        appVisibilityState.isVisible = val;
        appVisibilityState.callingActivity = callingActivity.getLocalClassName();
        Log.w(TAG, "-= setIsVisible: App in foreground set to: " + appVisibilityState.isVisible
                + " by: " + appVisibilityState.callingActivity + " =-");

        // Don't be a fool and add any additional functionality in here!  In fact, this very method was
        // probably a bad idea.  Should probably have just made "isVisible" a public static instead of
        // using a setter...

    }

    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        fixGoogleMapBug();

        Method m = null;
        try {
            m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
            m.invoke(null);



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * I cannot remember what this fixes but I seem to remember it being pretty helpful once upon a time.
     */
    private void fixGoogleMapBug() {
        SharedPreferences googleBug = getSharedPreferences("google_bug",
                Context.MODE_PRIVATE);

        if (!googleBug.contains("fixed")) {
            File corruptedZoomTables = new File(getFilesDir(), "ZoomTables.data");
            corruptedZoomTables.delete();
            googleBug.edit().putBoolean("fixed", true).apply();
        }
    }

    /**
     * Checks that all permissions the app needs are valid.
     * @param activity The checkCallingOrSelfPermission() (which this method will soon call) is a
     *                 function of the Activity class so we need a valid activity in order to call it.
     * @return True if all conditions for full use are met.
     */
    public static boolean allPermissionsSatisfied(Activity activity) {
        return (checkLocationPermission(activity) == LocationPermissionResult.FULL
                && checkStoragePermission(activity) == true);
    }

    /**
     * Checks to what degree location permissions are granted.
     * @param activity The "checkCallingOrSelfPermission()" (which this method will soon call) is a
     *                 function of the Activity class so a valid activity is needed in order to call it.
     * @return         FULL, PARTIAL, NONE - The app really needs FULL to be returned in order to function.
     */
    public static LocationPermissionResult checkLocationPermission(Activity activity) {
        boolean result = true;
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        String bgPermission = "android.permission.ACCESS_BACKGROUND_LOCATION";
        int res1 = activity.checkCallingOrSelfPermission(permission);
        int res2 = activity.checkCallingOrSelfPermission(bgPermission);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (res1 == PackageManager.PERMISSION_GRANTED && res2 == PackageManager.PERMISSION_GRANTED) {
                return LocationPermissionResult.FULL;
            } else if (res1 == PackageManager.PERMISSION_GRANTED && res2 != PackageManager.PERMISSION_GRANTED ||
                    res2 == PackageManager.PERMISSION_GRANTED && res1 != PackageManager.PERMISSION_GRANTED) {
                return LocationPermissionResult.PARTIAL;
            } else {
                return LocationPermissionResult.NONE;
            }
        } else {
            if (res1 == PackageManager.PERMISSION_GRANTED) {
                return LocationPermissionResult.FULL;
            } else {
                return LocationPermissionResult.NONE;
            }
        }
    }

    /**
     * Checks if the app has the WRITE_EXTERNAL_STORAGE permission.
     * @param activity The "checkCallingOrSelfPermission()" (which this method will soon call) is a
     *                 function of the Activity class so a valid activity is needed in order to call it.
     * @return         True if true and false if false.
     */
    public static boolean checkStoragePermission(Activity activity) {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = activity.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public static Context getAppContext() {
        return mContext;
    }
}
