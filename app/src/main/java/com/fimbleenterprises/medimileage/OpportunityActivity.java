package com.fimbleenterprises.medimileage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import cz.msebera.android.httpclient.Header;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.CrmEntities.Annotations.Annotation;
import com.fimbleenterprises.medimileage.CrmEntities.Opportunities.Opportunity;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class OpportunityActivity extends AppCompatActivity {
    private static final String TAG = "OpportunityActivity";
    public static final String OPPORTUNITY_TAG = "OPPORTUNITY_TAG";
    public Opportunity opportunity;
    NonScrollRecyclerView notesListView;
    AnnotationsAdapter adapterNotes;
    Context context;

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
            Toast.makeText(this,  opportunity.name, Toast.LENGTH_SHORT).show();
            populateOppDetails();
            getOpportunityNotes();
        }

    }

    void populateOppDetails() {

        txtAccount = findViewById(R.id.textView_OppAccount);
        txtTopic = findViewById(R.id.textView_OppTopic);
        txtStatus = findViewById(R.id.textView_OppStatus);
        txtDealStatus = findViewById(R.id.textView_OppDealStatus);
        txtDealType = findViewById(R.id.textView_OppDealType);
        txtCloseProb = findViewById(R.id.textView_OppCloseProb);
        txtBackground = findViewById(R.id.textView_OppBackground);
        txtNotesLoading = findViewById(R.id.textViewopportunityNotesLoading);
        btnAddNote = findViewById(R.id.btnAddNote);
        pbNotesLoading = findViewById(R.id.progressBarWorking);
        btnAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddEditNote(NoteAction.CREATE, null);
            }
        });

        txtAccount.setText(opportunity.accountname);
        txtTopic.setText(opportunity.name);
        txtStatus.setText(opportunity.status);
        txtDealStatus.setText(opportunity.dealStatus);
        txtDealType.setText(opportunity.dealTypePretty);
        txtCloseProb.setText(opportunity.probabilityPretty);
        txtBackground.setText(opportunity.currentSituation);
    }

    void showAddEditNote(final NoteAction action, @Nullable final Annotation clickedNote) {

        final Dialog dialog = new Dialog(OpportunityActivity.this);
        dialog.setContentView(R.layout.dialog_note);
        final EditText noteBody = dialog.findViewById(R.id.body_text);
        Button btnSubmit = dialog.findViewById(R.id.button_submit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (action) {
                    case CREATE:
                        String notetext = noteBody.getText().toString();
                        submitNewNote(notetext, new MyInterfaces.YesNoResult() {
                            @Override
                            public void onYes(@Nullable Object object) {
                                dialog.dismiss();
                                getOpportunityNotes();
                                Toast.makeText(context, "Note created!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onNo(@Nullable Object object) {
                                Toast.makeText(context, "Failed to create note!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case EDIT:
                        clickedNote.notetext = noteBody.getText().toString();
                        editExistingNote(clickedNote, new MyInterfaces.YesNoResult() {
                            @Override
                            public void onYes(@Nullable Object object) {
                                Toast.makeText(context, "Note was updated.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                getOpportunityNotes();
                            }

                            @Override
                            public void onNo(@Nullable Object object) {
                                Toast.makeText(context, "Failed to update note\n\n" + object.toString(), Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                }
            }
        });
        dialog.setTitle("Note");
        dialog.setCancelable(true);
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

    void editExistingNote(Annotation note, final MyInterfaces.YesNoResult listener) {

        // Entity values to update
        Containers.EntityContainer container = new Containers.EntityContainer();
        container.entityFields.add(new Containers.EntityField("notetext", note.notetext));

        // Start constructing request object adding arguments based on function to perform
        Requests.Request request = new Requests.Request(Requests.Request.Function.UPDATE);
        request.arguments.add(new Requests.Argument("entityid", note.annotationid));
        request.arguments.add(new Requests.Argument("entityname", "annotation"));
        request.arguments.add(new Requests.Argument("json", container.toJson()));
        request.arguments.add(new Requests.Argument("asuserid", MediUser.getMe().systemuserid));

        Crm crm = new Crm();
        crm.makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                Log.i(TAG, "onSuccess " + response);
                listener.onYes(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                listener.onNo(error);
            }
        });

    }

    void submitNewNote(String noteBody, final MyInterfaces.YesNoResult listener) {
        Containers.Annotation annotation = new Containers.Annotation();
        annotation.subject = "MileBuddy Note";
        annotation.notetext = noteBody;
        annotation.ownerid = MediUser.getMe().systemuserid;
        annotation.isdocument = false;
        annotation.objectidtypecode = "opportunity";
        annotation.objectid = opportunity.opportunityid;

        Requests.Request request = new Requests.Request(Requests.Request.Function.CREATE_NOTE);
        request.arguments.add(new Requests.Argument("noteobject", annotation.toJson()));

        final MyProgressDialog dialog = new MyProgressDialog(this, "Creating note...");
        dialog.show();

        Crm crm = new Crm();
        crm.makeCrmRequest(this, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                Log.i(TAG, "onSuccess " + response);
                dialog.dismiss();
                listener.onYes(null);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.w(TAG, "onFailure: " + error.getLocalizedMessage());
                listener.onNo(error.getLocalizedMessage());
                dialog.dismiss();
                listener.onNo(error.getLocalizedMessage());
            }
        });
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
                notesListView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                        DividerItemDecoration.VERTICAL));
                adapterNotes.setClickListener(new AnnotationsAdapter.ItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Annotation note = annotations.list.get(position);
                        Toast.makeText(context, note.subject, Toast.LENGTH_SHORT).show();
                        showNoteOptions(note);
                    }
                });
                Toast.makeText(OpportunityActivity.this, "Notes: " + annotations.list.size(),
                        Toast.LENGTH_SHORT).show();
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
        final Dialog dialog = new Dialog(context);
        final Context c = context;
        dialog.setContentView(R.layout.dialog_note_options);
        Button btnEdit = dialog.findViewById(R.id.btn_edit_note);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddEditNote(NoteAction.EDIT, clickedNote);
            }
        });
        Button btnDelete = dialog.findViewById(R.id.btn_delete_note);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        dialog.setTitle("");
        dialog.setCancelable(true);
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

        // See if the current user is the author of the clicked note
        btnEdit.setEnabled(clickedNote.createdByValue.equals(MediUser.getMe().systemuserid) ? true : false);
        btnDelete.setEnabled(clickedNote.createdByValue.equals(MediUser.getMe().systemuserid) ? true : false);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }
}