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
import android.os.Bundle;
import android.text.InputType;
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
import com.fimbleenterprises.medimileage.objects_and_containers.BasicObjects;
import com.fimbleenterprises.medimileage.dialogs.ContactActions;
import com.fimbleenterprises.medimileage.Crm;
import com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities;
import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.objects_and_containers.MediUser;
import com.fimbleenterprises.medimileage.objects_and_containers.MileBuddyMetrics;
import com.fimbleenterprises.medimileage.dialogs.MonthYearPickerDialog;
import com.fimbleenterprises.medimileage.MyInterfaces;
import com.fimbleenterprises.medimileage.MyPreferencesHelper;
import com.fimbleenterprises.medimileage.ui.CustomViews.MyUnderlineEditText;
import com.fimbleenterprises.medimileage.MyViewPager;
import com.fimbleenterprises.medimileage.adapters.OrderLineRecyclerAdapter;
import com.fimbleenterprises.medimileage.CrmQueries;
import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.objects_and_containers.Requests;
import com.fimbleenterprises.medimileage.objects_and_containers.Territory;
import com.fimbleenterprises.medimileage.fullscreen_pickers.FullscreenActivityChooseTerritory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import static com.fimbleenterprises.medimileage.activities.BasicEntityActivity.ENTITYID;
import static com.fimbleenterprises.medimileage.activities.BasicEntityActivity.ENTITY_LOGICAL_NAME;
import static com.fimbleenterprises.medimileage.activities.BasicEntityActivity.REQUEST_BASIC;
import static com.fimbleenterprises.medimileage.activities.BasicEntityActivity.SEND_EMAIL;
import static com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities.OrderProducts;
import static com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities.OrderProducts.OrderProduct;
import static com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities.Tickets.NOT_RESOLVED;

import org.joda.time.DateTime;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerTitleStrip;
import cz.msebera.android.httpclient.Header;

public class Activity_TerritoryData extends AppCompatActivity {
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
    public ArrayList<Territory> cachedTerritories = new ArrayList<>();
    SearchView searchView;

    public static final String MENU_SELECTION = "MENU_SELECTION";

    // Intent filters for the various fragment's broadcast receivers.
    // public static IntentFilter intentFilterMonthYear;

    // vars for the date ranges
    public static int monthNum;
    public static int yearNum;

    // vars for case status
    public static int case_status = NOT_RESOLVED;

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

        globalTerritory = new Territory();
        globalTerritory.territoryid = MediUser.getMe().territoryid;
        globalTerritory.territoryName = MediUser.getMe().territoryname;
        monthNum = DateTime.now().getMonthOfYear();
        yearNum = DateTime.now().getYear();

