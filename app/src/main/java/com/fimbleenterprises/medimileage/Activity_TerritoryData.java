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
import android.os.Bundle;
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
import android.widget.ProgressBar;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.MaterialHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

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
import cn.pedant.SweetAlert.SweetAlertDialog;
import cz.msebera.android.httpclient.Header;

public class Activity_TerritoryData extends AppCompatActivity
{

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
    public static MySettingsHelper options;
    ProgressBar prog;

    // Receivers for date range changes at the activity level
    public static IntentFilter intentFilter;
    public static BroadcastReceiver goalsReceiverMtd;
    public static BroadcastReceiver goalsReceiverYtd;
    public static BroadcastReceiver salesLinesReceiver;
    public static BroadcastReceiver casesReceiver;
    public static BroadcastReceiver opportunitiesReceiver;

    // vars for the date ranges
    public static int monthNum;
    public static int yearNum;

    public final static String TAG = "TerritoryData";
    public static final String TAG_TITLE = "TAG_TITLE";
    public static final String TAG_DATE = "TAG_DATE";
    public static final String TAG_DISTANCE = "TAG_DISTANCE";
    public static final String TAG_TO_LOC = "TAG_TO_MARKER";
    public static final String TAG_FROM_LOC = "TAG_FROM_MARKER";
    public static final String TAG_TO_TITLE = "TAG_TO_TITLE";
    public static final String TAG_FROM_TITLE = "TAG_FROM_TITLE";
    public static final String DATE_CHANGED = "DATE_CHANGED";
    public static final String MONTH = "MONTH";
    public static final String YEAR = "YEAR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = this;
        activity = this;
        intentFilter = new IntentFilter(DATE_CHANGED);

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
                /*Log.i(TAG, "onScrollChange scrollX: " + scrollX + " scrollY: " + scrollY + "" +
                        "oldScrollX: " + oldScrollX + " oldScrollY: " + oldScrollY);
                Log.i(TAG, "onScrollChange Page: " + mViewPager.currentPosition);*/


            }
        });



        fragMgr = getSupportFragmentManager();

        if (savedInstanceState != null) {

        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // monthNum = DateTime.now().getMonthOfYear();
        // yearNum = DateTime.now().getYear();

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

        switch (mViewPager.currentPosition) {
            case 2 :
                menu.findItem(R.id.action_this_month).setVisible(false);
                menu.findItem(R.id.action_last_year).setVisible(false);
                menu.findItem(R.id.action_choose_month).setVisible(false);

                menu.findItem(R.id.action_this_year).setVisible(true);
                menu.findItem(R.id.action_last_year).setVisible(true);
                break;
            default:
                menu.findItem(R.id.action_choose_month).setVisible(true);
                menu.findItem(R.id.action_last_month).setVisible(true);
                menu.findItem(R.id.action_this_month).setVisible(true);

                menu.findItem(R.id.action_this_year).setVisible(false);
                menu.findItem(R.id.action_last_year).setVisible(false);
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
            case R.id.action_this_month :
                dateChanged = new Intent(DATE_CHANGED);
                dateChanged.putExtra(MONTH, now.getMonthOfYear());
                dateChanged.putExtra(YEAR, now.getYear());
                sendBroadcast(dateChanged);
                break;
            case R.id.action_last_month :
                dateChanged = new Intent(DATE_CHANGED);
                dateChanged.putExtra(MONTH, aMonthAgo.getMonthOfYear());
                dateChanged.putExtra(YEAR, aMonthAgo.getYear());
                sendBroadcast(dateChanged);
                break;
            case R.id.action_choose_month :
                showMonthYearPicker();
                break;
            case R.id.action_this_year :
                dateChanged = new Intent(DATE_CHANGED);
                dateChanged.putExtra(YEAR, now.getYear());
                sendBroadcast(dateChanged);
                break;
            case R.id.action_last_year :
                dateChanged = new Intent(DATE_CHANGED);
                dateChanged.putExtra(YEAR, now.minusYears(1));
                sendBroadcast(dateChanged);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void showMonthYearDialog() {
        final Dialog dialog = new Dialog(this);
        final Context c = this;
        dialog.setContentView(R.layout.make_receipt);
        dialog.setCancelable(true);
        Button btnThisMonth = dialog.findViewById(R.id.btnThisMonth);
        Button btnLastMonth = dialog.findViewById(R.id.btnLastMonth);
        Button btnChoose = dialog.findViewById(R.id.btnChooseMonth);
        btnThisMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent dateChanged = new Intent(DATE_CHANGED);
                DateTime now = DateTime.now();
                dateChanged.putExtra(MONTH, now.getMonthOfYear());
                dateChanged.putExtra(YEAR, now.getYear());
                sendBroadcast(dateChanged);
                dialog.dismiss();
            }
        });
        btnLastMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent dateChanged = new Intent(DATE_CHANGED);
                DateTime now = DateTime.now();
                DateTime aMonthAgo = now.minusMonths(1);
                dateChanged.putExtra(MONTH, aMonthAgo.getMonthOfYear());
                dateChanged.putExtra(YEAR, aMonthAgo.getYear());
                sendBroadcast(dateChanged);
                dialog.dismiss();
            }
        });
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMonthYearPicker();
                dialog.dismiss();
            }
        });
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
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
        dialog.show();
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

            switch (position) {
                case 0:
                    return "Sales Lines";
                case 1:
                    return "MTD Goals";
                case 2:
                    return "YTD Goals";
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
        ArrayList<CrmEntities.OrderProduct> allOrders = new ArrayList<>();

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            root = inflater.inflate(R.layout.frag_saleslines, container, false);
            refreshLayout = root.findViewById(R.id.refreshLayout);
            RefreshLayout refreshLayout = (RefreshLayout) root.findViewById(R.id.refreshLayout);
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
        public void onResume() {
            getActivity().registerReceiver(salesLinesReceiver, intentFilter);
            Log.i(TAG, "onResume Registered the sales lines receiver");
            super.onResume();
        }

        @Override
        public void onPause() {
            getActivity().unregisterReceiver(salesLinesReceiver);
            Log.i(TAG, "onPause Unregistered the sales lines receiver");
            super.onPause();
        }

        protected void getSalesLines() {
            refreshLayout.autoRefreshAnimationOnly();

            String query = null;

            if (monthNum == DateTime.now().getMonthOfYear()) {
                query = Queries.OrderLines.getOrderLines(MediUser.getMe().territoryid,
                        Queries.Operators.DateOperator.THIS_MONTH);
            } else if (monthNum == DateTime.now().minusMonths(1).getMonthOfYear()) {
                query = Queries.OrderLines.getOrderLines(MediUser.getMe().territoryid,
                        Queries.Operators.DateOperator.LAST_MONTH);
            }

            ArrayList<Requests.Argument> args = new ArrayList<>();
            Requests.Argument argument = new Requests.Argument("query", query);
            args.add(argument);
            Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);
            Crm crm = new Crm();
            crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody);
                    Log.i(TAG, "onSuccess " + response);
                    allOrders = new CrmEntities.OrderProducts(response).list;
                    populateList();
                    refreshLayout.finishRefresh();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.w(TAG, "onFailure: " + error.getLocalizedMessage());

                    refreshLayout.finishRefresh();
                }
            });
        }

        protected void populateList() {

            ArrayList<CrmEntities.OrderProduct> orderList = new ArrayList<>();

            boolean addedTodayHeader = false;
            boolean addedYesterdayHeader = false;
            boolean addedThisWeekHeader = false;
            boolean addedThisMonthHeader = false;
            boolean addedOlderHeader = false;

            // Now today's date attributes
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
                        CrmEntities.OrderProduct headerObj = new CrmEntities.OrderProduct();
                        headerObj.isSeparator = true;
                        headerObj.setTitle("Today");
                        orderList.add(headerObj);
                        addedTodayHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'Today' - This will not be added again!");
                    }
                    // Trip was yesterday
                } else if (tripDayOfYear == (todayDayOfYear - 1)) {
                    if (addedYesterdayHeader == false) {
                        CrmEntities.OrderProduct headerObj = new CrmEntities.OrderProduct();
                        headerObj.isSeparator = true;
                        headerObj.setTitle("Yesterday");
                        orderList.add(headerObj);
                        addedYesterdayHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'Yesterday' - This will not be added again!");
                    }

                    // Trip was this week
                } else if (tripWeekOfYear == todayWeekOfYear) {
                    if (addedThisWeekHeader == false) {
                        CrmEntities.OrderProduct headerObj = new CrmEntities.OrderProduct();
                        headerObj.isSeparator = true;
                        headerObj.setTitle("This week");
                        orderList.add(headerObj);
                        addedThisWeekHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'This week' - This will not be added again!");
                    }

                    // Trip was this month
                } else if (tripMonthOfYear == todayMonthOfYear) {
                    if (addedThisMonthHeader == false) {
                        CrmEntities.OrderProduct headerObj = new CrmEntities.OrderProduct();
                        headerObj.isSeparator = true;
                        headerObj.setTitle("This month");
                        orderList.add(headerObj);
                        addedThisMonthHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'This month' - This will not be added again!");
                    }

                    // Trip was older than this month
                } else if (tripMonthOfYear < todayMonthOfYear) {
                    if (addedOlderHeader == false) {
                        CrmEntities.OrderProduct headerObj = new CrmEntities.OrderProduct();
                        headerObj.isSeparator = true;
                        headerObj.setTitle("Last month and older");
                        orderList.add(headerObj);
                        addedOlderHeader = true;
                        Log.d(TAG + "getAllFullTrips", "Added a header object to the array that will eventually be a header childView in the list view named, 'Older' - This will not be added again!");
                    }
                }

                CrmEntities.OrderProduct orderProduct = allOrders.get(i);
                orderList.add(orderProduct);
            }

            Log.i(TAG, "populateTripList Finished preparing the dividers and trips.");

            adapter = new OrderLineRecyclerAdapter(getContext(), orderList);
            adapter.setClickListener(new OrderLineRecyclerAdapter.ItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {

                }
            });
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);

            refreshLayout.finishRefresh();
        }
    }

    public static class Frag_Goals_MTD extends Fragment {
        private View rootView;
        public static final String ARG_SECTION_NUMBER = "section_number";
        // ProgressBar pbLoading;
        /*Cartesian bar;
        AnyChartView anyChartView;*/
        AnyChartView mtdChartView;
        RefreshLayout refreshLayout;
        Cartesian bar;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.frag_sales_mtd, container, false);
            /*bar = AnyChart.bar();
            anyChartView = (AnyChartView) rootView.findViewById(R.id.chartMtd);
            anyChartView.setChart(bar);*/
            goalsReceiverMtd = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Received month and year broadcast! (goals MTD frag)");
                    monthNum = intent.getIntExtra(MONTH, DateTime.now().getMonthOfYear());
                    yearNum = intent.getIntExtra(YEAR, DateTime.now().getYear());
                    getMtdGoalsByRegion();
                }
            };

            mtdChartView = rootView.findViewById(R.id.chartMtd);

            refreshLayout = (RefreshLayout) rootView.findViewById(R.id.refreshLayout);
            refreshLayout.setRefreshHeader(new MaterialHeader(getContext()));
            refreshLayout.setRefreshFooter(new ClassicsFooter(getContext()));
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshlayout) {
                    getMtdGoalsByRegion();
                }
            });
            refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore(RefreshLayout refreshlayout) {
                    refreshlayout.finishLoadMore(500/*,false*/);
                }
            });

            if (monthNum == 0) {
                monthNum = DateTime.now().getMonthOfYear();
            }

            if (yearNum == 0) {
                yearNum = DateTime.now().getYear();
            }

            bar = AnyChart.bar();
            mtdChartView = rootView.findViewById(R.id.chartMtd);
            mtdChartView.setChart(bar);

            getMtdGoalsByRegion();
            return rootView;
        }

        @Override
        public void onResume() {
            getActivity().registerReceiver(goalsReceiverMtd, intentFilter);
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
            refreshLayout.autoRefreshAnimationOnly();

            String query = Queries.Goals.getMtdGoalsByRegion(MediUser.getMe().salesregionid, monthNum, yearNum);
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
                    refreshLayout.finishRefresh();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.w(TAG, "onFailure: " + error.getLocalizedMessage());
                    // pbLoading.setVisibility(View.GONE);
                    refreshLayout.finishRefresh();
                }
            });
        }

        void populateChartMtd(CrmEntities.Goals goals) {

            List<DataEntry> data = new ArrayList<>();
            for (int i = 0; i < goals.list.size(); i++) {
                CrmEntities.Goal goal = goals.list.get(i);
                GoalSummary goalSummary = goals.list.get(i).getGoalSummary(goal.getStartDateForMonthlyGoal(), goal.getEndDateForMonthlyGoal(), DateTime.now());
                data.add(new ValueDataEntry(goalSummary.goal.ownername, goalSummary.getPctAcheivedAsOfToday()));
            }

            bar.data(data);

            String title = "MTD Goals " + MediUser.getMe().salesregionname + " Region (month: " + monthNum
                    + " year: " + yearNum + ")";

            bar.title(title);
            mtdChartView.invalidate(); // refresh

            /*bar.title(title);
            bar.labels(true);
            bar.labels().selectable(true);
            bar.labels().enabled(true);
            bar.data(data);*/

        } // END ONCREATEVIEW

    }

    public static class Frag_Goals_YTD extends Fragment {
        private View rootView;
        public static final String ARG_SECTION_NUMBER = "section_number";
        // ProgressBar pbLoading;
        /*Cartesian bar;
        AnyChartView anyChartView;*/
        AnyChartView ytdChartView;
        RefreshLayout refreshLayout;
        Cartesian bar;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.frag_sales_ytd, container, false);
            /*bar = AnyChart.bar();
            anyChartView = (AnyChartView) rootView.findViewById(R.id.chartMtd);
            anyChartView.setChart(bar);*/
            goalsReceiverYtd = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Received month and year broadcast! (goals YTD frag)");
                    yearNum = intent.getIntExtra(YEAR, DateTime.now().getYear());
                    getYtdGoalsByRegion();
                }
            };

            ytdChartView = rootView.findViewById(R.id.chartYtd);

            refreshLayout = (RefreshLayout) rootView.findViewById(R.id.refreshLayout);
            refreshLayout.setRefreshHeader(new MaterialHeader(getContext()));
            refreshLayout.setRefreshFooter(new ClassicsFooter(getContext()));
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshlayout) {
                    getYtdGoalsByRegion();
                }
            });
            refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore(RefreshLayout refreshlayout) {
                    refreshlayout.finishLoadMore(500/*,false*/);
                }
            });

            if (monthNum == 0) {
                monthNum = DateTime.now().getMonthOfYear();
            }

            if (yearNum == 0) {
                yearNum = DateTime.now().getYear();
            }

            bar = AnyChart.bar();
            ytdChartView = rootView.findViewById(R.id.chartYtd);
            ytdChartView.setChart(bar);

            getYtdGoalsByRegion();
            return rootView;
        }

        @Override
        public void onResume() {
            getActivity().registerReceiver(goalsReceiverYtd, intentFilter);
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
            refreshLayout.autoRefreshAnimationOnly();

            String query = Queries.Goals.getYtdGoalsByRegion(MediUser.getMe().salesregionid,  yearNum);
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
                    refreshLayout.finishRefresh();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.w(TAG, "onFailure: " + error.getLocalizedMessage());
                    // pbLoading.setVisibility(View.GONE);
                    refreshLayout.finishRefresh();
                }
            });
        }

        void populateChartYtd(CrmEntities.Goals goals) {

            List<DataEntry> data = new ArrayList<>();
            for (int i = 0; i < goals.list.size(); i++) {
                CrmEntities.Goal goal = goals.list.get(i);
                GoalSummary goalSummary = goals.list.get(i).getGoalSummary(goal.getStartDate(), goal.getEndDate(), DateTime.now());
                data.add(new ValueDataEntry(goalSummary.goal.ownername, goalSummary.getPctAcheivedAsOfToday()));
            }

            bar.data(data);

            String title = "YTD Goals " + MediUser.getMe().salesregionname + " Region (year: " + yearNum + ")";

            bar.title(title);
            ytdChartView.invalidate(); // refresh

            /*bar.title(title);
            bar.labels(true);
            bar.labels().selectable(true);
            bar.labels().enabled(true);
            bar.data(data);*/

        } // END ONCREATEVIEW

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
            getActivity().registerReceiver(opportunitiesReceiver, intentFilter);
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
            getActivity().registerReceiver(casesReceiver, intentFilter);
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
