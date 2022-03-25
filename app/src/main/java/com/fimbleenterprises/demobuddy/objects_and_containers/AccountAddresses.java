package com.fimbleenterprises.demobuddy.objects_and_containers;

import android.content.Context;
import android.util.Log;

import com.fimbleenterprises.demobuddy.Crm;
import com.fimbleenterprises.demobuddy.MyApp;
import com.fimbleenterprises.demobuddy.MyInterfaces;
import com.fimbleenterprises.demobuddy.MyPreferencesHelper;
import com.fimbleenterprises.demobuddy.MySqlDatasource;
import com.fimbleenterprises.demobuddy.QueryFactory;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class AccountAddresses  {

    public static final String DEBUG_USERID_FOR_ACCOUNTS = "E4A46FDF-5B7C-E711-80D1-005056A32EEA";
    private static final String TAG = "MyAccounts";
    public static final int ADDYS_VALID_FOR_X_DAYS = 7;
    public ArrayList<AccountAddress> addresses = new ArrayList<>();

    public AccountAddresses() {

    }

    public AccountAddresses(String crmResponse) {
        Log.i(TAG, "MyAccounts " + crmResponse);

        JSONArray array = null;

        try {
            array = new JSONObject(crmResponse).getJSONArray("value");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = null;
                try {
                    json = array.getJSONObject(i);
                    this.addresses.add(new AccountAddress(json));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static AccountAddresses fromGson(String gsonString) {
        AccountAddresses accounts = new AccountAddresses();
        Gson gson = new Gson();
        accounts = gson.fromJson(gsonString, AccountAddresses.class);
        return accounts;
    }

    public static void getFromCrm(final Context context, final MyInterfaces.GetAccountsListener listener) {
        QueryFactory factory = new QueryFactory("col_customerinventory");
        factory.addColumn("col_name");
        factory.addColumn("col_accountid");
        factory.addColumn("msus_productstatus");
        factory.addColumn("statuscode");
        factory.addColumn("msus_revision");
        factory.addColumn("msus_serial_number_numeric");

        QueryFactory.Filter.FilterCondition condition1 = new QueryFactory.Filter.FilterCondition(
                "col_itemgroup",
                QueryFactory.Filter.Operator.EQUALS,
                "4050"
        );
        QueryFactory.Filter.FilterCondition condition2 = new QueryFactory.Filter.FilterCondition(
                "statuscode",
                QueryFactory.Filter.Operator.EQUALS,
                "1"
        );
        QueryFactory.Filter filter = new QueryFactory.Filter(
                QueryFactory.Filter.FilterType.AND
        );
        filter.conditions.add(condition1);
        filter.conditions.add(condition2);
        factory.setFilter(filter);

        QueryFactory.SortClause sortClause1 = new QueryFactory.SortClause("col_accountid",
                false, QueryFactory.SortClause.ClausePosition.ONE);
        QueryFactory.SortClause sortClause2 = new QueryFactory.SortClause("col_name",
                false, QueryFactory.SortClause.ClausePosition.TWO);
        factory.addSortClause(sortClause1);
        factory.addSortClause(sortClause2);

        QueryFactory.LinkEntity linkEntity = new QueryFactory.LinkEntity(
                "account",
                "accountid",
                "col_accountid",
                "a_92f7efedfc19e71180d2005056a36b9b"
        );
        linkEntity.addColumn(new QueryFactory.EntityColumn("address1_longitude"));
        linkEntity.addColumn(new QueryFactory.EntityColumn("address1_latitude"));
        linkEntity.addColumn(new QueryFactory.EntityColumn("address1_composite"));
        factory.addLinkEntity(linkEntity);

        String query = factory.construct();

        ArrayList<Requests.Argument> args = new ArrayList<>();
        Requests.Argument arg = new Requests.Argument("query", query);
        args.add(arg);
        Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);

        Crm crm = new Crm();
        crm.makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String crmJson = new String(responseBody);
                AccountAddresses accounts = new AccountAddresses(crmJson);
                ArrayList<AccountAddress> distinct = new ArrayList<>();
                for (AccountAddress addy : accounts.addresses) {
                    if (!contains(addy, distinct)) {
                        distinct.add(addy);
                    }
                }
                accounts.addresses = distinct;
                listener.onSuccess(accounts);
                Log.i(TAG, "onSuccess | found " + accounts.addresses.size() + " accounts!");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                listener.onFailure(error.getLocalizedMessage());
                Log.w(TAG, "onFailure: " + error.getLocalizedMessage());
            }
        });

    }

    private static boolean contains(AccountAddress addy, ArrayList<AccountAddress> distinctAccounts) {
        for (AccountAddress a : distinctAccounts) {
            if (addy.accountid.equals(a.accountid)) {
                return true;
            }
        }
        return false;
    }

    public static AccountAddresses getSavedActAddys() {
        return new MySqlDatasource().getAccounts();
    }

    public String toGson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public void save() {
        MySqlDatasource ds = new MySqlDatasource(MyApp.getAppContext());
        try {
            if (ds.getAccounts() == null) {
                Log.i(TAG, "save|created:"+ds.createAccounts(this));
            } else {
                Log.i(TAG, "save|updated:"+ds.updateAccounts(this));
            }
            new MyPreferencesHelper(MyApp.getAppContext()).updateLastUpdatedActAddys();
            Log.i(TAG, "save Accounts saved to local database!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "save: Failed to save accounts to local database!");
        }
    }

    @Override
    public String toString() {
        return this.addresses.size() + " accounts";
    }

    public static class AccountAddress {

        public String accountname;
        public String accountid;
        public String productnumber;
        public String productstatus;
        public double latitude;
        public double longitude;
        public int productsn;

        public AccountAddress(JSONObject json){

            /*
            "etag": "W/\"290264495\"",
			"_col_accountid_valueFormattedValue": "HCA Houston Healthcare Tomball",
			"_col_accountid_value": "9afa8157-2337-e711-80d4-005056a36b9b",
			"col_name": "VQ4122-P3",
			"col_customerinventoryid": "da76d285-56d2-e911-80f5-005056a36b9b",
			"statuscodeFormattedValue": "Returned",
			"statuscode": 181400005,
			"msus_serial_number_numericFormattedValue": "1687",
			"msus_serial_number_numeric": 1687,
			"a_92f7efedfc19e71180d2005056a36b9b_address1_longitudeFormattedValue": "-95.73795",
			"a_92f7efedfc19e71180d2005056a36b9b_address1_longitude": -95.73795,
			"a_92f7efedfc19e71180d2005056a36b9b_address1_composite": "605 Holderrieth Blvd\r\nTomball, TX 77375\r\nUSA",
			"a_92f7efedfc19e71180d2005056a36b9b_address1_latitudeFormattedValue": "38.20242",
			"a_92f7efedfc19e71180d2005056a36b9b_address1_latitude": 38.20242
             */

            try {
                if (!json.isNull("_col_accountid_valueFormattedValue")) {
                    this.accountname = (json.getString("_col_accountid_valueFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_col_accountid_value")) {
                    this.accountid = (json.getString("_col_accountid_value"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("col_name")) {
                    this.productnumber = (json.getString("col_name"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("statuscodeFormattedValue")) {
                    this.productstatus = (json.getString("statuscodeFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("a_92f7efedfc19e71180d2005056a36b9b_address1_latitude")) {
                    this.latitude = (json.getDouble("a_92f7efedfc19e71180d2005056a36b9b_address1_latitude"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("a_92f7efedfc19e71180d2005056a36b9b_address1_longitude")) {
                    this.longitude = (json.getDouble("a_92f7efedfc19e71180d2005056a36b9b_address1_longitude"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_serial_number_numeric")) {
                    this.productsn = (json.getInt("msus_serial_number_numeric"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        public void save() {

        }

        public static AccountAddress fromGson(String gson) {
            Gson gson1 = new Gson();
            return gson1.fromJson(gson, AccountAddress.class);
        }

        public String toGson() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }

        @Override
        public String toString() {
            return this.productnumber + " (" + this.accountname + ")";
        }
    }

}




































































