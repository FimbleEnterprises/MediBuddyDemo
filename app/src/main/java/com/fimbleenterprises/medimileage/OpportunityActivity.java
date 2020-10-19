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

import org.joda.time.DateTime;

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
    }

    void showAddEditNote(@Nullable final Annotation clickedNote) {

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

                final MyProgressDialog addEditNoteProgressDialog = new MyProgressDialog(context, "Working...");
                addEditNoteProgressDialog.show();

                clickedNote.submit(context, new MyInterfaces.YesNoResult() {
                    @Override
                    public void onYes(@Nullable Object object) {
                        Toast.makeText(context, "Note was added/edited!", Toast.LENGTH_SHORT).show();
                        addEditNoteProgressDialog.dismiss();
                        // Create result example (note the quotes): "1cd8d874-3412-eb11-810f-005056a36b9b"
                        // Update result example: {"WasSuccessful":true,"ResponseMessage":"Existing record was updated!","Guid":"00000000-0000-0000-0000-000000000000","WasCreated":false}

                        // Try to create an UpdateResponse object with the returned result.  If it
                        // succeeds then we know that it was an update operation that was executed.
                        // If it fails then it was almost certainly a successful create operation
                        // (create returns just the GUID of the new note)
                        CrmEntities.UpdateResponse updateResponse = new CrmEntities.UpdateResponse(object.toString());
                        if (updateResponse.wasSuccessful) {
                            Log.i(TAG, "onYes Note was updated!");
                            if (updateResponse.wasCreated) {
                                Toast.makeText(context, "Note was created!", Toast.LENGTH_SHORT).show();
                                clickedNote.annotationid = updateResponse.guid;
                                clickedNote.createdon = DateTime.now();
                                clickedNote.modifiedon = DateTime.now();
                                clickedNote.createdByValue = MediUser.getMe().systemuserid;
                                clickedNote.modifiedByValue = MediUser.getMe().systemuserid;
                                clickedNote.modifedByName = MediUser.getMe().fullname;
                                clickedNote.createdByName = MediUser.getMe().fullname;
                                adapterNotes.mData.add(0, clickedNote);
                                adapterNotes.notifyDataSetChanged();
                            } else {
                                Toast.makeText(context, "Note was updated!", Toast.LENGTH_SHORT).show();
                                adapterNotes.updateAnnotationAndReload(clickedNote);
                            }
                        }
                    }

                    @Override
                    public void onNo(@Nullable Object object) {
                        Toast.makeText(context, "Failed to add/edit note!\n\n" + object.toString(), Toast.LENGTH_SHORT).show();
                        addEditNoteProgressDialog.dismiss();
                    }
                });
            }
        });
        dialog.show();
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
                        showNoteOptions(note);
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
        final Dialog dialog = new Dialog(context);
        final Context c = context;
        dialog.setContentView(R.layout.dialog_note_options);
        Button btnEdit = dialog.findViewById(R.id.btn_edit_note);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddEditNote(clickedNote);
                dialog.dismiss();
            }
        });
        Button btnDelete = dialog.findViewById(R.id.btn_delete_note);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final MyProgressDialog deleteProgressDialog = new MyProgressDialog(context, "Deleting note...");
                deleteProgressDialog.show();
                clickedNote.delete(context, new MyInterfaces.YesNoResult() {
                    @Override
                    public void onYes(@Nullable Object object) {
                        Toast.makeText(context, "Note was deleted.", Toast.LENGTH_SHORT).show();
                        deleteProgressDialog.dismiss();
                        adapterNotes.removeAnnotationAndReload(clickedNote);
                    }

                    @Override
                    public void onNo(@Nullable Object object) {
                        Toast.makeText(context, "Failed to delete note!\n" + object.toString(), Toast.LENGTH_SHORT).show();
                        deleteProgressDialog.dismiss();
                    }
                });
                dialog.dismiss();
            }
        });

        dialog.setTitle("");
        dialog.setCancelable(true);
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