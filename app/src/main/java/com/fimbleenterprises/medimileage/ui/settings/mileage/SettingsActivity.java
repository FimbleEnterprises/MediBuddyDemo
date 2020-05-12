package com.fimbleenterprises.medimileage.ui.settings.mileage;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.AdapterDbBackups;
import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.MyInterfaces;
import com.fimbleenterprises.medimileage.MyLocationService;
import com.fimbleenterprises.medimileage.MySettingsHelper;
import com.fimbleenterprises.medimileage.MySqlDatasource;
import com.fimbleenterprises.medimileage.MyYesNoDialog;
import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.RestoreDbActivity;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.DropDownPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/*import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;*/

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    public static final int REQ_PERMISSION_BACKUP = 1;
    public static final int REQ_PERMISSION_RESTORE = 0;
    public static final int RESTORE_DB_REQUEST_CODE = 100;

    public static final String BACKUP_DB_KEY = "backupdb";
    public static final String RESTORE_DB_KEY = "restoredb";
    public static final String DELETE_ALL_BACKUPS = "DELETE_ALL_BACKUPS";
    public static final String DELETE_ALL_TRIP_DATA = "DELETE_ALL_TRIP_DATA";
    public static final String DELETE_EMPTY_TRIP_DATA = "DELETE_EMPTY_TRIP_DATA";
    public static final String SUBMIT_ON_END = "SUBMIT_ON_END";
    public static final String TRIP_MINDER = "TRIP_MINDER";
    public static final String TRIP_MINDER_INTERVAL = "TRIP_MINDER_INTERVAL";

    public static String DEFAULT_DATABASE_NAME = "mileagetracking.db";
    Context context;
    MySettingsHelper options;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        this.context = this;
        options = new MySettingsHelper(context);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private static final String TAG = "SettingsFragment";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            Preference prefBackupDb;
            Preference prefRestoreBackup;
            Preference prefDeleteAllBackups;
            Preference prefDeleteAllMileageData;
            Preference prefDeleteEmptyTrips;
            Preference prefAutoSubmit;
            DropDownPreference tripMinderInterval;

            prefBackupDb = findPreference(BACKUP_DB_KEY);
            prefBackupDb.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (checkStoragePermission()) {
                        exportDB();
                    } else {
                        requestStoragePermission(REQ_PERMISSION_BACKUP);
                    }
                    return false;
                }
            });

            prefRestoreBackup = findPreference(RESTORE_DB_KEY);
            prefRestoreBackup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (checkStoragePermission()) {
                        restoreDb();
                    } else {
                        requestStoragePermission(REQ_PERMISSION_RESTORE);
                    }
                    return false;
                }
            });

            prefDeleteAllBackups = findPreference(DELETE_ALL_BACKUPS);
            prefDeleteAllBackups.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    MyYesNoDialog.showDialog(getContext(), "Are you sure you want to delete all backups?",
                            new MyYesNoDialog.YesNoListener() {
                                @Override
                                public void onYes() {
                                    int count = 0;
                                    for (File file : Environment.getExternalStorageDirectory().listFiles()) {
                                        if (file.getName().endsWith(".db")) {
                                            if (file.delete()) {
                                                count++;
                                            }
                                        }
                                    }
                                    Toast.makeText(getContext(), "Deleted " + count + " backups.", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onNo() {
                                    Log.i(TAG, "onNo ");
                                }
                            });
                    return false;
                }
            });

            prefDeleteAllMileageData = findPreference(DELETE_ALL_TRIP_DATA);
            prefDeleteAllMileageData.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    MyYesNoDialog.showDialog(getContext(), "Are you sure you want to delete all mileage data?",
                            new MyYesNoDialog.YesNoListener() {
                                @Override
                                public void onYes() {
                                    MySqlDatasource ds = new MySqlDatasource();
                                    if (ds.deleteAllTripData()) {
                                        Toast.makeText(getContext(), "Done"
                                                , Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onNo() {

                                }
                            });
                    return false;
                }
            });

            prefDeleteEmptyTrips = findPreference(DELETE_EMPTY_TRIP_DATA);
            prefDeleteEmptyTrips.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                MyYesNoDialog.showDialog(getContext(), "Are you sure you want to delete all empty trips?",
                    new MyYesNoDialog.YesNoListener() {
                        @Override
                        public void onYes() {
                            MySqlDatasource datasource = new MySqlDatasource();
                            datasource.deleteEmptyTrips(false, new MyInterfaces.TripDeleteCallback() {
                                @Override
                                public void onSuccess(int entriesDeleted) {
                                    Toast.makeText(getContext(), "Removed " + entriesDeleted + " empty trips.", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(String message) {
                                    Toast.makeText(getContext(), "Failed to delete empty trips\n" + message, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onNo() {

                        }
                    });
                return false;
                }
            });

            // We do not want to backup or restore the db while a trip is running.
            if (MyLocationService.isRunning) {
                prefBackupDb.setEnabled(false);
                prefRestoreBackup.setEnabled(false);
                prefDeleteAllMileageData.setEnabled(false);
                prefDeleteEmptyTrips.setEnabled(false);
            }

        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                               @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            switch (requestCode) {
                case REQ_PERMISSION_BACKUP: {
                    if (checkStoragePermission()) {
                        exportDB();
                    }
                    break;
                }
                case REQ_PERMISSION_RESTORE :
                    if (checkStoragePermission()) {
                        restoreDb();
                    }
                    break;
            }

        }

        void requestStoragePermission(int requestCode) {
            if (!checkStoragePermission()) {
                this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
                return;
            }
        }

        public boolean checkStoragePermission() {
            String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            int res = getContext().checkCallingOrSelfPermission(permission);
            return (res == PackageManager.PERMISSION_GRANTED);
        }

        public void restoreDb() {
            Log.i(TAG, "restoreDb ");
            Intent i = new Intent(getContext(), RestoreDbActivity.class);
            startActivityForResult(i, RESTORE_DB_REQUEST_CODE);
        }

        public void exportDB(){
            try {
                // File sd = Environment.getExternalStorageDirectory();
                SQLiteDatabase database = new MySqlDatasource().getDatabase();

                if (Helpers.Files.getBackupDirectory().canWrite()) {
                    Log.d("TAG", "DatabaseHandler: can write in sd");
                    String currentDBPath = database.getPath();
                    String copieDBPath = System.currentTimeMillis() + ".db";
                    File currentDB = new File(currentDBPath);
                    File copieDB = new File(Helpers.Files.getBackupDirectory(), copieDBPath);
                    if (currentDB.exists()) {
                        Log.d("TAG", "DatabaseHandler: DB exist");
                        @SuppressWarnings("resource")
                        FileChannel src = new FileInputStream(currentDB).getChannel();
                        @SuppressWarnings("resource")
                        FileChannel dst = new FileOutputStream(copieDB).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        src.close();
                        dst.close();
                        if (copieDB.exists() && copieDB.length() > 0) {
                            Toast.makeText(getContext(), "Database was backed up.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to backup database.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } catch  (Exception e) {
                e.printStackTrace();
            }

        }
    }


}