package com.fimbleenterprises.medimileage;

import static com.fimbleenterprises.medimileage.QueryFactory.Filter.FilterType.AND;

public class Queries {
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
