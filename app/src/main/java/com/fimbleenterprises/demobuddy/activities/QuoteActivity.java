package com.fimbleenterprises.demobuddy.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;

import com.fimbleenterprises.demobuddy.Crm;
import com.fimbleenterprises.demobuddy.PdfTools;
import com.fimbleenterprises.demobuddy.Helpers;
import com.fimbleenterprises.demobuddy.adapters.QuoteProductsRecyclerAdapter;
import com.fimbleenterprises.demobuddy.dialogs.MyProgressDialog;
import com.fimbleenterprises.demobuddy.objects_and_containers.CrmEntities;
import com.fimbleenterprises.demobuddy.objects_and_containers.EntityContainers;
import com.fimbleenterprises.demobuddy.objects_and_containers.MediUser;
import com.fimbleenterprises.demobuddy.objects_and_containers.Requests;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cz.msebera.android.httpclient.Header;

import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;
import android.view.View;
import android.widget.Toast;

import com.fimbleenterprises.demobuddy.R;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import static com.fimbleenterprises.demobuddy.objects_and_containers.EntityContainers.*;

public class QuoteActivity extends AppCompatActivity {

    Activity activity;
    Context context;
    RecyclerView recyclerView;
    QuoteProductsRecyclerAdapter adapter;
    ArrayList<CrmEntities.AccountProducts.AccountProduct> products = new ArrayList<>();
    MyProgressDialog mpd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote);
        context = this;
        activity = this;
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerView = findViewById(R.id.recyclerview);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view,
                "Replace with your own action",
                 Snackbar.LENGTH_LONG)
                .setAction("Action", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(QuoteActivity.this, "You clicked the snackbar!", Toast.LENGTH_SHORT).show();
                        makeDummyData();
                        quoteTest();
                    }
                })
                .show();
            }
        });
    }

    void quoteTest() {
//                      if (value.Arguments[0].value != null) quoteid = value.Arguments[0].value.ToString();
//						customerid = value.Arguments[1].value.ToString();
//						customername = value.Arguments[2].value.ToString();
//						asUserid = value.Arguments[3].value.ToString();
//						// The following may be null on arrival so calling .ToString() would throw an exception
//						if (value.Arguments[4].value != null) quoteterms = value.Arguments[4].value.ToString();
//						if (value.Arguments[5].value != null) additionalquoteterms = value.Arguments[5].value.ToString();
//						if (value.Arguments[6].value != null) casenumber = value.Arguments[6].value.ToString();
//						if (value.Arguments[7].value != null) discountamount = value.Arguments[7].value.ToString();
//						if (value.Arguments[8].value != null) opportunityid = value.Arguments[8].value.ToString();
//						if (value.Arguments[9].value != null) caseid = value.Arguments[9].value.ToString();

        mpd = new MyProgressDialog(context, "Creating base quote...");
        mpd.show();

        Crm crm = new Crm();
        Requests.Request request = new Requests.Request(Requests.Request.Function.CREATEQUOTE);
        // request.arguments.add(new Requests.Argument("quoteid", "20205938-de31-ec11-811c-005056a36b9b"));
        request.arguments.add(new Requests.Argument("customerid", "c4de55b6f56ae81180e3005056a36b9b"));
        request.arguments.add(new Requests.Argument("customername", "TESTACCOUNT"));
        request.arguments.add(new Requests.Argument("asuserid", "3ECF2393-C71D-E711-80D2-005056A36B9B"));
        request.arguments.add(new Requests.Argument("quoteterms", null));
        request.arguments.add(new Requests.Argument("additionalquoteterms", null));
        request.arguments.add(new Requests.Argument("casenumber", null));
        request.arguments.add(new Requests.Argument("discountamount", "0"));
        request.arguments.add(new Requests.Argument("opportunityid", null));
        request.arguments.add(new Requests.Argument("caseid", null));
        request.arguments.add(new Requests.Argument("prodjson", doProducts()));
        request.arguments.add(new Requests.Argument("finjson", doFinancialSolutions()));

        crm.makeCrmRequest(this, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                mpd.dismiss();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });

    }

    String doFinancialSolutions() {
        EntityContainers containers = new EntityContainers();
        EntityContainer container = new EntityContainer();

        container.entityFields.add(new EntityField("msus_quoteid","20205938-de31-ec11-811c-005056a36b9b"));
        container.entityFields.add(new EntityField("msus_name","CV000443"));
        container.entityFields.add(new EntityField("msus_product","2d9e0566-1537-e711-80d4-005056a36b9b"));
        container.entityFields.add(new EntityField("msus_pricepermonth","455"));
        containers.entityContainers.add(container);

        return containers.toJson();
    }

    String doProducts() {
        EntityContainers containers = new EntityContainers();
        EntityContainer container = new EntityContainer();

        container.entityFields.add(new EntityField("description","3mm Quick-Fit Probe w/ handle (H - Series)"));
        container.entityFields.add(new EntityField("uomid","36BFB7B9-8F1E-E711-80D2-005056A36B9B"));
        container.entityFields.add(new EntityField("quoteid","20205938-de31-ec11-811c-005056a36b9b"));
        container.entityFields.add(new EntityField("manualdiscountamount","0"));
        container.entityFields.add(new EntityField("extendedamount","2420"));
        container.entityFields.add(new EntityField("priceperunit","2420"));
        container.entityFields.add(new EntityField("productid","9e2e2d72-1537-e711-80d4-005056a36b9b"));
        container.entityFields.add(new EntityField("quantity","1"));
        container.entityFields.add(new EntityField("msus_serial_number",""));
        containers.entityContainers.add(container);

        return containers.toJson();
    }

    void makeDummyData() {

        CrmEntities.AccountProducts.AccountProduct p1 = new CrmEntities.AccountProducts.AccountProduct();
        p1.accountname = "Awesomesausce";
        p1.isCapital = false;
        p1.partNumber = "PS100022";
        products.add(p1);

        adapter = new QuoteProductsRecyclerAdapter(this, products);
        adapter.setClickListener(new QuoteProductsRecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                doHardPdf();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    void doHardPdf() {
        try {
            PdfTools.manipulatePdf();
            /*MediUser me = MediUser.getMe();
            String[] recips = new String[1];
            recips[0] = me.email;
            Helpers.Email.sendEmail(recips, getString(R.string.receipt_mail_body),
                    getString(R.string.receipt_mail_subject_preamble), context, file, false);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void doEasyPdf() {
        // Create a shiny new (but blank) PDF document in memory
        // We want it to optionally be printable, so add PrintAttributes
        // and use a PrintedPdfDocument. Simpler: new PdfDocument().
        PrintAttributes printAttrs = new PrintAttributes.Builder().
                setColorMode(PrintAttributes.COLOR_MODE_COLOR).
                setMediaSize(PrintAttributes.MediaSize.NA_LETTER).
                setResolution(new PrintAttributes.Resolution("zooey", PRINT_SERVICE, 300, 300)).
                setMinMargins(PrintAttributes.Margins.NO_MARGINS).
                build();
        PdfDocument document = new PrintedPdfDocument(context, printAttrs);

        // crate a page description
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 300, 1).create();

        // create a new page from the PageInfo
        PdfDocument.Page page = document.startPage(pageInfo);

        // repaint the user's text into the page
        View content = findViewById(R.id.pdfcontainer);
        content.draw(page.getCanvas());

        // do final processing of the page
        document.finishPage(page);

        // Here you could add more pages in a longer doc app, but you'd have
        // to handle page-breaking yourself in e.g., write your own word processor...

        // Now write the PDF document to a file; it actually needs to be a file
        // since the Share mechanism can't accept a byte[]. though it can
        // accept a String/CharSequence. Meh.
        try {
            File pdfDirPath = new File(Helpers.Files.AttachmentTempFiles.getDirectory(), "pdfs");
            pdfDirPath.mkdirs();
            File file = new File(pdfDirPath, "pdfsend.pdf");
            OutputStream os = new FileOutputStream(file);
            document.writeTo(os);
            document.close();
            os.close();

            MediUser me = MediUser.getMe();
            String[] recips = new String[1];
            recips[0] = me.email;
            Helpers.Email.sendEmail(recips, getString(R.string.receipt_mail_body),
                    getString(R.string.receipt_mail_subject_preamble), context, file, false);

            // shareDocument(contentUri);
        } catch (IOException e) {
            throw new RuntimeException("Error generating file", e);
        }
    }

    private void shareDocument(Uri uri) {
        Intent mShareIntent = new Intent();
        mShareIntent.setAction(Intent.ACTION_SEND);
        mShareIntent.setType("application/pdf");
        // Assuming it may go via eMail:
        mShareIntent.putExtra(Intent.EXTRA_SUBJECT, "Here is a PDF from PdfSend");
        // Attach the PDf as a Uri, since Android can't take it as bytes yet.
        mShareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(mShareIntent);
        return;
    }

}