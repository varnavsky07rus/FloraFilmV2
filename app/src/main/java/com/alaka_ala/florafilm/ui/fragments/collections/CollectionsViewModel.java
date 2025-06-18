package com.alaka_ala.florafilm.ui.fragments.collections;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.Collection;

import java.util.Objects;

public class CollectionsViewModel extends ViewModel {
    // Фильмы-сериалы
    private final MutableLiveData<Collection> collectionMutableLiveDataPopularAll;
    private final MutableLiveData<Integer> pagePopularAllMutableLiveData;

    // Фильмы
    private final MutableLiveData<Collection> collectionMutableLiveDataMovie;
    private final MutableLiveData<Integer> pageMovieMutableLiveData;

    // Сериалы
    private final MutableLiveData<Collection> collectionMutableLiveDataSerial;
    private final MutableLiveData<Integer> pageSerialMutableLiveData;


    public CollectionsViewModel() {
        // Фильмы-сериалы
        this.collectionMutableLiveDataPopularAll = new MutableLiveData<>();
        this.pagePopularAllMutableLiveData = new MutableLiveData<>();
        // Фильмы
        this.collectionMutableLiveDataMovie = new MutableLiveData<>();
        this.pageMovieMutableLiveData = new MutableLiveData<>();
        // Сериалы
        this.pageSerialMutableLiveData = new MutableLiveData<>();
        this.collectionMutableLiveDataSerial = new MutableLiveData<>();
    }


    // Фильмы - Movie
    public void addDataCollectionMovie(Collection collection) {
        if (collectionMutableLiveDataMovie.getValue() == null) {
            collectionMutableLiveDataMovie.setValue(collection);
        } else {
            Objects.requireNonNull(collectionMutableLiveDataMovie.getValue()).getItems().addAll(collection.getItems());
        }
    }

    public MutableLiveData<Collection> getCollectionMutableLiveDataMovie() {
        return collectionMutableLiveDataMovie;
    }

    public MutableLiveData<Integer> getPageMovieMutableLiveData() {
        return pageMovieMutableLiveData;
    }


    // PopularAll (Фильмы/Сериалы)
    public void addDataCollectionPopularAll(Collection collection) {
        if (collectionMutableLiveDataPopularAll.getValue() == null) {
            collectionMutableLiveDataPopularAll.setValue(collection);
        } else {
            Objects.requireNonNull(collectionMutableLiveDataPopularAll.getValue()).getItems().addAll(collection.getItems());
        }
    }

    public MutableLiveData<Collection> getCollectionMutableLiveDataPopularAll() {
        return collectionMutableLiveDataPopularAll;
    }

    public MutableLiveData<Integer> getPagePopularAllMutableLiveData() {
        return pagePopularAllMutableLiveData;
    }


    // Сериалы (Сериалы)
    public void addDataCollectionSerial(Collection collection) {
        if (collectionMutableLiveDataSerial.getValue() == null) {
            collectionMutableLiveDataSerial.setValue(collection);
        } else {
            Objects.requireNonNull(collectionMutableLiveDataSerial.getValue()).getItems().addAll(collection.getItems());
        }
    }

    public MutableLiveData<Collection> getCollectionMutableLiveDataSerial() {
        return collectionMutableLiveDataSerial;
    }

    public MutableLiveData<Integer> getPageSerialMutableLiveData() {
        return pageSerialMutableLiveData;
    }

}
