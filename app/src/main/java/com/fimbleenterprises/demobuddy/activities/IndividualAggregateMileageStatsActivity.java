package com.fimbleenterprises.demobuddy.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.fimbleenterprises.demobuddy.MyApp;
import com.fimbleenterprises.demobuddy.objects_and_containers.AggregateStats;
import com.fimbleenterprises.demobuddy.Crm;
import com.fimbleenterprises.demobuddy.CustomTypefaceSpan;
import com.fimbleenterprises.demobuddy.objects_and_containers.ExcelSpreadsheet;
import com.fimbleenterprises.demobuddy.Helpers;
import com.fimbleenterprises.demobuddy.objects_and_containers.MileBuddyMetrics;
import com.fimbleenterprises.demobuddy.objects_and_containers.MileageUser;
import com.fimbleenterprises.demobuddy.dialogs.MyProgressDialog;
import com.fimbleenterprises.demobuddy.QueryFactory;
import com.fimbleenterprises.demobuddy.QueryFactory.Filter;
import com.fimbleenterprises.demobuddy.QueryFactory.Filter.FilterCondition;
import com.fimbleenterprises.demobuddy.QueryFactory.Filter.Operator;
import com.fimbleenterprises.demobuddy.QueryFactory.SortClause;
import com.fimbleenterprises.demobuddy.R;
import com.fimbleenterprises.demobuddy.objects_and_containers.Requests;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Utils;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.joda.time.DateTime;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import cz.msebera.android.httpclient.Header;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class IndividualAggregateMileageStatsActivity extends AppCompatActivity {

    Context context;
    public static final String USER_TAG = "USER_TAG";
    private static final String TAG = "AggregateStats";
    AggregateStats stats;

    MileageUser user;

    TextView txtUserTripsThisMonth;
    TextView txtUserMilesThisMonth;
    TextView txtReimbursementThisMonth;

    TextView txtUserTripsLastMonth;
    TextView txtUserMilesLastMonth;
    TextView txtReimbursementLastMonth;

    TextView txtUserTripsLastLastMonth;
    TextView txtUserMilesLastLastMonth;
    TextView txtReimbursementLastLastMonth;

    LineChart userTripTrends;

    PieChart pieThisMonthEdited;
    PieChart pieLastMonthEdited;
    PieChart pieLastLastMonthEdited;

    PieChart pieThisMonthManual;
    PieChart pieLastMonthManual;
    PieChart pieLastLastMonthManual;

    PieChart pieThisMonthTripKilled;
    PieChart pieLastMonthTripKilled;
    PieChart pieLastLastMonthTripKilled;

    Typeface tf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_aggregate_stats);
        this.context = this;

        // Log a metric
        MileBuddyMetrics.updateMetric(this, MileBuddyMetrics.MetricName.LAST_ACCESSED_MILEAGE_STATS, DateTime.now());

        this.setTitle("Statistics");

        userTripTrends = findViewById(R.id.trendChart);

        pieThisMonthEdited = findViewById(R.id.thisMonthEditedTripsPieChart);
        pieThisMonthManual = findViewById(R.id.thisMonthManualPieChart);
        pieThisMonthTripKilled = findViewById(R.id.thisMonthKilledPieChart);

        pieLastMonthEdited = findViewById(R.id.lastMonthEditedTripsPieChart);
        pieLastMonthManual = findViewById(R.id.lastMonthManualPieChart);
        pieLastMonthTripKilled = findViewById(R.id.lastMonthKilledPieChart);

        pieLastLastMonthEdited = findViewById(R.id.lastLastMonthEditedTripsPieChart);
        pieLastLastMonthManual = findViewById(R.id.lastLastMonthManualPieChart);
        pieLastLastMonthTripKilled = findViewById(R.id.lastLastMonthKilledPieChart);

        txtUserTripsThisMonth = findViewById(R.id.txtTripsThisMonth);
        txtUserMilesThisMonth = findViewById(R.id.txtMilesThisMonth);
        txtReimbursementThisMonth = findViewById(R.id.txtReimbursementThisMonth);

        txtUserTripsLastMonth = findViewById(R.id.txtTripsLastMonth);
        txtUserMilesLastMonth = findViewById(R.id.txtMilesLastMonth);
        txtReimbursementLastMonth = findViewById(R.id.txtReimbursementLastMonth);

        txtUserTripsLastLastMonth = findViewById(R.id.txtTripsLastLastMonth);
        txtUserMilesLastLastMonth = findViewById(R.id.txtMilesLastLastMonth);
        txtReimbursementLastLastMonth = findViewById(R.id.txtReimbursementLastLastMonth);

        tf = txtUserTripsThisMonth.getTypeface();

        // Create the navigation up button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (intent != null && intent.getParcelableExtra(USER_TAG) != null) {
            user = intent.getParcelableExtra(USER_TAG);
            getStats();
        } else {
            Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show();
            finish();
        }

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
    protected void onResume() {
        super.onResume();
        MyApp.setIsVisible(true, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApp.setIsVisible(false, this);
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
                String monthYear = Helpers.DatesAndTimes.getMonthName(DateTime.now().getMonthOfYear())
                        .toLowerCase().replace(" ", "") + "_" + DateTime.now().getYear();
                String filename = "milebuddy_aggregate_mileage_export_" + monthYear + "_"
                        + user.fullname.replace(" ","_") + ".xls";
                ExcelSpreadsheet spreadsheet = stats.exportToExcel(filename.toLowerCase());
                spreadsheet.share(this);
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void applyFontToMenuItem(MenuItem mi, Typeface font) {
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
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
        factory.addColumn("msus_is_manual");
        factory.addColumn("msus_user_stopped_trip");
        factory.addColumn("msus_edited");
        factory.addColumn("msus_trip_minder_killed");
        factory.addColumn("ownerid");

        Filter filter = new Filter(Filter.FilterType.AND);
        FilterCondition condition1 = new FilterCondition("msus_dt_tripdate", Operator.LAST_X_MONTHS, "2");
        filter.addCondition(condition1);
        FilterCondition condition2 = new FilterCondition("ownerid", Operator.EQUALS, user.ownerid);
        filter.addCondition(condition2);
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
                
                drawThisMonthEditedTripsPieChart();
                drawThisMonthManualTripsPieChart();
                drawThisMonthTripKilledPieChart();

                drawLastMonthEditedTripsPieChart();
                drawLastMonthManualTripsPieChart();
                drawLastMonthTripKilledPieChart();

                drawLastLastMonthEditedTripsPieChart();
                drawLastLastMonthManualTripsPieChart();
                drawLastLastMonthTripKilledPieChart();

                // Populate values for the totals fields
                txtUserTripsThisMonth.setText("" + stats.thisMonthTripCount);
                txtUserMilesThisMonth.setText("" + stats.thisMonthTotalMiles);
                txtReimbursementThisMonth.setText(Helpers.Numbers.convertToCurrency(stats.thisMonthTotalPayout));

                txtUserTripsLastMonth.setText("" + stats.lastMonthTripCount);
                txtUserMilesLastMonth.setText("" + stats.lastMonthTotalMiles);
                txtReimbursementLastMonth.setText(Helpers.Numbers.convertToCurrency(stats.lastMonthTotalPayout));

                txtUserTripsLastLastMonth.setText("" + stats.lastLastMonthTripCount);
                txtUserMilesLastLastMonth.setText("" + stats.lastLastMonthTotalMiles);
                txtReimbursementLastLastMonth.setText(Helpers.Numbers.convertToCurrency(stats.lastLastMonthTotalPayout));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(IndividualAggregateMileageStatsActivity.this, "Failed to get stats", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                finish();
            }
        });
    }

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
        tf = txtUserTripsThisMonth.getTypeface();

        // Format the x axis
        XAxis xAxis = userTripTrends.getXAxis();
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
        YAxis leftAxis = userTripTrends.getAxisLeft();
        YAxis rightAxis = userTripTrends.getAxisRight();
        leftAxis.setDrawLabels(false);
        rightAxis.setDrawLabels(false);

        // Apply our data and set chart-wide aesthetics
        LineDataSet set1;
        if (userTripTrends.getData() != null &&
                userTripTrends.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) userTripTrends.getData().getDataSetByIndex(0);
            set1.setValues(values);
            userTripTrends.getData().notifyDataChanged();
            userTripTrends.notifyDataSetChanged();
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

            userTripTrends.getLegend().setEnabled(false);
            userTripTrends.setDescription(null);

            // Fonts
            rightAxis.setTypeface(tf);
            leftAxis.setTypeface(tf);
            userTripTrends.getXAxis().setTypeface(tf);

            // Show the chart with sexy data
            userTripTrends.animateXY(1000, 2000);
            userTripTrends.setData(data);
            userTripTrends.invalidate();

        }

    }

    // This month shit
    private void drawThisMonthEditedTripsPieChart(){

        tf = txtUserTripsThisMonth.getTypeface();

        //pupulating list of PieEntires
        List<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(stats.thisMonthEditedTripCount, "Edited"));
        pieEntries.add(new PieEntry(stats.thisMonthEditedTripCount - stats.thisMonthEditedTripCount, "Non-edited"));

        PieDataSet dataSet = new PieDataSet(pieEntries,"");
        dataSet.setColors(ColorTemplate.PASTEL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueTypeface(tf);
        data.setValueTextSize(14);

        //Get the chart
        pieThisMonthEdited.setData(data);
        pieThisMonthEdited.setDrawEntryLabels(false);
        pieThisMonthEdited.setContentDescription("");
        pieThisMonthEdited.setDrawMarkers(true);
        pieThisMonthEdited.setMaxHighlightDistance(34);
        pieThisMonthEdited.getData().setValueTypeface(tf);
        pieThisMonthEdited.setEntryLabelTextSize(16f);
        pieThisMonthEdited.setHoleRadius(70);
        pieThisMonthEdited.setDescription(null);

        pieThisMonthEdited.setUsePercentValues(false);
        pieThisMonthEdited.setCenterText(stats.thisMonthEditedTripCount == 0 && stats.thisMonthTripCount == 0 ? "Edited Trips\n(no data)" : "Edited Trips");
        pieThisMonthEdited.setCenterTextSize(16);
        pieThisMonthEdited.setCenterTextTypeface(tf);

        //legend attributes
        Legend legend = pieThisMonthEdited.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextSize(12);
        legend.setFormSize(20);
        legend.setFormToTextSpace(2);

        // Fonts

        // Font to use

        pieThisMonthEdited.animate();
        pieThisMonthEdited.invalidate();

    }
    
    private void drawThisMonthManualTripsPieChart(){

        tf = txtUserTripsThisMonth.getTypeface();

        //pupulating list of PieEntires
        List<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(stats.thisMonthManTripCount, "Manual"));
        pieEntries.add(new PieEntry(stats.thisMonthTripCount - stats.thisMonthManTripCount, "Non-manual"));

        PieDataSet dataSet = new PieDataSet(pieEntries,"");
        dataSet.setColors(ColorTemplate.PASTEL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueTypeface(tf);
        data.setValueTextSize(14);

        //Get the chart
        pieThisMonthManual.setData(data);
        pieThisMonthManual.setDrawEntryLabels(false);
        pieThisMonthManual.setContentDescription("");
        pieThisMonthManual.setDrawMarkers(true);
        pieThisMonthManual.setMaxHighlightDistance(34);
        pieThisMonthManual.getData().setValueTypeface(tf);
        pieThisMonthManual.setEntryLabelTextSize(16f);
        pieThisMonthManual.setHoleRadius(70);
        pieThisMonthManual.setDescription(null);

        pieThisMonthManual.setUsePercentValues(false);
        pieThisMonthManual.setCenterText(stats.thisMonthManTripCount == 0 && stats.thisMonthTripCount == 0 ? "Manual trips\n(no data)" : "Manual trips");
        pieThisMonthManual.setCenterTextSize(16);
        pieThisMonthManual.setCenterTextTypeface(tf);

        //legend attributes
        Legend legend = pieThisMonthManual.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextSize(12);
        legend.setFormSize(20);
        legend.setFormToTextSpace(2);

        pieThisMonthManual.invalidate();

    }

    private void drawThisMonthTripKilledPieChart(){

        tf = txtUserTripsThisMonth.getTypeface();

        //populating list of PieEntires
        List<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(stats.thisMonthTripMinderKillCount, "Auto-killed"));
        pieEntries.add(new PieEntry(stats.thisMonthTripCount - stats.thisMonthTripMinderKillCount, "Not killed"));

        PieDataSet dataSet = new PieDataSet(pieEntries,"");
        dataSet.setColors(ColorTemplate.PASTEL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueTypeface(tf);
        data.setValueTextSize(14);

        //Get the chart
        pieThisMonthTripKilled.setData(data);
        pieThisMonthTripKilled.setDrawEntryLabels(false);
        pieThisMonthTripKilled.setContentDescription("");
        pieThisMonthTripKilled.setDrawMarkers(true);
        pieThisMonthTripKilled.setMaxHighlightDistance(34);
        pieThisMonthTripKilled.getData().setValueTypeface(tf);
        pieThisMonthTripKilled.setEntryLabelTextSize(16f);
        pieThisMonthTripKilled.setHoleRadius(70);
        pieThisMonthTripKilled.setDescription(null);

        pieThisMonthTripKilled.setUsePercentValues(false);
        pieThisMonthTripKilled.setCenterText(stats.thisMonthTripMinderKillCount == 0 && stats.thisMonthTripCount == 0 ? "Auto-killed trips\n(no data)" : "Auto-killed trips");
        pieThisMonthTripKilled.setCenterTextSize(16);
        pieThisMonthTripKilled.setCenterTextTypeface(tf);

        //legend attributes
        Legend legend = pieThisMonthTripKilled.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextSize(12);
        legend.setFormSize(20);
        legend.setFormToTextSpace(2);

        pieThisMonthTripKilled.invalidate();

    }
    
    // Last month shit
    private void drawLastMonthEditedTripsPieChart(){

        tf = txtUserTripsLastMonth.getTypeface();

        //pupulating list of PieEntires
        List<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(stats.lastMonthEditedTripCount, "Edited"));
        pieEntries.add(new PieEntry(stats.lastMonthTripCount - stats.lastMonthEditedTripCount, "Non-edited"));

        PieDataSet dataSet = new PieDataSet(pieEntries,"");
        dataSet.setColors(ColorTemplate.PASTEL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueTypeface(tf);
        data.setValueTextSize(14);

        //Get the chart
        pieLastMonthEdited.setData(data);
        pieLastMonthEdited.setDrawEntryLabels(false);
        pieLastMonthEdited.setContentDescription("");
        pieLastMonthEdited.setDrawMarkers(true);
        pieLastMonthEdited.setMaxHighlightDistance(34);
        pieLastMonthEdited.getData().setValueTypeface(tf);
        pieLastMonthEdited.setEntryLabelTextSize(16f);
        pieLastMonthEdited.setHoleRadius(70);
        pieLastMonthEdited.setDescription(null);

        pieLastMonthEdited.setUsePercentValues(false);
        pieLastMonthEdited.setCenterText(stats.lastMonthEditedTripCount == 0
                && stats.lastMonthTripCount == 0 ? "Edited trips\n(no data)" : "Edited trips");
        pieLastMonthEdited.setCenterTextSize(16);
        pieLastMonthEdited.setCenterTextTypeface(tf);

        //legend attributes
        Legend legend = pieLastMonthEdited.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextSize(12);
        legend.setFormSize(20);
        legend.setFormToTextSpace(2);

        // Fonts

        // Font to use

        pieLastMonthEdited.invalidate();

    }

    private void drawLastMonthManualTripsPieChart(){

        tf = txtUserTripsThisMonth.getTypeface();

        //pupulating list of PieEntires
        List<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(stats.lastMonthManTripCount, "Manual"));
        pieEntries.add(new PieEntry(stats.lastMonthTripCount - stats.lastMonthManTripCount, "Non-manual"));

        PieDataSet dataSet = new PieDataSet(pieEntries,"");
        dataSet.setColors(ColorTemplate.PASTEL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueTypeface(tf);
        data.setValueTextSize(14);

        //Get the chart
        pieLastMonthManual.setData(data);
        pieLastMonthManual.setDrawEntryLabels(false);
        pieLastMonthManual.setContentDescription("");
        pieLastMonthManual.setDrawMarkers(true);
        pieLastMonthManual.setMaxHighlightDistance(34);
        pieLastMonthManual.getData().setValueTypeface(tf);
        pieLastMonthManual.setEntryLabelTextSize(16f);
        pieLastMonthManual.setHoleRadius(70);
        pieLastMonthManual.setDescription(null);

        pieLastMonthManual.setUsePercentValues(false);
        pieLastMonthManual.setCenterText(stats.lastMonthManTripCount == 0
                && stats.lastMonthTripCount == 0 ? "Manual trips\n(no data)" : "Manual trips");
        pieLastMonthManual.setCenterTextSize(16);
        pieLastMonthManual.setCenterTextTypeface(tf);

        //legend attributes
        Legend legend = pieLastMonthManual.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextSize(12);
        legend.setFormSize(20);
        legend.setFormToTextSpace(2);

        pieLastMonthManual.invalidate();

    }

    private void drawLastMonthTripKilledPieChart(){

        tf = txtUserTripsLastMonth.getTypeface();

        //pupulating list of PieEntires
        List<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(stats.lastMonthTripMinderKillCount, "Auto-killed"));
        pieEntries.add(new PieEntry(stats.lastMonthTripCount - stats.lastMonthTripMinderKillCount, "Not killed"));

        PieDataSet dataSet = new PieDataSet(pieEntries,"");
        dataSet.setColors(ColorTemplate.PASTEL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueTypeface(tf);
        data.setValueTextSize(14);

        //Get the chart
        pieLastMonthTripKilled.setData(data);
        pieLastMonthTripKilled.setDrawEntryLabels(false);
        pieLastMonthTripKilled.setContentDescription("");
        pieLastMonthTripKilled.setDrawMarkers(true);
        pieLastMonthTripKilled.setMaxHighlightDistance(34);
        pieLastMonthTripKilled.getData().setValueTypeface(tf);
        pieLastMonthTripKilled.setEntryLabelTextSize(16f);
        pieLastMonthTripKilled.setHoleRadius(70);
        pieLastMonthTripKilled.setDescription(null);

        pieLastMonthTripKilled.setUsePercentValues(false);
        pieLastMonthTripKilled.setCenterText(stats.lastMonthTripMinderKillCount == 0
                && stats.lastMonthTripCount == 0 ? "Auto-killed trips\n(no data)" : "Auto-killed trips");
        pieLastMonthTripKilled.setCenterTextSize(16);
        pieLastMonthTripKilled.setCenterTextTypeface(tf);

        //legend attributes
        Legend legend = pieLastMonthTripKilled.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextSize(12);
        legend.setFormSize(20);
        legend.setFormToTextSpace(2);

        pieLastMonthTripKilled.invalidate();

    }

    // Last last month shit
    private void drawLastLastMonthEditedTripsPieChart(){

        tf = txtUserTripsLastMonth.getTypeface();

        //pupulating list of PieEntires
        List<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(stats.lastLastMonthEditedTripCount, "Edited"));
        pieEntries.add(new PieEntry(stats.lastLastMonthTripCount - stats.lastLastMonthEditedTripCount, "Non-edited"));

        PieDataSet dataSet = new PieDataSet(pieEntries,"");
        dataSet.setColors(ColorTemplate.PASTEL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueTypeface(tf);
        data.setValueTextSize(14);

        //Get the chart
        pieLastLastMonthEdited.setData(data);
        pieLastLastMonthEdited.setDrawEntryLabels(false);
        pieLastLastMonthEdited.setContentDescription("");
        pieLastLastMonthEdited.setDrawMarkers(true);
        pieLastLastMonthEdited.setMaxHighlightDistance(34);
        pieLastLastMonthEdited.getData().setValueTypeface(tf);
        pieLastLastMonthEdited.setEntryLabelTextSize(16f);
        pieLastLastMonthEdited.setHoleRadius(70);
        pieLastLastMonthEdited.setDescription(null);

        pieLastLastMonthEdited.setUsePercentValues(false);
        pieLastLastMonthEdited.setCenterText(stats.lastLastMonthEditedTripCount == 0
                && stats.lastLastMonthTripCount == 0 ? "Edited trips\n(no data)" : "Edited trips");
        pieLastLastMonthEdited.setCenterTextSize(16);
        pieLastLastMonthEdited.setCenterTextTypeface(tf);

        //legend attributes
        Legend legend = pieLastLastMonthEdited.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextSize(12);
        legend.setFormSize(20);
        legend.setFormToTextSpace(2);

        // Fonts

        // Font to use

        pieLastLastMonthEdited.invalidate();

    }

    private void drawLastLastMonthManualTripsPieChart(){

        tf = txtUserTripsThisMonth.getTypeface();

        //pupulating list of PieEntires
        List<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(stats.lastLastMonthManTripCount, "Manual"));
        pieEntries.add(new PieEntry(stats.lastLastMonthTripCount - stats.lastLastMonthManTripCount, "Non-manual"));

        PieDataSet dataSet = new PieDataSet(pieEntries,"");
        dataSet.setColors(ColorTemplate.PASTEL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueTypeface(tf);
        data.setValueTextSize(14);

        //Get the chart
        pieLastLastMonthManual.setData(data);
        pieLastLastMonthManual.setDrawEntryLabels(false);
        pieLastLastMonthManual.setContentDescription("");
        pieLastLastMonthManual.setDrawMarkers(true);
        pieLastLastMonthManual.setMaxHighlightDistance(34);
        pieLastLastMonthManual.getData().setValueTypeface(tf);
        pieLastLastMonthManual.setEntryLabelTextSize(16f);
        pieLastLastMonthManual.setHoleRadius(70);
        pieLastLastMonthManual.setDescription(null);

        pieLastLastMonthManual.setUsePercentValues(false);
        pieLastLastMonthManual.setCenterText(stats.lastLastMonthManTripCount == 0
                && stats.lastLastMonthTripCount == 0 ? "Manual trips\n(no data)" : "Manual trips");
        pieLastLastMonthManual.setCenterTextSize(16);
        pieLastLastMonthManual.setCenterTextTypeface(tf);

        //legend attributes
        Legend legend = pieLastLastMonthManual.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextSize(12);
        legend.setFormSize(20);
        legend.setFormToTextSpace(2);

        pieLastLastMonthManual.invalidate();

    }

    private void drawLastLastMonthTripKilledPieChart(){

        tf = txtUserTripsLastMonth.getTypeface();

        //pupulating list of PieEntires
        List<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(stats.lastLastMonthTripMinderKillCount, "Auto-killed"));
        pieEntries.add(new PieEntry(stats.lastLastMonthTripCount - stats.lastLastMonthTripMinderKillCount, "Not killed"));

        PieDataSet dataSet = new PieDataSet(pieEntries,"");
        dataSet.setColors(ColorTemplate.PASTEL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueTypeface(tf);
        data.setValueTextSize(14);

        //Get the chart
        pieLastLastMonthTripKilled.setData(data);
        pieLastLastMonthTripKilled.setDrawEntryLabels(false);
        pieLastLastMonthTripKilled.setContentDescription("");
        pieLastLastMonthTripKilled.setDrawMarkers(true);
        pieLastLastMonthTripKilled.setMaxHighlightDistance(34);
        pieLastLastMonthTripKilled.getData().setValueTypeface(tf);
        pieLastLastMonthTripKilled.setEntryLabelTextSize(16f);
        pieLastLastMonthTripKilled.setHoleRadius(70);
        pieLastLastMonthTripKilled.setDescription(null);

        pieLastLastMonthTripKilled.setUsePercentValues(false);
        pieLastLastMonthTripKilled.setCenterText(stats.lastLastMonthTripMinderKillCount == 0
                && stats.lastLastMonthTripCount == 0 ? "Auto-killed trips\n(no data)" : "Auto-killed trips");
        pieLastLastMonthTripKilled.setCenterTextSize(16);
        pieLastLastMonthTripKilled.setCenterTextTypeface(tf);

        //legend attributes
        Legend legend = pieLastLastMonthTripKilled.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextSize(12);
        legend.setFormSize(20);
        legend.setFormToTextSpace(2);

        pieLastLastMonthTripKilled.invalidate();

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

    @SuppressLint("SetTextI18n")
    void parseStats() {

        DecimalFormat formatter = new DecimalFormat("#,###");

        // Trip count
        txtUserTripsThisMonth.setText(stats.thisMonthTripCount + "");

        // Mile tally
        String thisMonthVal = formatter.format(Helpers.Numbers.formatAsZeroDecimalPointNumber
                (stats.thisMonthTotalMiles, RoundingMode.UP)) + "";
        txtUserTripsLastMonth.setText(thisMonthVal);

        // Reimbursement
        txtUserTripsLastLastMonth.setText(Helpers.Numbers.convertToCurrency(stats.thisMonthTotalPayout));
    }
}