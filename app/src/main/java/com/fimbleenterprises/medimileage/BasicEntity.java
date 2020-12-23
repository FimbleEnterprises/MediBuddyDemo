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

    public void setAccount(CrmEntities.Accounts.Account account) {
        for (EntityBasicField field : this.list) {
            if (field.isAccountField) {
                field.account = account;
                field.value = account.accountName;
            }
        }
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
        boolean isReadOnly = false;
        boolean isDateField = false;
        boolean isDateTimeField = false;
        String crmFieldName;
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

        public EntityContainers.EntityField toEntityField() {

            if (this.isOptionSet) {
                for (OptionSetValue value : this.optionSetValues) {
                    if (value.name.equals(this.value)) {
                        return new EntityContainers.EntityField(this.crmFieldName, value.value.toString());
                    }
                }
                return null;
            } else if (this.isAccountField) {
                return new EntityContainers.EntityField(this.crmFieldName, this.account.accountid);
            } else {
                return new EntityContainers.EntityField(this.crmFieldName, this.value);
            }
        }

        public String[] toOptionsetValueArray() {
            String[] vals = new String[this.optionSetValues.size()];
            for(int i = 0; i < this.optionSetValues.size(); i++) {
                vals[i] = this.optionSetValues.get(i).name.toString();
            }
            return vals;
        }

        /**
         * One of the values in an optionset
         */
        public static class OptionSetValue {
            String name;
            Object value;
            boolean isSelected = false;

            public OptionSetValue(String name, Object value) {
                this.name = name;
                this.value = value;
            }

        }

    }

}
