package com.fimbleenterprises.medimileage.objects_and_containers;

import org.joda.time.DateTime;

import java.util.ArrayList;

public class EmailsOrAnnotations {

    public ArrayList<EmailOrAnnotation> list;

    public EmailsOrAnnotations() {
        this.list = new ArrayList<>();
    }

    public void sortByDate(boolean descending) {

    }

    /**
     * Checks the arraylist for an item bearing the supplied entity id.
     * @param entityid The entity id to look for.
     * @return True if found.
     */
    public boolean exists(String entityid) {
        for (EmailOrAnnotation item : this.list) {
            if (item.entityid.equals(entityid)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a new EmailOrAnnotation to the list.  If the item already exists then it is updated.
     * @param email
     */
    public void upsert(CrmEntities.Emails.Email email) {
        if (this.list.size() == 0) {
            this.list.add(new EmailOrAnnotation(email));
        } else {
            if (this.exists(email.entityid)) {
                for (int i = 0; i < this.list.size(); i++) {
                    if (this.list.get(i).entityid.equals(email.entityid)) {
                        this.list.remove(i);
                        this.list.add(new EmailOrAnnotation(email));
                    }
                }
            } else {
                this.list.add(new EmailOrAnnotation(email));
            }
        }
    }

    /**
     * Adds a new EmailOrAnnotation to the list.  If the item already exists then it is updated.
     * @param annotation
     */
    public void upsert(CrmEntities.Annotations.Annotation annotation) {
        if (this.list.size() == 0) {
            this.list.add(new EmailOrAnnotation(annotation));
        } else {
            if (this.exists(annotation.entityid)) {
                for (int i = 0; i < this.list.size(); i++) {
                    if (this.list.get(i).entityid.equals(annotation.entityid)) {
                        this.list.remove(i);
                        this.list.add(new EmailOrAnnotation(annotation));
                    }
                }
            } else {
                this.list.add(new EmailOrAnnotation(annotation));
            }
        }
    }

    /**
     * Adds a new EmailOrAnnotation to the list.  If the item already exists then it is updated.
     * @param emailOrAnnotation
     */
    public void upsert(EmailOrAnnotation emailOrAnnotation) {
        if (this.list.size() == 0) {
            this.list.add(emailOrAnnotation);
        } else {
            if (this.exists(emailOrAnnotation.entityid)) {
                for (int i = 0; i < this.list.size(); i++) {
                    if (this.list.get(i).entityid.equals(emailOrAnnotation.entityid)) {
                        this.list.remove(i);
                        this.list.add(emailOrAnnotation);
                    }
                }
            } else {
                this.list.add(emailOrAnnotation);
            }
        }
    }

    public void remove(CrmEntities.Annotations.Annotation annotation) {
        for (int i = 0; i < this.list.size(); i++) {
            EmailOrAnnotation item = this.list.get(i);
            if (item.annotation.entityid.equals(item.entityid)) {
                this.list.remove(i);
            }
        }
    }

    public void remove(CrmEntities.Emails.Email email) {
        for (int i = 0; i < this.list.size(); i++) {
            EmailOrAnnotation item = this.list.get(i);
            if (item.email.entityid.equals(item.entityid)) {
                this.list.remove(i);
            }
        }
    }

    public void remove(EmailOrAnnotation item) {
        for (int i = 0; i < this.list.size(); i++) {
            EmailOrAnnotation _item = this.list.get(i);
            if (item.entityid.equals(_item.entityid)) {
                this.list.remove(i);
            }
        }
    }

    public static class EmailOrAnnotation extends CrmEntities.CrmEntity implements Comparable<EmailsOrAnnotations.EmailOrAnnotation> {

        public CrmEntities.Annotations.Annotation annotation;
        public CrmEntities.Emails.Email email;
        public DateTime dateTime;

        public EmailOrAnnotation(CrmEntities.Emails.Email email) {
            this.dateTime = email.createdOn;
            this.email = email;
            this.annotation = null;
            this.entityid = email.entityid;
        }

        public EmailOrAnnotation(CrmEntities.Annotations.Annotation annotation) {
            this.dateTime = annotation.createdon;
            this.annotation = annotation;
            this.email = null;
            this.entityid = annotation.entityid;
        }

        public boolean isEmail() {
            return this.annotation == null && this.email != null;
        }

        public boolean isAnnotation() {
            return this.annotation != null && this.email == null;
        }

        @Override
        public int compareTo(EmailOrAnnotation o) {
            return this.dateTime.compareTo(o.dateTime);
        }
    }

}
