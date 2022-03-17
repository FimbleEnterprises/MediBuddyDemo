package com.fimbleenterprises.medimileage.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/*import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;*/
import com.fimbleenterprises.medimileage.MyApp;
import com.fimbleenterprises.medimileage.adapters.BasicObjectRecyclerAdapter;
import com.fimbleenterprises.medimileage.adapters.LandingPageRecyclerAdapter;
import com.fimbleenterprises.medimileage.objects_and_containers.BasicObjects;
import com.fimbleenterprises.medimileage.dialogs.ContactActions;
import com.fimbleenterprises.medimileage.Crm;
import com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities;
import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.objects_and_containers.LandingPageItem;
import com.fimbleenterprises.medimileage.objects_and_containers.MediUser;
import com.fimbleenterprises.medimileage.objects_and_containers.MileBuddyMetrics;
import com.fimbleenterprises.medimileage.dialogs.MonthYearPickerDialog;
import com.fimbleenterprises.medimileage.MyInterfaces;
import com.fimbleenterprises.medimileage.MyPreferencesHelper;
import com.fimbleenterprises.medimileage.activities.ui.views.MyUnderlineEditText;
import com.fimbleenterprises.medimileage.MyViewPager;
import com.fimbleenterprises.medimileage.adapters.OrderLineRecyclerAdapter;
import com.fimbleenterprises.medimileage.CrmQueries;
import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.objects_and_containers.Opportunities;
import com.fimbleenterprises.medimileage.objects_and_containers.Requests;
import com.fimbleenterprises.medimileage.objects_and_containers.Territories.Territory;
import com.fimbleenterprises.medimileage.activities.fullscreen_pickers.FullscreenActivityChooseTerritory;
import com.fimbleenterprises.medimileage.objects_and_containers.Tickets;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import static com.fimbleenterprises.medimileage.CrmQueries.Tickets.ANY;
import static com.fimbleenterprises.medimileage.CrmQueries.Tickets.CLOSED;
import static com.fimbleenterprises.medimileage.CrmQueries.Tickets.OPEN;
import static com.fimbleenterprises.medimileage.activities.BasicEntityActivity.ENTITYID;
import static com.fimbleenterprises.medimileage.activities.BasicEntityActivity.ENTITY_LOGICAL_NAME;
import static com.fimbleenterprises.medimileage.activities.BasicEntityActivity.REQUEST_BASIC;
import static com.fimbleenterprises.medimileage.activities.BasicEntityActivity.SEND_EMAIL;
import static com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities.OrderProducts;
import static com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities.OrderProducts.OrderProduct;;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerTitleStrip;
import cz.msebera.android.httpclient.Header;

public class Activity_TerritoryData extends AppCompatActivity {

    public static final String REQUESTED_TERRITORY = "REQUESTED_TERRITORY";
    private static final String FAB_CLICKED = "FAB_CLICKED";
    public static Activity activity;
    public static EditText title;
    public static MyUnderlineEditText date;
    public static EditText distance;
    public static Marker fromMarker;
    public static Marker toMarker;
    public static Context context;
    public static Polyline polyline;
    public static LatLng fromLatLng;
    public static LatLng toLatLng;
    public static MyViewPager mViewPager;
    public static PagerTitleStrip mPagerStrip;
    public static SectionsPagerAdapter sectionsPagerAdapter;
    public static androidx.fragment.app.FragmentManager fragMgr;
    public static MyPreferencesHelper options;

    // Cached data
    public ArrayList<Territory> cachedTerritories = new ArrayList<>();
    public static ArrayList<OrderProducts.OrderProduct> allOrders;

    BroadcastReceiver fabClickReceiver;
    IntentFilter fabClickIntentFilter = new IntentFilter(FAB_CLICKED);
    SearchView searchView;

    public static Toolbar toolbar;

    public static final String MENU_SELECTION = "MENU_SELECTION";

    // Intent filters for the various fragment's broadcast receivers.
    // public static IntentFilter intentFilterMonthYear;

    // vars for the date ranges
    public static int monthNum;
    public static int yearNum;

    // vars for service agreement filters
    public enum ServiceAgreementFilter {
        EXPIRED, EXPIRING, CURRENT
    }
    public static ServiceAgreementFilter serviceAgreementFilter = ServiceAgreementFilter.EXPIRING;

    // vars for case status
    public static int case_status = ANY;
    public static int case_state = OPEN;

    public static CrmQueries.Leads.LeadFilter leadFilter = CrmQueries.Leads.LeadFilter.ANY;

    public static CrmQueries.Opportunities.DealStatus dealStatus = CrmQueries.Opportunities.DealStatus.ANY;

    // vars for customer type
    public static final int customer_type = CrmEntities.Accounts.ANY;

    // flag for region
    public static boolean isEastRegion = true;

    // var for territory shared with all fragments that want or need it
    public static Territory globalTerritory;

    // The popup dialog for goals represented by the chart.
    public static Dialog chartPopupDialog;

    public final static String TAG = "TerritoryData";
    // public static final String DATE_CHANGED = "DATE_CHANGED";
    public static final String MONTH = "MONTH";
    public static final String YEAR = "YEAR";

    public int curPageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = this;
        activity = this;
        options = new MyPreferencesHelper(getBaseContext());

