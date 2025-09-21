package com.alaka_ala.florafilm.ui.util.api.collapse.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class Result {
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getAge() {
        return age;
    }

    public String getQuality() {
        return quality;
    }

    public String getOriginName() {
        return originName;
    }

    public int getYear() {
        return year;
    }

    public String getActivateTime() {
        return activateTime;
    }

    public String getImdb() {
        return imdb;
    }

    public String getImdbId() {
        return imdbId;
    }

    public String getKinopoisk() {
        return kinopoisk;
    }

    public String getKinopoiskId() {
        return kinopoiskId;
    }

    public String getWorldArt() {
        return worldArt;
    }

    public String getWorldArtId() {
        return worldArtId;
    }

    public String getIframeUrl() {
        return iframeUrl;
    }

    public String getTrailer() {
        return trailer;
    }

    public String getPoster() {
        return poster;
    }

    public Map<String, String> getGenre() {
        return genre;
    }

    public Map<String, String> getCountry() {
        return country;
    }

    public Map<String, String> getCollection() {
        return collection;
    }

    public String getSerialStatus() {
        return serialStatus;
    }

    public List<Season> getSeasons() {
        return seasons;
    }

    private int id;
    private String name;
    private String type;
    private String age;
    private String quality;

    @SerializedName("origin_name")
    private String originName;

    private int year;

    @SerializedName("activate_time")
    private String activateTime;

    private String imdb;

    @SerializedName("imdb_id")
    private String imdbId;

    private String kinopoisk;

    @SerializedName("kinopoisk_id")
    private String kinopoiskId;

    @SerializedName("world_art")
    private String worldArt;

    @SerializedName("world_art_id")
    private String worldArtId;

    @SerializedName("iframe_url")
    private String iframeUrl;

    private String trailer;
    private String poster;

    private Map<String, String> genre;
    private Map<String, String> country;
    private Map<String, String> collection;

    @SerializedName("serial_status")
    private String serialStatus;  // может быть null

    private List<Season> seasons;  // может быть null
}
