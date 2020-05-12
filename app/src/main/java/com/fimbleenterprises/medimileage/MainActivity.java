package com.fimbleenterprises.medimileage;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.ui.settings.mileage.SettingsActivity;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
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
import androidx.preference.AndroidResources;

import static com.fimbleenterprises.medimileage.ui.mileage.MileageFragment.PERMISSION_UPDATE;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";



    private AppBarConfiguration mAppBarConfiguration;
    MySettingsHelper options;
    MyLocationService service;
    public static int SERVICE_ID = 200;
    IntentFilter locFilter = new IntentFilter(MyLocationService.LOCATION_EVENT);
    BroadcastReceiver locReceiver;
    private NotificationManager mNotificationManager;
    NavController navController;
    FragmentManager fragmentManager;
    Activity activity;

    ArrayList<String>myStack = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        options = new MySettingsHelper(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextAppearance(this, R.style.CasualTextAppearance);
        setSupportActionBar(toolbar);
        final Activity finalActivity = this;
        activity = this;
        fragmentManager = getSupportFragmentManager();

        Helpers.Files.makeAppDirectory();
        Helpers.Files.makeBackupDirectory();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_authentication)
                .setDrawerLayout(drawer)
                .build();

        TextView txtVersion = navigationView.getHeaderView(0).findViewById(R.id.textViewVersion);
        txtVersion.setText("Version " + Helpers.Application.getAppVersion(this));

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                Log.i(TAG, "onDestinationChanged " + destination.getLabel());
                String dest = destination.getLabel().toString();
                if (isInStack(dest)) {
                    myStack.remove(getPosInStack(dest));
                    myStack.add(0, dest);
                } else {
                    myStack.add(dest);
                }
                for (int i = 0; i < myStack.size(); i++) {
                    Log.i(TAG, "Backstack pos" + i + " = " + myStack.get(i));
                }
            }
        });

        // Initialize the broadcast receiver
        locFilter.addAction(MyLocationService.STOP_TRIP_ACTION);
        locReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    if (intent.getParcelableExtra(MyLocationService.LOCATION_CHANGED) != null) {
                        LocationContainer update = intent.getParcelableExtra(MyLocationService.LOCATION_CHANGED);
                        Log.d(TAG, "onReceive Location broadcast received!");
                    }
                }
            }
        };

        mNotificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if ( MediUser.getMe() == null || ! options.hasCachedCredentials()) {
            navController.navigate(R.id.action_HomeFragment_to_HomeSecondFragment);
            Toast.makeText(this, "You must login", Toast.LENGTH_SHORT).show();
        }

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

        if (checkStoragePermission()) {
            if (options.getCheckForUpdates()) {
                checkForUpdate(true);
            }
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_UPDATE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Bundle bundle = getIntent().getExtras();

        if (getIntent() != null && getIntent().getAction() != null
                && getIntent().getAction().equals(MyLocationService.STOP_TRIP_ACTION)) {
            stopService(new Intent(this, MyLocationService.class));
            Toast.makeText(this, "Stopping trip...", Toast.LENGTH_SHORT).show();
        }

        registerReceiver(locReceiver, locFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(locReceiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            int backstackcount = fragmentManager.getPrimaryNavigationFragment().getChildFragmentManager().getBackStackEntryCount();
            for (int i = 0; i < backstackcount; i++) {
                FragmentManager.BackStackEntry entry = fragmentManager.getPrimaryNavigationFragment().getChildFragmentManager().getBackStackEntryAt(i);
            }

            try {
                if (myStack.get(1).equals("Login")) {
                    Log.i(TAG, "onKeyDown Last page was login");
                    for (int i = 0; i < backstackcount; i++) {
                        fragmentManager.getPrimaryNavigationFragment().getChildFragmentManager().popBackStack();
                        Log.i(TAG, "onKeyDown Popped backstack entry");
                    }
                }
            } catch (Exception e) {}
        }

        return super.onKeyDown(keyCode, event);
    }

    boolean isInStack(String name) {
        for (String entry : myStack) {
            if (name.equals(entry)) {
                return true;
            }
        }
        return false;
    }

    int getPosInStack(String name) {
        for (int i = 0; i < myStack.size(); i++) {
            if (myStack.get(i).equals(name)) {
                return i;
            }
        }
        return -1;
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
            } else if (intent.getBooleanExtra(MyLocationService.WARN_USER, false) == true) {
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

    private void applyFontToMenuItem(MenuItem mi, Typeface font) {
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem login = menu.findItem(R.id.action_loginlogout);

        if (options.hasCachedCredentials()) {
            login.setTitle("Logout");
        } else {
            login.setTitle("Login");
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

            case R.id.action_loginlogout :
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.frag_authenticate);
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

    public boolean checkLocationPermission() {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public boolean checkStoragePermission() {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public void checkForUpdate(final boolean silently) {

        // If an update has been previously downloaded prompt the user to install it
        if (options.updateIsAvailable()) {
            if (options.getUpdateVersion() > Helpers.Application.getAppVersion(getApplicationContext())) {
                if (options.getUpdateFile() != null && options.getUpdateFile().exists()) {
                    Log.i(TAG, "checkForUpdate There is a saved update available on the device!");
                    UpdateDownloader.install(true, this);
                    return;
                }
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
                new UpdateDownloader(activity, updateObject, silently).run();
            }

            @Override
            public void onNotAvailable() {
                myProgressDialog.dismiss();
                Log.i(TAG, "onNotAvailable Update not available");
            }

            @Override
            public void onError(String msg) {
                myProgressDialog.dismiss();
                Log.i(TAG, "onError " + msg);
            }
        });
    }

/*    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (permissions.length > 0) {
                        if (permissions[0].equals("android.permission.ACCESS_FINE_LOCATION")) {
                            startMyLocService();
                        } else if (permissions[0].equals("android.permission.WRITE_EXTERNAL_STORAGE")) {
                            Toast.makeText(getApplicationContext(), "Database was exported.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }*/


































































}
