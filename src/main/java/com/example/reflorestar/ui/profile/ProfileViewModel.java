package com.example.reflorestar.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.reflorestar.ui.catalog.CatalogViewModel;

public class ProfileViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public ProfileViewModel(){
        mText = new MutableLiveData<>();
        mText.setValue("This is the profile fragment");
    }

    public LiveData<String> getText(){return mText;}
}