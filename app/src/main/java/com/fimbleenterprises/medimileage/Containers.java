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

    /**
     * This class should be used when creating a note in CRM.  It is a specialized entity container
     * that can be deserialized by the C# API receiving the request.  Note creation is slightly
     * different than traditional entity creation and this class simplifies the handling of those particularities.
     */
    public static class Annotation {
        public String annotaionid;
        public String notetext;
        public String subject;
        public String documentbody;
        public String filename;
        public boolean isdocument;
        public String mimetype;
        public String objectid;
        public String objecttypecode;
        public String ownerid;
        public String owneridtype = "systemuser";

        public String toJson() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }
    }

}