package com.fimbleenterprises.medimileage;

import android.util.Log;

import java.util.ArrayList;

import androidx.annotation.Nullable;

public class QueryFactory {
    private static final String TAG = "QueryFactory";
    private String entityName;
    private String preamble;
    ArrayList<EntityColumn> columns = new ArrayList<>();
    ArrayList<LinkEntity> linkEntities = new ArrayList<>();
    ArrayList<SortClause> sortClauses = new ArrayList<>();
    Filter filter;
    private boolean distinct = false;

    @Override
    public String toString() {
        return "Entity: " + this.entityName + ", Columns: " + this.columns.size() + ", Links: "
                + this.linkEntities.size();
    }

    public QueryFactory(String entity) {
        entityName = entity;
        this.columns = new ArrayList<>();
        setEntity(entity);
    }

    public QueryFactory(String entity, ArrayList<EntityColumn> columns) {
        this.entityName = entity;
        this.columns = columns;
        setEntity(entity);
    }

    public QueryFactory(String entity, ArrayList<EntityColumn> columns, Filter filter) {
        this.entityName = entity;
        this.columns = columns;
        this.filter = filter;
        setEntity(entity);
    }

    public QueryFactory(String entity, ArrayList<EntityColumn> columns, Filter filter,
                        ArrayList<LinkEntity> linkEntities) {
        this.entityName = entity;
        this.columns = columns;
        this.filter = filter;
        this.linkEntities = linkEntities;
        setEntity(entity);
    }

    public void addSortClause(SortClause clause) {
        if (sortClauses == null) {
            sortClauses = new ArrayList<>();
        }
        switch (clause.clausePosition) {
            case ONE:
                if (sortClauses.size() > 0) {
                    sortClauses.remove(0);
                }
                sortClauses.add(0, clause);
                break;
            case TWO:
                if (sortClauses.size() == 1) {
                    sortClauses.add(1, clause);
                } else if (sortClauses.size() == 0) {
                    sortClauses.add(0, clause);
                } else {
                    sortClauses.remove(1);
                    sortClauses.add(1, clause);
                    break;
                }
        }
    }

    public void isDistinct(boolean isDistinct) {
        this.distinct = isDistinct;
        setEntity(this.entityName);
    }

    public String construct() {
        String columnsString = "";
        String orderClausesString = "";
        String filterString = "";
        String linkEntitiesString = "";

        for (EntityColumn column : this.columns) {
            columnsString += column.toEncodedString();
        }

        if (sortClauses != null && sortClauses.size() > 0) {
            for (SortClause clause : sortClauses) {
                orderClausesString += clause.toEncodedString();
            }
        }

        if (filter != null) {
            filterString = filter.toEncodedString();
        }

        if (linkEntities != null && linkEntities.size() > 0) {
            for (LinkEntity link : linkEntities) {
                linkEntitiesString += link.toEncodedString();
            }
        }

        String query = this.preamble + columnsString + orderClausesString + filterString
                + linkEntitiesString + "%3C%2Fentity%3E%3C%2Ffetch%3E";

        return query;
    }

    private void setEntity(String entity) {
        this.entityName = entity;

        preamble = "/api/data/v8.2/" + ENTITY_NAMES.getPluralEntityName(entity)
                + "?fetchXml=%3Cfetch%20version%3D%221.0%22%20output-format%3D%22xml-platform%22%20mapping%3D%22logical%22%20distinct%3D%22" + Boolean.toString(distinct) + "%22%3E%3Centity%20name%3D%22" + entity + "%22%3E";
    }

    public void addLinkEntity(LinkEntity linkEntity) {
        this.linkEntities.add(linkEntity);
    }

    public void addLinkEntity(ArrayList<LinkEntity> linkEntities) {
        this.linkEntities = linkEntities;
    }

    public String entity() {
        return entityName;
    }

    public void addColumn(String columnName) {
        this.columns.add(new EntityColumn(columnName));
    }

    public void addColumns(ArrayList<String> columns) {
        for (String string : columns) {
            this.columns.add(new EntityColumn(string));
        }
    }

    public void removeColumn(int position) {
        columns.remove(position);
    }

