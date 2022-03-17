package com.fimbleenterprises.medimileage;

import android.util.Log;

import com.fimbleenterprises.medimileage.objects_and_containers.MediUser;

import org.joda.time.DateTime;

import java.util.ArrayList;

import javax.annotation.Nullable;

import static com.fimbleenterprises.medimileage.CrmQueries.Operators.getDateOperator;
import static com.fimbleenterprises.medimileage.QueryFactory.*;
import static com.fimbleenterprises.medimileage.QueryFactory.Filter.FilterType.AND;
import static com.fimbleenterprises.medimileage.QueryFactory.Filter.FilterType.OR;

/**
 * Common CRM queries used throughout the application.  If you cannot find a query here you can always
 * build your own using the QueryFactory class.  In fact, every query in this class was built using
 * the QueryFactory class - so, it works.
 */
public class CrmQueries {

    public static final String EAST_REGIONID = "00AB0144-2AFA-E711-80DE-005056A36B9B";
    public static final String WEST_REGIONID = "61E8B94E-2AFA-E711-80DE-005056A36B9B";
    public static final String WEST_BUSINESSUNITID = "02AD3593-FB81-E711-80D7-005056A36B9B";
    public static final String EAST_BUSINESSUNITID = "101E629C-FB81-E711-80D7-005056A36B9B";
    public static final String JOHN_SYSTEMUSERID = "DAA46FDF-5B7C-E711-80D1-005056A32EEA";

    /**
     * An enum specific to the col_customerinventory entity representing available statuscode values
     */
    public enum CustomerInventoryStatusCode {
        EXPIRED, LOST, RETURNED, ONSITE, SCRAPPED, ANY;

        /**
         * Returns the actual statuscode value for the selected status reason
         * @return The value as a string consumable by our CrmRest api
         */
        public String getCrmValue() {
            switch (this) {
                case EXPIRED:
                    return "181400000";
                case LOST:
                    return "181400007";
                case RETURNED:
                    return "181400005";
                case ONSITE:
                    return "1";
                case SCRAPPED:
                    return "181400004";
                default:
                    return null;
            }
        }
    }

    public static class Operators {
        public enum DateOperator {
            TODAY, YESTERDAY, THIS_WEEK, THIS_MONTH, THIS_YEAR, LAST_WEEK, LAST_MONTH, LAST_YEAR, LAST_7_DAYS, LAST_14_DAYS, LAST_X_MONTHS, LAST_X_YEARS
        }

        public static String getDateOperator(DateOperator operator) {
            switch (operator) {
                case YESTERDAY:
                    return Filter.Operator.YESTERDAY;
                case THIS_WEEK:
                    return Filter.Operator.THIS_WEEK;
                case THIS_MONTH:
                    return Filter.Operator.THIS_MONTH;
                case THIS_YEAR:
                    return Filter.Operator.THIS_YEAR;
                case LAST_WEEK:
                    return Filter.Operator.LAST_WEEK;
                case LAST_MONTH:
                    return Filter.Operator.LAST_MONTH;
                case LAST_YEAR:
                    return Filter.Operator.LAST_YEAR;
                case LAST_X_MONTHS:
                    return Filter.Operator.LAST_X_MONTHS;
                case LAST_X_YEARS:
                    return Filter.Operator.LAST_X_YEARS;
                default:
                    return Filter.Operator.TODAY;
            }
        }
    }

    public static class Trips {

        public static String getAllTripsWithoutEntriesByOwnerByMonthAndYear(int year, int month, String userid) {
            QueryFactory factory = new QueryFactory("msus_fulltrip");
            factory.addColumn("msus_name");
            factory.addColumn("msus_tripcode");
            factory.addColumn("msus_dt_tripdate");
            factory.addColumn("msus_reimbursement_rate");
            factory.addColumn("msus_reimbursement");
            factory.addColumn("msus_totaldistance");
            factory.addColumn("msus_trip_duration");
            factory.addColumn("msus_is_manual");
            factory.addColumn("msus_edited");
            factory.addColumn("msus_trip_minder_killed");
            factory.addColumn("msus_fulltripid");
            factory.addColumn("msus_is_submitted");
            factory.addColumn("ownerid");

            DateTime firstDayOfSuppliedMonth = new DateTime(year, month, 1, 0, 0);
            DateTime lastDayOfSuppliedMonth = Helpers.DatesAndTimes.getLastDayOfMonthAsDateTimeObject(month, year);
            String onOrAfter = Helpers.DatesAndTimes.getPrettyDate(firstDayOfSuppliedMonth);
            String onOrBefore = Helpers.DatesAndTimes.getPrettyDate(lastDayOfSuppliedMonth);

            Filter filter = new Filter(Filter.FilterType.AND);

            Filter.FilterCondition condition1 = new Filter.FilterCondition("msus_dt_tripdate",
                    Filter.Operator.ON_OR_AFTER, onOrAfter);
            Filter.FilterCondition condition2 = new Filter.FilterCondition("msus_dt_tripdate",
                    Filter.Operator.ON_OR_BEFORE, onOrBefore);
            Filter.FilterCondition condition3 = new Filter.FilterCondition("ownerid",
                    Filter.Operator.EQUALS, userid);

            filter.addCondition(condition1);
            filter.addCondition(condition2);
            filter.addCondition(condition3);

            factory.setFilter(filter);

            QueryFactory.SortClause sortby = new QueryFactory.SortClause("msus_tripcode", false, QueryFactory.SortClause.ClausePosition.ONE);
            factory.addSortClause(sortby);

            String query = factory.construct();

            return query;
        }

        public static String getAllTripsWithoutEntriesByOwnerForLastXmonths(int xMonths, String userid) {
            QueryFactory factory = new QueryFactory("msus_fulltrip");
            factory.addColumn("msus_name");
            factory.addColumn("msus_tripcode");
            factory.addColumn("msus_dt_tripdate");
            factory.addColumn("msus_reimbursement_rate");
            factory.addColumn("msus_reimbursement");
            factory.addColumn("msus_totaldistance");
            factory.addColumn("msus_trip_duration");
            factory.addColumn("msus_is_manual");
            factory.addColumn("msus_edited");
            factory.addColumn("msus_trip_minder_killed");
            factory.addColumn("msus_fulltripid");
            factory.addColumn("msus_is_submitted");
            factory.addColumn("ownerid");

            DateTime now = DateTime.now();
            int lastDayOfMonth = now.plusMonths(1).minusDays(1).getDayOfMonth();
            long startMillis = new DateTime(now.getYear(), now.getMonthOfYear(), 1, 1, 1).getMillis();
            long endMillis = new DateTime(now.getYear(), now.getMonthOfYear(), lastDayOfMonth, 1, 1).getMillis();

            Filter filter = new Filter(Filter.FilterType.AND);

            Filter.FilterCondition condition1 = new Filter.FilterCondition("msus_dt_tripdate",
                    Filter.Operator.LAST_X_MONTHS, Integer.toString(xMonths));
            Filter.FilterCondition condition2 = new Filter.FilterCondition("ownerid",
                    Filter.Operator.EQUALS, userid);

            filter.addCondition(condition1);
            filter.addCondition(condition2);

            factory.setFilter(filter);

            QueryFactory.SortClause sortby = new QueryFactory.SortClause("msus_tripcode", false, QueryFactory.SortClause.ClausePosition.ONE);
            factory.addSortClause(sortby);

            String query = factory.construct();

            return query;
        }

        public static String getAllTripsByOwnerForLastXmonths(int xMonths, String userid) {
            QueryFactory factory = new QueryFactory("msus_fulltrip");
            factory.addColumn("msus_name");
            factory.addColumn("msus_tripcode");
            factory.addColumn("msus_dt_tripdate");
            factory.addColumn("msus_reimbursement_rate");
            factory.addColumn("msus_reimbursement");
            factory.addColumn("msus_totaldistance");
            factory.addColumn("msus_trip_duration");
            factory.addColumn("msus_is_manual");
            factory.addColumn("msus_edited");
            factory.addColumn("msus_trip_minder_killed");
            factory.addColumn("msus_fulltripid");
            factory.addColumn("msus_trip_entries_json");
            factory.addColumn("msus_is_submitted");
            factory.addColumn("ownerid");

            DateTime now = DateTime.now();
            int lastDayOfMonth = now.plusMonths(1).minusDays(1).getDayOfMonth();
            long startMillis = new DateTime(now.getYear(), now.getMonthOfYear(), 1, 1, 1).getMillis();
            long endMillis = new DateTime(now.getYear(), now.getMonthOfYear(), lastDayOfMonth, 1, 1).getMillis();

            Filter filter = new Filter(Filter.FilterType.AND);

            Filter.FilterCondition condition1 = new Filter.FilterCondition("msus_dt_tripdate",
                    Filter.Operator.LAST_X_MONTHS, Integer.toString(xMonths));
            Filter.FilterCondition condition2 = new Filter.FilterCondition("ownerid",
                    Filter.Operator.EQUALS, userid);

            filter.addCondition(condition1);
            filter.addCondition(condition2);

            factory.setFilter(filter);

            QueryFactory.SortClause sortby = new QueryFactory.SortClause("msus_tripcode", false, QueryFactory.SortClause.ClausePosition.ONE);
            factory.addSortClause(sortby);

            String query = factory.construct();

            return query;
        }

        public static String getTripidByTripcode(long tripcode) {
            QueryFactory queryFactory = new QueryFactory("msus_fulltrip");
            queryFactory.addColumn("msus_fulltripid");
            Filter filter = new Filter(Filter.FilterType.AND);
            filter.addCondition(new Filter.FilterCondition("msus_tripcode", Filter.Operator.EQUALS,
                    Long.toString(tripcode)));
            queryFactory.setFilter(filter);
            String query = queryFactory.construct();
            return query;
        }

    }

    public static class Tickets {

        /**
         * Use to not apply a statuscode to the filter.
         */
        public static final int ANY = 69;
        /**
         * The value is the actual integer value for the statuscode in Dynamics for the incident entity.
         */
        public static final int IN_PROGRESS = 1;
        /**
         * The value is the actual integer value for the statuscode in Dynamics for the incident entity.
         */
        public static final int ON_HOLD = 2;
        /**
         * The value is the actual integer value for the statuscode in Dynamics for the incident entity.
         */
        public static final int TO_BE_INSPECTED = 100000002;
        /**
         * The value is the actual integer value for the statuscode in Dynamics for the incident entity.
         */
        public static final int WAITING_ON_REP = 3;
        /**
         * The value is the actual integer value for the statuscode in Dynamics for the incident entity.
         */
        public static final int WAITING_FOR_PRODUCT = 4;
        /**
         * The value is the actual integer value for the statuscode in Dynamics for the incident entity.
         */
        public static final int WAITING_ON_CUSTOMER = 100000001;
        /**
         * The value is the actual integer value for the statuscode in Dynamics for the incident entity.
         */
        public static final int TO_BE_BILLED = 100000003;
        /**
         * The value is the actual integer value for the statuscode in Dynamics for the incident entity.
         */
        public static final int PROBLEM_SOLVED = 5;
        // state codes
        public static final int OPEN = 0;
        public static final int CLOSED = 1;
        public static final int BOTH = -1;

        // Code to disregard lastXmonths queries
        public static final int IGNORE_DATE_RANGE = -1;

        /*public enum CaseFilter {
            ANY, NOT_RESOLVED, IN_PROGRESS, ON_HOLD, TO_BE_INSPECTED, WAITING_FOR_PRODUCT,
            WAITING_ON_CUSTOMER, PROBLEM_SOLVED, TO_BE_BILLED, WAITING_ON_REP
        }*/

        // status codes
        /*static final int IN_PROGRESS = 1;
        static final int ON_HOLD = 2;
        static final int TO_BE_INSPECTED = 100000002;
        static final int WAITING_ON_REP = 3;
        static final int WAITING_FOR_PRODUCT = 4;
        static final int WAITING_ON_CUSTOMER = 100000001;
        static final int TO_BE_BILLED = 100000003;
        static final int PROBLEM_SOLVED = 5;*/

        public static String getIncident(String caseid) {

            // Instantiate a new constructor for the case entity and add the columns we want to see
            QueryFactory query = new QueryFactory("incident");
            query.addColumn("incidentid");
            query.addColumn("caseorigincode");
            query.addColumn("title");
            query.addColumn("ticketnumber");
            query.addColumn("subjectid");
            query.addColumn("statuscode");
            query.addColumn("statecode");
            query.addColumn("servicestage");
            query.addColumn("prioritycode");
            query.addColumn("primarycontactid");
            query.addColumn("ownerid");
            query.addColumn("ticketnumber");
            query.addColumn("modifiedon");
            query.addColumn("modifiedby");
            query.addColumn("incidentstagecode");
            query.addColumn("description");
            query.addColumn("customerid");
            query.addColumn("accountid");
            query.addColumn("createdon");
            query.addColumn("new_mw_contact");
            query.addColumn("createdby");
            query.addColumn("new_mw_contact");
            query.addColumn("casetypecode");

            // Filter creation to make use of our conditions
            Filter filter = new Filter(AND);
            filter.addCondition(new Filter.FilterCondition("incidentid","eq",caseid));
            query.setFilter(filter);

            // Link entity creation to join to the account entity and apply our territory condition
            LinkEntity linkEntity_accountStuff = new LinkEntity(LINK_ENTITY_STRINGS.CASES_TO_ACCOUNT);
            linkEntity_accountStuff.addColumn(new EntityColumn("territoryid"));
            linkEntity_accountStuff.addColumn(new EntityColumn("msus_salesrep"));
            linkEntity_accountStuff.addColumn(new EntityColumn("accountnumber"));
            query.addLinkEntity(linkEntity_accountStuff);

            // Link entity creation to join the contact info
            LinkEntity linkEntity_contact = new LinkEntity("contact", "contactid", "new_mw_contact", "a_b49161e62067e71180d6005056a36b9b");
            linkEntity_contact.addColumn(new EntityColumn("fullname"));
            linkEntity_contact.addColumn(new EntityColumn("emailaddress1"));
            linkEntity_contact.addColumn(new EntityColumn("telephone1"));
            linkEntity_contact.addColumn(new EntityColumn("contactid"));
            query.addLinkEntity(linkEntity_contact);

            // Spit out the encoded query
            String rslt = query.construct();
            return rslt;
        }

        /**
         * Gets all tickets at the specified account from all time.
         * @param customerid Customer to query
         * @param statuscode Case status
         * @return An encoded xml query.
         */
        public static String getAccountIncidents(String customerid, int statuscode) {
            return getAccountIncidents(customerid, statuscode, -1, -1);
        }

        /**
         * Gets all tickets at the specified account from all time.
         * @param customerid Customer to query
         * @param statuscode Case status
         * @param statecode Case state
         * @return An encoded xml query.
         */
        public static String getAccountIncidents(String customerid, int statuscode, int statecode) {
            return getAccountIncidents(customerid, statuscode, statecode, -1);
        }

        /**
         * Gets all tickets at the specified account from all time.
         * @param customerid Customer to query
         * @param statuscode Case status
         * @param statecode Case state
         * @param lastXmonths The amount of months to limit to
         * @return An encoded xml query.
         */
        public static String getAccountIncidents(String customerid, int statuscode, int statecode, int lastXmonths) {
            QueryFactory query = new QueryFactory("incident");
            query.addColumn("ticketnumber");
            query.addColumn("title");
            query.addColumn("createdon");
            query.addColumn("customerid");
            query.addColumn("ownerid");
            query.addColumn("caseorigincode");
            query.addColumn("new_mw_contact");
            query.addColumn("statuscode");
            query.addColumn("new_accountnumber");
            query.addColumn("incidentid");
            query.addColumn("subjectid");
            query.addColumn("statecode");
            query.addColumn("stageid");
            query.addColumn("processid");
            query.addColumn("prioritycode");
            query.addColumn("modifiedon");
            query.addColumn("modifiedby");
            query.addColumn("description");
            query.addColumn("createdby");
            query.addColumn("casetypecode");
            query.addColumn("incidentstagecode");

            LinkEntity linkEntity = new LinkEntity("account", "accountid",
                    "customerid", "a_4b5945b8a4a64613afc1ae1d5e6828c7");
            linkEntity.addColumn(new EntityColumn("territoryid"));
            linkEntity.addColumn(new EntityColumn("msus_salesrep"));
            Filter filter = new Filter(AND);
            linkEntity.addFilter(filter);
            linkEntity.isOuterLink = true;
            query.addLinkEntity(linkEntity);

            // Link entity creation to join the contact info
            LinkEntity linkEntity_contact = new LinkEntity("contact", "contactid", "new_mw_contact", "a_b49161e62067e71180d6005056a36b9b");
            linkEntity_contact.addColumn(new EntityColumn("fullname"));
            linkEntity_contact.addColumn(new EntityColumn("firstname"));
            linkEntity_contact.addColumn(new EntityColumn("lastname"));
            linkEntity_contact.addColumn(new EntityColumn("emailaddress1"));
            linkEntity_contact.addColumn(new EntityColumn("telephone1"));
            linkEntity_contact.addColumn(new EntityColumn("contactid"));
            linkEntity_contact.isOuterLink = true;
            query.addLinkEntity(linkEntity_contact);

            SortClause sortClause = new SortClause("modifiedon", true, SortClause.ClausePosition.ONE);
            query.addSortClause(sortClause);

            Filter.FilterCondition condition1 = new Filter.FilterCondition("modifiedon",
                    getDateOperator(Operators.DateOperator.LAST_X_MONTHS), Integer.toString(lastXmonths));
            Filter.FilterCondition condition2 = new Filter.FilterCondition("statuscode",
                    Filter.Operator.EQUALS, Integer.toString(statuscode));
            Filter.FilterCondition condition3 = new Filter.FilterCondition("statecode",
                    Filter.Operator.EQUALS, Integer.toString(statecode));
            Filter.FilterCondition condition4 = new Filter.FilterCondition("customerid",
                    Filter.Operator.EQUALS, customerid);


            Filter filter1 = new Filter(AND);
            if (lastXmonths != IGNORE_DATE_RANGE) {
                filter1.addCondition(condition1);
            }
            if (statuscode != ANY) {
                filter1.addCondition(condition2);
            }
            if (statecode != BOTH) {
                filter1.addCondition(condition3);
            }
            filter1.addCondition(condition4);
            query.setFilter(filter1);

            return query.construct();

        }

