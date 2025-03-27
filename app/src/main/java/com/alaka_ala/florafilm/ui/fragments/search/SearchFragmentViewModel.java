package com.alaka_ala.florafilm.ui.fragments.search;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.Collection;

public class SearchFragmentViewModel extends ViewModel {
    private final MutableLiveData<String> query = new MutableLiveData<>();
    public final MutableLiveData<Collection> collection = new MutableLiveData<>();

    public MutableLiveData<Integer> getPage() {
        return page;
    }

    public final MutableLiveData<Integer> page = new MutableLiveData<>();


    public MutableLiveData<String> getQuery() {
        return query;
    }

    public void setCollection(Collection collection) {
        this.collection.setValue(collection);
    }

    public Collection getCollection() {
        return collection.getValue();
    }


}
