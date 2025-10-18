package com.alaka_ala.florafilm.ui.util.coreMatrix.api.model;

import com.google.gson.annotations.SerializedName;

public class SettingsRequest {

    @SerializedName("action")
    private String action;

    @SerializedName("sets")
    private BTSettings sets;

    public SettingsRequest(String action) {
        this.action = action;
    }

    public SettingsRequest(String action, BTSettings sets) {
        this.action = action;
        this.sets = sets;
    }
}
