package com.fimbleenterprises.medimileage;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.scwang.smart.refresh.header.MaterialHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import static com.fimbleenterprises.medimileage.BasicEntityActivity.ENTITYID;
import static com.fimbleenterprises.medimileage.BasicEntityActivity.ENTITY_LOGICAL_NAME;
import static com.fimbleenterprises.medimileage.BasicEntityActivity.REQUEST_BASIC;
import static com.fimbleenterprises.medimileage.BasicEntityActivity.SEND_EMAIL;
import static com.fimbleenterprises.medimileage.CrmEntities.OrderProducts;
import static com.fimbleenterprises.medimileage.CrmEntities.OrderProducts.OrderProduct;
import static com.fimbleenterprises.medimileage.CrmEntities.Tickets.NOT_RESOLVED;

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
    public static MySettingsHelper options;
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

    // flag for region
    public static boolean isEastRegion = true;

    // var for territory shared with all fragments that want or need it
    public static Territory territory;

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

        // Log a metric
        MileBuddyMetrics.updateMetric(this, MileBuddyMetrics.MetricName.LAST_ACCESSED_TERRITORY_DATA, DateTime.now());

        territory = new Territory();
        territory.territoryid = MediUser.getMe().territoryid;
        territory.territoryName = MediUser.getMe().territoryname;
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
        mViewPager.setCurrentItem(0);
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
                            emailSuffix = "\n\nCRM Link: " + recordurl;
                            Log.i(TAG, "onActivityResult:: " + recordurl);

                        } else if (entityLogicalName.equals("incident")) {
                            recordurl = Crm.getRecordUrl(entityid, Integer.toString(Crm.ETC_INCIDENT));
                            emailSuffix = "\n\nCRM Link: " + recordurl;
                            subject = "Ticket";
                            Log.i(TAG, "onActivityResult:: " + recordurl);
                        }

                        Helpers.Email.sendEmail(emailSuffix + "\n\n", "CRM Link", activity);

                    }

                }
            }

            territory = data.getParcelableExtra(FullscreenActivityChooseTerritory.TERRITORY_RESULT);
            cachedTerritories = data.getParcelableArrayListExtra(FullscreenActivityChooseTerritory.CACHED_TERRITORIES);
            sectionsPagerAdapter.notifyDataSetChanged();

            Intent intent = new Intent(MENU_SELECTION);
            intent.setAction(MENU_SELECTION);
            intent.putExtra(FullscreenActivityChooseTerritory.TERRITORY_RESULT, territory);
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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
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
            searchView.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onPreparePanel(int featureId, @Nullable View view, @NonNull Menu menu) {

        menu.findItem(R.id.action_east_region).setChecked(isEastRegion);
        menu.findItem(R.id.action_west_region).setChecked(!isEastRegion);

        switch (mViewPager.currentPosition) {
            case 0 : // Sales lines
                menu.findItem(R.id.action_west_region).setVisible(false);
                menu.findItem(R.id.action_east_region).setVisible(false);
                menu.findItem(R.id.action_this_year).setVisible(false);
                menu.findItem(R.id.action_last_year).setVisible(false);

                menu.findItem(R.id.action_this_month).setVisible(true);
                menu.findItem(R.id.action_last_month).setVisible(true);
                menu.findItem(R.id.action_choose_month).setVisible(true);
                menu.findItem(R.id.action_choose_territory).setVisible(true);

                menu.findItem(R.id.action_case_status).setVisible(false);
                break;
            case 1 : // Opportunities
                menu.findItem(R.id.action_west_region).setVisible(false);
                menu.findItem(R.id.action_east_region).setVisible(false);
                menu.findItem(R.id.action_this_year).setVisible(false);
                menu.findItem(R.id.action_last_year).setVisible(false);

                menu.findItem(R.id.action_this_month).setVisible(false);
                menu.findItem(R.id.action_last_month).setVisible(false);
                menu.findItem(R.id.action_choose_month).setVisible(false);
                menu.findItem(R.id.action_choose_territory).setVisible(true);

                menu.findItem(R.id.action_case_status).setVisible(false);
                break;
            case 2 : // Cases
                menu.findItem(R.id.action_west_region).setVisible(false);
                menu.findItem(R.id.action_east_region).setVisible(false);
                menu.findItem(R.id.action_this_year).setVisible(false);
                menu.findItem(R.id.action_last_year).setVisible(false);

                menu.findItem(R.id.action_this_month).setVisible(false);
                menu.findItem(R.id.action_last_month).setVisible(false);
                menu.findItem(R.id.action_choose_month).setVisible(false);
                menu.findItem(R.id.action_choose_territory).setVisible(true);

                menu.findItem(R.id.action_case_status).setVisible(true);

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
                intent.putExtra(FullscreenActivityChooseTerritory.CURRENT_TERRITORY, territory);
                intent.putExtra(FullscreenActivityChooseTerritory.CACHED_TERRITORIES, cachedTerritories);
                startActivityForResult(intent, 0);
                break;
            case R.id.action_east_region :
                isEastRegion = true;
                sendBroadcastUsingExistingValues();
                break;
            case R.id.action_west_region :
                isEastRegion = false;
                sendBroadcastUsingExistingValues();
                break;
            case R.id.action_this_month :
                monthNum = now.getMonthOfYear();
                yearNum = now.getYear();
                sendBroadcastUsingExistingValues();
                break;
            case R.id.action_last_month :
                monthNum = aMonthAgo.getMonthOfYear();
                yearNum = aMonthAgo.getYear();
                sendBroadcastUsingExistingValues();
                break;
            case R.id.action_choose_month :
                showMonthYearPicker();
                break;
            case R.id.action_this_year :
                yearNum = now.getYear();
                sendBroadcastUsingExistingValues();
                break;
            case R.id.action_last_year :
                yearNum = aMonthAgo.getYear();
                sendBroadcastUsingExistingValues();
                break;
            case R.id.action_change_case_status_not_resolved:
                case_status = NOT_RESOLVED;
                sendBroadcastUsingExistingValues();
                break;
            case R.id.action_change_case_status_inprogress:
                case_status = CrmEntities.Tickets.IN_PROGRESS;
                sendBroadcastUsingExistingValues();
                break;
            case R.id.action_change_case_status_on_hold:
                case_status = CrmEntities.Tickets.ON_HOLD;
                sendBroadcastUsingExistingValues();
                break;
            case R.id.action_change_case_status_to_be_inspected:
                case_status = CrmEntities.Tickets.TO_BE_INSPECTED;
                sendBroadcastUsingExistingValues();
                break;
            case R.id.action_change_case_status_waiting_for_product:
                case_status = CrmEntities.Tickets.WAITING_FOR_PRODUCT;
                sendBroadcastUsingExistingValues();
                break;
            case R.id.action_change_case_status_waiting_on_customer:
                case_status = CrmEntities.Tickets.WAITING_ON_CUSTOMER;
                sendBroadcastUsingExistingValues();
                break;
            case R.id.action_change_case_status_to_be_billed:
                case_status = CrmEntities.Tickets.TO_BE_BILLED;
                sendBroadcastUsingExistingValues();
                break;
            case R.id.action_change_case_status_problem_solved:
                case_status = CrmEntities.Tickets.PROBLEM_SOLVED;
                sendBroadcastUsingExistingValues();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void destroyChartDialogIfVisible() {
        if (chartPopupDialog != null && chartPopupDialog.isShowing()) {
            chartPopupDialog.dismiss();
        }
    }

    public static void sendBroadcastUsingExistingValues() {
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

        public SectionsPagerAdapter(androidx.fragment.app.FragmentManager fm) {
            super(fm);
            sectionsPagerAdapter = this;
        }

        @Override
        public Fragment getItem(int position) {

            Log.d("getItem", "Creating Fragment in pager at index: " + position);
            Log.w(TAG, "getItem: PAGER POSITION: " + position);

            if (position == 0) {
                Fragment fragment = new Frag_SalesLines();
                Bundle args = new Bundle();
                args.putInt(Frag_SalesLines.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == 1) {
                Fragment fragment = new Frag_Opportunities();
                Bundle args = new Bundle();
                args.putInt(Frag_Opportunities.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == 2) {
                Fragment fragment = new Frag_Cases();
                Bundle args = new Bundle();
                args.putInt(Frag_Opportunities.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            /*if (position == 3) {
                Fragment fragment = new Frag_Goals_MTD();
                Bundle args = new Bundle();
                args.putInt(Frag_Goals_MTD.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == 4) {
                Fragment fragment = new Frag_Goals_YTD();
                Bundle args = new Bundle();
                args.putInt(Frag_Goals_YTD.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }*/
            return null;
        }

        @Override
        public int getCount() {
            return 3;
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

            if (territory == null) {
                territory = MediUser.getMe().getTerritory();
            }

            switch (position) {
                case 0:
                    return "Sales Lines (" + territory.territoryName + ")";
                case 1:
                    return "Opportunities (" + territory.territoryName + ")";
                case 2:
                    return "Cases (" + territory.territoryName + ")" + getCaseCriteriaTitleAddendum();
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
        ArrayList<OrderProducts.OrderProduct> allOrders = new ArrayList<>();
        TextView txtNoSales;
        BroadcastReceiver salesLinesReceiver;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            root = inflater.inflate(R.layout.frag_saleslines, container, false);
            txtNoSales = root.findViewById(R.id.txtNoSales);
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

            if (allOrders == null || allOrders.size() == 0) {
                getSalesLines();
            } else {
                populateList();
            }

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
            Log.i(TAG, "onPause Unregistered the sales lines receiver");
        }

        @Override
        public void onResume() {
            IntentFilter filter = new IntentFilter(FullscreenActivityChooseTerritory.TERRITORY_RESULT);
            filter.addAction(MENU_SELECTION);
            getActivity().registerReceiver(salesLinesReceiver, filter);

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
                query = Queries.OrderLines.getOrderLines(territory.territoryid,
                        Queries.Operators.DateOperator.THIS_MONTH);
            } else if (monthNum == DateTime.now().minusMonths(1).getMonthOfYear()) {
                query = Queries.OrderLines.getOrderLines(territory.territoryid,
                        Queries.Operators.DateOperator.LAST_MONTH);
            } else {
                query = Queries.OrderLines.getOrderLines(territory.territoryid, monthNum);
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

    public static class Frag_Opportunities extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View rootView;
        public RecyclerView listview;
        RefreshLayout refreshLayout;
        public ArrayList<Territory> cachedTerritories;
        public Territory curTerritory;
        public CrmEntities.Opportunities opportunities;
        ArrayList<BasicObject> objects = new ArrayList<>();
        BasicObjectRecyclerAdapter adapter;
        BroadcastReceiver opportunitiesReceiver;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.frag_opportunities, container, false);
            listview = rootView.findViewById(R.id.opportunitiesRecyclerview);
            refreshLayout = rootView.findViewById(R.id.refreshLayout);

            refreshLayout.setEnableLoadMore(false);
            super.onCreateView(inflater, container, savedInstanceState);

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

            if (opportunities == null && curTerritory != null) {
                getOpportunities();
            } else {
                populateList();
            }

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
                BasicObject object = new BasicObject(opp.name, opp.dealTypePretty, opp);
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
                    intent.putExtra(BasicEntityActivity.ENTITYID, selectedOpportunity.opportunityid);
                    intent.putExtra(BasicEntityActivity.ENTITY_LOGICAL_NAME, "opportunity");
                    intent.putExtra(BasicEntityActivity.GSON_STRING, selectedOpportunity.toBasicEntity().toGson());
                    startActivityForResult(intent, BasicEntityActivity.REQUEST_BASIC);
                }
            });
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
            txtStatus.setText(opportunity.status);
            txtDealStatus.setText(opportunity.dealStatus);
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
            CrmEntities.Annotations.showAddNoteDialog(context, opportunity.opportunityid, new MyInterfaces.CrmRequestListener() {
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
        CrmEntities.Tickets tickets;
        ArrayList<BasicObject> objects = new ArrayList<>();
        BasicObjectRecyclerAdapter adapter;
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

            if (objects == null || objects.size() == 0) {
                getTickets();
            } else {
                populateList();
            }

            txtNoTickets = rootView.findViewById(R.id.txtNoTickets);

            return rootView;
        }

        void getTickets() {
            refreshLayout.autoRefreshAnimationOnly();
            String query = null;

            if (case_status == NOT_RESOLVED) {
                query = Queries.Tickets.getNonResolvedTickets(territory.territoryid);
            } else {
                query = Queries.Tickets.getTickets(territory.territoryid, case_status);
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
                        BasicObject object = new BasicObject(ticket.statusFormatted, null, null);
                        object.isHeader = true;
                        objects.add(object);
                        lastStatusCode = ticket.statuscode;
                    }

                    // Add the ticket as a BasicObject
                    BasicObject object = new BasicObject(ticket.title, ticket.ticketnumber, ticket);
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
                    BasicObject object = objects.get(position);
                    CrmEntities.Tickets.Ticket ticket = (CrmEntities.Tickets.Ticket) object.object;
                    Intent intent = new Intent(context, BasicEntityActivity.class);
                    intent.putExtra(BasicEntityActivity.GSON_STRING, ticket.toBasicEntity().toGson());
                    intent.putExtra(BasicEntityActivity.ENTITYID, ticket.ticketid);
                    intent.putExtra(BasicEntityActivity.ENTITY_LOGICAL_NAME, "incident");
                    intent.putExtra(BasicEntityActivity.ACTIVITY_TITLE, "Ticket " + ticket.ticketnumber);
                    startActivityForResult(intent, REQUEST_BASIC);
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
            Log.i(TAG, "onPause Unregistered the cases receiver");
        }
    }

/*
public static class Frag_Goals_MTD extends Fragment {
        private View rootView;
        public static final String ARG_SECTION_NUMBER = "section_number";
        // ProgressBar pbLoading;
        *//*Cartesian bar;
        AnyChartView anyChartView;*//*
        RefreshLayout mtdRefreshLayout;
        HorizontalBarChart chartMtd;
        TextView txtChartTitle;
        CrmEntities.Goals goals;
        BroadcastReceiver goalsReceiverMtd;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.frag_sales_mtd, container, false);

            txtChartTitle = rootView.findViewById(R.id.txtChartTitle);
            chartMtd = rootView.findViewById(R.id.chartMtd);

            goalsReceiverMtd = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Received month and year broadcast! (goals MTD frag)");
                    monthNum = intent.getIntExtra(MONTH, DateTime.now().getMonthOfYear());
                    yearNum = intent.getIntExtra(YEAR, DateTime.now().getYear());
                    getMtdGoalsByRegion();
                }
            };

            mtdRefreshLayout = (RefreshLayout) rootView.findViewById(R.id.mtdRefreshLayout);
            mtdRefreshLayout.setRefreshHeader(new MaterialHeader(getContext()));
            mtdRefreshLayout.setRefreshFooter(new ClassicsFooter(getContext()));
            mtdRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshlayout) {
                    getMtdGoalsByRegion();
                }
            });
            mtdRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore(RefreshLayout refreshlayout) {
                    refreshlayout.finishLoadMore(1*//*,false*//*);
                }
            });

            if (monthNum == 0) {
                monthNum = DateTime.now().getMonthOfYear();
            }

            if (yearNum == 0) {
                yearNum = DateTime.now().getYear();
            }

            if (goals == null || goals.list.size() == 0) {
                getMtdGoalsByRegion();
            }else {
                populateChartMtd();
            }
            return rootView;
        }

        @Override
        public void onResume() {
            getActivity().registerReceiver(goalsReceiverMtd, intentFilterMonthYear);
            Log.i(TAG, "onResume Registered the goals receiver");
            super.onResume();
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            getActivity().unregisterReceiver(goalsReceiverMtd);
            Log.i(TAG, "onPause Unregistered the goals receiver");
        }

        void getMtdGoalsByRegion() {
            // pbLoading.setVisibility(View.VISIBLE);
            // anyChartView.setVisibility(View.GONE);
            mtdRefreshLayout.autoRefreshAnimationOnly();

            String query = Queries.Goals.getMtdGoalsByRegion(getRegionid(), monthNum, yearNum);
            ArrayList<Requests.Argument> args = new ArrayList<>();
            Requests.Argument argument = new Requests.Argument("query", query);
            args.add(argument);
            Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);
            Crm crm = new Crm();
            crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody);
                    goals = new CrmEntities.Goals(response);
                    Log.i(TAG, "onSuccess " + response);
                    populateChartMtd();
                    mtdRefreshLayout.finishRefresh();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.w(TAG, "onFailure: " + error.getLocalizedMessage());
                    // pbLoading.setVisibility(View.GONE);
                    mtdRefreshLayout.finishRefresh();
                }
            });
        }

        void populateChartMtd() {

            String title = "MTD Goals " + getRegionName() + " (month: " + monthNum + ", year: " + yearNum + ")";
            
            txtChartTitle.setText(title);

            // Start building the containers to hold the goal data
            ArrayList<IBarDataSet> sets = new ArrayList<>();
            ArrayList<BarEntry> entries = new ArrayList<>();

            // Create and populate a container to hold the bar entries, labels and colors
            final ArrayList<String> xAxisLabel = new ArrayList<>();
            for(int i = 0; i < goals.size(); i++) {
                CrmEntities.Goal goal = goals.list.get(i);
                BarEntry entry = new BarEntry(i, goal.pct);
                entries.add(entry);
                xAxisLabel.add(goal.ownername + " (" + goal.territoryname + ")");
            }

            // Build a BarDataSet container and fill it with our data containers
            BarDataSet ds = new BarDataSet(entries, title);
            ds.setColors(ColorTemplate.MATERIAL_COLORS);
            sets.add(ds);
            BarData d = new BarData(sets);

            // Apply the chart data to the chart
            chartMtd.setData(d);

            // Hide the legend
            chartMtd.getLegend().setEnabled(false);

            // Aesthetics
            chartMtd.setDrawValueAboveBar(true);
            chartMtd.animateXY(2000, 2000);

            // Show each label for each entry
            ValueFormatter xAxisFormatter = new ValueFormatter() {
                @Override
                public String getBarLabel(BarEntry barEntry) {
                    return super.getBarLabel(barEntry);
                }
            };

            // Format the entries and labels
            XAxis xAxis = chartMtd.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE); // Where to put the labels
            xAxis.setDrawGridLines(false);
            xAxis.setGranularity(1f); // intervals
            xAxis.setLabelCount(xAxisLabel.size());
            xAxis.setValueFormatter(xAxisFormatter);
            xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
            ValueFormatter formatter = new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return xAxisLabel.get((int) value);
                }
            };
            xAxis.setValueFormatter(formatter);

            // Refresh the chart
            chartMtd.invalidate();

            // Make an onclick listener for chart values
            chartMtd.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    Log.i(TAG, "onValueSelected " + e.getData());

                    // Get the goal represented by the selected entry
                    CrmEntities.Goal selectedGoal = goals.list.get((int) e.getX());

                    // Show the goal summary for the selected entry
                    chartPopupDialog = new Dialog(context);
                    final Context c = context;
                    chartPopupDialog.setContentView(R.layout.generic_app_dialog);
                    chartPopupDialog.setTitle(selectedGoal.ownername);
                    chartPopupDialog.setCancelable(true);
                    final TextView txtMainText = chartPopupDialog.findViewById(R.id.txtMainText);
                    Button btnOkay = chartPopupDialog.findViewById(R.id.btnOkay);
                    btnOkay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            chartPopupDialog.dismiss();
                        }
                    });
                    txtMainText.setText(
                            selectedGoal.ownername + "\n" +
                            "Month: " + selectedGoal.period + ", Year: " + selectedGoal.year + "\n" +
                            "\n" +
                            "Target: " + selectedGoal.getPrettyTarget() + "\n" +
                            "Actual: " + selectedGoal.getPrettyActual() + "\n" +
                            "Percent: " + selectedGoal.getPrettyPct() + "\n"
                    );
                    chartPopupDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
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
                    chartPopupDialog.show();

                }

                @Override
                public void onNothingSelected() {

                }
            }); // end onChartClickListener

        }

    }

    public static class Frag_Goals_YTD extends Fragment {
        private View rootView;
        public static final String ARG_SECTION_NUMBER = "section_number";
        // ProgressBar pbLoading;
        *//*Cartesian bar;
        AnyChartView anyChartView;*//*
        RefreshLayout ytdRefreshLayout;
        TextView txtChartTitle;
        HorizontalBarChart chartYtd;
        CrmEntities.Goals goals;
        BroadcastReceiver goalsReceiverYtd;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.frag_sales_ytd, container, false);
            txtChartTitle = rootView.findViewById(R.id.txtChartTitle);
            chartYtd = (HorizontalBarChart) rootView.findViewById(R.id.chartYtd);
            goalsReceiverYtd = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Received month and year broadcast! (goals YTD frag)");
                    yearNum = intent.getIntExtra(YEAR, DateTime.now().getYear());
                    getYtdGoalsByRegion();
                }
            };

            txtChartTitle = rootView.findViewById(R.id.txtChartTitle);

            ytdRefreshLayout = (RefreshLayout) rootView.findViewById(R.id.ytdRefreshLayout);
            ytdRefreshLayout.setRefreshHeader(new MaterialHeader(getContext()));
            ytdRefreshLayout.setRefreshFooter(new ClassicsFooter(getContext()));
            ytdRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshlayout) {
                    getYtdGoalsByRegion();
                }
            });
            ytdRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore(RefreshLayout refreshlayout) {
                    refreshlayout.finishLoadMore(1*//*,false*//*);
                }
            });

            if (monthNum == 0) {
                monthNum = DateTime.now().getMonthOfYear();
            }

            if (yearNum == 0) {
                yearNum = DateTime.now().getYear();
            }

            if (goals == null || goals.list.size() == 0) {
                getYtdGoalsByRegion();
            } else {
                populateChartYtd();
            }
            return rootView;
        }

        @Override
        public void onResume() {
            getActivity().registerReceiver(goalsReceiverYtd, intentFilterMonthYear);
            Log.i(TAG, "onResume Registered the goals receiver");
            super.onResume();
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            getActivity().unregisterReceiver(goalsReceiverYtd);
            Log.i(TAG, "onPause Unregistered the goals receiver");
        }

        void getYtdGoalsByRegion() {
            // pbLoading.setVisibility(View.VISIBLE);
            // anyChartView.setVisibility(View.GONE);
            ytdRefreshLayout.autoRefreshAnimationOnly();

            String query = Queries.Goals.getYtdGoalsByRegion(getRegionid(),  yearNum);
            ArrayList<Requests.Argument> args = new ArrayList<>();
            Requests.Argument argument = new Requests.Argument("query", query);
            args.add(argument);
            Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);
            Crm crm = new Crm();
            crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody);
                    goals = new CrmEntities.Goals(response);
                    Log.i(TAG, "onSuccess " + response);
                    populateChartYtd();
                    ytdRefreshLayout.finishRefresh();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.w(TAG, "onFailure: " + error.getLocalizedMessage());
                    // pbLoading.setVisibility(View.GONE);
                    ytdRefreshLayout.finishRefresh();
                }
            });
        }

        void populateChartYtd() {

            String title = "YTD Goals " + getRegionName() + " (year: " + yearNum + ")";

            txtChartTitle.setText(title);

            // Start building the containers to hold the goal data
            ArrayList<IBarDataSet> sets = new ArrayList<>();
            ArrayList<BarEntry> entries = new ArrayList<>();

            // Create a container to hold the bar entry labels and populate them
            final ArrayList<String> xAxisLabel = new ArrayList<>();
            for(int i = 0; i < goals.size(); i++) {
                CrmEntities.Goal goal = goals.list.get(i);
                BarEntry entry = new BarEntry(i, goal.pct);
                entries.add(entry);
                xAxisLabel.add(goal.ownername + " (" + goal.territoryname + ")");
            }

            // Create and populate a container to hold the bar entries, labels and colors
            BarDataSet ds = new BarDataSet(entries, title);
            ds.setColors(ColorTemplate.MATERIAL_COLORS);
            sets.add(ds);
            BarData d = new BarData(sets);

            // Apply the chart data to the chart
            chartYtd.setData(d);

            // Hide the legend
            chartYtd.getLegend().setEnabled(false);

            // Aesthetics
            chartYtd.setDrawValueAboveBar(true);
            chartYtd.animateXY(2000, 2000);

            // Show each label for each entry
            ValueFormatter xAxisFormatter = new ValueFormatter() {
                @Override
                public String getBarLabel(BarEntry barEntry) {
                    return super.getBarLabel(barEntry);
                }
            };

            // Format the entries and labels
            XAxis xAxis = chartYtd.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE); // Where to put the labels
            xAxis.setDrawGridLines(false);
            xAxis.setGranularity(1f); // intervals
            xAxis.setLabelCount(xAxisLabel.size());
            xAxis.setValueFormatter(xAxisFormatter);
            xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
            ValueFormatter formatter = new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return xAxisLabel.get((int) value);
                }
            };
            xAxis.setValueFormatter(formatter);

            // Refresh the chart
            chartYtd.invalidate();

            // Make an onclick listener for chart values
            chartYtd.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    Log.i(TAG, "onValueSelected " + e.getData());

                    // Get the goal represented by the selected entry
                    CrmEntities.Goal selectedGoal = goals.list.get((int) e.getX());

                    // Show the goal summary for the selected entry
                    chartPopupDialog = new Dialog(context);
                    final Context c = context;
                    chartPopupDialog.setContentView(R.layout.generic_app_dialog);
                    chartPopupDialog.setTitle(selectedGoal.ownername);
                    chartPopupDialog.setCancelable(true);
                    final TextView txtMainText = chartPopupDialog.findViewById(R.id.txtMainText);
                    Button btnOkay = chartPopupDialog.findViewById(R.id.btnOkay);
                    btnOkay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            chartPopupDialog.dismiss();
                        }
                    });
                    txtMainText.setText(
                            selectedGoal.ownername + "\n" +
                            "Month: " + selectedGoal.period + ", Year: " + selectedGoal.year + "\n" +
                            "\n" +
                            "Target: " + selectedGoal.getPrettyTarget() + "\n" +
                            "Actual: " + selectedGoal.getPrettyActual() + "\n" +
                            "Percent: " + selectedGoal.getPrettyPct() + "\n"
                    );
                    chartPopupDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
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
                    chartPopupDialog.show();

                }

                @Override
                public void onNothingSelected() {

                }
            }); // end onChartClickListener

        }

    }
    */
    // endregion
}
