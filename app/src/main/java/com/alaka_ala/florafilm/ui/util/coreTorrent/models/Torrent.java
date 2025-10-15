package com.alaka_ala.florafilm.ui.util.coreTorrent.models;

import java.io.Serializable;

public class Torrent implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private long size;
    private final String magnet;
    private final String hashBtih;
    private final String pathFile;
    private int progress;
    private String state;
    private final byte[] benCode;

    public Torrent(String name, long size, String magnet, String hashBtih, String pathFile, int progress, String state, byte[] benCode) {
        this.magnet = magnet;
        this.hashBtih = hashBtih;
        this.progress = progress;
        this.state = state;
        this.benCode = benCode;
        this.pathFile = pathFile;
        this.size = size;
        this.name = name;
    }

    public String getPathFile() {
        return pathFile;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public String getMagnet() {
        return magnet;
    }

    public String getHashBtih() {
        return hashBtih;
    }

    public int getProgress() {
        return progress;
    }

    public String getState() {
        return state;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setState(String state) {
        this.state = state;
    }

    public byte[] getBenCode() {
        return benCode;
    }

}
