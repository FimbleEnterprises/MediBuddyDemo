package com.fimbleenterprises.medimileage;

import com.google.gson.Gson;

import java.util.ArrayList;

import androidx.annotation.Nullable;

public class BasicEntity {

    public Object baseEntity;
    public EntityStatusReason entityStatusReason;
    public ArrayList<EntityStatusReason> availableEntityStatusReasons = new ArrayList<>();
    public ArrayList<EntityBasicField> fields = new ArrayList<>();

    public BasicEntity() {

    }

    public BasicEntity(Object baseEntity) {
        this.baseEntity = baseEntity;
    }

    public BasicEntity(String gsonString) {
        Gson gson = new Gson();
        BasicEntity fromGson = gson.fromJson(gsonString, this.getClass());
        this.fields = fromGson.fields;
        this.availableEntityStatusReasons = fromGson.availableEntityStatusReasons;
        this.entityStatusReason = fromGson.entityStatusReason;
        this.baseEntity = fromGson.baseEntity;
    }

    public boolean hasStatusValue() {
        return this.entityStatusReason != null;
    }

    public void setAccount(CrmEntities.Accounts.Account account) {
        for (EntityBasicField field : this.fields) {
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

    public String[] toStatusReasonsArray() {
        String[] vals = new String[this.availableEntityStatusReasons.size()];
        for(int i = 0; i < this.availableEntityStatusReasons.size(); i++) {
            vals[i] = this.availableEntityStatusReasons.get(i).statusReasonText.toString();
        }
        return vals;
    }

    public int getStatusReasonIndex() {
        for (int i = 0; i < this.availableEntityStatusReasons.size(); i++) {
            EntityStatusReason reason = this.availableEntityStatusReasons.get(i);
            if (reason.statusReasonText.equals(this.entityStatusReason.statusReasonText)) {
                return i;
            }
        }
        return 0;
    }

    public EntityStatusReason getStatusReasonFromAvailable() {
        for (int i = 0; i < this.availableEntityStatusReasons.size(); i++) {
            EntityStatusReason reason = this.availableEntityStatusReasons.get(i);
            if (reason.statusReasonText.equals(this.entityStatusReason.statusReasonText)) {
                return reason;
            }
        }
        return null;
    }

    public static class EntityStatusReason {
        public String requiredState;
        public String statusReasonValue;
        public String statusReasonText;

        public EntityStatusReason(String statusReasonText, String statusReasonValue, String requiredState) {
            this.requiredState = requiredState;
            this.statusReasonValue = statusReasonValue;
            this.statusReasonText = statusReasonText;
        }

        @Override
        public String toString() {
            try {
                return this.statusReasonText;
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }

    }

    public static class EntityBasicField {

        String label;
        String value;
        boolean showLabel = false;
        boolean isBold = false;
        boolean isEditable = false;
        boolean isAccountField = false;
        boolean isContactField = false;
        boolean isOptionSet = false;
        boolean isReadOnly = false;
        boolean isDateField = false;
        boolean isNumber = false;
        boolean isDateTimeField = false;
        String crmFieldName;
        ArrayList<OptionSetValue> optionSetValues = new ArrayList<>();
        ArrayList<EntityStatusReason> entityStatusReasons = new ArrayList<>();
        CrmEntities.Accounts.Account account;
        CrmEntities.Contacts.Contact contact;

        public EntityBasicField(String label) {
            this.label = label;
        }

        public EntityBasicField(String label, String value) {
            this.label = label;
            this.value = value;
            this.showLabel = true;
        }

        public EntityBasicField(String label, String value, boolean isReadOnly) {
            this.label = label;
            this.value = value;
            this.showLabel = true;
            this.isReadOnly = isReadOnly;
        }

        public EntityBasicField(String label, String value, String crmFieldName) {
            this.label = label;
            this.value = value;
            this.showLabel = true;
            this.crmFieldName = crmFieldName;
        }

        public EntityBasicField(String label, String value, String crmFieldName, boolean isReadOnly) {
            this.label = label;
            this.value = value;
            this.showLabel = true;
            this.crmFieldName = crmFieldName;
            this.isReadOnly = isReadOnly;
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
         * and if a match is found, that optionset object is returned.
         * @return The OptionSet value that equals the object's "value" field.
         */
        public @Nullable
        EntityStatusReason tryGetStatusFromName() {
            try {
                for (EntityStatusReason value : this.entityStatusReasons) {
                    if (value.statusReasonText.equals(this.value)) {
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
                if (isOptionSet) {
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

        @Override
        public String toString() {
            try {
                return this.label + " | isReadOnly:" + this.isReadOnly + " | value: " + this.value;
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }

    }
}
