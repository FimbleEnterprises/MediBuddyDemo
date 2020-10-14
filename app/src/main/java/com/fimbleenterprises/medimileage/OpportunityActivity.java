package com.fimbleenterprises.medimileage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import cz.msebera.android.httpclient.Header;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.CrmEntities.Opportunities.Opportunity;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.ArrayList;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class OpportunityActivity extends AppCompatActivity {
    private static final String TAG = "OpportunityActivity";
    public static final String OPPORTUNITY_TAG = "OPPORTUNITY_TAG";
    public Opportunity opportunity;
    NonScrollRecyclerView notesListView;
    AnnotationsAdapter adapterNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_opportunity);

        setTitle("Opportunity Details");

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(OPPORTUNITY_TAG)) {
            opportunity = intent.getParcelableExtra(OPPORTUNITY_TAG);
            Log.i(TAG, "onCreate Opportunity was passed (" + opportunity.name + ")");
            Toast.makeText(this,  opportunity.name, Toast.LENGTH_SHORT).show();
            getOpportunityDetails();
        }

        notesListView = (NonScrollRecyclerView) findViewById(R.id.notesRecyclerView);

    }

    void getOpportunityDetails() {
        ArrayList<Requests.Argument> args = new ArrayList<>();
        Requests.Argument argument = new Requests.Argument("query", Queries.Annotations.getAnnotations(opportunity.opportunityid));
        args.add(argument);
        Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);

        Crm crm = new Crm();
        crm.makeCrmRequest(this, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                CrmEntities.Annotations annotations = new CrmEntities.Annotations(response);
                adapterNotes = new AnnotationsAdapter(getApplicationContext(), annotations.list);
                notesListView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                notesListView.setAdapter(adapterNotes);
                notesListView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                        DividerItemDecoration.VERTICAL));
                Toast.makeText(OpportunityActivity.this, "Notes: " + annotations.list.size(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.w(TAG, "onFailure: " + error.getLocalizedMessage());
                Toast.makeText(getApplicationContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }
}