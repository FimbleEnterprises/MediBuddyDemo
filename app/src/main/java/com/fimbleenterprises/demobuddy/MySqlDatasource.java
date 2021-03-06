package com.fimbleenterprises.demobuddy;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.fimbleenterprises.demobuddy.objects_and_containers.AccountAddresses;
import com.fimbleenterprises.demobuddy.objects_and_containers.FullTrip;
import com.fimbleenterprises.demobuddy.objects_and_containers.MediUser;
import com.fimbleenterprises.demobuddy.objects_and_containers.RecentOrSavedTrip;
import com.fimbleenterprises.demobuddy.objects_and_containers.TripEntry;
import com.fimbleenterprises.demobuddy.objects_and_containers.UserAddresses;
import com.fimbleenterprises.demobuddy.services.MyLocationService;

import org.jetbrains.annotations.NonNls;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Locale;

import static com.fimbleenterprises.demobuddy.MySQLiteHelper.ALL_TRIPENTRY_COLUMNS;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_ACTS_GSON;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_ADDY_GSON;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_DATETIME;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_DIST;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_DISTANCE;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_EDITED;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_EMAIL;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_FROM_LAT;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_FROM_LON;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_GUID;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_GU_USERNAME;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_HAS_ASSOCIATIONS;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_ID;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_IS_MANUAL;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_IS_ME;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_IS_SUBMITTED;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_JSON;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_LATITUDE;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_LONGITUDE;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_MILIS;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_NAME;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_OBJECTIVE;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_RATE;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_SPEED;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_SYSTEMUSERID;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_TITLE;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_TO_LAT;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_TO_LON;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_TRIPCODE;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_TRIP_GUID;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_TRIP_MINDER_KILLED;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_USER_STARTED_TRIP;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.COLUMN_USER_STOPPED_TRIP;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.TABLE_ADDYS;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.TABLE_FULL_TRIP;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.TABLE_MY_ACCOUNTS;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.TABLE_RECENT_OR_SAVED_TRIPS;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.TABLE_TRIP_ENTRIES;
import static com.fimbleenterprises.demobuddy.MySQLiteHelper.TABLE_USERS;

@SuppressLint("LongLogTag")
@SuppressWarnings("unused")
public class MySqlDatasource {
    // Database fields
    @NonNls
    public static SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    Context context;

    public static final String TAG = "MileageDatasource";

    public MySqlDatasource() {
        this.context = MyApp.getAppContext();
    }

    public MySqlDatasource(Context context) {

        if (context == null) {
            context = MyApp.getAppContext();
        }

        dbHelper = new MySQLiteHelper(context);
        this.context = context;

        if (database == null) {
            database = dbHelper.getWritableDatabase();
        }

        if (!database.isOpen()) {
            database = dbHelper.getWritableDatabase();
            database.setLocale(Locale.US);
        }
    }

    public void open() throws SQLException {
        if (database == null || !database.isOpen()) {
            database = dbHelper.getWritableDatabase();
            database.setLocale(Locale.US);
        }
    }

    public void close() {
        dbHelper.close();
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }

    boolean columnExists(int colIndex, Cursor cursor) {
        try {
            String name = cursor.getColumnName(colIndex);
            return (name != null && name.length() > 0);
        } catch (Exception e) {
            Log.w(TAG, "columnExists: column index: " + colIndex + " does not exist");
            return false;
        }
    }

