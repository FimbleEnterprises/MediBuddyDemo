package com.fimbleenterprises.demobuddy.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.fimbleenterprises.demobuddy.MyApp;
import com.fimbleenterprises.demobuddy.activities.ui.views.MyHyperlinkTextview;
import com.fimbleenterprises.demobuddy.activities.ui.views.NonScrollRecyclerView;
import com.fimbleenterprises.demobuddy.objects_and_containers.AggregateStats;
import com.fimbleenterprises.demobuddy.Crm;
import com.fimbleenterprises.demobuddy.CustomTypefaceSpan;
import com.fimbleenterprises.demobuddy.objects_and_containers.CrmEntities;
import com.fimbleenterprises.demobuddy.objects_and_containers.ExcelSpreadsheet;
import com.fimbleenterprises.demobuddy.objects_and_containers.FullTrip;
import com.fimbleenterprises.demobuddy.Helpers;
import com.fimbleenterprises.demobuddy.objects_and_containers.MileBuddyMetrics;
import com.fimbleenterprises.demobuddy.objects_and_containers.MileageUser;
import com.fimbleenterprises.demobuddy.dialogs.MyProgressDialog;
import com.fimbleenterprises.demobuddy.MyPreferencesHelper;
import com.fimbleenterprises.demobuddy.MySqlDatasource;
import com.fimbleenterprises.demobuddy.QueryFactory;
import com.fimbleenterprises.demobuddy.QueryFactory.Filter;
import com.fimbleenterprises.demobuddy.R;
import com.fimbleenterprises.demobuddy.objects_and_containers.Requests;
import com.fimbleenterprises.demobuddy.objects_and_containers.TripEntry;
import com.fimbleenterprises.demobuddy.adapters.TripListRecyclerAdapter;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import cz.msebera.android.httpclient.Header;
import com.fimbleenterprises.demobuddy.objects_and_containers.Requests.*;

public class UserTripsActivity extends AppCompatActivity implements TripListRecyclerAdapter.ItemClickListener {

    MyPreferencesHelper options;
    public static final String MILEAGE_USER = "USERID_INTENT";
    private static final String TAG = "UserTripsFragment";
    public static final String SALES_WEST_GUID = "CA022A1D-AEDE-E811-80EA-005056A36B9B";
    public static final String SALES_EAST_GUID = "08B9DA04-557C-E711-80D7-005056A36B9B";
    ArrayList<MileageUser> users = new ArrayList<>();
    TripListRecyclerAdapter adapter;
    ArrayList<FullTrip> allTrips = new ArrayList<>();
    NonScrollRecyclerView recyclerView;
    Switch switchLastMonth;
    LinearLayout llTop;
    public MileageUser user;
    Context context = this;
    ScrollView masterLayout;
    AggregateStats stats;

    LinearLayout userInfoContainer;
    TextView txtMtd;
    TextView txtMtdLastMonth;
    TextView txtMtdCount;
    TextView txtMtdCountLastMonth;
    TextView txtName;
    TextView txtRate;
    TextView txtRateLastMonth;
    MyHyperlinkTextview txtAccountAssociations;
    MyHyperlinkTextview txtOpportunityAssociations;
    TableRow tableRowAccountAssociations;
    TableRow tableRowOpportunityAssociations;
    RelativeLayout layoutAssociations;
    ProgressBar pbAssociations;

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

        this.options = new MyPreferencesHelper(this);
        this.recyclerView = findViewById(R.id.rvUserTrips);

        if (getIntent().getParcelableExtra(MILEAGE_USER) != null) {
            this.user = getIntent().getParcelableExtra(MILEAGE_USER);
        }

        // Log a metric
        MileBuddyMetrics.updateMetric(this, MileBuddyMetrics.MetricName.LAST_ACCESSED_OTHER_USER_TRIPS, DateTime.now());

