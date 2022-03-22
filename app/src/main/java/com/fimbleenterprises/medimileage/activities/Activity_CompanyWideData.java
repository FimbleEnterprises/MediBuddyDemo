package com.fimbleenterprises.medimileage.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.Crm;
import com.fimbleenterprises.medimileage.CrmQueries;
import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.MyApp;
import com.fimbleenterprises.medimileage.MyInterfaces;
import com.fimbleenterprises.medimileage.MyLinearSmoothScroller;
import com.fimbleenterprises.medimileage.MyPreferencesHelper;
import com.fimbleenterprises.medimileage.MyViewPager;
import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.activities.fullscreen_pickers.FullscreenActivityChooseTerritory;
import com.fimbleenterprises.medimileage.activities.ui.views.MyUnderlineEditText;
import com.fimbleenterprises.medimileage.adapters.BasicObjectRecyclerAdapter;
import com.fimbleenterprises.medimileage.adapters.LandingPageRecyclerAdapter;
import com.fimbleenterprises.medimileage.adapters.OrderLineRecyclerAdapter;
import com.fimbleenterprises.medimileage.dialogs.ContactActions;
import com.fimbleenterprises.medimileage.dialogs.MonthYearPickerDialog;
import com.fimbleenterprises.medimileage.objects_and_containers.AggregatedSales;
import com.fimbleenterprises.medimileage.objects_and_containers.BasicObjects;
import com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities;
import com.fimbleenterprises.medimileage.objects_and_containers.LandingPageItem;
import com.fimbleenterprises.medimileage.objects_and_containers.MediUser;
import com.fimbleenterprises.medimileage.objects_and_containers.MileBuddyMetrics;
import com.fimbleenterprises.medimileage.objects_and_containers.Opportunities;
import com.fimbleenterprises.medimileage.objects_and_containers.Requests;
import com.fimbleenterprises.medimileage.objects_and_containers.Territories.Territory;
import com.fimbleenterprises.medimileage.objects_and_containers.Tickets;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import org.jetbrains.annotations.NonNls;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

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

import static com.fimbleenterprises.medimileage.CrmQueries.Tickets.ANY;
import static com.fimbleenterprises.medimileage.CrmQueries.Tickets.CLOSED;
import static com.fimbleenterprises.medimileage.CrmQueries.Tickets.OPEN;
import static com.fimbleenterprises.medimileage.activities.BasicEntityActivity.ENTITYID;
import static com.fimbleenterprises.medimileage.activities.BasicEntityActivity.ENTITY_LOGICAL_NAME;
import static com.fimbleenterprises.medimileage.activities.BasicEntityActivity.REQUEST_BASIC;
import static com.fimbleenterprises.medimileage.activities.BasicEntityActivity.SEND_EMAIL;
import static com.fimbleenterprises.medimileage.activities.fullscreen_pickers.FullscreenActivityChooseTerritory.CACHED_TERRITORIES;
import static com.fimbleenterprises.medimileage.activities.fullscreen_pickers.FullscreenActivityChooseTerritory.CURRENT_TERRITORY;
import static com.fimbleenterprises.medimileage.activities.fullscreen_pickers.FullscreenActivityChooseTerritory.TERRITORY_RESULT;
import static com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities.OrderProducts;
import static com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities.OrderProducts.OrderProduct;

public class Activity_CompanyWideData extends AppCompatActivity {

    @NonNls
    public static final String VOLUME_CLICKED = "VOLUMN_CLICKED";
    @NonNls
    public static final String VOLUME_UP_CLICKED = "VOLUME_UP_CLICKED";
    @NonNls
    public static final String VOLUME_DOWN_CLICKED = "VOLUMN_DOWN_CLICKED";
    @NonNls
    private static final String FAB_CLICKED = "FAB_CLICKED";
    @NonNls
    private static final String OPPORTUNITY_LOGICAL_NAME = "opportunity";
    @NonNls
    private static final String INCIDENT_LOGICAL_NAME = "incident";

    public Activity activity;
    public EditText title;
    public MyUnderlineEditText date;
    public EditText distance;
    public static Marker fromMarker;
    public static Marker toMarker;
    public static Polyline polyline;
    public static LatLng fromLatLng;
    public static LatLng toLatLng;
    @SuppressLint("StaticFieldLeak")
    public static MyViewPager mViewPager;
    @SuppressLint("StaticFieldLeak")
    public static PagerTitleStrip mPagerStrip;
    @SuppressLint("StaticFieldLeak")
    public static Toolbar toolbar;
    public static SectionsPagerAdapter sectionsPagerAdapter;
    public static androidx.fragment.app.FragmentManager fragMgr;
    public static MyPreferencesHelper options;
    public ArrayList<Territory> cachedTerritories = new ArrayList<>();
    BroadcastReceiver fabClickReceiver;
    IntentFilter fabClickIntentFilter = new IntentFilter(FAB_CLICKED);
    SearchView searchView;
    public static CrmQueries.Opportunities.DealStatus dealStatus = CrmQueries.Opportunities.DealStatus.ANY;

    @NonNls
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

    public static CrmQueries.Leads.LeadFilter leadFilter = CrmQueries.Leads.LeadFilter.ANY;

    // vars for case status
    public static int case_status = ANY;
    public static int case_state = OPEN;

    // flag for region
    public static boolean isEastRegion = true;

    // var for territory shared with all fragments that want or need it
    public static Territory globalTerritory;

    // The popup dialog for goals represented by the chart.
    public static Dialog chartPopupDialog;

    public final static String TAG = "TerritoryData";

    @NonNls
    public static final String DATE_CHANGED = "DATE_CHANGED";
    @NonNls
    public static final String MONTH = "MONTH";
    @NonNls
    public static final String YEAR = "YEAR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        activity = this;
        options = new MyPreferencesHelper(getBaseContext());

        // Log a metric
        try {
            MileBuddyMetrics.updateMetric(this, MileBuddyMetrics.MetricName.LAST_ACCESSED_TERRITORY_DATA, DateTime.now());
        } catch (Exception e) {
            e.printStackTrace();
        }

        globalTerritory = new Territory();
        globalTerritory.territoryid = MediUser.getMe().territoryid;
        globalTerritory.territoryName = MediUser.getMe().territoryname;
        monthNum = DateTime.now().getMonthOfYear();
        yearNum = DateTime.now().getYear();

