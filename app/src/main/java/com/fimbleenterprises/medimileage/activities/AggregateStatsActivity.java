package com.fimbleenterprises.medimileage.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import cz.msebera.android.httpclient.Header;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.objects_and_containers.AggregateStats;
import com.fimbleenterprises.medimileage.Crm;
import com.fimbleenterprises.medimileage.objects_and_containers.ExcelSpreadsheet;
import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.objects_and_containers.MileBuddyMetrics;
import com.fimbleenterprises.medimileage.dialogs.MyProgressDialog;
import com.fimbleenterprises.medimileage.QueryFactory;
import com.fimbleenterprises.medimileage.QueryFactory.Filter;
import com.fimbleenterprises.medimileage.QueryFactory.Filter.Operator;
import com.fimbleenterprises.medimileage.QueryFactory.Filter.FilterCondition;
import com.fimbleenterprises.medimileage.QueryFactory.SortClause;
import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.objects_and_containers.Requests;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Utils;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class AggregateStatsActivity extends AppCompatActivity {


    public static final int AGGREGATE_TOTALS_SHEET = 0;
    public static final int THIS_MONTH_SHEET = 1;
    public static final int LAST_MONTH_SHEET = 2;
    public static final int LAST_LAST_MONTH_SHEET = 3;
    public static final int RAW_DATA_SHEET = 4;

    private static final String TAG = "AggregateStats";
    AppCompatActivity activity;
    AggregateStats stats;

    ScrollView scrollView;

    TextView txtCpyWideTripsThisMonth;
    TextView txtCpyWideMilesThisMonth;
    TextView txtCpyWideReimbursementThisMonth;
    TextView txtCpyWideTripsLastMonth;
    TextView txtCpyWideMilesLastMonth;
    TextView txtCpyWideReimbursementLastMonth;
    TextView txtCpyWideTripsLastLastMonth;
    TextView txtCpyWideMilesLastLastMonth;
    TextView txtCpyWideReimbursementLastLastMonth;
    SearchView searchView;

/*    TextView txtDriver1ThisMonth;
    TextView txtDriver2ThisMonth;
    TextView txtDriver3ThisMonth;
    TextView txtDriver1LastMonth;
    TextView txtDriver2LastMonth;
    TextView txtDriver3LastMonth;
    TextView txtDriver1LastLastMonth;
    TextView txtDriver2LastLastMonth;
    TextView txtDriver3LastLastMonth;

    TextView txtDriver1ThisMonthValue;
    TextView txtDriver2ThisMonthValue;
    TextView txtDriver3ThisMonthValue;
    TextView txtDriver1LastMonthValue;
    TextView txtDriver2LastMonthValue;
    TextView txtDriver3LastMonthValue;
    TextView txtDriver1LastLastMonthValue;
    TextView txtDriver2LastLastMonthValue;
    TextView txtDriver3LastLastMonthValue;*/

    LineChart cpyWideTripCountChart;
    BarChart thisMonthTopDriversChart;
    BarChart lastMonthTopDriversChart;
    BarChart lastLastMonthTopDriversChart;

    Typeface tf;

    GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aggregate_stats);
        this.activity = this;
        scrollView = findViewById(R.id.scrollview_master);

        Helpers.Views.MySwipeHandler swipeHandler = new Helpers.Views.MySwipeHandler(new Helpers.Views.MySwipeHandler.MySwipeListener() {
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
        swipeHandler.addView(scrollView);

        // Log a metric
        MileBuddyMetrics.updateMetric(this, MileBuddyMetrics.MetricName.LAST_ACCESSED_MILEAGE_STATS, DateTime.now());

        this.setTitle("Statistics");

        cpyWideTripCountChart = findViewById(R.id.trendChart);
        thisMonthTopDriversChart = findViewById(R.id.trendChartThisMonthDrivers);
        lastMonthTopDriversChart = findViewById(R.id.trendChartLastMonthDrivers);
        lastLastMonthTopDriversChart = findViewById(R.id.trendChartLastLastMonthDrivers);

        // Company-wide this month
        txtCpyWideTripsThisMonth = findViewById(R.id.txtTripsThisMonth);
        txtCpyWideTripsLastMonth = findViewById(R.id.txtTripsLastMonth);
        txtCpyWideTripsLastLastMonth = findViewById(R.id.txtTripsLastLastMonth);

        // Company-wide 1 month ago
        txtCpyWideMilesThisMonth = findViewById(R.id.txtMilesThisMonth);
        txtCpyWideMilesLastMonth = findViewById(R.id.txtMilesLastMonth);
        txtCpyWideMilesLastLastMonth = findViewById(R.id.txtMilesLastLastMonth);

        // Company-wide 2 months ago
        txtCpyWideReimbursementThisMonth = findViewById(R.id.txtReimbursementThisMonth);
        txtCpyWideReimbursementLastMonth = findViewById(R.id.txtReimbursementLastMonth);
        txtCpyWideReimbursementLastLastMonth = findViewById(R.id.txtReimbursementLastLastMonth);

        tf = txtCpyWideMilesLastLastMonth.getTypeface();

        /*// Top 3 Driver names
        txtDriver1ThisMonth = findViewById(R.id.textView_driver1);
        txtDriver2ThisMonth = findViewById(R.id.textView_driver2);
        txtDriver3ThisMonth = findViewById(R.id.textView_driver3);
        txtDriver1LastMonth = findViewById(R.id.textView_driver1_LastMonth);
        txtDriver2LastMonth = findViewById(R.id.textView_driver2_LastMonth);
        txtDriver3LastMonth = findViewById(R.id.textView_driver3_LastMonth);
        txtDriver1LastLastMonth = findViewById(R.id.textView_driver1_LastLastMonth);
        txtDriver2LastLastMonth = findViewById(R.id.textView_driver2_LastLastMonth);
        txtDriver3LastLastMonth = findViewById(R.id.textView_driver3_LastLastMonth);

        // Top 3 Driver values
        txtDriver1ThisMonthValue = findViewById(R.id.textView_driver1_value);
        txtDriver2ThisMonthValue = findViewById(R.id.textView_driver2_value);
        txtDriver3ThisMonthValue = findViewById(R.id.textView_driver3_value);
        txtDriver1LastMonthValue = findViewById(R.id.textView_driver1_LastMonth_value);
        txtDriver2LastMonthValue = findViewById(R.id.textView_driver2_LastMonth_value);
        txtDriver3LastMonthValue = findViewById(R.id.textView_driver3_LastMonth_value);
        txtDriver1LastLastMonthValue = findViewById(R.id.textView_driver1_LastLastMonth_value);
        txtDriver2LastLastMonthValue = findViewById(R.id.textView_driver2_LastLastMonth_value);
        txtDriver3LastLastMonthValue = findViewById(R.id.textView_driver3_LastLastMonth_value);*/

        // Create the navigation up button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mileage_stats_all_users_menu, menu);

        // Associate searchable configuration with the SearchView
        /*SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo( searchManager.getSearchableInfo(new
                ComponentName(this, SearchResultsActivity.class)));

        if (searchView != null) {
            searchView.setInputType(InputType.TYPE_CLASS_TEXT);
        }*/

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_export_to_excel:
                ExcelSpreadsheet spreadsheet = stats.exportToExcel();
                spreadsheet.share(this);
                break;

            case R.id.action_export_as_image :
                Bitmap image = Helpers.Bitmaps.saveScrollViewAsImage(scrollView);
                File file = new File(Helpers.Files.ExcelTempFiles.getDirectory(), "mileage_aggregate.png");
                try {
                    file.createNewFile();
                    file = Helpers.Bitmaps.bitmapToFile(image, file);
                    Helpers.Files.shareFile(activity, file, "Share mileage stats");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        getStats();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        Log.i(TAG, "onTouchEvent " + event.getAction());

        mGestureDetector.onTouchEvent(event);

        return super.onTouchEvent(event);
    }
    /*
    void exportToExcel() {
        
        if (this.stats == null || this.stats.size() < 1) {
            Toast.makeText(this, "No stats found!", Toast.LENGTH_SHORT).show();
            return;
        }

        ExcelSpreadsheet spreadsheet = null;

        // Create a new spreadsheet
        String monthYear = Helpers.DatesAndTimes.getMonthName(DateTime.now().getMonthOfYear())
                .toLowerCase().replace(" ","") + "_" + DateTime.now().getYear();
        try { spreadsheet = new ExcelSpreadsheet("milebuddy_aggregate_mileage_export_" + monthYear + ".xls"); } catch (Exception e) {
            Toast.makeText(this, "Failed to create spreadsheet!\n" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }

        // Add the sheets that we will populate
        try {
            String monthname;

            // All raw trips last 2 months
            spreadsheet.createSheet("Agregated data", AGGREGATE_TOTALS_SHEET);;

            // This month
            monthname = Helpers.DatesAndTimes.getMonthName(DateTime.now().getMonthOfYear());
            spreadsheet.createSheet(monthname + " " + DateTime.now().getYear(), THIS_MONTH_SHEET);

            // One month ago
            monthname = Helpers.DatesAndTimes.getMonthName(DateTime.now().minusMonths(1).getMonthOfYear());
            spreadsheet.createSheet(monthname + " " + DateTime.now().minusMonths(1).getYear(), LAST_MONTH_SHEET);

            // Two months ago
            monthname = Helpers.DatesAndTimes.getMonthName(DateTime.now().minusMonths(2).getMonthOfYear());
            spreadsheet.createSheet(monthname + " " + DateTime.now().minusMonths(2).getYear(), LAST_LAST_MONTH_SHEET);

            // All raw trips last 2 months
            spreadsheet.createSheet("All trips raw data", RAW_DATA_SHEET);;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Format header and content values
        WritableFont headerFont = new WritableFont(WritableFont.TAHOMA, 10, WritableFont.BOLD);
        try {
            headerFont.setColour(Colour.BLACK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        WritableCellFormat headerFormat = new WritableCellFormat(headerFont);

        WritableFont contentFont = new WritableFont(WritableFont.TAHOMA, 10, WritableFont.NO_BOLD);
        try {
            contentFont.setColour(Colour.BLACK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        WritableCellFormat contentFormat = new WritableCellFormat(contentFont);

        // *****************************************************************************************
        //                                 AGGREGATE TOTALS SHEET
        // *****************************************************************************************

        // trip count
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 0, "This month trips:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 0, stats.thisMonthTripCount + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 1, "Last month trips:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 1, stats.lastMonthTripCount + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 2, "Two months ago trips:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 2, stats.lastLastMonthTripCount + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 3, "", contentFormat);

        // total miles
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 4, "This month total miles:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 4, stats.thisMonthTotalMiles + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 5, "Last month total miles:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 5, stats.lastMonthTotalMiles + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 6, "Two months ago total miles:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 6, stats.lastLastMonthTotalMiles + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 7, "", contentFormat);

        // avg length
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 8, "This month average length:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 8, stats.thisMonthAverageTripLength + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 9, "Last month average length:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 9, stats.lastMonthAverageTripLength + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 10, "Two months ago average length:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 10, stats.lastLastMonthAverageTripLength + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 11, "", contentFormat);

        // avg reimbursement
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 12, "This month avg payout:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 12, stats.convertToCurrency(stats.thisMonthAverageReimbursement) + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 13, "Last month avg payout:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 13, stats.convertToCurrency(stats.lastMonthAverageReimbursement) + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 14, "Two months ago avg payout:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 14, stats.convertToCurrency(stats.lastLastMonthAverageReimbursement) + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 15, "", contentFormat);

        // man trip pct
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 16, "This month manual pct:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 16, stats.convertToPct(stats.thisMonthManualPct) + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 17, "Last month manual pct:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 17, stats.convertToPct(stats.lastMonthManualPct) + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 18, "Two months ago manual pct:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 18, stats.convertToPct(stats.lastLastMonthManualPct) + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 19, "", contentFormat);

        // edited pct
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 20, "This month edited pct:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 20, stats.convertToPct(stats.thisMonthEditedPct) + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 21, "Last month edited pct:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 21, stats.convertToPct(stats.lastMonthEditedPct) + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 22, "Two months ago edited pct:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 22, stats.convertToPct(stats.lastLastMonthEditedPct) + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 23, "", contentFormat);

        // auto killed pct
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 24, "This month auto kill pct:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 24, stats.convertToPct(stats.thisMonthAutoKillPct) + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 25, "Last month auto kill pct:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 25, stats.convertToPct(stats.lastMonthAutoKillPct) + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 26, "Two months ago auto kill pct:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 26, stats.convertToPct(stats.lastLastMonthAutoKillPct) + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 27, "", contentFormat);

        // *****************************************************************************************
        //                                 THIS MONTH TOTALS SHEET
        // *****************************************************************************************
        try {
            // Trip count
            spreadsheet.addCell(THIS_MONTH_SHEET, 0, 0, "Trip count:", headerFormat);
            spreadsheet.addCell(THIS_MONTH_SHEET, 1, 0, Float.toString(stats.thisMonthTripCount), contentFormat);

            // Total miles
            spreadsheet.addCell(THIS_MONTH_SHEET, 0, 1, "Total miles:", headerFormat);
            spreadsheet.addCell(THIS_MONTH_SHEET, 1, 1, Float.toString(stats.thisMonthTotalMiles), contentFormat);

            // Total payout
            spreadsheet.addCell(THIS_MONTH_SHEET, 0, 2, "Total payout:", headerFormat);
            spreadsheet.addCell(THIS_MONTH_SHEET, 1, 2, Float.toString(stats.thisMonthTotalPayout), contentFormat);

            // Total edited trips
            spreadsheet.addCell(THIS_MONTH_SHEET, 0, 3, "Total edited trips:", headerFormat);
            spreadsheet.addCell(THIS_MONTH_SHEET, 1, 3, Float.toString(stats.thisMonthEditedTripCount), contentFormat);

            // Total edited trips
            spreadsheet.addCell(THIS_MONTH_SHEET, 0, 4, "Total manual trips:", headerFormat);
            spreadsheet.addCell(THIS_MONTH_SHEET, 1, 4, Float.toString(stats.thisMonthManTripCount), contentFormat);

            // Total edited trips
            spreadsheet.addCell(THIS_MONTH_SHEET, 0, 5, "Total auto-stopped trips:", headerFormat);
            spreadsheet.addCell(THIS_MONTH_SHEET, 1, 5, Float.toString(stats.thisMonthTripMinderKillCount), contentFormat);

            // By user header
            spreadsheet.addCell(THIS_MONTH_SHEET, 0, 7, "Name", headerFormat);
            spreadsheet.addCell(THIS_MONTH_SHEET, 1, 7, "Total miles", headerFormat);
            spreadsheet.addCell(THIS_MONTH_SHEET, 2, 7, "Total reimbursement", headerFormat);

            // By user trip counts
            for (int i = 0; i < stats.topUserMilesThisMonth.size(); i++) {
                AggregateStats.UserTotals userTotals = stats.topUserMilesThisMonth.get(i);
                spreadsheet.addCell(THIS_MONTH_SHEET, 0, i + 8, userTotals.fullname, contentFormat);
                spreadsheet.addCell(THIS_MONTH_SHEET, 1, i + 8, Double.toString(userTotals.totalMiles), contentFormat);
                spreadsheet.addCell(THIS_MONTH_SHEET, 2, i + 8, Double.toString(userTotals.totalReimbursement), contentFormat);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // *****************************************************************************************
        //                                 LAST MONTH TOTALS SHEET
        // *****************************************************************************************
        try {
            // Trip count
            spreadsheet.addCell(LAST_MONTH_SHEET, 0, 0, "Trip count:", headerFormat);
            spreadsheet.addCell(LAST_MONTH_SHEET, 1, 0, Float.toString(stats.lastMonthTripCount), contentFormat);

            // Total miles
            spreadsheet.addCell(LAST_MONTH_SHEET, 0, 1, "Total miles:", headerFormat);
            spreadsheet.addCell(LAST_MONTH_SHEET, 1, 1, Float.toString(stats.lastMonthTotalMiles), contentFormat);

            // Total payout
            spreadsheet.addCell(LAST_MONTH_SHEET, 0, 2, "Total payout:", headerFormat);
            spreadsheet.addCell(LAST_MONTH_SHEET, 1, 2, Float.toString(stats.lastMonthTotalPayout), contentFormat);

            // Total edited trips
            spreadsheet.addCell(LAST_MONTH_SHEET, 0, 3, "Total edited trips:", headerFormat);
            spreadsheet.addCell(LAST_MONTH_SHEET, 1, 3, Float.toString(stats.lastMonthEditedTripCount), contentFormat);

            // Total edited trips
            spreadsheet.addCell(LAST_MONTH_SHEET, 0, 4, "Total manual trips:", headerFormat);
            spreadsheet.addCell(LAST_MONTH_SHEET, 1, 4, Float.toString(stats.lastMonthManTripCount), contentFormat);

            // Total edited trips
            spreadsheet.addCell(LAST_MONTH_SHEET, 0, 5, "Total auto-stopped trips:", headerFormat);
            spreadsheet.addCell(LAST_MONTH_SHEET, 1, 5, Float.toString(stats.lastMonthTripMinderKillCount), contentFormat);

            // By user header
            spreadsheet.addCell(LAST_MONTH_SHEET, 0, 7, "Name", headerFormat);
            spreadsheet.addCell(LAST_MONTH_SHEET, 1, 7, "Total miles", headerFormat);
            spreadsheet.addCell(LAST_MONTH_SHEET, 2, 7, "Total reimbursement", headerFormat);

            // By user trip counts
            for (int i = 0; i < stats.topUserMilesLastMonth.size(); i++) {
                AggregateStats.UserTotals userTotals = stats.topUserMilesLastMonth.get(i);
                spreadsheet.addCell(LAST_MONTH_SHEET, 0, i + 8, userTotals.fullname, contentFormat);
                spreadsheet.addCell(LAST_MONTH_SHEET, 1, i + 8, Double.toString(userTotals.totalMiles), contentFormat);
                spreadsheet.addCell(LAST_MONTH_SHEET, 2, i + 8, Double.toString(userTotals.totalReimbursement), contentFormat);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // *****************************************************************************************
        //                              LAST LAST MONTH TOTALS SHEET
        // *****************************************************************************************
        try {
            // Trip count
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 0, 0, "Trip count:", headerFormat);
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 1, 0, Float.toString(stats.lastLastMonthTripCount), contentFormat);

            // Total miles
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 0, 1, "Total miles:", headerFormat);
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 1, 1, Float.toString(stats.lastLastMonthTotalMiles), contentFormat);

            // Total payout
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 0, 2, "Total payout:", headerFormat);
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 1, 2, Float.toString(stats.lastLastMonthTotalPayout), contentFormat);

            // Total edited trips
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 0, 3, "Total edited trips:", headerFormat);
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 1, 3, Float.toString(stats.lastLastMonthEditedTripCount), contentFormat);

            // Total edited trips
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 0, 4, "Total manual trips:", headerFormat);
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 1, 4, Float.toString(stats.lastLastMonthManTripCount), contentFormat);

            // Total edited trips
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 0, 5, "Total auto-stopped trips:", headerFormat);
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 1, 5, Float.toString(stats.lastLastMonthTripMinderKillCount), contentFormat);

            // By user header
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 0, 7, "Name", headerFormat);
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 1, 7, "Total miles", headerFormat);
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 2, 7, "Total reimbursement", headerFormat);

            // By user trip counts
            for (int i = 0; i < stats.topUserMilesLastLastMonth.size(); i++) {
                AggregateStats.UserTotals userTotals = stats.topUserMilesLastLastMonth.get(i);
                spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 0, i + 8, userTotals.fullname, contentFormat);
                spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 1, i + 8, Double.toString(userTotals.totalMiles), contentFormat);
                spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 2, i + 8, Double.toString(userTotals.totalReimbursement), contentFormat);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // *****************************************************************************************
        //                                 RAW DATA SHEET
        // *****************************************************************************************
        try {
            spreadsheet.addCell(RAW_DATA_SHEET, 0, 0, "Trip name", headerFormat);
            spreadsheet.addCell(RAW_DATA_SHEET, 1, 0, "Date", headerFormat);
            spreadsheet.addCell(RAW_DATA_SHEET, 2, 0, "Driver", headerFormat);
            spreadsheet.addCell(RAW_DATA_SHEET, 3, 0, "Distance (mi)", headerFormat);
            spreadsheet.addCell(RAW_DATA_SHEET, 4, 0, "Reimbursement", headerFormat);
            spreadsheet.addCell(RAW_DATA_SHEET, 5, 0, "Manual trip", headerFormat);
            spreadsheet.addCell(RAW_DATA_SHEET, 6, 0, "Edited trip", headerFormat);
            spreadsheet.addCell(RAW_DATA_SHEET, 7, 0, "Auto-stopped trip", headerFormat);

            for (int i = 0; i < stats.size(); i++) {
                AggregateStats.AggregateStat stat = stats.get(i);
                spreadsheet.addCell(RAW_DATA_SHEET, 0, i + 1, stat.tripName, contentFormat);
                spreadsheet.addCell(RAW_DATA_SHEET, 1, i + 1, Helpers.DatesAndTimes.getPrettyDateAndTime(stat.tripDate), contentFormat);
                spreadsheet.addCell(RAW_DATA_SHEET, 2, i + 1, stat.ownerName, contentFormat);
                spreadsheet.addCell(RAW_DATA_SHEET, 3, i + 1, Double.toString(stat.distanceMiles), contentFormat);
                spreadsheet.addCell(RAW_DATA_SHEET, 4, i + 1, Double.toString(stat.reimbursement), contentFormat);
                spreadsheet.addCell(RAW_DATA_SHEET, 5, i + 1, Boolean.toString(stat.isManual), contentFormat);
                spreadsheet.addCell(RAW_DATA_SHEET, 6, i + 1, Boolean.toString(stat.isEdited), contentFormat);
                spreadsheet.addCell(RAW_DATA_SHEET, 7, i + 1, Boolean.toString(stat.tripMinderKilled), contentFormat);
            }
            
        } catch (Exception e) { e.printStackTrace(); }

        // Save the file
        spreadsheet.save();

        // Share the file
        spreadsheet.share(this);
    }
*/
    void drawTrendChart() {

        // Build a line dataset using our cached AggregateStats object
        ArrayList<Entry> values = new ArrayList<>();
        values.add(new Entry(0f, (float) stats.lastLastMonthTripCount));
        values.add(new Entry(1f, (float) stats.lastMonthTripCount));
        values.add(new Entry(2f, (float) stats.thisMonthTripCount));

        // Build the labels along the x axis
        final ArrayList<String> xAxisLabel = new ArrayList<>();
        xAxisLabel.add("Two months ago");
        xAxisLabel.add("Last month");
        xAxisLabel.add("This month");

        // Font to use
        tf = txtCpyWideMilesLastLastMonth.getTypeface();

        // Format the x axis
        XAxis xAxis = cpyWideTripCountChart.getXAxis();
        XAxis.XAxisPosition position = XAxis.XAxisPosition.BOTTOM;
        xAxis.setPosition(position);
        xAxis.enableGridDashedLine(2f, 7f, 0f);
        xAxis.setAxisMaximum(2f);
        xAxis.setAxisMinimum(0f);
        xAxis.setLabelCount(4, true);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(0f);
        xAxis.setValueFormatter(new ClaimsXAxisValueFormatter(xAxisLabel));
        xAxis.setCenterAxisLabels(true);
        xAxis.setDrawLimitLinesBehindData(true);

        // Remove the x axis labels on the left and right of the chart
        YAxis leftAxis = cpyWideTripCountChart.getAxisLeft();
        YAxis rightAxis = cpyWideTripCountChart.getAxisRight();
        leftAxis.setDrawLabels(false);
        rightAxis.setDrawLabels(false);

        // Apply our data and set chart-wide aesthetics
        LineDataSet set1;
        if (cpyWideTripCountChart.getData() != null &&
                cpyWideTripCountChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) cpyWideTripCountChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            cpyWideTripCountChart.getData().notifyDataChanged();
            cpyWideTripCountChart.notifyDataSetChanged();
        } else {
            set1 = new LineDataSet(values, "Last three months total mileage");
            set1.setDrawIcons(true);
            set1.disableDashedLine();
            set1.disableDashedHighlightLine();
            set1.setColor(Color.DKGRAY);
            set1.setCircleColor(Color.DKGRAY);
            set1.setLineWidth(2f);
            set1.setCircleRadius(6f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(11f);
            set1.setLabel("Trip counts");
            set1.setDrawFilled(false);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);
            if (Utils.getSDKInt() >= 18) {
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.orangemenu);
                set1.setFillDrawable(drawable);
            } else {
                set1.setFillColor(Color.DKGRAY);
            }
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            set1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            dataSets.add(set1);
            LineData data = new LineData(dataSets);

            cpyWideTripCountChart.getLegend().setEnabled(false);
            cpyWideTripCountChart.setDescription(null);

            // Fonts
            rightAxis.setTypeface(tf);
            leftAxis.setTypeface(tf);
            cpyWideTripCountChart.getXAxis().setTypeface(tf);

            // Show the chart with sexy data
            cpyWideTripCountChart.animateXY(1000, 2000);
            cpyWideTripCountChart.setData(data);
            cpyWideTripCountChart.invalidate();

        }

    }

    void populateChartThisMonthTopDrivers() {

        String title = "Top 3 drivers";

        // txtChartTitle.setText(title);

        // Start building the containers to hold the goal data
        ArrayList<IBarDataSet> sets = new ArrayList<>();
        ArrayList<BarEntry> entries = new ArrayList<>();

        // Create and populate a container to hold the bar entries, labels and colors
        final ArrayList<String> xAxisLabel = new ArrayList<>();

        for(int i = 0; i < stats.topUserMilesThisMonth.size(); i++) {

            if (i < 4) {
                AggregateStats.UserTotals totals = stats.topUserMilesThisMonth.get(i);
                BarEntry entry = new BarEntry(i, (float) totals.totalMiles);
                entries.add(entry);
                xAxisLabel.add(totals.fullname);
            }
        }

        // Build a BarDataSet container and fill it with our data containers
        BarDataSet ds = new BarDataSet(entries, title);
        ds.setColors(ColorTemplate.LIBERTY_COLORS);
        sets.add(ds);
        BarData d = new BarData(sets);

        // Apply the chart data to the chart
        thisMonthTopDriversChart.setData(d);

        // Hide the legend
        thisMonthTopDriversChart.getLegend().setEnabled(false);

        // Aesthetics
        thisMonthTopDriversChart.setDrawValueAboveBar(true);
        thisMonthTopDriversChart.animateXY(0, 1000);
        thisMonthTopDriversChart.setDescription(null);

        // Show each label for each entry
        ValueFormatter xAxisFormatter = new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                return super.getBarLabel(barEntry);
            }
        };

        // Format the entries and labels
        XAxis xAxis = thisMonthTopDriversChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Where to put the labels
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // intervals
        xAxis.setLabelCount(xAxisLabel.size());
        xAxis.setTypeface(tf);
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
        thisMonthTopDriversChart.invalidate();

        // Make an onclick listener for chart values
        thisMonthTopDriversChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Log.i(TAG, "onValueSelected " + e.getData());

            }

            @Override
            public void onNothingSelected() {

            }
        }); // end onChartClickListener

    }

    void populateChartLastMonthTopDrivers() {

        String title = "Top 3 drivers";

        // txtChartTitle.setText(title);

        // Start building the containers to hold the goal data
        ArrayList<IBarDataSet> sets = new ArrayList<>();
        ArrayList<BarEntry> entries = new ArrayList<>();

        // Create and populate a container to hold the bar entries, labels and colors
        final ArrayList<String> xAxisLabel = new ArrayList<>();

        for(int i = 0; i < stats.topUserMilesLastMonth.size(); i++) {

            if (i < 4) {
                AggregateStats.UserTotals totals = stats.topUserMilesLastMonth.get(i);
                BarEntry entry = new BarEntry(i, (float) totals.totalMiles);
                entries.add(entry);
                xAxisLabel.add(totals.fullname);
            }
        }

        // Build a BarDataSet container and fill it with our data containers
        BarDataSet ds = new BarDataSet(entries, title);
        ds.setColors(ColorTemplate.LIBERTY_COLORS);
        sets.add(ds);
        BarData d = new BarData(sets);

        // Apply the chart data to the chart
        lastMonthTopDriversChart.setData(d);

        // Hide the legend
        lastMonthTopDriversChart.getLegend().setEnabled(false);

        // Aesthetics
        lastMonthTopDriversChart.setDrawValueAboveBar(true);
        lastMonthTopDriversChart.animateXY(0, 1000);
        lastMonthTopDriversChart.setDescription(null);

        // Show each label for each entry
        ValueFormatter xAxisFormatter = new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                return super.getBarLabel(barEntry);
            }
        };

        // Format the entries and labels
        XAxis xAxis = lastMonthTopDriversChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Where to put the labels
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // intervals
        xAxis.setLabelCount(xAxisLabel.size());
        xAxis.setTypeface(tf);
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
        lastMonthTopDriversChart.invalidate();

        // Make an onclick listener for chart values
        lastMonthTopDriversChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Log.i(TAG, "onValueSelected " + e.getData());

            }

            @Override
            public void onNothingSelected() {

            }
        }); // end onChartClickListener

    }

    void populateChartLastLastMonthTopDrivers() {

        String title = "Top 3 drivers";

        // txtChartTitle.setText(title);

        // Start building the containers to hold the goal data
        ArrayList<IBarDataSet> sets = new ArrayList<>();
        ArrayList<BarEntry> entries = new ArrayList<>();

        // Create and populate a container to hold the bar entries, labels and colors
        final ArrayList<String> xAxisLabel = new ArrayList<>();

        for(int i = 0; i < stats.topUserMilesLastLastMonth.size(); i++) {

            if (i < 4) {
                AggregateStats.UserTotals totals = stats.topUserMilesLastLastMonth.get(i);
                BarEntry entry = new BarEntry(i, (float) totals.totalMiles);
                entries.add(entry);
                xAxisLabel.add(totals.fullname);
            }
        }

        // Build a BarDataSet container and fill it with our data containers
        BarDataSet ds = new BarDataSet(entries, title);
        ds.setColors(ColorTemplate.LIBERTY_COLORS);
        sets.add(ds);
        BarData d = new BarData(sets);

        // Apply the chart data to the chart
        lastLastMonthTopDriversChart.setData(d);

        // Hide the legend
        lastLastMonthTopDriversChart.getLegend().setEnabled(false);

        // Aesthetics
        lastLastMonthTopDriversChart.setDrawValueAboveBar(true);
        lastLastMonthTopDriversChart.animateXY(0, 1000);
        lastLastMonthTopDriversChart.setDescription(null);

        // Show each label for each entry
        ValueFormatter xAxisFormatter = new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                return super.getBarLabel(barEntry);
            }
        };

        // Format the entries and labels
        XAxis xAxis = lastLastMonthTopDriversChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Where to put the labels
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // intervals
        xAxis.setLabelCount(xAxisLabel.size());
        xAxis.setTypeface(tf);
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
        lastLastMonthTopDriversChart.invalidate();

        // Make an onclick listener for chart values
        lastLastMonthTopDriversChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Log.i(TAG, "onValueSelected " + e.getData());

            }

            @Override
            public void onNothingSelected() {

            }
        }); // end onChartClickListener

    }

    public class ClaimsXAxisValueFormatter extends ValueFormatter {

        List<String> datesList;

        public ClaimsXAxisValueFormatter(List<String> arrayOfMonths) {
            this.datesList = arrayOfMonths;
        }

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            /*
            Depends on the position number on the X axis, we need to display the label, Here, this
            is the logic to convert the float value to integer so that I can get the value from array
            based on that integer and can convert it to the required value here, month and date as
            value. This is required for my data to show properly, you can customize according to your needs.
            */
            Integer position = Math.round(value);

            if (value == 0) {
                position = 0;
            } else if (value > 0 && value <= .7) {
                position = 1;
            } else if (value > .7) {
                position = 2;
            }
            return this.datesList.get(position);
        }
    }

    void getStats() {

        final MyProgressDialog dialog = new MyProgressDialog(this, "Getting stats...");
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

        Filter filter = new Filter(Filter.FilterType.OR);
        FilterCondition condition1 = new FilterCondition("msus_dt_tripdate", Operator.LAST_X_MONTHS, "2");
        filter.addCondition(condition1);
        factory.setFilter(filter);

        factory.sortClauses.add(new SortClause("msus_dt_tripdate", true, SortClause.ClausePosition.ONE));
        factory.sortClauses.add(new SortClause("ownerid", true, SortClause.ClausePosition.TWO));

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
                parseStats();
                drawTrendChart();
                populateChartThisMonthTopDrivers();
                populateChartLastMonthTopDrivers();
                populateChartLastLastMonthTopDrivers();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(AggregateStatsActivity.this, "Failed to get stats", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                finish();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    void parseStats() {

        DecimalFormat formatter = new DecimalFormat("#,###");

        // Trip count
        txtCpyWideTripsThisMonth.setText(stats.thisMonthTripCount + "");
        txtCpyWideTripsLastMonth.setText(stats.lastMonthTripCount + "");
        txtCpyWideTripsLastLastMonth.setText(stats.lastLastMonthTripCount + "");

        // Mile tally
        String thisMonthVal = formatter.format(Helpers.Numbers.formatAsZeroDecimalPointNumber
                (stats.thisMonthTotalMiles, RoundingMode.UP)) + "";
        txtCpyWideMilesThisMonth.setText(thisMonthVal);
        String lastMonthVal = formatter.format(Helpers.Numbers.formatAsZeroDecimalPointNumber
                (stats.lastMonthTotalMiles, RoundingMode.UP)) + "";
        txtCpyWideMilesLastMonth.setText(lastMonthVal);
        String lastLastMonthVal = formatter.format(Helpers.Numbers.formatAsZeroDecimalPointNumber
                (stats.lastMonthTotalMiles, RoundingMode.UP)) + "";
        txtCpyWideMilesLastLastMonth.setText(lastLastMonthVal);

        // Reimbursement
        txtCpyWideReimbursementThisMonth.setText(Helpers.Numbers.convertToCurrency(stats.thisMonthTotalPayout));
        txtCpyWideReimbursementLastMonth.setText(Helpers.Numbers.convertToCurrency(stats.lastMonthTotalPayout));
        txtCpyWideReimbursementLastLastMonth.setText(Helpers.Numbers.convertToCurrency(stats.lastLastMonthTotalPayout));

        // ***************************************************
        //              Top three driver's names
        // ***************************************************
/*        // This month
        if (stats.topUserMilesThisMonth.size() > 0)
            txtDriver1ThisMonth.setText(stats.topUserMilesThisMonth.get(0).fullname);
        if (stats.topUserMilesThisMonth.size() > 1)
            txtDriver2ThisMonth.setText(stats.topUserMilesThisMonth.get(1).fullname);
        if (stats.topUserMilesThisMonth.size() > 2)
            txtDriver3ThisMonth.setText(stats.topUserMilesThisMonth.get(2).fullname);

        // Last month
        if (stats.topUserMilesLastMonth.size() > 0)
            txtDriver1LastMonth.setText(stats.topUserMilesLastMonth.get(0).fullname);
        if (stats.topUserMilesLastMonth.size() > 1)
            txtDriver2LastMonth.setText(stats.topUserMilesLastMonth.get(1).fullname);
        if (stats.topUserMilesLastMonth.size() > 2)
            txtDriver3LastMonth.setText(stats.topUserMilesLastMonth.get(2).fullname);

        // Two months ago
        if (stats.topUserMilesLastLastMonth.size() > 0)
            txtDriver1LastLastMonth.setText(stats.topUserMilesLastLastMonth.get(0).fullname);
        if (stats.topUserMilesLastLastMonth.size() > 1)
            txtDriver2LastLastMonth.setText(stats.topUserMilesLastLastMonth.get(1).fullname);
        if (stats.topUserMilesLastLastMonth.size() > 2)
            txtDriver3LastLastMonth.setText(stats.topUserMilesLastLastMonth.get(2).fullname);

        // ***************************************************
//                  Top three driver's reimbursement
        // ***************************************************
        // This month
        if (stats.topUserMilesThisMonth.size() > 0)
            txtDriver1ThisMonthValue.setText(Helpers.Numbers.convertToCurrency(stats.topUserMilesThisMonth.get(0).totalReimbursement));
        if (stats.topUserMilesThisMonth.size() > 1)
            txtDriver2ThisMonthValue.setText(Helpers.Numbers.convertToCurrency(stats.topUserMilesThisMonth.get(1).totalReimbursement));
        if (stats.topUserMilesThisMonth.size() > 2)
            txtDriver3ThisMonthValue.setText(Helpers.Numbers.convertToCurrency(stats.topUserMilesThisMonth.get(2).totalReimbursement));

        // Last month
        if (stats.topUserMilesLastMonth.size() > 0)
            txtDriver1LastMonthValue.setText(Helpers.Numbers.convertToCurrency(stats.topUserMilesLastMonth.get(0).totalReimbursement));
        if (stats.topUserMilesLastMonth.size() > 1)
            txtDriver2LastMonthValue.setText(Helpers.Numbers.convertToCurrency(stats.topUserMilesLastMonth.get(1).totalReimbursement));
        if (stats.topUserMilesLastMonth.size() > 2)
            txtDriver3LastMonthValue.setText(Helpers.Numbers.convertToCurrency(stats.topUserMilesLastMonth.get(2).totalReimbursement));

        // Two months ago
        if (stats.topUserMilesLastLastMonth.size() > 0)
            txtDriver1LastLastMonthValue.setText(Helpers.Numbers.convertToCurrency(stats.topUserMilesLastLastMonth.get(0).totalReimbursement));
        if (stats.topUserMilesLastLastMonth.size() > 1)
            txtDriver2LastLastMonthValue.setText(Helpers.Numbers.convertToCurrency(stats.topUserMilesLastLastMonth.get(1).totalReimbursement));
        if (stats.topUserMilesLastLastMonth.size() > 2)
            txtDriver3LastLastMonthValue.setText(Helpers.Numbers.convertToCurrency(stats.topUserMilesLastLastMonth.get(2).totalReimbursement));*/
    }
}