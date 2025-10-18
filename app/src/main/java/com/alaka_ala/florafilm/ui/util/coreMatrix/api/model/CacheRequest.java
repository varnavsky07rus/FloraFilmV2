package com.alaka_ala.florafilm.ui.util.coreMatrix.api.model;

import com.google.gson.annotations.SerializedName;

public class CacheRequest {

    @SerializedName("action")
    private String action;

    @SerializedName("hash")
    private String hash;

    public CacheRequest(String action, String hash) {
        this.action = action;
        this.hash = hash;
    }
}
