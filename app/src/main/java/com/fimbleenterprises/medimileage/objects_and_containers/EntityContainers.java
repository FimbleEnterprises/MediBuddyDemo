package com.fimbleenterprises.medimileage.objects_and_containers;


import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * This class is used for representing entity fields in an entity.
 */
public class EntityContainers {

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

    public static EntityContainers fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, EntityContainers.class);
    }

    /**
     * This class should be used when creating a note in CRM.  It is a specialized entity container
     * that can be deserialized by the C# API receiving the request.  Note creation is slightly
     * different than traditional entity creation and this class simplifies the handling of those peculiarities.
     */
    public static class AnnotationCreationContainer {

        public String annotaionid;
        public String notetext;
        public String subject;
        public String documentbody;
        public String filename;
        public boolean isdocument;
        public String mimetype;
        public String objectid;
        public String objectidtypecode;
        public String ownerid;
        public String owneridtype;

        public AnnotationCreationContainer() {
            this.ownerid = MediUser.getMe().systemuserid;
        }

        public String toJson() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }

        /**
         * Converts this EntityContainer representation of an annotation to a CrmEntity representation of an annotation.
         * @return A well enoughly populated CrmEntity, Annotation object.
         */
        public CrmEntities.Annotations.Annotation toNoteObject() {
            CrmEntities.Annotations.Annotation newNote = new CrmEntities.Annotations.Annotation();
            newNote.subject = this.subject;
            newNote.notetext = this.notetext;
            newNote.isDocument = this.isdocument;
            newNote.objectEntityName = this.objectidtypecode;
            newNote.objectid = this.objectid;
            return newNote;
        }
    }

}