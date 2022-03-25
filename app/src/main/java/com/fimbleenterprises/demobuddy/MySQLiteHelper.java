package com.fimbleenterprises.demobuddy;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.fimbleenterprises.demobuddy.objects_and_containers.MediUser;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;

@SuppressWarnings("WeakerAccess")
public class MySQLiteHelper extends SQLiteOpenHelper {

    private final static String TAG = "MySQLiteHelper.";
    public static final String DATABASE_NAME = "mileagetracking.db";
    private static final int DATABASE_VERSION = 10;
    private MyPreferencesHelper options;

    public static final String TABLE_FULL_TRIP = "fulltrips";
    public static final String TABLE_TRIP_ENTRIES = "tripentries";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_COMMENT = "comment";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_OBJECTIVE = "objective";
    public static final String COLUMN_DATETIME = "dtdate";
    public static final String COLUMN_TRIPCODE = "tripcode";
    public static final String COLUMN_DISTANCE = "distance";
    public static final String COLUMN_EMAIL = "googleEmail";
    public static final String COLUMN_MILIS = "milis";
    public static final String COLUMN_GUID = "guid";
    public static final String COLUMN_USERFIELD1 = "userfield1";
    public static final String COLUMN_USERFIELD2 = "userfield2";
    public static final String COLUMN_USERFIELD3 = "userfield3";
    public static final String COLUMN_EDITED = "edited";
    public static final String COLUMN_USERFIELD4 = "userfield4";
    public static final String COLUMN_GU_USERNAME = "gu_username";
    public static final String COLUMN_LOCATION_JSON = "locationjson";
    public static final String COLUMN_IS_SUBMITTED = "issubmitted";
    public static final String COLUMN_SPEED = "speed";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_IS_MANUAL = "ismanual";
    public static final String COLUMN_RATE = "rate";
    public static final String COLUMN_TRIP_GUID = "tripguid";
    public static final String COLUMN_USER_STARTED_TRIP = "userstartedtrip";
    public static final String COLUMN_USER_STOPPED_TRIP = "userstoppedtrip";
    public static final String COLUMN_TRIP_MINDER_KILLED = "tripminderkilled";
    public static final String COLUMN_TRIP_ASSOCIATED_ACCOUNTID = "associatedaccountid";
    public static final String COLUMN_TRIP_ASSOCIATED_ACCOUNTNAME = "associatedaccountname";
    public static final String COLUMN_TRIP_ASSOCIATED_OPPORTUNITYID = "associatedopportunityid";
    public static final String COLUMN_TRIP_ASSOCIATED_OPPORTUNITYNAME = "associatedopportunityname";
    public static final String COLUMN_HAS_ASSOCIATIONS = "hasassociations";

    public static final String TABLE_ADDYS = "useraddresses";
    public static final String COLUMN_ADDY_ADDRESS = "address";
    public static final String COLUMN_ADDY_USERID = "userid";
    public static final String COLUMN_ADDY_FULLNAME = "fullname";
    public static final String COLUMN_ADDY_PHONE = "phone";
    public static final String COLUMN_ADDY_TERRITORY = "territory";
    public static final String COLUMN_ADDY_GSON = "gson";

    public static final String TABLE_MY_ACCOUNTS = "myaccounts";
    public static final String COLUMN_ACT_NAME = "accountname";
    public static final String COLUMN_ACT_GUID = "accountguid";
    public static final String COLUMN_ACT_NUMBER = "accountnumber";
    public static final String COLUMN_ACT_ADDY1_COMPOSITE = "address1composit";
    public static final String COLUMN_ACT_LATITUDE = "address1Latitude";
    public static final String COLUMN_ACT_LONGITUDE = "address1Longitude";
    public static final String COLUMN_ACT_TERRITORY = "territory";
    public static final String COLUMN_ACT_TERRITORYID = "territoryid";
    public static final String COLUMN_ACT_SALESREPID = "salesrepid";
    public static final String COLUMN_ACT_RELATIONSHIP = "relationship";

