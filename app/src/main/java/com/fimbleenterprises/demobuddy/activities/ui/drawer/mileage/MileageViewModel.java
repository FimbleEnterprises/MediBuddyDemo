package com.fimbleenterprises.demobuddy.activities.ui.drawer.mileage;

import com.fimbleenterprises.demobuddy.objects_and_containers.LocationContainer;

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