package com.alaka_ala.florafilm.ui.util.coreMatrix.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TorrentDetails {

    @SerializedName("audioQuality")
    private int audioQuality;

    @SerializedName("categories")
    private String categories;

    @SerializedName("createDate")
    private String createDate;

    @SerializedName("hash")
    private String hash;

    @SerializedName("imdbid")
    private String imdbid;

    @SerializedName("link")
    private String link;

    @SerializedName("magnet")
    private String magnet;

    @SerializedName("name")
    private String name;

    @SerializedName("names")
    private List<String> names;

    @SerializedName("peer")
    private int peer;

    @SerializedName("seed")
    private int seed;

    @SerializedName("size")
    private String size;

    @SerializedName("title")
    private String title;

    @SerializedName("tracker")
    private String tracker;

    @SerializedName("videoQuality")
    private int videoQuality;

    @SerializedName("year")
    private int year;

    // Getters
    public int getAudioQuality() { return audioQuality; }
    public String getCategories() { return categories; }
    public String getCreateDate() { return createDate; }
    public String getHash() { return hash; }
    public String getImdbid() { return imdbid; }
    public String getLink() { return link; }
    public String getMagnet() { return magnet; }
    public String getName() { return name; }
    public List<String> getNames() { return names; }
    public int getPeer() { return peer; }
    public int getSeed() { return seed; }
    public String getSize() { return size; }
    public String getTitle() { return title; }
    public String getTracker() { return tracker; }
    public int getVideoQuality() { return videoQuality; }
    public int getYear() { return year; }
}
