package com.fimbleenterprises.medimileage;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.fimbleenterprises.medimileage.CrmEntities.Opportunities.Opportunity;

import org.joda.time.DateTime;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivityChooseOpportunity extends AppCompatActivity {

    private static final String TAG = "FullscreenActivityChooseTerritory";

    Context context;
    RecyclerView listView;
    ArrayList<BasicObject> objects = new ArrayList<>();
    BasicObjectRecyclerAdapter adapter;
    public static final int REQUESTCODE = 011;
    public static final String OPPORTUNITY_RESULT = "OPPORTUNITY_RESULT";
    public static final String FULLTRIP = "FULLTRIP";
    FullTrip fulltrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        setContentView(R.layout.activity_fullscreen_choose_opportunity);
        listView = findViewById(R.id.rvBasicObjects);

        Intent intent = getIntent();
        if (intent != null) {
            fulltrip = intent.getParcelableExtra(FULLTRIP);
            this.setTitle("Choose opportunity");
        }

        getOpportunities();

    }

    void getOpportunities() {
        final MyProgressDialog dialog = new MyProgressDialog(this, "Getting territories...");
        dialog.show();

        objects.clear();

        ArrayList<Opportunity> opportunities = TripAssociationManager.getNearbyOpportunities(fulltrip);
        for (Opportunity opportunity : opportunities) {
            BasicObject object = new BasicObject(opportunity.name, opportunity.accountname, opportunity);
            objects.add(object);
        }
        populateOpportunities();
        dialog.dismiss();
    }

    void populateOpportunities() {
        adapter = new BasicObjectRecyclerAdapter(this, objects);
        listView.setAdapter(adapter);
        listView.addItemDecoration(new DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL));
        listView.setLayoutManager(new LinearLayoutManager(context));
        adapter.setClickListener(new BasicObjectRecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Opportunity opportunity = (Opportunity) objects.get(position).object;

                Intent intent = new Intent(getApplicationContext(), OpportunityActivity.class);
                intent.putExtra(OpportunityActivity.OPPORTUNITY_TAG, opportunity);
                startActivity(intent);

                Intent resultIntent = new Intent(OPPORTUNITY_RESULT);
                resultIntent.putExtra(OPPORTUNITY_RESULT, opportunity);
                setResult(RESULT_OK, resultIntent);
                finish();
                Log.i(TAG, "onItemClick Position: " + position);
            }
        });

    }

}