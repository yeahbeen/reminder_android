package com.ben.reminder.ui.home;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    final String TAG = Thread.currentThread().getStackTrace()[2].getFileName().replace(".java","");
    public HomeViewModel() {
        Log.e(TAG,"in HomeViewModel()");
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}