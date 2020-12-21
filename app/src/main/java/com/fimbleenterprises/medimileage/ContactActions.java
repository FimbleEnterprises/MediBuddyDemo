package com.fimbleenterprises.medimileage;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

class ContactActions {

    CrmEntities.Contacts.Contact contact;
    Activity activity;
    public static final int CALL_PHONE_REQ = 123;
    public static final int SMS_REQ = 124;
    public static final String SENT_ACTION = "SENT_ACTION";
    public static final String DELIVERY_ACTION = "DELIVERY_ACTION";

    public ContactActions(Activity activity, CrmEntities.Contacts.Contact contact) {
        this.contact = contact;
        this.activity = activity;
    }

    void showContactOptions() {
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
        TableRow smsRow1 = dialog.findViewById(R.id.tableRowSms1);
        TableRow smsRow2 = dialog.findViewById(R.id.tableRowSms2);
        TableRow address1Row = dialog.findViewById(R.id.tableRow_address1Phone);
        TableRow businessRow = dialog.findViewById(R.id.tableRow_businessPhone);
        TableRow emailRow = dialog.findViewById(R.id.tableRow_email_addy);

        businessRow.setVisibility(contact.mobile == null ? View.GONE : View.VISIBLE);
        address1Row.setVisibility(contact.address1Phone == null ? View.GONE : View.VISIBLE);
        smsRow1.setVisibility(contact.address1Phone == null ? View.GONE : View.VISIBLE);
        smsRow2.setVisibility(contact.mobile == null ? View.GONE : View.VISIBLE);
        emailRow.setVisibility(contact.email == null ? View.GONE : View.VISIBLE);

        btnCallAddress1.setText(contact.address1Phone != null ? "Call: " + contact.address1Phone : "");
        btnCallBusiness1.setText(contact.mobile != null ? "Call: " + contact.mobile : "");
        btnSms1.setText(contact.address1Phone != null ? "Text: " + contact.address1Phone : "");
        btnSms2.setText(contact.mobile != null ? "Text: " + contact.mobile : "");

        String s1 = btnSms1.getText().toString();
        String s2 = btnSms2.getText().toString();

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helpers.Files.shareFile(activity, contact.toVcard());
            }
        });

        btnSms1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    Helpers.Permissions.RequestContainer container = new Helpers.Permissions.RequestContainer();
                    container.add(Helpers.Permissions.PermissionType.READ_SMS);
                    container.add(Helpers.Permissions.PermissionType.SEND_SMS);
                    activity.requestPermissions(container.toArray(), SMS_REQ);
                    return;
                }
                Helpers.Sms.sendSms(activity, contact.address1Phone);
            }
        });

        btnSms2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    Helpers.Permissions.RequestContainer container = new Helpers.Permissions.RequestContainer();
                    container.add(Helpers.Permissions.PermissionType.READ_SMS);
                    container.add(Helpers.Permissions.PermissionType.SEND_SMS);
                    container.add(Helpers.Permissions.PermissionType.RECEIVE_SMS);
                    container.add(Helpers.Permissions.PermissionType.RECEIVE_MMS);
                    activity.requestPermissions(container.toArray(), SMS_REQ);
                    return;
                }
                Helpers.Sms.sendSms(activity, contact.mobile);


            }
        });

        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EntityContainers.EntityContainer container = new EntityContainers.EntityContainer();
                // container.entityFields.add(new EntityContainers.EntityField(""))

                Helpers.Email.sendEmail(new String[]{contact.email}, "", "", activity);
            }
        });

        btnViewContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, BasicEntityActivity.class);
                intent.putExtra(BasicEntityActivity.ACTIVITY_TITLE, contact.fullname);
                intent.putExtra(BasicEntityActivity.ENTITY_LOGICAL_NAME, "contact");
                intent.putExtra(BasicEntityActivity.ENTITYID, contact.contactid);
                intent.putExtra(BasicEntityActivity.GSON_STRING, contact.toBasicEntity().toGson());
                intent.putExtra(BasicEntityActivity.LOAD_NOTES, false);
                intent.putExtra(BasicEntityActivity.HIDE_MENU, false);
                activity.startActivityForResult(intent, BasicEntityActivity.REQUEST_BASIC);
            }
        });

        btnAddToContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    insertContact(contact);
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
                    if (Helpers.Permissions.isGranted(Helpers.Permissions.PermissionType.CALL_PHONE)) {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contact.address1Phone));
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
                    if (Helpers.Permissions.isGranted(Helpers.Permissions.PermissionType.CALL_PHONE)) {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contact.mobile));
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

    public void insertContact(CrmEntities.Contacts.Contact contact) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.NAME, contact.fullname);
        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, contact.email);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, contact.mobile);
        intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, contact.address1Phone);
        intent.putExtra(ContactsContract.Intents.Insert.COMPANY, contact.accountFormatted);
        intent.putExtra(ContactsContract.Intents.Insert.NOTES, "Added from MileBuddy");
        intent.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, contact.jobtitle);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

}
