package com.fimbleenterprises.medimileage;

import org.joda.time.DateTime;

import java.util.ArrayList;

import static com.fimbleenterprises.medimileage.Queries.Operators.getDateOperator;
import static com.fimbleenterprises.medimileage.QueryFactory.Filter.FilterType.AND;

public class Queries {

    public static final String EAST_REGIONID = "00AB0144-2AFA-E711-80DE-005056A36B9B";
    public static final String WEST_REGIONID = "61E8B94E-2AFA-E711-80DE-005056A36B9B";
    public static final String WEST_BUSINESSUNITID = "02AD3593-FB81-E711-80D7-005056A36B9B";
    public static final String EAST_BUSINESSUNITID = "101E629C-FB81-E711-80D7-005056A36B9B";
    public static final String JOHN_SYSTEMUSERID = "DAA46FDF-5B7C-E711-80D1-005056A32EEA";

    public static class Operators {
        public enum DateOperator {
            TODAY, YESTERDAY, THIS_WEEK, THIS_MONTH, THIS_YEAR, LAST_WEEK, LAST_MONTH, LAST_YEAR, LAST_7_DAYS, LAST_14_DAYS
        }

        public static String getDateOperator(DateOperator operator) {
            switch (operator) {
                case YESTERDAY:
                    return QueryFactory.Filter.Operator.YESTERDAY;
                case THIS_WEEK:
                    return QueryFactory.Filter.Operator.THIS_WEEK;
                case THIS_MONTH:
                    return QueryFactory.Filter.Operator.THIS_MONTH;
                case THIS_YEAR:
                    return QueryFactory.Filter.Operator.THIS_YEAR;
                case LAST_WEEK:
                    return QueryFactory.Filter.Operator.LAST_WEEK;
                case LAST_MONTH:
                    return QueryFactory.Filter.Operator.LAST_MONTH;
                case LAST_YEAR:
                    return QueryFactory.Filter.Operator.LAST_YEAR;
                default:
                    return QueryFactory.Filter.Operator.TODAY;
            }
        }
    }

    public static class Tickets {
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
            QueryFactory.Filter filter = new QueryFactory.Filter(AND);
            filter.addCondition(new QueryFactory.Filter.FilterCondition("incidentid","eq",caseid));
            query.setFilter(filter);

            // Link entity creation to join to the account entity and apply our territory condition
            QueryFactory.LinkEntity linkEntity_accountStuff = new QueryFactory.LinkEntity(QueryFactory.LINK_ENTITY_STRINGS.CASES_TO_ACCOUNT);
            linkEntity_accountStuff.addColumn(new QueryFactory.EntityColumn("territoryid"));
            linkEntity_accountStuff.addColumn(new QueryFactory.EntityColumn("msus_salesrep"));
            linkEntity_accountStuff.addColumn(new QueryFactory.EntityColumn("accountnumber"));
            query.addLinkEntity(linkEntity_accountStuff);

            // Spit out the encoded query
            String rslt = query.construct();
            return rslt;
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
            QueryFactory.Filter filter = new QueryFactory.Filter(AND);
            filter.addCondition(new QueryFactory.Filter.FilterCondition("internalemailaddress"
                    ,"eq",email));
            query.setFilter(filter);

            // Link entity creation to join to the account entity and apply our territory condition
            QueryFactory.LinkEntity linkEntity_territory =
                    new QueryFactory.LinkEntity(QueryFactory.LINK_ENTITY_STRINGS.TERRITORY_TO_USER);
            linkEntity_territory.addColumn(new QueryFactory.EntityColumn("name"));
            linkEntity_territory.addColumn(new QueryFactory.EntityColumn("new_salesrepresentative"));
            linkEntity_territory.addColumn(new QueryFactory.EntityColumn("msus_salesregion"));
            linkEntity_territory.addColumn(new QueryFactory.EntityColumn("managerid"));
            linkEntity_territory.addColumn(new QueryFactory.EntityColumn("new_businessunit"));
            query.addLinkEntity(linkEntity_territory);

            QueryFactory.LinkEntity linkEntity_Manager =
                    new QueryFactory.LinkEntity(QueryFactory.LINK_ENTITY_STRINGS.MANAGER_TO_USER);
            linkEntity_Manager.addColumn(new QueryFactory.EntityColumn("businessunitid"));
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
            QueryFactory.Filter filter = new QueryFactory.Filter(AND);
            filter.addCondition(new QueryFactory.Filter.FilterCondition("msus_is_us_user"
                    ,"eq","1"));
            query.setFilter(filter);

