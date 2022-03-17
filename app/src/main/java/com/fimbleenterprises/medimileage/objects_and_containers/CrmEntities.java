package com.fimbleenterprises.medimileage.objects_and_containers;

import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.Crm;
import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.MyApp;
import com.fimbleenterprises.medimileage.MyInterfaces;
import com.fimbleenterprises.medimileage.MyPreferencesHelper;
import com.fimbleenterprises.medimileage.CrmQueries;
import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.objects_and_containers.BasicObjects.BasicObject;
import com.fimbleenterprises.medimileage.objects_and_containers.EntityContainers.EntityContainer;
import com.fimbleenterprises.medimileage.objects_and_containers.EntityContainers.EntityField;
import com.fimbleenterprises.medimileage.objects_and_containers.Requests.Request.Function;
import com.fimbleenterprises.medimileage.dialogs.MyProgressDialog;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cz.msebera.android.httpclient.Header;

public class CrmEntities {

    public static class CrmEntity {
        public String logicalname;
        public String pluralname;
        public String entityid;
        public String etag;
    }

    public static class Annotations {
        private static final String TAG = "Annotations";
        public ArrayList<Annotation> list = new ArrayList<>();
        public boolean hasNotes = false;

        public Annotations(String crmResponse) {
            try {
                JSONObject rootObject = new JSONObject(crmResponse);
                JSONArray array = rootObject.getJSONArray("value");
                hasNotes = array.length() > 0;
                for (int i = 0; i < array.length(); i++) {
                    JSONObject json = array.getJSONObject(i);
                    Annotation annotation = new Annotation(json);
                    this.list.add(annotation);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Returns a single annotation from CRM.
         * @param annotationid The annotation id.
         * @param includeAttachment Whether or not to retrieve the attachment if applicable
         * @param listener A callback.
        */
        public static void getAnnotationFromCrm(String annotationid, boolean includeAttachment, final MyInterfaces.CrmRequestListener listener) {
            String query = CrmQueries.Annotations.getAnnotation(annotationid, includeAttachment);
            Crm crm = new Crm();

            Requests.Request request = new Requests.Request(Function.GET);
            request.arguments.add(new Requests.Argument("query", query));

            crm.makeCrmRequest(MyApp.getAppContext(), request, Crm.Timeout.LONG, new MyInterfaces.CrmRequestListener() {
                @Override
                public void onComplete(Object result) {
                    listener.onComplete(result);
                }

                @Override
                public void onProgress(Crm.AsyncProgress progress) {
                    listener.onProgress(progress);
                }

                @Override
                public void onFail(String error) {
                    listener.onFail(error);
                }
            });
        }

        public static void showAddNoteDialog(final Context context, final String objectid, final MyInterfaces.CrmRequestListener listener) {

            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog_note);
            final EditText noteBody = dialog.findViewById(R.id.body_text);
            dialog.setTitle("Note");
            dialog.setCancelable(true);
            Button btnSubmit = dialog.findViewById(R.id.button_submit);
            btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();

                    CrmEntities.Annotations.Annotation newNote = new CrmEntities.Annotations.Annotation();
                    newNote.subject = "MileBuddy added note";
                    newNote.objectEntityName = "opportunity";
                    newNote.objectid = objectid;
                    newNote.notetext = noteBody.getText().toString();

                    final MyProgressDialog addEditNoteProgressDialog = new MyProgressDialog(context, "Working...");
                    addEditNoteProgressDialog.show();

                    newNote.submit(context, new MyInterfaces.CrmRequestListener() {
                        @Override
                        public void onComplete(Object result) {
                            Toast.makeText(context, "Note was added/edited!", Toast.LENGTH_SHORT).show();
                            addEditNoteProgressDialog.dismiss();
                            // Create result example (note the quotes): "1cd8d874-3412-eb11-810f-005056a36b9b"
                            // Update result example: {"WasSuccessful":true,"ResponseMessage":"Existing record was updated!","Guid":"00000000-0000-0000-0000-000000000000","WasCreated":false}

                            // Try to create an UpdateResponse object with the returned result.  If it
                            // succeeds then we know that it was an update operation that was executed.
                            // If it fails then it was almost certainly a successful create operation
                            // (create returns just the GUID of the new note)
                            CrmEntityResponse crmEntityResponse = new CrmEntityResponse(result.toString());
                            if (crmEntityResponse.wasSuccessful) {
                                Log.i(TAG, "onYes Note was updated!");
                                Toast.makeText(context, "Note was created.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                listener.onComplete(crmEntityResponse);
                            }
                        }

                        @Override
                        public void onProgress(Crm.AsyncProgress progress) {
                            listener.onProgress(progress);
                        }

                        @Override
                        public void onFail(String error) {
                            Toast.makeText(context, "Failed to add/edit note!\n\nError: " + error.toString(), Toast.LENGTH_SHORT).show();
                            addEditNoteProgressDialog.dismiss();
                            dialog.show();
                            listener.onFail(error);
                        }
                    });
                }
            });
            dialog.show();

        }

        public ArrayList<BasicObject> toBasicObjects() {
            ArrayList<BasicObject> objects = new ArrayList<>();
            for (Annotation annotation : this.list) {
                BasicObject object = new BasicObject();
                object.object = annotation;
                object.iconResource = R.drawable.text_message_icon_32x;
                object.topText = annotation.subject;
                object.middleText = annotation.notetext;
                object.bottomText = annotation.createdByName;
                object.bottomRightText = Helpers.DatesAndTimes.getPrettyDateAndTime(annotation.createdon);
                object.dateTime = annotation.createdon;
                objects.add(object);
            }
            return objects;
        }

        public static class Annotation extends CrmEntity implements Parcelable {

            private static final String TAG = "Annotation";

            // public String etag;
            // public String entityid;
            public String objectid;
            public String filename;
            public String documentBody;
            public int filesize;
            public boolean isDocument;
            public String subject;
            public String notetext;
            public String objectEntityName;
            public DateTime createdon;
            public DateTime modifiedon;
            public String modifiedByValue;
            public String modifedByName;
            public String createdByValue;
            public String createdByName;
            public String mimetype;
            public boolean inUse = false;

            public Annotation() { }

