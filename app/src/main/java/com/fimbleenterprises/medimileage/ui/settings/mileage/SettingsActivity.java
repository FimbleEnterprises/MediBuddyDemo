package com.fimbleenterprises.medimileage.ui.settings.mileage;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.AccountAddresses;
import com.fimbleenterprises.medimileage.Crm;
import com.fimbleenterprises.medimileage.CrmEntities;
import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.MediUser;
import com.fimbleenterprises.medimileage.MileBuddyMetrics;
import com.fimbleenterprises.medimileage.MileBuddyUpdate;
import com.fimbleenterprises.medimileage.MyInterfaces;
import com.fimbleenterprises.medimileage.MyLocationService;
import com.fimbleenterprises.medimileage.MyProgressDialog;
import com.fimbleenterprises.medimileage.MySettingsHelper;
import com.fimbleenterprises.medimileage.MySqlDatasource;
import com.fimbleenterprises.medimileage.MyYesNoDialog;
import com.fimbleenterprises.medimileage.Queries;
import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.Requests;
import com.fimbleenterprises.medimileage.RestResponse;
import com.fimbleenterprises.medimileage.UserAddresses;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.joda.time.DateTime;

import cz.msebera.android.httpclient.Header;

/*import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;*/

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    public static MySettingsHelper options;

    public static final int REQ_PERMISSION_BACKUP = 1;
    public static final int REQ_PERMISSION_RESTORE = 0;
    public static final int RESTORE_DB_REQUEST_CODE = 100;

    public static final String BACKUP_DB_KEY = "backupdb";
    public static final String RESTORE_DB_KEY = "restoredb";
    public static final String DELETE_ALL_BACKUPS = "DELETE_ALL_BACKUPS";
    public static final String DELETE_ALL_TRIP_DATA = "DELETE_ALL_TRIP_DATA";
    public static final String DELETE_EMPTY_TRIP_DATA = "DELETE_EMPTY_TRIP_DATA";
    public static final String UPDATE_USER_ADDYS = "updateUserAddys";
    public static final String UPDATE_ACT_ADDYS = "updateActAddys";
    public static final String DELETE_LOCAL_UPDATES = "DELETE_LOCAL_UPDATES";
    public static final String GOTO_PERMISSIONS = "GOTO_PERMISSIONS";
    public static final String SUBMIT_ON_END = "SUBMIT_ON_END";
    public static final String TRIP_MINDER = "TRIP_MINDER";
    public static final String TRIP_MINDER_INTERVAL = "TRIP_MINDER_INTERVAL";
    public static final String IS_SHOWING_MTD_REIMBURSEMENT = "IS_SHOWING_MTD_REIMBURSEMENT";
    public static final String EXPERIMENTAL_FUNCTION = "EXPERIMENTAL_FUNCTION";
    public static final String UPDATE_USER_INFO = "updateUserInfo";
    public static final String NAME_TRIP_ON_START = "NAME_TRIP_ON_START";
    public static final String CONFIRM_END = "CONFIRM_END";
    public static final String RECEIPT_FORMATS = "RECEIPT_FORMATS";
    public static final String DISTANCE_THRESHOLD = "DISTANCE_THRESHOLD";
    public static final String CHECK_FOR_UPDATES = "CHECK_FOR_UPDATES";
    public static final String DEBUG_MODE = "DEBUG_MODE";
    public static final String EXPLICIT_MODE = "EXPLICIT_MODE";

    public static String DEFAULT_DATABASE_NAME = "mileagetracking.db";
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        // Log a metric
        MileBuddyMetrics.updateMetric(this, MileBuddyMetrics.MetricName.LAST_ACCESSED_APP_SETTINGS, DateTime.now());

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

            if (options.isExplicitMode()) {
                makeExplicit();
            }

            Preference prefBackupDb;
            Preference prefRestoreBackup;
            Preference prefDeleteAllBackups;
            Preference prefDeleteAllMileageData;
            Preference prefDeleteEmptyTrips;
            Preference prefUpdateUserAddys;
            Preference prefUpdateActAddys;
            Preference prefDeleteAllLocalUpdates;
            Preference prefGoToPermissions;
            Preference prefExperimentalFunction;
            Preference prefUpdateMyUserInfo;
            Preference prefExplicitMode;

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
                        // restoreDb();
                    } else {
                        requestStoragePermission(REQ_PERMISSION_RESTORE);
                    }
                    return false;
                }
            });

            prefUpdateUserAddys = findPreference(UPDATE_USER_ADDYS);
            prefUpdateUserAddys.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final MyProgressDialog dialog = new MyProgressDialog(getContext(), "Updating user addresses...");
                    dialog.show();
                    UserAddresses.getAllUserAddysFromCrm(getContext(), new MyInterfaces.GetUserAddysListener() {
                        @Override
                        public void onSuccess(UserAddresses addresses) {
                            Log.i(TAG, "onSuccess Got accounts!");
                            addresses.save();
                            dialog.dismiss();
                            Toast.makeText(getContext(), "User addresses were updated.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(String msg) {
                            Log.w(TAG, "onFailure: " + msg);
                            dialog.dismiss();
                            Toast.makeText(getContext(), "Failed to update addresses", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return false;
                }
            });

            prefUpdateActAddys = findPreference(UPDATE_ACT_ADDYS);
            prefUpdateActAddys.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final MyProgressDialog dialog = new MyProgressDialog(getContext(), "Updating account addresses...");
                    dialog.show();
                    AccountAddresses.getFromCrm(getContext(), new MyInterfaces.GetAccountsListener() {
                        @Override
                        public void onSuccess(AccountAddresses accounts) {
                            Log.i(TAG, "onSuccess Got accounts!");
                            accounts.save();
                            dialog.dismiss();
                            Toast.makeText(getContext(), "Account addresses were updated.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(String msg) {
                            Log.w(TAG, "onFailure: " + msg);
                            dialog.dismiss();
                            Toast.makeText(getContext(), "Failed to update addresses", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return false;
                }
            });

            prefDeleteAllBackups = findPreference(DELETE_ALL_BACKUPS);
            prefDeleteAllBackups.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    MyYesNoDialog.show(getContext(), "Are you sure you want to delete all backups?",
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
                    MyYesNoDialog.show(getContext(), "Are you sure you want to delete all mileage data?",
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

            prefGoToPermissions = findPreference(GOTO_PERMISSIONS);
            prefGoToPermissions.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                Helpers.Application.openAppSettings(getContext());
                return false;
                }
            });

            prefDeleteEmptyTrips = findPreference(DELETE_EMPTY_TRIP_DATA);
            prefDeleteEmptyTrips.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                MyYesNoDialog.show(getContext(), "Are you sure you want to delete all empty trips?",
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

            prefDeleteAllLocalUpdates = findPreference(DELETE_LOCAL_UPDATES);
            prefDeleteAllLocalUpdates.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (checkStoragePermission()) {
                        try {
                            MileBuddyUpdate.deleteAllLocallyAvailableUpdates();
                            Toast.makeText(getContext(), "Deleted local backups", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Failed to delete local backups!\n"
                                    + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        requestStoragePermission(REQ_PERMISSION_RESTORE);
                    }
                    return false;
                }
            });

            prefUpdateMyUserInfo = findPreference(UPDATE_USER_INFO);
            prefUpdateMyUserInfo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    getUser(MediUser.getMe().email);
                    return false;
                }
            });

            prefExperimentalFunction = findPreference(EXPERIMENTAL_FUNCTION);
            prefExperimentalFunction.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    MyYesNoDialog.show(getContext(), "I don't even know what will run when you click, \"Yes\"\n\nAre you sure?", new MyYesNoDialog.YesNoListener() {
                        @Override
                        public void onYes() {
                            String query = Queries.OrderLines.getOrderLines(MediUser.getMe().systemuserid, Queries.Operators.DateOperator.THIS_MONTH);
                            Crm crm = new Crm();
                            ArrayList<Requests.Argument> arguments = new ArrayList<>();
                            Requests.Argument argument = new Requests.Argument("query", query);
                            arguments.add(argument);
                            Requests.Request request = new Requests.Request(Requests.Request.Function.GET, arguments);
                            crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    String response = new String(responseBody);
                                    CrmEntities.OrderProducts products = new CrmEntities.OrderProducts(response);
                                    Log.i(TAG, "onSuccess: Count: " + products.size());
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                    Toast.makeText(getContext(), "Error: " + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                            Toast.makeText(getContext(), "Made a " + query.length() + " character query!  Boring, I know.", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "onYes: Query: " + query);
                        }

                        @Override
                        public void onNo() {
                            Toast.makeText(getContext(), "Probably wise to say, \"No\"...", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return false;
                }
            });

            prefExplicitMode = findPreference(EXPLICIT_MODE);
            prefExplicitMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    getActivity().finish();
                    getActivity().startActivity(new Intent(getContext(), SettingsActivity.class));
                    return true;
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
                        //restoreDb();
                    }
                    break;
            }

        }

        public void makeExplicit() {
            findPreference(NAME_TRIP_ON_START).setTitle(R.string.settings_name_trips_explicit);
            findPreference(SUBMIT_ON_END).setTitle(R.string.settings_auto_submit_explicit);
            findPreference(CONFIRM_END).setTitle(R.string.settings_confirmend_explicit);
            findPreference(TRIP_MINDER).setTitle(R.string.settings_auto_stop_explicit);
            findPreference(TRIP_MINDER_INTERVAL).setTitle(R.string.settings_auto_stop_interval_explicit);
            findPreference(RECEIPT_FORMATS).setTitle(R.string.settings_receipt_format_explicit);
            findPreference(GOTO_PERMISSIONS).setTitle(R.string.settings_permissions_explicit);

            findPreference(UPDATE_USER_INFO).setTitle(R.string.settings_update_me_explicit);
            findPreference(UPDATE_USER_ADDYS).setTitle(R.string.settings_update_addresses_explicit);
            findPreference(UPDATE_ACT_ADDYS).setTitle(R.string.settings_update_account_addresses_explicit);

            findPreference(DELETE_ALL_TRIP_DATA).setTitle(R.string.settings_delete_mileage_explicit);
            findPreference(DELETE_EMPTY_TRIP_DATA).setTitle(R.string.settings_delete_empty_explicit);

            findPreference(DISTANCE_THRESHOLD).setTitle(R.string.settings_prompt_opportunities_explicit);
            findPreference(DELETE_LOCAL_UPDATES).setTitle(R.string.settings_delete_local_updates_explicit);
            findPreference(CHECK_FOR_UPDATES).setTitle(R.string.settings_check_updates_explicit);
            findPreference(DEBUG_MODE).setTitle(R.string.settings_debug_explicit);
            findPreference(EXPLICIT_MODE).setTitle(R.string.settings_explicit_explicit);

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

        public void exportDB(){
            try {
                // File sd = Environment.getExternalStorageDirectory();
                SQLiteDatabase database = new MySqlDatasource().getDatabase();

                if (Helpers.Files.getAppDirectory().canWrite()) {
                    Log.d("TAG", "DatabaseHandler: can write in sd");
                    String currentDBPath = database.getPath();
                    String copieDBPath = System.currentTimeMillis() + ".db";
                    File currentDB = new File(currentDBPath);
                    File copieDB = new File(Helpers.Files.getAppDirectory(), copieDBPath);
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

        public void getUser(String email) {
            String query = Queries.Users.getUser(email);
            Requests.Request request = new Requests.Request(Requests.Request.Function.GET);
            request.arguments.add(new Requests.Argument(null, query));
            Crm crm = new Crm();
            final MyProgressDialog progressDialog = new MyProgressDialog(getContext(), "Getting your user information...");
            progressDialog.show();

            try {
                crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          byte[] responseBody) {
                        progressDialog.dismiss();
                        String strResponse = new String(responseBody);
                        RestResponse response = new RestResponse(strResponse);
                        MediUser user = new MediUser(response);
                        user.save(getContext());
                        MySqlDatasource db = new MySqlDatasource(getContext());
                        Log.d(TAG, "onSuccess " + strResponse);
                        Toast.makeText(getContext(), "Successfully updated user.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          byte[] responseBody, Throwable error) {
                        Log.w(TAG, "onFailure: " + error.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Failed to get user information\n" + error.getMessage()
                                , Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Log.w(TAG, "onClick: " + e.getMessage());
                progressDialog.dismiss();
            }
        }
    }


}