package com.fimbleenterprises.medimileage.ui.authentication;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AuthenticationViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public AuthenticationViewModel() {
        mText = new MutableLiveData<>();
        // mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}