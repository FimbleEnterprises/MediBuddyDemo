package com.fimbleenterprises.medimileage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cz.msebera.android.httpclient.Header;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jaiselrahman.filepicker.activity.FilePickerActivity;
import com.jaiselrahman.filepicker.config.Configurations;
import com.jaiselrahman.filepicker.model.MediaFile;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BasicEntityActivity extends AppCompatActivity {

    public static final String ENTITY_LOGICAL_NAME = "ENTITY_LOGICAL_NAME";
    public static final String GSON_STRING = "GSON_STRING";
    public static final String ENTITYID = "ENTITYID";
    public static final String CLICK_ACTION = "CLICK_ACTION";
    public static final String LABEL = "LABEL";
    public static final String VALUE = "VALUE";
    public static final String LIST_POSITION = "LIST_POSITION";
    public static final String ACTIVITY_TITLE = "ACTIVITY_TITLE";
    private static final String TAG = "BasicEntityActivity";
    private static final int FILE_REQUEST_CODE = 88;

    Context context;
    String entityid;
    String entityLogicalName;
    String activityTitle;
    NonScrollRecyclerView notesListView;
    CrmEntities.Annotations.Annotation newImageBaseAnnotation;
    Dialog dialogNoteOptions;
    public static final int PERM_REQUEST_CAMERA_ADD_ATTACHMENT = 11;
    AnnotationsAdapter adapterNotes;
    String pendingNote;
    RefreshLayout refreshLayout;
    CrmEntities.Annotations.Annotation lastClickedNote;
    TextView txtNotesLoading;
    ProgressBar pbNotesLoading;
    ImageButton btnAddNote;

    BasicEntity basicEntity;

    public static IntentFilter getIntentFilter() {
        return new IntentFilter(CLICK_ACTION);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.basic_entity_activity);

        refreshLayout = findViewById(R.id.refreshLayout);
        notesListView = findViewById(R.id.notesRecyclerView);
        txtNotesLoading = findViewById(R.id.textViewopportunityNotesLoading);
        btnAddNote = findViewById(R.id.btnAddNote);
        btnAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CrmEntities.Annotations.Annotation newNote = new CrmEntities.Annotations.Annotation();
                newNote.subject = "MileBuddy added note";
                newNote.objectid = entityid;
                newNote.isDocument = false;
                newNote.objectEntityName = entityLogicalName;
                showAddEditNote(newNote);
            }
        });
        pbNotesLoading = findViewById(R.id.progressBarWorking);

        String gson = getIntent().getStringExtra(GSON_STRING);
        entityid = getIntent().getStringExtra(ENTITYID);
        entityLogicalName = getIntent().getStringExtra(ENTITY_LOGICAL_NAME);
        activityTitle = getIntent().getStringExtra(ACTIVITY_TITLE);
        setTitle(activityTitle);

        getNotes();

        this.basicEntity = new BasicEntity(gson);

        NonScrollRecyclerView recyclerView = findViewById(R.id.rvBasicObjects);
        BasicEntityActivityObjectRecyclerAdapter adapter =
                new BasicEntityActivityObjectRecyclerAdapter(this, this.basicEntity.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false); // Disables scrolling
        recyclerView.setAdapter(adapter);


        adapter.setClickListener(new BasicEntityActivityObjectRecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(CLICK_ACTION);
                intent.putExtra(LABEL, basicEntity.list.get(position).label);
                intent.putExtra(VALUE, basicEntity.list.get(position).value);
                intent.putExtra(LIST_POSITION, position);
                sendBroadcast(intent);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.i(TAG, "onRequestPermissionsResult ");

        if (requestCode == PERM_REQUEST_CAMERA_ADD_ATTACHMENT) {
            Log.i(TAG, "onRequestPermissionsResult ");
            if (permissions[0].equals(Helpers.Permissions.Permission.CAMERA) &&
                    grantResults[0] == 0) {
                Intent intent = new Intent(context, FilePickerActivity.class);
                intent.putExtra(FilePickerActivity.CONFIGS, new Configurations.Builder()
                        .setCheckPermission(true)
                        .setShowImages(true)
                        .setShowFiles(true)
                        .setSingleClickSelection(true)
                        .setShowVideos(false)
                        .enableImageCapture(true)
                        .setSkipZeroSizeFiles(true)
                        .build());
                startActivityForResult(intent, FILE_REQUEST_CODE);
                newImageBaseAnnotation = lastClickedNote;
                dialogNoteOptions.dismiss();
                lastClickedNote.inUse = true;
                adapterNotes.notifyDataSetChanged();
                dialogNoteOptions.dismiss();
            }
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_REQUEST_CODE
                && resultCode == RESULT_OK
                && data != null) {
            List<MediaFile> mediaFiles = data.<MediaFile>getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES);
            if(mediaFiles != null && newImageBaseAnnotation != null && mediaFiles.size() > 0) {
                try {
                    final MediaFile file = mediaFiles.get(0);
                    newImageBaseAnnotation.documentBody = Helpers.Files.base64Encode(file.getPath());
                    newImageBaseAnnotation.filename = file.getName();
                    newImageBaseAnnotation.mimetype = file.getMimeType();
                    newImageBaseAnnotation.isDocument = true;
                    newImageBaseAnnotation.inUse = true;
                    adapterNotes.notifyDataSetChanged();
                    newImageBaseAnnotation.addAttachment(context, new MyInterfaces.CrmRequestListener() {
                        @Override
                        public void onComplete(Object result) {
                            try {
                                dialogNoteOptions.dismiss();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            newImageBaseAnnotation.inUse = false;
                            newImageBaseAnnotation.filename = file.getName();
                            adapterNotes.notifyDataSetChanged();
                        }

                        @Override
                        public void onProgress(Crm.AsyncProgress progress) {
                            // Upload percents are fucky.  Ignore!
                        }

                        @Override
                        public void onFail(String error) {
                            Log.w(TAG, "onNo: ");
                            newImageBaseAnnotation.isDocument = false;
                            newImageBaseAnnotation.documentBody = null;
                            newImageBaseAnnotation.filename = null;
                            newImageBaseAnnotation.inUse = false;
                            adapterNotes.notifyDataSetChanged();
                            Toast.makeText(context, "Failed!\n" + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.i(TAG, "onActivityResult ");

                    // While we wait for the attachment to be attached we can insert a placeholder
                    newImageBaseAnnotation.filename = "uploading attachment...";
                    newImageBaseAnnotation.documentBody = "";
                    newImageBaseAnnotation.isDocument = true;
                    newImageBaseAnnotation.inUse = true;
                    adapterNotes.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(context, "Image not selected", Toast.LENGTH_SHORT).show();
            }
        }

    }

    void showAddEditNote(@Nullable final CrmEntities.Annotations.Annotation clickedNote) {

        final boolean isEditing = clickedNote.annotationid != null;
        final String originalSubject = clickedNote.subject;

        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_note);
        final EditText noteBody = dialog.findViewById(R.id.body_text);
        dialog.setTitle("Note");
        dialog.setCancelable(true);
        // Set the note's initial text to the note's actual text which shouldn't be null if we are editing.
        noteBody.setText(clickedNote.notetext);
        Button btnSubmit = dialog.findViewById(R.id.button_submit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                clickedNote.notetext = noteBody.getText().toString();

                // final MyProgressDialog addEditNoteProgressDialog = new MyProgressDialog(context, "Working...");
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                // addEditNoteProgressDialog.show();
                clickedNote.inUse = true;
                adapterNotes.notifyDataSetChanged();
                clickedNote.submit(context, new MyInterfaces.CrmRequestListener() {
                    @Override
                    public void onComplete(Object result) {
                        Toast.makeText(context, "Note was added/edited!", Toast.LENGTH_SHORT).show();
                        // addEditNoteProgressDialog.dismiss();
                        clickedNote.inUse = false;
                        clickedNote.subject = originalSubject;
                        // Create result example (note the quotes): "1cd8d874-3412-eb11-810f-005056a36b9b"
                        // Update result example: {"WasSuccessful":true,"ResponseMessage":"Existing record was updated!","Guid":"00000000-0000-0000-0000-000000000000","WasCreated":false}

                        // Try to create an UpdateResponse object with the returned result.  If it
                        // succeeds then we know that it was an update operation that was executed.
                        // If it fails then it was almost certainly a successful create operation
                        // (create returns just the GUID of the new note)
                        CrmEntities.CrmEntityResponse crmEntityResponse = new CrmEntities.CrmEntityResponse(result.toString());
                        if (crmEntityResponse.wasSuccessful) {
                            Log.i(TAG, "onYes Note was updated!");
                            clickedNote.annotationid = crmEntityResponse.guid;
                            if (crmEntityResponse.wasCreated) {
                                Toast.makeText(context, "Note was created!", Toast.LENGTH_SHORT).show();
                                /*clickedNote.annotationid = updateResponse.guid;
                                clickedNote.createdon = DateTime.now();
                                clickedNote.modifiedon = DateTime.now();
                                clickedNote.createdByValue = MediUser.getMe().systemuserid;
                                clickedNote.modifiedByValue = MediUser.getMe().systemuserid;
                                clickedNote.modifedByName = MediUser.getMe().fullname;
                                clickedNote.createdByName = MediUser.getMe().fullname;
                                adapterNotes.mData.add(0, clickedNote);*/
                                adapterNotes.notifyDataSetChanged();
                            } else {
                                Toast.makeText(context, "Note was updated!", Toast.LENGTH_SHORT).show();
                                adapterNotes.updateAnnotationAndReload(clickedNote);
                            }
                        }
                        adapterNotes.notifyDataSetChanged();

                        // Clearing the notetext cache since this operation succeeded.
                        pendingNote = null;
                    }

                    @Override
                    public void onProgress(Crm.AsyncProgress progress) {
                        // clickedNote.filename = "uploading... " + progress.getCompletedMb() + ")";
                        adapterNotes.notifyDataSetChanged();
                    }

                    @Override
                    public void onFail(String error) {
                        Toast.makeText(context, "Failed to add/edit note!\n\n" + error.toString(), Toast.LENGTH_SHORT).show();
                        // addEditNoteProgressDialog.dismiss();
                        clickedNote.inUse = false;
                        getNotes();
                        dialog.dismiss();
                        dialog.show();
                    }
                });

                if (isEditing) {
                    clickedNote.subject = "(UPDATING) " + clickedNote.subject;
                    clickedNote.inUse = true;
                    adapterNotes.notifyDataSetChanged();
                } else {
                    clickedNote.subject = "(ADDING) " + clickedNote.subject;
                    clickedNote.inUse = true;
                    // If these date fields are not added the adapter will crash when parsing them
                    clickedNote.createdon = DateTime.now();
                    clickedNote.modifiedon = DateTime.now();
                    clickedNote.createdByValue = MediUser.getMe().systemuserid;
                    clickedNote.modifiedByValue = MediUser.getMe().systemuserid;
                    clickedNote.modifedByName = MediUser.getMe().fullname;
                    clickedNote.createdByName = MediUser.getMe().fullname;
                    adapterNotes.mData.add(0, clickedNote);
                    adapterNotes.notifyDataSetChanged();
                }

            }
        });
        dialog.show();

        if (pendingNote != null) {
            noteBody.setText(pendingNote);
            Log.i(TAG, "showAddEditNote Found a pending note from a presumably failed note creation - using it.");
        }

    }

    void getNotes() {
        ArrayList<Requests.Argument> args = new ArrayList<>();
        Requests.Argument argument = new Requests.Argument("query", Queries.Annotations.getAnnotations(entityid));
        args.add(argument);
        Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);

        // Show the pulldown refresh progressbar
        // refreshLayout.autoRefreshAnimationOnly();
        txtNotesLoading.setVisibility(View.VISIBLE);
        txtNotesLoading.setText("Loading notes...");
        pbNotesLoading.setVisibility(View.VISIBLE);

        Crm crm = new Crm();
        crm.makeCrmRequest(this, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                final CrmEntities.Annotations annotations = new CrmEntities.Annotations(response);
                adapterNotes = new AnnotationsAdapter(getApplicationContext(), annotations.list);
                notesListView.setLayoutManager(new LinearLayoutManager(context));
                notesListView.setAdapter(adapterNotes);
                /*notesListView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                        DividerItemDecoration.VERTICAL));*/
                adapterNotes.setClickListener(new AnnotationsAdapter.ItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        CrmEntities.Annotations.Annotation note = annotations.list.get(position);
                        // If the note belongs to the user or if it has an attachment, show the note
                        // options dialog
                        if (note.belongsTo(MediUser.getMe().systemuserid) || (note.isDocument) ) {
                            showNoteOptions(note);
                        } else {
                            Toast.makeText(context, "No", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                // refreshLayout.finishRefresh();
                txtNotesLoading.setVisibility(View.GONE);
                pbNotesLoading.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.w(TAG, "onFailure: " + error.getLocalizedMessage());
                Toast.makeText(getApplicationContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                refreshLayout.finishRefresh();
                txtNotesLoading.setVisibility(View.GONE);
                pbNotesLoading.setVisibility(View.GONE);
            }
        });
    }

    void showNoteOptions(final CrmEntities.Annotations.Annotation clickedNote) {
        dialogNoteOptions = new Dialog(context);
        final Context c = context;
        dialogNoteOptions.setContentView(R.layout.dialog_note_options);
        LinearLayout layoutAttachments = dialogNoteOptions.findViewById(R.id.layout_attachments_container);
        // layoutAttachments.setVisibility(clickedNote.isDocument ? View.VISIBLE : View.GONE);
        TextView txtNoteYourNote = dialogNoteOptions.findViewById(R.id.txtNotYourNote);
        lastClickedNote = clickedNote;


        Button btnEdit = dialogNoteOptions.findViewById(R.id.btn_edit_note);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddEditNote(clickedNote);
                dialogNoteOptions.dismiss();
            }
        });
        Button btnDelete = dialogNoteOptions.findViewById(R.id.btn_delete_note);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // final MyProgressDialog deleteProgressDialog = new MyProgressDialog(context, "Deleting note...");
                // deleteProgressDialog.show();
                Toast.makeText(context, "Removing note...", Toast.LENGTH_SHORT).show();
                clickedNote.subject = "(deleting...) " + clickedNote.subject;
                clickedNote.inUse = true;
                adapterNotes.notifyDataSetChanged();
                dialogNoteOptions.dismiss();

                clickedNote.delete(context, new MyInterfaces.YesNoResult() {
                    @Override
                    public void onYes(@Nullable Object object) {
                        Toast.makeText(context, "Note was deleted.", Toast.LENGTH_SHORT).show();
                        adapterNotes.removeAnnotationAndReload(clickedNote);
                    }

                    @Override
                    public void onNo(@Nullable Object object) {
                        Toast.makeText(context, "Failed to delete note!\n" + object.toString(), Toast.LENGTH_SHORT).show();
                        clickedNote.inUse = false;
                        getNotes();
                    }
                });
                dialogNoteOptions.dismiss();
            }
        });
        Button btnViewAttachment = dialogNoteOptions.findViewById(R.id.btnViewAttachment);
        btnViewAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Helpers.Files.AttachmentTempFiles.fileExists(clickedNote.filename)) {
                    final MyProgressDialog getNoteProgress = new MyProgressDialog(context, "Retrieving attachment...");
                    getNoteProgress.show();
                    clickedNote.inUse = true;
                    adapterNotes.notifyDataSetChanged();
                    CrmEntities.Annotations.getAnnotationFromCrm(clickedNote.annotationid, true, new MyInterfaces.CrmRequestListener() {
                        @Override
                        public void onComplete(Object result) {
                            String response = result.toString();
                            CrmEntities.Annotations annotations = new CrmEntities.Annotations(response);
                            CrmEntities.Annotations.Annotation annotation;
                            if (annotations.list.size() > 0) {
                                annotation = annotations.list.get(0);
                                Log.i(TAG, "onSuccess Annotation retrieved: " + annotation.toString());
                                getNoteProgress.dismiss();

                                File attachment = Helpers.Files.base64Decode(annotation.documentBody,
                                        new File(Helpers.Files.AttachmentTempFiles.getDirectory() + File.separator +
                                                annotation.filename));
                                Helpers.Files.openFile(attachment, annotation.mimetype);
                            }
                            clickedNote.inUse = false;
                            adapterNotes.notifyDataSetChanged();
                        }

                        @Override
                        public void onProgress(Crm.AsyncProgress progress) {
                            getNoteProgress.setTitleText("Retrieving attachment (" + progress.getCompletedMb() + " mb)");
                        }

                        @Override
                        public void onFail(String error) {
                            Toast.makeText(context, "Failed to retrieve note!\nError: " + error, Toast.LENGTH_SHORT).show();
                            getNoteProgress.dismiss();
                            clickedNote.inUse = false;
                            adapterNotes.notifyDataSetChanged();
                        }
                    });
                } else {
                    Helpers.Files.openFile(Helpers.Files.AttachmentTempFiles.retrieve(clickedNote.filename), clickedNote.mimetype);
                }
                dialogNoteOptions.dismiss();
            }
        });
        Button btnShareAttachment = dialogNoteOptions.findViewById(R.id.btn_share_attachment);
        btnShareAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Helpers.Files.AttachmentTempFiles.fileExists(clickedNote.filename)) {
                    final MyProgressDialog getNoteProgress = new MyProgressDialog(context, "Retrieving attachment...");
                    getNoteProgress.show();
                    clickedNote.inUse = true;
                    adapterNotes.notifyDataSetChanged();
                    CrmEntities.Annotations.getAnnotationFromCrm(clickedNote.annotationid, true, new MyInterfaces.CrmRequestListener() {
                        @Override
                        public void onComplete(Object result) {
                            String response = new String(result.toString());
                            CrmEntities.Annotations annotations = new CrmEntities.Annotations(response);
                            CrmEntities.Annotations.Annotation annotation;
                            if (annotations.list.size() > 0) {
                                annotation = annotations.list.get(0);
                                Log.i(TAG, "onSuccess Annotation retrieved: " + annotation.toString());
                                getNoteProgress.dismiss();

                                File attachment = Helpers.Files.base64Decode(annotation.documentBody,
                                        new File(Helpers.Files.AttachmentTempFiles.getDirectory() + File.separator +
                                                annotation.filename));
                                Helpers.Files.shareFile(context, attachment);
                            }
                            clickedNote.inUse = false;
                            adapterNotes.notifyDataSetChanged();
                        }

                        @Override
                        public void onProgress(Crm.AsyncProgress progress) {
                            getNoteProgress.setTitleText("Retrieving attachment (" + progress.getCompletedMb() + ")");
                        }

                        @Override
                        public void onFail(String error) {
                            Toast.makeText(context, "Failed to retrieve note!\nError: " + error, Toast.LENGTH_SHORT).show();
                            getNoteProgress.dismiss();
                            clickedNote.inUse = false;
                            adapterNotes.notifyDataSetChanged();
                        }
                    });
                } else {
                    File attachment = Helpers.Files.base64Decode(clickedNote.documentBody,
                            new File(Helpers.Files.AttachmentTempFiles.getDirectory() + File.separator +
                                    clickedNote.filename));
                    Helpers.Files.shareFile(context, attachment);
                }
                dialogNoteOptions.dismiss();
            }
        });
        Button btnRemoveAttachment = dialogNoteOptions.findViewById(R.id.btn_remove_attachment);
        btnRemoveAttachment.setEnabled(MediUser.getMe().isMe(clickedNote.createdByValue));
        btnRemoveAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogNoteOptions.dismiss();
                clickedNote.isDocument = false;
                clickedNote.filename = null;
                clickedNote.filesize = 0;
                clickedNote.documentBody = null;
                adapterNotes.notifyDataSetChanged();
                Toast.makeText(context, "Updating server...", Toast.LENGTH_SHORT).show();
                clickedNote.inUse = true;
                adapterNotes.notifyDataSetChanged();
                clickedNote.removeAttachment(context, new MyInterfaces.YesNoResult() {
                    @Override
                    public void onYes(@Nullable Object object) {
                        Log.i(TAG, "onYes ");
                        Toast.makeText(context, "Attachment was removed!", Toast.LENGTH_SHORT).show();
                        clickedNote.isDocument = false;
                        clickedNote.filename = null;
                        clickedNote.filesize = 0;
                        clickedNote.documentBody = null;
                        clickedNote.inUse = false;
                        adapterNotes.notifyDataSetChanged();
                    }

                    @Override
                    public void onNo(@Nullable Object object) {
                        Log.i(TAG, "onNo ");
                        Toast.makeText(context, "Failed to remove attachment\nError: " + object.toString(), Toast.LENGTH_SHORT).show();
                        clickedNote.inUse = false;
                        adapterNotes.notifyDataSetChanged();
                        getNotes();
                    }
                });
                clickedNote.isDocument = true;
                clickedNote.filename = "removing attachment...";
                clickedNote.filesize = 0;
                clickedNote.documentBody = "";
                adapterNotes.notifyDataSetChanged();
                dialogNoteOptions.dismiss();
            }
        });
        Button btnAddAttachment = dialogNoteOptions.findViewById(R.id.btn_add_attachment);
        btnAddAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Helpers.Permissions.RequestContainer container = new Helpers.Permissions.RequestContainer();
                container.add(Helpers.Permissions.PermissionType.CAMERA);
                if (!Helpers.Permissions.isGranted(Helpers.Permissions.PermissionType.CAMERA)) {
                    requestPermissions(container.toArray(), PERM_REQUEST_CAMERA_ADD_ATTACHMENT);
                    return;
                }

                Log.i(TAG, "onClick ");
                Intent intent = new Intent(context, FilePickerActivity.class);
                intent.putExtra(FilePickerActivity.CONFIGS, new Configurations.Builder()
                        .setCheckPermission(true)
                        .setShowImages(true)
                        .setShowFiles(true)
                        .setSingleClickSelection(true)
                        .setShowVideos(false)
                        .enableImageCapture(true)
                        .setSkipZeroSizeFiles(true)
                        .build());
                startActivityForResult(intent, FILE_REQUEST_CODE);
                newImageBaseAnnotation = clickedNote;
                dialogNoteOptions.dismiss();
                clickedNote.inUse = true;
                adapterNotes.notifyDataSetChanged();
                dialogNoteOptions.dismiss();
            }
        });

        // show/hide/enable attachment buttons
        if (clickedNote.belongsTo(MediUser.getMe().systemuserid)) {
            // belongs to me and has attachment
            if (clickedNote.isDocument) {
                btnAddAttachment.setEnabled(false);
                btnRemoveAttachment.setEnabled(true);
            } else {
                // belongs to me but no attachment
                btnAddAttachment.setEnabled(true);
                btnRemoveAttachment.setEnabled(false);
            }
        } else {
            // Doesn't belong to me
            btnAddAttachment.setEnabled(false);
            btnRemoveAttachment.setEnabled(false);
        }

        btnShareAttachment.setEnabled(clickedNote.isDocument);
        btnViewAttachment.setEnabled(clickedNote.isDocument);

        dialogNoteOptions.setTitle("");
        dialogNoteOptions.setCancelable(true);
        dialogNoteOptions.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialogNoteOptions.show();

        // See if the current user is the author of the clicked note
        btnEdit.setEnabled(clickedNote.createdByValue.equals(MediUser.getMe().systemuserid) ? true : false);
        btnDelete.setEnabled(clickedNote.createdByValue.equals(MediUser.getMe().systemuserid) ? true : false);
        txtNoteYourNote.setVisibility(clickedNote.createdByValue.equals(MediUser.getMe().systemuserid) ? View.GONE : View.VISIBLE);

    }
}