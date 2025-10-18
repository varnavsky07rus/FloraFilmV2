
package com.alaka_ala.florafilm.ui.util.coreTorrent;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.alaka_ala.florafilm.BuildConfig;
import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.ui.util.coreTorrent.db.TorrentDatabase;
import com.alaka_ala.florafilm.ui.util.coreTorrent.interfaces.UpdateDataListener;
import com.alaka_ala.florafilm.ui.util.coreTorrent.models.Torrent;
import com.alaka_ala.florafilm.ui.util.coreTorrent.models.TorrentFile;
import com.alaka_ala.florafilm.ui.util.coreTorrent.utils.MagnetLinkParser;
import com.frostwire.jlibtorrent.AddTorrentParams;
import com.frostwire.jlibtorrent.AlertListener;
import com.frostwire.jlibtorrent.FileStorage;
import com.frostwire.jlibtorrent.Priority;
import com.frostwire.jlibtorrent.SessionManager;
import com.frostwire.jlibtorrent.TorrentFlags;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.AlertType;
import com.frostwire.jlibtorrent.alerts.TorrentAlert;
import com.frostwire.jlibtorrent.alerts.TorrentErrorAlert;
import com.frostwire.jlibtorrent.swig.remove_flags_t;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.Timer;
import java.util.TimerTask;

public class TorrentSessionService extends Service {

    public static final String ACTION_SERVICE_INITIALIZE = "com.alaka_ala.florafilm.ui.util.coreTorrent.ACTION_SERVICE_INITIALIZE";
    private SessionManager sessionManager;
    private static TorrentSessionService instance;

    private TorrentDatabase db;
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    private static final Map<String, Torrent> torrentsMap = new ConcurrentHashMap<>();
    private static final Map<String, TorrentInfo> torrentInfoMap = new ConcurrentHashMap<>();
    private static final Map<String, UpdateDataListener> listeners = new ConcurrentHashMap<>();

    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "TORRENT_SERVICE_CHANNEL";

    public static TorrentSessionService getInstance() {
        return instance;
    }

    public void addListener(String key, UpdateDataListener cb) {
        if (listeners.containsKey(key)) return;
        listeners.put(key, cb);
        log("Обработчик '" + key + "' добавлен в список.");
        for (Torrent torrent : torrentsMap.values()) {
            cb.onUpdatedTorrent(torrent);
        }
    }

    public void removeListener(String key) {
        listeners.remove(key);
        log("Обработчик '" + key + "' удален из списка.");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        db = TorrentDatabase.getDatabase(this);
        initializeSession();
        loadTorrentsFromCache();
        setupAlertListener();
    }

