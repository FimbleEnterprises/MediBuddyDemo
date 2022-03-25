package com.fimbleenterprises.demobuddy.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.Toast;

import com.fimbleenterprises.demobuddy.Crm;
import com.fimbleenterprises.demobuddy.DelayedWorker;
import com.fimbleenterprises.demobuddy.Helpers;
import com.fimbleenterprises.demobuddy.MyApp;
import com.fimbleenterprises.demobuddy.MyInterfaces;
import com.fimbleenterprises.demobuddy.MyPreferencesHelper;
import com.fimbleenterprises.demobuddy.MySqlDatasource;
import com.fimbleenterprises.demobuddy.QueryFactory;
import com.fimbleenterprises.demobuddy.QueryFactory.Filter;
import com.fimbleenterprises.demobuddy.QueryFactory.Filter.FilterCondition;
import com.fimbleenterprises.demobuddy.QueryFactory.Filter.FilterType;
import com.fimbleenterprises.demobuddy.QueryFactory.Filter.Operator;
import com.fimbleenterprises.demobuddy.R;
import com.fimbleenterprises.demobuddy.activities.MainActivity;
import com.fimbleenterprises.demobuddy.objects_and_containers.AccountAddresses;
import com.fimbleenterprises.demobuddy.objects_and_containers.EntityContainers;
import com.fimbleenterprises.demobuddy.objects_and_containers.FullTrip;
import com.fimbleenterprises.demobuddy.objects_and_containers.LocationContainer;
import com.fimbleenterprises.demobuddy.objects_and_containers.MediUser;
import com.fimbleenterprises.demobuddy.objects_and_containers.Requests;
import com.fimbleenterprises.demobuddy.objects_and_containers.TripEntry;
import com.fimbleenterprises.demobuddy.objects_and_containers.UserAddresses;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.jetbrains.annotations.NonNls;
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
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import cz.msebera.android.httpclient.Header;

public class MyLocationService extends Service implements LocationListener {
    private static final String TAG = "MyLocationService";
    
    public static final int REQ_CODE = 6744;
    public static final int STOP_TRIP_REQ_CODE = 4;
    public static final int MINIMUM_METERS_FOR_DB_ENTRY = 33;
    public static final int NOTIFICATION_ID = 1;
    private static final int LOCATION_INTERVAL = 3000;
    private static final float LOCATION_DISTANCE = 10f;
    public static final int MINIMUM_ACCURACY = 25;
    public static final int MINUTES_BETWEEN_REALTIME_UPDATES = 5;
    
    @NonNls
    public static final String STOP_TRIP_ACTION = "STOP_TRIP_ACTION";
    @NonNls
    public static final String LOCATION_CHANGED = "LOC_CHANGEDD";
    @NonNls
    public static final String WAKELOCK_ACQUIRED = "WAKELOCK_AQUIRED";
    @NonNls
    public static final String SERVICE_STARTED = "SERVICE_STARTED";
    @NonNls
    public static final String WAKELOCK_RELEASED = "WAKELOCK_RELEASED";
    @NonNls
    public static final String SERVICE_STOPPED = "SERVICE_STOPPED";
    @NonNls
    public static final String SERVICE_STOPPING = "SERVICE_STOPPING";
    @NonNls
    public static final String LOCATION_EVENT = "LOCATION_EVENT";
    @NonNls
    public static final String FINAL_LOCATION = "FINAL_LOCATION";
    @NonNls
    public static final String TRIP_PRENAME = "TRIP_PRENAME";
    @NonNls
    public static final String USER_STARTED_TRIP_FLAG = "USER_STARTED_TRIP_FLAG";
    @NonNls
    public static final String NOTIFICATION_CHANNEL = "MILEAGE_NOTIFICATION_CHANNEL";
    @NonNls
    public static final String NOTIFICATION_CHANNEL_REMINDER = "NOTIFICATION_CHANNEL_REMINDER";
    @NonNls
    public static final String NOT_MOVING = "NOT_MOVING";
    @NonNls
    public static final String WARN_USER = "WARN_USER";

    private static final ArrayList<TripEntry> tripEntries = new ArrayList<>();
    private final Handler notMovingHandler = new Handler();
    private final Handler tripMinderHandler = new Handler();

