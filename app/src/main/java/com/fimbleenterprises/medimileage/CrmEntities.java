package com.fimbleenterprises.medimileage;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CrmEntities {

    public static class OrderProduct {

        String productid;
        float amount;
        String salesorderdetailid;
        float price;
        int qty;
        boolean isCapital;
        DateTime orderDate;
        String accountnumber;
        String partNumber;
        String accountName;
        String productDescription;
        String salesrepid;

        public OrderProduct(JSONObject json) {
            try {
                if (!json.isNull("_productid_value")) {
                    this.productid = json.getString("_productid_value");
                }
            } catch (Exception e) { }
            try {
                if (!json.isNull("extendedamount")) {
                    this.amount = json.getLong("extendedamount");
                }
            } catch (Exception e) { }
            try {
                if (!json.isNull("salesorderdetailid")) {
                    this.salesorderdetailid = json.getString("salesorderdetailid");
                }
            } catch (Exception e) { }
            try {
                if (!json.isNull("priceperunit")) {
                    this.price = json.getLong("priceperunit");
                }
            } catch (Exception e) { }
            try {
                if (!json.isNull("quantity")) {
                    this.qty = json.getInt("quantity");
                }
            } catch (Exception e) { }
            try {
                if (!json.isNull("a_070ef9d142cd40d98bebd513e03c7cd1_msus_is_capital")) {
                    this.isCapital = json.getBoolean("a_070ef9d142cd40d98bebd513e03c7cd1_msus_is_capital");
                }
            } catch (Exception e) { }
            try {
                if (!json.isNull("ac_submitdate")) {
                    this.orderDate = new DateTime(json.getString("ac_submitdate"));
                }
            } catch (Exception e) { }
            try {
                if (!json.isNull("a_db24f99da8fee71180df005056a36b9b_accountnumber")) {
                    this.accountnumber = json.getString("a_db24f99da8fee71180df005056a36b9b_accountnumber");
                }
            } catch (Exception e) { }
            try {
                if (!json.isNull("a_070ef9d142cd40d98bebd513e03c7cd1_productnumber")) {
                    this.partNumber = json.getString("a_070ef9d142cd40d98bebd513e03c7cd1_productnumber");
                }
            } catch (Exception e) { }
            try {
                if (!json.isNull("a_db24f99da8fee71180df005056a36b9b_name")) {
                    this.accountName = json.getString("a_db24f99da8fee71180df005056a36b9b_name");
                }
            } catch (Exception e) { }
            try {
                if (!json.isNull("a_070ef9d142cd40d98bebd513e03c7cd1_name")) {
                    this.productDescription = json.getString("a_070ef9d142cd40d98bebd513e03c7cd1_name");
                }
            } catch (Exception e) { }
            try {
                if (!json.isNull("a_db24f99da8fee71180df005056a36b9b_msus_salesrep")) {
                    this.salesrepid = json.getString("a_db24f99da8fee71180df005056a36b9b_msus_salesrep");
                }
            } catch (Exception e) { }
        }

        public static ArrayList<OrderProduct> createMany(String crmResponse) {
            ArrayList<OrderProduct> orderProducts = new ArrayList<>();
            try {
                JSONObject root = new JSONObject(crmResponse);
                JSONArray rootArray = root.getJSONArray("value");
                for (int i = 0; i < rootArray.length(); i++) {
                    OrderProduct orderProduct = new OrderProduct(rootArray.getJSONObject(i));
                    orderProducts.add(orderProduct);
                }
                return orderProducts;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

    }

}
