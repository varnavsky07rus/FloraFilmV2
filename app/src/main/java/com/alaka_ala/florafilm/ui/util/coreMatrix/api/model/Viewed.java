package com.alaka_ala.florafilm.ui.util.coreMatrix.api.model;

import com.google.gson.annotations.SerializedName;

public class Viewed {

    @SerializedName("hash")
    private String hash;

    @SerializedName("file_index")
    private int fileIndex;

    // Getters
    public String getHash() { return hash; }
    public int getFileIndex() { return fileIndex; }
}
