package com.fimbleenterprises.medimileage.ui.settings;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.objects_and_containers.AccountAddresses;
import com.fimbleenterprises.medimileage.Crm;
import com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities;
import com.fimbleenterprises.medimileage.fullscreen_pickers.FullscreenActivityChooseRep;
import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.objects_and_containers.MediUser;
import com.fimbleenterprises.medimileage.objects_and_containers.MileBuddyMetrics;
import com.fimbleenterprises.medimileage.objects_and_containers.MileBuddyUpdate;
import com.fimbleenterprises.medimileage.MyInterfaces;
import com.fimbleenterprises.medimileage.services.MyLocationService;
import com.fimbleenterprises.medimileage.dialogs.MyProgressDialog;
import com.fimbleenterprises.medimileage.MyPreferencesHelper;
import com.fimbleenterprises.medimileage.MySqlDatasource;
import com.fimbleenterprises.medimileage.dialogs.MyYesNoDialog;
import com.fimbleenterprises.medimileage.CrmQueries;
import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.objects_and_containers.Requests;
import com.fimbleenterprises.medimileage.objects_and_containers.UserAddresses;
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
    public static MyPreferencesHelper options;

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
    public static final String SET_DEFAULTS = "SET_DEFAULTS";
    public static final String SERVER_BASE_URL = "SERVER_BASE_URL";
    public static final String OPPORTUNITIES_KEY = "updateOpportunities";
    public static final String DEFAULT_SEARCH_PAGE = "DEFAULT_SEARCH_PAGE";
    public static final String SHOW_USAGE_MENU_ITEM = "SHOW_USAGE_MENU_ITEM";

    public static String DEFAULT_DATABASE_NAME = "mileagetracking.db";

    // if this activity is started with an intent extra representing an existing preference then we will scroll to that preference after preferences are loaded.
    public static Preference initialScrollToPreference;
    public static String PREF_INTENT_TAG = "PREFERENCE";

    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        // Log a metric
        MileBuddyMetrics.updateMetric(this, MileBuddyMetrics.MetricName.LAST_ACCESSED_APP_SETTINGS, DateTime.now());

        this.context = this;
        options = new MyPreferencesHelper(context);

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

            final Intent intent = getActivity().getIntent();
            if (intent != null) {
                String string = intent.getStringExtra(PREF_INTENT_TAG);
                if (string != null) {
                    scrollToPreference(string);
                }
            }

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
            Preference prefSetDefaults;

            final Preference prefSetServerBaseUrl;
            Preference prefUpdateOpportunities;

            prefUpdateOpportunities = findPreference(OPPORTUNITIES_KEY);
            prefUpdateOpportunities.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    retrieveAndSaveOpportunities();
                    return true;
                }
            });

            prefSetDefaults = findPreference(SET_DEFAULTS);
            prefSetDefaults.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    MyYesNoDialog.show(getContext(), new MyYesNoDialog.YesNoListener() {
                        @Override
                        public void onYes() {
                            String cachedUsername = options.getCachedUsername();
                            String cachedPassword = options.getCachedPassword();
                            options.setDefaults();
                            options.setCachedUsername(cachedUsername);
                            options.setCachedPassword(cachedPassword);
                            Toast.makeText(getContext(), "Defaults set!", Toast.LENGTH_SHORT).show();
                            Helpers.Application.restartApplication(getContext());
                        }

                        @Override
                        public void onNo() {
                            return;
                        }
                    });
                    return false;
                }
            });

            prefSetServerBaseUrl = findPreference(SERVER_BASE_URL);
            prefSetServerBaseUrl.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    // Check if the user left it blank - if so then change that blank value to the
                    // default value
                    if (newValue.toString().length() == 0) {
                        newValue = requireContext().getString(R.string.default_base_server_url);
                    }

                    final String newValueFinal = newValue.toString();

                    final MyProgressDialog dialog = new MyProgressDialog(getContext(), getString(R.string.validate_server_url_progress_msg));
                    dialog.show();
                    final String originalValue = options.getServerBaseUrl();
                    options.setServerBaseUrl(newValue.toString());
                    Requests.Argument argument = new Requests.Argument("query", CrmQueries.Utility.getMyUser());
                    ArrayList<Requests.Argument> args = new ArrayList<>();
                    args.add(argument);
                    Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);
                    Crm crm = new Crm();
                    crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            Toast.makeText(getContext(), getString(R.string.server_url_validated_successfully), Toast.LENGTH_SHORT).show();
                            reloadAndScrollTo(SERVER_BASE_URL);
                            dialog.dismiss();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            options.setServerBaseUrl(originalValue);
                            Toast.makeText(getContext(), getString(R.string.server_url_validated_unsuccessfully), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });
                    return false;
                }
            });

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
                            Intent intent = new Intent(getContext(), FullscreenActivityChooseRep.class);
                            intent.putExtra(FullscreenActivityChooseRep.CURRENT_VALUE, MediUser.getMe());
                            startActivity(intent);
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
                    reloadAndScrollTo(EXPLICIT_MODE);
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

        private void reloadAndScrollTo(String prefName) {
            Log.i(TAG, "reloadAndScrollTo Reloading preferences and then (hopefully) scrolling to: " + prefName);
            Intent i = new Intent(getContext(), getActivity().getClass());
            i.putExtra(PREF_INTENT_TAG, prefName);
            getActivity().finish();
            startActivity(i);
        }

        public void retrieveAndSaveOpportunities() {
            final MyProgressDialog dialog = new MyProgressDialog(getContext(), "Retrieving opportunities...");
            dialog.show();
            String query = CrmQueries.Opportunities.getOpportunitiesByTerritory(MediUser.getMe().territoryid);
            ArrayList<Requests.Argument> args = new ArrayList<>();
            Requests.Argument argument = new Requests.Argument("query", query);
            args.add(argument);
            Requests.Request request = new Requests.Request(Requests.Request.GET, args);
            Crm crm = new Crm();
            crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    dialog.dismiss();
                    String response = new String(responseBody);
                    CrmEntities.Opportunities opportunities = new CrmEntities.Opportunities(response);
                    opportunities.save();
                    CrmEntities.Opportunities savedOpportunities = options.getSavedOpportunities();
                    Log.i(TAG, "onSuccess " + response);
                    Toast.makeText(getContext(), opportunities.toString() + " were saved", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    dialog.dismiss();
                    Log.w(TAG, "onFailure: " + error.getLocalizedMessage());
                    Toast.makeText(getContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });
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
            findPreference(OPPORTUNITIES_KEY).setTitle(getString(R.string.update_opportunities_explicit));

            findPreference(DELETE_ALL_TRIP_DATA).setTitle(R.string.settings_delete_mileage_explicit);

            findPreference(DELETE_EMPTY_TRIP_DATA).setTitle(R.string.settings_delete_empty_explicit);

            findPreference(DISTANCE_THRESHOLD).setTitle(R.string.settings_prompt_opportunities_explicit);

            findPreference(DELETE_LOCAL_UPDATES).setTitle(R.string.settings_delete_local_updates_explicit);

            findPreference(CHECK_FOR_UPDATES).setTitle(R.string.settings_check_updates_explicit);

            findPreference(DEBUG_MODE).setTitle(R.string.settings_debug_explicit);

            findPreference(EXPLICIT_MODE).setTitle(R.string.settings_explicit_explicit);

            findPreference(SET_DEFAULTS).setTitle(getString(R.string.set_defaults_explicit));
            findPreference(SET_DEFAULTS).setSummary(getString(R.string.set_defaults_description_explicit));

            findPreference(SERVER_BASE_URL).setTitle(getString(R.string.change_server_addy_explicit));
            findPreference(SERVER_BASE_URL).setSummary(getString(R.string.change_server_addy_description_explicit));

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
            String query = CrmQueries.Users.getUser(email);
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
                        MediUser user = MediUser.createOne(strResponse);
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