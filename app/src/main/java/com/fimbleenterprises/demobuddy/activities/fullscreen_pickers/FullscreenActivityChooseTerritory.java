package com.fimbleenterprises.demobuddy.activities.fullscreen_pickers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cz.msebera.android.httpclient.Header;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.fimbleenterprises.demobuddy.MyPreferencesHelper;
import com.fimbleenterprises.demobuddy.adapters.BasicObjectRecyclerAdapter;
import com.fimbleenterprises.demobuddy.objects_and_containers.BasicObjects;
import com.fimbleenterprises.demobuddy.Crm;
import com.fimbleenterprises.demobuddy.Helpers;
import com.fimbleenterprises.demobuddy.objects_and_containers.MileBuddyMetrics;
import com.fimbleenterprises.demobuddy.dialogs.MyProgressDialog;
import com.fimbleenterprises.demobuddy.CrmQueries;
import com.fimbleenterprises.demobuddy.R;
import com.fimbleenterprises.demobuddy.objects_and_containers.Requests;
import com.fimbleenterprises.demobuddy.objects_and_containers.Territories;
import com.fimbleenterprises.demobuddy.objects_and_containers.Territories.Territory;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.joda.time.DateTime;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivityChooseTerritory extends AppCompatActivity {

    private static final String TAG = "FullscreenActivityChooseTerritory";

    Context context;
    RecyclerView listView;
    ArrayList<BasicObjects.BasicObject> objects = new ArrayList<>();
    BasicObjectRecyclerAdapter adapter;
    public static final int REQUESTCODE = 011;
    public static final String TERRITORY_RESULT = "TERRITORY_RESULT";
    public static final String CURRENT_TERRITORY = "CURRENT_TERRITORY";
    public static final String CACHED_TERRITORIES = "CACHED_TERRITORIES";
    public static final int TERRITORY_CHOSEN_RESULT = 33;
    RefreshLayout refreshLayout;
    Territories.Territory currentTerritory;
    MyPreferencesHelper options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        options = new MyPreferencesHelper(this.context);
        setContentView(R.layout.activity_fullscreen_choose_territory);
        listView = findViewById(R.id.rvBasicObjects);
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                getTerritories();
            }
        });

        // Log a metric
        MileBuddyMetrics.updateMetric(this, MileBuddyMetrics.MetricName.LAST_ACCESSED_TERRITORY_CHANGER, DateTime.now());

        Intent intent = getIntent();
        if (intent != null) {
            Territory curTerritory = intent.getParcelableExtra(CURRENT_TERRITORY);
            if (curTerritory != null) {
                currentTerritory = curTerritory;
                this.setTitle("Choose territory");
            }

            // See if a territory list was passed in
            // v2.0 - territories are now cached in shared prefs much like contacts and accounts with an expiration date etc.
            // cachedTerritories = intent.getParcelableArrayListExtra(CACHED_TERRITORIES);
        }

        // Use cached territories if they exist
        if (options.hasCachedTerritories() && options.getCachedTerritories().list.size() > 0) {
            populateTerritories();
        } else {
            getTerritories();
        }

        Helpers.Views.MySwipeHandler mySwipeHandler = new Helpers.Views.MySwipeHandler(new Helpers.Views.MySwipeHandler.MySwipeListener() {
            @Override
            public void onSwipeLeft() {

            }

            @Override
            public void onSwipeRight() {
                onBackPressed();
            }
        });
        mySwipeHandler.addView(listView);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    void getTerritories() {
        final MyProgressDialog dialog = new MyProgressDialog(this, "Getting territories...");
        dialog.show();

        Crm crm = new Crm();
        ArrayList<Requests.Argument> args = new ArrayList<>();
        Requests.Argument argument = new Requests.Argument("query", CrmQueries.Territories
                .getTerritoriesWithManagersAssigned());
        args.add(argument);
        Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);

        crm.makeCrmRequest(this, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                try {
                    options.cacheTerritories(new Territories(result));
                    populateTerritories();
                    // This will return the cached territories even if a territory is not selected.
                    Intent intent = new Intent(TERRITORY_RESULT);
                    intent.putExtra(TERRITORY_RESULT, currentTerritory);
                    setResult(TERRITORY_CHOSEN_RESULT, intent);
                    dialog.dismiss();
                    refreshLayout.finishRefresh();
                    Log.i(TAG, "onSuccess Response is " + result.length() + " chars long");
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Failed to get territories\n" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, "Failed!\n" + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                refreshLayout.finishRefresh();
            }
        });

    }

    void populateTerritories() {

        objects.clear();
        for (Territory t : options.getCachedTerritories().list) {
            BasicObjects.BasicObject basicObject = new BasicObjects.BasicObject(t.territoryName, t.repName, t);
            basicObject.iconResource = R.drawable.next32;
            if (currentTerritory != null && currentTerritory.territoryid.equals(t.territoryid)) {
                basicObject.isSelected = true;
            }
            objects.add(basicObject);
        }

        adapter = new BasicObjectRecyclerAdapter(this, objects);
        listView.setAdapter(adapter);
        listView.addItemDecoration(new DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL));
        listView.setLayoutManager(new LinearLayoutManager(context));
        adapter.setClickListener(new BasicObjectRecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Territory territory = (Territory) objects.get(position).object;
                Intent intent = new Intent(TERRITORY_RESULT);
                intent.putExtra(TERRITORY_RESULT, territory);
                setResult(TERRITORY_CHOSEN_RESULT, intent);
                finish();
                Log.i(TAG, "onItemClick Position: " + position);
            }
        });
    }

}