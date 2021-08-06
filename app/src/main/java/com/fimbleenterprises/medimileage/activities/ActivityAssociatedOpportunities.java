package com.fimbleenterprises.medimileage.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.fimbleenterprises.medimileage.MyApp;
import com.fimbleenterprises.medimileage.MyInterfaces;
import com.fimbleenterprises.medimileage.adapters.BasicObjectRecyclerAdapter;
import com.fimbleenterprises.medimileage.dialogs.MyOkayOnlyDialog;
import com.fimbleenterprises.medimileage.dialogs.MyProgressDialog;
import com.fimbleenterprises.medimileage.objects_and_containers.BasicObjects;
import com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.R;

public class ActivityAssociatedOpportunities extends AppCompatActivity {
    private static final String TAG = "ActivityAssociatedOpportunities";
    public static final String OPPORTUNITY_LIST = "OPPORTUNITY_LIST";

    Context context;
    BasicObjectRecyclerAdapter adapter;
    RecyclerView listview;
    TextView txtNoOpportunities;

    @Override
    public boolean onSupportNavigateUp() {
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_associated_opportunities);
        context = this;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        CrmEntities.TripAssociations associations = CrmEntities.TripAssociations.fromGson(getIntent().getStringExtra(OPPORTUNITY_LIST));
        BasicObjects objects = associations.toBasicObjects();

        txtNoOpportunities = findViewById(R.id.txtNoOpportunities);
        txtNoOpportunities.setVisibility(objects.list.size() < 1 ? View.VISIBLE : View.GONE);

        adapter = new BasicObjectRecyclerAdapter(this, objects.list);
        listview = findViewById(R.id.opportunitiesRecyclerview);
        listview.setLayoutManager(new LinearLayoutManager(this));
        listview.setAdapter(adapter);
        adapter.setClickListener(new BasicObjectRecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                BasicObjects.BasicObject clickedObject = adapter.mData.get(position);
                CrmEntities.TripAssociations.TripAssociation mileageAssociation = (CrmEntities.TripAssociations.TripAssociation) clickedObject.object;

                final MyProgressDialog dialog = new MyProgressDialog(context, "Getting details...");
                dialog.show();

                CrmEntities.Opportunities.retrieveOpportunityDetails(mileageAssociation.associated_opportunity_id, new MyInterfaces.GetOpportunitiesListener() {
                    @Override
                    public void onSuccess(CrmEntities.Opportunities opportunities) {

                        if (opportunities.list.size() == 0) {
                            dialog.dismiss();
                            MyOkayOnlyDialog.show(context, "Opportunity was not found - perhaps" +
                                    " it has since been deleted?", new MyOkayOnlyDialog.OkayListener() {
                                @Override
                                public void onOkay() { Log.i(TAG, "onOkay - Do nothing."); }
                            });
                            return;
                        }

                        Intent intent = new Intent(context, BasicEntityActivity.class);
                        intent.putExtra(BasicEntityActivity.ACTIVITY_TITLE, "Opportunity Details");
                        intent.putExtra(BasicEntityActivity.ENTITYID, opportunities.list.get(0).entityid);
                        intent.putExtra(BasicEntityActivity.ENTITY_LOGICAL_NAME, "opportunity");
                        intent.putExtra(BasicEntityActivity.GSON_STRING, opportunities.list.get(0).toBasicEntity().toGson());
                        startActivityForResult(intent, BasicEntityActivity.REQUEST_BASIC);

                        dialog.dismiss();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
            }
        });

        Log.i(TAG, "onCreate ");



    }

    @Override
    protected void onStart() {
        super.onStart();
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
    protected void onStop() {
        super.onStop();
    }
}