package com.alaka_ala.florafilm.core.torrent;

public interface TorrentListener {
    void onTorrentAdded(TorrentInfo torrentInfo);
    void onTorrentError(String torrentId, String error);
    void onTorrentStatusChanged(String torrentId, TorrentInfo.DownloadStatus status);
    void onTorrentProgress(String torrentId, int progress);
    void onTorrentFinished(String torrentId);
}
