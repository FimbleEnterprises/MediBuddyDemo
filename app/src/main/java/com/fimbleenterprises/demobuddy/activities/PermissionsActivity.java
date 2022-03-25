package com.fimbleenterprises.demobuddy.activities;

import android.app.Activity;
import android.os.Bundle;

import com.fimbleenterprises.demobuddy.Helpers;
import com.fimbleenterprises.demobuddy.MyApp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.ImageView;
import android.widget.TableRow;

import com.fimbleenterprises.demobuddy.R;

public class PermissionsActivity extends AppCompatActivity {

    Activity activity;

    TableRow rowFilesystem;
    TableRow rowBackgroundLoc;
    TableRow rowForegroundLoc;
    TableRow rowManual;

    ImageView imgFilesystem;
    ImageView imgForegroundLoc;
    ImageView imgBackgroundLoc;
    ImageView imgManual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.activity = this;

        rowFilesystem = findViewById(R.id.rowFilesystem);
        rowForegroundLoc = findViewById(R.id.rowLocationForeground);
        rowBackgroundLoc = findViewById(R.id.rowLocationBackground);
        rowManual = findViewById(R.id.rowManualPermissions);

        rowFilesystem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helpers.Permissions.RequestContainer container = new Helpers.Permissions.RequestContainer();
                container.add(Helpers.Permissions.PermissionType.WRITE_EXTERNAL_STORAGE);
                requestPermissions(container.toArray(), 0);
            }
        });
        rowForegroundLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helpers.Permissions.RequestContainer container = new Helpers.Permissions.RequestContainer();
                container.add(Helpers.Permissions.PermissionType.ACCESS_FINE_LOCATION);
                requestPermissions(container.toArray(), 1);
            }
        });
        rowBackgroundLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helpers.Permissions.RequestContainer container = new Helpers.Permissions.RequestContainer();
                container.add(Helpers.Permissions.PermissionType.ACCESS_BACKGROUND_LOCATION);
                requestPermissions(container.toArray(), 2);
            }
        });
        rowManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helpers.Application.openAppSettings(activity);
            }
        });

        imgFilesystem = findViewById(R.id.imgExternalFiles);
        imgForegroundLoc = findViewById(R.id.imgLocationForeground);
        imgBackgroundLoc = findViewById(R.id.imgLocationBackground);

        checkExistingPerms();

    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApp.setIsVisible(false, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkExistingPerms();
        MyApp.setIsVisible(true, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    void checkExistingPerms() {

        boolean files = Helpers.Permissions.isGranted(Helpers.Permissions.PermissionType.WRITE_EXTERNAL_STORAGE);
        boolean fgLoc = Helpers.Permissions.isGranted(Helpers.Permissions.PermissionType.ACCESS_FINE_LOCATION);
        boolean bgLoc = Helpers.Permissions.isGranted(Helpers.Permissions.PermissionType.ACCESS_BACKGROUND_LOCATION);

        imgFilesystem.setImageResource(!files ? R.drawable.red_x : R.drawable.green_check);
        imgForegroundLoc.setImageResource(!fgLoc ? R.drawable.red_x : R.drawable.green_check);
        imgBackgroundLoc.setImageResource(!bgLoc ? R.drawable.red_x : R.drawable.green_check);

        if (files && fgLoc && bgLoc) {
            rowManual.setVisibility(View.GONE);
            finishAndRemoveTask();
        }

    }

}