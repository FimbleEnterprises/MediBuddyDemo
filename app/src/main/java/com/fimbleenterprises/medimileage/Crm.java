package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import java.sql.Time;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class Crm {
    private static final String TAG = "Crm";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    // public static final String DEFAULT_BASE_URL = MyApp.getAppContext().getString(R.string.base_server_url);
    // private static final String FCM_URL = "https://mediproxyrestapi.azurewebsites.net/api/crm/Fcm/";
    // public static final String BASE_URL = "http://192.168.16.135:44341/";
    private static AsyncHttpClient client = new AsyncHttpClient();
    private MySettingsHelper options;

    public Crm() {
        options = new MySettingsHelper(MyApp.getAppContext());
    }

    public static void userCanAuthenticate(String username, String password, final MyInterfaces.AuthenticationResult result) {

        final MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());

        Requests.Request request = new Requests.Request(Requests.Request.Function.CAN_AUTHENTICATE);
        request.arguments.add(new Requests.Argument(null, username));
        request.arguments.add(new Requests.Argument(null, password));
        Crm crm = new Crm();
        try {
            crm.makeCrmRequest(MyApp.getAppContext(), request, new AsyncHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                    String strResponse = new String(responseBody);
                    Log.d(TAG, "onSuccess " + strResponse);

                    // Added 1.5 - was authenticating everyone prior
                    if (strResponse != null && strResponse.equals(TRUE)) {
                        result.onSuccess();
                    } else if (strResponse != null && strResponse.equals(FALSE)) {
                        result.onFailure();
                    } else {
                        result.onError("UNKNOWN ERROR", null);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.w(TAG, "onFailure: " + error.getMessage());
                    result.onError(error.getMessage(), error);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "onClick: " + e.getMessage());
        }
    }

    public RequestHandle makeCrmRequest(Context context, Requests.Request request,
                                        final AsyncHttpResponseHandler responseHandler) {

        String argString = "";
        for (Requests.Argument arg : request.arguments) {
            argString = arg.toString() + "\n";
        }

        Log.i(TAG, "makeCrmRequest: Request function: " + request.function + " Request arguments: " + argString);

        StringEntity payload = null;
        try {
            payload = new StringEntity(request.toJson());
        } catch (Exception e) {
            e.printStackTrace();
            responseHandler.onFailure(0, null, null, e);
        }

        return client.post(context, options.getServerBaseUrl(), payload, "application/json",
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.i(TAG, "onSuccess : code = " + statusCode);
                        responseHandler.onSuccess(statusCode, headers, responseBody);
                    }

                    @Override
                    public void onProgress(long bytesWritten, long totalSize) {
                        super.onProgress(bytesWritten, totalSize);
                        responseHandler.onProgress(bytesWritten, totalSize);
                        Log.d(TAG, "onProgress | bytesWritten: " + bytesWritten + ", totalSize: " + totalSize);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.w(TAG, "onFailure: code: " + statusCode);
                        responseHandler.onFailure(statusCode, headers, responseBody, error);
                    }
                });
    }

    public RequestHandle makeCrmRequest(Context context, Requests.Request request, Timeout timeout,
                                        final MyInterfaces.CrmRequestListener listener) {

        String argString = "";
        for (Requests.Argument arg : request.arguments) {
            argString = arg.toString() + "\n";
        }

        Log.i(TAG, "makeCrmRequest: Request function: " + request.function + " Request arguments: " + argString);

        StringEntity payload = null;
        try {
            payload = new StringEntity(request.toJson());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (timeout == Timeout.LONG) {
            client.setTimeout(1000000);
        } else if (timeout == Timeout.SHORT){
            client.setTimeout(10000);
        }

        return client.post(context, options.getServerBaseUrl(), payload, "application/json",
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.i(TAG, "onSuccess : code = " + statusCode);
                        listener.onComplete(new String(responseBody));
                    }

                    @Override
                    public void onProgress(long bytesWritten, long totalSize) {
                        super.onProgress(bytesWritten, totalSize);
                        AsyncProgress proggy = new AsyncProgress(bytesWritten, totalSize);
                        listener.onProgress(proggy);
                        Log.d(TAG, "onProgress | KB written: " + proggy.getCompletedKb());
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.w(TAG, "onFailure: code: " + statusCode);
                        listener.onFail(error.getLocalizedMessage());
                    }

                });
    }

    public static RequestHandle get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        return client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static RequestHandle post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        return client.post(url, params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());
        return options.getServerBaseUrl() + relativeUrl;
    }

    /**
     * Returns a fully qualified URL to a specific record.
     * @param recordid The entityid of the specific record
     * @param entityTypeCode The numeric ID of the record (e.g. Account == 1, Case == 112)
     */
    public static String getRecordUrl(String recordid, String entityTypeCode) {
        String orgURL = "https://crmauth.medistim.com/main.aspx?";
        String recordURL = orgURL  + "etc=" + entityTypeCode + "&id=%7b" + recordid + "%7d&pagetype=entityrecord";
        return recordURL;
    }

    public enum Timeout {
        SHORT, LONG
    }

    public class AsyncProgress {
        private double bytesWritten = 1;
        private double totalSize = 1;

        public AsyncProgress(double bytesWritten, double totalSize) {

            try {
                this.bytesWritten = bytesWritten;
                this.totalSize = totalSize;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public double getCompletedBytes() {
            try {
                return this.bytesWritten;
            } catch (Exception e) {
                Log.w(TAG, "getCompletedBytes: " + e.getLocalizedMessage());
                return -1;
            }
        }

        public double getCompletedKb() {
            try {
                double completed = Helpers.Numbers.formatAsTwoDecimalPointNumber(Helpers.Files.convertBytesToKb(this.bytesWritten));
                return completed;
            } catch (Exception e) {
                Log.w(TAG, "getCompletedBytes: " + e.getLocalizedMessage());
                return -1;
            }
        }

        public double getCompletedMb() {
            try {
                double completed = Helpers.Numbers.formatAsTwoDecimalPointNumber(Helpers.Files.convertBytesToMb(this.bytesWritten));
                return completed;
            } catch (Exception e) {
                Log.w(TAG, "getCompletedBytes: " + e.getLocalizedMessage());
                return -1;
            }
        }
    }

    public static int tryGetEntityTypeCodeFromLogicalName(String entityLogicalName) {
        if (entityLogicalName.toLowerCase().equals("opportunity")) {
            return ETC_OPPORTUNITY;
        } else if (entityLogicalName.toLowerCase().equals("incident")) {
            return ETC_INCIDENT;
        } else if (entityLogicalName.toLowerCase().equals("contact")) {
            return ETC_CONTACT;
        } else if (entityLogicalName.toLowerCase().equals("account")) {
            return ETC_ACCOUNT;
        } else {
            return 0;
        }
    }

    // region Entity Type Codes
    public static final int ETC_ACCOUNT = 1;
    public static final int ETC_CONTACT = 2;
    public static final int ETC_OPPORTUNITY = 3;
    public static final int ETC_LEAD = 4;
    public static final int ETC_ANNOTATION = 5;
    public static final int ETC_BUSINESSUNITMAP = 6;
    public static final int ETC_OWNER = 7;
    public static final int ETC_SYSTEMUSER = 8;
    public static final int ETC_TEAM = 9;
    public static final int ETC_BUSINESSUNIT = 10;
    public static final int ETC_PRINCIPALOBJECTACCESS = 11;
    public static final int ETC_ROLEPRIVILEGES = 12;
    public static final int ETC_SYSTEMUSERLICENSES = 13;
    public static final int ETC_SYSTEMUSERPRINCIPALS = 14;
    public static final int ETC_SYSTEMUSERROLES = 15;
    public static final int ETC_ACCOUNTLEADS = 16;
    public static final int ETC_CONTACTINVOICES = 17;
    public static final int ETC_CONTACTQUOTES = 18;
    public static final int ETC_CONTACTORDERS = 19;
    public static final int ETC_SERVICECONTRACTCONTACTS = 20;
    public static final int ETC_PRODUCTSALESLITERATURE = 21;
    public static final int ETC_CONTACTLEADS = 22;
    public static final int ETC_TEAMMEMBERSHIP = 23;
    public static final int ETC_LEADCOMPETITORS = 24;
    public static final int ETC_OPPORTUNITYCOMPETITORS = 25;
    public static final int ETC_COMPETITORSALESLITERATURE = 26;
    public static final int ETC_LEADPRODUCT = 27;
    public static final int ETC_ROLETEMPLATEPRIVILEGES = 28;
    public static final int ETC_SUBSCRIPTION = 29;
    public static final int ETC_FILTERTEMPLATE = 30;
    public static final int ETC_PRIVILEGEOBJECTTYPECODES = 31;
    public static final int ETC_SALESPROCESSINSTANCE = 32;
    public static final int ETC_SUBSCRIPTIONSYNCINFO = 33;
    public static final int ETC_SUBSCRIPTIONTRACKINGDELETEDOBJECT = 35;
    public static final int ETC_CLIENTUPDATE = 36;
    public static final int ETC_SUBSCRIPTIONMANUALLYTRACKEDOBJECT = 37;
    public static final int ETC_SHAREDOBJECTSFORREAD = 39;
    public static final int ETC_TEAMROLES = 40;
    public static final int ETC_PRINCIPALENTITYMAP = 41;
    public static final int ETC_SYSTEMUSERBUSINESSUNITENTITYMAP = 42;
    public static final int ETC_PRINCIPALATTRIBUTEACCESSMAP = 43;
    public static final int ETC_PRINCIPALOBJECTATTRIBUTEACCESS = 44;
    public static final int ETC_SUBSCRIPTIONSTATISTICSOFFLINE = 45;
    public static final int ETC_SUBSCRIPTIONSTATISTICSOUTLOOK = 46;
    public static final int ETC_SUBSCRIPTIONSYNCENTRYOFFLINE = 47;
    public static final int ETC_SUBSCRIPTIONSYNCENTRYOUTLOOK = 48;
    public static final int ETC_POSITION = 50;
    public static final int ETC_SYSTEMUSERMANAGERMAP = 51;
    public static final int ETC_USERSEARCHFACET = 52;
    public static final int ETC_PRINCIPALOBJECTACCESSREADSNAPSHOT = 90;
    public static final int ETC_RECORDCOUNTSNAPSHOT = 91;
    public static final int ETC_TEAMTEMPLATE = 92;
    public static final int ETC_SOCIALPROFILE = 99;
    public static final int ETC_INCIDENT = 112;
    public static final int ETC_CHILDINCIDENTCOUNT = 113;
    public static final int ETC_COMPETITOR = 123;
    public static final int ETC_DOCUMENTINDEX = 126;
    public static final int ETC_KBARTICLE = 127;
    public static final int ETC_SUBJECT = 129;
    public static final int ETC_BUSINESSUNITNEWSARTICLE = 132;
    public static final int ETC_ACTIVITYPARTY = 135;
    public static final int ETC_USERSETTINGS = 150;
    public static final int ETC_NEWPROCESS = 950;
    public static final int ETC_TRANSLATIONPROCESS = 951;
    public static final int ETC_PHONETOCASEPROCESS = 952;
    public static final int ETC_OPPORTUNITYSALESPROCESS = 953;
    public static final int ETC_LEADTOOPPORTUNITYSALESPROCESS = 954;
    public static final int ETC_EXPIREDPROCESS = 955;
    public static final int ETC_ACTIVITYMIMEATTACHMENT = 1001;
    public static final int ETC_ATTACHMENT = 1002;
    public static final int ETC_INTERNALADDRESS = 1003;
    public static final int ETC_COMPETITORADDRESS = 1004;
    public static final int ETC_COMPETITORPRODUCT = 1006;
    public static final int ETC_IMAGEDESCRIPTOR = 1007;
    public static final int ETC_CONTRACT = 1010;
    public static final int ETC_CONTRACTDETAIL = 1011;
    public static final int ETC_DISCOUNT = 1013;
    public static final int ETC_KBARTICLETEMPLATE = 1016;
    public static final int ETC_LEADADDRESS = 1017;
    public static final int ETC_ORGANIZATION = 1019;
    public static final int ETC_ORGANIZATIONUI = 1021;
    public static final int ETC_PRICELEVEL = 1022;
    public static final int ETC_PRIVILEGE = 1023;
    public static final int ETC_PRODUCT = 1024;
    public static final int ETC_PRODUCTASSOCIATION = 1025;
    public static final int ETC_PRODUCTPRICELEVEL = 1026;
    public static final int ETC_PRODUCTSUBSTITUTE = 1028;
    public static final int ETC_SYSTEMFORM = 1030;
    public static final int ETC_USERFORM = 1031;
    public static final int ETC_ROLE = 1036;
    public static final int ETC_ROLETEMPLATE = 1037;
    public static final int ETC_SALESLITERATURE = 1038;
    public static final int ETC_SAVEDQUERY = 1039;
    public static final int ETC_intMAP = 1043;
    public static final int ETC_DYNAMICPROPERTY = 1048;
    public static final int ETC_DYNAMICPROPERTYOPTIONSETITEM = 1049;
    public static final int ETC_UOM = 1055;
    public static final int ETC_UOMSCHEDULE = 1056;
    public static final int ETC_SALESLITERATUREITEM = 1070;
    public static final int ETC_CUSTOMERADDRESS = 1071;
    public static final int ETC_SUBSCRIPTIONCLIENTS = 1072;
    public static final int ETC_STATUSMAP = 1075;
    public static final int ETC_DISCOUNTTYPE = 1080;
    public static final int ETC_KBARTICLECOMMENT = 1082;
    public static final int ETC_OPPORTUNITYPRODUCT = 1083;
    public static final int ETC_QUOTE = 1084;
    public static final int ETC_QUOTEDETAIL = 1085;
    public static final int ETC_USERFISCALCALENDAR = 1086;
    public static final int ETC_SALESORDER = 1088;
    public static final int ETC_SALESORDERDETAIL = 1089;
    public static final int ETC_INVOICE = 1090;
    public static final int ETC_INVOICEDETAIL = 1091;
    public static final int ETC_AUTHORIZATIONSERVER = 1094;
    public static final int ETC_PARTNERAPPLICATION = 1095;
    public static final int ETC_SAVEDQUERYVISUALIZATION = 1111;
    public static final int ETC_USERQUERYVISUALIZATION = 1112;
    public static final int ETC_RIBBONTABTOCOMMANDMAP = 1113;
    public static final int ETC_RIBBONCONTEXTGROUP = 1115;
    public static final int ETC_RIBBONCOMMAND = 1116;
    public static final int ETC_RIBBONRULE = 1117;
    public static final int ETC_RIBBONCUSTOMIZATION = 1120;
    public static final int ETC_RIBBONDIFF = 1130;
    public static final int ETC_REPLICATIONBACKLOG = 1140;
    public static final int ETC_CHARACTERISTIC = 1141;
    public static final int ETC_RATINGVALUE = 1142;
    public static final int ETC_RATINGMODEL = 1144;
    public static final int ETC_BOOKABLERESOURCEBOOKING = 1145;
    public static final int ETC_BOOKABLERESOURCEBOOKINGHEADER = 1146;
    public static final int ETC_BOOKABLERESOURCECATEGORY = 1147;
    public static final int ETC_BOOKABLERESOURCECHARACTERISTIC = 1148;
    public static final int ETC_BOOKABLERESOURCECATEGORYASSN = 1149;
    public static final int ETC_BOOKABLERESOURCE = 1150;
    public static final int ETC_BOOKABLERESOURCEGROUP = 1151;
    public static final int ETC_BOOKINGSTATUS = 1152;
    public static final int ETC_RECOMMENDEDDOCUMENT = 1189;
    public static final int ETC_FIELDSECURITYPROFILE = 1200;
    public static final int ETC_FIELDPERMISSION = 1201;
    public static final int ETC_SYSTEMUSERPROFILES = 1202;
    public static final int ETC_TEAMPROFILES = 1203;
    public static final int ETC_QUEUEMEMBERSHIP = 1213;
    public static final int ETC_CHANNELPROPERTYGROUP = 1234;
    public static final int ETC_DYNAMICPROPERTYASSOCIATION = 1235;
    public static final int ETC_CHANNELPROPERTY = 1236;
    public static final int ETC_SOCIALINSIGHTSCONFIGURATION = 1300;
    public static final int ETC_SAVEDORGINSIGHTSCONFIGURATION = 1309;
    public static final int ETC_DYNAMICPROPERTYINSTANCE = 1333;
    public static final int ETC_SYNCATTRIBUTEMAPPINGPROFILE = 1400;
    public static final int ETC_SYNCATTRIBUTEMAPPING = 1401;
    public static final int ETC_SYSTEMUSERSYNCMAPPINGPROFILES = 1402;
    public static final int ETC_TEAMSYNCATTRIBUTEMAPPINGPROFILES = 1403;
    public static final int ETC_PRINCIPALSYNCATTRIBUTEMAP = 1404;
    public static final int ETC_ANNUALFISCALCALENDAR = 2000;
    public static final int ETC_SEMIANNUALFISCALCALENDAR = 2001;
    public static final int ETC_QUARTERLYFISCALCALENDAR = 2002;
    public static final int ETC_MONTHLYFISCALCALENDAR = 2003;
    public static final int ETC_FIXEDMONTHLYFISCALCALENDAR = 2004;
    public static final int ETC_TEMPLATE = 2010;
    public static final int ETC_CONTRACTTEMPLATE = 2011;
    public static final int ETC_UNRESOLVEDADDRESS = 2012;
    public static final int ETC_TERRITORY = 2013;
    public static final int ETC_THEME = 2015;
    public static final int ETC_USERMAPPING = 2016;
    public static final int ETC_QUEUE = 2020;
    public static final int ETC_QUEUEITEMCOUNT = 2023;
    public static final int ETC_QUEUEMEMBERCOUNT = 2024;
    public static final int ETC_LICENSE = 2027;
    public static final int ETC_QUEUEITEM = 2029;
    public static final int ETC_USERENTITYUISETTINGS = 2500;
    public static final int ETC_USERENTITYINSTANCEDATA = 2501;
    public static final int ETC_INTEGRATIONSTATUS = 3000;
    public static final int ETC_CHANNELACCESSPROFILE = 3005;
    public static final int ETC_EXTERNALPARTY = 3008;
    public static final int ETC_CONNECTIONROLE = 3231;
    public static final int ETC_CONNECTIONROLEASSOCIATION = 3232;
    public static final int ETC_CONNECTIONROLEOBJECTTYPECODE = 3233;
    public static final int ETC_CONNECTION = 3234;
    public static final int ETC_EQUIPMENT = 4000;
    public static final int ETC_SERVICE = 4001;
    public static final int ETC_RESOURCE = 4002;
    public static final int ETC_CALENDAR = 4003;
    public static final int ETC_CALENDARRULE = 4004;
    public static final int ETC_RESOURCEGROUP = 4005;
    public static final int ETC_RESOURCESPEC = 4006;
    public static final int ETC_CONSTRAINTBASEDGROUP = 4007;
    public static final int ETC_SITE = 4009;
    public static final int ETC_RESOURCEGROUPEXPANSION = 4010;
    public static final int ETC_INTERPROCESSLOCK = 4011;
    public static final int ETC_EMAILHASH = 4023;
    public static final int ETC_DISPLAYintMAP = 4101;
    public static final int ETC_DISPLAYint = 4102;
    public static final int ETC_NOTIFICATION = 4110;
    public static final int ETC_EXCHANGESYNCIDMAPPING = 4120;
    public static final int ETC_ACTIVITYPOINTER = 4200;
    public static final int ETC_APPOINTMENT = 4201;
    public static final int ETC_EMAIL = 4202;
    public static final int ETC_FAX = 4204;
    public static final int ETC_INCIDENTRESOLUTION = 4206;
    public static final int ETC_LETTER = 4207;
    public static final int ETC_OPPORTUNITYCLOSE = 4208;
    public static final int ETC_ORDERCLOSE = 4209;
    public static final int ETC_PHONECALL = 4210;
    public static final int ETC_QUOTECLOSE = 4211;
    public static final int ETC_TASK = 4212;
    public static final int ETC_SERVICEAPPOINTMENT = 4214;
    public static final int ETC_COMMITMENT = 4215;
    public static final int ETC_SOCIALACTIVITY = 4216;
    public static final int ETC_UNTRACKEDEMAIL = 4220;
    public static final int ETC_USERQUERY = 4230;
    public static final int ETC_METADATADIFFERENCE = 4231;
    public static final int ETC_BUSINESSDATALOCALIZEDLABEL = 4232;
    public static final int ETC_RECURRENCERULE = 4250;
    public static final int ETC_RECURRINGAPPOINTMENTMASTER = 4251;
    public static final int ETC_EMAILSEARCH = 4299;
    public static final int ETC_LIST = 4300;
    public static final int ETC_LISTMEMBER = 4301;
    public static final int ETC_CAMPAIGN = 4400;
    public static final int ETC_CAMPAIGNRESPONSE = 4401;
    public static final int ETC_CAMPAIGNACTIVITY = 4402;
    public static final int ETC_CAMPAIGNITEM = 4403;
    public static final int ETC_CAMPAIGNACTIVITYITEM = 4404;
    public static final int ETC_BULKOPERATIONLOG = 4405;
    public static final int ETC_BULKOPERATION = 4406;
    public static final int ETC_IMPORT = 4410;
    public static final int ETC_IMPORTMAP = 4411;
    public static final int ETC_IMPORTFILE = 4412;
    public static final int ETC_IMPORTDATA = 4413;
    public static final int ETC_DUPLICATERULE = 4414;
    public static final int ETC_DUPLICATERECORD = 4415;
    public static final int ETC_DUPLICATERULECONDITION = 4416;
    public static final int ETC_COLUMNMAPPING = 4417;
    public static final int ETC_PICKLISTMAPPING = 4418;
    public static final int ETC_LOOKUPMAPPING = 4419;
    public static final int ETC_OWNERMAPPING = 4420;
    public static final int ETC_BOOKABLERESOURCEBOOKINGEXCHANGESYNCIDMAPPING = 4421;
    public static final int ETC_IMPORTLOG = 4423;
    public static final int ETC_BULKDELETEOPERATION = 4424;
    public static final int ETC_BULKDELETEFAILURE = 4425;
    public static final int ETC_TRANSFORMATIONMAPPING = 4426;
    public static final int ETC_TRANSFORMATIONPARAMETERMAPPING = 4427;
    public static final int ETC_IMPORTENTITYMAPPING = 4428;
    public static final int ETC_DATAPERFORMANCE = 4450;
    public static final int ETC_OFFICEDOCUMENT = 4490;
    public static final int ETC_RELATIONSHIPROLE = 4500;
    public static final int ETC_RELATIONSHIPROLEMAP = 4501;
    public static final int ETC_CUSTOMERRELATIONSHIP = 4502;
    public static final int ETC_CUSTOMEROPPORTUNITYROLE = 4503;
    public static final int ETC_ENTITLEMENTTEMPLATEPRODUCTS = 4545;
    public static final int ETC_AUDIT = 4567;
    public static final int ETC_ENTITYMAP = 4600;
    public static final int ETC_ATTRIBUTEMAP = 4601;
    public static final int ETC_PLUGINTYPE = 4602;
    public static final int ETC_PLUGINTYPESTATISTIC = 4603;
    public static final int ETC_PLUGINASSEMBLY = 4605;
    public static final int ETC_SDKMESSAGE = 4606;
    public static final int ETC_SDKMESSAGEFILTER = 4607;
    public static final int ETC_SDKMESSAGEPROCESSINGSTEP = 4608;
    public static final int ETC_SDKMESSAGEREQUEST = 4609;
    public static final int ETC_SDKMESSAGERESPONSE = 4610;
    public static final int ETC_SDKMESSAGERESPONSEFIELD = 4611;
    public static final int ETC_SDKMESSAGEPAIR = 4613;
    public static final int ETC_SDKMESSAGEREQUESTFIELD = 4614;
    public static final int ETC_SDKMESSAGEPROCESSINGSTEPIMAGE = 4615;
    public static final int ETC_SDKMESSAGEPROCESSINGSTEPSECURECONFIG = 4616;
    public static final int ETC_SERVICEENDPOINT = 4618;
    public static final int ETC_PLUGINTRACELOG = 4619;
    public static final int ETC_ASYNCOPERATION = 4700;
    public static final int ETC_WORKFLOWWAITSUBSCRIPTION = 4702;
    public static final int ETC_WORKFLOW = 4703;
    public static final int ETC_WORKFLOWDEPENDENCY = 4704;
    public static final int ETC_ISVCONFIG = 4705;
    public static final int ETC_WORKFLOWLOG = 4706;
    public static final int ETC_APPLICATIONFILE = 4707;
    public static final int ETC_ORGANIZATIONSTATISTIC = 4708;
    public static final int ETC_SITEMAP = 4709;
    public static final int ETC_PROCESSSESSION = 4710;
    public static final int ETC_PROCESSTRIGGER = 4712;
    public static final int ETC_PROCESSSTAGE = 4724;
    public static final int ETC_BUSINESSPROCESSFLOWINSTANCE = 4725;
    public static final int ETC_WEBWIZARD = 4800;
    public static final int ETC_WIZARDPAGE = 4802;
    public static final int ETC_WIZARDACCESSPRIVILEGE = 4803;
    public static final int ETC_TIMEZONEDEFINITION = 4810;
    public static final int ETC_TIMEZONERULE = 4811;
    public static final int ETC_TIMEZONELOCALIZEDNAME = 4812;
    public static final int ETC_ENTITLEMENTPRODUCTS = 6363;
    public static final int ETC_SYSTEMAPPLICATIONMETADATA = 7000;
    public static final int ETC_USERAPPLICATIONMETADATA = 7001;
    public static final int ETC_SOLUTION = 7100;
    public static final int ETC_PUBLISHER = 7101;
    public static final int ETC_PUBLISHERADDRESS = 7102;
    public static final int ETC_SOLUTIONCOMPONENT = 7103;
    public static final int ETC_DEPENDENCY = 7105;
    public static final int ETC_DEPENDENCYNODE = 7106;
    public static final int ETC_INVALIDDEPENDENCY = 7107;
    public static final int ETC_DEPENDENCYFEATURE = 7108;
    public static final int ETC_ENTITLEMENTCONTACTS = 7272;
    public static final int ETC_POST = 8000;
    public static final int ETC_POSTROLE = 8001;
    public static final int ETC_POSTREGARDING = 8002;
    public static final int ETC_POSTFOLLOW = 8003;
    public static final int ETC_POSTCOMMENT = 8005;
    public static final int ETC_POSTLIKE = 8006;
    public static final int ETC_TRACELOG = 8050;
    public static final int ETC_TRACEASSOCIATION = 8051;
    public static final int ETC_TRACEREGARDING = 8052;
    public static final int ETC_ROUTINGRULE = 8181;
    public static final int ETC_ROUTINGRULEITEM = 8199;
    public static final int ETC_HIERARCHYRULE = 8840;
    public static final int ETC_APPMODULE = 9006;
    public static final int ETC_APPMODULECOMPONENT = 9007;
    public static final int ETC_APPMODULEROLES = 9009;
    public static final int ETC_REPORT = 9100;
    public static final int ETC_REPORTENTITY = 9101;
    public static final int ETC_REPORTCATEGORY = 9102;
    public static final int ETC_REPORTVISIBILITY = 9103;
    public static final int ETC_REPORTLINK = 9104;
    public static final int ETC_TRANSACTIONCURRENCY = 9105;
    public static final int ETC_MAILMERGETEMPLATE = 9106;
    public static final int ETC_IMPORTJOB = 9107;
    public static final int ETC_LOCALCONFIGSTORE = 9201;
    public static final int ETC_CONVERTRULE = 9300;
    public static final int ETC_CONVERTRULEITEM = 9301;
    public static final int ETC_WEBRESOURCE = 9333;
    public static final int ETC_CHANNELACCESSPROFILERULE = 9400;
    public static final int ETC_CHANNELACCESSPROFILERULEITEM = 9401;
    public static final int ETC_CHANNELACCESSPROFILEENTITYACCESSLEVEL = 9404;
    public static final int ETC_SHAREPOINTSITE = 9502;
    public static final int ETC_SHAREPOINTDOCUMENT = 9507;
    public static final int ETC_SHAREPOINTDOCUMENTLOCATION = 9508;
    public static final int ETC_SHAREPOINTDATA = 9509;
    public static final int ETC_ROLLUPPROPERTIES = 9510;
    public static final int ETC_ROLLUPJOB = 9511;
    public static final int ETC_GOAL = 9600;
    public static final int ETC_GOALROLLUPQUERY = 9602;
    public static final int ETC_METRIC = 9603;
    public static final int ETC_ROLLUPFIELD = 9604;
    public static final int ETC_EMAILSERVERPROFILE = 9605;
    public static final int ETC_MAILBOX = 9606;
    public static final int ETC_MAILBOXSTATISTICS = 9607;
    public static final int ETC_MAILBOXTRACKINGFOLDER = 9608;
    public static final int ETC_SQLENCRYPTIONAUDIT = 9613;
    public static final int ETC_COMPLEXCONTROL = 9650;
    public static final int ETC_ORGINSIGHTSMETRIC = 9699;
    public static final int ETC_ENTITLEMENT = 9700;
    public static final int ETC_ENTITLEMENTCHANNEL = 9701;
    public static final int ETC_ENTITLEMENTTEMPLATE = 9702;
    public static final int ETC_ENTITLEMENTTEMPLATECHANNEL = 9703;
    public static final int ETC_SLA = 9750;
    public static final int ETC_SLAITEM = 9751;
    public static final int ETC_SLAKPIINSTANCE = 9752;
    public static final int ETC_CUSTOMCONTROL = 9753;
    public static final int ETC_CUSTOMCONTROLRESOURCE = 9754;
    public static final int ETC_CUSTOMCONTROLDEFAULTCONFIG = 9755;
    public static final int ETC_MOBILEOFFLINEPROFILE = 9866;
    public static final int ETC_MOBILEOFFLINEPROFILEITEM = 9867;
    public static final int ETC_MOBILEOFFLINEPROFILEITEMASSOCIATION = 9868;
    public static final int ETC_SYNCERROR = 9869;
    public static final int ETC_MULTIENTITYSEARCH = 9910;
    public static final int ETC_MULTIENTITYSEARCHENTITIES = 9911;
    public static final int ETC_HIERARCHYSECURITYCONFIGURATION = 9919;
    public static final int ETC_KNOWLEDGEBASERECORD = 9930;
    public static final int ETC_INCIDENTKNOWLEDGEBASERECORD = 9931;
    public static final int ETC_TIMESTAMPDATEMAPPING = 9932;
    public static final int ETC_RECOMMENDATIONMODEL = 9933;
    public static final int ETC_RECOMMENDATIONMODELMAPPING = 9934;
    public static final int ETC_RECOMMENDATIONMODELVERSION = 9935;
    public static final int ETC_AZURESERVICECONNECTION = 9936;
    public static final int ETC_RECOMMENDATIONMODELVERSIONHISTORY = 9937;
    public static final int ETC_RECOMMENDATIONCACHE = 9938;
    public static final int ETC_DOCUMENTTEMPLATE = 9940;
    public static final int ETC_PERSONALDOCUMENTTEMPLATE = 9941;
    public static final int ETC_TOPICMODELCONFIGURATION = 9942;
    public static final int ETC_TOPICMODELEXECUTIONHISTORY = 9943;
    public static final int ETC_TOPICMODEL = 9944;
    public static final int ETC_TEXTANALYTICSENTITYMAPPING = 9945;
    public static final int ETC_TOPICHISTORY = 9946;
    public static final int ETC_KNOWLEDGESEARCHMODEL = 9947;
    public static final int ETC_TOPIC = 9948;
    public static final int ETC_ADVANCEDSIMILARITYRULE = 9949;
    public static final int ETC_OFFICEGRAPHDOCUMENT = 9950;
    public static final int ETC_SIMILARITYRULE = 9951;
    public static final int ETC_KNOWLEDGEARTICLE = 9953;
    public static final int ETC_KNOWLEDGEARTICLEINCIDENT = 9954;
    public static final int ETC_KNOWLEDGEARTICLEVIEWS = 9955;
    public static final int ETC_LANGUAGELOCALE = 9957;
    public static final int ETC_FEEDBACK = 9958;
    public static final int ETC_CATEGORY = 9959;
    public static final int ETC_KNOWLEDGEARTICLESCATEGORIES = 9960;
    public static final int ETC_DELVEACTIONHUB = 9961;
    public static final int ETC_ACTIONCARD = 9962;
    public static final int ETC_ACTIONCARDUSERSTATE = 9968;
    public static final int ETC_ACTIONCARDUSERSETTINGS = 9973;
    public static final int ETC_CARDTYPE = 9983;
    public static final int ETC_INTERACTIONFOREMAIL = 9986;
    public static final int ETC_EXTERNALPARTYITEM = 9987;
    public static final int ETC_EMAILSIGNATURE = 9997;
    // endregion


}
