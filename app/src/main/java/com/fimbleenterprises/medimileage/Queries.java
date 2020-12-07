package com.fimbleenterprises.medimileage;

import org.joda.time.DateTime;

import java.util.ArrayList;

import static com.fimbleenterprises.medimileage.Queries.Operators.getDateOperator;
import static com.fimbleenterprises.medimileage.QueryFactory.*;
import static com.fimbleenterprises.medimileage.QueryFactory.Filter.FilterType.AND;

public class Queries {

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
            TODAY, YESTERDAY, THIS_WEEK, THIS_MONTH, THIS_YEAR, LAST_WEEK, LAST_MONTH, LAST_YEAR, LAST_7_DAYS, LAST_14_DAYS, LAST_X_MONTHS
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
                default:
                    return Filter.Operator.TODAY;
            }
        }
    }

    public static class Trips {

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

        // status codes
        public static final int IN_PROGRESS = 1;
        public static final int ON_HOLD = 2;
        public static final int TO_BE_INSPECTED = 100000002;
        public static final int WAITING_ON_REP = 3;
        public static final int WAITING_FOR_PRODUCT = 4;
        public static final int WAITING_ON_CUSTOMER = 100000001;
        public static final int TO_BE_BILLED = 100000003;

        public static String getCase(String caseid) {

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

            // Spit out the encoded query
            String rslt = query.construct();
            return rslt;
        }

        public static String getTickets(String territoryid) {
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

            SortClause sortClause = new SortClause("createdon", true, SortClause.ClausePosition.ONE);
            query.addSortClause(sortClause);

            Filter.FilterCondition condition1 = new Filter.FilterCondition("createdon",
                    getDateOperator(Operators.DateOperator.LAST_X_MONTHS), "3");
            Filter filter1 = new Filter(AND,condition1);

            query.setFilter(filter1);

            return query.construct();
        }

        public static String getTickets(String territoryid, int statuscode) {
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

            SortClause sortClause = new SortClause("createdon", true, SortClause.ClausePosition.ONE);
            query.addSortClause(sortClause);

            Filter.FilterCondition condition1 = new Filter.FilterCondition("createdon",
                    getDateOperator(Operators.DateOperator.LAST_X_MONTHS), "3");
            Filter.FilterCondition condition2 = new Filter.FilterCondition("statuscode",
                    Filter.Operator.EQUALS, Integer.toString(statuscode));

            Filter filter1 = new Filter(AND);
            filter1.addCondition(condition1);
            filter1.addCondition(condition2);
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

    public static class Accounts {

        public static String getAccountInventory(String accountid, String itemgroupnumber, CustomerInventoryStatusCode status) {
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

        public static String getAccountsByTerritory(String territoryid) {

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

        /*public static String getAllOpenOpportunities() {

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
            factory.addColumn("msus_probability");
            factory.addColumn("opportunityid");
            factory.addColumn("statuscode");
            factory.addColumn("statecode");
            factory.addColumn("currentsituation");
            factory.addColumn("stepname");

            // Link entities
            LinkEntity linkEntityAccount = new LinkEntity("account", "accountid", "parentaccountid", "ab");
            linkEntityAccount.addColumn(new EntityColumn("territoryid"));
            Filter.FilterCondition linkentityAccountCondition = new Filter.FilterCondition(
                    "statecode", Filter.Operator.EQUALS, "0");
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
        }*/

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
            factory.addColumn("msus_probability");
            factory.addColumn("opportunityid");
            factory.addColumn("statuscode");
            factory.addColumn("statecode");
            factory.addColumn("currentsituation");
            factory.addColumn("stepname");

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

        public static String getOpportunitiesByAccount(String accountid) {

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
            factory.addColumn("msus_probability");
            factory.addColumn("opportunityid");
            factory.addColumn("statuscode");
            factory.addColumn("statecode");
            factory.addColumn("currentsituation");
            factory.addColumn("stepname");

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
            factory.addColumn("msus_probability");
            factory.addColumn("opportunityid");
            factory.addColumn("statuscode");
            factory.addColumn("statecode");
            factory.addColumn("currentsituation");
            factory.addColumn("stepname");

            // Link entities
            LinkEntity linkEntityAccount = new LinkEntity("account", "accountid", "parentaccountid", "ab");
            linkEntityAccount.addColumn(new EntityColumn("territoryid"));
            Filter.FilterCondition linkentityAccountCondition = new Filter.FilterCondition(
                    "accountid", Filter.Operator.EQUALS, opportunityid);
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

    public static class OrderLines {

        public static String getOrderLines(String territoryid, Operators.DateOperator operator) {

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
            Filter.FilterCondition conditionRepId = new Filter.FilterCondition(
                    "territoryid", Filter.Operator.EQUALS, territoryid);
            customerConditions.add(conditionRepId);

            // Add new filters to the link entities that have filters
            linkEntitySalesOrder.addFilter(new Filter(AND, salesOrderConditions));
            linkEntityAccount.addFilter(new Filter(AND, customerConditions));

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

        public static String getOrderLines(String territoryid, Operators.DateOperator operator, int num) {

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
            Filter.FilterCondition conditionRepId = new Filter.FilterCondition(
                    "territoryid", Filter.Operator.EQUALS, territoryid);
            customerConditions.add(conditionRepId);

            // Add new filters to the link entities that have filters
            linkEntitySalesOrder.addFilter(new Filter(AND, salesOrderConditions));
            linkEntityAccount.addFilter(new Filter(AND, customerConditions));

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

        public static String getOrderLines(String territoryid, int monthNum) {

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
                    "submitdate", Filter.Operator.IN_FISCAL_PERIOD, Integer.toString(monthNum));
            salesOrderConditions.add(conditionOrderDate);

            // Create and populate a condition array for a link entity
            ArrayList<Filter.FilterCondition> customerConditions = new ArrayList<>();
            Filter.FilterCondition conditionRepId = new Filter.FilterCondition(
                    "territoryid", Filter.Operator.EQUALS, territoryid);
            customerConditions.add(conditionRepId);

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

        public static String getOrderLinesByAccount(String customerid, int monthNum) {

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
                    "submitdate", Filter.Operator.IN_FISCAL_PERIOD, Integer.toString(monthNum));
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
    }






































}
