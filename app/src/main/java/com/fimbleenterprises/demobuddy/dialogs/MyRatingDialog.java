package com.fimbleenterprises.demobuddy.dialogs;

import cz.msebera.android.httpclient.Header;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.fimbleenterprises.demobuddy.Crm;
import com.fimbleenterprises.demobuddy.objects_and_containers.EntityContainers;
import com.fimbleenterprises.demobuddy.Helpers;
import com.fimbleenterprises.demobuddy.objects_and_containers.MediUser;
import com.fimbleenterprises.demobuddy.R;
import com.fimbleenterprises.demobuddy.objects_and_containers.Requests;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class MyRatingDialog {
    private static Activity ratingActivity;
    private static Dialog ratingDialog;
    private static Button btnSubmit;

    private static RatingBar ratingBar;
    private static EditText txtComments;

    private static float rating;
    private static String comments;

    public static OnRatingSubmitted ratinglistener;

    public interface OnRatingSubmitted {
        void onSuccessful();
        void onFailed();
    }

    public static void rate(Activity activity, OnRatingSubmitted listener) {

        ratingActivity = activity;
        ratinglistener = listener;

        final Dialog dialog = new Dialog(activity);
        final Context c = activity;
        dialog.setContentView(R.layout.dialog_rating);

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

        ratingBar = dialog.findViewById(R.id.ratingBar);
        txtComments = dialog.findViewById(R.id.editComments);
        txtComments.setInputType(InputType.TYPE_CLASS_TEXT);

        btnSubmit = dialog.findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rating > 0) {
                    submitRating();
                } else {
                    Toast.makeText(ratingActivity, "Please supply a rating.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                rating = ratingBar.getRating();
            }
        });

        ratingDialog = dialog;

        ratingDialog.show();

    }

    private static void submitRating() {
        Toast.makeText(ratingActivity, "Submitting...", Toast.LENGTH_SHORT).show();
        ratingDialog.dismiss();

        EntityContainers.EntityContainer container = new EntityContainers.EntityContainer();
        container.entityFields.add(new EntityContainers.EntityField("msus_comment", txtComments.getText() == null ? "" : txtComments.getText().toString()));
        container.entityFields.add(new EntityContainers.EntityField("msus_rating", Float.toString(rating)));
        container.entityFields.add(new EntityContainers.EntityField("msus_version", "Version: " + Helpers.Application.getAppVersion(ratingActivity)));
        container.entityFields.add(new EntityContainers.EntityField("msus_name", "MileBuddy rating from " + MediUser.getMe().fullname));

        Requests.Request request = new Requests.Request(Requests.Request.Function.CREATE);
        request.arguments.add(new Requests.Argument("entity", "msus_milebuddy_rating"));
        request.arguments.add(new Requests.Argument("asuser", MediUser.getMe().systemuserid));
        request.arguments.add(new Requests.Argument("json", container.toJson()));

        new Crm().makeCrmRequest(ratingActivity, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                ratinglistener.onSuccessful();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                ratinglistener.onFailed();
            }
        });

    }
    
    public void showDialog() {
        this.ratingDialog.show();
    }

}