    private void updateNotification() {
        boolean isDownloading = false;
        for (Torrent torrent : torrentsMap.values()) {
            String status = torrent.getState();
            if (status != null && (status.equalsIgnoreCase("downloading") || status.equalsIgnoreCase("downloading_metadata"))) {
                isDownloading = true;
                break;
            }
        }

        if (isDownloading) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        "Torrent Service",
                        NotificationManager.IMPORTANCE_LOW
                );
                getSystemService(NotificationManager.class).createNotificationChannel(channel);
            }

            Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle("FloraFilm")
                    .setContentText("Идет загрузка...")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .build();

            startForeground(NOTIFICATION_ID, notification);
        } else {
            stopForeground(true);
        }
    }

    private void loadTorrentsFromCache() {
        dbExecutor.execute(() -> {
            List<Torrent> cachedTorrents = db.torrentDao().getAll();
            for (Torrent torrent : cachedTorrents) {
                torrentsMap.put(torrent.getHashBtih(), torrent);

                if (torrent.getBenCode() != null) {
                    try {
                        TorrentInfo ti = new TorrentInfo(torrent.getBenCode());
                        torrentInfoMap.put(torrent.getHashBtih(), ti);
                    } catch (Exception e) {
                        log("Не удалось восстановить торрент из bencode: " + torrent.getName() + " | " + e.getMessage());
                    }
                }
            }
            log(cachedTorrents.size() + " торрентов загружено из кэша.");
        });
    }

    private void initializeSession() {
        if (sessionManager == null) {
            sessionManager = new SessionManager();
        }
        if (!sessionManager.isRunning()) {
            sessionManager.start();
            log("SessionManager запущен.");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_SERVICE_INITIALIZE.equals(intent.getAction())) {
            initializeSession();
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        if (sessionManager != null && sessionManager.isRunning()) {
            sessionManager.stop();
            log("SessionManager остановлен.");
        }
    }

    public Future<List<TorrentFile>> getTorrentFiles(final String magnetLink) {
        return dbExecutor.submit(() -> {
            if (sessionManager == null || !sessionManager.isRunning()) {
                initializeSession();
            }

            try {
                waitForNodesInDHT(sessionManager);
            } catch (InterruptedException e) {
                log("getTorrentFiles was interrupted while waiting for DHT nodes.");
                Thread.currentThread().interrupt();
                return Collections.emptyList();
            }

            byte[] data = sessionManager.fetchMagnet(magnetLink, 30, new File(getCacheDir().getAbsolutePath()));
            if (data == null) {
                log("Failed to fetch metadata from magnet link.");
                return Collections.emptyList();
            }

            TorrentInfo ti = new TorrentInfo(data);
            FileStorage fs = ti.files();
            ArrayList<TorrentFile> files = new ArrayList<>();
            for (int i = 0; i < fs.numFiles(); i++) {
                files.add(new TorrentFile(i, fs.filePath(i), fs.fileSize(i)));
            }
            return files;
        });
    }

    public void startDownloadWithSelectedFiles(int kinopoisk_id, String magnetLink, List<Integer> selectedFileIndexes) {
        if (sessionManager == null || !sessionManager.isRunning()) {
            initializeSession();
        }
        final String btih = MagnetLinkParser.extractBtih(magnetLink);
        if (btih == null || btih.isEmpty()) {
            log("Error: Could not extract BTIH from magnet link.");
            return;
        }

        if (torrentsMap.containsKey(btih)) {
            log("Torrent with hash " + btih + " is already in the session. Ignoring call to startDownloadWithSelectedFiles.");
            return;
        }

        File saveDir = new File(this.getCacheDir(), kinopoisk_id + File.separator + btih);
        if (!saveDir.exists() && !saveDir.mkdirs()) {
            log("Error: Could not create directory " + saveDir.getAbsolutePath());
            return;
        }

        dbExecutor.execute(() -> {
            byte[] data = sessionManager.fetchMagnet(magnetLink, 30, new File(getCacheDir().getAbsolutePath()));
            if (data == null) {
                log("Failed to fetch metadata from magnet link.");
                return;
            }

            TorrentInfo ti = new TorrentInfo(data);
            torrentInfoMap.put(btih, ti);

            AddTorrentParams params = new AddTorrentParams();
            params.torrentInfo(ti);
            params.savePath(saveDir.getAbsolutePath());

            // Set file priorities
            Priority[] prios = new Priority[ti.numFiles()];
            for (int i = 0; i < ti.numFiles(); i++) {
                if (selectedFileIndexes.contains(i)) {
                    prios[i] = Priority.SEVEN;
                } else {
                    prios[i] = Priority.IGNORE;
                }
            }
            params.filePriorities(prios);

            // Save initial torrent state
            Torrent initialTorrent = new Torrent(
                    ti.name(),
                    ti.totalSize(), // This is the total size, UI should calculate selected size if needed
                    magnetLink,
                    btih,
                    saveDir.getAbsolutePath(),
                    0,
                    "Connecting...",
                    0,
                    0,
                    ti.bencode()
            );

            torrentsMap.put(btih, initialTorrent);
            db.torrentDao().upsert(initialTorrent);
            updateListeners(initialTorrent);

            sessionManager.download(ti, saveDir, null, prios, null, null);
            log("Torrent added to session with selected files: " + ti.name());
        });
    }

    /**
     * @deprecated Use {@link #getTorrentFiles(String)} and {@link #startDownloadWithSelectedFiles(int, String, List)} instead.
     */
    @Deprecated
    public void startdl(int kinopoisk_id, String magnetLink) {
        if (sessionManager == null || !sessionManager.isRunning()) {
            initializeSession();
        }
        final String btih = MagnetLinkParser.extractBtih(magnetLink);
        if (btih == null || btih.isEmpty()) {
            log("Ошибка: Не удалось извлечь BTIH из magnet-ссылки.");
            return;
        }

        if (torrentsMap.containsKey(btih)) {
            log("Торрент с хешем " + btih + " уже в сессии. Игнорируем повторный вызов startdl.");
            return;
        }

        File saveDir = new File(this.getCacheDir(), kinopoisk_id + File.separator + btih);
        if (!saveDir.exists() && !saveDir.mkdirs()) {
            log("Ошибка: Не удалось создать директорию " + saveDir.getAbsolutePath());
            return;
        }

        dbExecutor.execute(() -> {
            try {
                waitForNodesInDHT(sessionManager);
            } catch (InterruptedException e) {
                log("Процесс старта загрузки был прерван.");
                Thread.currentThread().interrupt();
            }
            byte[] data = sessionManager.fetchMagnet(magnetLink, 30, new File(getCacheDir().getAbsolutePath()));
            if (data == null) {
                log("Не удалось получить метаданные по magnet-ссылке.");
                return;
            }

            TorrentInfo ti = new TorrentInfo(data);
            torrentInfoMap.put(btih, ti);

            Torrent initialTorrent = new Torrent(
                    ti.name(),
                    ti.totalSize(),
                    magnetLink,
                    btih,
                    saveDir.getAbsolutePath(),
                    0,
                    "Connecting...",
                    0,
                    0,
                    ti.bencode()
            );

            torrentsMap.put(btih, initialTorrent);
            db.torrentDao().upsert(initialTorrent);
            updateListeners(initialTorrent);

            sessionManager.download(ti, saveDir);
            log("Торрент добавлен в сессию: " + ti.name());
        });
    }

    public void pauseTorrent(String btih) {
        TorrentInfo ti = torrentInfoMap.get(btih);
        if (ti == null) {
            log("pauseTorrent: TorrentInfo не найден для " + btih);
            return;
        }
        TorrentHandle th = sessionManager.find(ti.infoHashV1());
        if (th != null && th.isValid() && !(th.flags().and_(TorrentFlags.PAUSED)).nonZero()) {
            th.unsetFlags(TorrentFlags.AUTO_MANAGED);
            th.pause();
            log("Команда: Поставить на паузу торрент " + btih + ". Авто-управление ОТКЛЮЧЕНО.");
        }
    }

    public void resumeTorrent(String btih) {
        if (sessionManager == null || !sessionManager.isRunning()) {
            initializeSession();
        }

        TorrentInfo ti = torrentInfoMap.get(btih);
        if (ti == null) {
            log("resumeTorrent: TorrentInfo не найден для " + btih);
            return;
        }

        TorrentHandle th = sessionManager.find(ti.infoHashV1());
        if (th != null && th.isValid()) {
            // Торрент уже в сессии, просто возобновляем
            if ((th.flags().and_(TorrentFlags.PAUSED)).nonZero()) {
                th.setFlags(TorrentFlags.AUTO_MANAGED);
                th.resume();
                log("Команда: Возобновить торрент " + btih + ". Авто-управление ВКЛЮЧЕНО.");
            }
        } else {
            // Торрента нет в сессии, добавляем его
            Torrent cachedTorrent = torrentsMap.get(btih);
            if (cachedTorrent != null && cachedTorrent.getPathFile() != null) {
                File saveDir = new File(cachedTorrent.getPathFile());
                // This part is tricky. Resuming with selected files needs the priorities again.
                // The current implementation of resume will download all files.
                // This needs to be addressed if selective file resume is needed.
                // For now, let's assume resume downloads all files of a partially downloaded torrent.
                sessionManager.download(ti, saveDir);
                log("Торрент " + btih + " добавлен в сессию и возобновлен.");
            } else {
                log("resumeTorrent: Torrent не найден в кэше или путь к файлу не указан для " + btih);
            }
        }
    }

    public void removeTorrent(String btih, boolean deleteFiles) {
        TorrentInfo ti = torrentInfoMap.get(btih);
        if (ti == null) return;
        TorrentHandle th = sessionManager.find(ti.infoHashV1());
        if (th != null && th.isValid()) {
            sessionManager.remove(th, remove_flags_t.from_int(deleteFiles ? 1 : 0));
            log("Торрент " + btih + " удален из сессии.");
        }
        torrentsMap.remove(btih);
        torrentInfoMap.remove(btih);
        dbExecutor.execute(() -> db.torrentDao().deleteByHash(btih));
        updateNotification();
    }

    private void setupAlertListener() {
        AlertListener listener = new AlertListener() {
            @Override
            public int[] types() {
                return new int[]{
                        AlertType.ADD_TORRENT.swig(),
                        AlertType.TORRENT_FINISHED.swig(),
                        AlertType.TORRENT_PAUSED.swig(),
                        AlertType.TORRENT_RESUMED.swig(),
                        AlertType.TORRENT_ERROR.swig(),
                        AlertType.STATE_UPDATE.swig(),
                        AlertType.METADATA_RECEIVED.swig()
                };
            }

            @Override
            public void alert(Alert<?> alert) {
                if (alert.type().equals(AlertType.STATE_UPDATE)) {
                    for (TorrentInfo ti : torrentInfoMap.values()) {
                        TorrentHandle th = sessionManager.find(ti.infoHashV1());
                        updateAndNotify(th);
                    }
                    return;
                }

                if (!(alert instanceof TorrentAlert)) {
                    log("Системный Alert: " + alert.message());
                    return;
                }

                TorrentAlert<?> torrentAlert = (TorrentAlert<?>) alert;
                TorrentHandle th = torrentAlert.handle();

                if (th == null || !th.isValid()) {
                    return;
                }

                switch (alert.type()) {
                    case ADD_TORRENT:
                        log("Алерт: Торрент добавлен в сессию: " + th.name());
                        break;
                    case TORRENT_FINISHED:
                        log("Алерт: Торрент завершен: " + th.name());
                        th.pause();
                        break;
                    case TORRENT_ERROR:
                        log("Алерт: Ошибка торрента: " + ((TorrentErrorAlert) torrentAlert).error().message());
                        break;
                }
                updateAndNotify(th);
            }
        };

        sessionManager.addListener(listener);
        sessionManager.postTorrentUpdates();
        log("AlertListener установлен, обновления статуса запущены.");
    }

    private void updateAndNotify(TorrentHandle th) {
        if (th == null || !th.isValid()) {
            return;
        }

        String btih = th.infoHash().toHex();
        TorrentInfo ti = torrentInfoMap.get(btih);

        if (ti == null) {
            ti = th.torrentFile();
            if (ti != null && ti.isValid()) {
                torrentInfoMap.put(btih, ti);
            } else {
                return;
            }
        }

        String status = th.status().state().name();
        if (th.flags().and_(TorrentFlags.PAUSED).nonZero()) {
            status = "PAUSED";
        }

        Torrent newTorrent = new Torrent(
                ti.name(),
                ti.totalSize(),
                ti.makeMagnetUri(),
                btih,
                th.savePath(),
                (int) (th.status().progress() * 100),
                status,
                th.status().downloadRate(),
                th.status().uploadRate(),
                ti.bencode()
        );

        Torrent oldTorrent = torrentsMap.get(btih);

        if (!newTorrent.equals(oldTorrent)) {
            torrentsMap.put(btih, newTorrent);
            updateListeners(newTorrent);
            dbExecutor.execute(() -> db.torrentDao().upsert(newTorrent));
            log("Updating UI and Cache for " + newTorrent.getName() + " | Progress: " + newTorrent.getProgress() + "%");
            updateNotification();
        }
    }

    private void updateListeners(Torrent torrent) {
        if (torrent == null) {
            if (BuildConfig.DEBUG) log("Обновление невозможно. Передаваемый параметр Torrent равен NULL");
            return;
        }
        for (UpdateDataListener l : listeners.values()) {
            l.onUpdatedTorrent(torrent);
        }
    }

    private static void waitForNodesInDHT(final SessionManager s) throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(1);
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                long nodes = s.stats().dhtNodes();
                if (nodes >= 10) {
                    System.out.println("DHT contains " + nodes + " nodes");
                    signal.countDown();
                    timer.cancel();
                }
            }
        }, 0, 1000);

        System.out.println("Waiting for nodes in DHT (10 seconds)...");
        boolean r = signal.await(10, TimeUnit.SECONDS);
        if (!r) {
            System.out.println("DHT bootstrap timeout");
        }
    }

    private static void log(String s) {
        Log.i("TorrentSessionService", s);
    }
}
