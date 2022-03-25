package com.fimbleenterprises.demobuddy

import android.content.Context
import android.util.Log
import com.fimbleenterprises.demobuddy.objects_and_containers.Requests
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.RequestHandle
import com.fimbleenterprises.demobuddy.objects_and_containers.custom_exceptions.CrmRequestExceptions.NullRequestException
import cz.msebera.android.httpclient.entity.StringEntity
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import java.lang.Exception

class KtCrm {

    private val options: MyPreferencesHelper

    fun makeCrmRequest(
        context: Context?, request: Requests.Request?,
        responseHandler: AsyncHttpResponseHandler
    ): RequestHandle {
        var argString = ""
        if (request == null) {
            responseHandler.onFailure(
                0,
                null,
                "Request was null!".toByteArray(),
                NullRequestException("The fucking request itself cannot be fucking null!")
            )
        }
        for (arg in request!!.arguments) {
            argString = """
                $arg
                
                """.trimIndent()
        }
        Log.i(
            TAG,
            "makeCrmRequest: Request function: " + request.function + " Request arguments: " + argString
        )
        var payload: StringEntity? = null
        try {
            payload = StringEntity(request.toJson())
        } catch (e: Exception) {
            e.printStackTrace()
            responseHandler.onFailure(0, null, null, e)
        }
        return client.post(context, options.serverBaseUrl, payload, "application/json",
            object : AsyncHttpResponseHandler() {
                override fun onSuccess(
                    statusCode: Int,
                    headers: Array<Header>,
                    responseBody: ByteArray
                ) {
                    Log.i(TAG, "onSuccess : code = $statusCode")
                    responseHandler.onSuccess(statusCode, headers, responseBody)
                }

                override fun onProgress(bytesWritten: Long, totalSize: Long) {
                    super.onProgress(bytesWritten, totalSize)
                    responseHandler.onProgress(bytesWritten, totalSize)
                    Log.d(TAG, "onProgress | bytesWritten: $bytesWritten, totalSize: $totalSize")
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Array<Header>,
                    responseBody: ByteArray,
                    error: Throwable
                ) {
                    Log.w(TAG, "onFailure: code: $statusCode")
                    responseHandler.onFailure(statusCode, headers, responseBody, error)
                }
            })
    }

    fun makeCrmRequest(
        context: Context?, request: Requests.Request, timeout: Timeout,
        responseHandler: AsyncHttpResponseHandler
    ): RequestHandle {
        var argString = ""
        for (arg in request.arguments) {
            argString = """
                $arg
                
                """.trimIndent()
        }
        Log.i(
            TAG,
            "makeCrmRequest: Request function: " + request.function + " Request arguments: " + argString
        )
        var payload: StringEntity? = null
        try {
            payload = StringEntity(request.toJson())
        } catch (e: Exception) {
            e.printStackTrace()
            responseHandler.onFailure(0, null, null, e)
        }
        if (timeout == Timeout.LONG) {
            client.setTimeout(1000000)
        } else if (timeout == Timeout.SHORT) {
            client.setTimeout(10000)
        }
        return client.post(context, options.serverBaseUrl, payload, "application/json",
            object : AsyncHttpResponseHandler() {
                override fun onSuccess(
                    statusCode: Int,
                    headers: Array<Header>,
                    responseBody: ByteArray
                ) {
                    Log.i(TAG, "onSuccess : code = $statusCode")
                    responseHandler.onSuccess(statusCode, headers, responseBody)
                }

                override fun onProgress(bytesWritten: Long, totalSize: Long) {
                    super.onProgress(bytesWritten, totalSize)
                    responseHandler.onProgress(bytesWritten, totalSize)
                    Log.d(TAG, "onProgress | bytesWritten: $bytesWritten, totalSize: $totalSize")
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Array<Header>,
                    responseBody: ByteArray,
                    error: Throwable
                ) {
                    Log.w(TAG, "onFailure: code: $statusCode")
                    responseHandler.onFailure(statusCode, headers, responseBody, error)
                }
            })
    }

