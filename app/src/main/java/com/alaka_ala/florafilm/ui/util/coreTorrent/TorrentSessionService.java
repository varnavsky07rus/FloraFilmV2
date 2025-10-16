package com.alaka_ala.florafilm.ui.util.coreTorrent;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.alaka_ala.florafilm.BuildConfig;
import com.alaka_ala.florafilm.ui.util.coreTorrent.db.TorrentDatabase;
import com.alaka_ala.florafilm.ui.util.coreTorrent.interfaces.UpdateDataListener;
import com.alaka_ala.florafilm.ui.util.coreTorrent.models.Torrent;
import com.alaka_ala.florafilm.ui.util.coreTorrent.utils.MagnetLinkParser;
import com.frostwire.jlibtorrent.AlertListener;
import com.frostwire.jlibtorrent.SessionManager;
import com.frostwire.jlibtorrent.TorrentFlags; // <-- Важный импорт
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.AlertType;
import com.frostwire.jlibtorrent.alerts.TorrentAlert;
import com.frostwire.jlibtorrent.alerts.TorrentErrorAlert;
import com.frostwire.jlibtorrent.swig.remove_flags_t;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private void loadTorrentsFromCache() {
        dbExecutor.execute(() -> {
            List<Torrent> cachedTorrents = db.torrentDao().getAll();
            for (Torrent torrent : cachedTorrents) {
                torrentsMap.put(torrent.getHashBtih(), torrent);

                if (torrent.getProgress() < 100 && torrent.getBenCode() != null) {
                    try {
                        TorrentInfo ti = new TorrentInfo(torrent.getBenCode());
                        torrentInfoMap.put(torrent.getHashBtih(), ti);
                        File saveDir = new File(torrent.getPathFile());
                        // Добавляем торрент в сессию, но НЕ возобновляем его автоматически.
                        // Он будет добавлен в состоянии "пауза".
                        sessionManager.download(ti, saveDir, null, null, null, TorrentFlags.PAUSED);
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
        if (sessionManager != null && sessionManager.isRunning()) {
            sessionManager.stop();
            log("SessionManager остановлен.");
        }
    }

    public void startdl(int kinopoisk_id, String magnetLink) {
        final String btih = MagnetLinkParser.extractBtih(magnetLink);
        if (btih == null || btih.isEmpty()) {
            log("Ошибка: Не удалось извлечь BTIH из magnet-ссылки.");
            return;
        }

        if (torrentsMap.containsKey(btih)) {
            log("Торрент с хешем " + btih + " уже в сессии. Возобновляем.");
            resumeTorrent(btih); // Явно возобновляем, если он уже есть и на паузе
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

                byte[] data = sessionManager.fetchMagnet(magnetLink, 30, new File("/tmp"));
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

                // Добавляем торрент в сессию. Он начнет скачиваться автоматически.
                sessionManager.download(ti, saveDir);
                log("Торрент добавлен в сессию: " + ti.name());

            } catch (InterruptedException e) {
                log("Процесс старта загрузки был прерван.");
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * Ставит торрент на паузу.
     * Использует надежный поиск по хешу и правильную проверку флага.
     */
    public void pauseTorrent(String btih) {
        TorrentInfo ti = torrentInfoMap.get(btih);
        TorrentHandle th = sessionManager.find(ti.infoHashV1());
        // Проверяем, что торрент существует и НЕ находится на паузе
        if (th != null && th.isValid() && !(th.flags().and_(TorrentFlags.PAUSED)).nonZero()) {
            th.pause();
            log("Команда: Поставить на паузу торрент " + btih);
        }
    }

    /**
     * Возобновляет торрент.
     * Использует надежный поиск по хешу и правильную проверку флага.
     */
    public void resumeTorrent(String btih) {
        TorrentInfo ti = torrentInfoMap.get(btih);
        TorrentHandle th = sessionManager.find(ti.infoHashV1());
        // Проверяем, что торрент существует и НАХОДИТСЯ на паузе
        if (th != null && th.isValid() && (th.flags().and_(TorrentFlags.PAUSED)).nonZero()) {
            th.resume();
            log("Команда: Возобновить торрент " + btih);
        }
    }

    /**
     * Удаляет торрент, используя встроенный механизм jlibtorrent.
     */
    public void removeTorrent(String btih, boolean deleteFiles) {
        TorrentInfo ti = torrentInfoMap.get(btih);
        TorrentHandle th = sessionManager.find(ti.infoHashV1());
        if (th != null && th.isValid()) {
            sessionManager.remove(th, remove_flags_t.from_int(deleteFiles ? 1 : 0));
            log("Торрент " + btih + " удален из сессии.");
        }
        // Удаляем из наших карт и базы данных
        torrentsMap.remove(btih);
        torrentInfoMap.remove(btih);
        dbExecutor.execute(() -> db.torrentDao().deleteByHash(btih));

        // Уведомляем слушателей, что торрент удален (отправляем null или специальный флаг)
        // Простой способ - заставить адаптер перечитать данные из БД.
        // Более сложный - передать событие удаления. Пока оставим так.
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
                        // УБРАЛИ th.resume() ОТСЮДА! ЭТО БЫЛА ГЛАВНАЯ ПРОБЛЕМА.
                        break;
                    case TORRENT_FINISHED:
                        log("Алерт: Торрент завершен: " + th.name());
                        th.pause(); // Ставим на паузу, чтобы остановить раздачу
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

        Torrent newTorrent = new Torrent(
                ti.name(),
                ti.totalSize(),
                ti.makeMagnetUri(),
                btih,
                th.savePath(),
                (int) (th.status().progress() * 100),
                th.status().state().name(),
                th.status().downloadRate(),
                th.status().uploadRate(),
                ti.bencode()
        );

        Torrent oldTorrent = torrentsMap.get(btih);

        if (oldTorrent == null || !newTorrent.equals(oldTorrent)) {
            torrentsMap.put(btih, newTorrent);
            updateListeners(newTorrent);
            dbExecutor.execute(() -> db.torrentDao().upsert(newTorrent));
            log("Updating UI and Cache for " + newTorrent.getName() + " | Progress: " + newTorrent.getProgress() + "%");
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