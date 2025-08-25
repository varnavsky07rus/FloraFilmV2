package com.alaka_ala.florafilm.ui.fragments.home;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.Collection;

import java.util.Objects;

public class HomeViewModel extends ViewModel {


    // Фильмы-сериалы
    private final MutableLiveData<Collection> collectionMutableLiveDataPopularAll;
    private final MutableLiveData<Integer> pagePopularAllMutableLiveData;

    // Фильмы
    private final MutableLiveData<Collection> collectionMutableLiveDataMovie;
    private final MutableLiveData<Integer> pageMovieMutableLiveData;

    // Сериалы
    private final MutableLiveData<Collection> collectionMutableLiveDataSerial;
    private final MutableLiveData<Integer> pageSerialMutableLiveData;

    // Мультфильмы
    private final MutableLiveData<Collection> collectionMutableLiveDataAnimations;
    private final MutableLiveData<Integer> pageAnimationsMutableLiveData;

    // Драмы
    private final MutableLiveData<Collection> collectionMutableLiveDataDrama;
    private final MutableLiveData<Integer> pageMutableLiveDataDrama;

    // Детям
    private final MutableLiveData<Collection> collectionMutableLiveDataKids;
    private final MutableLiveData<Integer> pageKidsMutableLiveData;



    public HomeViewModel() {
        // Фильмы-сериалы
        this.collectionMutableLiveDataPopularAll = new MutableLiveData<>();
        this.pagePopularAllMutableLiveData = new MutableLiveData<>();
        // Фильмы
        this.collectionMutableLiveDataMovie = new MutableLiveData<>();
        this.pageMovieMutableLiveData = new MutableLiveData<>();
        // Сериалы
        this.pageSerialMutableLiveData = new MutableLiveData<>();
        this.collectionMutableLiveDataSerial = new MutableLiveData<>();
        // Мультфильмы
        this.pageAnimationsMutableLiveData = new MutableLiveData<>();
        this.collectionMutableLiveDataAnimations = new MutableLiveData<>();
        // Драмы
        this.pageMutableLiveDataDrama = new MutableLiveData<>();
        this.collectionMutableLiveDataDrama = new MutableLiveData<>();
        // Детям
        this.pageKidsMutableLiveData = new MutableLiveData<>();
        this.collectionMutableLiveDataKids = new MutableLiveData<>();
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


    // Мультфильмы (Мультфильмы)
    public void addDataCollectionAnimations(Collection collection) {
        if (collectionMutableLiveDataAnimations.getValue() == null) {
            collectionMutableLiveDataAnimations.setValue(collection);
        } else {
            Objects.requireNonNull(collectionMutableLiveDataAnimations.getValue()).getItems().addAll(collection.getItems());
        }
    }

    public MutableLiveData<Collection> getCollectionMutableLiveDataAnimations() {
        return collectionMutableLiveDataAnimations;
    }

    public MutableLiveData<Integer> getPageAnimationsMutableLiveData() {
        return pageAnimationsMutableLiveData;
    }


    // Драмы
    public void addDataCollectionDrama(Collection collection) {
        if (collectionMutableLiveDataDrama.getValue() == null) {
            collectionMutableLiveDataDrama.setValue(collection);
        } else {
            Objects.requireNonNull(collectionMutableLiveDataDrama.getValue()).getItems().addAll(collection.getItems());
        }
    }

    public MutableLiveData<Collection> getCollectionMutableLiveDataDrama() {
        return collectionMutableLiveDataDrama;
    }

    public MutableLiveData<Integer> getPageDramaMutableLiveData() {
        return pageMutableLiveDataDrama;
    }

    // Детям
    public void addDataCollectionKids(Collection collection) {
        if (collectionMutableLiveDataKids.getValue() == null) {
            collectionMutableLiveDataKids.setValue(collection);
        } else {
            Objects.requireNonNull(collectionMutableLiveDataKids.getValue()).getItems().addAll(collection.getItems());
        }
    }

    public MutableLiveData<Collection> getCollectionMutableLiveDataKids() {
        return collectionMutableLiveDataKids;
    }

    public MutableLiveData<Integer> getPageKidsMutableLiveData() {
        return pageKidsMutableLiveData;
    }
}