    public static final String TABLE_CURRENT_LOCATIONS = "currentlocations";
    public static final String COLUMN_CURRENT_LOCATIONS_ID = "_id";
    public static final String COLUMN_CURRENT_LOCATIONS_LAT = "lat";
    public static final String COLUMN_CURRENT_LOCATIONS_LNG = "lng";
    public static final String COLUMN_CURRENT_LOCATIONS_USER_EMAIL = "user_email";
    public static final String COLUMN_CURRENT_LOCATIONS_TIMESTAMP = "time_stamp";
    public static final String COLUMN_CURRENT_LOCATIONS_SPEED = "speed";
    public static final String COLUMN_CURRENT_LOCATIONS_DIRECTION = "direction";
    public static final String COLUMN_CURRENT_LOCATIONS_GU_USERNAME = "gu_username";
    public static final String COLUMN_CURRENT_LOCATIONS_USERFIELD1 = "userfield1";
    public static final String COLUMN_CURRENT_LOCATIONS_USERFIELD2 = "userfield2";
    public static final String COLUMN_CURRENT_LOCATIONS_USERFIELD3 = "userfield3";
    public static final String COLUMN_CURRENT_LOCATIONS_USERFIELD4 = "userfield4";
    public static final String COLUMN_CURRENT_LOCATIONS_USERFIELD5 = "userfield5";
    public static final String COLUMN_CURRENT_LOCATIONS_USERFIELD6 = "userfield6";
    public static final String COLUMN_CURRENT_LOCATIONS_USERFIELD7 = "userfield7";
    public static final String COLUMN_CURRENT_LOCATIONS_USERFIELD8 = "userfield8";
    public static final String COLUMN_CURRENT_LOCATIONS_USERFIELD9 = "userfield9";
    public static final String COLUMN_CURRENT_LOCATIONS_USERFIELD10 = "userfield10";

    // Users table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_JSON = "json";
    public static final String COLUMN_SYSTEMUSERID = "systemuserid";
    public static final String COLUMN_ACTS_GSON = "gson";
    public static final String COLUMN_IS_ME = "isme";

    // Settings table
    public static final String TABLE_SETTINGS = "settings";
    public static final String COLUMN_SETTING_NAME = "name";
    public static final String COLUMN_SETTING_INT_VALUE = "intvalue";
    public static final String COLUMN_SETTING_STRING_VALUE = "stringvalue";
    public static final String COLUMN_SETTING_BOOL_VALUE = "boolvalue";

    // Recent/Saved trips table
    public static final String TABLE_RECENT_OR_SAVED_TRIPS = "recenttrips";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_FROM_LAT = "fromlat";
    public static final String COLUMN_FROM_LON = "fromlon";
    public static final String COLUMN_TO_LAT = "tolat";
    public static final String COLUMN_TO_LON = "tolon";
    public static final String COLUMN_DIST = "distance";
    public static final String COLUMN_DATE = "date";


    public static final String[] ALL_TRIP_DETAILS_COLUMNS = {
            COLUMN_ID,
            COLUMN_DATETIME,
            COLUMN_DISTANCE,
            COLUMN_SPEED,
            COLUMN_LONGITUDE,
            COLUMN_LATITUDE,
            COLUMN_TRIPCODE,
            COLUMN_MILIS,
    };

    public static final String[] ALL_RECENT_TRIPS_COLUMNS = {
            COLUMN_ID,
            COLUMN_DATE,
            COLUMN_DIST,
            COLUMN_NAME,
            COLUMN_FROM_LON,
            COLUMN_FROM_LAT,
            COLUMN_TO_LON,
            COLUMN_TO_LAT
    };

    public static final String[] ALL_MYACCOUNTS_COLUMNS = {

            COLUMN_ID,
            COLUMN_GUID,
            COLUMN_ACT_NAME,
            COLUMN_ACT_NUMBER,
            COLUMN_ACT_ADDY1_COMPOSITE,
            COLUMN_ACT_LATITUDE,
            COLUMN_ACT_LONGITUDE,
            COLUMN_ACT_TERRITORY,
            COLUMN_ACT_TERRITORYID,
            COLUMN_ACTS_GSON,
            COLUMN_ACT_RELATIONSHIP,
            COLUMN_ACT_SALESREPID

    };

    public static final String[] ALL_USERADDYS_TABLE = {

            COLUMN_ID,
            COLUMN_ADDY_ADDRESS,
            COLUMN_ADDY_PHONE,
            COLUMN_ADDY_USERID,
            COLUMN_ADDY_TERRITORY,
            COLUMN_ADDY_GSON,
            COLUMN_ADDY_FULLNAME
    };

    public static final String[] ALL_FULLTRIP_COLUMNS = {
            COLUMN_ID,
            COLUMN_TITLE,
            COLUMN_OBJECTIVE,
            COLUMN_DATETIME,
            COLUMN_TRIPCODE,
            COLUMN_DISTANCE,
            COLUMN_EMAIL,
            COLUMN_COMMENT,
            COLUMN_MILIS,
            COLUMN_USERFIELD1,
            COLUMN_IS_SUBMITTED,
            COLUMN_IS_MANUAL,
            COLUMN_USERFIELD2,
            COLUMN_USERFIELD3,
            COLUMN_USERFIELD4,
            COLUMN_EDITED,
            COLUMN_GU_USERNAME,
            COLUMN_GUID,
            COLUMN_RATE,
            COLUMN_USER_STOPPED_TRIP,
            COLUMN_USER_STARTED_TRIP,
            COLUMN_TRIP_MINDER_KILLED,
            COLUMN_TRIP_ASSOCIATED_ACCOUNTID,
            COLUMN_TRIP_ASSOCIATED_ACCOUNTNAME,
            COLUMN_TRIP_ASSOCIATED_OPPORTUNITYID,
            COLUMN_TRIP_ASSOCIATED_OPPORTUNITYNAME,
            COLUMN_TRIP_GUID,
            COLUMN_HAS_ASSOCIATIONS
    };

