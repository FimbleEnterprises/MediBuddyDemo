package com.fimbleenterprises.demobuddy;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;

public class MyExpandableListview extends ExpandableListView implements GestureDetector.OnGestureListener {

    private static final String TAG = "MyExpandableListView";
    Context context;
    public Activity activity;
    ExpandableListAdapter adapter;
    MyPreferencesHelper options;
    GestureDetector gesture;

    public MyExpandableListview(Context context) {
        super(context);
        this.context = context;
        this.options = new MyPreferencesHelper(context);
        // enableSwipeForBack();
    }

    public MyExpandableListview(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.options = new MyPreferencesHelper(context);
        // enableSwipeForBack();
    }

    public MyExpandableListview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.options = new MyPreferencesHelper(context);
        // enableSwipeForBack();
    }

    public MyExpandableListview(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        this.options = new MyPreferencesHelper(context);
        // enableSwipeForBack();
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
    }

    @Override
    public void setAdapter(ExpandableListAdapter adapter) {
        this.adapter = adapter;
        super.setAdapter(adapter);
    }

    @Override
    public boolean expandGroup(int groupPos) {
        return super.expandGroup(groupPos);
    }

    @Override
    public void setOnChildClickListener(OnChildClickListener onChildClickListener) {
        super.setOnChildClickListener(onChildClickListener);
    }



    @Override
    public boolean expandGroup(int groupPos, boolean animate) {
        return super.expandGroup(groupPos, animate);
    }

    // Enables swipe for back functionality on the supplied listview.
    public void enableSwipeForBack() {
        // Creates a gesture detector to detect when the user swipes from left to right.  This
        // gesture will emulate a BACK press if enabled in the user's preferences.
        gesture = new GestureDetector(context,this);

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gesture.onTouchEvent(event);
            }
        });
    }

    @Override
    public boolean collapseGroup(int groupPos) {
        return super.collapseGroup(groupPos);
    }

    public void collapseAll() {
        try {
            for (int i = 0; i < adapter.getGroupCount(); i++) {
                this.collapseGroup(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
/*

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }
*/

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.v(TAG + "onDown", "OnDown called");
        return true;
    }

    @Override
    public void fling(int velocityY) {
        super.fling(velocityY);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return super.onNestedPreFling(target, velocityX, velocityY);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.v(TAG, "onFling has been called!");
        final int SWIPE_MIN_DISTANCE = 120;
        final int SWIPE_MAX_OFF_PATH = 250;
        final int SWIPE_THRESHOLD_VELOCITY = 200;
        try {
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                return false;
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                Log.i(TAG, "Right to Left");
                return true;
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                Log.i(TAG, "Left to Right");
                activity.onBackPressed();
                return false;
            }
        } catch (Exception e) {
            // nothing
        }
        return true;
    }
}