        // Log a metric
        try {
            MileBuddyMetrics.updateMetric(context, MileBuddyMetrics.MetricName.LAST_ACCESSED_TERRITORY_DATA, DateTime.now());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // See if this activity was called with a specific territory intent extra
        if (getIntent() != null && getIntent().hasExtra(REQUESTED_TERRITORY)) {
            globalTerritory = (Territory) getIntent().getParcelableExtra(REQUESTED_TERRITORY);
        } else { // Otherwise use the user's saved territory
            globalTerritory = new Territory();
            globalTerritory.territoryid = MediUser.getMe().territoryid;
            globalTerritory.territoryName = MediUser.getMe().territoryname;
        }

        monthNum = DateTime.now().getMonthOfYear();
        yearNum = DateTime.now().getYear();

        setContentView(R.layout.activity_sales_perf);

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (MyViewPager) findViewById(R.id.main_pager_yo_sales_perf);
        mViewPager.onRealPageChangedListener = new MyViewPager.OnRealPageChangedListener() {
            @Override
            public void onPageActuallyFuckingChanged(int pageIndex) {
                setTitle(sectionsPagerAdapter.getPageTitle(pageIndex));
                options.setLastTerritoryTab(pageIndex);
            }
        };
        mPagerStrip = (PagerTitleStrip) findViewById(R.id.pager_title_strip_sales_perf);
        mViewPager.setAdapter(sectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(0);
        mViewPager.setCurrentItem(options.getLastTerritoryTab());
        // mViewPager.setPageCount(6);
        mViewPager.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                destroyChartDialogIfVisible();
            }
        });

        // Set the viewpager font
        Typeface fontTypeFace = getResources().getFont(R.font.casual);
        for (int i = 0; i < mViewPager.getChildCount(); ++i) {
            View nextChild = mViewPager.getChildAt(i);
            for (int j = 0; j < ((PagerTitleStrip) nextChild).getChildCount(); j++) {
                View subChild = ((PagerTitleStrip) nextChild).getChildAt(j);
                if (subChild instanceof TextView) {
                    TextView textViewToConvert = (TextView) subChild;
                    textViewToConvert.setTypeface(fontTypeFace, Typeface.BOLD);
                }
            }
        }

        fragMgr = getSupportFragmentManager();

        toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        fabClickReceiver = new BroadcastReceiver() {
            @SuppressLint("RestrictedApi") // Apparently a bug causes method: .openOptionsMenu() to raise a lint warning (https://stackoverflow.com/a/44926919/2097893).
            @Override
            public void onReceive(Context context, Intent intent) {
                Objects.requireNonNull(getSupportActionBar()).openOptionsMenu();
            }
        };
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (searchView != null && !searchView.isIconified()) {
                searchView.setIconified(true);
            } else if (mViewPager.currentPosition == SectionsPagerAdapter.LANDING_PAGE) {
                onBackPressed();
                return true;
            } else {
                mViewPager.setCurrentItem(SectionsPagerAdapter.LANDING_PAGE);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {

            if (resultCode == RESULT_OK) {
                Log.i(TAG, "onActivityResult RESULT_CODE == " + resultCode);
                if (data.getAction().equals(BasicEntityActivity.MENU_SELECTION)) {
                    Log.i(TAG, "onActivityResult " + MENU_SELECTION);

                    if (data.getStringExtra(SEND_EMAIL) != null) {

                        String emailSuffix = "";

                        Log.i(TAG, "onActivityResult Received a " + SEND_EMAIL + " result extra");
                        String entityid = data.getStringExtra(ENTITYID);
                        String entityLogicalName = data.getStringExtra(ENTITY_LOGICAL_NAME);
                        Log.i(TAG, "onActivityResult Entityid: " + entityid + " - Entity logical name: " + entityLogicalName);
                        Log.i(TAG, "onActivityResult ");

                        String recordurl = "";
                        String subject = "";

                        if (entityLogicalName.equals("opportunity")) {
                            recordurl = Crm.getRecordUrl(entityid, Integer.toString(Crm.ETC_OPPORTUNITY));
                            subject = "Opportunity";
                            emailSuffix = "\n\nCRM Link:\n" + recordurl;
                            Log.i(TAG, "onActivityResult:: " + recordurl);

                        } else if (entityLogicalName.equals("incident")) {
                            recordurl = Crm.getRecordUrl(entityid, Integer.toString(Crm.ETC_INCIDENT));
                            emailSuffix = "\n\nCRM Link:\n" + recordurl;
                            subject = "Ticket";
                            Log.i(TAG, "onActivityResult:: " + recordurl);
                        }

                        Helpers.Email.sendEmail(emailSuffix + "\n\n", "CRM Link", activity);

                    }

                }
            }

            globalTerritory = data.getParcelableExtra(FullscreenActivityChooseTerritory.TERRITORY_RESULT);
            cachedTerritories = data.getParcelableArrayListExtra(FullscreenActivityChooseTerritory.CACHED_TERRITORIES);
            sectionsPagerAdapter.notifyDataSetChanged();

            Intent intent = new Intent(MENU_SELECTION);
            intent.setAction(MENU_SELECTION);
            intent.putExtra(FullscreenActivityChooseTerritory.TERRITORY_RESULT, globalTerritory);
            sendBroadcast(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(fabClickReceiver, fabClickIntentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(fabClickReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        MyApp.setIsVisible(true, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MyApp.setIsVisible(false, this);
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

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.territory_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo( searchManager.getSearchableInfo(new
                ComponentName(this, SearchResultsActivity.class)));

        if (searchView != null) {
            searchView.setInputType(InputType.TYPE_CLASS_TEXT);
        }

        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onPreparePanel(int featureId, @Nullable View view, @NonNull Menu menu) {

        menu.findItem(R.id.action_east_region).setChecked(isEastRegion);
        menu.findItem(R.id.action_west_region).setChecked(!isEastRegion);

        switch (mViewPager.currentPosition) {
            case SectionsPagerAdapter.LANDING_PAGE: // Sales lines
                menu.findItem(R.id.action_west_region).setVisible(false);
                menu.findItem(R.id.action_east_region).setVisible(false);
                menu.findItem(R.id.action_this_year).setVisible(false);
                menu.findItem(R.id.action_last_year).setVisible(false);

                menu.findItem(R.id.action_this_month).setVisible(false);
                menu.findItem(R.id.action_last_month).setVisible(false);
                menu.findItem(R.id.action_choose_month).setVisible(false);
                menu.findItem(R.id.action_choose_territory).setVisible(true);
                menu.findItem(R.id.action_lead_type).setVisible(false);
                menu.findItem(R.id.action_opportunity_status).setVisible(false);

                menu.findItem(R.id.action_case_state).setVisible(false);
                menu.findItem(R.id.action_case_status).setVisible(false);

                menu.findItem(R.id.action_account_type).setVisible(false);

                menu.findItem(R.id.action_expired_service_agreements).setVisible(false);
                menu.findItem(R.id.action_expiring_service_agreements).setVisible(false);
                menu.findItem(R.id.action_current_service_agreements).setVisible(false);
                break;
            case SectionsPagerAdapter.SALES_PAGE: // Sales lines
                menu.findItem(R.id.action_west_region).setVisible(false);
                menu.findItem(R.id.action_east_region).setVisible(false);
                menu.findItem(R.id.action_this_year).setVisible(false);
                menu.findItem(R.id.action_last_year).setVisible(false);

                menu.findItem(R.id.action_this_month).setVisible(true);
                menu.findItem(R.id.action_last_month).setVisible(true);
                menu.findItem(R.id.action_choose_month).setVisible(true);
                menu.findItem(R.id.action_choose_territory).setVisible(true);
                menu.findItem(R.id.action_lead_type).setVisible(false);
                menu.findItem(R.id.action_opportunity_status).setVisible(false);

                menu.findItem(R.id.action_case_state).setVisible(false);
                menu.findItem(R.id.action_case_status).setVisible(false);

                menu.findItem(R.id.action_account_type).setVisible(false);

                menu.findItem(R.id.action_expired_service_agreements).setVisible(false);
                menu.findItem(R.id.action_expiring_service_agreements).setVisible(false);
                menu.findItem(R.id.action_current_service_agreements).setVisible(false);
                break;

            case SectionsPagerAdapter.OPPORTUNITIES_PAGE: // Opportunities
                menu.findItem(R.id.action_opportunity_status).setVisible(true);
                menu.findItem(R.id.action_choose_territory).setVisible(true);
                menu.findItem(R.id.action_west_region).setVisible(false);
                menu.findItem(R.id.action_east_region).setVisible(false);
                menu.findItem(R.id.action_this_year).setVisible(false);
                menu.findItem(R.id.action_last_year).setVisible(false);

                menu.findItem(R.id.action_this_month).setVisible(false);
                menu.findItem(R.id.action_last_month).setVisible(false);
                menu.findItem(R.id.action_choose_month).setVisible(false);

                menu.findItem(R.id.action_account_type).setVisible(false);

                menu.findItem(R.id.action_case_state).setVisible(false);
                menu.findItem(R.id.action_case_status).setVisible(false);
                menu.findItem(R.id.action_lead_type).setVisible(false);

                menu.findItem(R.id.action_expired_service_agreements).setVisible(false);
                menu.findItem(R.id.action_expiring_service_agreements).setVisible(false);
                menu.findItem(R.id.action_current_service_agreements).setVisible(false);
                break;
                
            case SectionsPagerAdapter.LEADS_PAGE:

                menu.findItem(R.id.action_opportunity_status).setVisible(false);
                menu.findItem(R.id.action_lead_type).setVisible(true);

                menu.findItem(R.id.action_west_region).setVisible(false);
                menu.findItem(R.id.action_east_region).setVisible(false);
                menu.findItem(R.id.action_this_year).setVisible(false);
                menu.findItem(R.id.action_last_year).setVisible(false);

                menu.findItem(R.id.action_this_month).setVisible(false);
                menu.findItem(R.id.action_last_month).setVisible(false);
                menu.findItem(R.id.action_choose_month).setVisible(false);
                menu.findItem(R.id.action_choose_territory).setVisible(true);

                menu.findItem(R.id.action_case_state).setVisible(false);
                menu.findItem(R.id.action_case_status).setVisible(false);

                menu.findItem(R.id.action_account_type).setVisible(false);

                menu.findItem(R.id.action_expired_service_agreements).setVisible(false);
                menu.findItem(R.id.action_expiring_service_agreements).setVisible(false);
                menu.findItem(R.id.action_current_service_agreements).setVisible(false);
                break;

            case SectionsPagerAdapter.ACCOUNTS_PAGE: // Accounts
                menu.findItem(R.id.action_opportunity_status).setVisible(false);
                menu.findItem(R.id.action_west_region).setVisible(false);
                menu.findItem(R.id.action_east_region).setVisible(false);
                menu.findItem(R.id.action_this_year).setVisible(false);
                menu.findItem(R.id.action_last_year).setVisible(false);

                menu.findItem(R.id.action_this_month).setVisible(false);
                menu.findItem(R.id.action_last_month).setVisible(false);
                menu.findItem(R.id.action_choose_month).setVisible(false);
                menu.findItem(R.id.action_choose_territory).setVisible(true);

                menu.findItem(R.id.action_case_state).setVisible(false);
                menu.findItem(R.id.action_case_status).setVisible(false);

                menu.findItem(R.id.action_account_type).setVisible(false);

                menu.findItem(R.id.action_expired_service_agreements).setVisible(false);
                menu.findItem(R.id.action_expiring_service_agreements).setVisible(false);
                menu.findItem(R.id.action_current_service_agreements).setVisible(false);
                menu.findItem(R.id.action_lead_type).setVisible(false);
                break;

            case SectionsPagerAdapter.CASES_PAGE: // Cases
                menu.findItem(R.id.action_opportunity_status).setVisible(false);
                menu.findItem(R.id.action_west_region).setVisible(false);
                menu.findItem(R.id.action_east_region).setVisible(false);
                menu.findItem(R.id.action_this_year).setVisible(false);
                menu.findItem(R.id.action_last_year).setVisible(false);

                menu.findItem(R.id.action_this_month).setVisible(false);
                menu.findItem(R.id.action_last_month).setVisible(false);
                menu.findItem(R.id.action_choose_month).setVisible(false);
                menu.findItem(R.id.action_choose_territory).setVisible(true);

                menu.findItem(R.id.action_account_type).setVisible(false);

                menu.findItem(R.id.action_case_state).setVisible(true);
                menu.findItem(R.id.action_case_status).setVisible(true);

                menu.findItem(R.id.action_expired_service_agreements).setVisible(false);
                menu.findItem(R.id.action_expiring_service_agreements).setVisible(false);
                menu.findItem(R.id.action_current_service_agreements).setVisible(false);
                menu.findItem(R.id.action_lead_type).setVisible(false);
                break;

            case SectionsPagerAdapter.SERVICE_AGREEMENTS_PAGE:
                menu.findItem(R.id.action_opportunity_status).setVisible(false);
                menu.findItem(R.id.action_choose_territory).setVisible(true);
                menu.findItem(R.id.action_west_region).setVisible(false);
                menu.findItem(R.id.action_east_region).setVisible(false);
                menu.findItem(R.id.action_this_year).setVisible(false);
                menu.findItem(R.id.action_last_year).setVisible(false);

                menu.findItem(R.id.action_this_month).setVisible(false);
                menu.findItem(R.id.action_last_month).setVisible(false);
                menu.findItem(R.id.action_choose_month).setVisible(false);

                menu.findItem(R.id.action_account_type).setVisible(false);

                menu.findItem(R.id.action_case_state).setVisible(false);
                menu.findItem(R.id.action_case_status).setVisible(false);
                menu.findItem(R.id.action_lead_type).setVisible(false);

                menu.findItem(R.id.action_expired_service_agreements).setVisible(true);
                menu.findItem(R.id.action_expiring_service_agreements).setVisible(true);
                menu.findItem(R.id.action_current_service_agreements).setVisible(true);
                break;
        }

        // Check/Uncheck deal status
        menu.findItem(R.id.action_opp_any).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.ANY);
        menu.findItem(R.id.action_opp_stalled).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.STALLED);
        menu.findItem(R.id.action_opp_qualifying).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.QUALIFYING);
        menu.findItem(R.id.action_opp_dead).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.DEAD);
        menu.findItem(R.id.action_opp_evaluating).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.EVALUATING);
        menu.findItem(R.id.action_opp_pending).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.PENDING);
        menu.findItem(R.id.action_opp_closed).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.CLOSED);
        menu.findItem(R.id.action_opp_canceled).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.CANCELED);
        menu.findItem(R.id.action_opp_discovery).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.DISCOVERY);
        menu.findItem(R.id.action_opp_won).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.WON);

        // Check/Uncheck lead filters
        menu.findItem(R.id.action_lead_any).setChecked(leadFilter == CrmQueries.Leads.LeadFilter.ANY);
        menu.findItem(R.id.action_lead_qualified).setChecked(leadFilter == CrmQueries.Leads.LeadFilter.QUALIFIED);
        menu.findItem(R.id.action_lead_disqualified).setChecked(leadFilter == CrmQueries.Leads.LeadFilter.DISQUALIFIED);
        menu.findItem(R.id.action_lead_created_last_three_months).setChecked(leadFilter == CrmQueries.Leads.LeadFilter.LAST_THREE_MONTHS);

        // Set case state values
        switch (case_state) {
            case OPEN:
                menu.findItem(R.id.action_case_state_open).setChecked(true);
                menu.findItem(R.id.action_case_state_closed).setChecked(false);
                break;
            case CLOSED:
                menu.findItem(R.id.action_case_state_open).setChecked(false);
                menu.findItem(R.id.action_case_state_closed).setChecked(true);
                break;
        }

        // Set case status values
        switch (case_status) {
            case ANY:
                menu.findItem(R.id.action_change_case_status_any).setChecked(true);
                menu.findItem(R.id.action_change_case_status_inprogress).setChecked(false);
                menu.findItem(R.id.action_change_case_status_on_hold).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_inspected).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_for_product).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_rep).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_customer).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_billed).setChecked(false);
                break;
            case CrmQueries.Tickets.IN_PROGRESS :
                menu.findItem(R.id.action_change_case_status_any).setChecked(false);
                menu.findItem(R.id.action_change_case_status_inprogress).setChecked(true);
                menu.findItem(R.id.action_change_case_status_on_hold).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_inspected).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_for_product).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_customer).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_rep).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_billed).setChecked(false);
                break;
            case CrmQueries.Tickets.ON_HOLD :
                menu.findItem(R.id.action_change_case_status_any).setChecked(false);
                menu.findItem(R.id.action_change_case_status_inprogress).setChecked(false);
                menu.findItem(R.id.action_change_case_status_on_hold).setChecked(true);
                menu.findItem(R.id.action_change_case_status_to_be_inspected).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_for_product).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_customer).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_rep).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_billed).setChecked(false);
                break;
            case CrmQueries.Tickets.TO_BE_INSPECTED :
                menu.findItem(R.id.action_change_case_status_any).setChecked(false);
                menu.findItem(R.id.action_change_case_status_inprogress).setChecked(false);
                menu.findItem(R.id.action_change_case_status_on_hold).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_inspected).setChecked(true);
                menu.findItem(R.id.action_change_case_status_waiting_for_product).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_customer).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_rep).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_billed).setChecked(false);
                break;
            case CrmQueries.Tickets.WAITING_FOR_PRODUCT :
                menu.findItem(R.id.action_change_case_status_any).setChecked(false);
                menu.findItem(R.id.action_change_case_status_inprogress).setChecked(false);
                menu.findItem(R.id.action_change_case_status_on_hold).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_inspected).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_for_product).setChecked(true);
                menu.findItem(R.id.action_change_case_status_waiting_on_customer).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_rep).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_billed).setChecked(false);
                break;
            case CrmQueries.Tickets.WAITING_ON_CUSTOMER :
                menu.findItem(R.id.action_change_case_status_any).setChecked(false);
                menu.findItem(R.id.action_change_case_status_inprogress).setChecked(false);
                menu.findItem(R.id.action_change_case_status_on_hold).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_inspected).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_for_product).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_customer).setChecked(true);
                menu.findItem(R.id.action_change_case_status_waiting_on_rep).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_billed).setChecked(false);
                break;
            case CrmQueries.Tickets.TO_BE_BILLED :
                menu.findItem(R.id.action_change_case_status_any).setChecked(false);
                menu.findItem(R.id.action_change_case_status_inprogress).setChecked(false);
                menu.findItem(R.id.action_change_case_status_on_hold).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_inspected).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_for_product).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_customer).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_rep).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_billed).setChecked(true);
                break;
            case CrmQueries.Tickets.WAITING_ON_REP :
                menu.findItem(R.id.action_change_case_status_any).setChecked(false);
                menu.findItem(R.id.action_change_case_status_inprogress).setChecked(false);
                menu.findItem(R.id.action_change_case_status_on_hold).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_inspected).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_for_product).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_customer).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_rep).setChecked(true);
                menu.findItem(R.id.action_change_case_status_to_be_billed).setChecked(false);
                break;
        }

        // Set service agreement values
        switch (serviceAgreementFilter) {
            case CURRENT:
                menu.findItem(R.id.action_current_service_agreements).setChecked(true);
                menu.findItem(R.id.action_expired_service_agreements).setChecked(false);
                menu.findItem(R.id.action_expiring_service_agreements).setChecked(false);
                break;
            case EXPIRED:
                menu.findItem(R.id.action_current_service_agreements).setChecked(false);
                menu.findItem(R.id.action_expired_service_agreements).setChecked(true);
                menu.findItem(R.id.action_expiring_service_agreements).setChecked(false);
                break;
            case EXPIRING:
                menu.findItem(R.id.action_current_service_agreements).setChecked(false);
                menu.findItem(R.id.action_expired_service_agreements).setChecked(false);
                menu.findItem(R.id.action_expiring_service_agreements).setChecked(true);
                break;
        }

        return super.onPreparePanel(featureId, view, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        DateTime now = DateTime.now();
        DateTime aMonthAgo = now.minusMonths(1);

        switch (item.getItemId()) {
            case 16908332 :
                if (searchView.isIconified()) {
                    onBackPressed();
                }
                break;
            case R.id.action_choose_territory :
                Intent intent = new Intent(context, FullscreenActivityChooseTerritory.class);
                intent.putExtra(FullscreenActivityChooseTerritory.CURRENT_TERRITORY, globalTerritory);
                startActivityForResult(intent, 0);
                break;
            case R.id.action_east_region :
                isEastRegion = true;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_west_region :
                isEastRegion = false;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_this_month :
                monthNum = now.getMonthOfYear();
                yearNum = now.getYear();
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_last_month :
                monthNum = aMonthAgo.getMonthOfYear();
                yearNum = aMonthAgo.getYear();
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_choose_month :
                showMonthYearPicker();
                break;
            case R.id.action_this_year :
                yearNum = now.getYear();
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_last_year :
                yearNum = aMonthAgo.getYear();
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_lead_any:
                leadFilter = CrmQueries.Leads.LeadFilter.ANY;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_lead_disqualified:
                leadFilter = CrmQueries.Leads.LeadFilter.DISQUALIFIED;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_lead_qualified:
                leadFilter = CrmQueries.Leads.LeadFilter.QUALIFIED;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_lead_created_last_three_months:
                leadFilter = CrmQueries.Leads.LeadFilter.LAST_THREE_MONTHS;
                sendMenuSelectionBroadcast();
                break;

            case R.id.action_opp_any:
                dealStatus = CrmQueries.Opportunities.DealStatus.ANY;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_opp_canceled:
                dealStatus = CrmQueries.Opportunities.DealStatus.CANCELED;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_opp_closed:
                dealStatus = CrmQueries.Opportunities.DealStatus.CLOSED;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_opp_dead:
                dealStatus = CrmQueries.Opportunities.DealStatus.DEAD;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_opp_discovery:
                dealStatus = CrmQueries.Opportunities.DealStatus.DISCOVERY;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_opp_evaluating:
                dealStatus = CrmQueries.Opportunities.DealStatus.EVALUATING;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_opp_pending:
                dealStatus = CrmQueries.Opportunities.DealStatus.PENDING;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_opp_qualifying:
                dealStatus = CrmQueries.Opportunities.DealStatus.QUALIFYING;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_opp_stalled:
                dealStatus = CrmQueries.Opportunities.DealStatus.STALLED;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_opp_won:
                dealStatus = CrmQueries.Opportunities.DealStatus.WON;
                sendMenuSelectionBroadcast();
                break;

            case R.id.action_case_state_open:
                case_state = OPEN;
                sendMenuSelectionBroadcast();
                break;

            case R.id.action_case_state_closed:
                case_state = CLOSED;
                sendMenuSelectionBroadcast();
                break;

            case R.id.action_change_case_status_any:
                case_status = ANY;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_change_case_status_inprogress:
                case_status = CrmQueries.Tickets.IN_PROGRESS;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_change_case_status_on_hold:
                case_status = CrmQueries.Tickets.ON_HOLD;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_change_case_status_to_be_inspected:
                case_status = CrmQueries.Tickets.TO_BE_INSPECTED;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_change_case_status_waiting_for_product:
                case_status = CrmQueries.Tickets.WAITING_FOR_PRODUCT;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_change_case_status_waiting_on_customer:
                case_status = CrmQueries.Tickets.WAITING_ON_CUSTOMER;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_change_case_status_waiting_on_rep:
                case_status = CrmQueries.Tickets.WAITING_ON_REP;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_change_case_status_to_be_billed:
                case_status = CrmQueries.Tickets.TO_BE_BILLED;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_change_case_status_problem_solved:
                case_status = CrmQueries.Tickets.PROBLEM_SOLVED;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_current_service_agreements:
                serviceAgreementFilter = ServiceAgreementFilter.CURRENT;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_expiring_service_agreements:
                serviceAgreementFilter = ServiceAgreementFilter.EXPIRING;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_expired_service_agreements:
                serviceAgreementFilter = ServiceAgreementFilter.EXPIRED;
                sendMenuSelectionBroadcast();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void destroyChartDialogIfVisible() {
        if (chartPopupDialog != null && chartPopupDialog.isShowing()) {
            chartPopupDialog.dismiss();
        }
    }

    public static void sendMenuSelectionBroadcast() {
        Intent menuActionIntent = new Intent(MENU_SELECTION);
        menuActionIntent.putExtra(MONTH, monthNum);
        menuActionIntent.putExtra(YEAR, yearNum);
        activity.sendBroadcast(menuActionIntent);
    }

    @SuppressLint("NewApi")
    private void showMonthYearPicker() {
        final MonthYearPickerDialog mpd = new MonthYearPickerDialog();
        mpd.setListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Intent dateChanged = new Intent(MENU_SELECTION);
                dateChanged.putExtra(MONTH, month);
                dateChanged.putExtra(YEAR, year);
                monthNum = month;
                yearNum = year;
                sendBroadcast(dateChanged);
                mpd.dismiss();
            }
        });
        mpd.show(getSupportFragmentManager(), "MonthYearPickerDialog");
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public static final int LANDING_PAGE = 0;
        public static final int SALES_PAGE = 1;
        public static final int LEADS_PAGE= 2;
        public static final int OPPORTUNITIES_PAGE = 3;
        public static final int CASES_PAGE = 4;
        public static final int ACCOUNTS_PAGE = 5;
        public static final int SERVICE_AGREEMENTS_PAGE = 6;

        public SectionsPagerAdapter(androidx.fragment.app.FragmentManager fm) {
            super(fm);
            sectionsPagerAdapter = this;
        }

        @Override
        public Fragment getItem(int position) {

            Log.d("getItem", "Creating Fragment in pager at index: " + position);
            Log.w(TAG, "getItem: PAGER POSITION: " + position);

            if (position == LANDING_PAGE) {
                Fragment fragment = new Frag_LandingPage();
                Bundle args = new Bundle();
                args.putInt(Frag_LandingPage.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == SALES_PAGE) {
                Fragment fragment = new Frag_SalesLines();
                Bundle args = new Bundle();
                args.putInt(Frag_SalesLines.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == LEADS_PAGE) {
                Fragment fragment = new Frag_Leads();
                Bundle args = new Bundle();
                args.putInt(Frag_SalesLines.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == OPPORTUNITIES_PAGE) {
                Fragment fragment = new Frag_Opportunities();
                Bundle args = new Bundle();
                args.putInt(Frag_Opportunities.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == CASES_PAGE) {
                Fragment fragment = new Frag_Cases();
                Bundle args = new Bundle();
                args.putInt(Frag_Opportunities.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == ACCOUNTS_PAGE) {
                Fragment fragment = new Frag_Accounts();
                Bundle args = new Bundle();
                args.putInt(Frag_Accounts.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == SERVICE_AGREEMENTS_PAGE) {
                Fragment fragment = new Frag_ServiceAgreements();
                Bundle args = new Bundle();
                args.putInt(Frag_ServiceAgreements.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            return null;
        }

        @Override
        public int getCount() {
            return 7;
        }
        
        public String getCaseCriteriaTitleAddendum() {
            switch (case_status) {
                case CrmQueries.Tickets.ANY:
                    return " - Any";
                case CrmQueries.Tickets.IN_PROGRESS :
                    return " - In progress";
                case CrmQueries.Tickets.ON_HOLD :
                    return " - On hold";
                case CrmQueries.Tickets.TO_BE_BILLED :
                    return " - To be billed";
                case CrmQueries.Tickets.TO_BE_INSPECTED :
                    return " - To be inspected";
                case CrmQueries.Tickets.WAITING_ON_CUSTOMER :
                    return " - Waiting on customer";
                case CrmQueries.Tickets.WAITING_ON_REP :
                    return " - Waiting on rep";
                case CrmQueries.Tickets.WAITING_FOR_PRODUCT :
                    return " - Waiting for product";
                case CrmQueries.Tickets.PROBLEM_SOLVED :
                    return " - Problem solved";
            }
            return null;
        }

        public String getOpportunityTitleAddendum() {
            switch (dealStatus) {
                case DEAD:
                    return "Dead";
                case CANCELED:
                    return "Canceled";
                case CLOSED:
                    return "Closed";
                case WON:
                    return "Won";
                case PENDING:
                    return "Pending";
                case EVALUATING:
                    return "Evaluating";
                case QUALIFYING:
                    return "Qualifying";
                case STALLED:
                    return "Stalled";
                case DISCOVERY:
                    return "Discovery";
                default:
                    return "All";
            }
        }

        public String getLeadsTitleAddendum() {
            switch (leadFilter) {
                case QUALIFIED:
                    return "Qualified";
                case DISQUALIFIED:
                    return "Disqualified";
                case LAST_THREE_MONTHS:
                    return "Last 3 months";
                default:
                    return "Any";
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {

            curPageIndex = position;

            if (globalTerritory == null) {
                globalTerritory = MediUser.getMe().getTerritory();
            }

            switch (position) {
                case LANDING_PAGE:
                    return "Territory " + globalTerritory.territoryName;
                case SALES_PAGE:
                    String monthName = Helpers.DatesAndTimes.getMonthName(monthNum);
                    String t = monthName + yearNum;
                    return "Sales Lines (" + globalTerritory.territoryName + ") " + t;
                case LEADS_PAGE:
                    return "Leads (" + globalTerritory.territoryName + ") " + getLeadsTitleAddendum();
                case OPPORTUNITIES_PAGE:
                    return "Opportunities (" + globalTerritory.territoryName + ") " + getOpportunityTitleAddendum();
                case CASES_PAGE:
                    return "Cases (" + globalTerritory.territoryName + ")" + getCaseCriteriaTitleAddendum();
                case ACCOUNTS_PAGE:
                    return "Accounts (" + globalTerritory.territoryName + ")";
                case SERVICE_AGREEMENTS_PAGE:
                    switch (serviceAgreementFilter) {
                        case CURRENT:
                            return "Active Service Agreements (" + globalTerritory.territoryName + ")";
                        case EXPIRED:
                            return "Expired Service Agreements (" + globalTerritory.territoryName + ")";
                        case EXPIRING:
                            return "Expiring Service Agreements (" + globalTerritory.territoryName + ")";
                    }
            }
            return null;
        }
    }

    //region ********************************** FRAGS *****************************************
    public static class Frag_LandingPage extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View root;
        public RecyclerView recyclerView;
        RefreshLayout refreshLayout;
        LandingPageRecyclerAdapter adapter;
        RecyclerView listview;
        Territory curTerritory;
        BroadcastReceiver territoryChangedReceiver;
        TextView txtNoData;
        Button btnGoToSales;
        BroadcastReceiver menuSelectionReceiver;
        FloatingActionButton fab;
        ArrayList<LandingPageItem> landingPageItems;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            root = inflater.inflate(R.layout.frag_dashboard, container, false);
            super.onCreateView(inflater, container, savedInstanceState);

            /*fab = root.findViewById(R.id.floatingActionButton);
            fab.setVisibility(View.GONE);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(FAB_CLICKED);
                    getActivity().sendBroadcast(intent);
                }
            });*/

            menuSelectionReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Received month and year broadcast! (sales lines frag)");
                    monthNum = intent.getIntExtra(MONTH, DateTime.now().getMonthOfYear());
                    yearNum = intent.getIntExtra(YEAR, DateTime.now().getYear());
                    // getDashboardData();
                    sectionsPagerAdapter.notifyDataSetChanged();
                }
            };

            territoryChangedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    curTerritory = globalTerritory;
                    // getDashboardData();
                }
            };

            populateList();

            return root;
        }

        @Override
        public void onStop() {
            super.onStop();

        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            getActivity().unregisterReceiver(menuSelectionReceiver);
            getActivity().unregisterReceiver(territoryChangedReceiver);
            Log.i(TAG, "onPause Unregistered the sales lines receiver");
        }

        @Override
        public void onResume() {
            IntentFilter filter = new IntentFilter(FullscreenActivityChooseTerritory.TERRITORY_RESULT);
            filter.addAction(MENU_SELECTION);
            getActivity().registerReceiver(menuSelectionReceiver, filter);
            getActivity().registerReceiver(territoryChangedReceiver, new IntentFilter(MENU_SELECTION));

            Log.i(TAG, "onResume Registered the sales lines receiver");
            super.onResume();
        }

        @Override
        public void onPause() {

            super.onPause();
        }

        void populateList() {

            landingPageItems = new ArrayList<>();
            landingPageItems.add(new LandingPageItem("Go to sales", SectionsPagerAdapter.SALES_PAGE, R.drawable.dollar6, R.drawable.arrow_right2));
            landingPageItems.add(new LandingPageItem("Go to leads", SectionsPagerAdapter.LEADS_PAGE, R.drawable.lead2, R.drawable.arrow_right2));
            landingPageItems.add(new LandingPageItem("Go to opportunities", SectionsPagerAdapter.OPPORTUNITIES_PAGE, R.drawable.opportunity1, R.drawable.arrow_right2));
            landingPageItems.add(new LandingPageItem("Go to cases", SectionsPagerAdapter.CASES_PAGE, R.drawable.ticket1, R.drawable.arrow_right2));
            landingPageItems.add(new LandingPageItem("Go to accounts", SectionsPagerAdapter.ACCOUNTS_PAGE, R.drawable.customer2, R.drawable.arrow_right2));
            landingPageItems.add(new LandingPageItem("Go to service agreements", SectionsPagerAdapter.SERVICE_AGREEMENTS_PAGE, R.drawable.contract32, R.drawable.arrow_right2));
            landingPageItems.add(new LandingPageItem("Change territory...", LandingPageItem.CHANGE_TERRITORY_CODE));
            listview = root.findViewById(R.id.recyclerView);
            adapter = new LandingPageRecyclerAdapter(context, landingPageItems);
            listview.setLayoutManager(new LinearLayoutManager(context));
            listview.setAdapter(adapter);
            adapter.setClickListener(new LandingPageRecyclerAdapter.ItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    LandingPageItem item = adapter.getItem(position);
                    if (item.pageIndex == LandingPageItem.CHANGE_TERRITORY_CODE) {
                        Intent intent = new Intent(context, FullscreenActivityChooseTerritory.class);
                        intent.putExtra(FullscreenActivityChooseTerritory.CURRENT_TERRITORY, globalTerritory);
                        startActivityForResult(intent, 0);
                    } else {
                        mViewPager.setCurrentItem(item.pageIndex, true);
                    }
                }
            });
        }

    }

    public static class Frag_SalesLines extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View root;
        public RecyclerView recyclerView;
        RefreshLayout refreshLayout;
        OrderLineRecyclerAdapter adapter;
        Territory curTerritory;
        BroadcastReceiver territoryChangedReceiver;
        ArrayList<OrderProducts.OrderProduct> allOrders = new ArrayList<>();
        TextView txtNoSales;
        BroadcastReceiver menuSelectionReceiver;
        FloatingActionButton fab;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            root = inflater.inflate(R.layout.frag_saleslines, container, false);
            txtNoSales = root.findViewById(R.id.txtNoContacts);
            refreshLayout = root.findViewById(R.id.refreshLayout);
            RefreshLayout refreshLayout = root.findViewById(R.id.refreshLayout);
            refreshLayout.setEnableLoadMore(false);
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshlayout) {
                    getSalesLines();
                }
            });
            refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore(RefreshLayout refreshlayout) {
                    refreshlayout.finishLoadMore(500/*,false*/);
                }
            });

            txtNoSales.setVisibility( (allOrders == null || allOrders.size() == 0) ? View.VISIBLE : View.GONE);

            recyclerView = root.findViewById(R.id.orderLinesRecyclerview);
            super.onCreateView(inflater, container, savedInstanceState);

            fab = root.findViewById(R.id.floatingActionButton);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(FAB_CLICKED);
                    getActivity().sendBroadcast(intent);
                }
            });

            menuSelectionReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Received month and year broadcast! (sales lines frag)");
                    monthNum = intent.getIntExtra(MONTH, DateTime.now().getMonthOfYear());
                    yearNum = intent.getIntExtra(YEAR, DateTime.now().getYear());
                    getSalesLines();
                    sectionsPagerAdapter.notifyDataSetChanged();
                }
            };

            if (allOrders == null || allOrders.size() == 0 || curTerritory != globalTerritory) {
                getSalesLines();
            } else {
                populateList();
            }

            territoryChangedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    curTerritory = globalTerritory;
                    getSalesLines();
                }
            };

            return root;
        }

        @Override
        public void onStop() {
            super.onStop();

        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            getActivity().unregisterReceiver(menuSelectionReceiver);
            getActivity().unregisterReceiver(territoryChangedReceiver);
            Log.i(TAG, "onPause Unregistered the sales lines receiver");
        }

        @Override
        public void onResume() {
            IntentFilter filter = new IntentFilter(FullscreenActivityChooseTerritory.TERRITORY_RESULT);
            filter.addAction(MENU_SELECTION);
            getActivity().registerReceiver(menuSelectionReceiver, filter);
            getActivity().registerReceiver(territoryChangedReceiver, new IntentFilter(MENU_SELECTION));

            Log.i(TAG, "onResume Registered the sales lines receiver");
            super.onResume();
        }

        @Override
        public void onPause() {

            super.onPause();
        }

        protected void getSalesLines() {
            refreshLayout.autoRefreshAnimationOnly();

            String query = null;

            if (monthNum == DateTime.now().getMonthOfYear()) {
                query = CrmQueries.OrderLines.getOrderLines(globalTerritory.territoryid,
                        CrmQueries.Operators.DateOperator.THIS_MONTH);
            } else if (monthNum == DateTime.now().minusMonths(1).getMonthOfYear()) {
                query = CrmQueries.OrderLines.getOrderLines(globalTerritory.territoryid,
                        CrmQueries.Operators.DateOperator.LAST_MONTH);
            } else {
                query = CrmQueries.OrderLines.getOrderLines(globalTerritory.territoryid, monthNum, yearNum);
            }

            txtNoSales.setVisibility(View.GONE);

            toolbar.setTitle(sectionsPagerAdapter.getPageTitle(mViewPager.currentPosition));

            ArrayList<Requests.Argument> args = new ArrayList<>();
            Requests.Argument argument = new Requests.Argument("query", query);
            args.add(argument);
            Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);
            Crm crm = new Crm();
            crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    try {
                        String response = new String(responseBody);
                        Log.i(TAG, "onSuccess " + response);
                        allOrders = new OrderProducts(response).list;
                        populateList();
                        refreshLayout.finishRefresh();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.w(TAG, "onFailure: " + error.getLocalizedMessage());

                    refreshLayout.finishRefresh();
                }
            });
        }

        protected void populateList() {

            ArrayList<OrderProducts.OrderProduct> orderList = new ArrayList<>();

            boolean addedTodayHeader = false;
            boolean addedYesterdayHeader = false;
            boolean addedThisWeekHeader = false;
            boolean addedThisMonthHeader = false;
            boolean addedLastMonthHeader = false;
            boolean otherHeader = false;

            float todaySubtotal = 0;
            float yesterdaySubtotal = 0;
            float thisWeekSubtotal = 0;
            float thisMonthSubtotal = 0;
            float lastMonthSubtotal = 0;
            float otherSubtotal = 0;

            // Sorting criteria based on date - these will be used when constructing header rows
            int todayDayOfYear = Helpers.DatesAndTimes.returnDayOfYear(DateTime.now());
            int todayWeekOfYear = Helpers.DatesAndTimes.returnWeekOfYear(DateTime.now());
            int todayMonthOfYear = Helpers.DatesAndTimes.returnMonthOfYear(DateTime.now());

            Log.i(TAG, "populateTripList: Preparing the dividers and trips...");
            for (int i = 0; i < (allOrders.size()); i++) {
                int orderDayOfYear = Helpers.DatesAndTimes.returnDayOfYear(allOrders.get(i).orderDate);
                int orderWeekOfYear = Helpers.DatesAndTimes.returnWeekOfYear(allOrders.get(i).orderDate);
                int tripMonthOfYear = Helpers.DatesAndTimes.returnMonthOfYear(allOrders.get(i).orderDate);
                OrderProduct curProduct = allOrders.get(i);
                // Trip was today
                if (orderDayOfYear == todayDayOfYear) {
                    if (addedTodayHeader == false) {
                        OrderProducts.OrderProduct headerObj = new OrderProducts.OrderProduct();
                        headerObj.isHeader = true;
                        headerObj.setTitle("Today");
                        orderList.add(headerObj);
                        addedTodayHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'Today' - This will not be added again!");
                    }
                    todaySubtotal += curProduct.extendedAmt;
                    // Trip was yesterday
                } else if (orderDayOfYear == (todayDayOfYear - 1)) {
                    if (addedYesterdayHeader == false) {
                        OrderProduct headerObj = new OrderProduct();
                        headerObj.isHeader = true;
                        headerObj.setTitle("Yesterday");
                        orderList.add(headerObj);
                        addedYesterdayHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'Yesterday' - This will not be added again!");
                    }
                    yesterdaySubtotal += curProduct.extendedAmt;
                    // Trip was this week
                } else if (orderWeekOfYear == todayWeekOfYear) {
                    if (addedThisWeekHeader == false) {
                        OrderProduct headerObj = new OrderProduct();
                        headerObj.isHeader = true;
                        headerObj.setTitle("This week");
                        orderList.add(headerObj);
                        addedThisWeekHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'This week' - This will not be added again!");
                    }
                    thisWeekSubtotal += curProduct.extendedAmt;
                    // Trip was this month
                } else if (tripMonthOfYear == todayMonthOfYear) {
                    if (addedThisMonthHeader == false) {
                        OrderProduct headerObj = new OrderProduct();
                        headerObj.isHeader = true;
                        headerObj.setTitle("This month");
                        orderList.add(headerObj);
                        addedThisMonthHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'This month' - This will not be added again!");
                    }
                    thisMonthSubtotal += curProduct.extendedAmt;
                    // Trip was last month
                } else if (curProduct.orderDate.getMonthOfYear() == DateTime.now().minusMonths(1).getMonthOfYear() &&
                    curProduct.orderDate.getYear() == DateTime.now().minusMonths(1).getYear()) {
                    if (addedLastMonthHeader == false) {
                        OrderProduct headerObj = new OrderProduct();
                        headerObj.isHeader = true;
                        headerObj.setTitle("Last month");
                        orderList.add(headerObj);
                        addedLastMonthHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'This month' - This will not be added again!");
                    }
                    lastMonthSubtotal += curProduct.extendedAmt;
                    // Trip was older than this month
                } else {
                    if (otherHeader == false) {
                        OrderProduct headerObj = new OrderProduct();
                        headerObj.isHeader = true;
                        headerObj.setTitle("Total");
                        orderList.add(headerObj);
                        otherHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'Older' - This will not be added again!");
                    }
                    otherSubtotal += curProduct.extendedAmt;
                }

                OrderProducts.OrderProduct orderProduct = allOrders.get(i);
                orderList.add(orderProduct);
            }

            // Sum today, yesterday, this week, etc.
            // This feels... right but I can't actually logic out if it is or is even necessary.
            thisMonthSubtotal = thisMonthSubtotal + thisWeekSubtotal + yesterdaySubtotal + todaySubtotal;
            thisWeekSubtotal = thisWeekSubtotal + yesterdaySubtotal + todaySubtotal;

            // Append the subtotals to the headers
            for (OrderProducts.OrderProduct product : orderList) {
                if (todaySubtotal > 0) {
                    if (product.isHeader && product.partNumber.equals("Today")) {
                        product.partNumber = product.partNumber + " (" + Helpers.Numbers.convertToCurrency(todaySubtotal) + ")";
                    }
                }
                if (yesterdaySubtotal > 0) {
                    if (product.isHeader && product.partNumber.equals("Yesterday")) {
                        product.partNumber = product.partNumber + " (" + Helpers.Numbers.convertToCurrency(yesterdaySubtotal) + ")";
                    }
                }
                if (thisWeekSubtotal > 0) {
                    if (product.isHeader && product.partNumber.equals("This week")) {
                        product.partNumber = product.partNumber + " (" + Helpers.Numbers.convertToCurrency(thisWeekSubtotal) + ")";
                    }
                }
                if (thisMonthSubtotal > 0) {
                    if (product.isHeader && product.partNumber.equals("This month")) {
                        product.partNumber = product.partNumber + " (" + Helpers.Numbers.convertToCurrency(thisMonthSubtotal) + ")";
                    }
                }
                if (lastMonthSubtotal > 0) {
                    if (product.isHeader && product.partNumber.equals("Last month")) {
                        product.partNumber = product.partNumber + " (" + Helpers.Numbers.convertToCurrency(lastMonthSubtotal) + ")";
                    }
                }
                if (otherSubtotal > 0) {
                    if (product.isHeader && product.partNumber.equals("Total")) {
                        product.partNumber = product.partNumber + " (" + Helpers.Numbers.convertToCurrency(otherSubtotal) + ")";
                    }
                }
            }

            Log.i(TAG, "populateTripList Finished preparing the dividers and trips.");

            if (!getActivity().isFinishing()) {
                adapter = new OrderLineRecyclerAdapter(getContext(), orderList);
                adapter.setOnRowClickListener(new OrderLineRecyclerAdapter.RowClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        /*OrderProduct clickedProduct = adapter.mData.get(position);
                        Toast.makeText(getContext(), clickedProduct.salesorderidFormatted
                                + " placed on " + Helpers.DatesAndTimes
                                .getPrettyDate(clickedProduct.orderDate), Toast.LENGTH_SHORT)
                                .show();*/
                        Log.i(TAG, "onLinkButtonClick ");
                        CrmEntities.Accounts.Account selectedAccount = new CrmEntities.Accounts.Account(
                                adapter.mData.get(position).customerid, adapter.mData.get(position).customeridFormatted);
                        Intent intent = new Intent(context, Activity_AccountData.class);
                        intent.setAction(Activity_AccountData.GO_TO_ACCOUNT);
                        intent.putExtra(Activity_AccountData.GO_TO_ACCOUNT_OBJECT, selectedAccount);
                        intent.putExtra(Activity_AccountData.INITIAL_PAGE, Activity_AccountData.SectionsPagerAdapter.SALES_LINE_PAGE);
                        startActivity(intent);
                    }
                });
                adapter.setOnLinkButtonClickListener(new OrderLineRecyclerAdapter.OnLinkButtonClickListener() {
                    @Override
                    public void onLinkButtonClick(View view, int position) {
                        Log.i(TAG, "onLinkButtonClick ");
                        CrmEntities.Accounts.Account selectedAccount = new CrmEntities.Accounts.Account(
                                adapter.mData.get(position).customerid, adapter.mData.get(position).customeridFormatted);
                        Intent intent = new Intent(context, Activity_AccountData.class);
                        intent.setAction(Activity_AccountData.GO_TO_ACCOUNT);
                        intent.putExtra(Activity_AccountData.GO_TO_ACCOUNT_OBJECT, selectedAccount);
                        intent.putExtra(Activity_AccountData.INITIAL_PAGE, Activity_AccountData.SectionsPagerAdapter.SALES_LINE_PAGE);
                        startActivity(intent);
                    }
                });
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(adapter);

                refreshLayout.finishRefresh();
            } else {
                Log.w(TAG, "populateList: CAN'T POPULATE AS THE ACTIVITY IS FINISHING!!!");
            }

            txtNoSales.setVisibility( (allOrders == null || allOrders.size() == 0) ? View.VISIBLE : View.GONE);
        }
    }

    public static class Frag_Leads extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View rootView;
        public RecyclerView listview;
        RefreshLayout refreshLayout;
        TextView txtNoLeads;
        public ArrayList<Territory> cachedTerritories;
        public Territory curTerritory;
        public CrmEntities.Leads leads;
        ArrayList<BasicObjects.BasicObject> objects = new ArrayList<>();
        BasicObjectRecyclerAdapter adapter;
        BroadcastReceiver menuSelectionReceiver;
        BroadcastReceiver territoryChangedReceiver;
        FloatingActionButton fab;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.frag_leads, container, false);
            listview = rootView.findViewById(R.id.leadsRecyclerview);
            refreshLayout = rootView.findViewById(R.id.refreshLayout);
            txtNoLeads = rootView.findViewById(R.id.txtNoLeads);
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                    getLeads();
                }
            });

            refreshLayout.setEnableLoadMore(false);
            super.onCreateView(inflater, container, savedInstanceState);

            fab = rootView.findViewById(R.id.floatingActionButton);
            fab.setVisibility(View.VISIBLE); // Nothing to filter (yet) so hide.
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(FAB_CLICKED);
                    getActivity().sendBroadcast(intent);
                }
            });

            // Gifts from Santa - open present and do stuff with what we got!
            menuSelectionReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Opportunities frag received a broadcast!");

                    // User made an options menu selection - receive the details of that choice
                    // via the broadcast sent by the parent activity.
                    if (intent.getAction().equals(MENU_SELECTION)) {
                        if (intent.getParcelableExtra(FullscreenActivityChooseTerritory.TERRITORY_RESULT) != null) {
                            curTerritory = intent.getParcelableExtra(FullscreenActivityChooseTerritory.TERRITORY_RESULT);
                            getLeads();
                            sectionsPagerAdapter.notifyDataSetChanged();
                        }
                    }
                }
            };

            if (curTerritory == null) {
                curTerritory = MediUser.getMe().getTerritory();
            }

            if (leads == null && curTerritory != null && curTerritory != globalTerritory) {
                curTerritory = globalTerritory;
                getLeads();
            } else {
                populateList();
            }

            territoryChangedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    curTerritory = globalTerritory;
                    getLeads();
                }
            };

            return rootView;
        }

        @Override
        public void onPause() {
            super.onPause();

            Log.i(TAG, "onResume Unregistered opportunities receiver");
        }

        @Override
        public void onDestroyView() {
            getActivity().unregisterReceiver(menuSelectionReceiver);
            getActivity().unregisterReceiver(territoryChangedReceiver);
            super.onDestroyView();
        }

        @Override
        public void onResume() {
            super.onResume();
            IntentFilter intentFilterMenuSelection = new IntentFilter(MENU_SELECTION);
            getActivity().registerReceiver(menuSelectionReceiver, intentFilterMenuSelection);
            getActivity().registerReceiver(territoryChangedReceiver, new IntentFilter(MENU_SELECTION));
            Log.i(TAG, "onResume Registered opportunities receiver");
        }

        public void getLeads() {
            refreshLayout.autoRefreshAnimationOnly();
            toolbar.setTitle(sectionsPagerAdapter.getPageTitle(mViewPager.currentPosition));
            CrmEntities.Leads.getTerritoryLeads(context, leadFilter, curTerritory.territoryid, new MyInterfaces.GetLeadsListener() {
                @Override
                public void onSuccess(CrmEntities.Leads crmLeads) {
                    Log.i(TAG, "onSuccess ");
                    refreshLayout.finishRefresh();
                    leads = crmLeads;
                    objects = new ArrayList<>();
                    for (CrmEntities.Leads.Lead lead : leads.list) {
                        objects.add(lead.toBasicObject());
                    }
                    populateList();
                }

                @Override
                public void onFailure(String error) {
                    refreshLayout.finishRefresh();
                    Toast.makeText(context, "Failed to get leads!\n" + error, Toast.LENGTH_LONG).show();
                }
            });
        }

        void populateList() {
            adapter = new BasicObjectRecyclerAdapter(context, objects);
            listview.setLayoutManager(new LinearLayoutManager(context));
            listview.setAdapter(adapter);
            adapter.setClickListener(new BasicObjectRecyclerAdapter.ItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    CrmEntities.Leads.Lead selectedLead =
                            (CrmEntities.Leads.Lead) objects.get(position).object;

                    ContactActions actions = new ContactActions(activity, selectedLead);
                    actions.showContactOptions();

                    /*Intent intent = new Intent(context, BasicEntityActivity.class);
                    intent.putExtra(BasicEntityActivity.ACTIVITY_TITLE, "Lead Details");
                    intent.putExtra(BasicEntityActivity.ENTITYID, selectedLead.leadid);
                    intent.putExtra(BasicEntityActivity.ENTITY_LOGICAL_NAME, "lead");
                    intent.putExtra(BasicEntityActivity.GSON_STRING, selectedLead.toBasicEntity().toGson());
                    startActivityForResult(intent, BasicEntityActivity.REQUEST_BASIC);*/
                }
            });

            txtNoLeads.setVisibility( (leads == null || leads.list.size() == 0) ? View.VISIBLE : View.GONE );

        }

        void showOpportunityOptions(final Opportunities.Opportunity opportunity) {

            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog_opportunity_options);

            // Fields
            TextView txtAccount;
            TextView txtTopic;
            TextView txtStatus;
            TextView txtDealStatus;
            TextView txtDealType;
            TextView txtCloseProb;
            TextView txtBackground;

            txtAccount = dialog.findViewById(R.id.textView_OppAccount);
            txtTopic = dialog.findViewById(R.id.textView_OppTopic);
            txtStatus = dialog.findViewById(R.id.textView_OppStatus);
            txtDealStatus = dialog.findViewById(R.id.textView_OppDealStatus);
            txtDealType = dialog.findViewById(R.id.textView_OppDealType);
            txtCloseProb = dialog.findViewById(R.id.textView_OppCloseProb);
            txtBackground = dialog.findViewById(R.id.textView_OppBackground);

            txtAccount.setText(opportunity.accountname);
            txtTopic.setText(opportunity.name);
            txtStatus.setText(opportunity.statuscodeFormatted);
            txtDealStatus.setText(opportunity.statuscodeFormatted);
            txtDealType.setText(opportunity.dealTypePretty);
            txtCloseProb.setText(opportunity.probabilityPretty);

            String bgTruncated = "";
            if (opportunity.currentSituation != null && opportunity.currentSituation.length() > 125) {
                bgTruncated = opportunity.currentSituation.substring(0, 125) + "...\n";
            } else {
                bgTruncated = opportunity.currentSituation;
            }

            txtBackground.setText(bgTruncated);

            Button btnQuickNote;
            btnQuickNote = dialog.findViewById(R.id.btn_add_quick_note);
            btnQuickNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    showAddNoteDialog(opportunity);
                }
            });

            Button btnViewOpportunity;
            btnViewOpportunity = dialog.findViewById(R.id.btn_view_opportunity);
            btnViewOpportunity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, OpportunityActivity.class);
                    intent.putExtra(OpportunityActivity.OPPORTUNITY_TAG, opportunity);
                    startActivity(intent);
                    dialog.dismiss();
                }
            });

            dialog.setCancelable(true);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();
        }

        void showAddNoteDialog(final Opportunities.Opportunity opportunity) {
            CrmEntities.Annotations.showAddNoteDialog(context, opportunity.entityid, new MyInterfaces.CrmRequestListener() {
                @Override
                public void onComplete(Object result) {
                    Log.i(TAG, "onComplete ");
                    final Helpers.Notifications notifications = new Helpers.Notifications(context);
                    notifications.create("Opportunity note created",
                            "Your note was added to the opportunity!", false);
                    notifications.show();
                    notifications.setAutoCancel(6000);
                }

                @Override
                public void onProgress(Crm.AsyncProgress progress) {
                    Log.i(TAG, "onProgress ");
                }

                @Override
                public void onFail(String error) {
                    Log.i(TAG, "onFail ");
                }
            });

        }

    }

    public static class Frag_Opportunities extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View rootView;
        public RecyclerView listview;
        RefreshLayout refreshLayout;
        TextView txtNoOpportunities;
        public Territory curTerritory;
        public Opportunities opportunities;
        ArrayList<BasicObjects.BasicObject> objects = new ArrayList<>();
        BasicObjectRecyclerAdapter adapter;
        BroadcastReceiver menuReceiver;
        FloatingActionButton fab;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.frag_opportunities, container, false);
            listview = rootView.findViewById(R.id.opportunitiesRecyclerview);
            refreshLayout = rootView.findViewById(R.id.refreshLayout);
            txtNoOpportunities = rootView.findViewById(R.id.txtNoOpportunities);
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                    getOpportunities();
                }
            });
            refreshLayout.setEnableLoadMore(false);

            // Gifts from Santa - open present and do stuff with what we got!
            menuReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Opportunities frag received a broadcast!");

                    // User made an options menu selection - receive the details of that choice
                    // via the broadcast sent by the parent activity.
                    if (intent.getAction().equals(MENU_SELECTION)) {
                        if (intent.getParcelableExtra(FullscreenActivityChooseTerritory.TERRITORY_RESULT) != null) {
                            curTerritory = intent.getParcelableExtra(FullscreenActivityChooseTerritory.TERRITORY_RESULT);
                            getOpportunities();
                        }
                        getOpportunities();
                        sectionsPagerAdapter.notifyDataSetChanged();
                    }
                }
            };

            if (curTerritory == null) {
                curTerritory = MediUser.getMe().getTerritory();
            }

            if (globalTerritory != curTerritory) {
                curTerritory = globalTerritory;
                getOpportunities();
            }  else {
                if (opportunities != null) {
                    populateList();
                }
            }

            super.onCreateView(inflater, container, savedInstanceState);

            fab = rootView.findViewById(R.id.floatingActionButton);
            fab.setVisibility(View.VISIBLE); // Nothing to filter (yet) so hide.
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(FAB_CLICKED);
                    getActivity().sendBroadcast(intent);
                }
            });

            return rootView;
        }

        @Override
        public void onPause() {
            super.onPause();

            Log.i(TAG, "onResume Unregistered opportunities receiver");
        }

        @Override
        public void onDestroyView() {
            getActivity().unregisterReceiver(menuReceiver);
            super.onDestroyView();
        }

        @Override
        public void onResume() {
            super.onResume();
            IntentFilter intentFilterMenuSelection = new IntentFilter(MENU_SELECTION);
            getActivity().registerReceiver(menuReceiver, intentFilterMenuSelection);
            Log.i(TAG, "onResume Registered opportunities receiver");
        }

        public void getOpportunities() {
            toolbar.setTitle(sectionsPagerAdapter.getPageTitle(mViewPager.currentPosition));
            refreshLayout.autoRefresh();
            Opportunities.retrieveOpportunities(dealStatus, curTerritory.territoryid, new MyInterfaces.GetOpportunitiesListener() {
                @Override
                public void onSuccess(Opportunities crmOpportunities) {
                    opportunities = crmOpportunities;
                    populateList();
                    refreshLayout.finishRefresh();
                }

                @Override
                public void onFailure(String error) {
                    refreshLayout.finishRefresh();
                    Toast.makeText(context, "Failed to get opportunities!\n" + error, Toast.LENGTH_LONG).show();
                }
            });
        }

        void populateList() {
            objects = new ArrayList<>();
            for (Opportunities.Opportunity opp : opportunities.list) {
                BasicObjects.BasicObject object = new BasicObjects.BasicObject(opp.name, opp.dealTypePretty, opp);
                object.middleText = opp.accountname;
                object.topRightText = opp.probabilityPretty;
                object.iconResource = R.drawable.opportunity1;
                objects.add(object);
            }

            adapter = new BasicObjectRecyclerAdapter(context, objects);
            listview.setLayoutManager(new LinearLayoutManager(context));
            listview.setAdapter(adapter);
            adapter.setClickListener(new BasicObjectRecyclerAdapter.ItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Opportunities.Opportunity selectedOpportunity =
                            (Opportunities.Opportunity) objects.get(position).object;

                    Intent intent = new Intent(context, BasicEntityActivity.class);
                    intent.putExtra(BasicEntityActivity.ACTIVITY_TITLE, "Opportunity Details");
                    intent.putExtra(BasicEntityActivity.ENTITYID, selectedOpportunity.entityid);
                    intent.putExtra(BasicEntityActivity.ENTITY_LOGICAL_NAME, "opportunity");
                    intent.putExtra(BasicEntityActivity.GSON_STRING, selectedOpportunity.toBasicEntity().toGson());
                    startActivityForResult(intent, BasicEntityActivity.REQUEST_BASIC);

                    try {
                        MileBuddyMetrics.updateMetric(context, MileBuddyMetrics.MetricName.LAST_OPENED_OPPORTUNITY, DateTime.now());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            txtNoOpportunities.setVisibility( (opportunities == null || opportunities.list.size() == 0) ? View.VISIBLE : View.GONE );

        }

        void showOpportunityOptions(final Opportunities.Opportunity opportunity) {

            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog_opportunity_options);

            // Fields
            TextView txtAccount;
            TextView txtTopic;
            TextView txtStatus;
            TextView txtDealStatus;
            TextView txtDealType;
            TextView txtCloseProb;
            TextView txtBackground;

            txtAccount = dialog.findViewById(R.id.textView_OppAccount);
            txtTopic = dialog.findViewById(R.id.textView_OppTopic);
            txtStatus = dialog.findViewById(R.id.textView_OppStatus);
            txtDealStatus = dialog.findViewById(R.id.textView_OppDealStatus);
            txtDealType = dialog.findViewById(R.id.textView_OppDealType);
            txtCloseProb = dialog.findViewById(R.id.textView_OppCloseProb);
            txtBackground = dialog.findViewById(R.id.textView_OppBackground);

            txtAccount.setText(opportunity.accountname);
            txtTopic.setText(opportunity.name);
            txtStatus.setText(opportunity.statuscodeFormatted);
            txtDealStatus.setText(opportunity.statuscodeFormatted);
            txtDealType.setText(opportunity.dealTypePretty);
            txtCloseProb.setText(opportunity.probabilityPretty);

            String bgTruncated = "";
            if (opportunity.currentSituation != null && opportunity.currentSituation.length() > 125) {
                bgTruncated = opportunity.currentSituation.substring(0, 125) + "...\n";
            } else {
                bgTruncated = opportunity.currentSituation;
            }

            txtBackground.setText(bgTruncated);

            Button btnQuickNote;
            btnQuickNote = dialog.findViewById(R.id.btn_add_quick_note);
            btnQuickNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    showAddNoteDialog(opportunity);
                }
            });

            Button btnViewOpportunity;
            btnViewOpportunity = dialog.findViewById(R.id.btn_view_opportunity);
            btnViewOpportunity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, OpportunityActivity.class);
                    intent.putExtra(OpportunityActivity.OPPORTUNITY_TAG, opportunity);
                    startActivity(intent);
                    dialog.dismiss();
                }
            });

            dialog.setCancelable(true);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();
        }

        void showAddNoteDialog(final Opportunities.Opportunity opportunity) {
            CrmEntities.Annotations.showAddNoteDialog(context, opportunity.entityid, new MyInterfaces.CrmRequestListener() {
                @Override
                public void onComplete(Object result) {
                    Log.i(TAG, "onComplete ");
                    final Helpers.Notifications notifications = new Helpers.Notifications(context);
                    notifications.create("Opportunity note created",
                            "Your note was added to the opportunity!", false);
                    notifications.show();
                    notifications.setAutoCancel(6000);
                }

                @Override
                public void onProgress(Crm.AsyncProgress progress) {
                    Log.i(TAG, "onProgress ");
                }

                @Override
                public void onFail(String error) {
                    Log.i(TAG, "onFail ");
                }
            });

        }

    }

    public static class Frag_Cases extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View rootView;
        public RecyclerView listview;
        RefreshLayout refreshLayout;
        BroadcastReceiver casesReceiver;
        Territory curTerritory;
        Tickets tickets;
        ArrayList<BasicObjects.BasicObject> objects = new ArrayList<>();
        BasicObjectRecyclerAdapter adapter;
        BroadcastReceiver territoryChangeReceiver;
        TextView txtNoTickets;
        FloatingActionButton fab;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.frag_cases, container, false);
            listview = rootView.findViewById(R.id.casesRecyclerview);
            refreshLayout = rootView.findViewById(R.id.refreshLayout);
            refreshLayout.setEnableLoadMore(false);
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                    getTickets();
                }
            });
            super.onCreateView(inflater, container, savedInstanceState);

            fab = rootView.findViewById(R.id.floatingActionButton);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(FAB_CLICKED);
                    getActivity().sendBroadcast(intent);
                }
            });

            casesReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Received month and year broadcast! (cases frag)");
                    mViewPager.getAdapter().notifyDataSetChanged();
                    getTickets();
                    sectionsPagerAdapter.notifyDataSetChanged();
                }
            };

            if (curTerritory == null) {
                curTerritory = globalTerritory;
            }

            if (objects == null || objects.size() == 0 || curTerritory != globalTerritory) {
                curTerritory = globalTerritory;
                getTickets();
            } else {
                populateList();
            }

            territoryChangeReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getParcelableExtra(FullscreenActivityChooseTerritory.TERRITORY_RESULT) != null) {
                        curTerritory = globalTerritory;
                        getTickets();
                    }
                }
            };

            txtNoTickets = rootView.findViewById(R.id.txtNoTickets);

            return rootView;
        }

        void getTickets() {
            toolbar.setTitle(sectionsPagerAdapter.getPageTitle(mViewPager.currentPosition));
            refreshLayout.autoRefreshAnimationOnly();
            String query = null;

            curTerritory = globalTerritory;

            query = CrmQueries.Tickets.getIncidents(globalTerritory.territoryid, case_status, case_state, 4);

            ArrayList<Requests.Argument> args = new ArrayList<>();
            args.add(new Requests.Argument("query", query));
            Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);

            Crm crm = new Crm();
            crm.makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody);
                    Log.i(TAG, "onSuccess " + response);
                    tickets = new Tickets(response);
                    populateList();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    refreshLayout.finishRefresh();
                }
            });
        }

        void populateList() {

            objects.clear();

            if (tickets != null) {
                objects = tickets.toBasicObjects();
                adapter = new BasicObjectRecyclerAdapter(context, objects);
                listview.setLayoutManager(new LinearLayoutManager(context));
                listview.setAdapter(adapter);
                adapter.setClickListener(new BasicObjectRecyclerAdapter.ItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        BasicObjects.BasicObject object = objects.get(position);
                        Tickets.Ticket ticket = (Tickets.Ticket) object.object;
                        Intent intent = new Intent(context, BasicEntityActivity.class);
                        intent.putExtra(BasicEntityActivity.GSON_STRING, ticket.toBasicEntity().toGson());
                        intent.putExtra(BasicEntityActivity.ENTITYID, ticket.entityid);
                        intent.putExtra(BasicEntityActivity.CURRENT_TERRITORY, globalTerritory);
                        intent.putExtra(BasicEntityActivity.ENTITY_LOGICAL_NAME, "incident");
                        intent.putExtra(BasicEntityActivity.ACTIVITY_TITLE, "Ticket " + ticket.ticketnumber);
                        startActivityForResult(intent, REQUEST_BASIC);

                        try {
                            MileBuddyMetrics.updateMetric(context, MileBuddyMetrics.MetricName.LAST_OPENED_TICKET, DateTime.now());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                txtNoTickets.setVisibility(objects.size() == 0 ? View.VISIBLE : View.GONE);

                try {
                    refreshLayout.finishRefresh();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            IntentFilter filter = new IntentFilter(MENU_SELECTION);
            filter.addAction(MENU_SELECTION);
            getActivity().registerReceiver(casesReceiver, filter);
            getActivity().registerReceiver(territoryChangeReceiver, new IntentFilter(MENU_SELECTION));
            Log.i(TAG, "onResume Registered the cases receiver");
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            getActivity().unregisterReceiver(casesReceiver);
            getActivity().unregisterReceiver(territoryChangeReceiver);
            Log.i(TAG, "onPause Unregistered the cases receiver");
        }
    }

    public static class Frag_Accounts extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View rootView;
        public RecyclerView listview;
        RefreshLayout refreshLayout;
        BroadcastReceiver accountsReceiver;
        Territory curTerritory;
        CrmEntities.Accounts accounts;
        ArrayList<BasicObjects.BasicObject> objects = new ArrayList<>();
        BasicObjectRecyclerAdapter adapter;
        BroadcastReceiver territoryChangedReceiver;
        TextView txtNoAccounts;
        FloatingActionButton fab;
        RelativeLayout layoutFilter;
        EditText txtFilter;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.frag_accounts, container, false);
            listview = rootView.findViewById(R.id.casesRecyclerview);
            refreshLayout = rootView.findViewById(R.id.refreshLayout);
            refreshLayout.setEnableLoadMore(false);
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                    getAccounts();
                }
            });
            super.onCreateView(inflater, container, savedInstanceState);

            fab = rootView.findViewById(R.id.floatingActionButton);
            fab.setVisibility(View.GONE); // Nothing to filter (yet) so hide.
            /*fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(FAB_CLICKED);
                    getActivity().sendBroadcast(intent);
                }
            });*/


            txtNoAccounts = rootView.findViewById(R.id.txtNoAgreements);
            layoutFilter = rootView.findViewById(R.id.layoutFilter);

            accountsReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Received month and year broadcast! (cases frag)");
                    mViewPager.getAdapter().notifyDataSetChanged();
                    getAccounts();
                }
            };

            txtFilter = rootView.findViewById(R.id.edittextFilter);
            txtFilter.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    // NPE if we don't have a populated adapter!
                    if (adapter == null || adapter.mData == null) {
                        Log.w(TAG, "onTextChanged: No data in adapter - cannot filter!");
                        return;
                    }

                    String filter;

                    // Clear the adapter's data
                    adapter.mData.clear();

                    // Filter text present
                    if (charSequence != null && charSequence.length() > 0) {
                        filter = charSequence.toString().toLowerCase();

                        for (CrmEntities.Accounts.Account account : accounts.list) {
                            if (account.accountName.toLowerCase().contains(filter) || account.accountnumber.contains(filter)) {
                                BasicObjects.BasicObject object = new BasicObjects.BasicObject(account.accountnumber, account.customerTypeFormatted, account);
                                object.middleText = account.accountName;
                                object.iconResource = R.drawable.maps_hospital_32x37;
                                adapter.mData.add(object);
                            }
                        }
                    } else { // No filter text so add everything
                        for (CrmEntities.Accounts.Account account : accounts.list) {
                            BasicObjects.BasicObject object = new BasicObjects.BasicObject(account.accountnumber, account.customerTypeFormatted, account);
                            object.middleText = account.accountName;
                            object.iconResource = R.drawable.maps_hospital_32x37;
                            adapter.mData.add(object);
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    // Notify the list that the data has changed
                    if (adapter != null && adapter.mData != null) {
                        adapter.notifyDataSetChanged();
                    }
                }
            });

            territoryChangedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getParcelableExtra(FullscreenActivityChooseTerritory.TERRITORY_RESULT) != null) {
                        curTerritory = globalTerritory;
                    }
                }
            };

            // Reminder, don't use cached accounts (options.getCachedAccounts()) for territory-specific shit!

            if (objects == null || objects.size() == 0 || curTerritory != globalTerritory) {
                getAccounts();
            } else {
                populateList();
            }

            return rootView;
        }

        void getAccounts() {
            refreshLayout.autoRefreshAnimationOnly();
            try {
                layoutFilter.setEnabled(false);
                txtFilter.setEnabled(false);
                txtFilter.setText("");
            } catch (Exception e) {
                e.printStackTrace();
            }

            String query = CrmQueries.Accounts.getAccounts(globalTerritory.territoryid);

            ArrayList<Requests.Argument> args = new ArrayList<>();
            args.add(new Requests.Argument("query", query));
            Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);

            toolbar.setTitle(sectionsPagerAdapter.getPageTitle(mViewPager.currentPosition));
            Crm crm = new Crm();
            crm.makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody);
                    Log.i(TAG, "onSuccess " + response);
                    accounts = new CrmEntities.Accounts(response);
                    populateList();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    refreshLayout.finishRefresh();
                }
            });
        }

        void populateList() {
            populateList(null);
        }

        void populateList(String filter) {

            objects.clear();

            if (accounts != null) {

                int lastStatusCode = -1;

                for (CrmEntities.Accounts.Account account : accounts.list) {

                    // Add the ticket as a BasicObject
                    BasicObjects.BasicObject object = new BasicObjects.BasicObject(account.accountnumber, account.customerTypeFormatted, account);
                    object.middleText = account.accountName;
                    object.iconResource = R.drawable.customer2;
                    objects.add(object);
                }
            }

            adapter = new BasicObjectRecyclerAdapter(context, objects);
            listview.setLayoutManager(new LinearLayoutManager(context));
            listview.setAdapter(adapter);
            adapter.setClickListener(new BasicObjectRecyclerAdapter.ItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    BasicObjects.BasicObject object = objects.get(position);
                    CrmEntities.Accounts.Account account = (CrmEntities.Accounts.Account) object.object;

                    Intent intent = new Intent(context, Activity_AccountData.class);
                    intent.setAction(Activity_AccountData.GO_TO_ACCOUNT);
                    intent.putExtra(Activity_AccountData.GO_TO_ACCOUNT_OBJECT, account);
                    startActivity(intent);
                }
            });

            txtNoAccounts.setVisibility(objects.size() == 0 ? View.VISIBLE : View.GONE);

            try {
                refreshLayout.finishRefresh();
                layoutFilter.setEnabled(true);
                txtFilter.setEnabled(true);
                if (mViewPager.getCurrentItem() == SectionsPagerAdapter.ACCOUNTS_PAGE) {
                    Helpers.Application.showKeyboard(txtFilter, context);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onResume() {
            super.onResume();
            IntentFilter filter = new IntentFilter(MENU_SELECTION);
            filter.addAction(MENU_SELECTION);
            getActivity().registerReceiver(accountsReceiver, filter);
            getActivity().registerReceiver(territoryChangedReceiver, new IntentFilter(MENU_SELECTION));
            Log.i(TAG, "onResume Registered the cases receiver");
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            getActivity().unregisterReceiver(accountsReceiver);
            getActivity().unregisterReceiver(territoryChangedReceiver);
            Log.i(TAG, "onPause Unregistered the cases receiver");
        }
    }

    public static class Frag_ServiceAgreements extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View rootView;
        public RecyclerView listview;
        RefreshLayout refreshLayout;
        BroadcastReceiver agreementFilterReceiver;
        Territory curTerritory;
        CrmEntities.ServiceAgreements serviceAgreements;
        ArrayList<BasicObjects.BasicObject> objects = new ArrayList<>();
        BasicObjectRecyclerAdapter adapter;
        BroadcastReceiver territoryChangedReceiver;
        TextView txtNoAgreements;
        FloatingActionButton fab;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.frag_service_agreements, container, false);
            listview = rootView.findViewById(R.id.casesRecyclerview);
            refreshLayout = rootView.findViewById(R.id.refreshLayout);
            refreshLayout.setEnableLoadMore(false);
            fab = rootView.findViewById(R.id.floatingActionButton);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(FAB_CLICKED);
                    getActivity().sendBroadcast(intent);
                }
            });
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                    getServiceAgreements();
                }
            });
            super.onCreateView(inflater, container, savedInstanceState);

            agreementFilterReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Received an agreements filter broadcast! (service agreements frag)");
                    mViewPager.getAdapter().notifyDataSetChanged();
                    getServiceAgreements();
                    sectionsPagerAdapter.notifyDataSetChanged();
                }
            };

            territoryChangedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getParcelableExtra(FullscreenActivityChooseTerritory.TERRITORY_RESULT) != null) {
                        curTerritory = globalTerritory;
                    }
                }
            };

            if (objects == null || objects.size() == 0 || curTerritory != globalTerritory) {
                getServiceAgreements();
            } else {
                populateList();
            }

            txtNoAgreements = rootView.findViewById(R.id.txtNoAgreements);
            return rootView;
        }

        void getServiceAgreements() {

            refreshLayout.autoRefreshAnimationOnly();

            String query = "";
            toolbar.setTitle(sectionsPagerAdapter.getPageTitle(mViewPager.currentPosition));

            switch (serviceAgreementFilter) {
                case CURRENT:
                    query = CrmQueries.Accounts.getActiveServiceAgreementsByTerritory(globalTerritory.territoryid);
                    break;
                case EXPIRED:
                    query = CrmQueries.Accounts.getExpiredServiceAgreementsByTerritory(globalTerritory.territoryid);
                    break;
                case EXPIRING:
                    query = CrmQueries.Accounts.getExpiringServiceAgreementsByTerritory(globalTerritory.territoryid);
                    break;
            }

            ArrayList<Requests.Argument> args = new ArrayList<>();
            args.add(new Requests.Argument("query", query));
            Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);

            Crm crm = new Crm();
            crm.makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody);
                    Log.i(TAG, "onSuccess " + response);
                    serviceAgreements = new CrmEntities.ServiceAgreements(response);
                    populateList();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    refreshLayout.finishRefresh();
                }
            });
        }

        void populateList() {

            objects.clear();

            if (serviceAgreements != null) {

                int lastStatusCode = -1;

                for (CrmEntities.ServiceAgreements.ServiceAgreement agreement : serviceAgreements.list) {

                    // Add the ticket as a BasicObject
                    BasicObjects.BasicObject object = new BasicObjects.BasicObject(agreement._msus_customer_valueFormattedValue,
                            agreement._msus_product_valueFormattedValue + "\ns/n: " + agreement.msus_serialnumber, agreement);
                    object.middleText = "Expires: " + agreement.getPrettyEnddate();
                    object.iconResource = R.drawable.contract32;
                    objects.add(object);
                }
            }

            adapter = new BasicObjectRecyclerAdapter(context, objects);
            listview.setLayoutManager(new LinearLayoutManager(context));
            listview.setAdapter(adapter);
            adapter.setClickListener(new BasicObjectRecyclerAdapter.ItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    BasicObjects.BasicObject object = objects.get(position);
                    CrmEntities.ServiceAgreements.ServiceAgreement agreement = (CrmEntities.ServiceAgreements.ServiceAgreement) object.object;
                    Intent intent = new Intent(context, BasicEntityActivity.class);
                    intent.putExtra(BasicEntityActivity.GSON_STRING, agreement.toBasicEntity().toGson());
                    intent.putExtra(BasicEntityActivity.ENTITYID, agreement.entityid);
                    // intent.putExtra(BasicEntityActivity.HIDE_MENU, true);
                    intent.putExtra(BasicEntityActivity.CURRENT_TERRITORY, globalTerritory);
                    intent.putExtra(BasicEntityActivity.ENTITY_LOGICAL_NAME, "msus_medistimserviceagreement");
                    intent.putExtra(BasicEntityActivity.ACTIVITY_TITLE, "Service agreement " + agreement.msus_name);
                    startActivityForResult(intent, REQUEST_BASIC);
                }
            });

            txtNoAgreements.setVisibility(objects.size() == 0 ? View.VISIBLE : View.GONE);

            try {
                refreshLayout.finishRefresh();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onResume() {
            super.onResume();
            IntentFilter filter = new IntentFilter(MENU_SELECTION);
            filter.addAction(MENU_SELECTION);
            getActivity().registerReceiver(agreementFilterReceiver, filter);
            getActivity().registerReceiver(territoryChangedReceiver, new IntentFilter(MENU_SELECTION));
            Log.i(TAG, "onResume Registered the cases receiver");
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            getActivity().unregisterReceiver(agreementFilterReceiver);
            getActivity().unregisterReceiver(territoryChangedReceiver);
            Log.i(TAG, "onPause Unregistered the cases receiver");
        }
    }


    // endregion
}
