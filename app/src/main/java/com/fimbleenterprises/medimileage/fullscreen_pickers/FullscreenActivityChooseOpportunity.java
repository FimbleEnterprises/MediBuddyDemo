package com.fimbleenterprises.medimileage.fullscreen_pickers;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
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
import android.widget.TextView;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.adapters.BasicObjectRecyclerAdapter;
import com.fimbleenterprises.medimileage.objects_and_containers.BasicObjects;
import com.fimbleenterprises.medimileage.Crm;
import com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities;
import com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities.Opportunities.Opportunity;
import com.fimbleenterprises.medimileage.CustomTypefaceSpan;
import com.fimbleenterprises.medimileage.objects_and_containers.FullTrip;
import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.objects_and_containers.MediUser;
import com.fimbleenterprises.medimileage.MyApp;
import com.fimbleenterprises.medimileage.services.MyAttachmentUploadService;
import com.fimbleenterprises.medimileage.MyInterfaces;
import com.fimbleenterprises.medimileage.dialogs.MyProgressDialog;
import com.fimbleenterprises.medimileage.MyPreferencesHelper;
import com.fimbleenterprises.medimileage.activities.OpportunityActivity;
import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.objects_and_containers.Territory;
import com.fimbleenterprises.medimileage.objects_and_containers.TripEntry;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    ArrayList<BasicObjects.BasicObject> objects = new ArrayList<>();
    BasicObjectRecyclerAdapter adapter;
    public static final int REQUESTCODE = 011;
    public static final String FROM_MAIN_NAV_DRAWER = "FROM_MAIN_NAV_DRAWER";
    public static final String OPPORTUNITY_RESULT = "OPPORTUNITY_RESULT";
    public static final String FULLTRIP = "FULLTRIP";
    FullTrip fulltrip;
    TextView txtAbout;
    RefreshLayout refreshLayout;
    MyPreferencesHelper options;
    String baseMsg;
    String pendingNotetext;
    boolean isLaunchedFromExternalApp = false;
    boolean wasLauchedFromMainNavDrawer = false;
    Territory currentTerritory;
    CrmEntities.Opportunities opportunities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        objects = new ArrayList<>();
        options = new MyPreferencesHelper(context);
        currentTerritory = MediUser.getMe().getTerritory();
        opportunities = options.getSavedOpportunities();
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
                getOpportunities(true);
            }
        });

        Intent receivedIntent = getIntent();
        String receivedAction = receivedIntent.getAction();

        if (receivedIntent != null) {
            isLaunchedFromExternalApp = (receivedAction != null && receivedAction.equals(Intent.ACTION_SEND));
            fulltrip = receivedIntent.getParcelableExtra(FULLTRIP);
            wasLauchedFromMainNavDrawer = receivedAction.equals(FROM_MAIN_NAV_DRAWER);
            this.setTitle("Choose opportunity");
            getOpportunities(true);
        } else {
            finish();
        }


        this.setTitle("Choose opportunity");

        // Create the navigation up button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Helpers.Views.MySwipeHandler mySwipeHandler = new Helpers.Views.MySwipeHandler(new Helpers.Views.MySwipeHandler.MySwipeListener() {
            @Override
            public void onSwipeLeft() {

            }

            @Override
            public void onSwipeRight() {
                onBackPressed();
            }
        });
        mySwipeHandler.addView(listView);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data.getParcelableExtra(FullscreenActivityChooseTerritory.TERRITORY_RESULT) != null) {
            currentTerritory = data.getParcelableExtra(FullscreenActivityChooseTerritory.TERRITORY_RESULT);
            getOpportunities(false);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_opportunities, menu);

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

            case R.id.action_choose_territory :
                Intent intent = new Intent(context, FullscreenActivityChooseTerritory.class);
                intent.putExtra(FullscreenActivityChooseTerritory.CURRENT_TERRITORY, currentTerritory);
                startActivityForResult(intent, 11);
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
                String sharedText = receiverdIntent.getStringExtra(Intent.EXTRA_TEXT);

                // Build an annotation object to pass to the service
                final CrmEntities.Annotations.Annotation annotation = new CrmEntities.Annotations.Annotation();
                // Toast.makeText(context, "Created!  Adding attachment...", Toast.LENGTH_SHORT).show();
                annotation.objectid = opportunity.entityid;
                annotation.subject = "Shared from MileBuddy";
                annotation.notetext = "\n\nShared text:\n" + sharedText;
                annotation.isDocument = false;

                // If we encode the file here it will be too large to be an extra (parcelled)
                // and will fail with a TransactionTooLarge exception.  We need to pass a reference
                // to the file instead and have the receiver encode it on their end.
                // annotation1.documentBody = Helpers.Files.base64Encode(file.getPath());
                Intent intent = new Intent(context, MyAttachmentUploadService.class);
                intent.setAction(MyAttachmentUploadService.UPLOAD_NEW_ATTACHMENT);
                intent.putExtra(MyAttachmentUploadService.NEW_ATTACHMENT, annotation);
                intent.putExtra(MyAttachmentUploadService.IS_TEXT_ONLY, true);
                intent.setAction(MyAttachmentUploadService.UPLOAD_NEW_ATTACHMENT);
                intent.putExtra(OpportunityActivity.OPPORTUNITY_TAG, opportunity);
                startService(intent);
                finishAndRemoveTask();
            } else {

                try {
                    // Prepare the selected file for attachment
                    Uri receiveUri = (Uri) receiverdIntent.getParcelableExtra(Intent.EXTRA_STREAM);
                    String parsedFilename = Helpers.Files.parseFileNameFromPath(receiveUri.getPath());
                    final File file = new File(Helpers.Files.AttachmentTempFiles.getDirectory(), parsedFilename);
                    final InputStream in = getContentResolver().openInputStream(receiveUri);
                    OutputStream out = new FileOutputStream(file);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.close();
                    in.close();
                    final String mimetype = Helpers.Files.getMimetype(this, receiveUri);

                    // Build an annotation object to pass to the service
                    final CrmEntities.Annotations.Annotation annotation = new CrmEntities.Annotations.Annotation();
                    // Toast.makeText(context, "Created!  Adding attachment...", Toast.LENGTH_SHORT).show();
                    annotation.objectid = opportunity.entityid;
                    annotation.subject = "Shared from MileBuddy";
                    annotation.notetext = "I added this note from MileBuddy!";
                    annotation.isDocument = true;
                    annotation.mimetype = mimetype;
                    // annotation.objectEntityName = "opportunity";
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

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else if (receivedAction.equals(Intent.ACTION_MAIN)) {
            return null;
        }

        return null;
    }

    ArrayList<BasicObjects.BasicObject> buildOpportunityListBasedOnTrip() {
        if (objects == null) {
            objects = new ArrayList<>();
        }
        objects.clear();

        MyPreferencesHelper options = new MyPreferencesHelper(MyApp.getAppContext());
        CrmEntities.CrmAddresses accountAddresses = options.getAllSavedCrmAddresses();

        ArrayList<Opportunity> nearbyOpportunities = new ArrayList<>();
        ArrayList<Opportunity> notNearbyOpportunities = new ArrayList<>();

        if (    // Validate parameters before evaluating them
                fulltrip.getTripEntries() == null ||
                        fulltrip.getTripEntries().size() < 1 ||
                        accountAddresses == null ||
                        accountAddresses.list.size() < 1 ||
                        opportunities == null ||
                        opportunities.list.size() < 1
        ) { return objects; }

        TripEntry startEntry = fulltrip.tripEntries.get(0);
        TripEntry endEntry = fulltrip.tripEntries.get(fulltrip.tripEntries.size() - 1);

        for (Opportunity opp : opportunities.list) {
            CrmEntities.CrmAddresses.CrmAddress oppAddy = opp.tryGetCrmAddress();
            if (oppAddy != null) {
                if (oppAddy.isNearby(startEntry)) {
                    nearbyOpportunities.add(opp);
                } else {
                    notNearbyOpportunities.add(opp);
                }
            }
        }

        objects.add(new BasicObjects.BasicObject("Nearby"));
        // If there are no nearby opportunities then create an empty one
        if (nearbyOpportunities.size() == 0) {
            BasicObjects.BasicObject emptyObject = new BasicObjects.BasicObject();
            emptyObject.title = "No opportunities";
            emptyObject.isEmpty = true;
            emptyObject.isHeader = false;
            objects.add(emptyObject);
        }
        for (Opportunity nearbyOpp : nearbyOpportunities) {
            BasicObjects.BasicObject obj = new BasicObjects.BasicObject(nearbyOpp.name, nearbyOpp.getPrettyEstimatedValue(), nearbyOpp);
            obj.middleText = nearbyOpp.accountname;
            obj.topRightText = nearbyOpp.probabilityPretty;
            obj.iconResource = R.drawable.opportunity_icon;
            objects.add(obj);
        }
        objects.add(new BasicObjects.BasicObject("Other"));
        for (Opportunity otherOpp : notNearbyOpportunities) {
            BasicObjects.BasicObject obj = new BasicObjects.BasicObject(otherOpp.name, otherOpp.getPrettyEstimatedValue(), otherOpp);
            obj.middleText = otherOpp.accountname;
            obj.topRightText = otherOpp.probabilityPretty;
            obj.iconResource = R.drawable.opportunity_icon;
            objects.add(obj);
        }

        return objects;
    }

    ArrayList<BasicObjects.BasicObject> buildOpportunityListBasedOnExternalApp() {
        if (objects == null) {
            objects = new ArrayList<>();
        }
        objects.clear();

        MyPreferencesHelper options = new MyPreferencesHelper(MyApp.getAppContext());
        CrmEntities.Opportunities savedOpportunities = opportunities;

        for (Opportunity opp : savedOpportunities.list) {
            BasicObjects.BasicObject obj = new BasicObjects.BasicObject(opp.name, opp.getPrettyEstimatedValue(), opp);
            obj.middleText = opp.accountname;
            obj.topRightText = opp.probabilityPretty;
            obj.iconResource = R.drawable.opportunity_icon;
            objects.add(obj);
        }

        return objects;
    }

    @SuppressLint("StaticFieldLeak")
    void getOpportunities(boolean useCached) {

        if (useCached) {
            if (!options.hasSavedOpportunities()) {
                Toast.makeText(context, "Retrieving your opportunities - try again in a minute or so.", Toast.LENGTH_LONG).show();
                CrmEntities.Opportunities.retrieveAndSaveOpportunities();
                finish();
                return;
            }
        } else {
            final MyProgressDialog myProgressDialog = new MyProgressDialog(context, getString(R.string.retrieving_opportunities));
            myProgressDialog.show();
            CrmEntities.Opportunities.retrieveOpportunities(currentTerritory.territoryid, new MyInterfaces.GetOpportunitiesListener() {
                @Override
                public void onSuccess(CrmEntities.Opportunities crmOpportunities) {
                    opportunities = crmOpportunities;
                    populateOpportunities();
                    myProgressDialog.dismiss();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(context, "Failed to get opportunities!\n" + error, Toast.LENGTH_LONG).show();
                }
            });
        }

        buildOpportunityList();

    }

    void buildOpportunityList() {
        final MyProgressDialog dialog = new MyProgressDialog(context , baseMsg);

        AsyncTask<String, String, String> task = new AsyncTask<String, String, String>() {
            @Override
            protected void onPreExecute() { super.onPreExecute(); dialog.show(); }

            @Override
            protected String doInBackground(String... strings) {

                if (isLaunchedFromExternalApp) {
                    objects = buildOpportunityListBasedOnExternalApp();
                } else if (wasLauchedFromMainNavDrawer) {
                    objects = buildOpportunityListBasedOnExternalApp();
                } else {
                    objects = buildOpportunityListBasedOnTrip();
                }

                try {
                    for (BasicObjects.BasicObject o : objects) {
                        // Crashed here a couple of times while working on the background uploader.  Will put a STOP here to try and debug it next time it happens.
                        o.iconResource = (o.isHeader ? -1 : o.iconResource);
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
            if (!wasLauchedFromMainNavDrawer) {
                if (options.isExplicitMode()) {
                    txtMain.setText(getString(R.string.opportunity_picker_main_text_external_launch_explicit));
                } else {
                    txtMain.setText(getString(R.string.opportunity_picker_main_text_external_launch));
                }
            }
        } else if (wasLauchedFromMainNavDrawer) {
            txtMain.setText(getString(R.string.opportunities_about_this_list_from_main_nav_drawer));
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
        txtStatus.setText(opportunity.statuscodeFormatted);
        txtDealStatus.setText(opportunity.statuscodeFormatted);
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
        CrmEntities.Annotations.showAddNoteDialog(context, opportunity.entityid, new MyInterfaces.CrmRequestListener() {
            @Override
            public void onComplete(Object result) {
                Log.i(TAG, "onComplete ");
                final Helpers.Notifications notifications = new Helpers.Notifications(context);
                notifications.create("Opportunity note created",
                        "Your note was added to the opportunity!", false);
                notifications.show();
                notifications.setAutoCancel(6000);
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