        public static String getAccountIncidents(String accountid) {
            QueryFactory query = new QueryFactory("incident");
            query.addColumn("ticketnumber");
            query.addColumn("title");
            query.addColumn("createdon");
            query.addColumn("customerid");
            query.addColumn("ownerid");
            query.addColumn("caseorigincode");
            query.addColumn("statuscode");
            query.addColumn("new_accountnumber");
            query.addColumn("incidentid");
            query.addColumn("subjectid");
            query.addColumn("statecode");
            query.addColumn("stageid");
            query.addColumn("processid");
            query.addColumn("prioritycode");
            query.addColumn("modifiedon");
            query.addColumn("modifiedby");
            query.addColumn("description");
            query.addColumn("createdby");
            query.addColumn("casetypecode");
            query.addColumn("incidentstagecode");

            LinkEntity linkEntity = new LinkEntity("account", "accountid",
                    "customerid", "a_4b5945b8a4a64613afc1ae1d5e6828c7");
            linkEntity.addColumn(new EntityColumn("territoryid"));
            linkEntity.addColumn(new EntityColumn("msus_salesrep"));
            Filter filter = new Filter(AND);
            linkEntity.addFilter(filter);
            query.addLinkEntity(linkEntity);

            LinkEntity linkEntity_contact = new LinkEntity("contact", "contactid", "new_mw_contact", "a_b49161e62067e71180d6005056a36b9b");
            linkEntity_contact.addColumn(new EntityColumn("fullname"));
            linkEntity_contact.addColumn(new EntityColumn("emailaddress1"));
            linkEntity_contact.addColumn(new EntityColumn("telephone1"));
            linkEntity_contact.addColumn(new EntityColumn("contactid"));
            linkEntity_contact.isOuterLink = true;
            query.addLinkEntity(linkEntity_contact);

            SortClause sortClause = new SortClause("modifiedon", true, SortClause.ClausePosition.ONE);
            query.addSortClause(sortClause);
            Filter.FilterCondition condition2 = new Filter.FilterCondition("customerid",
                    Filter.Operator.EQUALS, accountid);

            Filter filter1 = new Filter(AND,condition2);
            query.setFilter(filter1);

            return query.construct();
        }

        public static String getNonResolvedIncidents(String territoryid, int lastXmonths) {
            QueryFactory query = new QueryFactory("incident");
            query.addColumn("ticketnumber");
            query.addColumn("title");
            query.addColumn("createdon");
            query.addColumn("customerid");
            query.addColumn("ownerid");
            query.addColumn("caseorigincode");
            query.addColumn("statuscode");
            query.addColumn("new_accountnumber");
            query.addColumn("incidentid");
            query.addColumn("subjectid");
            query.addColumn("statecode");
            query.addColumn("stageid");
            query.addColumn("processid");
            query.addColumn("prioritycode");
            query.addColumn("modifiedon");
            query.addColumn("modifiedby");
            query.addColumn("description");
            query.addColumn("createdby");
            query.addColumn("casetypecode");
            query.addColumn("incidentstagecode");

            Filter.FilterCondition condition = new Filter.FilterCondition("territoryid",
                    Filter.Operator.EQUALS, territoryid);

            LinkEntity linkEntity = new LinkEntity("account", "accountid",
                    "customerid", "a_4b5945b8a4a64613afc1ae1d5e6828c7");
            linkEntity.addColumn(new EntityColumn("territoryid"));
            linkEntity.addColumn(new EntityColumn("msus_salesrep"));
            Filter filter = new Filter(AND);
            filter.addCondition(condition);
            linkEntity.addFilter(filter);
            query.addLinkEntity(linkEntity);

            // Link entity creation to join the contact info
            LinkEntity linkEntity_contact = new LinkEntity("contact", "contactid", "new_mw_contact", "a_b49161e62067e71180d6005056a36b9b");
            linkEntity_contact.addColumn(new EntityColumn("fullname"));
            linkEntity_contact.addColumn(new EntityColumn("emailaddress1"));
            linkEntity_contact.addColumn(new EntityColumn("telephone1"));
            linkEntity_contact.addColumn(new EntityColumn("contactid"));
            linkEntity_contact.isOuterLink = true;
            query.addLinkEntity(linkEntity_contact);

            SortClause sortClause1 = new SortClause("statuscode", true, SortClause.ClausePosition.ONE);
            SortClause sortClause2 = new SortClause("modifiedon", true, SortClause.ClausePosition.TWO);
            query.addSortClause(sortClause1);
            query.addSortClause(sortClause2);

            Filter.FilterCondition condition1 = new Filter.FilterCondition("modifiedon",
                    getDateOperator(Operators.DateOperator.LAST_X_MONTHS), Integer.toString(lastXmonths));
            Filter.FilterCondition condition2 = new Filter.FilterCondition("statuscode",
                    Filter.Operator.NOT_EQUALS, Integer.toString(PROBLEM_SOLVED));
            Filter.FilterCondition condition3 = new Filter.FilterCondition("statecode",
                    Filter.Operator.NOT_EQUALS, Integer.toString(0));

            Filter filter1 = new Filter(AND);
            if (lastXmonths != IGNORE_DATE_RANGE) {
                filter1.addCondition(condition1);
            }
            filter1.addCondition(condition2);
            query.setFilter(filter1);

            return query.construct();

        }

        public static String getNonResolvedIncidents(int lastXmonths) {
            QueryFactory query = new QueryFactory("incident");
            query.addColumn("ticketnumber");
            query.addColumn("title");
            query.addColumn("createdon");
            query.addColumn("customerid");
            query.addColumn("ownerid");
            query.addColumn("caseorigincode");
            query.addColumn("statuscode");
            query.addColumn("new_accountnumber");
            query.addColumn("incidentid");
            query.addColumn("subjectid");
            query.addColumn("statecode");
            query.addColumn("stageid");
            query.addColumn("processid");
            query.addColumn("prioritycode");
            query.addColumn("modifiedon");
            query.addColumn("modifiedby");
            query.addColumn("description");
            query.addColumn("createdby");
            query.addColumn("casetypecode");
            query.addColumn("incidentstagecode");

            LinkEntity linkEntity = new LinkEntity("account", "accountid",
                    "customerid", "a_4b5945b8a4a64613afc1ae1d5e6828c7");
            linkEntity.addColumn(new EntityColumn("territoryid"));
            linkEntity.addColumn(new EntityColumn("msus_salesrep"));
            Filter filter = new Filter(AND);
            linkEntity.addFilter(filter);
            query.addLinkEntity(linkEntity);

            // Link entity creation to join the contact info
            LinkEntity linkEntity_contact = new LinkEntity("contact", "contactid", "new_mw_contact", "a_b49161e62067e71180d6005056a36b9b");
            linkEntity_contact.addColumn(new EntityColumn("fullname"));
            linkEntity_contact.addColumn(new EntityColumn("emailaddress1"));
            linkEntity_contact.addColumn(new EntityColumn("telephone1"));
            linkEntity_contact.addColumn(new EntityColumn("contactid"));
            linkEntity_contact.isOuterLink = true;
            query.addLinkEntity(linkEntity_contact);

            SortClause sortClause1 = new SortClause("statuscode", true, SortClause.ClausePosition.ONE);
            SortClause sortClause2 = new SortClause("modifiedon", true, SortClause.ClausePosition.TWO);
            query.addSortClause(sortClause1);
            query.addSortClause(sortClause2);

            Filter.FilterCondition condition1 = new Filter.FilterCondition("modifiedon",
                    getDateOperator(Operators.DateOperator.LAST_X_MONTHS), Integer.toString(lastXmonths));
            Filter.FilterCondition condition2 = new Filter.FilterCondition("statuscode",
                    Filter.Operator.NOT_EQUALS, Integer.toString(PROBLEM_SOLVED));
            Filter.FilterCondition condition3 = new Filter.FilterCondition("statecode",
                    Filter.Operator.NOT_EQUALS, Integer.toString(0));

            Filter filter1 = new Filter(AND);
            if (lastXmonths != IGNORE_DATE_RANGE) {
                filter1.addCondition(condition1);
            }
            filter1.addCondition(condition2);
            query.setFilter(filter1);

            return query.construct();

        }

        public static String getIncidents(String territoryid, int statuscode, int lastXmonths) {
            QueryFactory query = new QueryFactory("incident");
            query.addColumn("ticketnumber");
            query.addColumn("title");
            query.addColumn("createdon");
            query.addColumn("customerid");
            query.addColumn("ownerid");
            query.addColumn("caseorigincode");
            query.addColumn("new_mw_contact");
            query.addColumn("statuscode");
            query.addColumn("new_accountnumber");
            query.addColumn("incidentid");
            query.addColumn("subjectid");
            query.addColumn("statecode");
            query.addColumn("stageid");
            query.addColumn("processid");
            query.addColumn("prioritycode");
            query.addColumn("modifiedon");
            query.addColumn("modifiedby");
            query.addColumn("description");
            query.addColumn("createdby");
            query.addColumn("casetypecode");
            query.addColumn("incidentstagecode");

            Filter.FilterCondition condition = new Filter.FilterCondition("territoryid",
                    Filter.Operator.EQUALS, territoryid);

            LinkEntity linkEntity = new LinkEntity("account", "accountid",
                    "customerid", "a_4b5945b8a4a64613afc1ae1d5e6828c7");
            linkEntity.addColumn(new EntityColumn("territoryid"));
            linkEntity.addColumn(new EntityColumn("msus_salesrep"));
            Filter filter = new Filter(AND);
            filter.addCondition(condition);
            linkEntity.addFilter(filter);
            query.addLinkEntity(linkEntity);

            // Link entity creation to join the contact info
            LinkEntity linkEntity_contact = new LinkEntity("contact", "contactid", "new_mw_contact", "a_b49161e62067e71180d6005056a36b9b");
            linkEntity_contact.addColumn(new EntityColumn("fullname"));
            linkEntity_contact.addColumn(new EntityColumn("firstname"));
            linkEntity_contact.addColumn(new EntityColumn("lastname"));
            linkEntity_contact.addColumn(new EntityColumn("emailaddress1"));
            linkEntity_contact.addColumn(new EntityColumn("telephone1"));
            linkEntity_contact.addColumn(new EntityColumn("contactid"));
            linkEntity_contact.isOuterLink = true;
            query.addLinkEntity(linkEntity_contact);

            SortClause sortClause = new SortClause("modifiedon", true, SortClause.ClausePosition.ONE);
            query.addSortClause(sortClause);

            Filter.FilterCondition condition1 = new Filter.FilterCondition("modifiedon",
                    getDateOperator(Operators.DateOperator.LAST_X_MONTHS), Integer.toString(lastXmonths));
            Filter.FilterCondition condition2 = new Filter.FilterCondition("statuscode",
                    Filter.Operator.EQUALS, Integer.toString(statuscode));

            Filter filter1 = new Filter(AND);
            if (lastXmonths != IGNORE_DATE_RANGE) {
                filter1.addCondition(condition1);
            }
            if (statuscode != ANY) {
                filter1.addCondition(condition2);
            }
            query.setFilter(filter1);

            return query.construct();

        }

        public static String getIncidents(int statuscode, int statecode, int lastXmonths) {
            QueryFactory query = new QueryFactory("incident");
            query.addColumn("ticketnumber");
            query.addColumn("title");
            query.addColumn("createdon");
            query.addColumn("customerid");
            query.addColumn("ownerid");
            query.addColumn("caseorigincode");
            query.addColumn("new_mw_contact");
            query.addColumn("statuscode");
            query.addColumn("new_accountnumber");
            query.addColumn("incidentid");
            query.addColumn("subjectid");
            query.addColumn("statecode");
            query.addColumn("stageid");
            query.addColumn("processid");
            query.addColumn("prioritycode");
            query.addColumn("modifiedon");
            query.addColumn("modifiedby");
            query.addColumn("description");
            query.addColumn("createdby");
            query.addColumn("casetypecode");
            query.addColumn("incidentstagecode");

            LinkEntity linkEntity = new LinkEntity("account", "accountid",
                    "customerid", "a_4b5945b8a4a64613afc1ae1d5e6828c7");
            linkEntity.addColumn(new EntityColumn("territoryid"));
            linkEntity.addColumn(new EntityColumn("msus_salesrep"));
            Filter filter = new Filter(AND);
            linkEntity.addFilter(filter);
            query.addLinkEntity(linkEntity);

            // Link entity creation to join the contact info
            LinkEntity linkEntity_contact = new LinkEntity("contact", "contactid", "new_mw_contact", "a_b49161e62067e71180d6005056a36b9b");
            linkEntity_contact.addColumn(new EntityColumn("fullname"));
            linkEntity_contact.addColumn(new EntityColumn("firstname"));
            linkEntity_contact.addColumn(new EntityColumn("lastname"));
            linkEntity_contact.addColumn(new EntityColumn("emailaddress1"));
            linkEntity_contact.addColumn(new EntityColumn("telephone1"));
            linkEntity_contact.addColumn(new EntityColumn("contactid"));
            linkEntity_contact.isOuterLink = true;
            query.addLinkEntity(linkEntity_contact);

            SortClause sortClause = new SortClause("modifiedon", true, SortClause.ClausePosition.ONE);
            query.addSortClause(sortClause);

            Filter.FilterCondition condition1 = new Filter.FilterCondition("modifiedon",
                    getDateOperator(Operators.DateOperator.LAST_X_MONTHS), Integer.toString(lastXmonths));
            Filter.FilterCondition condition2 = new Filter.FilterCondition("statuscode",
                    Filter.Operator.EQUALS, Integer.toString(statuscode));
            Filter.FilterCondition condition3 = new Filter.FilterCondition("statecode",
                    Filter.Operator.EQUALS, Integer.toString(statecode));

            Filter filter1 = new Filter(AND);
            if (lastXmonths != IGNORE_DATE_RANGE) {
                filter1.addCondition(condition1);
            }
            if (statuscode != ANY) {
                filter1.addCondition(condition2);
            }
            filter1.addCondition(condition3);
            query.setFilter(filter1);

            return query.construct();

        }

        public static String getIncidents(String territoryid, int statuscode, int statecode, int lastXmonths) {
            QueryFactory query = new QueryFactory("incident");
            query.addColumn("ticketnumber");
            query.addColumn("title");
            query.addColumn("createdon");
            query.addColumn("customerid");
            query.addColumn("ownerid");
            query.addColumn("caseorigincode");
            query.addColumn("new_mw_contact");
            query.addColumn("statuscode");
            query.addColumn("new_accountnumber");
            query.addColumn("incidentid");
            query.addColumn("subjectid");
            query.addColumn("statecode");
            query.addColumn("stageid");
            query.addColumn("processid");
            query.addColumn("prioritycode");
            query.addColumn("modifiedon");
            query.addColumn("modifiedby");
            query.addColumn("description");
            query.addColumn("createdby");
            query.addColumn("casetypecode");
            query.addColumn("incidentstagecode");

            Filter.FilterCondition condition = new Filter.FilterCondition("territoryid",
                    Filter.Operator.EQUALS, territoryid);

            LinkEntity linkEntity = new LinkEntity("account", "accountid",
                    "customerid", "a_4b5945b8a4a64613afc1ae1d5e6828c7");
            linkEntity.addColumn(new EntityColumn("territoryid"));
            linkEntity.addColumn(new EntityColumn("msus_salesrep"));
            Filter filter = new Filter(AND);
            filter.addCondition(condition);
            linkEntity.addFilter(filter);
            query.addLinkEntity(linkEntity);

            // Link entity creation to join the contact info
            LinkEntity linkEntity_contact = new LinkEntity("contact", "contactid", "new_mw_contact", "a_b49161e62067e71180d6005056a36b9b");
            linkEntity_contact.addColumn(new EntityColumn("fullname"));
            linkEntity_contact.addColumn(new EntityColumn("firstname"));
            linkEntity_contact.addColumn(new EntityColumn("lastname"));
            linkEntity_contact.addColumn(new EntityColumn("emailaddress1"));
            linkEntity_contact.addColumn(new EntityColumn("telephone1"));
            linkEntity_contact.addColumn(new EntityColumn("contactid"));
            linkEntity_contact.isOuterLink = true;
            query.addLinkEntity(linkEntity_contact);

            SortClause sortClause = new SortClause("modifiedon", true, SortClause.ClausePosition.ONE);
            query.addSortClause(sortClause);

            Filter.FilterCondition condition1 = new Filter.FilterCondition("modifiedon",
                    getDateOperator(Operators.DateOperator.LAST_X_MONTHS), Integer.toString(lastXmonths));
            Filter.FilterCondition condition2 = new Filter.FilterCondition("statuscode",
                    Filter.Operator.EQUALS, Integer.toString(statuscode));
            Filter.FilterCondition condition3 = new Filter.FilterCondition("statecode",
                    Filter.Operator.EQUALS, Integer.toString(statecode));

            Filter filter1 = new Filter(AND);

            // If caller passes -1 we do not filter on date
            if (lastXmonths != IGNORE_DATE_RANGE) {
                filter1.addCondition(condition1);
            }
            if (statuscode != ANY) {
                filter1.addCondition(condition2);
            }
            filter1.addCondition(condition3);
            query.setFilter(filter1);

            return query.construct();

        }
    }

