package com.fimbleenterprises.medimileage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import cz.msebera.android.httpclient.Header;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.CrmEntities.Annotations.Annotation;
import com.fimbleenterprises.medimileage.CrmEntities.Opportunities.Opportunity;
import com.google.android.gms.maps.internal.ICameraUpdateFactoryDelegate;
import com.jaiselrahman.filepicker.activity.FilePickerActivity;
import com.jaiselrahman.filepicker.config.Configurations;
import com.jaiselrahman.filepicker.model.MediaFile;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class OpportunityActivity extends AppCompatActivity {
    private static final String TAG = "OpportunityActivity";
    public static final String OPPORTUNITY_TAG = "OPPORTUNITY_TAG";
    private static final int FILE_REQUEST_CODE = 88;
    private static final String ATTACHMENT_ANNOTATION = "ATTACHMENT_ANNOTATION";
    public Opportunity opportunity;
    NonScrollRecyclerView notesListView;
    AnnotationsAdapter adapterNotes;
    String pendingNote;
    Context context;
    Annotation newImageBaseAnnotation;
    Dialog dialogNoteOptions;
    public static final int PERM_REQUEST_CAMERA_ADD_ATTACHMENT = 11;

    // Fields
    TextView txtAccount;
    TextView txtTopic;
    TextView txtStatus;
    TextView txtDealStatus;
    TextView txtDealType;
    TextView txtCloseProb;
    TextView txtBackground;
    SmartRefreshLayout refreshLayout;
    TextView txtNotesLoading;
    ProgressBar pbNotesLoading;
    ImageButton btnAddNote;
    Annotation lastClickedNote;

    public enum NoteAction {
        CREATE, EDIT, DELETE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_opportunity);

        setTitle("Opportunity Details");

        this.context = this;

        notesListView = findViewById(R.id.notesRecyclerView);

        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                getOpportunityNotes();
            }
        });

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(OPPORTUNITY_TAG)) {
            opportunity = intent.getParcelableExtra(OPPORTUNITY_TAG);
            Log.i(TAG, "onCreate Opportunity was passed (" + opportunity.name + ")");
            populateOppDetails();
            getOpportunityNotes();
        }

        Helpers.Files.AttachmentTempFiles.makeDirectory();
        Helpers.Files.AttachmentTempFiles.clear();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.basic_list_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        Typeface typeface = getResources().getFont(R.font.casual);

        for (int i = 0; i < menu.size(); i++) {
            MenuItem mi = menu.getItem(i);
            //for aapplying a font to subMenu ...
            SubMenu subMenu = mi.getSubMenu();
            if (subMenu != null && subMenu.size() > 0) {
                for (int j = 0; j < subMenu.size(); j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem, typeface);
                }
            }
            //the method we have create in activity
            applyFontToMenuItem(mi, typeface);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_refresh :
                refreshLayout.autoRefreshAnimationOnly();
                getOpportunityNotes();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void applyFontToMenuItem(MenuItem mi, Typeface font) {
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
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

    void populateOppDetails() {

        txtNotesLoading = findViewById(R.id.textViewopportunityNotesLoading);
        btnAddNote = findViewById(R.id.btnAddNote);
        pbNotesLoading = findViewById(R.id.progressBarWorking);
        btnAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Annotation newNote = new Annotation();
                newNote.subject = "MileBuddy added note";
                newNote.objectid = opportunity.opportunityid;
                newNote.isDocument = false;
                newNote.objectEntityName = "opportunity";
                showAddEditNote(newNote);
            }
        });

        txtAccount = findViewById(R.id.textView_OppAccount);
        txtTopic = findViewById(R.id.textView_OppTopic);
        txtStatus = findViewById(R.id.textView_OppStatus);
        txtDealStatus = findViewById(R.id.textView_OppDealStatus);
        txtDealType = findViewById(R.id.textView_OppDealType);
        txtCloseProb = findViewById(R.id.textView_OppCloseProb);
        txtBackground = findViewById(R.id.textView_OppBackground);
        txtAccount.setText(opportunity.accountname);
        txtTopic.setText(opportunity.name);
        txtStatus.setText(opportunity.status);
        txtDealStatus.setText(opportunity.dealStatus);
        txtDealType.setText(opportunity.dealTypePretty);
        txtCloseProb.setText(opportunity.probabilityPretty);
        txtBackground.setText(opportunity.currentSituation);

        Helpers.Animations.pulseAnimation(btnAddNote);
    }

    void showAddEditNote(@Nullable final Annotation clickedNote) {

        final boolean isEditing = clickedNote.annotationid != null;
        final String originalSubject = clickedNote.subject;

        final Dialog dialog = new Dialog(OpportunityActivity.this);
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
                        getOpportunityNotes();
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

    void getOpportunityNotes() {
        ArrayList<Requests.Argument> args = new ArrayList<>();
        Requests.Argument argument = new Requests.Argument("query", Queries.Annotations.getAnnotations(opportunity.opportunityid));
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
                notesListView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                notesListView.setAdapter(adapterNotes);
                /*notesListView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                        DividerItemDecoration.VERTICAL));*/
                adapterNotes.setClickListener(new AnnotationsAdapter.ItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Annotation note = annotations.list.get(position);
                        // If the note belongs to the user or if it has an attachment, show the note
                        // options dialog
                        if (note.belongsTo(MediUser.getMe().systemuserid) || (note.isDocument) ) {
                            showNoteOptions(note);
                        } else {
                            Toast.makeText(context, "No", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                refreshLayout.finishRefresh();
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
    
    void showNoteOptions(final Annotation clickedNote) {
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
                        getOpportunityNotes();
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
                            Annotation annotation;
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
                            Annotation annotation;
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
                        getOpportunityNotes();
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

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }
}