package com.fimbleenterprises.medimileage;

import com.google.gson.Gson;

import java.util.ArrayList;

import androidx.annotation.Nullable;

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
        boolean isEntityStatus = false;
        String crmFieldName;
        boolean isEntityid = false;
        ArrayList<OptionSetValue> optionSetValues = new ArrayList<>();
        ArrayList<StatusReason> statusReasons = new ArrayList<>();
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

        public String[] toStatusReasonsArray() {
            String[] vals = new String[this.statusReasons.size()];
            for(int i = 0; i < this.statusReasons.size(); i++) {
                vals[i] = this.statusReasons.get(i).statusReasonText.toString();
            }
            return vals;
        }

        /**
         * Using the object's current value property, all available optionset values are evaluated
         * and if a match is found, that optionset object is returned.
         * @return The OptionSet value that equals the object's "value" field.
         */
        public @Nullable OptionSetValue tryGetValueFromName() {
            try {
                for (OptionSetValue value : this.optionSetValues) {
                    if (value.name.equals(this.value)) {
                        return value;
                    }
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Using the object's current value property, all available optionset values are evaluated
         * and if a match is found, that optionset object is returned.  If the object is a status reason
         * then the status reasons will be parsed instead.
         * @return The OptionSet value that equals the object's "value" field.
         */
        public int tryGetValueIndexFromName() {
            try {
                if (isEntityStatus) {
                    for (int i = 0; i < this.statusReasons.size(); i++) {
                        StatusReason value = this.statusReasons.get(i);
                        if (value.statusReasonText.equals(this.value)) {
                            return i;
                        }
                    }
                } else if (isOptionSet) {
                    for (int i = 0; i < this.optionSetValues.size(); i++) {
                        OptionSetValue value = this.optionSetValues.get(i);
                        if (value.name.equals(this.value)) {
                            return i;
                        }
                    }
                } else {
                    return 0;
                }
                return 0;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
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

        public static class StatusReason {
            public String requiredState;
            public String statusReasonValue;
            public String statusReasonText;

            public StatusReason(String statusReasonText, String statusReasonValue, String requiredState) {
                this.requiredState = requiredState;
                this.statusReasonValue = statusReasonValue;
                this.statusReasonText = statusReasonText;
            }

        }


    }
}