    public static class Users {
        public static String getUser(String email) {

            // Instantiate a new constructor for the case entity and add the columns we want to see
            QueryFactory query = new QueryFactory("systemuser");
            query.addColumn("fullname");
            query.addColumn("address1_telephone1");
            query.addColumn("businessunitid");
            query.addColumn("siteid");
            query.addColumn("positionid");
            query.addColumn("systemuserid");
            query.addColumn("domainname");
            query.addColumn("territoryid");
            query.addColumn("employeeid");
            query.addColumn("photourl");
            query.addColumn("msus_last_used_milebuddy");
            query.addColumn("msus_last_viewed_mileage_stats");
            query.addColumn("msus_last_synced_mileage");
            query.addColumn("msus_last_opened_settings");
            query.addColumn("msus_last_accessed_milebuddy");
            query.addColumn("msus_last_generated_receipt");
            query.addColumn("msus_last_generated_receipt_milebuddy");
            query.addColumn("msus_milebuddy_last_accessed_territory_changer");
            query.addColumn("msus_last_accessed_other_user_trips");
            query.addColumn("msus_last_accessed_medibuddy");
            query.addColumn("msus_is_mileage_user");
            query.addColumn("msus_ismanager");
            query.addColumn("msus_is_driving");
            query.addColumn("address1_composite");
            query.addColumn("address1_latitude");
            query.addColumn("address1_longitude");
            query.addColumn("msus_medibuddy_managed_territories");
            query.addColumn("msus_push_onallorders");
            query.addColumn("msus_push_onorder");
            query.addColumn("internalemailaddress");
            query.addColumn("mobilephone");
            query.addColumn("title");
            query.addColumn("parentsystemuserid");
            query.addColumn("positionid");
            query.addColumn("msus_user_salesregion");

            // Filter creation to make use of our conditions
            Filter filter = new Filter(AND);
            filter.addCondition(new Filter.FilterCondition("internalemailaddress"
                    ,"eq",email));
            query.setFilter(filter);

            // Link entity creation to join to the account entity and apply our territory condition
            LinkEntity linkEntity_territory =
                    new LinkEntity(LINK_ENTITY_STRINGS.TERRITORY_TO_USER);
            linkEntity_territory.addColumn(new EntityColumn("name"));
            linkEntity_territory.addColumn(new EntityColumn("new_salesrepresentative"));
            linkEntity_territory.addColumn(new EntityColumn("msus_salesregion"));
            linkEntity_territory.addColumn(new EntityColumn("managerid"));
            linkEntity_territory.addColumn(new EntityColumn("new_businessunit"));
            query.addLinkEntity(linkEntity_territory);

            LinkEntity linkEntity_Manager =
                    new LinkEntity(LINK_ENTITY_STRINGS.MANAGER_TO_USER);
            linkEntity_Manager.addColumn(new EntityColumn("businessunitid"));
            query.addLinkEntity(linkEntity_Manager);

            // Spit out the encoded query
            String rslt = query.construct();
            return rslt;
        }

        public static String getUsUsers() {
            // Instantiate a new constructor for the case entity and add the columns we want to see
            QueryFactory query = new QueryFactory("systemuser");
            query.addColumn("fullname");
            query.addColumn("address1_telephone1");
            query.addColumn("businessunitid");
            query.addColumn("siteid");
            query.addColumn("positionid");
            query.addColumn("systemuserid");
            query.addColumn("domainname");
            query.addColumn("territoryid");
            query.addColumn("employeeid");
            query.addColumn("photourl");
            query.addColumn("msus_last_used_milebuddy");
            query.addColumn("msus_last_viewed_mileage_stats");
            query.addColumn("msus_last_synced_mileage");
            query.addColumn("msus_last_opened_settings");
            query.addColumn("msus_last_accessed_milebuddy");
            query.addColumn("msus_last_generated_receipt");
            query.addColumn("msus_last_generated_receipt_milebuddy");
            query.addColumn("msus_milebuddy_last_accessed_territory_changer");
            query.addColumn("msus_last_accessed_other_user_trips");
            query.addColumn("msus_last_accessed_medibuddy");
            query.addColumn("msus_is_mileage_user");
            query.addColumn("msus_ismanager");
            query.addColumn("msus_is_driving");
            query.addColumn("address1_composite");
            query.addColumn("address1_latitude");
            query.addColumn("address1_longitude");
            query.addColumn("msus_medibuddy_managed_territories");
            query.addColumn("msus_push_onallorders");
            query.addColumn("msus_push_onorder");
            query.addColumn("internalemailaddress");
            query.addColumn("mobilephone");
            query.addColumn("title");
            query.addColumn("parentsystemuserid");
            query.addColumn("positionid");
            query.addColumn("msus_user_salesregion");

            // Filter creation to make use of our conditions
            Filter filter = new Filter(AND);
            filter.addCondition(new Filter.FilterCondition("msus_is_us_user"
                    ,"eq","1"));
            query.setFilter(filter);

            // Link entity creation to join to the account entity and apply our territory condition
            LinkEntity linkEntity_territory =
                    new LinkEntity(LINK_ENTITY_STRINGS.TERRITORY_TO_USER);
            linkEntity_territory.addColumn(new EntityColumn("name"));
            linkEntity_territory.addColumn(new EntityColumn("new_salesrepresentative"));
            linkEntity_territory.addColumn(new EntityColumn("msus_salesregion"));
            linkEntity_territory.addColumn(new EntityColumn("managerid"));
            linkEntity_territory.addColumn(new EntityColumn("new_businessunit"));
            query.addLinkEntity(linkEntity_territory);

            LinkEntity linkEntity_Manager =
                    new LinkEntity(LINK_ENTITY_STRINGS.MANAGER_TO_USER);
            linkEntity_Manager.addColumn(new EntityColumn("businessunitid"));
            query.addLinkEntity(linkEntity_Manager);

            // Spit out the encoded query
            String rslt = query.construct();
            return rslt;
        }
    }

    public static class Territories {

        public static String getTerritoriesWithManagersAssigned() {

            // Instantiate a new constructor for the case entity and add the columns we want to see
            QueryFactory query = new QueryFactory("territory");
            query.addColumn("name");
            query.addColumn("territoryid");
            query.addColumn("new_salesrepresentative");
            query.addColumn("msus_salesregion");
            query.addColumn("managerid");

            // Filter creation to make use of our conditions
            Filter filter = new Filter(AND);
            filter.addCondition(new Filter.FilterCondition("managerid",
                            Filter.Operator.CONTAINS_DATA));
            query.setFilter(filter);

            SortClause sortClause = new SortClause("name", false, SortClause.ClausePosition.ONE);
            query.addSortClause(sortClause);

            // Spit out the encoded query
            String rslt = query.construct();
            return rslt;
        }
    }

    public static class Leads {
        private static final String TAG = "LEADS";
        public static String getTerritoryLeads(String territoryid) {
            QueryFactory query = new QueryFactory("lead");
            query.addColumn("fullname");
            query.addColumn("createdon");
            query.addColumn("subject");
            query.addColumn("emailaddress1");
            query.addColumn("ownerid");
            query.addColumn("leadsourcecode");
            query.addColumn("leadqualitycode");
            query.addColumn("msus_leadsource_congress");
            query.addColumn("preferredcontactmethodcode");
            query.addColumn("leadid");
            query.addColumn("col_vascular");
            query.addColumn("col_transplant");
            query.addColumn("col_totalproceduresperyear");
            query.addColumn("statuscode");
            query.addColumn("statecode");
            query.addColumn("msus_leadsource");
            query.addColumn("mobilephone");
            query.addColumn("lastname");
            query.addColumn("jobtitle");
            query.addColumn("firstname");
            query.addColumn("donotphone");
            query.addColumn("donotpostalmail");
            query.addColumn("donotfax");
            query.addColumn("donotemail");
            query.addColumn("donotbulkemail");
            query.addColumn("description");
            query.addColumn("decisionmaker");
            query.addColumn("createdby");
            query.addColumn("parentcontactid");
            query.addColumn("address1_composite");
            query.addColumn("msus_account_in_system");

            SortClause sortClause = new SortClause("createdon", true, SortClause.ClausePosition.ONE);
            query.addSortClause(sortClause);

            LinkEntity le1 = new LinkEntity("account", "accountid", "parentaccountid", "a_e1b9eb98752946799234e25abb9bb751");
            le1.addColumn(new EntityColumn("name"));
            le1.addColumn(new EntityColumn("accountnumber"));
            le1.addColumn(new EntityColumn("accountid"));
            query.addLinkEntity(le1);

            LinkEntity le3 = new LinkEntity("systemuser", "systemuserid", "owninguser", "av");
            Filter.FilterCondition condition = new Filter.FilterCondition("territoryid", Filter.Operator.EQUALS, territoryid);
            Filter filter = new Filter(AND, condition);
            le3.addFilter(filter);
            query.addLinkEntity(le3);

            /*LinkEntity le4 = new LinkEntity("account", "accountid", "customerid", "a_64b8dfc0d53940e082640b6f5dd3707d");
            le4.addColumn(new EntityColumn("accountnumber"));
            le4.addColumn(new EntityColumn("name"));
            query.addLinkEntity(le4);*/

            return query.construct();
        }

        public static String getAllLeads() {
            QueryFactory query = new QueryFactory("lead");
            query.addColumn("fullname");
            query.addColumn("createdon");
            query.addColumn("subject");
            query.addColumn("emailaddress1");
            query.addColumn("ownerid");
            query.addColumn("leadsourcecode");
            query.addColumn("leadqualitycode");
            query.addColumn("msus_leadsource_congress");
            query.addColumn("preferredcontactmethodcode");
            query.addColumn("leadid");
            query.addColumn("col_vascular");
            query.addColumn("col_transplant");
            query.addColumn("col_totalproceduresperyear");
            query.addColumn("statuscode");
            query.addColumn("statecode");
            query.addColumn("msus_leadsource");
            query.addColumn("mobilephone");
            query.addColumn("lastname");
            query.addColumn("jobtitle");
            query.addColumn("firstname");
            query.addColumn("donotphone");
            query.addColumn("donotpostalmail");
            query.addColumn("donotfax");
            query.addColumn("donotemail");
            query.addColumn("donotbulkemail");
            query.addColumn("description");
            query.addColumn("decisionmaker");
            query.addColumn("createdby");
            query.addColumn("parentcontactid");
            query.addColumn("address1_composite");
            query.addColumn("msus_account_in_system");

            SortClause sortClause = new SortClause("createdon", true, SortClause.ClausePosition.ONE);
            query.addSortClause(sortClause);

            LinkEntity le1 = new LinkEntity("account", "accountid", "parentaccountid", "a_e1b9eb98752946799234e25abb9bb751");
            le1.addColumn(new EntityColumn("name"));
            le1.addColumn(new EntityColumn("accountnumber"));
            le1.addColumn(new EntityColumn("accountid"));
            query.addLinkEntity(le1);

            /*LinkEntity le3 = new LinkEntity("systemuser", "systemuserid", "owninguser", "av");
            Filter.FilterCondition condition = new Filter.FilterCondition("territoryid", Filter.Operator.EQUALS, territoryid);
            Filter filter = new Filter(AND, condition);
            le3.addFilter(filter);
            query.addLinkEntity(le3);*/

            /*LinkEntity le4 = new LinkEntity("account", "accountid", "customerid", "a_64b8dfc0d53940e082640b6f5dd3707d");
            le4.addColumn(new EntityColumn("accountnumber"));
            le4.addColumn(new EntityColumn("name"));
            query.addLinkEntity(le4);*/

            return query.construct();
        }

        public static String getTerritoryLeads(String territoryid, LeadFilter leadFilter) {
            QueryFactory query = new QueryFactory("lead");
            query.addColumn("fullname");
            query.addColumn("createdon");
            query.addColumn("subject");
            query.addColumn("emailaddress1");
            query.addColumn("ownerid");
            query.addColumn("leadsourcecode");
            query.addColumn("leadqualitycode");
            query.addColumn("msus_leadsource_congress");
            query.addColumn("preferredcontactmethodcode");
            query.addColumn("leadid");
            query.addColumn("col_vascular");
            query.addColumn("col_transplant");
            query.addColumn("col_totalproceduresperyear");
            query.addColumn("statuscode");
            query.addColumn("statecode");
            query.addColumn("msus_leadsource");
            query.addColumn("telephone1");
            query.addColumn("mobilephone");
            query.addColumn("lastname");
            query.addColumn("jobtitle");
            query.addColumn("firstname");
            query.addColumn("donotphone");
            query.addColumn("donotpostalmail");
            query.addColumn("donotfax");
            query.addColumn("donotemail");
            query.addColumn("donotbulkemail");
            query.addColumn("description");
            query.addColumn("decisionmaker");
            query.addColumn("createdby");
            query.addColumn("parentcontactid");
            query.addColumn("address1_composite");
            query.addColumn("msus_account_in_system");

            SortClause sortClause = new SortClause("createdon", true, SortClause.ClausePosition.ONE);
            query.addSortClause(sortClause);

            LinkEntity le1 = new LinkEntity("account", "accountid", "parentaccountid", "a_e1b9eb98752946799234e25abb9bb751");
            le1.addColumn(new EntityColumn("name"));
            le1.addColumn(new EntityColumn("accountnumber"));
            le1.addColumn(new EntityColumn("accountid"));
            query.addLinkEntity(le1);

            LinkEntity le3 = new LinkEntity("systemuser", "systemuserid", "owninguser", "av");
            Filter.FilterCondition condition = new Filter.FilterCondition("territoryid", Filter.Operator.EQUALS, territoryid);
            Filter leFilter = new Filter(AND, condition);
            le3.addFilter(leFilter);
            query.addLinkEntity(le3);

            Filter coreQueryFilter = new Filter(AND);

            switch (leadFilter) {
                case QUALIFIED:
                    coreQueryFilter.addCondition(new Filter.FilterCondition("statuscode", Filter.Operator.EQUALS, "1"));
                    break;
                case DISQUALIFIED:
                    coreQueryFilter.addCondition(new Filter.FilterCondition("statuscode", Filter.Operator.EQUALS, "2"));
                    break;
                case LAST_THREE_MONTHS:
                    coreQueryFilter.addCondition(new Filter.FilterCondition("createdon", Filter.Operator.LAST_X_MONTHS, "3"));
                    break;
                default:
                    break;
            };

            if (coreQueryFilter.conditions != null && coreQueryFilter.conditions.size() > 0) {
                query.setFilter(coreQueryFilter);
            } else {
                Log.i(TAG, "getAllLeads ANY stipulated, will not filter!");
            }

            /*LinkEntity le3 = new LinkEntity("systemuser", "systemuserid", "owninguser", "av");
            Filter.FilterCondition condition = new Filter.FilterCondition("territoryid", Filter.Operator.EQUALS, territoryid);
            Filter filter = new Filter(AND, condition);
            le3.addFilter(filter);
            query.addLinkEntity(le3);*/

            /*LinkEntity le4 = new LinkEntity("account", "accountid", "customerid", "a_64b8dfc0d53940e082640b6f5dd3707d");
            le4.addColumn(new EntityColumn("accountnumber"));
            le4.addColumn(new EntityColumn("name"));
            query.addLinkEntity(le4);*/

            return query.construct();
        }

        public static String getAllLeads(LeadFilter leadFilter) {
            QueryFactory query = new QueryFactory("lead");
            query.addColumn("fullname");
            query.addColumn("createdon");
            query.addColumn("subject");
            query.addColumn("emailaddress1");
            query.addColumn("ownerid");
            query.addColumn("leadsourcecode");
            query.addColumn("leadqualitycode");
            query.addColumn("msus_leadsource_congress");
            query.addColumn("preferredcontactmethodcode");
            query.addColumn("leadid");
            query.addColumn("col_vascular");
            query.addColumn("col_transplant");
            query.addColumn("col_totalproceduresperyear");
            query.addColumn("statuscode");
            query.addColumn("statecode");
            query.addColumn("msus_leadsource");
            query.addColumn("mobilephone");
            query.addColumn("lastname");
            query.addColumn("jobtitle");
            query.addColumn("firstname");
            query.addColumn("donotphone");
            query.addColumn("donotpostalmail");
            query.addColumn("donotfax");
            query.addColumn("donotemail");
            query.addColumn("donotbulkemail");
            query.addColumn("description");
            query.addColumn("decisionmaker");
            query.addColumn("createdby");
            query.addColumn("parentcontactid");
            query.addColumn("address1_composite");
            query.addColumn("msus_account_in_system");

            SortClause sortClause = new SortClause("createdon", true, SortClause.ClausePosition.ONE);
            query.addSortClause(sortClause);

            LinkEntity le1 = new LinkEntity("account", "accountid", "parentaccountid", "a_e1b9eb98752946799234e25abb9bb751");
            le1.addColumn(new EntityColumn("name"));
            le1.addColumn(new EntityColumn("accountnumber"));
            le1.addColumn(new EntityColumn("accountid"));
            query.addLinkEntity(le1);

            Filter filter = new Filter(AND);
            switch (leadFilter) {
                case QUALIFIED:
                    filter.addCondition(new Filter.FilterCondition("statuscode", Filter.Operator.EQUALS, "1"));
                    break;
                case DISQUALIFIED:
                    filter.addCondition(new Filter.FilterCondition("statuscode", Filter.Operator.EQUALS, "2"));
                    break;
                case LAST_THREE_MONTHS:
                    filter.addCondition(new Filter.FilterCondition("createdon", Filter.Operator.LAST_X_MONTHS, "3"));
                    break;
                default:
                    break;
            };

            if (filter.conditions != null && filter.conditions.size() > 0) {
                query.setFilter(filter);
            } else {
                Log.i(TAG, "getAllLeads ANY stipulated, will not filter!");
            }

            /*LinkEntity le3 = new LinkEntity("systemuser", "systemuserid", "owninguser", "av");
            Filter.FilterCondition condition = new Filter.FilterCondition("territoryid", Filter.Operator.EQUALS, territoryid);
            Filter filter = new Filter(AND, condition);
            le3.addFilter(filter);
            query.addLinkEntity(le3);*/

            /*LinkEntity le4 = new LinkEntity("account", "accountid", "customerid", "a_64b8dfc0d53940e082640b6f5dd3707d");
            le4.addColumn(new EntityColumn("accountnumber"));
            le4.addColumn(new EntityColumn("name"));
            query.addLinkEntity(le4);*/

            return query.construct();
        }

