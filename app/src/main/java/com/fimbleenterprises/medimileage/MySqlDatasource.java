package com.fimbleenterprises.medimileage;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Cache;

import static com.fimbleenterprises.medimileage.MySQLiteHelper.COLUMN_IS_ME;
import static com.fimbleenterprises.medimileage.MySQLiteHelper.COLUMN_JSON;
import static com.fimbleenterprises.medimileage.MySQLiteHelper.COLUMN_SYSTEMUSERID;
import static com.fimbleenterprises.medimileage.MySQLiteHelper.TABLE_USERS;
import static com.fimbleenterprises.medimileage.MySQLiteHelper.*;
import static com.fimbleenterprises.medimileage.MySQLiteHelper.COLUMN_DATETIME;
import static com.fimbleenterprises.medimileage.MySQLiteHelper.COLUMN_DISTANCE;
import static com.fimbleenterprises.medimileage.MySQLiteHelper.COLUMN_MILIS;
import static com.fimbleenterprises.medimileage.MySQLiteHelper.COLUMN_TRIPCODE;

@SuppressLint("LongLogTag")
public class MySqlDatasource {
    // Database fields
    public static SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    Context context;

    public static final String TAG = "MileageDatasource";

    public MySqlDatasource() {
        this.context = MyApp.getAppContext();
    }

    public MySqlDatasource(Context context) {

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
        int index = cursor.getColumnIndex(colName);
        return index;
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

        boolean result = true;
        try {
            /*
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_JSON + " text, " +
            COLUMN_SYSTEMUSERID + " text, " +
            COLUMN_IS_ME + " integer);";    // Database creation sql statement
             */
            ContentValues values = new ContentValues();
            values.put(COLUMN_JSON, user.toGson());
            values.put(COLUMN_SYSTEMUSERID, user.systemuserid);
            values.put(COLUMN_IS_ME, user.isMe);

            return (database.update(TABLE_USERS, values, COLUMN_IS_ME + "= 1", null) > 0);

        } catch (SQLException e) {
            result = false;
            e.printStackTrace();
        }
        Log.i(TAG, "updateUser " + result);
        return result;
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

    /*public boolean createTripCacheEntry(FullTrip trip) {
        try {
            ContentValues values = new ContentValues();

            values.put(COLUMN_TRIP_CACHE_GSON, trip.toGson());
            values.put(COLUMN_TRIP_CACHE_TRIPCODE, trip.getTripcode());

            // Returns the id of the childView just added or -1 if something went wrong
            long insertId = database.insert(TABLE_TRIP_CACHE, null, values);
            Log.i(TAG, "createTripCacheEntry " + (insertId > -1));
            return insertId > -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTripCacheEntry(String tripcode) {
        boolean result;

        String whereClause = COLUMN_TRIP_CACHE_TRIPCODE + " = ?";
        String[] whereArgs = { tripcode };
        result = (database.delete(TABLE_TRIP_CACHE, whereClause, whereArgs)) > 0;
        Log.i(TAG, "deleteTripCacheEntry " + result);
        return result;
    }

    public FullTrip getTripCacheEntry(String tripcode) {
        boolean result;
        FullTrip trip = null;

        Cursor cursor = database.rawQuery(
                "SELECT " + COLUMN_TRIP_CACHE_GSON + " " +
                "FROM " + TABLE_TRIP_CACHE + " " +
                "WHERE " + COLUMN_TRIP_CACHE_TRIPCODE + " = '" + tripcode + "'"
                , null, null);

        if (cursor.getCount() > 0) {
            String gson = cursor.getString(getColumnIndex(COLUMN_TRIP_CACHE_GSON, cursor));
            if (gson != null) {
                Log.i(TAG, "getTripCacheEntry Found trip cache entry!");
                trip = new Gson().fromJson(gson, FullTrip.class);
            }
        } else {
            Log.w(TAG, "getUser: User does not exist.");
            cursor.close();
            return null;
        }
        // Make sure to close the cursor
        cursor.close();
        return trip;
    }

    public FullTrip getOldestTripCacheEntry() {
        boolean result;
        FullTrip trip = null;

        Cursor cursor = database.rawQuery(
                "SELECT " + COLUMN_TRIP_CACHE_GSON + " " +
                        "FROM " + TABLE_TRIP_CACHE + " " +
                        "ORDER BY " + COLUMN_TRIP_CACHE_ID + " desc"
                , null, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            String gson = cursor.getString(getColumnIndex(COLUMN_TRIP_CACHE_GSON, cursor));
            if (gson != null) {
                Log.i(TAG, "getTripCacheEntry Found trip cache entry!");
                trip = new Gson().fromJson(gson, FullTrip.class);
            }
        } else {
            Log.w(TAG, "getUser: User does not exist.");
            cursor.close();
            return null;
        }
        // Make sure to close the cursor
        cursor.close();
        return trip;
    }

    public int countTripCacheEntries() {

        Cursor cursor = database.rawQuery(
                "SELECT COUNT(*) " +
                        "FROM " + TABLE_TRIP_CACHE
                , null, null);
        cursor.close();
        return cursor.getCount();
    }

    public boolean deleteAllTripCacheEntries() {
        boolean result;

        result = (database.delete(TABLE_TRIP_CACHE, null, null)) > 0;
        Log.i(TAG, "deleteAllTripCacheEntries " + result);
        return result;
    }*/

    public boolean updateAccounts(AccountAddresses accounts) {

        boolean result = true;
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ACTS_GSON, accounts.toGson());

            return database.update(TABLE_MY_ACCOUNTS, values, null, null) > 0;

        } catch (SQLException e) {
            result = false;
            e.printStackTrace();
        }
        Log.i(TAG, "updateAccounts " + result);
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

