package com.alaka_ala.florafilm.ui.util.api.collapse.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Season {
    public String getPoster() {
        return poster;
    }

    public String getIframeUrl() {
        return iframeUrl;
    }

    public int getSeason() {
        return season;
    }

    public List<Episode> getEpisodes() {
        return episodes;
    }

    private String poster;

    @SerializedName("iframe_url")
    private String iframeUrl;

    private int season;

    private List<Episode> episodes;

}