        // Leads
        public enum LeadFilter {
            ANY, QUALIFIED, DISQUALIFIED, LAST_THREE_MONTHS;
        }
    }

    public static class Accounts {

        public static String getAccountInventory(String accountid, String itemgroupnumber, CustomerInventoryStatusCode status) {
            // Instantiate a new constructor for the case entity and add the columns we want to see
            QueryFactory query = new QueryFactory("col_customerinventory");
            query.addColumn("col_name");
            query.addColumn("statuscode");
            query.addColumn("col_serialnumber");
            query.addColumn("col_ownershipcapital");
            query.addColumn("new_physical_date"); // Added 1.83
            query.addColumn("modifiedon");
            query.addColumn("col_revision");
            query.addColumn("overriddencreatedon");
            query.addColumn("col_quantity");
            query.addColumn("col_productid");
            query.addColumn("col_item");
            query.addColumn("col_itemgroup");
            query.addColumn("col_batchnumber");
            query.addColumn("col_accountid");
            query.addColumn("col_referencenumber");
            query.addColumn("col_customerinventoryid");

            LinkEntity le1 = new LinkEntity("systemuser", "systemuserid", "owninguser", "a_4114a0e1fc19e71180d2005056a36b9b");
            le1.addColumn(new EntityColumn("domainname"));
            le1.addColumn(new EntityColumn("territoryid"));

            LinkEntity le2 = new LinkEntity("team", "teamid", "owningteam", "a_4814a0e1fc19e71180d2005056a36b9b");
            le2.addColumn(new EntityColumn("name"));

            LinkEntity le3 = new LinkEntity("account", "accountid", "col_accountid", "aa");
            Filter le3Filter = new Filter(AND);
            le3Filter.conditions.add(new Filter.FilterCondition("accountid", Filter.Operator.EQUALS, accountid));
            le3.addFilter(le3Filter);

            // Add our link entities
            // query.addLinkEntity(le1);
            // query.addLinkEntity(le2);
            query.addLinkEntity(le3);

            // Create a filter
            Filter filter = new Filter(AND);

            // Set filter conditions
            Filter.FilterCondition condition1 = new Filter.FilterCondition("col_itemgroup", Filter.Operator.CONTAINS, itemgroupnumber);
            Filter.FilterCondition condition2 = new Filter.FilterCondition("statuscode", Filter.Operator.EQUALS, status.getCrmValue());

            // Apply the conditions to the filter
            filter.addCondition(condition1);
            // Only add the status condition if one is stipulated
            if (status != CustomerInventoryStatusCode.ANY) {
                filter.addCondition(condition2);
            }

            // Set the query's global filter
            query.setFilter(filter);

            // Spit out the encoded query
            String rslt = query.construct();
            return rslt;
        }

        public static String getAccounts(@Nullable String territoryid) {

            // Instantiate a new constructor for the case entity and add the columns we want to see
            QueryFactory query = new QueryFactory("account");
            query.addColumn("name");
            query.addColumn("customertypecode");
            query.addColumn("accountid");
            query.addColumn("territoryid");
            query.addColumn("msus_salesrep");
            query.addColumn("msus_salesregionid");
            query.addColumn("col_agreementtype");
            query.addColumn("accountnumber");


            // Filter creation to make use of our conditions
            Filter filter = new Filter(AND);

            // Only active accounts
            filter.addCondition(new Filter.FilterCondition("statecode", "eq", "0"));
            filter.addCondition(new Filter.FilterCondition("accountnumber", Filter.Operator.CONTAINS_DATA));

            if (territoryid != null) {
                filter.addCondition(new Filter.FilterCondition("territoryid","eq", territoryid));
            }

            // Apply the filter
            query.setFilter(filter);

            // Link entity creation to join to the account entity and apply our territory condition
            LinkEntity le1 = new LinkEntity("businessunit", "businessunitid", "owningbusinessunit", "a_6ad8133d2f1e4c43a3da460bacb3d6a5");
            le1.addColumn(new EntityColumn("new_managername"));
            le1.addColumn(new EntityColumn("name"));

            SortClause sortClause = new SortClause("name", false, SortClause.ClausePosition.ONE);
            query.addSortClause(sortClause);

            // Spit out the encoded query
            String rslt = query.construct();
            return rslt;
        }

        /**
         * Builds a query to retrieve service agreements that will expire this month or the next.
         * @param territoryid
         * @return
         */
        public static String getExpiringServiceAgreementsByTerritory(String territoryid) {

            // Instantiate a new constructor for the case entity and add the columns we want to see
            QueryFactory query = new QueryFactory("msus_medistimserviceagreement");
            query.addColumn("msus_name");
            query.addColumn("msus_warrantytype");
            query.addColumn("msus_startdate");
            query.addColumn("msus_serialnumber");
            query.addColumn("msus_product");
            query.addColumn("msus_enddate");
            query.addColumn("msus_customer");
            query.addColumn("modifiedon");
            query.addColumn("modifiedby");
            query.addColumn("msus_declined");
            query.addColumn("msus_termlengthyears");
            query.addColumn("ownerid");
            query.addColumn("msus_medistimserviceagreementid");

            Filter mainFilter = new Filter(AND);
            mainFilter.addCondition(new Filter.FilterCondition("statecode", "eq", "0"));

            // Filter creation to make use of start and end date
            Filter filter_start_end_dates = new Filter(OR);
            filter_start_end_dates.addCondition(new Filter.FilterCondition("msus_enddate", Filter.Operator.THIS_MONTH));
            filter_start_end_dates.addCondition(new Filter.FilterCondition("msus_enddate", Filter.Operator.NEXT_MONTH));
            mainFilter.addFilter(filter_start_end_dates);

            query.setFilter(mainFilter);

            // Link entity creation to join to the account entity and apply our territory condition
            LinkEntity linkEntityForGettingTerritoryFromAccount = new LinkEntity("account"
                    , "accountid", "msus_customer", "a_963da520835eec11811d005056a36b9b");
            Filter territoryFilter = new Filter(AND);
            territoryFilter.addCondition(new Filter.FilterCondition("territoryid", "eq", territoryid));
            linkEntityForGettingTerritoryFromAccount.addFilter(territoryFilter);
            linkEntityForGettingTerritoryFromAccount.addColumn(new EntityColumn("territoryid"));
            query.addLinkEntity(linkEntityForGettingTerritoryFromAccount);

            SortClause sortClause1 = new SortClause("msus_enddate", true, SortClause.ClausePosition.ONE);
            query.addSortClause(sortClause1);
            SortClause sortClause2 = new SortClause("msus_customer", false, SortClause.ClausePosition.TWO);
            query.addSortClause(sortClause2);

            // Spit out the encoded query
            String rslt = query.construct();
            return rslt;
        }

        public static String getExpiredServiceAgreementsByTerritory(String territoryid) {

            // Instantiate a new constructor for the case entity and add the columns we want to see
            QueryFactory query = new QueryFactory("msus_medistimserviceagreement");
            query.addColumn("msus_name");
            query.addColumn("msus_warrantytype");
            query.addColumn("msus_startdate");
            query.addColumn("msus_serialnumber");
            query.addColumn("msus_product");
            query.addColumn("msus_enddate");
            query.addColumn("msus_customer");
            query.addColumn("modifiedon");
            query.addColumn("modifiedby");
            query.addColumn("msus_declined");
            query.addColumn("msus_termlengthyears");
            query.addColumn("ownerid");
            query.addColumn("msus_medistimserviceagreementid");

            Filter mainFilter = new Filter(AND);
            mainFilter.addCondition(new Filter.FilterCondition("statecode", "eq", "0"));

            // Filter creation to make use of start and end date
            Filter filter_start_end_dates = new Filter(OR);
            filter_start_end_dates.addCondition(new Filter.FilterCondition("msus_enddate", Filter.Operator.OLDER_THAN_X_DAYS, "1"));
            mainFilter.addFilter(filter_start_end_dates);

            query.setFilter(mainFilter);

            // Link entity creation to join to the account entity and apply our territory condition
            LinkEntity linkEntityForGettingTerritoryFromAccount = new LinkEntity("account"
                    , "accountid", "msus_customer", "a_963da520835eec11811d005056a36b9b");
            Filter territoryFilter = new Filter(AND);
            territoryFilter.addCondition(new Filter.FilterCondition("territoryid", "eq", territoryid));
            linkEntityForGettingTerritoryFromAccount.addFilter(territoryFilter);
            linkEntityForGettingTerritoryFromAccount.addColumn(new EntityColumn("territoryid"));
            query.addLinkEntity(linkEntityForGettingTerritoryFromAccount);

            SortClause sortClause1 = new SortClause("msus_enddate", true, SortClause.ClausePosition.ONE);
            query.addSortClause(sortClause1);
            SortClause sortClause2 = new SortClause("msus_customer", false, SortClause.ClausePosition.TWO);
            query.addSortClause(sortClause2);

            // Spit out the encoded query
            String rslt = query.construct();
            return rslt;
        }

        public static String getActiveServiceAgreementsByTerritory(String territoryid) {

            // Instantiate a new constructor for the case entity and add the columns we want to see
            QueryFactory query = new QueryFactory("msus_medistimserviceagreement");
            query.addColumn("msus_name");
            query.addColumn("msus_warrantytype");
            query.addColumn("msus_startdate");
            query.addColumn("msus_serialnumber");
            query.addColumn("msus_product");
            query.addColumn("msus_enddate");
            query.addColumn("msus_customer");
            query.addColumn("msus_declined");
            query.addColumn("modifiedon");
            query.addColumn("modifiedby");
            query.addColumn("msus_termlengthyears");
            query.addColumn("ownerid");
            query.addColumn("msus_medistimserviceagreementid");

            Filter mainFilter = new Filter(AND);
            mainFilter.addCondition(new Filter.FilterCondition("statecode", "eq", "0"));

            // Filter creation to make use of start and end date
            Filter filter_start_end_dates = new Filter(OR);
            filter_start_end_dates.addCondition(new Filter.FilterCondition("msus_enddate", Filter.Operator.TODAY));
            filter_start_end_dates.addCondition(new Filter.FilterCondition("msus_enddate", Filter.Operator.NEXT_X_YEARS, "30"));
            mainFilter.addFilter(filter_start_end_dates);

            query.setFilter(mainFilter);

            // Link entity creation to join to the account entity and apply our territory condition
            LinkEntity linkEntityForGettingTerritoryFromAccount = new LinkEntity("account"
                    , "accountid", "msus_customer", "a_963da520835eec11811d005056a36b9b");
            Filter territoryFilter = new Filter(AND);
            territoryFilter.addCondition(new Filter.FilterCondition("territoryid", "eq", territoryid));
            linkEntityForGettingTerritoryFromAccount.addFilter(territoryFilter);
            linkEntityForGettingTerritoryFromAccount.addColumn(new EntityColumn("territoryid"));
            query.addLinkEntity(linkEntityForGettingTerritoryFromAccount);

            SortClause sortClause1 = new SortClause("msus_enddate", true, SortClause.ClausePosition.ONE);
            query.addSortClause(sortClause1);
            SortClause sortClause2 = new SortClause("msus_customer", false, SortClause.ClausePosition.TWO);
            query.addSortClause(sortClause2);

            // Spit out the encoded query
            String rslt = query.construct();
            return rslt;
        }

        public static String getAccounts(String territoryid, int relationshipType) {

            // Instantiate a new constructor for the case entity and add the columns we want to see
            QueryFactory query = new QueryFactory("account");
            query.addColumn("name");
            query.addColumn("customertypecode");
            query.addColumn("accountid");
            query.addColumn("territoryid");
            query.addColumn("msus_salesrep");
            query.addColumn("msus_salesregionid");
            query.addColumn("col_agreementtype");
            query.addColumn("accountnumber");

            // Filter creation to make use of our conditions
            Filter filter = new Filter(AND);
            filter.addCondition(new Filter.FilterCondition("territoryid","eq", territoryid));
            filter.addCondition(new Filter.FilterCondition("customertypecode","eq", Integer.toString(relationshipType)));
            query.setFilter(filter);

            // Link entity creation to join to the account entity and apply our territory condition
            LinkEntity le1 = new LinkEntity("businessunit", "businessunitid", "owningbusinessunit", "a_6ad8133d2f1e4c43a3da460bacb3d6a5");
            le1.addColumn(new EntityColumn("new_managername"));
            le1.addColumn(new EntityColumn("name"));

            SortClause sortClause = new SortClause("name", false, SortClause.ClausePosition.ONE);
            query.addSortClause(sortClause);

            // Spit out the encoded query
            String rslt = query.construct();
            return rslt;
        }
    }

    public static class Opportunities {

        public enum DealStatus {
            ANY, DISCOVERY, STALLED, QUALIFYING, EVALUATING, PENDING, WON, CLOSED, CANCELED, OUT_SOLD, DEAD
        }

        public static String getOpportunitiesByTerritory(String territoryid) {

            // Query columns
            QueryFactory factory = new QueryFactory("opportunity");
            factory.addColumn("name");
            factory.addColumn("estimatedvalue");
            factory.addColumn("estimatedclosedate");
            factory.addColumn("col_dealtype");
            factory.addColumn("ownerid");
            factory.addColumn("parentaccountid");
            factory.addColumn("stepname");
            factory.addColumn("createdon");
            factory.addColumn("createdby");
            factory.addColumn("modifiedon");
            factory.addColumn("modifiedby");
            factory.addColumn("msus_probability");
            factory.addColumn("opportunityid");
            factory.addColumn("new_monthrevenuepppleasecurrency");
            factory.addColumn("statuscode");
            factory.addColumn("statecode");
            factory.addColumn("currentsituation");
            factory.addColumn("stepname");

            factory.addColumn("col_estmrevenuedevices");
            factory.addColumn("col_estmrevenueprobes");
            factory.addColumn("estimatedvalue");
            factory.addColumn("new_territoryrevenue");

            // Link entities
            LinkEntity linkEntityAccount = new LinkEntity("account", "accountid", "parentaccountid", "ab");
            linkEntityAccount.addColumn(new EntityColumn("territoryid"));
            Filter.FilterCondition linkentityAccountCondition = new Filter.FilterCondition(
                    "territoryid", Filter.Operator.EQUALS, territoryid);
            linkEntityAccount.addFilter(new Filter(AND, linkentityAccountCondition));
            factory.addLinkEntity(linkEntityAccount);

            // Filter conditions
            Filter.FilterCondition condition1 = new Filter
                    .FilterCondition("statecode", Filter.Operator.EQUALS,
                    Integer.toString(0));

            ArrayList<Filter.FilterCondition> conditions = new ArrayList<>();
            conditions.add(condition1);

            // Set filter
            Filter filter = new Filter(AND, conditions);
            factory.setFilter(filter);

            // Sort clause
            SortClause sortClause = new SortClause("createdon",
                    true, SortClause.ClausePosition.ONE);
            factory.addSortClause(sortClause);

            // Build query
            String query = factory.construct();

            return query;
        }

