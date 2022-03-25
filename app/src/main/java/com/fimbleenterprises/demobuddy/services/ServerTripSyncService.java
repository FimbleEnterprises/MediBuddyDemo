package com.fimbleenterprises.demobuddy.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.fimbleenterprises.demobuddy.Crm;
import com.fimbleenterprises.demobuddy.CrmQueries;
import com.fimbleenterprises.demobuddy.MySqlDatasource;
import com.fimbleenterprises.demobuddy.R;
import com.fimbleenterprises.demobuddy.activities.MainActivity;
import com.fimbleenterprises.demobuddy.objects_and_containers.FullTrip;
import com.fimbleenterprises.demobuddy.objects_and_containers.MediUser;
import com.fimbleenterprises.demobuddy.objects_and_containers.MileBuddyMetrics;
import com.fimbleenterprises.demobuddy.objects_and_containers.Requests;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.joda.time.DateTime;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import cz.msebera.android.httpclient.Header;

//--------------------------------------------------------------------------------
/* This service's purpose is to get the sync trips logic off of the main thread or even associated
   with the app's front end.  Prior to this service it was executed using an async task off of the
   mileage fragment.  This was pretty much fine but if a user began a sync and then closed or
   minimized the activity it could be problematic.*/
//--------------------------------------------------------------------------------

/**
 * Service to reconcile client side to server side trips.  Concerned parties should subscribe to
 * broadcasts bearing the action, "TRIP_SYNC_SERVICE".
 */
public class ServerTripSyncService extends Service {

    public static final String TRIP_SYNC_SERVICE = "TRIP_SYNC_SERVICE";
    private static final String TAG = "ServerTripSyncService";
    public static final String MY_SERVICE_NOTIFICATION_CHANNEL = "MY_SERVICE_NOTIFICATION_CHANNEL";
    public static final int START_ID = 888;
    private static final String WAKELOCK_ACQUIRED = "WAKELOCK_ACQUIRED";
    public static final String STARTED = "STARTED";
    public static final String COMPLETED = "COMPLETED";
    public static final String FAILED = "FAILED";
    public static final String PROGRESS = "PROGRESS";

    public static boolean isRunning = false;
    Notification notification;
    PowerManager.WakeLock wakeLock;
    ArrayList<FullTrip> locallySavedTrips = new ArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        if (intent != null) {

            getWakelock();

            locallySavedTrips = new MySqlDatasource().getTrips();

            notification = getNotification("DOING SOMETHING",
                    "MY SERVICE IS RUNNING IN THE BACKGROUND, DAWG!", true);

            try {
                syncTrips();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                stopSelf();
            }

        }

        Notification notification = getNotification("Syncing Trips", "Syncing trip list with CRM.", false);
        NotificationManager mNotificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        // mNotificationManager.notify(START_ID, notification);
        startForeground(START_ID, notification);

        isRunning = wakeLock.isHeld();

        Intent serviceStartingBroadcastIntent = new Intent(TRIP_SYNC_SERVICE);
        serviceStartingBroadcastIntent.putExtra(STARTED, true);
        sendBroadcast(serviceStartingBroadcastIntent);

