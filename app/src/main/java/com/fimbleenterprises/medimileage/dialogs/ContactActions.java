package com.fimbleenterprises.medimileage.dialogs;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.MyInterfaces;
import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.activities.BasicEntityActivity;
import com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities;
import com.fimbleenterprises.medimileage.objects_and_containers.EntityContainers;

import androidx.core.app.ActivityCompat;

public class ContactActions {

    /*
        Version: 1.81
        Fixed an erroneous line of code, likely from a copy/paste action that was showing a completely
        unnecessary Yes/No dialog and then deleting the contact regardless of choice.
     */

    public static class Person extends CrmEntities.Contacts.Contact {
        public boolean isLead = false;
        public String parentid;

        public void qualifyLead(MyInterfaces.leadQualifyListener listener) {

        }

        public Person(CrmEntities.Contacts.Contact contact) {
            this.lastname = contact.lastname;
            this.firstname = contact.firstname;
            this.telephone1 = contact.telephone1;
            this.mobile = contact.mobile;
            this.email = contact.email;
            this.accountFormatted = contact.accountFormatted;
            this.jobtitle = contact.jobtitle;
            this.accountid = contact.accountid;
            this.npiid = contact.npiid;
            this.npiFormatted = contact.npiFormatted;
            this.modifiedBy = contact.modifiedBy;
            this.modifiedByFormatted = contact.modifiedByFormatted;
            this.createdBy = contact.createdBy;
            this.createdByFormatted = contact.createdByFormatted;
            this.modifiedOn = contact.modifiedOn;
            this.modifiedOnFormatted = contact.modifiedOnFormatted;
            this.createdOn = contact.createdOn;
            this.createdOnFormatted = contact.createdOnFormatted;
            this.etag = contact.etag;
            this.parentid = contact.entityid;
        }

        public Person(CrmEntities.Leads.Lead lead) {
            this.isLead = true;
            this.lastname = lead.lastname;
            this.firstname = lead.firstname;
            this.telephone1 = lead.businessphone;
            this.mobile = lead.mobilephone;
            this.email = lead.email;
            this.accountFormatted = lead.parentAccountName;
            this.jobtitle = lead.jobtitle;
            this.accountid = lead.parentAccountId;
            this.modifiedBy = lead.modifiedBy;
            this.modifiedByFormatted = lead.modifiedByFormatted;
            this.createdBy = lead.createdBy;
            this.createdByFormatted = lead.createdByFormatted;
            this.modifiedOn = lead.modifiedOn;
            this.modifiedOnFormatted = lead.modifiedOnFormatted;
            this.createdOn = lead.createdOn;
            this.createdOnFormatted = lead.createdOnFormatted;
            this.etag = lead.etag;
            this.parentid = lead.entityid;
        }

    }

    Person person;
    Activity activity;
    public static final int CALL_PHONE_REQ = 123;
    public static final int SMS_REQ = 124;
    public static final String SENT_ACTION = "SENT_ACTION";
    public static final String DELIVERY_ACTION = "DELIVERY_ACTION";
    public boolean dismissOnSelection = false;
    public boolean allowDelete = false;

    public ContactActions(Activity activity, CrmEntities.Contacts.Contact contact) {
        this.person = new Person(contact);

        this.activity = activity;
    }

    public ContactActions(Activity activity, CrmEntities.Leads.Lead lead) {
        this.person = new Person(lead);
        this.person.lastname = lead.lastname;
        this.person.firstname = lead.firstname;
        this.person.telephone1 = lead.businessphone;
        this.person.mobile = lead.mobilephone;
        this.person.email = lead.email;
        this.person.accountFormatted = lead.parentAccountName;
        this.person.jobtitle = lead.jobtitle;
        this.person.accountid = lead.parentAccountId;

        this.activity = activity;
    }

    public void showContactOptions() {
        // custom dialog
        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_contact_options);
        dialog.setTitle("Options");
        Button btnViewContact = dialog.findViewById(R.id.view_contact);
        Button btnAddToContacts = dialog.findViewById(R.id.btn_add_to_contacts);
        Button btnCallBusiness1 = dialog.findViewById(R.id.btnCallBusinessPhone);
        Button btnCallAddress1 = dialog.findViewById(R.id.btnCallAddress1);
        Button btnEmail = dialog.findViewById(R.id.btnEmailContact);
        Button btnShare = dialog.findViewById(R.id.btn_share_contact);
        Button btnSms1 = dialog.findViewById(R.id.btn_sms_contact1);
        Button btnSms2 = dialog.findViewById(R.id.btn_sms_contact2);
        Button btnDelete = dialog.findViewById(R.id.delete_contact);
        TableRow deleteRow = dialog.findViewById(R.id.tableRowDeleteButton);
        TableRow smsRow1 = dialog.findViewById(R.id.tableRowSms1);
        TableRow smsRow2 = dialog.findViewById(R.id.tableRowSms2);
        TableRow address1Row = dialog.findViewById(R.id.tableRow_address1Phone);
        TableRow businessRow = dialog.findViewById(R.id.tableRow_businessPhone);
        TableRow emailRow = dialog.findViewById(R.id.tableRow_email_addy);

        deleteRow.setVisibility(allowDelete ? View.VISIBLE : View.GONE);
        businessRow.setVisibility(person.mobile == null ? View.GONE : View.VISIBLE);
        address1Row.setVisibility(person.telephone1 == null ? View.GONE : View.VISIBLE);
        smsRow1.setVisibility(person.telephone1 == null ? View.GONE : View.VISIBLE);
        smsRow2.setVisibility(person.mobile == null ? View.GONE : View.VISIBLE);
        emailRow.setVisibility(person.email == null ? View.GONE : View.VISIBLE);

