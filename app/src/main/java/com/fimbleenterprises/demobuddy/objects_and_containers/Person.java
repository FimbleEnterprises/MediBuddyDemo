package com.fimbleenterprises.demobuddy.objects_and_containers;

import com.fimbleenterprises.demobuddy.MyInterfaces;

public class Person extends Contacts.Contact {
    public boolean isLead = false;
    public String parentid;

    public void qualifyLead(MyInterfaces.leadQualifyListener listener) {

    }

    public Person(Contacts.Contact contact) {
        this.lastname = contact.lastname;
        this.firstname = contact.firstname;
        this.telephone1 = contact.telephone1;
        this.mobile = contact.mobile;
        this.email = contact.email;
        this.accountFormatted = contact.accountFormatted;
        this.jobtitle = contact.jobtitle;
        this.accountid = contact.accountid;
        this.npi = contact.npi;
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
        this.msus_credentials = contact.msus_credentials;
        this.msus_primaryspecialty = contact.msus_primaryspecialty;
        this.msus_secondary_specialties = contact.msus_secondary_specialties;
        this.msus_medical_school_name = contact.msus_medical_school_name;
        this.msus_ttfm_vendor = contact.msus_ttfm_vendor;
        this.msus_ttfm_procedures = contact.msus_ttfm_procedures;
        this.msus_vessels_imaged = contact.msus_vessels_imaged;
        this.preferredcontactmethodcodeFormatted = contact.preferredcontactmethodcodeFormatted;
        this.preferredcontactmethodcode = contact.preferredcontactmethodcode;
        this.msus_medicare_claims = contact.msus_medicare_claims;
        this.msus_graduation_year = contact.msus_graduation_year;
        this.msus_annual_cabg_cases = contact.msus_annual_cabg_cases;
        this.msus_percentagecaseswttfm = contact.msus_percentagecaseswttfm;
        this.msus_percentageonpump = contact.msus_percentageonpump;
        this.msus_percentagescanaorta = contact.msus_percentagescanaorta;
        this.msus_percentageusingmedistimimaging = contact.msus_percentageusingmedistimimaging;
        this.msus_medistim_customer = contact.msus_medistim_customer;
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
