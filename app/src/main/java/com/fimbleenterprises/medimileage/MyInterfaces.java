package com.fimbleenterprises.medimileage;

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

    public interface TripSubmitListener {
        void onSuccess(FullTrip trip);
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

    public interface GetAccountsListener {
        public void onSuccess(AccountAddresses accounts);
        public void onFailure(String msg);
    }

    public interface GetUserAddysListener {
        public void onSuccess(UserAddresses addresses);
        public void onFailure(String msg);
    }
}
