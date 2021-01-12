package com.fimbleenterprises.medimileage;

import androidx.appcompat.app.AppCompatActivity;
import cz.msebera.android.httpclient.Header;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.ArrayList;

public class FullscreenAccountTerritoryPicker extends AppCompatActivity implements
        ExpandableListView.OnGroupClickListener, ExpandableListView.OnGroupExpandListener,
        ExpandableListView.OnGroupCollapseListener, ExpandableListView.OnChildClickListener {

    public static final String ACCOUNT_RESULT = "ACCOUNT_RESULT";
    private static final String TAG = "TestExpandableList";
    public static final String CACHED_TERRITORIES = "CACHED_TERRITORIES";
    public static final String FOUND_TERRITORIES = "FOUND_TERRITORIES";

    MyExpandableListview listview;
    ExpandableBasicObjectListviewAdapter adapter;
    ArrayList<BasicObjects> parents = new ArrayList<>();
    ArrayList<Territory> territories = new ArrayList<>();
    Context context;

    CrmEntities.Accounts.Account selectedAccount;

    public static void showPicker(Activity callingActivity) {
        Intent intent = new Intent(callingActivity, FullscreenAccountTerritoryPicker.class);
        callingActivity.startActivityForResult(intent, 0);
    }

    public static void showPicker(Activity callingActivity, ArrayList<Territory> cachedTerritories) {
        Intent intent = new Intent(callingActivity, FullscreenAccountTerritoryPicker.class);
        intent.putExtra(CACHED_TERRITORIES, cachedTerritories);
        callingActivity.startActivityForResult(intent, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullscreen_account_territory_picker);
        this.context = this;
        listview = findViewById(R.id.expandableListview);

        // Caller can supply an arraylist of Territory objects which will be used instead of querying CRM
        if (getIntent().hasExtra(CACHED_TERRITORIES)) {
            if (getIntent().getParcelableExtra(CACHED_TERRITORIES) != null) {
                territories = getIntent().getParcelableExtra(CACHED_TERRITORIES);
                for (Territory t : territories) {
                    BasicObjects.BasicObject parentTerritoryObject = new BasicObjects.BasicObject(t.repName, t.territoryName, t);
                    BasicObjects parent = new BasicObjects(t.territoryName, parentTerritoryObject);
                    parents.add(parent);
                }

                populateListdata();
                return;
            }
        } else {
            Log.i(TAG, "onCreate No cached territories were supplied - must retrieve from CRM");
            getListData();
        }
    }

    void getListData() {
        final MyProgressDialog progressDialog = new MyProgressDialog(context, "Getting territories...");
        progressDialog.show();

        String query = Queries.Territories.getTerritoriesWithManagersAssigned();
        ArrayList<Requests.Argument> args = new ArrayList<>();
        args.add(new Requests.Argument("query", query));
        Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);
        new Crm().makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                territories = Territory.createMany(new String(responseBody));

                for (Territory t : territories) {
                    BasicObjects.BasicObject parentTerritoryObject = new BasicObjects.BasicObject(t.repName, t.territoryName, t);
                    BasicObjects parent = new BasicObjects(t.territoryName, parentTerritoryObject);
                    parents.add(parent);
                }

                populateListdata();
                progressDialog.dismiss();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    void populateListdata() {

        adapter = new ExpandableBasicObjectListviewAdapter(parents, listview, this);

        listview.setOnGroupExpandListener(this);
        listview.setOnGroupCollapseListener(this);
        listview.setOnGroupClickListener(this);
        listview.setOnChildClickListener(this);



        listview.setAdapter(adapter);
    }

    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {

        try {
            selectedAccount = (CrmEntities.Accounts.Account) parents.get(i).list.get(i1).object;
        } catch (Exception e) {
            e.printStackTrace();
            setResult(RESULT_CANCELED);
            Toast.makeText(context, "Failed to load territories and accounts!", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (selectedAccount != null) {
            // Build a result intent and set that result for the calling activity to consume
            Intent resultIntent = new Intent(ACCOUNT_RESULT);
            resultIntent.putExtra(ACCOUNT_RESULT, selectedAccount);
            resultIntent.putExtra(FOUND_TERRITORIES, territories);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            return false;
        }

        return true;
    }

    @Override
    public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
        //listview.collapseAll();
        return false;
    }

    @Override
    public void onGroupCollapse(int i) {

    }

    @Override
    public void onGroupExpand(final int groupIndex) {
        final BasicObjects parent = parents.get(groupIndex);

        if (parent != null) {
            if (parent.list.size() == 0) {
                final MyProgressDialog progressDialog = new MyProgressDialog(context, "Getting accounts...");
                progressDialog.show();

                Territory selectedTerritory = (Territory) parent.parentObject.object;

                String query = Queries.Accounts.getAccountsByTerritory(selectedTerritory.territoryid);
                ArrayList<Requests.Argument> args = new ArrayList<>();
                args.add(new Requests.Argument("query", query));
                Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);
                new Crm().makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        CrmEntities.Accounts accounts = new CrmEntities.Accounts(new String(responseBody));
                        for (CrmEntities.Accounts.Account a : accounts.list) {
                            BasicObjects.BasicObject accountObject = new BasicObjects.BasicObject(a.accountnumber, a.accountName, a);
                            parent.list.add(accountObject);
                        }
                        populateListdata();
                        listview.collapseAll();
                        listview.expandGroup(groupIndex);
                        progressDialog.dismiss();

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(getApplicationContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            } else {

            }
        }

        Log.i(TAG, "onGroupExpand ");
    }
}