package com.fimbleenterprises.medimileage.activities;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.MimeTypeFilter;
import cz.msebera.android.httpclient.entity.mime.MIME;

import android.content.ContentResolver;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities;

import java.io.File;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ViewEmailActivity extends AppCompatActivity {

    private static final String TAG = "";
    public static final String EMAIL = "EMAIL";
    CrmEntities.Emails.Email email;
    TextView txtFrom;
    TextView txtTo;
    TextView txtSubject;
    TextView txtSentOn;
    private View mContentView;
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_email);
        webView = (WebView) findViewById(R.id.webview);
        txtFrom = findViewById(R.id.txtFrom);
        txtTo = findViewById(R.id.txtTo);
        txtSentOn = findViewById(R.id.txtDate);
        txtSubject = findViewById(R.id.txtSubject);

        if (getIntent() == null || !getIntent().hasExtra(EMAIL)) {
            finish();
            Toast.makeText(this, "Failed to display email.", Toast.LENGTH_SHORT).show();
        }

        email = getIntent().getParcelableExtra(EMAIL);
        this.setTitle(email.regardingFormatted);

        txtFrom.setText(email.fromSender);
        txtTo.setText(email.toRecipients);
        txtSubject.setText(email.subject);
        txtSentOn.setText(email.createdOnFormatted);
        webView.loadDataWithBaseURL(null, email.description, "text/html", "UTF-8", null);

    }


}