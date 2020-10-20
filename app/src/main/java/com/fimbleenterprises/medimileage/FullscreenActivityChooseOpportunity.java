package com.fimbleenterprises.medimileage;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.CrmEntities.Opportunities.Opportunity;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.joda.time.DateTime;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivityChooseOpportunity extends AppCompatActivity {

    private static final String TAG = "FullscreenActivityChooseTerritory";

    Context context;
    RecyclerView listView;
    ArrayList<BasicObject> objects = new ArrayList<>();
    BasicObjectRecyclerAdapter adapter;
    public static final int REQUESTCODE = 011;
    public static final String OPPORTUNITY_RESULT = "OPPORTUNITY_RESULT";
    public static final String FULLTRIP = "FULLTRIP";
    FullTrip fulltrip;
    TextView txtAbout;
    RefreshLayout refreshLayout;
    MySettingsHelper options;
    String baseMsg;

    String pendingNotetext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        options = new MySettingsHelper(context);
        setContentView(R.layout.activity_fullscreen_choose_opportunity);
        listView = findViewById(R.id.rvBasicObjects);

        txtAbout = findViewById(R.id.txtAboutActivity);
        txtAbout.setText(options.isExplicitMode() ? R.string.opportunities_about_this_list_explicit :
                R.string.opportunities_about_this_list);

        baseMsg = "" +
                  "Checking for opportunities within (roughly) " +
                  options.getDistanceThresholdInMiles() + " miles...";

        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                getOpportunities();
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            fulltrip = intent.getParcelableExtra(FULLTRIP);
            this.setTitle("Choose opportunity");
            getOpportunities();
        } else {
            finish();
        }
    }

    @SuppressLint("StaticFieldLeak")
    void getOpportunities() {

        final MyProgressDialog dialog = new MyProgressDialog(context , baseMsg);

        AsyncTask<String, String, String> task = new AsyncTask<String, String, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog.show();
            }

            @Override
            protected String doInBackground(String... strings) {
                objects.clear();
                ArrayList<Opportunity> opportunities = TripAssociationManager.getNearbyOpportunities(fulltrip);
                for (Opportunity opportunity : opportunities) {
                    BasicObject object = new BasicObject(opportunity.name, opportunity.accountname, opportunity);
                    object.iconResource = R.drawable.about;
                    objects.add(object);
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                populateOpportunities();
                refreshLayout.finishRefresh();
                dialog.dismiss();
            }
        };

        // The lack of this check has burned me before.  It's verbose and not always needed for reasons
        // unknown but I'd leave it!
        if(Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }

    }

    void populateOpportunities() {

        adapter = new BasicObjectRecyclerAdapter(this, objects);
        listView.setAdapter(adapter);
        listView.addItemDecoration(new DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL));
        listView.setLayoutManager(new LinearLayoutManager(context));
        adapter.setClickListener(new BasicObjectRecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.i(TAG, "onItemClick Position: " + position);
                Opportunity opportunity = (Opportunity) objects.get(position).object;
                showOppOptions(opportunity);
            }
        });

    }

    void showAddQuickNote(Opportunity opportunity) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_note);
        final EditText noteBody = dialog.findViewById(R.id.body_text);
        dialog.setTitle("Note");
        dialog.setCancelable(true);
        Button btnSubmit = dialog.findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String notetext = noteBody.getText().toString();
                dialog.dismiss();
                final MyProgressDialog addNoteProgressDialog = new MyProgressDialog(context, "Creating note...");
                addNoteProgressDialog.show();

            }
        });
    }

    void showOppOptions(final Opportunity opportunity) {

        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_opportunity_options);

        // Fields
        TextView txtAccount;
        TextView txtTopic;
        TextView txtStatus;
        TextView txtDealStatus;
        TextView txtDealType;
        TextView txtCloseProb;
        TextView txtBackground;

        txtAccount = dialog.findViewById(R.id.textView_OppAccount);
        txtTopic = dialog.findViewById(R.id.textView_OppTopic);
        txtStatus = dialog.findViewById(R.id.textView_OppStatus);
        txtDealStatus = dialog.findViewById(R.id.textView_OppDealStatus);
        txtDealType = dialog.findViewById(R.id.textView_OppDealType);
        txtCloseProb = dialog.findViewById(R.id.textView_OppCloseProb);
        txtBackground = dialog.findViewById(R.id.textView_OppBackground);
        
        txtAccount.setText(opportunity.accountname);
        txtTopic.setText(opportunity.name);
        txtStatus.setText(opportunity.status);
        txtDealStatus.setText(opportunity.dealStatus);
        txtDealType.setText(opportunity.dealTypePretty);
        txtCloseProb.setText(opportunity.probabilityPretty);

        String bgTruncated = "";
        if (opportunity.currentSituation != null && opportunity.currentSituation.length() > 125) {
            bgTruncated = opportunity.currentSituation.substring(0, 125) + "...\n";
        } else {
            bgTruncated = opportunity.currentSituation;
        }

        txtBackground.setText(bgTruncated);
        
        Button btnQuickNote;
        btnQuickNote = dialog.findViewById(R.id.btn_add_quick_note);
        btnQuickNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                showAddNoteDialog(opportunity);
            }
        });

        Button btnViewOpportunity;
        btnViewOpportunity = dialog.findViewById(R.id.btn_view_opportunity);
        btnViewOpportunity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), OpportunityActivity.class);
                intent.putExtra(OpportunityActivity.OPPORTUNITY_TAG, opportunity);
                startActivity(intent);

                Intent resultIntent = new Intent(OPPORTUNITY_RESULT);
                resultIntent.putExtra(OPPORTUNITY_RESULT, opportunity);
                setResult(RESULT_OK, resultIntent);
                dialog.dismiss();
            }
        });

        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    void showAddNoteDialog(final Opportunity opportunity) {

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
                newNote.objectid = opportunity.opportunityid + "fuckyou";
                newNote.notetext = noteBody.getText().toString();

                // Cache the pending note text in case the operation fails
                pendingNotetext = noteBody.getText().toString();

                final MyProgressDialog addEditNoteProgressDialog = new MyProgressDialog(context, "Working...");
                addEditNoteProgressDialog.show();

                newNote.submit(context, new MyInterfaces.YesNoResult() {
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
                            Toast.makeText(context, "Note was created.", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }

                        // Clear the cached note text
                        pendingNotetext = null;
                        if (pendingNotetext == null) {
                            Log.i(TAG, "onYes Pending note cache was null'd");
                        }

                    }

                    @Override
                    public void onNo(@Nullable Object object) {
                        Toast.makeText(context, "Failed to add/edit note!\n\nError: " + object.toString(), Toast.LENGTH_SHORT).show();
                        addEditNoteProgressDialog.dismiss();
                        dialog.show();
                    }
                });
            }
        });
        dialog.show();

        // Check if there is a cached note from a failed note add
        if (pendingNotetext != null) {
            noteBody.setText(pendingNotetext);
            Log.i(TAG, "showAddNoteDialog Found a cached note that must have been a failed note add.  Gon' use it!");
        }

    }

}