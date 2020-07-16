package com.fimbleenterprises.medimileage;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import cz.msebera.android.httpclient.Header;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.QueryFactory.Filter;
import com.fimbleenterprises.medimileage.QueryFactory.Filter.Operator;
import com.fimbleenterprises.medimileage.QueryFactory.Filter.FilterCondition;
import com.fimbleenterprises.medimileage.QueryFactory.SortClause;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class AggregateStatsActivity extends AppCompatActivity {

    private static final String TAG = "AggregateStats";
    AggregateStats stats;

    TextView txtCpyWideTripsThisMonth;
    TextView txtCpyWideMilesThisMonth;
    TextView txtCpyWideReimbursementThisMonth;
    TextView txtCpyWideTripsLastMonth;
    TextView txtCpyWideMilesLastMonth;
    TextView txtCpyWideReimbursementLastMonth;
    TextView txtCpyWideTripsLastLastMonth;
    TextView txtCpyWideMilesLastLastMonth;
    TextView txtCpyWideReimbursementLastLastMonth;

    TextView txtDriver1ThisMonth;
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
    TextView txtDriver3LastLastMonthValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aggregate_stats);

        this.setTitle("Statistics");

        // Company-wide this month
        txtCpyWideTripsThisMonth = findViewById(R.id.textView_tripCountThisMonth);
        txtCpyWideTripsLastMonth = findViewById(R.id.textView_tripCountLastMonth);
        txtCpyWideTripsLastLastMonth = findViewById(R.id.textView_tripCountLastLastMonth);

        // Company-wide 1 month ago
        txtCpyWideMilesThisMonth = findViewById(R.id.textView_totalMilesThisMonth);
        txtCpyWideMilesLastMonth = findViewById(R.id.textView_totalMilesLastMonth);
        txtCpyWideMilesLastLastMonth = findViewById(R.id.textView_totalMilesLastLastMonth);

        // Company-wide 2 months ago
        txtCpyWideReimbursementThisMonth = findViewById(R.id.textView_ReimbursementThisMonth);
        txtCpyWideReimbursementLastMonth = findViewById(R.id.textView_ReimbursementLastMonth);
        txtCpyWideReimbursementLastLastMonth = findViewById(R.id.textView_ReimbursementLastLastMonth);

        // Top 3 Driver names
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
        txtDriver3LastLastMonthValue = findViewById(R.id.textView_driver3_LastLastMonth_value);

        // Create the navigation up button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

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

    void getStats() {

        final MyProgressDialog dialog = new MyProgressDialog(this, "Getting stats...");
        dialog.show();

        QueryFactory factory = new QueryFactory("msus_fulltrip");
        factory.addColumn("msus_name");
        factory.addColumn("msus_dt_tripdate");
        factory.addColumn("msus_trip_duration");
        factory.addColumn("msus_totaldistance");
        factory.addColumn("msus_reimbursement");
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
        // This month
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
            txtDriver3LastLastMonthValue.setText(Helpers.Numbers.convertToCurrency(stats.topUserMilesLastLastMonth.get(2).totalReimbursement));
    }
}