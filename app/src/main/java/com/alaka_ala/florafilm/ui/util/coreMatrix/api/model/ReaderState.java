package com.alaka_ala.florafilm.ui.util.coreMatrix.api.model;

import com.google.gson.annotations.SerializedName;

public class ReaderState {

    @SerializedName("reader")
    private int reader;

    @SerializedName("start")
    private int start;

    @SerializedName("end")
    private int end;

    // Getters
    public int getReader() { return reader; }
    public int getStart() { return start; }
    public int getEnd() { return end; }
}