    boolean columnExists(String colName, Cursor cursor) {
        try {
            String[] columnNames = cursor.getColumnNames();
            for (String name : columnNames) {
                if (colName.equals(name)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Log.w(TAG, "columnExists: column index: " + colName + " does not exist");
            return false;
        }
    }

    int getColumnIndex(String colName, Cursor cursor) {
        return cursor.getColumnIndex(colName);
    }

    public boolean saveUser(MediUser user) {
        try {
            if (getUser(user.systemuserid) == null) {
                return createUser(user);
            } else {
                return updateUser(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUser(MediUser user) {

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_JSON, user.toGson());
            values.put(COLUMN_SYSTEMUSERID, user.systemuserid);
            values.put(COLUMN_IS_ME, user.isMe);

            return (database.update(TABLE_USERS, values, COLUMN_IS_ME + "= 1", null) > 0);

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createUser(MediUser user) {
        try {
            ContentValues values = new ContentValues();

            values.put(COLUMN_JSON, user.toGson());
            values.put(COLUMN_SYSTEMUSERID, user.systemuserid);
            values.put(COLUMN_IS_ME, (user.isMe ? 1:0));

            // Returns the id of the childView just added or -1 if something went wrong
            long insertId = database.insert(TABLE_USERS, null, values);
            Log.i(TAG, "createUser " + (insertId > -1));
            return insertId > -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int updateAccounts(AccountAddresses accounts) {
        int result = 0;
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ACTS_GSON, accounts.toGson());

            result = database.update(TABLE_MY_ACCOUNTS, values, null, null);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "updateAccounts|" + result);
        return result;
    }

    public boolean createAccounts(AccountAddresses accounts) {
        try {

            ContentValues values = new ContentValues();
            values.put(COLUMN_ACTS_GSON, accounts.toGson());

            // Returns the id of the childView just added or -1 if something went wrong
            long insertId = database.insert(TABLE_MY_ACCOUNTS, null, values);
            Log.i(TAG, "createAccounts " + (insertId > -1));
            return insertId > -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUserAddys(UserAddresses addresses) {
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ADDY_GSON, addresses.toGson());
            return database.update(TABLE_ADDYS, values, null, null) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "updateAccounts " + false);
        return false;
    }

    public boolean createUserAddys(UserAddresses addresses) {
        try {

            ContentValues values = new ContentValues();
            values.put(COLUMN_ADDY_GSON, addresses.toGson());

            // Returns the id of the childView just added or -1 if something went wrong
            long insertId = database.insert(TABLE_ADDYS, null, values);
            Log.i(TAG, "createAccounts " + (insertId > -1));
            return insertId > -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public UserAddresses getUserAddys() {

        Cursor cursor = database.rawQuery("SELECT " + COLUMN_ADDY_GSON + " FROM " + TABLE_ADDYS, null);
        UserAddresses addresses;
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            String gson = cursor.getString(getColumnIndex(COLUMN_ADDY_GSON, cursor));
            addresses = UserAddresses.fromGson(gson);
            if (addresses != null) {
                Log.i(TAG, "getAct Found " + addresses.addresses.size() + " addresses.");
            }
        } else {
            Log.w(TAG, "getUser: User does not exist.");
            return null;
        }
        // Make sure to close the cursor
        cursor.close();
        return addresses;
    }

    public MediUser getMe() {

        String sql = COLUMN_JSON + "," +
                COLUMN_SYSTEMUSERID + "," +
                COLUMN_IS_ME;

        Cursor cursor = database.rawQuery("SELECT " + sql + " FROM " + TABLE_USERS + " WHERE "
                + COLUMN_IS_ME + " = ?", new String[] {"1"});
        MediUser user;
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            String json = cursor.getString(getColumnIndex(COLUMN_JSON, cursor));
            user = MediUser.fromGson(json);
            Log.i(TAG, "getUser Found user: " + user.fullname);
        } else {
            Log.w(TAG, "getUser: User does not exist.");
            return null;
        }
        // Make sure to close the cursor
        cursor.close();
        return user;
    }

    public void deleteMe() {
       database.delete(TABLE_USERS, null, null);
    }

    public AccountAddresses getAccounts() {

        Cursor cursor = database.rawQuery("SELECT " + COLUMN_ACTS_GSON + " FROM " + TABLE_MY_ACCOUNTS, null);
        AccountAddresses accounts;
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            String gson = cursor.getString(getColumnIndex(COLUMN_ACTS_GSON, cursor));
            accounts = AccountAddresses.fromGson(gson);
            if (accounts != null) {
                Log.i(TAG, "getAct Found " + accounts.addresses.size() + " accounts: ");
            }
        } else {
            Log.w(TAG, "getUser: User does not exist.");
            return null;
        }
        // Make sure to close the cursor
        cursor.close();
        return accounts;
    }

    public MediUser getUser(String userid) {

        String sql = COLUMN_JSON + "," +
                COLUMN_SYSTEMUSERID + "," +
                COLUMN_IS_ME;

        Cursor cursor = database.rawQuery("SELECT " + sql + " FROM " + TABLE_USERS + " WHERE "
                + COLUMN_SYSTEMUSERID + " = ?", new String[] { userid });
        MediUser user;
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            String gson = cursor.getString(getColumnIndex(COLUMN_JSON, cursor));
            user = MediUser.fromGson(gson);
            user.systemuserid = cursor.getString(getColumnIndex(COLUMN_SYSTEMUSERID, cursor));
            Log.i(TAG, "getUser Found user: " + user.fullname);
        } else {
            Log.w(TAG, "getUser: User does not exist.");
            return null;
        }
        // Make sure to close the cursor
        cursor.close();
        return user;
    }

    // region MILEAGE

    public boolean appendTrip(TripEntry tripEntry) {

        ContentValues values = new ContentValues();
        values.put(COLUMN_DATETIME, tripEntry.getDateTime().toString());
        values.put(COLUMN_DISTANCE, tripEntry.getDistance());
        values.put(COLUMN_TRIPCODE, tripEntry.getTripcode());
        values.put(COLUMN_SPEED, tripEntry.getSpeed());
        values.put(COLUMN_LATITUDE, tripEntry.getLattitude());
        values.put(COLUMN_LONGITUDE, tripEntry.getLongitude());
        values.put(COLUMN_MILIS, tripEntry.getMilis());

        // Returns the id of the childView just added or -1 if something went wrong
        long insertId = database.insert(TABLE_TRIP_ENTRIES, null, values);

        return insertId > 0;
    }

    public TripEntry datarowToTripEntry(Cursor cursor) {
        // Instantiate a new trip entry object
        TripEntry tripEntry = new TripEntry();
        tripEntry.setTripcode(cursor.getLong(getColumnIndex(COLUMN_TRIPCODE, cursor)));
        tripEntry.setDateTime(cursor.getString(getColumnIndex(COLUMN_DATETIME, cursor)));
        tripEntry.setDistance(cursor.getInt(getColumnIndex(COLUMN_DISTANCE, cursor)));
        tripEntry.setSpeed(cursor.getFloat(getColumnIndex(COLUMN_SPEED, cursor)));
        tripEntry.setLongitude(cursor.getDouble(getColumnIndex(COLUMN_LONGITUDE, cursor)));
        tripEntry.setLatitude(cursor.getDouble(getColumnIndex(COLUMN_LATITUDE, cursor)));
        tripEntry.setMilis(cursor.getLong(getColumnIndex(COLUMN_MILIS, cursor)));
        return tripEntry;
    }

    public FullTrip datarowToFullTrip(Cursor cursor) {
        // Instantiate a new trip entry object
        FullTrip fullTrip = new FullTrip();
        fullTrip.setTitle(cursor.getString(getColumnIndex(COLUMN_TITLE, cursor)));
        fullTrip.setTripcode(cursor.getLong(getColumnIndex(COLUMN_TRIPCODE, cursor)));
        fullTrip.setDateTime(cursor.getString(getColumnIndex(COLUMN_DATETIME, cursor)));
        fullTrip.setDistance(cursor.getLong(getColumnIndex(COLUMN_DISTANCE, cursor)));
        fullTrip.setMillis(cursor.getLong(getColumnIndex(COLUMN_MILIS, cursor)));
        fullTrip.setEmail(cursor.getString(getColumnIndex(COLUMN_EMAIL, cursor)));
        fullTrip.setGu_username(cursor.getString(getColumnIndex(COLUMN_GU_USERNAME, cursor)));
        fullTrip.setObjective(cursor.getString(getColumnIndex(COLUMN_OBJECTIVE, cursor)));
        fullTrip.setOwnerid(cursor.getString(getColumnIndex(COLUMN_GUID, cursor)));
        fullTrip.isSubmitted = cursor.getInt(getColumnIndex(COLUMN_IS_SUBMITTED, cursor));
        fullTrip.edited = cursor.getInt(getColumnIndex(COLUMN_EDITED, cursor));
        fullTrip.isManualTrip = cursor.getInt(getColumnIndex(COLUMN_IS_MANUAL, cursor));
        fullTrip.setReimbursementRate(cursor.getFloat(getColumnIndex(COLUMN_RATE, cursor)));
        fullTrip.setTripGuid(cursor.getString(getColumnIndex(COLUMN_TRIP_GUID, cursor)));
        fullTrip.userStoppedTrip = cursor.getInt(getColumnIndex(COLUMN_USER_STOPPED_TRIP, cursor));
        fullTrip.userStartedTrip = cursor.getInt(getColumnIndex(COLUMN_USER_STARTED_TRIP, cursor));
        fullTrip.tripMinderKilled = cursor.getInt(getColumnIndex(COLUMN_TRIP_MINDER_KILLED, cursor));
        fullTrip.hasNearbyAssociations = cursor.getInt(getColumnIndex(COLUMN_HAS_ASSOCIATIONS, cursor));
        return fullTrip;
    }

    /**
     * Converts a data row to a RecentOrSavedTrip object.
     * @param cursor A cursor from a SQLite database.
     * @return A RecentOrSavedTrip object.
     */
    public RecentOrSavedTrip datarowToRecentOrSavedTrip(Cursor cursor) {
        RecentOrSavedTrip recentOrSavedTrip = new RecentOrSavedTrip();
        recentOrSavedTrip.id = cursor.getDouble(getColumnIndex(COLUMN_ID, cursor));
        recentOrSavedTrip.name = cursor.getString(getColumnIndex(COLUMN_NAME, cursor));

        recentOrSavedTrip.distanceInMiles = cursor.getFloat(getColumnIndex(COLUMN_DISTANCE, cursor));
        recentOrSavedTrip.fromLat = cursor.getDouble(getColumnIndex(COLUMN_FROM_LAT, cursor));
        recentOrSavedTrip.fromLon = cursor.getDouble(getColumnIndex(COLUMN_FROM_LON, cursor));
        recentOrSavedTrip.toLat = cursor.getDouble(getColumnIndex(COLUMN_TO_LAT, cursor));
        recentOrSavedTrip.toLon = cursor.getDouble(getColumnIndex(COLUMN_TO_LON, cursor));
        return recentOrSavedTrip;
    }

    /**
     * Returns all RecentOrSavedTrip entries in the database.
     * @return Ordered by ID desc.
     */
    public ArrayList<RecentOrSavedTrip> getAllRecentOrSavedTrips() {
        ArrayList<RecentOrSavedTrip> trips = new ArrayList<>();

        String sql = "" +
                "select *, (select count(*) from " + TABLE_RECENT_OR_SAVED_TRIPS + " b  where a." + COLUMN_ID + " >= b." + COLUMN_ID + ") as rownum " +
                "from " + TABLE_RECENT_OR_SAVED_TRIPS + " a " +
                "order by " + COLUMN_ID + " desc ";

        Cursor c = database.rawQuery(sql, null);

        while (c.moveToNext()) {
            RecentOrSavedTrip trip = datarowToRecentOrSavedTrip(c);
            trips.add(trip);
        }

        c.close();
        return trips;
    }

    /**
     * Creates a new RecentOrSavedTrip in the database.
     * @param trip A valid RecentOrSavedTrip object.
     */
    public void createNewRecentOrSavedTrip(RecentOrSavedTrip trip, boolean overwrite) {

        // If we find a trip with the same name already saved then we will want to delete it after
        // we confirm this one has been saved.
        RecentOrSavedTrip existingRecentWithSameName = null;
        if (overwrite) {
            ArrayList<RecentOrSavedTrip> recentTrips = getAllRecentOrSavedTrips();
            if (recentTrips != null) {
                for (RecentOrSavedTrip t : recentTrips) {
                    if (t.name.equals(trip.name)) {
                        existingRecentWithSameName = t;
                    }
                }
            }
        }

        boolean result;
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, trip.name);
            values.put(COLUMN_DIST, trip.distanceInMiles);
            values.put(COLUMN_TO_LAT, trip.toLat);
            values.put(COLUMN_TO_LON, trip.toLon);
            values.put(COLUMN_FROM_LAT, trip.fromLat);
            values.put(COLUMN_FROM_LON, trip.fromLon);

            Log.i(TAG, "RecentOrSavedTrip row was created.");

            // Remove existing trip in the recents list now that we have saved this one.
            if (overwrite && existingRecentWithSameName != null) {
                existingRecentWithSameName.delete();
                Log.i(TAG, "createNewRecentOrSavedTrip | Existing trip with same name was removed after saving this recent.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates an existing RecentOrSavedTrip in teh database.
     */
    public boolean updateRecentOrSavedTrip(RecentOrSavedTrip trip) {

        boolean result;
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, trip.name);
            values.put(COLUMN_DIST, trip.distanceInMiles);
            values.put(COLUMN_TO_LAT, trip.toLat);
            values.put(COLUMN_TO_LON, trip.toLon);
            values.put(COLUMN_FROM_LAT, trip.fromLat);
            values.put(COLUMN_FROM_LON, trip.fromLon);

            String whereClause = COLUMN_ID + " = ?";
            String[] whereArgs = {String.valueOf(trip.id)};

            result = (database.update(TABLE_RECENT_OR_SAVED_TRIPS, values, whereClause, whereArgs) > 0);

        } catch (SQLException e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Deletes all RecentOrSavedTrip entries in the recent trips table.
     * @return The amount of rows removed.
     */
    public int deleteAllRecentOrSavedTripEntries() {
        return database.delete(TABLE_RECENT_OR_SAVED_TRIPS, null, null);
    }

    /**
     * Deletes a row from the recent trips table.
     * @param id The id of the row to remove.
     * @return True if successful.
     */
    public boolean deleteRecentOrSavedtrip(double id) {
        boolean result;
        String whereClause = COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        result = (database.delete(TABLE_RECENT_OR_SAVED_TRIPS, whereClause, whereArgs)) > 0;
        Log.i(TAG, "deleteRecentOrSavedtrip " + result);
        return result;
    }

    public boolean deleteAllTripData() {
        int fulltrips = database.delete(TABLE_FULL_TRIP, null, null);
        int entries = database.delete(TABLE_TRIP_ENTRIES, null, null);
        return (fulltrips > 0 && entries > 0);
    }

    public FullTrip getFulltrip(long tripcode) {

        ArrayList<FullTrip> fullTrips = getTrips();
        for (FullTrip trip : fullTrips) {
            if (trip.getTripcode() == tripcode) {
                return trip;
            }
        }
        return null;
    }

    /**
     * Returns the latest non-manual, recorded trip based on tripcode.
     */
    public FullTrip getLatestNonManualTrip() {
        ArrayList<FullTrip> trips = getNonManualTrips();
        if (trips != null && trips.size() > 0) {
            return trips.get(0);
        }
        return null;
    }

    /**
     * Pass null to add nothing to the top of the list
     **/
    public ArrayList<FullTrip> getTrips() {
        ArrayList<FullTrip> trips = new ArrayList<>();

        String sql = "" +
                "select *, (select count(*) from fulltrips b  where a._id >= b._id) as rownum " +
                "from fulltrips a " +
                "order by tripcode desc ";

        Cursor c = database.rawQuery(sql, null);

        while (c.moveToNext()) {
            FullTrip trip = datarowToFullTrip(c);
            trips.add(trip);
        }

        c.close();
        return trips;
    }

    /**
     * Pass null to add nothing to the top of the list
     **/
    public ArrayList<FullTrip> getNonManualTrips() {
        ArrayList<FullTrip> trips = new ArrayList<>();

        String sql = "" +
                "select *, (select count(*) from fulltrips b  where a._id >= b._id) as rownum " +
                "from fulltrips a " +
                "where " + COLUMN_IS_MANUAL + " = 0 " +
                "order by tripcode desc ";

        Cursor c = database.rawQuery(sql, null);

        while (c.moveToNext()) {
            FullTrip trip = datarowToFullTrip(c);
            trips.add(trip);
        }

        c.close();
        return trips;
    }

    public ArrayList<FullTrip> getTrips(int monthNum, int year) {
        return getTrips(monthNum, year, false);
    }

    public ArrayList<FullTrip> getTrips(int monthNum, int year, boolean isSubmitted) {
        ArrayList<FullTrip> trips = new ArrayList<>();

        @NonNls String sql = "" +
                "SELECT * " +
                "FROM fulltrips " +
                "ORDER BY tripcode desc";

        if (isSubmitted) {
            sql = sql.replace("FROM fulltrips ", "FROM fulltrips WHERE " + COLUMN_IS_SUBMITTED + " = 1 ");
        }

        Cursor c = database.rawQuery(sql, null);

        while (c.moveToNext()) {
            FullTrip trip = datarowToFullTrip(c);
            if (trip.getDateTime().getMonthOfYear() == monthNum && trip.getDateTime().getYear() == year) {
                trips.add(trip);
            }
        }

        c.close();
        return trips;
    }

    public String[] getTripNames() {

        int index = 0;

        Cursor c = database.query(TABLE_FULL_TRIP, new String[] { COLUMN_TITLE }, null, null,
                null, null, null);
        String[] titles = new String[c.getCount()];
        while (c.moveToNext()) {
            titles[index] = c.getString(0);
            index++;
        }
        c.close();
        return titles;
    }

    public boolean fullTripExists(long tripcode) {
        boolean result;
        result = getFulltrip(tripcode) != null;
        Log.i(TAG, "fullTripExists " + result);
        return result;
    }

    public boolean updateFulltrip(FullTrip trip) {

        boolean result;
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, trip.getTitle());
            values.put(COLUMN_TRIPCODE, trip.getTripcode());
            values.put(COLUMN_DATETIME, trip.getDateTime().toString());
            values.put(COLUMN_DISTANCE, trip.getDistance());
            values.put(COLUMN_MILIS, trip.getMillis());
            values.put(COLUMN_EMAIL, trip.getEmail());
            values.put(COLUMN_GU_USERNAME, trip.getGu_username());
            values.put(COLUMN_OBJECTIVE, trip.getObjective());
            values.put(COLUMN_GUID, trip.getOwnerid());
            values.put(COLUMN_IS_SUBMITTED, trip.isSubmitted);
            values.put(COLUMN_IS_MANUAL, trip.isManualTrip);
            values.put(COLUMN_EDITED, trip.edited);
            values.put(COLUMN_RATE, trip.getReimbursementRate());
            values.put(COLUMN_TRIP_GUID, trip.getTripGuid());
            values.put(COLUMN_USER_STOPPED_TRIP, trip.userStoppedTrip);
            values.put(COLUMN_USER_STARTED_TRIP, trip.userStartedTrip);
            values.put(COLUMN_TRIP_MINDER_KILLED, trip.tripMinderKilled);
            values.put(COLUMN_HAS_ASSOCIATIONS, trip.hasNearbyAssociations);

            String whereClause = COLUMN_TRIPCODE + " = ?";
            String[] whereArgs = {String.valueOf(trip.getTripcode())};

            result = (database.update(TABLE_FULL_TRIP, values, whereClause, whereArgs) > 0);
            Log.i(TAG, "updateFulltrip Trip updated, removing (if necessary) and re-adding entries now");

            Log.i(TAG, "updateFulltrip Added " + trip.getTripEntries().size() + " entries");

            Log.i(TAG, "updated full trip " + result);

        } catch (SQLException e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    public boolean deleteFulltrip(long tripcode, boolean deleteEntriesToo) {
        boolean result;
        String whereClause = COLUMN_TRIPCODE + " = ?";
        String[] whereArgs = {String.valueOf(tripcode)};
        if (deleteEntriesToo) {
            result = (database.delete(TABLE_FULL_TRIP, whereClause, whereArgs)) > 0
                    &&
                    (deleteTripEntries(tripcode) > 0);
        } else {
            result = (database.delete(TABLE_FULL_TRIP, whereClause, whereArgs)) > 0;
        }
        Log.i(TAG, "deleteFulltrip " + result);
        return result;
    }

    public boolean deleteFulltrips(int olderThanXmonths) {
        boolean result;
        DateTime date = DateTime.now().minusMonths(olderThanXmonths);

        String whereClause = COLUMN_DATETIME + " < ?";
        String[] whereArgs = { date.toString() };
        result = (database.delete(TABLE_FULL_TRIP, whereClause, whereArgs)) > 0;
        Log.i(TAG, "deleteFulltrips " + result);
        return result;
    }

    public boolean deleteAllFulltrips() {
        boolean result;

        String whereClause = COLUMN_DATETIME + " < ?";
        result = (database.delete(TABLE_FULL_TRIP, whereClause, null)) > 0;
        Log.i(TAG, "deleteFulltrips " + result);
        return result;
    }

    /**
     * Deletes trip entries that do not have a corresponding full trip entry
     */
    public void deleteUnreferencedTripEntries(final MyInterfaces.TripDeleteCallback callback) {

        new MyAsyncTask() {
            boolean wasFaulted;
            int deletions;
            @Override
            public void onPreExecute() { }

            @Override
            public void doInBackground() {
                try {
                    deletions = deleteUnreferencedTripEntriesWorker();
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailure(e.getMessage());
                    wasFaulted = true;
                }
            }

            @Override
            public void onProgress(Object msg) { }

            @Override
            public void onPostExecute() {
                if (! wasFaulted) {
                    callback.onSuccess(deletions);
                }
            }

        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public void deleteEmptyTrips(final boolean justCurrentMonth, final MyInterfaces.TripDeleteCallback callback) {

        // Do not delete empty trips while a trip is running
        if (MyLocationService.isRunning) {
            return;
        }

        new MyAsyncTask() {
            boolean wasFaulted;
            int deleted;
            @Override
            public void onPreExecute() { }

            @Override
            public void doInBackground() {
                try {
                    ArrayList<FullTrip> trips;
                    if (justCurrentMonth) {
                        trips = getTrips(DateTime.now().getMonthOfYear(), DateTime.now().getYear());
                    } else {
                        trips = getTrips();
                    }
                    for (FullTrip trip : trips) {
                        if (trip.getDistanceInMiles() < 1f && ! trip.getIsRunning()) {
                            if (deleteFulltrip(trip.getTripcode(), true)) {
                                deleted++;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailure(e.getMessage());
                    wasFaulted = true;
                }
            }

            @Override
            public void onProgress(Object msg) { }

            @Override
            public void onPostExecute() {
                if (! wasFaulted) {
                    callback.onSuccess(deleted);
                }
            }

        }.execute();
    }

    /**
     * Deletes trip entries that do not have a corresponding full trip entry
     * @return The amount of entries removed.
     */
    private int deleteUnreferencedTripEntriesWorker() {

        int tripcount = 0;
        int tripentrycount = 0;

        ArrayList<Long> tripcodes = new ArrayList<>();

        // Get a group of trip codes from the trip entries table using the GROUP BY sqlite function
        Cursor tripEntriesCursor = database.query(TABLE_TRIP_ENTRIES, new String[] { COLUMN_TRIPCODE },
               null, null, COLUMN_TRIPCODE, null, null);

        // Populate our array with tripcodes
        while (tripEntriesCursor.moveToNext()) {
            long tripcode = tripEntriesCursor.getLong(getColumnIndex(COLUMN_TRIPCODE, tripEntriesCursor));
            tripcodes.add(tripcode);
        }

        tripEntriesCursor.close();

        for (Long code : tripcodes) {
            // See if the tripcode exists in the fulltrips table
            Cursor tripCursor = database.query(TABLE_FULL_TRIP, new String[]{COLUMN_TRIPCODE},
                    COLUMN_TRIPCODE + " = ?", new String[]{Long.toString(code)}
                    , null, null, null);
            if (tripCursor.getCount() == 0) {
                Log.i(TAG, "deleteUnreferencedTripEntries Found an unreferenced trip");
                tripentrycount += deleteTripEntries(code);
                tripcount += 1;
                Log.i(TAG, "deleteUnreferencedTripEntries Deleted " + tripentrycount + " entries " +
                        "representing " + tripcount + " trips.");
            }
            tripCursor.close();
        } // foreach code
        return tripentrycount;
    }

    public boolean createFullTrip(FullTrip trip) {
        boolean result;
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, trip.getTitle());
            values.put(COLUMN_TRIPCODE, trip.getTripcode());
            values.put(COLUMN_DATETIME, trip.getDateTime().toString());
            values.put(COLUMN_DISTANCE, trip.getDistance());
            values.put(COLUMN_MILIS, trip.getMillis());
            values.put(COLUMN_EMAIL, trip.getEmail());
            values.put(COLUMN_GU_USERNAME, trip.getGu_username());
            values.put(COLUMN_OBJECTIVE, trip.getObjective());
            values.put(COLUMN_GUID, trip.getOwnerid());
            values.put(COLUMN_IS_SUBMITTED, trip.isSubmitted);
            values.put(COLUMN_IS_MANUAL, trip.isManualTrip);
            values.put(COLUMN_EDITED, trip.edited);
            values.put(COLUMN_RATE, trip.getReimbursementRate());
            values.put(COLUMN_TRIP_GUID, trip.getTripGuid());
            values.put(COLUMN_USER_STOPPED_TRIP, trip.userStoppedTrip);
            values.put(COLUMN_USER_STARTED_TRIP, trip.userStartedTrip);
            values.put(COLUMN_TRIP_MINDER_KILLED, trip.tripMinderKilled);
            values.put(COLUMN_HAS_ASSOCIATIONS, trip.hasNearbyAssociations);


            result = (database.insert(TABLE_FULL_TRIP, null, values) > 0);
            Log.i(TAG, "createFullTrip Created.  Will do entries now.");

            // We have to use the JSON to reconstruct the trip entries as there are not in the database yet
            ArrayList<TripEntry> entries = TripEntry.parseCrmTripEntries(trip.tripEntriesJson);
            for (TripEntry entry : entries) {
                appendTrip(entry);
            }

            Log.i(TAG, "createFullTrip : Added " + trip.getTripEntries().size() + " entries");

        } catch (SQLException e) {
            result = false;
            e.printStackTrace();
        }
        Log.i(TAG, "updated full trip " + result);
        return result;
    }

    /**
     * Creates the full trip in the database but NOT the entries.  This is a fast operation.
     */
    public boolean createNewTrip(FullTrip trip) {
        boolean result;
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, trip.getTitle());
            values.put(COLUMN_TRIPCODE, trip.getTripcode());
            values.put(COLUMN_DATETIME, trip.getDateTime().toString());
            values.put(COLUMN_DISTANCE, trip.getDistance());
            values.put(COLUMN_MILIS, trip.getMillis());
            values.put(COLUMN_EMAIL, trip.getEmail());
            values.put(COLUMN_GU_USERNAME, trip.getGu_username());
            values.put(COLUMN_OBJECTIVE, trip.getObjective());
            values.put(COLUMN_GUID, trip.getOwnerid());
            values.put(COLUMN_IS_SUBMITTED, trip.isSubmitted);
            values.put(COLUMN_IS_MANUAL, trip.isManualTrip);
            values.put(COLUMN_EDITED, trip.edited);
            values.put(COLUMN_RATE, trip.getReimbursementRate());
            values.put(COLUMN_TRIP_GUID, trip.getTripGuid());
            values.put(COLUMN_USER_STOPPED_TRIP, trip.userStoppedTrip);
            values.put(COLUMN_USER_STARTED_TRIP, trip.userStartedTrip);
            values.put(COLUMN_TRIP_MINDER_KILLED, trip.tripMinderKilled);
            values.put(COLUMN_HAS_ASSOCIATIONS, trip.hasNearbyAssociations);

            result = (database.insert(TABLE_FULL_TRIP, null, values) > 0);
            Log.i(TAG, "createFullTrip Created.  Will do entries now.");

            Log.i(TAG, "createFullTrip : Added " + trip.getTripEntries().size() + " entries");

        } catch (SQLException e) {
            result = false;
            e.printStackTrace();
        }
        Log.i(TAG, "updated full trip " + result);
        return result;
    }

    /**
     * Pass null to add nothing to the top of the list
     **/
    public TripEntry getTripEntry(long milis, long tripcode) {
        TripEntry result;
        String whereClause = COLUMN_TRIPCODE + " = ? AND " + COLUMN_MILIS + " = ?";
        String[] whereArgs = {String.valueOf(tripcode), String.valueOf(milis)};

        Cursor c = database.query(TABLE_TRIP_ENTRIES, ALL_TRIPENTRY_COLUMNS, whereClause, whereArgs,
                null, null, null);

        result = datarowToTripEntry(c);
        Log.i(TAG, "getTripEntry " + result.toString());
        return result;
    }

    /**
     * Pass null to add nothing to the top of the list
     **/
    public ArrayList<TripEntry> getAllTripEntries(long tripcode) {

        ArrayList<TripEntry> tripEntries = new ArrayList<>();

        String whereClause = COLUMN_TRIPCODE + " = ?";
        String[] whereArgs = {String.valueOf(tripcode)};

        Cursor c = database.query(TABLE_TRIP_ENTRIES, ALL_TRIPENTRY_COLUMNS, whereClause, whereArgs,
                null, null, null);

        while (c.moveToNext()) {
            TripEntry entry = datarowToTripEntry(c);
            tripEntries.add(entry);
        }

        return tripEntries;
    }

    public boolean tripEntryExists(long tripcode, long milis) {
        return getTripEntry(tripcode, milis) != null;
    }

    public boolean updateTripEntry(TripEntry tripEntry) {

        boolean result;
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TRIPCODE, tripEntry.getTripcode());
            values.put(COLUMN_DATETIME, tripEntry.getDateTime().toString());
            values.put(COLUMN_DISTANCE, tripEntry.getDistance());
            values.put(COLUMN_SPEED, tripEntry.getSpeed());
            values.put(COLUMN_LATITUDE, tripEntry.getLattitude());
            values.put(COLUMN_LONGITUDE, tripEntry.getLongitude());
            values.put(COLUMN_TRIPCODE, tripEntry.getTripcode());
            values.put(COLUMN_MILIS, tripEntry.getMilis());

            String whereClause = COLUMN_TRIPCODE + " = ? AND " + COLUMN_MILIS + " = ?";
            String[] whereArgs = {
                    String.valueOf(tripEntry.getTripcode()),
                    String.valueOf(tripEntry.getMilis())
            };

            result = (database.update(TABLE_TRIP_ENTRIES, values, whereClause, whereArgs) > 0);

        } catch (SQLException e) {
            result = false;
            e.printStackTrace();
        }
        Log.i(TAG, "updated full trip " + result);
        return result;
    }

    public boolean createTripEntry(TripEntry tripEntry) {
        boolean result;
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TRIPCODE, tripEntry.getTripcode());
            values.put(COLUMN_DATETIME, tripEntry.getDateTime().toString());
            values.put(COLUMN_DISTANCE, tripEntry.getDistance());
            values.put(COLUMN_SPEED, tripEntry.getSpeed());
            values.put(COLUMN_LATITUDE, tripEntry.getLattitude());
            values.put(COLUMN_LONGITUDE, tripEntry.getLongitude());
            values.put(COLUMN_TRIPCODE, tripEntry.getTripcode());
            values.put(COLUMN_MILIS, tripEntry.getMilis());

            result = (database.insert(TABLE_TRIP_ENTRIES, null, values) > 0);

        } catch (SQLException e) {
            result = false;
            e.printStackTrace();
        }
        Log.i(TAG, "updated full trip " + result);
        return result;
    }

    public boolean deleteTripEntry(long milis, long tripcode) {

        String whereClause = COLUMN_TRIPCODE + " = ? AND " + COLUMN_MILIS + " = ?";
        String[] whereArgs = {
                String.valueOf(tripcode),
                String.valueOf(milis)
        };
        return (database.delete(TABLE_TRIP_ENTRIES, whereClause, whereArgs)) > 0;
    }

    public int deleteTripEntries(long tripcode) {

        String whereClause = COLUMN_TRIPCODE + " = ? ";
        String[] whereArgs = {String.valueOf(tripcode)};
        return database.delete(TABLE_TRIP_ENTRIES, whereClause, whereArgs);
    }

    // endregion




























































}