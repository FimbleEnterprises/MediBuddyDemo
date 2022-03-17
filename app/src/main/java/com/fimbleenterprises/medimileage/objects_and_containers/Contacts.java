package com.fimbleenterprises.medimileage.objects_and_containers;

import android.os.Parcel;
import android.os.Parcelable;

import com.fimbleenterprises.medimileage.Helpers;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Contacts {
    public ArrayList<Contacts.Contact> list = new ArrayList<>();

    public Contacts(String crmResponse) {
        try {
            JSONObject rootObject = new JSONObject(crmResponse);
            JSONArray rootArray = rootObject.getJSONArray("value");
            for (int i = 0; i < rootArray.length(); i++) {
                list.add(new Contacts.Contact(rootArray.getJSONObject(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Converts this object's contact list to LITE versions having fewer properties
     */
    public void convertToLiteContacts() {
        ArrayList liteList = new ArrayList();
        for (Contacts.Contact c : this.list) {
            Contacts.Contact c1 = new Contacts.Contact();
            c1.entityid = c.entityid;
            c1.firstname = c.firstname;
            c1.lastname = c.lastname;
            c1.accountid = c.accountid;
            c1.accountFormatted = c.accountFormatted;
            liteList.add(c1);
        }
        this.list = liteList;
    }

    /**
     * Converts the supplied Contacts object's contact list to LITE versions having fewer properties
     *  (fullname, accountid and account name).
     * @param listToConvert The list to convert from.
     * @return
     */
    public static Contacts convertToLiteContacts(Contacts listToConvert) {
        ArrayList liteList = new ArrayList();
        for (Contacts.Contact c : listToConvert.list) {
            Contacts.Contact c1 = new Contacts.Contact();
            c1.entityid = c.entityid;
            c1.firstname = c.firstname;
            c1.lastname = c.lastname;
            c1.accountid = c.accountid;
            c1.accountFormatted = c.accountFormatted;
            liteList.add(c1);
        }

        listToConvert.list = liteList;
        return listToConvert;
    }

    public static class Contact extends CrmEntities.CrmEntity implements Parcelable {

        /*public String etag;
                    public String entityid;*/
        public String firstname;
        public String lastname;
        public String accountid;
        public String accountFormatted;
        public String mobile;
        public String address1composite;
        public String telephone1;
        public String address1Phone;
        public String jobtitle;
        public String npi;
        public String email;
        public String createdBy;
        public String createdByFormatted;
        public DateTime createdOn;
        public String createdOnFormatted;
        public DateTime modifiedOn;
        public String modifiedOnFormatted;
        public String modifiedBy;
        public String modifiedByFormatted;
        public String statecodeformatted;
        public String statuscodeformatted;
        public int statecode;
        public int statuscode;
        private String description;
        public int preferredcontactmethodcode;

        // Surgeon properties
        public String msus_credentials;
        public String msus_primaryspecialty;
        public String msus_secondary_specialties;
        public String msus_medical_school_name;
        public String msus_ttfm_vendor;
        public String msus_ttfm_procedures;
        public String msus_vessels_imaged;
        public String preferredcontactmethodcodeFormatted;
        public int msus_medicare_claims;
        public int msus_graduation_year;
        public int msus_annual_cabg_cases;
        public double msus_percentagecaseswttfm;
        public double msus_percentageonpump;
        public double msus_percentagescanaorta;
        public double msus_percentageusingmedistimimaging;
        public boolean msus_medistim_customer;


        public Contact() {}

        public Contact(JSONObject json) {
            try {
                if (!json.isNull("createdonFormattedValue")) {
                    this.createdOnFormatted = (json.getString("createdonFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("createdon")) {
                    this.createdOn = (new DateTime(json.getString("createdon")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("modifiedonFormattedValue")) {
                    this.modifiedOnFormatted = (json.getString("modifiedonFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("modifiedon")) {
                    this.modifiedOn = (new DateTime(json.getString("modifiedon")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_createdby_value")) {
                    this.createdBy = (json.getString("_createdby_value"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_createdby_valueFormattedValue")) {
                    this.createdByFormatted = (json.getString("_createdby_valueFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_modifiedby_value")) {
                    this.modifiedBy = (json.getString("_modifiedby_value"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_modifiedby_valueFormattedValue")) {
                    this.modifiedByFormatted = (json.getString("_modifiedby_valueFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("etag")) {
                    this.etag = (json.getString("etag"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("telephone1")) {
                    this.telephone1 = (json.getString("telephone1"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("firstname")) {
                    this.firstname = (json.getString("firstname"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("lastname")) {
                    this.lastname = (json.getString("lastname"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_parentcustomerid_valueFormattedValue")) {
                    this.accountFormatted = (json.getString("_parentcustomerid_valueFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("emailaddress1")) {
                    this.email = (json.getString("emailaddress1"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("address1_telephone1")) {
                    this.address1Phone = (json.getString("address1_telephone1"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("mobilephone")) {
                    this.mobile = (json.getString("mobilephone"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_parentcustomerid_value")) {
                    this.accountid = (json.getString("_parentcustomerid_value"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("jobtitle")) {
                    this.jobtitle = (json.getString("jobtitle"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("contactid")) {
                    this.entityid = (json.getString("contactid"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_npi")) {
                    this.npi = (json.getString("msus_npi"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("modifiedon")) {
                    this.modifiedOn = (new DateTime(json.getString("modifiedon")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_modifiedby_valueFormattedValue")) {
                    this.modifiedByFormatted = (json.getString("_modifiedby_valueFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_modifiedby_value")) {
                    this.modifiedBy = (json.getString("_modifiedby_value"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("address1_composite")) {
                    this.address1composite = (json.getString("address1_composite"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("statuscode")) {
                    this.statuscode = (json.getInt("statuscode"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("statuscodeFormattedValue")) {
                    this.statuscodeformatted = (json.getString("statuscodeFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("statecode")) {
                    this.statecode = (json.getInt("statecode"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("statecodeFormattedValue")) {
                    this.statecodeformatted = (json.getString("statecodeFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_credentials")) {
                    this.msus_credentials = (json.getString("msus_credentials"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("description")) {
                    this.description = (json.getString("description"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_primaryspecialty")) {
                    this.msus_primaryspecialty = (json.getString("msus_primaryspecialty"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_secondary_specialties")) {
                    this.msus_secondary_specialties = (json.getString("msus_secondary_specialties"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_medical_school_name")) {
                    this.msus_medical_school_name = (json.getString("msus_medical_school_name"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_ttfm_vendor")) {
                    this.msus_ttfm_vendor = (json.getString("msus_ttfm_vendor"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_ttfm_procedures")) {
                    this.msus_ttfm_procedures = (json.getString("msus_ttfm_procedures"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_vessels_imaged")) {
                    this.msus_vessels_imaged = (json.getString("msus_vessels_imaged"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("preferredcontactmethodcodeFormatted")) {
                    this.preferredcontactmethodcodeFormatted = (json.getString("preferredcontactmethodcodeFormatted"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_medicare_claims")) {
                    this.msus_medicare_claims = (json.getInt("msus_medicare_claims"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_graduation_year")) {
                    this.msus_graduation_year = (json.getInt("msus_graduation_year"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_annual_cabg_cases")) {
                    this.msus_annual_cabg_cases = (json.getInt("msus_annual_cabg_cases"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("preferredcontactmethodcode")) {
                    this.preferredcontactmethodcode = (json.getInt("preferredcontactmethodcode"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_percentagecaseswttfm")) {
                    this.msus_percentagecaseswttfm = (json.getDouble("msus_percentagecaseswttfm"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_percentageonpump")) {
                    this.msus_percentageonpump = (json.getDouble("msus_percentageonpump"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_percentagescanaorta")) {
                    this.msus_percentagescanaorta = (json.getDouble("msus_percentagescanaorta"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_percentageusingmedistimimaging")) {
                    this.msus_percentageusingmedistimimaging = (json.getDouble("msus_percentageusingmedistimimaging"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_medistim_customer")) {
                    this.msus_medistim_customer = (json.getBoolean("msus_medistim_customer"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public boolean isSurgeon() {
            return this.npi != null && this.npi.length() > 0;
        }

        public BasicEntity toBasicEntity() {
            return toBasicEntity(false);
        }

        public BasicEntity toBasicEntity(boolean isLead) {
            BasicEntity entity = new BasicEntity(this);
            entity.fields.add(new BasicEntity.EntityBasicField("First name:", this.firstname, "firstname"));
            entity.fields.add(new BasicEntity.EntityBasicField("Last name:", this.lastname, "lastname"));
            entity.fields.add(new BasicEntity.EntityBasicField("Email:", this.email, "emailaddress1"));
            entity.fields.add(new BasicEntity.EntityBasicField("Mobile phone:", this.mobile, "mobilephone"));
            entity.fields.add(new BasicEntity.EntityBasicField("Business phone:", this.telephone1, "telephone1"));


            BasicEntity.EntityBasicField accountField = null;
            if (isLead) {
                accountField = new BasicEntity.EntityBasicField("Account:", this.accountFormatted, "customerid");
            } else {
                accountField = new BasicEntity.EntityBasicField("Account:", this.accountFormatted, "parentcustomerid");
            }
            accountField.isAccountField = true;
            accountField.account = new CrmEntities.Accounts.Account(this.accountid, this.accountFormatted);
            entity.fields.add(accountField);

            entity.fields.add(new BasicEntity.EntityBasicField("Title:", this.jobtitle, "jobtitle"));
            entity.fields.add(new BasicEntity.EntityBasicField("Description", this.description, "description"));

            entity.fields.add(new BasicEntity.EntityBasicField("Created on:", this.createdOnFormatted, true));
            entity.fields.add(new BasicEntity.EntityBasicField("Created by:", this.createdByFormatted, true));
            entity.fields.add(new BasicEntity.EntityBasicField("Modified on:", this.modifiedOnFormatted, true));
            entity.fields.add(new BasicEntity.EntityBasicField("Modified by:", this.modifiedByFormatted, true));

            ArrayList<BasicEntity.EntityBasicField.OptionSetValue> preferredContactMethodOptionsSet = new ArrayList<>();
            preferredContactMethodOptionsSet.add(new BasicEntity.EntityBasicField.OptionSetValue("Mobile", "100000002"));
            preferredContactMethodOptionsSet.add(new BasicEntity.EntityBasicField.OptionSetValue("Office", "100000000"));
            preferredContactMethodOptionsSet.add(new BasicEntity.EntityBasicField.OptionSetValue("Assistant", "100000001"));
            preferredContactMethodOptionsSet.add(new BasicEntity.EntityBasicField.OptionSetValue("Other", "100000003"));
            preferredContactMethodOptionsSet.add(new BasicEntity.EntityBasicField.OptionSetValue("Text", "745820000"));
            preferredContactMethodOptionsSet.add(new BasicEntity.EntityBasicField.OptionSetValue("Any", "1"));
            preferredContactMethodOptionsSet.add(new BasicEntity.EntityBasicField.OptionSetValue("Email", "2"));
            preferredContactMethodOptionsSet.add(new BasicEntity.EntityBasicField.OptionSetValue("Phone", "3"));
            preferredContactMethodOptionsSet.add(new BasicEntity.EntityBasicField.OptionSetValue("Fax", "4"));
            preferredContactMethodOptionsSet.add(new BasicEntity.EntityBasicField.OptionSetValue("Mail", "5"));
            BasicEntity.EntityBasicField preferredContactMethod = new BasicEntity.EntityBasicField("Preferred contact method: ", preferredcontactmethodcodeFormatted);
            preferredContactMethod.optionSetValues = preferredContactMethodOptionsSet;
            preferredContactMethod.crmFieldName = "preferredcontactmethodcode";
            preferredContactMethod.isOptionSet = true;
            preferredContactMethod.isReadOnly = false;
            entity.fields.add(preferredContactMethod);

            // Surgeon properties
            BasicEntity.EntityBasicField npiNum = new BasicEntity.EntityBasicField("NPI:", this.npi, "msus_npi");
            npiNum.isReadOnly = true;
            entity.fields.add(npiNum);

            BasicEntity.EntityBasicField msus_credentials = new BasicEntity.EntityBasicField("Credentials:", this.msus_credentials, "msus_credentials");
            msus_credentials.isReadOnly = false;
            entity.fields.add(msus_credentials);

            BasicEntity.EntityBasicField msus_primaryspecialty = new BasicEntity.EntityBasicField("Primary specialty:", this.msus_primaryspecialty, "msus_primaryspecialty");
            msus_primaryspecialty.isReadOnly = false;
            entity.fields.add(msus_primaryspecialty);

            BasicEntity.EntityBasicField msus_secondary_specialties = new BasicEntity.EntityBasicField("Secondary specialty:", this.msus_secondary_specialties, "msus_secondary_specialties");
            msus_secondary_specialties.isReadOnly = false;
            entity.fields.add(msus_secondary_specialties);

            BasicEntity.EntityBasicField msus_medical_school_name = new BasicEntity.EntityBasicField("Medical school:", this.msus_medical_school_name, "msus_medical_school_name");
            msus_medical_school_name.isReadOnly = false;
            entity.fields.add(msus_medical_school_name);

            BasicEntity.EntityBasicField msus_graduation_year = new BasicEntity.EntityBasicField("Graduation year:", Integer.toString(this.msus_graduation_year), "msus_graduation_year");
            msus_graduation_year.isReadOnly = false;
            msus_graduation_year.isNumber = true;
            entity.fields.add(msus_graduation_year);

            BasicEntity.EntityBasicField msus_ttfm_vendor = new BasicEntity.EntityBasicField("TTFM vendor:", this.msus_ttfm_vendor, "msus_ttfm_vendor");
            msus_ttfm_vendor.isReadOnly = false;
            entity.fields.add(msus_ttfm_vendor);

            BasicEntity.EntityBasicField msus_ttfm_procedures = new BasicEntity.EntityBasicField("TTFM procedures:", this.msus_ttfm_procedures, "msus_ttfm_procedures");
            msus_ttfm_procedures.isReadOnly = false;
            entity.fields.add(msus_ttfm_procedures);

            BasicEntity.EntityBasicField msus_vessels_imaged = new BasicEntity.EntityBasicField("Vessels imaged:", this.msus_vessels_imaged, "msus_vessels_imaged");
            msus_vessels_imaged.isReadOnly = false;
            entity.fields.add(msus_vessels_imaged);

            BasicEntity.EntityBasicField msus_medicare_claims = new BasicEntity.EntityBasicField("Medicare claims:", Integer.toString(this.msus_medicare_claims), "msus_medicare_claims");
            msus_medicare_claims.isReadOnly = false;
            msus_medicare_claims.isNumber = true;
            entity.fields.add(msus_medicare_claims);

            BasicEntity.EntityBasicField msus_annual_cabg_cases = new BasicEntity.EntityBasicField("Annual CABG cases:", Integer.toString(this.msus_annual_cabg_cases), "msus_annual_cabg_cases");
            msus_annual_cabg_cases.isReadOnly = false;
            msus_annual_cabg_cases.isNumber = true;
            entity.fields.add(msus_annual_cabg_cases);

            BasicEntity.EntityBasicField msus_percentagecaseswttfm = new BasicEntity.EntityBasicField("Percentage w/TTFM:", Double.toString(this.msus_percentagecaseswttfm), "msus_percentagecaseswttfm");
            msus_percentagecaseswttfm.isReadOnly = false;
            msus_percentagecaseswttfm.isNumber = true;
            entity.fields.add(msus_percentagecaseswttfm);

            BasicEntity.EntityBasicField msus_percentageonpump = new BasicEntity.EntityBasicField("Percentage on pump:", Double.toString(this.msus_percentageonpump), "msus_percentageonpump");
            msus_percentageonpump.isReadOnly = false;
            msus_percentageonpump.isNumber = true;
            entity.fields.add(msus_percentageonpump);

            BasicEntity.EntityBasicField msus_percentagescanaorta = new BasicEntity.EntityBasicField("Percentage scan aorta:", Double.toString(this.msus_percentagescanaorta), "msus_percentagescanaorta");
            msus_percentagescanaorta.isReadOnly = false;
            msus_percentagescanaorta.isNumber = true;
            entity.fields.add(msus_percentagescanaorta);

            BasicEntity.EntityBasicField msus_percentageusingmedistimimaging = new BasicEntity.EntityBasicField("Percentage use Medistim imaging:", Double.toString(this.msus_percentageusingmedistimimaging), "msus_percentageusingmedistimimaging");
            msus_percentageusingmedistimimaging.isReadOnly = false;
            msus_percentageusingmedistimimaging.isNumber = true;
            entity.fields.add(msus_percentageusingmedistimimaging);

            BasicEntity.EntityBasicField msus_medistim_customer = new BasicEntity.EntityBasicField("Is Medistim customer:", Boolean.toString(this.msus_medistim_customer), "msus_medistim_customer");
            msus_medistim_customer.isReadOnly = false;
            msus_medistim_customer.isBoolean = true;
            entity.fields.add(msus_medistim_customer);

            return entity;
        }

        public String getFullname() {
            return this.firstname + " " + this.lastname;
        }

        private String toVcardString() {
            String preamble = "" +
                    "BEGIN:VCARD\n" +
                    "VERSION:2.1\n";

            StringBuilder vBody = new StringBuilder(preamble);

            if (this.firstname != null) {
                vBody.append("N:" + this.firstname + " " + this.lastname + ";;;\n");
            }
            if (this.firstname != null) {
                vBody.append("FN:" + this.firstname + " " + this.lastname + "\n");
            }
            if (this.address1Phone != null) {
                vBody.append("TEL;CELL:" + this.address1Phone + "\n");
            }
            if (this.mobile != null) {
                vBody.append("TEL;WORK:" + this.mobile + "\n");
            }
            if (this.email != null) {
                vBody.append("EMAIL;HOME:" + this.email + "\n");
            }
            vBody.append("ORG:MileBuddy Export\n");
            if (this.accountFormatted != null) {
                vBody.append("ORG:" + this.accountFormatted + "\n");
            }
            if (this.jobtitle != null) {
                vBody.append("TITLE:" + this.jobtitle + "\n");
            }
            if (this.npi != null) {
                vBody.append("NOTE:NPI: " + this.npi + "\n");
            }

            String closingText = "END:VCARD";

            vBody.append(closingText);

            return vBody.toString();
        }

        /**
         * Tries to convert this object to a vcard (version 2.1)
         * @return A file (fullname.vcf) or null if unsuccessful.
         */
        public File toVcard() {

            String vBody = this.toVcardString();



            try {
                String fqfn = Helpers.Files.getAppDirectory().getPath() + File.separator + this.firstname + "_" + this.lastname + ".vcf";
                File file = new File(fqfn);
                if (file.exists()) {
                    file.delete();
                    file.createNewFile();
                }
                PrintWriter out = new PrintWriter(file);
                out.println(vBody);
                out.close();
                return file;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        public String toString() {
            return this.firstname + " " + this.lastname + ", " + this.accountFormatted;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.firstname);
            dest.writeString(this.lastname);
            dest.writeString(this.accountid);
            dest.writeString(this.accountFormatted);
            dest.writeString(this.mobile);
            dest.writeString(this.address1composite);
            dest.writeString(this.telephone1);
            dest.writeString(this.address1Phone);
            dest.writeString(this.jobtitle);
            dest.writeString(this.npi);
            dest.writeString(this.email);
            dest.writeString(this.createdBy);
            dest.writeString(this.createdByFormatted);
            dest.writeSerializable(this.createdOn);
            dest.writeString(this.createdOnFormatted);
            dest.writeSerializable(this.modifiedOn);
            dest.writeString(this.modifiedOnFormatted);
            dest.writeString(this.modifiedBy);
            dest.writeString(this.modifiedByFormatted);
            dest.writeString(this.statecodeformatted);
            dest.writeString(this.statuscodeformatted);
            dest.writeInt(this.statecode);
            dest.writeInt(this.statuscode);
            dest.writeString(this.entityid);
            dest.writeString(msus_credentials);
            dest.writeString(msus_primaryspecialty);
            dest.writeString(msus_secondary_specialties);
            dest.writeString(msus_medical_school_name);
            dest.writeString(msus_ttfm_vendor);
            dest.writeString(msus_ttfm_procedures);
            dest.writeString(msus_vessels_imaged);
            dest.writeString(preferredcontactmethodcodeFormatted);
            dest.writeInt(preferredcontactmethodcode);
            dest.writeInt(msus_medicare_claims);
            dest.writeInt(msus_graduation_year);
            dest.writeInt(msus_annual_cabg_cases);
            dest.writeDouble(msus_percentagecaseswttfm);
            dest.writeDouble(msus_percentageonpump);
            dest.writeDouble(msus_percentagescanaorta);
            dest.writeDouble(msus_percentageusingmedistimimaging);
            dest.writeByte((byte) (msus_medistim_customer ? 0x01 : 0x00));
        }

        public void readFromParcel(Parcel source) {
            this.firstname = source.readString();
            this.lastname = source.readString();
            this.accountid = source.readString();
            this.accountFormatted = source.readString();
            this.mobile = source.readString();
            this.address1composite = source.readString();
            this.telephone1 = source.readString();
            this.address1Phone = source.readString();
            this.jobtitle = source.readString();
            this.npi = source.readString();
            this.email = source.readString();
            this.createdBy = source.readString();
            this.createdByFormatted = source.readString();
            this.createdOn = (DateTime) source.readSerializable();
            this.createdOnFormatted = source.readString();
            this.modifiedOn = (DateTime) source.readSerializable();
            this.modifiedOnFormatted = source.readString();
            this.modifiedBy = source.readString();
            this.modifiedByFormatted = source.readString();
            this.statecodeformatted = source.readString();
            this.statuscodeformatted = source.readString();
            this.statecode = source.readInt();
            this.statuscode = source.readInt();
            this.entityid = source.readString();
        }

        protected Contact(Parcel in) {
            this.firstname = in.readString();
            this.lastname = in.readString();
            this.accountid = in.readString();
            this.accountFormatted = in.readString();
            this.mobile = in.readString();
            this.address1composite = in.readString();
            this.telephone1 = in.readString();
            this.address1Phone = in.readString();
            this.jobtitle = in.readString();
            this.npi = in.readString();
            this.email = in.readString();
            this.createdBy = in.readString();
            this.createdByFormatted = in.readString();
            this.createdOn = (DateTime) in.readSerializable();
            this.createdOnFormatted = in.readString();
            this.modifiedOn = (DateTime) in.readSerializable();
            this.modifiedOnFormatted = in.readString();
            this.modifiedBy = in.readString();
            this.modifiedByFormatted = in.readString();
            this.statecodeformatted = in.readString();
            this.statuscodeformatted = in.readString();
            this.statecode = in.readInt();
            this.statuscode = in.readInt();
            this.entityid = in.readString();
            this.msus_credentials = in.readString();
            this.msus_primaryspecialty = in.readString();
            this.msus_secondary_specialties = in.readString();
            this.msus_medical_school_name = in.readString();
            this.msus_ttfm_vendor = in.readString();
            this.msus_ttfm_procedures = in.readString();
            this.msus_vessels_imaged = in.readString();
            this.preferredcontactmethodcodeFormatted = in.readString();
            this.preferredcontactmethodcode = in.readInt();
            this.msus_medicare_claims = in.readInt();
            this.msus_graduation_year = in.readInt();
            this.msus_annual_cabg_cases = in.readInt();
            this.msus_percentagecaseswttfm = in.readDouble();
            this.msus_percentageonpump = in.readDouble();
            this.msus_percentagescanaorta = in.readDouble();
            this.msus_percentageusingmedistimimaging = in.readDouble();
            this.msus_medistim_customer = in.readByte() != 0x00;
        }

        public static final Parcelable.Creator<Contacts.Contact> CREATOR = new Parcelable.Creator<Contacts.Contact>() {
            @Override
            public Contacts.Contact createFromParcel(Parcel source) {
                return new Contacts.Contact(source);
            }

            @Override
            public Contacts.Contact[] newArray(int size) {
                return new Contacts.Contact[size];
            }
        };
    }
}