    public static final String[] ALL_TRIPENTRY_COLUMNS = {
            COLUMN_ID,
            COLUMN_DATETIME,
            COLUMN_TRIPCODE,
            COLUMN_DISTANCE,
            COLUMN_MILIS,
            COLUMN_SPEED,
            COLUMN_LATITUDE,
            COLUMN_LONGITUDE,
            COLUMN_USERFIELD1,
            COLUMN_USERFIELD2,
            COLUMN_USERFIELD3,
            COLUMN_USERFIELD4,
            COLUMN_GUID
    };

    // private static final String TYPE_NUMERIC = "NUMERIC";
    private static final String TYPE_INTEGER = "INTEGER";
    private static final String TYPE_TEXT = "TEXT";
    private static final String TYPE_REAL = "REAL";
    //private static final String TYPE_BLOB = "BLOB";

    // Database creation sql statement
    private static final String SETTINGS_TABLE_CREATE = "create table " + TABLE_SETTINGS + "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_SETTING_NAME + " text, " +
            COLUMN_SETTING_INT_VALUE + " integer, " +
            COLUMN_SETTING_STRING_VALUE + " text, " +
            COLUMN_SETTING_BOOL_VALUE + " integer);";    // Database creation sql statement

    // Database creation sql statement
    private static final String MY_ACCOUNTS_TABLE_CREATE = "create table " + TABLE_MY_ACCOUNTS + "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_ACT_GUID + " real, " +
            COLUMN_ACT_NAME + " text, " +
            COLUMN_ACT_NUMBER + " text, " +
            COLUMN_ACT_ADDY1_COMPOSITE + " text, " +
            COLUMN_ACT_LATITUDE + " real, " +
            COLUMN_ACT_LONGITUDE + " real, " +
            COLUMN_ACT_TERRITORY + " text, " +
            COLUMN_ACTS_GSON + " text, " +
            COLUMN_ACT_RELATIONSHIP + " text, " +
            COLUMN_ACT_TERRITORYID + " text);";   // Database creation sql statement

    // Database creation sql statement
    private static final String USER_TABLE_CREATE = "create table " + TABLE_USERS + "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_JSON + " text, " +
            COLUMN_SYSTEMUSERID + " text, " +
            COLUMN_ACTS_GSON + " text, " +
            COLUMN_IS_ME + " integer);";    // Database creation sql statement

    // Database creation sql statement
    private static final String ADDYS_TABLE_CREATE = "create table " + TABLE_ADDYS + "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_ADDY_FULLNAME + " text, " +
            COLUMN_ADDY_TERRITORY + " text, " +
            COLUMN_ADDY_USERID + " text, " +
            COLUMN_ADDY_ADDRESS + " text, " +
            COLUMN_ADDY_PHONE + " text, " +
            COLUMN_ADDY_GSON + " text);";    // Database creation sql statement

    private static final String TRIP_ENTRIES_TABLE_CREATE = "create table " + TABLE_TRIP_ENTRIES + "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_DATETIME + " text, " +
            COLUMN_TRIPCODE + " real, " +
            COLUMN_DISTANCE + " real, " +
            COLUMN_MILIS + " real" + ", " +
            COLUMN_SPEED + " real" + ", " +
            COLUMN_LONGITUDE + " real" + ", " +
            COLUMN_LATITUDE + " real" + ", " +
            COLUMN_USERFIELD1 + " text" + ", " +
            COLUMN_USERFIELD2 + " text" + ", " +
            COLUMN_USERFIELD3 + " text" + ", " +
            COLUMN_USERFIELD4 + " text" + ", " +
            COLUMN_GUID + " text);";    // Database creation sql statement

    private static final String RECENT_TRIPS_TABLE_CREATE = "create table " + TABLE_RECENT_OR_SAVED_TRIPS + "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_NAME + " text, " +
            COLUMN_DATE + " text, " +
            COLUMN_DIST + " real, " +
            COLUMN_FROM_LON + " real" + ", " +
            COLUMN_FROM_LAT + " real" + ", " +
            COLUMN_TO_LON + " real" + ", " +
            COLUMN_TO_LAT + " real);";  // Database creation sql statement