        /**
         * Gets opportunities based on deal status (statuscode) and optionally by territory.
         * @param territoryid Territory GUID to constrain on.
         * @param dealStatus Deal status to limit by.
         * @return
         */
        public static String getAllOpportunities(@Nullable String territoryid, DealStatus dealStatus) {

            // Query columns
            QueryFactory factory = new QueryFactory("opportunity");
            factory.addColumn("name");
            factory.addColumn("estimatedvalue");
            factory.addColumn("estimatedclosedate");
            factory.addColumn("col_dealtype");
            factory.addColumn("ownerid");
            factory.addColumn("parentaccountid");
            factory.addColumn("stepname");
            factory.addColumn("createdon");
            factory.addColumn("createdby");
            factory.addColumn("modifiedon");
            factory.addColumn("modifiedby");
            factory.addColumn("msus_probability");
            factory.addColumn("opportunityid");
            factory.addColumn("new_monthrevenuepppleasecurrency");
            factory.addColumn("statuscode");
            factory.addColumn("statecode");
            factory.addColumn("currentsituation");
            factory.addColumn("stepname");
            factory.addColumn("col_estmrevenuedevices");
            factory.addColumn("col_estmrevenueprobes");
            factory.addColumn("estimatedvalue");
            factory.addColumn("new_territoryrevenue");

            // Link entities
            LinkEntity linkEntityAccount = new LinkEntity("account", "accountid", "parentaccountid", "ab");
            linkEntityAccount.addColumn(new EntityColumn("territoryid"));
            Filter.FilterCondition territoryCondition = new Filter.FilterCondition(
                    "territoryid", Filter.Operator.EQUALS, territoryid);
            if (territoryid != null) {
                linkEntityAccount.addFilter(new Filter(AND, territoryCondition));
            }
            factory.addLinkEntity(linkEntityAccount);

            // Filter conditions
            ArrayList<Filter.FilterCondition> conditions = new ArrayList<>();

            // Active opportunities only
            Filter.FilterCondition condition1 = new Filter
                    .FilterCondition("statecode", Filter.Operator.EQUALS,
                    Integer.toString(0));
            conditions.add(condition1);

            // Condition based on the deal status and will be configured based on supplied parameter.
            Filter.FilterCondition condition2 = new Filter.FilterCondition("statuscode", Filter.Operator.EQUALS);
            switch (dealStatus) {
                case WON:
                    condition2.value = "100000007";
                    break;
                case CLOSED:
                    condition2.value = "100000010";
                    break;
                case STALLED:
                    condition2.value = "2";
                    break;
                case QUALIFYING:
                    condition2.value = "100000002";
                    break;
                case EVALUATING:
                    condition2.value = "100000003";
                    break;
                case PENDING:
                    condition2.value = "100000009";
                    break;
                case CANCELED:
                    condition2.value = "4";
                    break;
                case OUT_SOLD:
                    condition2.value = "5";
                    break;
                case DEAD:
                    condition2.value = "100000001";
                    break;
                default: // DISCOVERY
                    condition2.value = "1";
            }

            // Add the newly constructed status condition if it is anything other than "ANY".
            if (dealStatus != DealStatus.ANY) {
                conditions.add(condition2);
            }

            // Set filter
            Filter filter = new Filter(AND, conditions);
            factory.setFilter(filter);

            // Sort clause
            SortClause sortClause = new SortClause("createdon",
                    true, SortClause.ClausePosition.ONE);
            factory.addSortClause(sortClause);

            // Build query
            String query = factory.construct();
            return query;
        }

        public static String getOpportunitiesByAccount(String accountid, DealStatus dealStatus) {

            // Query columns
            QueryFactory factory = new QueryFactory("opportunity");
            factory.addColumn("name");
            factory.addColumn("estimatedvalue");
            factory.addColumn("estimatedclosedate");
            factory.addColumn("col_dealtype");
            factory.addColumn("ownerid");
            factory.addColumn("parentaccountid");
            factory.addColumn("stepname");
            factory.addColumn("createdon");
            factory.addColumn("createdby");
            factory.addColumn("modifiedon");
            factory.addColumn("modifiedby");
            factory.addColumn("msus_probability");
            factory.addColumn("opportunityid");
            factory.addColumn("new_monthrevenuepppleasecurrency");
            factory.addColumn("statuscode");
            factory.addColumn("statecode");
            factory.addColumn("currentsituation");
            factory.addColumn("stepname");

            factory.addColumn("col_estmrevenuedevices");
            factory.addColumn("col_estmrevenueprobes");
            factory.addColumn("estimatedvalue");
            factory.addColumn("new_territoryrevenue");

            // Link entities
            LinkEntity linkEntityAccount = new LinkEntity("account", "accountid", "parentaccountid", "ab");
            linkEntityAccount.addColumn(new EntityColumn("territoryid"));
            Filter.FilterCondition linkentityAccountCondition = new Filter.FilterCondition(
                    "accountid", Filter.Operator.EQUALS, accountid);
            linkEntityAccount.addFilter(new Filter(AND, linkentityAccountCondition));
            factory.addLinkEntity(linkEntityAccount);

            // Filter conditions
            Filter.FilterCondition condition1 = new Filter
                    .FilterCondition("statecode", Filter.Operator.EQUALS,
                    Integer.toString(0));

            ArrayList<Filter.FilterCondition> conditions = new ArrayList<>();
            conditions.add(condition1);

            // Condition based on the deal status and will be configured based on supplied parameter.
            Filter.FilterCondition condition2 = new Filter.FilterCondition("statuscode", Filter.Operator.EQUALS);
            switch (dealStatus) {
                case WON:
                    condition2.value = "100000007";
                    break;
                case CLOSED:
                    condition2.value = "100000010";
                    break;
                case STALLED:
                    condition2.value = "2";
                    break;
                case QUALIFYING:
                    condition2.value = "100000002";
                    break;
                case EVALUATING:
                    condition2.value = "100000003";
                    break;
                case PENDING:
                    condition2.value = "100000009";
                    break;
                case CANCELED:
                    condition2.value = "4";
                    break;
                case OUT_SOLD:
                    condition2.value = "5";
                    break;
                case DEAD:
                    condition2.value = "100000001";
                    break;
                default: // DISCOVERY
                    condition2.value = "1";
            }

            // Add the newly constructed status condition if it is anything other than "ANY".
            if (dealStatus != DealStatus.ANY) {
                conditions.add(condition2);
            }

            // Set filter
            Filter filter = new Filter(AND, conditions);
            factory.setFilter(filter);

            // Sort clause
            SortClause sortClause = new SortClause("createdon",
                    true, SortClause.ClausePosition.ONE);
            factory.addSortClause(sortClause);

            // Build query
            String query = factory.construct();

            return query;
        }

        public static String getOpportunityDetails(String opportunityid) {

            // Query columns
            QueryFactory factory = new QueryFactory("opportunity");
            factory.addColumn("name");
            factory.addColumn("estimatedvalue");
            factory.addColumn("estimatedclosedate");
            factory.addColumn("col_dealtype");
            factory.addColumn("ownerid");
            factory.addColumn("parentaccountid");
            factory.addColumn("stepname");
            factory.addColumn("createdon");
            factory.addColumn("createdby");
            factory.addColumn("modifiedon");
            factory.addColumn("modifiedby");
            factory.addColumn("msus_probability");
            factory.addColumn("opportunityid");
            factory.addColumn("new_monthrevenuepppleasecurrency");
            factory.addColumn("statuscode");
            factory.addColumn("statecode");
            factory.addColumn("currentsituation");
            factory.addColumn("stepname");
            factory.addColumn("col_estmrevenuedevices");
            factory.addColumn("col_estmrevenueprobes");
            factory.addColumn("estimatedvalue");
            factory.addColumn("new_territoryrevenue");

            // Link entities
            LinkEntity linkEntityAccount = new LinkEntity("account", "accountid", "parentaccountid", "ab");
            linkEntityAccount.addColumn(new EntityColumn("territoryid"));
            factory.addLinkEntity(linkEntityAccount);

            // Filter conditions
            Filter.FilterCondition condition1 = new Filter
                    .FilterCondition("statecode", Filter.Operator.EQUALS,
                    Integer.toString(0));

            Filter.FilterCondition condition2 = new Filter
                    .FilterCondition("opportunityid", Filter.Operator.EQUALS,
                    opportunityid);

            ArrayList<Filter.FilterCondition> conditions = new ArrayList<>();
            conditions.add(condition1);
            conditions.add(condition2);

            // Set filter
            Filter filter = new Filter(AND, conditions);
            factory.setFilter(filter);

            // Sort clause
            SortClause sortClause = new SortClause("createdon",
                    true, SortClause.ClausePosition.ONE);
            factory.addSortClause(sortClause);

            // Build query
            String query = factory.construct();

            return query;
        }

    }

    public static class CustomerInventory {

        public enum ProductStatus {
            IN_STOCK, RETURNED, EXPIRED, LOST, ANY
        }

        public enum ProductType {
            PROBES, CABLES, FLOWMETERS, CARDS
        }

        public static String getCustomerInventoryDetails(String customerinventoryid) {
            QueryFactory query = new QueryFactory("col_customerinventory");
            query.addColumn("col_name");
            query.addColumn("statuscode");
            query.addColumn("col_serialnumber");
            query.addColumn("col_ownershipcapital");
            query.addColumn("modifiedon");
            query.addColumn("new_physical_date"); // Added in 1.83 to replace modifiedon
            query.addColumn("col_revision");
            query.addColumn("overriddencreatedon");
            query.addColumn("col_quantity");
            query.addColumn("col_productid");
            query.addColumn("col_item");
            query.addColumn("col_itemgroup");
            query.addColumn("col_batchnumber");
            query.addColumn("col_accountid");
            query.addColumn("col_referencenumber");
            query.addColumn("col_customerinventoryid");
            query.addColumn("msus_statusadditionaldetails");
            query.addColumn("msus_serialnotemodifieddate");
            query.addColumn("msus_serial_note");
            query.addColumn("msus_revision");
            query.addColumn("mw_notes");
            query.addColumn("msus_licensing_config");
            query.addColumn("msus_c177performed");

            SortClause clause1 = new SortClause("col_itemgroup", false, SortClause.ClausePosition.ONE);
            query.addSortClause(clause1);

            Filter filter = new Filter(AND);
            Filter.FilterCondition condition = new Filter.FilterCondition("col_customerinventoryid",
                    Filter.Operator.EQUALS, customerinventoryid);
            filter.addCondition(condition);
            query.setFilter(filter);

            return query.construct();

        }
    }

    public static class Contacts {

        public static String getContacts() {
            // Instantiate a new constructor for the case entity and add the columns we want to see
            QueryFactory query = new QueryFactory("contact");
            query.addColumn("fullname");
            query.addColumn("firstname");
            query.addColumn("lastname");
            query.addColumn("parentcustomerid");
            query.addColumn("telephone1");
            query.addColumn("mobilephone");
            query.addColumn("address1_telephone1");
            query.addColumn("emailaddress1");
            query.addColumn("msus_associated_npi_number");
            query.addColumn("jobtitle");
            query.addColumn("msus_department");
            query.addColumn("contactid");
            query.addColumn("mobilephone");
            query.addColumn("createdon");
            query.addColumn("createdby");
            query.addColumn("modifiedon");
            query.addColumn("modifiedby");
            query.addColumn("description");
            query.addColumn("preferredcontactmethodcode");
            // Surgeon properties
            query.addColumn("msus_npi");
            query.addColumn("msus_credentials");
            query.addColumn("msus_primaryspecialty");
            query.addColumn("msus_secondary_specialties");
            query.addColumn("msus_medical_school_name");
            query.addColumn("msus_medicare_claims");
            query.addColumn("msus_graduation_year");
            query.addColumn("msus_annual_cabg_cases");
            query.addColumn("msus_ttfm_vendor");
            query.addColumn("msus_medistim_customer");
            query.addColumn("msus_ttfm_procedures");
            query.addColumn("msus_percentagecaseswttfm");
            query.addColumn("msus_percentageonpump");
            query.addColumn("msus_percentagescanaorta");
            query.addColumn("msus_percentageusingmedistimimaging");
            query.addColumn("msus_vessels_imaged");

            // Create a filter
            Filter filter = new Filter(AND);

            // Set filter
            /*Filter.FilterCondition condition1 = new Filter.FilterCondition("parentcustomerid", Filter.Operator.EQUALS, accountid );
            filter.addCondition(condition1);
            query.setFilter(filter);*/

            // Spit out the encoded query
            String rslt = query.construct();
            return rslt;
        }

        public static String getContacts(String accountid) {
            // Instantiate a new constructor for the case entity and add the columns we want to see
            QueryFactory query = new QueryFactory("contact");
            query.addColumn("fullname");
            query.addColumn("firstname");
            query.addColumn("lastname");
            query.addColumn("parentcustomerid");
            query.addColumn("telephone1");
            query.addColumn("mobilephone");
            query.addColumn("address1_telephone1");
            query.addColumn("emailaddress1");
            query.addColumn("msus_associated_npi_number");
            query.addColumn("jobtitle");
            query.addColumn("msus_department");
            query.addColumn("contactid");
            query.addColumn("mobilephone");
            query.addColumn("createdon");
            query.addColumn("createdby");
            query.addColumn("modifiedon");
            query.addColumn("modifiedby");
            query.addColumn("description");
            query.addColumn("preferredcontactmethodcode");
            // Surgeon properties
            query.addColumn("msus_npi");
            query.addColumn("msus_credentials");
            query.addColumn("msus_primaryspecialty");
            query.addColumn("msus_secondary_specialties");
            query.addColumn("msus_medical_school_name");
            query.addColumn("msus_medicare_claims");
            query.addColumn("msus_graduation_year");
            query.addColumn("msus_annual_cabg_cases");
            query.addColumn("msus_ttfm_vendor");
            query.addColumn("msus_medistim_customer");
            query.addColumn("msus_ttfm_procedures");
            query.addColumn("msus_percentagecaseswttfm");
            query.addColumn("msus_percentageonpump");
            query.addColumn("msus_percentagescanaorta");
            query.addColumn("msus_percentageusingmedistimimaging");
            query.addColumn("msus_vessels_imaged");

            // Create a filter
            Filter filter = new Filter(AND);

            // Set filter
            Filter.FilterCondition condition1 = new Filter.FilterCondition("parentcustomerid", Filter.Operator.EQUALS, accountid );
            filter.addCondition(condition1);
            query.setFilter(filter);

            // Spit out the encoded query
            String rslt = query.construct();
            return rslt;
        }

        public static String getContact(String contactid) {
            // Instantiate a new constructor for the case entity and add the columns we want to see
            QueryFactory query = new QueryFactory("contact");
            query.addColumn("fullname");
            query.addColumn("firstname");
            query.addColumn("lastname");
            query.addColumn("parentcustomerid");
            query.addColumn("telephone1");
            query.addColumn("mobilephone");
            query.addColumn("address1_telephone1");
            query.addColumn("emailaddress1");
            query.addColumn("msus_associated_npi_number");
            query.addColumn("jobtitle");
            query.addColumn("msus_department");
            query.addColumn("contactid");
            query.addColumn("mobilephone");
            query.addColumn("createdon");
            query.addColumn("createdby");
            query.addColumn("modifiedon");
            query.addColumn("modifiedby");
            query.addColumn("description");
            query.addColumn("preferredcontactmethodcode");
            // Surgeon properties
            query.addColumn("msus_npi");
            query.addColumn("msus_credentials");
            query.addColumn("msus_primaryspecialty");
            query.addColumn("msus_secondary_specialties");
            query.addColumn("msus_medical_school_name");
            query.addColumn("msus_medicare_claims");
            query.addColumn("msus_graduation_year");
            query.addColumn("msus_annual_cabg_cases");
            query.addColumn("msus_ttfm_vendor");
            query.addColumn("msus_medistim_customer");
            query.addColumn("msus_ttfm_procedures");
            query.addColumn("msus_percentagecaseswttfm");
            query.addColumn("msus_percentageonpump");
            query.addColumn("msus_percentagescanaorta");
            query.addColumn("msus_percentageusingmedistimimaging");
            query.addColumn("msus_vessels_imaged");

            // Create a filter
            Filter filter = new Filter(AND);

            // Set filter
            Filter.FilterCondition condition1 = new Filter.FilterCondition("contactid", Filter.Operator.EQUALS, contactid );
            filter.addCondition(condition1);
            query.setFilter(filter);

            // Spit out the encoded query
            String rslt = query.construct();
            return rslt;
        }

    }

    /**
     * Contains queries designed to test basic functionality and CRM communication
     */
    public static class Utility {

        /**
         * Used to test connectivity by requesting the current user by the current user's saved systemuserid.
         * @return Some basic columns about the user (fullname, title etc.)
         */
        public static String getMyUser() {
            // Instantiate a new constructor for the case entity and add the columns we want to see
            QueryFactory query = new QueryFactory(EntityNames.USER);
            query.addColumn("fullname");

            // Filter creation to make use of our conditions
            Filter filter = new Filter(AND);
            filter.addCondition(new Filter.FilterCondition("systemuserid",
                    Filter.Operator.EQUALS, MediUser.getMe().systemuserid));
            query.setFilter(filter);

            // Spit out the encoded query
            String rslt = query.construct();
            return rslt;
        }
    }

    public static class Annotations {
        public static String getAnnotations(String associatedentityid) {
            // Query columns
            QueryFactory factory = new QueryFactory("annotation");
            factory.addColumn("annotationid");
            factory.addColumn("createdby");
            factory.addColumn("createdon");
            factory.addColumn("isdocument");
            factory.addColumn("mimetype");
            factory.addColumn("modifiedby");
            factory.addColumn("modifiedon");
            factory.addColumn("notetext");
            factory.addColumn("objectid");
            factory.addColumn("objecttypecode");
            factory.addColumn("subject");
            factory.addColumn("filename");
            factory.addColumn("filesize");

            // Filter conditions
            Filter.FilterCondition condition1 = new Filter
                    .FilterCondition("objectid", Filter.Operator.EQUALS, associatedentityid);

            ArrayList<Filter.FilterCondition> conditions = new ArrayList<>();
            conditions.add(condition1);

            // Set filter
            Filter filter = new Filter(AND, conditions);
            factory.setFilter(filter);

            // Sort clause
            SortClause sortClause = new SortClause("createdon",
                    true, SortClause.ClausePosition.ONE);
            factory.addSortClause(sortClause);

            // Build query
            String query = factory.construct();

            return query;
        }

