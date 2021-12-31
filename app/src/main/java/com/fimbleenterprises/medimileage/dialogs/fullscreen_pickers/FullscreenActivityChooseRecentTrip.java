package com.fimbleenterprises.medimileage.dialogs.fullscreen_pickers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.MyPreferencesHelper;
import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.adapters.RecentTripsRecyclerAdapter;
import com.fimbleenterprises.medimileage.dialogs.MyYesNoDialog;
import com.fimbleenterprises.medimileage.objects_and_containers.BasicObjects;
import com.fimbleenterprises.medimileage.objects_and_containers.RecentOrSavedTrip;

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
        final BasicObjects tripsAsBasicObjects = RecentOrSavedTrip.toBasicObjects(trips);
        adapter = new RecentTripsRecyclerAdapter(this, tripsAsBasicObjects.list, new RecentTripsRecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                MyYesNoDialog.show(context, new MyYesNoDialog.YesNoListener() {
                    @Override
                    public void onYes() {
                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                        RecentOrSavedTrip clickedTrip = (RecentOrSavedTrip) adapter.mData.get(position).object;
                        if (clickedTrip.delete()) {
                            adapter.mData.remove(position);
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(context, "Failed to remove recent trip.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onNo() {
                        // nothing
                    }
                });
            }
        });
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