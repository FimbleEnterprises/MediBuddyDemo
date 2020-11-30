package com.fimbleenterprises.medimileage;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.impl.client.TargetAuthenticationStrategy;

import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class SearchResultsActivity extends AppCompatActivity {

    Context context;
    public RecyclerView recyclerView;
    RefreshLayout refreshLayout;
    AccountInventoryRecyclerAdapter adapter;
    CrmEntities.AccountProducts accountProducts ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_search_results);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        handleIntent(getIntent());

        refreshLayout = findViewById(R.id.refreshLayout);
        recyclerView = findViewById(R.id.orderLinesRecyclerview);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow

            Toast.makeText(this, query, Toast.LENGTH_SHORT).show();
            doSearch(Integer.parseInt(query));
        }
    }

    void doSearch(int arg) {
        final MyProgressDialog progressDialog = new MyProgressDialog(this, "Searching...");
        progressDialog.show();

        String query = Queries.Search.searchCustInventory(arg);
        Requests.Request request = new Requests.Request(Requests.Request.Function.GET);
        ArrayList<Requests.Argument> args = new ArrayList<>();
        args.add(new Requests.Argument("query", query));
        request.arguments = args;

        Crm crm = new Crm();
        crm.makeCrmRequest(this, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                accountProducts = new CrmEntities.AccountProducts(response);
                populateList();
                Toast.makeText(SearchResultsActivity.this, response, Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(SearchResultsActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    void populateList() {

        adapter = new AccountInventoryRecyclerAdapter(context, accountProducts.list);
        adapter.setClickListener(new AccountInventoryRecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
    }

}