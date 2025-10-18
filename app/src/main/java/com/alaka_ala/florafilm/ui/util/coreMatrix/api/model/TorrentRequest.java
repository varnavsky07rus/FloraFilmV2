package com.alaka_ala.florafilm.ui.util.coreMatrix.api.model;

import com.google.gson.annotations.SerializedName;

public class TorrentRequest {

    @SerializedName("action")
    private String action;

    @SerializedName("hash")
    private String hash;

    @SerializedName("link")
    private String link;

    @SerializedName("title")
    private String title;

    @SerializedName("poster")
    private String poster;

    @SerializedName("category")
    private String category;

    @SerializedName("data")
    private String data;

    @SerializedName("save_to_db")
    private boolean saveToDb;

    public TorrentRequest(String action) {
        this.action = action;
    }

    public TorrentRequest(String action, String hash) {
        this.action = action;
        this.hash = hash;
    }

    public TorrentRequest(String action, String link, String title, String poster, String category, String data, boolean saveToDb) {
        this.action = action;
        this.link = link;
        this.title = title;
        this.poster = poster;
        this.category = category;
        this.data = data;
        this.saveToDb = saveToDb;
    }
}