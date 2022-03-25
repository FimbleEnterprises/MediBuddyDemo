package com.fimbleenterprises.demobuddy.activities;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.fimbleenterprises.demobuddy.MyApp;
import com.fimbleenterprises.demobuddy.MyInterfaces;
import com.fimbleenterprises.demobuddy.activities.ui.views.MyAutoCompleteEditText;
import com.fimbleenterprises.demobuddy.activities.ui.views.MyHyperlinkTextview;
import com.fimbleenterprises.demobuddy.dialogs.MyDatePicker;
import com.fimbleenterprises.demobuddy.dialogs.MyProgressDialog;
import com.fimbleenterprises.demobuddy.activities.fullscreen_pickers.FullscreenActivityChooseRecentTrip;
import com.fimbleenterprises.demobuddy.objects_and_containers.CrmEntities;
import com.fimbleenterprises.demobuddy.objects_and_containers.FullTrip;
import com.fimbleenterprises.demobuddy.Helpers;
import com.fimbleenterprises.demobuddy.objects_and_containers.MediUser;
import com.fimbleenterprises.demobuddy.adapters.MyInfoWindowAdapter;
import com.fimbleenterprises.demobuddy.objects_and_containers.MyMapMarker;
import com.fimbleenterprises.demobuddy.MyMapRouteHelper;
import com.fimbleenterprises.demobuddy.MyPreferencesHelper;
import com.fimbleenterprises.demobuddy.MySqlDatasource;
import com.fimbleenterprises.demobuddy.MyViewPager;
import com.fimbleenterprises.demobuddy.R;
import com.fimbleenterprises.demobuddy.objects_and_containers.RecentOrSavedTrip;
import com.fimbleenterprises.demobuddy.objects_and_containers.TripEntry;
import com.fimbleenterprises.demobuddy.objects_and_containers.UserAddresses;
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
import java.util.List;
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

    public static final String ADD_MANUAL_TRIP = "ADD_MANUAL_TRIP";
    public static final String ERROR_MESSAGE = "ERROR_MESSAGE";
    public static final String DATE_INVALID_ACTION = "DATE_INVALID_ACTION";
    public static AutocompleteSupportFragment autoCompleteFrag_From;
    public static AutocompleteSupportFragment autoCompleteFrag_To;
    public static Activity activity;

    public static final String INTENT_TRIP_CHANGED = "INTENT_TRIP_CHANGED";

    // Editable views
    public static MyAutoCompleteEditText titleField;
    // public static MyUnderlineEditText dateField;
    public static Button dateButton;
    public static EditText distanceField;

    public static GoogleMap map;
    public static MapFragment mapFragment;
    public static Marker fromMarker;
    public static Marker toMarker;
    public static Context context;
    public static String tripTitle;
    public static DateTime tripDate;
    public static Polyline polyline;
    public static String distanceStr;
    public static LatLng fromLatLng;
    public static LatLng toLatLng;
    public static String toTitle;
    public static String fromTitle;
    public static SweetAlertDialog pDialog;
    public static Button btnPrev;
    public static MyHyperlinkTextview btnRecents;
    /**
     * This flag starts false and can only be true if the user selects a RecentOrSaved trip from the recents list.
     * It will flip back to false if the user selects either a FROM or TO address from the places autocomplete.
     */
    public static boolean overrideDistance = false;
    public static Button btnNext;
    public static MyViewPager mViewPager;
    public static PagerTitleStrip mPagerStrip;
    public static SectionsPagerAdapter sectionsPagerAdapter;
    public static androidx.fragment.app.FragmentManager fragMgr;
    public static RectangularBounds bounds;
    public static MyPreferencesHelper options;
    static List<LatLng> points = new ArrayList<>();
    ProgressBar prog;

    public static final int TAG_FROM = 0;
    public static final int TAG_TO = 1;
    public final static String TAG = "ManualTrip";
    private static final String TAG_TITLE = "TAG_TITLE";
    private static final String TAG_DATE = "TAG_DATE";
    private static final String TAG_DISTANCE = "TAG_DISTANCE";
    private static final String TAG_TO_LOC = "TAG_TO_MARKER";
    private static final String TAG_FROM_LOC = "TAG_FROM_MARKER";
    private static final String TAG_TO_TITLE = "TAG_TO_TITLE";
    private static final String TAG_FROM_TITLE = "TAG_FROM_TITLE";
    private MyInfoWindowAdapter infoWindowAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        activity = this;
        options = new MyPreferencesHelper(context);

        try {
            setContentView(R.layout.activity_manual_trip);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Please try again!", Toast.LENGTH_SHORT).show();
            finish();
        }
        // getActionBar().setDisplayHomeAsUpEnabled(true);
        prog = findViewById(R.id.progress_map_loading);
        prog.setVisibility(View.VISIBLE);

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (MyViewPager) findViewById(R.id.main_pager_yo);
        mViewPager.onRealPageChangedListener = new MyViewPager.OnRealPageChangedListener() {
            @Override
            public void onPageActuallyFuckingChanged(int pageIndex) {

                if (pageIndex > 0) {
                    if (tripTitle == null && tripDate != null) {
                        tripTitle = Helpers.DatesAndTimes.getPrettyDate(tripDate);
                        Log.w(TAG, "onPageActuallyFuckingChanged | Set trip title to: " + Helpers.DatesAndTimes.getPrettyDate(tripDate));
                    } else if (tripTitle == null && tripDate == null) {
                        tripDate = DateTime.now();
                        tripTitle = Helpers.DatesAndTimes.getPrettyDate(tripDate);
                        Log.w(TAG, "onPageActuallyFuckingChanged | Set trip title to: " + Helpers.DatesAndTimes.getPrettyDate(tripDate));
                    }
                }

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

        clear();

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

        btnRecents = findViewById(R.id.btnRecents);
        btnRecents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<RecentOrSavedTrip> recentOrSavedTrips = new MySqlDatasource().getAllRecentOrSavedTrips();
                // recentTrips = RecentOrSavedTrip.toBasicObjects(recentOrSavedTrips);
                // recentTrips.parentObject = new BasicObjects.BasicObject("","", null);
                FullscreenActivityChooseRecentTrip.showPicker(activity, recentOrSavedTrips, FullscreenActivityChooseRecentTrip.REQUESTCODE);
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
                tripTitle = savedInstanceState.getString(TAG_TITLE);
                titleField.setText(savedInstanceState.getString(TAG_TITLE));
            }
            if (savedInstanceState.get(TAG_DATE) != null) {
                tripDate = new DateTime(savedInstanceState.getLong(TAG_DATE));
                dateButton.setText(savedInstanceState.getString(TAG_DATE));
            }
            if (savedInstanceState.get(TAG_DISTANCE) != null) {
                distanceStr = savedInstanceState.getString(TAG_DISTANCE);
                distanceField.setText(savedInstanceState.getString(TAG_DISTANCE));
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            if (data.hasExtra(FullscreenActivityChooseRecentTrip.CHOICE_RESULT)) {
                RecentOrSavedTrip chosenTrip = data.getParcelableExtra(FullscreenActivityChooseRecentTrip.CHOICE_RESULT);
                if (chosenTrip != null) {
                    Toast.makeText(activity, "Loading trip: " + chosenTrip.name, Toast.LENGTH_SHORT).show();
                    loadSavedTrip(chosenTrip);
                    overrideDistance = true;
                }
            }
        }

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
            titleField = rootView.findViewById(R.id.textView_title_value);

            // Get and set autocomplete entries using existing trip names.
            // Create the object of ArrayAdapter with String
            // which hold the data as the list item.
            String[] tripNames = new MySqlDatasource().getTripNames();

            ArrayAdapter<String> adapter
                    = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.select_dialog_item,
                    tripNames);

            // Give the suggestion after 1 words.
            titleField.setThreshold(0);

            titleField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {
                        titleField.showDropDown();
                    } else {
                        tripTitle = titleField.getText().toString();
                    }

                }
            });

            titleField.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (titleField.hasFocus()) {
                        titleField.showDropDown();
                    }
                }
            });

            // Set the adapter for data as a list
            titleField.setAdapter(adapter);
            titleField.setTextColor(Color.BLACK);

            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            if (tripTitle != null) {
                titleField.setText(tripTitle);
            }
        }
    }

    public static class Frag_From extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View rootView;
        BroadcastReceiver tripChangedReceiver;

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
                        overrideDistance = false;
                        setFromMarker(place.getLatLng(), place.getName().toString());
                        fromTitle = place.getName();
                        EditText etPlace = (EditText) autoCompleteFrag_From.getView().findViewById(R.id.places_autocomplete_search_input);
                        etPlace.setHint(fromTitle);
                    } else {
                        overrideDistance = false;
                        setFromMarker(place.getLatLng());
                        fromTitle = place.getLatLng().latitude + "/" + place.getLatLng().longitude;
                        EditText etPlace = (EditText) autoCompleteFrag_From.getView().findViewById(R.id.places_autocomplete_search_input);
                        etPlace.setHint(fromTitle);
                    }
                }

                @Override
                public void onError(Status status) {
                    Log.i(TAG, "An error occurred: " + status);
                }
            });

            // Limit results to the US
            autoCompleteFrag_From.setCountry("US");

            if (fromLatLng != null && fromTitle != null && fromTitle.length() == 0) {
                fromTitle = "Lat: " + fromLatLng.latitude + "|Lon: " + fromLatLng.longitude;
            } else if (fromTitle != null) {
                EditText etPlace = (EditText) autoCompleteFrag_From.getView().findViewById(R.id.places_autocomplete_search_input);
                etPlace.setHint(fromTitle);
            }

            // Set up the area that the completes are biased towards (if supplied)
            if (bounds != null) {
                autoCompleteFrag_From.setLocationBias(bounds);
            }

            tripChangedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    onResume();
                }
            };

            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();

            getContext().registerReceiver(tripChangedReceiver, new IntentFilter(INTENT_TRIP_CHANGED));

            if (fromTitle != null && fromLatLng != null) {
                fromTitle = "Lat: " + fromLatLng.latitude + "|Lon: " + fromLatLng.longitude;
                EditText etPlace = (EditText) autoCompleteFrag_From.getView().findViewById(R.id.places_autocomplete_search_input);
                etPlace.setHint(fromTitle);
            }
        }

        @Override
        public void onStop() {
            super.onStop();
            if (tripChangedReceiver != null) {
                getContext().unregisterReceiver(tripChangedReceiver);
            }
        }
    }

    public static class Frag_To extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        private View rootView;
        BroadcastReceiver tripChangedReceiver;

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
                        overrideDistance = false;
                        setToMarker(place.getLatLng(), place.getName());
                        toTitle = place.getName();
                        EditText etPlace = (EditText) autoCompleteFrag_To.getView().findViewById(R.id.places_autocomplete_search_input);
                        etPlace.setHint(toTitle);
                    } else {
                        overrideDistance = false;
                        setToMarker(place.getLatLng());
                        toTitle = place.getLatLng().latitude + "/" + place.getLatLng().longitude;
                        EditText etPlace = (EditText) autoCompleteFrag_To.getView().findViewById(R.id.places_autocomplete_search_input);
                        etPlace.setHint(toTitle);

                    }
                }

                @Override
                public void onError(Status status) {
                    Log.i(TAG, "An error occurred: " + status);
                }
            });

            autoCompleteFrag_To.setCountry("US");

            if (toLatLng != null && toTitle != null && toTitle.length() == 0) {
                toTitle = "Lat: " + toLatLng.latitude + "|Lon: " + toLatLng.longitude;
            } else if (toTitle != null) {
                EditText etPlace = (EditText) autoCompleteFrag_To.getView().findViewById(R.id.places_autocomplete_search_input);
                etPlace.setHint(toTitle);
            }

            if (bounds != null) {
                autoCompleteFrag_To.setLocationBias(bounds);
            }

            tripChangedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    onResume();
                }
            };

            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();

            getContext().registerReceiver(tripChangedReceiver, new IntentFilter(INTENT_TRIP_CHANGED));

            if (toLatLng != null && toTitle != null && toTitle.length() == 0) {
                toTitle = "Lat: " + toLatLng.latitude + "|Lon: " + toLatLng.longitude;
            } else if (toTitle != null) {
                EditText etPlace = (EditText) autoCompleteFrag_To.getView().findViewById(R.id.places_autocomplete_search_input);
                etPlace.setHint(toTitle);
            }
        }

        @Override
        public void onStop() {
            super.onStop();
            if (tripChangedReceiver != null) {
                getContext().unregisterReceiver(tripChangedReceiver);
            }
        }
    }

    public static class Frag_Date extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        private View rootView;
        BroadcastReceiver tripChangedReceiver;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.man_trip_date, container, false);
            // dateField = rootView.findViewById(R.id.textView_date_value);
            // dateField.setAsDatePicker(true);
            dateButton = rootView.findViewById(R.id.btnTripDate);
            dateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MyDatePicker datePicker = new MyDatePicker(getContext(), tripDate, new MyInterfaces.DateSelector() {
                        @Override
                        public void onDateSelected(DateTime selectedDate, String selectedDateStr) {
                            tripDate = selectedDate;
                            dateButton.setText(Helpers.DatesAndTimes.getPrettyDate(selectedDate));
                        }
                    });
                    datePicker.show();
                }
            });

            if (tripDate == null) {
                tripDate = DateTime.now();
                dateButton.setText(Helpers.DatesAndTimes.getPrettyDate(DateTime.now()));
            } else {
                dateButton.setText(Helpers.DatesAndTimes.getPrettyDate(tripDate));
            }

            tripChangedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction() != null && intent.getAction().equals(DATE_INVALID_ACTION)) {
                           dateButton.performClick();
                    } else {
                        onResume();
                    }
                }
            };

            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            IntentFilter filter = new IntentFilter(INTENT_TRIP_CHANGED);
            filter.addAction(DATE_INVALID_ACTION);
            getContext().registerReceiver(tripChangedReceiver, filter);

            if (tripDate == null) {
                dateButton.setText(Helpers.DatesAndTimes.getPrettyDate(DateTime.now()));
            } else {
                dateButton.setText(Helpers.DatesAndTimes.getPrettyDate(tripDate));
            }
        }

        @Override
        public void onStop() {
            super.onStop();
            if (tripChangedReceiver != null) {
                getContext().unregisterReceiver(tripChangedReceiver);
            }
        }
    }

    public static class Frag_Distance extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        private View rootView;
        BroadcastReceiver tripChangedReceiver;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.man_trip_distance, container, false);
            distanceField = rootView.findViewById(R.id.textView_distance_value);
            distanceField.setText(distanceStr);

            tripChangedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    onResume();
                }
            };

            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            getContext().registerReceiver(tripChangedReceiver, new IntentFilter(INTENT_TRIP_CHANGED));
            distanceField.setText(distanceStr);
        }

        @Override
        public void onStop() {
            super.onStop();
            if (tripChangedReceiver != null) {
                getContext().unregisterReceiver(tripChangedReceiver);
            }
        }
    }

    //endregion **************************** END TRIP FRAGS ****************************************


    private void clear() {
        /*
            public static Marker fromMarker;
            public static Marker toMarker;
            public static String tripTitle;
            public static DateTime tripDate;
            public static Polyline polyline;
            public static String distanceStr;
            public static LatLng fromLatLng;
            public static LatLng toLatLng;
            public static String toTitle;
            public static String fromTitle;
         */

        fromMarker = null;
        toMarker = null;
        tripTitle = null;
        tripDate = null;
        polyline = null;
        distanceStr = null;
        fromLatLng = null;
        toLatLng = null;
        toTitle = null;
        fromTitle = null;


    }

    /**
     * Adds an entry to the recents table.  This is called by the saveTrip() method assuming that
     * method completes successfully.  If a trip is found with the same name it is (effectively) overwritten.
     */
    private void saveTripToRecents() {
        try {
            RecentOrSavedTrip trip = new RecentOrSavedTrip();
            trip.name = tripTitle;
            trip.distanceInMiles = Float.parseFloat(distanceField.getText().toString());
            trip.fromLat = fromLatLng.latitude;
            trip.fromLon = fromLatLng.longitude;
            trip.toLat = toLatLng.latitude;
            trip.toLon = toLatLng.longitude;
            new MySqlDatasource().createNewRecentOrSavedTrip(trip, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSavedTrip(RecentOrSavedTrip recentOrSavedTrip) {

        // Set the trip text.
        tripTitle = recentOrSavedTrip.name;
        if (titleField != null)
            titleField.setText(recentOrSavedTrip.name);

        // Set the date text
        tripDate = DateTime.now();
        if (dateButton != null) {
            dateButton.setText(Helpers.DatesAndTimes.getPrettyDate(tripDate));
        }

        // Set the distance text
        distanceStr = Float.toString(recentOrSavedTrip.distanceInMiles);
        if (distanceField != null)
            distanceField.setText(Float.toString(recentOrSavedTrip.distanceInMiles));

        // Set the first map marker.
        LatLng fromLatLng = new LatLng(recentOrSavedTrip.fromLat, recentOrSavedTrip.fromLon);
        fromTitle = "Lat: " + fromLatLng.latitude + "|Lon: " + fromLatLng.longitude;
        setFromMarker(fromLatLng);

        // Set the destination map marker.
        LatLng toLatLng = new LatLng(recentOrSavedTrip.toLat, recentOrSavedTrip.toLon);
        setToMarker(toLatLng);
        toTitle = "Lat: " + toLatLng.latitude + "|Lon: " + toLatLng.longitude;

        // Move to the date frag to give the user a chance to put the correct date (defaults to today).
        mViewPager.setCurrentItem(3, true);

        Intent intent = new Intent(INTENT_TRIP_CHANGED);
        sendBroadcast(intent);

    }

    private void saveTrip() {

        if (options.getReimbursementRate() == 0) {

        }

        try {
            // Instantiate a datasource
            final MySqlDatasource ds = new MySqlDatasource();
            final MediUser user = MediUser.getMe();

            // This should never be null but just in case.
            if (tripDate == null) {
                mViewPager.setCurrentItem(3);
                Intent setDateIntent = new Intent(DATE_INVALID_ACTION);
                sendBroadcast(setDateIntent);
                Toast.makeText(context, "Please set a trip date!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate the user's account
            if (user == null || user.domainname == null || user.systemuserid == null || user.email == null) {
                // Create a return intent to be received by the calling activity's onActivityResult() method
                Intent intentCreateFailed = new Intent(ADD_MANUAL_TRIP);

                // Store the error message and add it to the intent as an extra
                intentCreateFailed.putExtra(ERROR_MESSAGE, "User account is fucked up.  Logout/Login and try again!");

                // Set the result and close this activity
                setResult(Activity.RESULT_CANCELED, intentCreateFailed);
                finish();
                return;
            }

            // Create the base fulltrip object
            final FullTrip fullTrip = new FullTrip(new DateTime(tripDate).getMillis()
                    , user.domainname, user.systemuserid, user.email);

            // Set the title to the trip date if it's null
            if (tripTitle == null) {
                tripTitle = Helpers.DatesAndTimes.getPrettyDate(tripDate);
            }

            // Set title, date and millis
            fullTrip.setTitle(tripTitle);
            fullTrip.setDateTime(tripDate);
            fullTrip.setMillis(fullTrip.getTripcode());

            // Finish buliding the trip
            float distmiles = Float.parseFloat(distanceStr);
            float dist = Helpers.Geo.convertMilesToMeters(distmiles, 2);
            fullTrip.setDistance(dist);
            fullTrip.setIsManualTrip(true);
            fullTrip.setReimbursementRate((float) options.getReimbursementRate());
            ds.createNewTrip(fullTrip);

            // Create an asynctask to write entries to the database
            AsyncTask<String, String, String> task = new AsyncTask<String, String, String>() {
                MyProgressDialog progressDialog = new MyProgressDialog(context, "Preparing...");
                List<LatLng> points = polyline.getPoints();
                int i = 0;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    progressDialog.setTitleText("Writing trip entries...");
                    progressDialog.show();
                }

                @Override
                protected String doInBackground(String... args) {
                    Location lastLoc = null;

                    for (int j = 0; j < points.size(); j++) {
                        if (j % 100 == 0) {
                            LatLng point = points.get(j);
                            Location location = new Location("gps");
                            location.setLatitude(point.latitude);
                            location.setLongitude(point.longitude);

                            if (lastLoc == null) {
                                lastLoc = location;
                            }

                            TripEntry entry = new TripEntry();
                            entry.setLatitude(location.getLatitude());
                            entry.setLongitude(location.getLongitude());
                            entry.setTripcode(fullTrip.getTripcode());
                            entry.setGuid(user.systemuserid);
                            entry.setDateTime(tripDate);
                            entry.setMilis(entry.getDateTime().getMillis());
                            entry.setDistance(location.distanceTo(lastLoc));
                            entry.setSpeed(0);
                            ds.appendTrip(entry);

                            lastLoc = location;
                            i++;
                            publishProgress(Integer.toString(i));
                            Log.i(TAG, "doInBackground | Loop: " + i);
                        }
                    }
                    return null;
                }

                @Override
                protected void onProgressUpdate(String... values) {
                    super.onProgressUpdate(values);
                    double p = Double.parseDouble(values[0]);
                    double t = Double.parseDouble(Integer.toString(points.size()));
                    String progress = Helpers.Numbers.convertToPercentage( (p / t) * 100, true);
                    progressDialog.setContentText(progress);
                }

                @Override
                protected void onPostExecute(String val) {
                    super.onPostExecute(val);
                    progressDialog.dismiss();
                    saveTripToRecents();
                    Intent intent = new Intent(ADD_MANUAL_TRIP);
                    intent.putExtra(ADD_MANUAL_TRIP, fullTrip);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            };

            // The lack of this check has burned me before.  It's verbose and not always needed for reasons
            // unknown but I'd leave it!
            if(Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                task.execute();
            }

        } catch (Exception e) {
            e.printStackTrace();

            // Create a return intent to be received by the calling activity's onActivityResult() method
            Intent intentCreateFailed = new Intent(ADD_MANUAL_TRIP);

            // Store the error message and add it to the intent as an extra
            intentCreateFailed.putExtra(ERROR_MESSAGE, e.getLocalizedMessage());

            // Set the result and close this activity
            setResult(Activity.RESULT_CANCELED, intentCreateFailed);
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

        if (tripTitle != null) {
            outState.putString(TAG_TITLE, tripTitle);
        }

        if (tripDate != null) {
            outState.putLong(TAG_DATE, tripDate.getMillis());
        }

        if (distanceStr != null) {
            outState.putString(TAG_DISTANCE, distanceStr);
        }

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
            ArrayList<MyMapMarker> myMapMarkers = new ArrayList<>();
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
        moveCameraToShowMarkers();
        calculateRoute();

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

        ArrayList<LatLng> latLngList = new ArrayList<LatLng>();
        PolylineOptions line = new PolylineOptions();
        boolean operationFailed = false;
        double distanceVal;
        // MyProgressDialog mpd;
        int prog;
        int total;

        @Override
        protected void onPreExecute() {
            Toast.makeText(context, "Calculating route...", Toast.LENGTH_SHORT).show();
            /*mpd = new MyProgressDialog(context, "Drawing a path - hang tight...");
            mpd.show();*/
        }

        @Override
        protected Boolean doInBackground(LatLng... params) {
            try {
                MyMapRouteHelper routeHelper = new MyMapRouteHelper();
                Document document = routeHelper.getDocument(params[0], params[1],
                        MyMapRouteHelper.MODE_DRIVING);
                latLngList = routeHelper.getDirection(document);
                points = latLngList;

                // If the distanceStr is not null then the user probably loaded this trip from the
                // recents list and we can then forgo calculating the distance from the route.
                if (!overrideDistance) {
                    int meters = routeHelper.getTotalDistanceInmeters(document);
                    distanceVal = Double.parseDouble(Helpers.Geo.convertMetersToMiles((double)meters
                            , false));
                    distanceStr = Double.toString(distanceVal);
                } else {
                    distanceVal = Double.parseDouble(distanceStr);
                }

                prog = 1;
                total = latLngList.size();

                for (LatLng latLng : latLngList) {
                    line.add(latLng);
                    if (prog % 5 == 0) {
                        publishProgress();
                    }
                    prog++;
                }

                return true;
            } catch (Exception e) {
                operationFailed = true;
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(LatLng... values) {
            int pct = Helpers.Numbers.formatAsZeroDecimalPointNumber(prog / total);

                /*mpd.setContentText(pct + "%");
                mpd.setTitleText(pct + "%");*/
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (result == false) {
                return;
            }


            line.width(5);
            line.color(Color.BLUE);

            if (polyline != null) {
                polyline.remove();
            }
            polyline = map.addPolyline(line);
            distanceStr = Double.toString(distanceVal);
            try {
                distanceField.setText(distanceStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // mpd.dismiss();
        }
    }
}
