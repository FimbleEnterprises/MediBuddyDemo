package com.fimbleenterprises.medimileage.activities.ui.settings;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.MySQLiteHelper;
import com.fimbleenterprises.medimileage.activities.QuoteActivity;
import com.fimbleenterprises.medimileage.dialogs.MonthYearPickerDialog;
import com.fimbleenterprises.medimileage.objects_and_containers.AccountAddresses;
import com.fimbleenterprises.medimileage.Crm;
import com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities;
import com.fimbleenterprises.medimileage.dialogs.fullscreen_pickers.FullscreenActivityChooseRep;
import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.objects_and_containers.FullTrip;
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
import com.jaiselrahman.filepicker.activity.FilePickerActivity;
import com.jaiselrahman.filepicker.model.MediaFile;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.joda.time.DateTime;

import cz.msebera.android.httpclient.Header;
import ir.esfandune.filepickerDialog.ui.PickerDialog;

/*import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;*/

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    public static MyPreferencesHelper options;
    public static AppCompatActivity activity;

    public static final int REQ_PERMISSION_BACKUP = 1;
    public static final int REQ_PERMISSION_RESTORE = 0;
    public static final int RESTORE_DB_REQUEST_CODE = 100;
    private static final int FILE_REQUEST_CODE = 77788;

    public static final String SHARE_DB_KEY = "backupdb";
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
    public static final String MASQUERADE_AS = "MASQUERADE_AS";
    public static final String CREATE_RECEIPT_FOR_USER = "CREATE_RECEIPT_FOR_USER";
    public static final String EXPORT_DB = "SHARE_DB";
    public static final String GET_DB_SIZE = "GET_DB_SIZE";

    public static String DEFAULT_DATABASE_NAME = "mileagetracking.db";

    // if this activity is started with an intent extra representing an existing preference then we will scroll to that preference after preferences are loaded.
    public static Preference initialScrollToPreference;
    public static String PREF_INTENT_TAG = "PREFERENCE";
    public static final int MAKE_RECEIPT_FOR_USER_CODE = 556;
    public static final int MASQUERADE_AS_REQUEST = 888;

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

        this.activity = this;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.hasExtra(FullscreenActivityChooseRep.CHOICE_RESULT) && requestCode == FullscreenActivityChooseRep.REQUESTCODE) {
            MediUser chosenUser = data.getParcelableExtra(FullscreenActivityChooseRep.CHOICE_RESULT);
            chosenUser.isMasquerading = true;
            try {
                chosenUser.save(getApplicationContext());
                Toast.makeText(context, "You are now masquerading as: " + chosenUser.fullname, Toast.LENGTH_SHORT).show();
                MySqlDatasource ds = new MySqlDatasource();
                // ds.deleteAllTripData();
            } catch (Exception e) {
                Toast.makeText(context, "Failed to masquerade!\n" + e.getLocalizedMessage() , Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        if (data != null && data.hasExtra(FullscreenActivityChooseRep.CHOICE_RESULT) && requestCode == MAKE_RECEIPT_FOR_USER_CODE) {
            MediUser chosenUser = data.getParcelableExtra(FullscreenActivityChooseRep.CHOICE_RESULT);
            Toast.makeText(context, "Make receipt for " + chosenUser.fullname, Toast.LENGTH_SHORT).show();
            showReceiptMonthPicker(chosenUser);
        }

        if (requestCode == FILE_REQUEST_CODE
                && resultCode == RESULT_OK
                && data != null) {
            List<MediaFile> files = data.<MediaFile>getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES);
            if(files != null && files.size() > 0) {
                MediaFile mediaFile = files.get(0);
                File file = new File(mediaFile.getPath());
                Toast.makeText(activity, "Name: " + file.getName(), Toast.LENGTH_SHORT).show();
            }
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

            Preference prefShareDatabase;
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
            Preference prefMasqueradeAs;
            Preference prefCreateReceiptFor;
            Preference prefExportDb;
            Preference prefGetDbSize;

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

            prefShareDatabase = findPreference(SHARE_DB_KEY);
            prefShareDatabase.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (checkStoragePermission()) {
                        MySQLiteHelper sqLiteHelper = new MySQLiteHelper(getContext());
                        File backupFile = new File(Helpers.Files.AttachmentTempFiles.getDirectory().getAbsolutePath(), "backupdb.png");
                        sqLiteHelper.exportDB(backupFile, getContext());
                    } else {
                        requestStoragePermission(REQ_PERMISSION_BACKUP);
                    }
                    return false;
                }
            });

            prefExportDb = findPreference(EXPORT_DB);
            prefExportDb.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (checkStoragePermission()) {
                        MySQLiteHelper sqLiteHelper = new MySQLiteHelper(getContext());
                        File backupFile = new File(Helpers.Files.AttachmentTempFiles.getDirectory().getAbsolutePath(), "backupdb.png");
                        sqLiteHelper.exportDB(backupFile, getContext());
                    } else {
                        requestStoragePermission(REQ_PERMISSION_BACKUP);
                    }
                    return false;
                }
            });

            prefGetDbSize = findPreference(GET_DB_SIZE);
            final File file = MySQLiteHelper.getCurrentDbFile();
            final float fltSize = Helpers.Files.convertBytesToMb(file.length(), true);
            prefGetDbSize.setSummary(fltSize + " MB");
            prefGetDbSize.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Toast.makeText(getContext(), Helpers.Files.convertBytesToMb(file.length(), true) + " MB", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });

            prefRestoreBackup = findPreference(RESTORE_DB_KEY);
            prefRestoreBackup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (checkStoragePermission()) {

                        PickerDialog.FilePicker(activity).onFileSelect(new PickerDialog.FileClickListener() {
                            @Override
                            public void onFileClicked(File clickedFile) {
                                MySQLiteHelper sqLiteHelper = new MySQLiteHelper(getContext());
                                try {
                                    // Added in 1.88 - a provisional version created for Eoin to troubleshoot a trip creation/submission problem.
                                    // Actually overwrites the db - no restart required - scary but... effective?
                                    sqLiteHelper.importDatabase(clickedFile);
                                    Toast.makeText(getContext(), "I mean... I think it worked...", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }
                        });

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

            prefMasqueradeAs = findPreference(MASQUERADE_AS);
            if (MediUser.getMe().email.equals("matt.weber@medistimusa.com") || MediUser.getMe().isMasquerading) {
                prefMasqueradeAs.setEnabled(true);
            } else {
                prefMasqueradeAs.setEnabled(false);
            }
            prefMasqueradeAs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // Show the rep picker.  Obtain the result by overriding onActivityResult()
                    FullscreenActivityChooseRep.showRepChooser(getActivity(), MediUser.getMe(), MASQUERADE_AS_REQUEST);
                    return false;
                }
            });

            prefCreateReceiptFor = findPreference(CREATE_RECEIPT_FOR_USER);
            prefCreateReceiptFor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    FullscreenActivityChooseRep.showRepChooser(getActivity(), MediUser.getMe(), MAKE_RECEIPT_FOR_USER_CODE);
                    return false;
                }
            });

            prefExportDb = findPreference(EXPORT_DB);
            prefShareDatabase.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    return false;
                }
            });

            // We do not want to backup or restore the db while a trip is running.
            if (MyLocationService.isRunning) {
                prefShareDatabase.setEnabled(false);
                prefRestoreBackup.setEnabled(false);
                prefDeleteAllMileageData.setEnabled(false);
                prefDeleteEmptyTrips.setEnabled(false);
            }

        }

        protected void restoreDb() {

        }

        protected void testPdf() {
            Toast.makeText(getContext(), "Fuck you!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), QuoteActivity.class);
            startActivity(intent);
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
                        MySQLiteHelper sqLiteHelper = new MySQLiteHelper(getContext());
                        File backupFile = new File(Helpers.Files.AttachmentTempFiles.getDirectory().getAbsolutePath(), "backupdb.png");
                        File db = sqLiteHelper.exportDB(backupFile, getContext());
                        MediUser me = MediUser.getMe();
                        String[] recips = new String[1];
                        recips[0] = "matt.weber@medistimusa.com";
                        Helpers.Email.sendEmail(recips, "MileageDB", "MileageDb"
                                , activity, db, false);
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

    void showReceiptDialog() {
        // showReceiptDialog(MediUser.getMe().systemuserid);
    }

    /*void showReceiptDialog(String userid) {
        final Dialog dialog = new Dialog(context);
        final Context c = context;
        dialog.setContentView(R.layout.make_receipt);
        dialog.setCancelable(true);
        Button btnThisMonth = dialog.findViewById(R.id.btnThisMonth);
        Button btnLastMonth = dialog.findViewById(R.id.btnLastMonth);
        Button btnChoose = dialog.findViewById(R.id.btnChooseMonth);

        btnThisMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doActualReceipt(DateTime.now().getMonthOfYear(), DateTime.now().getYear());
                dialog.dismiss();
            }
        });
        btnLastMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateTime lastMonth = DateTime.now().minusMonths(1);
                doActualReceipt(lastMonth.getMonthOfYear(), lastMonth.getYear());
                dialog.dismiss();
            }
        });
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showReceiptMonthPicker();
                dialog.dismiss();
            }
        });
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                    return true;
                } else {
                    return false;
                }
            }
        });
        dialog.show();
    }*/

    @SuppressLint("NewApi")
    private void showReceiptMonthPicker(final MediUser onBehalfOfUser) {
        MonthYearPickerDialog mpd = new MonthYearPickerDialog();
        mpd.setListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                if (month >= 1 && month <= 12) {
                    makeReceiptOnBehalfOf(onBehalfOfUser, month, year);
                }
            }
        });
        mpd.show(getSupportFragmentManager(), "MonthYearPickerDialog");
    }

    void doActualReceipt(File receipt) {

        // Log a metric
        MileBuddyMetrics.updateMetric(context, MileBuddyMetrics.MetricName
                .LAST_ACCESSED_GENERATE_RECEIPT, DateTime.now());

        if (receipt != null) {
            MediUser me = MediUser.getMe();
            String[] recips = new String[1];
            recips[0] = me.email;
            Helpers.Email.sendEmail(recips, getString(R.string.receipt_mail_body),
                    getString(R.string.receipt_mail_subject_preamble), context, receipt, false);
        } else {
            Toast.makeText(context, "No trips found for that month/year."
                    , Toast.LENGTH_SHORT).show();
        }
    }

    public File makeReceiptFile(ArrayList<FullTrip> allTrips, int monthNum, int yearNum, MediUser user) {
        MySqlDatasource ds = new MySqlDatasource();

        String fName = user.fullname + "_mileage_receipt_month_" + Helpers.DatesAndTimes.getMonthName(monthNum) + "_year_" +
                yearNum + ".txt";
        File txtFile = new File(Helpers.Files.ReceiptTempFiles.getDirectory(), fName);
        File finalReceiptFile = null;

        float total = 0f;
        float tripAmt = 0f;
        int tripCount = 0;
        float totalMiles = 0f;

        try {
            FileOutputStream stream = new FileOutputStream(txtFile);
            StringBuilder stringBuilder = new StringBuilder();
            try {
                stringBuilder.append("----------------------------------------\n");
                stringBuilder.append(("Mileage Report (" + user.fullname + ")\n"));
                stringBuilder.append("----------------------------------------\n\n");
                for (FullTrip trip : allTrips) {
                    if (trip.getIsSubmitted()) {
                        tripCount += 1;
                        tripAmt = trip.calculateReimbursement();
                        total += tripAmt;
                        totalMiles += trip.getDistanceInMiles();
                        stringBuilder.append("* Trip: " + Helpers.DatesAndTimes.getPrettyDate(trip.getDateTime()) + "\n\t" +
                                "Distance:" + trip.getDistanceInMiles() + "\n\t" +
                                "Reimbursement: " + Helpers.Numbers.convertToCurrency(tripAmt) + "\n\t" +
                                "Is edited: " + trip.getIsEdited() + "\n\t" +
                                "Is manual: " + trip.getIsManualTrip() + "\n\n");
                    }
                }

                totalMiles = Helpers.Numbers.formatAsZeroDecimalPointNumber(totalMiles, RoundingMode.HALF_UP);
                stringBuilder.append("\n\n Trip count: " + tripCount);
                stringBuilder.append("\n Total reimbursement: " + Helpers.Numbers.convertToCurrency(totalMiles * options.getReimbursementRate()));
                stringBuilder.append("\n Total miles: " + totalMiles);

                stream.write(stringBuilder.toString().getBytes());
            } finally {
                stream.close();
            }

            try {
                if (options.getReceiptFormat().equals(MyPreferencesHelper.RECEIPT_FORMAT_PNG)) {
                    finalReceiptFile = Helpers.Bitmaps.createPngFileFromString(stringBuilder.toString(), fName);
                } else if (options.getReceiptFormat().equals(MyPreferencesHelper.RECEIPT_FORMAT_JPEG)) {
                    finalReceiptFile = Helpers.Bitmaps.createJpegFileFromString(stringBuilder.toString(), fName);
                } else {
                    finalReceiptFile = txtFile;
                }
                return finalReceiptFile;
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Failed to make an image file.  Text file will have to do!", Toast.LENGTH_SHORT).show();
                return txtFile;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (tripCount > 0 && finalReceiptFile != null) {
            return finalReceiptFile;
        } else if (tripCount > 0 && finalReceiptFile == null) {
            return txtFile;
        } else {
            return null;
        }
    }

    void makeReceiptOnBehalfOf(final MediUser user, final int month, final int year) {

        final MyProgressDialog dialog = new MyProgressDialog(context, "Getting user's mileage...");
        dialog.show();

        String query = CrmQueries.Trips.getAllTripsWithoutEntriesByOwnerByMonthAndYear(year, month, user.systemuserid);
        Requests.Request request = new Requests.Request(Requests.Request.Function.GET);
        Requests.Argument arg = new Requests.Argument("query", query);
        request.arguments.add(arg);
        new Crm().makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                ArrayList<FullTrip> trips = FullTrip.createTripsFromCrmJson(response, true);
                Toast.makeText(context, "Trip count: " + trips.size(), Toast.LENGTH_SHORT).show();
                File receipt = makeReceiptFile(trips, month, year, user);
                doActualReceipt(receipt);
                dialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

}