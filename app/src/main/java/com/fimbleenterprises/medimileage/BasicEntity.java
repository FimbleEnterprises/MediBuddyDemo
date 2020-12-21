package com.fimbleenterprises.medimileage;

import com.google.gson.Gson;

import java.util.ArrayList;

public class BasicEntity {

    public Object object;
    public ArrayList<EntityBasicField> list = new ArrayList<>();

    public BasicEntity() {

    }

    public BasicEntity(Object object) {
        this.object = object;
    }

    public BasicEntity(String gsonString) {
        Gson gson = new Gson();
        BasicEntity fromGson = new BasicEntity();
        fromGson = gson.fromJson(gsonString, this.getClass());
        this.list = fromGson.list;
        this.object = fromGson.object;
    }

    public String toGson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static class EntityBasicField {

        String label;
        String value;
        boolean showLabel = false;
        boolean isBold = false;
        boolean isEditable = false;
        boolean isAccountField = false;
        boolean isOptionSet = false;
        ArrayList<OptionSetValue> optionSetValues = new ArrayList<>();
        CrmEntities.Accounts.Account account;


        public EntityBasicField(String label) {
            this.label = label;
        }

        public EntityBasicField(String label, String value) {
            this.label = label;
            this.value = value;
            this.showLabel = true;
        }

        public String[] toOptionsetValueArray() {
            String[] vals = new String[this.optionSetValues.size()];
            return this.optionSetValues.toArray(vals);
        }

        /**
         * One of the values in an optionset
         */
        public static class OptionSetValue {
            String name;
            Object value;

            public OptionSetValue(String name, Object value) {
                this.name = name;
                this.value = value;
            }

        }

    }

}
