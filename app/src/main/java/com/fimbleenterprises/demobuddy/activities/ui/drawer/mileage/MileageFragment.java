package com.fimbleenterprises.demobuddy.activities.ui.drawer.mileage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.fimbleenterprises.demobuddy.Crm;
import com.fimbleenterprises.demobuddy.CrmQueries;
import com.fimbleenterprises.demobuddy.Helpers;
import com.fimbleenterprises.demobuddy.MyApp;
import com.fimbleenterprises.demobuddy.MyAsyncTask;
import com.fimbleenterprises.demobuddy.MyInterfaces;
import com.fimbleenterprises.demobuddy.MyPreferencesHelper;
import com.fimbleenterprises.demobuddy.MySpeedoGauge;
import com.fimbleenterprises.demobuddy.MySqlDatasource;
import com.fimbleenterprises.demobuddy.R;
import com.fimbleenterprises.demobuddy.TripAssociationManager;
import com.fimbleenterprises.demobuddy.activities.Activity_ManualTrip;
import com.fimbleenterprises.demobuddy.activities.ViewTripActivity;
import com.fimbleenterprises.demobuddy.activities.fullscreen_pickers.FullscreenActivityChooseOpportunity;
import com.fimbleenterprises.demobuddy.activities.fullscreen_pickers.FullscreenActivityChooseRep;
import com.fimbleenterprises.demobuddy.activities.ui.views.MyAnimatedNumberTextView;
import com.fimbleenterprises.demobuddy.adapters.TripListRecyclerAdapter;
import com.fimbleenterprises.demobuddy.dialogs.DatePickerFragment;
import com.fimbleenterprises.demobuddy.dialogs.MonthYearPickerDialog;
import com.fimbleenterprises.demobuddy.dialogs.MyProgressDialog;
import com.fimbleenterprises.demobuddy.dialogs.MyYesNoDialog;
import com.fimbleenterprises.demobuddy.objects_and_containers.CrmEntities;
import com.fimbleenterprises.demobuddy.objects_and_containers.FullTrip;
import com.fimbleenterprises.demobuddy.objects_and_containers.LocationContainer;
import com.fimbleenterprises.demobuddy.objects_and_containers.MediUser;
import com.fimbleenterprises.demobuddy.objects_and_containers.MileBuddyMetrics;
import com.fimbleenterprises.demobuddy.objects_and_containers.Opportunities;
import com.fimbleenterprises.demobuddy.objects_and_containers.Requests;
import com.fimbleenterprises.demobuddy.objects_and_containers.Requests.Request;
import com.fimbleenterprises.demobuddy.objects_and_containers.custom_exceptions.CrmRequestExceptions;
import com.fimbleenterprises.demobuddy.services.MyLocationService;
import com.fimbleenterprises.demobuddy.services.ServerTripSyncService;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.scwang.smart.refresh.header.MaterialHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import org.jetbrains.annotations.NonNls;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cz.msebera.android.httpclient.Header;

import static com.fimbleenterprises.demobuddy.MyApp.LocationPermissionResult;
import static com.fimbleenterprises.demobuddy.objects_and_containers.CrmEntities.TripAssociations;

public class MileageFragment extends Fragment implements TripListRecyclerAdapter.ItemClickListener {

    public static final int PERMISSION_MAKE_RECEIPT = 0;
    public static final int PERMISSION_START_TRIP = 1;
    public static final int PERMISSION_MAKE_TRIP = 2;
    public static final int PERMISSION_UPDATE = 3;

    @NonNls
    public static final String GENERIC_RECEIVER_ACTION = "GENERIC_RECEIVER_ACTION";
    @NonNls
    public static final String MAKE_RECEIPT = "MAKE_RECEIPT";
    @NonNls
    public static final String REIMBURSEMENT_EMAIL = "receipts@concur.com";
    @NonNls
    public static final String START_STOP_TRIP_INTENT = "START_STOP_TRIP_INTENT";

    private static final String TAG = "MileageFragment";
    private static final int SHOW_CLICK_HERE_TRIP_COUNT_THRESHOLD = 8;
    BroadcastReceiver locReceiver;
    BroadcastReceiver miscReceiver;
    IntentFilter allPurposeFilter = new IntentFilter(GENERIC_RECEIVER_ACTION);
    MySqlDatasource datasource;
    TripListRecyclerAdapter adapter;
    ArrayList<FullTrip> locallySavedTrips = new ArrayList<>();
    RecyclerView recyclerView;
    IntentFilter locIntentFilter = new IntentFilter(MyLocationService.LOCATION_EVENT);
    public static final int DEFAULT_FONT_COLOR = -1979711488;
    MyPreferencesHelper options;
    ImageView emptyTripList;
    boolean isStarting = true;
    public View rootView;
    Dialog dialogTripOptions;
    RelativeLayout tripStatusContainer;
    MyProgressDialog progressDialog;
    TextView txtSpeed;
    TextView txtDistance;
    TextView txtReimbursement;
    TextView txtStatus;
    TextView txtMilesTotal;
    TextView txtMtd;
    Button btnStartStop;
    TextView txtDuration;
    ToggleButton toggleEditButton;
    Button btnDeleteTrips;
    ImageView gif_view;
    Button btnAddManualTrip;
    Button btnCreateReceipt;
    Button btnSync;
    MySpeedoGauge gauge;
    Runnable tripDurationRunner;
    Handler tripDurationHandler = new Handler();
    Runnable mtdTogglerRunner;
    Handler mtdTogglerHandler = new Handler();
    boolean isShowingTotalMiles;

    FullTrip tripToReassign;

    double lastMtdValue;
    MyAnimatedNumberTextView animatedNumberTextView;

    // Filter to listen for rep chooser result broadcasts.
    IntentFilter repChosenFilter = new IntentFilter(FullscreenActivityChooseRep.CHOICE_RESULT);
    BroadcastReceiver repChosenReceiver;

    IntentFilter tripSyncFilter = new IntentFilter(ServerTripSyncService.TRIP_SYNC_SERVICE);
    BroadcastReceiver tripSyncReceiver;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (data != null && data.hasExtra(FullscreenActivityChooseOpportunity.OPPORTUNITY_RESULT)) {
                Opportunities.Opportunity opportunity =
                        data.getParcelableExtra(FullscreenActivityChooseOpportunity.OPPORTUNITY_RESULT);
                if (opportunity != null) {
                    Log.i(TAG, "onActivityResult Opportunity was manipulated (" + opportunity.name + ")");
                }
                populateTripList();
            } else if (data != null && data.hasExtra(Activity_ManualTrip.ADD_MANUAL_TRIP)) {
                FullTrip createdTrip = data.getParcelableExtra(Activity_ManualTrip.ADD_MANUAL_TRIP);
                try {
                    if (options.autoSubmitOnTripEnded()) {
                        assert createdTrip != null;
                        submitTrip(createdTrip);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                populateTripList();
                Toast.makeText(getContext(), getString(R.string.toast_created_trip), Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            if (data != null && data.getAction() != null) {
                if (data.getAction().equals(Activity_ManualTrip.ADD_MANUAL_TRIP)) {
                    if (data.hasExtra(Activity_ManualTrip.ERROR_MESSAGE)) {
                        String err = data.getStringExtra(Activity_ManualTrip.ERROR_MESSAGE);
                        if (err != null) {
                            String msg = getString(R.string.toast_failed_to_create_trip);
                            for (Toast toast : Arrays.asList(
                                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT)
                                    , Toast.makeText(getContext(), err, Toast.LENGTH_LONG))) {
                                toast.show();
                            } // show toast for each error returned
                        } // error returned in intent has message
                    } // error returned in intent
                } // action == manual trip created
            } // intent has data and action
        } // result == cancelled

    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_START_TRIP:
                if (checkLocationPermission() != LocationPermissionResult.FULL) {
                    Toast.makeText(getContext(), getString(R.string.toast_permission_msg)
                            , Toast.LENGTH_SHORT).show();
                    break;
                }
                startStopTrip();
                break;

            case PERMISSION_MAKE_RECEIPT:
                if (checkStoragePermission()) {
                    showReceiptDialog();
                }
                break;

            case PERMISSION_MAKE_TRIP:
                if (checkLocationPermission() != LocationPermissionResult.NONE) {
                    Intent intent = new Intent(getContext(), Activity_ManualTrip.class);
                    startActivityForResult(intent, 0);
                }
                break;
        }
    }

