package com.fimbleenterprises.medimileage.objects_and_containers;

import android.content.Context;
import android.util.Log;

import com.fimbleenterprises.medimileage.Crm;
import com.fimbleenterprises.medimileage.MyApp;
import com.fimbleenterprises.medimileage.MyInterfaces;
import com.fimbleenterprises.medimileage.MyPreferencesHelper;
import com.fimbleenterprises.medimileage.MySqlDatasource;
import com.fimbleenterprises.medimileage.QueryFactory;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class UserAddresses {

    private static final String TAG = "UserAddresses";
    public static final int ADDYS_VALID_FOR_X_DAYS = 7;
    public ArrayList<UserAddress> addresses = new ArrayList<>();

    public UserAddresses(String crmResponse) {
        try {
            JSONObject root = new JSONObject(crmResponse);
            JSONArray array = root.getJSONArray("value");
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                this.addresses.add(new UserAddress(obj));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getAllUserAddysFromCrm(final Context context, final MyInterfaces.GetUserAddysListener listener) {
        QueryFactory factory = new QueryFactory("systemuser");
        factory.addColumn("fullname");
        factory.addColumn("systemuserid");
        factory.addColumn("employeeid");
        factory.addColumn("address1_telephone1");
        factory.addColumn("address1_latitude");
        factory.addColumn("address1_longitude");
        factory.addColumn("address1_composite");

        QueryFactory.Filter.FilterCondition condition = new QueryFactory.Filter.FilterCondition("address1_composite",
                QueryFactory.Filter.Operator.CONTAINS_DATA);
        QueryFactory.Filter filter = new QueryFactory.Filter(QueryFactory.Filter.FilterType.AND, condition);
        factory.setFilter(filter);

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
                UserAddresses addys = new UserAddresses(crmJson);
                addys.save();
                listener.onSuccess(addys);
                Log.i(TAG, "onSuccess | found " + addys.addresses.size() + " addresses!");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                listener.onFailure(error.getLocalizedMessage());
                Log.w(TAG, "onFailure: " + error.getLocalizedMessage());
            }
        });

    }

    public static UserAddresses getSavedUserAddys() {
        return new MySqlDatasource().getUserAddys();
    }

    public String toGson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static UserAddresses fromGson(String gsonString) {
        Gson gson = new Gson();
        return gson.fromJson(gsonString, UserAddresses.class);
    }

    public void save() {
        MySqlDatasource ds = new MySqlDatasource();
        try {
            if (ds.getUserAddys() == null) {
                Log.i(TAG, "save|created:"+ds.createUserAddys(this));
            } else {
                Log.i(TAG, "save|updated:"+ds.updateUserAddys(this));
            }
            new MyPreferencesHelper(MyApp.getAppContext()).updateLastUpdatedUserAddys();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class UserAddress {

        public String fullname;
        public String userguid;
        public String territory;
        public String phonenumber;
        public double longitude;
        public double latitude;
        public String address;

        public UserAddress(JSONObject json) {
            Log.i(TAG, "UserAddress Raw json: " + json.toString());

            try {
                if (!json.isNull("employeeid")) {
                    this.territory = (json.getString("employeeid"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("systemuserid")) {
                    this.userguid = (json.getString("systemuserid"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("address1_composite")) {
                    this.address = (json.getString("address1_composite"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("address1_latitude")) {
                    this.latitude = (json.getDouble("address1_latitude"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("address1_longitude")) {
                    this.longitude = (json.getDouble("address1_longitude"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("address1_telephone1")) {
                    this.phonenumber = (json.getString("address1_telephone1"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("fullname")) {
                    this.fullname = (json.getString("fullname"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

}
