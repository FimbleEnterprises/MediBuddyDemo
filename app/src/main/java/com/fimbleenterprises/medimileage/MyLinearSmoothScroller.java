package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.util.DisplayMetrics;

import androidx.recyclerview.widget.LinearSmoothScroller;

/**
 * This class overrides LinearSmoothScroller and only for the purposes of increasing the scroll speed.
 * The speed is set in the original class at 25f and it is set as <i>final</i>.<br/>
  */
public class MyLinearSmoothScroller extends LinearSmoothScroller {

    // Lower is faster (stock speed == 25f)
    public static final float SMOOTH_SCROLL_SPEED = 10f;

    /**
     * This class overrides LinearSmoothScroller and only for the purposes of increasing the scroll speed.
     * The speed is set in the original class at 25f and it is set as <i>final</i>.<br/>
     */
    public MyLinearSmoothScroller(Context context) {
        super(context);
    }

    @Override
    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
        return SMOOTH_SCROLL_SPEED / displayMetrics.densityDpi;
    }
}