            // Link entity creation to join to the account entity and apply our territory condition
            QueryFactory.LinkEntity linkEntity_territory =
                    new QueryFactory.LinkEntity(QueryFactory.LINK_ENTITY_STRINGS.TERRITORY_TO_USER);
            linkEntity_territory.addColumn(new QueryFactory.EntityColumn("name"));
            linkEntity_territory.addColumn(new QueryFactory.EntityColumn("new_salesrepresentative"));
            linkEntity_territory.addColumn(new QueryFactory.EntityColumn("msus_salesregion"));
            linkEntity_territory.addColumn(new QueryFactory.EntityColumn("managerid"));
            linkEntity_territory.addColumn(new QueryFactory.EntityColumn("new_businessunit"));
            query.addLinkEntity(linkEntity_territory);

            QueryFactory.LinkEntity linkEntity_Manager =
                    new QueryFactory.LinkEntity(QueryFactory.LINK_ENTITY_STRINGS.MANAGER_TO_USER);
            linkEntity_Manager.addColumn(new QueryFactory.EntityColumn("businessunitid"));
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
            QueryFactory.Filter filter = new QueryFactory.Filter(AND);
            filter.addCondition(new QueryFactory.Filter.FilterCondition("managerid",
                            QueryFactory.Filter.Operator.CONTAINS_DATA));
            query.setFilter(filter);

            QueryFactory.SortClause sortClause = new QueryFactory.SortClause("name", false, QueryFactory.SortClause.ClausePosition.ONE);
            query.addSortClause(sortClause);

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
            QueryFactory query = new QueryFactory(QueryFactory.EntityNames.USER);
            query.addColumn("fullname");

            // Filter creation to make use of our conditions
            QueryFactory.Filter filter = new QueryFactory.Filter(AND);
            filter.addCondition(new QueryFactory.Filter.FilterCondition("systemuserid",
                    QueryFactory.Filter.Operator.EQUALS, MediUser.getMe().systemuserid));
            query.setFilter(filter);

            // Spit out the encoded query
            String rslt = query.construct();
            return rslt;
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
            QueryFactory.LinkEntity linkEntitySalesOrder = new QueryFactory.LinkEntity(
                    "salesorder",
                    "salesorderid",
                    "salesorderid",
                    "a_6ec0e72e4c104394bc627456c6412838"
            );
            QueryFactory.LinkEntity linkEntitySystemUser = new QueryFactory.LinkEntity(
                    "systemuser",
                    "systemuserid",
                    "salesrepid",
                    "a_a1cf96c07c114d478335b8c445651a12"
            );
            QueryFactory.LinkEntity linkEntityAccount = new QueryFactory.LinkEntity(
                    "account",
                    "accountid",
                    "new_customer",
                    "a_db24f99da8fee71180df005056a36b9b"
            );
            QueryFactory.LinkEntity linkEntityProduct = new QueryFactory.LinkEntity(
                    "product",
                    "productid",
                    "productid",
                    "a_070ef9d142cd40d98bebd513e03c7cd1"
            );

            // Add columns to link entities
            linkEntitySalesOrder.addColumn(new QueryFactory.EntityColumn("submitdate"));
            linkEntitySystemUser.addColumn(new QueryFactory.EntityColumn("employeeid"));
            linkEntityAccount.addColumn(new QueryFactory.EntityColumn("accountnumber"));
            linkEntityAccount.addColumn(new QueryFactory.EntityColumn("territoryid"));
            linkEntityProduct.addColumn(new QueryFactory.EntityColumn("msus_is_capital"));
            linkEntityProduct.addColumn(new QueryFactory.EntityColumn("productnumber"));
            linkEntityProduct.addColumn(new QueryFactory.EntityColumn("col_itemgroup"));
            linkEntityProduct.addColumn(new QueryFactory.EntityColumn("col_producfamily"));

            // Create and populate a condition array for a link entity
            ArrayList<QueryFactory.Filter.FilterCondition> salesOrderConditions = new ArrayList<>();
            QueryFactory.Filter.FilterCondition conditionOrderDate = new QueryFactory.Filter.FilterCondition(
                    "submitdate", getDateOperator(operator));
            salesOrderConditions.add(conditionOrderDate);

            // Create and populate a condition array for a link entity
            ArrayList<QueryFactory.Filter.FilterCondition> customerConditions = new ArrayList<>();
            QueryFactory.Filter.FilterCondition conditionRepId = new QueryFactory.Filter.FilterCondition(
                    "territoryid", QueryFactory.Filter.Operator.EQUALS, territoryid);
            customerConditions.add(conditionRepId);

