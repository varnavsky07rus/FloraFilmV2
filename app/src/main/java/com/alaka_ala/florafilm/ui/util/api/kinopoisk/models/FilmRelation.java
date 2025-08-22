package com.alaka_ala.florafilm.ui.util.api.kinopoisk.models;

import com.google.gson.annotations.SerializedName;

public class FilmRelation {
    @SerializedName("filmId")
    private int filmId;

    @SerializedName("nameRu")
    private String nameRu;

    @SerializedName("nameEn")
    private String nameEn;

    @SerializedName("nameOriginal")
    private String nameOriginal;

    @SerializedName("posterUrl")
    private String posterUrl;

    @SerializedName("posterUrlPreview")
    private String posterUrlPreview;

    @SerializedName("relationType")
    private String relationType;

    // Конструкторы
    public FilmRelation() {}

    public FilmRelation(int filmId, String nameRu, String nameEn, String nameOriginal,
                        String posterUrl, String posterUrlPreview, String relationType) {
        this.filmId = filmId;
        this.nameRu = nameRu;
        this.nameEn = nameEn;
        this.nameOriginal = nameOriginal;
        this.posterUrl = posterUrl;
        this.posterUrlPreview = posterUrlPreview;
        this.relationType = relationType;
    }

    // Геттеры и сеттеры
    public int getFilmId() {
        return filmId;
    }

    public void setFilmId(int filmId) {
        this.filmId = filmId;
    }

    public String getNameRu() {
        return nameRu;
    }

    public void setNameRu(String nameRu) {
        this.nameRu = nameRu;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameOriginal() {
        return nameOriginal;
    }

    public void setNameOriginal(String nameOriginal) {
        this.nameOriginal = nameOriginal;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getPosterUrlPreview() {
        return posterUrlPreview;
    }

    public void setPosterUrlPreview(String posterUrlPreview) {
        this.posterUrlPreview = posterUrlPreview;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    @Override
    public String toString() {
        return "FilmRelation{" +
                "filmId=" + filmId +
                ", nameRu='" + nameRu + '\'' +
                ", nameEn='" + nameEn + '\'' +
                ", nameOriginal='" + nameOriginal + '\'' +
                ", relationType='" + relationType + '\'' +
                '}';
    }
}