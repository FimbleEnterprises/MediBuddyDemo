package com.fimbleenterprises.medimileage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.joda.time.DateTime;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cz.msebera.android.httpclient.Header;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivityChooseRep extends AppCompatActivity {

    private static final String TAG = "FullscreenActivityChooseTerritory";

    Context context;
    RecyclerView listView;
    ArrayList<BasicObjects.BasicObject> objects = new ArrayList<>();
    BasicObjectRecyclerAdapter adapter;
    public static final int REQUESTCODE = 012;
    public static final String CHOICE_RESULT = "CHOICE_RESULT";
    public static final String CURRENT_VALUE = "CURRENT_VALUE";
    MediUser currentUser;
    MySettingsHelper options;

    /**
     * Shows a picker for sales reps.  Will return an intent with the selected user as a MediUser object with a tag of: CHOICE_RESULT
     * @param activity An activity that can raise an OnActivityResult event.
     * @param currentUser A MediUser - if null will use the current user.
     */
    public static void showRepChooser(Activity activity, MediUser currentUser) {
        if (currentUser == null) {
            currentUser = MediUser.getMe();
        }
        Intent intent = new Intent(activity, FullscreenActivityChooseRep.class);
        intent.putExtra(CURRENT_VALUE, currentUser);
        activity.startActivityForResult(intent, REQUESTCODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        options = new MySettingsHelper(context);
        setContentView(R.layout.activity_fullscreen_choose_rep);
        listView = findViewById(R.id.rvBasicObjects);

        Intent intent = getIntent();
        if (intent != null) {
            MediUser curUser = (MediUser) intent.getParcelableExtra(CURRENT_VALUE);
            if (curUser != null) {
                currentUser = curUser;
                this.setTitle(options.isExplicitMode() ? getString(R.string.select_user_explicit)
                        : getString(R.string.select_user));
            }
        } else {
            currentUser = MediUser.getMe();
        }

        getReps();

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

    void getReps() {
        final MyProgressDialog dialog = new MyProgressDialog(this, "Getting reps...");
        dialog.show();

        Crm crm = new Crm();
        ArrayList<Requests.Argument> args = new ArrayList<>();
        Requests.Argument argument = new Requests.Argument("query", Queries.Users.getUsUsers());
        args.add(argument);
        Requests.Request request = new Requests.Request(Requests.Request.Function.GET, args);

        crm.makeCrmRequest(this, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                RestResponse response = new RestResponse(new String(responseBody));
                ArrayList<MediUser> users = MediUser.createMany(response);
                objects.clear();
                for (MediUser u : users) {
                    BasicObjects.BasicObject basicObject = new BasicObjects.BasicObject(u.fullname, u.territoryname, u);
                    basicObject.iconResource = R.drawable.next32;
                    if (currentUser.systemuserid.equals(u.systemuserid)) {
                        basicObject.isSelected = true;
                    }
                    objects.add(basicObject);
                    dialog.dismiss();
                }
                populateUsers();
                Log.i(TAG, "onSuccess Response is " + response.value.length() + " chars long");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, "Failed!\n" + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    void populateUsers() {
        adapter = new BasicObjectRecyclerAdapter(this, objects);
        listView.setAdapter(adapter);
        listView.addItemDecoration(new DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL));
        listView.setLayoutManager(new LinearLayoutManager(context));
        adapter.setClickListener(new BasicObjectRecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                MediUser user = (MediUser) objects.get(position).object;
                Intent intent = new Intent(CHOICE_RESULT);
                intent.putExtra(CHOICE_RESULT, user);
                setResult(RESULT_OK, intent);
                finish();
                Log.i(TAG, "onItemClick Position: " + position);
            }
        });
    }

}