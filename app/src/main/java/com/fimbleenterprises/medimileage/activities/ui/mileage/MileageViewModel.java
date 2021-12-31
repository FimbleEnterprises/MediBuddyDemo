package com.fimbleenterprises.medimileage.activities.ui.mileage;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MileageViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public MileageViewModel() {
        mText = new MutableLiveData<>();
        // mText.setValue("");
    }

    public LiveData<String> getText() {
        return mText;
    }
}