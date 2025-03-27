package com.alaka_ala.florafilm.ui.util.api.kinopoisk.models;

import androidx.annotation.StringDef;

import java.io.Serializable;
import java.util.ArrayList;

public class Collection implements Serializable {

    public Collection(String titleCollection, String total, String totalPages, ArrayList<ListFilmItem> items) {
        this.titleCollection = titleCollection;
        this.total = total;
        this.totalPages = totalPages;
        this.items = items;
    }

    public String getTitleCollection() {
        return titleCollection;
    }

    public String getTotal() {
        return total;
    }

    public String getTotalPages() {
        return totalPages;
    }

    public ArrayList<ListFilmItem> getItems() {
        return items;
    }

    private final String titleCollection;
    private final String total;
    private final String totalPages;
    private final ArrayList<ListFilmItem> items;
}
