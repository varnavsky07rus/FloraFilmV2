package com.alaka_ala.florafilm.ui.fragments.home;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.Collection;

import java.util.Objects;

public class HomeViewModel extends ViewModel {


    private final MutableLiveData<Collection> collectionMutableLiveData;
    private final MutableLiveData<Integer> pagePopularAllMutableLiveData;



    public HomeViewModel() {
        this.collectionMutableLiveData = new MutableLiveData<>();
        this.pagePopularAllMutableLiveData = new MutableLiveData<>();
    }

    public void addDataCollectionPopularAll(Collection collection) {
        if (collectionMutableLiveData.getValue() == null){
            collectionMutableLiveData.setValue(collection);
        } else {
            Objects.requireNonNull(collectionMutableLiveData.getValue()).getItems().addAll(collection.getItems());
        }
    }

    public MutableLiveData<Collection> getCollectionMutableLiveData() {
        return collectionMutableLiveData;
    }

    public MutableLiveData<Integer> getPagePopularAllMutableLiveData() {
        return pagePopularAllMutableLiveData;
    }
}
