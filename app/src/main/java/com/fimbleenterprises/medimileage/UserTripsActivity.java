package com.fimbleenterprises.medimileage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.QueryFactory.Filter;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import cz.msebera.android.httpclient.Header;
import com.fimbleenterprises.medimileage.Requests.*;

public class UserTripsActivity extends AppCompatActivity implements TripListRecyclerAdapter.ItemClickListener {

    MySettingsHelper options;
    public static final String MILEAGE_USER = "USERID_INTENT";
    private static final String TAG = "UserTripsFragment";
    public static final String SALES_WEST_GUID = "CA022A1D-AEDE-E811-80EA-005056A36B9B";
    public static final String SALES_EAST_GUID = "08B9DA04-557C-E711-80D7-005056A36B9B";
    ArrayList<MileageUser> users = new ArrayList<>();
    TripListRecyclerAdapter adapter;
    ArrayList<FullTrip> allTrips = new ArrayList<>();
    RecyclerView recyclerView;
    Switch switchLastMonth;
    public MileageUser user;
    Context context;

    LinearLayout userInfoContainer;
    TextView txtMtd;
    TextView txtMtdLastMonth;
    TextView txtMtdCount;
    TextView txtMtdCountLastMonth;
    TextView txtName;
    TextView txtRate;
    TextView txtRateLastMonth;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Trip was created.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_trips);

        this.context = this;
        this.options = new MySettingsHelper(this);
        this.recyclerView = findViewById(R.id.rvUserTrips);

        if (getIntent().getParcelableExtra(MILEAGE_USER) != null) {
            this.user = getIntent().getParcelableExtra(MILEAGE_USER);
        }

        // Log a metric
        MileBuddyMetrics.updateMetric(this, MileBuddyMetrics.MetricName.LAST_ACCESSED_OTHER_USER_TRIPS, DateTime.now());

        adapter = new TripListRecyclerAdapter(this, allTrips);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        userInfoContainer = findViewById(R.id.ll_top);
        txtName = findViewById(R.id.txtName);
        txtMtd = findViewById(R.id.txtReimbursementThisMonth);
        txtMtdLastMonth = findViewById(R.id.txtReimbursementLastMonth);
        txtMtdCount = findViewById(R.id.txtTripCountThisMonth);
        txtMtdCountLastMonth = findViewById(R.id.txtTripCountLastMonth);
        txtRate = findViewById(R.id.txtReimbursementRateThisMonth);
        txtRateLastMonth = findViewById(R.id.txtReimbursementRateLastMonth);
        userInfoContainer.setVisibility(View.INVISIBLE);

        getAllMtdTrips();// Create the navigation up button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (options.isExplicitMode()) {
            this.setTitle("Fucking Trips");
        } else {
            this.setTitle("Trips");
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0 :
                break;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        final FullTrip clickedTrip = allTrips.get(position);

        if (clickedTrip.isSeparator) {
            return;
        }

        MySqlDatasource ds = new MySqlDatasource(this);

        final MyProgressDialog dialog = new MyProgressDialog(this,"Getting trip details...");
        dialog.show();

        Log.i(TAG, "onItemClick Getting trip details for tripcode: " + clickedTrip.getTripcode());


        QueryFactory queryFactory = new QueryFactory("msus_fulltrip");
        queryFactory.addColumn("msus_trip_entries_json");
        Filter.FilterCondition condition = new Filter.FilterCondition("msus_tripcode",
                Filter.Operator.EQUALS, Long.toString(clickedTrip.getTripcode()));
        Filter filter = new Filter(Filter.FilterType.AND, condition);
        queryFactory.setFilter(filter);
        String query = queryFactory.construct();

        Request request = new Request(Requests.Request.Function.GET);
        Argument arg = new Argument("query", query);
        request.arguments.add(arg);

        Crm crm = new Crm();
        crm.makeCrmRequest(this, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {


                try {
                    String responseStr = new String(responseBody);
                    JSONArray array = new JSONObject(responseStr).getJSONArray("value");
                    ArrayList<TripEntry> entries = TripEntry.parseCrmTripEntries(array.getJSONObject(0).getString("msus_trip_entries_json"));
                    // clickedTrip.setTrip=
                    Intent intent = new Intent(context, ViewTripActivity.class);
                    intent.putExtra(ViewTripActivity.CLICKED_TRIP, clickedTrip);
                    if (entries != null && entries.size() > 0) {
                        intent.putExtra(ViewTripActivity.TRIP_ENTRIES, entries);
                    }
                    startActivity(intent);

                    dialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                dialog.dismiss();
                Toast.makeText(context, "Failed to get trip details!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void populateTripList() {

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

        for (int i = 0; i < (allTrips.size()); i++) {
            int tripDayOfYear = Helpers.DatesAndTimes.returnDayOfYear(allTrips.get(i).getDateTime());
            int tripWeekOfYear = Helpers.DatesAndTimes.returnWeekOfYear(allTrips.get(i).getDateTime());
            int tripMonthOfYear = Helpers.DatesAndTimes.returnMonthOfYear(allTrips.get(i).getDateTime());

            // Trip was today
            if (tripDayOfYear == todayDayOfYear) {
                if (addedTodayHeader == false) {
                    FullTrip headerObj = new FullTrip();
                    headerObj.isSeparator = true;
                    headerObj.setTitle(options.isExplicitMode() ? getString(R.string.triplist_today_explicit)
                    : getString(R.string.triplist_today));
                    triplist.add(headerObj);
                    addedTodayHeader = true;
                    Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'Today' - This will not be added again!");
                }

                // Trip was yesterday
            } else if (tripDayOfYear == (todayDayOfYear - 1)) {
                if (addedYesterdayHeader == false) {
                    FullTrip headerObj = new FullTrip();
                    headerObj.isSeparator = true;
                    headerObj.setTitle(options.isExplicitMode() ? getString(R.string.triplist_yesterday_explicit)
                            : getString(R.string.triplist_yesterday));
                    triplist.add(headerObj);
                    addedYesterdayHeader = true;
                    Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'Yesterday' - This will not be added again!");
                }

                // Trip was this week
            } else if (tripWeekOfYear == todayWeekOfYear) {
                if (addedThisWeekHeader == false) {
                    FullTrip headerObj = new FullTrip();
                    headerObj.isSeparator = true;
                    headerObj.setTitle(options.isExplicitMode() ? getString(R.string.triplist_this_week_explicit)
                            : getString(R.string.triplist_this_week));
                    triplist.add(headerObj);
                    addedThisWeekHeader = true;
                    Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'This week' - This will not be added again!");
                }

                // Trip was this month
            } else if (tripMonthOfYear == todayMonthOfYear) {
                if (addedThisMonthHeader == false) {
                    FullTrip headerObj = new FullTrip();
                    headerObj.isSeparator = true;
                    headerObj.setTitle(options.isExplicitMode() ? getString(R.string.triplist_this_month_explicit)
                            : getString(R.string.triplist_this_month_explicit));
                    triplist.add(headerObj);
                    addedThisMonthHeader = true;
                    Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'This month' - This will not be added again!");
                }

                // Trip was older than this month
            } else if (tripMonthOfYear < todayMonthOfYear) {
                if (addedOlderHeader == false) {
                    FullTrip headerObj = new FullTrip();
                    headerObj.isSeparator = true;
                    headerObj.setTitle(options.isExplicitMode() ? getString(R.string.triplist_last_month_and_older_explicit)
                            : getString(R.string.triplist_last_month_and_older));
                    triplist.add(headerObj);
                    addedOlderHeader = true;
                    Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'Older' - This will not be added again!");
                }
            }
            triplist.add(allTrips.get(i));
        }

        // Since the new arraylist now has headers it will throw off the original array's indexes
        allTrips = triplist;
        adapter = new TripListRecyclerAdapter(this, triplist);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        float mtdTotal = 0f;
        float lastMonthTotal = 0f;
        int tripCount = 0;
        int tripCountLastMonth = 0;
        float rate = 0f;
        float rateLastMonth = 0f;

        for (FullTrip trip : allTrips) {
            DateTime tripDate = trip.getDateTime();
            DateTime curDate = new DateTime(System.currentTimeMillis());
            int thisMonth = curDate.getMonthOfYear();
            int lastMonth = curDate.minusMonths(1).getMonthOfYear();

            if (tripDate.getMonthOfYear() == thisMonth) {
                mtdTotal += trip.calculateReimbursement();
                tripCount += 1;
                rate = trip.getReimbursementRate();
            } else if (tripDate.getMonthOfYear() == lastMonth) {
                lastMonthTotal += trip.calculateReimbursement();
                tripCountLastMonth += 1;
                rateLastMonth = trip.getReimbursementRate();
            }
        }

        if (options.isExplicitMode()) {
            try {
                String[] names = user.fullname.split(" ");
                txtName.setText(names[0] + " Fucking " + names[1]);
                txtMtdCount.setText("" + tripCount + " fucking trips");
                txtMtdCountLastMonth.setText("" + tripCountLastMonth + " fucking trips");
                txtRate.setText("Rate: " + Helpers.Numbers.convertToCurrency((double) rate) + " / fucking mile");
                txtRateLastMonth.setText("Rate: " + Helpers.Numbers.convertToCurrency((double) rateLastMonth) + " / fucking mile");
            } catch (Exception e) {
                txtName.setText(user.fullname);
                txtMtdCount.setText("" + tripCount + " trips");
                txtMtdCountLastMonth.setText("" + tripCountLastMonth + " trips");
                txtRate.setText("Rate: " + Helpers.Numbers.convertToCurrency((double) rate) + " / mile");
                txtRateLastMonth.setText("Rate: " + Helpers.Numbers.convertToCurrency((double) rateLastMonth) + " / mile");
            }
        } else {
            txtName.setText(user.fullname);
            txtMtdCount.setText("" + tripCount + " trips");
            txtMtdCountLastMonth.setText("" + tripCountLastMonth + " trips");
            txtRate.setText("Rate: " + Helpers.Numbers.convertToCurrency((double) rate) + " / mile");
            txtRateLastMonth.setText("Rate: " + Helpers.Numbers.convertToCurrency((double) rateLastMonth) + " / mile");
        }

        txtMtd.setText(Helpers.Numbers.convertToCurrency(mtdTotal));
        txtMtdLastMonth.setText(Helpers.Numbers.convertToCurrency(lastMonthTotal));
        userInfoContainer.setVisibility(View.VISIBLE);



    }

    public void getAllMtdTrips() {

        final MyProgressDialog progressDialog = new MyProgressDialog(context, "Retrieving trips...");
        progressDialog.show();

        QueryFactory factory = new QueryFactory("msus_fulltrip");
        factory.addColumn("msus_fulltripid");
        factory.addColumn("msus_name");
        factory.addColumn("createdon");
        factory.addColumn("msus_trip_duration");
        factory.addColumn("msus_tripcode");
        factory.addColumn("msus_totaldistance");
        factory.addColumn("msus_reimbursement_rate");
        factory.addColumn("msus_reimbursement");
        factory.addColumn("ownerid");
        factory.addColumn("msus_tripdate");
        factory.addColumn("msus_dt_tripdate");
        factory.addColumn("msus_is_manual");
        factory.addColumn("msus_edited");
        factory.addColumn("msus_is_submitted");

        Filter.FilterCondition condition1 = new Filter.FilterCondition("msus_dt_tripdate", Filter.Operator.THIS_MONTH);
        Filter.FilterCondition condition2 = new Filter.FilterCondition("msus_dt_tripdate", Filter.Operator.LAST_MONTH);
        Filter filter = new Filter(Filter.FilterType.OR);
        filter.addCondition(condition1);
        filter.addCondition(condition2);
        factory.setFilter(filter);

        QueryFactory.LinkEntity linkEntity = new QueryFactory.LinkEntity("systemuser","systemuserid", "owninguser", "a_79740df757a5e81180e8005056a36b9b");
        linkEntity.addColumn(new QueryFactory.EntityColumn("employeeid"));
        factory.addLinkEntity(linkEntity);
        factory.addSortClause(new QueryFactory.SortClause("msus_dt_tripdate", true, QueryFactory.SortClause.ClausePosition.ONE));
        String query = factory.construct();

        Requests.Request request = new Requests.Request(Requests.Request.Function.GET);
        Requests.Argument argument = new Requests.Argument("query", query);
        request.arguments.add(argument);

        Crm crm = new Crm();
        crm.makeCrmRequest(this, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i(TAG, "onSuccess: StatusCode: " + statusCode);
                try {
                    JSONObject json = new JSONObject(new String(responseBody));
                    JSONArray array = json.getJSONArray("value");
                    ArrayList<FullTrip> trips = FullTrip.createTripsFromCrmJson(new String(responseBody), true);
                    allTrips.clear();

                    for (FullTrip trip : trips) {
                        if (trip.getOwnerid().equals(user.ownerid)) {
                            allTrips.add(trip);
                        }
                    }
                    Log.i(TAG, "onSuccess trip count:" + allTrips.size());
                    populateTripList();
                    progressDialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.w(TAG, "onFailure: " + error.getMessage());
                Toast.makeText(context, "Failed to load trips", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                finish();
            }
        });
    }

}






















































