package com.fimbleenterprises.medimileage;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import ezvcard.Ezvcard;
import ezvcard.VCard;

/**
 * A container for a contact obtained from the user's address book
 */
class MyVcardParser {

    private static final String TAG = "MyVcardParser";
    Context context;
    String contactid;
    String firstName;
    String lastName;
    String fullname;
    String phone1;
    String phone2;
    String address1;
    String address2;
    String title;
    String email;

    private String vcardString;

    public String toVcardString() {
        return this.vcardString;
    }

    public MyVcardParser() { }

    public MyVcardParser(String data) {

        this.vcardString = data;

        VCard vcard = Ezvcard.parse(data).first();
        try {
            this.firstName = vcard.getStructuredName().getGiven();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.lastName = vcard.getStructuredName().getFamily();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.fullname = vcard.getFormattedName().getValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.phone1 = vcard.getTelephoneNumbers().get(0).getText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.phone2 = vcard.getTelephoneNumbers().get(1).getText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.address1 = vcard.getAddresses().get(0).getStreetAddressFull() + " "
                    + vcard.getAddresses().get(0).getLocality() + ", "
                    + vcard.getAddresses().get(0).getRegion() + " "
                    + vcard.getAddresses().get(0).getPostalCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.address2 = vcard.getAddresses().get(0).getStreetAddressFull() + " "
                    + vcard.getAddresses().get(0).getLocality() + ", "
                    + vcard.getAddresses().get(0).getRegion() + " "
                    + vcard.getAddresses().get(0).getPostalCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.title = vcard.getTitles().get(0).getValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.email = vcard.getEmails().get(0).getValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor from a shared contact
     * @param context A valid context that can call "getContactResolver()"
     * @param intent An intent from the user sharing a contact from their addressbook
     */
    public MyVcardParser(Context context, Intent intent) {
        if (intent != null) {

            Log.i(TAG, "MyVcard : Constructing from intent...");

            Uri contactData = intent.getParcelableExtra("android.intent.extra.STREAM");
            ContentResolver cr = context.getContentResolver();
            InputStream stream;
            try {
                stream = cr.openInputStream(contactData);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }

            StringBuffer fileContent = new StringBuffer("");
            int ch;
            try {
                while ((ch = stream.read()) != -1)
                    fileContent.append((char) ch);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            String data = new String(fileContent);
            this.vcardString = data;
            Log.i("TAG", "data: " + data);

            VCard vcard = Ezvcard.parse(data).first();
            try {
                this.firstName = vcard.getStructuredName().getGiven();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                this.lastName = vcard.getStructuredName().getFamily();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                this.fullname = vcard.getFormattedName().getValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                this.phone1 = vcard.getTelephoneNumbers().get(0).getText();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                this.phone2 = vcard.getTelephoneNumbers().get(1).getText();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                this.address1 = vcard.getAddresses().get(0).getStreetAddressFull() + " "
                        + vcard.getAddresses().get(0).getLocality() + ", "
                        + vcard.getAddresses().get(0).getRegion() + " "
                        + vcard.getAddresses().get(0).getPostalCode();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                this.address2 = vcard.getAddresses().get(0).getStreetAddressFull() + " "
                        + vcard.getAddresses().get(0).getLocality() + ", "
                        + vcard.getAddresses().get(0).getRegion() + " "
                        + vcard.getAddresses().get(0).getPostalCode();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                this.title = vcard.getTitles().get(0).getValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                this.email = vcard.getEmails().get(0).getValue();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private Containers.EntityContainer toContainer(String accountid) {

        this.context = context;

        Containers.EntityContainer container = new Containers.EntityContainer();

        if (this.fullname != null) {
            container.entityFields.add(new Containers.EntityField("fullname", this.fullname));
        }

        if (this.firstName != null) {
            container.entityFields.add(new Containers.EntityField("firstname", this.firstName));
        }

        if (this.lastName != null) {
            container.entityFields.add(new Containers.EntityField("lastname", this.lastName));
        }

        if (this.phone1 != null) {
            container.entityFields.add(new Containers.EntityField("telephone1", this.phone1));
        }

        if (this.phone2 != null) {
            container.entityFields.add(new Containers.EntityField("mobilephone", this.phone2));
        }

        if (this.address1 != null) {
            container.entityFields.add(new Containers.EntityField("address1_composite", this.address1));
        }

        if (this.address2 != null) {
            container.entityFields.add(new Containers.EntityField("address2_composite", this.address2));
        }

        if (this.title != null) {
            container.entityFields.add(new Containers.EntityField("jobtitle", this.title));
        }

        if (this.email != null) {
            container.entityFields.add(new Containers.EntityField("emailaddress1", this.email));
        }

        if (this.email != null) {
            container.entityFields.add(new Containers.EntityField("emailaddress1", this.email));
        }

        container.entityFields.add(new Containers.EntityField("parentcustomerid", accountid));

        return container;
    }

    public void uploadToCrm(String accountid, final MyInterfaces.EntityUpdateListener listener) {
        Requests.Request request = new Requests.Request(Requests.Request.Function.CREATE);
        ArrayList<Requests.Argument> args = new ArrayList<>();
        args.add(new Requests.Argument("entityname", "contact"));
        args.add(new Requests.Argument("asuser", MediUser.getMe().systemuserid));
        args.add(new Requests.Argument("values", this.toContainer(accountid).toJson()));
        request.arguments = args;

        new Crm().makeCrmRequest(this.context, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                listener.onSuccess();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                listener.onFailure(error.getLocalizedMessage());
            }
        });
    }

}
