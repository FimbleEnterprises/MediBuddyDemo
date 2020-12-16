package com.fimbleenterprises.medimileage;

import com.google.gson.Gson;

import java.util.ArrayList;

public class BasicEntity {

    public Object object;
    public ArrayList<BasicEntityField> list = new ArrayList<>();

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

    public static class BasicEntityField {

        String label;
        String value;
        boolean showLabel = false;
        boolean isBold = false;
        boolean isEditable = false;
        boolean isAccountField = false;
        CrmEntities.Accounts.Account account;

        public BasicEntityField(String value) {
            this.value = value;
        }

        public BasicEntityField(String label, String value) {
            this.label = label;
            this.value = value;
            this.showLabel = true;
        }

    }

}
