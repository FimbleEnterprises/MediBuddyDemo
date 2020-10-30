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

import java.io.File;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class MyAttachmentUploadService extends Service {

    public static final String IS_TEXT_ONLY = "IS_TEXT_ONLY";
    Context context;
    private static final String TAG = "MyAttachmentUploadService";
    private static final String SERVICE_STARTED = "ATTACHMENT_SERVICE_STARTED";
    private static final int NOTIFICATION_ID = 933;
    public static final String RELATED_OPPORTUNITY = "RELATED_OPPORTUNITY";
    public static final String RELATED_FILEPATH = "RELATED_FILEPATH";
    private static final String ATTACHMENT_UPDATE = "ATTACHMENT_UPLOAD";
    public static final String WAKELOCK_ACQUIRED = "WAKELOCK_ACQUIRED";
    public static final String WAKELOCK_RELEASED = "WAKELOCK_RELEASED";
    public static final String NOTIFICATION_CHANNEL = "ATTACHMENT_NOTIFICATION_CHANNEL";
    private static final String GO_TO_OPPORTUNITY = "GO_TO_OPPORTUNITY";
    public static final String ERROR_MESSAGE = "ERROR_MESSAGE";
    public static final String ATTACHMENT_SERVICE_STOPPING = "ATTACHMENT_SERVICE_STOPPING";
    public static final String ATTACHMENT_SERVICE_STOPPED = "ATTACHMENT_SERVICE_STOPPED";
    public static final String ATTACHMENT_SERVICE_STARTED = "ATTACHMENT_SERVICE_STARTED";
    public static final String NEW_ATTACHMENT = "NEW_ATTACHMENT";
    public static final String ATTACHMENT_FILE_PATH = "ATTACHMENT_FILE_PATH";
    public static final String UPLOAD_NEW_ATTACHMENT = "UPLOAD_NEW_ATTACHMENT";
    public static final String UPLOAD_COMPLETE = "UPLOAD_COMPLETE";
    public static final String UPLOAD_BROADCAST = "UPLOAD_BROADCAST";
    public static String uploadedFilePath;
    public static int START_ID = 21;
    CrmEntities.Opportunities.Opportunity selectedOpportunity;

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

            this.annotation = intent.getParcelableExtra(NEW_ATTACHMENT);
            this.selectedOpportunity = intent.getParcelableExtra(OpportunityActivity.OPPORTUNITY_TAG);

            // Check if this is just plain text being shared.  If so we don't want to try to find a
            // file and read it (because it obviously won't exist and that would be stupid.
            if (intent.getBooleanExtra(IS_TEXT_ONLY, false)) {

                notification = getNotification("Sharing text with MileBuddy",
                        "Your note is being created in the background; you will be notified when it has completed.", false, true);

                encodeFile(null); // passing null will indicate that this is plain text

            } else if (intent.getAction().equals(UPLOAD_NEW_ATTACHMENT)) {
                // This is a file being shared so we need to encode it in order to attach it in CRM
                notification = getNotification("Uploading " + annotation.filename,
                        "Your file is being uploaded in the background and will be attached when finished.", false, true);

                final File file = new File(intent.getStringExtra(ATTACHMENT_FILE_PATH));

                Toast.makeText(context, "Uploading attachment...", Toast.LENGTH_SHORT).show();
                encodeFile(file);

            }
        }

        return START_NOT_STICKY;
    }

    private Notification getNotification(String title, String text, boolean onClickGoesToOpportunity, boolean showProgress){

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(
                    new NotificationChannel(NOTIFICATION_CHANNEL,NOTIFICATION_SERVICE,NotificationManager.IMPORTANCE_HIGH));
        }

        Intent onClickIntent = new Intent();

        if (onClickGoesToOpportunity) {
            onClickIntent = new Intent(context, OpportunityActivity.class);
            onClickIntent.putExtra(OpportunityActivity.OPPORTUNITY_TAG, selectedOpportunity);
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

        return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    void encodeFile(final File file) {
        try {

            startForeground(START_ID, notification);
            getWakelock();
            isRunning = true;

            Log.i(TAG, "onStartCommand Service starting and an attachment has been passed!");
            Log.i(TAG, "onStartCommand Annotation will be attached to object with id: " + annotation.objectid);

            if (file == null) {
                annotation.submit(context, new MyInterfaces.CrmRequestListener() {
                    @Override
                    public void onComplete(Object result) {
                        stopSelf(START_ID);
                        updateNotification("Note was created!", "Your note was successfully created.", true, false);
                        isRunning = false;
                    }

                    @Override
                    public void onProgress(Crm.AsyncProgress progress) { }

                    @Override
                    public void onFail(String error) {
                        notification = getNotification("Upload failed", error, false, false);
                        mNotificationManager.notify(NOTIFICATION_ID, notification);
                        stopSelf(START_ID);
                        mNotificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL);
                        isRunning = false;
                    }
                });
            } else {
                uploadedFilePath = file.getPath();
                Helpers.Files.base64Encode(file.getPath(), new MyInterfaces.EncoderListener() {
                    @Override
                    public void onSuccess(final String base64File) {
                        annotation.submit(context, new MyInterfaces.CrmRequestListener() {
                            @Override
                            public void onComplete(Object result) {
                                CrmEntities.CrmEntityResponse response = new CrmEntities.CrmEntityResponse(result.toString());
                                annotation.annotationid = response.guid;
                                annotation.documentBody = base64File;
                                annotation.addAttachment(context, new MyInterfaces.CrmRequestListener() {
                                    @Override
                                    public void onComplete(Object result) {
                                        // stopSelf(START_ID);
                                        Toast.makeText(context, "File was uploaded to MileBuddy", Toast.LENGTH_SHORT).show();
                                        stopSelf(START_ID);
                                        updateNotification("Upload complete!", "Your file was successfully uploaded and attached.", true, false);
                                        isRunning = false;
                                    }

                                    @Override
                                    public void onProgress(Crm.AsyncProgress progress) {  }

                                    @Override
                                    public void onFail(String error) {
                                        notification = getNotification("Upload failed", error, false, false);
                                        mNotificationManager.notify(NOTIFICATION_ID, notification);
                                        // remove annotation here
                                        stopSelf(START_ID);
                                        isRunning = false;
                                    }
                                });
                            }

                            @Override
                            public void onProgress(Crm.AsyncProgress progress) {  }

                            @Override
                            public void onFail(String error) {
                                stopSelf();
                            }
                        });
                        annotation.documentBody = base64File;

                    }

                    @Override
                    public void onFailure(String error) {
                        stopSelf(START_ID);
                        mNotificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL);
                        Log.w(TAG, "onFailure: Failure: " + error);
                        isRunning = false;
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            stopSelf(START_ID);
            Log.w(TAG, "startInForeground: Failure: " + e.getLocalizedMessage());
            isRunning = false;
        }
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
    }/*

    public void sendStartBroadcast() {
        Intent intent = new Intent(UPLOAD_BROADCAST);
        intent.putExtra(RELATED_OPPORTUNITY, selectedOpportunity);
        intent.putExtra(SERVICE_STARTED, true);
        sendBroadcast(intent);
    }

    void sendUploadFailedBroadcast(String error) {
        Intent intent = new Intent(UPLOAD_BROADCAST);
        intent.putExtra(RELATED_OPPORTUNITY, selectedOpportunity);
        intent.putExtra(ERROR_MESSAGE, error);
        sendBroadcast(intent);
    }

    void sendUploadCompleteBroadcast() {
        Intent intent = new Intent(UPLOAD_BROADCAST);
        intent.putExtra(RELATED_OPPORTUNITY, selectedOpportunity);
        intent.putExtra(RELATED_FILEPATH, uploadedFilePath);
        sendBroadcast(intent);
    }

    public void sendStoppingBroadcast() {
        Intent intent = new Intent(UPLOAD_BROADCAST);
        intent.putExtra(RELATED_OPPORTUNITY, selectedOpportunity);
        intent.putExtra(ATTACHMENT_SERVICE_STOPPING,"");
        sendBroadcast(intent);
    }

    public void sendStoppedBroadcast() {
        Intent intent = new Intent(UPLOAD_BROADCAST);
        intent.putExtra(RELATED_OPPORTUNITY, selectedOpportunity);
        intent.putExtra(ATTACHMENT_SERVICE_STOPPED, "");
        sendBroadcast(intent);
    }*/

}