        btnCallAddress1.setText(person.telephone1 != null ? "Call: " + person.telephone1 : "");
        btnCallBusiness1.setText(person.mobile != null ? "Call mobile: " + person.mobile : "");
        btnSms1.setText(person.telephone1 != null ? "Text: " + person.telephone1 : "");
        btnSms2.setText(person.mobile != null ? "Text mobile: " + person.mobile : "");

        String s1 = btnSms1.getText().toString();
        String s2 = btnSms2.getText().toString();

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helpers.Files.shareFileProperly(activity, person.toVcard());
            }
        });

        btnSms1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    if (dismissOnSelection) { dialog.dismiss(); }
                    Helpers.Permissions.RequestContainer container = new Helpers.Permissions.RequestContainer();
                    container.add(Helpers.Permissions.PermissionType.READ_SMS);
                    container.add(Helpers.Permissions.PermissionType.SEND_SMS);
                    activity.requestPermissions(container.toArray(), SMS_REQ);
                    return;
                }
                Helpers.Sms.sendSms(activity, person.address1Phone);
            }
        });

        btnSms2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    if (dismissOnSelection) { dialog.dismiss(); }
                    Helpers.Permissions.RequestContainer container = new Helpers.Permissions.RequestContainer();
                    container.add(Helpers.Permissions.PermissionType.READ_SMS);
                    container.add(Helpers.Permissions.PermissionType.SEND_SMS);
                    container.add(Helpers.Permissions.PermissionType.RECEIVE_SMS);
                    container.add(Helpers.Permissions.PermissionType.RECEIVE_MMS);
                    activity.requestPermissions(container.toArray(), SMS_REQ);
                    return;
                }
                Helpers.Sms.sendSms(activity, person.mobile);


            }
        });

        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EntityContainers.EntityContainer container = new EntityContainers.EntityContainer();
                if (dismissOnSelection) { dialog.dismiss(); }
                Helpers.Email.sendEmail(new String[]{person.email}, "", "", activity);
            }
        });

        btnViewContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dismissOnSelection) { dialog.dismiss(); }
                Intent intent = new Intent(activity, BasicEntityActivity.class);
                intent.putExtra(BasicEntityActivity.ACTIVITY_TITLE, person.getFullname());
                String entityLogicalName = (person.isLead) ? "lead" : "contact";
                intent.putExtra(BasicEntityActivity.ENTITY_LOGICAL_NAME, entityLogicalName);
                intent.putExtra(BasicEntityActivity.ENTITYID, person.parentid);
                intent.putExtra(BasicEntityActivity.GSON_STRING, person.toBasicEntity(person.isLead).toGson());
                intent.putExtra(BasicEntityActivity.LOAD_NOTES, true);
                intent.putExtra(BasicEntityActivity.HIDE_MENU, false);
                activity.startActivityForResult(intent, BasicEntityActivity.REQUEST_BASIC);
            }
        });

        btnAddToContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (dismissOnSelection) { dialog.dismiss(); }
                    insertContact();
                } catch (Exception e) {
                    Toast.makeText(activity, "Failed to add contact\n" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });

        btnCallAddress1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (dismissOnSelection) { dialog.dismiss(); }
                    if (Helpers.Permissions.isGranted(Helpers.Permissions.PermissionType.CALL_PHONE)) {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + person.address1Phone));
                        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            Helpers.Permissions.RequestContainer container = new Helpers.Permissions.RequestContainer();
                            container.add(Helpers.Permissions.PermissionType.CALL_PHONE);
                            activity.requestPermissions(container.toArray(), CALL_PHONE_REQ);
                            return;
                        }
                        activity.startActivity(intent);
                    } else {
                        Helpers.Permissions.RequestContainer container = new Helpers.Permissions.RequestContainer();
                        container.add(Helpers.Permissions.PermissionType.CALL_PHONE);
                        activity.requestPermissions(container.toArray(), CALL_PHONE_REQ);
                    }
                } catch (Exception e) {
                    Toast.makeText(activity, "Failed to call\n" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });

        btnCallBusiness1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (dismissOnSelection) { dialog.dismiss(); }
                    if (Helpers.Permissions.isGranted(Helpers.Permissions.PermissionType.CALL_PHONE)) {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + person.mobile));
                        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            Helpers.Permissions.RequestContainer container = new Helpers.Permissions.RequestContainer();
                            container.add(Helpers.Permissions.PermissionType.CALL_PHONE);
                            activity.requestPermissions(container.toArray(), CALL_PHONE_REQ);
                            return;
                        }
                        activity.startActivity(intent);
                    } else {
                        Helpers.Permissions.RequestContainer container = new Helpers.Permissions.RequestContainer();
                        container.add(Helpers.Permissions.PermissionType.CALL_PHONE);
                        activity.requestPermissions(container.toArray(), CALL_PHONE_REQ);
                    }
                } catch (Exception e) {
                    Toast.makeText(activity, "Failed to call\n" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });

        dialog.show();
    }

    void showUpdateEntityOptions() {

    }

    /**
     * Shows a dialog allowing the user to add this person to their device's address book.
     */
    public void insertContact() {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.NAME, person.getFullname());
        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, person.email);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, person.mobile);
        intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, person.address1Phone);
        intent.putExtra(ContactsContract.Intents.Insert.COMPANY, person.accountFormatted);
        intent.putExtra(ContactsContract.Intents.Insert.NOTES, "Added from MileBuddy");
        intent.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, person.jobtitle);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

}