    private static final String FULL_TRIP_TABLE_CREATE = "create table " + TABLE_FULL_TRIP + "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_TITLE + " text not null, " +
            COLUMN_OBJECTIVE + " text, " +
            COLUMN_DATETIME + " text, " +
            COLUMN_TRIPCODE + " real, " +
            COLUMN_DISTANCE + " real, " +
            COLUMN_EMAIL + " text, " +
            COLUMN_COMMENT + " text, " +
            COLUMN_MILIS + " real" + ", " +
            COLUMN_IS_MANUAL + " integer" + ", " +
            COLUMN_USERFIELD1 + " text" + ", " +
            COLUMN_USERFIELD2 + " text" + ", " +
            COLUMN_USERFIELD3 + " text" + ", " +
            COLUMN_USERFIELD4 + " text" + ", " +
            COLUMN_EDITED + " integer" + ", " +
            COLUMN_GU_USERNAME + " text" + ", " +
            COLUMN_RATE + " integer" + ", " +
            COLUMN_IS_SUBMITTED + " integer" + ", " +
            COLUMN_USER_STOPPED_TRIP + " integer" + ", " +
            COLUMN_USER_STARTED_TRIP + " integer" + ", " +
            COLUMN_TRIP_MINDER_KILLED + " integer" + ", " +
            COLUMN_TRIP_ASSOCIATED_OPPORTUNITYID + " text" + ", " +
            COLUMN_TRIP_ASSOCIATED_OPPORTUNITYNAME + " text" + ", " +
            COLUMN_TRIP_ASSOCIATED_ACCOUNTID + " text" + ", " +
            COLUMN_TRIP_ASSOCIATED_ACCOUNTNAME + " text" + ", " +
            COLUMN_TRIP_GUID + " text" + ", " +
            COLUMN_HAS_ASSOCIATIONS + " integer" + ", " +
            COLUMN_GUID + " text);";

    private static final String CURRENTLOCATIONS_TABLE_CREATE = "create table " + TABLE_CURRENT_LOCATIONS + "(" +
            COLUMN_CURRENT_LOCATIONS_ID + " integer primary key autoincrement, " +
            COLUMN_CURRENT_LOCATIONS_LAT + " text not null, " +
            COLUMN_CURRENT_LOCATIONS_LNG + " text not null, " +
            COLUMN_CURRENT_LOCATIONS_USER_EMAIL + " text not null, " +
            COLUMN_CURRENT_LOCATIONS_TIMESTAMP + " text not null, " +
            COLUMN_CURRENT_LOCATIONS_SPEED + " text, " +
            COLUMN_CURRENT_LOCATIONS_DIRECTION + "text, " +
            COLUMN_CURRENT_LOCATIONS_GU_USERNAME + " text" + ", " +
            COLUMN_CURRENT_LOCATIONS_USERFIELD1 + " text, " +
            COLUMN_CURRENT_LOCATIONS_USERFIELD2 + " text, " +
            COLUMN_CURRENT_LOCATIONS_USERFIELD3 + " text, " +
            COLUMN_CURRENT_LOCATIONS_USERFIELD4 + " text, " +
            COLUMN_CURRENT_LOCATIONS_USERFIELD5 + " text, " +
            COLUMN_CURRENT_LOCATIONS_USERFIELD6 + " text, " +
            COLUMN_CURRENT_LOCATIONS_USERFIELD7 + " text, " +
            COLUMN_CURRENT_LOCATIONS_USERFIELD8 + " text, " +
            COLUMN_CURRENT_LOCATIONS_USERFIELD9 + " text, " +
            COLUMN_CURRENT_LOCATIONS_USERFIELD10 + " text); ";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        options = new MyPreferencesHelper(context);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {

        // database.setVersion(3);
        Log.w(TAG + "onCreate", "Creating the databases...");

        validateTables(database);

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        Log.d(TAG + "onOpen", "The database has been opened.  Checking that the database contains all necessary tables...");
        db.setLocale(Locale.US);
        Log.i(TAG, "onOpen Database opening.  Local set to: " + Locale.US.getDisplayName());
        options.setDbPath(db.getPath());

        validateTables(db);
        validateColumns(db);
    }