    public void removeColumn(String columnName) {
        try {
            for (int i = 0; i < columns.size(); i++) {
                EntityColumn column = columns.get(i);
                if (column.columnName.equals(columnName)) {
                    columns.remove(i);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public static class SortClause {

        @Override
        public String toString() {
            return this.clausePosition + ", Descending: " + this.isDescending;
        }

        public enum ClausePosition {
            ONE, TWO;
        }

        String attribute;
        boolean isDescending = false;
        ClausePosition clausePosition;

        public SortClause(String attribute, boolean isDescending, ClausePosition position) {
            this.attribute = attribute;
            this.isDescending = isDescending;
            clausePosition = position;
        }

        public String toEncodedString() {
            String encoded = "%3Corder%20attribute%3D%22" + attribute + "%22%20descending%3D%22" + isDescending + "%22%20%2F%3E";
            return encoded;
        }
    }

    public static class EntityColumn {
        String columnName;

        public EntityColumn(String columnName) {
            this.columnName = columnName;
        }

        public String toEncodedString() {
            return "%3Cattribute%20name%3D%22" + this.columnName + "%22%20%2F%3E";
        }

        @Override
        public String toString() {
            return this.columnName;
        }
    }

    public static class LinkEntity {
        String entityName;
        String from;
        String to;
        String alias;
        String fullString;
        Filter filter;
        ArrayList<EntityColumn> columns = new ArrayList<>();

        @Override
        public String toString() {
            return this.entityName + " , From: " + this.from + " To: " + this.to
                    + ", Columns(" + this.columns.size() + ")";
        }

        public LinkEntity() {

        }

        /**
         * Creates a new LinkEntity
         * @param fullString The full link entity text (from, to, alias etc.)
         */
        public LinkEntity(String fullString) {
            this.fullString = fullString;
            int firstContact = fullString.lastIndexOf("alias=");
            String subString = fullString.substring(firstContact);
            alias = subString.replace("\"", "");
            Log.i(TAG, "LinkEntity Alias = " + subString);
        }

        public LinkEntity(String entityName, String from, String to, String alias) {
            this.entityName = entityName;
            this.from = from;
            this.to = to;
            this.alias = alias;
        }

        public LinkEntity(String entityName, String from, String to, String alias, Filter filter) {
            this.entityName = entityName;
            this.from = from;
            this.to = to;
            this.alias = alias;
        }

        public LinkEntity(String entityName, String from, String to, String alias, Filter filter, EntityColumn column) {
            this.entityName = entityName;
            this.from = from;
            this.to = to;
            this.alias = alias;
            this.filter = filter;
            this.addColumn(column);
        }

        public LinkEntity(String entityName, String from, String to, String alias, Filter filter, ArrayList<EntityColumn> columns) {
            this.entityName = entityName;
            this.from = from;
            this.to = to;
            this.alias = alias;
            this.filter = filter;
            for (EntityColumn column : columns) {
                this.addColumn(column);
            }
        }

        public void addFilter(Filter filter) {
            this.filter = filter;
        }

        public void removeFilter() {
            this.filter = null;
        }

        public void removeColumn(int index) {
            try {
                columns.remove(index);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void addColumn(EntityColumn column) {

            this.columns.add(column);
        }

        public void addColumns(ArrayList<EntityColumn> columns) {
            this.columns = columns;
        }

        public String toEncodedString() {

            String encoded = "";

            if (fullString != null) {
                encoded = "%3Clink-entity%20" + this.fullString + "%3E";
            } else {

                encoded = "%3Clink-entity%20name%3D%22" + this.entityName + "%22%20 " +
                        " from%3D%22" + this.from + "%22%20 " +
                        " to%3D%22" + this.to + "%22%20 " +
                        " alias%3D%22" + this.alias + "%22%3E";
            }

            String columns = "";
            if (this.columns.size() > 0) {
                for (EntityColumn column : this.columns) {
                    columns += column.toEncodedString();
                }
            }

            encoded += columns;

            if (filter != null) {
                encoded += filter.toEncodedString();
            }

            encoded += "%3C%2Flink-entity%3E";
            return encoded;
        }

    }

    public static class Filter {
        private FilterType type;
        ArrayList<FilterCondition> conditions;

        @Override
        public String toString() {
            return "Type: " + this.type.name();
        }

        public Filter() {
            conditions = new ArrayList<>();
        }

        public Filter(FilterType type) {
            conditions = new ArrayList<>();
            this.type = type;
        }

        public Filter(FilterType type, FilterCondition condition) {
            this.type = type;
            this.conditions = new ArrayList<>();
            this.conditions.add(condition);
        }

        public Filter(FilterType type, ArrayList<FilterCondition> conditions) {
            this.type = type;
            this.conditions = conditions;
        }

        public void addCondition(FilterCondition condition) {
            conditions.add(condition);
        }

        public enum FilterType {
            AND, OR;
        }

        public String getType() {
            switch (type) {
                case AND:
                    return "and";
                case OR:
                    return "or";
            }
            return "and";
        }

        public static class Operator {
            public static final String EQUALS = "eq";
            public static final String NOT_EQUALS = "ne";
            public static final String GREATER_THAN_OR_EQUAL_TO = "ge";
            public static final String LESS_THAN_OR_EQUAL_TO = "le";
            public static final String LESS_THAN = "lt";
            public static final String GREATER_THAN = "gt";
            public static final String ON_OR_AFTER = "on-or-after";
            public static final String ON_OR_BEFORE = "on-or-before";
            public static final String TODAY = "today";
            public static final String YESTERDAY = "yesterday";
            public static final String LAST_X_DAYS = "last-x-days";
            public static final String THIS_WEEK = "this-week";
            public static final String LAST_WEEK = "last-week";
            public static final String LAST_X_WEEKS = "last-x-weeks";
            public static final String THIS_MONTH = "this-month";
            public static final String LAST_MONTH = "last-month";
            public static final String LAST_X_MONTHS = "last-x-months";
            public static final String THIS_YEAR = "this-year";
            public static final String LAST_YEAR = "last-year";
            public static final String LAST_X_YEARS = "last-x-years";
            public static final String CONTAINS = "like";
            public static final String NOT_CONTAINS = "not-like";
            public static final String CONTAINS_DATA = "not-null";
            public static final String NOT_CONTAINS_DATA = "con";
            public static final String ENDS_WITH = "endswith";
            public static final String STARTS_WITH = "startswith";
            public static final String IN_FISCAL_YEAR = "in-fiscal-year";
            public static final String IN_FISCAL_PERIOD = "in-fiscal-period";

            Operands operand;
            public Operator(Operands operand) {
                this.operand = operand;
            }

            enum Operands {
                EQUALS, NOT_EQUALS, GREATER_THAN_OR_EQUAL_TO, LESS_THAN_OR_EQUAL_TO, LESS_THAN,
                GREATER_THAN, ON_OR_AFTER, ON_OR_BEFORE, TODAY, THIS_WEEK, THIS_MONTH, THIS_YEAR,
                CONTAINS, NOT_CONTAINS, CONTAINS_DATA, NOT_CONTAINS_DATA, ENDS_WITH, STARTS_WITH,
                AND, OR, IN_FISCAL_PERIOD, IN_FISCAL_YEAR;
            }

            public String getOperator() {
                return Operator.get(this.operand);
            }

            public static String get(Operands operand) {
                switch (operand) {
                    case EQUALS:
                        return " eq ";
                    case NOT_EQUALS:
                        return " ne ";
                    case GREATER_THAN_OR_EQUAL_TO:
                        return " ge ";
                    case LESS_THAN_OR_EQUAL_TO:
                        return " le ";
                    case LESS_THAN:
                        return " lt ";
                    case GREATER_THAN:
                        return " gt ";
                    case ON_OR_AFTER:
                        return " on-or-after ";
                    case ON_OR_BEFORE:
                        return " on-or-before ";
                    case TODAY:
                        return " today ";
                    case THIS_WEEK:
                        return " this-week ";
                    case THIS_MONTH:
                        return " this-month ";
                    case THIS_YEAR:
                        return " this-year ";
                    case CONTAINS:
                        return " like ";
                    case NOT_CONTAINS:
                        return "not-like";
                    case CONTAINS_DATA:
                        return " ne null ";
                    case NOT_CONTAINS_DATA:
                        return " con ";
                    case ENDS_WITH:
                        return " endswith ";
                    case STARTS_WITH:
                        return " startswith ";
                    default:
                        return " eq ";
                }
            }
        }

        public static class FilterCondition  {
            String attribute;
            private Operator operator;
            private String strOperator;
            String value;
            String uiType;

            @Override
            public String toString() {
                return "Field: " + this.attribute + ", Operator: " + this.getStrOperator() + ", Value: " + value;
            }

            public FilterCondition(String attribute, Operator operator, String value) {
                this.attribute = attribute;
                this.operator = operator;
                this.value = value;
            }

            public FilterCondition(String attribute, Operator operator, String value, String uiType) {
                this.attribute = attribute;
                this.operator = operator;
                this.value = value;
                this.uiType = uiType;
            }

            public FilterCondition(String attribute, String operator, @Nullable String value) {
                this.attribute = attribute;
                this.strOperator = operator;
                this.value = value;
            }

            public FilterCondition(String attribute, String operator) {
                this.attribute = attribute;
                this.strOperator = operator;
            }

            public FilterCondition(String attribute, String operator, String value, String uiType) {
                this.attribute = attribute;
                this.strOperator = operator;
                this.value = value;
                this.uiType = uiType;
            }

            private String getStrOperator() {
                return this.operator == null ? strOperator : this.operator.getOperator();
            }

            public String toEncodedString() {
                String encoded;
                String uiString = (this.uiType == null) ? "" : "uitype%3D%22" + this.uiType + "%22%20";

                // If the operator is, "Contains" or "Not Contains" then surround the value with the % character)
                if (this.getStrOperator().equals("like") || this.getStrOperator().equals("not-like")) {
                    this.value = "%25" + this.value + "%25";
                }

                String valueString = (this.value == null) ? "" : "value%3D%22" + this.value + "%22%20";

                encoded = "%3Ccondition%20"
                        + "attribute%3D%22" + this.attribute + "%22%20"
                        + "operator%3D%22" + getStrOperator() + "%22%20"
                        + uiString
                        + valueString
                        + "%2F%3E";

                return encoded;
            }
        }

        public String toEncodedString() {
            String conditions = "";
            for (FilterCondition filterCondition : this.conditions) {
                conditions += filterCondition.toEncodedString();
            }
            String encoded = "%3Cfilter%20type%3D%22" + getType() + "%22%3E" + conditions + "%3C%2Ffilter%3E";
            return encoded;
        }
    }

    public static class ENTITY_GUIDS {
        public static final String MEDIBUDDY_OBJECT = "8A87C6A2-82B7-E811-80E8-005056A36B9B";
        public static final String MEDISTIMUSA_BUSINESSUNIT_ENTITY_GUID = "8B31B2C2-E519-E711-80D2-005056A36B9B";
        public static final String FCM_TOKENS = "783b425c-4ace-e811-80e9-005056a36b9b";
    }

    public static class ENTITY_NAMES {
        public static final String ACCOUNT = "account";
        public static final String CASE = "incident";
        public static final String NOTE = "annotation";
        public static final String CUSTOMER_INVENTORY = "col_customerinventory";
        public static final String TERRITORY = "territory";
        public static final String CONTACT = "contact";
        public static final String ONHAND_INVENTORY = "msus_onhandinventory";
        public static final String ORDER_LINES = "salesorderdetail";
        public static final String FULL_TRIP = "msus_fulltrip";
        public static final String MEDIBUDDY_OBJECT = "msus_medibuddy_object";
        public static final String MEDIA_ITEM = "msus_medibuddy_media_item";
        public static final String USER = "systemuser";
        public static final String FCM_TOKEN = "msus_firebasetoken";
        private static final String OPPORTUNITIY = "opportunity";
        public static final String SALESLITERATURE = "salesliterature";
        public static final String SALESLITERATUREITEM = "salesliteratureitem";

        public static String getPrettyName(String entityLogicalName) {
            if (entityLogicalName.equals(ACCOUNT)) {
                return "Account";
            } else if (entityLogicalName.equals(CASE)) {
                return "Ticket/Case";
            } else if (entityLogicalName.equals(CUSTOMER_INVENTORY)) {
                return "Customer inventory";
            } else if (entityLogicalName.equals(CONTACT)) {
                return "Contact";
            } else if (entityLogicalName.equals(ONHAND_INVENTORY)) {
                return "On-hand Inventory";
            } else if (entityLogicalName.equals(OPPORTUNITIY)) {
                return "Opportunity";
            } else {
                return entityLogicalName;
            }
        }

        public static String getPluralEntityName(String entityLogicalName) {
            if (entityLogicalName.equals(ENTITY_NAMES.CUSTOMER_INVENTORY)) {
                return "col_customerinventories";
            } else if (entityLogicalName.equals(ENTITY_NAMES.OPPORTUNITIY)) {
                return "opportunities";
            }  else if (entityLogicalName.equals(ENTITY_NAMES.SALESLITERATURE)) {
                return "salesliteratures";
            } else if (entityLogicalName.equals(ENTITY_NAMES.SALESLITERATUREITEM)) {
                return "salesliteratureitems";
            } else if (entityLogicalName.equals(ENTITY_NAMES.TERRITORY)) {
                return "territories";
            } else {
                return entityLogicalName + "s";
            }
        }
    }

    public static class LINK_ENTITY_STRINGS {
        public static final String CASES_TO_ACCOUNT = "name=\"account\"%20from=\"accountid\"%20to=\"customerid\"%20alias=\"al\" ";
        public static final String CASES_TO_ACCOUNT_WITH_ACCOUNTNUMBER = "name=\"account\" from=\"accountid\" to=\"customerid\" visible=\"false\" link-type=\"outer\" alias=\"a_4b5945b8a4a64613afc1ae1d5e6828c7\"";
        public static final String CONTACTS_TO_ACCOUNT = "name=\"account\" from=\"accountid\" to=\"parentcustomerid\" visible=\"false\" link-type=\"outer\" alias=\"a_dc9b80f8c78146d89fd6a3b610836975\"";
        public static final String ORDERS_TO_ACCOUNT = "name=\"account\" from=\"accountid\" to=\"customerid\" visible=\"false\" link-type=\"outer\" alias=\"a_92b77a4070bd4d1a82d9fa6ce38df2cc\"";
        public static final String ORDERS_TO_ORDERLINES = "name=\"salesorder\" from=\"salesorderid\" to=\"salesorderid\" visible=\"false\" link-type=\"outer\" alias=\"a_6ec0e72e4c104394bc627456c6412838\"";
        public static final String CUSTOMER_INV_TO_ACCOUNT = "name=\"account\" from=\"accountid\" to=\"col_accountid\" visible=\"false\" link-type=\"outer\" alias=\"a_92f7efedfc19e71180d2005056a36b9b\"";
        public static final String ONHAND_TO_ONHAND_TOTALS = "name=\"msus_onhandinventorytotals\" from=\"msus_onhandinventorytotalsid\" to=\"msus_onhandinventory_totals_guid\" visible=\"false\" link-type=\"outer\" alias=\"a_f54bea7d41e3e71180dc005056a36b9b\"";
        public static final String ORDERLINES_TO_PRODUCT = "name=\"product\" from=\"productid\" to=\"productid\" visible=\"false\" link-type=\"outer\" alias=\"a_070ef9d142cd40d98bebd513e03c7cd1\"";
        public static final String LITERATURE_ITEM_TO_ATTACHMENT = "name=\"salesliterature\" from=\"salesliteratureid\" to=\"salesliteratureid\" visible=\"false\" link-type=\"outer\" alias=\"a_020b4d04b5044c5eafe22a8353c87dd5\"";
        public static final String ORDER_TO_ACCOUNT = "name=\"account\" from=\"accountid\" to=\"msus_customerid_override\" visible=\"false\" link-type=\"outer\" alias=\"aa\"";
        public static final String CONTRACT_TO_ACCOUNT = "name=\"account\" from=\"accountid\" to=\"customerid\" visible=\"false\" link-type=\"outer\" alias=\"a_6984df23c1fb4ec4be29e6afd19a145d\"";
        public static final String TERRITORY_TO_USER = "name=\"territory\" from=\"territoryid\" to=\"territoryid\" visible=\"false\" link-type=\"outer\" alias=\"a_4484069205d044a7bee3fb52c273d285\"";
        public static final String MANAGER_TO_USER = "name=\"systemuser\" from=\"systemuserid\" to=\"parentsystemuserid\" visible=\"false\" link-type=\"outer\" alias=\"a_af5d32e9bffd4f9ea21ec50f8501a7ea\"";
    }

// QueryFactory
}
