package com.alaka_ala.florafilm.ui.util.coreMatrix.api.model;

import com.google.gson.annotations.SerializedName;

public class TorrentFileStat {

    @SerializedName("id")
    private int id;

    @SerializedName("path")
    private String path;

    @SerializedName("length")
    private long length;

    // Getters
    public int getId() { return id; }
    public String getPath() { return path; }
    public long getLength() { return length; }
}
