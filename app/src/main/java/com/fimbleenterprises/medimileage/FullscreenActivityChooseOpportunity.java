package com.fimbleenterprises.medimileage;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.CrmEntities.Opportunities.Opportunity;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
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
    boolean isLaunchedFromExternalApp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;

        objects = new ArrayList<>();

        options = new MySettingsHelper(context);
        setContentView(R.layout.activity_fullscreen_choose_opportunity);
        listView = findViewById(R.id.rvBasicObjects);

        txtAbout = findViewById(R.id.txtDescription);
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

        Intent receivedIntent = getIntent();
        String receivedAction = receivedIntent.getAction();

        if (receivedIntent != null) {
            isLaunchedFromExternalApp = (receivedAction != null && receivedAction.equals(Intent.ACTION_SEND));
            fulltrip = receivedIntent.getParcelableExtra(FULLTRIP);
            this.setTitle("Choose opportunity");
            getOpportunities();
        } else {
            finish();
        }


        this.setTitle("Choose opportunity");

        // Create the navigation up button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private CrmEntities.Annotations.Annotation buildAnnotation(Opportunity opportunity) {
        Intent receiverdIntent = getIntent();
        String receivedAction = receiverdIntent.getAction();
        String receivedType = receiverdIntent.getType();

        if (receivedAction.equals(Intent.ACTION_SEND)) {

            // check mime type
            if (receivedType.startsWith("text/")) {

                return null;
            }

            else if (receivedType.startsWith("image/")) {
                try {
                    // Prepare the selected file for attachment
                    Uri receiveUri = (Uri) receiverdIntent.getParcelableExtra(Intent.EXTRA_STREAM);
                    String parsedFilename = Helpers.Files.parseFileNameFromPath(receiveUri.getPath());
                    final File file = new File(Helpers.Files.AttachmentTempFiles.getDirectory(), parsedFilename);
                    final InputStream in =  getContentResolver().openInputStream(receiveUri);
                    OutputStream out = new FileOutputStream(file);
                    byte[] buf = new byte[1024];
                    int len;
                    while((len=in.read(buf))>0){
                        out.write(buf,0,len);
                    }
                    out.close();
                    in.close();
                    final String mimetype = Helpers.Files.getMimeType(this, receiveUri);

                    // Build an annotation object to pass to the service
                    final CrmEntities.Annotations.Annotation annotation = new CrmEntities.Annotations.Annotation();
                    // Toast.makeText(context, "Created!  Adding attachment...", Toast.LENGTH_SHORT).show();
                    annotation.objectid = opportunity.opportunityid;
                    annotation.subject = "Shared from MileBuddy";
                    annotation.notetext = "I added this note from MileBuddy!";
                    annotation.isDocument = true;
                    annotation.mimetype = mimetype;
                    annotation.filename = file.getName();
                    // If we encode the file here it will be too large to be an extra (parcelled)
                    // and will fail with a TransactionTooLarge exception.  We need to pass a reference
                    // to the file instead and have the receiver encode it on their end.
                    // annotation1.documentBody = Helpers.Files.base64Encode(file.getPath());
                    Intent intent = new Intent(context, MyAttachmentUploadService.class);
                    intent.setAction(MyAttachmentUploadService.UPLOAD_NEW_ATTACHMENT);
                    intent.putExtra(MyAttachmentUploadService.NEW_ATTACHMENT, annotation);
                    intent.putExtra(MyAttachmentUploadService.ATTACHMENT_FILE_PATH, file.getPath());
                    intent.setAction(MyAttachmentUploadService.UPLOAD_NEW_ATTACHMENT);
                    intent.putExtra(OpportunityActivity.OPPORTUNITY_TAG, opportunity);
                    startService(intent);

                    Toast.makeText(context, "Attachment is being uploaded...", Toast.LENGTH_SHORT).show();
                    finishAndRemoveTask();

                    return annotation;

                }  catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else if (receivedAction.equals(Intent.ACTION_MAIN)) {
            return null;
        }

        return null;
    }

    ArrayList<BasicObject> buildOpportunityListBasedOnTrip() {
        if (objects == null) {
            objects = new ArrayList<>();
        }
        objects.clear();

        MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());
        CrmEntities.CrmAddresses accountAddresses = options.getAllSavedCrmAddresses();
        CrmEntities.Opportunities savedOpportunities = options.getSavedOpportunities();

        ArrayList<Opportunity> nearbyOpportunities = new ArrayList<>();
        ArrayList<Opportunity> notNearbyOpportunities = new ArrayList<>();
        ArrayList<BasicObject> fullList = new ArrayList<>();

        if (    // Validate parameters before evaluating them
                fulltrip.getTripEntries() == null ||
                        fulltrip.getTripEntries().size() < 1 ||
                        accountAddresses == null ||
                        accountAddresses.list.size() < 1 ||
                        savedOpportunities == null ||
                        savedOpportunities.list.size() < 1
        ) { return objects; }

        TripEntry startEntry = fulltrip.tripEntries.get(0);
        TripEntry endEntry = fulltrip.tripEntries.get(fulltrip.tripEntries.size() - 1);

        for (Opportunity opp : savedOpportunities.list) {
            CrmEntities.CrmAddresses.CrmAddress oppAddy = opp.tryGetCrmAddress();
            if (oppAddy != null) {
                if (oppAddy.isNearby(startEntry)) {
                    nearbyOpportunities.add(opp);
                } else {
                    notNearbyOpportunities.add(opp);
                }
            }
        }

        objects.add(new BasicObject("Nearby"));
        // If there are no nearby opportunities then create an empty one
        if (nearbyOpportunities.size() == 0) {
            BasicObject emptyObject = new BasicObject();
            emptyObject.title = "No opportunities";
            emptyObject.isEmpty = true;
            emptyObject.isHeader = false;
            objects.add(emptyObject);
        }
        for (Opportunity nearbyOpp : nearbyOpportunities) {
            BasicObject obj = new BasicObject(nearbyOpp.name, nearbyOpp.accountname, nearbyOpp);
            objects.add(obj);
        }
        objects.add(new BasicObject("Other"));
        for (Opportunity otherOpp : notNearbyOpportunities) {
            BasicObject obj = new BasicObject(otherOpp.name, otherOpp.accountname, otherOpp);
            objects.add(obj);
        }



        return objects;
    }

    ArrayList<BasicObject> buildOpportunityListBasedOnExternalApp() {
        if (objects == null) {
            objects = new ArrayList<>();
        }
        objects.clear();

        MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());
        CrmEntities.CrmAddresses accountAddresses = options.getAllSavedCrmAddresses();
        CrmEntities.Opportunities savedOpportunities = options.getSavedOpportunities();

        for (Opportunity opp : savedOpportunities.list) {
            BasicObject obj = new BasicObject(opp.name, opp.accountname, opp);
            objects.add(obj);
        }

        return objects;
    }

    @SuppressLint("StaticFieldLeak")
    void getOpportunities() {

        if (!options.hasSavedOpportunities()) {
            Toast.makeText(context, "Retrieving your opportunities - try again in a minute or so.", Toast.LENGTH_LONG).show();
            CrmEntities.Opportunities.retrieveAndSaveOpportunities();
            finish();
            return;
        }

        final MyProgressDialog dialog = new MyProgressDialog(context , baseMsg);

        AsyncTask<String, String, String> task = new AsyncTask<String, String, String>() {
            @Override
            protected void onPreExecute() { super.onPreExecute(); dialog.show(); }

            @Override
            protected String doInBackground(String... strings) {

                if (isLaunchedFromExternalApp) {
                    objects = buildOpportunityListBasedOnExternalApp();
                } else {
                    objects = buildOpportunityListBasedOnTrip();
                }

                try {
                    for (BasicObject o : objects) {
                        // Crashed here a couple of times while working on the background uploader.  Will put a STOP here to try and debug it next time it happens.
                        o.iconResource = (o.isHeader ? -1 : R.drawable.about_icon_black_48x48);
                    }
                } catch (Exception e) { e.printStackTrace(); }
                return "";
            }

            @Override
            protected void onPostExecute(String s) {

                if (s==null) {
                    Toast.makeText(context, "No opportunities found.", Toast.LENGTH_SHORT).show();
                    finish();
                }

                super.onPostExecute(s);

                if (objects.size() == 0) {
                    Toast.makeText(context, "No opportunities found!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    populateOpportunities();
                    refreshLayout.finishRefresh();
                    dialog.dismiss();
                }
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

        TextView txtMain = findViewById(R.id.txtDescription);

        if (isLaunchedFromExternalApp) {
            if (options.isExplicitMode()) {
                txtMain.setText(getString(R.string.opportunity_picker_main_text_external_launch_explicit));
            } else {
                txtMain.setText(getString(R.string.opportunity_picker_main_text_external_launch));
            }
        }

        adapter = new BasicObjectRecyclerAdapter(this, objects);
        listView.setAdapter(adapter);
        listView.addItemDecoration(new DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL));
        listView.setLayoutManager(new LinearLayoutManager(context));
        adapter.setClickListener(new BasicObjectRecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.i(TAG, "onItemClick Position: " + position);
                if (!objects.get(position).isEmpty && !objects.get(position).isHeader) {
                    Opportunity opportunity = (Opportunity) objects.get(position).object;
                    if (!isLaunchedFromExternalApp) {
                        showOppOptions(opportunity);
                    } else {
                        buildAnnotation(opportunity);
                    }
                }
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

        CrmEntities.Annotations.showAddNoteDialog(context, opportunity.opportunityid, new MyInterfaces.CrmRequestListener() {
            @Override
            public void onComplete(Object result) {
                Log.i(TAG, "onComplete ");
            }

            @Override
            public void onProgress(Crm.AsyncProgress progress) {
                Log.i(TAG, "onProgress ");
            }

            @Override
            public void onFail(String error) {
                Log.i(TAG, "onFail ");
            }
        });

    }

}