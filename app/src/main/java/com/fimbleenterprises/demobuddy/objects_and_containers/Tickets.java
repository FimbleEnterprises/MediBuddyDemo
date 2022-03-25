package com.fimbleenterprises.demobuddy.objects_and_containers;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.fimbleenterprises.demobuddy.Helpers;
import com.fimbleenterprises.demobuddy.R;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Tickets {
    private static final String TAG = "";
    public ArrayList<Tickets.Ticket> list = new ArrayList<>();

    // status codes

    public Tickets(String crmResponse) {
        try {
            JSONObject rootObject = new JSONObject(crmResponse);
            JSONArray rootArray = rootObject.getJSONArray("value");
            for (int i = 0; i < rootArray.length(); i++) {
                JSONObject json = rootArray.getJSONObject(i);
                list.add(new Tickets.Ticket(json));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts to an arraylist suitable for consumption by a BasicObjectRecyclerAdapter.
     * @return
     */
    public ArrayList<BasicObjects.BasicObject> toBasicObjects() {
        ArrayList<BasicObjects.BasicObject> objects = new ArrayList<>();

        boolean addedTodayHeader = false;
        boolean addedYesterdayHeader = false;
        boolean addedThisWeekHeader = false;
        boolean addedThisMonthHeader = false;
        boolean addedLastMonthHeader = false;
        boolean addedOlderHeader = false;

        ArrayList<Tickets.Ticket> tickets = this.list;

        Log.i(TAG, "populateTripList: Preparing the dividers and trips...");
        for (int i = 0; i < (tickets.size()); i++) {

            Tickets.Ticket curTicket = tickets.get(i);

            // Ticket was today
            if (curTicket.modifiedon.getDayOfMonth() == DateTime.now().getDayOfMonth() &&
                    curTicket.modifiedon.getMonthOfYear() == DateTime.now().getMonthOfYear() &&
                    curTicket.modifiedon.getYear() == DateTime.now().getYear()) {
                if (addedTodayHeader == false) {
                    BasicObjects.BasicObject headerObj = new BasicObjects.BasicObject();
                    headerObj.isHeader = true;
                    headerObj.topText = ("Today");
                    objects.add(headerObj);
                    addedTodayHeader = true;
                    Log.d(TAG + "populateList", "Added a header object to the array that will eventually be a header childView in the list view named, 'Today' - This will not be added again!");
                }
                // Ticket was yesterday
            } else if (curTicket.modifiedon.getDayOfMonth() == DateTime.now().minusDays(1).getDayOfMonth() &&
                    curTicket.modifiedon.getMonthOfYear() == DateTime.now().minusDays(1).getMonthOfYear() &&
                    curTicket.modifiedon.getYear() == DateTime.now().minusDays(1).getYear()) {
                if (addedYesterdayHeader == false) {
                    BasicObjects.BasicObject headerObj = new BasicObjects.BasicObject();
                    headerObj.isHeader = true;
                    headerObj.topText = ("Yesterday");
                    objects.add(headerObj);
                    addedYesterdayHeader = true;
                    Log.d(TAG + "populateList", "Added a header object to the array that will eventually be a header childView in the list view named, 'Yesterday' - This will not be added again!");
                }
                // Ticket was this week
            }  else if (curTicket.modifiedon.getWeekOfWeekyear() == DateTime.now().getWeekOfWeekyear() &&
                    curTicket.modifiedon.getMonthOfYear() == DateTime.now().getMonthOfYear() &&
                    curTicket.modifiedon.getYear() == DateTime.now().getYear()) {
                if (addedThisWeekHeader == false) {
                    BasicObjects.BasicObject headerObj = new BasicObjects.BasicObject();
                    headerObj.isHeader = true;
                    headerObj.topText = ("This week");
                    objects.add(headerObj);
                    addedThisWeekHeader = true;
                    Log.d(TAG + "populateList", "Added a header object to the array that will eventually be a header childView in the list view named, 'This week' - This will not be added again!");
                }
                // Ticket was this month
            } else if (curTicket.modifiedon.getMonthOfYear() == DateTime.now().getMonthOfYear() &&
                    curTicket.modifiedon.getYear() == DateTime.now().getYear()) {
                if (addedThisMonthHeader == false) {
                    BasicObjects.BasicObject headerObj = new BasicObjects.BasicObject();
                    headerObj.isHeader = true;
                    headerObj.topText = ("This month");
                    objects.add(headerObj);
                    addedThisMonthHeader = true;
                    Log.d(TAG + "populateList", "Added a header object to the array that will eventually be a header childView in the list view named, 'This month' - This will not be added again!");
                }
                // Ticket was last month
            } else if (curTicket.modifiedon.getMonthOfYear() == DateTime.now().minusMonths(1).getMonthOfYear() &&
                    curTicket.modifiedon.getYear() == DateTime.now().minusMonths(1).getYear()) {
                if (addedLastMonthHeader == false) {
                    BasicObjects.BasicObject headerObj = new BasicObjects.BasicObject();
                    headerObj.isHeader = true;
                    headerObj.topText = ("Last month");
                    objects.add(headerObj);
                    addedLastMonthHeader = true;
                    Log.d(TAG + "populateList", "Added a header object to the array that will eventually be a header childView in the list view named, 'Last month' - This will not be added again!");
                }
                // Ticket was older than 2 months.
            } else {
                if (addedOlderHeader == false) {
                    BasicObjects.BasicObject headerObj = new BasicObjects.BasicObject();
                    headerObj.isHeader = true;
                    headerObj.topText = ("Total");
                    objects.add(headerObj);
                    addedOlderHeader = true;
                    Log.d(TAG + "populateList", "Added a header object to the array that will eventually be a header childView in the list view named, 'Older' - This will not be added again!");
                }
            }

            // Add the ticket as a BasicObject
            BasicObjects.BasicObject object = new BasicObjects.BasicObject(curTicket.title,
                    curTicket.ticketnumber + "\n" + curTicket.ownerName, curTicket);
            object.middleText = curTicket.customerFormatted + "\nModified on: " + Helpers.DatesAndTimes.getPrettyDateAndTime(curTicket.modifiedon);
            // object.topRightText = Helpers.DatesAndTimes.getPrettyDateAndTime(ticket.modifiedon);
            object.bottomRightText = curTicket.statusFormatted;
            object.iconResource = R.drawable.ticket1;
            objects.add(object);
        }
        return objects;
    }

    public static class Ticket extends CrmEntities.CrmEntity implements Parcelable {

        private static final String TAG = "Ticket";

        /**************************************************************************************
         *                                  Case type values
         **************************************************************************************/
        // public static final int COMPLAINT

            /*public String etag;
            public String entityid;*/
        public String statecodeFormatted;
        public int statecode;
        public String statusFormatted;
        public int statuscode;
        public String caseTypeFormatted;
        public String contactid;
        public String contactFirstname;
        public String contactLastname;
        public String contactFullname;
        public int casetype;
        public DateTime createdon;
        public String ticketnumber;
        public String ownerName;
        public String ownerid;
        public DateTime modifiedon;
        public String title;
        public String priorityFormatted;
        public int priority;
        public String description;
        public String modifiedByFormatted;
        public String modifiedBy;
        public String caseOriginFormatted;
        public int caseorigin;
        public String customerFormatted;
        public String customerid;
        public String subjectid;
        public String subjectFormatted;
        public String createdby;
        public String createdByFormatted;
        public String territoryid;
        public String territoryFormatted;
        public String repFormatted;
        public String repid;

        public Ticket() { }

        public Ticket(JSONObject json) {
            try {
                if (!json.isNull("etag")) {
                    this.etag = (json.getString("etag"));
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
                if (!json.isNull("statuscodeFormattedValue")) {
                    this.statusFormatted = (json.getString("statuscodeFormattedValue"));
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
                if (!json.isNull("casetypecodeFormattedValue")) {
                    this.caseTypeFormatted = (json.getString("casetypecodeFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("casetypecode")) {
                    this.casetype = (json.getInt("casetypecode"));
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
                if (!json.isNull("ticketnumber")) {
                    this.ticketnumber = (json.getString("ticketnumber"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_ownerid_valueFormattedValue")) {
                    this.ownerName = (json.getString("_ownerid_valueFormattedValue"));
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
                if (!json.isNull("modifiedon")) {
                    this.modifiedon = (new DateTime(json.getString("modifiedon")));
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
                if (!json.isNull("prioritycode")) {
                    this.priority = (json.getInt("prioritycode"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("prioritycodeFormattedValue")) {
                    this.priorityFormatted = (json.getString("prioritycodeFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("description")) {
                    this.description = (json.getString("description"));
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
                if (!json.isNull("caseorigincode")) {
                    this.caseorigin = (json.getInt("caseorigincode"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("caseorigincodeFormattedValue")) {
                    this.caseOriginFormatted = (json.getString("caseorigincodeFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_subjectid_valueFormattedValue")) {
                    this.subjectFormatted = (json.getString("_subjectid_valueFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_subjectid_value")) {
                    this.subjectid = (json.getString("_subjectid_value"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_subjectid_valueFormattedValue")) {
                    this.subjectFormatted = (json.getString("_subjectid_valueFormattedValue"));
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
                if (!json.isNull("_createdby_value")) {
                    this.createdby = (json.getString("_createdby_value"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_customerid_valueFormattedValue")) {
                    this.customerFormatted = (json.getString("_customerid_valueFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_customerid_value")) {
                    this.customerid = (json.getString("_customerid_value"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("incidentid")) {
                    this.entityid = (json.getString("incidentid"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("a_4b5945b8a4a64613afc1ae1d5e6828c7_territoryidFormattedValue")) {
                    this.territoryFormatted = (json.getString("a_4b5945b8a4a64613afc1ae1d5e6828c7_territoryidFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("a_4b5945b8a4a64613afc1ae1d5e6828c7_territoryid")) {
                    this.territoryid = (json.getString("a_4b5945b8a4a64613afc1ae1d5e6828c7_territoryid"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("a_4b5945b8a4a64613afc1ae1d5e6828c7_msus_salesrepFormattedValue")) {
                    this.repFormatted = (json.getString("a_4b5945b8a4a64613afc1ae1d5e6828c7_msus_salesrepFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("a_4b5945b8a4a64613afc1ae1d5e6828c7_msus_salesrep")) {
                    this.repid = (json.getString("a_4b5945b8a4a64613afc1ae1d5e6828c7_msus_salesrep"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("a_b49161e62067e71180d6005056a36b9b_contactid")) {
                    this.contactid = (json.getString("a_b49161e62067e71180d6005056a36b9b_contactid"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("a_b49161e62067e71180d6005056a36b9b_fullname")) {
                    this.contactFullname = (json.getString("a_b49161e62067e71180d6005056a36b9b_fullname"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("a_b49161e62067e71180d6005056a36b9b_firstname")) {
                    this.contactFirstname = (json.getString("a_b49161e62067e71180d6005056a36b9b_firstname"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("a_b49161e62067e71180d6005056a36b9b_lastname")) {
                    this.contactLastname = (json.getString("a_b49161e62067e71180d6005056a36b9b_lastname"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            /*

             */

        }

        public BasicEntity toBasicEntity() {

            BasicEntity entity = new BasicEntity(this);

            try {
                // Set the created basicentity to non-editable so it cannot be edited on the BasicEntityActivity activity.
                if (this.statecode != 0) {
                    entity.isEditable = false;
                    entity.cannotEditReason = "Cannot edit a closed/cancelled ticket using MediBuddy (yet).  Sorry!";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            BasicEntity.EntityBasicField ticketNumber = new BasicEntity.EntityBasicField("Ticket number:", this.ticketnumber);
            ticketNumber.crmFieldName = "ticketnumber";
            ticketNumber.isReadOnly = true;
            entity.fields.add(ticketNumber);

            BasicEntity.EntityBasicField titleField = new BasicEntity.EntityBasicField("Title:", this.title);
            titleField.isRequired = true;
            titleField.crmFieldName = "title";
            entity.fields.add(titleField);

            BasicEntity.EntityBasicField descriptionField = new BasicEntity.EntityBasicField("Description:", this.description);
            descriptionField.crmFieldName = "description";
            descriptionField.isRequired = true;
            entity.fields.add(descriptionField);

            BasicEntity.EntityBasicField accountField = new BasicEntity.EntityBasicField("Customer:", this.customerFormatted);
            accountField.isAccountField = true;
            accountField.crmFieldName = "customerid";
            accountField.account = new CrmEntities.Accounts.Account(this.customerid, this.customerFormatted);
            accountField.isRequired = true;
            entity.fields.add(accountField);

            BasicEntity.EntityBasicField territory = new BasicEntity.EntityBasicField("Territory:", this.territoryFormatted);
            territory.isReadOnly = true;
            entity.fields.add(territory);

            BasicEntity.EntityBasicField rep = new BasicEntity.EntityBasicField("Rep:", this.repFormatted);
            rep.isReadOnly = true;
            entity.fields.add(rep);

            BasicEntity.EntityBasicField contact = new BasicEntity.EntityBasicField("Contact:", this.contactFullname);
            contact.isContactField = true;
            contact.crmFieldName = "new_mw_contact";
            Contacts.Contact objContact = new Contacts.Contact();
            objContact.entityid = this.contactid;
            objContact.firstname = this.contactFirstname;
            objContact.lastname = this.contactLastname;
            contact.contact = objContact;
            entity.fields.add(contact);

            BasicEntity.EntityBasicField createdOn = new BasicEntity.EntityBasicField("Created on:", Helpers.DatesAndTimes.getPrettyDateAndTime(this.createdon));
            createdOn.isReadOnly = true;
            createdOn.isDateTimeField = true;
            entity.fields.add(createdOn);

            BasicEntity.EntityBasicField createdBy = new BasicEntity.EntityBasicField("Created by:", this.createdByFormatted);
            createdBy.isReadOnly = true;
            entity.fields.add(createdBy);

            BasicEntity.EntityBasicField modifiedOn = new BasicEntity.EntityBasicField("Modified on:", Helpers.DatesAndTimes.getPrettyDateAndTime(this.modifiedon));
            modifiedOn.isDateTimeField = true;
            modifiedOn.isReadOnly = true;
            entity.fields.add(modifiedOn);

            BasicEntity.EntityBasicField modifiedBy = new BasicEntity.EntityBasicField("Modified by:", this.modifiedByFormatted);
            modifiedBy.isReadOnly = true;
            entity.fields.add(modifiedBy);

            ArrayList<BasicEntity.EntityBasicField.OptionSetValue> caseTypes = new ArrayList<>();
            caseTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("Complaint", "100000004"));
            caseTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("Service Request", "100000005"));
            caseTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("NONCON", "100000006"));
            caseTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("CAPA", "100000007"));
            caseTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("Question", "1"));
            caseTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("Problem", "2"));
            caseTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("Request", "3"));
            caseTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("Evaluation", "100000000"));
            caseTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("Sales", "100000001"));
            BasicEntity.EntityBasicField caseType = new BasicEntity.EntityBasicField("Case type: ", caseTypeFormatted);
            caseType.crmFieldName = "casetypecode";
            caseType.optionSetValues = caseTypes;
            caseType.isOptionSet = true;
            caseType.isReadOnly = false;
            entity.fields.add(caseType);

            ArrayList<BasicEntity.EntityStatusReason> statusValues = new ArrayList<>();
            statusValues.add(new BasicEntity.EntityStatusReason("In Progress", "1", "0"));
            statusValues.add(new BasicEntity.EntityStatusReason("On Hold", "2", "0"));
            statusValues.add(new BasicEntity.EntityStatusReason("To be inspected", "100000002", "0"));
            statusValues.add(new BasicEntity.EntityStatusReason("Waiting on rep", "3", "0"));
            statusValues.add(new BasicEntity.EntityStatusReason("Waiting for product", "4", "0"));
            statusValues.add(new BasicEntity.EntityStatusReason("Waiting on customer", "100000001", "0"));
            statusValues.add(new BasicEntity.EntityStatusReason("To be billed", "100000003", "0"));
            statusValues.add(new BasicEntity.EntityStatusReason("Problem solved", "5", "0"));
            entity.availableEntityStatusReasons = statusValues;
            entity.entityStatusReason = new BasicEntity.EntityStatusReason(this.statusFormatted,
                    Integer.toString(this.statuscode), Integer.toString(this.statecode));

            ArrayList<BasicEntity.EntityBasicField.OptionSetValue> caseOrigins = new ArrayList<>();
            caseOrigins.add(new BasicEntity.EntityBasicField.OptionSetValue("Saleslogix", "100000000"));
            caseOrigins.add(new BasicEntity.EntityBasicField.OptionSetValue("Phone", "1"));
            caseOrigins.add(new BasicEntity.EntityBasicField.OptionSetValue("Email", "2"));
            caseOrigins.add(new BasicEntity.EntityBasicField.OptionSetValue("Web", "3"));
            caseOrigins.add(new BasicEntity.EntityBasicField.OptionSetValue("Facebook", "2483"));
            caseOrigins.add(new BasicEntity.EntityBasicField.OptionSetValue("Twitter", "3986"));
            BasicEntity.EntityBasicField caseOrigin = new BasicEntity.EntityBasicField("Case origin: ", caseOriginFormatted);
            caseOrigin.optionSetValues = caseOrigins;
            caseOrigin.crmFieldName = "caseorigincode";
            caseOrigin.isOptionSet = true;
            caseOrigin.isReadOnly = false;
            entity.fields.add(caseOrigin);

            ArrayList<BasicEntity.EntityBasicField.OptionSetValue> subjects = new ArrayList<>();
            subjects.add(new BasicEntity.EntityBasicField.OptionSetValue("Credit hold", "D8BAD965-5B66-E711-80D6-005056A36B9B"));
            subjects.add(new BasicEntity.EntityBasicField.OptionSetValue("Customer service", "6E0E1407-5B66-E711-80D6-005056A36B9B"));
            subjects.add(new BasicEntity.EntityBasicField.OptionSetValue("Evaluation", "CA14106D-6766-E711-80D6-005056A36B9B"));
            subjects.add(new BasicEntity.EntityBasicField.OptionSetValue("<Enter a subject>", "E1ABC067-429A-E711-80D8-005056A36B9B"));
            subjects.add(new BasicEntity.EntityBasicField.OptionSetValue("Information", "42ADD710-E698-E911-80F4-005056A36B9B"));
            subjects.add(new BasicEntity.EntityBasicField.OptionSetValue("Non-Conformance", "085CBAEA-E885-E811-80E4-005056A36B9B"));
            subjects.add(new BasicEntity.EntityBasicField.OptionSetValue("Product complaint (cable)", "4BD83C47-5B66-E711-80D6-005056A36B9B"));
            subjects.add(new BasicEntity.EntityBasicField.OptionSetValue("Product complaint (flowmeter)", "FD242450-5B66-E711-80D6-005056A36B9B"));
            subjects.add(new BasicEntity.EntityBasicField.OptionSetValue("Product complaint (probe)", "44663885-CE62-476D-9268-C95A618B3CD9"));
            subjects.add(new BasicEntity.EntityBasicField.OptionSetValue("System Customization", "0D137FFB-6666-E711-80D6-005056A36B9B"));
            BasicEntity.EntityBasicField subject = new BasicEntity.EntityBasicField("Subject: ", subjectFormatted);
            subject.isOptionSet = true;
            subject.crmFieldName = "subjectid";
            subject.optionSetValues = subjects;
            subject.isReadOnly = false;
            entity.fields.add(subject);

            return entity;
        }

        public static BasicEntity toBasicCreateEntity() {


            Tickets.Ticket ticket = new Tickets.Ticket();

            ticket.subjectFormatted = "Product complaint (probe)";
            ticket.subjectid = "44663885-CE62-476D-9268-C95A618B3CD9";

            ticket.caseorigin = 1;
            ticket.caseOriginFormatted = "Phone";

            ticket.caseTypeFormatted = "Problem";
            ticket.casetype = 2;

            ticket.customerFormatted = "";
            ticket.customerid = "";

            ticket.contactid = "";
            ticket.contactLastname = "";
            ticket.contactFirstname = "";
            ticket.contactFullname = "";

            ticket.customerid = "";
            ticket.customerFormatted = "";

            ticket.createdon = DateTime.now();
            ticket.createdByFormatted = MediUser.getMe().fullname;
            ticket.createdby = MediUser.getMe().systemuserid;

            ticket.modifiedon = DateTime.now();
            ticket.modifiedBy = MediUser.getMe().systemuserid;
            ticket.modifiedByFormatted = MediUser.getMe().fullname;

            return ticket.toBasicEntity();


        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.statecodeFormatted);
            dest.writeInt(this.statecode);
            dest.writeString(this.statusFormatted);
            dest.writeInt(this.statuscode);
            dest.writeString(this.caseTypeFormatted);
            dest.writeString(this.contactid);
            dest.writeString(this.contactFirstname);
            dest.writeString(this.contactLastname);
            dest.writeString(this.contactFullname);
            dest.writeInt(this.casetype);
            dest.writeSerializable(this.createdon);
            dest.writeString(this.ticketnumber);
            dest.writeString(this.ownerName);
            dest.writeString(this.ownerid);
            dest.writeSerializable(this.modifiedon);
            dest.writeString(this.title);
            dest.writeString(this.priorityFormatted);
            dest.writeInt(this.priority);
            dest.writeString(this.description);
            dest.writeString(this.modifiedByFormatted);
            dest.writeString(this.modifiedBy);
            dest.writeString(this.caseOriginFormatted);
            dest.writeInt(this.caseorigin);
            dest.writeString(this.customerFormatted);
            dest.writeString(this.customerid);
            dest.writeString(this.subjectid);
            dest.writeString(this.subjectFormatted);
            dest.writeString(this.createdby);
            dest.writeString(this.createdByFormatted);
            dest.writeString(this.territoryid);
            dest.writeString(this.territoryFormatted);
            dest.writeString(this.repFormatted);
            dest.writeString(this.repid);
        }

        public void readFromParcel(Parcel source) {
            this.statecodeFormatted = source.readString();
            this.statecode = source.readInt();
            this.statusFormatted = source.readString();
            this.statuscode = source.readInt();
            this.caseTypeFormatted = source.readString();
            this.contactid = source.readString();
            this.contactFirstname = source.readString();
            this.contactLastname = source.readString();
            this.contactFullname = source.readString();
            this.casetype = source.readInt();
            this.createdon = (DateTime) source.readSerializable();
            this.ticketnumber = source.readString();
            this.ownerName = source.readString();
            this.ownerid = source.readString();
            this.modifiedon = (DateTime) source.readSerializable();
            this.title = source.readString();
            this.priorityFormatted = source.readString();
            this.priority = source.readInt();
            this.description = source.readString();
            this.modifiedByFormatted = source.readString();
            this.modifiedBy = source.readString();
            this.caseOriginFormatted = source.readString();
            this.caseorigin = source.readInt();
            this.customerFormatted = source.readString();
            this.customerid = source.readString();
            this.subjectid = source.readString();
            this.subjectFormatted = source.readString();
            this.createdby = source.readString();
            this.createdByFormatted = source.readString();
            this.territoryid = source.readString();
            this.territoryFormatted = source.readString();
            this.repFormatted = source.readString();
            this.repid = source.readString();
        }

        protected Ticket(Parcel in) {
            this.statecodeFormatted = in.readString();
            this.statecode = in.readInt();
            this.statusFormatted = in.readString();
            this.statuscode = in.readInt();
            this.caseTypeFormatted = in.readString();
            this.contactid = in.readString();
            this.contactFirstname = in.readString();
            this.contactLastname = in.readString();
            this.contactFullname = in.readString();
            this.casetype = in.readInt();
            this.createdon = (DateTime) in.readSerializable();
            this.ticketnumber = in.readString();
            this.ownerName = in.readString();
            this.ownerid = in.readString();
            this.modifiedon = (DateTime) in.readSerializable();
            this.title = in.readString();
            this.priorityFormatted = in.readString();
            this.priority = in.readInt();
            this.description = in.readString();
            this.modifiedByFormatted = in.readString();
            this.modifiedBy = in.readString();
            this.caseOriginFormatted = in.readString();
            this.caseorigin = in.readInt();
            this.customerFormatted = in.readString();
            this.customerid = in.readString();
            this.subjectid = in.readString();
            this.subjectFormatted = in.readString();
            this.createdby = in.readString();
            this.createdByFormatted = in.readString();
            this.territoryid = in.readString();
            this.territoryFormatted = in.readString();
            this.repFormatted = in.readString();
            this.repid = in.readString();
        }

        public static final Parcelable.Creator<Tickets.Ticket> CREATOR = new Parcelable.Creator<Tickets.Ticket>() {
            @Override
            public Tickets.Ticket createFromParcel(Parcel source) {
                return new Tickets.Ticket(source);
            }

            @Override
            public Tickets.Ticket[] newArray(int size) {
                return new Tickets.Ticket[size];
            }
        };
    }
}
