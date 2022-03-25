package com.fimbleenterprises.demobuddy.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import cz.msebera.android.httpclient.Header;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fimbleenterprises.demobuddy.MyApp;
import com.fimbleenterprises.demobuddy.adapters.EmailsAndAnnotationsAdapter;
import com.fimbleenterprises.demobuddy.dialogs.MyDatePicker;
import com.fimbleenterprises.demobuddy.dialogs.MyYesNoDialog;
import com.fimbleenterprises.demobuddy.activities.fullscreen_pickers.FullscreenActivityChooseContact;
import com.fimbleenterprises.demobuddy.objects_and_containers.BasicEntity;
import com.fimbleenterprises.demobuddy.adapters.BasicEntityActivityObjectRecyclerAdapter;
import com.fimbleenterprises.demobuddy.dialogs.ContactActions;
import com.fimbleenterprises.demobuddy.Crm;
import com.fimbleenterprises.demobuddy.objects_and_containers.Contacts;
import com.fimbleenterprises.demobuddy.objects_and_containers.CrmEntities;
import com.fimbleenterprises.demobuddy.CustomTypefaceSpan;
import com.fimbleenterprises.demobuddy.objects_and_containers.EmailsOrAnnotations;
import com.fimbleenterprises.demobuddy.objects_and_containers.EntityContainers;
import com.fimbleenterprises.demobuddy.Helpers;
import com.fimbleenterprises.demobuddy.objects_and_containers.MediUser;
import com.fimbleenterprises.demobuddy.MyInterfaces;
import com.fimbleenterprises.demobuddy.dialogs.MyProgressDialog;
import com.fimbleenterprises.demobuddy.MyPreferencesHelper;
import com.fimbleenterprises.demobuddy.activities.ui.views.NonScrollRecyclerView;
import com.fimbleenterprises.demobuddy.CrmQueries;
import com.fimbleenterprises.demobuddy.R;
import com.fimbleenterprises.demobuddy.objects_and_containers.Opportunities;
import com.fimbleenterprises.demobuddy.objects_and_containers.Requests;
import com.fimbleenterprises.demobuddy.objects_and_containers.Territories.Territory;
import com.fimbleenterprises.demobuddy.activities.fullscreen_pickers.FullscreenActivityChooseAccount;