            // Add new filters to the link entities that have filters
            linkEntitySalesOrder.addFilter(new QueryFactory.Filter(AND, salesOrderConditions));
            linkEntityAccount.addFilter(new QueryFactory.Filter(AND, customerConditions));

            // Create and add a sort clause
            QueryFactory.SortClause sortClause = new QueryFactory.SortClause("salesorderid",
                    true, QueryFactory.SortClause.ClausePosition.ONE);
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
            QueryFactory.LinkEntity linkEntitySalesOrder = new QueryFactory.LinkEntity(
                    "salesorder",
                    "salesorderid",
                    "salesorderid",
                    "a_6ec0e72e4c104394bc627456c6412838"
            );
            QueryFactory.LinkEntity linkEntitySystemUser = new QueryFactory.LinkEntity(
                    "systemuser",
                    "systemuserid",
                    "salesrepid",
                    "a_a1cf96c07c114d478335b8c445651a12"
            );
            QueryFactory.LinkEntity linkEntityAccount = new QueryFactory.LinkEntity(
                    "account",
                    "accountid",
                    "new_customer",
                    "a_db24f99da8fee71180df005056a36b9b"
            );
            QueryFactory.LinkEntity linkEntityProduct = new QueryFactory.LinkEntity(
                    "product",
                    "productid",
                    "productid",
                    "a_070ef9d142cd40d98bebd513e03c7cd1"
            );

            // Add columns to link entities
            linkEntitySalesOrder.addColumn(new QueryFactory.EntityColumn("submitdate"));
            linkEntitySystemUser.addColumn(new QueryFactory.EntityColumn("employeeid"));
            linkEntityAccount.addColumn(new QueryFactory.EntityColumn("accountnumber"));
            linkEntityAccount.addColumn(new QueryFactory.EntityColumn("territoryid"));
            linkEntityProduct.addColumn(new QueryFactory.EntityColumn("msus_is_capital"));
            linkEntityProduct.addColumn(new QueryFactory.EntityColumn("productnumber"));
            linkEntityProduct.addColumn(new QueryFactory.EntityColumn("col_itemgroup"));
            linkEntityProduct.addColumn(new QueryFactory.EntityColumn("col_producfamily"));

            // Create and populate a condition array for a link entity
            ArrayList<QueryFactory.Filter.FilterCondition> salesOrderConditions = new ArrayList<>();
            QueryFactory.Filter.FilterCondition conditionOrderDate = new QueryFactory.Filter.FilterCondition(
                    "submitdate", QueryFactory.Filter.Operator.IN_FISCAL_PERIOD, Integer.toString(monthNum));
            salesOrderConditions.add(conditionOrderDate);

            // Create and populate a condition array for a link entity
            ArrayList<QueryFactory.Filter.FilterCondition> customerConditions = new ArrayList<>();
            QueryFactory.Filter.FilterCondition conditionRepId = new QueryFactory.Filter.FilterCondition(
                    "territoryid", QueryFactory.Filter.Operator.EQUALS, territoryid);
            customerConditions.add(conditionRepId);

            // Add new filters to the link entities that have filters
            linkEntitySalesOrder.addFilter(new QueryFactory.Filter(AND, salesOrderConditions));
            linkEntityAccount.addFilter(new QueryFactory.Filter(AND, customerConditions));

            // Create and add a sort clause
            QueryFactory.SortClause sortClause = new QueryFactory.SortClause("salesorderid",
                    true, QueryFactory.SortClause.ClausePosition.ONE);
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

            ArrayList<QueryFactory.Filter.FilterCondition> conditions = new ArrayList<>();
            QueryFactory.Filter.FilterCondition condition1 = new QueryFactory.Filter.FilterCondition("datefulfilled", getDateOperator(operator));
            QueryFactory.Filter.FilterCondition condition2 = new QueryFactory.Filter.FilterCondition("ownerid", QueryFactory.Filter.Operator.EQUALS, repid);
            conditions.add(condition1);
            conditions.add(condition2);

            QueryFactory.Filter filter1 = new QueryFactory.Filter(AND, conditions);
            factory.setFilter(filter1);