        setContentView(R.layout.activity_sales_perf);

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (MyViewPager) findViewById(R.id.main_pager_yo_sales_perf);
        mViewPager.onRealPageChangedListener = new MyViewPager.OnRealPageChangedListener() {
            @Override
            public void onPageActuallyFuckingChanged(int pageIndex) {
                setTitle(sectionsPagerAdapter.getPageTitle(pageIndex));
            }
        };
        mPagerStrip = (PagerTitleStrip) findViewById(R.id.pager_title_strip_sales_perf);
        mViewPager.setAdapter(sectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(0);
        mViewPager.setCurrentItem(options.getDefaultTerritoryPage());
        mViewPager.setPageCount(6);
        mViewPager.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                destroyChartDialogIfVisible();
            }
        });
        fragMgr = getSupportFragmentManager();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (searchView != null && !searchView.isIconified()) {
                searchView.setIconified(true);
            } else {
                onBackPressed();
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
            case SectionsPagerAdapter.SALES_PAGE: // Sales lines
                menu.findItem(R.id.action_west_region).setVisible(false);
                menu.findItem(R.id.action_east_region).setVisible(false);
                menu.findItem(R.id.action_this_year).setVisible(false);
                menu.findItem(R.id.action_last_year).setVisible(false);

                menu.findItem(R.id.action_this_month).setVisible(true);
                menu.findItem(R.id.action_last_month).setVisible(true);
                menu.findItem(R.id.action_choose_month).setVisible(true);
                menu.findItem(R.id.action_choose_territory).setVisible(true);

                menu.findItem(R.id.action_case_status).setVisible(false);

                menu.findItem(R.id.action_account_type).setVisible(false);
                break;
            case SectionsPagerAdapter.LEADS_PAGE: // Sales lines
            case SectionsPagerAdapter.OPPORTUNITIES_PAGE: // Opportunities
            case SectionsPagerAdapter.ACCOUNTS_PAGE: // Accounts
                menu.findItem(R.id.action_west_region).setVisible(false);
                menu.findItem(R.id.action_east_region).setVisible(false);
                menu.findItem(R.id.action_this_year).setVisible(false);
                menu.findItem(R.id.action_last_year).setVisible(false);

                menu.findItem(R.id.action_this_month).setVisible(false);
                menu.findItem(R.id.action_last_month).setVisible(false);
                menu.findItem(R.id.action_choose_month).setVisible(false);
                menu.findItem(R.id.action_choose_territory).setVisible(true);

                menu.findItem(R.id.action_case_status).setVisible(false);

                menu.findItem(R.id.action_account_type).setVisible(false);
                break;

            case SectionsPagerAdapter.CASES_PAGE: // Cases
                menu.findItem(R.id.action_west_region).setVisible(false);
                menu.findItem(R.id.action_east_region).setVisible(false);
                menu.findItem(R.id.action_this_year).setVisible(false);
                menu.findItem(R.id.action_last_year).setVisible(false);

                menu.findItem(R.id.action_this_month).setVisible(false);
                menu.findItem(R.id.action_last_month).setVisible(false);
                menu.findItem(R.id.action_choose_month).setVisible(false);
                menu.findItem(R.id.action_choose_territory).setVisible(true);

                menu.findItem(R.id.action_account_type).setVisible(false);

                menu.findItem(R.id.action_case_status).setVisible(true);
                break;

        }

        // Set case status values
        switch (case_status) {
            case NOT_RESOLVED :
                menu.findItem(R.id.action_change_case_status_not_resolved).setChecked(true);
                menu.findItem(R.id.action_change_case_status_inprogress).setChecked(false);
                menu.findItem(R.id.action_change_case_status_on_hold).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_inspected).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_for_product).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_customer).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_billed).setChecked(false);
                break;
            case CrmEntities.Tickets.IN_PROGRESS :
                menu.findItem(R.id.action_change_case_status_not_resolved).setChecked(false);
                menu.findItem(R.id.action_change_case_status_inprogress).setChecked(true);
                menu.findItem(R.id.action_change_case_status_on_hold).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_inspected).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_for_product).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_customer).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_billed).setChecked(false);
                break;
            case CrmEntities.Tickets.ON_HOLD :
                menu.findItem(R.id.action_change_case_status_not_resolved).setChecked(false);
                menu.findItem(R.id.action_change_case_status_inprogress).setChecked(false);
                menu.findItem(R.id.action_change_case_status_on_hold).setChecked(true);
                menu.findItem(R.id.action_change_case_status_to_be_inspected).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_for_product).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_customer).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_billed).setChecked(false);
                break;
            case CrmEntities.Tickets.TO_BE_INSPECTED :
                menu.findItem(R.id.action_change_case_status_not_resolved).setChecked(false);
                menu.findItem(R.id.action_change_case_status_inprogress).setChecked(false);
                menu.findItem(R.id.action_change_case_status_on_hold).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_inspected).setChecked(true);
                menu.findItem(R.id.action_change_case_status_waiting_for_product).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_customer).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_billed).setChecked(false);
                break;
            case CrmEntities.Tickets.WAITING_FOR_PRODUCT :
                menu.findItem(R.id.action_change_case_status_not_resolved).setChecked(false);
                menu.findItem(R.id.action_change_case_status_inprogress).setChecked(false);
                menu.findItem(R.id.action_change_case_status_on_hold).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_inspected).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_for_product).setChecked(true);
                menu.findItem(R.id.action_change_case_status_waiting_on_customer).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_billed).setChecked(false);
                break;
            case CrmEntities.Tickets.WAITING_ON_CUSTOMER :
                menu.findItem(R.id.action_change_case_status_not_resolved).setChecked(false);
                menu.findItem(R.id.action_change_case_status_inprogress).setChecked(false);
                menu.findItem(R.id.action_change_case_status_on_hold).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_inspected).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_for_product).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_customer).setChecked(true);
                menu.findItem(R.id.action_change_case_status_to_be_billed).setChecked(false);
                break;
            case CrmEntities.Tickets.TO_BE_BILLED :
                menu.findItem(R.id.action_change_case_status_not_resolved).setChecked(false);
                menu.findItem(R.id.action_change_case_status_inprogress).setChecked(false);
                menu.findItem(R.id.action_change_case_status_on_hold).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_inspected).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_for_product).setChecked(false);
                menu.findItem(R.id.action_change_case_status_waiting_on_customer).setChecked(false);
                menu.findItem(R.id.action_change_case_status_to_be_billed).setChecked(true);
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
                intent.putExtra(FullscreenActivityChooseTerritory.CACHED_TERRITORIES, cachedTerritories);
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
            case R.id.action_change_case_status_not_resolved:
                case_status = NOT_RESOLVED;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_change_case_status_inprogress:
                case_status = CrmEntities.Tickets.IN_PROGRESS;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_change_case_status_on_hold:
                case_status = CrmEntities.Tickets.ON_HOLD;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_change_case_status_to_be_inspected:
                case_status = CrmEntities.Tickets.TO_BE_INSPECTED;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_change_case_status_waiting_for_product:
                case_status = CrmEntities.Tickets.WAITING_FOR_PRODUCT;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_change_case_status_waiting_on_customer:
                case_status = CrmEntities.Tickets.WAITING_ON_CUSTOMER;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_change_case_status_to_be_billed:
                case_status = CrmEntities.Tickets.TO_BE_BILLED;
                sendMenuSelectionBroadcast();
                break;
            case R.id.action_change_case_status_problem_solved:
                case_status = CrmEntities.Tickets.PROBLEM_SOLVED;
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

        public static final int SALES_PAGE = 0;
        public static final int LEADS_PAGE= 1;
        public static final int OPPORTUNITIES_PAGE = 2;
        public static final int CASES_PAGE = 3;
        public static final int ACCOUNTS_PAGE = 4;

        public SectionsPagerAdapter(androidx.fragment.app.FragmentManager fm) {
            super(fm);
            sectionsPagerAdapter = this;
        }

        @Override
        public Fragment getItem(int position) {

            Log.d("getItem", "Creating Fragment in pager at index: " + position);
            Log.w(TAG, "getItem: PAGER POSITION: " + position);

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

            return null;
        }

        @Override
        public int getCount() {
            return 5;
        }
        
        public String getCaseCriteriaTitleAddendum() {
            switch (case_status) {
                case CrmEntities.Tickets.NOT_RESOLVED :
                    return " - Not resolved";
                case CrmEntities.Tickets.IN_PROGRESS :
                    return " - In progress";
                case CrmEntities.Tickets.ON_HOLD :
                    return " - On hold";
                case CrmEntities.Tickets.TO_BE_BILLED :
                    return " - To be billed";
                case CrmEntities.Tickets.TO_BE_INSPECTED :
                    return " - To be inspected";
                case CrmEntities.Tickets.WAITING_ON_CUSTOMER :
                    return " - Waiting on customer";
                case CrmEntities.Tickets.WAITING_ON_REP :
                    return " - Waiting on rep";
                case CrmEntities.Tickets.WAITING_FOR_PRODUCT :
                    return " - Waiting for product";
                case CrmEntities.Tickets.PROBLEM_SOLVED :
                    return " - Problem solved";
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            curPageIndex = position;

            if (globalTerritory == null) {
                globalTerritory = MediUser.getMe().getTerritory();
            }

            switch (position) {
                case SALES_PAGE:
                    return "Sales Lines (" + globalTerritory.territoryName + ")";
                case LEADS_PAGE:
                    return "Leads (" + globalTerritory.territoryName + ")";
                case OPPORTUNITIES_PAGE:
                    return "Opportunities (" + globalTerritory.territoryName + ")";
                case CASES_PAGE:
                    return "Cases (" + globalTerritory.territoryName + ")" + getCaseCriteriaTitleAddendum();
                case ACCOUNTS_PAGE:
                    return "Accounts (" + globalTerritory.territoryName + ")";
            }
            return null;
        }
    }

    //region ********************************** FRAGS *****************************************
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
        BroadcastReceiver salesLinesReceiver;

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

            salesLinesReceiver = new BroadcastReceiver() {
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

            return root;
        }

        @Override
        public void onStop() {
            super.onStop();

        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            getActivity().unregisterReceiver(salesLinesReceiver);
            getActivity().unregisterReceiver(territoryChangedReceiver);
            Log.i(TAG, "onPause Unregistered the sales lines receiver");
        }

        @Override
        public void onResume() {
            IntentFilter filter = new IntentFilter(FullscreenActivityChooseTerritory.TERRITORY_RESULT);
            filter.addAction(MENU_SELECTION);
            getActivity().registerReceiver(salesLinesReceiver, filter);
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
                query = CrmQueries.OrderLines.getOrderLines(globalTerritory.territoryid, monthNum);
            }

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

            ArrayList<OrderProducts.OrderProduct> orderList = new ArrayList<>();

            boolean addedTodayHeader = false;
            boolean addedYesterdayHeader = false;
            boolean addedThisWeekHeader = false;
            boolean addedThisMonthHeader = false;
            boolean addedOlderHeader = false;

            // Sorting criteria based on date - these will be used when constructing header rows
            int todayDayOfYear = Helpers.DatesAndTimes.returnDayOfYear(DateTime.now());
            int todayWeekOfYear = Helpers.DatesAndTimes.returnWeekOfYear(DateTime.now());
            int todayMonthOfYear = Helpers.DatesAndTimes.returnMonthOfYear(DateTime.now());

            Log.i(TAG, "populateTripList: Preparing the dividers and trips...");
            for (int i = 0; i < (allOrders.size()); i++) {
                int tripDayOfYear = Helpers.DatesAndTimes.returnDayOfYear(allOrders.get(i).orderDate);
                int tripWeekOfYear = Helpers.DatesAndTimes.returnWeekOfYear(allOrders.get(i).orderDate);
                int tripMonthOfYear = Helpers.DatesAndTimes.returnMonthOfYear(allOrders.get(i).orderDate);

                // Trip was today
                if (tripDayOfYear == todayDayOfYear) {
                    if (addedTodayHeader == false) {
                        OrderProducts.OrderProduct headerObj = new OrderProducts.OrderProduct();
                        headerObj.isSeparator = true;
                        headerObj.setTitle("Today");
                        orderList.add(headerObj);
                        addedTodayHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'Today' - This will not be added again!");
                    }
                    // Trip was yesterday
                } else if (tripDayOfYear == (todayDayOfYear - 1)) {
                    if (addedYesterdayHeader == false) {
                        OrderProduct headerObj = new OrderProduct();
                        headerObj.isSeparator = true;
                        headerObj.setTitle("Yesterday");
                        orderList.add(headerObj);
                        addedYesterdayHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'Yesterday' - This will not be added again!");
                    }

                    // Trip was this week
                } else if (tripWeekOfYear == todayWeekOfYear) {
                    if (addedThisWeekHeader == false) {
                        OrderProduct headerObj = new OrderProduct();
                        headerObj.isSeparator = true;
                        headerObj.setTitle("This week");
                        orderList.add(headerObj);
                        addedThisWeekHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'This week' - This will not be added again!");
                    }

                    // Trip was this month
                } else if (tripMonthOfYear == todayMonthOfYear) {
                    if (addedThisMonthHeader == false) {
                        OrderProduct headerObj = new OrderProduct();
                        headerObj.isSeparator = true;
                        headerObj.setTitle("This month");
                        orderList.add(headerObj);
                        addedThisMonthHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'This month' - This will not be added again!");
                    }

                    // Trip was older than this month
                } else if (tripMonthOfYear < todayMonthOfYear) {
                    if (addedOlderHeader == false) {
                        OrderProduct headerObj = new OrderProduct();
                        headerObj.isSeparator = true;
                        headerObj.setTitle("Last month and older");
                        orderList.add(headerObj);
                        addedOlderHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'Older' - This will not be added again!");
                    }
                }

                OrderProducts.OrderProduct orderProduct = allOrders.get(i);
                orderList.add(orderProduct);
            }

            Log.i(TAG, "populateTripList Finished preparing the dividers and trips.");

            if (!getActivity().isFinishing()) {
                adapter = new OrderLineRecyclerAdapter(getContext(), orderList);
                adapter.setClickListener(new OrderLineRecyclerAdapter.ItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

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
        BroadcastReceiver leadsReceiver;
        BroadcastReceiver territoryChangedReceiver;

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

            // Gifts from Santa - open present and do stuff with what we got!
            leadsReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Opportunities frag received a broadcast!");

                    // User made an options menu selection - receive the details of that choice
                    // via the broadcast sent by the parent activity.
                    if (intent.getAction().equals(MENU_SELECTION)) {
                        if (intent.getParcelableExtra(FullscreenActivityChooseTerritory.TERRITORY_RESULT) != null) {
                            curTerritory = intent.getParcelableExtra(FullscreenActivityChooseTerritory.TERRITORY_RESULT);
                            getLeads();
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
            getActivity().unregisterReceiver(leadsReceiver);
            getActivity().unregisterReceiver(territoryChangedReceiver);
            super.onDestroyView();
        }

        @Override
        public void onResume() {
            super.onResume();
            IntentFilter intentFilterMenuSelection = new IntentFilter(MENU_SELECTION);
            getActivity().registerReceiver(leadsReceiver, intentFilterMenuSelection);
            getActivity().registerReceiver(territoryChangedReceiver, new IntentFilter(MENU_SELECTION));
            Log.i(TAG, "onResume Registered opportunities receiver");
        }

        public void getLeads() {
            refreshLayout.autoRefreshAnimationOnly();
            CrmEntities.Leads.getCrmLeads(context, curTerritory.territoryid, new MyInterfaces.GetLeadsListener() {
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
                    Toast.makeText(context, "Failed to get opportunities!\n" + error, Toast.LENGTH_LONG).show();
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

        void showOpportunityOptions(final CrmEntities.Opportunities.Opportunity opportunity) {

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

        void showAddNoteDialog(final CrmEntities.Opportunities.Opportunity opportunity) {
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
        public ArrayList<Territory> cachedTerritories;
        public Territory curTerritory;
        public CrmEntities.Opportunities opportunities;
        ArrayList<BasicObjects.BasicObject> objects = new ArrayList<>();
        BasicObjectRecyclerAdapter adapter;
        BroadcastReceiver opportunitiesReceiver;

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
            opportunitiesReceiver = new BroadcastReceiver() {
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
            return rootView;
        }

        @Override
        public void onPause() {
            super.onPause();

            Log.i(TAG, "onResume Unregistered opportunities receiver");
        }

        @Override
        public void onDestroyView() {
            getActivity().unregisterReceiver(opportunitiesReceiver);
            super.onDestroyView();
        }

        @Override
        public void onResume() {
            super.onResume();
            IntentFilter intentFilterMenuSelection = new IntentFilter(MENU_SELECTION);
            getActivity().registerReceiver(opportunitiesReceiver, intentFilterMenuSelection);
            Log.i(TAG, "onResume Registered opportunities receiver");
        }

        public void getOpportunities() {
            refreshLayout.autoRefresh();
            CrmEntities.Opportunities.retrieveOpportunities(curTerritory.territoryid, new MyInterfaces.GetOpportunitiesListener() {
                @Override
                public void onSuccess(CrmEntities.Opportunities crmOpportunities) {
                    opportunities = crmOpportunities;
                    populateList();
                    // populateOpportunities();
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
            for (CrmEntities.Opportunities.Opportunity opp : opportunities.list) {
                BasicObjects.BasicObject object = new BasicObjects.BasicObject(opp.name, opp.dealTypePretty, opp);
                object.middleText = opp.accountname;
                object.topRightText = opp.probabilityPretty;
                objects.add(object);
            }

            adapter = new BasicObjectRecyclerAdapter(context, objects);
            listview.setLayoutManager(new LinearLayoutManager(context));
            listview.setAdapter(adapter);
            adapter.setClickListener(new BasicObjectRecyclerAdapter.ItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    CrmEntities.Opportunities.Opportunity selectedOpportunity =
                            (CrmEntities.Opportunities.Opportunity) objects.get(position).object;

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

        void showOpportunityOptions(final CrmEntities.Opportunities.Opportunity opportunity) {

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

        void showAddNoteDialog(final CrmEntities.Opportunities.Opportunity opportunity) {
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
        CrmEntities.Tickets tickets;
        ArrayList<BasicObjects.BasicObject> objects = new ArrayList<>();
        BasicObjectRecyclerAdapter adapter;
        BroadcastReceiver territoryChangeReceiver;
        TextView txtNoTickets;

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

            casesReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Received month and year broadcast! (cases frag)");
                    mViewPager.getAdapter().notifyDataSetChanged();
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
            refreshLayout.autoRefreshAnimationOnly();
            String query = null;

            curTerritory = globalTerritory;

            if (case_status == NOT_RESOLVED) {
                query = CrmQueries.Tickets.getNonResolvedTickets(globalTerritory.territoryid);
            } else {
                query = CrmQueries.Tickets.getTickets(globalTerritory.territoryid, case_status);
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
                    tickets = new CrmEntities.Tickets(response);
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

                int lastStatusCode = -1;

                for (CrmEntities.Tickets.Ticket ticket : tickets.list) {

                    // Logic to create headers denoting newly seen status'
                    if (ticket.statuscode != lastStatusCode) {
                        BasicObjects.BasicObject object = new BasicObjects.BasicObject(ticket.statusFormatted, null, null);
                        object.isHeader = true;
                        objects.add(object);
                        lastStatusCode = ticket.statuscode;
                    }

                    // Add the ticket as a BasicObject
                    BasicObjects.BasicObject object = new BasicObjects.BasicObject(ticket.title, ticket.ticketnumber, ticket);
                    object.middleText = ticket.customerFormatted;
                    object.topRightText = Helpers.DatesAndTimes.getPrettyDateAndTime(ticket.modifiedon);
                    object.bottomRightText = ticket.statusFormatted;
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
                    CrmEntities.Tickets.Ticket ticket = (CrmEntities.Tickets.Ticket) object.object;
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

            accountsReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Received month and year broadcast! (cases frag)");
                    mViewPager.getAdapter().notifyDataSetChanged();
                    getAccounts();
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
                getAccounts();
            } else {
                populateList();
            }

            txtNoAccounts = rootView.findViewById(R.id.txtNoAccounts);

            return rootView;
        }

        void getAccounts() {
            refreshLayout.autoRefreshAnimationOnly();

            String query = CrmQueries.Accounts.getAccountsByTerritory(globalTerritory.territoryid);

            ArrayList<Requests.Argument> args = new ArrayList<>();
            args.add(new Requests.Argument("query", query));
            Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);

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

            objects.clear();

            if (accounts != null) {

                int lastStatusCode = -1;

                for (CrmEntities.Accounts.Account account : accounts.list) {

                    // Add the ticket as a BasicObject
                    BasicObjects.BasicObject object = new BasicObjects.BasicObject(account.accountnumber, account.customerTypeFormatted, account);
                    object.middleText = account.accountName;
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


    // endregion
}