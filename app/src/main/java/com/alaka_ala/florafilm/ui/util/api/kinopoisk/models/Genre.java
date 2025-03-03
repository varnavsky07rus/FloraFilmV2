package com.alaka_ala.florafilm.ui.util.api.kinopoisk.models;

import java.io.Serializable;

public class Genre implements Serializable {

    public Genre(String genre) {
        this.genre = genre;
    }

    public String getGenre() {
        return genre;
    }

    private final String genre;

}
