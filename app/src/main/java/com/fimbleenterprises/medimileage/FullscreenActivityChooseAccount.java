package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.CrmEntities.Accounts;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.joda.time.DateTime;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
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
    public static final String CACHED_ACCOUNTS = "CACHED_ACCOUNTS";
    Territory currentTerritory;
    Accounts.Account currentAccount;
    ArrayList<Accounts.Account> accounts;
    SmartRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        setContentView(R.layout.activity_fullscreen_choose_account);
        listView = findViewById(R.id.rvBasicObjects);
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                getAccounts();
            }
        });

        // Log a metric
        MileBuddyMetrics.updateMetric(this, MileBuddyMetrics.MetricName.LAST_ACCESSED_TERRITORY_CHANGER, DateTime.now());

        Intent intent = getIntent();
        if (intent != null) {
            currentTerritory = intent.getParcelableExtra(CURRENT_TERRITORY);
            currentAccount = intent.getParcelableExtra(CURRENT_ACCOUNT);
            // See if a cached account list was passed
            if (intent.getParcelableArrayListExtra(CACHED_ACCOUNTS) != null) {
                accounts = intent.getParcelableArrayListExtra(CACHED_ACCOUNTS);
            }
        }

        // If there are cached accounts then use them instead of going to CRM for them.
        if (accounts == null) {
            getAccounts();
        } else {
            populateAccounts();
        }

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

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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
                accounts = new Accounts(result).list;
                populateAccounts();
                Log.i(TAG, "onSuccess Response is " + result.length() + " chars long");
                progressDialog.dismiss();
                Intent intent = new Intent(ACCOUNT_RESULT);
                intent.putExtra(CACHED_ACCOUNTS, accounts);
                setResult(ACCOUNT_CHOSEN_RESULT, intent);
                refreshLayout.finishRefresh();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, "Failed!\n" + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                refreshLayout.finishRefresh();
            }
        });

    }

    void populateAccounts() {

        objects.clear();

        for (Accounts.Account account : accounts) {
            BasicObject object = new BasicObject(account.accountName, account.accountnumber
                    + " " + account.getAgreementTypeFormatted(), account);
            object.iconResource = R.mipmap.ic_business_black_24dp;
            if (currentAccount != null) {
                object.isSelected = currentAccount.accountid.equals(account.accountid);
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
                    intent.putExtra(CACHED_ACCOUNTS, accounts);
                    intent.putExtra(ACCOUNT_RESULT, account);
                    setResult(ACCOUNT_CHOSEN_RESULT, intent);
                    finish();
                    Log.i(TAG, "onItemClick Position: " + position);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}