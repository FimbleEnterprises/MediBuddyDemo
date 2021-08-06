package com.fimbleenterprises.medimileage.objects_and_containers;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.fimbleenterprises.medimileage.Crm;
import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.MyInterfaces;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import ezvcard.Ezvcard;
import ezvcard.VCard;

/**
 * A container for a contact obtained from the user's address book
 */
public class MyVcard {

    private static final String TAG = "MyVcard";
    public Context context;
    public String contactid;
    public String firstName;
    public String lastName;
    public String fullname;
    public String phone1;
    public String phone2;
    public String address1;
    public String address2;
    public String title;
    public String email;

    private String vcardString;

    public static ArrayList<MyVcard> parseVcards(String vcardString) {
        ArrayList<MyVcard> parsedVcards = new ArrayList<>();

        String[] varray = vcardString.split("BEGIN:VCARD");
        ArrayList<String> preCreatedStrings = new ArrayList<>();

        for (int i = 0; i < varray.length; i++) {
            if (varray[i].length() > 0) {
                String preCreate = "BEGIN:VCARD\r\n" + varray[i];
                preCreatedStrings.add(preCreate);
            }
        }

        for (String s : preCreatedStrings) {
            parsedVcards.add(new MyVcard(s));
        }

        return parsedVcards;
    }

    /**
     * Tries to convert this object to a vcard (version 2.1)
     * @return A file (fullname.vcf) or null if unsuccessful.
     */
    public File toVcard() {
        String preamble = "" +
                "BEGIN:VCARD\n" +
                "VERSION:2.1\n";

        StringBuilder vBody = new StringBuilder(preamble);

        if (this.fullname != null) {
            vBody.append("N:" + this.fullname + " ;;;\n");
        }
        if (this.fullname != null) {
            vBody.append("FN:" + this.fullname + "\n");
        }
        if (this.phone1 != null) {
            vBody.append("TEL;CELL:" + this.phone1 + "\n");
        }
        if (this.phone2 != null) {
            vBody.append("TEL;WORK:" + this.phone2 + "\n");
        }
        if (this.email != null) {
            vBody.append("EMAIL;HOME:" + this.email + "\n");
        }
        if (this.address1 != null) {
            vBody.append("ADR;HOME:;;" + this.address1 + "\n");
        }
        if (this.address2 != null) {
            vBody.append("ADR;WORK:;;" + this.address2 + "\n");
        }
        vBody.append("ORG:MileBuddy Export\n");
        if (this.title != null) {
            vBody.append("TITLE:" + this.title + "\n");
        }
        if (this.address2 != null) {
            vBody.append("ADR;WORK:;;" + this.address2 + "\n");
        }

        String closingText = "END:VCARD";

        vBody.append(closingText);

        try {
            PrintWriter out = new PrintWriter(Helpers.Files.getAppDirectory() + this.fullname + ".vcf");
            out.println(vBody);
            File vcard = new File(Helpers.Files.getAppDirectory() + this.fullname + ".vcf");
            return vcard;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static String intentToVcardsString(Context context, Intent intent) {
        Uri contactData = intent.getParcelableExtra("android.intent.extra.STREAM");
        ContentResolver cr = context.getContentResolver();
        InputStream stream;
        try {
            stream = cr.openInputStream(contactData);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        StringBuffer fileContent = new StringBuffer("");
        int ch;
        try {
            while ((ch = stream.read()) != -1)
                fileContent.append((char) ch);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        String data = new String(fileContent);
        return data;
    }

    public MyVcard() { }

    private MyVcard(String data) {

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
    private MyVcard(Context context, Intent intent) {
        if (intent != null) {

            Log.i(TAG, "MyVcard : Constructing from intent...");

            this.vcardString = intentToVcardsString(context, intent);

            VCard vcard = Ezvcard.parse(vcardString).first();
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

    public EntityContainers.EntityContainer toContainer(String accountid) {

        this.context = context;

        EntityContainers.EntityContainer container = new EntityContainers.EntityContainer();

        if (this.fullname != null) {
            container.entityFields.add(new EntityContainers.EntityField("fullname", this.fullname));
        }

        if (this.firstName != null) {
            container.entityFields.add(new EntityContainers.EntityField("firstname", this.firstName));
        }

        if (this.lastName != null) {
            container.entityFields.add(new EntityContainers.EntityField("lastname", this.lastName));
        }

        if (this.phone1 != null) {
            container.entityFields.add(new EntityContainers.EntityField("telephone1", this.phone1));
        }

        if (this.phone2 != null) {
            container.entityFields.add(new EntityContainers.EntityField("mobilephone", this.phone2));
        }

        if (this.address1 != null) {
            container.entityFields.add(new EntityContainers.EntityField("address1_composite", this.address1));
        }

        if (this.address2 != null) {
            container.entityFields.add(new EntityContainers.EntityField("address2_composite", this.address2));
        }

        if (this.title != null) {
            container.entityFields.add(new EntityContainers.EntityField("jobtitle", this.title));
        }

        if (this.email != null) {
            container.entityFields.add(new EntityContainers.EntityField("emailaddress1", this.email));
        }

        if (this.email != null) {
            container.entityFields.add(new EntityContainers.EntityField("emailaddress1", this.email));
        }

        container.entityFields.add(new EntityContainers.EntityField("parentcustomerid", accountid));

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