    public View onCreateView(@NonNull final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        options = new MyPreferencesHelper();
        options.authenticateFragIsVisible(false);

        // Log a metric
        MileBuddyMetrics.updateMetric(getContext(), MileBuddyMetrics.MetricName.LAST_OPENED_APP, DateTime.now());

        final View root = inflater.inflate(R.layout.frag_mileage, container, false);
        rootView = root;

        progressDialog = new MyProgressDialog(getContext(), getString(R.string.progress_working));

        locIntentFilter.addAction(MyLocationService.STOP_TRIP_ACTION);
        locIntentFilter.addAction(MyLocationService.SERVICE_STARTED);
        locIntentFilter.addAction(MyLocationService.SERVICE_STOPPING);
        locIntentFilter.addAction(MyLocationService.SERVICE_STOPPED);
        locIntentFilter.addAction(MyLocationService.NOT_MOVING);

        emptyTripList = root.findViewById(R.id.image_no_trips);
        gif_view = root.findViewById(R.id.gifview);
        gauge = root.findViewById(R.id.speedo);
        tripStatusContainer = root.findViewById(R.id.tripStatusContainer);
        txtStatus = root.findViewById(R.id.txtTripStatus);
        txtDistance = root.findViewById(R.id.txtDistance);
        txtSpeed = root.findViewById(R.id.txtSpeed);
        txtDuration = root.findViewById(R.id.txtTime);
        txtReimbursement = root.findViewById(R.id.txtReimbursement);
        txtMtd = root.findViewById(R.id.txtMtdValue);
        txtMtd.setOnClickListener(v -> {
            stopMtdTogglerRunner();
            startMtdTogglerRunner();
        });
        txtMilesTotal = root.findViewById(R.id.txtMilesTotal);
        txtMilesTotal.setOnClickListener(v -> {
            stopMtdTogglerRunner();
            startMtdTogglerRunner();
        });
        btnSync = root.findViewById(R.id.button_sync);
        btnSync.setOnClickListener(v -> {
            try {
                Intent syncTripServiceIntent =
                        new Intent(getContext(), ServerTripSyncService.class);
                requireContext().startForegroundService(syncTripServiceIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        btnStartStop = root.findViewById(R.id.btnStartStopTrip);
        btnStartStop.setOnClickListener(v -> {
            if (options.getNameTripOnStart() && !MyLocationService.isRunning) {
                showNameTrip();
            } else {
                startStopTrip();
            }
        });
        toggleEditButton = root.findViewById(R.id.tgglebtn_editTrips);
        toggleEditButton.setOnCheckedChangeListener((buttonView, isChecked) -> setEditMode(isChecked));
        btnDeleteTrips = root.findViewById(R.id.btn_deleteSelectedTrips);
        btnDeleteTrips.setOnClickListener(v -> MyYesNoDialog.show(getContext(), new MyYesNoDialog.YesNoListener() {
            @Override
            public void onYes() {
                deleteSelected();
                populateTripList();
            }

            @Override
            public void onNo() {
                Toast.makeText(getContext(), getString(R.string.cancelled), Toast.LENGTH_SHORT).show();
            }
        }));
        btnAddManualTrip = root.findViewById(R.id.button_add_manual);
        btnAddManualTrip.setOnClickListener(v -> {
            // Check permission and request if not present
            if (checkLocationPermission() == LocationPermissionResult.NONE) {
                boolean showRational = shouldShowRequestPermissionRationale(
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (!showRational) {
                    final Dialog dialog = new Dialog(requireActivity());
                    dialog.setContentView(R.layout.generic_app_dialog);
                    TextView txtMain = dialog.findViewById(R.id.txtMainText);
                    txtMain.setText(getString(R.string.location_permissions_not_correct));
                    Button btnOkay = dialog.findViewById(R.id.btnOkay);
                    btnOkay.setOnClickListener(v1 -> {
                        Helpers.Application.openAppSettings(requireContext());
                        dialog.dismiss();
                    });
                    dialog.setTitle(getString(R.string.permissions));
                    dialog.setCancelable(true);
                    dialog.setOnKeyListener((dg, keyCode, event) -> {
                        if (keyCode != KeyEvent.KEYCODE_BACK) {
                            Helpers.Application.openAppSettings(requireContext());
                        }
                        dg.dismiss();
                        return true;
                    });
                    dialog.show();
                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                            , PERMISSION_MAKE_TRIP);
                }
            } else {
                Intent intent = new Intent(getContext(), Activity_ManualTrip.class);
                startActivityForResult(intent, 0);
            }
        });
        btnCreateReceipt = root.findViewById(R.id.button_get_receipt);
        btnCreateReceipt.setOnClickListener(v -> {
            if (!checkStoragePermission()) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                        , PERMISSION_MAKE_RECEIPT);
            } else {
                showReceiptDialog();
            }
        });
        // set up the RecyclerView
        recyclerView = root.findViewById(R.id.rvTrips);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        RefreshLayout refreshLayout = root.findViewById(R.id.refreshLayout);
        refreshLayout.setRefreshHeader(new MaterialHeader(requireContext()));
        refreshLayout.setEnableLoadMore(true);
        refreshLayout.setOnRefreshListener(layout -> {
            layout.finishRefresh(500);
            populateTripList();
        });
        refreshLayout.setOnLoadMoreListener(layout -> layout.finishLoadMore(500));

        // Update the current user silently just in case something has changed
        if (MediUser.getMe() != null && MediUser.getMe().email != null) {
            getUser(MediUser.getMe().email);
        }

        try {
            getAllAccountAddresses();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize the receiver listening for broadcasts from the location service (running trip).
        locReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {
                if (intent != null) {

                    if (options.isExplicitMode()) {
                        btnStartStop.setText((MyLocationService.isRunning) ?
                                getString(R.string.fucking_stop) : getString(R.string.fucking_go));
                    } else {
                        btnStartStop.setText((MyLocationService.isRunning) ?
                                getString(R.string.stop) : getString(R.string.go));
                    }
                    btnSync.setTextColor((MyLocationService.isRunning) ? Color.GRAY : Color.BLUE);
                    btnSync.setEnabled(!MyLocationService.isRunning);
                    tripStatusContainer.setVisibility((MyLocationService.isRunning) ? View.VISIBLE : View.GONE);
                    txtDuration.setText(getString(R.string.trip_duration, MyLocationService.getTripDuration()));

                    // NEW LOCATION INFORMATION RECEIVED
                    if (intent.getParcelableExtra(MyLocationService.LOCATION_CHANGED) != null) {
                        LocationContainer update =
                                intent.getParcelableExtra(MyLocationService.LOCATION_CHANGED);
                        Log.d(TAG, "onReceive Location broadcast received!");

                        if (MyLocationService.isRunning) {
                            txtStatus.setText(getString(R.string.trip_running));
                            txtStatus.setTextColor(Color.GREEN);
                            txtReimbursement.setText(Objects.requireNonNull(update).fullTrip.calculatePrettyReimbursement());
                            Helpers.Animations.pulseAnimation(txtStatus, 1.00f, 1.25f, 9000, 150);
                            if (isStarting) {
                                showGif(false);
                                isStarting = false;
                            }
                        } else {
                            txtStatus.setText(getString(R.string.trip_not_started));
                            txtStatus.setTextColor(DEFAULT_FONT_COLOR);
                        }

                        float miles =
                                Helpers.Geo.convertMetersToMiles(Objects.requireNonNull(update)
                                        .fullTrip.getDistance(), 1);
                        txtDistance.setText(getString(R.string.distance, Float.toString(miles)));
                        txtSpeed.setText(update.tripEntry.getSpeedInMph(true));
                        gauge.setSpeed(update.tripEntry.getMph());

                        setEditMode(false);
                        manageDefaultImage();

                    }

                    // SERVICE HAS STARTED
                    if (intent.getAction() != null && intent.getAction().equals(MyLocationService.SERVICE_STARTED)) {
                        btnSync.setEnabled(false);
                        txtStatus.setText(getString(R.string.start_driving));
                        txtDistance.setText(getString(R.string.x_miles, 0));
                        txtSpeed.setText(getString(R.string.x_mph, 0));
                        isStarting = true;
                        tripStatusContainer.setVisibility(View.VISIBLE);
                        txtStatus.setTextColor(Color.BLUE);
                        txtReimbursement.setText(getString(R.string.x_dollars, 0));
                        Helpers.Animations.pulseAnimation(txtStatus, 1.00f, 1.25f, 9000, 450);
                        Log.i(TAG, "onReceive | trip starting broadcast received.");
                        manageDefaultImage();
                        gauge.setSpeed(0);
                        gauge.setMaxSpeed(120);
                        startTripDurationRunner();
                    }

                    // SERVICE IS STOPPING
                    if (intent.getAction() != null && intent.getAction().equals(MyLocationService.SERVICE_STOPPED)) {
                        txtStatus.setText(getString(R.string.trip_stopping));
                        Log.i(TAG, "onReceive  | trip stopping broadcast received");
                    }

                    // SERVICE IS ENDED
                    if (intent.getAction() != null && intent.getAction().equals(MyLocationService.SERVICE_STOPPED)) {
                        txtStatus.setText(getString(R.string.trip_stopped));
                        txtStatus.setTextColor(Color.RED);
                        tripStatusContainer.setVisibility(View.GONE);
                        Log.i(TAG, "onReceive | trip stopped broadcast received.");
                        populateTripList();
                        manageDefaultImage();
                        Log.i(TAG, "onReceive | trip stopping broadcast received.");

                        if (intent.getParcelableExtra(MyLocationService.FINAL_LOCATION) != null) {
                            FullTrip trip =
                                    intent.getParcelableExtra(MyLocationService.FINAL_LOCATION);
                            Log.i(TAG, "onReceive " + Objects.requireNonNull(trip));

                            if (options.autoSubmitOnTripEnded()) {
                                try {
                                    if (trip.getDistanceInMiles() >= 2 && trip.getTripEntries().size() > 2) {
                                        submitTrip(trip);
                                    }
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }

                    // USER IS NOT MOVING
                    if (intent.getAction() != null && intent.getAction().equals(MyLocationService.NOT_MOVING)) {
                        txtSpeed.setText(getString(R.string.x_mph, 0));
                        Log.i(TAG, "onReceive: Got a stopped moving broadcast!");
                    }

                }
            }
        };

        miscReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    // Handle an intent received for creating a new receipt.
                    if (intent.getBooleanExtra(MAKE_RECEIPT, false)) {
                        if (Objects.requireNonNull(intent).getBooleanExtra(MAKE_RECEIPT, false)) {
                            showReceiptDialog();
                        }
                    }
                    // Handle an intent received to start/stop a trip (like from the drawer item).
                    if (intent.getBooleanExtra(START_STOP_TRIP_INTENT, false)) {
                        Log.i(TAG, "onReceive | Got an intent to start a trip!!");
                        if (options.getNameTripOnStart() && !MyLocationService.isRunning) {
                            showNameTrip();
                        } else {
                            startStopTrip();
                        }
                    }
                }
            }
        };

        repChosenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent data) {
                Log.i(TAG, "onReceive Rep chosen receiver was triggered!");
                if (data != null && data.hasExtra(FullscreenActivityChooseRep.CHOICE_RESULT)) {
                    if (tripToReassign != null) {
                        MediUser chosenUser =
                                data.getParcelableExtra(FullscreenActivityChooseRep.CHOICE_RESULT);
                        if (chosenUser != null) {
                            final MyProgressDialog progressDialog =
                                    new MyProgressDialog(getContext()
                                            , getString(R.string.reassign_trip_to_x, chosenUser.fullname));
                            progressDialog.show();

                            Request request = new Request(Request.Function.ASSIGN);
                            request.arguments.add(new Requests.Argument("entityname", "msus_fulltrip"));
                            request.arguments.add(new Requests.Argument("entityid", tripToReassign.tripGuid));
                            request.arguments.add(new Requests.Argument("assignto", chosenUser.systemuserid));
                            request.arguments.add(new Requests.Argument("asuser", MediUser.getMe().systemuserid));

                            new Crm().makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    MySqlDatasource ds = new MySqlDatasource(getContext());
                                    ds.deleteFulltrip(tripToReassign.getTripcode(), true);
                                    Toast.makeText(getContext(), getString(R.string.trip_was_reassigned)
                                            , Toast.LENGTH_SHORT).show();
                                    populateTripList();
                                    progressDialog.dismiss();
                                    tripToReassign = null;
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                    Toast.makeText(getContext(), getString(R.string.toast_failed_reassign_trip,
                                            error.getLocalizedMessage()), Toast.LENGTH_LONG).show();
                                    populateTripList();
                                    progressDialog.dismiss();
                                    tripToReassign = null;
                                }
                            });
                        }
                    }
                }
            }
        };

        tripSyncReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null && intent.getAction().equals(ServerTripSyncService.TRIP_SYNC_SERVICE)) {

                    Log.i(TAG, "onReceive (trip sync service broadcast)");

                    if (progressDialog == null) {
                        Log.i(TAG, "onReceive (trip sync service broadcast) | Had to create a progress dialog!");
                        progressDialog = new MyProgressDialog(getContext());
                        progressDialog.changeAlertType(MyProgressDialog.PROGRESS_TYPE);
                    }

                    if (intent.hasExtra(ServerTripSyncService.STARTED)) {
                        progressDialog = new MyProgressDialog(getContext());
                        progressDialog.setTitleText(getString(R.string.syncing_trips_title));
                        progressDialog.setContentText(getString(R.string.syncing_trips_body));
                        progressDialog.show();
                    } else if (intent.hasExtra(ServerTripSyncService.PROGRESS)) {
                        Log.i(TAG, "onReceive (trip sync service broadcast) | progress intent");
                        int curVal =
                                Objects.requireNonNull(intent.getIntegerArrayListExtra(ServerTripSyncService.PROGRESS)).get(0);
                        int totalVal =
                                Objects.requireNonNull(intent.getIntegerArrayListExtra(ServerTripSyncService.PROGRESS)).get(1);
                        progressDialog.setTitleText(getString(R.string.syncing_trips_title));
                        progressDialog.setContentText(getString(R.string.prog_x_of_y, curVal, totalVal));
                    } else if (intent.hasExtra(ServerTripSyncService.COMPLETED)) {
                        Log.i(TAG, "onReceive (trip sync service broadcast) | completed intent");
                        progressDialog.dismiss();
                        populateTripList();
                    } else if (intent.hasExtra(ServerTripSyncService.FAILED)) {
                        Log.i(TAG, "onReceive (trip sync service broadcast) | failed intent");
                        progressDialog.dismiss();
                        populateTripList();
                    }
                }
            }
        };

        requireActivity().registerReceiver(repChosenReceiver, repChosenFilter);

        root.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            //At this point the layout is complete and the dimensions and any child views are known.
            Log.i(TAG, "onGlobalLayout");

            // This check is here because having it in onResume() it would often result in showing
            // a dialog when the service isn't actually running and then will only go away if
            // manually dismissed (clicking/back pressed etc.).
            if (getContext() != null) {
                if (ServerTripSyncService.isRunning) {
                    if (progressDialog == null || progressDialog.isShowing()) {
                        progressDialog = new MyProgressDialog(getContext());
                    }
                    progressDialog.setTitleText(getString(R.string.syncing_trips_title));
                    progressDialog.setContentText(getString(R.string.syncing_trips_body));
                    progressDialog.show();
                } else {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                }
            }
        });

        if (!MediUser.isLoggedIn()) {
            Log.i(TAG, "onCreateView | NOT LOGGED IN!");
            requireActivity().finishAffinity();
            return root;
        }

        btnStartStop.setText((MyLocationService.isRunning) ? getString(R.string.stop_trip) : getString(R.string.start_trip));
        tripStatusContainer.setVisibility((MyLocationService.isRunning) ? View.VISIBLE : View.GONE);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        // This shit only works if the user actually exists in the database
        if (MediUser.getMe() != null && MediUser.getMe().systemuserid != null) {

            parseTripsForAssociations();
            populateTripList();
            // Do some mileage database maintenance
            datasource.deleteUnreferencedTripEntries(new MyInterfaces.TripDeleteCallback() {
                @Override
                public void onSuccess(int entriesDeleted) {
                    if (entriesDeleted > 0) {
                        Toast.makeText(getContext(), getString(R.string.toast_cleaned_unref_trips)
                                , Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(getContext(), getString(R.string.toast_failed_to_clean_unref_trips)
                            , Toast.LENGTH_SHORT).show();
                }
            });

            requireActivity().registerReceiver(locReceiver, locIntentFilter);
            Log.i(TAG, "onStart Registered the location receiver");
            Log.i(TAG, "onStart Registered the back press receiver.");

            if (MyLocationService.isRunning) {
                txtStatus.setText(getString(R.string.trip_running));
                MyLocationService.sendUpdateBroadcast(getContext());
                startTripDurationRunner();
            } else {
                txtStatus.setText(getString(R.string.trip_not_started));
            }

            this.requireView().setFocusableInTouchMode(true);
            this.requireView().requestFocus();
            this.requireView().setOnKeyListener((view, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (adapter.isInEditMode) {
                        setEditMode(false);
                        return true;
                    }
                }
                return false;
            });

            datasource.deleteEmptyTrips(true, new MyInterfaces.TripDeleteCallback() {
                @Override
                public void onSuccess(int entriesDeleted) {
                    if (entriesDeleted > 0) {
                        Log.w(TAG, "onSuccess: Deleted " + entriesDeleted + " empty trips.");
                        Toast.makeText(getContext(), getString(R.string.x_entries_deleted
                                , entriesDeleted), Toast.LENGTH_SHORT).show();
                        populateTripList();
                    }
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(getContext(), getString(R.string.failed_to_delete_trip, message)
                            , Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            if (options.getDebugMode()) {
                Toast.makeText(getContext(), getString(R.string.user_not_exist), Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            requireActivity().unregisterReceiver(repChosenReceiver);
            Log.i(TAG, "onDestroy | Unregistered the rep chosen receiver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            // Unregister broadcast receivers.
            requireActivity().unregisterReceiver(locReceiver);
            requireActivity().unregisterReceiver(miscReceiver);
            requireActivity().unregisterReceiver(tripSyncReceiver);

            tripDurationHandler.removeCallbacks(tripDurationRunner);
            Log.i(TAG, "onStop | Unregistered the location receiver");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if (!MediUser.isLoggedIn()) {
            try {
                options.logout();
                requireActivity().finishAffinity();
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < this.getParentFragmentManager().getBackStackEntryCount(); i++) {
            Log.i(TAG, "onCreateView: Backstack[" + i + "] name: " + this.getParentFragmentManager().getBackStackEntryAt(i).getId());
        }

        Helpers.Animations.pulseAnimation(txtMilesTotal);
        Helpers.Animations.pulseAnimation(txtMtd);
        startMtdTogglerRunner();

        try {
            requireActivity().registerReceiver(miscReceiver, allPurposeFilter);
            requireActivity().registerReceiver(tripSyncReceiver, tripSyncFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i(TAG, "onInflate | Sync service running = " + ServerTripSyncService.isRunning);

        // Defense against teh sync dialog being displayed when a sync actually isn't happening/has finished.
        if (!ServerTripSyncService.isRunning) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        stopMtdTogglerRunner();
    }

    @Override
    public void onItemClick(View view, int position) {
        FullTrip clickedTrip = locallySavedTrips.get(position);
        if (clickedTrip.isSeparator) {
            return;
        }
        if (!adapter.isInEditMode) {
            showTripOptions(clickedTrip);
        } else {
            for (FullTrip trip : adapter.mData) {
                if (trip.isChecked) {
                    btnDeleteTrips.setEnabled(true);
                    return;
                }
                btnDeleteTrips.setEnabled(false);
            }
        }
    }

    void startTripDurationRunner() {
        if (MyLocationService.isRunning) {
            try {
                tripDurationRunner = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            txtDuration.setText(getString(R.string.x_mins, MyLocationService.getTripDuration()));
                            tripDurationHandler.postDelayed(this, 1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                tripDurationRunner.run();
            } catch (java.lang.SecurityException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * This is a runnable that toggles between showing MTD mileage and the reimbursement from that mileage.
     * The transition between is animated.
     */
    void startMtdTogglerRunner() {
        mtdTogglerRunner = () -> {
            try {
                if (isShowingTotalMiles) {
                    Helpers.Animations.fadeOut(txtMilesTotal, 250, new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            isShowingTotalMiles = false;
                            Helpers.Animations.fadeIn(txtMtd, 250);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            txtMilesTotal.setVisibility(View.INVISIBLE);
                            txtMtd.setVisibility(View.VISIBLE);
                            mtdTogglerHandler.postDelayed(mtdTogglerRunner, 5000);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                } else {
                    Helpers.Animations.fadeOut(txtMtd, 250, new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            isShowingTotalMiles = true;
                            Helpers.Animations.fadeIn(txtMilesTotal, 250);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            txtMilesTotal.setVisibility(View.VISIBLE);
                            txtMtd.setVisibility(View.INVISIBLE);
                            mtdTogglerHandler.postDelayed(mtdTogglerRunner, 5000);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        mtdTogglerRunner.run();
    }

    void stopMtdTogglerRunner() {
        mtdTogglerHandler.removeCallbacks(mtdTogglerRunner);
    }

    void startStopTrip(@Nullable String tripname) {
        if (MediUser.getMe(getContext()) == null) {
            NavController navController =
                    Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_HomeFragment_to_HomeSecondFragment);
            Toast.makeText(getContext(), getString(R.string.toast_need_to_login), Toast.LENGTH_SHORT).show();
            return;
        }

        if (MyLocationService.isRunning) {
            if (MyLocationService.tripIsValid()) {
                if (options.getConfirmTripEnd()) {
                    MyYesNoDialog.show(getContext(), getString(R.string.are_you_sure_stop_trip),
                            new MyYesNoDialog.YesNoListener() {
                                @Override
                                public void onYes() {
                                    MyLocationService.userStoppedTrip = true;
                                    requireContext().stopService(new Intent(getContext(), MyLocationService.class));
                                }

                                @Override
                                public void onNo() {
                                }
                            });
                } else {
                    MyLocationService.userStoppedTrip = true;
                    requireContext().stopService(new Intent(getContext(), MyLocationService.class));
                }
            } else {
                requireContext().stopService(new Intent(getContext(), MyLocationService.class));
            }
        } else {
            if (options.getReimbursementRate() == 0) {
                try {
                    setMileageRate();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                // Check permission and request if not present
                if (validateLocPermissions()) {
                    // Set up the visuals
                    tripStatusContainer.setVisibility(View.VISIBLE);
                    btnStartStop.setText(getString(R.string.stop));
                    showGif(true);

                    // Start the actual service
                    Intent intent = new Intent(getContext(), MyLocationService.class);
                    intent.putExtra(MyLocationService.TRIP_PRENAME, tripname);
                    intent.putExtra(MyLocationService.USER_STARTED_TRIP_FLAG, true);
                    requireContext().startForegroundService(intent);
                    Log.d(TAG, "startMyLocService Sent start request...");
                    startTripDurationRunner();
                }
            }
        }

        populateTripList();

    }

    void startStopTrip() {
        startStopTrip(null);
    }

    public boolean validateLocPermissions() {
        String[] permissions;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION};
        } else {
            permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        }

        if (checkLocationPermission() != LocationPermissionResult.FULL) {

            boolean showRational =
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);
            if (showRational && checkLocationPermission() == LocationPermissionResult.NONE) {
                final Dialog dialog = new Dialog(requireActivity());
                dialog.setContentView(R.layout.generic_app_dialog);
                TextView txtMain = dialog.findViewById(R.id.txtMainText);
                txtMain.setText(getString(R.string.location_permissions_not_correct));
                Button btnOkay = dialog.findViewById(R.id.btnOkay);
                btnOkay.setOnClickListener(v -> {
                    Helpers.Application.openAppSettings(requireContext());
                    dialog.dismiss();
                });
                dialog.setTitle(getString(R.string.permissions));
                dialog.setCancelable(true);
                dialog.setOnKeyListener((dg, keyCode, event) -> {
                    if (keyCode != KeyEvent.KEYCODE_BACK) {
                        Helpers.Application.openAppSettings(requireContext());
                    }
                    dialog.dismiss();
                    return true;
                });
                dialog.show();
            }
            requestPermissions(permissions, PERMISSION_START_TRIP);
            return false;
        } else {
            return true;
        }
    }

    public void showGif(boolean showStartupAnimation) {

        int drawable;

        if (showStartupAnimation) {
            drawable = R.drawable.car2_static;
        } else {
            drawable = R.drawable.car2;
        }

        Uri path = Uri.parse(getString(R.string.car_gif_uri) + drawable);
        ImageView imageView = rootView.findViewById(R.id.gifview);
        Glide.with(requireContext()).load(path).into(imageView);

    }

    void editTrip(final FullTrip clickedTrip) {
        final Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.edit_trip);
        final Button btnChangeDate = dialog.findViewById(R.id.btnChangeDate);
        btnChangeDate.setText(clickedTrip.getPrettyDate());
        Button btnSaveTrip = dialog.findViewById(R.id.btnSaveEdits);
        final AutoCompleteTextView txtName =
                dialog.findViewById(R.id.autocomplete_EditText_NameTrip);
        final EditText txtDistance = dialog.findViewById(R.id.editTxt_Distance);
        final float originalMiles = clickedTrip.getDistanceInMiles();
        final long originalDate = clickedTrip.getDateTime().getMillis();
        final long[] newDate = {originalDate};
        txtDistance.setText(getString(R.string.original_miles, originalMiles));

        String[] tripnames = datasource.getTripNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>
                (requireContext(), android.R.layout.select_dialog_item, tripnames);
        txtName.setThreshold(1);
        txtName.setAdapter(adapter);
        txtName.setText(clickedTrip.getTitle());
        dialog.setTitle(getString(R.string.edit_trip));
        dialog.setCancelable(true);

        btnSaveTrip.setOnClickListener(v -> {
            float newMiles = Float.parseFloat(txtDistance.getText().toString());


            if (originalMiles != newMiles) {
                clickedTrip.setIsEdited(true);
                clickedTrip.setDistance(Helpers.Geo.convertMilesToMeters(newMiles, 1));
                clickedTrip.setMillis(newDate[0]);
                clickedTrip.setDateTime(new DateTime(newDate[0]));
                clickedTrip.save();
            }

            Log.i(TAG, "onClick Renaming trip..." + clickedTrip.getTitle());
            if (txtName.getText().length() > 0) {
                clickedTrip.setTitle(txtName.getText().toString());
                clickedTrip.save();
            }

            populateTripList();
            dialog.dismiss();
            dialogTripOptions.dismiss();
        });
        dialog.setOnKeyListener((dialog1, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                dialog1.dismiss();
                return true;
            } else {
                return false;
            }
        });

        btnChangeDate.setOnClickListener(v -> {
            DatePickerFragment newFragment =
                    new DatePickerFragment((selectedDate, selectedDateStr) -> {
                        btnChangeDate.setText(selectedDateStr);
                        clickedTrip.setDateTime(selectedDate);
                    });
            newFragment.setShowDate(clickedTrip.getDateTime());
            newFragment.show(requireActivity().getSupportFragmentManager(), "datePicker");
        });
        dialog.show();
    }

    public void setEditMode(boolean isChecked) {

        btnDeleteTrips.setEnabled(isChecked);
        toggleEditButton.setChecked(isChecked);
        btnDeleteTrips.setVisibility((isChecked) ? View.VISIBLE : View.INVISIBLE);

        if (adapter != null) {
            adapter.setEditModeEnabled(isChecked);
            if (isChecked) {
                for (FullTrip trip : adapter.mData) {
                    if (trip.isChecked) {
                        btnDeleteTrips.setEnabled(true);
                        return;
                    }
                    btnDeleteTrips.setEnabled(false);
                }
            }
        }
    }

    public void getUser(String email) {
        String query = CrmQueries.Users.getUser(email);
        Requests.Request request = new Requests.Request(Requests.Request.Function.GET);
        request.arguments.add(new Requests.Argument(null, query));
        Crm crm = new Crm();

        try {
            crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers,
                                      byte[] responseBody) {
                    String strResponse = new String(responseBody);
                    MediUser user = MediUser.createOne(strResponse);
                    user.save(getActivity());
                    Log.d(TAG, "onSuccess " + strResponse);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                                      byte[] responseBody, Throwable error) {
                    Log.w(TAG, "onFailure: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "onClick: " + e.getMessage());
        }
    }

    void deleteSelected() {

        new MyAsyncTask() {
            int deletedTrips;

            @Override
            public void onPreExecute() {
                showProgress(true, getString(R.string.deleting_trips));
            }

            @Override
            public void doInBackground() {
                MySqlDatasource ds = new MySqlDatasource();
                for (FullTrip trip : adapter.mData) {
                    if (trip.isChecked) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (ds.deleteFulltrip(trip.getTripcode(), true)) {
                            deletedTrips++;
                            reportProgress(getString(R.string.deleted_x_trips, deletedTrips));
                        } // if successful
                    } // if checked
                } // each trip
            } // doInBackground

            @Override
            public void onProgress(Object msg) {
                showProgress(true, msg.toString());
            }

            @Override
            public void onPostExecute() {
                showProgress(false, null);
                populateTripList();
                setEditMode(false);
                Toast.makeText(getContext(), getString(R.string.deleted_x_trips, deletedTrips)
                        , Toast.LENGTH_SHORT).show();
            }

        }.execute();

    }

    void setMileageRate() throws UnsupportedEncodingException {

        final MyProgressDialog dialog = new MyProgressDialog(getContext(),
                getString(R.string.getting_reimbursement_rate));
        dialog.show();

        MyLocationService.setReimbursementRate(new MyInterfaces.ReimbursementRateCallback() {
            @Override
            public void onSuccess(float rate) {
                dialog.dismiss();
                Toast.makeText(getContext(), getString(R.string.toast_updated_rate, options.getReimbursementRate())
                        , Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String message) {
                dialog.dismiss();
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    void showProgress(boolean show, String msg) {
        if (progressDialog == null) {
            progressDialog = new MyProgressDialog(getContext());
            progressDialog.setContentText(msg);
        }
        if (show) {
            progressDialog.show();
        } else {
            progressDialog.dismiss();
        }
    }

    void populateTripList() {

        datasource = new MySqlDatasource(getContext());

        boolean wasKilled = options.lastTripAutoKilled();
        boolean isRunning = MyLocationService.isRunning;
        if (wasKilled && !isRunning) {
            Log.i(TAG, "onReceive: Last trip was auto-killed and the service is not running!");
            tripStatusContainer.setVisibility(View.GONE);
            if (options.isExplicitMode()) {
                btnStartStop.setText(getString(R.string.go));
            } else {
                btnStartStop.setText(getString(R.string.start_trip));
            }
            options.lastTripAutoKilled(false);
        }


        txtMtd.setText("---");
        txtMilesTotal.setText("---");

        locallySavedTrips = new ArrayList<>();
        ArrayList<FullTrip> tempList;
        Log.i(TAG, "populateTripList Getting trips from database...");
        tempList = datasource.getTrips();
        Log.i(TAG, "populateTripList Trips retrieved from database");

        Log.i(TAG, "populateTripList Adding trips to the master list...");
        for (FullTrip trip : tempList) {
            if (!trip.getIsRunning()) {
                locallySavedTrips.add(trip);
            }
        }

        Log.i(TAG, "populateTripList Added: " + locallySavedTrips.size() + " trips to the master list.");

        ArrayList<FullTrip> triplist = new ArrayList<>();

        boolean addedTodayHeader = false;
        boolean addedYesterdayHeader = false;
        boolean addedThisWeekHeader = false;
        boolean addedThisMonthHeader = false;
        boolean addedOlderHeader = false;

        // Now today's date attributes
        int todayDayOfYear = Helpers.DatesAndTimes.returnDayOfYear(DateTime.now());
        int todayWeekOfYear = Helpers.DatesAndTimes.returnWeekOfYear(DateTime.now());
        int todayMonthOfYear = Helpers.DatesAndTimes.returnMonthOfYear(DateTime.now());

        Log.i(TAG, "populateTripList: Preparing the dividers and trips...");
        for (int i = 0; i < (locallySavedTrips.size()); i++) {
            int tripDayOfYear =
                    Helpers.DatesAndTimes.returnDayOfYear(locallySavedTrips.get(i).getDateTime());
            int tripWeekOfYear =
                    Helpers.DatesAndTimes.returnWeekOfYear(locallySavedTrips.get(i).getDateTime());
            int tripMonthOfYear =
                    Helpers.DatesAndTimes.returnMonthOfYear(locallySavedTrips.get(i).getDateTime());

            // Trip was today
            if (tripDayOfYear == todayDayOfYear) {
                if (!addedTodayHeader) {
                    FullTrip headerObj = new FullTrip();
                    headerObj.isSeparator = true;
                    headerObj.setTitle(options.isExplicitMode() ? requireContext().getString(R.string
                            .triplist_today_explicit) : requireContext().getString(R.string.today));
                    triplist.add(headerObj);
                    addedTodayHeader = true;
                    Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'Today' - This will not be added again!");
                }
                // Trip was yesterday
            } else if (tripDayOfYear == (todayDayOfYear - 1)) {
                if (!addedYesterdayHeader) {
                    FullTrip headerObj = new FullTrip();
                    headerObj.isSeparator = true;
                    headerObj.setTitle(options.isExplicitMode() ? requireContext().getString(R.string.triplist_yesterday_explicit) : requireContext().getString(R.string.yesterday));
                    triplist.add(headerObj);
                    addedYesterdayHeader = true;
                    Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'Yesterday' - This will not be added again!");
                }

                // Trip was this week
            } else if (tripWeekOfYear == todayWeekOfYear) {
                if (!addedThisWeekHeader) {
                    FullTrip headerObj = new FullTrip();
                    headerObj.isSeparator = true;
                    headerObj.setTitle(options.isExplicitMode() ? requireContext().getString(R.string.triplist_this_week_explicit) : requireContext().getString(R.string.this_week));
                    triplist.add(headerObj);
                    addedThisWeekHeader = true;
                    Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'This week' - This will not be added again!");
                }

                // Trip was this month
            } else if (tripMonthOfYear == todayMonthOfYear) {
                if (!addedThisMonthHeader) {
                    FullTrip headerObj = new FullTrip();
                    headerObj.isSeparator = true;
                    headerObj.setTitle(options.isExplicitMode() ? requireContext().getString(R.string.triplist_this_month_explicit) : requireContext().getString(R.string.triplist_this_month));
                    triplist.add(headerObj);
                    addedThisMonthHeader = true;
                    Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'This month' - This will not be added again!");
                }

                // Trip was older than this month
            } else if (tripMonthOfYear < todayMonthOfYear) {
                if (!addedOlderHeader) {
                    FullTrip headerObj = new FullTrip();
                    headerObj.isSeparator = true;
                    headerObj.setTitle(options.isExplicitMode() ? requireContext().getString(R.string.triplist_last_month_and_older_explicit) : requireContext().getString(R.string.last_month_and_older));
                    triplist.add(headerObj);
                    addedOlderHeader = true;
                    Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'Older' - This will not be added again!");
                }
            }
            triplist.add(locallySavedTrips.get(i));
        }

        Log.i(TAG, "populateTripList Finished preparing the dividers and trips.");

        // Since the new arraylist now has headers it will throw off the original array's indexes
        locallySavedTrips = triplist;
        adapter = new TripListRecyclerAdapter(getContext(), triplist, this);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        // Tally the MTD reimbursement
        float mtdTotal = 0;
        float mtdMilesTotal = 0;
        for (FullTrip trip : locallySavedTrips) {
            // Only submitted trips
            if (trip.getIsSubmitted()) {
                // Only tally this month's trips
                int thisMonth = DateTime.now().getMonthOfYear();
                int thisYear = DateTime.now().getYear();
                if (trip.getDateTime().getMonthOfYear() == thisMonth
                        && trip.getDateTime().getYear() == thisYear) {
                    mtdMilesTotal += trip.getDistanceInMiles();
                }
            }
        }

        mtdMilesTotal = Helpers.Numbers.formatAsZeroDecimalPointNumber(mtdMilesTotal);
        mtdTotal += Helpers.Numbers.formatAsTwoDecimalPointNumber(
                mtdMilesTotal * options.getReimbursementRate());

        animatedNumberTextView = new MyAnimatedNumberTextView(getContext(), txtMtd);
        if (lastMtdValue > 0) {
            animatedNumberTextView.setNewValue(mtdTotal, lastMtdValue, true);
        } else {
            txtMtd.setText(Helpers.Numbers.convertToCurrency(mtdTotal));
        }

        lastMtdValue = Math.round(mtdTotal);
        double lastMtdMilesValue = Helpers.Numbers.formatAsOneDecimalPointNumber(mtdMilesTotal);

        txtMilesTotal.setText(getString(R.string.x_miles, Helpers.Numbers.formatAsZeroDecimalPointNumber(lastMtdMilesValue)));

        manageDefaultImage();

        Log.i(TAG, "populateTripList Deleting empty trips from the database.");
        datasource.deleteEmptyTrips(true, new MyInterfaces.TripDeleteCallback() {
            @Override
            public void onSuccess(int entriesDeleted) {
                Log.i(TAG, "onSuccess Deleted " + entriesDeleted + " empty trips.");
                manageDefaultImage();
            }

            @Override
            public void onFailure(String message) {
                Log.i(TAG, "onFailure Failed to delete empty trips\n" + message);
                manageDefaultImage();
            }
        });

        toggleEditButton.setEnabled(triplist.size() > 0);

    }

    void showTripOptions(final FullTrip clickedTrip) {
        // custom dialog
        final Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.trip_options_minimalist);
        dialog.setTitle(getString(R.string.trip_options));
        Button btnEditTrip = dialog.findViewById(R.id.edit_trip);
        Button btnDeleteTrip = dialog.findViewById(R.id.btnDelete);
        // Hidden - having this button here doesn't fit with the current design and flow
        Button btnOpportunity = dialog.findViewById(R.id.btn_opportunities);
        btnOpportunity.setOnClickListener(v -> {
            if (clickedTrip.getIsSubmitted()) {
                Intent intent = new Intent(getContext(), FullscreenActivityChooseOpportunity.class);
                intent.putExtra(FullscreenActivityChooseOpportunity.FULLTRIP, clickedTrip);
                startActivityForResult(intent, FullscreenActivityChooseOpportunity.REQUESTCODE);
            } else {
                Toast.makeText(getContext(), getString(R.string.toast_you_must_submit)
                        , Toast.LENGTH_SHORT).show();
            }
        });
        Button btnSubmit = dialog.findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(v -> {
            try {
                dialog.dismiss();

                if (clickedTrip.getDistanceInMiles() <= 2) {
                    datasource.deleteEmptyTrips(true, new MyInterfaces.TripDeleteCallback() {
                        @Override
                        public void onSuccess(int entriesDeleted) {
                            if (entriesDeleted > 0) {
                                Log.w(TAG, "onSuccess: Deleted " + entriesDeleted + " empty trips.");
                                Toast.makeText(getContext(), getString(R.string.remove_x_trips
                                        , entriesDeleted), Toast.LENGTH_SHORT).show();
                                populateTripList();
                            }
                        }

                        @Override
                        public void onFailure(String message) {
                            Toast.makeText(getContext(), getString(R.string.failed_to_delete_trip
                                    , message), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                isTripSubmitted(clickedTrip);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i(TAG, "onClick Prepared trip for CRM");
        });

        Button btnRecall = dialog.findViewById(R.id.unsubmit_trip);
        btnRecall.setOnClickListener(v -> {
            try {
                dialog.dismiss();
                unsubmitTrip(clickedTrip);
            } catch (UnsupportedEncodingException e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });

        Button btn_ViewTrip = dialog.findViewById(R.id.view_trip);
        btn_ViewTrip.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ViewTripActivity.class);
            intent.putExtra(ViewTripActivity.CLICKED_TRIP, clickedTrip);
            startActivity(intent);
            dialog.dismiss();
        });

        btnEditTrip.setOnClickListener(v -> {
            editTrip(clickedTrip);
            dialog.dismiss();
        });

        btnDeleteTrip.setOnClickListener(v ->
                MyYesNoDialog.show(getContext(), getString(R.string.are_you_sure_delete_trips_yn),
                        new MyYesNoDialog.YesNoListener() {
                            @Override
                            public void onYes() {
                                if (datasource.deleteFulltrip(clickedTrip.getTripcode(), true)) {
                                    Toast.makeText(getContext(), getString(R.string.toast_trips_deleted)
                                            , Toast.LENGTH_SHORT).show();
                                    populateTripList();
                                    dialog.dismiss();
                                }
                            }

                            @Override
                            public void onNo() {
                                Toast.makeText(getContext(), getString(R.string.cancelled)
                                        , Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }));

        btnEditTrip.setEnabled(!clickedTrip.getIsSubmitted());

        Button btnReassign = dialog.findViewById(R.id.assign_trip);
        btnReassign.setEnabled(MediUser.getMe().isAdmin());
        btnReassign.setOnClickListener(v -> {
            if (!clickedTrip.getIsSubmitted()) {
                Toast.makeText(getContext(), getString(R.string.toast_you_must_submit)
                        , Toast.LENGTH_LONG).show();
                return;
            }

            tripToReassign = clickedTrip;
            dialog.dismiss();
            FullscreenActivityChooseRep.showRepChooser(requireActivity(), MediUser.getMe());
        });

        if (options.isExplicitMode()) {
            btn_ViewTrip.setText(requireContext().getString(R.string.tripOptions_view_explicit));
            btnSubmit.setText(requireContext().getString(R.string.tripOptions_submit_explicit));
            btnRecall.setText(requireContext().getString(R.string.tripOptions_recall_explicit));
            btnEditTrip.setText(requireContext().getString(R.string.tripOptions_edit_explicit));
            btnDeleteTrip.setText(requireContext().getString(R.string.tripOptions_delete_explicit));
        } else {
            btn_ViewTrip.setText(requireContext().getString(R.string.tripOptions_view));
            btnSubmit.setText(requireContext().getString(R.string.tripOptions_submit));
            btnRecall.setText(requireContext().getString(R.string.tripOptions_recall));
            btnEditTrip.setText(requireContext().getString(R.string.tripOptions_edit));
            btnDeleteTrip.setText(requireContext().getString(R.string.tripOptions_delete));
        }

        dialogTripOptions = dialog;
        dialog.show();
    }

    /**
     * Algorithm to determine whether or not to show "Click Here" image just above start trip btn.
     */
    void manageDefaultImage() {
        setEditMode(false);
        if (!MyLocationService.isRunning) {
            emptyTripList.setVisibility((locallySavedTrips == null || locallySavedTrips.size()
                    <= SHOW_CLICK_HERE_TRIP_COUNT_THRESHOLD) ? View.VISIBLE : View.GONE);
        } else {
            emptyTripList.setVisibility(View.GONE);
        }

        Helpers.Animations.pulseAnimation(emptyTripList, 1.05f, 1.02f, 9000, 350);

    }

    /**
     * Not too happy with how this worked out but it tries to evaluate all trips proximity to
     * opportunities in CRM.
     */
    void parseTripsForAssociations() {

        String query = CrmQueries.TripAssociation.getAssociationsLastXMonths(3);

        if (locallySavedTrips != null && locallySavedTrips.size() > 0) {
            // String query = Queries.TripAssociation.getAssociationsLastXMonths(3);
            ArrayList<Requests.Argument> args = new ArrayList<>();
            args.add(new Requests.Argument("query", query));
            Request request = new Request(Request.Function.GET, args);
            Crm crm = new Crm();
            Log.i(TAG, "parseTripsForAssociations - querying server...");
            crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody);
                    TripAssociations associations = new TripAssociations(response);
                    for (FullTrip trip : locallySavedTrips) {
                        if (associations.getAssociation(trip) != null) {
                            Log.i(TAG, "onSuccess Found an association!");
                            trip.hasAssociations(true);
                            trip.save();
                        }
                    }
                    Log.i(TAG, "onSuccess Finished parsing " + associations.list.size() + " associations for " +
                            locallySavedTrips.size() + " trips.");
                    try {
                        populateTripList();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.w(TAG, "onFailure: " + error.getLocalizedMessage());
                }
            });
        } else {
            Log.i(TAG, "parseTripsForAssociations Not enough local trips to check for associations!");
        }
    }

    public void getAllAccountAddresses() {
        Requests.Argument argument =
                new Requests.Argument("query", CrmQueries.Addresses.getAllAccountAddresses());
        ArrayList<Requests.Argument> args = new ArrayList<>();
        args.add(argument);
        Request request = new Request(Request.Function.GET, args);
        Crm crm = new Crm();
        crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // Construct an array of CrmAddresses
                String response = new String(responseBody);
                CrmEntities.CrmAddresses addresses = new CrmEntities.CrmAddresses(response);
                options.saveAllCrmAddresses(addresses);
                Log.i(TAG, "onSuccess response: " + response.length());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.w(TAG, "onFailure: error: " + error.getLocalizedMessage());
            }
        });
    }

    public LocationPermissionResult checkLocationPermission() {

        // Check if device has permission for fine and bg locations.
        int permFineLoc = requireActivity().checkCallingOrSelfPermission(Helpers
                .Permissions.Permission.ACCESS_FINE_LOCATION);
        int permBgLoc = requireActivity().checkCallingOrSelfPermission(Helpers
                .Permissions.Permission.ACCESS_BACKGROUND_LOCATION);

        // Convert the results to a LocationPermissionResult object
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (permFineLoc == PackageManager.PERMISSION_GRANTED &&
                    permBgLoc == PackageManager.PERMISSION_GRANTED) {
                return MyApp.LocationPermissionResult.FULL;
            } else if (permFineLoc == PackageManager.PERMISSION_GRANTED ||
                    permBgLoc == PackageManager.PERMISSION_GRANTED) {
                return LocationPermissionResult.PARTIAL;
            } else {
                return LocationPermissionResult.NONE;
            }
        } else {
            if (permFineLoc == PackageManager.PERMISSION_GRANTED) {
                return LocationPermissionResult.FULL;
            } else {
                return LocationPermissionResult.NONE;
            }
        }
    }

    public boolean checkStoragePermission() {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = requireActivity().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Deprecated - Now using a dedicated service to perform trip sync.
     */
    @SuppressWarnings("unused")
    public void syncTrips() {

        final MyProgressDialog dialog = new MyProgressDialog(getContext(), getString(R.string
                .getting_server_trips), MyProgressDialog.PROGRESS_TYPE);
        dialog.show();
        Log.i(TAG, "syncTrips Beginning sync...");

        Crm crm = new Crm();
        Request request = new Request();
        request.function = Request.Function.GET.name();
        request.arguments.add(new Requests.Argument("query", CrmQueries.Trips
                .getAllTripsByOwnerForLastXmonths(2, MediUser.getMe().systemuserid)));

        // Log a metric
        MileBuddyMetrics.updateMetric(getContext(), MileBuddyMetrics.MetricName
                .LAST_ACCESSED_MILEAGE_SYNC, DateTime.now());

        crm.makeCrmRequest(getContext(), request, Crm.Timeout.LONG, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                final String response = new String(responseBody);

                Log.i(TAG, "onSuccess Sync returned: " + response);

                // Mark all locals as unsubmitted - this should get corrected when we loop through the server trips...
                if (responseBody != null && locallySavedTrips != null && locallySavedTrips.size() > 0) {
                    for (FullTrip trip : locallySavedTrips) {
                        if (!trip.isSeparator) {
                            trip.setIsSubmitted(false);
                            trip.save();
                            Log.i(TAG, "Unsubmitted local trip: " + trip.getTitle());
                        }
                    }
                }

                // Loop through the server trips and compare to locals.
                new MyAsyncTask(getString(R.string.syncing_trips_thread_name)) {
                    @Override
                    public void onPreExecute() {
                    }

                    @Override
                    public void doInBackground() {
                        ArrayList<FullTrip> serverTrips = FullTrip.createTripsFromCrmJson(response, false);
                        int totalTrips = serverTrips.size();
                        int i = 1;
                        for (FullTrip serverTrip : serverTrips) {
                            Log.i(TAG, "onSuccess Updating local trip with server trip...");
                            reportProgress(new int[]{i, totalTrips});
                            updateLocalTrip(serverTrip);
                            i++;
                        }
                    }

                    @Override
                    public void onProgress(Object msg) {
                        int[] p = (int[]) msg;
                        int curVal = p[0];
                        int totalVal = p[1];
                        dialog.setContentText(getString(R.string.syncd_x_of_y, curVal, totalVal));
                        if (curVal == totalVal - 1) {
                            dialog.setContentText(getString(R.string.refreshing_list));
                        }
                    }

                    @Override
                    public void onPostExecute() {
                        Log.i(TAG, "onSuccess Local trips were synced");
                        populateTripList();
                        dialog.dismiss();
                    }

                }.execute();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    /**
     * Overwrites or creates a local trip with the server-side version
     *
     * @param serverTrip A FullTrip constructed using server-side json
     */
    void updateLocalTrip(FullTrip serverTrip) {
        for (FullTrip trip : locallySavedTrips) {
            if (trip.getTripcode() == serverTrip.getTripcode()) {
                trip = serverTrip;
                trip.save();
                Log.i(TAG, "updateLocalTrip Local trip was updated.");
                return;
            }
        }
        serverTrip.save();
        Log.i(TAG, "updateLocalTrip Local trip was created.");
    }

    public void isTripSubmitted(final FullTrip clickedTrip) {

        Crm crm = new Crm();

        String query = CrmQueries.Trips.getTripidByTripcode(clickedTrip.getTripcode());

        Request request = new Request();
        request.function = Request.Function.GET.name();
        request.arguments.add(new Requests.Argument("query", query));

        final MyProgressDialog dialog = new MyProgressDialog(getContext(), getString(R.string
                .check_trip_submitted), MyProgressDialog.PROGRESS_TYPE);
        dialog.show();

        crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                try {
                    if (!new JSONObject(result).getJSONArray("value").isNull(0)) {
                        String guid = new JSONObject(result).getJSONArray("value")
                                .getJSONObject(0).getString("msus_fulltripid");
                        clickedTrip.setTripGuid(guid);
                        clickedTrip.setIsSubmitted(true);
                        clickedTrip.save();
                        populateTripList();
                        Toast.makeText(getContext(), getString(R.string.toast_trip_updated)
                                , Toast.LENGTH_SHORT).show();
                        dialog.dismiss();

                        TripAssociationManager.manageTripAssociations(clickedTrip, new MyInterfaces.CreateManyListener() {
                            @Override
                            public void onResult(CrmEntities.CreateManyResponses responses) {
                                if (options.getDebugMode()) {
                                    Toast.makeText(getContext(), getString(R.string.created_x_associations
                                            , responses.responses.size()), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onError(String msg) {
                                if (options.getDebugMode()) {
                                    Toast.makeText(getContext(), getString(R.string
                                            .failed_to_associate_x_trips, msg), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } else {
                        try {
                            dialog.dismiss();
                            submitTrip(clickedTrip);
                        } catch (Exception e) {
                            dialog.dismiss();
                            Toast.makeText(getContext(), getString(R.string
                                    .toast_failed_to_submit_trip), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    dialog.dismiss();
                    e.printStackTrace();
                }

                Log.i(TAG, "onSuccess " + result);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.w(TAG, "onFailure: " + error.getMessage());
                Toast.makeText(getContext(), getString(R.string.toast_failed_to_verify_trip), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    public void submitTrip(final FullTrip clickedTrip) throws UnsupportedEncodingException {

        Crm crm = new Crm();
        Request request = clickedTrip.packageForCrm();

        if (request == null) {
            Toast.makeText(getContext(), getString(R.string.toast_trip_invalid), Toast.LENGTH_LONG).show();
            return;
        }

        final MyProgressDialog dialog = new MyProgressDialog(getContext(), getString(R.string
                .submitting_trip), MyProgressDialog.PROGRESS_TYPE);
        dialog.show();

        Log.i(TAG, "submitTrip JSON LENGTH: " + clickedTrip.tripEntriesJson.length());

        crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                try {
                    if (!new JSONObject(result).isNull("Guid")) {
                        clickedTrip.setIsSubmitted(true);
                        clickedTrip.setTripGuid(new JSONObject(result).getString("Guid"));
                        clickedTrip.setIsSubmitted(true);
                        clickedTrip.save();
                        populateTripList();
                        TripAssociationManager.manageTripAssociations(clickedTrip, new MyInterfaces.CreateManyListener() {
                            @Override
                            public void onResult(CrmEntities.CreateManyResponses responses) {
                                if (options.getDebugMode()) {
                                    Toast.makeText(getContext(), getString(R.string.created_x_associations
                                            , responses.responses.size()), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onError(String msg) {
                                if (options.getDebugMode()) {
                                    Toast.makeText(getContext(), getString(R.string
                                            .failed_to_associate_x_trips, msg), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    dialog.dismiss();
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                Log.i(TAG, "onSuccess " + new String(responseBody));
                Toast.makeText(getContext(), getString(R.string.toast_trip_submitted), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                if (error.getClass() == CrmRequestExceptions.NullRequestException.class) {
                    Toast.makeText(getContext(), getString(R.string.toast_trip_probably_invalid)
                            , Toast.LENGTH_LONG).show();
                    Toast.makeText(getContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Log.w(TAG, "onFailure: " + new String(responseBody));
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
    }

    public void unsubmitTrip(final FullTrip clickedTrip) throws UnsupportedEncodingException {

        final String tripGuid = clickedTrip.tripGuid;

        Crm crm = new Crm();
        Request request = new Request();
        request.function = Request.Function.DELETE.name();
        request.arguments.add(new Requests.Argument("entity", "msus_fulltrip"));
        request.arguments.add(new Requests.Argument("entityid", tripGuid));
        request.arguments.add(new Requests.Argument("asuserid", MediUser.getMe().systemuserid));

        final MyProgressDialog dialog = new MyProgressDialog(getContext(),
                getString(R.string.recalling_trip), MyProgressDialog.PROGRESS_TYPE);
        dialog.show();

        crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                try {
                    JSONObject object = new JSONObject(result);
                    if (object.getBoolean("WasSuccessful") ||
                            object.getString("ResponseMessage").toLowerCase().contains("not exist")) {
                        clickedTrip.setIsSubmitted(false);
                        clickedTrip.hasAssociations(false);
                        clickedTrip.setTripGuid(null);
                        clickedTrip.save();
                        populateTripList();
                    }
                } catch (Exception e) {
                    dialog.dismiss();
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                Log.i(TAG, "onSuccess " + new String(responseBody));
                Toast.makeText(getContext(), getString(R.string.toast_recalled_trip), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.w(TAG, "onFailure: " + error.getMessage());
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    void showNameTrip() {

        ArrayList<FullTrip> fullTrips = new MySqlDatasource().getTrips();
        String[] names = new String[fullTrips.size()];

        for (int i = 0; i < fullTrips.size(); i++) {
            names[i] = fullTrips.get(i).getTitle();
        }

        final Dialog dialog = new Dialog(requireContext());

        dialog.setContentView(R.layout.rename_trip);
        final AutoCompleteTextView actv = dialog.findViewById(R.id.autocomplete_EditText_NameTrip);
        Button startButton = dialog.findViewById(R.id.btnSaveEdits);
        dialog.setCancelable(true);
        startButton.setOnClickListener(v -> {
            if (actv.getText().length() > 0) {
                startStopTrip(actv.getText().toString());
            } else {
                startStopTrip();
            }
            dialog.dismiss();
        });

        dialog.setOnKeyListener((d, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                dialog.dismiss();
                return true;
            } else {
                return false;
            }
        });

        //Creating the instance of ArrayAdapter containing list of fruit names
        ArrayAdapter<String> adapter = new ArrayAdapter<>
                (requireContext(), android.R.layout.select_dialog_item, names);
        //Getting the instance of AutoCompleteTextView
        actv.setThreshold(0);//will start working from first character
        actv.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
        actv.setTextColor(Color.RED);

        dialog.show();
    }

    void showReceiptDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.make_receipt);
        dialog.setCancelable(true);
        Button btnThisMonth = dialog.findViewById(R.id.btnThisMonth);
        Button btnLastMonth = dialog.findViewById(R.id.btnLastMonth);
        Button btnChoose = dialog.findViewById(R.id.btnChooseMonth);

        btnThisMonth.setOnClickListener(v -> {
            doActualReceipt(DateTime.now().getMonthOfYear(), DateTime.now().getYear());
            dialog.dismiss();
        });
        btnLastMonth.setOnClickListener(v -> {
            DateTime lastMonth = DateTime.now().minusMonths(1);
            doActualReceipt(lastMonth.getMonthOfYear(), lastMonth.getYear());
            dialog.dismiss();
        });
        btnChoose.setOnClickListener(v -> {
            showReceiptMonthPicker();
            dialog.dismiss();
        });
        dialog.setOnKeyListener((d, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                d.dismiss();
                return true;
            } else {
                return false;
            }
        });
        dialog.show();
    }

    void doActualReceipt(int month, int year) {

        // Log a metric
        MileBuddyMetrics.updateMetric(getContext(), MileBuddyMetrics.MetricName
                .LAST_ACCESSED_GENERATE_RECEIPT, DateTime.now());

        File receipt = makeReceiptFile(month, year);
        if (receipt != null) {
            MediUser me = MediUser.getMe();
            String[] recipients = new String[2];
            recipients[0] = me.email;
            recipients[1] = REIMBURSEMENT_EMAIL;
            Helpers.Email.sendEmail(recipients, getString(R.string.receipt_mail_body),
                    getString(R.string.receipt_mail_subject, month, year)
                    , getContext(), receipt, false);
        } else {
            Toast.makeText(getContext(), getString(R.string.toast_no_trips_found)
                    , Toast.LENGTH_SHORT).show();
        }
    }

    public File makeReceiptFile(int monthNum, int year) {
        MySqlDatasource ds = new MySqlDatasource();
        List<FullTrip> allTrips = ds.getTrips(monthNum, year, true);
        String fName = getString(R.string.receipt_filename, monthNum, year);
        File txtFile = new File(Helpers.Files.ReceiptTempFiles.getDirectory(), fName);
        File finalReceiptFile;

        float tripAmt;
        int tripCount = 0;
        float totalMiles = 0f;

        try {
            FileOutputStream stream = new FileOutputStream(txtFile);
            StringBuilder stringBuilder = new StringBuilder();
            try {
                stringBuilder.append(getString(R.string.receipt_line1));
                stringBuilder.append((getString(R.string.receipt_line2, MediUser.getMe().fullname)));
                stringBuilder.append(getString(R.string.receipt_line3));
                for (FullTrip trip : allTrips) {
                    if (trip.getIsSubmitted()) {
                        tripCount += 1;
                        tripAmt = trip.calculateReimbursement();
                        totalMiles += trip.getDistanceInMiles();

                        stringBuilder.append(getString(R.string.receipt_trip_line1
                                , Helpers.DatesAndTimes.getPrettyDate(trip.getDateTime())));
                        stringBuilder.append(getString(R.string.receipt_trip_line2
                                , trip.getDistanceInMiles()));
                        stringBuilder.append(getString(R.string.receipt_trip_line3
                                , Helpers.Numbers.convertToCurrency(tripAmt)));
                        stringBuilder.append(getString(R.string.receipt_trip_line4
                                , Boolean.toString(trip.getIsEdited())));
                        stringBuilder.append(getString(R.string.receipt_trip_line5
                                , Boolean.toString(trip.getIsManualTrip())));
                    }
                }

                totalMiles =
                        Helpers.Numbers.formatAsZeroDecimalPointNumber(totalMiles, RoundingMode.HALF_UP);
                stringBuilder.append(getString(R.string.receipt_line4, tripCount));
                stringBuilder.append(getString(R.string.receipt_line5, Helpers.Numbers
                        .convertToCurrency(totalMiles * options.getReimbursementRate())));
                stringBuilder.append(getString(R.string.receipt_line6, totalMiles));

                stream.write(stringBuilder.toString().getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }

            try {
                if (options.getReceiptFormat().equals(MyPreferencesHelper.RECEIPT_FORMAT_PNG)) {
                    finalReceiptFile =
                            Helpers.Bitmaps.createPngFileFromString(stringBuilder.toString(), fName);
                } else if (options.getReceiptFormat().equals(MyPreferencesHelper.RECEIPT_FORMAT_JPEG)) {
                    finalReceiptFile =
                            Helpers.Bitmaps.createJpegFileFromString(stringBuilder.toString(), fName);
                } else {
                    finalReceiptFile = txtFile;
                }
                return finalReceiptFile;
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), getString(R.string.toast_failed_to_create_reciept_image)
                        , Toast.LENGTH_SHORT).show();
                return txtFile;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressLint("NewApi")
    private void showReceiptMonthPicker() {
        MonthYearPickerDialog mpd = new MonthYearPickerDialog();
        mpd.setListener(((view, year, month, dayOfMonth) -> confirmReceipt(month, year)));
        mpd.show(getParentFragmentManager(), "MonthYearPickerDialog");
    }

    private void confirmReceipt(final int month, final int year) {
        MySqlDatasource mileage = new MySqlDatasource();
        List<FullTrip> tripsThisMonth = mileage.getTrips(month, year);
        int subbed = 0;
        for (FullTrip t : tripsThisMonth) {
            if (t.getIsSubmitted()) {
                subbed += 1;
            }
        }

        if (subbed == 0) {
            Toast.makeText(getContext(), getString(R.string.toast_no_trips_found), Toast.LENGTH_SHORT).show();
            return;
        }

        if ((subbed > 0)) {
            doActualReceipt(month, year);
        }
    }

}






















































