package com.fimbleenterprises.demobuddy.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.fimbleenterprises.demobuddy.R;
import com.fimbleenterprises.demobuddy.adapters.BasicObjectRecyclerAdapter;
import com.fimbleenterprises.demobuddy.objects_and_containers.AggregatedSales;
import com.fimbleenterprises.demobuddy.objects_and_containers.BasicObjects;

import java.util.ArrayList;

public class AggregateSalesActivity extends AppCompatActivity {

    public static final String AGGREGATED_TOTALS = "AGGREGATED_TOTALS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aggregate_sales);

        RecyclerView listview = findViewById(R.id.recyclerview);

        AggregatedSales totals = (AggregatedSales) getIntent().getParcelableExtra(AGGREGATED_TOTALS);
        ArrayList<BasicObjects.BasicObject> objects = totals.toBasicObjects();

        BasicObjectRecyclerAdapter adapter = new BasicObjectRecyclerAdapter(this, objects);
        listview.setLayoutManager(new LinearLayoutManager(this));
        listview.setAdapter(adapter);
        adapter.setClickListener(new BasicObjectRecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(AggregateSalesActivity.this, "Clicked: " + adapter.mData
                        .get(position).middleText, Toast.LENGTH_SHORT).show();
                
            }
        });

    }
}