        public static String getAnnotation(String annotationid, boolean includeAttachment) {
            // Query columns
            QueryFactory factory = new QueryFactory("annotation");
            factory.addColumn("annotationid");
            factory.addColumn("createdby");
            factory.addColumn("createdon");
            factory.addColumn("isdocument");
            factory.addColumn("mimetype");
            factory.addColumn("modifiedby");
            factory.addColumn("modifiedon");
            factory.addColumn("notetext");
            factory.addColumn("objectid");
            factory.addColumn("objecttypecode");
            factory.addColumn("subject");
            factory.addColumn("filename");
            factory.addColumn("filesize");
            if (includeAttachment) {
                factory.addColumn("documentbody");
            }

            // Filter conditions
            Filter.FilterCondition condition1 = new Filter
                    .FilterCondition("annotationid", Filter.Operator.EQUALS, annotationid);

            ArrayList<Filter.FilterCondition> conditions = new ArrayList<>();
            conditions.add(condition1);

            // Set filter
            Filter filter = new Filter(AND, conditions);
            factory.setFilter(filter);

            // Sort clause
            SortClause sortClause = new SortClause("createdon",
                    true, SortClause.ClausePosition.ONE);
            factory.addSortClause(sortClause);

            // Build query
            String query = factory.construct();

            return query;
        }
    }

    public static class Activities {
        public static String getCaseActivities(String caseid) {

            // Instantiate QueryFactory and set base entity columns
            QueryFactory query = new QueryFactory("activitypointer");
            query.addColumn("subject");
            query.addColumn("ownerid");
            query.addColumn("activitytypecode");
            query.addColumn("statecode");
            query.addColumn("scheduledend");
            query.addColumn("instancetypecode");
            query.addColumn("community");
            query.addColumn("description");
            query.addColumn("createdon");
            query.addColumn("activityid");
            query.addColumn("regardingobjectid");

            // Link entity for owner
            LinkEntity le1 = new LinkEntity("systemuser", "systemuserid", "owninguser", "activitypointerowningusersystemusersystemuserid");
            le1.addColumn(new EntityColumn("internalemailaddress"));
            query.addLinkEntity(le1);

            // Link entity for email
            LinkEntity le2 = new LinkEntity("email", "activityid", "activityid", "email_engagement");
            le2.addColumn(new EntityColumn("isemailfollowed"));
            le2.addColumn(new EntityColumn("lastopenedtime"));
            le2.addColumn(new EntityColumn("delayedemailsendtime"));
            query.addLinkEntity(le2);

            // Create a filter
            Filter filter = new Filter(AND);
            Filter.FilterCondition condition1 = new Filter.FilterCondition("isregularactivity", Filter.Operator.EQUALS, "1");
            Filter.FilterCondition condition2 = new Filter.FilterCondition("regardingobjectid", Filter.Operator.EQUALS, caseid);
            filter.addCondition(condition1);
            filter.addCondition(condition2);
            query.setFilter(filter);

            // Sort by
            SortClause sortClause = new SortClause("createdon",true, SortClause.ClausePosition.ONE);
            query.addSortClause(sortClause);

            // Spit out the encoded query
            return query.construct();
        }
    }

    public static class Emails {
        public static String getEmailsRegarding(String regardingid) {
            // Instantiate QueryFactory and set base entity columns
            QueryFactory query = new QueryFactory("email");
            query.addColumn("subject");
            query.addColumn("regardingobjectid");
            query.addColumn("from");
            query.addColumn("sender");
            query.addColumn("torecipients");
            query.addColumn("to");
            query.addColumn("cc");
            query.addColumn("prioritycode");
            query.addColumn("statuscode");
            query.addColumn("activityid");
            query.addColumn("emailsender");
            query.addColumn("description");
            query.addColumn("createdon");
            query.addColumn("createdby");
            query.addColumn("activitytypecode");
            query.addColumn("subject");

            // Create a filter
            Filter filter = new Filter(AND);
            Filter.FilterCondition condition1 = new Filter.FilterCondition("regardingobjectid", Filter.Operator.EQUALS, regardingid);
            filter.addCondition(condition1);
            query.setFilter(filter);

            // Sort by
            SortClause sortClause = new SortClause("createdon",true, SortClause.ClausePosition.ONE);
            query.addSortClause(sortClause);

            // Spit out the encoded query
            return query.construct();
        }
    }

    public static class OrderLines {

        public static String getOrderLines(@Nullable String territoryid, Operators.DateOperator operator) {

            /************** TESTING *************/
            // repid = "DAA46FDF-5B7C-E711-80D1-005056A32EEA";
            /************************************/


            // Main entity columns
            QueryFactory factory = new QueryFactory("salesorderdetail");
            factory.addColumn("productid");
            factory.addColumn("msus_price_per_unit");
            factory.addColumn("new_customer");
            factory.addColumn("quantity");
            factory.addColumn("extendedamount");
            factory.addColumn("salesrepid");
            factory.addColumn("salesorderid");
            factory.addColumn("salesorderdetailid");

            // Create link entities
            LinkEntity linkEntitySalesOrder = new LinkEntity(
                    "salesorder",
                    "salesorderid",
                    "salesorderid",
                    "a_6ec0e72e4c104394bc627456c6412838"
            );
            LinkEntity linkEntitySystemUser = new LinkEntity(
                    "systemuser",
                    "systemuserid",
                    "salesrepid",
                    "a_a1cf96c07c114d478335b8c445651a12"
            );
            LinkEntity linkEntityAccount = new LinkEntity(
                    "account",
                    "accountid",
                    "new_customer",
                    "a_db24f99da8fee71180df005056a36b9b"
            );
            LinkEntity linkEntityProduct = new LinkEntity(
                    "product",
                    "productid",
                    "productid",
                    "a_070ef9d142cd40d98bebd513e03c7cd1"
            );

            // Add columns to link entities
            linkEntitySalesOrder.addColumn(new EntityColumn("submitdate"));
            linkEntitySystemUser.addColumn(new EntityColumn("employeeid"));
            linkEntityAccount.addColumn(new EntityColumn("accountnumber"));
            linkEntityAccount.addColumn(new EntityColumn("territoryid"));
            linkEntityProduct.addColumn(new EntityColumn("msus_is_capital"));
            linkEntityProduct.addColumn(new EntityColumn("productnumber"));
            linkEntityProduct.addColumn(new EntityColumn("col_itemgroup"));
            linkEntityProduct.addColumn(new EntityColumn("col_producfamily"));

            // Create and populate a condition array for a link entity
            ArrayList<Filter.FilterCondition> salesOrderConditions = new ArrayList<>();
            Filter.FilterCondition conditionOrderDate = new Filter.FilterCondition(
                    "submitdate", getDateOperator(operator));
            salesOrderConditions.add(conditionOrderDate);

            // Limit by territory if territory was supplied.
            if (territoryid != null) {
                ArrayList<Filter.FilterCondition> territoryConditions = new ArrayList<>();
                Filter.FilterCondition territoryCondition = new Filter.FilterCondition(
                        "territoryid", Filter.Operator.EQUALS, territoryid);
                territoryConditions.add(territoryCondition);
                linkEntityAccount.addFilter(new Filter(AND, territoryConditions));
            }

            // Add new filters to the link entities that have filters
            linkEntitySalesOrder.addFilter(new Filter(AND, salesOrderConditions));

            // Create and add a sort clause
            SortClause sortClause = new SortClause("salesorderid",
                    true, SortClause.ClausePosition.ONE);
            factory.addSortClause(sortClause);

            // Add the constructed link entities
            factory.addLinkEntity(linkEntityAccount);
            factory.addLinkEntity(linkEntityProduct);
            factory.addLinkEntity(linkEntitySalesOrder);
            factory.addLinkEntity(linkEntitySystemUser);

            // Build teh query
            String query = factory.construct();

            return query;
        }

        public static String getOrderLinesForSalesOrder(String salesorderid) {

            /************** TESTING *************/
            // repid = "DAA46FDF-5B7C-E711-80D1-005056A32EEA";
            /************************************/


            // Main entity columns
            QueryFactory factory = new QueryFactory("salesorderdetail");
            factory.addColumn("productid");
            factory.addColumn("msus_price_per_unit");
            factory.addColumn("new_customer");
            factory.addColumn("quantity");
            factory.addColumn("extendedamount");
            factory.addColumn("salesrepid");
            factory.addColumn("salesorderid");
            factory.addColumn("salesorderdetailid");

            // Create link entities
            LinkEntity linkEntitySalesOrder = new LinkEntity(
                    "salesorder",
                    "salesorderid",
                    "salesorderid",
                    "a_6ec0e72e4c104394bc627456c6412838"
            );
            LinkEntity linkEntitySystemUser = new LinkEntity(
                    "systemuser",
                    "systemuserid",
                    "salesrepid",
                    "a_a1cf96c07c114d478335b8c445651a12"
            );
            LinkEntity linkEntityAccount = new LinkEntity(
                    "account",
                    "accountid",
                    "new_customer",
                    "a_db24f99da8fee71180df005056a36b9b"
            );
            LinkEntity linkEntityProduct = new LinkEntity(
                    "product",
                    "productid",
                    "productid",
                    "a_070ef9d142cd40d98bebd513e03c7cd1"
            );

            // Add columns to link entities
            linkEntitySalesOrder.addColumn(new EntityColumn("submitdate"));
            linkEntitySystemUser.addColumn(new EntityColumn("employeeid"));
            linkEntityAccount.addColumn(new EntityColumn("accountnumber"));
            linkEntityAccount.addColumn(new EntityColumn("territoryid"));
            linkEntityProduct.addColumn(new EntityColumn("msus_is_capital"));
            linkEntityProduct.addColumn(new EntityColumn("productnumber"));
            linkEntityProduct.addColumn(new EntityColumn("col_itemgroup"));
            linkEntityProduct.addColumn(new EntityColumn("col_producfamily"));

            Filter.FilterCondition condition = new Filter.FilterCondition(
                    "salesorderid", Filter.Operator.EQUALS, salesorderid);
            factory.setFilter(new Filter(AND, condition));

            // Create and add a sort clause
            SortClause sortClause = new SortClause("salesorderid",
                    true, SortClause.ClausePosition.ONE);
            factory.addSortClause(sortClause);

            // Add the constructed link entities
            factory.addLinkEntity(linkEntityAccount);
            factory.addLinkEntity(linkEntityProduct);
            factory.addLinkEntity(linkEntitySalesOrder);
            factory.addLinkEntity(linkEntitySystemUser);

            // Build teh query
            String query = factory.construct();

            return query;
        }

        public static String getOrderLines(@Nullable String territoryid, Operators.DateOperator operator, int num) {

            /************** TESTING *************/
            // repid = "DAA46FDF-5B7C-E711-80D1-005056A32EEA";
            /************************************/


            // Main entity columns
            QueryFactory factory = new QueryFactory("salesorderdetail");
            factory.addColumn("productid");
            factory.addColumn("msus_price_per_unit");
            factory.addColumn("new_customer");
            factory.addColumn("quantity");
            factory.addColumn("extendedamount");
            factory.addColumn("salesrepid");
            factory.addColumn("salesorderid");
            factory.addColumn("salesorderdetailid");

            // Create link entities
            LinkEntity linkEntitySalesOrder = new LinkEntity(
                    "salesorder",
                    "salesorderid",
                    "salesorderid",
                    "a_6ec0e72e4c104394bc627456c6412838"
            );
            LinkEntity linkEntitySystemUser = new LinkEntity(
                    "systemuser",
                    "systemuserid",
                    "salesrepid",
                    "a_a1cf96c07c114d478335b8c445651a12"
            );
            LinkEntity linkEntityAccount = new LinkEntity(
                    "account",
                    "accountid",
                    "new_customer",
                    "a_db24f99da8fee71180df005056a36b9b"
            );
            LinkEntity linkEntityProduct = new LinkEntity(
                    "product",
                    "productid",
                    "productid",
                    "a_070ef9d142cd40d98bebd513e03c7cd1"
            );

            // Add columns to link entities
            linkEntitySalesOrder.addColumn(new EntityColumn("submitdate"));
            linkEntitySystemUser.addColumn(new EntityColumn("employeeid"));
            linkEntityAccount.addColumn(new EntityColumn("accountnumber"));
            linkEntityAccount.addColumn(new EntityColumn("territoryid"));
            linkEntityProduct.addColumn(new EntityColumn("msus_is_capital"));
            linkEntityProduct.addColumn(new EntityColumn("productnumber"));
            linkEntityProduct.addColumn(new EntityColumn("col_itemgroup"));
            linkEntityProduct.addColumn(new EntityColumn("col_producfamily"));

            // Create and populate a condition array for a link entity
            ArrayList<Filter.FilterCondition> salesOrderConditions = new ArrayList<>();
            Filter.FilterCondition conditionOrderDate = new Filter.FilterCondition(
                    "submitdate", getDateOperator(operator), Integer.toString(num));
            salesOrderConditions.add(conditionOrderDate);

            // Limit by territory if territory is supplied
            if (territoryid != null) {
                ArrayList<Filter.FilterCondition> territoryConditions = new ArrayList<>();
                Filter.FilterCondition conditionRepId = new Filter.FilterCondition(
                        "territoryid", Filter.Operator.EQUALS, territoryid);
                territoryConditions.add(conditionRepId);
                linkEntityAccount.addFilter(new Filter(AND, territoryConditions));
            }

            // Add new filters to the link entities that have filters
            linkEntitySalesOrder.addFilter(new Filter(AND, salesOrderConditions));

            // Create and add a sort clause
            SortClause sortClause = new SortClause("salesorderid",
                    true, SortClause.ClausePosition.ONE);
            factory.addSortClause(sortClause);

            // Add the constructed link entities
            factory.addLinkEntity(linkEntityAccount);
            factory.addLinkEntity(linkEntityProduct);
            factory.addLinkEntity(linkEntitySalesOrder);
            factory.addLinkEntity(linkEntitySystemUser);

            // Build teh query
            String query = factory.construct();

            return query;
        }

        public static String getOrderLines(String territoryid, int monthNum, int yearNum) {

            /************** TESTING *************/
            // repid = "DAA46FDF-5B7C-E711-80D1-005056A32EEA";
            /************************************/


            // Main entity columns
            QueryFactory factory = new QueryFactory("salesorderdetail");
            factory.addColumn("productid");
            factory.addColumn("msus_price_per_unit");
            factory.addColumn("new_customer");
            factory.addColumn("quantity");
            factory.addColumn("extendedamount");
            factory.addColumn("salesrepid");
            factory.addColumn("salesorderid");
            factory.addColumn("salesorderdetailid");

            // Create link entities
            LinkEntity linkEntitySalesOrder = new LinkEntity(
                    "salesorder",
                    "salesorderid",
                    "salesorderid",
                    "a_6ec0e72e4c104394bc627456c6412838"
            );
            LinkEntity linkEntitySystemUser = new LinkEntity(
                    "systemuser",
                    "systemuserid",
                    "salesrepid",
                    "a_a1cf96c07c114d478335b8c445651a12"
            );
            LinkEntity linkEntityAccount = new LinkEntity(
                    "account",
                    "accountid",
                    "new_customer",
                    "a_db24f99da8fee71180df005056a36b9b"
            );
            LinkEntity linkEntityProduct = new LinkEntity(
                    "product",
                    "productid",
                    "productid",
                    "a_070ef9d142cd40d98bebd513e03c7cd1"
            );

            // Add columns to link entities
            linkEntitySalesOrder.addColumn(new EntityColumn("submitdate"));
            linkEntitySystemUser.addColumn(new EntityColumn("employeeid"));
            linkEntityAccount.addColumn(new EntityColumn("accountnumber"));
            linkEntityAccount.addColumn(new EntityColumn("territoryid"));
            linkEntityProduct.addColumn(new EntityColumn("msus_is_capital"));
            linkEntityProduct.addColumn(new EntityColumn("productnumber"));
            linkEntityProduct.addColumn(new EntityColumn("col_itemgroup"));
            linkEntityProduct.addColumn(new EntityColumn("col_producfamily"));

            // Create and populate a condition array for a link entity
            ArrayList<Filter.FilterCondition> dateConditions = new ArrayList<>();
            Filter.FilterCondition conditionFiscalMonth = new Filter.FilterCondition(
                    "submitdate", Filter.Operator.IN_FISCAL_PERIOD, Integer.toString(monthNum));
            Filter.FilterCondition conditionFiscalYear = new Filter.FilterCondition(
                    "submitdate", Filter.Operator.IN_FISCAL_YEAR, Integer.toString(yearNum));
            dateConditions.add(conditionFiscalMonth);
            dateConditions.add(conditionFiscalYear);

            // Add new filters to the link entities that have filters
            linkEntitySalesOrder.addFilter(new Filter(AND, dateConditions));

            // Create and add a sort clause
            SortClause sortClause = new SortClause("createdon",
                    true, SortClause.ClausePosition.ONE);
            factory.addSortClause(sortClause);

            // Add the constructed link entities
            factory.addLinkEntity(linkEntityAccount);
            factory.addLinkEntity(linkEntityProduct);
            factory.addLinkEntity(linkEntitySalesOrder);
            factory.addLinkEntity(linkEntitySystemUser);

            // Build teh query
            String query = factory.construct();

            return query;
        }

