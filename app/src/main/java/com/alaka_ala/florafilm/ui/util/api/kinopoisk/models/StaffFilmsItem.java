package com.alaka_ala.florafilm.ui.util.api.kinopoisk.models;

import java.io.Serializable;

public class StaffFilmsItem implements Serializable {
    public StaffFilmsItem(int filmId, String nameRu, String nameEn, int rating, boolean general, String description, String professionKey) {
        this.filmId = filmId;
        this.nameRu = nameRu;
        this.nameEn = nameEn;
        this.rating = rating;
        this.general = general;
        this.description = description;
        this.professionKey = professionKey;
        posterUrl = "https://kinopoiskapiunofficial.tech/images/posters/kp/" + filmId + ".jpg";
        posterUrlPreview = "https://kinopoiskapiunofficial.tech/images/posters/kp_small/" + filmId + ".jpg";
    }

    public int getFilmId() {
        return filmId;
    }

    public String getNameRu() {
        return nameRu;
    }

    public String getNameEn() {
        return nameEn;
    }

    public int getRating() {
        return rating;
    }

    public boolean isGeneral() {
        return general;
    }

    public String getDescription() {
        return description;
    }

    public String getProfessionKey() {
        return professionKey;
    }

    public String getPosterUrlPreview() {
        return posterUrlPreview;
    }

    public String getPosterUrl() {
        return posterUrl;
    }


    private final int filmId;
    private final String nameRu;
    private final String nameEn;
    private final int rating;
    private final boolean general;
    private final String description;
    private final String professionKey;
    private final String posterUrl;
    private final String posterUrlPreview;


}