    public static boolean isRunning; // Cannot be private
    public static boolean userStoppedTrip; // Cannot be private 
    public static boolean userHasBeenWarned; // Cannot be private
    private static Location mLastLocation;
    private static TripEntry currentTripEntry;
    private static FullTrip fullTrip;

    private boolean isMoving;
    private MediUser user;
    private long lastRealtimeUpdateMillis;
    private boolean isUpdatingRealtimeLoc;
    private boolean userStartedTrip;
    private Runnable tripMinderRunner;
    private MyPreferencesHelper options;
    private WakeLock wakeLock;
    private LocationManager mLocationManager;
    private NotificationManager mNotificationManager;
    private String prenameTrip;
    private long lastLocationChangedTimeInMillis;
    private MySqlDatasource datasource;

    public MyLocationService() {
        Log.d(TAG, "MyLocationService Empty constructor");
        prenameTrip = null;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        options = new MyPreferencesHelper(this);
        userHasBeenWarned = false;
    }

    public static boolean tripIsValid() {
        return (tripEntries.size() > 2 &&
                fullTrip.getDistanceInMiles() > 1 &&
                isRunning);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind ");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        if (intent != null && intent.getBooleanExtra(USER_STARTED_TRIP_FLAG, false)) {
            userStartedTrip = true;
            options.lastTripAutoKilled(false);
        }

