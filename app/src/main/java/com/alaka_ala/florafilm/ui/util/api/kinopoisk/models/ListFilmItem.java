package com.alaka_ala.florafilm.ui.util.api.kinopoisk.models;

import java.io.Serializable;
import java.util.ArrayList;

/**В данном классе содержится краткая информация о фильме/сериале из кинопоиска*/
public class ListFilmItem implements Serializable {
    private final int kinopoiskId;
    private final int imdbId;
    private final String nameRu;
    private final String nameEn;
    private final String nameOriginal;
    private final ArrayList<Country> countries;
    private final ArrayList<Genre> genres;
    private final double ratingKinopoisk;
    private final String ratingImdb;
    private final int year;
    private final String type;
    private final String posterUrl;
    private final String posterUrlPreview;
    private final String coverUrl;
    private final String logoUrl;
    private final String description;
    private final String ratingAgeLimits;

    public String getRatingAgeLimits() {
        return ratingAgeLimits;
    }

    public String getDescription() {
        return description;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public String getPosterUrlPreview() {
        return posterUrlPreview;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public String getType() {
        return type;
    }

    public int getYear() {
        return year;
    }

    public String getRatingImdb() {
        return ratingImdb;
    }

    public double getRatingKinopoisk() {
        return ratingKinopoisk;
    }

    public ArrayList<Genre> getGenres() {
        return genres;
    }

    public ArrayList<Country> getCountries() {
        return countries;
    }

    public String getNameOriginal() {
        return nameOriginal;
    }

    public String getNameEn() {
        return nameEn;
    }

    public String getNameRu() {
        return nameRu;
    }

    public int getImdbId() {
        return imdbId;
    }

    public int getKinopoiskId() {
        return kinopoiskId;
    }


    public ListFilmItem(int kinopoiskId,
                        int imdbId,
                        String nameRu,
                        String nameEn,
                        String nameOriginal,
                        ArrayList<Country> countries,
                        ArrayList<Genre> genres,
                        double ratingKinopoisk,
                        String ratingImdb,
                        int year,
                        String type,
                        String posterUrl,
                        String posterUrlPreview,
                        String coverUrl,
                        String logoUrl,
                        String description,
                        String ratingAgeLimits) {
        this.kinopoiskId = kinopoiskId;
        this.imdbId = imdbId;
        this.nameRu = nameRu;
        this.nameEn = nameEn;
        this.nameOriginal = nameOriginal;
        this.countries = countries;
        this.genres = genres;
        this.ratingKinopoisk = ratingKinopoisk;
        this.ratingImdb = ratingImdb;
        this.year = year;
        this.type = type;
        this.posterUrl = posterUrl;
        this.posterUrlPreview = posterUrlPreview;
        this.coverUrl = coverUrl;
        this.logoUrl = logoUrl;
        this.description = description;
        this.ratingAgeLimits = ratingAgeLimits;
    }


}
