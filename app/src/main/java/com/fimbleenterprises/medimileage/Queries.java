package com.fimbleenterprises.medimileage;

import java.util.ArrayList;

import static com.fimbleenterprises.medimileage.Queries.Operators.getDateOperator;
import static com.fimbleenterprises.medimileage.QueryFactory.Filter.FilterType.AND;

public class Queries {

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

            // Spit out the encoded query
            String rslt = query.construct();
            return rslt;
        }
    }

    public static class OrderLines {

        public static String getOrderLines(String repid, Operators.DateOperator operator) {

            /************** TESTING *************/
             repid = "E2A46FDF-5B7C-E711-80D1-005056A32EEA";
            /************************************/


            // Order line columns
            QueryFactory factory = new QueryFactory("salesorderdetail");
            factory.addColumn("productid");
            factory.addColumn("priceperunit");
            factory.addColumn("quantity");
            factory.addColumn("extendedamount");
            factory.addColumn("salesorderdetailid");

            // Account entity link
            QueryFactory.LinkEntity linkEntityAccount = new QueryFactory.LinkEntity("account","accountid", "new_customer", "a_db24f99da8fee71180df005056a36b9b");
            linkEntityAccount.columns.add(new QueryFactory.EntityColumn("msus_salesrep"));
            linkEntityAccount.columns.add(new QueryFactory.EntityColumn("name"));
            linkEntityAccount.columns.add(new QueryFactory.EntityColumn("accountnumber"));
            QueryFactory.Filter.FilterCondition accountCondition = new QueryFactory.Filter.FilterCondition("msus_salesrep", QueryFactory.Filter.Operator.EQUALS, repid);
            linkEntityAccount.addFilter(new QueryFactory.Filter(AND, accountCondition));
            factory.linkEntities.add(linkEntityAccount);

            // Order entity link
            QueryFactory.LinkEntity linkEntitySalesOrder = new QueryFactory.LinkEntity("salesorder","salesorderid", "salesorderid", "ac");
            linkEntitySalesOrder.columns.add(new QueryFactory.EntityColumn("submitdate"));
            QueryFactory.Filter.FilterCondition orderCondition = new QueryFactory.Filter.FilterCondition("submitdate", getDateOperator(operator));
            linkEntitySalesOrder.addFilter(new QueryFactory.Filter(AND, orderCondition));
            factory.linkEntities.add(linkEntitySalesOrder);

            // Product entity link
            QueryFactory.LinkEntity linkEntityProduct = new QueryFactory.LinkEntity("product","productid", "productid", "a_070ef9d142cd40d98bebd513e03c7cd1");
            linkEntityProduct.columns.add(new QueryFactory.EntityColumn("msus_is_capital"));
            linkEntityProduct.columns.add(new QueryFactory.EntityColumn("productnumber"));
            linkEntityProduct.columns.add(new QueryFactory.EntityColumn("name"));
            factory.linkEntities.add(linkEntityProduct);

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





































}
