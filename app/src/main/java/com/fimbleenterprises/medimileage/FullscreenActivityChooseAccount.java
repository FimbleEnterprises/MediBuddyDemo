package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.CrmEntities.Accounts;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.joda.time.DateTime;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cz.msebera.android.httpclient.Header;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivityChooseAccount extends AppCompatActivity {

    public static final String CHANGE_ACCOUNT = "CHANGE_ACCOUNT";
    public static final String CURRENT_TERRITORY = "CURRENT_TERRITORY";
    private static final String TAG = "FullscreenActivityChooseTerritory";
    public static final int ACCOUNT_CHOSEN_RESULT = 9191;

    Context context;
    RecyclerView listView;
    ArrayList<BasicObject> objects = new ArrayList<>();
    BasicObjectRecyclerAdapter adapter;
    public static final int REQUESTCODE = 011;
    public static final String ACCOUNT_RESULT = "ACCOUNT_RESULT";
    public static final String CURRENT_ACCOUNT = "CURRENT_ACCOUNT";
    Territory currentTerritory;
    String currentAccountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        setContentView(R.layout.activity_fullscreen_choose_account);
        listView = findViewById(R.id.rvBasicObjects);

        // Log a metric
        MileBuddyMetrics.updateMetric(this, MileBuddyMetrics.MetricName.LAST_ACCESSED_TERRITORY_CHANGER, DateTime.now());

        Intent intent = getIntent();
        if (intent != null) {
            Territory curTerritory = intent.getParcelableExtra(CURRENT_TERRITORY);
            if (curTerritory != null) {
                currentTerritory = curTerritory;
                this.setTitle("Choose territory");
            }
        }

        getAccounts();

        Helpers.Views.MySwipeHandler mySwipeHandler = new Helpers.Views.MySwipeHandler(new Helpers.Views.MySwipeHandler.MySwipeListener() {
            @Override
            public void onSwipeLeft() {  }

            @Override
            public void onSwipeRight() {
                onBackPressed();
            }
        });
        mySwipeHandler.addView(listView);

    }

    void getAccounts() {
        final MyProgressDialog progressDialog = new MyProgressDialog(this, "Getting accounts...");
        progressDialog.show();

        Crm crm = new Crm();
        ArrayList<Requests.Argument> args = new ArrayList<>();
        Requests.Argument argument = new Requests.Argument("query", Queries.Accounts.getAccountsByTerritory(currentTerritory.territoryid));
        args.add(argument);
        Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);

        crm.makeCrmRequest(this, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                Accounts accounts = new Accounts(result);
                populateAccounts(accounts);
                Log.i(TAG, "onSuccess Response is " + result.length() + " chars long");
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, "Failed!\n" + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    void populateAccounts(Accounts accounts) {

        objects.clear();

        MySettingsHelper options = new MySettingsHelper(context);
        Accounts.Account lastSelectedAccount = options.getLastAccountSelected();

        for (Accounts.Account account : accounts.list) {
            BasicObject object = new BasicObject(account.accountName, account.accountnumber, account);
            if (lastSelectedAccount != null) {
                object.isSelected = lastSelectedAccount.accountid.equals(account.accountid);
            }
            objects.add(object);
        }

        adapter = new BasicObjectRecyclerAdapter(this, objects);
        listView.setAdapter(adapter);
        listView.addItemDecoration(new DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL));
        listView.setLayoutManager(new LinearLayoutManager(context));
        adapter.setClickListener(new BasicObjectRecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                try {
                    Accounts.Account account = (Accounts.Account) objects.get(position).object;
                    Intent intent = new Intent(ACCOUNT_RESULT);
                    intent.putExtra(ACCOUNT_RESULT, account);
                    setResult(ACCOUNT_CHOSEN_RESULT, intent);
                    MySettingsHelper options = new MySettingsHelper(context);
                    options.setLastAccountSelected(account);
                    finish();
                    Log.i(TAG, "onItemClick Position: " + position);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}