        boolean result = true;
        try {
            /*
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_JSON + " text, " +
            COLUMN_SYSTEMUSERID + " text, " +
            COLUMN_IS_ME + " integer);";    // Database creation sql statement
             */
            ContentValues values = new ContentValues();
            values.put(COLUMN_ADDY_GSON, addresses.toGson());

            return database.update(TABLE_ADDYS, values, null, null) > 0;

        } catch (SQLException e) {
            result = false;
            e.printStackTrace();
        }
        Log.i(TAG, "updateAccounts " + result);
        return result;
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

        String sql = COLUMN_ADDY_GSON;

        Cursor cursor = database.rawQuery("SELECT " + sql + " FROM " + TABLE_ADDYS, null);
        UserAddresses addresses = null;
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
        MediUser user = null;
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

    public AccountAddresses getAccounts() {

        String sql = COLUMN_ACTS_GSON;

        Cursor cursor = database.rawQuery("SELECT " + sql + " FROM " + TABLE_MY_ACCOUNTS, null);
        AccountAddresses accounts = null;
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
        MediUser user = null;
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
        fullTrip.setMilis(cursor.getLong(getColumnIndex(COLUMN_MILIS, cursor)));
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

    public ArrayList<FullTrip> getTrips(int monthNum, int year) {
        return getTrips(monthNum, year, false);
    }

    public ArrayList<FullTrip> getTrips(int monthNum, int year, boolean isSubmitted) {
        ArrayList<FullTrip> trips = new ArrayList<>();

        String sql = "" +
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
        return titles;
    }

    /*public ArrayList<CachedTrip> getCachedTrips() {
        Cursor c = database.query(TABLE_TRIP_CACHE, new String[] { COLUMN_TRIP_CACHE_ID, COLUMN_TRIP_CACHE_JSON, COLUMN_TRIP_CACHE_TRIPCODE }, null, null,
                null, null, null);
        ArrayList<CachedTrip> trips = new ArrayList<>();
        while (c.moveToNext()) {
            CachedTrip trip = new CachedTrip(c.getString(1));
            trips.add(trip);
        }
        return trips;
    }

    public boolean deleteOldestCachedTrip() {
        boolean result;
        CachedTrip oldestTrip = getOldestCachedTrip();
        String whereClause = COLUMN_TRIPCODE + " = ?";
        String[] whereArgs = {String.valueOf(oldestTrip.tripcode)};
        result = (database.delete(TABLE_TRIP_CACHE, whereClause, whereArgs)) > 0;
        Log.i(TAG, "deleteOldestTrip " + result);
        return result;
    }

    public boolean deleteCachedTrip(CachedTrip trip) {
        boolean result;
        String whereClause = COLUMN_TRIP_CACHE_TRIPCODE + " = ?";
        String[] whereArgs = {String.valueOf(trip.tripcode)};
        result = (database.delete(TABLE_TRIP_CACHE, whereClause, whereArgs)) > 0;
        Log.i(TAG, "deleteCachedTrip " + result);
        return result;
    }

    public boolean deleteCachedTrip(long tripcode) {
        boolean result;
        String whereClause = COLUMN_TRIP_CACHE_TRIPCODE + " = ?";
        String[] whereArgs = {String.valueOf(tripcode)};
        result = (database.delete(TABLE_TRIP_CACHE, whereClause, whereArgs)) > 0;
        Log.i(TAG, "deleteCachedTrip " + result);
        return result;
    }

    public CachedTrip getOldestCachedTrip() {

        String sql = "" +
                "SELECT " + COLUMN_TRIP_CACHE_JSON + " " +
                "FROM " + TABLE_TRIP_CACHE + " " +
                "ORDER BY " + COLUMN_TRIP_CACHE_TRIPCODE + " desc";

        Cursor c = database.rawQuery(sql, null);

        while (c.moveToNext()) {
            CachedTrip trip = new CachedTrip(c.getString(0));
            c.close();
            return trip;
        }

        c.close();
        return null;
    }

    public boolean createNewTrip(CachedTrip trip) {
        boolean result = true;
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TRIP_CACHE_TRIPCODE, trip.tripcode);
            values.put(COLUMN_TRIP_CACHE_JSON, trip.toGson());

            result = (database.insert(TABLE_TRIP_CACHE, null, values) > 0);
            Log.i(TAG, "createCachedTrip Created.");

        } catch (SQLException e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    public CachedTrip getCachedTrip(long tripcode) {

        String sql = "" +
                "SELECT " + COLUMN_TRIP_CACHE_JSON + " " +
                "FROM " + TABLE_TRIP_CACHE + " " +
                "WHERE " + COLUMN_TRIP_CACHE_TRIPCODE + " = " + tripcode + ";";

        Cursor c = database.rawQuery(sql, null);

        while (c.moveToNext()) {
            CachedTrip trip = new CachedTrip(c.getString(0));
            c.close();
            return trip;
        }

        c.close();
        return null;
    }

    public int countCachedTrips() {

        String sql = "" +
                "SELECT " + COLUMN_TRIP_CACHE_JSON + " " +
                "FROM " + TABLE_TRIP_CACHE + " " +
                "ORDER BY " + COLUMN_TRIP_CACHE_TRIPCODE + " desc";

        Cursor c = database.rawQuery(sql, null);

        return c.getCount();
    }*/

    public boolean fullTripExists(long tripcode) {
        boolean result;
        result = getFulltrip(tripcode) != null;
        Log.i(TAG, "fullTripExists " + result);
        return result;
    }

    public boolean updateFulltrip(FullTrip trip) {

        boolean result = true;
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, trip.getTitle());
            values.put(COLUMN_TRIPCODE, trip.getTripcode());
            values.put(COLUMN_DATETIME, trip.getDateTime().toString());
            values.put(COLUMN_DISTANCE, trip.getDistance());
            values.put(COLUMN_MILIS, trip.getMilis());
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
     * @return The amount of entries removed.
     */
    @SuppressLint("StaticFieldLeak")
    public void deleteUnreferencedTripEntries(final MyInterfaces.TripDeleteCallback callback) {

        new AsyncTask<Integer, Integer, Integer>() {
            boolean wasFaulted = false;


            @Override
            protected Integer doInBackground(Integer... ints) {
                try {
                    return deleteUnreferencedTripEntriesWorker();
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailure(e.getMessage());
                    wasFaulted = true;
                    return 0;
                }
            }

            @Override
            protected void onPostExecute(Integer i) {
                super.onPostExecute(i);
                if (! wasFaulted) {
                    callback.onSuccess(i);
                }
            }
        }.execute(null, null, null);
    }

    @SuppressLint("StaticFieldLeak")
    public void deleteEmptyTrips(final boolean justCurrentMonth, final MyInterfaces.TripDeleteCallback callback) {

        // Do not delete empty trips while a trip is running
        if (MyLocationService.isRunning) {
            return;
        }

        new AsyncTask<Integer, Integer, Integer>() {
            boolean wasFaulted = false;

            @Override
            protected Integer doInBackground(Integer... ints) {
                try {
                    ArrayList<FullTrip> trips = new ArrayList<>();
                    if (justCurrentMonth) {
                        trips = getTrips(DateTime.now().getMonthOfYear(), DateTime.now().getYear());
                    } else {
                        trips = getTrips();
                    }
                    int deleteCount = 0;
                    for (FullTrip trip : trips) {
                        if (trip.getDistanceInMiles() < 1f && ! trip.getIsRunning()) {
                            if (deleteFulltrip(trip.getTripcode(), true)) {
                                deleteCount++;
                            }
                        }
                    }
                    return deleteCount;
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailure(e.getMessage());
                    wasFaulted = true;
                    return 0;
                }
            }

            @Override
            protected void onPostExecute(Integer i) {
                super.onPostExecute(i);
                if (! wasFaulted) {
                    callback.onSuccess(i);
                }
            }
        }.execute(null, null, null);
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
        boolean result = true;
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, trip.getTitle());
            values.put(COLUMN_TRIPCODE, trip.getTripcode());
            values.put(COLUMN_DATETIME, trip.getDateTime().toString());
            values.put(COLUMN_DISTANCE, trip.getDistance());
            values.put(COLUMN_MILIS, trip.getMilis());
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

    public boolean createNewTrip(FullTrip trip) {
        boolean result = true;
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, trip.getTitle());
            values.put(COLUMN_TRIPCODE, trip.getTripcode());
            values.put(COLUMN_DATETIME, trip.getDateTime().toString());
            values.put(COLUMN_DISTANCE, trip.getDistance());
            values.put(COLUMN_MILIS, trip.getMilis());
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

        boolean result = true;
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
        boolean result = true;
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