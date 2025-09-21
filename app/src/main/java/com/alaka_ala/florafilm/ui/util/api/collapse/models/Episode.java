package com.alaka_ala.florafilm.ui.util.api.collapse.models;

import com.google.gson.annotations.SerializedName;

public class Episode {
    public int getEpisode() {
        return episode;
    }

    public String getIframeUrl() {
        return iframeUrl;
    }

    private int episode;

    @SerializedName("iframe_url")
    private String iframeUrl;
}