        public static String getOrderLinesByAccount(String customerid, Operators.DateOperator operator) {

            /************** TESTING *************/
            // repid = "DAA46FDF-5B7C-E711-80D1-005056A32EEA";
            /************************************/


            // Main entity columns
            QueryFactory factory = new QueryFactory("salesorderdetail");
            factory.addColumn("productid");
            factory.addColumn("msus_price_per_unit");
            factory.addColumn("new_customer");
            factory.addColumn("quantity");
            factory.addColumn("extendedamount");
            factory.addColumn("salesrepid");
            factory.addColumn("salesorderid");
            factory.addColumn("salesorderdetailid");

            // Create link entities
            LinkEntity linkEntitySalesOrder = new LinkEntity(
                    "salesorder",
                    "salesorderid",
                    "salesorderid",
                    "a_6ec0e72e4c104394bc627456c6412838"
            );
            LinkEntity linkEntitySystemUser = new LinkEntity(
                    "systemuser",
                    "systemuserid",
                    "salesrepid",
                    "a_a1cf96c07c114d478335b8c445651a12"
            );
            LinkEntity linkEntityAccount = new LinkEntity(
                    "account",
                    "accountid",
                    "new_customer",
                    "a_db24f99da8fee71180df005056a36b9b"
            );
            LinkEntity linkEntityProduct = new LinkEntity(
                    "product",
                    "productid",
                    "productid",
                    "a_070ef9d142cd40d98bebd513e03c7cd1"
            );

            // Add columns to link entities
            linkEntitySalesOrder.addColumn(new EntityColumn("submitdate"));
            linkEntitySystemUser.addColumn(new EntityColumn("employeeid"));
            linkEntityAccount.addColumn(new EntityColumn("accountnumber"));
            linkEntityAccount.addColumn(new EntityColumn("territoryid"));
            linkEntityProduct.addColumn(new EntityColumn("msus_is_capital"));
            linkEntityProduct.addColumn(new EntityColumn("productnumber"));
            linkEntityProduct.addColumn(new EntityColumn("col_itemgroup"));
            linkEntityProduct.addColumn(new EntityColumn("col_producfamily"));

            // Create and populate a condition array for a link entity
            ArrayList<Filter.FilterCondition> salesOrderConditions = new ArrayList<>();
            Filter.FilterCondition conditionOrderDate = new Filter.FilterCondition(
                    "submitdate", getDateOperator(operator));
            salesOrderConditions.add(conditionOrderDate);

            // Create and populate a condition array for a link entity
            ArrayList<Filter.FilterCondition> customerConditions = new ArrayList<>();
            Filter.FilterCondition customerCondition1 = new Filter.FilterCondition(
                    "accountid", Filter.Operator.EQUALS, customerid);
            customerConditions.add(customerCondition1);

            // Add new filters to the link entities that have filters
            linkEntitySalesOrder.addFilter(new Filter(AND, salesOrderConditions));
            linkEntityAccount.addFilter(new Filter(AND, customerConditions));

            // Create and add a sort clause
            SortClause sortClause = new SortClause("createdon",
                    true, SortClause.ClausePosition.ONE);
            factory.addSortClause(sortClause);

            // Add the constructed link entities
            factory.addLinkEntity(linkEntityAccount);
            factory.addLinkEntity(linkEntityProduct);
            factory.addLinkEntity(linkEntitySalesOrder);
            factory.addLinkEntity(linkEntitySystemUser);

            // Build teh query
            String query = factory.construct();

            return query;
        }

        public static String getOrderLinesByAccount(String customerid, Operators.DateOperator operator, int num) {

            /************** TESTING *************/
            // repid = "DAA46FDF-5B7C-E711-80D1-005056A32EEA";
            /************************************/


            // Main entity columns
            QueryFactory factory = new QueryFactory("salesorderdetail");
            factory.addColumn("productid");
            factory.addColumn("msus_price_per_unit");
            factory.addColumn("new_customer");
            factory.addColumn("quantity");
            factory.addColumn("extendedamount");
            factory.addColumn("salesrepid");
            factory.addColumn("salesorderid");
            factory.addColumn("salesorderdetailid");

            // Create link entities
            LinkEntity linkEntitySalesOrder = new LinkEntity(
                    "salesorder",
                    "salesorderid",
                    "salesorderid",
                    "a_6ec0e72e4c104394bc627456c6412838"
            );
            LinkEntity linkEntitySystemUser = new LinkEntity(
                    "systemuser",
                    "systemuserid",
                    "salesrepid",
                    "a_a1cf96c07c114d478335b8c445651a12"
            );
            LinkEntity linkEntityAccount = new LinkEntity(
                    "account",
                    "accountid",
                    "new_customer",
                    "a_db24f99da8fee71180df005056a36b9b"
            );
            LinkEntity linkEntityProduct = new LinkEntity(
                    "product",
                    "productid",
                    "productid",
                    "a_070ef9d142cd40d98bebd513e03c7cd1"
            );

            // Add columns to link entities
            linkEntitySalesOrder.addColumn(new EntityColumn("submitdate"));
            linkEntitySystemUser.addColumn(new EntityColumn("employeeid"));
            linkEntityAccount.addColumn(new EntityColumn("accountnumber"));
            linkEntityAccount.addColumn(new EntityColumn("territoryid"));
            linkEntityProduct.addColumn(new EntityColumn("msus_is_capital"));
            linkEntityProduct.addColumn(new EntityColumn("productnumber"));
            linkEntityProduct.addColumn(new EntityColumn("col_itemgroup"));
            linkEntityProduct.addColumn(new EntityColumn("col_producfamily"));

            // Create and populate a condition array for a link entity
            ArrayList<Filter.FilterCondition> salesOrderConditions = new ArrayList<>();
            Filter.FilterCondition conditionOrderDate = new Filter.FilterCondition(
                    "submitdate", getDateOperator(operator), Integer.toString(num));
            salesOrderConditions.add(conditionOrderDate);

            // Create and populate a condition array for a link entity
            ArrayList<Filter.FilterCondition> customerConditions = new ArrayList<>();
            Filter.FilterCondition customerCondition1 = new Filter.FilterCondition(
                    "accountid", Filter.Operator.EQUALS, customerid);
            customerConditions.add(customerCondition1);

            // Add new filters to the link entities that have filters
            linkEntitySalesOrder.addFilter(new Filter(AND, salesOrderConditions));
            linkEntityAccount.addFilter(new Filter(AND, customerConditions));

            // Create and add a sort clause
            SortClause sortClause = new SortClause("createdon",
                    true, SortClause.ClausePosition.ONE);
            factory.addSortClause(sortClause);

            // Add the constructed link entities
            factory.addLinkEntity(linkEntityAccount);
            factory.addLinkEntity(linkEntityProduct);
            factory.addLinkEntity(linkEntitySalesOrder);
            factory.addLinkEntity(linkEntitySystemUser);

            // Build teh query
            String query = factory.construct();

            return query;
        }

        public static String getOrderLinesByAccount(String customerid, int monthNum, int yearNum) {

            /************** TESTING *************/
            // repid = "DAA46FDF-5B7C-E711-80D1-005056A32EEA";
            /************************************/


            // Main entity columns
            QueryFactory factory = new QueryFactory("salesorderdetail");
            factory.addColumn("productid");
            factory.addColumn("msus_price_per_unit");
            factory.addColumn("new_customer");
            factory.addColumn("quantity");
            factory.addColumn("extendedamount");
            factory.addColumn("salesrepid");
            factory.addColumn("salesorderid");
            factory.addColumn("salesorderdetailid");

            // Create link entities
            LinkEntity linkEntitySalesOrder = new LinkEntity(
                    "salesorder",
                    "salesorderid",
                    "salesorderid",
                    "a_6ec0e72e4c104394bc627456c6412838"
            );
            LinkEntity linkEntitySystemUser = new LinkEntity(
                    "systemuser",
                    "systemuserid",
                    "salesrepid",
                    "a_a1cf96c07c114d478335b8c445651a12"
            );
            LinkEntity linkEntityAccount = new LinkEntity(
                    "account",
                    "accountid",
                    "new_customer",
                    "a_db24f99da8fee71180df005056a36b9b"
            );
            LinkEntity linkEntityProduct = new LinkEntity(
                    "product",
                    "productid",
                    "productid",
                    "a_070ef9d142cd40d98bebd513e03c7cd1"
            );

            // Add columns to link entities
            linkEntitySalesOrder.addColumn(new EntityColumn("submitdate"));
            linkEntitySystemUser.addColumn(new EntityColumn("employeeid"));
            linkEntityAccount.addColumn(new EntityColumn("accountnumber"));
            linkEntityAccount.addColumn(new EntityColumn("territoryid"));
            linkEntityProduct.addColumn(new EntityColumn("msus_is_capital"));
            linkEntityProduct.addColumn(new EntityColumn("productnumber"));
            linkEntityProduct.addColumn(new EntityColumn("col_itemgroup"));
            linkEntityProduct.addColumn(new EntityColumn("col_producfamily"));

            // Filter by fiscal month
            ArrayList<Filter.FilterCondition> salesOrderConditions = new ArrayList<>();
            Filter.FilterCondition conditionOrderDateMonth = new Filter.FilterCondition(
                    "submitdate", Filter.Operator.IN_FISCAL_PERIOD, Integer.toString(monthNum));
            salesOrderConditions.add(conditionOrderDateMonth);

            // Filter by fiscal year
            Filter.FilterCondition conditionOrderDateYear = new Filter.FilterCondition(
                    "submitdate", Filter.Operator.IN_FISCAL_YEAR, Integer.toString(yearNum));
            salesOrderConditions.add(conditionOrderDateYear);

            // Create and populate a condition array for a link entity
            ArrayList<Filter.FilterCondition> customerConditions = new ArrayList<>();
            Filter.FilterCondition customerCondition1 = new Filter.FilterCondition(
                    "accountid", Filter.Operator.EQUALS, customerid);
            customerConditions.add(customerCondition1);

            // Add new filters to the link entities that have filters
            linkEntitySalesOrder.addFilter(new Filter(AND, salesOrderConditions));
            linkEntityAccount.addFilter(new Filter(AND, customerConditions));

            // Create and add a sort clause
            SortClause sortClause = new SortClause("createdon",
                    true, SortClause.ClausePosition.ONE);
            factory.addSortClause(sortClause);

            // Add the constructed link entities
            factory.addLinkEntity(linkEntityAccount);
            factory.addLinkEntity(linkEntityProduct);
            factory.addLinkEntity(linkEntitySalesOrder);
            factory.addLinkEntity(linkEntitySystemUser);

            // Build teh query
            String query = factory.construct();

            return query;
        }

        public static String getOrderLinesForAggregatedTotals(String customerid, int lastXyears) {

            /************** TESTING *************/
            // repid = "DAA46FDF-5B7C-E711-80D1-005056A32EEA";
            /************************************/


            // Main entity columns
            QueryFactory factory = new QueryFactory("salesorderdetail");
            factory.addColumn("productid");
            factory.addColumn("quantity");
            factory.addColumn("extendedamount");
            factory.addColumn("salesorderid");
            factory.addColumn("salesorderdetailid");

            // Create link entities
            LinkEntity le1 = new LinkEntity(
                    "salesorder",
                    "salesorderid",
                    "salesorderid",
                    "a_6ec0e72e4c104394bc627456c6412838"
            );
            Filter.FilterCondition filterCondition = new Filter.FilterCondition("submitdate",
                    getDateOperator(Operators.DateOperator.LAST_X_YEARS), Integer.toString(lastXyears));
            le1.addFilter(new Filter(AND, filterCondition));
            factory.addLinkEntity(le1);

            LinkEntity le2 = new LinkEntity(
                    "product",
                    "productid",
                    "productid",
                    "a_070ef9d142cd40d98bebd513e03c7cd1"
            );
            le2.addColumn(new EntityColumn("col_itemgroup"));
            le2.addColumn(new EntityColumn("msus_is_capital"));
            factory.addLinkEntity(le2);

            filterCondition = new Filter.FilterCondition("new_customer", Filter.Operator.EQUALS, customerid);
            Filter filter = new Filter(AND, filterCondition);
            factory.setFilter(filter);

            String query = factory.construct();

            return query;
        }

    }

    public static class Orders {

        public static String getOrders(String repid, Operators.DateOperator operator) {

            // Order line columns
            QueryFactory factory = new QueryFactory("salesorder");
            factory.addColumn("name");
            factory.addColumn("customerid");
            factory.addColumn("totalamount");
            factory.addColumn("salesorderid");
            factory.addColumn("submitdate");
            factory.addColumn("ownerid");
            factory.addColumn("col_salesterritory");

            ArrayList<Filter.FilterCondition> conditions = new ArrayList<>();
            Filter.FilterCondition condition1 = new Filter.FilterCondition("datefulfilled", getDateOperator(operator));
            Filter.FilterCondition condition2 = new Filter.FilterCondition("ownerid", Filter.Operator.EQUALS, repid);
            conditions.add(condition1);
            conditions.add(condition2);

            Filter filter1 = new Filter(AND, conditions);
            factory.setFilter(filter1);

            SortClause sortClause = new SortClause("submitdate", true, SortClause.ClausePosition.ONE);
            factory.addSortClause(sortClause);

            String query = factory.construct();

            return query;
        }
    }

    public static class Goals {

        public static String getYtdGoalsByRep(String repid, int yearNum) {

            /************** TESTING *************/
            // repid = "E2A46FDF-5B7C-E711-80D1-005056A32EEA";
            /************************************/


            // Query columns
            QueryFactory factory = new QueryFactory("goal");
            factory.addColumn("title");
            factory.addColumn("goalownerid");
            factory.addColumn("percentage");
            factory.addColumn("targetmoney");
            factory.addColumn("actualmoney");
            factory.addColumn("goalid");
            factory.addColumn("msus_fiscalfirstdayofmonth");

            // Link entities
            LinkEntity linkEntityOwner = new LinkEntity("systemuser", "systemuserid", "owninguser", "a_1124b4bdf013df11a16e00155d7aa40d");
            linkEntityOwner.addColumn(new EntityColumn("territoryid"));
            linkEntityOwner.addColumn(new EntityColumn("employeeid"));
            factory.addLinkEntity(linkEntityOwner);


            // Filter conditions
            Filter.FilterCondition condition1 = new Filter
                    .FilterCondition("goalstartdate", Filter.Operator.IN_FISCAL_YEAR, Integer.toString(yearNum));
            Filter.FilterCondition condition2 = new Filter
                    .FilterCondition("goalenddate", Filter.Operator.IN_FISCAL_YEAR, Integer.toString(yearNum));
            Filter.FilterCondition condition3 = new Filter
                    .FilterCondition("title", Filter.Operator.NOT_CONTAINS, "month");
            Filter.FilterCondition condition4 = new Filter
                    .FilterCondition("goalownerid", Filter.Operator.EQUALS, repid);

            ArrayList<Filter.FilterCondition> conditions = new ArrayList<>();
            conditions.add(condition1);
            conditions.add(condition2);
            conditions.add(condition3);
            conditions.add(condition4);

            // Set filter
            Filter filter = new Filter(AND, conditions);
            factory.setFilter(filter);

            // Build query
            String query = factory.construct();

            return query;
        }

        public static String getMtdGoalsByRep(String repid, int monthNum, int yearNum) {

            /**************     TESTING     *************/
            // repid = "E2A46FDF-5B7C-E711-80D1-005056A32EEA";
            /********************************************/

            DateTime firstofCurMonth = new DateTime(DateTime.now().getYear(),
                    DateTime.now().getMonthOfYear(), 1,0,0);

            // Query columns
            QueryFactory factory = new QueryFactory("goal");
            factory.addColumn("title");
            factory.addColumn("goalownerid");
            factory.addColumn("percentage");
            factory.addColumn("targetmoney");
            factory.addColumn("actualmoney");
            factory.addColumn("goalid");
            factory.addColumn("msus_fiscalfirstdayofmonth");

            // Link entities
            LinkEntity linkEntityOwner = new LinkEntity("systemuser", "systemuserid", "owninguser", "a_1124b4bdf013df11a16e00155d7aa40d");
            linkEntityOwner.addColumn(new EntityColumn("territoryid"));
            linkEntityOwner.addColumn(new EntityColumn("employeeid"));
            factory.addLinkEntity(linkEntityOwner);


            // Filter conditions
            Filter.FilterCondition condition1 = new Filter
                    .FilterCondition("title", Filter.Operator.CONTAINS, "month");
            Filter.FilterCondition condition2 = new Filter
                    .FilterCondition("goalownerid", Filter.Operator.EQUALS, repid);
            Filter.FilterCondition condition3 = new Filter
                    .FilterCondition("msus_fiscalfirstdayofmonth", Filter.Operator.IN_FISCAL_YEAR, Integer.toString(yearNum));
            Filter.FilterCondition condition4 = new Filter
                    .FilterCondition("msus_fiscalfirstdayofmonth", Filter.Operator.IN_FISCAL_PERIOD, Integer.toString(monthNum));

            ArrayList<Filter.FilterCondition> conditions = new ArrayList<>();
            conditions.add(condition1);
            conditions.add(condition2);
            conditions.add(condition3);
            conditions.add(condition4);

            // Set filter
            Filter filter = new Filter(AND, conditions);
            factory.setFilter(filter);

            // Build query
            String query = factory.construct();

            return query;
        }

