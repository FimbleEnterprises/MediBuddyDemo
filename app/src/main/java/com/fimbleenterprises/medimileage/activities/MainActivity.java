package com.fimbleenterprises.medimileage.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.Crm;
import com.fimbleenterprises.medimileage.CrmQueries;
import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.MileBuddyUpdater;
import com.fimbleenterprises.medimileage.MyApp;
import com.fimbleenterprises.medimileage.MyInterfaces;
import com.fimbleenterprises.medimileage.MyPreferencesHelper;
import com.fimbleenterprises.medimileage.QueryFactory;
import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.UpdateDownloader;
import com.fimbleenterprises.medimileage.objects_and_containers.Requests;
import com.fimbleenterprises.medimileage.services.MyFirebaseMessagingService;
import com.fimbleenterprises.medimileage.services.MyLocationService;
import com.fimbleenterprises.medimileage.dialogs.MyProgressDialog;
import com.fimbleenterprises.medimileage.dialogs.MyRatingDialog;
import com.fimbleenterprises.medimileage.dialogs.MyTwoChoiceDialog;
import com.fimbleenterprises.medimileage.dialogs.fullscreen_pickers.FullscreenAccountTerritoryPicker;
import com.fimbleenterprises.medimileage.dialogs.fullscreen_pickers.FullscreenActivityChooseRep;
import com.fimbleenterprises.medimileage.objects_and_containers.AggregateStats;
import com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities;
import com.fimbleenterprises.medimileage.objects_and_containers.ExcelSpreadsheet;
import com.fimbleenterprises.medimileage.objects_and_containers.MediUser;
import com.fimbleenterprises.medimileage.objects_and_containers.MileBuddyUpdate;
import com.fimbleenterprises.medimileage.objects_and_containers.MileageUser;
import com.fimbleenterprises.medimileage.objects_and_containers.Territories.Territory;
import com.fimbleenterprises.medimileage.activities.ui.mileage.MileageFragment;
import com.fimbleenterprises.medimileage.activities.ui.settings.SettingsActivity;
import com.google.android.material.navigation.NavigationView;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cz.msebera.android.httpclient.Header;

