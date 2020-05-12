package com.fimbleenterprises.medimileage;


import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * This class is used for representing entity fields in an entity.
 */
public class Containers {

    public ArrayList<EntityContainer> entityContainers = new ArrayList<EntityContainer>();

    @Override
    public String toString() {
        return "[entityContainers = "+ entityContainers +"]";
    }

    public static class EntityContainer {

        public ArrayList<EntityField> entityFields = new ArrayList<EntityField>();

        public EntityContainer() { }

        @Override
        public String toString() {
            return "[entityFields = "+entityFields+"]";
        }

        public String toJson() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }
    }

    public static class EntityField {
        public String name;
        public String value;

        public EntityField(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return "[name = "+name+", value = "+value+"]";
        }
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Containers fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Containers.class);
    }
}