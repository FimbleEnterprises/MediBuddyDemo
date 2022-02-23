package com.fimbleenterprises.medimileage.activities.ui.mileage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.fimbleenterprises.medimileage.objects_and_containers.LocationContainer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// Working on incorporating a MVVM architecture into the app to compare to broadcast/broadcast
// receiver model currently used.


public class MileageViewModel extends ViewModel {
    private static final String TAG = "MileageViewModel";

    private MutableLiveData<LocationContainer> mLoc;

    public MileageViewModel() {
        mLoc = new MutableLiveData<>();
    }

    /**
     * Updates the live data for any observers to leverage.
     * @param container A LocationContainer object - The MyLocationService generates these based on an active trip changing.
     */
    public void updateLocationContainer(LocationContainer container) {
        mLoc.setValue(container);
    }

    public LiveData<LocationContainer> getLocationContainer() {
        return mLoc;
    }

}