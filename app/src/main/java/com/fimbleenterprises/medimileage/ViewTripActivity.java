package com.fimbleenterprises.medimileage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import com.fimbleenterprises.medimileage.CrmEntities.CrmAddresses;
import com.fimbleenterprises.medimileage.CrmEntities.CrmAddresses.CrmAddress;
import com.fimbleenterprises.medimileage.CrmEntities.Opportunities.Opportunity;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.w3c.dom.Document;

import java.util.ArrayList;

public class ViewTripActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int ANIMATE_DURATION = 1000;
    public static final int CAMERA_PADDING = 300;
    boolean cameraIsMoving = false;
    private static final String TAG = "ViewTripActivity";
    public static final String CLICKED_TRIP = "CLICKED_TRIP";
    public static final String TRIP_ENTRIES = "TRIP_ENTRIES";
    ArrayList<Marker> mapMarkers = new ArrayList<>();
    ToggleButton toggleShowHideGoogle;
    FullTrip clickedTrip;
    ArrayList<TripEntry> tripEntries = new ArrayList<>();
    GoogleMap map;
    MySettingsHelper options;
    Polyline googlePoly;
    LinearLayout linearLayout_Shell;
    LinearLayout linearLayout_Contents;
    TableLayout googleDataExpanded;
    TextView txt_GoogleDistanceValue;
    TextView txtTitle;
    TextView txtDistance;
    TextView txtDuration;
    TextView txtDate;
    TextView txtTopSpeed;
    TextView txtAvgSpeed;
    TextView txtReimbursement;
    ImageButton btnCycleMapType;
    ProgressBar progressBar;
    // TextView txt_GoogleDistanceLabel;
    LinearLayout linearLayoutMaster;
    public PolylineOptions googlePolyBuilder;
    boolean mapIsReady = false;
    LatLng defaultPosition;
    boolean firstLoad = true;
    Context context;
    int curMapType = GoogleMap.MAP_TYPE_NORMAL;
    ArrayList<MyMapMarker> myMapMarkers = new ArrayList<>();
    MyInfoWindowAdapter infoWindowAdapter;
    CrmAddresses crmAddresses;

    interface DrawCompleteListener {
        void onFinished();
        void onFailed(String msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);
        /*Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        context = this;
        options = new MySettingsHelper(this);
        curMapType = options.getMapMode();


        if (getIntent().hasExtra(CLICKED_TRIP)) {

            clickedTrip = getIntent().getParcelableExtra(CLICKED_TRIP);

            Log.i(TAG, "onNewIntent Received a trip");
            if (getIntent().hasExtra(TRIP_ENTRIES)) {
                Log.d(TAG, "onCreate: Changing theme for this activity!");
                setTheme(R.style.AppThemeAlt);

                Log.i(TAG, "onNewIntent Found trip entries passed in an intent.  Gon' use em!");
                tripEntries = getIntent().getParcelableArrayListExtra(TRIP_ENTRIES);
                clickedTrip.tripEntries = tripEntries;
                Log.i(TAG, "onNewIntent " + tripEntries.size() + " entries were used from the intent extra!");
            } else {
                setTheme(R.style.AppTheme);
            }
        } else {
            Toast.makeText(context, "No trip data found", Toast.LENGTH_SHORT).show();
            finish();
        }

        googleDataExpanded = findViewById(R.id.tableLayout_Stats2);
        googleDataExpanded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (googlePoly != null && googlePoly.isVisible()) {
                    showGooglePoly(false);
                }
            }
        });

        txtTitle = findViewById(R.id.textView_TripName);
        txtDistance = findViewById(R.id.TextView_distance);
        txtDuration = findViewById(R.id.TextView_duration);
        txtDate = findViewById(R.id.TextView_date);
        txtTopSpeed = findViewById(R.id.TextView_topSpeed);
        txtAvgSpeed = findViewById(R.id.TextView_avgSpeed);
        txtReimbursement = findViewById(R.id.TextView_reimbursement);
        linearLayoutMaster = findViewById(R.id.LinearLayout_MapMaster);
        linearLayout_Contents = findViewById(R.id.LinearLayout_google_contents);
        linearLayout_Shell = findViewById(R.id.LinearLayout_google_shell);
        progressBar = findViewById(R.id.progressBar);
        btnCycleMapType = findViewById(R.id.imgbtn_toggleMap);
        btnCycleMapType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mapIsReady) {
                    cycleMapType();
                }
            }
        });
        progressBar.setVisibility(View.VISIBLE);

        // this.txt_GoogleDistanceLabel = (TextView) findViewById(R.id.TextView_google_distance_lbl);
        txt_GoogleDistanceValue = (TextView) findViewById(R.id.TextView_google_distance_value);
        toggleShowHideGoogle = (ToggleButton) findViewById(R.id.toggleButton_MapOverlay_ShowHideGoogle);
        toggleShowHideGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showGooglePoly(toggleShowHideGoogle.isChecked());
                Log.i(TAG, "onClick google button clicked.");

            }
        });// Create the navigation up button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (options.hasSavedAddresses()) {
            crmAddresses = options.getAllSavedCrmAddresses();
        }



    }

/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_viewtrip, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_evaluate_addresses :

        }
        return super.onOptionsItemSelected(item);
    }*/

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    public void cycleMapType() {
        if (curMapType == 4) {
            curMapType = 1;
        } else {
            curMapType += 1;
        }
        map.setMapType(curMapType);
        options.setMapMode(curMapType);

        switch (curMapType) {
            case GoogleMap.MAP_TYPE_HYBRID:
                Toast.makeText(context, "Hybrid map", Toast.LENGTH_SHORT).show();
                break;
            case GoogleMap.MAP_TYPE_NORMAL:
                Toast.makeText(context, "Normal map", Toast.LENGTH_SHORT).show();
                break;
            case GoogleMap.MAP_TYPE_SATELLITE:
                Toast.makeText(context, "Satellite map", Toast.LENGTH_SHORT).show();
                break;
            case GoogleMap.MAP_TYPE_TERRAIN:
                Toast.makeText(context, "Terrain map", Toast.LENGTH_SHORT).show();
                break;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!mapIsCentered()) {
                drawRoute(tripEntries);
                if (mapIsCentered()) {
                    finish();
                } else {
                    moveCameraToShowMarkers(false, null, CAMERA_PADDING);
                    return true;
                }
            }

            if (toggleShowHideGoogle.isChecked()) {
                showGooglePoly(false);
                return true;
            }

        }

        return super.onKeyDown(keyCode, event);
    }

    boolean mapIsCentered() {
        LatLng current = map.getCameraPosition().target;
        double cur = current.latitude + current.longitude;
        double def = defaultPosition.latitude + defaultPosition.longitude;
        return ( (float) cur == (float) def );
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.map = googleMap;
        this.mapIsReady = true;
        this.map.setMapType(curMapType);

        this.map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                cameraIsMoving = true;
                firstLoad = false;
                Log.i(TAG, "onCameraMove Camera is moving");
            }
        });

        this.map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                cameraIsMoving = false;
                Log.i(TAG, "onCameraIdle Camera is idle.");
                if (defaultPosition == null & !firstLoad) {
                    defaultPosition = map.getCameraPosition().target;
                }
            }
        });

        infoWindowAdapter = new MyInfoWindowAdapter(this);
        this.map.setInfoWindowAdapter(infoWindowAdapter);

        new DelayedWorker(500, 0, new DelayedWorker.DelayedJob() {
            @Override
            public void doWork() {
                moveCameraToShowUSA(true);

                if (tripEntries.size() > 0) {
                    Log.i(TAG, "Drawing route...");
                    drawRoute(tripEntries);
                    moveCameraToShowMarkers(true, 550, CAMERA_PADDING);
                    showGooglePoly(toggleShowHideGoogle.isChecked());
                    populateDetails(clickedTrip);
                    populateAllAddresses();
                    Log.i(TAG, "onFinished route finished");
                } else {
                    getTripEntries(new DrawCompleteListener() {
                        @Override
                        public void onFinished() {
                            drawRoute(tripEntries);
                            moveCameraToShowMarkers(true, 550, CAMERA_PADDING);
                            showGooglePoly(toggleShowHideGoogle.isChecked());
                            populateDetails(clickedTrip);
                            populateAllAddresses();
                        }

                        @Override
                        public void onFailed(String msg) {

                        }
                    });
                }
            }

            @Override
            public void onComplete(Object object) {

            }
        });
    }

    void populateAllAddresses() {
        try {
            myMapMarkers = new ArrayList<>();
            UserAddresses userAddresses = UserAddresses.getSavedUserAddys();
            CrmAddresses crmAddresses = options.getAllSavedCrmAddresses();

            if (crmAddresses != null) {
                for (CrmAddress addy : crmAddresses.list) {
                    Bitmap pin = Helpers.Bitmaps.getBitmapFromResource(context, R.drawable.maps_hospital_32x37);
                    MarkerOptions marker = new MarkerOptions();
                    LatLng position = new LatLng(addy.latitude, addy.longitude);
                    marker.position(position);
                    marker.title(addy.accountName);
                    marker.icon(BitmapDescriptorFactory.fromBitmap(pin));

                    MyMapMarker myMapMarker = new MyMapMarker(addy);
                    myMapMarker.name = addy.accountName;
                    myMapMarker.marker = map.addMarker(marker);
                    myMapMarkers.add(myMapMarker);
                }
                infoWindowAdapter.setMyMapMarkers(myMapMarkers);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @SuppressLint("StaticFieldLeak")
    void getTripEntries(final DrawCompleteListener listener) {

    final MyProgressDialog dialog = new MyProgressDialog(context, "Loading trip...");

        new AsyncTask<String, String, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog.show();
            }

            @Override
            protected String doInBackground(String... strings) {
                MySqlDatasource ds = new MySqlDatasource();
                Log.i(TAG, "doWork Getting trip entries...");
                tripEntries = ds.getAllTripEntries(clickedTrip.getTripcode());
                clickedTrip.tripEntries = tripEntries;
                Log.i(TAG, "doWork Trip entries retrieved.");
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                listener.onFinished();
                dialog.dismiss();
            }
        }.execute(null, null, null);
    }

    @SuppressLint("SetTextI18n")
    void populateDetails(FullTrip clickedTrip) {
        txtTitle.setText(clickedTrip.getTitle());
        txtDistance.setText(clickedTrip.getDistanceInMiles() + " miles");
        txtDuration.setText(clickedTrip.getDurationInMinutes() + " minutes");
        txtDate.setText(clickedTrip.getPrettyDate());
        txtAvgSpeed.setText(clickedTrip.getAvgSpeedInMph() + " mph");
        txtTopSpeed.setText(clickedTrip.getTopSpeedInMph() + " mph");
        txtReimbursement.setText(clickedTrip.calculatePrettyReimbursement());
    }

    /** Shows/hides Google's polyline.  If the polyline hasn't already been built it creates a new one.  If it has, it reuses it.  **/
    public void showGooglePoly(boolean shouldShow) {

        toggleShowHideGoogle.setChecked(shouldShow);

        // Determine whether or not to draw Google's poly and whehter or not to query their server for
        // it or use our cached line.
        if (shouldShow) {
            if (map != null && mapIsReady) {
                if (googlePolyBuilder == null) {
                    new DrawGooglePolyline().execute(tripEntries.get(0).getLatLng(),
                            tripEntries.get(tripEntries.size() - 1).getLatLng(), null);
                } else {
                    googlePoly = map.addPolyline(googlePolyBuilder);
                }
            } else {
                if (googlePoly != null) {
                    googlePoly.remove();
                } else {
                    map.clear();
                    if (this.tripEntries != null) {
                        try {
                            drawRoute(tripEntries);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            if (googlePoly != null) {
                googlePoly.remove();
            }
        }

        if (shouldShow) {
            this.linearLayout_Shell.setBackgroundColor(Color.parseColor("#EF6100"));
            this.linearLayout_Contents.setBackgroundColor(Color.parseColor("#0026FF"));
            this.txt_GoogleDistanceValue.setVisibility(View.VISIBLE);
        } else {
            this.linearLayout_Shell.setBackgroundColor(Color.parseColor("#00FFFFFF"));
            this.linearLayout_Contents.setBackgroundColor(Color.parseColor("#00FFFFFF"));
            this.txt_GoogleDistanceValue.setVisibility(View.GONE);
        }

    }

    @SuppressLint("StaticFieldLeak")
    void drawRoute(final ArrayList<TripEntry> entries) {
        // Add start and end markers
        Bitmap pin = Helpers.Bitmaps.getBitmapFromResource(context, R.drawable.facility_map_pin_4);

        MarkerOptions startMarker = new MarkerOptions();
        LatLng startPos = entries.get(0).getLatLng();
        startMarker.position(startPos);
        startMarker.title(options.isExplicitMode() ? getString(R.string.start_marker_explicit) :
                getString(R.string.start_marker));
        startMarker.icon(BitmapDescriptorFactory.fromBitmap(pin));
        MarkerOptions endMarker = new MarkerOptions();
        LatLng endPos = entries.get(entries.size() - 1).getLatLng();
        endMarker.icon(BitmapDescriptorFactory.fromBitmap(pin));
        endMarker.title(options.isExplicitMode() ? getString(R.string.end_marker_explicit) :
                getString(R.string.end_marker));
        endMarker.position(endPos);

        mapMarkers = new ArrayList<>();

        Marker sMarker = map.addMarker(startMarker);
        Marker eMarker = map.addMarker(endMarker);

        mapMarkers.add(sMarker);
        mapMarkers.add(eMarker);

        // Draw the route.
        PolylineOptions poly = new PolylineOptions();
        for (TripEntry entry : entries) {
            LatLng latLng = entry.getLatLng();
            poly.add(latLng);
        }
        map.addPolyline(poly);

        sMarker.showInfoWindow();
        eMarker.showInfoWindow();
    }
    
    private void moveCameraToShowUSA(boolean animate) {

        Log.d(TAG, "Moving the camera to my location");

        double lat, lng;
        lat = 40.4655;
        lng = -95.5473;
        Location startingLoc = Helpers.Geo.createLocFromLatLng(new LatLng(lat,lng));
        CameraPosition overUSA = new CameraPosition.Builder()
                .target(new LatLng(lat, lng))
                .bearing(startingLoc.getBearing()).tilt(90f).zoom(3).build();
        if (animate == true) {
            map.animateCamera(CameraUpdateFactory.newCameraPosition(overUSA));
        } else {
            map.moveCamera(CameraUpdateFactory.newCameraPosition(overUSA));
        }
    }

    /** moves the camera to a position such that both the start and end map markers are viewable on screen.
     *  cameraAnimationDuration represents how long the animation should take (effectively the speed) in ms **/
    private void moveCameraToShowMarkers(boolean animate, @Nullable Integer cameraAnimationDuration, int padding) {

        Log.d(TAG, "Moving the camera to get all the markers in view");


        // Create a new LatLngBounds.Builder object
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        if (tripEntries != null) {
            for (TripEntry entry : tripEntries) {
                builder.include(entry.getLatLng());
            }
        }

        // Check that the two markers representing the start and end points are present
        if (mapMarkers != null) {
            // Loop through the array (array's a strong term though accurate as there's only two items (start and end points))
            for (Marker m : mapMarkers) {
                // Add the current marker's position to the builder object
                builder.include(m.getPosition());

                if (crmAddresses != null) {

                }

            }

            // Create a populated LatLngBounds object by calling the builder object's build() method
            LatLngBounds markerBounds = builder.build();
            // Create a CameraUpdate object using the new markerBounds object (and a bit of padding)
            // that we can feed to the map's resetCameraToCurrentAndEnableTripClipping method to actually and finally move the
            // camera to a position such that both start and end markers are visible.
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(markerBounds, padding);

            try {
                // Finally we actually get to move the damn camera
                if (animate) {
                    map.animateCamera(cu, cameraAnimationDuration, null);
                }else {
                    map.moveCamera(cu);
                }
                defaultPosition = map.getCameraPosition().target;
            } catch (Exception e) {
                finish();
                e.printStackTrace();
            }
        }
    }

    /** Async class that cases Google and using the returned data draws a polyline indicating the driving/waling path
     *  one would have to take to arrive at the saved parking spot **/
    public class DrawGooglePolyline extends AsyncTask<LatLng, LatLng, Boolean>
    {
        /*	Here's how you would call this class and execute your async task:

        -= Code begin =-
        String a=GetTodaysDate();
        String b=GetTodaysDate();
        new ContactGoogle().execute(a,b);
        -= Code end =-
        */
        public String rslt;
        MyMapRouteHelper gmap = new MyMapRouteHelper();
        ArrayList<LatLng> latLngList = new ArrayList<LatLng>();
        Document doc;
        Context context;
        boolean operationFailed = false;
        double dbl_totalPolyMeters = 0;
        String str_totalPolyMiles = "";

        @Override
        protected void onPreExecute() {
            txt_GoogleDistanceValue.setText("Doing the math...");
        }

        @Override
        protected Boolean doInBackground(LatLng... params) {
            try {
                LatLng startLat = params[0];
                LatLng endLat = params[1];
                doc = gmap.getDocument(startLat, endLat, MyMapRouteHelper.MODE_DRIVING);
                if (doc == null) {
                    operationFailed = true;
                    return false;
                }
                latLngList = gmap.getDirection(doc);
                dbl_totalPolyMeters = gmap.getTotalDistanceInmeters(doc);
                str_totalPolyMiles = Helpers.Geo.convertMetersToMiles(dbl_totalPolyMeters, true);
                return true;
            } catch (Exception e) {
                operationFailed = true;
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (operationFailed == true || result == false) {
                txt_GoogleDistanceValue.setText("Service couldn't be reached.");
                return;
            }

            googlePolyBuilder = new PolylineOptions();
            googlePolyBuilder.width(5);
            googlePolyBuilder.color(Color.BLUE);

            for (LatLng latLng : latLngList) {
                googlePolyBuilder.add(latLng);
            }

            //calculatedPolyline = line;
            googlePoly = map.addPolyline(googlePolyBuilder);
            //totalDistance = dis;
            txt_GoogleDistanceValue.setText("Google's route \u2245 " + str_totalPolyMiles);

        }

    }




}

































