        adapter = new TripListRecyclerAdapter(this, allTrips);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        llTop = findViewById(R.id.ll_top);
        userInfoContainer = findViewById(R.id.ll_top);
        txtName = findViewById(R.id.txtName);
        txtMtd = findViewById(R.id.txtReimbursementThisMonth);
        txtMtdLastMonth = findViewById(R.id.txtReimbursementLastMonth);
        txtMtdCount = findViewById(R.id.txtTripCountThisMonth);
        txtMtdCountLastMonth = findViewById(R.id.txtTripCountLastMonth);
        txtRate = findViewById(R.id.txtReimbursementRateThisMonth);
        txtRateLastMonth = findViewById(R.id.txtReimbursementRateLastMonth);
        userInfoContainer.setVisibility(View.INVISIBLE);
        masterLayout = findViewById(R.id.masterLayout);
        tableRowAccountAssociations = findViewById(R.id.rowFilesystem);
        tableRowOpportunityAssociations = findViewById(R.id.tableRow_02);
        layoutAssociations = findViewById(R.id.associationslayout);
        layoutAssociations.setVisibility(View.VISIBLE);
        tableRowAccountAssociations.setVisibility(View.GONE);
        tableRowOpportunityAssociations.setVisibility(View.INVISIBLE);
        pbAssociations = findViewById(R.id.pbAssociations);
        txtAccountAssociations = findViewById(R.id.txtNearbyAccounts);
        txtOpportunityAssociations = findViewById(R.id.txtNearbyOpportunities);

        Helpers.Views.MySwipeHandler mySwipeHandler = new Helpers.Views.MySwipeHandler(new Helpers.Views.MySwipeHandler.MySwipeListener() {
            @Override
            public void onSwipeLeft() {
                Log.i(TAG, "onSwipeLeft ");
            }

            @Override
            public void onSwipeRight() {
                Log.i(TAG, "onSwipeRight ");
                onBackPressed();
            }
        });
        mySwipeHandler.addView(masterLayout);
        mySwipeHandler.addView(recyclerView);

        getAllMtdTrips();   // Get the trips to date for the user
        getAssociations();  // Get the recent opportunity and account associations for the user

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (options.isExplicitMode()) {
            this.setTitle("Fucking Trips");
        } else {
            this.setTitle("Trips");
        }

        llTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Toast.makeText(context, "User: " + user.fullname, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, IndividualAggregateMileageStatsActivity.class);
                intent.putExtra(IndividualAggregateMileageStatsActivity.USER_TAG, user);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mileage_stats_single_user_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

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
            case R.id.action_export_to_excel :
                getAndExportAggregateStats();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void applyFontToMenuItem(MenuItem mi, Typeface font) {
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
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
    protected void onStop() {
        super.onStop();
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0 :
                break;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        try {

            if (isFinishing()) {
                return;
            }

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
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    : getString(R.string.today));
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
                            : getString(R.string.yesterday));
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
                            : getString(R.string.this_week));
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
                            : getString(R.string.last_month_and_older));
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
                txtName.setText(names[0] + " Fucking " + names[1] + " (" + user.milebuddyVersion + ")");
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
            txtName.setText(user.fullname + " (" + user.milebuddyVersion + ")");
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

    public void getAssociations() {

        // Grab a reference to the progressbar just to be safe
        pbAssociations = findViewById(R.id.pbAssociations);

        QueryFactory factory = new QueryFactory("msus_mileageassociation");
        factory.addColumn("msus_name");
        factory.addColumn("msus_disposition");
        factory.addColumn("msus_associated_trip");
        factory.addColumn("msus_associated_opportunity");
        factory.addColumn("msus_associated_account");
        factory.addColumn("createdon");
        factory.addColumn("msus_mileageassociationid");

        // Sort by create date desc
        factory.addSortClause(new QueryFactory.SortClause("createdon", true, QueryFactory.SortClause.ClausePosition.ONE));

        // Main query filter (must have associated opportunity)
        Filter mainFilter = new Filter(Filter.FilterType.AND, new Filter.FilterCondition("msus_associated_opportunity", Filter.Operator.CONTAINS_DATA));
        factory.setFilter(mainFilter);

        // Trip association - filter must have trip within last two months and owned by user being viewed.
        QueryFactory.LinkEntity linkEntityFullTrip = new QueryFactory.LinkEntity("msus_fulltrip","msus_fulltripid", "msus_associated_trip", "a_856aa51bdcf9ea11810b005056a36b9b");
        linkEntityFullTrip.addColumn(new QueryFactory.EntityColumn("msus_dt_tripdate"));
        linkEntityFullTrip.addColumn(new QueryFactory.EntityColumn("ownerid"));
        linkEntityFullTrip.addColumn(new QueryFactory.EntityColumn("msus_totaldistance"));

        Filter filter = new Filter(Filter.FilterType.AND);
        filter.conditions.add(new Filter.FilterCondition("msus_dt_tripdate", Filter.Operator.LAST_X_MONTHS, "2"));
        filter.conditions.add(new Filter.FilterCondition("ownerid", Filter.Operator.EQUALS, user.ownerid));
        linkEntityFullTrip.addFilter(filter);
        factory.addLinkEntity(linkEntityFullTrip);

        // Opportunity link entity
        QueryFactory.LinkEntity linkEntityOpportunity = new QueryFactory.LinkEntity("opportunity", "opportunityid", "msus_associated_opportunity", "a_eeba8cefdbf9ea11810b005056a36b9b");
        linkEntityOpportunity.addColumn(new QueryFactory.EntityColumn("name"));
        linkEntityOpportunity.addColumn(new QueryFactory.EntityColumn("modifiedon"));
        factory.addLinkEntity(linkEntityOpportunity);

        // Set distinct though this shouldn't really be necessary...
        factory.isDistinct(true);

        String query = factory.construct();

        Requests.Request request = new Requests.Request(Requests.Request.Function.GET);
        Requests.Argument argument = new Requests.Argument("query", query);
        request.arguments.add(argument);

        Crm crm = new Crm();
        crm.makeCrmRequest(this, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String rslt = new String(responseBody);
                Toast.makeText(UserTripsActivity.this, "rslt", Toast.LENGTH_SHORT).show();
                CrmEntities.TripAssociations tripAssociations = new CrmEntities.TripAssociations(rslt);
                populateAssociationSummaries(tripAssociations);
                pbAssociations.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(UserTripsActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                populateAssociationSummaries(null);
                pbAssociations.setVisibility(View.GONE);
            }
        });

        // Show the pb while the request is executed.
        pbAssociations.setVisibility(View.VISIBLE);
    }

    void populateAssociationSummaries(final CrmEntities.TripAssociations associations) {
        int accts = 0, opps = 0;

        for (CrmEntities.TripAssociations.TripAssociation association : associations.list) {
            if (association.associated_opportunity_id != null) { opps ++; }
            if (association.associated_account_id != null) { accts ++; }
        }

        txtAccountAssociations.setText(accts + " account associations");
        txtOpportunityAssociations.setText(opps + " nearby opportunities");
        txtOpportunityAssociations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ActivityAssociatedOpportunities.class);
                intent.putExtra(ActivityAssociatedOpportunities.OPPORTUNITY_LIST, associations.toGson());
                startActivity(intent);
            }
        });



        tableRowOpportunityAssociations.setVisibility(View.VISIBLE);
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
        filter.addCondition(new Filter.FilterCondition("ownerid", Filter.Operator.EQUALS, user.ownerid));
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
                stats = new AggregateStats(response);
                Log.i(TAG, "onSuccess | " + response);
                dialog.dismiss();
                String monthYear = Helpers.DatesAndTimes.getMonthName(DateTime.now().getMonthOfYear())
                        .toLowerCase().replace(" ", "") + "_" + DateTime.now().getYear();
                String filename = "milebuddy_aggregate_mileage_export_" + monthYear + "_"
                        + user.fullname.replace(" ","_") + ".xls";
                ExcelSpreadsheet spreadsheet = stats.exportToExcel(filename.toLowerCase());
                spreadsheet.share(context);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, "Failed to get stats", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                finish();
            }
        });
    }

}






















































