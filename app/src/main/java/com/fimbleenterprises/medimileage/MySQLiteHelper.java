package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Locale;

@SuppressWarnings("WeakerAccess")
public class MySQLiteHelper extends SQLiteOpenHelper {

    private final static String TAG = "MySQLiteHelper.";
    public static final String DATABASE_NAME = "mileagetracking.db";
    private static final int DATABASE_VERSION = 3;
    private MySettingsHelper options;

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
    public static final String COLUMN_IS_ME = "isme";

    // Settings table
    public static final String TABLE_SETTINGS = "settings";
    public static final String COLUMN_SETTING_NAME = "name";
    public static final String COLUMN_SETTING_INT_VALUE = "intvalue";
    public static final String COLUMN_SETTING_STRING_VALUE = "stringvalue";
    public static final String COLUMN_SETTING_BOOL_VALUE = "boolvalue";

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
        COLUMN_TRIP_GUID
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
    private static final String USER_TABLE_CREATE = "create table " + TABLE_USERS + "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_JSON + " text, " +
            COLUMN_SYSTEMUSERID + " text, " +
            COLUMN_IS_ME + " integer);";    // Database creation sql statement

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
            COLUMN_TRIP_GUID + " text" + ", " +
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
        options = new MySettingsHelper(context);
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
                Log.e( "createTable", "Failed to create the '" + tableName + "' table");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean tableExists(String tableName, SQLiteDatabase mDatabase) {
        Cursor cursor = mDatabase.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name ='" + tableName + "'", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
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
    private boolean columnExists(String columnName, String tableName, SQLiteDatabase db)
    {
        boolean isExist = false;
        Cursor res = db.rawQuery("PRAGMA table_info("+tableName+")",null);
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
            if (! this.tableExists(TABLE_USERS, db)) {
                Log.e(TAG + "onOpen", "The currentLocations table does not exist.  Will try to create it now.");
               createTable(db, USER_TABLE_CREATE, TABLE_USERS);
            } else {
                Log.d(TAG + "onOpen", "The users table exists.  No need to create it.");
            }

            if (! this.tableExists(TABLE_FULL_TRIP, db)) {
                Log.e(TAG + "onOpen", "The currentLocations table does not exist.  Will try to create it now.");
               createTable(db, FULL_TRIP_TABLE_CREATE, TABLE_FULL_TRIP);
            } else {
                Log.d(TAG + "onOpen", "The full trip table exists.  No need to create it.");
            }

            if (! this.tableExists(TABLE_TRIP_ENTRIES, db)) {
                Log.e(TAG + "onOpen", "The trip details table does not exist.  Will try to create it now.");
               createTable(db, TRIP_ENTRIES_TABLE_CREATE, TABLE_TRIP_ENTRIES);
            } else {
                Log.d(TAG + "onOpen", "The trip details table exists.  No need to create it.");
            }

            if (! this.tableExists(TABLE_CURRENT_LOCATIONS, db)) {
                Log.e(TAG + "onOpen", "The currentLocations table does not exist.  Will try to create it now.");
               createTable(db, CURRENTLOCATIONS_TABLE_CREATE, TABLE_CURRENT_LOCATIONS);
            } else {
                Log.d(TAG + "onOpen", "The currentLocations table exists.  No need to create it.");
            }

            if (! this.tableExists(TABLE_SETTINGS, db)) {
                Log.e(TAG + "onOpen", "The settings table does not exist.  Will try to create it now.");
                createTable(db, SETTINGS_TABLE_CREATE, TABLE_SETTINGS);
            } else {
                Log.d(TAG + "onOpen", "The settings table exists.  No need to create it.");
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
        
        // Users table
        addColumnIfMissing(TABLE_USERS, COLUMN_ID, TYPE_INTEGER, db);
        addColumnIfMissing(TABLE_USERS, COLUMN_JSON, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_USERS, COLUMN_SYSTEMUSERID, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_USERS, COLUMN_IS_ME, TYPE_INTEGER, db);

        // Settings table
        addColumnIfMissing(TABLE_SETTINGS, COLUMN_ID, TYPE_INTEGER, db);
        addColumnIfMissing(TABLE_SETTINGS, COLUMN_SETTING_NAME, TYPE_TEXT, db);
        addColumnIfMissing(TABLE_SETTINGS, COLUMN_SETTING_BOOL_VALUE, TYPE_INTEGER, db);
        addColumnIfMissing(TABLE_SETTINGS, COLUMN_SETTING_INT_VALUE, TYPE_INTEGER, db);
        addColumnIfMissing(TABLE_SETTINGS, COLUMN_SETTING_STRING_VALUE, TYPE_TEXT, db);
        
    }

    private void addColumnIfMissing(String tableName, String columnName, String dataType, SQLiteDatabase db) {
        if ( ! columnExists(columnName, tableName, db)) {
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













































}