        public static String getYtdGoalsByRegion(String regionid, int yearNum) {

            /************** TESTING *************/
            // regionid = WEST_REGIONID;
            /************************************/


            // Query columns
            QueryFactory factory = new QueryFactory("goal");
            factory.addColumn("title");
            factory.addColumn("goalownerid");
            factory.addColumn("percentage");
            factory.addColumn("targetmoney");
            factory.addColumn("actualmoney");
            factory.addColumn("goalid");
            factory.addColumn("goalstartdate");
            factory.addColumn("goalenddate");
            factory.addColumn("msus_fiscalfirstdayofmonth");

            // Link entities
            LinkEntity linkEntityOwner = new LinkEntity("systemuser", "systemuserid", "owninguser", "a_1124b4bdf013df11a16e00155d7aa40d");
            linkEntityOwner.addColumn(new EntityColumn("territoryid"));
            linkEntityOwner.addColumn(new EntityColumn("employeeid"));
            factory.addLinkEntity(linkEntityOwner);

            // Filter conditions
            Filter.FilterCondition condition1 = new Filter
                    .FilterCondition("goalstartdate", Filter.Operator.IN_FISCAL_YEAR,
                        Integer.toString(yearNum));
            Filter.FilterCondition condition2 = new Filter
                    .FilterCondition("goalenddate", Filter.Operator.IN_FISCAL_YEAR,
                        Integer.toString(yearNum));
            Filter.FilterCondition condition3 = new Filter
                    .FilterCondition("title", Filter.Operator.NOT_CONTAINS, "month");
            Filter.FilterCondition condition4 = new Filter
                    .FilterCondition("msus_region", Filter.Operator.EQUALS, regionid);

            ArrayList<Filter.FilterCondition> conditions = new ArrayList<>();
            conditions.add(condition1);
            conditions.add(condition2);
            conditions.add(condition3);
            conditions.add(condition4);

            // Set filter
            Filter filter = new Filter(AND, conditions);
            factory.setFilter(filter);

            // Sort clause
            SortClause sortClause = new SortClause("percentage", true, SortClause.ClausePosition.ONE);
            factory.addSortClause(sortClause);

            // Build query
            String query = factory.construct();

            return query;
        }

        public static String getMtdGoalsByRegion(String regionid, int monthNum, int yearNum) {

            /**************     TESTING     *************/
            // regionid = WEST_REGIONID;
            /********************************************/

            // Query columns
            QueryFactory factory = new QueryFactory("goal");
            factory.addColumn("title");
            factory.addColumn("goalownerid");
            factory.addColumn("percentage");
            factory.addColumn("targetmoney");
            factory.addColumn("actualmoney");
            factory.addColumn("goalid");
            factory.addColumn("goalstartdate");
            factory.addColumn("goalenddate");
            factory.addColumn("msus_fiscalfirstdayofmonth");

            // Link entities
            LinkEntity linkEntityOwner = new LinkEntity("systemuser", "systemuserid", "owninguser", "a_1124b4bdf013df11a16e00155d7aa40d");
            linkEntityOwner.addColumn(new EntityColumn("territoryid"));
            linkEntityOwner.addColumn(new EntityColumn("employeeid"));
            factory.addLinkEntity(linkEntityOwner);

            // Create filter conditions
            Filter.FilterCondition condition1 = new Filter
                    .FilterCondition("title", Filter.Operator.CONTAINS, "month");
            Filter.FilterCondition condition2 = new Filter
                    .FilterCondition("msus_region", Filter.Operator.EQUALS, regionid);
            Filter.FilterCondition condition3 = new Filter
                    .FilterCondition("msus_fiscalfirstdayofmonth",
                        Filter.Operator.IN_FISCAL_YEAR, Integer.toString(yearNum));
            Filter.FilterCondition condition4 = new Filter
                    .FilterCondition("msus_fiscalfirstdayofmonth",
                        Filter.Operator.IN_FISCAL_PERIOD, Integer.toString(monthNum));

            // Add filter conditions
            ArrayList<Filter.FilterCondition> conditions = new ArrayList<>();
            conditions.add(condition1);
            conditions.add(condition2);
            conditions.add(condition3);
            conditions.add(condition4);

            // Create and set filter
            Filter filter = new Filter(AND, conditions);
            factory.setFilter(filter);

            // Sort clause
            SortClause sortClause = new SortClause("percentage", true, SortClause.ClausePosition.ONE);
            factory.addSortClause(sortClause);

            // Build query
            String query = factory.construct();

            return query;
        }

    }

    public static class Addresses {

        /**
         * Gets all addresses on file for the account specified.  Some accounts will not have address
         * names.  Accounts that do not contain data in the Street1 field will be omitted.
         * @param accountid The account to query.
         * @return The fetchXml query to use against the Dynamics API.
         */
        public static String getAllAddresses(String accountid) {
            QueryFactory query = new QueryFactory("customeraddress");
            query.addColumn("customeraddressid");
            query.addColumn("name");
            query.addColumn("line1");
            query.addColumn("city");
            query.addColumn("stateorprovince");
            query.addColumn("postalcode");
            query.addColumn("customeraddressid");
            query.addColumn("addresstypecode");
            query.addColumn("addressnumber");
            query.addColumn("composite");

            // Only get addresses that have data in the address1 field (non-null)
            Filter.FilterCondition condition1 = new Filter.FilterCondition
                    ("line1", Filter.Operator.CONTAINS_DATA);
            Filter filter = new Filter(AND, condition1);
            query.setFilter(filter);

            // Link to the account entity using the accountid
            LinkEntity linkEntity1 = new LinkEntity("account", "accountid", "parentid", "ac");
            linkEntity1.addColumn(new EntityColumn("accountnumber"));
            linkEntity1.addColumn(new EntityColumn("new_accountid"));
            linkEntity1.addColumn(new EntityColumn("accountid"));
            Filter le1Filter = new Filter(AND);
            le1Filter.conditions.add(new Filter.FilterCondition("accountid", Filter.Operator.EQUALS, accountid));
            linkEntity1.addFilter(le1Filter);
            query.addLinkEntity(linkEntity1);

            // Spit out the encoded query
            String rslt = query.construct();
            return rslt;
        }

        public static String getAllAccountAddresses() {

            QueryFactory query = new QueryFactory("account");
            query.addColumn("name");
            query.addColumn("customertypecode");
            query.addColumn("accountid");
            query.addColumn("address1_name");
            query.addColumn("address1_composite");
            query.addColumn("address1_longitude");
            query.addColumn("address1_latitude");
            query.addColumn("accountnumber");
            query.addColumn("msus_loc_updated");

            ArrayList<Filter.FilterCondition> conditions = new ArrayList<>();
            Filter.FilterCondition condition1 = new Filter.FilterCondition
                    ("msus_loc_updated", Filter.Operator.EQUALS, "true");
            conditions.add(condition1);
            Filter filter = new Filter(AND, conditions);

            query.setFilter(filter);

            // Spit out the encoded query
            String rslt = query.construct();
            return rslt;
        }

    }

    public static class TripAssociation {
        public static String getAssociationsByTripid(String tripid) {

            // Query columns
            QueryFactory factory = new QueryFactory("msus_mileageassociation");
            factory.addColumn("msus_name");
            factory.addColumn("createdon");
            factory.addColumn("msus_associated_trip");
            factory.addColumn("msus_associated_opportunity");
            factory.addColumn("msus_associated_account");
            factory.addColumn("msus_mileageassociationid");
            factory.addColumn("ownerid");
            factory.addColumn("msus_disposition");

            // Filter conditions
            Filter.FilterCondition condition1 = new Filter
                    .FilterCondition("msus_associated_trip", Filter.Operator.EQUALS,
                    tripid);

            ArrayList<Filter.FilterCondition> conditions = new ArrayList<>();
            conditions.add(condition1);

            // Set filter
            Filter filter = new Filter(AND, conditions);
            factory.setFilter(filter);

            // Build query
            String query = factory.construct();

            return query;
        }

        public static String getAssociationsLastXMonths(int months) {

            // Query columns
            QueryFactory factory = new QueryFactory("msus_mileageassociation");
            factory.addColumn("msus_name");
            factory.addColumn("createdon");
            factory.addColumn("msus_associated_trip");
            factory.addColumn("msus_associated_opportunity");
            factory.addColumn("msus_associated_account");
            factory.addColumn("msus_mileageassociationid");
            factory.addColumn("ownerid");
            factory.addColumn("msus_disposition");

            // Filter conditions
            Filter.FilterCondition condition2 = new Filter
                    .FilterCondition("ownerid", Filter.Operator.EQUALS,
                    MediUser.getMe().systemuserid);

            ArrayList<Filter.FilterCondition> conditions = new ArrayList<>();
            conditions.add(condition2);

            LinkEntity linkEntityTrip = new LinkEntity("msus_fulltrip", "msus_fulltripid",
                    "msus_associated_trip", "af");
            linkEntityTrip.addFilter(new Filter(AND, new Filter.FilterCondition(
                    "msus_dt_tripdate", Filter.Operator.LAST_X_MONTHS, Integer.toString(months))));

            factory.addLinkEntity(linkEntityTrip);

            // Set filter
            Filter filter = new Filter(AND, conditions);
            factory.setFilter(filter);

            // Build query
            String query = factory.construct();

            return query;
        }
    }

    public static class Search {

        public static String searchCustInventory(int serialnumber) {
            // Instantiate a new constructor for the case entity and add the columns we want to see
            QueryFactory query = new QueryFactory("col_customerinventory");
            query.addColumn("col_name");
            query.addColumn("statuscode");
            query.addColumn("col_serialnumber");
            query.addColumn("col_ownershipcapital");
            query.addColumn("modifiedon");
            query.addColumn("col_revision");
            query.addColumn("overriddencreatedon");
            query.addColumn("col_quantity");
            query.addColumn("col_productid");
            query.addColumn("col_item");
            query.addColumn("col_itemgroup");
            query.addColumn("col_batchnumber");
            query.addColumn("col_accountid");
            query.addColumn("col_referencenumber");
            query.addColumn("col_customerinventoryid");

            LinkEntity le1 = new LinkEntity("systemuser", "systemuserid", "owninguser", "a_4114a0e1fc19e71180d2005056a36b9b");
            le1.addColumn(new EntityColumn("domainname"));
            le1.addColumn(new EntityColumn("territoryid"));

            LinkEntity le2 = new LinkEntity("team", "teamid", "owningteam", "a_4814a0e1fc19e71180d2005056a36b9b");
            le2.addColumn(new EntityColumn("name"));

            LinkEntity le3 = new LinkEntity("account", "accountid", "col_accountid", "aa");
            Filter le3Filter = new Filter(AND);
            le3.addFilter(le3Filter);

            // Add our link entities
            // query.addLinkEntity(le1);
            // query.addLinkEntity(le2);
            query.addLinkEntity(le3);

            // Create a filter
            Filter filter = new Filter(AND);

            // Set filter conditions
            Filter.FilterCondition condition1 = new Filter.FilterCondition("col_serialnumber", Filter.Operator.CONTAINS, Integer.toString(serialnumber));

            // Apply the conditions to the filter
            filter.addCondition(condition1);

            // Set the query's global filter
            query.setFilter(filter);

            // Spit out the encoded query
            String rslt = query.construct();
            return rslt;
        }

        public static String searchAccounts(String searchQuery) {
            // Instantiate a new constructor for the case entity and add the columns we want to see
            QueryFactory query = new QueryFactory("account");
            query.addColumn("name");
            query.addColumn("customertypecode");
            query.addColumn("accountid");
            query.addColumn("territoryid");
            query.addColumn("msus_salesrep");
            query.addColumn("msus_salesregionid");
            query.addColumn("col_agreementtype");
            query.addColumn("accountnumber");

            // Filter creation to make use of our conditions
            Filter filter = new Filter(OR);
            filter.addCondition(new Filter.FilterCondition("name", Filter.Operator.CONTAINS, searchQuery));
            filter.addCondition(new Filter.FilterCondition("accountnumber", Filter.Operator.CONTAINS, searchQuery));
            query.setFilter(filter);

            // Link entity creation to join to the account entity and apply our territory condition
            LinkEntity le1 = new LinkEntity("businessunit", "businessunitid", "owningbusinessunit", "a_6ad8133d2f1e4c43a3da460bacb3d6a5");
            le1.addColumn(new EntityColumn("new_managername"));
            le1.addColumn(new EntityColumn("name"));

            SortClause sortClause = new SortClause("name", false, SortClause.ClausePosition.ONE);
            query.addSortClause(sortClause);

            // Spit out the encoded query
            String rslt = query.construct();
            return rslt;
        }

        public static String searchTickets(String searchQuery) {
            QueryFactory query = new QueryFactory("incident");
            query.addColumn("ticketnumber");
            query.addColumn("title");
            query.addColumn("createdon");
            query.addColumn("customerid");
            query.addColumn("ownerid");
            query.addColumn("caseorigincode");
            query.addColumn("statuscode");
            query.addColumn("new_accountnumber");
            query.addColumn("incidentid");
            query.addColumn("subjectid");
            query.addColumn("statecode");
            query.addColumn("stageid");
            query.addColumn("processid");
            query.addColumn("prioritycode");
            query.addColumn("modifiedon");
            query.addColumn("modifiedby");
            query.addColumn("description");
            query.addColumn("createdby");
            query.addColumn("casetypecode");
            query.addColumn("incidentstagecode");

            LinkEntity linkEntity = new LinkEntity("account", "accountid",
                    "customerid", "a_4b5945b8a4a64613afc1ae1d5e6828c7");
            linkEntity.addColumn(new EntityColumn("territoryid"));
            linkEntity.addColumn(new EntityColumn("msus_salesrep"));
            query.addLinkEntity(linkEntity);

            // Link entity creation to join the contact info
            LinkEntity linkEntity_contact = new LinkEntity("contact", "contactid", "new_mw_contact", "a_b49161e62067e71180d6005056a36b9b");
            linkEntity_contact.addColumn(new EntityColumn("fullname"));
            linkEntity_contact.addColumn(new EntityColumn("emailaddress1"));
            linkEntity_contact.addColumn(new EntityColumn("telephone1"));
            query.addLinkEntity(linkEntity_contact);

            SortClause sortClause = new SortClause("createdon", true, SortClause.ClausePosition.ONE);
            query.addSortClause(sortClause);

            Filter.FilterCondition condition1 = new Filter.FilterCondition("title",
                    Filter.Operator.CONTAINS, searchQuery);
            Filter.FilterCondition condition2 = new Filter.FilterCondition("description",
                    Filter.Operator.CONTAINS, searchQuery);
            Filter.FilterCondition condition3 = new Filter.FilterCondition("ticketnumber",
                    Filter.Operator.CONTAINS, searchQuery);

            Filter filter1 = new Filter(OR);
            filter1.addCondition(condition1);
            filter1.addCondition(condition2);
            filter1.addCondition(condition3);
            query.setFilter(filter1);

            return query.construct();

        }

        public static String searchOpportunities(String searchQuery) {

            // Query columns
            QueryFactory factory = new QueryFactory("opportunity");
            factory.addColumn("name");
            factory.addColumn("estimatedvalue");
            factory.addColumn("estimatedclosedate");
            factory.addColumn("col_dealtype");
            factory.addColumn("ownerid");
            factory.addColumn("parentaccountid");
            factory.addColumn("stepname");
            factory.addColumn("createdon");
            factory.addColumn("createdby");
            factory.addColumn("modifiedon");
            factory.addColumn("modifiedby");
            factory.addColumn("msus_probability");
            factory.addColumn("opportunityid");
            factory.addColumn("statuscode");
            factory.addColumn("statecode");
            factory.addColumn("currentsituation");
            factory.addColumn("stepname");

            factory.addColumn("col_estmrevenuedevices");
            factory.addColumn("col_estmrevenueprobes");
            factory.addColumn("estimatedvalue");
            factory.addColumn("new_territoryrevenue");

            // Link entities
            LinkEntity linkEntityAccount = new LinkEntity("account", "accountid", "parentaccountid", "ab");
            linkEntityAccount.addColumn(new EntityColumn("territoryid"));

            // Filter conditions
            Filter.FilterCondition condition1 = new Filter
                    .FilterCondition("name", Filter.Operator.CONTAINS, searchQuery);
            Filter.FilterCondition condition2 = new Filter
                    .FilterCondition("parentaccountidname", Filter.Operator.CONTAINS, searchQuery);

            ArrayList<Filter.FilterCondition> conditions = new ArrayList<>();
            conditions.add(condition1);
            conditions.add(condition2);

            // Set filter
            Filter filter = new Filter(OR, conditions);
            factory.setFilter(filter);

            // Sort clause
            SortClause sortClause = new SortClause("createdon",
                    true, SortClause.ClausePosition.ONE);
            factory.addSortClause(sortClause);

            // Build query
            String query = factory.construct();

            return query;
        }

        public static String searchContacts(String searchQuery) {
            // Instantiate a new constructor for the case entity and add the columns we want to see
            QueryFactory query = new QueryFactory("contact");
            query.addColumn("fullname");
            query.addColumn("firstname");
            query.addColumn("lastname");
            query.addColumn("createdon");
            query.addColumn("createdby");
            query.addColumn("statecode");
            query.addColumn("modifiedon");
            query.addColumn("modifiedby");
            query.addColumn("statuscode");
            query.addColumn("address1_composite");
            query.addColumn("parentcustomerid");
            query.addColumn("mobilephone");
            query.addColumn("telephone1");
            query.addColumn("address1_telephone1");
            query.addColumn("emailaddress1");
            query.addColumn("msus_associated_npi_number");
            query.addColumn("jobtitle");
            query.addColumn("msus_department");
            query.addColumn("contactid");
            query.addColumn("mobilephone");

            // Create a filter
            Filter filter = new Filter(AND);

            // Set filter
            Filter.FilterCondition condition1 = new Filter.FilterCondition("fullname", Filter.Operator.CONTAINS, searchQuery );
            filter.addCondition(condition1);
            query.setFilter(filter);

            // Spit out the encoded query
            String rslt = query.construct();
            return rslt;
        }
    }






































}
