package com.fimbleenterprises.medimileage.activities.fullscreen_pickers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.MyPreferencesHelper;
import com.fimbleenterprises.medimileage.adapters.BasicObjectRecyclerAdapter;
import com.fimbleenterprises.medimileage.objects_and_containers.BasicObjects;
import com.fimbleenterprises.medimileage.Crm;
import com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities.Accounts;
import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.objects_and_containers.MileBuddyMetrics;
import com.fimbleenterprises.medimileage.dialogs.MyProgressDialog;
import com.fimbleenterprises.medimileage.CrmQueries;
import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.objects_and_containers.Requests;
import com.fimbleenterprises.medimileage.objects_and_containers.Territories.Territory;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.joda.time.DateTime;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    RelativeLayout mainLayout;
    ArrayList<BasicObjects.BasicObject> objects = new ArrayList<>();
    BasicObjectRecyclerAdapter adapter;
    public static final int REQUESTCODE = 011;
    public static final String ACCOUNT_RESULT = "ACCOUNT_RESULT";
    public static final String CURRENT_ACCOUNT = "CURRENT_ACCOUNT";
    // public static final String CACHED_ACCOUNTS = "CACHED_ACCOUNTS";
    Territory currentTerritory;
    Accounts.Account currentAccount;
    ArrayList<Accounts.Account> accounts;
    ArrayList<Territory> cachedTerritories;
    SmartRefreshLayout refreshLayout;
    AutoCompleteTextView txtFilter;
    String filterString;
    MyPreferencesHelper options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        options = new MyPreferencesHelper(context);
        setContentView(R.layout.activity_fullscreen_choose_account);
        listView = findViewById(R.id.rvBasicObjects);
        mainLayout = findViewById(R.id.main_layout);
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                getAccounts();
            }
        });
        txtFilter = findViewById(R.id.autoCompleteTextView);
        txtFilter.setText("");
        // txtFilter.setFocusedByDefault(true);
        // txtFilter.requestFocus();
        txtFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence != null && charSequence.length() > 0) {
                    filterString = charSequence.toString();
                    populateAccounts(filterString);
                } else {
                    filterString = null;
                    populateAccounts();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Log a metric
        MileBuddyMetrics.updateMetric(this, MileBuddyMetrics.MetricName.LAST_ACCESSED_TERRITORY_CHANGER, DateTime.now());

        Intent intent = getIntent();
        if (intent != null) {
            currentTerritory = intent.getParcelableExtra(CURRENT_TERRITORY);
            currentAccount = intent.getParcelableExtra(CURRENT_ACCOUNT);
        }

        if (options.hasCachedAccounts()) {
            try {
                accounts = options.getCachedAccounts().list;
            } catch (Exception e) {
                e.printStackTrace();
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
        mySwipeHandler.addView(mainLayout);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "onActivityResult ");

        if (data != null && resultCode == RESULT_OK) {
            Territory newTerritory = data.getParcelableExtra(FullscreenActivityChooseTerritory.TERRITORY_RESULT);
            if (currentTerritory == null || newTerritory == null) {
                return;
            }
            if (!newTerritory.territoryid.equals(currentTerritory.territoryid)) {
                Log.i(TAG, "onActivityResult New territory chosen!");
                currentTerritory = newTerritory;
                getAccounts();
            }
        }

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

    /**
     * Shows a picker for accounts.  Will return an intent with the selected account as an object with a tag of: CHOICE_RESULT
     * @param activity An activity that can raise an OnActivityResult event.
     * @param currentAccount The current account if applicable.
     * @param territory The territory to limit the results to.
     */
    public static void showPicker(Activity activity, Accounts.Account currentAccount, Territory territory, int requestCode) {

        Intent intent = new Intent(activity, FullscreenActivityChooseAccount.class);
        intent.putExtra(CURRENT_ACCOUNT, currentAccount);
        intent.putExtra(CURRENT_TERRITORY, territory);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Shows a picker for accounts.  Will return an intent with the selected account as an object with a tag of: CHOICE_RESULT
     * @param activity An activity that can raise an OnActivityResult event.
     * @param territory The territory to limit the results to.
     */
    public static void showPicker(Activity activity, Territory territory, int requestCode) {

        Intent intent = new Intent(activity, FullscreenActivityChooseAccount.class);
        intent.putExtra(CURRENT_TERRITORY, territory);
        activity.startActivityForResult(intent, requestCode);
    }

    void getAccounts() {
        final MyProgressDialog progressDialog = new MyProgressDialog(this, "Getting accounts...");
        progressDialog.show();

        Crm crm = new Crm();
        ArrayList<Requests.Argument> args = new ArrayList<>();
        // Requests.Argument argument = new Requests.Argument("query", CrmQueries.Accounts.getAccountsByTerritory(currentTerritory.territoryid));
        Requests.Argument argument = new Requests.Argument("query", CrmQueries.Accounts.getAccounts(null));
        args.add(argument);
        Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);

        crm.makeCrmRequest(this, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                Accounts objAccounts = new Accounts(result);
                accounts = objAccounts.list;
                options.cacheAccounts(objAccounts);
                populateAccounts();
                Log.i(TAG, "onSuccess Response is " + result.length() + " chars long");
                progressDialog.dismiss();
                Intent intent = new Intent(ACCOUNT_RESULT);
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
        populateAccounts(null);
    }

    void populateAccounts(String filter) {

        objects.clear();

        for (Accounts.Account account : accounts) {

            // Convert account object to BasicObject
            BasicObjects.BasicObject object = new BasicObjects.BasicObject(account.accountName, account.accountnumber
                    + " " + account.getAgreementTypeFormatted(), account);

            // Set its icon
            object.iconResource = R.mipmap.ic_business_black_24dp;

            // See if it is currently selected so it can be reflected as such in the list.
            if (currentAccount != null) {
                object.isSelected = currentAccount.entityid.equals(account.entityid);
            }

            // Apply filter if applicable.
            if (filter != null) {
                filter = filter.toLowerCase();

                // If it's an account number we filter on that property.
                if (Helpers.Numbers.isNumeric(filter)) {
                    if (account.accountnumber.toLowerCase().contains(filter)) {
                        objects.add(object);
                    }
                } else { // Otherwise we filter on name.
                    if (account.accountName.toLowerCase().contains(filter)) {
                        objects.add(object);
                    }
                }
            } else { // Filter not applicable - just add.
                objects.add(object);
            }
        }

        // Create adapter, setup its click listener, set adapter to the list and populate.
        adapter = new BasicObjectRecyclerAdapter(this, objects);
        listView.setAdapter(adapter);
        listView.addItemDecoration(new DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL));
        listView.setLayoutManager(new LinearLayoutManager(context));
        adapter.setClickListener(new BasicObjectRecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                try {
                    // This started failing as the parcel was too big for an intent.  Need a new approach.
                    /*Accounts.Account account = (Accounts.Account) objects.get(position).object;
                    Intent intent = new Intent(ACCOUNT_RESULT);
                    intent.putExtra(CACHED_ACCOUNTS, accounts);
                    intent.putExtra(ACCOUNT_RESULT, account);
                    setResult(ACCOUNT_CHOSEN_RESULT, intent);
                    finishActivity(REQUESTCODE);
                    Log.i(TAG, "onItemClick Position: " + position);*/

                    Accounts.Account account = (Accounts.Account) objects.get(position).object;
                    Intent intent = new Intent(ACCOUNT_RESULT);
                    // intent.putExtra(CACHED_ACCOUNTS, accounts);
                    intent.putExtra(ACCOUNT_RESULT, account);
                    setResult(ACCOUNT_CHOSEN_RESULT, intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}