    private void createTable(SQLiteDatabase database, String query, String tableName) {
        try {
            database.execSQL(query);
            if (tableExists(tableName, database)) {
                Log.d("createTable", "The '" + tableName + "' table was created");
            } else {
                Log.e("createTable", "Failed to create the '" + tableName + "' table");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean tableExists(String tableName, SQLiteDatabase mDatabase) {
        Cursor cursor = mDatabase.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name ='" + tableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                Log.d("tableExists", "The " + tableName + " table exists");
                return true;
            }
            cursor.close();
        }
        Log.w("tableExists", "The " + tableName + " table doesn't exist!");
        return false;
    }

    // This method will check if column exists in your table
    private boolean columnExists(String columnName, String tableName, SQLiteDatabase db) {
        boolean isExist = false;
        Cursor res = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
        if (res != null) {
            res.moveToFirst();
            do {
                String currentColumn = res.getString(1);
                if (currentColumn.equals(columnName)) {
                    isExist = true;
                }
            } while (res.moveToNext());
            res.close();
        }
        return isExist;
    }

    private void validateTables(SQLiteDatabase db) {
        try {
            if (!this.tableExists(TABLE_USERS, db)) {
                Log.e(TAG + "onOpen", "The currentLocations table does not exist.  Will try to create it now.");
                createTable(db, USER_TABLE_CREATE, TABLE_USERS);
            } else {
                Log.d(TAG + "onOpen", "The users table exists.  No need to create it.");
            }

            if (!this.tableExists(TABLE_FULL_TRIP, db)) {
                Log.e(TAG + "onOpen", "The currentLocations table does not exist.  Will try to create it now.");
                createTable(db, FULL_TRIP_TABLE_CREATE, TABLE_FULL_TRIP);
            } else {
                Log.d(TAG + "onOpen", "The full trip table exists.  No need to create it.");
            }

            if (!this.tableExists(TABLE_TRIP_ENTRIES, db)) {
                Log.e(TAG + "onOpen", "The trip details table does not exist.  Will try to create it now.");
                createTable(db, TRIP_ENTRIES_TABLE_CREATE, TABLE_TRIP_ENTRIES);
            } else {
                Log.d(TAG + "onOpen", "The trip details table exists.  No need to create it.");
            }

            if (!this.tableExists(TABLE_CURRENT_LOCATIONS, db)) {
                Log.e(TAG + "onOpen", "The currentLocations table does not exist.  Will try to create it now.");
                createTable(db, CURRENTLOCATIONS_TABLE_CREATE, TABLE_CURRENT_LOCATIONS);
            } else {
                Log.d(TAG + "onOpen", "The currentLocations table exists.  No need to create it.");
            }

            if (!this.tableExists(TABLE_SETTINGS, db)) {
                Log.e(TAG + "onOpen", "The settings table does not exist.  Will try to create it now.");
                createTable(db, SETTINGS_TABLE_CREATE, TABLE_SETTINGS);
            } else {
                Log.d(TAG + "onOpen", "The settings table exists.  No need to create it.");
            }

            if (!this.tableExists(TABLE_MY_ACCOUNTS, db)) {
                Log.e(TAG + "onOpen", "The accounts table does not exist.  Will try to create it now.");
                createTable(db, MY_ACCOUNTS_TABLE_CREATE, TABLE_MY_ACCOUNTS);
            } else {
                Log.d(TAG + "onOpen", "The accounts table exists.  No need to create it.");
            }

            if (!this.tableExists(TABLE_ADDYS, db)) {
                Log.e(TAG + "onOpen", "The user addresses table does not exist.  Will try to create it now.");
                createTable(db, ADDYS_TABLE_CREATE, TABLE_ADDYS);
            } else {
                Log.d(TAG + "onOpen", "The user addresses table exists.  No need to create it.");
            }

            if (!this.tableExists(TABLE_RECENT_OR_SAVED_TRIPS, db)) {
                Log.e(TAG + "onOpen", "The recent trips table does not exist.  Will try to create it now.");
                createTable(db, RECENT_TRIPS_TABLE_CREATE, TABLE_RECENT_OR_SAVED_TRIPS);
            } else {
                Log.d(TAG + "onOpen", "The recent trips table exists.  No need to create it.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void validateColumns(SQLiteDatabase db) {

        // Trip entries
        addColumnIfMissing(TABLE_TRIP_ENTRIES, COLUMN_ID, TYPE_INTEGER, db);
        addColumnIfMissing(TABLE_TRIP_ENTRIES, COLUMN_DATETIME, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_TRIP_ENTRIES, COLUMN_TRIPCODE, TYPE_REAL, db);
        addColumnIfMissing(TABLE_TRIP_ENTRIES, COLUMN_DISTANCE, TYPE_REAL, db);
        addColumnIfMissing(TABLE_TRIP_ENTRIES, COLUMN_MILIS, TYPE_REAL, db);
        addColumnIfMissing(TABLE_TRIP_ENTRIES, COLUMN_USERFIELD1, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_TRIP_ENTRIES, COLUMN_USERFIELD2, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_TRIP_ENTRIES, COLUMN_USERFIELD3, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_TRIP_ENTRIES, COLUMN_USERFIELD4, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_TRIP_ENTRIES, COLUMN_GUID, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_TRIP_ENTRIES, COLUMN_LATITUDE, TYPE_REAL, db);
        addColumnIfMissing(TABLE_TRIP_ENTRIES, COLUMN_LONGITUDE, TYPE_REAL, db);
        addColumnIfMissing(TABLE_TRIP_ENTRIES, COLUMN_SPEED, TYPE_REAL, db);

        // Full trips
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_ID, TYPE_INTEGER, db);
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_IS_MANUAL, TYPE_INTEGER, db);
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_TITLE, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_OBJECTIVE, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_DATETIME, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_TRIPCODE, TYPE_REAL, db);
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_DISTANCE, TYPE_REAL, db);
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_EMAIL, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_MILIS, TYPE_REAL, db);
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_USERFIELD1, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_USERFIELD2, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_USERFIELD3, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_USERFIELD4, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_EDITED, TYPE_INTEGER, db);
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_GU_USERNAME, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_GUID, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_IS_SUBMITTED, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_RATE, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_TRIP_GUID, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_USER_STOPPED_TRIP, TYPE_INTEGER, db);
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_USER_STARTED_TRIP, TYPE_INTEGER, db);
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_TRIP_MINDER_KILLED, TYPE_INTEGER, db);
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_TRIP_ASSOCIATED_ACCOUNTNAME, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_TRIP_ASSOCIATED_ACCOUNTID, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_TRIP_ASSOCIATED_OPPORTUNITYNAME, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_TRIP_ASSOCIATED_OPPORTUNITYID, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_FULL_TRIP, COLUMN_HAS_ASSOCIATIONS, TYPE_INTEGER, db);

        // Users table
        addColumnIfMissing(TABLE_USERS, COLUMN_ID, TYPE_INTEGER, db);
        addColumnIfMissing(TABLE_USERS, COLUMN_JSON, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_USERS, COLUMN_SYSTEMUSERID, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_USERS, COLUMN_ACTS_GSON, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_USERS, COLUMN_IS_ME, TYPE_INTEGER, db);

        // User addresses table
        addColumnIfMissing(TABLE_ADDYS, COLUMN_ID, TYPE_INTEGER, db);
        addColumnIfMissing(TABLE_ADDYS, COLUMN_ADDY_FULLNAME, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_ADDYS, COLUMN_ADDY_ADDRESS, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_ADDYS, COLUMN_ADDY_GSON, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_ADDYS, COLUMN_ADDY_PHONE, TYPE_INTEGER, db);
        addColumnIfMissing(TABLE_ADDYS, COLUMN_ADDY_TERRITORY, TYPE_INTEGER, db);
        addColumnIfMissing(TABLE_ADDYS, COLUMN_ADDY_USERID, TYPE_INTEGER, db);

        // Recent trips table
        addColumnIfMissing(TABLE_RECENT_OR_SAVED_TRIPS, COLUMN_ID, TYPE_INTEGER, db);
        addColumnIfMissing(TABLE_RECENT_OR_SAVED_TRIPS, COLUMN_NAME, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_RECENT_OR_SAVED_TRIPS, COLUMN_DATE, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_RECENT_OR_SAVED_TRIPS, COLUMN_DIST, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_RECENT_OR_SAVED_TRIPS, COLUMN_FROM_LAT, TYPE_REAL, db);
        addColumnIfMissing(TABLE_RECENT_OR_SAVED_TRIPS, COLUMN_FROM_LON, TYPE_REAL, db);
        addColumnIfMissing(TABLE_RECENT_OR_SAVED_TRIPS, COLUMN_TO_LAT, TYPE_REAL, db);
        addColumnIfMissing(TABLE_RECENT_OR_SAVED_TRIPS, COLUMN_TO_LON, TYPE_REAL, db);

        // Settings table
        addColumnIfMissing(TABLE_SETTINGS, COLUMN_ID, TYPE_INTEGER, db);
        addColumnIfMissing(TABLE_SETTINGS, COLUMN_SETTING_NAME, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_SETTINGS, COLUMN_SETTING_BOOL_VALUE, TYPE_INTEGER, db);
        addColumnIfMissing(TABLE_SETTINGS, COLUMN_SETTING_INT_VALUE, TYPE_INTEGER, db);
        addColumnIfMissing(TABLE_SETTINGS, COLUMN_SETTING_STRING_VALUE, TYPE_TEXT, db);

        // Accounts table
        addColumnIfMissing(TABLE_MY_ACCOUNTS, COLUMN_ACT_NAME, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_MY_ACCOUNTS, COLUMN_ACT_NUMBER, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_MY_ACCOUNTS, COLUMN_ACT_ADDY1_COMPOSITE, TYPE_INTEGER, db);
        addColumnIfMissing(TABLE_MY_ACCOUNTS, COLUMN_ACT_LATITUDE, TYPE_REAL, db);
        addColumnIfMissing(TABLE_MY_ACCOUNTS, COLUMN_ACT_LONGITUDE, TYPE_REAL, db);
        addColumnIfMissing(TABLE_MY_ACCOUNTS, COLUMN_ACT_TERRITORY, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_MY_ACCOUNTS, COLUMN_ACT_TERRITORYID, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_MY_ACCOUNTS, COLUMN_ACT_GUID, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_MY_ACCOUNTS, COLUMN_ACTS_GSON, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_MY_ACCOUNTS, COLUMN_ACT_RELATIONSHIP, TYPE_TEXT, db);

    }

    private void addColumnIfMissing(String tableName, String columnName, String dataType, SQLiteDatabase db) {
        if (!columnExists(columnName, tableName, db)) {
            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + dataType);
            Log.i(TAG, "addColumnIfMissing:: Added column: (" + columnName + ")");
        } else {
            Log.i(TAG, "addColumnIfMissing:: Column already exists (" + columnName + ")");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(), "Upgrading database from version " + oldVersion
                + " to " + newVersion);

        validateTables(db);

        validateColumns(db);

        onCreate(db);
    }

    /**
     * Returns the file (null or not) at the following path:<br/>
     * <i>new File(Environment.getDataDirectory() + File.separator + "data//com.fimbleenterprises.medimileage//databases/mileagetracking.db")</i>
     *
     * @return A File object of where the sqlite db <i>should</i> be.
     */
    public static File getCurrentDbFile() {
        return new File(Environment.getDataDirectory() + File.separator
                + "data//com.fimbleenterprises.medimileage//databases//" + DATABASE_NAME);
    }

    /**
     * This will make a copy of the actual SQLite db file and optionally share it via email.
     * @param context A valid context for an intent (for sharing via email).  If null the
     *                sharing dialog will not be shown.
     * @param backupDB The absolute path to the file to use as the backup.
     * @return A File object representing the copied file, null if something goes wrong.
     */
    public File exportDB(File backupDB, @Nullable Context context) {

        // File backupDB = new File(targetFileAbsPath);

        try {
            if (backupDB.getParentFile().canWrite()) {

                File currentDB = getCurrentDbFile();
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();

                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();

                // See if it exists and is not zero length and return if so.
                if (backupDB.exists() && backupDB.length() > 0) {
                    if (context != null) {
                        String[] recips = new String[1];
                        recips[0] = "matt.weber@medistimusa.com";
                        Helpers.Email.sendEmail(recips, "Database generated from device with logged in user: "
                                + MediUser.getMe().fullname, "Mileage database export", context, backupDB, true);
                    }
                    return backupDB;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {

            return null;

        }

        /*try {
            if (Helpers.Files.ReceiptTempFiles.getDirectory().canWrite()) {

                File currentDB = getActualDbFile();
                File backupDB = new File(Helpers.Files.ReceiptTempFiles.getDirectory()
                        , "backupdb.png");
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();

                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();

                // See if it exists and is not zero length and return if so.
                if (backupDB.exists() && backupDB.length() > 0) {
                    if (shareFile) {
                        String[] recips = new String[1];
                        recips[0] = "matt.weber@medistimusa.com";
                        Helpers.Email.sendEmail(recips, "test", "test sub", context, backupDB, true);
                    }
                    return backupDB;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {

            return null;

        }*/
    }

    /**
     * This works; meaning, it does create a CSV file that appears valid.  Have not tested it in any
     * meaningful way however.  It's fucking cool though so I am leaving it here.
     * @return
     */
    public String exportToCsv(File targetFile) {
        try {
            return SqliteExporter.exportToCsv(getWritableDatabase(), targetFile) ;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Every SQLite database file has always in it's first 16 bytes, the value: SQLite format 3\0000
     * @param dbPath The file to evaluate.
     * @return True if the file contains the proper SQLite header.
     */
    public static boolean isValidSQLite(String dbPath) {
        File file = new File(dbPath);

        if (!file.exists() || !file.canRead()) {
            return false;
        }

        try {
            FileReader fr = new FileReader(file);
            char[] buffer = new char[16];

            fr.read(buffer, 0, 16);
            String str = String.valueOf(buffer);
            fr.close();

            return str.equals("SQLite format 3\u0000");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Overwrites the existing database file with the supplied file.  <s>Should probably restart the
     * application afterwards</s>  No restart required.
     * @param file The file to overwrite with.
     * @throws IOException Typical IO stuff - if exception is raised then God only knows what hell you have wrought.
     */
    public void importDatabase(File file) throws IOException {

        // Rudimentary check of the supplied file to see if it contains the proper header to be a SQLite file.
        if (!isValidSQLite(file.getAbsolutePath())) {
            Log.w(TAG, "importDatabase: " + file.getName() + " is not a valid SQLite file");
            throw new IOException(file.getName() + " is not a valid SQLite file.");
        }

        InputStream mInput = new FileInputStream(file);
        String outFileName = file.getName();
        OutputStream mOutput = new FileOutputStream(getCurrentDbFile());
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer))>0)
        {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    /**
     * Can export a sqlite database to a csv file.
     *
     * The file has on the top dbVersion and on top of each table data the name of the table
     *
     * Inspired by
     * https://stackoverflow.com/questions/31367270/exporting-sqlite-database-to-csv-file-in-android
     * and some other SO threads as well.
     *
     */
    public static class SqliteExporter {
        private static final String TAG = SqliteExporter.class.getSimpleName();

        public static final String DB_BACKUP_DB_VERSION_KEY = "dbVersion";
        public static final String DB_BACKUP_TABLE_NAME = "table";

        /**
         * Creates a CSV file with all of the data from the SQLite db.
         * @return
         * @throws IOException
         */
        public static String exportToCsv(SQLiteDatabase db, @Nullable File targetFile) throws IOException {
            File backupFile;
            if (targetFile == null) {
                backupFile = new File(Helpers.Files.AttachmentTempFiles.getDirectory().getPath(), createBackupFileName());
            } else {
                backupFile = targetFile;
            }

            boolean success = backupFile.createNewFile();
            if(!success){
                throw new IOException("Failed to create the backup file");
            }
            List<String> tables = getTablesInDataBase(db);
            Log.d(TAG, "Started to fill the backup file in " + backupFile.getAbsolutePath());
            long starTime = System.currentTimeMillis();
            writeCsv(backupFile, db, tables);
            long endTime = System.currentTimeMillis();
            Log.d(TAG, "Creating backup took " + (endTime - starTime) + "ms.");

            return backupFile.getAbsolutePath();
        }

        /**
         * Creates a filename using the current date.
         * @return
         */
        private static String createBackupFileName(){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmm");
            return "db_backup_" + sdf.format(new Date()) + ".csv";
        }

        /**
         * Get all the table names we have in the db
         *
         * @param db
         * @return A list of table names in the database supplied.
         */
        public static List<String> getTablesInDataBase(SQLiteDatabase db){
            Cursor c = null;
            List<String> tables = new ArrayList<>();
            try{
                c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
                if (c.moveToFirst()) {
                    while ( !c.isAfterLast() ) {
                        tables.add(c.getString(0));
                        c.moveToNext();
                    }
                }
            }
            catch(Exception throwable){
                Log.e(TAG, "Could not get the table names from db", throwable);
            }
            finally{
                if(c!=null)
                    c.close();
            }
            return tables;
        }

        /**
         * Loops through each table in the database and appending its data to a CSV file.
         * @param backupFile The file to write to.
         * @param db The database to read from.
         * @param tables A list of table names in the database.
         */
        private static void writeCsv(File backupFile, SQLiteDatabase db, List<String> tables){
            CSVWriter csvWrite = null;
            Cursor curCSV = null;
            try {
                csvWrite = new CSVWriter(new FileWriter(backupFile));
                writeSingleValue(csvWrite, DB_BACKUP_DB_VERSION_KEY + "=" + db.getVersion());
                for(String table: tables){
                    writeSingleValue(csvWrite, DB_BACKUP_TABLE_NAME + "=" + table);
                    curCSV = db.rawQuery("SELECT * FROM " + table,null);
                    csvWrite.writeNext(curCSV.getColumnNames());
                    while(curCSV.moveToNext()) {
                        int columns = curCSV.getColumnCount();
                        String[] columnArr = new String[columns];
                        for( int i = 0; i < columns; i++){
                            columnArr[i] = curCSV.getString(i);
                        }
                        csvWrite.writeNext(columnArr);
                    }
                }
            }
            catch(Exception sqlEx) {
                Log.e(TAG, sqlEx.getMessage(), sqlEx);
            }finally {
                if(csvWrite != null){
                    try {
                        csvWrite.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if( curCSV != null ){
                    curCSV.close();
                }
            }
        }

        private static void writeSingleValue(CSVWriter writer, String value){
            writer.writeNext(new String[]{value});
        }
    }
}













































