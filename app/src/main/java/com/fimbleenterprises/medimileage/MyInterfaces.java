package com.fimbleenterprises.medimileage;

import android.location.Location;

import org.joda.time.DateTime;

import java.util.ArrayList;

import androidx.annotation.Nullable;

public class MyInterfaces {

    public interface YesNoResult {
        public void onYes(@Nullable Object object);
        public void onNo(@Nullable Object object);
    }

    public interface TripDeleteCallback {
        void onSuccess(int entriesDeleted);
        void onFailure(String message);
    }

    public interface GetTripsCallback {
        void onSuccess(ArrayList<FullTrip> trips);
        void onFailure(String message);
    }

    public interface DateSelector {
        void onDateSelected(DateTime selectedDate, String selectedDateStr);
    }

    public interface TripPageCallback {
        void onSend();
        void onTripPageVisible();
        void onTripPageGone();
    }

    public interface ReimbursementRateCallback {
        void onSuccess(float rate);
        void onFailure(String message);
    }

    public interface OnPageChanged {
        void pageChanged();

    }

    public interface AnimationEndedListener {
        public void onEnd();
    }
}
