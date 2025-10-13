package com.alaka_ala.florafilm.core.torrent;

public class TorrentInfo {

    private final String magnetLink;
    private final String hash;
    private final String name;
    private final long totalSize;
    private long downloadedSize;
    private int progress = 0;
    private DownloadStatus status;

    public TorrentInfo(String magnetLink, String hash, String name, long totalSize) {
        this.magnetLink = magnetLink;
        this.hash = hash;
        this.name = name;
        this.totalSize = totalSize;
        this.status = DownloadStatus.QUEUED;
    }

    // getters and setters

    public String getMagnetLink() {
        return magnetLink;
    }

    public String getHash() {
        return hash;
    }

    public String getName() {
        return name;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public long getDownloadedSize() {
        return downloadedSize;
    }

    public void setDownloadedSize(long downloadedSize) {
        this.downloadedSize = downloadedSize;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public DownloadStatus getStatus() {
        return status;
    }

    public void setStatus(DownloadStatus status) {
        this.status = status;
    }

    public enum DownloadStatus {
        QUEUED,
        DOWNLOADING,
        PAUSED,
        COMPLETED,
        ERROR
    }
}
