package com.fimbleenterprises.medimileage.objects_and_containers;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.fimbleenterprises.medimileage.Crm;
import com.fimbleenterprises.medimileage.CrmQueries;
import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.MyApp;
import com.fimbleenterprises.medimileage.MyInterfaces;
import com.fimbleenterprises.medimileage.MyPreferencesHelper;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import cz.msebera.android.httpclient.Header;

public class Opportunities {
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
        MyPreferencesHelper options = new MyPreferencesHelper(MyApp.getAppContext());
        return options.getSavedOpportunities();
    }

    public void save() {
        MyPreferencesHelper options = new MyPreferencesHelper(MyApp.getAppContext());
        options.saveOpportunities(this);
    }

    /**
     * Will query CRM using the current user's territory id and retrieve all opportunities in their
     * territory.  When obtained they will be saved locally as JSON to shared preferences.
     * @param listener A basic YesNo listener which will return a populated Opportunities object
     *                 on success (cast the returned object to Opportunities) or the
     *                 error message as a string on failure (cast returned object to string).
     */
    public static void retrieveAndSaveOpportunities(final MyInterfaces.YesNoResult listener) {
        final MyPreferencesHelper options = new MyPreferencesHelper(MyApp.getAppContext());
        String query = CrmQueries.Opportunities.getOpportunitiesByTerritory(MediUser.getMe().territoryid);
        ArrayList<Requests.Argument> args = new ArrayList<>();
        Requests.Argument argument = new Requests.Argument("query", query);
        args.add(argument);
        Requests.Request request = new Requests.Request(Requests.Request.GET, args);
        Crm crm = new Crm();
        crm.makeCrmRequest(MyApp.getAppContext(), request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                Opportunities opportunities = new Opportunities(response);
                opportunities.save();
                Opportunities savedOpportunities = options.getSavedOpportunities();
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
     * Will query CRM using the supplied territory id and retrieve all opportunities.
     * That's it.  That's all it does.
     * @param listener An interface that constructs a Territories object and returns it on success.
     */
    public static void retrieveOpportunities(String territoryId, final MyInterfaces.GetOpportunitiesListener listener) {
        String query = CrmQueries.Opportunities.getOpportunitiesByTerritory(territoryId);
        ArrayList<Requests.Argument> args = new ArrayList<>();
        Requests.Argument argument = new Requests.Argument("query", query);
        args.add(argument);
        Requests.Request request = new Requests.Request(Requests.Request.GET, args);
        Crm crm = new Crm();
        crm.makeCrmRequest(MyApp.getAppContext(), request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                listener.onSuccess(new Opportunities(response));
                Log.i(TAG, "onSuccess " + response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.w(TAG, "onFailure: " + error.getLocalizedMessage());
                listener.onFailure(error.getLocalizedMessage());
            }
        });
    }

    /**
     * Will query CRM using the supplied territory id and retrieve all opportunities.
     * That's it.  That's all it does.
     * @param listener An interface that constructs a Territories object and returns it on success.
     */
    public static void retrieveOpportunities(CrmQueries.Opportunities.DealStatus status,
                                             @Nullable String territoryid, final MyInterfaces.GetOpportunitiesListener listener) {
        String query = CrmQueries.Opportunities.getAllOpportunities(territoryid, status);
        ArrayList<Requests.Argument> args = new ArrayList<>();
        Requests.Argument argument = new Requests.Argument("query", query);
        args.add(argument);
        Requests.Request request = new Requests.Request(Requests.Request.GET, args);
        Crm crm = new Crm();
        crm.makeCrmRequest(MyApp.getAppContext(), request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                listener.onSuccess(new Opportunities(response));
                Log.i(TAG, "onSuccess " + response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.w(TAG, "onFailure: " + error.getLocalizedMessage());
                listener.onFailure(error.getLocalizedMessage());
            }
        });
    }

    /**
     * Will query CRM and get an opportunity's details.
     * That's it.  That's all it does.
     * @param listener An interface that constructs a Territories object and returns it on success.
     */
    public static void retrieveOpportunityDetails(String opportunityid, final MyInterfaces.GetOpportunitiesListener listener) {
        String query = CrmQueries.Opportunities.getOpportunityDetails(opportunityid);
        ArrayList<Requests.Argument> args = new ArrayList<>();
        Requests.Argument argument = new Requests.Argument("query", query);
        args.add(argument);
        Requests.Request request = new Requests.Request(Requests.Request.GET, args);
        Crm crm = new Crm();
        crm.makeCrmRequest(MyApp.getAppContext(), request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                listener.onSuccess(new Opportunities(response));
                Log.i(TAG, "onSuccess " + response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.w(TAG, "onFailure: " + error.getLocalizedMessage());
                listener.onFailure(error.getLocalizedMessage());
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
    public boolean accountHasOpportunity(CrmEntities.CrmAddresses.CrmAddress address) {
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

    public static class Opportunity extends CrmEntities.CrmEntity implements Parcelable {
        /*public String etag;
        public String entityid;*/
        public String accountid;
        public String accountname;
        public String probabilityPretty;
        public int probabilityOptionsetValue;
        public String ownername;
        public String createdBy;
        public String createdByFormatted;
        public String modifiedBy;
        public String modifiedByFormatted;
        private double modifiedOn;
        public double estDeviceRev;
        public double estProbeRev;
        public double estTotalRev;
        public double estTerritoryRev;
        public String modifiedOnFormatted;
        public double monthRevPppLease;
        public String createdOnFormatted;
        public String ownerid;
        private double estimatedCloseDate;
        private double createdon;
        private int statecode;
        public String statecodeFormatted;
        public String stepName;
        public String dealTypePretty;
        public int dealTypeOptionsetValue;
        public String territoryid;
        public String name;
        public float floatEstimatedValue;
        public String currentSituation;
        private int statuscode;
        public String statuscodeFormatted;
        public boolean isSeparator = false;

        @Override
        public String toString() {
            return this.name;
        }

        public BasicEntity toBasicEntity() {
            BasicEntity entity = new BasicEntity(this);
            BasicEntity.EntityBasicField topic = new BasicEntity.EntityBasicField("Topic:", this.name);
            topic.crmFieldName = "name";
            entity.fields.add(topic);

            BasicEntity.EntityBasicField accountField = new BasicEntity.EntityBasicField("Account:", this.accountname);
            accountField.isAccountField = true;
            CrmEntities.Accounts.Account account = new CrmEntities.Accounts.Account();
            account.entityid = this.accountid;
            account.accountName = this.accountname;
            accountField.account = account;
            accountField.crmFieldName = "parentaccountid";
            entity.fields.add(accountField);

            BasicEntity.EntityBasicField curSit = new BasicEntity.EntityBasicField("Background:", this.currentSituation);
            curSit.crmFieldName = "currentsituation";
            curSit.isEditable = true;
            entity.fields.add(curSit);

            ArrayList<BasicEntity.EntityStatusReason> statusValues = new ArrayList<>();
            statusValues.add(new BasicEntity.EntityStatusReason("Discovery", "1", "0"));
            statusValues.add(new BasicEntity.EntityStatusReason("Stalled", "2", "0"));
            statusValues.add(new BasicEntity.EntityStatusReason("Qualifying", "100000002", "0"));
            statusValues.add(new BasicEntity.EntityStatusReason("Evaluating", "100000003", "0"));
            statusValues.add(new BasicEntity.EntityStatusReason("Pending", "100000009", "0"));
            statusValues.add(new BasicEntity.EntityStatusReason("Won", "100000007", "1"));
            statusValues.add(new BasicEntity.EntityStatusReason("Closed", "100000010", "1"));
            statusValues.add(new BasicEntity.EntityStatusReason("Cancelled", "4", "2"));
            statusValues.add(new BasicEntity.EntityStatusReason("Out-Sold", "5", "2"));
            statusValues.add(new BasicEntity.EntityStatusReason("Dead", "100000001", "2"));
            entity.availableEntityStatusReasons = statusValues;
            entity.entityStatusReason = new BasicEntity.EntityStatusReason(this.statuscodeFormatted,
                    Integer.toString(this.statuscode), Integer.toString(this.statecode));

            BasicEntity.EntityBasicField dealType = new BasicEntity.EntityBasicField("Deal type: ", dealTypePretty);
            dealType.crmFieldName = "col_dealtype";
            ArrayList<BasicEntity.EntityBasicField.OptionSetValue> dealTypes = new ArrayList<>();
            dealTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("NA", "100000000"));
            dealTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("Cap", "1"));
            dealTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("Lease", "2"));
            dealTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("Hybrid", "3"));
            dealTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("PPP", "4"));
            dealTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("TB", "181400000"));
            dealType.optionSetValues = dealTypes;
            dealType.isOptionSet = true;
            dealType.isReadOnly = false;
            entity.fields.add(dealType);

            BasicEntity.EntityBasicField closeProbability = new BasicEntity.EntityBasicField("Close probability: ", probabilityPretty);
            closeProbability.crmFieldName = "msus_probability";
            ArrayList<BasicEntity.EntityBasicField.OptionSetValue> probabilities = new ArrayList<>();
            probabilities.add(new BasicEntity.EntityBasicField.OptionSetValue("20%", "745820000"));
            probabilities.add(new BasicEntity.EntityBasicField.OptionSetValue("40%", "745820001"));
            probabilities.add(new BasicEntity.EntityBasicField.OptionSetValue("60%", "745820002"));
            probabilities.add(new BasicEntity.EntityBasicField.OptionSetValue("90%", "745820003"));
            closeProbability.optionSetValues = probabilities;
            closeProbability.isOptionSet = true;
            entity.fields.add(closeProbability);

            BasicEntity.EntityBasicField deviceRev = new BasicEntity.EntityBasicField("Device revenue:", Double.toString(this.estDeviceRev));
            deviceRev.isNumber = true;
            deviceRev.crmFieldName = "col_estmrevenuedevices";
            entity.fields.add(deviceRev);

            BasicEntity.EntityBasicField probeRev = new BasicEntity.EntityBasicField("Probe revenue:", Double.toString(this.estProbeRev));
            probeRev.crmFieldName = "col_estmrevenueprobes";
            probeRev.isNumber = true;
            entity.fields.add(probeRev);

            BasicEntity.EntityBasicField totalRev = new BasicEntity.EntityBasicField("Total revenue:", Double.toString(this.estTotalRev));
            totalRev.isNumber = true;
            totalRev.crmFieldName = "estimatedvalue";
            entity.fields.add(totalRev);

            BasicEntity.EntityBasicField territoryRev = new BasicEntity.EntityBasicField("Territory revenue:", Double.toString(this.estTerritoryRev));
            territoryRev.isNumber = true;
            territoryRev.crmFieldName = "new_territoryrevenue";
            entity.fields.add(territoryRev);

            BasicEntity.EntityBasicField monthRev = new BasicEntity.EntityBasicField("Month Revenue PPP/Lease:", Double.toString(this.monthRevPppLease));
            monthRev.isNumber = true;
            monthRev.crmFieldName = "new_monthrevenuepppleasecurrency";
            entity.fields.add(monthRev);

            entity.fields.add(new BasicEntity.EntityBasicField("Step:", this.stepName, true));
            entity.fields.add(new BasicEntity.EntityBasicField("Created on:",
                    Helpers.DatesAndTimes.getPrettyDateAndTime(this.getCreatedOn()), true));
            entity.fields.add(new BasicEntity.EntityBasicField("Created by:", this.createdByFormatted, true));
            entity.fields.add(new BasicEntity.EntityBasicField("Modified on:", this.modifiedOnFormatted, true));
            entity.fields.add(new BasicEntity.EntityBasicField("Modified by:", this.modifiedByFormatted, true));

            return entity;
        }

        public DateTime getCreatedOn() {
            try {
                return new DateTime(createdon);
            } catch (Exception e) {
                e.printStackTrace();
                return DateTime.now();
            }
        }

        public String getDealTypePretty() {
            if (this.dealTypePretty == null) {
                return "";
            } else {
                return "Deal type " + this.dealTypePretty;
            }
        }

        public DateTime getModifiedOn() {
            return new DateTime(modifiedOn);
        }

        public DateTime getEstimatedClose() {
            try {
                return new DateTime(estimatedCloseDate);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public String getPrettyEstimatedValue() {
            return Helpers.Numbers.convertToCurrency(floatEstimatedValue);
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
                if (!json.isNull("statuscode")) {
                    this.statuscode = (json.getInt("statuscode"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("statuscodeFormattedValue")) {
                    this.statuscodeFormatted = (json.getString("statuscodeFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("statecodeFormattedValue")) {
                    this.statecodeFormatted = (json.getString("statecodeFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("statecode")) {
                    this.statecode = (json.getInt("statecode"));
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
                    this.statuscodeFormatted = (json.getString("statuscodeFormattedValue"));
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
                if (!json.isNull("_createdby_valueFormattedValue")) {
                    this.createdByFormatted = (json.getString("_createdby_valueFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_modifiedby_valueFormattedValue")) {
                    this.modifiedByFormatted = (json.getString("_modifiedby_valueFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_modifiedby_value")) {
                    this.modifiedBy = (json.getString("_modifiedby_value"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("modifiedon")) {
                    this.modifiedOn = (new DateTime(json.getString("modifiedon")).getMillis());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("modifiedonFormattedValue")) {
                    this.modifiedOnFormatted = (json.getString("modifiedonFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("createdonFormattedValue")) {
                    this.createdOnFormatted = (json.getString("createdonFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_createdby_value")) {
                    this.createdBy = (json.getString("_createdby_value"));
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
                if (!json.isNull("col_estmrevenuedevices")) {
                    this.estDeviceRev = (json.getDouble("col_estmrevenuedevices"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("estimatedvalue")) {
                    this.estTotalRev = (json.getDouble("estimatedvalue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("col_estmrevenueprobes")) {
                    this.estProbeRev = (json.getDouble("col_estmrevenueprobes"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("new_monthrevenuepppleasecurrency")) {
                    this.monthRevPppLease = json.getDouble("new_monthrevenuepppleasecurrency");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("new_territoryrevenue")) {
                    this.estTerritoryRev = (json.getDouble("new_territoryrevenue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("opportunityid")) {
                    this.entityid = (json.getString("opportunityid"));
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
        public CrmEntities.CrmAddresses.CrmAddress tryGetCrmAddress() {
            try {
                MyPreferencesHelper options = new MyPreferencesHelper(MyApp.getAppContext());
                if (options.hasSavedAddresses()) {
                    CrmEntities.CrmAddresses addresses = options.getAllSavedCrmAddresses();
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
                MyPreferencesHelper options = new MyPreferencesHelper(MyApp.getAppContext());
                if (!options.hasSavedAddresses()) {
                    return false;
                }

                CrmEntities.CrmAddresses.CrmAddress thisAddress, targetAddress;
                CrmEntities.CrmAddresses savedAddys = options.getAllSavedCrmAddresses();
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
        public boolean isNearby(CrmEntities.CrmAddresses.CrmAddress addy) {
            try {
                MyPreferencesHelper options = new MyPreferencesHelper(MyApp.getAppContext());
                if (!options.hasSavedAddresses()) {
                    return false;
                }

                CrmEntities.CrmAddresses.CrmAddress thisAddress, targetAddress;
                CrmEntities.CrmAddresses savedAddys = options.getAllSavedCrmAddresses();
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
            createdBy = in.readString();
            createdByFormatted = in.readString();
            modifiedBy = in.readString();
            modifiedByFormatted = in.readString();
            modifiedOn = in.readDouble();
            estDeviceRev = in.readDouble();
            estProbeRev = in.readDouble();
            estTotalRev = in.readDouble();
            estTerritoryRev = in.readDouble();
            modifiedOnFormatted = in.readString();
            createdOnFormatted = in.readString();
            ownerid = in.readString();
            estimatedCloseDate = in.readDouble();
            createdon = in.readDouble();
            statecode = in.readInt();
            statecodeFormatted = in.readString();
            stepName = in.readString();
            dealTypePretty = in.readString();
            dealTypeOptionsetValue = in.readInt();
            territoryid = in.readString();
            entityid = in.readString();
            name = in.readString();
            floatEstimatedValue = in.readFloat();
            currentSituation = in.readString();
            statuscode = in.readInt();
            statuscodeFormatted = in.readString();
            isSeparator = in.readByte() != 0x00;
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
            dest.writeString(createdBy);
            dest.writeString(createdByFormatted);
            dest.writeString(modifiedBy);
            dest.writeString(modifiedByFormatted);
            dest.writeDouble(modifiedOn);
            dest.writeDouble(estDeviceRev);
            dest.writeDouble(estProbeRev);
            dest.writeDouble(estTotalRev);
            dest.writeDouble(estTerritoryRev);
            dest.writeString(modifiedOnFormatted);
            dest.writeString(createdOnFormatted);
            dest.writeString(ownerid);
            dest.writeDouble(estimatedCloseDate);
            dest.writeDouble(createdon);
            dest.writeInt(statecode);
            dest.writeString(statecodeFormatted);
            dest.writeString(stepName);
            dest.writeString(dealTypePretty);
            dest.writeInt(dealTypeOptionsetValue);
            dest.writeString(territoryid);
            dest.writeString(entityid);
            dest.writeString(name);
            dest.writeFloat(floatEstimatedValue);
            dest.writeString(currentSituation);
            dest.writeInt(statuscode);
            dest.writeString(statuscodeFormatted);
            dest.writeByte((byte) (isSeparator ? 0x01 : 0x00));
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
