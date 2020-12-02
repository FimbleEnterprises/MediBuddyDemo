package com.fimbleenterprises.medimileage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.ui.mileage.MileageFragment;
import com.fimbleenterprises.medimileage.ui.settings.mileage.SettingsActivity;
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
import static com.fimbleenterprises.medimileage.ui.mileage.MileageFragment.PERMISSION_UPDATE;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DrawerLayout.DrawerListener {
    private static final String TAG = "MainActivity";
    private AppBarConfiguration mAppBarConfiguration;
    MySettingsHelper options;
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

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        options = new MySettingsHelper(this);
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
                    MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());
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

    }

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
        //Called when a drawer's position changes.
    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {
        try {
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
            startActivity(new Intent(activity, AggregateStatsActivity.class));
            // drawer.closeDrawer(navigationView);
        } else if (item.getItemId() == R.id.nav_myterritory) {
            startActivity(new Intent(activity, Activity_TerritoryData.class));
            // drawer.closeDrawer(navigationView);
        } else if (item.getItemId() == R.id.nav_myaccounts) {
            startActivity(new Intent(activity, Activity_AccountInfo.class));
            // drawer.closeDrawer(navigationView);
        } else if (item.getItemId() == R.id.nav_myopportunities) {
            Intent oppIntent = new Intent(activity, FullscreenActivityChooseOpportunity.class);
            oppIntent.setAction(FullscreenActivityChooseOpportunity.FROM_MAIN_NAV_DRAWER);
            startActivity(oppIntent);
            // drawer.closeDrawer(navigationView);
        } else {
            try {
                Log.i(TAG, "onNavigationItemSelected index:" + item.getItemId());
                Log.i(TAG, "onNavigationItemSelected fullname:" + users.get(item.getItemId()).fullname);
                drawer.closeDrawer(navigationView);
                Intent intent = new Intent(getApplicationContext(), UserTripsActivity.class);
                intent.putExtra(UserTripsActivity.MILEAGE_USER, users.get(item.getItemId()));
                startActivity(intent);
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
        // makeDrawerTitles();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (drawer.isOpen()) {
                drawer.close();
                return true;
            }

            int backstackcount = Objects.requireNonNull(fragmentManager.getPrimaryNavigationFragment()).getChildFragmentManager().getBackStackEntryCount();

            try {
                if (myStack.get(1).equals("Login")) {
                    Log.i(TAG, "onKeyDown Last page was login");
                    for (int i = 0; i < backstackcount; i++) {
                        fragmentManager.getPrimaryNavigationFragment().getChildFragmentManager().popBackStack();
                        Log.i(TAG, "onKeyDown Popped backstack entry");
                    }
                }
            } catch (Exception ignored) {}
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

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem update = menu.findItem(R.id.action_update);
        update.setTitle(options.isExplicitMode() ? getString(R.string.action_update_check_explicit) : getString(R.string.action_update_check));

        MenuItem settings = menu.findItem(R.id.action_settings);
        settings.setTitle(options.isExplicitMode() ? getString(R.string.action_settings_explicit) : getString(R.string.action_settings));

        MenuItem login = menu.findItem(R.id.action_loginlogout);
        if (options.hasCachedCredentials()) {
            login.setTitle(options.isExplicitMode() ? getString(R.string.logout_explicit) : getString(R.string.logout));
        } else {
            login.setTitle(options.isExplicitMode() ? getString(R.string.login_explicit) : getString(R.string.login));
        }

        Typeface typeface = getResources().getFont(R.font.casual);

        for (int i = 0; i < menu.size(); i++) {
            MenuItem mi = menu.getItem(i);
            //for aapplying a font to subMenu ...
            SubMenu subMenu = mi.getSubMenu();
            if (subMenu != null && subMenu.size() > 0) {
                for (int j = 0; j < subMenu.size(); j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem, typeface);
                }
            }
            //the method we have create in activity
            applyFontToMenuItem(mi, typeface);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_settings :
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivityForResult(intent, 0);
                // navController.navigate(R.id.action_HomeFragment_to_SettingsFragment);
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
    }

    public void makeDrawerTitles() {
        Menu menu = navigationView.getMenu();
        
        if (options.isExplicitMode()) {
            menu.findItem(R.id.nav_home).setTitle(R.string.menu_mileage_explicit);
            menu.findItem(R.id.nav_aggregatedmileagestats).setTitle(R.string.menu_stats_explicit);
            menu.findItem(R.id.nav_data).setTitle(R.string.menu_data_explicit);
            menu.findItem(R.id.nav_myterritory).setTitle(R.string.menu_other_fucking_sales_shit);
            menu.findItem(R.id.nav_myaccounts).setTitle(getString(R.string.menu_account_info_explicit));
            menu.findItem(R.id.nav_myopportunities).setTitle(getString(R.string.menu_myopportunities_explicit));
            menu.findItem(R.id.nav_settings).setTitle(R.string.menu_settings_explicit);
            menu.findItem(R.id.nav_user_trips).setTitle(R.string.menu_users_explicit);
        } else {
            menu.findItem(R.id.nav_home).setTitle(R.string.menu_mileage);
            menu.findItem(R.id.nav_aggregatedmileagestats).setTitle(R.string.menu_aggregated_mileage_stats);
            menu.findItem(R.id.nav_data).setTitle(R.string.menu_data);
            menu.findItem(R.id.nav_myterritory).setTitle(R.string.menu_territory_info);
            menu.findItem(R.id.nav_myaccounts).setTitle(getString(R.string.menu_account_info));
            menu.findItem(R.id.nav_myopportunities).setTitle(getString(R.string.menu_myopportunities));
            menu.findItem(R.id.nav_settings).setTitle(R.string.menu_settings);
            menu.findItem(R.id.nav_user_trips).setTitle(R.string.menu_users);
        }

        getDistinctUsersWithTrips();
    }

    public boolean checkStoragePermission() {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public void checkForUpdate(final boolean silently) {

        Helpers.Files.deleteAppTempDirectory();

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

        String query = Queries.TripAssociation.getAssociationsByTripid("26C4B74D-58F8-EA11-810B-005056A36B9B");

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
                Log.i(TAG, "onYes " + object.toString());
                if (options.getDebugMode()) {
                    Toast.makeText(activity, object.toString() + " were saved locally", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNo(@Nullable Object object) {
                Log.w(TAG, "onNo: " + object.toString());
                if (options.getDebugMode()) {
                    Toast.makeText(activity, "Failed to retrieve and save opportunities", Toast.LENGTH_SHORT).show();
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
        linkEntity.addColumn(new EntityColumn("msus_last_lat"));
        linkEntity.addColumn(new EntityColumn("msus_last_loc_timestamp"));
        factory.addLinkEntity(linkEntity);

        Filter.FilterCondition condition1 = new Filter.FilterCondition("msus_dt_tripdate",
                Filter.Operator.THIS_MONTH);
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
            SubMenu subMenu = m.getItem(2).getSubMenu();
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
                        SubMenu subMenu = m.getItem(2).getSubMenu();
                        subMenu.removeItem(subMenu.getItem(0).getItemId());

                        for (int i = 0; i < users.size(); i++) {
                            MileageUser user = users.get(i);
                            if (user.isDriving()) {
                                subMenu.add(0, i, i, user.fullname + " (driving now)");
                            } else {
                                subMenu.add(0, i, i, options.isExplicitMode() ? user.fullFuckingName() : user.fullname);
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
                        SubMenu subMenu = m.getItem(3).getSubMenu();
                        subMenu.getItem(0).setTitle("Retry");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.i(TAG, "getDistinctUsersWithTrips FOund a cached users list.  Will use it instead of going to CRM for a rebuild.");

            Menu m = navigationView.getMenu();
            SubMenu subMenu = m.getItem(2).getSubMenu();
            subMenu.clear();
            subMenu.add("Loading...");
            subMenu.getItem(0).setTitle("Loading...");
            subMenu.removeItem(subMenu.getItem(0).getItemId());

            for (int i = 0; i < users.size(); i++) {
                MileageUser user = users.get(i);
                if (user.isDriving()) {
                    subMenu.add(0, i, i, user.fullname + " (driving now)");
                } else {
                    subMenu.add(0, i, i, options.isExplicitMode() ? user.fullFuckingName() : user.fullname);
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

    private void applyFontToMenuItem(MenuItem mi, Typeface font) {
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
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
                spreadsheet.share(activity);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(activity, "Failed to get stats", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                finish();
            }
        });
    }
}
