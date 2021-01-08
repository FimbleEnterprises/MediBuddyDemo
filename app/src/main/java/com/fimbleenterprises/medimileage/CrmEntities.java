package com.fimbleenterprises.medimileage;

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

import com.fimbleenterprises.medimileage.EntityContainers.EntityContainer;
import com.fimbleenterprises.medimileage.EntityContainers.EntityField;
import com.fimbleenterprises.medimileage.Requests.Request.Function;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cz.msebera.android.httpclient.Header;

public class CrmEntities {

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
            String query = Queries.Annotations.getAnnotation(annotationid, includeAttachment);
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

        public static class Annotation implements Parcelable {

            private static final String TAG = "Annotation";

            public String etag;
            public String annotationid;
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
                        this.annotationid = (json.getString("annotationid"));
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

                if (this.annotationid == null) {
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
                    request.arguments.add(new Requests.Argument("guid", this.annotationid));
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
                request.arguments.add(new Requests.Argument("entityid", this.annotationid));
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

                if (this.annotationid == null) {
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
                    request.arguments.add(new Requests.Argument("guid", this.annotationid));
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
                request.arguments.add(new Requests.Argument("entityid", this.annotationid));
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
                annotationid = in.readString();
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
                dest.writeString(annotationid);
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

        public static class OrderProduct {

            boolean isSeparator;
            String etag;
            String productid;
            String productidFormatted;
            String partNumber;
            float extendedAmt;
            String extendedAmtFormatted;
            String customerid;
            String customeridFormatted;
            String salesorderid;
            String salesorderidFormatted;
            String salesrepid;
            String salesrepidFormatted;
            float priceperunit;
            String priceperunitFormatted;
            String itemgroup;
            int qty;
            boolean isCapital;
            String territoryid;
            String territoryidFormatted;
            String accountnumber;
            String orderdateFormatted;
            DateTime orderDate;
            int productfamilyValue;
            String productfamilyFormattedValue;

            @Override
            public String toString() {
                return this.partNumber + ", Qty: " + this.qty + ", Amount: " + this.extendedAmtFormatted;
            }

            public OrderProduct() { }

            public OrderProduct(JSONObject json) {
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

            public ProductFamily getFamily(int productfamilyValue) {
                switch (productfamilyValue) {
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

        public static class AccountProduct {

            boolean isSeparator;
            String etag;
            String serialnumber;
            String partNumber;
            String customerinventoryid;
            String itemgroupnumber;
            String productDescription;
            String productid;
            String qty;
            String statusFormatted;
            int statuscode;
            String accountname;
            String accountid;
            DateTime modifiedOn;
            String modifiedOnFormatted;
            boolean isCapital;
            String isCapitalFormatted;
            String revision;

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

    }

    public static class Goal {

        public String goalid;
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
                    this.goalid = (json.getString("goalid"));
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

    public static class Leads {

        ArrayList<Lead> list = new ArrayList<>();

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

        public static void getCrmLeads(Context context, String territoryid, final MyInterfaces.GetLeadsListener listener) {

            String query = Queries.Leads.getTerritoryLeads(territoryid);
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

        public static class Lead {

            public String etag;
            public String statecodeFormatted;
            public int statecode;
            public String leadQualityFormatted;
            public int leadQuality;
            public boolean dontBulkEmail;
            public String leadid;
            public String createdOnFormatted;
            public DateTime createdon;
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
                        this.leadid = (json.getString("leadid"));
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
                       this.createdon = (new DateTime(json.getString("createdon")));
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
                    if (!json.isNull("_createdby_valueFormattedValue")) {
                        this.createdbyFormatted = (json.getString("_createdby_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_createdby_value")) {
                        this.createdbyid = (json.getString("_createdby_value"));
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
            }

            public BasicObject toBasicObject() {
                BasicObject object = new BasicObject(this.subject, this.firstname + " " + this.lastname, this);
                object.middleText = this.statuscodeFormatted;
                object.topRightText = this.leadQualityFormatted;
                object.iconResource = R.drawable.lead_icon2;
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
                field.crmFieldName = "parentaccountid";
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

    public static class Opportunities {
        private static final String TAG = "Opportunities";
        public ArrayList<Opportunity> list = new ArrayList<>();

        public Opportunities(String crmResponse) {
            try {
                JSONObject rootObject = new JSONObject(crmResponse);
                JSONArray rootArray = rootObject.getJSONArray("value");
                for (int i = 0; i < rootArray.length(); i++) {
                    list.add(new Opportunity(rootArray.getJSONObject(i)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public String toString() {
            return list.size() + " opportunities";
        }

        public String toGson() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }

        public static Opportunities fromGson(String gsonString) {
            Gson gson = new Gson();
            return gson.fromJson(gsonString, Opportunities.class);
        }

        public static Opportunities getSaved() {
            MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());
            return options.getSavedOpportunities();
        }

        public void save() {
            MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());
            options.saveOpportunities(this);
        }

        /**
         * Will query CRM using the current user's territory id and retrieve all opportunities in their
         * territory.  When obtained they will be saved locally as JSON to shared preferences.
         * @param listener A basic YesNo listener which will return a populated Opportunities object
         *                 on success (cast the returned object to CrmEntities.Opportunities) or the
         *                 error message as a string on failure (cast returned object to string).
         */
        public static void retrieveAndSaveOpportunities(final MyInterfaces.YesNoResult listener) {
            final MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());
            String query = Queries.Opportunities.getOpportunitiesByTerritory(MediUser.getMe().territoryid);
            ArrayList<Requests.Argument> args = new ArrayList<>();
            Requests.Argument argument = new Requests.Argument("query", query);
            args.add(argument);
            Requests.Request request = new Requests.Request(Requests.Request.GET, args);
            Crm crm = new Crm();
            crm.makeCrmRequest(MyApp.getAppContext(), request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody);
                    CrmEntities.Opportunities opportunities = new CrmEntities.Opportunities(response);
                    opportunities.save();
                    CrmEntities.Opportunities savedOpportunities = options.getSavedOpportunities();
                    Log.i(TAG, "onSuccess " + response);
                    if (savedOpportunities != null) {
                        listener.onYes(opportunities);
                    } else {
                        listener.onNo(null);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.w(TAG, "onFailure: " + error.getLocalizedMessage());
                    listener.onNo(null);
                }
            });
        }

        /**
         * Will query CRM using the supplied territory id and retrieve all opportunities.
         * That's it.  That's all it does.
         * @param listener An interface that constructs a Territories object and returns it on success.
         */
        public static void retrieveOpportunities(String territoryId, final MyInterfaces.GetOpportunitiesListener listener) {
            String query = Queries.Opportunities.getOpportunitiesByTerritory(territoryId);
            ArrayList<Requests.Argument> args = new ArrayList<>();
            Requests.Argument argument = new Requests.Argument("query", query);
            args.add(argument);
            Requests.Request request = new Requests.Request(Requests.Request.GET, args);
            Crm crm = new Crm();
            crm.makeCrmRequest(MyApp.getAppContext(), request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody);
                    listener.onSuccess(new CrmEntities.Opportunities(response));
                    Log.i(TAG, "onSuccess " + response);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.w(TAG, "onFailure: " + error.getLocalizedMessage());
                    listener.onFailure(error.getLocalizedMessage());
                }
            });
        }

        /**
         * Will query CRM using the current user's territory id and retrieve all opportunities in their
         * territory.  When obtained they will be saved locally as JSON to shared preferences.
         */
        public static void retrieveAndSaveOpportunities() {
            retrieveAndSaveOpportunities(new MyInterfaces.YesNoResult() {
                @Override
                public void onYes(@Nullable Object object) { }

                @Override
                public void onNo(@Nullable Object object) { }
            });
        }

        /**
         * Returns an opportunity in the list.
         * @param accountid The accountid to use when searching for opportunities
         * @return An arraylist of Opportunity objects (or null if none are found).
         */
        public ArrayList<Opportunity> getOpportunities(String accountid) {
            ArrayList<Opportunity> foundOpps = new ArrayList<>();
            for (Opportunity opp : this.list) {
                if (opp.accountid.equals(accountid)) {
                    foundOpps.add(opp);
                }
            }
            if (foundOpps.size() > 0) {
                return foundOpps;
            } else {
                return null;
            }
        }

        /**
         * Evaluates all opportunities in the list checking if the specified account id exists in one of them.
         * @param accountid The accountid to look for in the list of opportunities.
         * @return True on the first accountid match found.
         */
        public boolean accountHasOpportunity(String accountid) {
            for (Opportunity opp : this.list) {
                if (opp.accountid.equals(accountid)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Evaluates all opportunities in the list checking if the specified account id exists in one of them.
         * @param address The address to look for in the list of opportunities.  Note that the
         *                accountid property is the only property that is evaluated in the address object.
         * @return True on the first accountid match found.
         */
        public boolean accountHasOpportunity(CrmAddresses.CrmAddress address) {
            for (Opportunity opp : this.list) {
                if (opp.accountid.equals(address.accountid)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Counts how many opportunities reference the specified account id.
         * @param accountid The accountid to look for in the list of opportunities.
         * @return The amount of opportunities the accountid was referenced.
         */
        public int accountHasXopportunities(String accountid) {
            int count = 0;
            for (Opportunity opp : this.list) {
                if (opp.accountid.equals(accountid)) {
                    count++;
                }
            }
            return count;
        }

        public static class Opportunity implements Parcelable {
            public String etag;
            public String accountid;
            public String accountname;
            public String probabilityPretty;
            public int probabilityOptionsetValue;
            public String ownername;
            public String createdBy;
            public String createdByFormatted;
            public String modifiedBy;
            public String modifiedByFormatted;
            private double modifiedOn;
            public double estDeviceRev;
            public double estProbeRev;
            public double estTotalRev;
            public double estTerritoryRev;
            public String modifiedOnFormatted;
            public double monthRevPppLease;
            public String createdOnFormatted;
            public String ownerid;
            private double estimatedCloseDate;
            private double createdon;
            private int statecode;
            public String statecodeFormatted;
            public String stepName;
            public String dealTypePretty;
            public int dealTypeOptionsetValue;
            public String territoryid;
            public String opportunityid;
            public String name;
            public float floatEstimatedValue;
            public String currentSituation;
            private int statuscode;
            public String statuscodeFormatted;
            public boolean isSeparator = false;

            @Override
            public String toString() {
                return this.name;
            }

            public BasicEntity toBasicEntity() {
                BasicEntity entity = new BasicEntity(this);
                BasicEntity.EntityBasicField topic = new BasicEntity.EntityBasicField("Topic:", this.name);
                topic.crmFieldName = "name";
                entity.fields.add(topic);

                BasicEntity.EntityBasicField accountField = new BasicEntity.EntityBasicField("Account:", this.accountname);
                accountField.isAccountField = true;
                Accounts.Account account = new Accounts.Account();
                account.accountid = this.accountid;
                account.accountName = this.accountname;
                accountField.account = account;
                accountField.crmFieldName = "parentaccountid";
                entity.fields.add(accountField);

                BasicEntity.EntityBasicField curSit = new BasicEntity.EntityBasicField("Background:", this.currentSituation);
                curSit.crmFieldName = "currentsituation";
                curSit.isEditable = true;
                entity.fields.add(curSit);

                ArrayList<BasicEntity.EntityStatusReason> statusValues = new ArrayList<>();
                statusValues.add(new BasicEntity.EntityStatusReason("Discovery", "1", "0"));
                statusValues.add(new BasicEntity.EntityStatusReason("Stalled", "2", "0"));
                statusValues.add(new BasicEntity.EntityStatusReason("Qualifying", "100000002", "0"));
                statusValues.add(new BasicEntity.EntityStatusReason("Evaluating", "100000003", "0"));
                statusValues.add(new BasicEntity.EntityStatusReason("Pending", "100000009", "0"));
                statusValues.add(new BasicEntity.EntityStatusReason("Won", "100000007", "1"));
                statusValues.add(new BasicEntity.EntityStatusReason("Closed", "100000010", "1"));
                statusValues.add(new BasicEntity.EntityStatusReason("Cancelled", "4", "2"));
                statusValues.add(new BasicEntity.EntityStatusReason("Out-Sold", "5", "2"));
                statusValues.add(new BasicEntity.EntityStatusReason("Dead", "100000001", "2"));
                entity.availableEntityStatusReasons = statusValues;
                entity.entityStatusReason = new BasicEntity.EntityStatusReason(this.statuscodeFormatted,
                        Integer.toString(this.statuscode), Integer.toString(this.statecode));

                BasicEntity.EntityBasicField dealType = new BasicEntity.EntityBasicField("Case type: ", dealTypePretty);
                dealType.crmFieldName = "col_dealtype";
                ArrayList<BasicEntity.EntityBasicField.OptionSetValue> dealTypes = new ArrayList<>();
                dealTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("NA", "100000000"));
                dealTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("Cap", "1"));
                dealTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("Lease", "2"));
                dealTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("Hybrid", "3"));
                dealTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("PPP", "4"));
                dealTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("TB", "181400000"));
                dealType.optionSetValues = dealTypes;
                dealType.isOptionSet = true;
                dealType.isReadOnly = false;
                entity.fields.add(dealType);

                BasicEntity.EntityBasicField closeProbability = new BasicEntity.EntityBasicField("Close probability: ", probabilityPretty);
                closeProbability.crmFieldName = "msus_probability";
                ArrayList<BasicEntity.EntityBasicField.OptionSetValue> probabilities = new ArrayList<>();
                probabilities.add(new BasicEntity.EntityBasicField.OptionSetValue("20%", "745820000"));
                probabilities.add(new BasicEntity.EntityBasicField.OptionSetValue("40%", "745820001"));
                probabilities.add(new BasicEntity.EntityBasicField.OptionSetValue("60%", "745820002"));
                probabilities.add(new BasicEntity.EntityBasicField.OptionSetValue("90%", "745820003"));
                closeProbability.optionSetValues = probabilities;
                closeProbability.isOptionSet = true;
                entity.fields.add(closeProbability);

                BasicEntity.EntityBasicField deviceRev = new BasicEntity.EntityBasicField("Device revenue:", Double.toString(this.estDeviceRev));
                deviceRev.isNumber = true;
                deviceRev.crmFieldName = "col_estmrevenuedevices";
                entity.fields.add(deviceRev);

                BasicEntity.EntityBasicField probeRev = new BasicEntity.EntityBasicField("Probe revenue:", Double.toString(this.estProbeRev));
                probeRev.crmFieldName = "col_estmrevenueprobes";
                probeRev.isNumber = true;
                entity.fields.add(probeRev);

                BasicEntity.EntityBasicField totalRev = new BasicEntity.EntityBasicField("Total revenue:", Double.toString(this.estTotalRev));
                totalRev.isNumber = true;
                totalRev.crmFieldName = "estimatedvalue";
                entity.fields.add(totalRev);

                BasicEntity.EntityBasicField territoryRev = new BasicEntity.EntityBasicField("Territory revenue:", Double.toString(this.estTerritoryRev));
                territoryRev.isNumber = true;
                territoryRev.crmFieldName = "new_territoryrevenue";
                entity.fields.add(territoryRev);

                BasicEntity.EntityBasicField monthRev = new BasicEntity.EntityBasicField("Month Revenue PPP/Lease:", Double.toString(this.monthRevPppLease));
                monthRev.isNumber = true;
                monthRev.crmFieldName = "new_monthrevenuepppleasecurrency";
                entity.fields.add(monthRev);

                entity.fields.add(new BasicEntity.EntityBasicField("Step:", this.stepName, true));
                entity.fields.add(new BasicEntity.EntityBasicField("Created on:",
                        Helpers.DatesAndTimes.getPrettyDateAndTime(this.getCreatedOn()), true));
                entity.fields.add(new BasicEntity.EntityBasicField("Created by:", this.createdByFormatted, true));
                entity.fields.add(new BasicEntity.EntityBasicField("Modified on:", this.modifiedOnFormatted, true));
                entity.fields.add(new BasicEntity.EntityBasicField("Modified by:", this.modifiedByFormatted, true));

                return entity;
            }

            public DateTime getCreatedOn() {
                try {
                    return new DateTime(createdon);
                } catch (Exception e) {
                    e.printStackTrace();
                    return DateTime.now();
                }
            }

            public DateTime getModifiedOn() {
                return new DateTime(modifiedOn);
            }

            public DateTime getEstimatedClose() {
                return new DateTime(estimatedCloseDate);
            }

            public String getPrettyEstimatedValue() {
                return Helpers.Numbers.convertToCurrency(floatEstimatedValue);
            }

            public Opportunity(JSONObject json) {
                try {
                    if (!json.isNull("name")) {
                        this.name = (json.getString("name"));
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
                        this.statuscodeFormatted = (json.getString("statuscodeFormattedValue"));
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
                    if (!json.isNull("statecode")) {
                        this.statecode = (json.getInt("statecode"));
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
                    if (!json.isNull("_parentaccountid_value")) {
                        this.accountid = (json.getString("_parentaccountid_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("ab_territoryid")) {
                        this.territoryid = (json.getString("ab_territoryid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_parentaccountid_valueFormattedValue")) {
                        this.accountname = (json.getString("_parentaccountid_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("msus_probabilityFormattedValue")) {
                        this.probabilityPretty = (json.getString("msus_probabilityFormattedValue"));
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
                    if (!json.isNull("msus_probability")) {
                        this.probabilityOptionsetValue = (json.getInt("msus_probability"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("currentsituation")) {
                        this.currentSituation = (json.getString("currentsituation"));
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
                    if (!json.isNull("modifiedon")) {
                        this.modifiedOn = (new DateTime(json.getString("modifiedon")).getMillis());
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
                    if (!json.isNull("createdonFormattedValue")) {
                        this.createdOnFormatted = (json.getString("createdonFormattedValue"));
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
                    if (!json.isNull("_ownerid_valueFormattedValue")) {
                        this.ownername = (json.getString("_ownerid_valueFormattedValue"));
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
                    if (!json.isNull("estimatedclosedate")) {
                        this.estimatedCloseDate = (new DateTime(json.getString("estimatedclosedate")).getMillis());
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
                    if (!json.isNull("stepname")) {
                        this.stepName = (json.getString("stepname"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("col_dealtypeFormattedValue")) {
                        this.dealTypePretty = (json.getString("col_dealtypeFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("col_dealtype")) {
                        this.dealTypeOptionsetValue = (json.getInt("col_dealtype"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("col_estmrevenuedevices")) {
                        this.estDeviceRev = (json.getDouble("col_estmrevenuedevices"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("estimatedvalue")) {
                        this.estTotalRev = (json.getDouble("estimatedvalue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("col_estmrevenueprobes")) {
                        this.estProbeRev = (json.getDouble("col_estmrevenueprobes"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                   if (!json.isNull("new_monthrevenuepppleasecurrency")) {
                       this.monthRevPppLease = json.getDouble("new_monthrevenuepppleasecurrency");
                   }
                } catch (JSONException e) {
                   e.printStackTrace();
                }
                try {
                    if (!json.isNull("new_territoryrevenue")) {
                        this.estTerritoryRev = (json.getDouble("new_territoryrevenue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("opportunityid")) {
                        this.opportunityid = (json.getString("opportunityid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("estimatedvalue")) {
                        this.floatEstimatedValue = (json.getLong("estimatedvalue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            /**
             * Returns the address of the account this opportunity is associated with
             * @return A CrmAddress object or null
             */
            public CrmAddresses.CrmAddress tryGetCrmAddress() {
                try {
                    MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());
                    if (options.hasSavedAddresses()) {
                        CrmAddresses addresses = options.getAllSavedCrmAddresses();
                        return addresses.getAddress(this.accountid);
                    }
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            /**
             * Evaluates if this opportunity's account is nearby another account based on the
             * distance threshold stipulated in shared preferences.
             * @param accountid The account to compare to.
             * @return True or false as to whether they are near each other.
             */
            public boolean isNearby(String accountid) {
                try {
                    MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());
                    if (!options.hasSavedAddresses()) {
                        return false;
                    }

                    CrmAddresses.CrmAddress thisAddress, targetAddress;
                    CrmAddresses savedAddys = options.getAllSavedCrmAddresses();
                    thisAddress = savedAddys.getAddress(this.accountid);
                    targetAddress = savedAddys.getAddress(accountid);

                    return thisAddress.isNearby(targetAddress);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            /**
             * Evaluates if this opportunity's account is nearby another account based on the
             * distance threshold stipulated in shared preferences.
             * @param addy The account to compare to.
             * @return True or false as to whether they are near each other.
             */
            public boolean isNearby(CrmAddresses.CrmAddress addy) {
                try {
                    MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());
                    if (!options.hasSavedAddresses()) {
                        return false;
                    }

                    CrmAddresses.CrmAddress thisAddress, targetAddress;
                    CrmAddresses savedAddys = options.getAllSavedCrmAddresses();
                    thisAddress = savedAddys.getAddress(this.accountid);
                    targetAddress = savedAddys.getAddress(addy.accountid);

                    return thisAddress.isNearby(targetAddress);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            protected Opportunity(Parcel in) {
                etag = in.readString();
                accountid = in.readString();
                accountname = in.readString();
                probabilityPretty = in.readString();
                probabilityOptionsetValue = in.readInt();
                ownername = in.readString();
                createdBy = in.readString();
                createdByFormatted = in.readString();
                modifiedBy = in.readString();
                modifiedByFormatted = in.readString();
                modifiedOn = in.readDouble();
                estDeviceRev = in.readDouble();
                estProbeRev = in.readDouble();
                estTotalRev = in.readDouble();
                estTerritoryRev = in.readDouble();
                modifiedOnFormatted = in.readString();
                createdOnFormatted = in.readString();
                ownerid = in.readString();
                estimatedCloseDate = in.readDouble();
                createdon = in.readDouble();
                statecode = in.readInt();
                statecodeFormatted = in.readString();
                stepName = in.readString();
                dealTypePretty = in.readString();
                dealTypeOptionsetValue = in.readInt();
                territoryid = in.readString();
                opportunityid = in.readString();
                name = in.readString();
                floatEstimatedValue = in.readFloat();
                currentSituation = in.readString();
                statuscode = in.readInt();
                statuscodeFormatted = in.readString();
                isSeparator = in.readByte() != 0x00;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(etag);
                dest.writeString(accountid);
                dest.writeString(accountname);
                dest.writeString(probabilityPretty);
                dest.writeInt(probabilityOptionsetValue);
                dest.writeString(ownername);
                dest.writeString(createdBy);
                dest.writeString(createdByFormatted);
                dest.writeString(modifiedBy);
                dest.writeString(modifiedByFormatted);
                dest.writeDouble(modifiedOn);
                dest.writeDouble(estDeviceRev);
                dest.writeDouble(estProbeRev);
                dest.writeDouble(estTotalRev);
                dest.writeDouble(estTerritoryRev);
                dest.writeString(modifiedOnFormatted);
                dest.writeString(createdOnFormatted);
                dest.writeString(ownerid);
                dest.writeDouble(estimatedCloseDate);
                dest.writeDouble(createdon);
                dest.writeInt(statecode);
                dest.writeString(statecodeFormatted);
                dest.writeString(stepName);
                dest.writeString(dealTypePretty);
                dest.writeInt(dealTypeOptionsetValue);
                dest.writeString(territoryid);
                dest.writeString(opportunityid);
                dest.writeString(name);
                dest.writeFloat(floatEstimatedValue);
                dest.writeString(currentSituation);
                dest.writeInt(statuscode);
                dest.writeString(statuscodeFormatted);
                dest.writeByte((byte) (isSeparator ? 0x01 : 0x00));
            }

            @SuppressWarnings("unused")
            public static final Parcelable.Creator<Opportunity> CREATOR = new Parcelable.Creator<Opportunity>() {
                @Override
                public Opportunity createFromParcel(Parcel in) {
                    return new Opportunity(in);
                }

                @Override
                public Opportunity[] newArray(int size) {
                    return new Opportunity[size];
                }
            };
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
            final MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());
            Requests.Argument argument = new Requests.Argument("query", Queries.Addresses.getAllAccountAddresses());
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
                        MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());
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
                        MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());
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

    public static class Contacts {
        public ArrayList<Contact> list = new ArrayList<>();

        public Contacts(String crmResponse) {
            try {
                JSONObject rootObject = new JSONObject(crmResponse);
                JSONArray rootArray = rootObject.getJSONArray("value");
                for (int i = 0; i < rootArray.length(); i++) {
                    list.add(new Contact(rootArray.getJSONObject(i)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public static class Contact {

            public String etag;
            public String firstname;
            public String lastname;
            public String accountid;
            public String accountFormatted;
            public String mobile;
            public String telephone1;
            public String address1Phone;
            public String jobtitle;
            public String contactid;
            public String npiid;
            public String npiFormatted;
            public String email;
            public String createdBy;
            public String createdByFormatted;
            public DateTime createdOn;
            public String createdOnFormatted;
            public DateTime modifiedOn;
            public String modifiedOnFormatted;
            public String modifiedBy;
            public String modifiedByFormatted;

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
                        this.contactid = (json.getString("contactid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_msus_associated_npi_number_valueFormattedValue")) {
                        this.npiFormatted = (json.getString("_msus_associated_npi_number_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_msus_associated_npi_number_value")) {
                        this.npiid = (json.getString("_msus_associated_npi_number_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            public BasicEntity toBasicEntity() {
                BasicEntity entity = new BasicEntity(this);
                entity.fields.add(new BasicEntity.EntityBasicField("First name:", this.firstname, "firstname"));
                entity.fields.add(new BasicEntity.EntityBasicField("Last name:", this.lastname, "lastname"));
                entity.fields.add(new BasicEntity.EntityBasicField("Email:", this.email, "emailaddress1"));
                entity.fields.add(new BasicEntity.EntityBasicField("Phone:", this.mobile, "telephone1"));

                BasicEntity.EntityBasicField accountField = new BasicEntity.EntityBasicField("Account:", this.accountFormatted, "parentcustomerid");
                accountField.isAccountField = true;
                accountField.account = new Accounts.Account(this.accountid, this.accountFormatted);
                entity.fields.add(accountField);

                entity.fields.add(new BasicEntity.EntityBasicField("Title:", this.jobtitle, "jobtitle"));

                entity.fields.add(new BasicEntity.EntityBasicField("Created on:", this.createdOnFormatted, true));
                entity.fields.add(new BasicEntity.EntityBasicField("Created by:", this.createdByFormatted, true));
                entity.fields.add(new BasicEntity.EntityBasicField("Modified on:", this.modifiedOnFormatted, true));
                entity.fields.add(new BasicEntity.EntityBasicField("Modified by:", this.modifiedByFormatted, true));

                // This is a lookup (sadly) and cannot easily be updated/created here.
                BasicEntity.EntityBasicField npiNum = new BasicEntity.EntityBasicField("NPI:", this.npiFormatted, "msus_associated_npi_number");
                npiNum.isReadOnly = true;
                entity.fields.add(npiNum);
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
                if (this.npiFormatted != null) {
                    vBody.append("NOTE:NPI: " + this.npiFormatted + "\n");
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
                    PrintWriter out = new PrintWriter(Helpers.Files.getAppTempDirectory() + this.firstname + "_" + this.lastname + ".vcf");
                    out.println(vBody);
                    File vcard = new File(Helpers.Files.getAppTempDirectory() + this.firstname + " " + this.lastname + ".vcf");
                    out.close();
                    return vcard;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }

            }

            @Override
            public String toString() {
                return this.firstname + " " + this.lastname + ", " + this.accountFormatted;
            }

        }
    }

    public static class Tickets {

        ArrayList<Ticket> list = new ArrayList<>();

        // status codes
        public static final int NOT_RESOLVED = 50;
        public static final int IN_PROGRESS = 1;
        public static final int ON_HOLD = 2;
        public static final int TO_BE_INSPECTED = 100000002;
        public static final int WAITING_ON_REP = 3;
        public static final int WAITING_FOR_PRODUCT = 4;
        public static final int WAITING_ON_CUSTOMER = 100000001;
        public static final int TO_BE_BILLED = 100000003;
        public static final int PROBLEM_SOLVED = 5;

        // state codes
        public static final int OPEN = 0;
        public static final int RESOLVED = 1;

        public Tickets(String crmResponse) {
            try {
                JSONObject rootObject = new JSONObject(crmResponse);
                JSONArray rootArray = rootObject.getJSONArray("value");
                for (int i = 0; i < rootArray.length(); i++) {
                    JSONObject json = rootArray.getJSONObject(i);
                    list.add(new Ticket(json));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static class Ticket {

            private static final String TAG = "Ticket";

             /**************************************************************************************
             *                                  Case type values
             **************************************************************************************/
            // public static final int COMPLAINT

            public String etag;
            public String statecodeFormatted;
            public int statecode;
            public String statusFormatted;
            public int statuscode;
            public String caseTypeFormatted;
            public String contactid;
            public String contactFirstname;
            public String contactLastname;
            public String contactFullname;
            public int casetype;
            public DateTime createdon;
            public String ticketnumber;
            public String ownerName;
            public String ownerid;
            public DateTime modifiedon;
            public String title;
            public String priorityFormatted;
            public int priority;
            public String description;
            public String modifiedByFormatted;
            public String modifiedBy;
            public String caseOriginFormatted;
            public int caseorigin;
            public String customerFormatted;
            public String customerid;
            public String subjectid;
            public String subjectFormatted;
            public String createdby;
            public String createdByFormatted;
            public String ticketid;
            public String territoryid;
            public String territoryFormatted;
            public String repFormatted;
            public String repid;

            public Ticket() { }
                        
            public Ticket(JSONObject json) {
                try {
                    if (!json.isNull("etag")) {
                        this.etag = (json.getString("etag"));
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
                    if (!json.isNull("statecode")) {
                        this.statecode = (json.getInt("statecode"));
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
                    if (!json.isNull("casetypecodeFormattedValue")) {
                        this.caseTypeFormatted = (json.getString("casetypecodeFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("casetypecode")) {
                        this.casetype = (json.getInt("casetypecode"));
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
                try {
                    if (!json.isNull("ticketnumber")) {
                        this.ticketnumber = (json.getString("ticketnumber"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_ownerid_valueFormattedValue")) {
                        this.ownerName = (json.getString("_ownerid_valueFormattedValue"));
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
                   if (!json.isNull("modifiedon")) {
                       this.modifiedon = (new DateTime(json.getString("modifiedon")));
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
                    if (!json.isNull("prioritycode")) {
                        this.priority = (json.getInt("prioritycode"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("prioritycodeFormattedValue")) {
                        this.priorityFormatted = (json.getString("prioritycodeFormattedValue"));
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
                    if (!json.isNull("caseorigincode")) {
                        this.caseorigin = (json.getInt("caseorigincode"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("caseorigincodeFormattedValue")) {
                        this.caseOriginFormatted = (json.getString("caseorigincodeFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_subjectid_valueFormattedValue")) {
                        this.subjectFormatted = (json.getString("_subjectid_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_subjectid_value")) {
                        this.subjectid = (json.getString("_subjectid_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_subjectid_valueFormattedValue")) {
                        this.subjectFormatted = (json.getString("_subjectid_valueFormattedValue"));
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
                        this.createdby = (json.getString("_createdby_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_customerid_valueFormattedValue")) {
                        this.customerFormatted = (json.getString("_customerid_valueFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("_customerid_value")) {
                        this.customerid = (json.getString("_customerid_value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("incidentid")) {
                        this.ticketid = (json.getString("incidentid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_4b5945b8a4a64613afc1ae1d5e6828c7_territoryidFormattedValue")) {
                        this.territoryFormatted = (json.getString("a_4b5945b8a4a64613afc1ae1d5e6828c7_territoryidFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_4b5945b8a4a64613afc1ae1d5e6828c7_territoryid")) {
                        this.territoryid = (json.getString("a_4b5945b8a4a64613afc1ae1d5e6828c7_territoryid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_4b5945b8a4a64613afc1ae1d5e6828c7_msus_salesrepFormattedValue")) {
                        this.repFormatted = (json.getString("a_4b5945b8a4a64613afc1ae1d5e6828c7_msus_salesrepFormattedValue"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_4b5945b8a4a64613afc1ae1d5e6828c7_msus_salesrep")) {
                        this.repid = (json.getString("a_4b5945b8a4a64613afc1ae1d5e6828c7_msus_salesrep"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_b49161e62067e71180d6005056a36b9b_contactid")) {
                        this.contactid = (json.getString("a_b49161e62067e71180d6005056a36b9b_contactid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_b49161e62067e71180d6005056a36b9b_fullname")) {
                        this.contactFullname = (json.getString("a_b49161e62067e71180d6005056a36b9b_fullname"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_b49161e62067e71180d6005056a36b9b_firstname")) {
                        this.contactFirstname = (json.getString("a_b49161e62067e71180d6005056a36b9b_firstname"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("a_b49161e62067e71180d6005056a36b9b_lastname")) {
                        this.contactLastname = (json.getString("a_b49161e62067e71180d6005056a36b9b_lastname"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                /*

                 */

            }

            public BasicEntity toBasicEntity() {

                BasicEntity entity = new BasicEntity(this);

                BasicEntity.EntityBasicField ticketNumber = new BasicEntity.EntityBasicField("Ticket number:", this.ticketnumber);
                ticketNumber.crmFieldName = "ticketnumber";
                ticketNumber.isReadOnly = true;
                entity.fields.add(ticketNumber);

                BasicEntity.EntityBasicField titleField = new BasicEntity.EntityBasicField("Title:", this.title);
                titleField.crmFieldName = "title";
                entity.fields.add(titleField);

                BasicEntity.EntityBasicField descriptionField = new BasicEntity.EntityBasicField("Description:", this.description);
                descriptionField.crmFieldName = "description";
                entity.fields.add(descriptionField);

                BasicEntity.EntityBasicField accountField = new BasicEntity.EntityBasicField("Customer:", this.customerFormatted);
                accountField.isAccountField = true;
                accountField.crmFieldName = "customerid";
                accountField.account = new Accounts.Account(this.customerid, this.customerFormatted);
                entity.fields.add(accountField);

                BasicEntity.EntityBasicField territory = new BasicEntity.EntityBasicField("Territory:", this.territoryFormatted);
                territory.isReadOnly = true;
                entity.fields.add(territory);

                BasicEntity.EntityBasicField rep = new BasicEntity.EntityBasicField("Rep:", this.repFormatted);
                rep.isReadOnly = true;
                entity.fields.add(rep);

                BasicEntity.EntityBasicField contact = new BasicEntity.EntityBasicField("Contact:", this.contactFullname);
                contact.isContactField = true;
                contact.crmFieldName = "new_mw_contact";
                Contacts.Contact objContact = new Contacts.Contact();
                objContact.contactid = this.contactid;
                objContact.firstname = this.contactFirstname;
                objContact.lastname = this.contactLastname;
                contact.contact = objContact;
                entity.fields.add(contact);

                BasicEntity.EntityBasicField createdOn = new BasicEntity.EntityBasicField("Created on:", Helpers.DatesAndTimes.getPrettyDateAndTime(this.createdon));
                createdOn.isReadOnly = true;
                createdOn.isDateTimeField = true;
                entity.fields.add(createdOn);

                BasicEntity.EntityBasicField createdBy = new BasicEntity.EntityBasicField("Created by:", this.createdByFormatted);
                createdBy.isReadOnly = true;
                entity.fields.add(createdBy);

                BasicEntity.EntityBasicField modifiedOn = new BasicEntity.EntityBasicField("Modified on:", Helpers.DatesAndTimes.getPrettyDateAndTime(this.modifiedon));
                modifiedOn.isDateTimeField = true;
                modifiedOn.isReadOnly = true;
                entity.fields.add(modifiedOn);

                BasicEntity.EntityBasicField modifiedBy = new BasicEntity.EntityBasicField("Modified by:", this.modifiedByFormatted);
                modifiedBy.isReadOnly = true;
                entity.fields.add(modifiedBy);

                ArrayList<BasicEntity.EntityBasicField.OptionSetValue> caseTypes = new ArrayList<>();
                caseTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("Complaint", "100000004"));
                caseTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("Service Request", "100000005"));
                caseTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("NONCON", "100000006"));
                caseTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("CAPA", "100000007"));
                caseTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("Question", "1"));
                caseTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("Problem", "2"));
                caseTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("Request", "3"));
                caseTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("Evaluation", "100000000"));
                caseTypes.add(new BasicEntity.EntityBasicField.OptionSetValue("Sales", "100000001"));
                BasicEntity.EntityBasicField caseType = new BasicEntity.EntityBasicField("Case type: ", caseTypeFormatted);
                caseType.crmFieldName = "casetypecode";
                caseType.optionSetValues = caseTypes;
                caseType.isOptionSet = true;
                caseType.isReadOnly = false;
                entity.fields.add(caseType);

                ArrayList<BasicEntity.EntityStatusReason> statusValues = new ArrayList<>();
                statusValues.add(new BasicEntity.EntityStatusReason("In Progress", "1", "0"));
                statusValues.add(new BasicEntity.EntityStatusReason("On Hold", "2", "0"));
                statusValues.add(new BasicEntity.EntityStatusReason("To be inspected", "100000002", "0"));
                statusValues.add(new BasicEntity.EntityStatusReason("Waiting on rep", "3", "0"));
                statusValues.add(new BasicEntity.EntityStatusReason("Waiting for product", "4", "0"));
                statusValues.add(new BasicEntity.EntityStatusReason("Waiting on customer", "100000001", "0"));
                statusValues.add(new BasicEntity.EntityStatusReason("To be billed", "100000003", "0"));
                statusValues.add(new BasicEntity.EntityStatusReason("Problem solved", "5", "0"));
                entity.availableEntityStatusReasons = statusValues;
                entity.entityStatusReason = new BasicEntity.EntityStatusReason(this.statusFormatted,
                        Integer.toString(this.statuscode), Integer.toString(this.statecode));

                ArrayList<BasicEntity.EntityBasicField.OptionSetValue> caseOrigins = new ArrayList<>();
                caseOrigins.add(new BasicEntity.EntityBasicField.OptionSetValue("Saleslogix", "100000000"));
                caseOrigins.add(new BasicEntity.EntityBasicField.OptionSetValue("Phone", "1"));
                caseOrigins.add(new BasicEntity.EntityBasicField.OptionSetValue("Email", "2"));
                caseOrigins.add(new BasicEntity.EntityBasicField.OptionSetValue("Web", "3"));
                caseOrigins.add(new BasicEntity.EntityBasicField.OptionSetValue("Facebook", "2483"));
                caseOrigins.add(new BasicEntity.EntityBasicField.OptionSetValue("Twitter", "3986"));
                BasicEntity.EntityBasicField caseOrigin = new BasicEntity.EntityBasicField("Case origin: ", caseOriginFormatted);
                caseOrigin.optionSetValues = caseOrigins;
                caseOrigin.crmFieldName = "caseorigincode";
                caseOrigin.isOptionSet = true;
                caseOrigin.isReadOnly = false;
                entity.fields.add(caseOrigin);

                ArrayList<BasicEntity.EntityBasicField.OptionSetValue> subjects = new ArrayList<>();
                subjects.add(new BasicEntity.EntityBasicField.OptionSetValue("Credit hold", "D8BAD965-5B66-E711-80D6-005056A36B9B"));
                subjects.add(new BasicEntity.EntityBasicField.OptionSetValue("Customer service", "6E0E1407-5B66-E711-80D6-005056A36B9B"));
                subjects.add(new BasicEntity.EntityBasicField.OptionSetValue("Evaluation", "CA14106D-6766-E711-80D6-005056A36B9B"));
                subjects.add(new BasicEntity.EntityBasicField.OptionSetValue("<Enter a subject>", "E1ABC067-429A-E711-80D8-005056A36B9B"));
                subjects.add(new BasicEntity.EntityBasicField.OptionSetValue("Information", "42ADD710-E698-E911-80F4-005056A36B9B"));
                subjects.add(new BasicEntity.EntityBasicField.OptionSetValue("Non-Conformance", "085CBAEA-E885-E811-80E4-005056A36B9B"));
                subjects.add(new BasicEntity.EntityBasicField.OptionSetValue("Product complaint (cable)", "4BD83C47-5B66-E711-80D6-005056A36B9B"));
                subjects.add(new BasicEntity.EntityBasicField.OptionSetValue("Product complaint (flowmeter)", "FD242450-5B66-E711-80D6-005056A36B9B"));
                subjects.add(new BasicEntity.EntityBasicField.OptionSetValue("Product complaint (probe)", "44663885-CE62-476D-9268-C95A618B3CD9"));
                subjects.add(new BasicEntity.EntityBasicField.OptionSetValue("System Customization", "0D137FFB-6666-E711-80D6-005056A36B9B"));
                BasicEntity.EntityBasicField subject = new BasicEntity.EntityBasicField("Subject: ", subjectFormatted);
                subject.isOptionSet = true;
                subject.crmFieldName = "subjectid";
                subject.optionSetValues = subjects;
                subject.isReadOnly = false;
                entity.fields.add(subject);

                return entity;
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
            final MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());
            Requests.Argument argument = new Requests.Argument("query", Queries.Addresses.getAllAccountAddresses());
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

        public static class Account implements Parcelable {

            public String etag;
            public String accountid;
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

            public Account(String accountid, String accountName) {
                this.accountName = accountName;
                this.accountid = accountid;
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
                accountid = in.readString();
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
                dest.writeString(accountid);
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

        public static class TripAssociation implements Parcelable {
            private static final String TAG = "MileageReimbursementAssociation";
            public String etag;
            public String name;
            public String id;
            public String ownerid;
            public String ownername;
            public DateTime createdon;
            public String associated_trip_name;
            public String associated_trip_id;
            public String associated_account_name;
            public String associated_account_id;
            public String associated_opportunity_name;
            public String associated_opportunity_id;
            public float associated_trip_reimbursement;
            public long associated_trip_date;
            public TripDisposition tripDisposition;

            public DateTime getAssociatedTripDate() {
                return new DateTime(associated_trip_date);
            }

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
                    if (!json.isNull("_msus_disposition_value")) {
                        setTripDisposition(json.getInt("_msus_disposition_value"));
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
                        this.id = (json.getString("msus_mileageassociationid"));
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

            /**
             * Returns a pretty string stipulating whether the association is the beginning or end of a trip.
             * @return "Start" or "End"
             */
            public String getDispositionTitle() {
                switch (this.tripDisposition) {
                    case START :
                        return "Start";
                    case END :
                        return "End";
                    default:
                        return "0";
                }
            }

            public int getDispositioValue() {
                switch (this.tripDisposition) {
                    case START :
                        return 745820000;
                    case END :
                        return 745820001;
                    default:
                        return 0;
                }
            }

            public void setTripDisposition(int optionSetValue) {
                switch (optionSetValue) {
                    case 745820000 :
                        this.tripDisposition = TripDisposition.START;
                        break;
                    case 745820001 :
                        this.tripDisposition = TripDisposition.END;
                        break;
                }
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
                if (this.tripDisposition != null) {
                    container.entityFields.add(new EntityField("msus_disposition", Integer.toString(this.getDispositioValue())));
                }
                return container;
            }


            protected TripAssociation(Parcel in) {
                etag = in.readString();
                name = in.readString();
                id = in.readString();
                ownerid = in.readString();
                ownername = in.readString();
                createdon = (DateTime) in.readValue(DateTime.class.getClassLoader());
                associated_trip_name = in.readString();
                associated_trip_id = in.readString();
                associated_account_name = in.readString();
                associated_account_id = in.readString();
                associated_opportunity_name = in.readString();
                associated_opportunity_id = in.readString();
                associated_trip_reimbursement = in.readFloat();
                associated_trip_date = in.readLong();
                tripDisposition = (TripDisposition) in.readValue(TripDisposition.class.getClassLoader());
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(etag);
                dest.writeString(name);
                dest.writeString(id);
                dest.writeString(ownerid);
                dest.writeString(ownername);
                dest.writeValue(createdon);
                dest.writeString(associated_trip_name);
                dest.writeString(associated_trip_id);
                dest.writeString(associated_account_name);
                dest.writeString(associated_account_id);
                dest.writeString(associated_opportunity_name);
                dest.writeString(associated_opportunity_id);
                dest.writeFloat(associated_trip_reimbursement);
                dest.writeLong(associated_trip_date);
                dest.writeValue(tripDisposition);
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
    }

}
