package com.fimbleenterprises.medimileage.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.MyApp;
import com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities;
import com.fimbleenterprises.medimileage.objects_and_containers.MediUser;
import com.fimbleenterprises.medimileage.services.MyContactUploadService;
import com.fimbleenterprises.medimileage.objects_and_containers.MyVcard;
import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.dialogs.fullscreen_pickers.FullscreenActivityChooseAccount;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ActivityReceiveContact extends AppCompatActivity {

    Context context;
    private static final String TAG = "ActivityReceiveContact";
    String vcardString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        setContentView(R.layout.activity_receive_contact);

        if (getIntent() != null) {
            vcardString = MyVcard.intentToVcardsString(context, getIntent());


            if (vcardString != null) {
                showAccountPicker();
            } else {
                Toast.makeText(context, "Failed to parse contacts", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApp.setIsVisible(false, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApp.setIsVisible(true, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "onActivityResult ");



        if (requestCode == FullscreenActivityChooseAccount.REQUESTCODE
                && resultCode == FullscreenActivityChooseAccount.ACCOUNT_CHOSEN_RESULT) {

            CrmEntities.Accounts.Account chosenAccount = data.getParcelableExtra(FullscreenActivityChooseAccount.ACCOUNT_RESULT);

            Intent startIntent = new Intent(context, MyContactUploadService.class);
            startIntent.setAction(MyContactUploadService.CREATE_NEW_CONTACT);
            startIntent.putExtra(MyContactUploadService.ACCOUNTID, chosenAccount.entityid);
            startIntent.putExtra(MyContactUploadService.VCARD_STRING, vcardString);
            getApplicationContext().startService(startIntent);
            finish();
        }

    }

    void showAccountPicker() {
        String terr = MediUser.getMe().territoryid;

        Intent intent = new Intent(this, FullscreenActivityChooseAccount.class);
        intent.putExtra(FullscreenActivityChooseAccount.CURRENT_TERRITORY, MediUser.getMe().getTerritory());
        startActivityForResult(intent, FullscreenActivityChooseAccount.REQUESTCODE);

    }
}