package com.fimbleenterprises.demobuddy.activities.fullscreen_pickers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.fimbleenterprises.demobuddy.Helpers;
import com.fimbleenterprises.demobuddy.R;
import com.fimbleenterprises.demobuddy.adapters.BasicObjectRecyclerAdapter;
import com.fimbleenterprises.demobuddy.objects_and_containers.BasicObjects;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivityBasicObjectPicker extends AppCompatActivity {

    private static final String TAG = "FullscreenActivityBasicObjectPicker";
    public static final int ITEM_CHOSEN_RESULT_CODE = 9291;

    Context context;
    RecyclerView listView;
    BasicObjects objects = new BasicObjects();
    public static final String BASIC_OBJECTS_TO_SHOW = "BASIC_OBJECTS_TO_SHOW";
    public static final String CHOSEN_OBJECT_INDEX = "OBJECT_SELECTED";
    BasicObjectRecyclerAdapter adapter;
    public static final int REQUESTCODE = 012;
    SmartRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        setContentView(R.layout.activity_fullscreen_choose_account);
        listView = findViewById(R.id.rvBasicObjects);
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setEnableRefresh(false); // No need to have a refresh for statically supplied objects.

        if (getIntent().getParcelableExtra(BASIC_OBJECTS_TO_SHOW) == null) {
            Toast.makeText(context, "No data to show", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            this.objects = getIntent().getParcelableExtra(BASIC_OBJECTS_TO_SHOW);
            populate();
        }

        Helpers.Views.MySwipeHandler mySwipeHandler = new Helpers.Views.MySwipeHandler(new Helpers.Views.MySwipeHandler.MySwipeListener() {
            @Override
            public void onSwipeLeft() {  }

            @Override
            public void onSwipeRight() {
                onBackPressed();
            }
        });
        mySwipeHandler.addView(listView);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "onActivityResult ");

        if (data != null && resultCode == RESULT_OK) {

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Shows a picker for accounts.  Will return an intent with the selected account as an object with a tag of: CHOICE_RESULT
     * @param activity An activity that can raise an OnActivityResult event.
     * @param objects The objects to show.
     */
    public static void showPicker(Activity activity, BasicObjects objects, int requestCode) {

        Intent intent = new Intent(activity, FullscreenActivityBasicObjectPicker.class);
        // intent.putExtra(BASIC_OBJECTS_TO_SHOW, objects);
        activity.startActivityForResult(intent, requestCode);
    }

    void populate() {

        adapter = new BasicObjectRecyclerAdapter(this, objects.list);
        listView.setAdapter(adapter);
        listView.addItemDecoration(new DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL));
        listView.setLayoutManager(new LinearLayoutManager(context));
        adapter.setClickListener(new BasicObjectRecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                try {
                    Intent intent = new Intent(CHOSEN_OBJECT_INDEX);
                    intent.putExtra(CHOSEN_OBJECT_INDEX, position);
                    setResult(ITEM_CHOSEN_RESULT_CODE, intent);
                    finish();
                    Log.i(TAG, "onItemClick Position: " + position);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}