package com.alaka_ala.florafilm.ui.util.torrents;

import android.content.Context;

import com.frostwire.jlibtorrent.*;

import java.io.File;

public class TorrentStreamer {
    private SessionManager session;
    private TorrentHandle torrentHandle;

    private Context context;

    public TorrentStreamer(Context context) {
        this.context = context;
    }


    public void initTorrentSession() {
        session = new SessionManager();
        session.start();
    }

    public void downloadFromMagnet(String magnetLink) {
        // Добавляем магнет-ссылку в сессию
        byte[] torrnetByte = session.fetchMagnet(magnetLink, 100);
        TorrentInfo torrentInfo = TorrentInfo.bdecode(torrnetByte);
        session.download(torrentInfo, context.getExternalCacheDir());

        torrentHandle = new TorrentHandle(session.find(torrentInfo.infoHash()).swig());
        torrentHandle.piecePriority(0, Priority.SEVEN); // Первые куски в высоком приоритете
        torrentHandle.setSequentialDownload(true); // Скачивать последовательно


    }
}