package com.fimbleenterprises.medimileage.activities;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.adapters.BasicObjectRecyclerAdapter;
import com.fimbleenterprises.medimileage.adapters.EmailsRecyclerAdapter;
import com.fimbleenterprises.medimileage.objects_and_containers.BasicObjects;
import com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities;
import com.fimbleenterprises.medimileage.objects_and_containers.MileBuddyMetrics;

import org.joda.time.DateTime;

import java.util.ArrayList;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class EmailsFullscreenActivity extends AppCompatActivity {

    private static final String TAG = "EmailsFullscreenActivity";
    public static final String EMAILS = "EMAILS";
    public RecyclerView recyclerView;
    public EmailsRecyclerAdapter adapter;
    public ArrayList<CrmEntities.Emails.Email> emails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_emails_fullscreen);

        populateList();

    }

    protected void populateList() {

        if (getIntent() != null && getIntent().getAction() != null) {
            if (getIntent().getAction().equals(EMAILS)) {
                this.emails = getIntent().getParcelableArrayListExtra(EMAILS);
            }
        } else {
            finish();
            Toast.makeText(this, "No emails to show", Toast.LENGTH_SHORT).show();
        }

        adapter = new EmailsRecyclerAdapter(this, emails);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

}