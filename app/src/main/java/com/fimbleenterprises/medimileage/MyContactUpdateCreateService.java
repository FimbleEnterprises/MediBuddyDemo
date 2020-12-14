package com.fimbleenterprises.medimileage;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class MyContactUpdateCreateService extends Service {

    public static final String IS_TEXT_ONLY = "IS_TEXT_ONLY";
    Context context;
    private static final String TAG = "MyContactUpdateCreateService";
    private static final int NOTIFICATION_ID = 944;
    private static final String ATTACHMENT_UPDATE = "ATTACHMENT_UPLOAD";
    public static final String WAKELOCK_ACQUIRED = "WAKELOCK_ACQUIRED";
    public static final String CONTACT_NOTIFICATION_CHANNEL = "CONTACT_NOTIFICATION_CHANNEL";
    public static final String CREATE_NEW_CONTACT = "CREATE_NEW_CONTACT";
    public static final String UPDATE_EXISTING_CONTACT = "UPDATE_EXISTING_CONTACT";
    public static final String VCARD_STRING = "VCARD_STRING";
    public static final String ACCOUNTID = "ACCOUNTID";
    public static int START_ID = 21;
    CrmEntities.Accounts.Account selectedAccount;

    Notification notification;
    public static boolean isRunning = false;
    PowerManager.WakeLock wakeLock;
    private NotificationManager mNotificationManager;
    CrmEntities.Annotations.Annotation annotation;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate ");
        context = this;
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
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
        START_ID = startId;

        if (intent != null) {

            getWakelock();

            // Check if this is just plain text being shared.  If so we don't want to try to find a
            // file and read it (because it obviously won't exist and that would be stupid.
            if (intent.getAction().equals(UPDATE_EXISTING_CONTACT)) {

                notification = getNotification("Updating Contact",
                        "Your contact is being updated in the background.", false, true);

            } else if (intent.getAction().equals(CREATE_NEW_CONTACT)) {
                // This is a file being shared so we need to encode it in order to attach it in CRM
                notification = getNotification("Creating contact...",
                        "Your contact is being uploaded to CRM in the background.", false, true);

                if (intent.getParcelableExtra(VCARD_STRING) != null) {
                    MyVcardParser myVcardParser = new MyVcardParser(intent.getStringExtra(VCARD_STRING));
                    myVcardParser.uploadToCrm(intent.getStringExtra(ACCOUNTID), new MyInterfaces.EntityUpdateListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(context, "Successfully created contact", Toast.LENGTH_SHORT).show();
                            releaseWakelock();
                            stopSelf();
                        }

                        @Override
                        public void onFailure(String msg) {
                            releaseWakelock();
                            Toast.makeText(context, "Failed to create contact!", Toast.LENGTH_SHORT).show();
                            stopSelf();
                        }
                    });
                }

                Toast.makeText(context, "Creating contact in CRM...", Toast.LENGTH_SHORT).show();

            }
        }

        return START_NOT_STICKY;
    }

    private Notification getNotification(String title, String text, boolean onClickGoesToOpportunity, boolean showProgress){

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(
                    new NotificationChannel(CONTACT_NOTIFICATION_CHANNEL,NOTIFICATION_SERVICE,NotificationManager.IMPORTANCE_HIGH));
        }

        Intent onClickIntent = new Intent();

        if (onClickGoesToOpportunity) {

        }

        // The PendingIntent to launch our activity if the user selects
        // this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.BigTextStyle style;
        if (onClickGoesToOpportunity) {
            style = new NotificationCompat.BigTextStyle()
                    .bigText(text);
        } else {
            style = new NotificationCompat.BigTextStyle()
                    .bigText("Your file is being uploaded and will be attached when finished.  Stand by!");
        }

        return new NotificationCompat.Builder(this, CONTACT_NOTIFICATION_CHANNEL)
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
    private void updateNotification(String title, String text, boolean onClickGoesToOpportunity, boolean showProgress){

        Notification notification= getNotification(title, text, onClickGoesToOpportunity, showProgress);

        NotificationManager mNotificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(START_ID,notification);
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

        Log.e(TAG, "onDestroy");

        releaseWakelock();

        /*NotificationManager mgr = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mgr.cancel(NOTIFICATION_ID);
        mgr.cancelAll();*/

        isRunning = false;

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
            Intent intent = new Intent(CREATE_NEW_CONTACT);
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

}