            QueryFactory.SortClause sortClause = new QueryFactory.SortClause("submitdate", true, QueryFactory.SortClause.ClausePosition.ONE);
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
            QueryFactory.LinkEntity linkEntityOwner = new QueryFactory.LinkEntity("systemuser", "systemuserid", "owninguser", "a_1124b4bdf013df11a16e00155d7aa40d");
            linkEntityOwner.addColumn(new QueryFactory.EntityColumn("territoryid"));
            linkEntityOwner.addColumn(new QueryFactory.EntityColumn("employeeid"));
            factory.addLinkEntity(linkEntityOwner);


            // Filter conditions
            QueryFactory.Filter.FilterCondition condition1 = new QueryFactory.Filter
                    .FilterCondition("goalstartdate", QueryFactory.Filter.Operator.IN_FISCAL_YEAR, Integer.toString(yearNum));
            QueryFactory.Filter.FilterCondition condition2 = new QueryFactory.Filter
                    .FilterCondition("goalenddate", QueryFactory.Filter.Operator.IN_FISCAL_YEAR, Integer.toString(yearNum));
            QueryFactory.Filter.FilterCondition condition3 = new QueryFactory.Filter
                    .FilterCondition("title", QueryFactory.Filter.Operator.NOT_CONTAINS, "month");
            QueryFactory.Filter.FilterCondition condition4 = new QueryFactory.Filter
                    .FilterCondition("goalownerid", QueryFactory.Filter.Operator.EQUALS, repid);

            ArrayList<QueryFactory.Filter.FilterCondition> conditions = new ArrayList<>();
            conditions.add(condition1);
            conditions.add(condition2);
            conditions.add(condition3);
            conditions.add(condition4);

            // Set filter
            QueryFactory.Filter filter = new QueryFactory.Filter(AND, conditions);
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
            QueryFactory.LinkEntity linkEntityOwner = new QueryFactory.LinkEntity("systemuser", "systemuserid", "owninguser", "a_1124b4bdf013df11a16e00155d7aa40d");
            linkEntityOwner.addColumn(new QueryFactory.EntityColumn("territoryid"));
            linkEntityOwner.addColumn(new QueryFactory.EntityColumn("employeeid"));
            factory.addLinkEntity(linkEntityOwner);


            // Filter conditions
            QueryFactory.Filter.FilterCondition condition1 = new QueryFactory.Filter
                    .FilterCondition("title", QueryFactory.Filter.Operator.CONTAINS, "month");
            QueryFactory.Filter.FilterCondition condition2 = new QueryFactory.Filter
                    .FilterCondition("goalownerid", QueryFactory.Filter.Operator.EQUALS, repid);
            QueryFactory.Filter.FilterCondition condition3 = new QueryFactory.Filter
                    .FilterCondition("msus_fiscalfirstdayofmonth", QueryFactory.Filter.Operator.IN_FISCAL_YEAR, Integer.toString(yearNum));
            QueryFactory.Filter.FilterCondition condition4 = new QueryFactory.Filter
                    .FilterCondition("msus_fiscalfirstdayofmonth", QueryFactory.Filter.Operator.IN_FISCAL_PERIOD, Integer.toString(monthNum));

            ArrayList<QueryFactory.Filter.FilterCondition> conditions = new ArrayList<>();
            conditions.add(condition1);
            conditions.add(condition2);
            conditions.add(condition3);
            conditions.add(condition4);

            // Set filter
            QueryFactory.Filter filter = new QueryFactory.Filter(AND, conditions);
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
            QueryFactory.LinkEntity linkEntityOwner = new QueryFactory.LinkEntity("systemuser", "systemuserid", "owninguser", "a_1124b4bdf013df11a16e00155d7aa40d");
            linkEntityOwner.addColumn(new QueryFactory.EntityColumn("territoryid"));
            linkEntityOwner.addColumn(new QueryFactory.EntityColumn("employeeid"));
            factory.addLinkEntity(linkEntityOwner);

            // Filter conditions
            QueryFactory.Filter.FilterCondition condition1 = new QueryFactory.Filter
                    .FilterCondition("goalstartdate", QueryFactory.Filter.Operator.IN_FISCAL_YEAR,
                        Integer.toString(yearNum));
            QueryFactory.Filter.FilterCondition condition2 = new QueryFactory.Filter
                    .FilterCondition("goalenddate", QueryFactory.Filter.Operator.IN_FISCAL_YEAR,
                        Integer.toString(yearNum));
            QueryFactory.Filter.FilterCondition condition3 = new QueryFactory.Filter
                    .FilterCondition("title", QueryFactory.Filter.Operator.NOT_CONTAINS, "month");
            QueryFactory.Filter.FilterCondition condition4 = new QueryFactory.Filter
                    .FilterCondition("msus_region", QueryFactory.Filter.Operator.EQUALS, regionid);

