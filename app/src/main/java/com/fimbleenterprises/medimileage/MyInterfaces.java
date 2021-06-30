package com.fimbleenterprises.medimileage;

import com.fimbleenterprises.medimileage.objects_and_containers.AccountAddresses;
import com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities;
import com.fimbleenterprises.medimileage.objects_and_containers.FullTrip;
import com.fimbleenterprises.medimileage.objects_and_containers.UserAddresses;

import org.joda.time.DateTime;

import java.io.File;
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

    public interface DeleteManyListener {
        void onResult(CrmEntities.DeleteManyResponses responses);
        void onError(String msg);
    }

    public interface CreateManyListener {
        void onResult(CrmEntities.CreateManyResponses responses);
        void onError(String msg);
    }

    public interface TripAssociationsListener {
        public void onSuccess(CrmEntities.TripAssociations associations);
        public void onFailure(String msg);
    }

    public interface MetricUpdateListener {
        void onSuccess();
        void onFailure(String msg);
    }

    public interface EntityUpdateListener {
        void onSuccess();
        void onFailure(String msg);
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

    public interface AuthenticationResult {
        void onSuccess();
        void onFailure();
        void onError(String msg, Throwable exception);
    }

    public interface CrmRequestListener {
        void onComplete(Object result);
        void onProgress(Crm.AsyncProgress progress);
        void onFail(String error);
    }

    public interface EncoderListener {
        public void onSuccess(String base64String);
        public void onFailure(String error);
    }

    public interface DecoderListener {
        public void onSuccess(File decodedFile);
        public void onFailure(String error);
    }

    public interface GetOpportunitiesListener {
        void onSuccess(CrmEntities.Opportunities opportunities);
        void onFailure(String error);
    }

    public interface GetLeadsListener {
        void onSuccess(CrmEntities.Leads leads);
        void onFailure(String error);
    }

    public interface leadQualifyListener {
        void onSuccess(CrmEntities.Opportunities.Opportunity newOpportunity);
        void onFailure(String errorMsg);
    }
}
