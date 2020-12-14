package com.fimbleenterprises.medimileage;

import android.annotation.SuppressLint;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import cz.msebera.android.httpclient.Header;
import ezvcard.Ezvcard;
import ezvcard.VCard;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static android.os.FileUtils.closeQuietly;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ActivityReceiveContact extends AppCompatActivity {

    Context context;
    private static final String TAG = "ActivityReceiveContact";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        setContentView(R.layout.activity_receive_contact);

        if (getIntent() != null) {
            showAccountPicker();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "onActivityResult ");

        if (requestCode == FullscreenActivityChooseAccount.REQUESTCODE
                && resultCode == FullscreenActivityChooseAccount.ACCOUNT_CHOSEN_RESULT) {

            CrmEntities.Accounts.Account chosenAccount = data.getParcelableExtra(FullscreenActivityChooseAccount.ACCOUNT_RESULT);
            showUpdateCreateDialog(chosenAccount.accountid);
        }

    }

    void showUpdateCreateDialog(final String accountid) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.update_create_dialog);
        Button btnUpdate = dialog.findViewById(R.id.btnUpdateExisting);
        Button btnCreate = dialog.findViewById(R.id.btnCreateNew);
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createContact(accountid);
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getConctacts(accountid);
            }
        });
        dialog.show();
    }

    void createContact(String accountid) {
        final MyProgressDialog progressDialog = new MyProgressDialog(this, "Creating contact...");
        progressDialog.show();
        MyVcardParser vcardParser = new MyVcardParser(this, getIntent());
        Intent startCreateContactService = new Intent(MyContactUpdateCreateService.CREATE_NEW_CONTACT);
        startCreateContactService.putExtra(MyContactUpdateCreateService.ACCOUNTID, accountid);
        startCreateContactService.putExtra(MyContactUpdateCreateService.VCARD_STRING, vcardParser.toVcardString());
    }

    void updateContact(String contactid, MyVcardParser vcard) {

    }

    void showAccountPicker() {
        String terr = MediUser.getMe().territoryid;

        Intent intent = new Intent(this, FullscreenActivityChooseAccount.class);
        intent.putExtra(FullscreenActivityChooseAccount.CURRENT_TERRITORY, MediUser.getMe().getTerritory());
        startActivityForResult(intent, FullscreenActivityChooseAccount.REQUESTCODE);

    }

    void getConctacts(String accountid) {

        final MyProgressDialog myProgressDialog = new MyProgressDialog(this, "Getting contacts...");
        myProgressDialog.show();

        String query = Queries.Contacts.getContacts(accountid);
        Requests.Request request = new Requests.Request(Requests.Request.Function.GET);
        request.arguments = new ArrayList<>();
        request.arguments.add(new Requests.Argument("query", query));

        new Crm().makeCrmRequest(this, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                Log.i(TAG, "onSuccess " + result);
                myProgressDialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(ActivityReceiveContact.this, "Failed to get contacts!", Toast.LENGTH_SHORT).show();
                myProgressDialog.dismiss();
                finish();
            }
        });

    }
}