            ArrayList<QueryFactory.Filter.FilterCondition> conditions = new ArrayList<>();
            conditions.add(condition1);
            conditions.add(condition2);
            conditions.add(condition3);
            conditions.add(condition4);

            // Set filter
            QueryFactory.Filter filter = new QueryFactory.Filter(AND, conditions);
            factory.setFilter(filter);

            // Sort clause
            QueryFactory.SortClause sortClause = new QueryFactory.SortClause("percentage", true, QueryFactory.SortClause.ClausePosition.ONE);
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
            QueryFactory.LinkEntity linkEntityOwner = new QueryFactory.LinkEntity("systemuser", "systemuserid", "owninguser", "a_1124b4bdf013df11a16e00155d7aa40d");
            linkEntityOwner.addColumn(new QueryFactory.EntityColumn("territoryid"));
            linkEntityOwner.addColumn(new QueryFactory.EntityColumn("employeeid"));
            factory.addLinkEntity(linkEntityOwner);

            // Create filter conditions
            QueryFactory.Filter.FilterCondition condition1 = new QueryFactory.Filter
                    .FilterCondition("title", QueryFactory.Filter.Operator.CONTAINS, "month");
            QueryFactory.Filter.FilterCondition condition2 = new QueryFactory.Filter
                    .FilterCondition("msus_region", QueryFactory.Filter.Operator.EQUALS, regionid);
            QueryFactory.Filter.FilterCondition condition3 = new QueryFactory.Filter
                    .FilterCondition("msus_fiscalfirstdayofmonth",
                        QueryFactory.Filter.Operator.IN_FISCAL_YEAR, Integer.toString(yearNum));
            QueryFactory.Filter.FilterCondition condition4 = new QueryFactory.Filter
                    .FilterCondition("msus_fiscalfirstdayofmonth",
                        QueryFactory.Filter.Operator.IN_FISCAL_PERIOD, Integer.toString(monthNum));

            // Add filter conditions
            ArrayList<QueryFactory.Filter.FilterCondition> conditions = new ArrayList<>();
            conditions.add(condition1);
            conditions.add(condition2);
            conditions.add(condition3);
            conditions.add(condition4);

            // Create and set filter
            QueryFactory.Filter filter = new QueryFactory.Filter(AND, conditions);
            factory.setFilter(filter);

            // Sort clause
            QueryFactory.SortClause sortClause = new QueryFactory.SortClause("percentage", true, QueryFactory.SortClause.ClausePosition.ONE);
            factory.addSortClause(sortClause);

            // Build query
            String query = factory.construct();

            return query;
        }

    }

    public static class Addresses {

        public static String getAllAccountAddresses() {
            // Instantiate a new constructor for the case entity and add the columns we want to see
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

            ArrayList<QueryFactory.Filter.FilterCondition> conditions = new ArrayList<>();
            QueryFactory.Filter.FilterCondition condition1 = new QueryFactory.Filter.FilterCondition
                    ("msus_loc_updated", QueryFactory.Filter.Operator.EQUALS, "true");
            conditions.add(condition1);
            QueryFactory.Filter filter = new QueryFactory.Filter(AND, conditions);

            query.setFilter(filter);

            // Spit out the encoded query
            String rslt = query.construct();
            return rslt;
        }

    }

    public static class TripAssociation {
        public static String getAssociationsByAccount(String accountid) {

            // Query columns
            QueryFactory factory = new QueryFactory("msus_mileageassociation");
            factory.addColumn("msus_name");
            factory.addColumn("createdon");
            factory.addColumn("msus_associated_trip");
            factory.addColumn("msus_associated_opportunity");
            factory.addColumn("msus_associated_account");
            factory.addColumn("msus_mileageassociationid");

            // Filter conditions
            QueryFactory.Filter.FilterCondition condition1 = new QueryFactory.Filter
                    .FilterCondition("msus_associated_trip", QueryFactory.Filter.Operator.EQUALS,
                    accountid);

            ArrayList<QueryFactory.Filter.FilterCondition> conditions = new ArrayList<>();
            conditions.add(condition1);

            // Set filter
            QueryFactory.Filter filter = new QueryFactory.Filter(AND, conditions);
            factory.setFilter(filter);

            // Build query
            String query = factory.construct();

            return query;
        }
    }






































}
