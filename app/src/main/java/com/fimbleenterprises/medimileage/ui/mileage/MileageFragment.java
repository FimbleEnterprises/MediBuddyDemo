package com.fimbleenterprises.medimileage.ui.mileage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cz.msebera.android.httpclient.Header;

import com.bumptech.glide.Glide;
import com.fimbleenterprises.medimileage.MyApp;
import com.fimbleenterprises.medimileage.activities.Activity_ManualTrip;
import com.fimbleenterprises.medimileage.Crm;
import com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities;
import com.fimbleenterprises.medimileage.dialogs.DatePickerFragment;
import com.fimbleenterprises.medimileage.objects_and_containers.FullTrip;
import com.fimbleenterprises.medimileage.fullscreen_pickers.FullscreenActivityChooseOpportunity;
import com.fimbleenterprises.medimileage.fullscreen_pickers.FullscreenActivityChooseRep;
import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.objects_and_containers.LocationContainer;
import com.fimbleenterprises.medimileage.activities.MainActivity;
import com.fimbleenterprises.medimileage.objects_and_containers.MediUser;
import com.fimbleenterprises.medimileage.objects_and_containers.MileBuddyMetrics;
import com.fimbleenterprises.medimileage.dialogs.MonthYearPickerDialog;
import com.fimbleenterprises.medimileage.ui.CustomViews.MyAnimatedNumberTextView;
import com.fimbleenterprises.medimileage.services.MyAttachmentUploadService;
import com.fimbleenterprises.medimileage.MyInterfaces;
import com.fimbleenterprises.medimileage.services.MyLocationService;
import com.fimbleenterprises.medimileage.dialogs.MyProgressDialog;
import com.fimbleenterprises.medimileage.MyPreferencesHelper;
import com.fimbleenterprises.medimileage.MySpeedoGauge;
import com.fimbleenterprises.medimileage.MySqlDatasource;
import com.fimbleenterprises.medimileage.dialogs.MyYesNoDialog;
import com.fimbleenterprises.medimileage.CrmQueries;
import com.fimbleenterprises.medimileage.QueryFactory;
import com.fimbleenterprises.medimileage.QueryFactory.Filter;
import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.objects_and_containers.Requests;
import com.fimbleenterprises.medimileage.objects_and_containers.Requests.Request;
import com.fimbleenterprises.medimileage.TripAssociationManager;
import com.fimbleenterprises.medimileage.adapters.TripListRecyclerAdapter;
import com.fimbleenterprises.medimileage.activities.ViewTripActivity;
import static com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities.TripAssociations;
import static com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities.TripAssociations.TripAssociation;
import static com.fimbleenterprises.medimileage.MyApp.LocationPermissionResult;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.scwang.smart.refresh.header.MaterialHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MileageFragment extends Fragment implements TripListRecyclerAdapter.ItemClickListener {

    public static final int PERMISSION_MAKE_RECEIPT = 0;
    public static final int PERMISSION_START_TRIP = 1;
    public static final int PERMISSION_MAKE_TRIP = 2;
    public static final int PERMISSION_UPDATE = 3;
    public static final int PERMISSION_FLOATY = 4;

    public static final String GENERIC_RECEIVER_ACTION = "GENERIC_RECEIVER_ACTION";
    public static final String MAKE_RECEIPT = "MAKE_RECEIPT";
    public static final String REIMBURSEMENT_EMAIL = "receipts@concur.com";

    private static final String TAG = "MileageFragment";
    private static final int SHOW_CLICK_HERE_TRIP_COUNT_THRESHOLD = 8;
    private MileageViewModel mileageViewModel;
    BroadcastReceiver locReceiver;
    BroadcastReceiver allPurposeReceiver;
    IntentFilter allPurposeFilter = new IntentFilter(GENERIC_RECEIVER_ACTION);
    MySqlDatasource datasource;
    TripListRecyclerAdapter adapter;
    ArrayList<FullTrip> allTrips = new ArrayList<>();
    RecyclerView recyclerView;
    public static int SERVICE_ID = 200;
    IntentFilter locFilter = new IntentFilter(MyLocationService.LOCATION_EVENT);
    public static final int DEFAULT_FONT_COLOR = -1979711488;
    MyPreferencesHelper options;
    ImageView emptyTripList;
    boolean isStarting = true;
    public static View rootView;
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
    Button btnSubmitTrips;
    ImageView gif_view;
    Button btnAddManualTrip;
    Button btnCreateReceipt;
    Button btnSync;
    MySpeedoGauge gauge;
    Runnable tripDurationRunner;
    Handler tripDurationHandler = new Handler();
    Runnable mtdTogglerRunner;
    Handler mtdTogglerHandler = new Handler();
    boolean isShowingMilesTotal = false;

    FullTrip tripToReassign;

    double lastMtdValue;
    private double lastMtdMilesValue = 0;
    MyAnimatedNumberTextView animatedNumberTextView;

    // Filter to listen for rep chooser result broadcasts.
    IntentFilter repChosenFilter = new IntentFilter(FullscreenActivityChooseRep.CHOICE_RESULT);
    BroadcastReceiver repChosenReceiver;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (data != null && data.hasExtra(FullscreenActivityChooseOpportunity.OPPORTUNITY_RESULT)) {
                CrmEntities.Opportunities.Opportunity opportunity =
                        data.getParcelableExtra(FullscreenActivityChooseOpportunity.OPPORTUNITY_RESULT);
                if (opportunity != null) {
                    Log.i(TAG, "onActivityResult Opportunity was manipulated (" + opportunity.name + ")");
                }
                populateTripList();
            } else {
                populateTripList();
                Toast.makeText(getContext(), "Trip was created.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public View onCreateView(@NonNull final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mileageViewModel = ViewModelProviders.of(this).get(MileageViewModel.class);

        options = new MyPreferencesHelper(getContext());
        options.authenticateFragIsVisible(false);

        // Log a metric
        MileBuddyMetrics.updateMetric(getContext(), MileBuddyMetrics.MetricName.LAST_OPENED_APP, DateTime.now());

        final View root = inflater.inflate(R.layout.frag_mileage, container, false);
        rootView = root;

        locFilter.addAction(MyLocationService.STOP_TRIP_ACTION);
        locFilter.addAction(MyLocationService.SERVICE_STARTED);
        locFilter.addAction(MyLocationService.SERVICE_STOPPING);
        locFilter.addAction(MyLocationService.SERVICE_STOPPED);
        locFilter.addAction(MyLocationService.NOT_MOVING);
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
        txtMtd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopMtdTogglerRunner();
                startMtdTogglerRunner();
                /*if (!animatedNumberTextView.isRunning) {
                    txtMtd.setText("---");
                    lastMtdValue = 1;
                    populateTripList();
                }*/
            }
        });

        txtMilesTotal = root.findViewById(R.id.txtMilesTotal);
        txtMilesTotal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopMtdTogglerRunner();
                startMtdTogglerRunner();
                /*if (!animatedNumberTextView.isRunning) {
                    txtMtd.setText("---");
                    lastMtdValue = 1;
                    populateTripList();
                }*/
            }
        });

        btnSync = root.findViewById(R.id.button_sync);
        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    syncTrips();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btnStartStop = root.findViewById(R.id.btnStartStopTrip);
        btnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (options.getNameTripOnStart() && !MyLocationService.isRunning) {
                    showNameTrip();
                } else {
                    startStopTrip();
                }
            }
        });

        toggleEditButton = root.findViewById(R.id.tgglebtn_editTrips);
        toggleEditButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                setEditMode(isChecked);
            }
        });

        btnDeleteTrips = root.findViewById(R.id.btn_deleteSelectedTrips);
        btnDeleteTrips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyYesNoDialog.show(getContext(), "Are you sure you want to delete these trips?"
                        , new MyYesNoDialog.YesNoListener() {
                    @Override
                    public void onYes() {
                        deleteSelected();
                        populateTripList();
                    }

                    @Override
                    public void onNo() {
                        Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnAddManualTrip = root.findViewById(R.id.button_add_manual);
        btnAddManualTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Check permission and request if not present
                if (checkLocationPermission() == LocationPermissionResult.NONE) {

                    boolean showRational = shouldShowRequestPermissionRationale(
                            Manifest.permission.ACCESS_FINE_LOCATION);
                    if (!showRational) {
                        final Dialog dialog = new Dialog(getActivity());
                        final Context c = getActivity();
                        dialog.setContentView(R.layout.generic_app_dialog);
                        TextView txtMain = dialog.findViewById(R.id.txtMainText);
                        txtMain.setText("You restrictive sunovabitch!  " +
                                "You need to allow MileBuddy access to your device's location.\n\n" +
                                "How else can we do this whole mileage tracking thing?!");
                        Button btnOkay = dialog.findViewById(R.id.btnOkay);
                        btnOkay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Helpers.Application.openAppSettings(getContext());
                                dialog.dismiss();
                            }
                        });
                        dialog.setTitle("Permissions");
                        dialog.setCancelable(true);
                        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                            @Override
                            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                if (keyCode != KeyEvent.KEYCODE_BACK) {
                                    Helpers.Application.openAppSettings(getContext());
                                }
                                dialog.dismiss();
                                return true;
                            }
                        });
                        dialog.show();
                    } else {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                                , PERMISSION_MAKE_TRIP);
                    }
                    return;
                } else {
                    Intent intent = new Intent(getContext(), Activity_ManualTrip.class);
                    startActivityForResult(intent, 0);
                }
            }
        });

        btnCreateReceipt = root.findViewById(R.id.button_get_receipt);
        btnCreateReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkStoragePermission()) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                            , PERMISSION_MAKE_RECEIPT);
                    return;
                } else {
                    showReceiptDialog();
                }
            }
        });

        int defaultColor = txtStatus.getCurrentTextColor();
        Log.i(TAG, "onCreateView " + defaultColor);

        // Initialize the broadcast receiver
        locReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {
            if (intent != null) {

                if (intent.getAction().equals(MyAttachmentUploadService.ATTACHMENT_SERVICE_STARTED)) {
                    return;
                }
                
                if (options.isExplicitMode()) {
                    btnStartStop.setText((MyLocationService.isRunning) ? "FUCKING STOP" : "FUCKING GO");
                } else {
                    btnStartStop.setText((MyLocationService.isRunning) ? "STOP" : "GO");
                }
                btnSync.setTextColor((MyLocationService.isRunning) ? Color.GRAY : Color.BLUE);
                btnSync.setEnabled((MyLocationService.isRunning) ? false : true);
                tripStatusContainer.setVisibility((MyLocationService.isRunning) ? View.VISIBLE : View.GONE);
                txtDuration.setText(MyLocationService.getTripDuration() + " mins");

                // NEW LOCATION INFORMATION RECEIVED
                if (intent.getParcelableExtra(MyLocationService.LOCATION_CHANGED) != null) {
                    LocationContainer update = intent.getParcelableExtra(MyLocationService.LOCATION_CHANGED);
                    Log.d(TAG, "onReceive Location broadcast received!");

                    if (MyLocationService.isRunning) {
                        txtStatus.setText("Trip is running");
                        txtStatus.setTextColor(Color.GREEN);
                        txtReimbursement.setText(update.fullTrip.calculatePrettyReimbursement());
                        Helpers.Animations.pulseAnimation(txtStatus, 1.00f, 1.25f, 9000, 150);
                        if (isStarting) {
                            showGif(false);
                            isStarting = false;
                        }
                    } else {
                        txtStatus.setText("Trip not started");
                        txtStatus.setTextColor(DEFAULT_FONT_COLOR);
                    }

                    txtDistance.setText(Helpers.Geo.convertMetersToMiles(update.fullTrip.getDistance(), 2) + " miles");
                    txtSpeed.setText(update.tripEntry.getSpeedInMph(true));
                    gauge.setSpeed(update.tripEntry.getMph());

                    setEditMode(false);
                    manageDefaultImage();

                }

                // SERVICE HAS STARTED
                if (intent.getAction() != null && intent.getAction().equals(MyLocationService.SERVICE_STARTED)) {
                    btnSync.setEnabled(false);
                    txtStatus.setText("Start driving!");
                    txtDistance.setText("0 miles");
                    txtSpeed.setText("0 mph");
                    isStarting = true;
                    tripStatusContainer.setVisibility(View.VISIBLE);
                    txtStatus.setTextColor(Color.BLUE);
                    txtReimbursement.setText("$0.00");
                    Helpers.Animations.pulseAnimation(txtStatus, 1.00f, 1.25f, 9000, 450);
                    Log.i(TAG, "onReceive | trip starting broadcast received.");
                    manageDefaultImage();
                    gauge.setSpeed(0);
                    gauge.setMaxSpeed(120);
                    startTripDurationRunner();
                }

                // SERVICE IS STOPPING
                if (intent.getAction() != null && intent.getAction().equals(MyLocationService.SERVICE_STOPPED)) {
                    txtStatus.setText("Trip is stopping");
                    Log.i(TAG, "onReceive  | trip stopping broadcast received");
                }

                // SERVICE IS ENDED
                if (intent.getAction() != null && intent.getAction().equals(MyLocationService.SERVICE_STOPPED)) {
                    txtStatus.setText("Trip is stopped.");
                    txtStatus.setTextColor(Color.RED);
                    tripStatusContainer.setVisibility(View.GONE);
                    Log.i(TAG, "onReceive | trip stopped broadcast received.");
                    adapter.notifyDataSetChanged();
                    populateTripList();
                    manageDefaultImage();
                    Log.i(TAG, "onReceive | trip stopping broadcast received.");

                    if (intent.getParcelableExtra(MyLocationService.FINAL_LOCATION) != null) {
                        FullTrip trip = intent.getParcelableExtra(MyLocationService.FINAL_LOCATION);
                        Log.i(TAG, "onReceive " + trip.toString());

                        if (options.getAutosubmitOnTripEnd()) {
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
                    txtSpeed.setText("0 mph");
                    Log.i(TAG, "onReceive: Got a stopped moving broadcast!");
                }

            }
            }
        };

        // set up the RecyclerView
        recyclerView = root.findViewById(R.id.rvTrips);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btnStartStop.setText((MyLocationService.isRunning) ? "Stop trip" : "Start new trip");
        tripStatusContainer.setVisibility((MyLocationService.isRunning) ? View.VISIBLE : View.GONE);

        RefreshLayout refreshLayout = (RefreshLayout) root.findViewById(R.id.refreshLayout);
        refreshLayout.setRefreshHeader(new MaterialHeader(getContext()));
        refreshLayout.setEnableLoadMore(true);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                refreshlayout.finishRefresh(500/*,false*/);
                populateTripList();
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                refreshlayout.finishLoadMore(500/*,false*/);
            }
        });

        // Update the current user silently just in case something has changed
        if (MediUser.getMe() != null && MediUser.getMe().email != null) {
            getUser(MediUser.getMe().email);
        }

        try {
            getAllAccountAddresses();
        } catch (Exception e) {
            e.printStackTrace();
        }

        allPurposeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    if (intent.getBooleanExtra(MAKE_RECEIPT, false))  {
                        if (intent.getBooleanExtra(MAKE_RECEIPT, false) == true) {
                            showReceiptDialog();
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
                        MediUser chosenUser = data.getParcelableExtra(FullscreenActivityChooseRep.CHOICE_RESULT);
                        if (chosenUser != null) {
                            final MyProgressDialog progressDialog = new MyProgressDialog(getContext(), "Reassigning trip to " + chosenUser.fullname);
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
                                    Toast.makeText(getContext(), "Trip was reassigned", Toast.LENGTH_SHORT).show();
                                    populateTripList();
                                    progressDialog.dismiss();
                                    tripToReassign = null;
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                    Toast.makeText(getContext(), "Failed to reassign trip\n" + error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
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

        getActivity().registerReceiver(repChosenReceiver, repChosenFilter);

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
                        Toast.makeText(getContext(), "Cleaned up unreferenced trips.", Toast.LENGTH_SHORT).show();
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(getContext(), "Something went wrong cleaning unreferenced trips.", Toast.LENGTH_SHORT).show();
                }
            });

            getActivity().registerReceiver(locReceiver, locFilter);
            Log.i(TAG, "onStart Registered the location receiver");
            Log.i(TAG, "onStart Registered the back press receiver.");

            if (MyLocationService.isRunning) {
                txtStatus.setText("Trip is running");
                MyLocationService.sendUpdateBroadcast(getContext());
                startTripDurationRunner();
            } else {
                txtStatus.setText("Trip not started");
            }

            this.getView().setFocusableInTouchMode(true);
            this.getView().requestFocus();
            this.getView().setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        if (adapter.isInEditMode) {
                            // adapter.setEditModeEnabled(false);
                            setEditMode(false);
                            return true;
                        }
                    }
                    return false;
                }
            });

            datasource.deleteEmptyTrips(true, new MyInterfaces.TripDeleteCallback() {
                @Override
                public void onSuccess(int entriesDeleted) {
                    if (entriesDeleted > 0) {
                        Log.w(TAG, "onSuccess: Deleted " + entriesDeleted + " empty trips.");
                        Toast.makeText(getContext(), "Removed " + entriesDeleted + " empty trips.", Toast.LENGTH_SHORT).show();
                        populateTripList();
                    }
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(getContext(), "Failed to delete empty trips\n" + message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            if (options.getDebugMode()) {
                Toast.makeText(getContext(), "User is not yet saved and doesn't exist!", Toast.LENGTH_SHORT).show();
            }
        }

        // int vals[] = { 13, 2, 64, 41, 1, 19, 24 }; // 43
        // int vals[] = { 2, 2, 64, 41, 1, 19, 24 }; // 43
        // int vals[] = { 2, 2, 64, 41, 25, 19, 24 }; // 60
        int vals[] = { 13, 2, 64, 41, 41, 40, 19, 24 }; // 54

        try {
            arrayGame(vals, true);
            arrayGame(vals, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Sums the second highest and the second lowest elements of an array.
     * @param vals The array to parse (must have at least 4 elements).
     * @param ignoreDuplicateValues A lazy, fast method that can get screwed up if duplicate values
     *                              are present at the beginning or end of the array.
     * @return The sum of the second largest and second smallest elements.
     * @throws Exception Exceptions can be thrown for multiple reasons - see exception text.
     */
    int arrayGame(int[] vals, boolean ignoreDuplicateValues) throws Exception {

        // Need at least four elements in the array
        if (vals.length < 4) {
            throw new Exception("You must supply an array with at least four elements.");
        }

        // Sort the array from lowest to highest
        Arrays.sort(vals);

        // If we do not care about leading or trailing duplicates (e.g. { 2, 2, 53, 21, 88, 21 } )
        if (ignoreDuplicateValues) {

            // Grab the second from first and second from last values in the sorted array
            int secondLowest = vals[1]; // probably
            int secondHighest = vals[vals.length - 2]; // probably

            // Sum and return.
            Log.i(TAG, "arrayGame | lazy method | " + ((secondLowest) + (secondHighest)));
            return  (secondLowest) + (secondHighest);
        }

        int lowest, highest, secondLowest, secondHighest;

        // Can assume first and last values are lowest and highest respectively in this sorted array.
        lowest = vals[0];
        highest = vals[vals.length - 1];

        // safest initial value (like negative whatever the maximum integer value can be)
        secondHighest = Integer.MIN_VALUE;

        // Do our second iteration
        for (int i = 0; i < vals.length; i++) {
            // Ignoring the highest value, find the next highest value
            if (vals[i] != highest) {
                secondHighest = Math.max(secondHighest, vals[i]);
            }
        }

        // If var is still our initial value, something has almost certainly gone horribly wrong.
        if (secondHighest == Integer.MIN_VALUE) {
            throw new Exception("Could not determine a second largest value!");
        }

        // Safeset initial value
        secondLowest = Integer.MAX_VALUE;

        // Do our third and final iteration
        for (int i = 0; i < vals.length; i++) {
            if(vals[i] != lowest) {
                secondLowest = Math.min(secondLowest, vals[i]);
            }
        }

        // If var is still our initial value, something has almost certainly gone horribly wrong.
        if (secondLowest == Integer.MAX_VALUE) {
            throw new Exception("Could not determine a second smallest value!");
        }

        // Sum and return
        Log.i(TAG, "arrayGame | verbose method | " + ((secondLowest) + (secondHighest)));
        return secondLowest + secondHighest;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            getActivity().unregisterReceiver(repChosenReceiver);
            Log.i(TAG, "onDestroy Unregistered the rep chosen receiver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            getActivity().unregisterReceiver(locReceiver);
            tripDurationHandler.removeCallbacks(tripDurationRunner);
            Log.i(TAG, "onStop Unregistered the location receiver");
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
                getActivity().onBackPressed();
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
        getContext().registerReceiver(allPurposeReceiver, allPurposeFilter);

    }

    @Override
    public void onPause() {
        super.onPause();
        stopMtdTogglerRunner();
        getContext().unregisterReceiver(allPurposeReceiver);
    }

    @Override
    public void onItemClick(View view, int position) {
        FullTrip clickedTrip = allTrips.get(position);
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
                            txtDuration.setText(MyLocationService.getTripDuration() + " mins");
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

    void startMtdTogglerRunner() {
        mtdTogglerRunner = new Runnable() {
            @Override
            public void run() {
                try {
                    if (isShowingMilesTotal) {
                        Helpers.Animations.fadeOut(txtMilesTotal, 250, new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                isShowingMilesTotal = false;
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
                                isShowingMilesTotal = true;
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
            }
        };
        mtdTogglerRunner.run();
    }

    void stopMtdTogglerRunner() {
        mtdTogglerHandler.removeCallbacks(mtdTogglerRunner);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_START_TRIP:
                if (checkLocationPermission() != LocationPermissionResult.FULL) {
                    Toast.makeText(getContext(), "Location permission must be: " +
                            "\"Allow all the time\"", Toast.LENGTH_SHORT).show();
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

        }
    }

    void startStopTrip(@Nullable String tripname) {
        if (MediUser.getMe(getContext()) == null) {
            NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_HomeFragment_to_HomeSecondFragment);
            Toast.makeText(getContext(), "Need to login", Toast.LENGTH_SHORT).show();
            return;
        }

        if (MyLocationService.isRunning) {
            if (MyLocationService.tripIsValid()) {
                if (options.getConfirmTripEnd()) {
                    MyYesNoDialog.show(getContext(), "Are you sure you want to stop this trip?",
                            new MyYesNoDialog.YesNoListener() {
                                @Override
                                public void onYes() {
                                    MyLocationService.userStoppedTrip = true;
                                    getContext().stopService(new Intent(getContext(), MyLocationService.class));
                                }
                                @Override public void onNo() {} });
                } else {
                    MyLocationService.userStoppedTrip = true;
                    getContext().stopService(new Intent(getContext(), MyLocationService.class));
                }
            } else {
                getContext().stopService(new Intent(getContext(), MyLocationService.class));
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
                    btnStartStop.setText("STOP");
                    showGif(true);

                    // Start the actual service
                    Intent intent = new Intent(getContext(), MyLocationService.class);
                    intent.putExtra(MyLocationService.TRIP_PRENAME, tripname);
                    intent.putExtra(MyLocationService.USER_STARTED_TRIP_FLAG, true);
                    getContext().startForegroundService(intent);
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
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION};
        } else {
            permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        }

        if (checkLocationPermission() != LocationPermissionResult.FULL) {

            boolean showRational = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);
            if (showRational && checkLocationPermission() == LocationPermissionResult.NONE) {
                final Dialog dialog = new Dialog(getActivity());
                final Context c = getActivity();
                dialog.setContentView(R.layout.generic_app_dialog);
                TextView txtMain = dialog.findViewById(R.id.txtMainText);
                txtMain.setText("You restrictive sunovabitch!  " +
                        "You need to allow MileBuddy access to your device's location.\n\n" +
                        "How else can we do this whole mileage tracking thing?!");
                Button btnOkay = dialog.findViewById(R.id.btnOkay);
                btnOkay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Helpers.Application.openAppSettings(getContext());
                        dialog.dismiss();
                    }
                });
                dialog.setTitle("Permissions");
                dialog.setCancelable(true);
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            dialog.dismiss();
                            return true;
                        } else {
                            Helpers.Application.openAppSettings(getContext());
                            dialog.dismiss();
                            return true;
                        }
                    }
                });
                dialog.show();
                requestPermissions(permissions, PERMISSION_START_TRIP);
            } else {
                requestPermissions(permissions, PERMISSION_START_TRIP);
            }
            return false;
        } else {
            return true;
        }
    }

    public void showGif(boolean showStartupAnimation) {

        int drawable = -1;

        if (showStartupAnimation) {
            drawable = R.drawable.car2_static;
        } else {
            drawable = R.drawable.car2;
        }

        Uri path = Uri.parse("android.resource://com.fimbleenterprises.medimileage/" + drawable);
        ImageView imageView = rootView.findViewById(R.id.gifview);
        Glide.with(getContext()).load(path).into(imageView);

    }

    void editTrip(final FullTrip clickedTrip) {
        final Dialog dialog = new Dialog(getContext());
        final Context c = getContext();
        dialog.setContentView(R.layout.edit_trip);
        final Button btnChangeDate = dialog.findViewById(R.id.btnChangeDate);
        btnChangeDate.setText(clickedTrip.getPrettyDate());
        Button btnSaveTrip = dialog.findViewById(R.id.button_NameAndCreateTrip);
        final AutoCompleteTextView txtName = dialog.findViewById(R.id.autocomplete_EditText_NameTrip);
        final EditText txtDistance = dialog.findViewById(R.id.editTxt_Distance);
        final float originalMiles = clickedTrip.getDistanceInMiles();
        final long originalDate = clickedTrip.getDateTime().getMillis();
        final long[] newDate = {originalDate};
        txtDistance.setText(Float.toString(originalMiles));

        String[] tripnames = datasource.getTripNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (getContext(), android.R.layout.select_dialog_item, tripnames);
        txtName.setThreshold(1);
        txtName.setAdapter(adapter);
        txtName.setText(clickedTrip.getTitle());
        dialog.setTitle("Edit Trip");
        dialog.setCancelable(true);

        btnSaveTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                float newMiles = Float.parseFloat(txtDistance.getText().toString());


                if (originalMiles != newMiles || originalDate != newDate[0]) {
                    clickedTrip.setIsEdited(true);
                    clickedTrip.setDistance(Helpers.Geo.convertMilesToMeters(newMiles, 1));
                    clickedTrip.setMilis(newDate[0]);
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

        btnChangeDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new DatePickerFragment(new MyInterfaces.DateSelector() {
                    @Override
                    public void onDateSelected(DateTime selectedDate, String selectedDateStr) {
                        btnChangeDate.setText(selectedDateStr);
                        clickedTrip.setDateTime(selectedDate);
                    }
                });
                ((DatePickerFragment) newFragment).setShowDate(clickedTrip.getDateTime());
                newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
            }
        });
        dialog.show();
    }

    public void setEditMode(boolean isChecked) {
        adapter.setEditModeEnabled(isChecked);
        btnDeleteTrips.setEnabled(isChecked);
        toggleEditButton.setChecked(isChecked);
        btnDeleteTrips.setVisibility((isChecked) ? View.VISIBLE : View.INVISIBLE);
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

    @SuppressLint("StaticFieldLeak")
    void deleteSelected() {
        new AsyncTask<String, String, String>() {
            int deletedTrips = 0;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgress(true, "Deleting trips...");
            }

            @Override
            protected String doInBackground(String... strings) {
                MySqlDatasource ds = new MySqlDatasource();
                for (FullTrip trip : adapter.mData) {
                    if (trip.isChecked) {
                        if (ds.deleteFulltrip(trip.getTripcode(), true)) {
                            deletedTrips++;
                        }
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                showProgress(false, null);
                populateTripList();
                adapter.notifyDataSetChanged();
                setEditMode(false);
                Toast.makeText(getContext(), "Deleted " + deletedTrips + " trips.", Toast.LENGTH_SHORT).show();
                super.onPostExecute(s);
            }
        }.execute(null, null, null);
    }

    int countSelectedTrips() {
        int count = 0;
        for (FullTrip trip : adapter.mData) {
            if (trip.isChecked) {
                count++;
            }
        }
        return count;
    }

    void setMileageRate() throws UnsupportedEncodingException {

        final MyProgressDialog dialog = new MyProgressDialog(getContext(),
                "Updating reimbursement rate...");
        dialog.show();

        MyLocationService.setReimbursementRate(new MyInterfaces.ReimbursementRateCallback() {
            @Override
            public void onSuccess(float rate) {
                dialog.dismiss();
                Toast.makeText(getContext(), "Success!\nRate: " + options.getPrettyReimbursementRate()
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
        if (wasKilled && ! isRunning) {
            Log.i(TAG, "onReceive: Last trip was auto-killed and the service is not running!");
            tripStatusContainer.setVisibility(View.GONE);
            if (options.isExplicitMode()) {
                btnStartStop.setText("FUCKING GO");
            } else {
                btnStartStop.setText("GO");
            }
            options.lastTripAutoKilled(false);
        }


        txtMtd.setText("---");
        txtMilesTotal.setText("---");

        allTrips = new ArrayList<>();
        ArrayList<FullTrip> tempList = new ArrayList<>();
        Log.i(TAG, "populateTripList Getting trips from database...");
        tempList = datasource.getTrips();
        Log.i(TAG, "populateTripList Trips retrieved from database");

        Log.i(TAG, "populateTripList Adding trips to the master list...");
        for (FullTrip trip : tempList) {
            if (!trip.getIsRunning()) {
                allTrips.add(trip);
            }
        }

        Log.i(TAG, "populateTripList Added: " + allTrips.size() + " trips to the master list.");

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
        for (int i = 0; i < (allTrips.size()); i++) {
            int tripDayOfYear = Helpers.DatesAndTimes.returnDayOfYear(allTrips.get(i).getDateTime());
            int tripWeekOfYear = Helpers.DatesAndTimes.returnWeekOfYear(allTrips.get(i).getDateTime());
            int tripMonthOfYear = Helpers.DatesAndTimes.returnMonthOfYear(allTrips.get(i).getDateTime());

            // Trip was today
            if (tripDayOfYear == todayDayOfYear) {
                if (addedTodayHeader == false) {
                    FullTrip headerObj = new FullTrip();
                    headerObj.isSeparator = true;
                    headerObj.setTitle(options.isExplicitMode() ? getString(R.string.triplist_today_explicit) : getString(R.string.triplist_today));
                    triplist.add(headerObj);
                    addedTodayHeader = true;
                    Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'Today' - This will not be added again!");
                }
                // Trip was yesterday
            } else if (tripDayOfYear == (todayDayOfYear - 1)) {
                if (addedYesterdayHeader == false) {
                    FullTrip headerObj = new FullTrip();
                    headerObj.isSeparator = true;
                    headerObj.setTitle(options.isExplicitMode() ? getString(R.string.triplist_yesterday_explicit) : getString(R.string.triplist_yesterday));
                    triplist.add(headerObj);
                    addedYesterdayHeader = true;
                    Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'Yesterday' - This will not be added again!");
                }

                // Trip was this week
            } else if (tripWeekOfYear == todayWeekOfYear) {
                if (addedThisWeekHeader == false) {
                    FullTrip headerObj = new FullTrip();
                    headerObj.isSeparator = true;
                    headerObj.setTitle(options.isExplicitMode() ? getString(R.string.triplist_this_week_explicit) : getString(R.string.triplist_this_week));
                    triplist.add(headerObj);
                    addedThisWeekHeader = true;
                    Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'This week' - This will not be added again!");
                }

                // Trip was this month
            } else if (tripMonthOfYear == todayMonthOfYear) {
                if (addedThisMonthHeader == false) {
                    FullTrip headerObj = new FullTrip();
                    headerObj.isSeparator = true;
                    headerObj.setTitle(options.isExplicitMode() ? getString(R.string.triplist_this_month_explicit) : getString(R.string.triplist_this_month));
                    triplist.add(headerObj);
                    addedThisMonthHeader = true;
                    Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'This month' - This will not be added again!");
                }

                // Trip was older than this month
            } else if (tripMonthOfYear < todayMonthOfYear) {
                if (addedOlderHeader == false) {
                    FullTrip headerObj = new FullTrip();
                    headerObj.isSeparator = true;
                    headerObj.setTitle(options.isExplicitMode() ? getString(R.string.triplist_last_month_and_older_explicit) : getString(R.string.triplist_last_month_and_older));
                    triplist.add(headerObj);
                    addedOlderHeader = true;
                    Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'Older' - This will not be added again!");
                }
            }
            triplist.add(allTrips.get(i));
        }

        Log.i(TAG, "populateTripList Finished preparing the dividers and trips.");

        // Since the new arraylist now has headers it will throw off the original array's indexes
        allTrips = triplist;
        adapter = new TripListRecyclerAdapter(getContext(), triplist, this);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        // Tally the MTD reimbursement
        float mtdTotal = 0;
        float mtdMilesTotal = 0;
        for (FullTrip trip : allTrips) {
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
                mtdMilesTotal * options.getReimbursementRate()) ;

        animatedNumberTextView = new MyAnimatedNumberTextView(getContext(), txtMtd);
        if (lastMtdValue > 0) {
            animatedNumberTextView.setNewValue(mtdTotal, lastMtdValue, true);
        } else {
            txtMtd.setText(Helpers.Numbers.convertToCurrency(mtdTotal));
        }

        lastMtdValue = Math.round(mtdTotal);
        lastMtdMilesValue = Helpers.Numbers.formatAsOneDecimalPointNumber(mtdMilesTotal);

        txtMilesTotal.setText(Helpers.Numbers.formatAsZeroDecimalPointNumber(lastMtdMilesValue) + " mi");

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
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.trip_options_minimalist);
        dialog.setTitle("Trip Options");
        Button btnEditTrip = dialog.findViewById(R.id.edit_trip);
        Button btnDeleteTrip = dialog.findViewById(R.id.btnDelete);
        // Hidden - having this button here doesn't fit with the current design and flow
        Button btnOpportunity = dialog.findViewById(R.id.btn_opportunities);
        /*TableRow tableRow = dialog.findViewById(R.id.tableRowopp);
        tableRow.setVisibility(clickedTrip.hasAssociations() ? View.VISIBLE : View.GONE);
        btnOpportunity.setEnabled(clickedTrip.getIsSubmitted());*/
        btnOpportunity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (clickedTrip.getIsSubmitted()) {
                    Intent intent = new Intent(getContext(), FullscreenActivityChooseOpportunity.class);
                    intent.putExtra(FullscreenActivityChooseOpportunity.FULLTRIP, clickedTrip);
                    startActivityForResult(intent, FullscreenActivityChooseOpportunity.REQUESTCODE);
                } else {
                    Toast.makeText(getContext(), "You have to submit this trip first.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Button btnSubmit = dialog.findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    dialog.dismiss();

                    if (clickedTrip.getDistanceInMiles() <= 2) {
                        datasource.deleteEmptyTrips(true, new MyInterfaces.TripDeleteCallback() {
                            @Override
                            public void onSuccess(int entriesDeleted) {
                                if (entriesDeleted > 0) {
                                    Log.w(TAG, "onSuccess: Deleted " + entriesDeleted + " empty trips.");
                                    Toast.makeText(getContext(), "Removed " + entriesDeleted + " empty trips.", Toast.LENGTH_SHORT).show();
                                    populateTripList();
                                }
                            }

                            @Override
                            public void onFailure(String message) {
                                Toast.makeText(getContext(), "Failed to delete empty trips\n" + message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    isTripSubmitted(clickedTrip);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "onClick Prepared trip for CRM");
            }
        });

        Button btnRecall = dialog.findViewById(R.id.unsubmit_trip);
        btnRecall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    dialog.dismiss();
                    unsubmitTrip(clickedTrip);
                } catch (UnsupportedEncodingException e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });

        Button btn_ViewTrip = dialog.findViewById(R.id.view_trip);
        btn_ViewTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ViewTripActivity.class);
                intent.putExtra(ViewTripActivity.CLICKED_TRIP, clickedTrip);
                startActivity(intent);
                dialog.dismiss();
            }
        });

        btnEditTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTrip(clickedTrip);
                dialog.dismiss();
            }
        });

        btnDeleteTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyYesNoDialog.show(getContext(), "Are you sure you want to delete this trip?",
                        new MyYesNoDialog.YesNoListener() {
                            @Override
                            public void onYes() {
                                if (datasource.deleteFulltrip(clickedTrip.getTripcode(), true)) {
                                    Toast.makeText(getContext(), "Deleted trip.", Toast.LENGTH_SHORT).show();
                                    populateTripList();
                                    dialog.dismiss();
                                }
                            }

                            @Override
                            public void onNo() {
                                Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
            }
        });

        btnEditTrip.setEnabled(!clickedTrip.getIsSubmitted());

        Button btnReassign = dialog.findViewById(R.id.assign_trip);
        btnReassign.setEnabled(MediUser.getMe().isAdmin());
        btnReassign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!clickedTrip.getIsSubmitted()) {
                    Toast.makeText(getContext(), "Trip must be submitted before it can be reassigned!", Toast.LENGTH_LONG).show();
                    return;
                }

                tripToReassign = clickedTrip;
                dialog.dismiss();
                FullscreenActivityChooseRep.showRepChooser((MainActivity)getActivity(), MediUser.getMe());
            }
        });

        if (options.isExplicitMode()) {
            btn_ViewTrip.setText(getString(R.string.tripOptions_view_explicit));
            btnSubmit.setText(getString(R.string.tripOptions_submit_explicit));
            btnRecall.setText(getString(R.string.tripOptions_recall_explicit));
            btnEditTrip.setText(getString(R.string.tripOptions_edit_explicit));
            btnDeleteTrip.setText(getString(R.string.tripOptions_delete_explicit));
        } else {
            btn_ViewTrip.setText(getString(R.string.tripOptions_view));
            btnSubmit.setText(getString(R.string.tripOptions_submit));
            btnRecall.setText(getString(R.string.tripOptions_recall));
            btnEditTrip.setText(getString(R.string.tripOptions_edit));
            btnDeleteTrip.setText(getString(R.string.tripOptions_delete));
        }

        dialogTripOptions = dialog;
        dialog.show();
    }

    void manageDefaultImage() {
        setEditMode(false);
        if (! MyLocationService.isRunning) {
            emptyTripList.setVisibility((allTrips == null || allTrips.size()
                    <= SHOW_CLICK_HERE_TRIP_COUNT_THRESHOLD) ? View.VISIBLE : View.GONE);
        } else {
            emptyTripList.setVisibility(View.GONE);
        }

        Helpers.Animations.pulseAnimation(emptyTripList, 1.05f, 1.02f, 9000, 350);

    }

    void parseTripsForAssociations() {

        String query = CrmQueries.TripAssociation.getAssociationsLastXMonths(3);

        if (allTrips != null && allTrips.size() > 0) {
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
                    for (FullTrip trip : allTrips) {
                        if (associations.getAssociation(trip) != null) {
                            Log.i(TAG, "onSuccess Found an association!");
                            TripAssociation association = associations.getAssociation(trip);
                            trip.hasAssociations(true);
                            trip.save();
                        }
                    }
                    Log.i(TAG, "onSuccess Finished parsing " + associations.list.size() + " associations for " +
                            allTrips.size() + " trips.");
                    populateTripList();
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
        Requests.Argument argument = new Requests.Argument("query", CrmQueries.Addresses.getAllAccountAddresses());
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

    public void getAllOpportunityAddresses() {
        Requests.Argument argument = new Requests.Argument("query", CrmQueries.Addresses.getAllAccountAddresses());
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
        boolean result = true;
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        String bgPermission = "android.permission.ACCESS_BACKGROUND_LOCATION";
        int res1 = getActivity().checkCallingOrSelfPermission(permission);
        int res2 = getActivity().checkCallingOrSelfPermission(bgPermission);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (res1 == PackageManager.PERMISSION_GRANTED && res2 == PackageManager.PERMISSION_GRANTED) {
                return MyApp.LocationPermissionResult.FULL;
            } else if (res1 == PackageManager.PERMISSION_GRANTED && res2 != PackageManager.PERMISSION_GRANTED ||
                    res2 == PackageManager.PERMISSION_GRANTED && res1 != PackageManager.PERMISSION_GRANTED) {
                return LocationPermissionResult.PARTIAL;
            } else {
                return LocationPermissionResult.NONE;
            }
        } else {
            if (res1 == PackageManager.PERMISSION_GRANTED) {
                return LocationPermissionResult.FULL;
            } else {
                return LocationPermissionResult.NONE;
            }
        }
    }

    public boolean checkStoragePermission() {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = getActivity().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public void syncTrips() throws UnsupportedEncodingException {

        final MyProgressDialog dialog = new MyProgressDialog(getContext(), "Getting submitted trips from server...",
                MyProgressDialog.PROGRESS_TYPE);
        dialog.show();
        Log.i(TAG, "syncTrips Beginning sync...");

        Crm crm = new Crm();
        Request request = new Request();
        request.function = Request.Function.GET.name();
        request.arguments.add(new Requests.Argument("query", CrmQueries.Trips.getAllTripsByOwnerForLastXmonths(2, MediUser.getMe().systemuserid)));

        // Log a metric
        MileBuddyMetrics.updateMetric(getContext(), MileBuddyMetrics.MetricName.LAST_ACCESSED_MILEAGE_SYNC, DateTime.now());

        crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                final String response = new String(responseBody);

                Log.i(TAG, "onSuccess Sync returned: " + response);

                new AsyncTask<String, String, String>() {
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                        }

                        @Override
                        protected String doInBackground(String... strings) {
                            ArrayList<FullTrip> serverTrips = FullTrip.createTripsFromCrmJson(response);
                            int totalTrips = serverTrips.size();
                            int i = 1;
                            for (FullTrip serverTrip : serverTrips) {
                                Log.i(TAG, "onSuccess Updating local trip with server trip...");
                                publishProgress(Integer.toString(i), Integer.toString(totalTrips), null);
                                updateLocalTrip(serverTrip);
                                i++;
                            }
                            return null;
                        }

                    @Override
                    protected void onProgressUpdate(String... values) {
                        int curVal = Integer.parseInt(values[0]);
                        int totalVal = Integer.parseInt(values[1]);
                        dialog.setContentText("Updating local trip " + curVal + " of " + totalVal);
                        if (curVal == totalVal - 1) {
                            dialog.setContentText("Redrawing the list.  Stand by...");
                        }
                        super.onProgressUpdate(values);
                    }

                    @Override
                        protected void onPostExecute(String s) {
                            super.onPostExecute(s);
                            Log.i(TAG, "onSuccess Local trips were synced");
                            populateTripList();
                            dialog.dismiss();
                        }
                    }.execute(null, null, null);
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
     * @return A boolean indicating success.
     */
    boolean updateLocalTrip(FullTrip serverTrip) {
        for (FullTrip trip : allTrips) {
            if (trip.getTripcode() == serverTrip.getTripcode()) {
                trip = serverTrip;
                trip.save();
                Log.i(TAG, "updateLocalTrip Local trip was updated.");
                return true;
            }
        }
        serverTrip.save();
        Log.i(TAG, "updateLocalTrip Local trip was created.");
        return true;
    }

    public void confirmSubmittedTrips() throws UnsupportedEncodingException {

        Crm crm = new Crm();

        final MyProgressDialog dialog = new MyProgressDialog(getContext(), "Syncing trips...",
                MyProgressDialog.PROGRESS_TYPE);
        dialog.show();

        for (final FullTrip trip : allTrips) {
            Toast.makeText(getContext(), "Syncing trip list...", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "syncTrips tripcode: " + trip.getTripcode() + " has guid: " + trip.getTripGuid());

            if (trip.getDateTime().getYear() == DateTime.now().getYear()) {
                if (trip.getDateTime().getMonthOfYear() == DateTime.now().getMonthOfYear()) {
                    if (trip.getIsSubmitted()) {
                        Log.i(TAG, "syncTrips Found a trip from this month");

                        QueryFactory queryFactory = new QueryFactory("msus_fulltrip");
                        queryFactory.addColumn("msus_fulltripid");
                        queryFactory.addColumn("msus_tripcode");
                        Filter filter = new Filter(Filter.FilterType.AND);
                        filter.addCondition(new Filter.FilterCondition("msus_tripcode", Filter.Operator.EQUALS,
                                Long.toString(trip.getTripcode())));
                        queryFactory.setFilter(filter);
                        String query = queryFactory.construct();

                        Request request = new Request();
                        request.function = Request.Function.GET.name();
                        request.arguments.add(new Requests.Argument("getshit", query));

                        crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                String result = new String(responseBody);
                                Log.i(TAG, "onSuccess " + result);
                                try {
                                    String guid = new JSONObject(result).getJSONArray("value").getJSONObject(0).getString("msus_fulltripid");
                                    trip.setTripGuid(guid);
                                    trip.setIsSubmitted(true);
                                    trip.save();
                                    dialog.dismiss();
                                } catch (JSONException e) {
                                    dialog.dismiss();
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                Log.w(TAG, "onFailure: " + error.getMessage());
                                Toast.makeText(getContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
                    }
                }
            }
        }
        populateTripList();
    }

    public void isTripSubmitted(final FullTrip clickedTrip) {

        Crm crm = new Crm();

        String query = CrmQueries.Trips.getTripidByTripcode(clickedTrip.getTripcode());

        Request request = new Request();
        request.function = Request.Function.GET.name();
        request.arguments.add(new Requests.Argument("query", query));

        final MyProgressDialog dialog = new MyProgressDialog(getContext(), "Checking if trip is already submitted...", MyProgressDialog.PROGRESS_TYPE);
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
                        Toast.makeText(getContext(), "Trip was updated", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();

                        TripAssociationManager.manageTripAssociations(clickedTrip, new MyInterfaces.CreateManyListener() {
                            @Override
                            public void onResult(CrmEntities.CreateManyResponses responses) {
                                if (options.getDebugMode()) {
                                    Toast.makeText(getContext(), "Created " + responses.responses.size() + " associations", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onError(String msg) {
                                if (options.getDebugMode()) {
                                    Toast.makeText(getContext(), "Failed to do trip associations\n" + msg, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } else {
                        try {
                            dialog.dismiss();
                            submitTrip(clickedTrip);
                        } catch (Exception e) {
                            dialog.dismiss();
                            Toast.makeText(getContext(), "Failed to submit trip", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), "Failed to verify this trip's existance", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    public void submitTrip(final FullTrip clickedTrip) throws UnsupportedEncodingException {

        Crm crm = new Crm();
        Request request = clickedTrip.packageForCrm();

        final MyProgressDialog dialog = new MyProgressDialog(getContext(), "Submitting trip...", MyProgressDialog.PROGRESS_TYPE);
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
                                    Toast.makeText(getContext(), "Created " + responses.responses.size() + " associations.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onError(String msg) {
                                if (options.getDebugMode()) {
                                    Toast.makeText(getContext(), "Failed to associate trips\n" + msg, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), "Trip was submitted", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.w(TAG, "onFailure: " + new String(responseBody));
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
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
                "Recalling trip...", MyProgressDialog.PROGRESS_TYPE);
        dialog.show();

        crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                try {
                    JSONObject object = new JSONObject(result);
                    if (object.getBoolean("WasSuccessful") == true ||
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
                Toast.makeText(getContext(), "Trip was recalled", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                // Trip associations are removed at the server by a workflow when the associated trip or opportunity are modified (removed)
                /*TripAssociationManager.removeAssociations(tripGuid, new MyInterfaces.DeleteManyListener() {
                    @Override
                    public void onResult(CrmEntities.DeleteManyResponses responses) {
                        if (options.getDebugMode()) {
                            Toast.makeText(getContext(), "Removed " + responses.responses.size() + " associations", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String msg) {
                        if (options.getDebugMode()) {
                            Toast.makeText(getContext(), "Failed to disassociate accounts\n" + msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });*/
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.w(TAG, "onFailure: " + new String(error.getMessage()));
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

        final Dialog dialog = new Dialog(getContext());

        dialog.setContentView(R.layout.rename_trip);
        final AutoCompleteTextView actv = dialog.findViewById(R.id.autocomplete_EditText_NameTrip);
        Button startButton = dialog.findViewById(R.id.button_NameAndCreateTrip);
        dialog.setCancelable(true);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (actv.getText().length() > 0) {
                    startStopTrip(actv.getText().toString());
                } else {
                    startStopTrip();
                }
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

        //Creating the instance of ArrayAdapter containing list of fruit names
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (getContext(), android.R.layout.select_dialog_item, names);
        //Getting the instance of AutoCompleteTextView
        actv.setThreshold(0);//will start working from first character
        actv.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
        actv.setTextColor(Color.RED);

        dialog.show();
    }

    void showReceiptDialog() {
        final Dialog dialog = new Dialog(getContext());
        final Context c = getContext();
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
    }

    void doActualReceipt(int month, int year) {

        // Log a metric
        MileBuddyMetrics.updateMetric(getContext(), MileBuddyMetrics.MetricName
                .LAST_ACCESSED_GENERATE_RECEIPT, DateTime.now());

        File receipt = makeReceiptFile(month, year);
        if (receipt != null) {
            MediUser me = MediUser.getMe();
            String[] recips = new String[2];
            recips[0] = me.email;
            recips[1] = REIMBURSEMENT_EMAIL;
            Helpers.Email.sendEmail(recips, String.valueOf(R.string.receipt_mail_body),
                    String.valueOf(R.string.receipt_mail_subject_preamble) + " (" + month + "/" + year + ")"
                    , getContext(), receipt, false);
        } else {
            Toast.makeText(getContext(), "No trips found for that month/year."
                    , Toast.LENGTH_SHORT).show();
        }
    }

    public File makeReceiptFile(int monthNum, int year) {
        MySqlDatasource ds = new MySqlDatasource();
        List<FullTrip> allTrips = ds.getTrips(monthNum, year, true);
        String fName = "mileage_receipt_month_" + DateTime.now().getMonthOfYear() + "_year_" +
                DateTime.now().getYear() + ".txt";
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
                stringBuilder.append(("Mileage Report (" + MediUser.getMe().fullname + ")\n"));
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
                Toast.makeText(getContext(), "Failed to make an image file.  Text file will have to do!", Toast.LENGTH_SHORT).show();
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

    @SuppressLint("NewApi")
    private void showReceiptMonthPicker() {
        MonthYearPickerDialog mpd = new MonthYearPickerDialog();
        mpd.setListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                confirmReceipt(month, year);
            }
        });
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
            Toast.makeText(getContext(), "No trips found for that month/year.", Toast.LENGTH_SHORT).show();
            return;
        }

        if ((subbed > 0)) {
            doActualReceipt(month, year);
        }
    }

    interface LocalTripsUpdatedListener {
        void onFinished();
    }

}






















































