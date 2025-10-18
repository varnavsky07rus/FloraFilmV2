package com.alaka_ala.florafilm.ui.util.coreMatrix.api.model;

import com.google.gson.annotations.SerializedName;

public class ViewedRequest {

    @SerializedName("action")
    private String action;

    @SerializedName("hash")
    private String hash;

    @SerializedName("file_index")
    private int fileIndex;

    public ViewedRequest(String action, String hash) {
        this.action = action;
        this.hash = hash;
    }

    public ViewedRequest(String action, String hash, int fileIndex) {
        this.action = action;
        this.hash = hash;
        this.fileIndex = fileIndex;
    }
}