            public Annotation(JSONObject json) {

                try {
                    if (!json.isNull("etag")) {
                        this.etag = (json.getString("etag"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("documentbody")) {
                        this.documentBody = (json.getString("documentbody"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("filesize")) {
                        this.filesize = (json.getInt("filesize"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("filename")) {
                        this.filename = (json.getString("filename"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("annotationid")) {
                        this.entityid = (json.getString("annotationid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_objectid_value")) {
                        this.objectid = (json.getString("_objectid_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("objecttypecode")) {
                        this.objectEntityName = (json.getString("objecttypecode"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("isdocument")) {
                        this.isDocument = (json.getBoolean("isdocument"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("subject")) {
                        this.subject = (json.getString("subject"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("notetext")) {
                        this.notetext = (json.getString("notetext"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_modifiedby_valueFormattedValue")) {
                        this.modifedByName = (json.getString("_modifiedby_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_modifiedby_value")) {
                        this.modifiedByValue = (json.getString("_modifiedby_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_createdby_valueFormattedValue")) {
                        this.createdByName = (json.getString("_createdby_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_createdby_value")) {
                        this.createdByValue = (json.getString("_createdby_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("mimetype")) {
                        this.mimetype = (json.getString("mimetype"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("modifiedon")) {
                        this.modifiedon = (new DateTime(json.getString("modifiedon")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("createdon")) {
                        this.createdon = (new DateTime(json.getString("createdon")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String toString() {
                return this.subject + " (attachment: " + isDocument + ")";
            }

            /**
             * Creates a new or edits an existing note on the CRM server.  Add/Edit is determined
             * based on whether or not the annotation object has an annotationid or not (is null).
             * @param context A context suitable to execute a Crm request.
             * @param listener A simple listener to handle the callback.
             */
            public void submit(Context context, final MyInterfaces.CrmRequestListener listener) {

                try {
                    MileBuddyMetrics.updateMetric(context, MileBuddyMetrics.MetricName.LAST_CREATED_NOTE, DateTime.now());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (this.entityid == null) {
                    // Create new note

                    // The annotation entity is slightly different so instead of a basic EntityContainer
                    // we create an AnnotationCreationContainer for the creation request
                    EntityContainers.AnnotationCreationContainer annotationContainer =
                            new EntityContainers.AnnotationCreationContainer();
                    annotationContainer.notetext = this.notetext;
                    annotationContainer.subject = this.subject;
                    annotationContainer.objectidtypecode = objectEntityName;
                    annotationContainer.objectid = objectid;
                    if (this.documentBody != null) {
                        annotationContainer.documentbody = this.documentBody;
                    }

                    final Requests.Request request = new Requests.Request(Requests.Request.Function.CREATE_NOTE);
                    request.arguments.add(new Requests.Argument("noteobject", annotationContainer.toJson()));

                    Crm crm = new Crm();
                    crm.makeCrmRequest(context, request, Crm.Timeout.LONG, new MyInterfaces.CrmRequestListener() {
                        @Override
                        public void onComplete(Object result) {
                            listener.onComplete(result);
                        }

                        @Override
                        public void onProgress(Crm.AsyncProgress progress) {
                            listener.onProgress(progress);
                        }

                        @Override
                        public void onFail(String error) {
                            listener.onFail(error);
                        }
                    });
                } else {
                    String originalSubject = this.subject;
                    EntityContainer entityContainer = new EntityContainer();
                    entityContainer.entityFields.add(new EntityField("notetext", this.notetext));
                    entityContainer.entityFields.add(new EntityField("subject", this.subject));
                    // entityContainer.entityFields.add(new EntityField("isdocument", Boolean.toString(this.isDocument)));
                    // entityContainer.entityFields.add(new EntityField("documentbody", this.documentBody));
                    // entityContainer.entityFields.add(new EntityField("mimetype", this.mimetype));
                    // entityContainer.entityFields.add(new EntityField("filename", this.filename));

                    // Update existing note
                    Requests.Request request = new Requests.Request(Function.UPDATE);
                    request.arguments.add(new Requests.Argument("guid", this.entityid));
                    request.arguments.add(new Requests.Argument("entityname", "annotation"));
                    request.arguments.add(new Requests.Argument("container", entityContainer.toJson()));
                    request.arguments.add(new Requests.Argument("asuserid", MediUser.getMe().systemuserid));

                    Crm crm = new Crm();
                    crm.makeCrmRequest(context, request, Crm.Timeout.LONG, new MyInterfaces.CrmRequestListener() {
                                @Override
                                public void onComplete(Object result) {
                                    listener.onComplete(result);
                                }

                                @Override
                                public void onProgress(Crm.AsyncProgress progress) {
                                    listener.onProgress(progress);
                                }

                                @Override
                                public void onFail(String error) {
                                    listener.onFail(error);
                                }
                            });

                    /*
                    string guid = (string)value.Arguments[0].value;
                    entityName = (string)value.Arguments[1].value;
                    container = JsonConvert.DeserializeObject<EntityContainer>((string)value.Arguments[2].value);
                    asUserid = (string)value.Arguments[3].value;
                     */
                }
            }

            public void delete(Context context, final MyInterfaces.YesNoResult listener) {
                // Args:
                // 0: Entity name
                // 1: Entityid
                // 2: AsUserid

                Requests.Request request = new Requests.Request(Function.DELETE);
                request.arguments.add(new Requests.Argument("entityname", "annotation"));
                request.arguments.add(new Requests.Argument("entityid", this.entityid));
                request.arguments.add(new Requests.Argument("asuserid", MediUser.getMe().systemuserid));

                Crm crm = new Crm();
                crm.makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.i(TAG, "onSuccess Note was deleted (" + new String(responseBody) + ")");
                        listener.onYes(new String(responseBody));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.w(TAG, "onFailure: Failed to delete note (" + new String(responseBody) + ")");
                        listener.onNo(error.getLocalizedMessage());
                    }
                });
            }

            /**
             * Creates a new or edits an existing note on the CRM server.  Add/Edit is determined
             * based on whether or not the annotation object has an annotationid or not (is null).
             * @param context A context suitable to execute a Crm request.
             * @param listener A simple listener to handle the callback.
             */
            public void addAttachment(Context context, final MyInterfaces.CrmRequestListener listener) {

                if (this.entityid == null) {
                    // Create new note

                    // The annotation entity is slightly different so instead of a basic EntityContainer
                    // we create an AnnotationCreationContainer for the creation request
                    EntityContainers.AnnotationCreationContainer annotationContainer =
                            new EntityContainers.AnnotationCreationContainer();
                    annotationContainer.notetext = this.notetext;
                    annotationContainer.subject = this.subject;
                    annotationContainer.objectidtypecode = "opportunity";
                    annotationContainer.objectid = objectid;
                    if (this.documentBody != null) {
                        annotationContainer.documentbody = this.documentBody;
                    }

                    final Requests.Request request = new Requests.Request(Requests.Request.Function.CREATE_NOTE);
                    request.arguments.add(new Requests.Argument("noteobject", annotationContainer.toJson()));

                    Crm crm = new Crm();
                    crm.makeCrmRequest(context, request, Crm.Timeout.LONG, new MyInterfaces.CrmRequestListener() {
                        @Override
                        public void onComplete(Object result) {
                            listener.onComplete(result);
                        }

                        @Override
                        public void onProgress(Crm.AsyncProgress progress) {
                            listener.onProgress(progress);
                        }

                        @Override
                        public void onFail(String error) {
                            listener.onFail(error);
                        }
                    });
                } else {
                    EntityContainer entityContainer = new EntityContainer();
                    entityContainer.entityFields.add(new EntityField("notetext", this.notetext));
                    entityContainer.entityFields.add(new EntityField("subject", this.subject));
                    entityContainer.entityFields.add(new EntityField("isdocument", Boolean.toString(this.isDocument)));
                    entityContainer.entityFields.add(new EntityField("documentbody", this.documentBody));
                    entityContainer.entityFields.add(new EntityField("mimetype", this.mimetype));
                    entityContainer.entityFields.add(new EntityField("filename", this.filename));

                    // Build request to update the existing note on the server
                    Requests.Request request = new Requests.Request(Function.UPDATE);
                    request.arguments.add(new Requests.Argument("guid", this.entityid));
                    request.arguments.add(new Requests.Argument("entityname", "annotation"));
                    request.arguments.add(new Requests.Argument("container", entityContainer.toJson()));
                    request.arguments.add(new Requests.Argument("asuserid", MediUser.getMe().systemuserid));

                    Crm crm = new Crm();
                    crm.makeCrmRequest(context, request, Crm.Timeout.LONG, new MyInterfaces.CrmRequestListener() {
                        @Override
                        public void onComplete(Object result) {
                            listener.onComplete(result);
                        }

                        @Override
                        public void onProgress(Crm.AsyncProgress progress) {
                            listener.onProgress(progress);
                        }

                        @Override
                        public void onFail(String error) {
                            listener.onFail(error);
                        }
                    });

                    /*
                    string guid = (string)value.Arguments[0].value;
                    entityName = (string)value.Arguments[1].value;
                    container = JsonConvert.DeserializeObject<EntityContainer>((string)value.Arguments[2].value);
                    asUserid = (string)value.Arguments[3].value;
                     */
                }
            }

            public void removeAttachment(Context context, final MyInterfaces.YesNoResult listener) {
                // Args:
                // 0: Entity name
                // 1: Entityid
                // 2: AsUserid

                /*
                string guid = (string)value.Arguments[0].value;
                entityName = (string)value.Arguments[1].value;
                container = JsonConvert.DeserializeObject<EntityContainer>((string)value.Arguments[2].value);
                asUserid = (string)value.Arguments[3].value;
                */

                EntityContainer container = new EntityContainer();
                container.entityFields.add(new EntityField("documentbody", null));
                container.entityFields.add(new EntityField("isdocument", "false"));
                container.entityFields.add(new EntityField("filesize", "0"));
                container.entityFields.add(new EntityField("filename", ""));
                container.entityFields.add(new EntityField("mimetype", ""));

                Requests.Request request = new Requests.Request(Function.UPDATE);
                request.arguments.add(new Requests.Argument("entityid", this.entityid));
                request.arguments.add(new Requests.Argument("entityname", "annotation"));
                request.arguments.add(new Requests.Argument("container", container.toJson()));
                request.arguments.add(new Requests.Argument("asuserid", MediUser.getMe().systemuserid));

                Crm crm = new Crm();
                crm.makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.i(TAG, "onSuccess Attachment was deleted (" + new String(responseBody) + ")");
                        listener.onYes(new String(responseBody));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.w(TAG, "onFailure: Failed to delete note (" + new String(responseBody) + ")");
                        listener.onNo(error.getLocalizedMessage());
                    }
                });
            }

            public boolean belongsTo(String systemuserid) {
                if (systemuserid == null || this.createdByValue == null) {
                    return false;
                }

                return (systemuserid.equals(this.createdByValue));
            }


            protected Annotation(Parcel in) {
                etag = in.readString();
                entityid = in.readString();
                objectid = in.readString();
                filename = in.readString();
                documentBody = in.readString();
                filesize = in.readInt();
                isDocument = in.readByte() != 0x00;
                subject = in.readString();
                notetext = in.readString();
                objectEntityName = in.readString();
                createdon = (DateTime) in.readValue(DateTime.class.getClassLoader());
                modifiedon = (DateTime) in.readValue(DateTime.class.getClassLoader());
                modifiedByValue = in.readString();
                modifedByName = in.readString();
                createdByValue = in.readString();
                createdByName = in.readString();
                mimetype = in.readString();
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(etag);
                dest.writeString(entityid);
                dest.writeString(objectid);
                dest.writeString(filename);
                dest.writeString(documentBody);
                dest.writeInt(filesize);
                dest.writeByte((byte) (isDocument ? 0x01 : 0x00));
                dest.writeString(subject);
                dest.writeString(notetext);
                dest.writeString(objectEntityName);
                dest.writeValue(createdon);
                dest.writeValue(modifiedon);
                dest.writeString(modifiedByValue);
                dest.writeString(modifedByName);
                dest.writeString(createdByValue);
                dest.writeString(createdByName);
                dest.writeString(mimetype);
            }

            @SuppressWarnings("unused")
            public static final Parcelable.Creator<Annotation> CREATOR = new Parcelable.Creator<Annotation>() {
                @Override
                public Annotation createFromParcel(Parcel in) {
                    return new Annotation(in);
                }

                @Override
                public Annotation[] newArray(int size) {
                    return new Annotation[size];
                }
            };
        }

    }

    public static class OrderProducts {
        public ArrayList<OrderProduct> list = new ArrayList<>();

        public int size() {
            return this.list.size();
        }

        @Override
        public String toString() {
            return "Order products: " + this.list.size();
        }

        public OrderProducts() { }

        public OrderProducts(String crmResponse) {
            ArrayList<OrderProduct> orderProducts = new ArrayList<>();
            try {
                JSONObject root = new JSONObject(crmResponse);
                JSONArray rootArray = root.getJSONArray("value");
                for (int i = 0; i < rootArray.length(); i++) {
                    OrderProduct orderProduct = new OrderProduct(rootArray.getJSONObject(i));
                    orderProducts.add(orderProduct);
                }
                this.list = orderProducts;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public static class OrderProduct extends CrmEntity implements Parcelable {

            public int groupid = 0;
            public boolean isClickableHeader = false;
            public boolean isHeader;
            // public String etag;
            public String productid;
            public String productidFormatted;
            public String partNumber;
            public float extendedAmt;
            public String extendedAmtFormatted;
            public String customerid;
            public String customeridFormatted;
            public String salesorderid;
            public String salesorderidFormatted;
            public String salesrepid;
            public String salesrepidFormatted;
            public float priceperunit;
            public String priceperunitFormatted;
            public String itemgroup;
            public int qty;
            public boolean isCapital;
            public String territoryid;
            public String territoryidFormatted;
            public String accountnumber;
            public String orderdateFormatted;
            public DateTime orderDate;
            public int productfamilyValue;
            public String productfamilyFormattedValue;

            @Override
            public String toString() {
                return this.partNumber + ", Qty: " + this.qty + ", Amount: " + this.extendedAmtFormatted;
            }

            public OrderProduct() { }

            public OrderProduct(JSONObject json) {
                try {
                    if (!json.isNull("orderdetailid")) {
                        this.entityid = (json.getString("orderdetailid"));
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
                    if (!json.isNull("_productid_value")) {
                        this.productid = (json.getString("_productid_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_productid_valueFormattedValue")) {
                        this.productidFormatted = (json.getString("_productid_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("extendedamount")) {
                        this.extendedAmt = (json.getLong("extendedamount"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("extendedamountFormattedValue")) {
                        this.extendedAmtFormatted = (json.getString("extendedamountFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_new_customer_value")) {
                        this.customerid = (json.getString("_new_customer_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_new_customer_valueFormattedValue")) {
                        this.customeridFormatted = (json.getString("_new_customer_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_salesrepid_valueFormattedValue")) {
                        this.salesrepidFormatted = (json.getString("_salesrepid_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_salesrepid_value")) {
                        this.salesrepid = (json.getString("_salesrepid_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("quantity")) {
                        this.qty = (json.getInt("quantity"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_salesorderid_valueFormattedValue")) {
                        this.salesorderidFormatted = (json.getString("_salesorderid_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_salesorderid_value")) {
                        this.salesorderid = (json.getString("_salesorderid_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_070ef9d142cd40d98bebd513e03c7cd1_msus_is_capital")) {
                        this.isCapital = (json.getBoolean("a_070ef9d142cd40d98bebd513e03c7cd1_msus_is_capital"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_070ef9d142cd40d98bebd513e03c7cd1_productnumber")) {
                        this.setTitle(json.getString("a_070ef9d142cd40d98bebd513e03c7cd1_productnumber"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_070ef9d142cd40d98bebd513e03c7cd1_col_itemgroup")) {
                        this.itemgroup = (json.getString("a_070ef9d142cd40d98bebd513e03c7cd1_col_itemgroup"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_070ef9d142cd40d98bebd513e03c7cd1_col_producfamily")) {
                        this.productfamilyValue = (json.getInt("a_070ef9d142cd40d98bebd513e03c7cd1_col_producfamily"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_070ef9d142cd40d98bebd513e03c7cd1_col_producfamilyFormattedValue")) {
                        this.productfamilyFormattedValue = (json.getString("a_070ef9d142cd40d98bebd513e03c7cd1_col_producfamilyFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_db24f99da8fee71180df005056a36b9b_accountnumber")) {
                        this.accountnumber = (json.getString("a_db24f99da8fee71180df005056a36b9b_accountnumber"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_db24f99da8fee71180df005056a36b9b_territoryid")) {
                        this.territoryid = (json.getString("a_db24f99da8fee71180df005056a36b9b_territoryid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_a1cf96c07c114d478335b8c445651a12_employeeid")) {
                        this.territoryidFormatted = (json.getString("a_a1cf96c07c114d478335b8c445651a12_employeeid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_6ec0e72e4c104394bc627456c6412838_submitdate")) {
                        this.orderDate = (new DateTime(json.getString("a_6ec0e72e4c104394bc627456c6412838_submitdate")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_6ec0e72e4c104394bc627456c6412838_submitdateFormattedValue")) {
                        this.orderdateFormatted = (json.getString("a_6ec0e72e4c104394bc627456c6412838_submitdateFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            public enum ProductFamily {
                AUX_CABLE, FLOWMETER, LEASE_COMPLIANCE, LICENSE_CARD, PROBE, PROBE_PRODUCT, SERVICE_MISC,
                SPARE_PART, SYSTEM_PRODUCT, SHIPPING_HANDLING
            }

            public ProductFamily getFamily() {
                switch (this.productfamilyValue) {
                    case 100004070 :
                        return ProductFamily.AUX_CABLE;
                    case 181004050 :
                        return ProductFamily.FLOWMETER;
                    case 100004040 :
                        return ProductFamily.LEASE_COMPLIANCE;
                    case 100004030 :
                        return ProductFamily.LICENSE_CARD;
                    case 181400001 :
                        return ProductFamily.PROBE_PRODUCT;
                    case 100004010 :
                        return ProductFamily.SERVICE_MISC;
                    case 100004020 :
                        return ProductFamily.SHIPPING_HANDLING;
                    case 100004075 :
                        return ProductFamily.SPARE_PART;
                    case 181400000 :
                        return ProductFamily.SYSTEM_PRODUCT;
                    default :
                        return ProductFamily.PROBE;
                }
            }

            public void setTitle(String text) {
                this.partNumber = text;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeByte(this.isHeader ? (byte) 1 : (byte) 0);
                dest.writeString(this.productid);
                dest.writeString(this.productidFormatted);
                dest.writeString(this.partNumber);
                dest.writeFloat(this.extendedAmt);
                dest.writeString(this.extendedAmtFormatted);
                dest.writeString(this.customerid);
                dest.writeString(this.customeridFormatted);
                dest.writeString(this.salesorderid);
                dest.writeString(this.salesorderidFormatted);
                dest.writeString(this.salesrepid);
                dest.writeString(this.salesrepidFormatted);
                dest.writeFloat(this.priceperunit);
                dest.writeString(this.priceperunitFormatted);
                dest.writeString(this.itemgroup);
                dest.writeInt(this.qty);
                dest.writeByte(this.isCapital ? (byte) 1 : (byte) 0);
                dest.writeString(this.territoryid);
                dest.writeString(this.territoryidFormatted);
                dest.writeString(this.accountnumber);
                dest.writeString(this.orderdateFormatted);
                dest.writeSerializable(this.orderDate);
                dest.writeInt(this.productfamilyValue);
                dest.writeString(this.productfamilyFormattedValue);
                dest.writeString(this.logicalname);
                dest.writeString(this.pluralname);
                dest.writeString(this.entityid);
                dest.writeString(this.etag);
                dest.writeInt(this.groupid);
            }

            public void readFromParcel(Parcel source) {
                this.isHeader = source.readByte() != 0;
                this.productid = source.readString();
                this.productidFormatted = source.readString();
                this.partNumber = source.readString();
                this.extendedAmt = source.readFloat();
                this.extendedAmtFormatted = source.readString();
                this.customerid = source.readString();
                this.customeridFormatted = source.readString();
                this.salesorderid = source.readString();
                this.salesorderidFormatted = source.readString();
                this.salesrepid = source.readString();
                this.salesrepidFormatted = source.readString();
                this.priceperunit = source.readFloat();
                this.priceperunitFormatted = source.readString();
                this.itemgroup = source.readString();
                this.qty = source.readInt();
                this.isCapital = source.readByte() != 0;
                this.territoryid = source.readString();
                this.territoryidFormatted = source.readString();
                this.accountnumber = source.readString();
                this.orderdateFormatted = source.readString();
                this.orderDate = (DateTime) source.readSerializable();
                this.productfamilyValue = source.readInt();
                this.productfamilyFormattedValue = source.readString();
                this.logicalname = source.readString();
                this.pluralname = source.readString();
                this.entityid = source.readString();
                this.etag = source.readString();
                this.groupid = source.readInt();
            }

            protected OrderProduct(Parcel in) {
                this.isHeader = in.readByte() != 0;
                this.productid = in.readString();
                this.productidFormatted = in.readString();
                this.partNumber = in.readString();
                this.extendedAmt = in.readFloat();
                this.extendedAmtFormatted = in.readString();
                this.customerid = in.readString();
                this.customeridFormatted = in.readString();
                this.salesorderid = in.readString();
                this.salesorderidFormatted = in.readString();
                this.salesrepid = in.readString();
                this.salesrepidFormatted = in.readString();
                this.priceperunit = in.readFloat();
                this.priceperunitFormatted = in.readString();
                this.itemgroup = in.readString();
                this.qty = in.readInt();
                this.isCapital = in.readByte() != 0;
                this.territoryid = in.readString();
                this.territoryidFormatted = in.readString();
                this.accountnumber = in.readString();
                this.orderdateFormatted = in.readString();
                this.orderDate = (DateTime) in.readSerializable();
                this.productfamilyValue = in.readInt();
                this.productfamilyFormattedValue = in.readString();
                this.logicalname = in.readString();
                this.pluralname = in.readString();
                this.entityid = in.readString();
                this.etag = in.readString();
                this.groupid = in.readInt();
            }

            public static final Parcelable.Creator<OrderProduct> CREATOR = new Parcelable.Creator<OrderProduct>() {
                @Override
                public OrderProduct createFromParcel(Parcel source) {
                    return new OrderProduct(source);
                }

                @Override
                public OrderProduct[] newArray(int size) {
                    return new OrderProduct[size];
                }
            };
        }
    }

    public static class AccountProducts {

        public static final String ITEM_GROUP_FLOWMETERS = "4050";
        public static final String ITEM_GROUP_PROBES = "4060";
        public static final String ITEM_GROUP_CABLES = "4070";
        public static final String ITEM_GROUP_LICENSES = "4030";

        public ArrayList<AccountProduct> list = new ArrayList<>();

        public int size() {
            return this.list.size();
        }

        @Override
        public String toString() {
            return "Account products: " + this.list.size();
        }

        public AccountProducts() { }

        public AccountProducts(String crmResponse) {
            ArrayList<AccountProduct> accountProducts = new ArrayList<>();
            try {
                JSONObject root = new JSONObject(crmResponse);
                JSONArray rootArray = root.getJSONArray("value");
                for (int i = 0; i < rootArray.length(); i++) {
                    AccountProduct accountProduct = new AccountProduct(rootArray.getJSONObject(i));
                    accountProducts.add(accountProduct);
                }
                this.list = accountProducts;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public static class AccountProduct extends CrmEntity {

            public boolean isSeparator;
            // public String etag;
            public String serialnumber;
            public String partNumber;
            public String customerinventoryid;
            public String itemgroupnumber;
            public String productDescription;
            public DateTime physicalDate;
            public String productid;
            public String qty;
            public String statusFormatted;
            public int statuscode;
            public String accountname;
            public String accountid;
            public DateTime modifiedOn;
            public String modifiedOnFormatted;
            public boolean isCapital;
            public String isCapitalFormatted;
            public String revision;

            // CRM statuscodes
            public static final int EXPIRED = 181400000;
            public static final int RETURNED = 181400005;
            public static final int SCRAPPED = 181400004;
            public static final int LOST = 181400007;
            public static final int ONSITE = 1;

            @Override
            public String toString() {
                return this.partNumber + ", Qty: " + this.qty;
            }

            public AccountProduct() {
            }

            public AccountProduct(JSONObject json) {

                try {
                    if (!json.isNull("etag")) {
                        this.etag = (json.getString("etag"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("col_serialnumber")) {
                        this.serialnumber = (json.getString("col_serialnumber"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("col_name")) {
                        this.partNumber = (json.getString("col_name"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("col_customerinventoryid")) {
                        this.accountid = (json.getString("col_customerinventoryid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("col_itemgroup")) {
                        this.itemgroupnumber = (json.getString("col_itemgroup"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_col_productid_valueFormattedValue")) {
                        this.productDescription = (json.getString("_col_productid_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_col_productid_value")) {
                        this.productid = (json.getString("_col_productid_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("col_quantity")) {
                        this.qty = (json.getString("col_quantity"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // Added 1.83 and meant to replace modifiedon
                try {
                   if (!json.isNull("new_physical_date")) {
                       this.physicalDate = (new DateTime(json.getString("new_physical_date")));
                   }
                } catch (JSONException e) {
                   e.printStackTrace();
                }
                try {
                    if (!json.isNull("statuscodeFormattedValue")) {
                        this.statusFormatted = (json.getString("statuscodeFormattedValue"));
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
                    if (!json.isNull("_col_accountid_valueFormattedValue")) {
                        this.accountname = (json.getString("_col_accountid_valueFormattedValue"));
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
                    if (!json.isNull("modifiedonFormattedValue")) {
                        this.modifiedOnFormatted = (json.getString("modifiedonFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("col_ownershipcapital")) {
                        this.isCapital = (json.getBoolean("col_ownershipcapital"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("col_ownershipcapitalFormattedValue")) {
                        this.isCapitalFormatted = (json.getString("col_ownershipcapitalFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("col_revision")) {
                        this.revision = (json.getString("col_revision"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // Added (1.83)
            public String getPrettyPhysicalDate() {
                try {
                    return Helpers.DatesAndTimes.getPrettyDateAndTime(this.physicalDate);
                } catch (Exception e) {
                    e.printStackTrace();
                    return "unknown";
                }
            }
        }
    }

    public static class Goals {

        private static final String TAG = "Goals";

        public int size() {
            return this.list.size();
        }

        @Override
        public String toString() {
            return "Goals | size: " + list.size();
        }

        public ArrayList<Goal> list = new ArrayList<>();

        public Goals(ArrayList<Goal> goals) {
            this.list = goals;
        }

        public Goals(String crmResponse) {
            ArrayList<Goal> goals = new ArrayList<>();
            try {
                JSONObject root = new JSONObject(crmResponse);
                JSONArray rootArray = root.getJSONArray("value");
                for (int i = 0; i < rootArray.length(); i++) {
                    Goal goal = new Goal(rootArray.getJSONObject(i));
                    goals.add(goal);
                }
                this.list = goals;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public static class Goal extends CrmEntity {

            public float pct;
            public String title;
            public String ownerid;
            public String ownername;
            public float target;
            public float actual;
            public int period;
            public int year;
            public String fiscalFirstDayFormatted;
            public String fiscalFirstDayValue;
            public DateTime rawStartDate;
            public DateTime rawEndDate;
            public String territoryid;
            public String territoryname;

            public String getPrettyPct() {
               return Helpers.Numbers.formatAsZeroDecimalPointNumber(this.pct, RoundingMode.UNNECESSARY) + "%";
            }

            public String getPrettyTarget() {
                return Helpers.Numbers.convertToCurrency(this.target);
            }

            public String getPrettyActual() {
                return Helpers.Numbers.convertToCurrency(this.actual);
            }

            @NonNull
            @Override
            public String toString() {
                return title + " Target: " + this.getPrettyTarget() + ", Actual: "
                        + this.getPrettyActual() + ", Pct: " + this.getPrettyPct();
            }

            public Goal(JSONObject json) {
                try {
                    if (!json.isNull("goalid")) {
                        this.entityid = (json.getString("goalid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_1124b4bdf013df11a16e00155d7aa40d_employeeid")) {
                        this.territoryname = (json.getString("a_1124b4bdf013df11a16e00155d7aa40d_employeeid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_1124b4bdf013df11a16e00155d7aa40d_territoryid")) {
                        this.territoryid = (json.getString("a_1124b4bdf013df11a16e00155d7aa40d_territoryid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("percentage")) {
                        this.pct = (json.getLong("percentage"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("goalstartdate")) {
                        this.rawStartDate = (new DateTime(json.getString("goalstartdate")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("goalenddate")) {
                        this.rawEndDate = (new DateTime(json.getString("goalenddate")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("title")) {
                        this.title = (json.getString("title"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_goalownerid_value")) {
                        this.ownerid = (json.getString("_goalownerid_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_goalownerid_valueFormattedValue")) {
                        this.ownername = (json.getString("_goalownerid_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("targetmoney")) {
                        this.target = (json.getLong("targetmoney"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_fiscalfirstdayofmonthFormattedValue")) {
                        this.fiscalFirstDayFormatted = json.getString("msus_fiscalfirstdayofmonthFormattedValue");
                    }
                } catch (Exception e) { }
                try {
                    if (!json.isNull("msus_fiscalfirstdayofmonth")) {
                        this.fiscalFirstDayValue = json.getString("msus_fiscalfirstdayofmonth");
                        DateTime fiscalFirstDay = new DateTime(this.fiscalFirstDayValue);
                        this.period = fiscalFirstDay.getMonthOfYear();
                        this.year = fiscalFirstDay.getYear();
                    }
                } catch (Exception e) { }
                try {
                    if (!json.isNull("actualmoney")) {
                        this.actual = (json.getLong("actualmoney"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            public static ArrayList<Goal> createMany(String crmResponse, int period, int year) {
                ArrayList<Goal> goals = new ArrayList<>();
                try {
                    JSONObject root = new JSONObject(crmResponse);
                    JSONArray rootArray = root.getJSONArray("value");
                    for (int i = 0; i < rootArray.length(); i++) {
                        Goal goal = new Goal(rootArray.getJSONObject(i));
                        goal.period = period;
                        goal.year = year;
                        goals.add(goal);
                    }
                    return goals;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            public GoalSummary getGoalSummary(DateTime startDate, DateTime endDate, DateTime measureDate) {
                return new GoalSummary(this, measureDate, startDate, endDate);
            }

            public DateTime getStartDate() {
                return this.rawStartDate;
            }

            public DateTime getEndDate() {
                return this.rawEndDate;
            }

            public DateTime getEndDateForMonthlyGoal() {
                int daysInMonth = Helpers.DatesAndTimes.getDaysInMonth(this.year, this.period);
                return new DateTime(this.year, this.period, daysInMonth, 0, 0);
            }

            public DateTime getStartDateForMonthlyGoal() {
                return new DateTime(this.year, this.period, 1, 0, 0);
            }

            public int daysInMonth() {
                return Helpers.DatesAndTimes.getDaysInMonth(this.year, this.period);
            }

        }
    }

    public static class Leads {

        public ArrayList<Lead> list = new ArrayList<>();

        public Leads(String crmResponse) {
            try {
                JSONObject rootObject = new JSONObject(crmResponse);
                JSONArray rootArray = rootObject.getJSONArray("value");
                for (int i = 0; i < rootArray.length(); i++) {
                    this.list.add(new Lead(rootArray.getJSONObject(i)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void getTerritoryLeads(Context context, String territoryid, final MyInterfaces.GetLeadsListener listener) {

            String query = CrmQueries.Leads.getTerritoryLeads(territoryid);
            ArrayList<Requests.Argument> args = new ArrayList<>();
            args.add(new Requests.Argument("query", query));
            Requests.Request request = new Requests.Request(Function.GET, args);
            new Crm().makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody);
                    Leads leads = new Leads(response);
                    listener.onSuccess(leads);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    listener.onFailure(error.getLocalizedMessage());
                }
            });

        }

        public static void getTerritoryLeads(Context context, CrmQueries.Leads.LeadFilter filter, String territoryid, final MyInterfaces.GetLeadsListener listener) {

            String query = CrmQueries.Leads.getTerritoryLeads(territoryid, filter);
            ArrayList<Requests.Argument> args = new ArrayList<>();
            args.add(new Requests.Argument("query", query));
            Requests.Request request = new Requests.Request(Function.GET, args);
            new Crm().makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody);
                    Leads leads = new Leads(response);
                    listener.onSuccess(leads);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    listener.onFailure(error.getLocalizedMessage());
                }
            });

        }

        public static void getAllLeads(Context context, final MyInterfaces.GetLeadsListener listener) {

            String query = CrmQueries.Leads.getAllLeads();
            ArrayList<Requests.Argument> args = new ArrayList<>();
            args.add(new Requests.Argument("query", query));
            Requests.Request request = new Requests.Request(Function.GET, args);
            new Crm().makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody);
                    Leads leads = new Leads(response);
                    listener.onSuccess(leads);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    listener.onFailure(error.getLocalizedMessage());
                }
            });

        }

        public static void getAllLeads(Context context, CrmQueries.Leads.LeadFilter filter, final MyInterfaces.GetLeadsListener listener) {

            String query = CrmQueries.Leads.getAllLeads(filter);
            ArrayList<Requests.Argument> args = new ArrayList<>();
            args.add(new Requests.Argument("query", query));
            Requests.Request request = new Requests.Request(Function.GET, args);
            new Crm().makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody);
                    Leads leads = new Leads(response);
                    listener.onSuccess(leads);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    listener.onFailure(error.getLocalizedMessage());
                }
            });

        }

        public static class Lead extends CrmEntity {

            // public String etag;
            // public String entityid;
            public String statecodeFormatted;
            public int statecode;
            public String leadQualityFormatted;
            public int leadQuality;
            public boolean dontBulkEmail;
            public String fullname;
            public String subject;
            public boolean dontMail;
            public String ownerid;
            public String owneridFormatted;
            public boolean dontemail;
            public boolean dontcall;
            public String email;
            public String mobilephone;
            public String businessphone;
            public int specialtycode;
            public String specialtycodeFormatted;
            public String statuscodeFormatted;
            public int statuscode;
            public String firstname;
            public String lastname;
            public String createdbyid;
            public String createdbyFormatted;
            public String congressid;
            public String congressidFormatted;
            public String jobtitle;
            public String parentcontactid;
            public String parentcontactidFormatted;
            public int leadsourcecode = -1;
            public String leadsourcecodeFormatted;
            public int preferredContactMethodCode;
            public String preferredContactMethodCodeFormatted;
            public String parentAccountId;
            public String parentAccountName;
            public String parentAccountnumber;
            public String createdBy;
            public String createdByFormatted;
            public DateTime createdOn;
            public String createdOnFormatted;
            public DateTime modifiedOn;
            public String modifiedOnFormatted;
            public String modifiedBy;
            public String modifiedByFormatted;

            public Lead(JSONObject json) {
                try {
                    if (!json.isNull("etag")) {
                        this.etag = (json.getString("etag"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_specialtyFormattedValue")) {
                        this.specialtycodeFormatted = (json.getString("msus_specialtyFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_specialty")) {
                        this.specialtycode = (json.getInt("msus_specialty"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("statecodeFormattedValue")) {
                        this.statecodeFormatted = (json.getString("statecodeFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("mobilephone")) {
                        this.mobilephone = (json.getString("mobilephone"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("telephone1")) {
                        this.businessphone = (json.getString("telephone1"));
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
                    if (!json.isNull("leadqualitycodeFormattedValue")) {
                        this.leadQualityFormatted = (json.getString("leadqualitycodeFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("leadqualitycode")) {
                        this.leadQuality = (json.getInt("leadqualitycode"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("donotbulkemail")) {
                        this.dontBulkEmail = (json.getBoolean("donotbulkemail"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("leadid")) {
                        this.entityid = (json.getString("leadid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
                    if (!json.isNull("fullname")) {
                        this.fullname = (json.getString("fullname"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("subject")) {
                        this.subject = (json.getString("subject"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("donotpostalmail")) {
                        this.dontMail = (json.getBoolean("donotpostalmail"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_ownerid_valueFormattedValue")) {
                        this.owneridFormatted = (json.getString("_ownerid_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_ownerid_value")) {
                        this.ownerid = (json.getString("_ownerid_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("donotemail")) {
                        this.dontemail = (json.getBoolean("donotemail"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("donotphone")) {
                        this.dontcall = (json.getBoolean("donotphone"));
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
                    if (!json.isNull("statuscodeFormattedValue")) {
                        this.statuscodeFormatted = (json.getString("statuscodeFormattedValue"));
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
                    if (!json.isNull("preferredcontactmethodcode")) {
                        this.preferredContactMethodCode = (json.getInt("preferredcontactmethodcode"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("preferredcontactmethodcodeFormattedValue")) {
                        this.preferredContactMethodCodeFormatted = (json.getString("preferredcontactmethodcodeFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_msus_leadsource_congress_valueFormattedValue")) {
                        this.congressidFormatted = (json.getString("_msus_leadsource_congress_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_msus_leadsource_congress_value")) {
                        this.congressid = (json.getString("_msus_leadsource_congress_value"));
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
                    if (!json.isNull("_parentcontactid_valueFormattedValue")) {
                        this.parentcontactidFormatted = (json.getString("_parentcontactid_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_parentcontactid_value")) {
                        this.parentcontactid = (json.getString("_parentcontactid_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("leadsourcecodeFormattedValue")) {
                        this.leadsourcecodeFormatted = (json.getString("leadsourcecodeFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("leadsourcecode")) {
                        this.leadsourcecode = (json.getInt("leadsourcecode"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_e1b9eb98752946799234e25abb9bb751_accountid")) {
                        this.parentAccountId = (json.getString("a_e1b9eb98752946799234e25abb9bb751_accountid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_e1b9eb98752946799234e25abb9bb751_name")) {
                        this.parentAccountName = (json.getString("a_e1b9eb98752946799234e25abb9bb751_name"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_e1b9eb98752946799234e25abb9bb751_accountnumber")) {
                        this.parentAccountnumber = (json.getString("a_e1b9eb98752946799234e25abb9bb751_accountnumber"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
            }

            public BasicObject toBasicObject() {
                BasicObject object = new BasicObject(this.firstname + " " + this.lastname,
                        this.parentAccountName + "\n" + this.owneridFormatted + "\n" + this.createdOnFormatted, this);
                object.middleText = subject;
                object.topRightText = this.leadQualityFormatted + "\n(" + statuscodeFormatted + ")";
                object.iconResource = R.drawable.lead2;
                object.bottomRightText = "";
                return object;
            }

            public BasicEntity toBasicEntity() {

                BasicEntity entity = new BasicEntity(this);

                BasicEntity.EntityBasicField field = new BasicEntity.EntityBasicField("First name:", this.firstname);
                field.crmFieldName = "firstname";
                field.isReadOnly = false;
                entity.fields.add(field);

                field = new BasicEntity.EntityBasicField("Last name:", this.lastname);
                field.crmFieldName = "lastname";
                entity.fields.add(field);

                field = new BasicEntity.EntityBasicField("Topic:", this.subject);
                field.crmFieldName = "subject";
                entity.fields.add(field);

                field = new BasicEntity.EntityBasicField("Account:", this.parentAccountName);
                field.isAccountField = true;
                field.crmFieldName = "customerid";
                field.account = new Accounts.Account(this.parentAccountId, this.parentAccountName);
                entity.fields.add(field);

                ArrayList<BasicEntity.EntityBasicField.OptionSetValue> optionset = new ArrayList<>();
                optionset.add(new BasicEntity.EntityBasicField.OptionSetValue("Cadiac", "745820000"));
                optionset.add(new BasicEntity.EntityBasicField.OptionSetValue("Vascular", "745820001"));
                optionset.add(new BasicEntity.EntityBasicField.OptionSetValue("Transplant", "745820002"));
                optionset.add(new BasicEntity.EntityBasicField.OptionSetValue("Multiple", "745820003"));
                optionset.add(new BasicEntity.EntityBasicField.OptionSetValue("Unknown", "745820004"));
                optionset.add(new BasicEntity.EntityBasicField.OptionSetValue("Other", "745820005"));
                field = new BasicEntity.EntityBasicField("Specialty: ", this.specialtycodeFormatted);
                field.crmFieldName = "msus_specialty";
                field.optionSetValues = optionset;
                field.isOptionSet = true;
                field.isReadOnly = false;
                entity.fields.add(field);

                field = new BasicEntity.EntityBasicField("Email:", this.email);
                field.crmFieldName = "emailaddress1";
                entity.fields.add(field);

                field = new BasicEntity.EntityBasicField("Mobile:", this.mobilephone);
                field.crmFieldName = "mobilephone";
                entity.fields.add(field);

                field = new BasicEntity.EntityBasicField("Business phone:", this.businessphone);
                field.crmFieldName = "telephone1";
                entity.fields.add(field);

                field = new BasicEntity.EntityBasicField("Title:", this.jobtitle);
                field.crmFieldName = "jobtitle";
                entity.fields.add(field);

                optionset = new ArrayList<>();
                optionset.add(new BasicEntity.EntityBasicField.OptionSetValue("Advertisement", "1"));
                optionset.add(new BasicEntity.EntityBasicField.OptionSetValue("Prospecting", "100000003"));
                optionset.add(new BasicEntity.EntityBasicField.OptionSetValue("Referral", "2"));
                optionset.add(new BasicEntity.EntityBasicField.OptionSetValue("Current customer - new surgeon", "3"));
                optionset.add(new BasicEntity.EntityBasicField.OptionSetValue("Partner", "4"));
                optionset.add(new BasicEntity.EntityBasicField.OptionSetValue("Other", "745820005"));
                optionset.add(new BasicEntity.EntityBasicField.OptionSetValue("Web", "8"));
                optionset.add(new BasicEntity.EntityBasicField.OptionSetValue("Other", "745820005"));
                optionset.add(new BasicEntity.EntityBasicField.OptionSetValue("Surgeon User - New Hospital", "100000002"));
                optionset.add(new BasicEntity.EntityBasicField.OptionSetValue("Congress/Show", "100000001"));
                optionset.add(new BasicEntity.EntityBasicField.OptionSetValue("Customer contacted office", "100000000"));
                field = new BasicEntity.EntityBasicField("Lead source: ", this.leadsourcecodeFormatted);
                field.crmFieldName = "leadsourcecode";
                field.optionSetValues = optionset;
                field.isOptionSet = true;
                field.isReadOnly = false;
                entity.fields.add(field);

                optionset = new ArrayList<>();
                optionset.add(new BasicEntity.EntityBasicField.OptionSetValue("Text/SMS", "745820000"));
                optionset.add(new BasicEntity.EntityBasicField.OptionSetValue("Any", "1"));
                optionset.add(new BasicEntity.EntityBasicField.OptionSetValue("Email", "2"));
                optionset.add(new BasicEntity.EntityBasicField.OptionSetValue("Phone", "3"));
                optionset.add(new BasicEntity.EntityBasicField.OptionSetValue("Mail", "5"));
                field = new BasicEntity.EntityBasicField("Lead source: ", this.preferredContactMethodCodeFormatted);
                field.crmFieldName = "preferredcontactmethodcode";
                field.optionSetValues = optionset;
                field.isOptionSet = true;
                field.isReadOnly = false;
                entity.fields.add(field);

                field = new BasicEntity.EntityBasicField("Congress:", this.congressidFormatted, true);
                entity.fields.add(field);

                return entity;

            }

            @Override
            public String toString() {
                return this.fullname + ", " + this.congressidFormatted + " - " + this.leadQualityFormatted;
            }

        }
    }

    public static class ServiceAgreements {
        public ArrayList<ServiceAgreement> list = new ArrayList<>();

        public ServiceAgreements(String crmServerResponse) {
            try {
                JSONObject rootObject = new JSONObject(crmServerResponse);
                JSONArray rootArray = rootObject.getJSONArray("value");
                for (int i = 0; i < rootArray.length(); i++) {
                    this.list.add(new ServiceAgreement(rootArray.getJSONObject(i)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public static class ServiceAgreement extends CrmEntity {

            public int msus_serialnumber;
            public String msus_name;
            public DateTime msus_enddate;
            public DateTime msus_startdate;
            public boolean msus_declined;
            public boolean msus_imaging;
            public String _msus_product_valueFormattedValue;
            public String _msus_product_value;
            public int msus_termlengthyears;
            public String _msus_customer_valueFormattedValue;
            public String _msus_customer_value;
            public String _ownerid_valueFormattedValue;
            public String _ownerid_value;
            public String msus_warrantytypeFormattedValue;
            public int msus_warrantytype;
            public String a_963da520835eec11811d005056a36b9b_territoryidFormattedValue;
            public String a_963da520835eec11811d005056a36b9b_territoryid;
            public String modifiedByFormatted;
            public String modifiedBy;
            public DateTime modifiedOn;

            public ServiceAgreement(JSONObject json) {
                try {
                    if (!json.isNull("msus_medistimserviceagreementid")) {
                        this.entityid = (json.getString("msus_medistimserviceagreementid"));
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
                    if (!json.isNull("msus_serialnumber")) {
                        this.msus_serialnumber = (json.getInt("msus_serialnumber"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_name")) {
                        this.msus_name = (json.getString("msus_name"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                   if (!json.isNull("msus_enddate")) {
                       this.msus_enddate = (new DateTime(json.getString("msus_enddate")));
                   }
                } catch (JSONException e) {
                   e.printStackTrace();
                }
                try {
                   if (!json.isNull("msus_startdate")) {
                       this.msus_startdate = (new DateTime(json.getString("msus_startdate")));
                   }
                } catch (JSONException e) {
                   e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_declined")) {
                        this.msus_declined = (json.getBoolean("msus_declined"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_imaging")) {
                        this.msus_imaging = (json.getBoolean("msus_imaging"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_msus_product_valueFormattedValue")) {
                        this._msus_product_valueFormattedValue = (json.getString("_msus_product_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_msus_product_value")) {
                        this._msus_product_value = (json.getString("_msus_product_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_termlengthyears")) {
                        this.msus_termlengthyears = (json.getInt("msus_termlengthyears"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_msus_customer_valueFormattedValue")) {
                        this._msus_customer_valueFormattedValue = (json.getString("_msus_customer_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_msus_customer_value")) {
                        this._msus_customer_value = (json.getString("_msus_customer_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_ownerid_value")) {
                        this._ownerid_value = (json.getString("_ownerid_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_ownerid_valueFormattedValue")) {
                        this._ownerid_valueFormattedValue = (json.getString("_ownerid_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_warrantytype")) {
                        this.msus_warrantytype = (json.getInt("msus_warrantytype"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_warrantytypeFormattedValue")) {
                        this.msus_warrantytypeFormattedValue = (json.getString("msus_warrantytypeFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_963da520835eec11811d005056a36b9b_territoryidFormattedValue")) {
                        this.a_963da520835eec11811d005056a36b9b_territoryidFormattedValue = (json.getString("a_963da520835eec11811d005056a36b9b_territoryidFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_963da520835eec11811d005056a36b9b_territoryid")) {
                        this.a_963da520835eec11811d005056a36b9b_territoryid = (json.getString("a_963da520835eec11811d005056a36b9b_territoryid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("modifiedby")) {
                        this.modifiedBy = (json.getString("modifiedby"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("modifiedbyFormatted")) {
                        this.modifiedByFormatted = (json.getString("modifiedbyFormatted"));
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
            }

            public String getYesNo(boolean val) {
                return val ? "Yes" : "No";
            }

            public String getPrettyEnddate() {
                return Helpers.DatesAndTimes.getPrettyDate(this.msus_enddate);
            }

            public BasicEntity toBasicEntity() {

                BasicEntity entity = new BasicEntity();

                BasicEntity.EntityBasicField name = new BasicEntity.EntityBasicField("Name:", this.msus_name, "msus_name");
                name.isReadOnly = false;
                entity.fields.add(name);

                BasicEntity.EntityBasicField custy = new BasicEntity.EntityBasicField("Customer:", this._msus_customer_valueFormattedValue, "msus_customer");
                custy.isReadOnly = false;
                custy.isAccountField = true;
                custy.account = new Accounts.Account(this._msus_customer_value, this._msus_customer_valueFormattedValue);
                entity.fields.add(custy);

                BasicEntity.EntityBasicField product = new BasicEntity.EntityBasicField("Product:", this._msus_product_valueFormattedValue, "msus_product");
                product.isReadOnly = true;
                entity.fields.add(product);

                BasicEntity.EntityBasicField serialnumber = new BasicEntity.EntityBasicField("Serial number:", Integer.toString(this.msus_serialnumber), "msus_serialnumber");
                serialnumber.isReadOnly = false;
                serialnumber.isNumber = true;
                entity.fields.add(serialnumber);

                BasicEntity.EntityBasicField startdate = new BasicEntity.EntityBasicField("Start date:", Helpers.DatesAndTimes.getPrettyDate(this.msus_startdate), "msus_startdate");
                startdate.isReadOnly = false;
                startdate.isDateField = true;
                entity.fields.add(startdate);

                BasicEntity.EntityBasicField enddate = new BasicEntity.EntityBasicField("End date:", Helpers.DatesAndTimes.getPrettyDate(this.msus_enddate), "msus_enddate");
                enddate.isReadOnly = false;
                enddate.isDateField = true;
                entity.fields.add(enddate);

                BasicEntity.EntityBasicField termlength = new BasicEntity.EntityBasicField("Term length:", Integer.toString(msus_termlengthyears), "msus_termlengthyears");
                termlength.isReadOnly = false;
                termlength.isNumber = true;
                entity.fields.add(termlength);

                BasicEntity.EntityBasicField modifiedBy = new BasicEntity.EntityBasicField("Modified by:", this.modifiedByFormatted);
                modifiedBy.isReadOnly = true;
                entity.fields.add(modifiedBy);

                BasicEntity.EntityBasicField modifiedon = new BasicEntity.EntityBasicField("Modified on:", this.modifiedOn);
                modifiedon.isReadOnly = true;
                entity.fields.add(modifiedon);

                // warranty type options set
                ArrayList<BasicEntity.EntityBasicField.OptionSetValue> warrantyTypeOptionset = new ArrayList<>();
                warrantyTypeOptionset.add(new BasicEntity.EntityBasicField.OptionSetValue("Warranty", "745820000"));
                warrantyTypeOptionset.add(new BasicEntity.EntityBasicField.OptionSetValue("Extended warranty", "745820001"));
                BasicEntity.EntityBasicField warrantyTypeOptionsetField = new BasicEntity.EntityBasicField("Warranty type: ", msus_warrantytypeFormattedValue);
                warrantyTypeOptionsetField.crmFieldName = "msus_warrantytype";
                warrantyTypeOptionsetField.optionSetValues = warrantyTypeOptionset;
                warrantyTypeOptionsetField.isOptionSet = true;
                warrantyTypeOptionsetField.isReadOnly = false;
                entity.fields.add(warrantyTypeOptionsetField);

                // Declined warranty options set
                ArrayList<BasicEntity.EntityBasicField.OptionSetValue> custyDeclinedOptionset = new ArrayList<>();
                custyDeclinedOptionset.add(new BasicEntity.EntityBasicField.OptionSetValue("Yes", "true"));
                custyDeclinedOptionset.add(new BasicEntity.EntityBasicField.OptionSetValue("No", "false"));
                BasicEntity.EntityBasicField custyDeclinedEntityField = new BasicEntity.EntityBasicField("Customer declined extended warranty: ", getYesNo(msus_declined));
                custyDeclinedEntityField.crmFieldName = "msus_declined";
                custyDeclinedEntityField.optionSetValues = custyDeclinedOptionset;
                custyDeclinedEntityField.isOptionSet = true;
                custyDeclinedEntityField.isReadOnly = false;
                entity.fields.add(custyDeclinedEntityField);

                // Imaging options set
                ArrayList<BasicEntity.EntityBasicField.OptionSetValue> imagingOptionset = new ArrayList<>();
                imagingOptionset.add(new BasicEntity.EntityBasicField.OptionSetValue("Yes", "true"));
                imagingOptionset.add(new BasicEntity.EntityBasicField.OptionSetValue("No", "false"));
                BasicEntity.EntityBasicField imagingOptionsetEntityField = new BasicEntity.EntityBasicField("Imaging: ", getYesNo(msus_imaging));
                imagingOptionsetEntityField.crmFieldName = "msus_imaging";
                imagingOptionsetEntityField.optionSetValues = imagingOptionset;
                imagingOptionsetEntityField.isOptionSet = true;
                imagingOptionsetEntityField.isReadOnly = false;
                entity.fields.add(imagingOptionsetEntityField);

                return entity;
            }
        }
    }

    /**
     * Typically used to contain addresses for a single account.  Note that this is from the CUSTOMERADDRESS
     * entity directly!  This is NOT an address constructed from reading values from the ACCOUNT entity.
     */
    public static class CustomerAddresses {

        /**
         * This value is not retrieved from the Dynamics API.  It must be populated manually if
         * it is to be used.  The only parent account values returned are in the children (list)
         * and are: parentaccountnumber and parentaccountid.  I do not believe either of those values
         * can ever be null.
         */
        @Nullable
        public Accounts.Account baseAccount;
        public ArrayList<CustomerAddress> list = new ArrayList<>();

        /**
         * Constructs the addresses for a customer.
         * @param crmJson The JSON returned directly from the Dynamics API
         */
        public CustomerAddresses(String crmJson) {
            try {
                JSONObject rootObject = new JSONObject(crmJson);
                JSONArray rootArray = rootObject.getJSONArray("value");
                for (int i = 0; i < rootArray.length(); i++) {
                    this.list.add(new CustomerAddress(rootArray.getJSONObject(i)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public String toString() {
            return this.list.size() + " addresses (act name [may be null] " + baseAccount.accountName +")";
        }

        /**
         * Represents a single customer address entity from CRM.  Note that this is from the CUSTOMERADDRESS
         * entity directly!  This is NOT an address constructed from reading values from the ACCOUNT entity.
         */
        public static class CustomerAddress extends CrmEntity {
            public String name;
            public String parentaccountnumber;
            public String parentaccountid;
            public String line1;
            public String city;
            public String stateorprovince;
            public String postalcode;
            public String addressnumberFormattedValue;
            public int addressnumber;
            public String composite;

            public CustomerAddress(JSONObject json) {
                try {
                    if (!json.isNull("name")) {
                        this.name = (json.getString("name"));
                    } else {
                        this.name = "(no name)";
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
                    if (!json.isNull("line1")) {
                        this.line1 = (json.getString("line1"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("city")) {
                        this.city = (json.getString("city"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("stateorprovince")) {
                        this.stateorprovince = (json.getString("stateorprovince"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("postalcode")) {
                        this.postalcode = (json.getString("postalcode"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("customeraddressid")) {
                        this.entityid = (json.getString("customeraddressid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("addressnumberFormattedValue")) {
                        this.addressnumberFormattedValue = (json.getString("addressnumberFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("addressnumber")) {
                        this.addressnumber = (json.getInt("addressnumber"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("composite")) {
                        this.composite = (json.getString("composite"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("ac_x002e_accountnumber")) {
                        this.parentaccountnumber = (json.getString("ac_x002e_accountnumber"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("ac_x002e_accountid")) {
                        this.parentaccountid = (json.getString("ac_x002e_accountid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String toString() {
                return this.addressnumberFormattedValue + " | " + this.composite;
            }

            /**
             * For some reason, occasionally an address will not have a composite property despite
             * having all the ingredients to build one.  I think this is a CRM issue.  So we build
             * one here.  No null checking here - just builds a string using the four address properties.
             * @return
             */
            public String buildCompositeAddress() {
                String c = "";
                c = this.line1 +"\n" +
                            this.city + ", " + this.stateorprovince + " " + this.postalcode;
                return c;
            }

        }

    }

    public static class CrmAddresses {
        private static final String TAG = "CrmAddresses";
        public ArrayList<CrmAddress> list = new ArrayList<>();

        public String toGson() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }

        public static CrmAddresses fromGson(String gsonString) {
            Gson gson = new Gson();
            return gson.fromJson(gsonString, CrmAddresses.class);
        }

        public CrmAddress getAddress(String accountid) {
            for (CrmAddress address : this.list) {
                if (address.accountid.equals(accountid)) {
                    return address;
                }
            }
            return null;
        }

        /**
         * Will query CRM and retrieve all account addresses in the system.
         * When obtained they will be saved locally as JSON to shared preferences.
         * @param listener A basic YesNo listener which will return a populated CrmAddresses object
         *                 on success (cast the returned object to CrmEntities.CrmAddresses) or the
         *                 error message as a string on failure (cast returned object to string).
         */
        public static void retrieveAndSaveCrmAddresses(final MyInterfaces.YesNoResult listener) {
            final MyPreferencesHelper options = new MyPreferencesHelper(MyApp.getAppContext());
            Requests.Argument argument = new Requests.Argument("query", CrmQueries.Addresses.getAllAccountAddresses());
            ArrayList<Requests.Argument> args = new ArrayList<>();
            args.add(argument);
            Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);
            Crm crm = new Crm();
            crm.makeCrmRequest(MyApp.getAppContext(), request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    // Construct an array of CrmAddresses
                    String response = new String(responseBody);
                    CrmEntities.CrmAddresses addresses = new CrmEntities.CrmAddresses(response);
                    options.saveAllCrmAddresses(addresses);
                    Log.i(TAG, "onSuccess response: " + response.length());
                    listener.onYes(addresses);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.w(TAG, "onFailure: error: " + error.getLocalizedMessage());
                    listener.onNo(error.getLocalizedMessage());
                }
            });
        }

        /**
         * Will query CRM and retrieve all account addresses in the system.
         * When obtained they will be saved locally as JSON to shared preferences.
         */
        public static void retrieveAndSaveCrmAddresses() {
            retrieveAndSaveCrmAddresses(new MyInterfaces.YesNoResult() {
                @Override
                public void onYes(@Nullable Object object) {
                    // nothing to do, homie
                }

                @Override
                public void onNo(@Nullable Object object) {
                    // nothing to do, homie
                }
            });
        }

        @Override
        public String toString() {
            return this.list.size() + " addresses, ";
        }

        public CrmAddresses(ArrayList<CrmAddress> addresses) {
            this.list = addresses;
        }

        public CrmAddresses(String crmResponse) {
            ArrayList<CrmAddress> addys = new ArrayList<>();
            try {
                JSONObject root = new JSONObject(crmResponse);
                JSONArray rootArray = root.getJSONArray("value");
                for (int i = 0; i < rootArray.length(); i++) {
                    CrmAddress addy = new CrmAddress(rootArray.getJSONObject(i));
                    addys.add(addy);
                }
                this.list = addys;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public static class CrmAddress {

            public String etag;
            public String accountid;
            public String accountnumber;
            public int customertypeValue;
            public String customertypeFormatted;
            public String accountName;
            public String addressComposite;
            public double latitude;
            public double longitude;
            public double latitude_precise;
            public double longitude_precise;

            public CrmAddress(JSONObject json) {
                try {
                    if (!json.isNull("etag")) {
                        this.etag = (json.getString("etag"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("accountid")) {
                        this.accountid = (json.getString("accountid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("accountnumber")) {
                        this.accountnumber = (json.getString("accountnumber"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("customertypecodeFormattedValue")) {
                        this.customertypeFormatted = (json.getString("customertypecodeFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("customertypecode")) {
                        this.customertypeValue = (json.getInt("customertypecode"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("name")) {
                        this.accountName = (json.getString("name"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("address1_composite")) {
                        this.addressComposite = (json.getString("address1_composite"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("address1_longitude")) {
                        this.longitude = (json.getDouble("address1_longitude"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("address1_latitude")) {
                        this.latitude = (json.getDouble("address1_latitude"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_latitudeprecise")) {
                        this.latitude_precise = (json.getDouble("msus_latitudeprecise"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_longitudeprecise")) {
                        this.longitude_precise = (json.getDouble("msus_longitudeprecise"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String toString() {
                return accountName + ", Addy: " + addressComposite;
            }

            public LatLng getLatLng() {
                return new LatLng(this.latitude, this.longitude);
            }

            /**
             * Calculate the distance between this location and the supplied location.
             * @param latLng The location to measure to.
             * @return The distance between the two locations in meters.
             */
            public double distanceTo(LatLng latLng) {
                Location startLoc = new Location("START");
                Location endLoc = new Location("END");

                startLoc.setLatitude(latLng.latitude);
                startLoc.setLongitude(latLng.longitude);

                endLoc.setLatitude(this.latitude);
                endLoc.setLongitude(this.longitude);

                return startLoc.distanceTo(endLoc);
            }

            /**
             * Evaluates two addresses and determines if they are within the distance threshold
             * stipulated in preferences.
             * @param targetAddy The address to compare to this one.
             * @return True if the distance is less than or equal to the preference value saved in shared preferences.
             */
            public boolean isNearby(CrmAddress targetAddy) {
                try {
                    try {
                        MyPreferencesHelper options = new MyPreferencesHelper(MyApp.getAppContext());
                        return targetAddy.distanceTo(this.getLatLng()) <= options.getDistanceThreshold();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            /**
             * Evaluates two addresses and determines if they are within the distance threshold
             * stipulated in preferences.
             * @param targetAddy The address to compare to this one.
             * @return True if the distance is less than or equal to the preference value saved in shared preferences.
             */
            public boolean isNearby(TripEntry targetAddy) {
                try {
                    try {
                        MyPreferencesHelper options = new MyPreferencesHelper(MyApp.getAppContext());
                        return targetAddy.distanceTo(this.getLatLng()) <= options.getDistanceThreshold();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

        }

    }

    public static class Accounts implements Parcelable {
        private static final String TAG = "Accounts";
        public ArrayList<Account> list = new ArrayList<>();

        public static final int POTENTIAL_CUSTOMER = 1;
        public static final int CUSTOMER = 1;
        public static final int FORMER_CUSTOMER = 745820002;
        public static final int DISTRIBUTOR = 1;
        public static final int EVALUATION_IN_PROGRESS = 745820000;
        public static final int OTHER = 12;
        public static final int COMPETITOR = 4;
        public static final int ANY = -1;

        public String toGson() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }

        public static Accounts fromGson(String gsonString) {
            Gson gson = new Gson();
            return gson.fromJson(gsonString, Accounts.class);
        }

        /**
         * Will query CRM and retrieve all account addresses in the system.
         * When obtained they will be saved locally as JSON to shared preferences.
         * @param listener A basic YesNo listener which will return a populated CrmAddresses object
         *                 on success (cast the returned object to CrmEntities.CrmAddresses) or the
         *                 error message as a string on failure (cast returned object to string).
         */
        public static void retrieveAndSaveCrmAddresses(final MyInterfaces.YesNoResult listener) {
            final MyPreferencesHelper options = new MyPreferencesHelper(MyApp.getAppContext());
            Requests.Argument argument = new Requests.Argument("query", CrmQueries.Addresses.getAllAccountAddresses());
            ArrayList<Requests.Argument> args = new ArrayList<>();
            args.add(argument);
            Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);
            Crm crm = new Crm();
            crm.makeCrmRequest(MyApp.getAppContext(), request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    // Construct an array of CrmAddresses
                    String response = new String(responseBody);
                    CrmEntities.CrmAddresses addresses = new CrmEntities.CrmAddresses(response);
                    options.saveAllCrmAddresses(addresses);
                    Log.i(TAG, "onSuccess response: " + response.length());
                    listener.onYes(addresses);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.w(TAG, "onFailure: error: " + error.getLocalizedMessage());
                    listener.onNo(error.getLocalizedMessage());
                }
            });
        }

        /**
         * Will query CRM and retrieve all account addresses in the system.
         * When obtained they will be saved locally as JSON to shared preferences.
         */
        public static void retrieveAndSaveCrmAddresses() {
            retrieveAndSaveCrmAddresses(new MyInterfaces.YesNoResult() {
                @Override
                public void onYes(@Nullable Object object) {
                    // nothing to do, homie
                }

                @Override
                public void onNo(@Nullable Object object) {
                    // nothing to do, homie
                }
            });
        }

        @Override
        public String toString() {
            return "There are " + this.list.size() + " accounts in this list, you little bitch.";
        }

        public Accounts(ArrayList<Account> accounts) {
            this.list = accounts;
        }

        public Accounts(String crmResponse) {
            ArrayList<Account> accounts = new ArrayList<>();
            try {
                JSONObject root = new JSONObject(crmResponse);
                JSONArray rootArray = root.getJSONArray("value");
                for (int i = 0; i < rootArray.length(); i++) {
                    Account act = new Account(rootArray.getJSONObject(i));
                    accounts.add(act);
                }
                this.list = accounts;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        protected Accounts(Parcel in) {
            if (in.readByte() == 0x01) {
                list = new ArrayList<Account>();
                in.readList(list, Account.class.getClassLoader());
            } else {
                list = null;
            }
        }

        public ArrayList<BasicObject> toBasicObjects() {
            ArrayList<BasicObject> objects = new ArrayList<>();
            for (Account account : this.list) {
                BasicObjects.BasicObject object = new BasicObjects.BasicObject(account.accountnumber, account.customerTypeFormatted, account);
                object.middleText = account.accountName;
                object.iconResource = R.drawable.customer2;
                objects.add(object);
            }
            return objects;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            if (list == null) {
                dest.writeByte((byte) (0x00));
            } else {
                dest.writeByte((byte) (0x01));
                dest.writeList(list);
            }
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<Accounts> CREATOR = new Parcelable.Creator<Accounts>() {
            @Override
            public Accounts createFromParcel(Parcel in) {
                return new Accounts(in);
            }

            @Override
            public Accounts[] newArray(int size) {
                return new Accounts[size];
            }
        };

        public static class Account extends CrmEntity implements Parcelable {

           /* public String etag;
            public String entityid;*/
            public String accountnumber;
            public int customerTypeCode;
            public String customerTypeFormatted;
            public String accountName;
            public String territoryid;
            public String territoryFormatted;
            public String repid;
            public String repFormatted;
            public String regionid;
            public String regionFormatted;
            public String agreementTypeFormatted;
            public int agreementType;

            public String getAgreementTypeFormatted() {
                return (agreementTypeFormatted == null) ? "" : agreementTypeFormatted;
            }

            public Account() { }

            public Account(String entityid, String accountName) {
                this.accountName = accountName;
                this.entityid = entityid;
            }

            public Account(JSONObject json) {
                try {
                    if (!json.isNull("etag")) {
                        this.etag = (json.getString("etag"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("accountid")) {
                        this.entityid = (json.getString("accountid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("accountnumber")) {
                        this.accountnumber = (json.getString("accountnumber"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("customertypecodeFormattedValue")) {
                        this.customerTypeFormatted = (json.getString("customertypecodeFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("customertypecode")) {
                        this.customerTypeCode = (json.getInt("customertypecode"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("name")) {
                        this.accountName = (json.getString("name"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_territoryid_valueFormattedValue")) {
                        this.territoryFormatted = (json.getString("_territoryid_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_territoryid_value")) {
                        this.territoryid = (json.getString("_territoryid_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_msus_salesrep_valueFormattedValue")) {
                        this.repFormatted = (json.getString("_msus_salesrep_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_msus_salesrep_value")) {
                        this.repid = (json.getString("_msus_salesrep_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_msus_salesregionid_valueFormattedValue")) {
                        this.regionFormatted = (json.getString("_msus_salesregionid_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_msus_salesregionid_value")) {
                        this.regionid = (json.getString("_msus_salesregionid_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("col_agreementtype")) {
                        this.agreementType = (json.getInt("col_agreementtype"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("col_agreementtypeFormattedValue")) {
                        this.agreementTypeFormatted = (json.getString("col_agreementtypeFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String toString() {
                return accountName + " - " + accountnumber;
            }

            public String toGson() {
                Gson gson = new Gson();
                return gson.toJson(this);
            }


            protected Account(Parcel in) {
                etag = in.readString();
                entityid = in.readString();
                accountnumber = in.readString();
                customerTypeCode = in.readInt();
                customerTypeFormatted = in.readString();
                accountName = in.readString();
                territoryid = in.readString();
                territoryFormatted = in.readString();
                repid = in.readString();
                repFormatted = in.readString();
                regionid = in.readString();
                regionFormatted = in.readString();
                agreementTypeFormatted = in.readString();
                agreementType = in.readInt();
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(etag);
                dest.writeString(entityid);
                dest.writeString(accountnumber);
                dest.writeInt(customerTypeCode);
                dest.writeString(customerTypeFormatted);
                dest.writeString(accountName);
                dest.writeString(territoryid);
                dest.writeString(territoryFormatted);
                dest.writeString(repid);
                dest.writeString(repFormatted);
                dest.writeString(regionid);
                dest.writeString(regionFormatted);
                dest.writeString(agreementTypeFormatted);
                dest.writeInt(agreementType);
            }

            @SuppressWarnings("unused")
            public static final Parcelable.Creator<Account> CREATOR = new Parcelable.Creator<Account>() {
                @Override
                public Account createFromParcel(Parcel in) {
                    return new Account(in);
                }

                @Override
                public Account[] newArray(int size) {
                    return new Account[size];
                }
            };
        }
    }

    public static class CreateManyResponses {

        public ArrayList<CreateManyResponse> responses = new ArrayList<>();
        public String errorMessage;
        public boolean wasFaulted;

        public CreateManyResponses(String crmResponse) {
            try {
                JSONObject rootObject = new JSONObject(crmResponse);
                try {
                    if (!rootObject.isNull("ErrorMessage")) {
                        this.errorMessage = (rootObject.getString("ErrorMessage"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!rootObject.isNull("WasFaulted")) {
                        this.wasFaulted = (rootObject.getBoolean("WasFaulted"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONArray rootArray = rootObject.getJSONArray("AllResponses");
                for (int i = 0; i < rootArray.length(); i++) {
                    this.responses.add(new CreateManyResponse(rootArray.getJSONObject(i)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static class CreateManyResponse {
            public boolean wasSuccessful;
            public String responseMessage;
            public String guid;


            public CreateManyResponse(JSONObject json) {
                try {
                    if (!json.isNull("WasSuccessful")) {
                        this.wasSuccessful = (json.getBoolean("WasSuccessful"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("ResponseMessage")) {
                        this.responseMessage = (json.getString("ResponseMessage"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("Guid")) {
                        this.guid = (json.getString("Guid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String toString() {
                return "Was successful: " + this.wasSuccessful + ", Guid: " + this.guid;
            }
        }

        @Override
        public String toString() {
            return "Response count: " + this.responses.size();
        }
    }

    public static class DeleteManyResponses {

        public ArrayList<DeleteManyResponse> responses = new ArrayList<>();
        public String errorMessage;
        public boolean wasFaulted;


        public DeleteManyResponses(String crmResponse) {
            try {
                JSONObject rootObject = new JSONObject(crmResponse);
                try {
                    if (!rootObject.isNull("ErrorMessage")) {
                        this.errorMessage = (rootObject.getString("ErrorMessage"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!rootObject.isNull("WasFaulted")) {
                        this.wasFaulted = (rootObject.getBoolean("WasFaulted"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONArray rootArray = rootObject.getJSONArray("AllResponses");
                for (int i = 0; i < rootArray.length(); i++) {
                    this.responses.add(new DeleteManyResponse(rootArray.getJSONObject(i)));
            }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static class DeleteManyResponse {
            public boolean wasSuccessful;
            public String responseMessage;
            public String guid;
            public boolean wasCreated;


            public DeleteManyResponse(JSONObject json) {
                try {
                    if (!json.isNull("WasSuccessful")) {
                        this.wasSuccessful = (json.getBoolean("WasSuccessful"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("ResponseMessage")) {
                        this.responseMessage = (json.getString("ResponseMessage"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("Guid")) {
                        this.guid = (json.getString("Guid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String toString() {
                return "Was successful: " + this.wasSuccessful + ", Guid: " + this.guid;
            }
        }

        @Override
        public String toString() {
            return "Response count: " + this.responses.size();
        }
    }

    public static class CrmEntityResponse {
        public boolean wasSuccessful;
        public String responseMessage;
        public String guid;
        public boolean wasCreated;

        public CrmEntityResponse(String crmResponse) {
            // {"WasSuccessful":true,"ResponseMessage":"Existing record was updated!","Guid":"00000000-0000-0000-0000-000000000000","WasCreated":false}
            try {
                JSONObject json = new JSONObject(crmResponse);
                try {
                    if (!json.isNull("WasSuccessful")) {
                        this.wasSuccessful = (json.getBoolean("WasSuccessful"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("ResponseMessage")) {
                        this.responseMessage = (json.getString("ResponseMessage"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("Guid")) {
                        this.guid = (json.getString("Guid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("WasCreated")) {
                        this.wasCreated = (json.getBoolean("WasCreated"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class TripAssociations implements Parcelable {

        private static final String TAG = "MileageReimbursementAssociations";

        public ArrayList<TripAssociation> list = new ArrayList<>();

        public TripAssociation getAssociation(FullTrip trip) {
            for (TripAssociation a : this.list) {
                if (a.associated_trip_id.equals(trip.tripGuid)) {
                    return a;
                }
            }
            return null;
        }

        public TripAssociations() {
            this.list = new ArrayList<>();
        }

        public TripAssociations(String crmResponse) {
            ArrayList<TripAssociation> associations = new ArrayList<>();
            try {
                JSONObject root = new JSONObject(crmResponse);
                JSONArray rootArray = root.getJSONArray("value");
                for (int i = 0; i < rootArray.length(); i++) {
                    TripAssociation association = new TripAssociation(rootArray.getJSONObject(i));
                    associations.add(association);
                }
                this.list = associations;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void addAssociation(TripAssociation association) {
            this.list.add(association);
        }

        public String toJson() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }

        public EntityContainers toContainers() {
            EntityContainers entityContainers = new EntityContainers();
            for (TripAssociation association : this.list) {
                entityContainers.entityContainers.add(association.toContainer());
            }
            return entityContainers;
        }

        @Override
        public String toString() {
            return this.list.size() + " associations, ";
        }

        public BasicObjects toBasicObjects() {
            BasicObjects basicObjects = new BasicObjects();
            for (TripAssociation association : this.list) {
                BasicObject object = new BasicObject(association.associated_opportunity_name,
                        association.associated_account_name, Helpers.DatesAndTimes
                        .getPrettyDate2(association.getAssociatedTripDate()), association);
                object.topRightText = association.tripDispositionFormatted;
                basicObjects.add(object);
            }
            return basicObjects;
        }

        public String toGson() {
            return new Gson().toJson(this);
        }

        public static TripAssociations fromGson(String gson) {
            return new Gson().fromJson(gson, TripAssociations.class);
        }

        protected TripAssociations(Parcel in) {
            if (in.readByte() == 0x01) {
                list = new ArrayList<TripAssociation>();
                in.readList(list, TripAssociation.class.getClassLoader());
            } else {
                list = null;
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            if (list == null) {
                dest.writeByte((byte) (0x00));
            } else {
                dest.writeByte((byte) (0x01));
                dest.writeList(list);
            }
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<TripAssociations> CREATOR = new Parcelable.Creator<TripAssociations>() {
            @Override
            public TripAssociations createFromParcel(Parcel in) {
                return new TripAssociations(in);
            }

            @Override
            public TripAssociations[] newArray(int size) {
                return new TripAssociations[size];
            }
        };

        public static class TripAssociation extends CrmEntity implements Parcelable {
            private static final String TAG = "MileageReimbursementAssociation";
            /*public String etag;
            public String entityid;*/
            public String name;
            public String ownerid;
            public String ownername;
            private long createdon;
            public String associated_trip_name;
            public String associated_trip_id;
            public String associated_account_name;
            public String associated_account_id;
            public String associated_opportunity_name;
            public String associated_opportunity_id;
            public float associated_trip_reimbursement;
            public float associated_trip_distance;
            public long associated_trip_date;
            public String tripDispositionFormatted;
            public double tripDispositionValue;

            public DateTime getAssociatedTripDate() {
                return new DateTime(associated_trip_date);
            }

            public DateTime getCreatedOn() { return new DateTime(this.createdon); }

            public void setAssociatedTripDate(DateTime dateTime) {
                this.associated_trip_date = dateTime.getMillis();
            }

            public TripAssociation(DateTime tripDate) {
                this.associated_trip_date = tripDate.getMillis();
                this.ownerid = MediUser.getMe().systemuserid;
                this.ownername = MediUser.getMe().fullname;
                this.name = this.ownername + " was nearby during a MileBuddy trip";
            }

            public TripAssociation(JSONObject json) {
                Log.i(TAG, "MileageReimbursementAssociation " + json);

                try {
                    if (!json.isNull("etag")) {
                        this.etag = (json.getString("etag"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_ownerid_value")) {
                        this.ownerid = (json.getString("_ownerid_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_ownerid_valueFormattedValue")) {
                        this.ownername = (json.getString("_ownerid_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_856aa51bdcf9ea11810b005056a36b9b_ownerid")) {
                        this.ownerid = (json.getString("a_856aa51bdcf9ea11810b005056a36b9b_ownerid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_856aa51bdcf9ea11810b005056a36b9b_owneridFormattedValue")) {
                        this.ownername = (json.getString("a_856aa51bdcf9ea11810b005056a36b9b_owneridFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("createdon")) {
                        this.createdon = (new DateTime(json.getString("createdon")).getMillis());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_msus_associated_trip_valueFormattedValue")) {
                        this.associated_trip_name = (json.getString("_msus_associated_trip_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_msus_associated_trip_value")) {
                        this.associated_trip_id = (json.getString("_msus_associated_trip_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_msus_associated_account_valueFormattedValue")) {
                        this.associated_account_name = (json.getString("_msus_associated_account_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_msus_associated_account_value")) {
                        this.associated_account_id = (json.getString("_msus_associated_account_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_msus_associated_opportunity_valueFormattedValue")) {
                        this.associated_opportunity_name = (json.getString("_msus_associated_opportunity_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_msus_associated_opportunity_value")) {
                        this.associated_opportunity_id = (json.getString("_msus_associated_opportunity_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_mileageassociationid")) {
                        this.entityid = (json.getString("msus_mileageassociationid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_name")) {
                        this.name = (json.getString("msus_name"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_cc3500d91af9ea11810b005056a36b9b_msus_reimbursement")) {
                        this.associated_trip_reimbursement = (json.getLong("a_cc3500d91af9ea11810b005056a36b9b_msus_reimbursement"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_cc3500d91af9ea11810b005056a36b9b_msus_dt_tripdate")) {
                        this.associated_trip_date = (new DateTime(json.getString("a_cc3500d91af9ea11810b005056a36b9b_msus_dt_tripdate")).getMillis());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_eeba8cefdbf9ea11810b005056a36b9b_name")) {
                        this.associated_opportunity_name = (json.getString("a_eeba8cefdbf9ea11810b005056a36b9b_name"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_856aa51bdcf9ea11810b005056a36b9b_msus_totaldistance")) {
                        this.associated_trip_distance = (json.getLong("a_856aa51bdcf9ea11810b005056a36b9b_msus_totaldistance"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_disposition")) {
                        this.tripDispositionValue = json.getDouble("msus_disposition");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_dispositionFormattedValue")) {
                        this.tripDispositionFormatted = (json.getString("msus_dispositionFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_856aa51bdcf9ea11810b005056a36b9b_msus_dt_tripdate")) {
                        this.associated_trip_date = new DateTime(json.getString("a_856aa51bdcf9ea11810b005056a36b9b_msus_dt_tripdate")).getMillis();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                /*

			"a_eeba8cefdbf9ea11810b005056a36b9b_name": "SSM Health Cardinal Glennon Childrens Opportunity",
			"a_856aa51bdcf9ea11810b005056a36b9b_msus_totaldistanceFormattedValue": "38.1",
			"a_856aa51bdcf9ea11810b005056a36b9b_msus_totaldistance": 38.1,
                 */
            }

            public enum TripDisposition {
                START, END
            }

            /**
             * Evaluates whether this association references an existing opportunity.
             * @return True if an association is found
             */
            public boolean hasOpportunity() {
                return this.associated_opportunity_id != null;
            }


            @Override
            public String toString() {
                return this.name + ", " + new DateTime(this.associated_trip_date).toLocalDateTime().toString() +
                        ", " + this.ownername;
            }

            public EntityContainer toContainer() {
                EntityContainer container = new EntityContainer();
                if (this.name != null) {
                    container.entityFields.add(new EntityField("msus_name", name));
                }
                if (this.associated_trip_id != null) {
                    container.entityFields.add(new EntityField("msus_associated_trip", this.associated_trip_id));
                }
                if (this.associated_account_id != null) {
                    container.entityFields.add(new EntityField("msus_associated_account", this.associated_account_id));
                }
                if (this.associated_opportunity_id != null) {
                    container.entityFields.add(new EntityField("msus_associated_opportunity", this.associated_opportunity_id));
                }
                return container;
            }


            protected TripAssociation(Parcel in) {
                etag = in.readString();
                name = in.readString();
                entityid = in.readString();
                ownerid = in.readString();
                ownername = in.readString();
                createdon = in.readLong();
                associated_trip_name = in.readString();
                associated_trip_id = in.readString();
                associated_account_name = in.readString();
                associated_account_id = in.readString();
                associated_opportunity_name = in.readString();
                associated_opportunity_id = in.readString();
                associated_trip_reimbursement = in.readFloat();
                associated_trip_date = in.readLong();
                associated_trip_distance = in.readFloat();
                tripDispositionFormatted = in.readString();
                tripDispositionValue = in.readDouble();
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(etag);
                dest.writeString(name);
                dest.writeString(entityid);
                dest.writeString(ownerid);
                dest.writeString(ownername);
                dest.writeLong(createdon);
                dest.writeString(associated_trip_name);
                dest.writeString(associated_trip_id);
                dest.writeString(associated_account_name);
                dest.writeString(associated_account_id);
                dest.writeString(associated_opportunity_name);
                dest.writeString(associated_opportunity_id);
                dest.writeFloat(associated_trip_reimbursement);
                dest.writeLong(associated_trip_date);
                dest.writeFloat(associated_trip_distance);
                dest.writeString(tripDispositionFormatted);
                dest.writeDouble(tripDispositionValue);
            }

            @SuppressWarnings("unused")
            public static final Parcelable.Creator<TripAssociation> CREATOR = new Parcelable.Creator<TripAssociation>() {
                @Override
                public TripAssociation createFromParcel(Parcel in) {
                    return new TripAssociation(in);
                }

                @Override
                public TripAssociation[] newArray(int size) {
                    return new TripAssociation[size];
                }
            };
        }
    }

    public static class Emails {

        public ArrayList<Email> list;

        public Emails(String crmServerResponse) {

            this.list = new ArrayList<>();

            try {
                JSONObject root = new JSONObject(crmServerResponse);
                JSONArray rootArray = root.getJSONArray("value");
                for (int i = 0; i < rootArray.length(); i++) {
                    this.list.add(new Email(rootArray.getJSONObject(i)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static class Email extends CrmEntity implements Parcelable {

            public String description;
            public String subject;
            public String statusCodeFormatted;
            public String toRecipients;
            public String fromSender;
            public int statusCode;
            public String senderid;
            public String senderFormatted;
            public String createdByid;
            public String createdByFormatted;
            public String regardingid;
            public String regardingFormatted;
            public String activityTypeCodeFormatted;
            public int priorityCode;
            public String priorityCodeFormatted;
            public DateTime createdOn;
            public String createdOnFormatted;

            public Email(JSONObject json) {
                try {
                    if (!json.isNull("etag")) {
                        this.etag = (json.getString("etag"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("activityid")) {
                        this.entityid = (json.getString("activityid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("torecipients")) {
                        this.toRecipients = (json.getString("torecipients"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("sender")) {
                        this.fromSender = (json.getString("sender"));
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
                    if (!json.isNull("subject")) {
                        this.subject = (json.getString("subject"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("statuscodeFormattedValue")) {
                        this.statusCodeFormatted = (json.getString("statuscodeFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("statuscode")) {
                        this.statusCode = (json.getInt("statuscode"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_emailsender_valueFormattedValue")) {
                        this.senderFormatted = (json.getString("_emailsender_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_emailsender_value")) {
                        this.senderid = (json.getString("_emailsender_value"));
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
                    if (!json.isNull("_createdby_value")) {
                        this.createdByid = (json.getString("_createdby_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_regardingobjectid_valueFormattedValue")) {
                        this.regardingFormatted = (json.getString("_regardingobjectid_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_regardingobjectid_value")) {
                        this.regardingid = (json.getString("_regardingobjectid_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("activitytypecodeFormattedValue")) {
                        this.activityTypeCodeFormatted = (json.getString("activitytypecodeFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("prioritycode")) {
                        this.priorityCode = (json.getInt("prioritycode"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("prioritycodeFormattedValue")) {
                        this.priorityCodeFormatted = (json.getString("prioritycodeFormattedValue"));
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
                    if (!json.isNull("createdonFormattedValue")) {
                        this.createdOnFormatted = (json.getString("createdonFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(this.description);
                dest.writeString(this.subject);
                dest.writeString(this.statusCodeFormatted);
                dest.writeInt(this.statusCode);
                dest.writeString(this.senderid);
                dest.writeString(this.senderFormatted);
                dest.writeString(this.createdByid);
                dest.writeString(this.createdByFormatted);
                dest.writeString(this.regardingid);
                dest.writeString(this.regardingFormatted);
                dest.writeString(this.activityTypeCodeFormatted);
                dest.writeInt(this.priorityCode);
                dest.writeString(this.priorityCodeFormatted);
                dest.writeSerializable(this.createdOn);
                dest.writeString(this.createdOnFormatted);
                dest.writeString(this.toRecipients);
                dest.writeString(this.fromSender);
            }

            protected Email(Parcel in) {
                this.description = in.readString();
                this.subject = in.readString();
                this.statusCodeFormatted = in.readString();
                this.statusCode = in.readInt();
                this.senderid = in.readString();
                this.senderFormatted = in.readString();
                this.createdByid = in.readString();
                this.createdByFormatted = in.readString();
                this.regardingid = in.readString();
                this.regardingFormatted = in.readString();
                this.activityTypeCodeFormatted = in.readString();
                this.priorityCode = in.readInt();
                this.priorityCodeFormatted = in.readString();
                this.createdOn = (DateTime) in.readSerializable();
                this.createdOnFormatted = in.readString();
                this.toRecipients = in.readString();
                this.fromSender = in.readString();
            }

            public static final Parcelable.Creator<Email> CREATOR = new Parcelable.Creator<Email>() {
                @Override
                public Email createFromParcel(Parcel source) {
                    return new Email(source);
                }

                @Override
                public Email[] newArray(int size) {
                    return new Email[size];
                }
            };
        }

    }

}
