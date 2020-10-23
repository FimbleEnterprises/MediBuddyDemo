package com.fimbleenterprises.medimileage;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.QueryFactory.Filter;
import com.fimbleenterprises.medimileage.QueryFactory.Filter.FilterCondition;
import com.fimbleenterprises.medimileage.QueryFactory.Filter.FilterType;
import com.fimbleenterprises.medimileage.QueryFactory.Filter.Operator;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import cz.msebera.android.httpclient.Header;

public class MyAttachmentUploadService extends Service {

    private static final String TAG = "MyAttachmentUploadService";
    private static final String SERVICE_STARTED = "SERVICE_STARTED";
    private static final int NOTIFICATION_ID = 933;
    private static final String ATTACHMENT_UPDATE = "ATTACHMENT_UPLOAD";
    public static final String WAKELOCK_ACQUIRED = "WAKELOCK_ACQUIRED";
    public static final String WAKELOCK_RELEASED = "WAKELOCK_RELEASED";
    public static final String NOTIFICATION_CHANNEL = "ATTACHMENT_NOTIFICATION_CHANNEL";
    private static final String GO_TO_OPPORTUNITY = "GO_TO_OPPORTUNITY";
    public static final String ATTACHMENT_SERVICE_STOPPING = "ATTACHMENT_SERVICE_STOPPING";
    public static final String ATTACHMENT_SERVICE_STOPPED = "ATTACHMENT_SERVICE_STOPPED";
    public static final String ATTACHMENT_SERVICE_STARTED = "ATTACHMENT_SERVICE_STARTED";

    Notification notification;
    public static boolean isRunning = false;
    PowerManager.WakeLock wakeLock;
    private NotificationManager mNotificationManager;

    public MyAttachmentUploadService() {

    }

    public MyAttachmentUploadService(String tripName) {

    }

    @Override
    public void onCreate() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind ");
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        if (intent != null) {
            Log.i(TAG, "onStartCommand " + intent.getAction());
        }

        try {
            sendStartBroadcast();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void startInForeground() {

        // Send a starting up broadcast
        Intent startIntent = new Intent(SERVICE_STARTED);
        sendBroadcast(startIntent);

        getWakelock();

        Notification notification = getNotification("Trip is starting",
                "Speed 0\nDistance: 0 miles", NotificationManager.IMPORTANCE_LOW);

        startForeground(MyAttachmentUploadService.NOTIFICATION_ID, notification);

        isRunning = true;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        sendStoppingBroadcast();

        Log.e(TAG, "onDestroy");

        releaseWakelock();

        NotificationManager mgr = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mgr.cancel(NOTIFICATION_ID);
        mgr.cancelAll();

        isRunning = false;

        sendStoppedBroadcast();
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
            Intent intent = new Intent(ATTACHMENT_UPDATE);
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

        Intent intent = new Intent(ATTACHMENT_UPDATE);
        intent.putExtra(WAKELOCK_RELEASED, true);
        sendBroadcast(intent);
    }

    void updateNotification() {
        String title = "Trip is running";
        String contentText = "Uploading attachment...";

        Notification notification = getNotification(title, contentText, NotificationManager.IMPORTANCE_LOW);
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    Notification getNotification(String title, String contentText, int importance) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this.getApplicationContext(), NOTIFICATION_CHANNEL);

        Intent i = new Intent(this.getApplicationContext(), MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent ii = new Intent(this.getApplicationContext(), OpportunityActivity.class);
        ii.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ii.setAction(GO_TO_OPPORTUNITY);

        PendingIntent pendingIntent = PendingIntent.getActivity(this.getApplicationContext(), 1, i
                , PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent stopTripIntent = PendingIntent.getActivity(this.getApplicationContext(), 2
                , ii, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setOngoing(true);
        mBuilder.setSmallIcon(R.drawable.car_icon_circular);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(contentText);
        mBuilder.addAction(R.drawable.stop_icon_32x32, getString(R.string.stop_trip_btn_notification_text),
                stopTripIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setSmallIcon(R.drawable.notification_small_car);
            mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.car2_static_round_tparent_icon));
            mBuilder.setColor(Color.WHITE);
        } else {
            Log.i(TAG, "getNotification ");
        }

        mNotificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(getApplicationContext().NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = NOTIFICATION_CHANNEL;
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "MediMiles",
                    importance);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }

        Notification notif = mBuilder.build();

        notif.flags = Notification.FLAG_ONGOING_EVENT;
        notification = notif;

        return notif;
    }

    public void sendStartBroadcast() {
        Intent startIntent = new Intent(ATTACHMENT_SERVICE_STARTED);
        startIntent.putExtra(SERVICE_STARTED, true);
        sendBroadcast(startIntent);
    }

    public void sendStoppingBroadcast() {
        Intent stoppedIntent = new Intent(ATTACHMENT_SERVICE_STOPPING);
        sendBroadcast(stoppedIntent);
    }

    public void sendStoppedBroadcast() {
        Intent stoppedIntent = new Intent(ATTACHMENT_SERVICE_STOPPED);
        stoppedIntent.putExtra(ATTACHMENT_SERVICE_STOPPED, "something_here");
        sendBroadcast(stoppedIntent);
    }

}