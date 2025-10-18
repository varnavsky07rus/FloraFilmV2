package com.alaka_ala.florafilm.ui.util.coreMatrix.api.model;

import com.google.gson.annotations.SerializedName;

public class ItemState {

    @SerializedName("id")
    private int id;

    @SerializedName("length")
    private long length;

    @SerializedName("size")
    private long size;

    @SerializedName("priority")
    private int priority;

    @SerializedName("completed")
    private boolean completed;

    // Getters
    public int getId() { return id; }
    public long getLength() { return length; }
    public long getSize() { return size; }
    public int getPriority() { return priority; }
    public boolean isCompleted() { return completed; }
}