import static com.fimbleenterprises.medimileage.QueryFactory.*;
import static com.fimbleenterprises.medimileage.activities.ui.mileage.MileageFragment.PERMISSION_UPDATE;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DrawerLayout.DrawerListener {
    private static final String TAG = "MainActivity";
    private AppBarConfiguration mAppBarConfiguration;
    MyPreferencesHelper options;
    IntentFilter locFilter = new IntentFilter(MyLocationService.LOCATION_EVENT);
    BroadcastReceiver locReceiver;
    NavController navController;
    FragmentManager fragmentManager = getSupportFragmentManager();
    Activity activity = this;
    DrawerLayout drawer;
    ArrayList<String> myStack = new ArrayList<>();
    ArrayList<MileageUser> users = new ArrayList<>();
    NavigationView navigationView;
    MyFirebaseMessagingService fcmService;
    SearchView searchView;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        options = new MyPreferencesHelper(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextAppearance(this, R.style.CasualTextAppearance);
        setSupportActionBar(toolbar);
        final Activity finalActivity = this;

        Helpers.Files.makeAppDirectory();

        drawer = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.nav_view);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_authentication)
                .setDrawerLayout(drawer)
                .build();

        TextView txtVersion = navigationView.getHeaderView(0).findViewById(R.id.textViewVersion);
        txtVersion.setText(getString(R.string.version) + Helpers.Application.getAppVersion(this));
        TextView txtUserName = navigationView.getHeaderView(0).findViewById(R.id.textViewUser);
        MediUser curUser = MediUser.getMe();
        if (curUser != null) {
            txtUserName.setText(MediUser.getMe().fullname);
        } else {
            txtUserName.setText("Not logged in");
        }

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                Log.i(TAG, "onDestinationChanged " + destination.getLabel());
                assert destination.getLabel() != null;
                String dest = destination.getLabel().toString();
                if (isInStack(dest)) {
                    myStack.add(0, dest);
                } else {
                    myStack.add(dest);
                }
                for (int i = 0; i < myStack.size(); i++) {
                    Log.i(TAG, "Backstack pos" + i + " = " + myStack.get(i));
                }

            }
        });

        navigationView.setNavigationItemSelectedListener(this);
        drawer.addDrawerListener(this);

        // Initialize the broadcast receiver that handles current trip states/updates
        locFilter.addAction(MyLocationService.STOP_TRIP_ACTION);
        locReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    if (intent.getParcelableExtra(MyLocationService.LOCATION_CHANGED) != null) {
                        // LocationContainer update = intent.getParcelableExtra(MyLocationService.LOCATION_CHANGED);
                        Log.d(TAG, "onReceive Location broadcast received!");
                    }
                }
            }
        }; 

        // Check in the background if the user has cached credentials and whether they are still valid
        if ( MediUser.getMe(this) == null || ! options.hasCachedCredentials()) {
            navController.navigate(R.id.action_HomeFragment_to_HomeSecondFragment);
            Toast.makeText(this, getString(R.string.must_login), Toast.LENGTH_SHORT).show();
        }

        // Try in the background to set the current reimbursement rate
        try {
            MyLocationService.setReimbursementRate(new MyInterfaces.ReimbursementRateCallback() {
                @Override
                public void onSuccess(float rate) {
                    if (options.getReimbursementRate() != rate) {
                        Toast.makeText(MyApp.getAppContext(), "Updated reimbursement rate from: " +
                                options.getPrettyReimbursementRate() + " to: "
                                + Helpers.Numbers.convertToCurrency(rate), Toast.LENGTH_SHORT).show();
                    }
                    MyPreferencesHelper options = new MyPreferencesHelper(MyApp.getAppContext());
                    options.setReimbursementRate(rate);
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(getApplicationContext(), "Failed to update reimbursement rate", Toast.LENGTH_SHORT).show();
                    if (options.getReimbursementRate() > 0) {
                        Toast.makeText(finalActivity, "Will use cached rate of: " + options.getPrettyReimbursementRate(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // Prompt for storage permissions if necessary
        if (checkStoragePermission()) {
            if (options.getCheckForUpdates()) {
                checkForUpdate(true);
            }
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_UPDATE);
        }

        // If we end up doing push notifications then these FCM tokens will be required
        try {
            // Requests the device's FCM token when instantiated and saves it to preferences.
            fcmService = new MyFirebaseMessagingService(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Grab opportunities and save them as JSON locally in the background
        try {
            retrieveAndSaveOpportunities();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Update usage metrics to CRM; specifically app version
        try {
            MediUser.updateCrmWithMyMileBuddyVersion(activity, new MyInterfaces.CrmRequestListener() {
                @Override
                public void onComplete(Object result) {
                    Log.i(TAG, "onComplete " + result.toString());
                }

                @Override
                public void onProgress(Crm.AsyncProgress progress) { }

                @Override
                public void onFail(String error) {
                    Log.w(TAG, "onFail: Failed to update version to CRM");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* *************************************************************
                -= PUT EXPERIMENTAL SHIT BETWEEN THESE COMMENTS =-
         * *************************************************************/
        if (options.getDebugMode()) {
           /* Intent intent = new Intent(this, CreateQuoteScrollingActivity.class);
            startActivity(intent);*/

        }
        /* *************************************************************
                -= PUT EXPERIMENTAL SHIT BETWEEN THESE COMMENTS =-
         * *************************************************************/
    }

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
        //Called when a drawer's position changes.
    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {
        try {

            if (!MediUser.isLoggedIn()) {
                // drawer.close();
                Toast.makeText(activity, "You must login.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                navigationView.getMenu().getItem(1).setTitle(MyLocationService.isRunning ? "Stop Trip" : "Start New Trip");
            } catch (Exception e) {
                e.printStackTrace();
            }

            TextView txtUserName = navigationView.getHeaderView(0).findViewById(R.id.textViewUser);
            txtUserName.setText(MediUser.getMe().fullname);

            makeDrawerTitles();
        } catch (Exception e) { }
    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {
        // Called when a drawer has settled in a completely closed state.
    }

    @Override
    public void onDrawerStateChanged(int newState) {
        // Called when the drawer motion state changes. The new state will be one of STATE_IDLE, STATE_DRAGGING or STATE_SETTLING.
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (!MediUser.isLoggedIn()) {
            Toast.makeText(activity, "You must login.", Toast.LENGTH_SHORT).show();
            drawer.close();
            return true;
        }

        if (item.getItemId() == R.id.nav_start_new_trip) {
            Intent intent = new Intent(MileageFragment.GENERIC_RECEIVER_ACTION);
            intent.putExtra(MileageFragment.START_STOP_TRIP_INTENT, true);
            sendBroadcast(intent);
            drawer.close();
        }

        if (item.getTitle().equals(getString(R.string.retry))) {
            Log.i(TAG, "onNavigationItemSelected Retrying user populate...");
            drawer.closeDrawer(navigationView);
            Menu m = navigationView.getMenu();
                SubMenu subMenu = m.getItem(3).getSubMenu();
            subMenu.getItem(0).setTitle(getString(R.string.loading));
            try {
                makeDrawerTitles();
            } catch (Exception e) { }
        } else if (item.getTitle().equals(getString(R.string.loading))) {
            return false;
        } else if (item.getItemId() == R.id.nav_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivityForResult(intent, 0);
            // drawer.closeDrawer(navigationView);
        } else if (item.getItemId() == R.id.nav_aggregatedmileagestats) {
            startActivity(new Intent(activity, AggregateMileageStatsActivity.class));
            // drawer.closeDrawer(navigationView);
        } else if (item.getItemId() == R.id.nav_territory_data) {
            startActivity(new Intent(activity, Activity_TerritoryData.class));
            // drawer.closeDrawer(navigationView);
        } else if (item.getItemId() == R.id.nav_cpywidedata) {
            startActivity(new Intent(activity, Activity_CompanyWideData.class));
            // drawer.closeDrawer(navigationView);
        } else if (item.getItemId() == R.id.nav_account_data) {
            startActivity(new Intent(activity, Activity_AccountData.class));
            // drawer.closeDrawer(navigationView);
        } else if (item.getItemId() == R.id.nav_salesquotas) {
            Intent oppIntent = new Intent(activity, Activity_SalesQuotas.class);
            startActivity(oppIntent);
            // drawer.closeDrawer(navigationView);
        } else if (item.getItemId() == R.id.nav_usage) {
            Intent usageIntent = new Intent(activity, UsageMetricsActivity.class);
            startActivity(usageIntent);
            // drawer.closeDrawer(navigationView);
        } else if (item.getItemId() == R.id.nav_parking) {
            Intent usageIntent = new Intent(activity, Activity_ParkingMap.class);
            startActivity(usageIntent);
            // drawer.closeDrawer(navigationView);
        } else {
            try {
                Log.i(TAG, "onNavigationItemSelected index:" + item.getItemId());
                Log.i(TAG, "onNavigationItemSelected fullname:" + users.get(item.getItemId()).fullname);
                Intent intent = new Intent(getApplicationContext(), UserTripsActivity.class);
                intent.putExtra(UserTripsActivity.MILEAGE_USER, users.get(item.getItemId()));
                startActivity(intent);
                // drawer.closeDrawer(navigationView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getIntent() != null && getIntent().getAction() != null
                && getIntent().getAction().equals(MyLocationService.STOP_TRIP_ACTION)) {
            stopService(new Intent(this, MyLocationService.class));
            Toast.makeText(this, getString(R.string.stopping_trip), Toast.LENGTH_SHORT).show();
        }

        registerReceiver(locReceiver, locFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(locReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MyApp.checkLocationPermission(activity) != MyApp.LocationPermissionResult.FULL ||
                !MyApp.checkStoragePermission(activity)) {
            // Helpers.Application.showPermissionsPage(activity);
        }
        MyApp.setIsVisible(true, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApp.setIsVisible(false, this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (drawer.isOpen()) {
                drawer.close();
                return true;
            }

            if (searchView != null && !searchView.isIconified()) {
                searchView.setIconified(true);
                return true;
            }

            int backstackcount = Objects.requireNonNull(fragmentManager.getPrimaryNavigationFragment()).getChildFragmentManager().getBackStackEntryCount();

            /*try {
                if (myStack.get(1).equals("Login")) {
                    Log.i(TAG, "onKeyDown Last page was login");
                    for (int i = 0; i < backstackcount; i++) {
                        fragmentManager.getPrimaryNavigationFragment().getChildFragmentManager().popBackStack();
                        Log.i(TAG, "onKeyDown Popped backstack entry");
                    }
                }
            } catch (Exception ignored) {}*/
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getAction() != null) {
            if (intent.getAction().equals(MyLocationService.STOP_TRIP_ACTION)) {
                stopService(new Intent(this, MyLocationService.class));
                Log.w(TAG, "onReceive: RECEIVED STOP REQUEST AT ACTIVITY!");
                Log.w(TAG, "onReceive: RECEIVED STOP REQUEST AT ACTIVITY!");
                Log.w(TAG, "onReceive: RECEIVED STOP REQUEST AT ACTIVITY!");
            } else if (intent.getBooleanExtra(MyLocationService.WARN_USER, false)) {
                MyLocationService.userHasBeenWarned = false;
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView - effectively, prepare the search
        // for use.  There isn't much else to do in the activity, the SearchResultsActivity will do
        // the lion's share of the labor.
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo( searchManager.getSearchableInfo(new
                ComponentName(this, SearchResultsActivity.class)));

        if (searchView != null) {
            searchView.setInputType(InputType.TYPE_CLASS_TEXT);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem update = menu.findItem(R.id.action_update);
        update.setTitle(options.isExplicitMode() ? getString(R.string.action_update_check_explicit) : getString(R.string.action_update_check));

        MenuItem feedback = menu.findItem(R.id.action_feedback);
        feedback.setTitle(options.isExplicitMode() ? getString(R.string.action_feedback_explicit) : getString(R.string.action_feedback));

        MenuItem settings = menu.findItem(R.id.action_settings);
        settings.setTitle(options.isExplicitMode() ? getString(R.string.action_settings_explicit) : getString(R.string.action_settings));

        MenuItem login = menu.findItem(R.id.action_loginlogout);
        if (options.hasCachedCredentials()) {
            login.setTitle(options.isExplicitMode() ? getString(R.string.logout_explicit) : getString(R.string.logout));
        } else {
            login.setTitle(options.isExplicitMode() ? getString(R.string.login_explicit) : getString(R.string.login));
        }



        Helpers.Strings.applyFontToMenuItem(this, menu);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (!MediUser.isLoggedIn()) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_settings :

                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivityForResult(intent, 0);
                // navController.navigate(R.id.action_HomeFragment_to_SettingsFragment);
                break;

            case R.id.action_feedback :
                MyRatingDialog.rate(activity, new MyRatingDialog.OnRatingSubmitted() {
                    @Override
                    public void onSuccessful() {
                        Toast.makeText(activity, "Feedback submitted!  Thanks!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed() {
                        Toast.makeText(activity, "Failed to submit feedback.", Toast.LENGTH_SHORT).show();
                    }
                });
                break;

            case R.id.action_export_to_excel :
                String btn1Text = options.isExplicitMode() ? "EXPORT MY SHIT" : "EXPORT MILEAGE DATA";
                String btn2Text = options.isExplicitMode() ? "RECEIPT ONLY, DICK" : "I JUST WANT A RECEIPT";
                String msg = options.isExplicitMode() ? getString(R.string.export_mileage_disclaimer_explicit)
                        : getString(R.string.export_mileage_disclaimer);
                MyTwoChoiceDialog.show(activity, btn1Text, btn2Text , msg, new MyTwoChoiceDialog.TwoButtonListener() {
                    @Override
                    public void onBtn1Pressed() {
                        getAndExportAggregateStats();
                    }

                    @Override
                    public void onBtn2Pressed() {
                        Intent showReceiptDialogIntent = new Intent(MileageFragment.GENERIC_RECEIVER_ACTION);
                        showReceiptDialogIntent.putExtra(MileageFragment.MAKE_RECEIPT, true);
                        sendBroadcast(showReceiptDialogIntent);
                    }
                });
                break;

            case R.id.action_loginlogout :
                if(options.authenticateFragIsVisible()) {
                    Log.i(TAG, "onOptionsItemSelected Already on login fragment.");
                    return true;
                }

                if (options.hasCachedCredentials()) {
                    options.logout();
                    navController.navigate(R.id.action_HomeFragment_to_HomeSecondFragment);
                } else {

                    navController.navigate(R.id.action_HomeFragment_to_HomeSecondFragment);
                }
                break;

            case R.id.action_update :
                if (checkStoragePermission()) {
                    checkForUpdate(false);
                } else {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                            , PERMISSION_UPDATE);
                }
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        makeDrawerTitles();

        // A rep was chosen using the fullscreen rep chooser dialog.  Broadcast that shiz.
        if (data != null && data.getAction().equals(FullscreenActivityChooseRep.CHOICE_RESULT)) {
            Intent repPickerResultIntent = new Intent(FullscreenActivityChooseRep.CHOICE_RESULT);
            MediUser chosenUser = data.getParcelableExtra(FullscreenActivityChooseRep.CHOICE_RESULT);
            repPickerResultIntent.putExtra(FullscreenActivityChooseRep.CHOICE_RESULT, chosenUser);
            sendBroadcast(repPickerResultIntent);
        }

        if (data != null) {
            if (data.hasExtra(FullscreenAccountTerritoryPicker.ACCOUNT_RESULT)) {
                Log.i(TAG, "onActivityResult Account/Territory selection was made!");
                CrmEntities.Accounts.Account selectedAccount = data.getParcelableExtra(FullscreenAccountTerritoryPicker.ACCOUNT_RESULT);
                if (selectedAccount != null) {
                    Log.i(TAG, "onActivityResult " + selectedAccount.accountName + " was chosen!");
                }
            }
            if (data.hasExtra(FullscreenAccountTerritoryPicker.FOUND_TERRITORIES)) {
                ArrayList<Territory> territories = data.getParcelableArrayListExtra(FullscreenAccountTerritoryPicker.FOUND_TERRITORIES);
                Log.i(TAG, "onActivityResult returned " + territories.size() + " territories that we can cache!");
            }
        }
    }

    public void makeDrawerTitles() {
        Menu menu = navigationView.getMenu();

        
        if (options.isExplicitMode()) {
            menu.findItem(R.id.nav_home).setTitle(R.string.menu_mileage_explicit);
            menu.findItem(R.id.nav_aggregatedmileagestats).setTitle(R.string.menu_stats_explicit);
            menu.findItem(R.id.nav_data).setTitle(R.string.menu_data_explicit);
            menu.findItem(R.id.nav_territory_data).setTitle(R.string.menu_other_fucking_sales_shit);
            menu.findItem(R.id.nav_account_data).setTitle(getString(R.string.menu_account_info_explicit));
            menu.findItem(R.id.nav_salesquotas).setTitle(getString(R.string.menu_salesquotas_explicit));
            menu.findItem(R.id.nav_settings).setTitle(R.string.menu_settings_explicit);
            menu.findItem(R.id.nav_usage).setTitle("Fucking usage");
            menu.findItem(R.id.nav_user_trips).setTitle(R.string.menu_users_explicit);
        } else {
            menu.findItem(R.id.nav_home).setTitle(R.string.menu_mileage);
            menu.findItem(R.id.nav_aggregatedmileagestats).setTitle(R.string.menu_aggregated_mileage_stats);
            menu.findItem(R.id.nav_data).setTitle(R.string.menu_my_data);
            menu.findItem(R.id.nav_territory_data).setTitle(R.string.menu_territory_info);
            menu.findItem(R.id.nav_account_data).setTitle(getString(R.string.menu_account_info));
            menu.findItem(R.id.nav_salesquotas).setTitle(getString(R.string.menu_salesquotas));
            menu.findItem(R.id.nav_settings).setTitle(R.string.menu_settings);
            menu.findItem(R.id.nav_usage).setTitle("Usage");
            menu.findItem(R.id.nav_user_trips).setTitle(R.string.menu_users);
        }

        // User can hide the usage menu item in preferences and in fact, by default, it is hidden.
        menu.findItem(R.id.nav_usage).setVisible(options.getShowUsageMenuItem());

        getDistinctUsersWithTrips();
    }

    public boolean checkStoragePermission() {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public void checkForUpdate(final boolean silently) {

        // getDirectory() also makes the directory if it doesn't exist
        Helpers.Files.AppUpdates.getDirectory();

        // If an update has been previously downloaded prompt the user to install it
        if (options.updateIsAvailableLocally()) {
            MileBuddyUpdate mileBuddyUpdate = options.getMileBuddyUpdate();
            if (mileBuddyUpdate.version > Helpers.Application.getAppVersion(getApplicationContext())) {
                UpdateDownloader.install(true, this, options.getMileBuddyUpdate());
                Log.i(TAG, "checkForUpdate Update is available and ready to be installed.");
                return;
            }
        }

        // Silently check, download and save an update if available
        MileBuddyUpdater updater = new MileBuddyUpdater(getApplicationContext());
        final MyProgressDialog myProgressDialog = new MyProgressDialog(activity);
        myProgressDialog.setContentText("Checking for update...");

        if (!silently) {
            myProgressDialog.show();
        }

        updater.checkForUpdate(new MileBuddyUpdater.UpdateCheckListener() {
            @Override
            public void onAvailable(MileBuddyUpdate updateObject) {

                Log.i(TAG, "onAvailable Update is available ver: " + updateObject.version);
                myProgressDialog.dismiss();
                options.setMilebuddyUpdate(updateObject.json);
                new UpdateDownloader(activity, updateObject, silently).run();
                Toast.makeText(activity, "An update is available.  It will be downloaded in " +
                        "the background for next time you start MileBuddy.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNotAvailable() {
                myProgressDialog.dismiss();
                if (! silently) {
                    Toast.makeText(activity, getString(R.string.lastest_version), Toast.LENGTH_SHORT).show();
                }
                Log.i(TAG, "onNotAvailable Update not available");
            }

            @Override
            public void onError(String msg) {
                myProgressDialog.dismiss();
                Toast.makeText(activity, "Failed to download new version\n"
                        + msg, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "onError " + msg);
            }
        });
    }

    public void getTripAssociationsByTripId() {

        String query = CrmQueries.TripAssociation.getAssociationsByTripid("26C4B74D-58F8-EA11-810B-005056A36B9B");

        Requests.Argument arg = new Requests.Argument("query", query);
        ArrayList<Requests.Argument> args = new ArrayList<>();
        args.add(arg);
        Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);

        Crm crm = new Crm();
        crm.makeCrmRequest(getApplicationContext(), request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                CrmEntities.TripAssociations associations = new CrmEntities.TripAssociations(response);
                Log.i(TAG, "onSuccess " + associations.list.size() + " associations.");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.w(TAG, "onFailure: " + error.getLocalizedMessage());
            }
        });

    }

    public void retrieveAndSaveOpportunities() {
        CrmEntities.Opportunities.retrieveAndSaveOpportunities(new MyInterfaces.YesNoResult() {
            @Override
            public void onYes(@Nullable Object object) {
                try {
                    Log.i(TAG, "onYes " + object.toString());
                    if (options.getDebugMode()) {
                        Toast.makeText(activity, object.toString() + " were saved locally", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNo(@Nullable Object object) {
                try {
                    Log.w(TAG, "onNo: " + object.toString());
                    if (options.getDebugMode()) {
                        Toast.makeText(activity, "Failed to retrieve and save opportunities", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void getDistinctUsersWithTrips() {

        getTripAssociationsByTripId();

        QueryFactory factory = new QueryFactory("msus_fulltrip");
        factory.addColumn("ownerid");

        LinkEntity linkEntity = new LinkEntity("systemuser", "systemuserid",
                "owninguser", "a_79740df757a5e81180e8005056a36b9b");
        linkEntity.addColumn(new EntityColumn("territoryid"));
        linkEntity.addColumn(new EntityColumn("address1_stateorprovince"));
        linkEntity.addColumn(new EntityColumn("new_many_user_to_salesregion"));
        linkEntity.addColumn(new EntityColumn("internalemailaddress"));
        linkEntity.addColumn(new EntityColumn("positionid"));
        linkEntity.addColumn(new EntityColumn("msus_medibuddy_managed_territories"));
        linkEntity.addColumn(new EntityColumn("parentsystemuserid"));
        linkEntity.addColumn(new EntityColumn("jobtitle"));
        linkEntity.addColumn(new EntityColumn("msus_ismanager"));
        linkEntity.addColumn(new EntityColumn("fullname"));
        linkEntity.addColumn(new EntityColumn("businessunitid"));
        linkEntity.addColumn(new EntityColumn("address1_composite"));
        linkEntity.addColumn(new EntityColumn("msus_last_lon"));
        linkEntity.addColumn(new EntityColumn("msus_milebuddy_version"));
        linkEntity.addColumn(new EntityColumn("msus_last_lat"));
        linkEntity.addColumn(new EntityColumn("msus_last_loc_timestamp"));
        factory.addLinkEntity(linkEntity);

        Filter.FilterCondition condition1 = new Filter.FilterCondition("msus_dt_tripdate",
                Filter.Operator.LAST_X_MONTHS, "2");
        Filter filter = new Filter(Filter.FilterType.OR, condition1);
        Filter.FilterCondition condition2 = new Filter.FilterCondition("msus_dt_tripdate",
                Filter.Operator.LAST_MONTH);
        filter.addCondition(condition2);
        factory.setFilter(filter);

        factory.isDistinct(true);

        factory.addSortClause(new QueryFactory.SortClause("ownerid", false,
                QueryFactory.SortClause.ClausePosition.ONE));
        String query = factory.construct();

        Requests.Request request = new Requests.Request(Requests.Request.Function.GET);
        Requests.Argument argument = new Requests.Argument("query", query);
        request.arguments.add(argument);

        if (users == null || users.size() < 1) {
            Log.i(TAG, "getDistinctUsersWithTrips The users array is empty or null.  Need to rebuild it from CRM!");

            Menu m = navigationView.getMenu();
            SubMenu subMenu = m.getItem(5).getSubMenu();
            subMenu.clear();
            subMenu.add("Loading...");
            subMenu.getItem(0).setTitle("Loading...");

            Crm crm = new Crm();
            crm.makeCrmRequest(this, request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Log.i(TAG, "onSuccess: StatusCode: " + statusCode);

                    try {
                        JSONObject json = new JSONObject(new String(responseBody));
                        JSONArray array = json.getJSONArray("value");
                        users = MileageUser.makeMany(array);
                        Menu m = navigationView.getMenu();
                        SubMenu subMenu = m.getItem(5).getSubMenu();
                        subMenu.removeItem(subMenu.getItem(0).getItemId());

                        for (int i = 0; i < users.size(); i++) {
                            MileageUser user = users.get(i);
                            if (user.isDriving()) {
                                subMenu.add(0, i, i, user.fullname + " (driving)").setIcon(R.drawable.notification_small_car);
                            } else {
                                subMenu.add(0, i, i, options.isExplicitMode() ? user.fullFuckingName() : user.fullname).setIcon(R.drawable.arrow_right2);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.w(TAG, "onFailure: " + error.getMessage());
                    try {
                        Menu m = navigationView.getMenu();
                        SubMenu subMenu = m.getItem(5).getSubMenu();
                        subMenu.getItem(0).setTitle("Retry");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.i(TAG, "getDistinctUsersWithTrips Found a cached users list.  Will use it instead of going to CRM for a rebuild.");

            Menu m = navigationView.getMenu();
            SubMenu subMenu = m.getItem(5).getSubMenu();
            subMenu.clear();
            subMenu.add("Loading...");
            subMenu.getItem(0).setTitle("Loading...");
            subMenu.removeItem(subMenu.getItem(0).getItemId());

            for (int i = 0; i < users.size(); i++) {
                MileageUser user = users.get(i);
                if (user.isDriving()) {
                    subMenu.add(0, i, i, user.fullname + " (driving)").setIcon(R.drawable.notification_small_car);
                } else {
                    subMenu.add(0, i, i, options.isExplicitMode() ? user.fullFuckingName() : user.fullname).setIcon(R.drawable.arrow_right2);
                }
            }



        }


    }

    boolean isInStack(String name) {
        for (String entry : myStack) {
            if (name.equals(entry)) {
                return true;
            }
        }
        return false;
    }

    void getAndExportAggregateStats() {

        String msg = options.isExplicitMode() ? getString(R.string.progress_dialog_generic_explicit)
                : getString(R.string.export_aggregate_stats_excel_progress_dialog);
        final MyProgressDialog dialog = new MyProgressDialog(this, msg);
        dialog.show();

        QueryFactory factory = new QueryFactory("msus_fulltrip");
        factory.addColumn("msus_name");
        factory.addColumn("msus_dt_tripdate");
        factory.addColumn("msus_trip_duration");
        factory.addColumn("msus_totaldistance");
        factory.addColumn("msus_reimbursement");
        factory.addColumn("msus_trip_minder_killed");
        factory.addColumn("msus_edited");
        factory.addColumn("msus_is_manual");
        factory.addColumn("ownerid");

        Filter filter = new Filter(Filter.FilterType.AND);
        filter.addCondition(new Filter.FilterCondition("msus_dt_tripdate", Filter.Operator.LAST_X_MONTHS, "2"));
        filter.addCondition(new Filter.FilterCondition("ownerid", Filter.Operator.EQUALS, MediUser.getMe().systemuserid));
        factory.setFilter(filter);

        factory.sortClauses.add(new QueryFactory.SortClause("msus_dt_tripdate", true, QueryFactory.SortClause.ClausePosition.ONE));
        factory.sortClauses.add(new QueryFactory.SortClause("ownerid", true, QueryFactory.SortClause.ClausePosition.TWO));

        String query = factory.construct();

        Requests.Request request = new Requests.Request(Requests.Request.Function.GET);
        Requests.Argument argument = new Requests.Argument("query", query);
        request.arguments.add(argument);

        Crm crm = new Crm();
        crm.makeCrmRequest(this, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                AggregateStats stats = new AggregateStats(response);
                Log.i(TAG, "onSuccess | " + response);
                dialog.dismiss();
                String monthYear = Helpers.DatesAndTimes.getMonthName(DateTime.now().getMonthOfYear())
                        .toLowerCase().replace(" ", "") + "_" + DateTime.now().getYear();
                String filename = "milebuddy_aggregate_mileage_export_" + monthYear + "_"
                        + MediUser.getMe().fullname.replace(" ","_") + ".xls";
                ExcelSpreadsheet spreadsheet = stats.exportToExcel(filename.toLowerCase());
                Helpers.Files.shareFileProperly(activity, spreadsheet.file);
                // spreadsheet.share(activity);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(activity, "Failed to get stats", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                finish();
            }
        });
    }

    protected void testPdf() {
        Toast.makeText(this, "Fuck you!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, QuoteActivity.class);
        startActivity(intent);
    }
}