    fun makeCrmRequest(
        context: Context?, request: Requests.Request, timeout: Timeout,
        listener: MyInterfaces.KtCrmRequestListener
    ): RequestHandle {
        var argString = ""
        for (arg in request.arguments) {
            argString = """
                $arg
                
                """.trimIndent()
        }
        Log.i(
            TAG,
            "makeCrmRequest: Request function: " + request.function + " Request arguments: " + argString
        )
        var payload: StringEntity? = null
        try {
            payload = StringEntity(request.toJson())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (timeout == Timeout.LONG) {
            client.setTimeout(9000000)
        } else if (timeout == Timeout.SHORT) {
            client.setTimeout(10000)
        }
        return client.post(context, options.serverBaseUrl, payload, "application/json",
            object : AsyncHttpResponseHandler() {
                override fun onSuccess(
                    statusCode: Int,
                    headers: Array<Header>,
                    responseBody: ByteArray
                ) {
                    Log.i(TAG, "onSuccess : code = $statusCode")
                    listener.onComplete(String(responseBody))
                }

                override fun onProgress(bytesWritten: Long, totalSize: Long) {
                    super.onProgress(bytesWritten, totalSize)
                    val proggy = AsyncProgress(
                        bytesWritten.toDouble(), totalSize.toDouble()
                    )
                    listener.onProgress(proggy)
                    Log.d(TAG, "onProgress | KB written: " + proggy.completedKb)
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Array<Header>,
                    responseBody: ByteArray,
                    error: Throwable
                ) {
                    Log.w(TAG, "onFailure: code: $statusCode")
                    listener.onFail(error.localizedMessage)
                }
            })
    }

    enum class Timeout {
        SHORT, LONG
    }

    class AsyncProgress(bytesWritten: Double, totalSize: Double) {
        private var bytesWritten = 1.0
        private var totalSize = 1.0
        val completedBytes: Double
            get() = try {
                bytesWritten
            } catch (e: Exception) {
                Log.w(TAG, "getCompletedBytes: " + e.localizedMessage)
                (-1).toDouble()
            }
        val completedKb: Double
            get() = try {
                Helpers.Numbers.formatAsTwoDecimalPointNumber(
                    Helpers.Files.convertBytesToKb(
                        bytesWritten
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "getCompletedBytes: " + e.localizedMessage)
                (-1).toDouble()
            }
        val completedMb: Double
            get() = try {
                Helpers.Numbers.formatAsTwoDecimalPointNumber(
                    Helpers.Files.convertBytesToMb(
                        bytesWritten
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "getCompletedBytes: " + e.localizedMessage)
                (-1).toDouble()
            }

        init {
            try {
                this.bytesWritten = bytesWritten
                this.totalSize = totalSize
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val TAG = "Crm"
        const val TRUE = "true"
        const val FALSE = "false"

        // public static final String DEFAULT_BASE_URL = MyApp.getAppContext().getString(R.string.base_server_url);
        // private static final String FCM_URL = "https://mediproxyrestapi.azurewebsites.net/api/crm/Fcm/";
        // public static final String BASE_URL = "http://192.168.16.135:44341/";
        private val client = AsyncHttpClient()
        fun userCanAuthenticate(
            username: String?,
            password: String?,
            result: MyInterfaces.AuthenticationResult
        ) {
            val options = MyPreferencesHelper(MyApp.getAppContext())
            val request = Requests.Request(Requests.Request.Function.CAN_AUTHENTICATE)
            request.arguments.add(Requests.Argument(null, username))
            request.arguments.add(Requests.Argument(null, password))
            val crm = KtCrm()
            try {
                crm.makeCrmRequest(
                    MyApp.getAppContext(),
                    request,
                    object : AsyncHttpResponseHandler() {
                        override fun onSuccess(
                            statusCode: Int,
                            headers: Array<Header>,
                            responseBody: ByteArray
                        ) {
                            val strResponse = String(responseBody)
                            Log.d(TAG, "onSuccess $strResponse")

                            // Added 1.5 - was authenticating everyone prior
                            if (strResponse != null && strResponse == TRUE) {
                                result.onSuccess()
                            } else if (strResponse != null && strResponse == FALSE) {
                                result.onFailure()
                            } else {
                                result.onError("UNKNOWN ERROR", null)
                            }
                        }

                        override fun onFailure(
                            statusCode: Int,
                            headers: Array<Header>,
                            responseBody: ByteArray,
                            error: Throwable
                        ) {
                            Log.w(TAG, "onFailure: " + error.message)
                            result.onError(error.message, error)
                        }
                    })
            } catch (e: Exception) {
                e.printStackTrace()
                Log.w(TAG, "onClick: " + e.message)
            }
        }

        operator fun get(
            url: String,
            params: RequestParams?,
            responseHandler: AsyncHttpResponseHandler?
        ): RequestHandle {
            return client[getAbsoluteUrl(url), params, responseHandler]
        }

        fun post(
            url: String?,
            params: RequestParams?,
            responseHandler: AsyncHttpResponseHandler?
        ): RequestHandle {
            return client.post(url, params, responseHandler)
        }

        private fun getAbsoluteUrl(relativeUrl: String): String {
            val options = MyPreferencesHelper(MyApp.getAppContext())
            return options.serverBaseUrl + relativeUrl
        }

        /**
         * Returns a fully qualified URL to a specific record.
         * @param recordid The entityid of the specific record
         * @param entityTypeCode The numeric ID of the record (e.g. Account == 1, Case == 112)
         */
        fun getRecordUrl(recordid: String, entityTypeCode: String): String {
            val orgURL = "https://crmauth.medistim.com/main.aspx?"
            return orgURL + "etc=" + entityTypeCode + "&id=%7b" + recordid + "%7d&pagetype=entityrecord"
        }

        fun tryGetEntityTypeCodeFromLogicalName(entityLogicalName: String): Int {
            return if (entityLogicalName.toLowerCase() == "opportunity") {
                ETC_OPPORTUNITY
            } else if (entityLogicalName.toLowerCase() == "incident") {
                ETC_INCIDENT
            } else if (entityLogicalName.toLowerCase() == "contact") {
                ETC_CONTACT
            } else if (entityLogicalName.toLowerCase() == "account") {
                ETC_ACCOUNT
            } else {
                0
            }
        }

        // region Entity Type Codes
        const val ETC_ACCOUNT = 1
        const val ETC_CONTACT = 2
        const val ETC_OPPORTUNITY = 3
        const val ETC_LEAD = 4
        const val ETC_ANNOTATION = 5
        const val ETC_BUSINESSUNITMAP = 6
        const val ETC_OWNER = 7
        const val ETC_SYSTEMUSER = 8
        const val ETC_TEAM = 9
        const val ETC_BUSINESSUNIT = 10
        const val ETC_PRINCIPALOBJECTACCESS = 11
        const val ETC_ROLEPRIVILEGES = 12
        const val ETC_SYSTEMUSERLICENSES = 13
        const val ETC_SYSTEMUSERPRINCIPALS = 14
        const val ETC_SYSTEMUSERROLES = 15
        const val ETC_ACCOUNTLEADS = 16
        const val ETC_CONTACTINVOICES = 17
        const val ETC_CONTACTQUOTES = 18
        const val ETC_CONTACTORDERS = 19
        const val ETC_SERVICECONTRACTCONTACTS = 20
        const val ETC_PRODUCTSALESLITERATURE = 21
        const val ETC_CONTACTLEADS = 22
        const val ETC_TEAMMEMBERSHIP = 23
        const val ETC_LEADCOMPETITORS = 24
        const val ETC_OPPORTUNITYCOMPETITORS = 25
        const val ETC_COMPETITORSALESLITERATURE = 26
        const val ETC_LEADPRODUCT = 27
        const val ETC_ROLETEMPLATEPRIVILEGES = 28
        const val ETC_SUBSCRIPTION = 29
        const val ETC_FILTERTEMPLATE = 30
        const val ETC_PRIVILEGEOBJECTTYPECODES = 31
        const val ETC_SALESPROCESSINSTANCE = 32
        const val ETC_SUBSCRIPTIONSYNCINFO = 33
        const val ETC_SUBSCRIPTIONTRACKINGDELETEDOBJECT = 35
        const val ETC_CLIENTUPDATE = 36
        const val ETC_SUBSCRIPTIONMANUALLYTRACKEDOBJECT = 37
        const val ETC_SHAREDOBJECTSFORREAD = 39
        const val ETC_TEAMROLES = 40
        const val ETC_PRINCIPALENTITYMAP = 41
        const val ETC_SYSTEMUSERBUSINESSUNITENTITYMAP = 42
        const val ETC_PRINCIPALATTRIBUTEACCESSMAP = 43
        const val ETC_PRINCIPALOBJECTATTRIBUTEACCESS = 44
        const val ETC_SUBSCRIPTIONSTATISTICSOFFLINE = 45
        const val ETC_SUBSCRIPTIONSTATISTICSOUTLOOK = 46
        const val ETC_SUBSCRIPTIONSYNCENTRYOFFLINE = 47
        const val ETC_SUBSCRIPTIONSYNCENTRYOUTLOOK = 48
        const val ETC_POSITION = 50
        const val ETC_SYSTEMUSERMANAGERMAP = 51
        const val ETC_USERSEARCHFACET = 52
        const val ETC_PRINCIPALOBJECTACCESSREADSNAPSHOT = 90
        const val ETC_RECORDCOUNTSNAPSHOT = 91
        const val ETC_TEAMTEMPLATE = 92
        const val ETC_SOCIALPROFILE = 99
        const val ETC_INCIDENT = 112
        const val ETC_CHILDINCIDENTCOUNT = 113
        const val ETC_COMPETITOR = 123
        const val ETC_DOCUMENTINDEX = 126
        const val ETC_KBARTICLE = 127
        const val ETC_SUBJECT = 129
        const val ETC_BUSINESSUNITNEWSARTICLE = 132
        const val ETC_ACTIVITYPARTY = 135
        const val ETC_USERSETTINGS = 150
        const val ETC_NEWPROCESS = 950
        const val ETC_TRANSLATIONPROCESS = 951
        const val ETC_PHONETOCASEPROCESS = 952
        const val ETC_OPPORTUNITYSALESPROCESS = 953
        const val ETC_LEADTOOPPORTUNITYSALESPROCESS = 954
        const val ETC_EXPIREDPROCESS = 955
        const val ETC_ACTIVITYMIMEATTACHMENT = 1001
        const val ETC_ATTACHMENT = 1002
        const val ETC_INTERNALADDRESS = 1003
        const val ETC_COMPETITORADDRESS = 1004
        const val ETC_COMPETITORPRODUCT = 1006
        const val ETC_IMAGEDESCRIPTOR = 1007
        const val ETC_CONTRACT = 1010
        const val ETC_CONTRACTDETAIL = 1011
        const val ETC_DISCOUNT = 1013
        const val ETC_KBARTICLETEMPLATE = 1016
        const val ETC_LEADADDRESS = 1017
        const val ETC_ORGANIZATION = 1019
        const val ETC_ORGANIZATIONUI = 1021
        const val ETC_PRICELEVEL = 1022
        const val ETC_PRIVILEGE = 1023
        const val ETC_PRODUCT = 1024
        const val ETC_PRODUCTASSOCIATION = 1025
        const val ETC_PRODUCTPRICELEVEL = 1026
        const val ETC_PRODUCTSUBSTITUTE = 1028
        const val ETC_SYSTEMFORM = 1030
        const val ETC_USERFORM = 1031
        const val ETC_ROLE = 1036
        const val ETC_ROLETEMPLATE = 1037
        const val ETC_SALESLITERATURE = 1038
        const val ETC_SAVEDQUERY = 1039
        const val ETC_intMAP = 1043
        const val ETC_DYNAMICPROPERTY = 1048
        const val ETC_DYNAMICPROPERTYOPTIONSETITEM = 1049
        const val ETC_UOM = 1055
        const val ETC_UOMSCHEDULE = 1056
        const val ETC_SALESLITERATUREITEM = 1070
        const val ETC_CUSTOMERADDRESS = 1071
        const val ETC_SUBSCRIPTIONCLIENTS = 1072
        const val ETC_STATUSMAP = 1075
        const val ETC_DISCOUNTTYPE = 1080
        const val ETC_KBARTICLECOMMENT = 1082
        const val ETC_OPPORTUNITYPRODUCT = 1083
        const val ETC_QUOTE = 1084
        const val ETC_QUOTEDETAIL = 1085
        const val ETC_USERFISCALCALENDAR = 1086
        const val ETC_SALESORDER = 1088
        const val ETC_SALESORDERDETAIL = 1089
        const val ETC_INVOICE = 1090
        const val ETC_INVOICEDETAIL = 1091
        const val ETC_AUTHORIZATIONSERVER = 1094
        const val ETC_PARTNERAPPLICATION = 1095
        const val ETC_SAVEDQUERYVISUALIZATION = 1111
        const val ETC_USERQUERYVISUALIZATION = 1112
        const val ETC_RIBBONTABTOCOMMANDMAP = 1113
        const val ETC_RIBBONCONTEXTGROUP = 1115
        const val ETC_RIBBONCOMMAND = 1116
        const val ETC_RIBBONRULE = 1117
        const val ETC_RIBBONCUSTOMIZATION = 1120
        const val ETC_RIBBONDIFF = 1130
        const val ETC_REPLICATIONBACKLOG = 1140
        const val ETC_CHARACTERISTIC = 1141
        const val ETC_RATINGVALUE = 1142
        const val ETC_RATINGMODEL = 1144
        const val ETC_BOOKABLERESOURCEBOOKING = 1145
        const val ETC_BOOKABLERESOURCEBOOKINGHEADER = 1146
        const val ETC_BOOKABLERESOURCECATEGORY = 1147
        const val ETC_BOOKABLERESOURCECHARACTERISTIC = 1148
        const val ETC_BOOKABLERESOURCECATEGORYASSN = 1149
        const val ETC_BOOKABLERESOURCE = 1150
        const val ETC_BOOKABLERESOURCEGROUP = 1151
        const val ETC_BOOKINGSTATUS = 1152
        const val ETC_RECOMMENDEDDOCUMENT = 1189
        const val ETC_FIELDSECURITYPROFILE = 1200
        const val ETC_FIELDPERMISSION = 1201
        const val ETC_SYSTEMUSERPROFILES = 1202
        const val ETC_TEAMPROFILES = 1203
        const val ETC_QUEUEMEMBERSHIP = 1213
        const val ETC_CHANNELPROPERTYGROUP = 1234
        const val ETC_DYNAMICPROPERTYASSOCIATION = 1235
        const val ETC_CHANNELPROPERTY = 1236
        const val ETC_SOCIALINSIGHTSCONFIGURATION = 1300
        const val ETC_SAVEDORGINSIGHTSCONFIGURATION = 1309
        const val ETC_DYNAMICPROPERTYINSTANCE = 1333
        const val ETC_SYNCATTRIBUTEMAPPINGPROFILE = 1400
        const val ETC_SYNCATTRIBUTEMAPPING = 1401
        const val ETC_SYSTEMUSERSYNCMAPPINGPROFILES = 1402
        const val ETC_TEAMSYNCATTRIBUTEMAPPINGPROFILES = 1403
        const val ETC_PRINCIPALSYNCATTRIBUTEMAP = 1404
        const val ETC_ANNUALFISCALCALENDAR = 2000
        const val ETC_SEMIANNUALFISCALCALENDAR = 2001
        const val ETC_QUARTERLYFISCALCALENDAR = 2002
        const val ETC_MONTHLYFISCALCALENDAR = 2003
        const val ETC_FIXEDMONTHLYFISCALCALENDAR = 2004
        const val ETC_TEMPLATE = 2010
        const val ETC_CONTRACTTEMPLATE = 2011
        const val ETC_UNRESOLVEDADDRESS = 2012
        const val ETC_TERRITORY = 2013
        const val ETC_THEME = 2015
        const val ETC_USERMAPPING = 2016
        const val ETC_QUEUE = 2020
        const val ETC_QUEUEITEMCOUNT = 2023
        const val ETC_QUEUEMEMBERCOUNT = 2024
        const val ETC_LICENSE = 2027
        const val ETC_QUEUEITEM = 2029
        const val ETC_USERENTITYUISETTINGS = 2500
        const val ETC_USERENTITYINSTANCEDATA = 2501
        const val ETC_INTEGRATIONSTATUS = 3000
        const val ETC_CHANNELACCESSPROFILE = 3005
        const val ETC_EXTERNALPARTY = 3008
        const val ETC_CONNECTIONROLE = 3231
        const val ETC_CONNECTIONROLEASSOCIATION = 3232
        const val ETC_CONNECTIONROLEOBJECTTYPECODE = 3233
        const val ETC_CONNECTION = 3234
        const val ETC_EQUIPMENT = 4000
        const val ETC_SERVICE = 4001
        const val ETC_RESOURCE = 4002
        const val ETC_CALENDAR = 4003
        const val ETC_CALENDARRULE = 4004
        const val ETC_RESOURCEGROUP = 4005
        const val ETC_RESOURCESPEC = 4006
        const val ETC_CONSTRAINTBASEDGROUP = 4007
        const val ETC_SITE = 4009
        const val ETC_RESOURCEGROUPEXPANSION = 4010
        const val ETC_INTERPROCESSLOCK = 4011
        const val ETC_EMAILHASH = 4023
        const val ETC_DISPLAYintMAP = 4101
        const val ETC_DISPLAYint = 4102
        const val ETC_NOTIFICATION = 4110
        const val ETC_EXCHANGESYNCIDMAPPING = 4120
        const val ETC_ACTIVITYPOINTER = 4200
        const val ETC_APPOINTMENT = 4201
        const val ETC_EMAIL = 4202
        const val ETC_FAX = 4204
        const val ETC_INCIDENTRESOLUTION = 4206
        const val ETC_LETTER = 4207
        const val ETC_OPPORTUNITYCLOSE = 4208
        const val ETC_ORDERCLOSE = 4209
        const val ETC_PHONECALL = 4210
        const val ETC_QUOTECLOSE = 4211
        const val ETC_TASK = 4212
        const val ETC_SERVICEAPPOINTMENT = 4214
        const val ETC_COMMITMENT = 4215
        const val ETC_SOCIALACTIVITY = 4216
        const val ETC_UNTRACKEDEMAIL = 4220
        const val ETC_USERQUERY = 4230
        const val ETC_METADATADIFFERENCE = 4231
        const val ETC_BUSINESSDATALOCALIZEDLABEL = 4232
        const val ETC_RECURRENCERULE = 4250
        const val ETC_RECURRINGAPPOINTMENTMASTER = 4251
        const val ETC_EMAILSEARCH = 4299
        const val ETC_LIST = 4300
        const val ETC_LISTMEMBER = 4301
        const val ETC_CAMPAIGN = 4400
        const val ETC_CAMPAIGNRESPONSE = 4401
        const val ETC_CAMPAIGNACTIVITY = 4402
        const val ETC_CAMPAIGNITEM = 4403
        const val ETC_CAMPAIGNACTIVITYITEM = 4404
        const val ETC_BULKOPERATIONLOG = 4405
        const val ETC_BULKOPERATION = 4406
        const val ETC_IMPORT = 4410
        const val ETC_IMPORTMAP = 4411
        const val ETC_IMPORTFILE = 4412
        const val ETC_IMPORTDATA = 4413
        const val ETC_DUPLICATERULE = 4414
        const val ETC_DUPLICATERECORD = 4415
        const val ETC_DUPLICATERULECONDITION = 4416
        const val ETC_COLUMNMAPPING = 4417
        const val ETC_PICKLISTMAPPING = 4418
        const val ETC_LOOKUPMAPPING = 4419
        const val ETC_OWNERMAPPING = 4420
        const val ETC_BOOKABLERESOURCEBOOKINGEXCHANGESYNCIDMAPPING = 4421
        const val ETC_IMPORTLOG = 4423
        const val ETC_BULKDELETEOPERATION = 4424
        const val ETC_BULKDELETEFAILURE = 4425
        const val ETC_TRANSFORMATIONMAPPING = 4426
        const val ETC_TRANSFORMATIONPARAMETERMAPPING = 4427
        const val ETC_IMPORTENTITYMAPPING = 4428
        const val ETC_DATAPERFORMANCE = 4450
        const val ETC_OFFICEDOCUMENT = 4490
        const val ETC_RELATIONSHIPROLE = 4500
        const val ETC_RELATIONSHIPROLEMAP = 4501
        const val ETC_CUSTOMERRELATIONSHIP = 4502
        const val ETC_CUSTOMEROPPORTUNITYROLE = 4503
        const val ETC_ENTITLEMENTTEMPLATEPRODUCTS = 4545
        const val ETC_AUDIT = 4567
        const val ETC_ENTITYMAP = 4600
        const val ETC_ATTRIBUTEMAP = 4601
        const val ETC_PLUGINTYPE = 4602
        const val ETC_PLUGINTYPESTATISTIC = 4603
        const val ETC_PLUGINASSEMBLY = 4605
        const val ETC_SDKMESSAGE = 4606
        const val ETC_SDKMESSAGEFILTER = 4607
        const val ETC_SDKMESSAGEPROCESSINGSTEP = 4608
        const val ETC_SDKMESSAGEREQUEST = 4609
        const val ETC_SDKMESSAGERESPONSE = 4610
        const val ETC_SDKMESSAGERESPONSEFIELD = 4611
        const val ETC_SDKMESSAGEPAIR = 4613
        const val ETC_SDKMESSAGEREQUESTFIELD = 4614
        const val ETC_SDKMESSAGEPROCESSINGSTEPIMAGE = 4615
        const val ETC_SDKMESSAGEPROCESSINGSTEPSECURECONFIG = 4616
        const val ETC_SERVICEENDPOINT = 4618
        const val ETC_PLUGINTRACELOG = 4619
        const val ETC_ASYNCOPERATION = 4700
        const val ETC_WORKFLOWWAITSUBSCRIPTION = 4702
        const val ETC_WORKFLOW = 4703
        const val ETC_WORKFLOWDEPENDENCY = 4704
        const val ETC_ISVCONFIG = 4705
        const val ETC_WORKFLOWLOG = 4706
        const val ETC_APPLICATIONFILE = 4707
        const val ETC_ORGANIZATIONSTATISTIC = 4708
        const val ETC_SITEMAP = 4709
        const val ETC_PROCESSSESSION = 4710
        const val ETC_PROCESSTRIGGER = 4712
        const val ETC_PROCESSSTAGE = 4724
        const val ETC_BUSINESSPROCESSFLOWINSTANCE = 4725
        const val ETC_WEBWIZARD = 4800
        const val ETC_WIZARDPAGE = 4802
        const val ETC_WIZARDACCESSPRIVILEGE = 4803
        const val ETC_TIMEZONEDEFINITION = 4810
        const val ETC_TIMEZONERULE = 4811
        const val ETC_TIMEZONELOCALIZEDNAME = 4812
        const val ETC_ENTITLEMENTPRODUCTS = 6363
        const val ETC_SYSTEMAPPLICATIONMETADATA = 7000
        const val ETC_USERAPPLICATIONMETADATA = 7001
        const val ETC_SOLUTION = 7100
        const val ETC_PUBLISHER = 7101
        const val ETC_PUBLISHERADDRESS = 7102
        const val ETC_SOLUTIONCOMPONENT = 7103
        const val ETC_DEPENDENCY = 7105
        const val ETC_DEPENDENCYNODE = 7106
        const val ETC_INVALIDDEPENDENCY = 7107
        const val ETC_DEPENDENCYFEATURE = 7108
        const val ETC_ENTITLEMENTCONTACTS = 7272
        const val ETC_POST = 8000
        const val ETC_POSTROLE = 8001
        const val ETC_POSTREGARDING = 8002
        const val ETC_POSTFOLLOW = 8003
        const val ETC_POSTCOMMENT = 8005
        const val ETC_POSTLIKE = 8006
        const val ETC_TRACELOG = 8050
        const val ETC_TRACEASSOCIATION = 8051
        const val ETC_TRACEREGARDING = 8052
        const val ETC_ROUTINGRULE = 8181
        const val ETC_ROUTINGRULEITEM = 8199
        const val ETC_HIERARCHYRULE = 8840
        const val ETC_APPMODULE = 9006
        const val ETC_APPMODULECOMPONENT = 9007
        const val ETC_APPMODULEROLES = 9009
        const val ETC_REPORT = 9100
        const val ETC_REPORTENTITY = 9101
        const val ETC_REPORTCATEGORY = 9102
        const val ETC_REPORTVISIBILITY = 9103
        const val ETC_REPORTLINK = 9104
        const val ETC_TRANSACTIONCURRENCY = 9105
        const val ETC_MAILMERGETEMPLATE = 9106
        const val ETC_IMPORTJOB = 9107
        const val ETC_LOCALCONFIGSTORE = 9201
        const val ETC_CONVERTRULE = 9300
        const val ETC_CONVERTRULEITEM = 9301
        const val ETC_WEBRESOURCE = 9333
        const val ETC_CHANNELACCESSPROFILERULE = 9400
        const val ETC_CHANNELACCESSPROFILERULEITEM = 9401
        const val ETC_CHANNELACCESSPROFILEENTITYACCESSLEVEL = 9404
        const val ETC_SHAREPOINTSITE = 9502
        const val ETC_SHAREPOINTDOCUMENT = 9507
        const val ETC_SHAREPOINTDOCUMENTLOCATION = 9508
        const val ETC_SHAREPOINTDATA = 9509
        const val ETC_ROLLUPPROPERTIES = 9510
        const val ETC_ROLLUPJOB = 9511
        const val ETC_GOAL = 9600
        const val ETC_GOALROLLUPQUERY = 9602
        const val ETC_METRIC = 9603
        const val ETC_ROLLUPFIELD = 9604
        const val ETC_EMAILSERVERPROFILE = 9605
        const val ETC_MAILBOX = 9606
        const val ETC_MAILBOXSTATISTICS = 9607
        const val ETC_MAILBOXTRACKINGFOLDER = 9608
        const val ETC_SQLENCRYPTIONAUDIT = 9613
        const val ETC_COMPLEXCONTROL = 9650
        const val ETC_ORGINSIGHTSMETRIC = 9699
        const val ETC_ENTITLEMENT = 9700
        const val ETC_ENTITLEMENTCHANNEL = 9701
        const val ETC_ENTITLEMENTTEMPLATE = 9702
        const val ETC_ENTITLEMENTTEMPLATECHANNEL = 9703
        const val ETC_SLA = 9750
        const val ETC_SLAITEM = 9751
        const val ETC_SLAKPIINSTANCE = 9752
        const val ETC_CUSTOMCONTROL = 9753
        const val ETC_CUSTOMCONTROLRESOURCE = 9754
        const val ETC_CUSTOMCONTROLDEFAULTCONFIG = 9755
        const val ETC_MOBILEOFFLINEPROFILE = 9866
        const val ETC_MOBILEOFFLINEPROFILEITEM = 9867
        const val ETC_MOBILEOFFLINEPROFILEITEMASSOCIATION = 9868
        const val ETC_SYNCERROR = 9869
        const val ETC_MULTIENTITYSEARCH = 9910
        const val ETC_MULTIENTITYSEARCHENTITIES = 9911
        const val ETC_HIERARCHYSECURITYCONFIGURATION = 9919
        const val ETC_KNOWLEDGEBASERECORD = 9930
        const val ETC_INCIDENTKNOWLEDGEBASERECORD = 9931
        const val ETC_TIMESTAMPDATEMAPPING = 9932
        const val ETC_RECOMMENDATIONMODEL = 9933
        const val ETC_RECOMMENDATIONMODELMAPPING = 9934
        const val ETC_RECOMMENDATIONMODELVERSION = 9935
        const val ETC_AZURESERVICECONNECTION = 9936
        const val ETC_RECOMMENDATIONMODELVERSIONHISTORY = 9937
        const val ETC_RECOMMENDATIONCACHE = 9938
        const val ETC_DOCUMENTTEMPLATE = 9940
        const val ETC_PERSONALDOCUMENTTEMPLATE = 9941
        const val ETC_TOPICMODELCONFIGURATION = 9942
        const val ETC_TOPICMODELEXECUTIONHISTORY = 9943
        const val ETC_TOPICMODEL = 9944
        const val ETC_TEXTANALYTICSENTITYMAPPING = 9945
        const val ETC_TOPICHISTORY = 9946
        const val ETC_KNOWLEDGESEARCHMODEL = 9947
        const val ETC_TOPIC = 9948
        const val ETC_ADVANCEDSIMILARITYRULE = 9949
        const val ETC_OFFICEGRAPHDOCUMENT = 9950
        const val ETC_SIMILARITYRULE = 9951
        const val ETC_KNOWLEDGEARTICLE = 9953
        const val ETC_KNOWLEDGEARTICLEINCIDENT = 9954
        const val ETC_KNOWLEDGEARTICLEVIEWS = 9955
        const val ETC_LANGUAGELOCALE = 9957
        const val ETC_FEEDBACK = 9958
        const val ETC_CATEGORY = 9959
        const val ETC_KNOWLEDGEARTICLESCATEGORIES = 9960
        const val ETC_DELVEACTIONHUB = 9961
        const val ETC_ACTIONCARD = 9962
        const val ETC_ACTIONCARDUSERSTATE = 9968
        const val ETC_ACTIONCARDUSERSETTINGS = 9973
        const val ETC_CARDTYPE = 9983
        const val ETC_INTERACTIONFOREMAIL = 9986
        const val ETC_EXTERNALPARTYITEM = 9987
        const val ETC_EMAILSIGNATURE = 9997 // endregion
    }

    init {
        options = MyPreferencesHelper(MyApp.getAppContext())
    }
}
