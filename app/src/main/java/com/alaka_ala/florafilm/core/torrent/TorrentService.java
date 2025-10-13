package com.alaka_ala.florafilm.core.torrent;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.alaka_ala.florafilm.BuildConfig;
import com.alaka_ala.florafilm.core.torrent.TorrentInfo.DownloadStatus;

import org.libtorrent4j.AddTorrentParams;
import org.libtorrent4j.AlertListener;
import org.libtorrent4j.SessionManager;
import org.libtorrent4j.SessionParams;
import org.libtorrent4j.SettingsPack;
import org.libtorrent4j.TorrentFlags;
import org.libtorrent4j.TorrentHandle;
import org.libtorrent4j.alerts.AddTorrentAlert;
import org.libtorrent4j.alerts.Alert;
import org.libtorrent4j.alerts.AlertType;
import org.libtorrent4j.alerts.BlockFinishedAlert;
import org.libtorrent4j.alerts.TorrentFinishedAlert;
import org.libtorrent4j.swig.torrent_flags_t;

import java.io.File;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TorrentService extends Service {
    public static final String ACTION_ADD_TORRENT = "com.alaka_ala.florafilm.ADD_TORRENT";
    public static final String EXTRA_MAGNET_LINK = "com.alaka_ala.florafilm.MAGNET_LINK";
    public static final String EXTRA_KINOPOISK_ID = "com.alaka_ala.florafilm.KINOPOISK_ID";

    private static final String TAG = "TorrentService";

    private final Queue<String> downloadQueue = new ConcurrentLinkedQueue<>();
    private boolean isDownloading = false;

    private SessionManager sessionManager;
    private TorrentManager torrentManager;

    private String magnetLink;

    @Override
    public void onCreate() {
        super.onCreate();
        torrentManager = TorrentManager.getInstance();
        sessionManager = new SessionManager();

        sessionManager.addListener(new AlertListener() {
            @Override
            public int[] types() {
                return new int[]{
                        AlertType.ADD_TORRENT.swig(),
                        AlertType.BLOCK_FINISHED.swig(),
                        AlertType.TORRENT_FINISHED.swig()
                };
            }

            @Override
            public void alert(Alert<?> alert) {
                switch (alert.type()) {
                    case ADD_TORRENT: {
                        AddTorrentAlert ata = (AddTorrentAlert) alert;
                        TorrentHandle th = ata.handle();
                        String hash = DigestUtils.createMd5Digest(magnetLink);

                        // This alert is fired when the metadata has been received and the torrent is being added.
                        // We can now get the full torrent info, like total size.
                        if (th.torrentFile() != null) {
                            long totalSize = th.torrentFile().totalSize();
                            Map<String, TorrentInfo> currentTorrents = torrentManager.getTorrentsLiveData().getValue();
                            if (currentTorrents != null) {
                                TorrentInfo info = currentTorrents.get(hash);
                                if (info != null) {
                                    info.setDownloadedSize(totalSize); // Correctly set the total size
                                    info.setStatus(DownloadStatus.DOWNLOADING);
                                    torrentManager.updateTorrentInfo(info);
                                    if (BuildConfig.DEBUG) Log.d(TAG, "Торрент добавлен");
                                }
                            }
                        }
                        break;
                    }
                    case BLOCK_FINISHED: {
                        BlockFinishedAlert bfa = (BlockFinishedAlert) alert;
                        TorrentHandle th = bfa.handle();
                        String hash = DigestUtils.createMd5Digest(magnetLink);

                        Map<String, TorrentInfo> currentTorrents = torrentManager.getTorrentsLiveData().getValue();
                        if (currentTorrents != null) {
                            TorrentInfo info = currentTorrents.get(hash);
                            if (info != null && info.getStatus() == DownloadStatus.DOWNLOADING) {
                                long downloadedSize = th.status().totalDone();
                                int progress = (int) (th.status().progress() * 100);
                                info.setDownloadedSize(downloadedSize);
                                info.setProgress(progress);
                                torrentManager.updateTorrentInfo(info);
                                if (BuildConfig.DEBUG) Log.d(TAG, "Прогресс загрузки: " + progress + "%" + " (" + downloadedSize + "/" + info.getTotalSize() + ")");
                            }
                        }
                        break;
                    }
                    case TORRENT_FINISHED: {
                        TorrentFinishedAlert tfa = (TorrentFinishedAlert) alert;
                        String hash = DigestUtils.createMd5Digest(magnetLink);

                        Map<String, TorrentInfo> currentTorrents = torrentManager.getTorrentsLiveData().getValue();
                        if (currentTorrents != null) {
                            TorrentInfo info = currentTorrents.get(hash);
                            if (info != null) {
                                info.setStatus(DownloadStatus.COMPLETED);
                                info.setProgress(100);
                                torrentManager.updateTorrentInfo(info);
                                if (BuildConfig.DEBUG) Log.d(TAG, "Торрент \"" + info.getName() + "\" загружен");
                            }
                        }
                        isDownloading = false;
                        startNextDownload();
                        break;
                    }
                }
            }
        });

        SettingsPack sp = new SettingsPack();
        sp.setEnableDht(false);
        sessionManager.start(new SessionParams());
    }

    private int kinopoisk_id;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_ADD_TORRENT.equals(intent.getAction())) {
            magnetLink = intent.getStringExtra(EXTRA_MAGNET_LINK);
            kinopoisk_id = intent.getIntExtra(EXTRA_KINOPOISK_ID, 0);
            if (magnetLink != null && !downloadQueue.contains(magnetLink)) {
                downloadQueue.add(magnetLink);
                if (!isDownloading) {
                    startNextDownload();
                }
            }
        }
        return START_STICKY;
    }

    private void startNextDownload() {
        if (isDownloading || downloadQueue.isEmpty()) {
            return;
        }

        isDownloading = true;
        String magnetLink = downloadQueue.poll();

        if (magnetLink == null) {
            isDownloading = false;
            return;
        }

        try {
            // We need the hash to create a unique directory for the download.
            AddTorrentParams params = AddTorrentParams.parseMagnetUri(magnetLink);
            String hash = DigestUtils.createMd5Digest(magnetLink);

            File baseDir = getCacheDir();
            if (baseDir == null) {
                isDownloading = false;
                startNextDownload(); // Try next in queue
                return;
            }

            // Create a unique directory for this torrent based on its hash
            File saveDir = getCacheDirTorrent(kinopoisk_id, magnetLink);
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }

            // Use the correct download method signature
            sessionManager.download(magnetLink, saveDir, TorrentFlags.AUTO_MANAGED);

        } catch (IllegalArgumentException e) {
            // The magnet link was invalid
            e.printStackTrace();
            isDownloading = false;
            startNextDownload(); // Try the next one
        }
    }

    @Override
    public void onDestroy() {
        sessionManager.stop();
        super.onDestroy();
    }

    private File getCacheDirTorrent(int kinopoisk_id, String magnet) {
        File baseDir = getCacheDir();
        File folderKP = new File(baseDir, "/" + kinopoisk_id + "/");
        if (!folderKP.exists()) {
            folderKP.mkdirs();
        }
        return new File(baseDir, "/" + kinopoisk_id + "/" + DigestUtils.createMd5Digest(magnet));
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