        setContentView(R.layout.activity_cpy_wide_sales_perf);

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.main_pager_yo_sales_perf);
        mViewPager.onRealPageChangedListener = pageIndex ->  {
            setTitle(sectionsPagerAdapter.getPageTitle(pageIndex));
            options.setLastCpyWidePage(pageIndex);
        };
        mPagerStrip = findViewById(R.id.pager_title_strip_sales_perf);
        mViewPager.setAdapter(sectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(0);
        mViewPager.setCurrentItem(0); // Set to landing page now that we have the landing page.
        mViewPager.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY)
                -> destroyChartDialogIfVisible());

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
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        fabClickReceiver = new BroadcastReceiver() {
            @SuppressLint("RestrictedApi")
            // Apparently a bug causes method: .openOptionsMenu() to raise a lint warning (https://stackoverflow.com/a/44926919/2097893).
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
            } else if (mViewPager.currentPosition == Activity_TerritoryData.SectionsPagerAdapter.LANDING_PAGE) {
                onBackPressed();
                return true;
            } else {
                mViewPager.setCurrentItem(Activity_TerritoryData.SectionsPagerAdapter.LANDING_PAGE);
            }
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && options.volumeButtonsCanScroll()) {
            Intent volDown = new Intent(VOLUME_CLICKED);
            volDown.putExtra(VOLUME_CLICKED, VOLUME_DOWN_CLICKED);
            sendBroadcast(volDown);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && options.volumeButtonsCanScroll()) {
            Intent volUp = new Intent(VOLUME_CLICKED);
            volUp.putExtra(VOLUME_CLICKED, VOLUME_UP_CLICKED);
            sendBroadcast(volUp);
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
                if (data != null && data.getAction() != null && data.getAction()
                        .equals(BasicEntityActivity.MENU_SELECTION)) {
                    Log.i(TAG, "onActivityResult " + MENU_SELECTION);

                    if (data.getStringExtra(SEND_EMAIL) != null) {

                        AtomicReference<String> recordUrl = new AtomicReference<>("");
                        AtomicReference<String> emailSuffix = new AtomicReference<>("");

                        Log.i(TAG, "onActivityResult Received a " + SEND_EMAIL + " result extra");
                        String entityid = data.getStringExtra(ENTITYID);
                        String entityLogicalName = data.getStringExtra(ENTITY_LOGICAL_NAME);
                        Log.i(TAG, "onActivityResult Entityid: " + entityid + " - Entity logical name: " + entityLogicalName);
                        Log.i(TAG, "onActivityResult ");

                        if (entityLogicalName != null && entityLogicalName.equals(OPPORTUNITY_LOGICAL_NAME)) {
                            recordUrl.set(Crm.getRecordUrl(entityid, Integer.toString(Crm.ETC_OPPORTUNITY)));
                            emailSuffix.set(getString(R.string.crm_link) + recordUrl);
                            Log.i(TAG, "onActivityResult:: " + recordUrl);

                        } else if (entityLogicalName != null && entityLogicalName.equals(INCIDENT_LOGICAL_NAME)) {
                            recordUrl.set(Crm.getRecordUrl(entityid, Integer.toString(Crm.ETC_INCIDENT)));
                            emailSuffix.set(getString(R.string.crm_link) + recordUrl);
                            Log.i(TAG, "onActivityResult:: " + recordUrl);
                        }

                        Helpers.Email.sendEmail(emailSuffix + "\n\n", getString(R.string
                                .crm_summary_email_subject_line), activity);

                    }

                }
            }

            if (data != null && data.getExtras() != null) {
                globalTerritory = data.getParcelableExtra(TERRITORY_RESULT);
                cachedTerritories = data.getParcelableArrayListExtra(CACHED_TERRITORIES);
            }
            sectionsPagerAdapter.notifyDataSetChanged();


            Intent intent = new Intent(this, Activity_TerritoryData.class);
            intent.putExtra(Activity_TerritoryData.REQUESTED_TERRITORY, globalTerritory);
            finishAndRemoveTask();
            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
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
        getMenuInflater().inflate(R.menu.cpy_wide_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new
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
            case Activity_TerritoryData.SectionsPagerAdapter.LANDING_PAGE: // Sales lines
                menu.findItem(R.id.action_west_region).setVisible(false);
                menu.findItem(R.id.action_east_region).setVisible(false);
                menu.findItem(R.id.action_this_year).setVisible(false);
                menu.findItem(R.id.action_last_year).setVisible(false);
                menu.findItem(R.id.action_case_state).setVisible(false);

                menu.findItem(R.id.action_this_month).setVisible(false);
                menu.findItem(R.id.action_last_month).setVisible(false);
                menu.findItem(R.id.action_choose_month).setVisible(false);
                // Need to have at least one item visible or the overflow menu button won't be generated.
                menu.findItem(R.id.action_choose_territory).setVisible(true);
                // menu.findItem(R.id.action_choose_territory).setEnabled(false);
                menu.findItem(R.id.action_lead_type).setVisible(false);
                menu.findItem(R.id.action_opportunity_status).setVisible(false);

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

                menu.findItem(R.id.action_case_status).setVisible(false);
                menu.findItem(R.id.action_case_state).setVisible(false);

                menu.findItem(R.id.action_account_type).setVisible(false);

                menu.findItem(R.id.action_expired_service_agreements).setVisible(false);
                menu.findItem(R.id.action_expiring_service_agreements).setVisible(false);
                menu.findItem(R.id.action_current_service_agreements).setVisible(false);

                menu.findItem(R.id.action_opportunity_status).setVisible(false);
                break;
            case SectionsPagerAdapter.LEADS_PAGE:
                menu.findItem(R.id.action_opportunity_status).setVisible(false);
                menu.findItem(R.id.action_case_status).setVisible(false);
                menu.findItem(R.id.action_account_type).setVisible(false);
                menu.findItem(R.id.action_east_region).setVisible(false);
                menu.findItem(R.id.action_west_region).setVisible(false);
                menu.findItem(R.id.action_lead_type).setVisible(true);
                menu.findItem(R.id.action_case_state).setVisible(false);

                switch (leadFilter) {
                    case ANY:
                        menu.findItem(R.id.action_lead_any).setChecked(true);
                        menu.findItem(R.id.action_lead_qualified).setChecked(false);
                        menu.findItem(R.id.action_lead_disqualified).setChecked(false);
                        menu.findItem(R.id.action_lead_created_last_three_months).setChecked(false);
                        break;
                    case QUALIFIED:
                        menu.findItem(R.id.action_lead_any).setChecked(false);
                        menu.findItem(R.id.action_lead_qualified).setChecked(true);
                        menu.findItem(R.id.action_lead_disqualified).setChecked(false);
                        menu.findItem(R.id.action_lead_created_last_three_months).setChecked(false);
                        break;
                    case DISQUALIFIED:
                        menu.findItem(R.id.action_lead_any).setChecked(false);
                        menu.findItem(R.id.action_lead_qualified).setChecked(false);
                        menu.findItem(R.id.action_lead_disqualified).setChecked(true);
                        menu.findItem(R.id.action_lead_created_last_three_months).setChecked(false);
                        break;
                    case LAST_THREE_MONTHS:
                        menu.findItem(R.id.action_lead_any).setChecked(false);
                        menu.findItem(R.id.action_lead_qualified).setChecked(false);
                        menu.findItem(R.id.action_lead_disqualified).setChecked(false);
                        menu.findItem(R.id.action_lead_created_last_three_months).setChecked(true);
                        break;

                }
                // Hide service agreements
                menu.findItem(R.id.action_expired_service_agreements).setVisible(false);
                menu.findItem(R.id.action_expiring_service_agreements).setVisible(false);
                menu.findItem(R.id.action_current_service_agreements).setVisible(false);
                // Hide date stuff
                menu.findItem(R.id.action_last_month).setVisible(false);
                menu.findItem(R.id.action_this_month).setVisible(false);
                menu.findItem(R.id.action_this_year).setVisible(false);
                menu.findItem(R.id.action_last_year).setVisible(false);
                menu.findItem(R.id.action_choose_month).setVisible(false);
                break;
            case SectionsPagerAdapter.OPPORTUNITIES_PAGE: // Opportunities
                menu.findItem(R.id.action_opportunity_status).setVisible(true);
                menu.findItem(R.id.action_case_state).setVisible(false);
                menu.findItem(R.id.action_case_status).setVisible(false);
                menu.findItem(R.id.action_account_type).setVisible(false);
                menu.findItem(R.id.action_east_region).setVisible(false);
                menu.findItem(R.id.action_west_region).setVisible(false);

                menu.findItem(R.id.action_lead_type).setVisible(false);
                // Check the appropriate opportunity filter
                menu.findItem(R.id.action_opp_any).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.ANY);
                menu.findItem(R.id.action_opp_canceled).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.CANCELED);
                menu.findItem(R.id.action_opp_closed).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.CLOSED);
                menu.findItem(R.id.action_opp_dead).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.DEAD);
                menu.findItem(R.id.action_opp_discovery).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.DISCOVERY);
                menu.findItem(R.id.action_opp_evaluating).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.EVALUATING);
                menu.findItem(R.id.action_opp_pending).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.PENDING);
                menu.findItem(R.id.action_opp_qualifying).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.QUALIFYING);
                menu.findItem(R.id.action_opp_stalled).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.STALLED);
                menu.findItem(R.id.action_opp_won).setChecked(dealStatus == CrmQueries.Opportunities.DealStatus.WON);
                // Hide service agreements
                menu.findItem(R.id.action_expired_service_agreements).setVisible(false);
                menu.findItem(R.id.action_expiring_service_agreements).setVisible(false);
                menu.findItem(R.id.action_current_service_agreements).setVisible(false);
                // Hide date stuff
                menu.findItem(R.id.action_last_month).setVisible(false);
                menu.findItem(R.id.action_this_month).setVisible(false);
                menu.findItem(R.id.action_this_year).setVisible(false);
                menu.findItem(R.id.action_last_year).setVisible(false);
                menu.findItem(R.id.action_choose_month).setVisible(false);
                break;

            case SectionsPagerAdapter.ACCOUNTS_PAGE: // Accounts
                menu.findItem(R.id.action_west_region).setVisible(false);
                menu.findItem(R.id.action_east_region).setVisible(false);
                menu.findItem(R.id.action_case_state).setVisible(false);
                menu.findItem(R.id.action_this_year).setVisible(false);
                menu.findItem(R.id.action_last_year).setVisible(false);

                menu.findItem(R.id.action_this_month).setVisible(false);
                menu.findItem(R.id.action_last_month).setVisible(false);
                menu.findItem(R.id.action_choose_month).setVisible(false);
                menu.findItem(R.id.action_choose_territory).setVisible(true);

                menu.findItem(R.id.action_lead_type).setVisible(false);

                menu.findItem(R.id.action_case_status).setVisible(false);

                menu.findItem(R.id.action_account_type).setVisible(false);

                menu.findItem(R.id.action_expired_service_agreements).setVisible(false);
                menu.findItem(R.id.action_expiring_service_agreements).setVisible(false);
                menu.findItem(R.id.action_current_service_agreements).setVisible(false);

                menu.findItem(R.id.action_opportunity_status).setVisible(false);
                break;

            case SectionsPagerAdapter.CASES_PAGE: // Cases
                menu.findItem(R.id.action_west_region).setVisible(false);
                menu.findItem(R.id.action_east_region).setVisible(false);
                menu.findItem(R.id.action_this_year).setVisible(false);
                menu.findItem(R.id.action_last_year).setVisible(false);
                menu.findItem(R.id.action_case_state).setVisible(true);

                menu.findItem(R.id.action_this_month).setVisible(false);
                menu.findItem(R.id.action_last_month).setVisible(false);
                menu.findItem(R.id.action_choose_month).setVisible(false);
                menu.findItem(R.id.action_choose_territory).setVisible(true);

                menu.findItem(R.id.action_lead_type).setVisible(false);

                menu.findItem(R.id.action_account_type).setVisible(false);

                menu.findItem(R.id.action_case_status).setVisible(true);

                menu.findItem(R.id.action_expired_service_agreements).setVisible(false);
                menu.findItem(R.id.action_expiring_service_agreements).setVisible(false);
                menu.findItem(R.id.action_current_service_agreements).setVisible(false);

                menu.findItem(R.id.action_opportunity_status).setVisible(false);
                break;

            case SectionsPagerAdapter.SERVICE_AGREEMENTS_PAGE:
                menu.findItem(R.id.action_case_state).setVisible(false);
                menu.findItem(R.id.action_choose_territory).setVisible(true);
                menu.findItem(R.id.action_west_region).setVisible(false);
                menu.findItem(R.id.action_east_region).setVisible(false);
                menu.findItem(R.id.action_this_year).setVisible(false);
                menu.findItem(R.id.action_last_year).setVisible(false);

                menu.findItem(R.id.action_this_month).setVisible(false);
                menu.findItem(R.id.action_last_month).setVisible(false);
                menu.findItem(R.id.action_choose_month).setVisible(false);

                menu.findItem(R.id.action_lead_type).setVisible(false);

                menu.findItem(R.id.action_account_type).setVisible(false);

                menu.findItem(R.id.action_case_status).setVisible(false);

                menu.findItem(R.id.action_expired_service_agreements).setVisible(true);
                menu.findItem(R.id.action_expiring_service_agreements).setVisible(true);
                menu.findItem(R.id.action_current_service_agreements).setVisible(true);

                menu.findItem(R.id.action_opportunity_status).setVisible(false);
                break;

        }

        // Set case state values
        switch (case_state) {
            case OPEN:
                menu.findItem(R.id.action_case_state_closed).setChecked(false);
                menu.findItem(R.id.action_case_state_open).setChecked(true);
                break;
            case CLOSED:
                menu.findItem(R.id.action_case_state_closed).setChecked(true);
                menu.findItem(R.id.action_case_state_open).setChecked(false);
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
                menu.findItem(R.id.action_change_case_status_waiting_on_customer).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_rep).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_billed).setChecked(false);
                break;
            case CrmQueries.Tickets.IN_PROGRESS:
                menu.findItem(R.id.action_change_case_status_any).setChecked(false);
                menu.findItem(R.id.action_change_case_status_inprogress).setChecked(true);
                menu.findItem(R.id.action_change_case_status_on_hold).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_inspected).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_for_product).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_customer).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_rep).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_billed).setChecked(false);
                break;
            case CrmQueries.Tickets.ON_HOLD:
                menu.findItem(R.id.action_change_case_status_any).setChecked(false);
                menu.findItem(R.id.action_change_case_status_inprogress).setChecked(false);
                menu.findItem(R.id.action_change_case_status_on_hold).setChecked(true);
                menu.findItem(R.id.action_change_case_status_to_be_inspected).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_for_product).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_rep).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_customer).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_billed).setChecked(false);
                break;
            case CrmQueries.Tickets.TO_BE_INSPECTED:
                menu.findItem(R.id.action_change_case_status_any).setChecked(false);
                menu.findItem(R.id.action_change_case_status_inprogress).setChecked(false);
                menu.findItem(R.id.action_change_case_status_on_hold).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_inspected).setChecked(true);
                menu.findItem(R.id.action_change_case_status_waiting_for_product).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_rep).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_customer).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_billed).setChecked(false);
                break;
            case CrmQueries.Tickets.WAITING_FOR_PRODUCT:
                menu.findItem(R.id.action_change_case_status_any).setChecked(false);
                menu.findItem(R.id.action_change_case_status_inprogress).setChecked(false);
                menu.findItem(R.id.action_change_case_status_on_hold).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_inspected).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_for_product).setChecked(true);
                menu.findItem(R.id.action_change_case_status_waiting_on_rep).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_customer).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_billed).setChecked(false);
                break;
            case CrmQueries.Tickets.WAITING_ON_CUSTOMER:
                menu.findItem(R.id.action_change_case_status_any).setChecked(false);
                menu.findItem(R.id.action_change_case_status_inprogress).setChecked(false);
                menu.findItem(R.id.action_change_case_status_on_hold).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_inspected).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_rep).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_for_product).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_customer).setChecked(true);
                menu.findItem(R.id.action_change_case_status_to_be_billed).setChecked(false);
                break;
            case CrmQueries.Tickets.TO_BE_BILLED:
                menu.findItem(R.id.action_change_case_status_any).setChecked(false);
                menu.findItem(R.id.action_change_case_status_inprogress).setChecked(false);
                menu.findItem(R.id.action_change_case_status_on_hold).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_inspected).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_rep).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_for_product).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_customer).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_billed).setChecked(true);
                break;
            case CrmQueries.Tickets.WAITING_ON_REP:
                menu.findItem(R.id.action_change_case_status_any).setChecked(false);
                menu.findItem(R.id.action_change_case_status_inprogress).setChecked(false);
                menu.findItem(R.id.action_change_case_status_on_hold).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_inspected).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_rep).setChecked(true);
                menu.findItem(R.id.action_change_case_status_waiting_for_product).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_customer).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_billed).setChecked(false);

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

        // I think that I have named these menu items intuitively enough that you can ascertain
        // their function.  If not, I am sorry, future me!

        DateTime now = DateTime.now();
        DateTime aMonthAgo = now.minusMonths(1);

        /* Converted this from a switch statement, which was elegant, to an if/elseif/then
        stuff due to Google moving away from resource constants being final in Gradle v8.
        https://stackoverflow.com/questions/7840914/android-resource-ids-suddenly-not-final-switches-broken  */

        final int itemId = item.getItemId();

        if (itemId == 16908332) {
            if (searchView.isIconified()) {
                onBackPressed();
            }
        } else if (itemId == R.id.action_choose_territory) {
            Intent intent = new Intent(this, FullscreenActivityChooseTerritory.class);
            intent.putExtra(CURRENT_TERRITORY, globalTerritory);
            intent.putExtra(CACHED_TERRITORIES, cachedTerritories);
            startActivityForResult(intent, 0);
        } else if (itemId == R.id.action_east_region) {
            isEastRegion = true;
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_west_region) {
            isEastRegion = false;
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_this_month) {
            monthNum = now.getMonthOfYear();
            yearNum = now.getYear();
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_last_month) {
            monthNum = aMonthAgo.getMonthOfYear();
            yearNum = aMonthAgo.getYear();
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_choose_month) {
            showMonthYearPicker();
        } else if (itemId == R.id.action_this_year) {
            yearNum = now.getYear();
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_last_year) {
            yearNum = aMonthAgo.getYear();
            sendMenuSelectionBroadcast();

            // LEADS
        } else if (itemId == R.id.action_lead_any) {
            leadFilter = CrmQueries.Leads.LeadFilter.ANY;
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_lead_qualified) {
            leadFilter = CrmQueries.Leads.LeadFilter.QUALIFIED;
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_lead_disqualified) {
            leadFilter = CrmQueries.Leads.LeadFilter.DISQUALIFIED;
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_lead_created_last_three_months) {
            leadFilter = CrmQueries.Leads.LeadFilter.LAST_THREE_MONTHS;
            sendMenuSelectionBroadcast();

            // CASE STATE
        } else if (itemId == R.id.action_case_state_open) {
            case_state = OPEN;
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_case_state_closed) {
            case_state = CLOSED;
            sendMenuSelectionBroadcast();

            // CASE STATUS
        } else if (itemId == R.id.action_change_case_status_any) {
            case_status = ANY;
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_change_case_status_inprogress) {
            case_status = CrmQueries.Tickets.IN_PROGRESS;
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_change_case_status_on_hold) {
            case_status = CrmQueries.Tickets.ON_HOLD;
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_change_case_status_to_be_inspected) {
            case_status = CrmQueries.Tickets.TO_BE_INSPECTED;
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_change_case_status_waiting_for_product) {
            case_status = CrmQueries.Tickets.WAITING_FOR_PRODUCT;
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_change_case_status_waiting_on_customer) {
            case_status = CrmQueries.Tickets.WAITING_ON_CUSTOMER;
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_change_case_status_waiting_on_rep) {
            case_status = CrmQueries.Tickets.WAITING_ON_REP;
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_change_case_status_to_be_billed) {
            case_status = CrmQueries.Tickets.TO_BE_BILLED;
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_change_case_status_problem_solved) {
            case_status = CrmQueries.Tickets.PROBLEM_SOLVED;
            sendMenuSelectionBroadcast();

            // Service agreements
        } else if (itemId == R.id.action_current_service_agreements) {
            serviceAgreementFilter = ServiceAgreementFilter.CURRENT;
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_expiring_service_agreements) {
            serviceAgreementFilter = ServiceAgreementFilter.EXPIRING;
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_expired_service_agreements) {
            serviceAgreementFilter = ServiceAgreementFilter.EXPIRED;
            sendMenuSelectionBroadcast();

            // OPPORTUNITIES
        } else if (itemId == R.id.action_opp_any) {
            dealStatus = CrmQueries.Opportunities.DealStatus.ANY;
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_opp_discovery) {
            dealStatus = CrmQueries.Opportunities.DealStatus.DISCOVERY;
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_opp_dead) {
            dealStatus = CrmQueries.Opportunities.DealStatus.DEAD;
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_opp_canceled) {
            dealStatus = CrmQueries.Opportunities.DealStatus.CANCELED;
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_opp_closed) {
            dealStatus = CrmQueries.Opportunities.DealStatus.CLOSED;
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_opp_won) {
            dealStatus = CrmQueries.Opportunities.DealStatus.WON;
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_opp_pending) {
            dealStatus = CrmQueries.Opportunities.DealStatus.PENDING;
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_opp_evaluating) {
            dealStatus = CrmQueries.Opportunities.DealStatus.EVALUATING;
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_opp_qualifying) {
            dealStatus = CrmQueries.Opportunities.DealStatus.QUALIFYING;
            sendMenuSelectionBroadcast();
        } else if (itemId == R.id.action_opp_stalled) {
            dealStatus = CrmQueries.Opportunities.DealStatus.STALLED;
            sendMenuSelectionBroadcast();
        }
        return super.onOptionsItemSelected(item);
    }

    public static void destroyChartDialogIfVisible() {
        if (chartPopupDialog != null && chartPopupDialog.isShowing()) {
            chartPopupDialog.dismiss();
        }
    }

    public void sendMenuSelectionBroadcast() {
        Intent menuActionIntent = new Intent(MENU_SELECTION);
        menuActionIntent.putExtra(MONTH, monthNum);
        menuActionIntent.putExtra(YEAR, yearNum);
        sendBroadcast(menuActionIntent);
    }

    @SuppressLint("NewApi")
    private void showMonthYearPicker() {
        final MonthYearPickerDialog mpd = new MonthYearPickerDialog();
        mpd.setListener((view, year, month, dayOfMonth) -> {
            Intent dateChanged = new Intent(MENU_SELECTION);
            dateChanged.putExtra(MONTH, month);
            dateChanged.putExtra(YEAR, year);
            monthNum = month;
            yearNum = year;
            sendBroadcast(dateChanged);
            mpd.dismiss();
        });
        mpd.show(getSupportFragmentManager(), "MonthYearPickerDialog");
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public static final int LANDING_PAGE = 0;
        public static final int SALES_PAGE = 1;
        public static final int LEADS_PAGE = 2;
        public static final int OPPORTUNITIES_PAGE = 3;
        public static final int CASES_PAGE = 4;
        public static final int ACCOUNTS_PAGE = 5;
        public static final int SERVICE_AGREEMENTS_PAGE = 6;

        public SectionsPagerAdapter(androidx.fragment.app.FragmentManager fm) {
            super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            sectionsPagerAdapter = this;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            Log.d("getItem", "Creating Fragment in pager at index: " + position);
            Log.w(TAG, "getItem: PAGER POSITION: " + position);

            switch (position) {
                case SALES_PAGE: {
                    Fragment fragment = new Frag_SalesLines();
                    Bundle args = new Bundle();
                    args.putInt(Frag_SalesLines.ARG_SECTION_NUMBER, position + 1);
                    fragment.setArguments(args);
                    return fragment;
                }
                case LEADS_PAGE: {
                    Fragment fragment = new Frag_Leads();
                    Bundle args = new Bundle();
                    args.putInt(Frag_SalesLines.ARG_SECTION_NUMBER, position + 1);
                    fragment.setArguments(args);
                    return fragment;
                }
                case OPPORTUNITIES_PAGE: {
                    Fragment fragment = new Frag_Opportunities();
                    Bundle args = new Bundle();
                    args.putInt(Frag_Opportunities.ARG_SECTION_NUMBER, position + 1);
                    fragment.setArguments(args);
                    return fragment;
                }
                case CASES_PAGE: {
                    Fragment fragment = new Frag_Cases();
                    Bundle args = new Bundle();
                    args.putInt(Frag_Opportunities.ARG_SECTION_NUMBER, position + 1);
                    fragment.setArguments(args);
                    return fragment;
                }
                case ACCOUNTS_PAGE: {
                    Fragment fragment = new Frag_Accounts();
                    Bundle args = new Bundle();
                    args.putInt(Frag_Accounts.ARG_SECTION_NUMBER, position + 1);
                    fragment.setArguments(args);
                    return fragment;
                }
                case SERVICE_AGREEMENTS_PAGE: {
                    Fragment fragment = new Frag_ServiceAgreements();
                    Bundle args = new Bundle();
                    args.putInt(Frag_ServiceAgreements.ARG_SECTION_NUMBER, position + 1);
                    fragment.setArguments(args);
                    return fragment;
                }
                default: {
                    Fragment fragment = new Frag_LandingPage();
                    Bundle args = new Bundle();
                    args.putInt(Frag_SalesLines.ARG_SECTION_NUMBER, position + 1);
                    fragment.setArguments(args);
                    return fragment;
                }
            }
        }

        @Override
        public int getCount() {
            return 7;
        }

        public Object getCaseCriteriaTitleAddendum() {
            switch (case_status) {
                case CrmQueries.Tickets.ANY:
                    return getString(R.string.any);
                case CrmQueries.Tickets.IN_PROGRESS:
                    return getString(R.string.in_progress);
                case CrmQueries.Tickets.ON_HOLD:
                    return getString(R.string.on_hold);
                case CrmQueries.Tickets.TO_BE_BILLED:
                    return getString(R.string.to_be_billed);
                case CrmQueries.Tickets.TO_BE_INSPECTED:
                    return getString(R.string.to_be_inspected);
                case CrmQueries.Tickets.WAITING_ON_CUSTOMER:
                    return getString(R.string.waiting_on_customer);
                case CrmQueries.Tickets.WAITING_ON_REP:
                    return getString(R.string.waiting_on_rep);
                case CrmQueries.Tickets.WAITING_FOR_PRODUCT:
                    return getString(R.string.waiting_for_product);
                case CrmQueries.Tickets.PROBLEM_SOLVED:
                    return getString(R.string.problem_solved);
            }
            return null;
        }

        public String getOpportunityTitleAddendum() {
            switch (dealStatus) {
                case DEAD:
                    return getString(R.string.dead);
                case CANCELED:
                    return getString(R.string.cancelled);
                case CLOSED:
                    return getString(R.string.closed);
                case WON:
                    return getString(R.string.won);
                case PENDING:
                    return getString(R.string.pending);
                case EVALUATING:
                    return getString(R.string.evaluating);
                case QUALIFYING:
                    return getString(R.string.qualifying);
                case STALLED:
                    return getString(R.string.stalled);
                case DISCOVERY:
                    return getString(R.string.discovery);
                default:
                    return getString(R.string.all);
            }
        }

        public String getLeadsTitleAddendum() {
            switch (leadFilter) {
                case QUALIFIED:
                    return getString(R.string.qualified);
                case DISQUALIFIED:
                    return getString(R.string.disqualified);
                case LAST_THREE_MONTHS:
                    return getString(R.string.last_3_months);
                default:
                    return getString(R.string.any);
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {

            if (globalTerritory == null) {
                globalTerritory = MediUser.getMe().getTerritory();
            }

            switch (position) {
                case LANDING_PAGE:
                    return getString(R.string.companywide_page_title);
                case SALES_PAGE:
                    return getString(R.string.sales_lines_page_title) + getSalesTimeSpanTitle();
                case LEADS_PAGE:
                    return getString(R.string.leads_page_title) + getLeadsTitleAddendum();
                case OPPORTUNITIES_PAGE:
                    return getString(R.string.opportunities_page_title) + getOpportunityTitleAddendum();
                case CASES_PAGE:
                    return getString(R.string.cases_page_title) + getCaseCriteriaTitleAddendum();
                case ACCOUNTS_PAGE:
                    return getString(R.string.accounts_page_title);
                case SERVICE_AGREEMENTS_PAGE:
                    switch (serviceAgreementFilter) {
                        case CURRENT:
                            return getString(R.string.active_service_agreements_for_territory_page_title, globalTerritory.territoryName);
                        case EXPIRED:
                            return getString(R.string.expired_service_agreements_for_territory_page_title, globalTerritory.territoryName);
                        case EXPIRING:
                            return getString(R.string.expiring_service_agreements_for_territory_page_title, globalTerritory.territoryName);
                    }
            }
            return null;
        }

        public String getSalesTimeSpanTitle() {
            if (monthNum == DateTime.now().getMonthOfYear()) {
                return getString(R.string.this_month);
            } else if (monthNum == DateTime.now().minusMonths(1).getMonthOfYear()) {
                return getString(R.string.last_month);
            } else {
                return Helpers.DatesAndTimes.getMonthName(monthNum) + yearNum;
            }
        }
    }

    //region ********************************** FRAGS *****************************************
    public static class Frag_LandingPage extends Fragment {
        public View root;
        public RecyclerView recyclerView;
        LandingPageRecyclerAdapter adapter;
        RecyclerView listview;
        Territory curTerritory;
        BroadcastReceiver territoryChangedReceiver;
        BroadcastReceiver menuSelectionReceiver;
        ArrayList<LandingPageItem> landingPageItems;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            root = inflater.inflate(R.layout.frag_dashboard, container, false);
            super.onCreateView(inflater, container, savedInstanceState);

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
            requireActivity().unregisterReceiver(menuSelectionReceiver);
            requireActivity().unregisterReceiver(territoryChangedReceiver);
            Log.i(TAG, "onPause Unregistered the sales lines receiver");
        }

        @Override
        public void onResume() {
            IntentFilter filter = new IntentFilter(TERRITORY_RESULT);
            filter.addAction(MENU_SELECTION);
            requireActivity().registerReceiver(menuSelectionReceiver, filter);
            requireActivity().registerReceiver(territoryChangedReceiver, new IntentFilter(MENU_SELECTION));

            Log.i(TAG, "onResume Registered the sales lines receiver");
            super.onResume();
        }

        @Override
        public void onPause() {

            super.onPause();
        }

        void populateList() {

            landingPageItems = new ArrayList<>();
            landingPageItems.add(new LandingPageItem(getString(R.string.go_to_sales), Activity_TerritoryData.SectionsPagerAdapter.SALES_PAGE, R.drawable.dollar6, R.drawable.arrow_right2));
            landingPageItems.add(new LandingPageItem(getString(R.string.go_to_leads), Activity_TerritoryData.SectionsPagerAdapter.LEADS_PAGE, R.drawable.lead2, R.drawable.arrow_right2));
            landingPageItems.add(new LandingPageItem(getString(R.string.go_to_opps), Activity_TerritoryData.SectionsPagerAdapter.OPPORTUNITIES_PAGE, R.drawable.opportunity1, R.drawable.arrow_right2));
            landingPageItems.add(new LandingPageItem(getString(R.string.go_to_cases), Activity_TerritoryData.SectionsPagerAdapter.CASES_PAGE, R.drawable.ticket1, R.drawable.arrow_right2));
            landingPageItems.add(new LandingPageItem(getString(R.string.go_to_accounts), Activity_TerritoryData.SectionsPagerAdapter.ACCOUNTS_PAGE, R.drawable.customer2, R.drawable.arrow_right2));
            landingPageItems.add(new LandingPageItem(getString(R.string.go_to_service_agreements), Activity_TerritoryData.SectionsPagerAdapter.SERVICE_AGREEMENTS_PAGE, R.drawable.contract32, R.drawable.arrow_right2));
            landingPageItems.add(new LandingPageItem(getString(R.string.choose_territory), LandingPageItem.CHANGE_TERRITORY_CODE));

            listview = root.findViewById(R.id.recyclerView);
            adapter = new LandingPageRecyclerAdapter(getContext(), landingPageItems);
            listview.setLayoutManager(new LinearLayoutManager(getContext()));
            listview.setAdapter(adapter);
            adapter.setClickListener((view, position) -> {
                LandingPageItem item = adapter.getItem(position);
                if (item.pageIndex == LandingPageItem.CHANGE_TERRITORY_CODE) {
                    Intent intent = new Intent(getContext(), FullscreenActivityChooseTerritory.class);
                    intent.putExtra(CURRENT_TERRITORY, globalTerritory);
                    startActivityForResult(intent, 0);
                } else {
                    mViewPager.setCurrentItem(item.pageIndex, true);
                }
            });
        }

    }

    public static class Frag_SalesLines extends Fragment {
        @NonNls
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View root;
        public RecyclerView recyclerView;
        RefreshLayout refreshLayout;
        OrderLineRecyclerAdapter adapter;
        Territory curTerritory;
        BroadcastReceiver territoryChangedReceiver;
        ArrayList<OrderProduct> allOrders = new ArrayList<>();
        TextView txtNoSales;
        BroadcastReceiver menuSelectionReceiver;
        BroadcastReceiver volumeButtonClickReceiver;
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
            refreshLayout.setOnRefreshListener(onRefresh -> getSalesLines());
            refreshLayout.setOnLoadMoreListener(onLoadMore -> refreshLayout.finishLoadMore(500/*,false*/));

            txtNoSales.setVisibility((allOrders == null || allOrders.size() == 0) ? View.VISIBLE : View.GONE);

            recyclerView = root.findViewById(R.id.orderLinesRecyclerview);
            super.onCreateView(inflater, container, savedInstanceState);

            fab = root.findViewById(R.id.floatingActionButton);
            fab.setOnClickListener((view) -> {
                Intent intent = new Intent(FAB_CLICKED);
                requireActivity().sendBroadcast(intent);
            });

            menuSelectionReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Received month and year broadcast! (sales lines frag)");
                    monthNum = intent.getIntExtra(MONTH, DateTime.now().getMonthOfYear());
                    yearNum = intent.getIntExtra(YEAR, DateTime.now().getYear());
                    getSalesLines();
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

            // Parent activity will send a broadcast if the user clicks the volume up/down buttons.
            // We can use this to scroll the listview to the next time span (Today -> This week etc.)
            volumeButtonClickReceiver = new BroadcastReceiver() {

                final RecyclerView.SmoothScroller smoothScroller = new
                        MyLinearSmoothScroller(requireContext()) {
                            @Override
                            protected int getVerticalSnapPreference() {
                                return MyLinearSmoothScroller.SNAP_TO_START;
                            }
                        };


                @Override
                public void onReceive(Context context, Intent intent) {

                    // Set up a mechanism to scroll the listview to the next category if a volume
                    // button is pressed.
                    if (intent != null && intent.hasExtra(VOLUME_CLICKED)) {
                        if (Objects.requireNonNull(intent.getStringExtra(VOLUME_CLICKED))
                                .equals(VOLUME_UP_CLICKED)) {
                            Log.i(TAG, "onReceive | volume up broadcast received");
                            if (adapter != null && adapter.mData != null && adapter.mData.size() > 0) {
                                smoothScroller.setTargetPosition(adapter.getPreviousClickableCategoryPosition());
                            }
                        } else if (Objects.requireNonNull(intent.getStringExtra(VOLUME_CLICKED))
                                .equals(VOLUME_DOWN_CLICKED)) {
                            Log.i(TAG, "onReceive | volume down broadcast received");
                            if (adapter != null && adapter.mData != null && adapter.mData.size() > 0) {
                                smoothScroller.setTargetPosition(adapter.getNextClickableCategoryPosition());
                            }
                        }

                        try {
                            // Actually scroll the listview
                            Objects.requireNonNull(recyclerView.getLayoutManager()).startSmoothScroll(smoothScroller);
                        } catch (Exception e) { /* do nothing */ }
                    }
                }
            };

            return root;
        }

        @Override
        public void onStop() {
            super.onStop();
            requireActivity().unregisterReceiver(menuSelectionReceiver);
            requireActivity().unregisterReceiver(territoryChangedReceiver);
            requireActivity().unregisterReceiver(volumeButtonClickReceiver);
            Log.i(TAG, "onStop | Unregistered receivers");
        }

        @Override
        public void onStart() {
            super.onStart();
            requireActivity().registerReceiver(territoryChangedReceiver, new IntentFilter(TERRITORY_RESULT));
            requireActivity().registerReceiver(menuSelectionReceiver, new IntentFilter(MENU_SELECTION));
            requireActivity().registerReceiver(volumeButtonClickReceiver, new IntentFilter(VOLUME_CLICKED));
            Log.i(TAG, "onStart | Registered receivers");
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            Log.i(TAG, "onDestroyView ");
        }

        @Override
        public void onResume() {
            Log.i(TAG, "onResume ");
            super.onResume();
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        protected void getSalesLines() {
            refreshLayout.autoRefreshAnimationOnly();

            String query;

            if (monthNum == DateTime.now().getMonthOfYear() && yearNum == DateTime.now().getYear()) {
                query = CrmQueries.OrderLines.getOrderLines(null,
                        CrmQueries.Operators.DateOperator.THIS_MONTH);
            } else if (monthNum == DateTime.now().minusMonths(1).getMonthOfYear() && yearNum == DateTime.now().getYear()) {
                query = CrmQueries.OrderLines.getOrderLines(null,
                        CrmQueries.Operators.DateOperator.LAST_MONTH);
            } else {
                query = CrmQueries.OrderLines.getOrderLines(null, monthNum, yearNum);
            }

            String title = getString(R.string.sales_lines_time_span_page_title, sectionsPagerAdapter
                    .getSalesTimeSpanTitle());
            toolbar.setTitle(title);
            sectionsPagerAdapter.notifyDataSetChanged();

            txtNoSales.setVisibility(View.GONE);

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

            ArrayList<OrderProduct> orderList = new ArrayList<>();

            boolean addedTodayHeader = false;
            boolean addedYesterdayHeader = false;
            boolean addedThisWeekHeader = false;
            boolean addedThisMonthHeader = false;
            boolean addedLastMonthHeader = false;
            boolean addedOlderHeader = false;

            float todaySubtotal = 0f;
            float yesterdaySubtotal = 0f;
            float thisWeekSubtotal = 0f;
            float thisMonthSubtotal = 0f;
            float lastMonthSubtotal = 0f;
            float olderSubtotal = 0f;

            ArrayList<OrderProduct> todayAggregate = new ArrayList<>();
            ArrayList<OrderProduct> yesterdayAggregate = new ArrayList<>();
            ArrayList<OrderProduct> thisWeekAggregate = new ArrayList<>();
            ArrayList<OrderProduct> thisMonthAggregate = new ArrayList<>();
            ArrayList<OrderProduct> lastMonthAggregate = new ArrayList<>();
            ArrayList<OrderProduct> olderAggregate = new ArrayList<>();

            final int TODAY = 1;
            final int YESTERDAY = 2;
            final int THISWEEK = 3;
            final int THISMONTH = 4;
            final int LASTMONTH = 5;
            final int OLDER = 6;

            Log.i(TAG, "populateTripList: Preparing the dividers and trips...");
            for (int i = 0; i < (allOrders.size()); i++) {

                OrderProduct curProduct = allOrders.get(i);

                // Order was today
                if (curProduct.orderDate.getDayOfMonth() == DateTime.now().getDayOfMonth() &&
                        curProduct.orderDate.getMonthOfYear() == DateTime.now().getMonthOfYear() &&
                        curProduct.orderDate.getYear() == DateTime.now().getYear()) {
                    if (!addedTodayHeader) {
                        OrderProduct headerObj = new OrderProduct();
                        headerObj.isClickableHeader = true;
                        headerObj.setTitle(getString(R.string.today));
                        headerObj.groupid = TODAY;
                        orderList.add(headerObj);
                        addedTodayHeader = true;
                        Log.d(TAG + "populateList", "Added a header object to the array that will eventually be a header childView in the list view named, 'Today' - This will not be added again!");
                    }
                    todaySubtotal += curProduct.extendedAmt;
                    curProduct.groupid = TODAY;
                    todayAggregate.add(curProduct);
                    // Order was yesterday
                } else if (curProduct.orderDate.getDayOfMonth() == DateTime.now().minusDays(1).getDayOfMonth() &&
                        curProduct.orderDate.getMonthOfYear() == DateTime.now().minusDays(1).getMonthOfYear() &&
                        curProduct.orderDate.getYear() == DateTime.now().minusDays(1).getYear()) {
                    if (!addedYesterdayHeader) {
                        OrderProduct headerObj = new OrderProduct();
                        headerObj.isClickableHeader = true;
                        headerObj.setTitle(getString(R.string.yesterday));
                        headerObj.groupid = YESTERDAY;
                        orderList.add(headerObj);
                        addedYesterdayHeader = true;
                        Log.d(TAG + "populateList", "Added a header object to the array " +
                                "that will eventually be a header childView in the list view named," +
                                " 'Yesterday' - This will not be added again!");
                    }
                    yesterdaySubtotal += curProduct.extendedAmt;
                    curProduct.groupid = YESTERDAY;
                    yesterdayAggregate.add(curProduct);
                    // Order was this week
                } else if (curProduct.orderDate.getWeekOfWeekyear() == DateTime.now().getWeekOfWeekyear() &&
                        curProduct.orderDate.getMonthOfYear() == DateTime.now().getMonthOfYear() &&
                        curProduct.orderDate.getYear() == DateTime.now().getYear()) {
                    if (!addedThisWeekHeader) {
                        OrderProduct headerObj = new OrderProduct();
                        headerObj.isClickableHeader = true;
                        headerObj.setTitle(getString(R.string.this_week));
                        headerObj.groupid = THISWEEK;
                        orderList.add(headerObj);
                        addedThisWeekHeader = true;
                        Log.d(TAG + "populateList", "Added a header object to the array that will eventually be a header childView in the list view named, 'This week' - This will not be added again!");
                    }
                    thisWeekSubtotal += curProduct.extendedAmt;
                    curProduct.groupid = THISWEEK;
                    thisWeekAggregate.add(curProduct);
                    // Order was this month
                } else if (curProduct.orderDate.getMonthOfYear() == DateTime.now().getMonthOfYear() &&
                        curProduct.orderDate.getYear() == DateTime.now().getYear()) {
                    if (!addedThisMonthHeader) {
                        OrderProduct headerObj = new OrderProduct();
                        headerObj.isClickableHeader = true;
                        headerObj.setTitle(getString(R.string.this_month));
                        headerObj.groupid = THISMONTH;
                        orderList.add(headerObj);
                        addedThisMonthHeader = true;
                        Log.d(TAG + "populateList", "Added a header object to the array that will eventually be a header childView in the list view named, 'This month' - This will not be added again!");
                    }
                    thisMonthSubtotal += curProduct.extendedAmt;
                    curProduct.groupid = THISMONTH;
                    thisMonthAggregate.add(curProduct);
                    // Order was last month
                } else if (curProduct.orderDate.getMonthOfYear() == DateTime.now().minusMonths(1).getMonthOfYear() &&
                        curProduct.orderDate.getYear() == DateTime.now().minusMonths(1).getYear()) {
                    if (!addedLastMonthHeader) {
                        OrderProduct headerObj = new OrderProduct();
                        headerObj.isClickableHeader = true;
                        headerObj.setTitle(getString(R.string.last_month));
                        headerObj.groupid = LASTMONTH;
                        orderList.add(headerObj);
                        addedLastMonthHeader = true;
                        Log.d(TAG + "populateList", "Added a header object to the array that will eventually be a header childView in the list view named, 'Last month' - This will not be added again!");
                    }
                    lastMonthSubtotal += curProduct.extendedAmt;
                    curProduct.groupid = LASTMONTH;
                    lastMonthAggregate.add(curProduct);
                    // Order was older than 2 months.
                } else {
                    if (!addedOlderHeader) {
                        OrderProduct headerObj = new OrderProduct();
                        headerObj.isClickableHeader = true;
                        headerObj.setTitle(getString(R.string.total));
                        headerObj.groupid = OLDER;
                        orderList.add(headerObj);
                        addedOlderHeader = true;
                        Log.d(TAG + "populateList", "Added a header object to the array " +
                                "that will eventually be a header childView in the list view named, " +
                                "'Older' - This will not be added again!");
                    }
                    olderSubtotal += curProduct.extendedAmt;
                    curProduct.groupid = OLDER;
                    olderAggregate.add(curProduct);
                }

                // This seems unnecessary... commenting it out
                // OrderProduct orderProduct = allOrders.get(i);
                orderList.add(curProduct);
            }

            // Sum today, yesterday, this week, etc.
            // This feels... right but I can't actually logic out if it is or is even necessary.
            thisMonthSubtotal = thisMonthSubtotal + thisWeekSubtotal + yesterdaySubtotal + todaySubtotal;
            thisWeekSubtotal = thisWeekSubtotal + yesterdaySubtotal + todaySubtotal;

            // Append the subtotals to the headers
            for (OrderProducts.OrderProduct product : orderList) {
                if (todaySubtotal > 0) {
                    if (product.isHeader && product.partNumber.equals(getString(R.string.today))) {
                        product.partNumber = product.partNumber + " (" + Helpers.Numbers.convertToCurrency(todaySubtotal) + ")";
                    }
                }
                if (yesterdaySubtotal > 0) {
                    if (product.isHeader && product.partNumber.equals(getString(R.string.yesterday))) {
                        product.partNumber = product.partNumber + " (" + Helpers.Numbers.convertToCurrency(yesterdaySubtotal) + ")";
                    }
                }
                if (yesterdaySubtotal > 0) {
                    if (product.isHeader && product.partNumber.equals(getString(R.string.this_week))) {
                        product.partNumber = product.partNumber + " (" + Helpers.Numbers.convertToCurrency(thisWeekSubtotal) + ")";
                    }
                }
                if (thisMonthSubtotal > 0) {
                    if (product.isHeader && product.partNumber.equals(getString(R.string.this_month))) {
                        product.partNumber = product.partNumber + " (" + Helpers.Numbers.convertToCurrency(thisMonthSubtotal) + ")";
                    }
                }
                if (lastMonthSubtotal > 0) {
                    if (product.isHeader && product.partNumber.equals(getString(R.string.last_month))) {
                        product.partNumber = product.partNumber + " (" + Helpers.Numbers.convertToCurrency(lastMonthSubtotal) + ")";
                    }
                }
                if (olderSubtotal > 0) {
                    if (product.isHeader && product.partNumber.equals(getString(R.string.total))) {
                        product.partNumber = product.partNumber + " (" + Helpers.Numbers.convertToCurrency(olderSubtotal) + ")";
                    }
                }
            }

            Log.i(TAG, "populateTripList Finished preparing the dividers and trips.");

            if (!requireActivity().isFinishing()) {
                adapter = new OrderLineRecyclerAdapter(getContext(), orderList);
                adapter.setOnRowClickListener((view, position) -> {
                    OrderProduct orderProduct = adapter.mData.get(position);
                    if (orderProduct.isClickableHeader) {

                        AggregatedSales aggregatedSales = null;

                        switch (orderProduct.groupid) {
                            case TODAY:
                                aggregatedSales = new AggregatedSales(todayAggregate);
                                break;
                            case YESTERDAY:
                                aggregatedSales = new AggregatedSales(yesterdayAggregate);
                                break;
                            case THISWEEK:
                                aggregatedSales = new AggregatedSales(thisWeekAggregate);
                                break;
                            case THISMONTH:
                                aggregatedSales = new AggregatedSales(thisMonthAggregate);
                                break;
                            case LASTMONTH:
                                aggregatedSales = new AggregatedSales(lastMonthAggregate);
                                break;
                            case OLDER:
                                aggregatedSales = new AggregatedSales(olderAggregate);
                                break;
                        }

                        if (aggregatedSales != null) {
                            getSummary(aggregatedSales);
                        } else {
                            Toast.makeText(getContext(), getString(R.string
                                    .failed_to_create_summary), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                adapter.setOnLinkButtonClickListener((view, position) -> {
                    Log.i(TAG, "onLinkButtonClick ");
                    CrmEntities.Accounts.Account selectedAccount = new CrmEntities.Accounts.Account(
                            adapter.mData.get(position).customerid, adapter.mData.get(position).customeridFormatted);
                    Intent intent = new Intent(getContext(), Activity_AccountData.class);
                    intent.setAction(Activity_AccountData.GO_TO_ACCOUNT);
                    intent.putExtra(Activity_AccountData.GO_TO_ACCOUNT_OBJECT, selectedAccount);
                    intent.putExtra(Activity_AccountData.INITIAL_PAGE, Activity_AccountData.SectionsPagerAdapter.SALES_LINE_PAGE);
                    startActivity(intent);
                });
                adapter.setOnRowLongClickListener((view, position) -> {});
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(adapter);

                refreshLayout.finishRefresh();
            } else {
                Log.w(TAG, "populateList: CAN'T POPULATE AS THE ACTIVITY IS FINISHING!!!");
            }

            txtNoSales.setVisibility((allOrders == null || allOrders.size() == 0) ? View.VISIBLE : View.GONE);

        }

        protected void getSummary(AggregatedSales totals) {

            if (allOrders == null || allOrders.size() == 0) {
                getSalesLines();
                Toast.makeText(getContext(), getString(R.string.toast_getting_sales_data), Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(getContext(), AggregateSalesActivity.class);
            intent.putExtra(AggregateSalesActivity.AGGREGATED_TOTALS, totals);
            startActivity(intent);

        }
    }

    public static class Frag_Leads extends Fragment {
        public View rootView;
        public RecyclerView listview;
        RefreshLayout refreshLayout;
        TextView txtNoLeads;
        public Territory curTerritory;
        public CrmEntities.Leads leads;
        ArrayList<BasicObjects.BasicObject> objects = new ArrayList<>();
        BasicObjectRecyclerAdapter adapter;
        BroadcastReceiver leadsReceiver;
        BroadcastReceiver territoryChangedReceiver;
        FloatingActionButton fab;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.frag_leads, container, false);
            listview = rootView.findViewById(R.id.leadsRecyclerview);
            refreshLayout = rootView.findViewById(R.id.refreshLayout);
            txtNoLeads = rootView.findViewById(R.id.txtNoLeads);
            refreshLayout.setOnRefreshListener(refreshLayout -> getLeads());

            refreshLayout.setEnableLoadMore(false);
            super.onCreateView(inflater, container, savedInstanceState);

            fab = rootView.findViewById(R.id.floatingActionButton);
            fab.setVisibility(View.VISIBLE); // Nothing to filter (yet) so hide.
            fab.setOnClickListener((view) -> {
                Intent intent = new Intent(FAB_CLICKED);
                requireActivity().sendBroadcast(intent);
            });

            // Gifts from Santa - open present and do stuff with what we got!
            leadsReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Opportunities frag received a broadcast!");

                    // User made an options menu selection - receive the details of that choice
                    // via the broadcast sent by the parent activity.
                    if (Objects.requireNonNull(intent.getAction()).equals(MENU_SELECTION)) {
                        getLeads();
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
            requireActivity().unregisterReceiver(leadsReceiver);
            requireActivity().unregisterReceiver(territoryChangedReceiver);
            super.onDestroyView();
        }

        @Override
        public void onResume() {
            super.onResume();
            IntentFilter intentFilterMenuSelection = new IntentFilter(MENU_SELECTION);
            requireActivity().registerReceiver(leadsReceiver, intentFilterMenuSelection);
            requireActivity().registerReceiver(territoryChangedReceiver, new IntentFilter(MENU_SELECTION));
            Log.i(TAG, "onResume Registered opportunities receiver");
        }

        public void getLeads() {
            refreshLayout.autoRefreshAnimationOnly();
            CrmEntities.Leads.getAllLeads(getContext(), leadFilter, new MyInterfaces.GetLeadsListener() {
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
                    Toast.makeText(getContext(), getString(R.string.toast_failed_to_get_opportunities) + error, Toast.LENGTH_LONG).show();
                }
            });
        }

        void populateList() {
            adapter = new BasicObjectRecyclerAdapter(getContext(), objects);
            listview.setLayoutManager(new LinearLayoutManager(getContext()));
            listview.setAdapter(adapter);
            adapter.setClickListener((view, position) -> {
                CrmEntities.Leads.Lead selectedLead =
                        (CrmEntities.Leads.Lead) objects.get(position).object;
                ContactActions actions = new ContactActions(getActivity(), selectedLead);
                actions.showContactOptions(); 
            });

            txtNoLeads.setVisibility((leads == null || leads.list.size() == 0) ? View.VISIBLE : View.GONE);

        }

        void showOpportunityOptions(final Opportunities.Opportunity opportunity) {

            final Dialog dialog = new Dialog(requireContext());
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

            String bgTruncated;
            if (opportunity.currentSituation != null && opportunity.currentSituation.length() > 125) {
                bgTruncated = opportunity.currentSituation.substring(0, 125) + "...\n";
            } else {
                bgTruncated = opportunity.currentSituation;
            }

            txtBackground.setText(bgTruncated);

            Button btnQuickNote;
            btnQuickNote = dialog.findViewById(R.id.btn_add_quick_note);
            btnQuickNote.setOnClickListener(view -> {
                dialog.dismiss();
                showAddNoteDialog(opportunity);
            });

            Button btnViewOpportunity = dialog.findViewById(R.id.btn_view_opportunity);
            btnViewOpportunity.setOnClickListener(view -> {
                Intent intent = new Intent(getContext(), OpportunityActivity.class);
                intent.putExtra(OpportunityActivity.OPPORTUNITY_TAG, opportunity);
                startActivity(intent);
                dialog.dismiss();
            });

            dialog.setCancelable(true);
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();
        }

        void showAddNoteDialog(final Opportunities.Opportunity opportunity) {
            CrmEntities.Annotations.showAddNoteDialog(getContext(), opportunity.entityid, new MyInterfaces.CrmRequestListener() {
                @Override
                public void onComplete(Object result) {
                    Log.i(TAG, "onComplete ");
                    final Helpers.Notifications notifications = new Helpers.Notifications(requireContext());
                    notifications.create(getString(R.string.notification_title_opp_note_created),
                            getString(R.string.notification_body_note_added), false);
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
        @NonNls
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
            refreshLayout.setOnRefreshListener(refreshLayout -> getOpportunities());
            refreshLayout.setEnableLoadMore(false);

            menuReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    getOpportunities();
                    Objects.requireNonNull(mViewPager.getAdapter()).notifyDataSetChanged();
                }
            };

            if (curTerritory == null) {
                curTerritory = MediUser.getMe().getTerritory();
            }

            if (globalTerritory != curTerritory) {
                curTerritory = globalTerritory;
                getOpportunities();
            } else {
                if (opportunities != null) {
                    populateList();
                }
            }

            super.onCreateView(inflater, container, savedInstanceState);

            fab = rootView.findViewById(R.id.floatingActionButton);
            fab.setVisibility(View.VISIBLE); // Nothing to filter (yet) so hide.
            fab.setOnClickListener((view) -> {
                Intent intent = new Intent(FAB_CLICKED);
                requireActivity().sendBroadcast(intent);
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
            requireActivity().unregisterReceiver(menuReceiver);
            super.onDestroyView();
        }

        @Override
        public void onResume() {
            super.onResume();
            IntentFilter intentFilterMenuSelection = new IntentFilter(MENU_SELECTION);
            requireActivity().registerReceiver(menuReceiver, intentFilterMenuSelection);
            Log.i(TAG, "onResume Registered opportunities receiver");
        }

        public void getOpportunities() {
            refreshLayout.autoRefresh();
            Opportunities.retrieveOpportunities(dealStatus, null, new MyInterfaces.GetOpportunitiesListener() {
                @Override
                public void onSuccess(Opportunities crmOpportunities) {
                    opportunities = crmOpportunities;
                    populateList();
                    refreshLayout.finishRefresh();
                }

                @Override
                public void onFailure(String error) {
                    refreshLayout.finishRefresh();
                    Toast.makeText(getContext(), getString(R.string.toast_failed_to_get_opportunities) 
                            + error, Toast.LENGTH_LONG).show();
                }
            });
        }

        void populateList() {
            objects = new ArrayList<>();
            for (Opportunities.Opportunity opp : opportunities.list) {
                BasicObjects.BasicObject object = new BasicObjects.BasicObject(opp.name, opp
                        .getDealTypePretty() + getString(R.string.created_on) + opp.createdOnFormatted, opp);
                object.middleText = opp.accountname;
                object.topRightText = opp.probabilityPretty;
                object.iconResource = R.drawable.opportunity1;
                object.bottomRightText = opp.ownername + "\n" + opp.getPrettyEstimatedValue();
                objects.add(object);
            }

            adapter = new BasicObjectRecyclerAdapter(getContext(), objects);
            listview.setLayoutManager(new LinearLayoutManager(getContext()));
            listview.setAdapter(adapter);
            adapter.setClickListener(((view, position) -> {
                Opportunities.Opportunity selectedOpportunity =
                        (Opportunities.Opportunity) objects.get(position).object;

                Intent intent = new Intent(getContext(), BasicEntityActivity.class);
                intent.putExtra(BasicEntityActivity.ACTIVITY_TITLE, getString(R.string.opp_details));
                intent.putExtra(BasicEntityActivity.ENTITYID, selectedOpportunity.entityid);
                intent.putExtra(BasicEntityActivity.ENTITY_LOGICAL_NAME, "opportunity");
                intent.putExtra(BasicEntityActivity.GSON_STRING, selectedOpportunity.toBasicEntity().toGson());
                startActivityForResult(intent, BasicEntityActivity.REQUEST_BASIC);

                try {
                    MileBuddyMetrics.updateMetric(getContext(), MileBuddyMetrics.MetricName.LAST_OPENED_OPPORTUNITY, DateTime.now());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));

            txtNoOpportunities.setVisibility((opportunities == null || opportunities.list.size() == 0) ? View.VISIBLE : View.GONE);

        }

        void showOpportunityOptions(final Opportunities.Opportunity opportunity) {

            final Dialog dialog = new Dialog(requireContext());
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

            String bgTruncated;
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
                    Intent intent = new Intent(getContext(), OpportunityActivity.class);
                    intent.putExtra(OpportunityActivity.OPPORTUNITY_TAG, opportunity);
                    startActivity(intent);
                    dialog.dismiss();
                }
            });

            dialog.setCancelable(true);
            Objects.requireNonNull(dialog.getWindow(), getString(R.string.error_opp_dialog_is_null))
                    .setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();
        }

        void showAddNoteDialog(final Opportunities.Opportunity opportunity) {
            CrmEntities.Annotations.showAddNoteDialog(getContext(), opportunity.entityid, new MyInterfaces.CrmRequestListener() {
                @Override
                public void onComplete(Object result) {
                    Log.i(TAG, "onComplete ");
                    final Helpers.Notifications notifications = new Helpers.Notifications(requireContext());
                    notifications.create(getString(R.string.notification_title_opp_note_created),
                            getString(R.string.notification_body_note_added), false);
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
            refreshLayout.setOnRefreshListener(listener -> getTickets());
            super.onCreateView(inflater, container, savedInstanceState);

            fab = rootView.findViewById(R.id.floatingActionButton);
            fab.setOnClickListener(v -> {
                Intent intent = new Intent(FAB_CLICKED);
                requireActivity().sendBroadcast(intent);
            });

            casesReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Received month and year broadcast! (cases frag)");
                    Objects.requireNonNull(mViewPager.getAdapter()).notifyDataSetChanged();
                    getTickets();
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
                    if (intent.getParcelableExtra(TERRITORY_RESULT) != null) {
                        curTerritory = globalTerritory;
                        getTickets();
                    }
                }
            };

            txtNoTickets = rootView.findViewById(R.id.txtNoTickets);

            return rootView;
        }

        void getTickets() {
            refreshLayout.autoRefreshAnimationOnly();
            curTerritory = globalTerritory;
            String query = CrmQueries.Tickets.getIncidents(case_status, case_state, 6);

            ArrayList<Requests.Argument> args = new ArrayList<>();
            args.add(new Requests.Argument("query", query));
            Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);

            Crm crm = new Crm();
            crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
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

                adapter = new BasicObjectRecyclerAdapter(getContext(), objects);
                listview.setLayoutManager(new LinearLayoutManager(getContext()));
                listview.setAdapter(adapter);
                adapter.setClickListener(((view, position) -> {
                    BasicObjects.BasicObject object = objects.get(position);
                    Tickets.Ticket ticket = (Tickets.Ticket) object.object;
                    Intent intent = new Intent(getContext(), BasicEntityActivity.class);
                    intent.putExtra(BasicEntityActivity.GSON_STRING, ticket.toBasicEntity().toGson());
                    intent.putExtra(BasicEntityActivity.ENTITYID, ticket.entityid);
                    intent.putExtra(BasicEntityActivity.CURRENT_TERRITORY, globalTerritory);
                    intent.putExtra(BasicEntityActivity.ENTITY_LOGICAL_NAME, "incident");
                    intent.putExtra(BasicEntityActivity.ACTIVITY_TITLE, "Ticket " + ticket.ticketnumber);
                    startActivityForResult(intent, REQUEST_BASIC);

                    try {
                        MileBuddyMetrics.updateMetric(getContext(), MileBuddyMetrics.MetricName.LAST_OPENED_TICKET, DateTime.now());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));

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
            Log.i(TAG, "onResume ");
        }

        @Override
        public void onStart() {
            super.onStart();
            IntentFilter filter = new IntentFilter(MENU_SELECTION);
            filter.addAction(MENU_SELECTION);
            requireActivity().registerReceiver(casesReceiver, filter);
            requireActivity().registerReceiver(territoryChangeReceiver, new IntentFilter(MENU_SELECTION));
            Log.i(TAG, "onStart | Registered the cases receivers");
        }

        @Override
        public void onStop() {
            super.onStop();
            requireActivity().unregisterReceiver(casesReceiver);
            requireActivity().unregisterReceiver(territoryChangeReceiver);
            Log.i(TAG, "onStop | Unregistered the cases receivers");
        }

        @Override
        public void onPause() {
            super.onPause();
            Log.i(TAG, "onPause ");
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            Log.i(TAG, "onDestroyView ");
        }
    }

    public static class Frag_Accounts extends Fragment {
        @NonNls
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
            refreshLayout.setOnRefreshListener(refreshLayout1 -> getAccounts());
            super.onCreateView(inflater, container, savedInstanceState);

            txtNoAccounts = rootView.findViewById(R.id.txtNoAgreements);

            fab = rootView.findViewById(R.id.floatingActionButton);
            fab.setVisibility(View.GONE); // Nothing to filter (yet) so hide.

            accountsReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Received month and year broadcast! (cases frag)");
                    Objects.requireNonNull(mViewPager.getAdapter()).notifyDataSetChanged();
                    getAccounts();
                }
            };

            layoutFilter = rootView.findViewById(R.id.layoutFilter);

            // Text filter for accounts
            txtFilter = rootView.findViewById(R.id.edittextFilter);
            txtFilter.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

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
                            try { // Lazy try/catch - I guess I am concerned with account number being null.
                                if (account.accountName.toLowerCase().contains(filter) || account.accountnumber.contains(filter)) {
                                    BasicObjects.BasicObject object = new BasicObjects.BasicObject(
                                            account.accountnumber,
                                            account.customerTypeFormatted,
                                            account);
                                    object.middleText = account.accountName;
                                    object.iconResource = R.drawable.maps_hospital_32x37;
                                    adapter.mData.add(object);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
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

                // I spent some time debating myself whether or not to call the blanket notifyDataSetChanged()
                // method as opposed to coming up with a more targeted way to add/remove items from the
                // adapter's dataset and I decided that would be unnecessarily complex.
                @SuppressLint("NotifyDataSetChanged")
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
                    if (intent.getParcelableExtra(TERRITORY_RESULT) != null) {
                        curTerritory = globalTerritory;
                    }
                }
            };

            if (options.hasCachedAccounts()) {
                accounts = options.getCachedAccounts();
                objects = accounts.toBasicObjects();
            }

            if (objects == null || objects.size() == 0) {
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

            String query = CrmQueries.Accounts.getAccounts(null);

            ArrayList<Requests.Argument> args = new ArrayList<>();
            args.add(new Requests.Argument("query", query));
            Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);

            Crm crm = new Crm();
            crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody);
                    Log.i(TAG, "onSuccess " + response);
                    accounts = new CrmEntities.Accounts(response);
                    options.cacheAccounts(accounts);
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

            if (accounts != null) {
                for (CrmEntities.Accounts.Account account : accounts.list) {
                    // Add the ticket as a BasicObject
                    BasicObjects.BasicObject object = new BasicObjects.BasicObject(account.accountnumber, account.customerTypeFormatted, account);
                    object.middleText = account.accountName;
                    object.iconResource = R.drawable.customer2;
                    objects.add(object);
                }
            }

            adapter = new BasicObjectRecyclerAdapter(getContext(), objects);
            listview.setLayoutManager(new LinearLayoutManager(getContext()));
            listview.setAdapter(adapter);
            adapter.setClickListener(((view, position) -> {
                BasicObjects.BasicObject object = objects.get(position);
                CrmEntities.Accounts.Account account = (CrmEntities.Accounts.Account) object.object;

                Intent intent = new Intent(getContext(), Activity_AccountData.class);
                intent.setAction(Activity_AccountData.GO_TO_ACCOUNT);
                intent.putExtra(Activity_AccountData.GO_TO_ACCOUNT_OBJECT, account);
                startActivity(intent);
            }));

            txtNoAccounts.setVisibility(objects.size() == 0 ? View.VISIBLE : View.GONE);

            try {
                refreshLayout.finishRefresh();
                layoutFilter.setEnabled(true);
                txtFilter.setEnabled(true);
                if (mViewPager.getCurrentItem() == SectionsPagerAdapter.ACCOUNTS_PAGE) {
                    Helpers.Application.showKeyboard(txtFilter, getContext());
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
            requireActivity().registerReceiver(accountsReceiver, filter);
            requireActivity().registerReceiver(territoryChangedReceiver, new IntentFilter(MENU_SELECTION));
            Log.i(TAG, "onResume Registered the cases receiver");
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            requireActivity().unregisterReceiver(accountsReceiver);
            requireActivity().unregisterReceiver(territoryChangedReceiver);
            Log.i(TAG, "onPause Unregistered the cases receiver");
        }
    }

    public static class Frag_ServiceAgreements extends Fragment {
        @NonNls
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
            fab.setOnClickListener(view -> {
                Intent intent = new Intent(FAB_CLICKED);
                requireActivity().sendBroadcast(intent);
            });
            refreshLayout.setOnRefreshListener(refreshLayout -> getServiceAgreements());
            super.onCreateView(inflater, container, savedInstanceState);

            agreementFilterReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Received an agreements filter broadcast! (service agreements frag)");
                    Objects.requireNonNull(mViewPager.getAdapter()).notifyDataSetChanged();
                    getServiceAgreements();
                }
            };

            territoryChangedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getParcelableExtra(TERRITORY_RESULT) != null) {
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
            crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
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

                for (CrmEntities.ServiceAgreements.ServiceAgreement agreement : serviceAgreements.list) {

                    // Add the ticket as a BasicObject
                    BasicObjects.BasicObject object =
                            new BasicObjects.BasicObject(
                                    agreement._msus_customer_valueFormattedValue
                                    , getString(
                                        R.string.service_agreement_basic_object_bottom_text,
                                        agreement._msus_product_valueFormattedValue,
                                        agreement.msus_serialnumber)
                                    , agreement);
                    object.middleText = getString(R.string.service_agreement_basic_object_middle_text);
                    object.iconResource = R.drawable.contract32;
                    objects.add(object);
                }
            }

            adapter = new BasicObjectRecyclerAdapter(getContext(), objects);
            listview.setLayoutManager(new LinearLayoutManager(getContext()));
            listview.setAdapter(adapter);
            adapter.setClickListener(((view, position) -> {
                BasicObjects.BasicObject object = objects.get(position);
                CrmEntities.ServiceAgreements.ServiceAgreement agreement = (CrmEntities.ServiceAgreements.ServiceAgreement) object.object;
                Intent intent = new Intent(getContext(), BasicEntityActivity.class);
                intent.putExtra(BasicEntityActivity.GSON_STRING, agreement.toBasicEntity().toGson());
                intent.putExtra(BasicEntityActivity.ENTITYID, agreement.entityid);
                // intent.putExtra(BasicEntityActivity.HIDE_MENU, true);
                intent.putExtra(BasicEntityActivity.CURRENT_TERRITORY, globalTerritory);
                intent.putExtra(BasicEntityActivity.ENTITY_LOGICAL_NAME, "msus_medistimserviceagreement");
                intent.putExtra(BasicEntityActivity.ACTIVITY_TITLE, "Service agreement " + agreement.msus_name);
                startActivityForResult(intent, REQUEST_BASIC);
            }));

            txtNoAgreements.setVisibility(objects.size() == 0 ? View.VISIBLE : View.GONE);

            Objects.requireNonNull(refreshLayout.finishRefresh());
        }

        @Override
        public void onResume() {
            super.onResume();
            Log.i(TAG, "onResume ");
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onStart() {
            super.onStart();
            IntentFilter filter = new IntentFilter(MENU_SELECTION);
            filter.addAction(MENU_SELECTION);
            requireActivity().registerReceiver(agreementFilterReceiver, filter);
            requireActivity().registerReceiver(territoryChangedReceiver, new IntentFilter(MENU_SELECTION));
            Log.i(TAG, "onStart | Registered the cases receiver");
        }

        @Override
        public void onStop() {
            super.onStop();
            requireActivity().unregisterReceiver(agreementFilterReceiver);
            requireActivity().unregisterReceiver(territoryChangedReceiver);
            Log.i(TAG, "onStop | Unregistered the cases receiver");
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            Log.i(TAG, "onDestroyView ");
        }
    }


    // endregion
}
