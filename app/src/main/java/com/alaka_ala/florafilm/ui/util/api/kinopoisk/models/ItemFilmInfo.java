package com.alaka_ala.florafilm.ui.util.api.kinopoisk.models;

import java.io.Serializable;
import java.util.ArrayList;

/**Класс содержит детальную информацию о фильме/сериале*/
public class ItemFilmInfo implements Serializable {

    private int kinopoiskId = 0;
    private String kinopoiskHDId = "null";
    private String imdbId = "null";
    private String nameRu = "null";
    private String nameEn = "null";
    private String nameOriginal = "null";
    private String posterUrl = "null";
    private String posterUrlPreview = "null";
    private String coverUrl = "null";
    private String logoUrl = "null";
    private int reviewsCount = 0;
    private int ratingGoodReview = 0;
    private int ratingGoodReviewVoteCount = 0;
    private double ratingKinopoisk = 0;
    private int ratingKinopoiskVoteCount = 0;
    private int ratingImdb = 0;
    private int ratingImdbVoteCount = 0;
    private int ratingFilmCritics = 0;
    private int ratingFilmCriticsVoteCount = 0;
    private int ratingAwait = 0;
    private int ratingAwaitCount = 0;
    private int ratingRfCritics = 0;
    private int ratingRfCriticsVoteCount = 0;
    private String webUrl = "null";
    private String year = "null";
    private int filmLength = 0;
    private String slogan = "null";
    private String description = "null";
    private String shortDescription = "null";
    private String editorAnnotation = "null";
    private boolean isTicketsAvailable = false;
    private String productionStatus = "null";
    private String type = "null";
    private String ratingMpaa = "null";
    private String ratingAgeLimits = "null";
    private ArrayList<Country> countries = new ArrayList<>();
    private ArrayList<Genre> genres = new ArrayList<>();
    private String startYear = "null";
    private String endYear = "null";
    private boolean serial = false;
    private boolean shortFilm = false;
    private boolean completed = false;
    private boolean hasImax = false;
    private boolean has3D = false;
    private boolean lastSync = false;


    public ItemFilmInfo(int kinopoiskId,
                        String kinopoiskHDId,
                        String imdbId,
                        String nameRu,
                        String nameEn,
                        String nameOriginal,
                        String posterUrl,
                        String posterUrlPreview,
                        String coverUrl,
                        String logoUrl,
                        int reviewsCount,
                        int ratingGoodReview,
                        int ratingGoodReviewVoteCount,
                        double ratingKinopoisk,
                        int ratingKinopoiskVoteCount,
                        int ratingImdb,
                        int ratingImdbVoteCount,
                        int ratingFilmCritics,
                        int ratingFilmCriticsVoteCount,
                        int ratingAwait,
                        int ratingAwaitCount,
                        int ratingRfCritics,
                        int ratingRfCriticsVoteCount,
                        String webUrl,
                        String year,
                        int filmLength,
                        String slogan,
                        String description,
                        String shortDescription,
                        String editorAnnotation,
                        boolean isTicketsAvailable,
                        String productionStatus,
                        String type,
                        String ratingMpaa,
                        String ratingAgeLimits,
                        ArrayList<Country> countries,
                        ArrayList<Genre> genres,
                        String startYear,
                        String endYear,
                        boolean serial,
                        boolean shortFilm,
                        boolean completed,
                        boolean hasImax,
                        boolean has3D,
                        boolean lastSync) {

        this.kinopoiskId = kinopoiskId;
        this.kinopoiskHDId = kinopoiskHDId;
        this.imdbId = imdbId;
        this.nameRu = nameRu;
        this.nameEn = nameEn;
        this.nameOriginal = nameOriginal;
        this.posterUrl = posterUrl;
        this.posterUrlPreview = posterUrlPreview;
        this.coverUrl = coverUrl;
        this.logoUrl = logoUrl;
        this.reviewsCount = reviewsCount;
        this.ratingGoodReview = ratingGoodReview;
        this.ratingGoodReviewVoteCount = ratingGoodReviewVoteCount;
        this.ratingKinopoisk = ratingKinopoisk;
        this.ratingKinopoiskVoteCount = ratingKinopoiskVoteCount;
        this.ratingImdb = ratingImdb;
        this.ratingImdbVoteCount = ratingImdbVoteCount;
        this.ratingFilmCritics = ratingFilmCritics;
        this.ratingFilmCriticsVoteCount = ratingFilmCriticsVoteCount;
        this.ratingAwait = ratingAwait;
        this.ratingAwaitCount = ratingAwaitCount;
        this.ratingRfCritics = ratingRfCritics;
        this.ratingRfCriticsVoteCount = ratingRfCriticsVoteCount;
        this.webUrl = webUrl;
        this.year = year;
        this.filmLength = filmLength;
        this.slogan = slogan;
        this.description = description;
        this.shortDescription = shortDescription;
        this.editorAnnotation = editorAnnotation;
        this.isTicketsAvailable = isTicketsAvailable;
        this.productionStatus = productionStatus;
        this.type = type;
        this.ratingMpaa = ratingMpaa;
        this.ratingAgeLimits = ratingAgeLimits;
        this.countries = countries;
        this.genres = genres;
        this.startYear = startYear;
        this.endYear = endYear;
        this.serial = serial;
        this.shortFilm = shortFilm;
        this.completed = completed;
        this.hasImax = hasImax;
        this.has3D = has3D;
        this.lastSync = lastSync;
    }