import com.fimbleenterprises.demobuddy.objects_and_containers.Tickets;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.jaiselrahman.filepicker.activity.FilePickerActivity;
import com.jaiselrahman.filepicker.config.Configurations;
import com.jaiselrahman.filepicker.model.MediaFile;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BasicEntityActivity extends AppCompatActivity {

    public static final String MENU_SELECTION = "MENU_SELECTION";
    public static final String SEND_EMAIL = "SEND_EMAIL";
    public static final String EXPORT_TO_EXCEL = "EXPORT_TO_EXCEL";
    public static final String ENTITY_LOGICAL_NAME = "ENTITY_LOGICAL_NAME";
    public static final String GSON_STRING = "GSON_STRING";
    public static final String ENTITYID = "ENTITYID";
    public static final String CLICK_ACTION = "CLICK_ACTION";
    public static final String LABEL = "LABEL";
    public static final String VALUE = "VALUE";
    public static final String LIST_POSITION = "LIST_POSITION";
    public static final String ACTIVITY_TITLE = "ACTIVITY_TITLE";
    public static final int REQUEST_BASIC = 6288;
    public static final String LOAD_NOTES = "LOAD_NOTES";
    public static final String HIDE_MENU = "HIDE_MENU";
    public static final String CREATE_NEW = "CREATE_NEW";
    private static final String TAG = "BasicEntityActivity";
    private static final int FILE_REQUEST_CODE = 88;
    public static final String CURRENT_TERRITORY = "CURRENT_TERRITORY";
    public static final String ENTITY_UPDATED = "ENTITY_UPDATED";
    public static final String RESULT_ENTITY_DELETED = "RESULT_ENTITY_DELETED";
    public static final int RESULT_CODE_ENTITY_UPDATED = 111;
    public static final int RESULT_CODE_ENTITY_DELETED = 222;

    Context context;
    Activity activity;
    String entityid;
    String entityLogicalName;
    String activityTitle;
    final EmailsOrAnnotations emailsOrAnnotations = new EmailsOrAnnotations();
    BasicEntityActivityObjectRecyclerAdapter adapter;
    FloatingActionButton fabComment;
    FloatingActionButton fabEdit;
    NonScrollRecyclerView notesListView;
    CrmEntities.Annotations.Annotation newImageBaseAnnotation;
    Dialog dialogNoteOptions;
    public static final int PERM_REQUEST_CAMERA_ADD_ATTACHMENT = 11;
    EmailsAndAnnotationsAdapter emailsAndAnnoationsAdapter;
    String pendingNote;
    RefreshLayout refreshLayout;
    CrmEntities.Annotations.Annotation lastClickedNote;
    TextView txtNotesLoading;
    ProgressBar pbNotesLoading;
    Button btnViewMails;
    TableLayout tblNotes;
    BasicEntity basicEntity;
    MyPreferencesHelper options;
    boolean hideMenu = false;
    boolean isEditMode = false;
    boolean isCreateMode = false;
    ArrayList<CrmEntities.Accounts.Account> cachedAccounts;
    Spinner spinnerStatus;
    Territory currentTerritory;
    boolean statusChangePending = false;
    boolean updatePending = false;
    ScrollView mainScrollview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        activity = this;
        setContentView(R.layout.basic_entity_activity_with_emails);
        options = new MyPreferencesHelper(context);
        updatePending = false;

        try {
            if (currentTerritory == null) {
                currentTerritory = MediUser.getMe().getTerritory();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Find and get handles on all of our important activity bits
        mainScrollview = findViewById(R.id.scrollview);
        tblNotes = findViewById(R.id.tableLayout_notes);
        refreshLayout = findViewById(R.id.refreshLayout);
        notesListView = findViewById(R.id.notesRecyclerView);
        txtNotesLoading = findViewById(R.id.textViewopportunityNotesLoading);
        pbNotesLoading = findViewById(R.id.progressBarWorking);
        btnViewMails = findViewById(R.id.btnViewEmails);
        btnViewMails.setEnabled(false);
        /*
        -= Switched to a floating action button (v1.85)=-
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
        });*/

        // Set up the floating action button and add a click listener for adding notes to the entity.
        fabComment = findViewById(R.id.fab_addNote);
        fabComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Cannot comment with demo app", Toast.LENGTH_SHORT).show();

                /*CrmEntities.Annotations.Annotation newNote = new CrmEntities.Annotations.Annotation();
                newNote.subject = "MileBuddy added note";
                newNote.objectid = entityid;
                newNote.isDocument = false;
                newNote.objectEntityName = entityLogicalName;
                showAddEditNote(newNote);
                mainScrollview.smoothScrollTo(0, findViewById(R.id.tableLayout_notes).getTop()); // Scroll to the beginning of the notes*/
            }
        });

        // Set up the floating action button and add a click listener for adding notes to the entity.
        fabEdit = findViewById(R.id.fab_edit);
        fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(context, "Cannot edit with demo app", Toast.LENGTH_SHORT).show();
                
                /*if (!basicEntity.isEditable) {
                    Toast.makeText(context, basicEntity.cannotEditReason, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isEditMode) {
                    if (updatePending) {
                        MyYesNoDialog.show(context, "You have pending changes.  Would you like to save them?", new MyYesNoDialog.YesNoListener() {
                            @Override
                            public void onYes() {
                                updateEntity();
                            }

                            @Override
                            public void onNo() {
                                isEditMode = false;
                                updatePending = false;
                                populateForm();
                            }
                        });
                    } else {
                        isEditMode = false;
                        populateForm(basicEntity.toGson(), false);
                    }
                } else {
                    isEditMode = true;
                    populateForm(basicEntity.toGson(), true);
                }*/
            }
        });

        // If the user was cool enough to get here via a URL we should be cool and try to load that record!
        if (getIntent().getData() != null && getIntent().getData().toString().contains("https://crmauth.medistim.com")) {
            loadRecordFromUrl();
            return;
        }

        // Get the gson string from the intent which should absolutely be there
        String gson = getIntent().getStringExtra(GSON_STRING);
        this.basicEntity = new Gson().fromJson(gson, BasicEntity.class);
        entityid = getIntent().getStringExtra(ENTITYID);
        entityLogicalName = getIntent().getStringExtra(ENTITY_LOGICAL_NAME);
        setTitle(getIntent().getStringExtra(ACTIVITY_TITLE));
        hideMenu = getIntent().getBooleanExtra(HIDE_MENU, false);
        if (getIntent().getParcelableExtra(CURRENT_TERRITORY) != null) {
            currentTerritory = getIntent().getParcelableExtra(CURRENT_TERRITORY);
        }
        if (getIntent().hasExtra(CREATE_NEW)) {
            isEditMode = getIntent().getBooleanExtra(CREATE_NEW, false);
            isCreateMode = getIntent().getBooleanExtra(CREATE_NEW, false);
            if (entityLogicalName.equals("incident")) {

            }
        }

        // Hide the notes table and the floating add note button button by default
        tblNotes.setVisibility(View.GONE);
        fabComment.setVisibility(View.GONE);

        if (gson != null) {
            // See if notes should be loaded (default is yes)
            if (getIntent() != null && getIntent().getBooleanExtra(LOAD_NOTES, true)) {
                getNotes();
                getEmails();
                tblNotes.setVisibility(View.VISIBLE);
                // Show the floating action button since this entity has notes
                fabComment.setVisibility(View.VISIBLE);
            }
            if (getIntent().hasExtra(CREATE_NEW)) {
                if (getIntent().getBooleanExtra(CREATE_NEW, false)) {
                    populateForm(gson, true);
                } else {
                    populateForm(gson, false);
                }
            } else {
                populateForm(gson, isEditMode);
            }
        } else {
            Toast.makeText(context, "Failed to load!", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApp.setIsVisible(false, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApp.setIsVisible(true, this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (updatePending) {
                MyYesNoDialog.show(context, "You have pending changes.  Would you like to save them?", new MyYesNoDialog.YesNoListener() {
                    @Override
                    public void onYes() {
                        updateEntity();
                    }

                    @Override
                    public void onNo() {
                        isEditMode = false;
                        updatePending = false;
                        populateForm();
                    }
                });
                return false;
            } else if (this.isEditMode) {
                this.isEditMode = false;
                populateForm(basicEntity.toGson(), false);
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.generic_activity_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        Typeface typeface = getResources().getFont(R.font.casual);

        for (int i = 0; i < menu.size(); i++) {
            MenuItem mi = menu.getItem(i);
            //for applying a font to subMenu ...
            SubMenu subMenu = mi.getSubMenu();
            if (subMenu != null && subMenu.size() > 0) {
                for (int j = 0; j < subMenu.size(); j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem, typeface);
                }
            }
            //the method we have create in activity
            applyFontToMenuItem(mi, typeface);

            mi.setEnabled(!hideMenu);

        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_send_email:

                try {
                    String emailSuffix = "";

                    Log.i(TAG, "onActivityResult Received a " + SEND_EMAIL + " result extra");
                    String entityid = this.entityid;
                    String entityLogicalName = this.entityLogicalName;
                    Log.i(TAG, "onActivityResult Entityid: " + entityid + " - Entity logical name: " + entityLogicalName);
                    Log.i(TAG, "onActivityResult ");

                    String recordurl = "";
                    recordurl = Crm.getRecordUrl(entityid, Integer.toString(Crm.tryGetEntityTypeCodeFromLogicalName(entityLogicalName)));
                    emailSuffix = "\n\nCRM Link:\n" + recordurl;
                    Log.i(TAG, "onActivityResult:: " + recordurl);

                    Helpers.Email.sendEmail(emailSuffix + "\n\n", "CRM Link", this);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Failed to create email link!\n" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.action_edit:
                if (!basicEntity.isEditable) {
                    Toast.makeText(context, basicEntity.cannotEditReason, Toast.LENGTH_SHORT).show();
                    return true;
                }
                isEditMode = true;
                populateForm(basicEntity.toGson(), true);
                break;
            case R.id.action_update:
                if (isCreateMode) {
                    createEntity();
                } else if (updatePending) {
                    updateEntity();
                } else if (statusChangePending) {
                    updateEntityStatus();
                }
                break;
            case R.id.action_delete:
                MyYesNoDialog.show(context, new MyYesNoDialog.YesNoListener() {
                    @Override
                    public void onYes() {
                        deleteEntity();
                    }

                    @Override
                    public void onNo() {
                        // nothing
                    }
                });
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
                emailsAndAnnoationsAdapter.notifyDataSetChanged();
                dialogNoteOptions.dismiss();
            }
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the file picker was busy and has a chosen file for us to attach to a new note!
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
                    emailsAndAnnoationsAdapter.notifyDataSetChanged();
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
                            emailsAndAnnoationsAdapter.notifyDataSetChanged();
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
                            emailsAndAnnoationsAdapter.notifyDataSetChanged();
                            Toast.makeText(context, "Failed!\n" + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.i(TAG, "onActivityResult ");

                    // While we wait for the attachment to be attached we can insert a placeholder
                    newImageBaseAnnotation.filename = "uploading attachment...";
                    newImageBaseAnnotation.documentBody = "";
                    newImageBaseAnnotation.isDocument = true;
                    newImageBaseAnnotation.inUse = true;
                    emailsAndAnnoationsAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(context, "Image not selected", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == FullscreenActivityChooseAccount.REQUESTCODE) {
            if (data != null) {
                if (data.hasExtra(FullscreenActivityChooseAccount.ACCOUNT_RESULT)) {
                    Log.i(TAG, "onActivityResult Account chosen!");
                    CrmEntities.Accounts.Account account = data.getParcelableExtra(FullscreenActivityChooseAccount.ACCOUNT_RESULT);
                    if (options.hasCachedAccounts()) {
                        cachedAccounts = options.getCachedAccounts().list;
                    }
                    this.basicEntity.setAccount(account);
                    populateForm(this.basicEntity.toGson(), isEditMode);
                    updatePending = true;
                    Log.i(TAG, "onActivityResult Chosen account: " + account.accountName);
                }
            }
        } else if (requestCode == FullscreenActivityChooseContact.REQUESTCODE) {
            if (data != null) {
                if (data.hasExtra(FullscreenActivityChooseContact.CONTACT_RESULT)) {
                    Contacts.Contact contact = data.getParcelableExtra(FullscreenActivityChooseContact.CONTACT_RESULT);
                    this.basicEntity.setContact(contact);
                    populateForm(this.basicEntity.toGson(), isEditMode);
                    updatePending = true;
                }
            }
        }

        if (options.hasCachedAccounts()) {
            cachedAccounts = options.getCachedAccounts().list;
        }
    }

    void deleteEntity() {
        MyProgressDialog progressDialog = new MyProgressDialog(context, "Deleting...");
        progressDialog.show();

        Requests.Request request = new Requests.Request(Requests.Request.Function.DELETE);
        request.arguments.add(new Requests.Argument("entity", this.entityLogicalName));
        request.arguments.add(new Requests.Argument("entityid", this.entityid));
        request.arguments.add(new Requests.Argument("asUser", MediUser.getMe(context).systemuserid));

        new Crm().makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Intent resultIntent = new Intent(ENTITY_UPDATED);
                resultIntent.putExtra(GSON_STRING, basicEntity.toGson());
                setResult(RESULT_CODE_ENTITY_DELETED, resultIntent);
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    void populateForm() {
        populateForm(basicEntity.toGson(), false);
    }

    void populateForm(final String gson, boolean makeEditable) {

        if (this.basicEntity == null) {
            return;
        }

        // Check if a status was associated and show that status if so
        View statusTable = findViewById(R.id.tableLayout_status_table);

        statusTable.setVisibility(this.basicEntity.hasStatusValue() ? View.VISIBLE : View.GONE);

        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerStatus.setEnabled(isEditMode);
        spinnerStatus.setBackgroundColor(isEditMode ? Color.parseColor(
                BasicEntityActivityObjectRecyclerAdapter.editColor) : Color.parseColor("#00000000"));

        if (this.basicEntity.hasStatusValue()) {
            ArrayAdapter arrayAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item, this.basicEntity.toStatusReasonsArray());
            spinnerStatus.setAdapter(arrayAdapter);
            spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    BasicEntity.EntityStatusReason selectedReason = basicEntity.availableEntityStatusReasons.get(i);
                    if (!selectedReason.statusReasonText.equals(basicEntity.entityStatusReason.statusReasonText)) {
                        Log.i(TAG, "onItemSelected ");
                        statusChangePending = true;
                        basicEntity.entityStatusReason = selectedReason;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    Log.i(TAG, "onNothingSelected ");
                }
            });
            spinnerStatus.setSelection(this.basicEntity.getStatusReasonIndex());
        }

        NonScrollRecyclerView recyclerView = findViewById(R.id.rvBasicObjects);
        adapter = new BasicEntityActivityObjectRecyclerAdapter(this, this.basicEntity.fields,
                new BasicEntityActivityObjectRecyclerAdapter.OnFieldsUpdatedListener() {
                    /*******************************************************************************
                    * THIS IS WHERE THE MAIN ENTITY OBJECT IS UPDATED WHEN USER CHANGES VALUES
                    *******************************************************************************/
                    @Override
                    public void onUpdated(ArrayList<BasicEntity.EntityBasicField> fields) {
                        basicEntity.fields = fields;
                        if (isEditMode) {
                            updatePending = true;
                        }
                        Log.i(TAG, "onUpdated ");
                    }
                });

        /*******************************************************************************
        * This is the listview's row instead of the view it may contain.  There should be identical logic
        * for any interactive view inside this row.
        *******************************************************************************/
        adapter.setClickListener(new BasicEntityActivityObjectRecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                BasicEntity.EntityBasicField field = basicEntity.fields.get(position);

                if (isEditMode) {
                    for (BasicEntity.EntityBasicField f : basicEntity.fields) {
                        if (!f.isAccountField) {
                            f.isEditable = true;
                        }
                    }
                } else {
                    if (!field.isAccountField && !field.isContactField && !field.isDateField && !field.isOptionSet) {
                        // Toast.makeText(context, adapter.mData.get(position).value, Toast.LENGTH_LONG).show();
                        final Dialog dialog = new Dialog(context);
                        dialog.setContentView(R.layout.dialog_show_single_field);
                        dialog.setCancelable(true);
                        TextView textView = dialog.findViewById(R.id.txtContent);
                        textView.setText(field.value);
                        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                            @Override
                            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                if (keyCode == KeyEvent.KEYCODE_BACK) {
                                    dialog.dismiss();
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                        });
                        dialog.show();
                    }
                }

                if (field.isAccountField) {
                    if (isEditMode) {
                        Intent intent = new Intent(context, FullscreenActivityChooseAccount.class);
                        intent.putExtra(FullscreenActivityChooseAccount.CURRENT_ACCOUNT, field.account);
                        intent.putExtra(FullscreenActivityChooseAccount.CURRENT_TERRITORY, currentTerritory);
                        startActivityForResult(intent, FullscreenActivityChooseAccount.REQUESTCODE);
                    } else {
                        Intent intent = new Intent(context, Activity_AccountData.class);
                        intent.setAction(Activity_AccountData.GO_TO_ACCOUNT);
                        intent.putExtra(Activity_AccountData.GO_TO_ACCOUNT_OBJECT, field.account);
                        startActivity(intent);
                    }
                }
            }
        });

        /*******************************************************************************
         * This is a button inside the row, probably for an account entity value
         *******************************************************************************/
        adapter.setButtonClickListener(new BasicEntityActivityObjectRecyclerAdapter.ItemButtonClickListener() {
            @Override
            public void onItemButtonClick(View view, int position) {
                final BasicEntity.EntityBasicField field = basicEntity.fields.get(position);
                if (field.isAccountField) {
                    if (isEditMode) {
                        Log.i(TAG, "onItemClick ACCOUNT FIELD WHILE EDITING!");
                        Intent intent = new Intent(context, FullscreenActivityChooseAccount.class);
                        intent.putExtra(FullscreenActivityChooseAccount.CURRENT_ACCOUNT, field.account);
                        intent.putExtra(FullscreenActivityChooseAccount.CURRENT_TERRITORY, currentTerritory);
                        startActivityForResult(intent, FullscreenActivityChooseAccount.REQUESTCODE);
                    } else {
                        Intent intent = new Intent(context, Activity_AccountData.class);
                        intent.setAction(Activity_AccountData.GO_TO_ACCOUNT);
                        intent.putExtra(Activity_AccountData.GO_TO_ACCOUNT_OBJECT, field.account);
                        startActivity(intent);
                    }
                } else if (field.isContactField) {
                    if (isEditMode) {
                        FullscreenActivityChooseContact.showPicker(activity, currentTerritory, FullscreenActivityChooseContact.REQUESTCODE);
                    } else {
                        final MyProgressDialog progressDialog = new MyProgressDialog(context, "Getting contact info...");
                        progressDialog.show();

                        String query = CrmQueries.Contacts.getContact(field.contact.entityid);
                        Requests.Request request = new Requests.Request(Requests.Request.Function.GET);
                        ArrayList<Requests.Argument> args = new ArrayList<>();
                        args.add(new Requests.Argument("query", query));
                        request.arguments = args;
                        new Crm().makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                String response = new String(responseBody);
                                Contacts contacts = new Contacts(response);
                                ContactActions actions = new ContactActions(activity, contacts.list.get(0));
                                actions.showContactOptions();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                Toast.makeText(context, "Failed to get contact info\n" + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        });
                    }
                } else if (field.isDateField) {
                    // Try to parse out the date from the field's value - no promises!
                    DateTime dtValue = DateTime.now();
                    try {
                        dtValue = Helpers.DatesAndTimes.parseDate(field.value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    MyDatePicker datePicker = new MyDatePicker(context, dtValue, new MyInterfaces.DateSelector() {
                        @Override
                        public void onDateSelected(DateTime selectedDate, String selectedDateStr) {
                            field.value = Helpers.DatesAndTimes.getPrettyDate(selectedDate);
                            adapter.mData = basicEntity.fields;
                            adapter.notifyDataSetChanged();
                            updatePending = true;
                        }
                    });
                    datePicker.show();
                } else if (field.isDateTimeField) {
                    // DOn't have a date time picker yet.
                }
            }
        });

        for (BasicEntity.EntityBasicField field : this.basicEntity.fields) {
            field.isEditable = makeEditable;
        }

        /*******************************************************************************
         * This should be fired only if the spinner marked as the entity status is actually changed.
         *******************************************************************************/
        adapter.setOnStatusChangedListener(new BasicEntityActivityObjectRecyclerAdapter.OnStatusChangedListener() {
            @Override
            public void onStatusChanged(BasicEntity.EntityStatusReason oldStatus, BasicEntity.EntityStatusReason newStatus) {
                try {
                    Log.i(TAG, "onStatusChanged old status: " + oldStatus.statusReasonText + " | new status: " + newStatus.statusReasonText);
                    basicEntity.entityStatusReason = newStatus;
                    statusChangePending = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        adapter.isEditMode = isEditMode;
        adapter.isCreateMode = isCreateMode;
        recyclerView.setAdapter(null);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false); // Disables scrolling
        recyclerView.setAdapter(adapter);
    }

    void updateEntity() {

        final MyProgressDialog progressDialog = new MyProgressDialog(context, "Updating...");
        progressDialog.show();

        EntityContainers.EntityContainer container = new EntityContainers.EntityContainer();

        // Build the request
        for (BasicEntity.EntityBasicField field : this.basicEntity.fields) {
            if (!field.isReadOnly && field != null && field.value != null) {
                container.entityFields.add(field.toEntityField());
            }
        }

        Log.i(TAG, "updateEntity ");

        // id, name, cont, user
        Requests.Request request = new Requests.Request(Requests.Request.Function.UPDATE);
        request.arguments.add(new Requests.Argument("entityid", entityid));
        request.arguments.add(new Requests.Argument("name", entityLogicalName));
        request.arguments.add(new Requests.Argument("container", container.toJson()));
        request.arguments.add(new Requests.Argument("asuser", MediUser.getMe().systemuserid));

        Crm crm = new Crm();
        crm.makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                isEditMode = false;
                populateForm(basicEntity.toGson(), false);
                Toast.makeText(context, "Updated!", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                updatePending = false;

                // Update entity status if necessary
                if (statusChangePending) {
                    updateEntityStatus();
                }
                isEditMode = false;

                // Send a result intent to any callers interested in the result
                Intent resultIntent = new Intent(ENTITY_UPDATED);
                resultIntent.putExtra(GSON_STRING, basicEntity.toGson());
                setResult(RESULT_CODE_ENTITY_UPDATED, resultIntent);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, "Failed\n" + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    void createEntity() {
        EntityContainers.EntityContainer container = new EntityContainers.EntityContainer();
        for (BasicEntity.EntityBasicField field : this.basicEntity.fields) {
            if (field.isRequired) {
                if (field.isAccountField) {
                    container.entityFields.add(new EntityContainers.EntityField(field.crmFieldName, field.account.entityid));
                } else if (field.isContactField) {
                    container.entityFields.add(new EntityContainers.EntityField(field.crmFieldName, field.contact.entityid));
                } else {
                    container.entityFields.add(new EntityContainers.EntityField(field.crmFieldName, field.value));
                }
            }
        }

        final MyProgressDialog dg = new MyProgressDialog(this, "Creating...");
        dg.show();

        Requests.Request request = new Requests.Request(Requests.Request.Function.CREATE);
        request.arguments.add(new Requests.Argument("entity", "incident"));
        request.arguments.add(new Requests.Argument("asuser", MediUser.getMe().systemuserid));
        request.arguments.add(new Requests.Argument("json", container.toJson()));

        new Crm().makeCrmRequest(this, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                dg.dismiss();
                setResult(RESULT_CODE_ENTITY_UPDATED);
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                dg.dismiss();
                Toast.makeText(context, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void updateEntityStatus() {

        /*
        entityName = (string)value.Arguments[0].value;
        entityid = (string)value.Arguments[1].value;
        int newstate = (int)value.Arguments[2].value;
        int newstatus = (int)value.Arguments[3].value;
        asUserid = (string)value.Arguments[4].value;
        */

        final MyProgressDialog progressDialog = new MyProgressDialog(context, "Updating...");
        progressDialog.show();

        Log.i(TAG, "updateEntity ");

        // id, name, cont, user
        Requests.Request request = new Requests.Request(Requests.Request.Function.SET_STATE);
        request.arguments.add(new Requests.Argument("name", entityLogicalName));
        request.arguments.add(new Requests.Argument("entityid", entityid));
        request.arguments.add(new Requests.Argument("newstate", basicEntity.entityStatusReason.requiredState));
        request.arguments.add(new Requests.Argument("newstatus", basicEntity.entityStatusReason.statusReasonValue));
        request.arguments.add(new Requests.Argument("asuserid", MediUser.getMe().systemuserid));

        Crm crm = new Crm();
        crm.makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i(TAG, "onSuccess ");
                Toast.makeText(BasicEntityActivity.this, "Status was updated", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                statusChangePending = false;
                isEditMode = false;
                populateForm();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.i(TAG, "onFailure ");
                Toast.makeText(BasicEntityActivity.this, "Failed\n" + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });

    }

    void loadRecordFromUrl() {
        // https://crmauth.medistim.com/main.aspx?etc=3&id=%7bbbc26479-adfd-ea11-810b-005056a36b9b%7d&pagetype=entityrecord
        // https://crmauth.medistim.com/main.aspx?pagetype=entityrecord&etc=3&id=%7bB2C53F4C-B47F-EC11-811F-005056A36B9B%7d&extraqs=&newWindow=true&histKey=216705665#881962037
        // https://crmauth.medistim.com/main.aspx?pagetype=entityrecord&etc=112&id=bb951130-ce82-ec11-811f-005056a36b9b&extraqs=&newWindow=true&histKey=201016870#249014784
        // https://crmauth.medistim.com/main.aspx?pagetype=entityrecord&etc=112&id=bb951130-ce82-ec11-811f-005056a36b9b&extraqs=&newWindow=true&histKey=201016870#249014784
        try {
            // Parsing url begins
            String url = getIntent().getData().toString().replace("%7b", "").replace("%7d", "");
            int etcStartPos = url.indexOf("etc=") + 4;
            int GUID_LENGTH = 36;
            String ss1 = url.substring(etcStartPos); // 3&id=%7bbbc26479-adfd-ea11-810b-005056a36b9b%7d&pagetype=entityrecord
            int nextArgStartPos = ss1.indexOf("&"); // Find the next & indicating the start of the next arg and end of the current

            int idStartPos = url.indexOf("id=") + 3;
            String guid = url.substring(idStartPos, idStartPos + 36);

            // We should now have the ugly business of parsing the URL over and be left with a type code and guid.
            final int entityTypeCode = Integer.parseInt(ss1.substring(0, nextArgStartPos)); // Should be typecode
            // final String guid = url.substring(idStartPos + 7, idStartPos + 7 + GUID_LENGTH);

            Log.i(TAG, "loadRecordFromUrl ETC: " + entityTypeCode + ", GUID: " + guid);

            // ***********************************************************************************
            //                                      CASE
            // ***********************************************************************************
            if (entityTypeCode == Crm.ETC_INCIDENT) {
                final MyProgressDialog dialog = new MyProgressDialog(context, "Retrieving ticket with id: " + guid + "...");

                String query = CrmQueries.Tickets.getIncident(guid);
                ArrayList<Requests.Argument> args = new ArrayList<>();
                args.add(new Requests.Argument("query", query));
                Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);
                Crm crm = new Crm();
                crm.makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                        Tickets tickets = new Tickets(new String(responseBody));

                        String gson = tickets.list.get(0).toBasicEntity().toGson();
                        entityid = guid;
                        entityLogicalName = "incident";
                        activityTitle = "Ticket";
                        setTitle(activityTitle);

                        basicEntity = new BasicEntity(gson);
                        populateForm(gson, false);
                        getNotes();

                        dialog.dismiss();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(BasicEntityActivity.this, "Failed to get record!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                dialog.show();
                Log.i(TAG, "onCreate Case record link was parsed!");

            // ***********************************************************************************
            //                                    OPPORTUNITY
            // ***********************************************************************************
            } else if (entityTypeCode == Crm.ETC_OPPORTUNITY) {
                final MyProgressDialog dialog = new MyProgressDialog(context, "Retrieving opportunity with id: " + guid + "...");

                String query = CrmQueries.Opportunities.getOpportunityDetails(guid);
                ArrayList<Requests.Argument> args = new ArrayList<>();
                args.add(new Requests.Argument("query", query));
                Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);
                Crm crm = new Crm();
                crm.makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Opportunities opportunities = new Opportunities(new String(responseBody));
                        String gson = opportunities.list.get(0).toBasicEntity().toGson();
                        entityid = guid;
                        entityLogicalName = "opportunity";
                        activityTitle = "Opportunity";
                        setTitle(activityTitle);

                        basicEntity = new BasicEntity(gson);
                        populateForm(gson, false);
                        getNotes();

                        dialog.dismiss();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(BasicEntityActivity.this, "Failed to get record!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                if (!this.isFinishing()) {
                    try {
                        dialog.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Log.i(TAG, "onCreate Case record link was parsed!");
            }

            Log.i(TAG, "onCreate GUID: " + guid);
            Log.i(TAG, "onCreate Type: " + entityTypeCode);
            // Toast.makeText(context, "Typecode: " + entityTypeCode + "\nGuid: " + guid, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        }
    }

    void showAddEditNote(@Nullable final CrmEntities.Annotations.Annotation clickedNote) {

        final boolean isEditing = clickedNote.entityid != null;
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
                emailsAndAnnoationsAdapter.notifyDataSetChanged();
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
                            clickedNote.entityid = crmEntityResponse.guid;
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
                                emailsAndAnnoationsAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(context, "Note was updated!", Toast.LENGTH_SHORT).show();
                                emailsAndAnnoationsAdapter.updateAnnotationAndReload(clickedNote);
                            }
                        }
                        emailsAndAnnoationsAdapter.notifyDataSetChanged();

                        // Clearing the notetext cache since this operation succeeded.
                        pendingNote = null;
                    }

                    @Override
                    public void onProgress(Crm.AsyncProgress progress) {
                        // clickedNote.filename = "uploading... " + progress.getCompletedMb() + ")";
                        emailsAndAnnoationsAdapter.notifyDataSetChanged();
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
                    emailsAndAnnoationsAdapter.notifyDataSetChanged();
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
                    emailsAndAnnoationsAdapter.mData.add(0, new EmailsOrAnnotations.EmailOrAnnotation(clickedNote));
                    emailsAndAnnoationsAdapter.notifyDataSetChanged();
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
        Requests.Argument argument = new Requests.Argument("query", CrmQueries.Annotations.getAnnotations(entityid));
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

                // Convert CRM server response to an arraylist of Annotations
                final CrmEntities.Annotations annotations = new CrmEntities.Annotations(response);

                // Iterate the arraylist and build the class-wide EmailsOrAnnotations object
                for (CrmEntities.Annotations.Annotation annotation : annotations.list) {
                    emailsOrAnnotations.upsert(annotation);
                }

                // Sort the array
                Collections.sort(emailsOrAnnotations.list, Collections.<EmailsOrAnnotations.EmailOrAnnotation>reverseOrder());

                // attach adapter
                setAdapter();

                // refreshLayout.finishRefresh();
                txtNotesLoading.setVisibility(View.GONE);
                pbNotesLoading.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                try {
                    Log.w(TAG, "onFailure: " + error.getLocalizedMessage());
                    Toast.makeText(getApplicationContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    refreshLayout.finishRefresh();
                    txtNotesLoading.setVisibility(View.GONE);
                    pbNotesLoading.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    void getActivities() {
        String query = CrmQueries.Activities.getCaseActivities(this.entityid);
        Requests.Request request = new Requests.Request(Requests.Request.Function.GET);
        request.arguments.add(new Requests.Argument("query", query));
        // Not implemented yet - copy logic from getEmails() or getNotes() if intend to implement.
    }
    
    void getEmails() {
        String query = CrmQueries.Emails.getEmailsRegarding(this.entityid);
        Requests.Request request = new Requests.Request(Requests.Request.Function.GET);
        request.arguments.add(new Requests.Argument("query", query));
        new Crm().makeCrmRequest(this, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String serverResponse = new String(responseBody);
                try {

                    // Convert server json to an Emails object
                    final CrmEntities.Emails emails = new CrmEntities.Emails(serverResponse);

                    // Iterate the Emails and create an arraylist of EmailOrAnnotation objects
                    for (CrmEntities.Emails.Email email : emails.list) {
                        emailsOrAnnotations.upsert(email);
                    }

                    // Sort the EmailOrAnnotation array by date
                    Collections.sort(emailsOrAnnotations.list, Collections.<EmailsOrAnnotations.EmailOrAnnotation>reverseOrder());

                    // Attach the adapter
                    setAdapter();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "onSuccess ");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(BasicEntityActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Attaches the adapter to the recyclerview and configures onClick, onLongClick etc.
     */
    void setAdapter() {
        emailsAndAnnoationsAdapter = new EmailsAndAnnotationsAdapter(context, emailsOrAnnotations);
        notesListView.setLayoutManager(new LinearLayoutManager(context));
        notesListView.setAdapter(emailsAndAnnoationsAdapter);
                /*notesListView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                        DividerItemDecoration.VERTICAL));*/
        emailsAndAnnoationsAdapter.setClickListener(new EmailsAndAnnotationsAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                EmailsOrAnnotations.EmailOrAnnotation item = emailsOrAnnotations.list.get(position);
                if (item.isAnnotation()) {
                    showNoteOptions(item.annotation);
                } else if (item.isEmail()) {
                    Intent intent = new Intent(context, ViewEmailActivity.class);
                    intent.putExtra(ViewEmailActivity.EMAIL, emailsAndAnnoationsAdapter.mData.get(position).email);
                    startActivity(intent);
                }
            }
        });

        emailsAndAnnoationsAdapter.setOnLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    TextView txtView = view.findViewById(R.id.txt_NoteBody);
                    txtView.setTextIsSelectable(!txtView.isTextSelectable());
                    if (txtView.isTextSelectable()) {
                        Toast.makeText(context, "Long press again to enable text copying.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
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
                emailsAndAnnoationsAdapter.notifyDataSetChanged();
                dialogNoteOptions.dismiss();

                clickedNote.delete(context, new MyInterfaces.YesNoResult() {
                    @Override
                    public void onYes(@Nullable Object object) {
                        Toast.makeText(context, "Note was deleted.", Toast.LENGTH_SHORT).show();
                        emailsAndAnnoationsAdapter.removeAnnotationAndReload(clickedNote);
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
                    final MyProgressDialog getNoteProgress = new MyProgressDialog(context
                            , "Retrieving attachment...");
                    getNoteProgress.show();
                    clickedNote.inUse = true;
                    emailsAndAnnoationsAdapter.notifyDataSetChanged();
                    CrmEntities.Annotations.getAnnotationFromCrm(clickedNote.entityid, true
                            , new MyInterfaces.CrmRequestListener() {
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
                            emailsAndAnnoationsAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onProgress(Crm.AsyncProgress progress) {

                            if (progress.getCompletedKb() % 2 == 1) {
                                getNoteProgress.setTitleText("Retrieving (" + progress.getCompletedMb() + " MB)");
                            }
                        }

                        @Override
                        public void onFail(String error) {
                            Toast.makeText(context, "Failed to retrieve note!\nError: " + error, Toast.LENGTH_SHORT).show();
                            getNoteProgress.dismiss();
                            clickedNote.inUse = false;
                            emailsAndAnnoationsAdapter.notifyDataSetChanged();
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
                    emailsAndAnnoationsAdapter.notifyDataSetChanged();
                    CrmEntities.Annotations.getAnnotationFromCrm(clickedNote.entityid, true, new MyInterfaces.CrmRequestListener() {
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
                                Helpers.Files.shareFileProperly(context, attachment);
                            }
                            clickedNote.inUse = false;
                            emailsAndAnnoationsAdapter.notifyDataSetChanged();
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
                            emailsAndAnnoationsAdapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    File attachment = Helpers.Files.base64Decode(clickedNote.documentBody,
                            new File(Helpers.Files.AttachmentTempFiles.getDirectory() + File.separator +
                                    clickedNote.filename));
                    Helpers.Files.shareFileProperly(context, attachment);
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
                emailsAndAnnoationsAdapter.notifyDataSetChanged();
                Toast.makeText(context, "Updating server...", Toast.LENGTH_SHORT).show();
                clickedNote.inUse = true;
                emailsAndAnnoationsAdapter.notifyDataSetChanged();
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
                        emailsAndAnnoationsAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onNo(@Nullable Object object) {
                        Log.i(TAG, "onNo ");
                        Toast.makeText(context, "Failed to remove attachment\nError: " + object.toString(), Toast.LENGTH_SHORT).show();
                        clickedNote.inUse = false;
                        emailsAndAnnoationsAdapter.notifyDataSetChanged();
                        getNotes();
                    }
                });
                clickedNote.isDocument = true;
                clickedNote.filename = "removing attachment...";
                clickedNote.filesize = 0;
                clickedNote.documentBody = "";
                emailsAndAnnoationsAdapter.notifyDataSetChanged();
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
                emailsAndAnnoationsAdapter.notifyDataSetChanged();
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



































































