package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
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
import androidx.annotation.Nullable;
import cz.msebera.android.httpclient.Header;

public class CrmEntities {

    public static class Annotations {
        private static final String TAG = "Annotations";
        public ArrayList<Annotation> list = new ArrayList<>();
        public boolean hasNotes = false;

        public Annotations(String crmResponse) {
            try {
                JSONObject rootObject = new JSONObject(crmResponse);
                JSONArray array = rootObject.getJSONArray("value");
                hasNotes = array.length() > 0;
                for (int i = 0; i < array.length(); i++) {
                    JSONObject json = array.getJSONObject(i);
                    Annotation annotation = new Annotation(json);
                    this.list.add(annotation);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Returns a single annotation from CRM.
         * @param annotationid The annotation id.
         * @param includeAttachment Whether or not to retrieve the attachment if applicable
         * @param listener A callback.
        */
        public static void getAnnotationFromCrm(String annotationid, boolean includeAttachment, final AsyncHttpResponseHandler listener) {
            String query = Queries.Annotations.getAnnotation(annotationid, includeAttachment);
            Crm crm = new Crm();

            Requests.Request request = new Requests.Request(Function.GET);
            request.arguments.add(new Requests.Argument("query", query));

            crm.makeCrmRequest(MyApp.getAppContext(), request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    listener.onSuccess(statusCode, headers, responseBody);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    listener.onFailure(statusCode, headers, responseBody, error);
                }
            });
        }

        public static class Annotation {

            private static final String TAG = "Annotation";

            public String etag;
            public String annotationid;
            public String objectid;
            public String filename;
            public String documentBody;
            public int filesize;
            public boolean isDocument;
            public String subject;
            public String notetext;
            public String objectEntityName;
            public DateTime createdon;
            public DateTime modifiedon;
            public String modifiedByValue;
            public String modifedByName;
            public String createdByValue;
            public String createdByName;
            public String mimetype;

            public Annotation() { }

            public Annotation(JSONObject json) {

                try {
                    if (!json.isNull("etag")) {
                        this.etag = (json.getString("etag"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("documentbody")) {
                        this.documentBody = (json.getString("documentbody"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("filesize")) {
                        this.filesize = (json.getInt("filesize"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("filename")) {
                        this.filename = (json.getString("filename"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("annotationid")) {
                        this.annotationid = (json.getString("annotationid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_objectid_value")) {
                        this.objectid = (json.getString("_objectid_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("objecttypecode")) {
                        this.objectEntityName = (json.getString("objecttypecode"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("isdocument")) {
                        this.isDocument = (json.getBoolean("isdocument"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("subject")) {
                        this.subject = (json.getString("subject"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("notetext")) {
                        this.notetext = (json.getString("notetext"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_modifiedby_valueFormattedValue")) {
                        this.modifedByName = (json.getString("_modifiedby_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_modifiedby_value")) {
                        this.modifiedByValue = (json.getString("_modifiedby_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_createdby_valueFormattedValue")) {
                        this.createdByName = (json.getString("_createdby_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_createdby_value")) {
                        this.createdByValue = (json.getString("_createdby_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("mimetype")) {
                        this.mimetype = (json.getString("mimetype"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                   if (!json.isNull("modifiedon")) {
                       this.modifiedon = (new DateTime(json.getString("modifiedon")));
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
            }

            @Override
            public String toString() {
                return this.subject + " (attachment: " + isDocument + ")";
            }

            /**
             * Creates a new or edits an existing note on the CRM server.  Add/Edit is determined
             * based on whether or not the annotation object has an annotationid or not (is null).
             * @param context A context suitable to execute a Crm request.
             * @param listener A simple listener to handle the callback.
             */
            public void submit(Context context, final MyInterfaces.YesNoResult listener) {

                if (this.annotationid == null) {
                    // Create new note

                    // The annotation entity is slightly different so instead of a basic EntityContainer
                    // we create an AnnotationCreationContainer for the creation request
                    Containers.AnnotationCreationContainer annotationContainer =
                            new Containers.AnnotationCreationContainer();
                    annotationContainer.notetext = this.notetext;
                    annotationContainer.subject = this.subject;
                    annotationContainer.objectidtypecode = "opportunity";
                    annotationContainer.objectid = objectid;

                    Requests.Request request = new Requests.Request(Requests.Request.Function.CREATE_NOTE);
                    request.arguments.add(new Requests.Argument("noteobject", annotationContainer.toJson()));

                    Crm crm = new Crm();
                    crm.makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String response = new String(responseBody);
                            Log.i(TAG, "onSuccess " + response);
                            listener.onYes(response);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.w(TAG, "onFailure: " + error.getLocalizedMessage());
                            listener.onNo(error.getLocalizedMessage());
                            listener.onNo(error.getLocalizedMessage());
                        }
                    });
                } else {

                    EntityContainer entityContainer = new EntityContainer();
                    entityContainer.entityFields.add(new EntityField("notetext", this.notetext));
                    entityContainer.entityFields.add(new EntityField("subject", this.subject));
                    // entityContainer.entityFields.add(new EntityField("isdocument", Boolean.toString(this.isDocument)));
                    // entityContainer.entityFields.add(new EntityField("documentbody", this.documentBody));
                    // entityContainer.entityFields.add(new EntityField("mimetype", this.mimetype));
                    // entityContainer.entityFields.add(new EntityField("filename", this.filename));

                    // Update existing note
                    Requests.Request request = new Requests.Request(Function.UPDATE);
                    request.arguments.add(new Requests.Argument("guid", this.annotationid));
                    request.arguments.add(new Requests.Argument("entityname", "annotation"));
                    request.arguments.add(new Requests.Argument("container", entityContainer.toJson()));
                    request.arguments.add(new Requests.Argument("asuserid", MediUser.getMe().systemuserid));

                    Crm crm = new Crm();
                    crm.makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            listener.onYes(new String(responseBody));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            listener.onNo(error.getLocalizedMessage());
                        }
                    });

                    /*
                    string guid = (string)value.Arguments[0].value;
                    entityName = (string)value.Arguments[1].value;
                    container = JsonConvert.DeserializeObject<EntityContainer>((string)value.Arguments[2].value);
                    asUserid = (string)value.Arguments[3].value;
                     */
                }
            }

            public void delete(Context context, final MyInterfaces.YesNoResult listener) {
                // Args:
                // 0: Entity name
                // 1: Entityid
                // 2: AsUserid

                Requests.Request request = new Requests.Request(Function.DELETE);
                request.arguments.add(new Requests.Argument("entityname", "annotation"));
                request.arguments.add(new Requests.Argument("entityid", this.annotationid));
                request.arguments.add(new Requests.Argument("asuserid", MediUser.getMe().systemuserid));

                Crm crm = new Crm();
                crm.makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.i(TAG, "onSuccess Note was deleted (" + new String(responseBody) + ")");
                        listener.onYes(new String(responseBody));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.w(TAG, "onFailure: Failed to delete note (" + new String(responseBody) + ")");
                        listener.onNo(error.getLocalizedMessage());
                    }
                });
            }

        }

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

    public static class Opportunities {
        private static final String TAG = "Opportunities";
        public ArrayList<Opportunity> list = new ArrayList<>();

        public Opportunities(String crmResponse) {
            try {
                JSONObject rootObject = new JSONObject(crmResponse);
                JSONArray rootArray = rootObject.getJSONArray("value");
                for (int i = 0; i < rootArray.length(); i++) {
                    list.add(new Opportunity(rootArray.getJSONObject(i)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public String toString() {
            return list.size() + " opportunities";
        }

        public String toGson() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }

        public static Opportunities fromGson(String gsonString) {
            Gson gson = new Gson();
            return gson.fromJson(gsonString, Opportunities.class);
        }

        public static Opportunities getSaved() {
            MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());
            return options.getSavedOpportunities();
        }

        public void save() {
            MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());
            options.saveOpportunities(this);
        }

        /**
         * Will query CRM using the current user's territory id and retrieve all opportunities in their
         * territory.  When obtained they will be saved locally as JSON to shared preferences.
         * @param listener A basic YesNo listener which will return a populated Opportunities object
         *                 on success (cast the returned object to CrmEntities.Opportunities) or the
         *                 error message as a string on failure (cast returned object to string).
         */
        public static void retrieveAndSaveOpportunities(final MyInterfaces.YesNoResult listener) {
            final MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());
            String query = Queries.Opportunities.getAllOpenOpportunities();
            ArrayList<Requests.Argument> args = new ArrayList<>();
            Requests.Argument argument = new Requests.Argument("query", query);
            args.add(argument);
            Requests.Request request = new Requests.Request(Requests.Request.GET, args);
            Crm crm = new Crm();
            crm.makeCrmRequest(MyApp.getAppContext(), request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody);
                    CrmEntities.Opportunities opportunities = new CrmEntities.Opportunities(response);
                    opportunities.save();
                    CrmEntities.Opportunities savedOpportunities = options.getSavedOpportunities();
                    Log.i(TAG, "onSuccess " + response);
                    if (savedOpportunities != null) {
                        listener.onYes(opportunities);
                    } else {
                        listener.onNo(null);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.w(TAG, "onFailure: " + error.getLocalizedMessage());
                    listener.onNo(null);
                }
            });
        }

        /**
         * Will query CRM using the current user's territory id and retrieve all opportunities in their
         * territory.  When obtained they will be saved locally as JSON to shared preferences.
         */
        public static void retrieveAndSaveOpportunities() {
            retrieveAndSaveOpportunities(new MyInterfaces.YesNoResult() {
                @Override
                public void onYes(@Nullable Object object) { }

                @Override
                public void onNo(@Nullable Object object) { }
            });
        }

        /**
         * Returns an opportunity in the list.
         * @param accountid The accountid to use when searching for opportunities
         * @return An arraylist of Opportunity objects (or null if none are found).
         */
        public ArrayList<Opportunity> getOpportunities(String accountid) {
            ArrayList<Opportunity> foundOpps = new ArrayList<>();
            for (Opportunity opp : this.list) {
                if (opp.accountid.equals(accountid)) {
                    foundOpps.add(opp);
                }
            }
            if (foundOpps.size() > 0) {
                return foundOpps;
            } else {
                return null;
            }
        }

        /**
         * Evaluates all opportunities in the list checking if the specified account id exists in one of them.
         * @param accountid The accountid to look for in the list of opportunities.
         * @return True on the first accountid match found.
         */
        public boolean accountHasOpportunity(String accountid) {
            for (Opportunity opp : this.list) {
                if (opp.accountid.equals(accountid)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Evaluates all opportunities in the list checking if the specified account id exists in one of them.
         * @param address The address to look for in the list of opportunities.  Note that the
         *                accountid property is the only property that is evaluated in the address object.
         * @return True on the first accountid match found.
         */
        public boolean accountHasOpportunity(CrmAddresses.CrmAddress address) {
            for (Opportunity opp : this.list) {
                if (opp.accountid.equals(address.accountid)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Counts how many opportunities reference the specified account id.
         * @param accountid The accountid to look for in the list of opportunities.
         * @return The amount of opportunities the accountid was referenced.
         */
        public int accountHasXopportunities(String accountid) {
            int count = 0;
            for (Opportunity opp : this.list) {
                if (opp.accountid.equals(accountid)) {
                    count++;
                }
            }
            return count;
        }

        public static class Opportunity implements Parcelable {
            public String etag;
            public String accountid;
            public String accountname;
            public String probabilityPretty;
            public int probabilityOptionsetValue;
            public String ownername;
            public String ownerid;
            private double estimatedCloseDate;
            private double createdon;
            public String stepName;
            public String dealTypePretty;
            public int dealTypeOptionsetValue;
            public String territoryid;
            public String opportunityid;
            public String name;
            public float floatEstimatedValue;
            public String currentSituation;
            public String status;
            public String dealStatus;
            public boolean isSeparator = false;

            @Override
            public String toString() {
                return this.name;
            }

            public DateTime getCreatedOn() {
                return new DateTime(createdon);
            }

            public DateTime getEstimatedClose() {
                return new DateTime(estimatedCloseDate);
            }

            public Opportunity(JSONObject json) {
                try {
                    if (!json.isNull("name")) {
                        this.name = (json.getString("name"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("statecodeFormattedValue")) {
                        this.status = (json.getString("statecodeFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("etag")) {
                        this.etag = (json.getString("etag"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_parentaccountid_value")) {
                        this.accountid = (json.getString("_parentaccountid_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("ab_territoryid")) {
                        this.territoryid = (json.getString("ab_territoryid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_parentaccountid_valueFormattedValue")) {
                        this.accountname = (json.getString("_parentaccountid_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_probabilityFormattedValue")) {
                        this.probabilityPretty = (json.getString("msus_probabilityFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("statuscodeFormattedValue")) {
                        this.dealStatus = (json.getString("statuscodeFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_probability")) {
                        this.probabilityOptionsetValue = (json.getInt("msus_probability"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("currentsituation")) {
                        this.currentSituation = (json.getString("currentsituation"));
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
                    if (!json.isNull("_ownerid_value")) {
                        this.ownerid = (json.getString("_ownerid_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("estimatedclosedate")) {
                        this.estimatedCloseDate = (new DateTime(json.getString("estimatedclosedate")).getMillis());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("createdon")) {
                        this.createdon = (new DateTime(json.getString("createdon")).getMillis());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("stepname")) {
                        this.stepName = (json.getString("stepname"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("col_dealtypeFormattedValue")) {
                        this.dealTypePretty = (json.getString("col_dealtypeFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("col_dealtype")) {
                        this.dealTypeOptionsetValue = (json.getInt("col_dealtype"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("opportunityid")) {
                        this.opportunityid = (json.getString("opportunityid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("estimatedvalue")) {
                        this.floatEstimatedValue = (json.getLong("estimatedvalue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            /**
             * Returns the address of the account this opportunity is associated with
             * @return A CrmAddress object or null
             */
            public CrmAddresses.CrmAddress tryGetCrmAddress() {
                try {
                    MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());
                    if (options.hasSavedAddresses()) {
                        CrmAddresses addresses = options.getAllSavedCrmAddresses();
                        return addresses.getAddress(this.accountid);
                    }
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            /**
             * Evaluates if this opportunity's account is nearby another account based on the
             * distance threshold stipulated in shared preferences.
             * @param accountid The account to compare to.
             * @return True or false as to whether they are near each other.
             */
            public boolean isNearby(String accountid) {
                try {
                    MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());
                    if (!options.hasSavedAddresses()) {
                        return false;
                    }

                    CrmAddresses.CrmAddress thisAddress, targetAddress;
                    CrmAddresses savedAddys = options.getAllSavedCrmAddresses();
                    thisAddress = savedAddys.getAddress(this.accountid);
                    targetAddress = savedAddys.getAddress(accountid);

                    return thisAddress.isNearby(targetAddress);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            /**
             * Evaluates if this opportunity's account is nearby another account based on the
             * distance threshold stipulated in shared preferences.
             * @param addy The account to compare to.
             * @return True or false as to whether they are near each other.
             */
            public boolean isNearby(CrmAddresses.CrmAddress addy) {
                try {
                    MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());
                    if (!options.hasSavedAddresses()) {
                        return false;
                    }

                    CrmAddresses.CrmAddress thisAddress, targetAddress;
                    CrmAddresses savedAddys = options.getAllSavedCrmAddresses();
                    thisAddress = savedAddys.getAddress(this.accountid);
                    targetAddress = savedAddys.getAddress(addy.accountid);

                    return thisAddress.isNearby(targetAddress);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            protected Opportunity(Parcel in) {
                etag = in.readString();
                accountid = in.readString();
                accountname = in.readString();
                probabilityPretty = in.readString();
                probabilityOptionsetValue = in.readInt();
                ownername = in.readString();
                ownerid = in.readString();
                estimatedCloseDate = in.readDouble();
                createdon = in.readDouble();
                stepName = in.readString();
                dealTypePretty = in.readString();
                dealTypeOptionsetValue = in.readInt();
                territoryid = in.readString();
                opportunityid = in.readString();
                name = in.readString();
                floatEstimatedValue = in.readFloat();
                dealStatus = in.readString();
                currentSituation = in.readString();
                status = in.readString();
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(etag);
                dest.writeString(accountid);
                dest.writeString(accountname);
                dest.writeString(probabilityPretty);
                dest.writeInt(probabilityOptionsetValue);
                dest.writeString(ownername);
                dest.writeString(ownerid);
                dest.writeDouble(estimatedCloseDate);
                dest.writeDouble(createdon);
                dest.writeString(stepName);
                dest.writeString(dealTypePretty);
                dest.writeInt(dealTypeOptionsetValue);
                dest.writeString(territoryid);
                dest.writeString(opportunityid);
                dest.writeString(name);
                dest.writeFloat(floatEstimatedValue);
                dest.writeString(dealStatus);
                dest.writeString(currentSituation);
                dest.writeString(status);
            }

            @SuppressWarnings("unused")
            public static final Parcelable.Creator<Opportunity> CREATOR = new Parcelable.Creator<Opportunity>() {
                @Override
                public Opportunity createFromParcel(Parcel in) {
                    return new Opportunity(in);
                }

                @Override
                public Opportunity[] newArray(int size) {
                    return new Opportunity[size];
                }
            };
        }
    }

    public static class CrmAddresses {
        private static final String TAG = "CrmAddresses";
        public ArrayList<CrmAddress> list = new ArrayList<>();

        public String toGson() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }

        public static CrmAddresses fromGson(String gsonString) {
            Gson gson = new Gson();
            return gson.fromJson(gsonString, CrmAddresses.class);
        }

        public CrmAddress getAddress(String accountid) {
            for (CrmAddress address : this.list) {
                if (address.accountid.equals(accountid)) {
                    return address;
                }
            }
            return null;
        }

        /**
         * Will query CRM and retrieve all account addresses in the system.
         * When obtained they will be saved locally as JSON to shared preferences.
         * @param listener A basic YesNo listener which will return a populated CrmAddresses object
         *                 on success (cast the returned object to CrmEntities.CrmAddresses) or the
         *                 error message as a string on failure (cast returned object to string).
         */
        public static void retrieveAndSaveCrmAddresses(final MyInterfaces.YesNoResult listener) {
            final MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());
            Requests.Argument argument = new Requests.Argument("query", Queries.Addresses.getAllAccountAddresses());
            ArrayList<Requests.Argument> args = new ArrayList<>();
            args.add(argument);
            Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);
            Crm crm = new Crm();
            crm.makeCrmRequest(MyApp.getAppContext(), request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    // Construct an array of CrmAddresses
                    String response = new String(responseBody);
                    CrmEntities.CrmAddresses addresses = new CrmEntities.CrmAddresses(response);
                    options.saveAllCrmAddresses(addresses);
                    Log.i(TAG, "onSuccess response: " + response.length());
                    listener.onYes(addresses);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.w(TAG, "onFailure: error: " + error.getLocalizedMessage());
                    listener.onNo(error.getLocalizedMessage());
                }
            });
        }

        /**
         * Will query CRM and retrieve all account addresses in the system.
         * When obtained they will be saved locally as JSON to shared preferences.
         */
        public static void retrieveAndSaveCrmAddresses() {
            retrieveAndSaveCrmAddresses(new MyInterfaces.YesNoResult() {
                @Override
                public void onYes(@Nullable Object object) {
                    // nothing to do, homie
                }

                @Override
                public void onNo(@Nullable Object object) {
                    // nothing to do, homie
                }
            });
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

            /**
             * Evaluates two addresses and determines if they are within the distance threshold
             * stipulated in preferences.
             * @param targetAddy The address to compare to this one.
             * @return True if the distance is less than or equal to the preference value saved in shared preferences.
             */
            public boolean isNearby(CrmAddress targetAddy) {
                try {
                    try {
                        MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());
                        return targetAddy.distanceTo(this.getLatLng()) <= options.getDistanceThreshold();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            /**
             * Evaluates two addresses and determines if they are within the distance threshold
             * stipulated in preferences.
             * @param targetAddy The address to compare to this one.
             * @return True if the distance is less than or equal to the preference value saved in shared preferences.
             */
            public boolean isNearby(TripEntry targetAddy) {
                try {
                    try {
                        MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());
                        return targetAddy.distanceTo(this.getLatLng()) <= options.getDistanceThreshold();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

        }

    }

    public static class CreateManyResponses {

        public ArrayList<CreateManyResponse> responses = new ArrayList<>();
        public String errorMessage;
        public boolean wasFaulted;

        public CreateManyResponses(String crmResponse) {
            try {
                JSONObject rootObject = new JSONObject(crmResponse);
                try {
                    if (!rootObject.isNull("ErrorMessage")) {
                        this.errorMessage = (rootObject.getString("ErrorMessage"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!rootObject.isNull("WasFaulted")) {
                        this.wasFaulted = (rootObject.getBoolean("WasFaulted"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONArray rootArray = rootObject.getJSONArray("AllResponses");
                for (int i = 0; i < rootArray.length(); i++) {
                    this.responses.add(new CreateManyResponse(rootArray.getJSONObject(i)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static class CreateManyResponse {
            public boolean wasSuccessful;
            public String responseMessage;
            public String guid;


            public CreateManyResponse(JSONObject json) {
                try {
                    if (!json.isNull("WasSuccessful")) {
                        this.wasSuccessful = (json.getBoolean("WasSuccessful"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("ResponseMessage")) {
                        this.responseMessage = (json.getString("ResponseMessage"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("Guid")) {
                        this.guid = (json.getString("Guid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String toString() {
                return "Was successful: " + this.wasSuccessful + ", Guid: " + this.guid;
            }
        }

        @Override
        public String toString() {
            return "Response count: " + this.responses.size();
        }
    }

    public static class DeleteManyResponses {

        public ArrayList<DeleteManyResponse> responses = new ArrayList<>();
        public String errorMessage;
        public boolean wasFaulted;


        public DeleteManyResponses(String crmResponse) {
            try {
                JSONObject rootObject = new JSONObject(crmResponse);
                try {
                    if (!rootObject.isNull("ErrorMessage")) {
                        this.errorMessage = (rootObject.getString("ErrorMessage"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!rootObject.isNull("WasFaulted")) {
                        this.wasFaulted = (rootObject.getBoolean("WasFaulted"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONArray rootArray = rootObject.getJSONArray("AllResponses");
                for (int i = 0; i < rootArray.length(); i++) {
                    this.responses.add(new DeleteManyResponse(rootArray.getJSONObject(i)));
            }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static class DeleteManyResponse {
            public boolean wasSuccessful;
            public String responseMessage;
            public String guid;
            public boolean wasCreated;


            public DeleteManyResponse(JSONObject json) {
                try {
                    if (!json.isNull("WasSuccessful")) {
                        this.wasSuccessful = (json.getBoolean("WasSuccessful"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("ResponseMessage")) {
                        this.responseMessage = (json.getString("ResponseMessage"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("Guid")) {
                        this.guid = (json.getString("Guid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String toString() {
                return "Was successful: " + this.wasSuccessful + ", Guid: " + this.guid;
            }
        }

        @Override
        public String toString() {
            return "Response count: " + this.responses.size();
        }
    }

    public static class UpdateResponse {
        public boolean wasSuccessful;
        public String responseMessage;
        public String guid;
        public boolean wasCreated;

        public UpdateResponse(String crmResponse) {
            // {"WasSuccessful":true,"ResponseMessage":"Existing record was updated!","Guid":"00000000-0000-0000-0000-000000000000","WasCreated":false}
            try {
                JSONObject json = new JSONObject(crmResponse);
                try {
                    if (!json.isNull("WasSuccessful")) {
                        this.wasSuccessful = (json.getBoolean("WasSuccessful"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("ResponseMessage")) {
                        this.responseMessage = (json.getString("ResponseMessage"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("Guid")) {
                        this.guid = (json.getString("Guid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("WasCreated")) {
                        this.wasCreated = (json.getBoolean("WasCreated"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class TripAssociations implements Parcelable {

        public static class TripAssociation implements Parcelable {
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
            public long associated_trip_date;
            public TripDisposition tripDisposition;

            public DateTime getAssociatedTripDate() {
                return new DateTime(associated_trip_date);
            }

            public void setAssociatedTripDate(DateTime dateTime) {
                this.associated_trip_date = dateTime.getMillis();
            }

            public TripAssociation(DateTime tripDate) {
                this.associated_trip_date = tripDate.getMillis();
                this.ownerid = MediUser.getMe().systemuserid;
                this.ownername = MediUser.getMe().fullname;
                this.name = this.ownername + " was nearby during a MileBuddy trip";
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
                        this.associated_trip_date = (new DateTime(json.getString("a_cc3500d91af9ea11810b005056a36b9b_msus_dt_tripdate")).getMillis());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            public enum TripDisposition {
                START, END
            }

            /**
             * Evaluates whether this association references an existing opportunity.
             * @return True if an association is found
             */
            public boolean hasOpportunity() {
                return this.associated_opportunity_id != null;
            }

            /**
             * Returns a pretty string stipulating whether the association is the beginning or end of a trip.
             * @return "Start" or "End"
             */
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
                return this.name + ", " + new DateTime(this.associated_trip_date).toLocalDateTime().toString() +
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


            protected TripAssociation(Parcel in) {
                etag = in.readString();
                name = in.readString();
                id = in.readString();
                ownerid = in.readString();
                ownername = in.readString();
                createdon = (DateTime) in.readValue(DateTime.class.getClassLoader());
                associated_trip_name = in.readString();
                associated_trip_id = in.readString();
                associated_account_name = in.readString();
                associated_account_id = in.readString();
                associated_opportunity_name = in.readString();
                associated_opportunity_id = in.readString();
                associated_trip_reimbursement = in.readFloat();
                associated_trip_date = in.readLong();
                tripDisposition = (TripDisposition) in.readValue(TripDisposition.class.getClassLoader());
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(etag);
                dest.writeString(name);
                dest.writeString(id);
                dest.writeString(ownerid);
                dest.writeString(ownername);
                dest.writeValue(createdon);
                dest.writeString(associated_trip_name);
                dest.writeString(associated_trip_id);
                dest.writeString(associated_account_name);
                dest.writeString(associated_account_id);
                dest.writeString(associated_opportunity_name);
                dest.writeString(associated_opportunity_id);
                dest.writeFloat(associated_trip_reimbursement);
                dest.writeLong(associated_trip_date);
                dest.writeValue(tripDisposition);
            }

            @SuppressWarnings("unused")
            public static final Parcelable.Creator<TripAssociation> CREATOR = new Parcelable.Creator<TripAssociation>() {
                @Override
                public TripAssociation createFromParcel(Parcel in) {
                    return new TripAssociation(in);
                }

                @Override
                public TripAssociation[] newArray(int size) {
                    return new TripAssociation[size];
                }
            };
        }

        private static final String TAG = "MileageReimbursementAssociations";

        public ArrayList<TripAssociation> list = new ArrayList<>();

        public TripAssociation getAssociation(FullTrip trip) {
            for (TripAssociation a : this.list) {
                if (a.associated_trip_id.equals(trip.tripGuid)) {
                    return a;
                }
            }
            return null;
        }

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


        protected TripAssociations(Parcel in) {
            if (in.readByte() == 0x01) {
                list = new ArrayList<TripAssociation>();
                in.readList(list, TripAssociation.class.getClassLoader());
            } else {
                list = null;
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            if (list == null) {
                dest.writeByte((byte) (0x00));
            } else {
                dest.writeByte((byte) (0x01));
                dest.writeList(list);
            }
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<TripAssociations> CREATOR = new Parcelable.Creator<TripAssociations>() {
            @Override
            public TripAssociations createFromParcel(Parcel in) {
                return new TripAssociations(in);
            }

            @Override
            public TripAssociations[] newArray(int size) {
                return new TripAssociations[size];
            }
        };
    }

}
