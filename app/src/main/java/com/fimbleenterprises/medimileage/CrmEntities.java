package com.fimbleenterprises.medimileage;

import android.util.Log;

import com.google.rpc.Help;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Formatter;

import androidx.annotation.NonNull;

public class CrmEntities {

    public static class OrderProducts {
        public ArrayList<OrderProduct> list = new ArrayList<>();

        @Override
        public String toString() {
            return "Order products: " + this.list.size();
        }

        public OrderProducts(ArrayList<OrderProduct> orders) {
            this.list = orders;
        }

        public OrderProducts(String crmResponse) {
            this.list = OrderProduct.createMany(crmResponse);
        }
    }

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

        @Override
        public String toString() {
            return this.partNumber + ", Qty: " + this.qty + ", Amount: " + this.amount;
        }

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

    public static class Goals {

        private static final String TAG = "Goals";

        @Override
        public String toString() {
            return "Goals | size: " + list.size();
        }

        public ArrayList<Goal> list = new ArrayList<>();

        public Goals(ArrayList<Goal> goals) {
            this.list = goals;
        }

        public Goals(String crmResponse) {
            this.list = Goal.createMany(crmResponse);
        }

    }

    public static class Goal {

        public String goalid;
        public float pct;
        public String title;
        public String ownerid;
        public String ownername;
        public float target;
        public float actual;
        public int period;
        public int year;
        public String fiscalFirstDayFormatted;
        public String fiscalFirstDayValue;

        public String getPrettyPct() {
           return Helpers.Numbers.formatAsZeroDecimalPointNumber(this.pct, RoundingMode.UNNECESSARY) + "%";
        }

        public String getPrettyTarget() {
            return Helpers.Numbers.convertToCurrency(this.target);
        }

        public String getPrettyActual() {
            return Helpers.Numbers.convertToCurrency(this.actual);
        }

        @NonNull
        @Override
        public String toString() {
            return title + " Target: " + this.getPrettyTarget() + ", Actual: "
                    + this.getPrettyActual() + ", Pct: " + this.getPrettyPct();
        }

        public Goal(JSONObject json) {
            try {
                if (!json.isNull("goalid")) {
                    this.goalid = (json.getString("goalid"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("percentage")) {
                    this.pct = (json.getLong("percentage"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("title")) {
                    this.title = (json.getString("title"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_goalownerid_value")) {
                    this.ownerid = (json.getString("_goalownerid_value"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_goalownerid_valueFormattedValue")) {
                    this.ownername = (json.getString("_goalownerid_valueFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("targetmoney")) {
                    this.target = (json.getLong("targetmoney"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_fiscalfirstdayofmonthFormattedValue")) {
                    this.fiscalFirstDayFormatted = json.getString("msus_fiscalfirstdayofmonthFormattedValue");
                }
            } catch (Exception e) { }
            try {
                if (!json.isNull("msus_fiscalfirstdayofmonth")) {
                    this.fiscalFirstDayValue = json.getString("msus_fiscalfirstdayofmonth");
                    DateTime fiscalFirstDay = new DateTime(this.fiscalFirstDayValue);
                    this.period = fiscalFirstDay.getMonthOfYear();
                    this.year = fiscalFirstDay.getYear();
                }
            } catch (Exception e) { }
            try {
                if (!json.isNull("actualmoney")) {
                    this.actual = (json.getLong("actualmoney"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        public static ArrayList<Goal> createMany(String crmResponse) {
            ArrayList<Goal> goals = new ArrayList<>();
            try {
                JSONObject root = new JSONObject(crmResponse);
                JSONArray rootArray = root.getJSONArray("value");
                for (int i = 0; i < rootArray.length(); i++) {
                    Goal goal = new Goal(rootArray.getJSONObject(i));
                    goals.add(goal);
                }
                return goals;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        public static ArrayList<Goal> createMany(String crmResponse, int period, int year) {
            ArrayList<Goal> goals = new ArrayList<>();
            try {
                JSONObject root = new JSONObject(crmResponse);
                JSONArray rootArray = root.getJSONArray("value");
                for (int i = 0; i < rootArray.length(); i++) {
                    Goal goal = new Goal(rootArray.getJSONObject(i));
                    goal.period = period;
                    goal.year = year;
                    goals.add(goal);
                }
                return goals;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        public GoalSummary getGoalSummary(DateTime startDate, DateTime endDate, DateTime measureDate) {
            return new GoalSummary(this, measureDate, startDate, endDate);
        }

        public DateTime getStartDate() {
            return new DateTime(this.year, this.period, 1, 0, 0);
        }

        public DateTime getEndDate() {
            int daysInMonth = Helpers.DatesAndTimes.getDaysInMonth(this.year, this.period);
            return new DateTime(this.year, this.period, daysInMonth, 0, 0);
        }

        public int daysInMonth() {
            return Helpers.DatesAndTimes.getDaysInMonth(this.year, this.period);
        }

    }

}
