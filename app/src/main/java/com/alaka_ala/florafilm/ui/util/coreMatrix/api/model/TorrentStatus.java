package com.alaka_ala.florafilm.ui.util.coreMatrix.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TorrentStatus {

    @SerializedName("hash")
    private String hash;

    @SerializedName("name")
    private String name;

    @SerializedName("title")
    private String title;

    @SerializedName("poster")
    private String poster;

    @SerializedName("category")
    private String category;

    @SerializedName("data")
    private String data;

    @SerializedName("stat")
    private TorrentStat stat;

    @SerializedName("stat_string")
    private String statString;

    @SerializedName("torrent_size")
    private long torrentSize;

    @SerializedName("loaded_size")
    private long loadedSize;

    @SerializedName("preload_size")
    private long preloadSize;

    @SerializedName("preloaded_bytes")
    private long preloadedBytes;

    @SerializedName("download_speed")
    private double downloadSpeed;

    @SerializedName("upload_speed")
    private double uploadSpeed;

    @SerializedName("total_peers")
    private int totalPeers;

    @SerializedName("pending_peers")
    private int pendingPeers;

    @SerializedName("active_peers")
    private int activePeers;

    @SerializedName("connected_seeders")
    private int connectedSeeders;

    @SerializedName("half_open_peers")
    private int halfOpenPeers;

    @SerializedName("timestamp")
    private long timestamp;

    @SerializedName("file_stats")
    private List<TorrentFileStat> fileStats;

    // Getters
    public String getHash() { return hash; }
    public String getName() { return name; }
    public String getTitle() { return title; }
    public String getPoster() { return poster; }
    public String getCategory() { return category; }
    public String getData() { return data; }
    public TorrentStat getStat() { return stat; }
    public String getStatString() { return statString; }
    public long getTorrentSize() { return torrentSize; }
    public long getLoadedSize() { return loadedSize; }
    public long getPreloadSize() { return preloadSize; }
    public long getPreloadedBytes() { return preloadedBytes; }
    public double getDownloadSpeed() { return downloadSpeed; }
    public double getUploadSpeed() { return uploadSpeed; }
    public int getTotalPeers() { return totalPeers; }
    public int getPendingPeers() { return pendingPeers; }
    public int getActivePeers() { return activePeers; }
    public int getConnectedSeeders() { return connectedSeeders; }
    public int getHalfOpenPeers() { return halfOpenPeers; }
    public long getTimestamp() { return timestamp; }
    public List<TorrentFileStat> getFileStats() { return fileStats; }
}
