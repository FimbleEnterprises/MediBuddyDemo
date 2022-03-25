package com.fimbleenterprises.demobuddy.activities.fullscreen_pickers;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fimbleenterprises.demobuddy.Helpers;
import com.fimbleenterprises.demobuddy.MyPreferencesHelper;
import com.fimbleenterprises.demobuddy.R;
import com.fimbleenterprises.demobuddy.adapters.RecentTripsRecyclerAdapter;
import com.fimbleenterprises.demobuddy.dialogs.MyYesNoDialog;
import com.fimbleenterprises.demobuddy.objects_and_containers.BasicObjects;
import com.fimbleenterprises.demobuddy.objects_and_containers.RecentOrSavedTrip;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivityChooseRecentTrip extends AppCompatActivity {

    private static final String TAG = "FullscreenActivityChooseRecentTrip";

    Context context;
    RecyclerView listView;
    ArrayList<RecentOrSavedTrip> trips = new ArrayList<>();

    // I cannot come up with an extensible solution for using BasicObjects for fullscreen pickers
    // because they just don't parcel well - their object property is too generic and results in
    // endless martialing errors when adding them as intent extras.

    // That said, it is arguably easier to continue to use the BasicObject strategy for the adapter alone if for no other
    // reason than it allows the adapter to be reused as opposed to creating a custom adapter for every
    // custom data type.  I have spent too much time trying to make BasicObjects pass through intent extras
    // and this is the compromise I am going with for now - custom picker activities that leverage
    // ArrayList<BasicObject> adapters (BasicObjectRecyclerAdapter and BasicObjectExpandableListviewAdapter)
    // -Matt Nov, 21.
    RecentTripsRecyclerAdapter adapter;

    public static final int REQUESTCODE = 012;
    public static final String CHOICE_RESULT = "CHOICE_RESULT";
    public static final String TRIP_LIST = "OBJECTS";
    MyPreferencesHelper options;

    /**
     * Shows a picker for recent trips.  Will return an intent with the selected user as a BasicObject object with a tag of: CHOICE_RESULT
     * @param activity An activity that can raise an OnActivityResult event.
     */
    public static void showPicker(Activity activity, ArrayList<RecentOrSavedTrip> trips, int requestCode) {
        Intent intent = new Intent(activity, FullscreenActivityChooseRecentTrip.class);
        intent.putExtra(TRIP_LIST, trips);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        options = new MyPreferencesHelper(context);
        setContentView(R.layout.activity_fullscreen_choose_generic);
        listView = findViewById(R.id.rvBasicObjects);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(TRIP_LIST)) {
                this.trips = intent.getParcelableArrayListExtra(TRIP_LIST);
                if (this.trips == null || this.trips.size() < 1) {
                    setResult(Activity.RESULT_CANCELED);
                    finishActivity(REQUESTCODE);
                }
                populateTrips();
            }
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

    void populateTrips() {

        // Create an array consumable by the recyclerview
        final BasicObjects tripsAsBasicObjects = RecentOrSavedTrip.toBasicObjects(trips);
        adapter = new RecentTripsRecyclerAdapter(this, tripsAsBasicObjects.list,
                new RecentTripsRecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                MyYesNoDialog.show(context, new MyYesNoDialog.YesNoListener() {
                    @Override
                    public void onYes() {
                        Toast.makeText(context, getString(R.string.DELETED), Toast.LENGTH_SHORT).show();
                        RecentOrSavedTrip clickedTrip = (RecentOrSavedTrip) adapter.mData.get(position).object;
                        if (clickedTrip.delete()) {
                            adapter.mData.remove(position);
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(context, getString(R.string.FAILED_TO_REMOVE_RECENT_TRIP), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onNo() {
                        // nothing
                    }
                });
            }
        });
        adapter.setOnEditClickListener(new RecentTripsRecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                RecentOrSavedTrip clickedTrip = (RecentOrSavedTrip) adapter.mData.get(position).object;
                final Dialog dialog = new Dialog(context);
                final Context c = context;
                dialog.setContentView(R.layout.edit_recent_trip);
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
                AutoCompleteTextView txtName = dialog.findViewById(R.id.autocomplete_EditText_NameTrip);
                txtName.setText(clickedTrip.name);

                EditText txtDistance = dialog.findViewById(R.id.editTxt_Distance);
                txtDistance.setText(clickedTrip.distanceInMiles + "");

                Button btnSave = dialog.findViewById(R.id.btnSaveEdits);
                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            clickedTrip.name = txtName.getText().toString();
                            clickedTrip.distanceInMiles = Float.parseFloat(txtDistance.getText().toString());
                            if (clickedTrip.save()) {
                                populateTrips();
                                dialog.dismiss();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                dialog.show();
            }
        });

        // If no data, show the no data label
        TextView tvNoData = findViewById(R.id.txtNoData);
        tvNoData.setVisibility((trips == null || trips.size() == 0) ? View.VISIBLE : View.GONE);

        // Configure and set the adapter
        listView.setAdapter(adapter);
        listView.addItemDecoration(new DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL));
        listView.setLayoutManager(new LinearLayoutManager(context));
        adapter.setClickListener(new RecentTripsRecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                RecentOrSavedTrip trip = (RecentOrSavedTrip) tripsAsBasicObjects.list.get(position).object;
                Intent intent = new Intent(CHOICE_RESULT);
                intent.putExtra(CHOICE_RESULT, trip);
                setResult(RESULT_OK, intent);
                finish();
                Log.i(TAG, "onItemClick Position: " + position);
            }
        });
    }

}