    public boolean isLastSync() {
        return lastSync;
    }

    public boolean isHas3D() {
        return has3D;
    }

    public boolean isHasImax() {
        return hasImax;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isShortFilm() {
        return shortFilm;
    }

    public boolean isSerial() {
        return serial;
    }

    public String getEndYear() {
        return endYear;
    }

    public String getStartYear() {
        return startYear;
    }

    public ArrayList<Genre> getGenres() {
        return genres;
    }

    public ArrayList<Country> getCountries() {
        return countries;
    }

    public String getRatingAgeLimits() {
        return ratingAgeLimits;
    }

    public String getRatingMpaa() {
        return ratingMpaa;
    }

    public String getType() {
        return type;
    }

    public String getProductionStatus() {
        return productionStatus;
    }

    public boolean isTicketsAvailable() {
        return isTicketsAvailable;
    }

    public String getEditorAnnotation() {
        return editorAnnotation;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public String getSlogan() {
        return slogan;
    }

    public int getFilmLength() {
        return filmLength;
    }

    public String getYear() {
        return year;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public int getRatingRfCriticsVoteCount() {
        return ratingRfCriticsVoteCount;
    }

    public int getRatingRfCritics() {
        return ratingRfCritics;
    }

    public int getRatingAwaitCount() {
        return ratingAwaitCount;
    }

    public int getRatingAwait() {
        return ratingAwait;
    }

    public int getRatingFilmCriticsVoteCount() {
        return ratingFilmCriticsVoteCount;
    }

    public int getRatingFilmCritics() {
        return ratingFilmCritics;
    }

    public int getRatingImdbVoteCount() {
        return ratingImdbVoteCount;
    }

    public int getRatingImdb() {
        return ratingImdb;
    }

    public int getRatingKinopoiskVoteCount() {
        return ratingKinopoiskVoteCount;
    }

    public double getRatingKinopoisk() {
        return ratingKinopoisk;
    }

    public int getRatingGoodReviewVoteCount() {
        return ratingGoodReviewVoteCount;
    }

    public int getRatingGoodReview() {
        return ratingGoodReview;
    }

    public int getReviewsCount() {
        return reviewsCount;
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

    public String getNameOriginal() {
        return nameOriginal;
    }

    public String getNameEn() {
        return nameEn;
    }

    public String getNameRu() {
        return nameRu;
    }

    public String getImdbId() {
        return imdbId;
    }

    public String getKinopoiskHDId() {
        return kinopoiskHDId;
    }

    public int getKinopoiskId() {
        return kinopoiskId;
    }







}
