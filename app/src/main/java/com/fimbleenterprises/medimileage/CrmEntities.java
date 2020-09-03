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

        public int size() {
            return this.list.size();
        }

        @Override
        public String toString() {
            return "Order products: " + this.list.size();
        }

        public OrderProducts() { }

        public OrderProducts(String crmResponse) {
            ArrayList<OrderProduct> orderProducts = new ArrayList<>();
            try {
                JSONObject root = new JSONObject(crmResponse);
                JSONArray rootArray = root.getJSONArray("value");
                for (int i = 0; i < rootArray.length(); i++) {
                    OrderProduct orderProduct = new OrderProduct(rootArray.getJSONObject(i));
                    orderProducts.add(orderProduct);
                }
                this.list = orderProducts;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static class OrderProduct {

        boolean isSeparator;
        String etag;
        String productid;
        String productidFormatted;
        String partNumber;
        float extendedAmt;
        String extendedAmtFormatted;
        String customerid;
        String customeridFormatted;
        String salesorderid;
        String salesorderidFormatted;
        String salesrepid;
        String salesrepidFormatted;
        float priceperunit;
        String priceperunitFormatted;
        String itemgroup;
        int qty;
        boolean isCapital;
        String territoryid;
        String territoryidFormatted;
        String accountnumber;
        String orderdateFormatted;
        DateTime orderDate;
        int productfamilyValue;
        String productfamilyFormattedValue;

        @Override
        public String toString() {
            return this.partNumber + ", Qty: " + this.qty + ", Amount: " + this.extendedAmtFormatted;
        }

        public OrderProduct() { }

        public OrderProduct(JSONObject json) {
            try {
                if (!json.isNull("etag")) {
                    this.etag = (json.getString("etag"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_productid_value")) {
                    this.productid = (json.getString("_productid_value"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_productid_valueFormattedValue")) {
                    this.productidFormatted = (json.getString("_productid_valueFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("extendedamount")) {
                    this.extendedAmt = (json.getLong("extendedamount"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("extendedamountFormattedValue")) {
                    this.extendedAmtFormatted = (json.getString("extendedamountFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_new_customer_value")) {
                    this.customerid = (json.getString("_new_customer_value"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_new_customer_valueFormattedValue")) {
                    this.customeridFormatted = (json.getString("_new_customer_valueFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_salesrepid_valueFormattedValue")) {
                    this.salesrepidFormatted = (json.getString("_salesrepid_valueFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_salesrepid_value")) {
                    this.salesrepid = (json.getString("_salesrepid_value"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("quantity")) {
                    this.qty = (json.getInt("quantity"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_salesorderid_valueFormattedValue")) {
                    this.salesorderidFormatted = (json.getString("_salesorderid_valueFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_salesorderid")) {
                    this.salesorderid = (json.getString("_salesorderid"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("a_070ef9d142cd40d98bebd513e03c7cd1_msus_is_capital")) {
                    this.isCapital = (json.getBoolean("a_070ef9d142cd40d98bebd513e03c7cd1_msus_is_capital"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("a_070ef9d142cd40d98bebd513e03c7cd1_productnumber")) {
                    this.setTitle(json.getString("a_070ef9d142cd40d98bebd513e03c7cd1_productnumber"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("a_070ef9d142cd40d98bebd513e03c7cd1_col_itemgroup")) {
                    this.itemgroup = (json.getString("a_070ef9d142cd40d98bebd513e03c7cd1_col_itemgroup"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("a_070ef9d142cd40d98bebd513e03c7cd1_col_producfamily")) {
                    this.productfamilyValue = (json.getInt("a_070ef9d142cd40d98bebd513e03c7cd1_col_producfamily"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("a_070ef9d142cd40d98bebd513e03c7cd1_col_producfamilyFormattedValue")) {
                    this.productfamilyFormattedValue = (json.getString("a_070ef9d142cd40d98bebd513e03c7cd1_col_producfamilyFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("a_db24f99da8fee71180df005056a36b9b_accountnumber")) {
                    this.accountnumber = (json.getString("a_db24f99da8fee71180df005056a36b9b_accountnumber"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("a_db24f99da8fee71180df005056a36b9b_territoryid")) {
                    this.territoryid = (json.getString("a_db24f99da8fee71180df005056a36b9b_territoryid"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("a_a1cf96c07c114d478335b8c445651a12_employeeid")) {
                    this.territoryidFormatted = (json.getString("a_a1cf96c07c114d478335b8c445651a12_employeeid"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("a_6ec0e72e4c104394bc627456c6412838_submitdate")) {
                    this.orderDate = (new DateTime(json.getString("a_6ec0e72e4c104394bc627456c6412838_submitdate")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("a_6ec0e72e4c104394bc627456c6412838_submitdateFormattedValue")) {
                    this.orderdateFormatted = (json.getString("a_6ec0e72e4c104394bc627456c6412838_submitdateFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public enum ProductFamily {
            AUX_CABLE, FLOWMETER, LEASE_COMPLIANCE, LICENSE_CARD, PROBE, PROBE_PRODUCT, SERVICE_MISC,
            SPARE_PART, SYSTEM_PRODUCT, SHIPPING_HANDLING
        }

        public ProductFamily getFamily(int productfamilyValue) {
            switch (productfamilyValue) {
                case 100004070 :
                    return ProductFamily.AUX_CABLE;
                case 181004050 :
                    return ProductFamily.FLOWMETER;
                case 100004040 :
                    return ProductFamily.LEASE_COMPLIANCE;
                case 100004030 :
                    return ProductFamily.LICENSE_CARD;
                case 181400001 :
                    return ProductFamily.PROBE_PRODUCT;
                case 100004010 :
                    return ProductFamily.SERVICE_MISC;
                case 100004020 :
                    return ProductFamily.SHIPPING_HANDLING;
                case 100004075 :
                    return ProductFamily.SPARE_PART;
                case 181400000 :
                    return ProductFamily.SYSTEM_PRODUCT;
                default :
                    return ProductFamily.PROBE;
            }
        }

        public void setTitle(String text) {
            this.partNumber = text;
        }
    }

    public static class Goals {

        private static final String TAG = "Goals";

        public int size() {
            return this.list.size();
        }

        @Override
        public String toString() {
            return "Goals | size: " + list.size();
        }

        public ArrayList<Goal> list = new ArrayList<>();

        public Goals(ArrayList<Goal> goals) {
            this.list = goals;
        }

        public Goals(String crmResponse) {
            ArrayList<Goal> goals = new ArrayList<>();
            try {
                JSONObject root = new JSONObject(crmResponse);
                JSONArray rootArray = root.getJSONArray("value");
                for (int i = 0; i < rootArray.length(); i++) {
                    Goal goal = new Goal(rootArray.getJSONObject(i));
                    goals.add(goal);
                }
                this.list = goals;
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
        public DateTime rawStartDate;
        public DateTime rawEndDate;

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
                if (!json.isNull("goalstartdate")) {
                    this.rawStartDate = (new DateTime(json.getString("goalstartdate")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("goalenddate")) {
                    this.rawEndDate = (new DateTime(json.getString("goalenddate")));
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
            return this.rawStartDate;
        }

        public DateTime getEndDate() {
            return this.rawEndDate;
        }

        public DateTime getEndDateForMonthlyGoal() {
            int daysInMonth = Helpers.DatesAndTimes.getDaysInMonth(this.year, this.period);
            return new DateTime(this.year, this.period, daysInMonth, 0, 0);
        }

        public DateTime getStartDateForMonthlyGoal() {
            return new DateTime(this.year, this.period, 1, 0, 0);
        }

        public int daysInMonth() {
            return Helpers.DatesAndTimes.getDaysInMonth(this.year, this.period);
        }

    }

}