        try {
            sendStartBroadcast();
            user = MediUser.getMe(this);

            assert intent != null;
            if (intent.getStringExtra(TRIP_PRENAME) != null) {
                prenameTrip = intent.getStringExtra(TRIP_PRENAME);
            }

            fullTrip = new FullTrip(System.currentTimeMillis(), user.domainname, user.systemuserid, user.email);

            datasource = new MySqlDatasource(getApplicationContext());
            startInForeground();
            isMovingChecker(StartStop.START);
            updateAddresses(7000);



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

        Notification notification = getNotification(getString(R.string.notif_trip_starting_title),
                getString(R.string.notif_trip_starting_body));

        initializeNewTrip();

        startForeground(MyLocationService.NOTIFICATION_ID, notification);

        if (options.getReimbursementRate() == 0) {
            Log.w(TAG, "initializeNewTrip: No rate found for reimbursement.  Stopping service.");
            Toast.makeText(this, getString(R.string.toast_no_reimb_rate), Toast.LENGTH_SHORT).show();
            stopSelf();
            return;
        }

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

        fullTrip.setUserStoppedTrip(userStoppedTrip);

        sendStoppingBroadcast();

        // Commit the trip to the local database
        datasource.updateFulltrip(fullTrip);

        Log.e(TAG, "onDestroy");

        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(this);
            } catch (Exception ex) {
                Log.i(TAG, "fail to remove location listners, ignore", ex);
            }
        }

        if (Helpers.Geo.convertMetersToMiles(fullTrip.getDistance(), 0) < 1.1) {
            datasource.deleteFulltrip(fullTrip.getTripcode(), true);
            Toast.makeText(this, getString(R.string.toast_trip_too_short), Toast.LENGTH_SHORT).show();
        }

        releaseWakelock();

        NotificationManager mgr = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mgr.cancel(NOTIFICATION_ID);
        mgr.cancelAll();

        isRunning = false;

        if (fullTrip.getDistanceInMiles() >= 2) {
            try {
                exportDB();
                Log.i(TAG, "onDestroy Database was backed up.");
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.toast_failed_to_backup_db)
                        , Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        isMovingChecker(StartStop.STOP);

        try {
            notMovingHandler.removeCallbacks(tripMinderRunner);
            tripMinderHandler.removeCallbacks(tripMinderRunner);
        } catch (Exception e) {
            e.printStackTrace();
        }

        sendStoppedBroadcast();

    }

    @SuppressLint("WakelockTimeout")
    private void getWakelock() {
        Log.d(TAG, "getWakelock Acquiring wakelock...");
        PowerManager mgr = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        wakeLock = mgr.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP,
                getString(R.string.wakelock_tag));
        wakeLock.acquire(/*240*60*1000L *//*4 hours*/);
        Log.d(TAG, "getWakelock Wakelock is held = " + wakeLock.isHeld());

        if (wakeLock.isHeld()) {
            Intent intent = new Intent(LOCATION_EVENT);
            intent.putExtra(WAKELOCK_ACQUIRED, true);
            sendBroadcast(intent);
            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        this);

                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        this);

            } catch (java.lang.SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "network provider does not exist, " + ex.getMessage());
            }
        } else {
            if (!wakeLock.isHeld()) {
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

        Intent intent = new Intent(LOCATION_EVENT);
        intent.putExtra(WAKELOCK_RELEASED, true);
        sendBroadcast(intent);
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager)
                    getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    void initializeNewTrip() {

        if (datasource == null) {
            datasource = new MySqlDatasource(getApplicationContext());
        }

        currentTripEntry = null;

        fullTrip = new FullTrip();

        if (prenameTrip != null) {
            fullTrip.setTitle(prenameTrip);
        } else {
            fullTrip.setDefaultTitle();
        }

        MediUser user = MediUser.getMe(this);
        long milis = System.currentTimeMillis();

        if (user != null) {
            fullTrip.setGu_username(user.domainname);
            fullTrip.setOwnerid(user.systemuserid);
            fullTrip.setEmail(user.email);
            fullTrip.setDateTime(DateTime.now());
            fullTrip.setDistance(0);
            fullTrip.setMillis(milis);
            fullTrip.setTripcode(milis);
            fullTrip.setReimbursementRate(options.getReimbursementRate());
            fullTrip.setUserStartedTrip(userStartedTrip);

            if (datasource.createNewTrip(fullTrip)) {
                Log.i(TAG, "initializeNewTrip New trip created and added to the database");
            }

            if (options.getTripEndReminder()) {
                try {
                    tripMinderRunner = new Runnable() {
                        @Override
                        public void run() {
                            tripMinderEvaluation();
                            tripMinderHandler.postDelayed(this, 1000);
                        }
                    };
                    tripMinderRunner.run();
                } catch (java.lang.SecurityException ex) {
                    ex.printStackTrace();
                }
            }

        }
    }

    void updateNotification() {
        String title = getString(R.string.notif_trip_running_title);
        String contentText = getString(R.string.notif_trip_running_body, Helpers.Geo
                .convertMetersToMiles(fullTrip.getDistance(), 2)
                , currentTripEntry.getSpeedInMph(true));
        Notification notification = getNotification(title, contentText);
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    Notification getNotification(String title, String contentText) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this.getApplicationContext(), NOTIFICATION_CHANNEL);

        Intent i = new Intent(this.getApplicationContext(), MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent ii = new Intent(this.getApplicationContext(), MainActivity.class);
        ii.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ii.setAction(STOP_TRIP_ACTION);

        PendingIntent pendingIntent = PendingIntent.getActivity(this.getApplicationContext()
                , REQ_CODE, i, PendingIntent.FLAG_IMMUTABLE);

        PendingIntent stopTripIntent = PendingIntent.getActivity(this.getApplicationContext()
                , STOP_TRIP_REQ_CODE, ii, PendingIntent.FLAG_IMMUTABLE);

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setOngoing(true);
        mBuilder.setSmallIcon(R.drawable.car_icon_circular);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(contentText);
        mBuilder.addAction(R.drawable.stop_icon_32x32, getString(R.string.stop_trip_btn_notification_text),
                stopTripIntent);

        mBuilder.setSmallIcon(R.drawable.notification_small_car);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap
                .car2_static_round_tparent_icon));
        mBuilder.setColor(Color.WHITE);

        mNotificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(NOTIFICATION_SERVICE);

        assert options != null;
        String channelId = NOTIFICATION_CHANNEL;
        NotificationChannel channel = new NotificationChannel(
                channelId,
                getString(R.string.app_name),
                options.useHighNotificationImportance() ? NotificationManager.IMPORTANCE_HIGH
                        : NotificationManager.IMPORTANCE_LOW);
        mNotificationManager.createNotificationChannel(channel);
        mBuilder.setChannelId(channelId);
        

        Notification notif = mBuilder.build();
        notif.flags = Notification.FLAG_ONGOING_EVENT;
        return notif;
    }

    Notification getTripStillRunningNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this.getApplicationContext(), NOTIFICATION_CHANNEL_REMINDER);

        Intent i = new Intent(this.getApplicationContext(), MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(WARN_USER, true);
        i.setAction(WARN_USER);

        Intent ii = new Intent(this.getApplicationContext(), MainActivity.class);
        ii.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ii.setAction(STOP_TRIP_ACTION);

        PendingIntent pendingIntent = PendingIntent.getActivity(this.getApplicationContext()
                , 200, i, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        PendingIntent stopTripIntent = PendingIntent.getActivity(this.getApplicationContext()
                , 200, ii, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.drawable.main_logo);

        mBuilder.setSmallIcon(R.drawable.notification_small_car);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap
                .car2_static_round_tparent_icon));
        mBuilder.setColor(Color.WHITE);

        mBuilder.setContentTitle(getString(R.string.trip_still_running));
        mBuilder.setContentText(getString(R.string.forget_to_stop_trip));
        mBuilder.addAction(R.drawable.stop_icon_32x32, getString(R.string.stop_trip_btn_notification_text),
                stopTripIntent);

        mNotificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = NOTIFICATION_CHANNEL_REMINDER;
        NotificationChannel channel = new NotificationChannel(
                channelId,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH);
        mNotificationManager.createNotificationChannel(channel);
        mBuilder.setChannelId(channelId);

        return mBuilder.build();

    }

    void sendUpdateBroadcast(Location location) {
        try {
            Intent intent = new Intent(LOCATION_EVENT);
            LocationContainer container = new LocationContainer(location, fullTrip, currentTripEntry);
            intent.putExtra(LOCATION_CHANGED, container);
            sendBroadcast(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateAddresses(int delayInMillis) {

        // Get account addresses from CRM or local database
        if (AccountAddresses.getSavedActAddys() == null || options.getShouldUpdateActAddys()) {
            new DelayedWorker(delayInMillis, new DelayedWorker.DelayedJob() {
                @Override
                public void doWork() {
                    if (!MyLocationService.isRunning) {
                        Log.w(TAG, "UpdateAccountsDoWork: Trip is not running.  Will not update accounts.");
                        return;
                    }
                    AccountAddresses.getFromCrm(getApplicationContext(), new MyInterfaces.GetAccountsListener() {
                        @Override
                        public void onSuccess(AccountAddresses accounts) {
                            Log.i(TAG, "onSuccess Got accounts!");
                            accounts.save();
                        }

                        @Override
                        public void onFailure(String msg) {
                            Log.w(TAG, "onFailure: " + msg);
                        }
                    });
                }

                @Override
                public void onComplete(Object object) {

                }
            });
        }

        // Get user addresses from CRM or local database
        if (UserAddresses.getSavedUserAddys() == null || options.getShouldUpdateUserAddys()) {
            new DelayedWorker(delayInMillis, new DelayedWorker.DelayedJob() {
                @Override
                public void doWork() {
                    if (UserAddresses.getSavedUserAddys() == null) {
                        UserAddresses.getAllUserAddysFromCrm(getApplicationContext(), new MyInterfaces.GetUserAddysListener() {
                            @Override
                            public void onSuccess(UserAddresses addresses) {
                                Log.i(TAG, "onSuccess Received: " + addresses.addresses.size() + " addresses");
                                addresses.save();
                            }

                            @Override
                            public void onFailure(String msg) {

                            }
                        });
                    }
                }

                @Override
                public void onComplete(Object object) {

                }
            });
        }

    }

    public void sendStartBroadcast() {
        Intent startIntent = new Intent(LOCATION_EVENT);
        startIntent.putExtra(SERVICE_STARTED, true);
        sendBroadcast(startIntent);
    }

    public void sendNotMovingBroadcast() {
        Intent intent = new Intent(NOT_MOVING);
        intent.setAction(NOT_MOVING);
        sendBroadcast(intent);
    }

    public void sendStoppingBroadcast() {
        Intent stoppedIntent = new Intent(SERVICE_STOPPING);
        sendBroadcast(stoppedIntent);
    }

    public void sendStoppedBroadcast() {
        Intent stoppedIntent = new Intent(SERVICE_STOPPED);
        stoppedIntent.putExtra(FINAL_LOCATION, fullTrip);
        sendBroadcast(stoppedIntent);
    }

    /**
     * Returns the private final fullTrip that this service is using to track the trip currently in
     * use.  If the private static isRunning bool == false this will return null.
     * @return A FullTrip object representing a trip that is currently being recorded.
     */
    public static FullTrip getCurrentRunningTrip() {
        if (isRunning) {
            return fullTrip;
        } else {
            return null;
        }
    }

    /**
     * Induces an update broadcast and can be called statically from outside the service.
     * @param context A valid context to support sending a broadcast.
     */
    public static void sendUpdateBroadcast(Context context) {
        if (currentTripEntry == null) {
            Log.w(TAG, "sendUpdateBroadcast: No current trip entry to broadcast so we are not going to broadcast.");
            return;
        }
        Intent intent = new Intent(LOCATION_EVENT);
        LocationContainer container = new LocationContainer(mLastLocation, fullTrip, currentTripEntry);
        intent.putExtra(LOCATION_CHANGED, container);
        context.sendBroadcast(intent);
        Log.i(TAG, "sendUpdateBroadcast This is a forced broadcast request.");
    }

    /**
     * This maintains an overwrittable database backup.
     */
    public void exportDB() {
        try {
            File storageDirectory = Environment.getExternalStorageDirectory();
            SQLiteDatabase database = new MySqlDatasource().getDatabase();

            if (storageDirectory.canWrite()) {
                Log.d("TAG", "DatabaseHandler: can write in sd");
                String currentDBPath = database.getPath();
                String copiedDbFilename = "autobackup.db";

                File currentDB = new File(currentDBPath);
                File copiedDb = new File(storageDirectory, copiedDbFilename);

                if (copiedDb.exists()) {
                    if (!copiedDb.delete()) {
                        Log.w(TAG, "exportDB|Failed to delete copiedDb; prob not a big deal.");
                    }
                }
                copiedDb = new File(storageDirectory, copiedDbFilename);

                if (currentDB.exists()) {
                    Log.d("TAG", "DatabaseHandler: DB exist");

                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(copiedDb).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    if (copiedDb.exists() && copiedDb.length() > 0) {
                        Toast.makeText(this, getString(R.string.toast_db_was_backed_up)
                                , Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, getString(R.string.toast_failed_to_backup_db)
                                , Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void setReimbursementRate(final MyInterfaces.ReimbursementRateCallback callback)
            throws UnsupportedEncodingException {
        QueryFactory factory = new QueryFactory("businessunit");
        factory.addColumn("msus_mileage_reimbursement_rate");
        FilterCondition condition = new FilterCondition("businessunitid", Operator.EQUALS
                , "8B31B2C2-E519-E711-80D2-005056A36B9B");
        factory.setFilter(new Filter(FilterType.AND, condition));
        String query = factory.construct();
        Requests.Request request = new Requests.Request(Requests.Request.Function.GET);
        request.arguments.add(new Requests.Argument(null, query));
        Crm crm = new Crm();
        crm.makeCrmRequest(MyApp.getAppContext(), request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONArray array = new JSONObject(new String(responseBody)).getJSONArray("value");
                    JSONObject object = array.getJSONObject(0);
                    double rate = object.getDouble("msus_mileage_reimbursement_rate");
                    MyPreferencesHelper options = new MyPreferencesHelper(MyApp.getAppContext());
                    options.setReimbursementRate((float) rate);
                    Log.i(TAG, "onSuccess Reimbursement rate set at: " + rate);
                    callback.onSuccess((float) rate);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    public static int getTripDuration() {
        long startMillis = fullTrip.getTripcode();
        long nowMillis = System.currentTimeMillis();
        long minutes = ((nowMillis - startMillis) / 1000) / 60;
        DecimalFormat decimalFormat = new DecimalFormat("#");
        return Integer.parseInt(decimalFormat.format(minutes));
    }

    enum StartStop {
        START, STOP
    }

    private void isMovingChecker(StartStop val) {

        final long LAST_MOVED_THRESHOLD = 3000;

        Runnable notMovingRunner = new Runnable() {
            public void run() {

                if (mLastLocation == null) {
                    Log.i(TAG, "isMovingChecker.run: No location detected yet.");
                    notMovingHandler.postDelayed(this, 250);
                    return;
                }

                long interval = System.currentTimeMillis() - mLastLocation.getTime();
                if (isRunning && interval > LAST_MOVED_THRESHOLD && isMoving) {
                    isMoving = false;
                    sendNotMovingBroadcast();
                    Log.i(TAG, "isMovingChecker.run: Detected stopped user.");
                }
                notMovingHandler.postDelayed(this, 250);
            }
        };

        if (val == StartStop.START) {
            notMovingHandler.postDelayed(notMovingRunner, 250);
            Log.i(TAG, "isMovingChecker Starting!");
        } else {
            Log.i(TAG, "isMovingChecker Stopping!");
            notMovingHandler.removeCallbacks(notMovingRunner);
        }
    }

    /** Poor environment is an environment with poor GPS accuracy and little to no speed
     This environment suggests the user forgot to end their current trip and they're
     currently indoors by evaluating the time of the last db entry to the current time. */
    private void tripMinderEvaluation() {

        int count = tripEntries.size();
        if (count < 3) {
            this.lastLocationChangedTimeInMillis = System.currentTimeMillis();
            Log.v(TAG + "", "Haven't travelled far enough to check yet...");
            return;
        }

        long curMillies = System.currentTimeMillis();

        if ((curMillies - this.lastLocationChangedTimeInMillis) > options.getTripMinderIntervalMillis()) {
            Log.i(TAG, "tripMinderEvaluation time between valid updates has been exceeded.");
            if (userHasBeenWarned) {
                Log.w(TAG, "tripMinderEvaluation: User has been warned.  Stopping the trip!");
                try {
                    this.stopSelf();
                    options.lastTripAutoKilled(true);

                    // This will update the trip as auto-killed prior to submission
                    fullTrip.setTripMinderKilledTrip(true);
                    fullTrip.userStoppedTrip = 0;
                    fullTrip.save();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.w(TAG, "tripMinderEvaluation: Warning the user!");
                this.lastLocationChangedTimeInMillis = System.currentTimeMillis();
                if (!userHasBeenWarned) {
                    Notification notification = getTripStillRunningNotification();
                    mNotificationManager.notify(NOTIFICATION_ID, notification);
                }
                userHasBeenWarned = true;
            }
        }

    }// END tripMinderEvaluation

    public boolean shouldMakeEntry(Location location) {
        return mLastLocation.distanceTo(location) >= MINIMUM_METERS_FOR_DB_ENTRY ||
                tripEntries.size() == 0;
    }

    public void updateRealtimeLocationInCrm(LatLng latLng) {
        try {

            if (isUpdatingRealtimeLoc) {
                Log.i(TAG, "updateRealtimeLocationInCrm Is being updated already.");
                return;
            }

            final long nowMilis = DateTime.now(DateTimeZone.UTC).getMillis();
            long gap = nowMilis - lastRealtimeUpdateMillis;

            if (Helpers.DatesAndTimes.convertMilisToMinutes(gap) < MINUTES_BETWEEN_REALTIME_UPDATES) {
                return;
            }

            EntityContainers.EntityContainer container = new EntityContainers.EntityContainer();
            container.entityFields.add(new EntityContainers.EntityField("msus_last_loc_timestamp", LocalDateTime.now().toString()));
            container.entityFields.add(new EntityContainers.EntityField("msus_last_lon", Double.toString(latLng.longitude)));
            container.entityFields.add(new EntityContainers.EntityField("msus_last_lat", Double.toString(latLng.latitude)));

            Requests.Request request = new Requests.Request(Requests.Request.Function.UPDATE);
            request.function = Requests.Request.Function.UPDATE.name();
            request.arguments.add(new Requests.Argument("systemuserid", MediUser.getMe().systemuserid));
            request.arguments.add(new Requests.Argument("entity", "systemuser"));
            request.arguments.add(new Requests.Argument("container", container.toJson()));
            request.arguments.add(new Requests.Argument("as_userid", MediUser.getMe().systemuserid));

            Crm crm = new Crm();
            crm.makeCrmRequest(this, request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Log.i(TAG, "onSuccess Successfully updated user's location");
                    lastRealtimeUpdateMillis = nowMilis;
                    isUpdatingRealtimeLoc = false;
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.i(TAG, "onFailure Failed to update user's location");
                    isUpdatingRealtimeLoc = false;
                }
            });

            isUpdatingRealtimeLoc = true;

        } catch (Exception e) {
            e.printStackTrace();
            isUpdatingRealtimeLoc = false;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG, "onLocationChanged: " + location);

        float accuracy = Helpers.Geo.getCurrentAccAsPct(location.getAccuracy());

        if (accuracy < MINIMUM_ACCURACY) {
            Log.w(TAG, "onLocationChanged: Accuracy (" + accuracy + ") is too low to use.");
        }

        if (tripEntries.size() < 1) {
            mLastLocation = location;
            Log.i(TAG, "onLocationChanged Set default last location");
        }

        Log.i(TAG, "onLocationChanged Provider: " + location.getProvider());

        if (!location.getProvider().equals("gps")) {
            Log.w(TAG, "onLocationChanged: Location not provided by GPS and will be ignored.");
            Log.w(TAG, "onLocationChanged: Provider: " + location.getProvider());
            Log.w(TAG, "onLocationChanged: Distance: " + location.distanceTo(mLastLocation));
            return;
        }

        updateRealtimeLocationInCrm(new LatLng(location.getLatitude(), location.getLongitude()));

        // Set the trip's total distance
        float dist = location.distanceTo(mLastLocation);

        if (dist > 3219) {
            Log.w(TAG, "onLocationChanged WAYYYY TOO FAR TO BE LEGIT!");
            mLastLocation = location;
            return;
        }

        float totalDistance = fullTrip.getDistance() + dist;
        fullTrip.setDistance(totalDistance);

        currentTripEntry = new TripEntry();
        currentTripEntry.setTripcode(fullTrip.getTripcode());
        currentTripEntry.setDateTime(DateTime.now());
        currentTripEntry.setDistance(totalDistance);
        currentTripEntry.setSpeed(location.getSpeed());
        currentTripEntry.setLongitude(location.getLongitude());
        currentTripEntry.setLatitude(location.getLatitude());
        currentTripEntry.setGuid(user.systemuserid);
        currentTripEntry.setMilis(System.currentTimeMillis());

        if (shouldMakeEntry(location)) {
            Log.i(TAG, "onLocationChanged | Eligible for commit!");

            // Update the database
            if (datasource.appendTrip(currentTripEntry)) {
                datasource.updateFulltrip(fullTrip);
                tripEntries.add(currentTripEntry);
                Log.i(TAG, "onLocationChanged: Successfully inserted new trip entry");
                userHasBeenWarned = false;
                TripEntry lastEntry = tripEntries.get(tripEntries.size() - 1);
                this.lastLocationChangedTimeInMillis = lastEntry.getMilis();
            }
        } else {
            Log.i(TAG, "onLocationChanged Not eligible for db insert.");
        }

        sendUpdateBroadcast(location);
        try {
            updateNotification();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mLastLocation.set(location);
        isMoving = true;
        Log.i(TAG, "onLocationChanged Updated lastLocation");
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Log.e(TAG, "onProviderDisabled: " + provider);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.e(TAG, "onStatusChanged: " + provider);
    }

    /**
     * Returns the last known location from the OS if available.
     * @param context A context valid for confirming permissions and accessing the location manager.
     * @return Null if not available.
     */
    public static Location getLastKnownCachedLocationFromOS(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        Location location = Objects.requireNonNull(locationManager).getLastKnownLocation(Objects
                .requireNonNull(locationManager.getBestProvider(criteria, false)));
        if (location != null) {
            Log.d(TAG, "zoomMyCurrentLocation: location not null");
            return location;
        }
        return null;
    }

    /**
     * Attempts to get the last known location from the OS and if not available attempts to find the
     * current location now using FusedLocationProvider.
     * @param context A context valid for confirming permissions and accessing the location manager.
     * @param onSuccessListener A listener for reporting the results when operation has finished.
     */
    @SuppressWarnings("unused")
    @SuppressLint("MissingPermission")
    public static void getLastKnownCachedLocationFromOS(Context context, OnSuccessListener<Location> onSuccessListener) {

        // See if the OS has a last known loc already and use that if so.
        if (getLastKnownCachedLocationFromOS(context) != null) {
            onSuccessListener.onSuccess(Objects.requireNonNull(
                    getLastKnownCachedLocationFromOS(context)));
            return;
        }

        // Leave if no permission.
        if (!Helpers.Permissions.isGranted(Helpers.Permissions.PermissionType.ACCESS_COARSE_LOCATION)
                || !Helpers.Permissions.isGranted(Helpers.Permissions.PermissionType.ACCESS_FINE_LOCATION)) {
            return;
        }

        // Determine the current location manually.
        Log.d(TAG, "setMyLastLocation: execute, and get last location");
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            double lat = location.getLatitude();
            double longi = location.getLongitude();
            LatLng latLng = new LatLng(lat, longi);
            Log.d(TAG, "MyLastLocation coordinate :" + latLng);
        });
    }
}