        return START_NOT_STICKY;
    }

    private Notification getNotification(String title, String text, boolean showProgress){

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(
                    new NotificationChannel(MY_SERVICE_NOTIFICATION_CHANNEL, NOTIFICATION_SERVICE
                            , NotificationManager.IMPORTANCE_HIGH));
        }

        Intent onClickIntent = new Intent(this, MainActivity.class);
        onClickIntent.setAction(Intent.ACTION_MAIN);
        onClickIntent.addCategory(Intent.CATEGORY_LAUNCHER);


        // The PendingIntent to launch our activity if the user selects
        // this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle().bigText(text);

        return new NotificationCompat.Builder(this, MY_SERVICE_NOTIFICATION_CHANNEL)
                .setContentTitle(title)
                .setContentText(text)
                .setOnlyAlertOnce(true) // so when data is updated don't make sound and alert in android 8.0+
                .setOngoing(false)
                .setStyle(style)
                .setProgress(0,0,showProgress)
                .setSmallIcon(R.drawable.notification_small_car)
                .setContentIntent(contentIntent)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.car2_static_round_tparent_icon))
                .build();
    }

    /**
     * This is the method that can be called to update the Notification
     */
    private void updateNotification(String title, String text, boolean showProgress){

        Notification notification= getNotification(title, text, showProgress);

        NotificationManager mNotificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(START_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        Log.e(TAG, "onDestroy");
        releaseWakelock();
    }

    private void getWakelock() {
        Log.d(TAG, "getWakelock Acquiring wakelock...");
        PowerManager mgr = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        wakeLock = mgr.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "MediBuddy:MyWakeLock");
        wakeLock.acquire();
        Log.d(TAG, "getWakelock Wakelock is held = " + wakeLock.isHeld());

        if (wakeLock.isHeld()) {
            Intent intent = new Intent(MY_SERVICE_NOTIFICATION_CHANNEL);
            intent.putExtra(WAKELOCK_ACQUIRED, true);
            sendBroadcast(intent);
        } else {
            if (! wakeLock.isHeld()) {
                Log.i(TAG, "getWakelock NOT HELD!");
                wakeLock.acquire();
            }
        }
    }

    private void releaseWakelock() {
        if (wakeLock == null) return;

        Log.d(TAG, "releaseWakelock Wakelock is held = " + wakeLock.isHeld());

        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    private void syncTrips() throws UnsupportedEncodingException {

        /*final MyProgressDialog dialog = new MyProgressDialog(this, "Getting submitted trips from server...",
                MyProgressDialog.PROGRESS_TYPE);*/
        // dialog.show();
        Log.i(TAG, "syncTrips Beginning sync...");

        Crm crm = new Crm();
        Requests.Request request = new Requests.Request();
        request.function = Requests.Request.Function.GET.name();
        request.arguments.add(new Requests.Argument("query", CrmQueries.Trips.getAllTripsByOwnerForLastXmonths(2, MediUser.getMe().systemuserid)));

        // Log a metric
        MileBuddyMetrics.updateMetric(this, MileBuddyMetrics.MetricName.LAST_ACCESSED_MILEAGE_SYNC, DateTime.now());

        crm.makeCrmRequest(this, request, Crm.Timeout.LONG, new AsyncHttpResponseHandler() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                final String response = new String(responseBody);

                Log.i(TAG, "onSuccess Sync returned: " + response);

                // Mark all locals as unsubmitted - this should get corrected when we loop through the server trips...
                if (responseBody != null && locallySavedTrips != null && locallySavedTrips.size() > 0) {
                    for (FullTrip trip : locallySavedTrips) {
                        if (!trip.isSeparator) {
                            trip.setIsSubmitted(false);
                            trip.save();
                            Log.i(TAG, "Unsubmitted local trip: " + trip.getTitle());
                        }
                    }
                }

                // Loop through the server trips and compare to locals.
                new AsyncTask<String, String, String>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                    }

                    @Override
                    protected String doInBackground(String... strings) {
                        ArrayList<FullTrip> serverTrips = FullTrip.createTripsFromCrmJson(response, false);
                        int totalTrips = serverTrips.size();
                        int i = 1;
                        for (FullTrip serverTrip : serverTrips) {
                            Log.i(TAG, "onSuccess Updating local trip with server trip...");
                            publishProgress(Integer.toString(i), Integer.toString(totalTrips), null);
                            updateLocalTrip(serverTrip);
                            i++;
                        }
                        return null;
                    }

                    @Override
                    protected void onProgressUpdate(String... values) {
                        int curVal = Integer.parseInt(values[0]);
                        int totalVal = Integer.parseInt(values[1]);
                        Intent onProgressChangedBroadcastIntent = new Intent(TRIP_SYNC_SERVICE);
                        ArrayList<Integer> vals = new ArrayList<>();
                        vals.add(curVal);
                        vals.add(totalVal);
                        onProgressChangedBroadcastIntent.putIntegerArrayListExtra(PROGRESS, vals);
                        sendBroadcast(onProgressChangedBroadcastIntent);
                        updateNotification("Syncing MediBuddy trips", "Completed " + curVal + " / " + totalVal + " trips.", true);
                        super.onProgressUpdate(values);
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        super.onPostExecute(s);
                        Log.i(TAG, "onSuccess Local trips were synced");
                        Intent intent1 = new Intent(TRIP_SYNC_SERVICE);
                        intent1.putExtra(COMPLETED, true);
                        sendBroadcast(intent1);
                        isRunning = false;
                        stopSelf();
                    }
                }.execute(null, null, null);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Intent onFailedBroadcastIntent = new Intent(TRIP_SYNC_SERVICE);
                onFailedBroadcastIntent.putExtra(FAILED, true);
                sendBroadcast(onFailedBroadcastIntent);
                isRunning = false;
                stopSelf();
            }
        });
    }

    /**
     * Overwrites or creates a local trip with the server-side version
     *
     * @param serverTrip A FullTrip constructed using server-side json
     * @return A boolean indicating success.
     */
    private boolean updateLocalTrip(FullTrip serverTrip) {
        for (FullTrip trip : locallySavedTrips) {
            if (trip.getTripcode() == serverTrip.getTripcode()) {
                trip = serverTrip;
                trip.save();
                Log.i(TAG, "updateLocalTrip Local trip was updated.");
                return true;
            }
        }
        serverTrip.save();
        Log.i(TAG, "updateLocalTrip Local trip was created.");
        return true;
    }
}
