package com.alaka_ala.florafilm.ui.util.coreMatrix.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class CacheState {

    @SerializedName("hash")
    private String hash;

    @SerializedName("capacity")
    private long capacity;

    @SerializedName("filled")
    private long filled;

    @SerializedName("piecesCount")
    private int piecesCount;

    @SerializedName("piecesLength")
    private long piecesLength;

    @SerializedName("torrent")
    private TorrentStatus torrent;

    @SerializedName("readers")
    private List<ReaderState> readers;

    @SerializedName("pieces")
    private Map<String, ItemState> pieces;

    // Getters
    public String getHash() { return hash; }
    public long getCapacity() { return capacity; }
    public long getFilled() { return filled; }
    public int getPiecesCount() { return piecesCount; }
    public long getPiecesLength() { return piecesLength; }
    public TorrentStatus getTorrent() { return torrent; }
    public List<ReaderState> getReaders() { return readers; }
    public Map<String, ItemState> getPieces() { return pieces; }
}
