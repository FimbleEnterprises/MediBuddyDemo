package com.fimbleenterprises.medimileage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

/*import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;*/
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.MaterialHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import static com.fimbleenterprises.medimileage.CrmEntities.OrderProducts;
import static com.fimbleenterprises.medimileage.CrmEntities.OrderProducts.OrderProduct;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
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

    // Receivers for date range changes at the activity level
    public static IntentFilter intentFilterMonthYear;
    public static BroadcastReceiver goalsReceiverMtd;
    public static BroadcastReceiver goalsReceiverYtd;
    public static BroadcastReceiver salesLinesReceiver;
    public static BroadcastReceiver casesReceiver;
    public static BroadcastReceiver opportunitiesReceiver;

    // vars for the date ranges
    public static int monthNum;
    public static int yearNum;

    // var for region
    public static boolean isEastRegion = true;

    // var for territoryid
    public static Territory territory;

    // The popup dialog for goals represented by the chart.
    public static Dialog chartPopupDialog;

    public final static String TAG = "TerritoryData";
    public static final String DATE_CHANGED = "DATE_CHANGED";
    public static final String MONTH = "MONTH";
    public static final String YEAR = "YEAR";

    public int curPageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = this;
        activity = this;
        intentFilterMonthYear = new IntentFilter(DATE_CHANGED);

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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            territory = data.getParcelableExtra(FullscreenActivityChooseTerritory.TERRITORY_RESULT);
            sectionsPagerAdapter.notifyDataSetChanged();

            Intent dateChanged = new Intent(DATE_CHANGED);
            dateChanged.putExtra(YEAR, yearNum);
            dateChanged.putExtra(MONTH, monthNum);
            sendBroadcast(dateChanged);
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
        getMenuInflater().inflate(R.menu.sales_menu, menu);

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
                break;
            case 1 : // MTD
                menu.findItem(R.id.action_this_year).setVisible(false);
                menu.findItem(R.id.action_last_year).setVisible(false);
                menu.findItem(R.id.action_choose_territory).setVisible(false);

                menu.findItem(R.id.action_west_region).setVisible(true);
                menu.findItem(R.id.action_east_region).setVisible(true);
                menu.findItem(R.id.action_this_month).setVisible(true);
                menu.findItem(R.id.action_last_month).setVisible(true);
                menu.findItem(R.id.action_choose_month).setVisible(true);
                break;
            case 2 : // YTD
                menu.findItem(R.id.action_this_month).setVisible(false);
                menu.findItem(R.id.action_last_month).setVisible(false);
                menu.findItem(R.id.action_choose_month).setVisible(false);
                menu.findItem(R.id.action_choose_territory).setVisible(false);

                menu.findItem(R.id.action_west_region).setVisible(true);
                menu.findItem(R.id.action_east_region).setVisible(true);
                menu.findItem(R.id.action_this_year).setVisible(true);
                menu.findItem(R.id.action_last_year).setVisible(true);
                break;

        }

        return super.onPreparePanel(featureId, view, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent dateChanged;
        DateTime now = DateTime.now();
        DateTime aMonthAgo = now.minusMonths(1);
        switch (item.getItemId()) {
            case R.id.action_choose_territory :
                Intent intent = new Intent(context, FullscreenActivityChooseTerritory.class);
                intent.putExtra(FullscreenActivityChooseTerritory.CURRENT_TERRITORY, territory);
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
        }

        return super.onOptionsItemSelected(item);
    }

    public static void destroyChartDialogIfVisible() {
        if (chartPopupDialog != null && chartPopupDialog.isShowing()) {
            chartPopupDialog.dismiss();
        }
    }

    public static void sendBroadcastUsingExistingValues() {
        Intent dateChanged = dateChanged = new Intent(DATE_CHANGED);
        dateChanged.putExtra(MONTH, monthNum);
        dateChanged.putExtra(YEAR, yearNum);
        activity.sendBroadcast(dateChanged);
    }

    public static String getRegionid() {
        if (isEastRegion) {
            return Queries.EAST_REGIONID;
        } else {
            return Queries.WEST_REGIONID;
        }
    }

    public static String getRegionName() {
        if (isEastRegion) {
            return "East Region";
        } else {
            return "West Region";
        }
    }

    @SuppressLint("NewApi")
    private void showMonthYearPicker() {
        final MonthYearPickerDialog mpd = new MonthYearPickerDialog();
        mpd.setListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Intent dateChanged = new Intent(DATE_CHANGED);
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
                Fragment fragment = new Frag_Goals_MTD();
                Bundle args = new Bundle();
                args.putInt(Frag_Goals_MTD.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == 2) {
                Fragment fragment = new Frag_Goals_YTD();
                Bundle args = new Bundle();
                args.putInt(Frag_Goals_YTD.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == 3) {
                Fragment fragment = new Frag_Opportunities();
                Bundle args = new Bundle();
                args.putInt(Frag_Opportunities.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == 4) {
                Fragment fragment = new Frag_Cases();
                Bundle args = new Bundle();
                args.putInt(Frag_Opportunities.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            curPageIndex = position;

            switch (position) {
                case 0:
                    return "Sales Lines (" + territory.territoryName + ")";
                case 1:
                    return "MTD Goals by Region";
                case 2:
                    return "YTD Goals by Region";
                case 3:
                    return "Opportunities";
                case 4:
                    return "Cases";
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

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            root = inflater.inflate(R.layout.frag_saleslines, container, false);
            txtNoSales = root.findViewById(R.id.txtNoSales);
            refreshLayout = root.findViewById(R.id.refreshLayout);
            RefreshLayout refreshLayout = root.findViewById(R.id.refreshLayout);
            refreshLayout.setRefreshHeader(new MaterialHeader(getContext()));
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

            getSalesLines();

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
            getActivity().registerReceiver(salesLinesReceiver, intentFilterMonthYear);

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

    public static class Frag_Goals_MTD extends Fragment {
        private View rootView;
        public static final String ARG_SECTION_NUMBER = "section_number";
        // ProgressBar pbLoading;
        /*Cartesian bar;
        AnyChartView anyChartView;*/
        RefreshLayout mtdRefreshLayout;
        HorizontalBarChart chartMtd;
        TextView txtChartTitle;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

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
                    refreshlayout.finishLoadMore(1/*,false*/);
                }
            });

            if (monthNum == 0) {
                monthNum = DateTime.now().getMonthOfYear();
            }

            if (yearNum == 0) {
                yearNum = DateTime.now().getYear();
            }

            getMtdGoalsByRegion();
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
            getActivity().unregisterReceiver(goalsReceiverMtd);
            Log.i(TAG, "onPause Unregistered the goals receiver");
            super.onPause();
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
                    CrmEntities.Goals goals = new CrmEntities.Goals(response);
                    Log.i(TAG, "onSuccess " + response);
                    populateChartMtd(goals);
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

        void populateChartMtd(final CrmEntities.Goals goals) {

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
        /*Cartesian bar;
        AnyChartView anyChartView;*/
        RefreshLayout ytdRefreshLayout;
        TextView txtChartTitle;
        HorizontalBarChart chartYtd;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

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
                    refreshlayout.finishLoadMore(1/*,false*/);
                }
            });

            if (monthNum == 0) {
                monthNum = DateTime.now().getMonthOfYear();
            }

            if (yearNum == 0) {
                yearNum = DateTime.now().getYear();
            }

            getYtdGoalsByRegion();
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
            getActivity().unregisterReceiver(goalsReceiverYtd);
            Log.i(TAG, "onPause Unregistered the goals receiver");
            super.onPause();
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
                    CrmEntities.Goals goals = new CrmEntities.Goals(response);
                    Log.i(TAG, "onSuccess " + response);
                    populateChartYtd(goals);
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

        void populateChartYtd(final CrmEntities.Goals goals) {

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

    public static class Frag_Opportunities extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View rootView;
        public RecyclerView listview;
        RefreshLayout refreshLayout;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public void onPause() {
            super.onPause();
            getActivity().unregisterReceiver(opportunitiesReceiver);
            Log.i(TAG, "onResume Unregistered opportunities receiver");
        }

        @Override
        public void onResume() {
            super.onResume();
            getActivity().registerReceiver(opportunitiesReceiver, intentFilterMonthYear);
            Log.i(TAG, "onResume Registered opportunities receiver");
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.frag_saleslines, container, false);
            listview = rootView.findViewById(R.id.opportunitiesRecyclerview);
            refreshLayout = rootView.findViewById(R.id.refreshLayout);

            super.onCreateView(inflater, container, savedInstanceState);
            opportunitiesReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Received month and year broadcast! (opportunities frag)");
                    monthNum = intent.getIntExtra(MONTH, DateTime.now().getMonthOfYear());
                    yearNum = intent.getIntExtra(YEAR, DateTime.now().getYear());
                }
            };


            return rootView;
        }

    }

    public static class Frag_Cases extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View rootView;
        public RecyclerView listview;
        RefreshLayout refreshLayout;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.frag_saleslines, container, false);
            listview = rootView.findViewById(R.id.casesRecyclerview);
            refreshLayout = rootView.findViewById(R.id.refreshLayout);
            super.onCreateView(inflater, container, savedInstanceState);

            casesReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Received month and year broadcast! (cases frag)");
                    monthNum = intent.getIntExtra(MONTH, DateTime.now().getMonthOfYear());
                    yearNum = intent.getIntExtra(YEAR, DateTime.now().getYear());
                }
            };

            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            getActivity().registerReceiver(casesReceiver, intentFilterMonthYear);
            Log.i(TAG, "onResume Registered the cases receiver");
        }

        @Override
        public void onPause() {
            super.onPause();
            getActivity().unregisterReceiver(casesReceiver);
            Log.i(TAG, "onPause Unregistered the cases receiver");
        }
    }

}
