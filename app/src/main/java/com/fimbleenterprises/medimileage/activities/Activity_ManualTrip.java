package com.fimbleenterprises.medimileage.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.MyApp;
import com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities;
import com.fimbleenterprises.medimileage.objects_and_containers.FullTrip;
import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.objects_and_containers.MediUser;
import com.fimbleenterprises.medimileage.adapters.MyInfoWindowAdapter;
import com.fimbleenterprises.medimileage.objects_and_containers.MyMapMarker;
import com.fimbleenterprises.medimileage.MyMapRouteHelper;
import com.fimbleenterprises.medimileage.MyPreferencesHelper;
import com.fimbleenterprises.medimileage.MySqlDatasource;
import com.fimbleenterprises.medimileage.ui.CustomViews.MyUnderlineEditText;
import com.fimbleenterprises.medimileage.MyViewPager;
import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.objects_and_containers.TripEntry;
import com.fimbleenterprises.medimileage.objects_and_containers.UserAddresses;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.joda.time.DateTime;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerTitleStrip;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Activity_ManualTrip extends AppCompatActivity implements OnMapReadyCallback {

    public static AutocompleteSupportFragment autoCompleteFrag_From;
    public static AutocompleteSupportFragment autoCompleteFrag_To;
    public static Activity activity;
    public static EditText title;
    public static MyUnderlineEditText date;
    public static EditText distance;
    public static GoogleMap map;
    public static MapFragment mapFragment;
    public static Marker fromMarker;
    public static Marker toMarker;
    public static Context context;
    public static Polyline polyline;
    public static LatLng fromLatLng;
    public static LatLng toLatLng;
    public static String toTitle;
    public static String fromTitle;
    public static SweetAlertDialog pDialog;
    public static Button btnPrev;
    public static Button btnNext;
    public static MyViewPager mViewPager;
    public static PagerTitleStrip mPagerStrip;
    public static SectionsPagerAdapter sectionsPagerAdapter;
    public static androidx.fragment.app.FragmentManager fragMgr;
    public static RectangularBounds bounds;
    public static String distanceStr;
    public static MyPreferencesHelper options;
    ProgressBar prog;

    public static final int TAG_FROM = 0;
    public static final int TAG_TO = 1;
    public final static String TAG = "ManualTrip";
    public static final String TAG_TITLE = "TAG_TITLE";
    public static final String TAG_DATE = "TAG_DATE";
    public static final String TAG_DISTANCE = "TAG_DISTANCE";
    public static final String TAG_TO_LOC = "TAG_TO_MARKER";
    public static final String TAG_FROM_LOC = "TAG_FROM_MARKER";
    public static final String TAG_TO_TITLE = "TAG_TO_TITLE";
    public static final String TAG_FROM_TITLE = "TAG_FROM_TITLE";
    private ArrayList<MyMapMarker> myMapMarkers;
    private MyInfoWindowAdapter infoWindowAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        activity = this;
        options = new MyPreferencesHelper(context);

        setContentView(R.layout.activity_manual_trip);
        // getActionBar().setDisplayHomeAsUpEnabled(true);
        prog = findViewById(R.id.progress_map_loading);
        prog.setVisibility(View.VISIBLE);

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (MyViewPager) findViewById(R.id.main_pager_yo);
        mViewPager.onRealPageChangedListener = new MyViewPager.OnRealPageChangedListener() {
            @Override
            public void onPageActuallyFuckingChanged(int pageIndex) {
                if (pageIndex == 4) {
                    btnNext.setText("Save");
                    btnNext.setBackgroundResource(R.drawable.btn_glass_gray_orange_border);
                } else {
                    btnNext.setText("Next");
                    btnNext.setBackgroundResource(R.drawable.btn_glass_gray);
                }
            }
        };
        mPagerStrip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
        mViewPager.setAdapter(sectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(0);
        mViewPager.setCurrentItem(0);
        mViewPager.setPageCount(6);
        mViewPager.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                Log.i(TAG, "onScrollChange scrollX: " + scrollX + " scrollY: " + scrollY + "" +
                        "oldScrollX: " + oldScrollX + " oldScrollY: " + oldScrollY);
                Log.i(TAG, "onScrollChange Page: " + mViewPager.currentPosition);
            }
        });


        fragMgr = getSupportFragmentManager();

        btnPrev = findViewById(R.id.btn_prev_view);
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int curPos = mViewPager.currentPosition;
                Log.i(TAG, "curPos: " + curPos);
                if (mViewPager.currentPosition != 0) {
                    mViewPager.setCurrentItem(curPos - 1, true);
                }
            }
        });

        btnNext = findViewById(R.id.btn_next_view);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int curPos = mViewPager.currentPosition;

                if (btnNext.getText().toString().toLowerCase().equals("save")) {
                    saveTrip();
                    return;
                }

                if (curPos == 1 && fromLatLng == null) {
                    Toast.makeText(context, "Please add a starting position", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (curPos == 2 && toLatLng == null) {
                    Toast.makeText(context, "Please add an ending position", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.i(TAG, "curPos: " + curPos);
                if (mViewPager.currentPosition != 4) {
                    mViewPager.setCurrentItem(curPos + 1, true);
                }
            }
        });

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key), Locale.US);
        }

        createMap();

        if (savedInstanceState != null) {
            if (savedInstanceState.get(TAG_TITLE) != null) {
                title.setText(savedInstanceState.getString(TAG_TITLE));
            }
            if (savedInstanceState.get(TAG_DATE) != null) {
                date.setText(savedInstanceState.getString(TAG_DATE));
            }
            if (savedInstanceState.get(TAG_DISTANCE) != null) {
                distance.setText(savedInstanceState.getString(TAG_DISTANCE));
            }
            if (savedInstanceState.get(TAG_FROM_LOC) != null) {
                fromLatLng = savedInstanceState.getParcelable(TAG_FROM_LOC);
                fromTitle = savedInstanceState.getString(TAG_FROM_TITLE);
            }
            if (savedInstanceState.get(TAG_TO_LOC) != null) {
                toLatLng = savedInstanceState.getParcelable(TAG_TO_LOC);
                toTitle = savedInstanceState.getString(TAG_TO_TITLE);
            }
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApp.setIsVisible(false, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApp.setIsVisible(true, this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (polyline != null) {
            polyline.remove();
        }
        polyline = null;

        fromLatLng = null;
        toLatLng = null;

        if (fromMarker != null) {
            fromMarker.remove();
        }
        fromMarker = null;

        if (toMarker != null) {
            toMarker.remove();
        }
        toMarker = null;

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(androidx.fragment.app.FragmentManager fm) {
            super(fm);
            sectionsPagerAdapter = this;

        }

        @Override
        public Fragment getItem(int position) {

            Log.d("getItem", "Creating Fragment in pager at index: " + position);
            Log.w(TAG, "getItem: PAGER POSITION: " + position);

            if (position == 0) {
                Fragment fragment = new Frag_Title();
                Bundle args = new Bundle();
                args.putInt(Frag_Title.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == 1) {
                Fragment fragment = new Frag_From();
                Bundle args = new Bundle();
                args.putInt(Frag_From.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == 2) {
                Fragment fragment = new Frag_To();
                Bundle args = new Bundle();
                args.putInt(Frag_To.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == 3) {
                Fragment fragment = new Frag_Date();
                Bundle args = new Bundle();
                args.putInt(Frag_Date.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == 4) {
                Fragment fragment = new Frag_Distance();
                Bundle args = new Bundle();
                args.putInt(Frag_Distance.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }/*

            if (position == 5) {
                Fragment fragment = new Frag_Submit();
                Bundle args = new Bundle();
                args.putInt(Frag_Submit.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }
*/
            return null;
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return "Title";
                case 1:
                    return "From";
                case 2:
                    return "To";
                case 3:
                    return "Date";
                case 4:
                    return "Distance";/*
                case 5:
                    return "Submit";*/
            }
            return null;
        }
    }

    //region ********************************** TRIP FRAGS *****************************************

    public static class Frag_Title extends Fragment {
        private View rootView;
        public static final String ARG_SECTION_NUMBER = "section_number";

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.man_trip_title, container, false);
            title = rootView.findViewById(R.id.textView_title_value);

            return rootView;
        }

    }

    public static class Frag_From extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View rootView;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.man_trip_from, container, false);
            super.onCreateView(inflater, container, savedInstanceState);

            androidx.fragment.app.FragmentManager mgr = getChildFragmentManager();
            autoCompleteFrag_From = (AutocompleteSupportFragment)
                    mgr.findFragmentById(R.id.autocomplete_fragment_from);


            // Specify the types of place data to return.
            autoCompleteFrag_From.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));

            // Set up a PlaceSelectionListener to handle the response.
            autoCompleteFrag_From.setOnPlaceSelectedListener(new com.google.android.libraries.places.widget.listener.PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull com.google.android.libraries.places.api.model.Place place) {
                    if (place.getName() != null && place.getName().length() > 0) {
                        setFromMarker(place.getLatLng(), place.getName().toString());
                    } else {
                        setFromMarker(place.getLatLng());
                    }
                }

                @Override
                public void onError(Status status) {
                    Log.i(TAG, "An error occurred: " + status);
                }
            });

            // Limit results to the US
            autoCompleteFrag_From.setCountry("US");


            // Set up the area that the completes are biased towards (if supplied)
            if (bounds != null) {
                autoCompleteFrag_From.setLocationBias(bounds);
            }

            return rootView;
        }

    }

    public static class Frag_To extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        private View rootView;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.man_trip_to, container, false);
            super.onCreateView(inflater, container, savedInstanceState);

            androidx.fragment.app.FragmentManager mgr = getChildFragmentManager();
            autoCompleteFrag_To = (AutocompleteSupportFragment)
                    mgr.findFragmentById(R.id.autocomplete_fragment_to);

            // Specify the types of place data to return.
            autoCompleteFrag_To.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));

            // Set up a PlaceSelectionListener to handle the response.
            autoCompleteFrag_To.setOnPlaceSelectedListener(new PlaceSelectionListener() {

                @Override
                public void onPlaceSelected(@NonNull com.google.android.libraries.places.api.model.Place place) {
                    if (place.getName() != null && place.getName().length() > 0) {
                        setToMarker(place.getLatLng(), place.getName());
                    } else {
                        setToMarker(place.getLatLng());
                    }
                }

                @Override
                public void onError(Status status) {
                    Log.i(TAG, "An error occurred: " + status);
                }
            });

            autoCompleteFrag_To.setCountry("US");

            if (bounds != null) {
                autoCompleteFrag_To.setLocationBias(bounds);
            }

            return rootView;
        }

    }

    public static class Frag_Date extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        private View rootView;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.man_trip_date, container, false);
            date = rootView.findViewById(R.id.textView_date_value);
            date.setAsDatePicker(true);
            date.setText(Helpers.DatesAndTimes.getPrettyDate(DateTime.now()));

            return rootView;
        }

    }

    public static class Frag_Distance extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        private View rootView;


        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.man_trip_distance, container, false);
            distance = rootView.findViewById(R.id.textView_distance_value);
            distance.setText(distanceStr);
            return rootView;
        }

    }

    //endregion **************************** END TRIP FRAGS ****************************************

    private void saveTrip() {

        if (options.getReimbursementRate() == 0) {

        }

        try {
            // Instantiate a datasource
            MySqlDatasource ds = new MySqlDatasource();
            MediUser user = MediUser.getMe();
            FullTrip fullTrip = new FullTrip(date.getDateSelectedAsDateTime().getMillis(), user.domainname, user.systemuserid, user.email);

            fullTrip.setTitle(title.getText().toString());
            fullTrip.setDateTime(date.getDateSelectedAsDateTime());
            fullTrip.setMilis(fullTrip.getTripcode());
            float distmiles = Float.parseFloat(distance.getText().toString());
            float dist = Helpers.Geo.convertMilesToMeters(distmiles, 2);
            fullTrip.setDistance(dist);
            fullTrip.setIsManualTrip(true);
            fullTrip.setReimbursementRate((float) options.getReimbursementRate());

            ds.createNewTrip(fullTrip);

            Location lastLoc = null;
            for (LatLng latLng : polyline.getPoints()) {

                Location location = new Location("gps");
                location.setLatitude(latLng.latitude);
                location.setLongitude(latLng.longitude);

                if (lastLoc == null) {
                    lastLoc = location;
                }

                TripEntry entry = new TripEntry();
                entry.setLatitude(location.getLatitude());
                entry.setLongitude(location.getLongitude());
                entry.setTripcode(fullTrip.getTripcode());
                entry.setGuid(user.systemuserid);
                entry.setDateTime(date.getDateSelectedAsDateTime());
                entry.setMilis(entry.getDateTime().getMillis());
                entry.setDistance(location.distanceTo(lastLoc));
                entry.setSpeed(0);
                ds.appendTrip(entry);

                lastLoc = location;

            }

            setResult(Activity.RESULT_OK);
            finish();

        } catch (Exception e) {
            e.printStackTrace();
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }

    void showProgress(boolean value, String msg) {
        if (pDialog == null) {
            pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        }
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText(msg);
        pDialog.setCancelable(false);
        try {
            if (value == true) {
                pDialog.show();
            } else {
                pDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (fromMarker != null) {
            outState.putParcelable(TAG_FROM_LOC, fromMarker.getPosition());
            outState.putString(TAG_FROM_TITLE, fromMarker.getTitle());
        }
        if (toMarker != null) {
            outState.putParcelable(TAG_TO_LOC, toMarker.getPosition());
            outState.putString(TAG_TO_TITLE, toMarker.getTitle());
        }
        outState.putString(TAG_TITLE, title.getText().toString());
        outState.putString(TAG_DATE, date.getText().toString());
        outState.putString(TAG_DISTANCE, date.getText().toString());

        super.onSaveInstanceState(outState);
    }

    /**
     * Begins an async operation to create the map.  The overridden "onMapReady" method
     * is called when it is ready.
     */
    public void createMap() {

        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.map = googleMap;
        moveCameraToLastKnownLoc();

        // See if the markers locs and titles were retrieved from the savedInstance state and
        // add those markers using that data.  If we try this in onCreate it will crash as the
        // map will never be ready at that time.
        if (fromLatLng != null) {
            setFromMarker(fromLatLng, fromTitle);
        }
        if (toLatLng != null) {
            setToMarker(toLatLng, toTitle);
            moveCameraToShowMarkers();
        }

        this.map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                addNextMarker(latLng);
                if ((mViewPager.currentPosition == 0 || mViewPager.currentPosition == 1) &&
                        fromMarker != null && toMarker != null) {
                    mViewPager.setCurrentItem(3, true);
                } else if ((mViewPager.currentPosition == 0 || mViewPager.currentPosition == 1)
                        && fromMarker != null && toMarker == null) {
                    mViewPager.setCurrentItem(2, true);
                } else {
                    mViewPager.setCurrentItem(3, true);
                }
            }
        });

        this.map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.i(TAG, "onMarkerClick " + marker.getPosition().latitude + " - " +
                        marker.getPosition().longitude);
                return false;
            }
        });

        this.map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if ((int) marker.getTag() == TAG_FROM) {
                    setFromMarker(marker.getPosition());
                }
                if ((int) marker.getTag() == TAG_TO) {
                    setToMarker(marker.getPosition());
                }
            }
        });

        prog.setVisibility(View.GONE);

        populateAllAddresses();
        /*LinearLayout masterLayout = findViewById(R.id.layout_master);
        masterLayout.setBackgroundColor(Color.parseColor("#1435EB")) ;*/

    }

    void populateAllAddresses() {
        try {
            myMapMarkers = new ArrayList<>();
            UserAddresses userAddresses = UserAddresses.getSavedUserAddys();
            CrmEntities.CrmAddresses crmAddresses = options.getAllSavedCrmAddresses();

            if (crmAddresses != null) {
                for (CrmEntities.CrmAddresses.CrmAddress addy : crmAddresses.list) {
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

    /**
     * Adds the next appropriate marker - if there is no FROM marker it creates that.  If there is a FROM
     * marker but no TO marker it creates that.
     * @param latLng Where to place the pin.
     */
    void addNextMarker(LatLng latLng) {

        BitmapDescriptor fromIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        BitmapDescriptor toIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);

        if (fromMarker == null) {
            setFromMarker(latLng);
        } else if (fromMarker != null && toMarker == null) {
            setToMarker(latLng);
        }
        moveCameraToShowMarkers();
    }

    /**
     * Draws a poly line using Google's suggested route between the TO and FROM markers.
     */
    static void calculateRoute() {
        if (fromMarker != null && toMarker != null && map != null) {
            new DrawRoute().execute(fromLatLng, toLatLng);
        }
    }

    /**
     * Places the FROM marker on the map
     * @param latLng The location to stick the pin.
     * @param markerTitle The title shown when the pin is clicked
     */
    static void setFromMarker(LatLng latLng, @Nullable String markerTitle) {

        fromLatLng = latLng;

        if (fromMarker != null) {
            fromMarker.remove();
        }

        BitmapDescriptor fromIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        if (markerTitle != null) {
            markerOptions.title(markerTitle);
        } else {
            markerOptions.title("Start");
        }
        markerOptions.icon(fromIcon);
        markerOptions.draggable(true);
        fromMarker = map.addMarker(markerOptions);
        fromMarker.setTag(TAG_FROM);
        moveCameraToShowMarkers();
        calculateRoute();
    }

    /**
     * Places the FROM marker on the map
     * @param latLng The location to stick the pin.
     */
    static void setFromMarker(LatLng latLng) {
        setFromMarker(latLng, null);
    }

    /**
     * Places the TO marker on the map
     * @param latLng The location to stick the pin.
     * @param markerTitle The title shown when the pin is clicked
     */
    static void setToMarker(LatLng latLng, @Nullable String markerTitle) {

        toLatLng = latLng;

        if (toMarker != null) {
            toMarker.remove();
        }

        BitmapDescriptor toIconIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        if (markerTitle != null) {
            markerOptions.title(markerTitle);
        } else {
            markerOptions.title("Finish");
        }
        markerOptions.icon(toIconIcon);
        markerOptions.draggable(true);
        toMarker = map.addMarker(markerOptions);
        toMarker.setTag(TAG_TO);
        moveCameraToShowMarkers();
        calculateRoute();
    }

    /**
     * Places the TO marker on the map
     * @param latLng The location to stick the pin.
     */
    static void setToMarker(LatLng latLng) {
        setToMarker(latLng, null);
    }

    /** Moves the camera to a position such that both the start and end map markers are viewable on screen. **/
    private static void moveCameraToShowMarkers() {

        Log.d(TAG, "Moving the camera to get all the markers in view");

        CameraUpdate cu;

        // Create a new LatLngBounds.Builder object
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLng singleLocation = new LatLng(0, 0);

        // Check that the two markers representing the start and end points are present
        if (fromMarker != null) {
            builder.include(fromMarker.getPosition());
            singleLocation = fromMarker.getPosition();
        }
        if (toMarker != null) {
            builder.include(toMarker.getPosition());
            singleLocation = toMarker.getPosition();
        }

        // If both markers are not null we'll move the camera using bounds.  Otherwise we will use
        // whichever marker has a position and move using a LatLngZoom
        if (fromMarker == null || toMarker == null) {
            Log.w(TAG, "moveCameraToShowMarkers: One marker is null - will animate using a LatLng");
            cu = CameraUpdateFactory.newLatLngZoom(singleLocation, 13);
        } else {
            Log.w(TAG, "moveCameraToShowMarkers We have two markers - will animate using bounds.");
            // Create a populated LatLngBounds object by calling the builder object's build() method
            LatLngBounds markerBounds = builder.build();
            // Create a CameraUpdate object using the new markerBounds object (and a bit of padding)
            // that we can feed to the map's resetCameraToCurrentAndEnableTripClipping method to actually and finally move the
            // camera to a position such that both start and end markers are visible.
            cu = CameraUpdateFactory.newLatLngBounds(markerBounds, 100);
        }

        try {
            // Finally we actually get to move the damn camera
            map.animateCamera(cu, 300, null);
        } catch (Exception e) {
            activity.finish();
            e.printStackTrace();
        }
    }

    /**
     * Uses the device's last known location to initially move the camera as well as create a rectangular
     * area of the map with which to bias the autocomplete results.
     */
    private void moveCameraToLastKnownLoc() {

        FusedLocationProviderClient fusedLocationClient;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> task = fusedLocationClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if (location == null) {
                    return;
                }

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(latLng, 13);
                map.animateCamera(cu, 100, null);
                RectangularBounds biasBounds = RectangularBounds.newInstance(
                    new LatLng(location.getLatitude() - 1.5, location.getLongitude() - 1.5),
                    new LatLng(location.getLatitude() + 1.5, location.getLongitude() + 1.5)
                );
                bounds = biasBounds;
            }
        });
    }

    /**
     * Moves the camera to the specified location with a zoom of 12 and padding of 300
     * @param location The location to move to.
     */
    private void moveCameraTo(Location location) {

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(latLng, 12);
        map.animateCamera(cu, 20, null);

    }

    public static class DrawRoute extends AsyncTask<LatLng, LatLng, Boolean> {
        public String rslt;
        MyMapRouteHelper gmap = new MyMapRouteHelper();
        ArrayList<LatLng> latLngList = new ArrayList<LatLng>();
        Document doc;
        boolean operationFailed = false;
        double distanceVal;

        @Override
        protected void onPreExecute() {
            Toast.makeText(context, "Calculating route...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Boolean doInBackground(LatLng... params) {
            try {
                MyMapRouteHelper routeHelper = new MyMapRouteHelper();
                Document document = routeHelper.getDocument(params[0], params[1], MyMapRouteHelper.MODE_DRIVING);
                latLngList = routeHelper.getDirection(document);

                int meters = routeHelper.getTotalDistanceInmeters(document);
                distanceVal = Double.parseDouble(Helpers.Geo.convertMetersToMiles((double)meters, false));
                distanceStr = Double.toString(distanceVal);
                return true;
            } catch (Exception e) {
                operationFailed = true;
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (result == false) {
                return;
            }

            PolylineOptions line = new PolylineOptions();
            line.width(5);
            line.color(Color.BLUE);

            for (LatLng latLng : latLngList) {
                line.add(latLng);
            }

            if (polyline != null) {
                polyline.remove();
            }
            polyline = map.addPolyline(line);
            distanceStr = Double.toString(distanceVal);
            try {
                distance.setText(distanceStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
