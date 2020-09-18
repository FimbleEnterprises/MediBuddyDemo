package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.fimbleenterprises.medimileage.Containers.EntityContainer;
import com.fimbleenterprises.medimileage.Containers.EntityField;
import com.fimbleenterprises.medimileage.Requests.Request.Function;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import cz.msebera.android.httpclient.Header;

public class CrmEntities {

    public interface GetTripAssociationsListener {
        public void onSuccess(TripAssociations associations);
        public void onFailure(String msg);
    }

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
        public String territoryid;
        public String territoryname;

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
                if (!json.isNull("a_1124b4bdf013df11a16e00155d7aa40d_employeeid")) {
                    this.territoryname = (json.getString("a_1124b4bdf013df11a16e00155d7aa40d_employeeid"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("a_1124b4bdf013df11a16e00155d7aa40d_territoryid")) {
                    this.territoryid = (json.getString("a_1124b4bdf013df11a16e00155d7aa40d_territoryid"));
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

    public static class CrmAddresses {
        public ArrayList<CrmAddress> list = new ArrayList<>();

        public String toJson() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }

        @Override
        public String toString() {
            return this.list.size() + " addresses, ";
        }

        public CrmAddresses(ArrayList<CrmAddress> addresses) {
            this.list = addresses;
        }

        public CrmAddresses(String crmResponse) {
            ArrayList<CrmAddress> addys = new ArrayList<>();
            try {
                JSONObject root = new JSONObject(crmResponse);
                JSONArray rootArray = root.getJSONArray("value");
                for (int i = 0; i < rootArray.length(); i++) {
                    CrmAddress addy = new CrmAddress(rootArray.getJSONObject(i));
                    addys.add(addy);
                }
                this.list = addys;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public static class CrmAddress {

            public String etag;
            public String accountid;
            public String accountnumber;
            public int customertypeValue;
            public String customertypeFormatted;
            public String accountName;
            public String addressComposite;
            public double latitude;
            public double longitude;
            public double latitude_precise;
            public double longitude_precise;

            public CrmAddress(JSONObject json) {
                try {
                    if (!json.isNull("etag")) {
                        this.etag = (json.getString("etag"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("accountid")) {
                        this.accountid = (json.getString("accountid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("accountnumber")) {
                        this.accountnumber = (json.getString("accountnumber"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("customertypecodeFormattedValue")) {
                        this.customertypeFormatted = (json.getString("customertypecodeFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("customertypecode")) {
                        this.customertypeValue = (json.getInt("customertypecode"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("name")) {
                        this.accountName = (json.getString("name"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("address1_composite")) {
                        this.addressComposite = (json.getString("address1_composite"));
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
                    if (!json.isNull("address1_latitude")) {
                        this.latitude = (json.getDouble("address1_latitude"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_latitudeprecise")) {
                        this.latitude_precise = (json.getDouble("msus_latitudeprecise"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_longitudeprecise")) {
                        this.longitude_precise = (json.getDouble("msus_longitudeprecise"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String toString() {
                return accountName + ", Addy: " + addressComposite;
            }

            public LatLng getLatLng() {
                return new LatLng(this.latitude, this.longitude);
            }

            /**
             * Calculate the distance between this location and the supplied location.
             * @param latLng The location to measure to.
             * @return The distance between the two locations in meters.
             */
            public double distanceTo(LatLng latLng) {
                Location startLoc = new Location("START");
                Location endLoc = new Location("END");

                startLoc.setLatitude(latLng.latitude);
                startLoc.setLongitude(latLng.longitude);

                endLoc.setLatitude(this.latitude);
                endLoc.setLongitude(this.longitude);

                return startLoc.distanceTo(endLoc);
            }

        }

    }

    public static class TripAssociations {

        private static final String TAG = "MileageReimbursementAssociations";

        public ArrayList<TripAssociation> list = new ArrayList<>();

        public TripAssociations() {
            this.list = new ArrayList<>();
        }

        public TripAssociations(String crmResponse) {
            ArrayList<TripAssociation> associations = new ArrayList<>();
            try {
                JSONObject root = new JSONObject(crmResponse);
                JSONArray rootArray = root.getJSONArray("value");
                for (int i = 0; i < rootArray.length(); i++) {
                    TripAssociation association = new TripAssociation(rootArray.getJSONObject(i));
                    associations.add(association);
                }
                this.list = associations;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void addAssociation(TripAssociation association) {
            this.list.add(association);
        }

        private static void getAssociationsByTripId(final Context context, String tripid, final GetTripAssociationsListener listener) {
            String query = Queries.TripAssociation.getAssociationsByTripid(tripid);
            Requests.Request request = new Requests.Request(Function.GET);
            ArrayList<Requests.Argument> args = new ArrayList<>();
            Requests.Argument argument1 = new Requests.Argument("query", query);
            request.arguments.add(argument1);

            Crm crm = new Crm();
            crm.makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    TripAssociations associations = new TripAssociations(new String(responseBody));
                    Log.i(TAG, "onSuccess Found: " + associations.list.size() + " associations.");
                    listener.onSuccess(associations);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.w(TAG, "onFailure: " + error.getLocalizedMessage());
                    listener.onFailure(error.getLocalizedMessage());
                }
            });
        }

        private static void deleteCrmAssociations(Context context, TripAssociations associations, final MyInterfaces.EntityUpdateListener listener) {
            ArrayList<String> guids = new ArrayList<>();
            for (TripAssociation association : associations.list) {
                guids.add(association.associated_trip_id);
            }

            Requests.Request request = new Requests.Request(Function.DELETE_MANY);
            Requests.Argument argument1 = new Requests.Argument("entityname", "msus_mileageassociation");
            Requests.Argument argument2 = new Requests.Argument("guids", guids);
            Requests.Argument argument3 = new Requests.Argument("asuserid", MediUser.getMe().systemuserid);
            request.arguments.add(argument1);
            request.arguments.add(argument2);
            request.arguments.add(argument3);

            Crm crm = new Crm();
            crm.makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    listener.onSuccess();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    listener.onFailure(error.getLocalizedMessage());
                }
            });

        }

        public static void uploadTripAssociations(Context context, TripAssociations associations, final MyInterfaces.EntityUpdateListener listener) {
            if (associations.list.size() == 0) {
                listener.onFailure("No associations to upload!");
            }

            Requests.Request request = new Requests.Request(Function.CREATE_MANY);

            ArrayList<Requests.Argument> args = new ArrayList<>();
            Requests.Argument argument1 = new Requests.Argument("entityName", "msus_mileageassociation");
            Requests.Argument argument2 = new Requests.Argument("asUserid", MediUser.getMe().systemuserid);
            Requests.Argument argument3 = new Requests.Argument("containers", associations.toContainers().toJson());;
            args.add(argument1);
            args.add(argument2);
            args.add(argument3);
            request.arguments = args;

            Crm crm = new Crm();
            crm.makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    listener.onSuccess();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    listener.onFailure(error.getLocalizedMessage());
                }
            });

        }

        public static void deleteTripAssociations(final Context context, String tripid, final MyInterfaces.EntityUpdateListener listener) {
            getAssociationsByTripId(context, tripid, new GetTripAssociationsListener() {
                @Override
                public void onSuccess(TripAssociations associations) {
                    deleteCrmAssociations(context, associations, new MyInterfaces.EntityUpdateListener() {
                        @Override
                        public void onSuccess() {
                            listener.onSuccess();
                        }

                        @Override
                        public void onFailure(String msg) {
                            listener.onFailure(msg);
                        }
                    });
                }

                @Override
                public void onFailure(String msg) {

                }
            });
        }

        public String toJson() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }

        public Containers toContainers() {
            Containers containers = new Containers();
            for (TripAssociation association : this.list) {
                containers.entityContainers.add(association.toContainer());
            }
            return containers;
        }

        @Override
        public String toString() {
            return this.list.size() + " associations, ";
        }

        public static class TripAssociation {
            private static final String TAG = "MileageReimbursementAssociation";
            public String etag;
            public String name;
            public String id;
            public String ownerid;
            public String ownername;
            public DateTime createdon;
            public String associated_trip_name;
            public String associated_trip_id;
            public String associated_account_name;
            public String associated_account_id;
            public String associated_opportunity_name;
            public String associated_opportunity_id;
            public float associated_trip_reimbursement;
            public DateTime associated_trip_date;
            public TripDisposition tripDisposition;

            public TripAssociation(DateTime tripDate) {
                this.associated_trip_date = tripDate;
                this.ownerid = MediUser.getMe().systemuserid;
                this.ownername = MediUser.getMe().fullname;
                this.name = tripDate.toLocalDateTime().toString() + " - " + this.ownername;
            }

            public TripAssociation(JSONObject json) {
                Log.i(TAG, "MileageReimbursementAssociation " + json);

                try {
                    if (!json.isNull("etag")) {
                        this.etag = (json.getString("etag"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_ownerid_value")) {
                        this.ownerid = (json.getString("_ownerid_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_ownerid_valueFormattedValue")) {
                        this.ownername = (json.getString("_ownerid_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_msus_disposition_value")) {
                        setTripDisposition(json.getInt("_msus_disposition_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("createdon")) {
                        this.createdon = (new DateTime(json.getString("createdon")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_msus_associated_trip_valueFormattedValue")) {
                        this.associated_trip_name = (json.getString("_msus_associated_trip_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_msus_associated_trip_value")) {
                        this.associated_trip_id = (json.getString("_msus_associated_trip_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_msus_associated_account_valueFormattedValue")) {
                        this.associated_account_name = (json.getString("_msus_associated_account_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_msus_associated_account_value")) {
                        this.associated_account_id = (json.getString("_msus_associated_account_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_msus_associated_opportunity_valueFormattedValue")) {
                        this.associated_opportunity_name = (json.getString("_msus_associated_opportunity_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_msus_associated_opportunity_value")) {
                        this.associated_opportunity_id = (json.getString("_msus_associated_opportunity_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_mileageassociationid")) {
                        this.id = (json.getString("msus_mileageassociationid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_name")) {
                        this.name = (json.getString("msus_name"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_cc3500d91af9ea11810b005056a36b9b_msus_reimbursement")) {
                        this.associated_trip_reimbursement = (json.getLong("a_cc3500d91af9ea11810b005056a36b9b_msus_reimbursement"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_cc3500d91af9ea11810b005056a36b9b_msus_dt_tripdate")) {
                        this.associated_trip_date = (new DateTime(json.getString("a_cc3500d91af9ea11810b005056a36b9b_msus_dt_tripdate")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            public enum TripDisposition {
                START, END
            }

            public String getDispositionTitle() {
                switch (this.tripDisposition) {
                    case START :
                        return "Start";
                    case END :
                        return "End";
                    default:
                        return "0";
                }
            }

            public int getDispositioValue() {
                switch (this.tripDisposition) {
                    case START :
                        return 745820000;
                    case END :
                        return 745820001;
                    default:
                        return 0;
                }
            }

            public void setTripDisposition(int optionSetValue) {
                switch (optionSetValue) {
                    case 745820000 :
                        this.tripDisposition = TripDisposition.START;
                        break;
                    case 745820001 :
                        this.tripDisposition = TripDisposition.END;
                        break;
                }
            }

            @Override
            public String toString() {
                return this.name + ", " + this.associated_trip_date.toLocalDateTime().toString() +
                        ", " + this.ownername;
            }

            public EntityContainer toContainer() {
                EntityContainer container = new EntityContainer();
                if (this.name != null) {
                    container.entityFields.add(new EntityField("msus_name", name));
                }
                if (this.associated_trip_id != null) {
                    container.entityFields.add(new EntityField("msus_associated_trip", this.associated_trip_id));
                }
                if (this.associated_account_id != null) {
                    container.entityFields.add(new EntityField("msus_associated_account", this.associated_account_id));
                }
                if (this.associated_opportunity_id != null) {
                    container.entityFields.add(new EntityField("msus_associated_opportunity", this.associated_opportunity_id));
                }
                if (this.tripDisposition != null) {
                    container.entityFields.add(new EntityField("msus_disposition", Integer.toString(this.getDispositioValue())));
                }
                return container;
            }

        }

    }

}
