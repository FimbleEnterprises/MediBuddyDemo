package com.fimbleenterprises.demobuddy.objects_and_containers;

import com.fimbleenterprises.demobuddy.Helpers;
import com.google.android.gms.maps.model.Marker;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MyMapMarker {

    private static final String TAG = "MyMapMarker";

    public Marker marker;
    public String name;
    public boolean isAccount = true;
    public MapMarkerDetails details;
    public CrmEntities.CrmAddresses.CrmAddress accountAddress;
    public UserAddresses.UserAddress userAddress;

    public MyMapMarker() { }

    public MyMapMarker(CrmEntities.CrmAddresses.CrmAddress accountAddress) {
        this.accountAddress = accountAddress;
        this.isAccount = true;
    }

    public MyMapMarker(UserAddresses.UserAddress userAddress) {
        this.userAddress = userAddress;
        this.isAccount = false;
    }

    public static MyMapMarker get(Marker marker, ArrayList<MyMapMarker> markers) {
        for (MyMapMarker m : markers) {
            if (m.marker.getPosition().latitude == marker.getPosition().latitude &&
                    m.marker.getPosition().longitude == marker.getPosition().longitude) {
                return m;
            }
        }
        return null;
    }

    public MapMarkerDetails getDetails() {
        return this.details;
    }

    public static class MapMarkerDetails {

        String name;
        ArrayList<MapMarkerDetail> details = new ArrayList<>();
        String body;

        public MapMarkerDetails(String name, String crmResponse) {
            this.name = name;
            try {
                JSONObject rootObj = new JSONObject(crmResponse);
                JSONArray rootArray = rootObj.getJSONArray("value");
                for (int i = 0; i < rootArray.length(); i++) {
                    this.details.add(new MapMarkerDetail(rootArray.getJSONObject(i)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public float getTotalAmount() {

            float total = 0;

            for (MapMarkerDetail detail : this.details) {
                total += detail.amount;
            }

            return total;
        }

        public int getTotalOrders() {
            return details.size();
        }

        public String getSummary() {
            this.body = this.name + "\n" +
                    "Orders: " + getTotalOrders() + "\n" +
                    "Amount: " + Helpers.Numbers.convertToCurrency(getTotalAmount());
            return this.body;
        }

        public static class MapMarkerDetail {
            float amount;
            DateTime orderDate;
            String custyId;
            String custyName;
            String orderId;
        
            public MapMarkerDetail(JSONObject json) {
                try {
                    try {
                        if (!json.isNull("totalamount")) {
                            this.amount = (float) (json.getDouble("totalamount"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (!json.isNull("datefulfilled")) {
                            this.orderDate = (new DateTime(json.getString("datefulfilled")));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (!json.isNull("_customerid_value")) {
                            this.custyId = (json.getString("_customerid_value"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (!json.isNull("orderId")) {
                            this.orderId = (json.getString("orderId"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (!json.isNull("_customerid_valueFormattedValue")) {
                            this.custyName = (json.getString("_customerid_valueFormattedValue"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        
        }

    }

    interface GetDetailsListener {
        public void onSuccess(MapMarkerDetails orders);
        public void onFailure(String msg);
    }

}
