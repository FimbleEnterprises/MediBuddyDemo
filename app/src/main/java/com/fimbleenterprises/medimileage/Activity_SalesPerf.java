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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.chart.common.listener.Event;
import com.anychart.chart.common.listener.ListenersInterface;
import com.anychart.charts.Cartesian;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
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
import androidx.viewpager.widget.PagerTitleStrip;
import cn.pedant.SweetAlert.SweetAlertDialog;
import cz.msebera.android.httpclient.Header;

public class Activity_SalesPerf extends AppCompatActivity
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
    public static IntentFilter intentFilter;
    public static BroadcastReceiver receiver;

    public static int monthNum;
    public static int yearNum;

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
    public static final String DATE_CHANGED = "DATE_CHANGED";
    public static final String MONTH = "MONTH";
    public static final String YEAR = "YEAR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        activity = this;
        intentFilter = new IntentFilter(DATE_CHANGED);

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
                Log.i(TAG, "onScrollChange scrollX: " + scrollX + " scrollY: " + scrollY + "" +
                        "oldScrollX: " + oldScrollX + " oldScrollY: " + oldScrollY);
                Log.i(TAG, "onScrollChange Page: " + mViewPager.currentPosition);
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
        registerReceiver(receiver, intentFilter);
        super.onResume();
    }

    @Override
    public void onPause() {
        unregisterReceiver(receiver);
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent dateChanged;
        DateTime now = DateTime.now();
        switch (item.getItemId()) {
            case R.id.action_this_month :
                dateChanged = new Intent(DATE_CHANGED);
                dateChanged.putExtra(MONTH, now.getMonthOfYear());
                dateChanged.putExtra(YEAR, now.getYear());
                sendBroadcast(dateChanged);
                break;
            case R.id.action_last_month :
                dateChanged = new Intent(DATE_CHANGED);
                DateTime aMonthAgo = now.minusMonths(1);
                dateChanged.putExtra(MONTH, aMonthAgo.getMonthOfYear());
                dateChanged.putExtra(YEAR, aMonthAgo.getYear());
                sendBroadcast(dateChanged);
                break;
            case R.id.action_choose_month :
                showMonthYearPicker();
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
                Fragment fragment = new Frag_MtdRegion();
                Bundle args = new Bundle();
                args.putInt(Frag_MtdRegion.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }

            if (position == 1) {
                Fragment fragment = new Frag_YtdRegion();
                Bundle args = new Bundle();
                args.putInt(Frag_YtdRegion.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
            }
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
                    return "MTD Goals";
                case 1:
                    return "YTD Goals";
                case 2:
                    return "";
                case 3:
                    return "";
                case 4:
                    return "";
            }
            return null;
        }
    }

    //region ********************************** FRAGS *****************************************

    public static class Frag_MtdRegion extends Fragment {
        private View rootView;
        public static final String ARG_SECTION_NUMBER = "section_number";
        // ProgressBar pbLoading;
        /*Cartesian bar;
        AnyChartView anyChartView;*/
        BarChart barChart;
        RefreshLayout refreshLayout;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive Received month and year broadcast!");
                    monthNum = intent.getIntExtra(MONTH, DateTime.now().getMonthOfYear());
                    yearNum = intent.getIntExtra(YEAR, DateTime.now().getYear());
                    getMtdGoalsByRegion();
                }
            };
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.frag_sales_mtd, container, false);
            /*bar = AnyChart.bar();
            anyChartView = (AnyChartView) rootView.findViewById(R.id.chartMtd);
            anyChartView.setChart(bar);*/

            barChart = rootView.findViewById(R.id.chart);

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

            getMtdGoalsByRegion();
            return rootView;
        }

        @Override
        public void onResume() {
            getActivity().registerReceiver(receiver, intentFilter);
            super.onResume();
        }

        @Override
        public void onPause() {
            // getActivity().unregisterReceiver(receiver);
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

            // List<DataEntry> data = new ArrayList<>();
            ArrayList<BarEntry> data = new ArrayList();

            for (int i = 0; i < goals.list.size(); i++) {
                CrmEntities.Goal goal = goals.list.get(i);
                GoalSummary goalSummary = goals.list.get(i).getGoalSummary(goal.getStartDate(), goal.getEndDate(), DateTime.now());
                data.add(new BarEntry(i, goalSummary.getPctAcheivedAsOfToday()));
            }

            BarDataSet dataset = new BarDataSet(data,"hi");

            ArrayList<String> labels = new ArrayList<String>();
            for (int i = 0; i < goals.list.size(); i++) {
                CrmEntities.Goal goal = goals.list.get(i);
                labels.add(goal.ownername);
            }
            BarData barData = new BarData((IBarDataSet) labels, dataset);

            barChart.setData(barData);
            barChart.animateXY(2000, 2000);
            barChart.invalidate();

            String title = "MTD Goals " + MediUser.getMe().salesregionname + " Region (month: " + monthNum
                    + " year: " + yearNum + ")";
            /*bar.title(title);
            bar.labels(true);
            bar.labels().selectable(true);
            bar.labels().enabled(true);
            bar.data(data);*/

        } // END ONCREATEVIEW

    } // END FRAGMENT

    public static class Frag_YtdRegion extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        public View rootView;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.man_trip_from, container, false);
            super.onCreateView(inflater, container, savedInstanceState);


            return rootView;
        }

    }

/*    public static class Frag_Submit extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        private View rootView;
        Button btnSubmit;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.man_trip_submit, container, false);
            btnSubmit = rootView.findViewById(R.id.btn_submit);
            btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveTrip();
                }
            });
            return rootView;
        }

        private boolean validateEntries() {
            boolean isValid = true;

            if (title.getText().length() == 0) {
                Toast.makeText(context, "Please name this trip.", Toast.LENGTH_SHORT).show();
                mViewPager.setCurrentItem(0, true);
                return false;
            }

            if (fromLatLng == null) {
                Toast.makeText(context, "Please set your starting location", Toast.LENGTH_SHORT).show();
                mViewPager.setCurrentItem(1, true);
                return false;
            }

            if (toLatLng == null) {
                Toast.makeText(context, "Please set your destination", Toast.LENGTH_SHORT).show();
                mViewPager.setCurrentItem(2, true);
                return false;
            }

            if (date.getText().length() == 0) {
                Toast.makeText(context, "Please set the date of this trip", Toast.LENGTH_SHORT).show();
                mViewPager.setCurrentItem(3, true);
                return false;
            }

            if (distance.getText().length() == 0) {
                Toast.makeText(context, "Please set the distance travelled", Toast.LENGTH_SHORT).show();
                mViewPager.setCurrentItem(4, true);
                return false;
            }

            return isValid;
        }

        private void saveTrip() {

            if (options.getReimbursementRate() == 0) {

            }

            try {
                // Instantiate a datasource
                MySqlDatasource ds = new MySqlDatasource();
                MediUser user = MediUser.getMe();
                FullTrip fullTrip = new FullTrip(date.getDateSelectedAsDateTime().getMillis(),user.domainname, user.systemuserid, user.email);

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

                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();

            } catch (Exception e) {
                e.printStackTrace();
                getActivity().setResult(Activity.RESULT_CANCELED);
                getActivity().finish();
            }
        }
    }*/

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

}
