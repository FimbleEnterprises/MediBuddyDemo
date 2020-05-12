package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.viewpager.widget.ViewPager;


/**
 * Created by mweber on 12/2/2015.
 */
public class MyViewPager extends ViewPager {

    final static String TAG = "MyViewPager";
    private boolean isPagingEnabled = true;
    MySettingsHelper options;
    Context context;
    MyInterfaces.TripPageCallback tripPageCallback;
    public int currentPosition;
    private int pageCount = -1;

    public MyViewPager(Context context, MyInterfaces.TripPageCallback tripPageCallback) {
        super(context);
        this.context = context;
        this.tripPageCallback = tripPageCallback;
        options = new MySettingsHelper(context);
    }

    public MyViewPager(Context context,  MyInterfaces.OnPageChanged onPageChanged) {
        super(context);

    }

    public MyViewPager(Context context, MyInterfaces.TripPageCallback tripPageCallback, int pageCount) {
        super(context);
        this.context = context;
        this.pageCount = pageCount;
        this.tripPageCallback = tripPageCallback;
        options = new MySettingsHelper(context);
    }

    public MyViewPager(Context context,  MyInterfaces.OnPageChanged onPageChanged, int pageCount) {
        super(context);
        this.pageCount = pageCount;
    }

    public void addOnPageChangeListener(MyInterfaces.OnPageChanged pageChanged, OnPageChangeListener listener) {
        Log.w(TAG, "addOnPageChangeListener: Page is: " + getCurrentItem());
        super.addOnPageChangeListener(listener);
    }

    @Override
    public void addOnPageChangeListener(OnPageChangeListener listener) {
        Log.w(TAG, "addOnPageChangeListener: Page is: " + getCurrentItem());
        super.addOnPageChangeListener(listener);
    }

    public void setPageCount(int count) {
        this.pageCount = count;
    }

    public int getPageCount() {
        return pageCount;
    }

    /**
     * Set the currently selected page. If the ViewPager has already been through its first
     * layout with its current adapter there will be a smooth animated transition between
     * the current item and the specified item.
     *
     * @param item Item index to select
     */
    @Override
    public void setCurrentItem(int item) {
        Log.d(TAG, "Page is being set to: " + item);
        currentPosition = item;
        super.setCurrentItem(item);

    }

    @Override
    public int getCurrentItem() {
        Log.w(TAG, "getCurrentItem: GET CURRENT ITEM CALLED! (" + super.getCurrentItem() + ")");

        return super.getCurrentItem();
    }

    @Override
    protected void onPageScrolled(int position, float offset, int offsetPixels) {
        super.onPageScrolled(position, offset, offsetPixels);
        Log.i(TAG, "onPageScrolled " + position);
        currentPosition = position;
    }

    /**
     * Set the currently selected page. `
     *
     * @param item         Item index to select
     * @param smoothScroll True to smoothly scroll to the new item, false to transition immediately
     */
    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        Log.d(TAG, "Page is being set to: " + item + " smoothly!");
        Log.w(TAG, "setCurrentItem: CURRENT ITEM: " + item);

        if (item < 0) {
            Log.w(TAG, "setCurrentItem: Can't set pager position less than zero.");
            return;
        }

        if (this.pageCount != -1 && item == this.pageCount) {
            Log.w(TAG, "setCurrentItem: Can't set pager position higher than the page count.");
            return;
        }

        this.currentPosition = item;
        options.setLastPage(item);
        super.setCurrentItem(item, smoothScroll);

    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        options = new MySettingsHelper(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Log.d(TAG, "MyViewPager received a touch event.  Paging enabled: " + this.isPagingEnabled);
        return this.isPagingEnabled && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        //Log.d(TAG, "MyViewPager intercepted a touch event.  Paging enabled: " + this.isPagingEnabled);
        return this.isPagingEnabled && super.onInterceptTouchEvent(event);
    }

    public void setPagingEnabled(boolean b) {
        this.isPagingEnabled = b;
        //Log.d(TAG, "Enable paging: " + this.isPagingEnabled);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {

/*        Log.v(TAG, "canScroll: Swipe triggered from:" + v.getId());
        String tag = "";

        if (Activity_MainActivity.mViewPager.getCurrentItem() == 4) {
            ViewParent viewparent = v.getParent();
            View parent;
            if (viewparent instanceof RelativeLayout) {
                parent = (RelativeLayout) viewparent;
                if (parent.getTag() != null) {
                    tag = parent.getTag().toString();
                }
                if (tag.equals("overview_map_container")) {
                    Log.w(TAG, "canScroll: ALLOWING A SCROLL! ");
                    return false;
                }
            }
        }
        */
        return super.canScroll(v, checkV, dx, x, y);
    }

}
