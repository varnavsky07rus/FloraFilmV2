package com.alaka_ala.florafilm.ui.util.coreMatrix.api.model;

import com.google.gson.annotations.SerializedName;

public enum TorrentStat {
    @SerializedName("0")
    ADDED(0),

    @SerializedName("1")
    GETTING_INFO(1),

    @SerializedName("2")
    PRELOAD(2),

    @SerializedName("3")
    WORKING(3),

    @SerializedName("4")
    CLOSED(4),

    @SerializedName("5")
    IN_DB(5);

    private final int value;

    TorrentStat(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
