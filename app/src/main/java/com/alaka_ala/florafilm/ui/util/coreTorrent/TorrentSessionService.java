package com.alaka_ala.florafilm.ui.util.coreTorrent;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alaka_ala.florafilm.BuildConfig;
import com.alaka_ala.florafilm.ui.util.coreTorrent.interfaces.UpdateDataListener;
import com.alaka_ala.florafilm.ui.util.coreTorrent.models.Torrent;
import com.alaka_ala.florafilm.ui.util.coreTorrent.utils.MagnetLinkParser;
import com.frostwire.jlibtorrent.AlertListener;
import com.frostwire.jlibtorrent.SessionManager;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.AlertType;
import com.frostwire.jlibtorrent.alerts.TorrentAlert;
import com.frostwire.jlibtorrent.alerts.TorrentErrorAlert;
import com.frostwire.jlibtorrent.alerts.TorrentFinishedAlert;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.Timer;
import java.util.TimerTask;

public class TorrentSessionService extends Service {

    public static final String ACTION_SERVICE_INITIALIZE = "com.alaka_ala.florafilm.ui.util.coreTorrent.ACTION_SERVICE_INITIALIZE";
    private SessionManager sessionManager;
    private static TorrentSessionService instance;

    // Используем ConcurrentHashMap для потокобезопасности
    private static final Map<String, Torrent> torrentsMap = new ConcurrentHashMap<>();
    private static final Map<String, TorrentInfo> torrentInfoMap = new ConcurrentHashMap<>();
    private static final Map<String, UpdateDataListener> listeners = new ConcurrentHashMap<>();

    public static TorrentSessionService getInstance() {
        return instance;
    }

    public void addListener(String key, UpdateDataListener cb) {
        // Ваша текущая реализация addListener
        if (listeners.containsKey(key)) return;
        listeners.put(key, cb);
        log("Обработчик '" + key + "' добавлен в список.");
        for (Torrent torrent : torrentsMap.values()) {
            cb.onUpdatedTorrent(torrent);
        }
    }

    // ДОБАВЬТЕ ЭТОТ МЕТОД
    public void removeListener(String key) {
        listeners.remove(key);
        log("Обработчик '" + key + "' удален из списка.");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initializeSession();
        // Устанавливаем ЕДИНЫЙ слушатель для сессии при создании сервиса
        setupAlertListener();
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
        return START_STICKY; // Сервис будет перезапущен, если система его убьет
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
            log("Торрент с хешем " + btih + " уже в сессии.");
            // Можно добавить логику для возобновления, если он на паузе
            TorrentHandle th = sessionManager.find(torrentInfoMap.get(btih).infoHashV2());
            if (th != null && th.isValid() && th.status().isFinished()) {
                th.resume();
            }
            return;
        }

        File saveDir = new File(this.getCacheDir(), kinopoisk_id + File.separator + btih);
        if (!saveDir.exists() && !saveDir.mkdirs()) {
            log("Ошибка: Не удалось создать директорию " + saveDir.getAbsolutePath());
            return;
        }

        try {
            waitForNodesInDHT(sessionManager);

            byte[] data = sessionManager.fetchMagnet(magnetLink, 30, new File("/tmp"));
            if (data == null) {
                log("Не удалось получить метаданные по magnet-ссылке. Возможно, ссылка недействительна или нет сидов.");
                // Здесь можно уведомить UI об ошибке
                return;
            }

            TorrentInfo ti = new TorrentInfo(data);
            torrentInfoMap.put(btih, ti); // Сохраняем информацию о торренте

            // Создаем первоначальный объект Torrent со статусом "Добавление"
            Torrent initialTorrent = new Torrent(
                    ti.name(),
                    ti.totalSize(),
                    magnetLink,
                    btih,
                    saveDir.getAbsolutePath(),
                    0,
                    "Connecting...", // Начальный статус
                    0, // Начальная скорость загрузки
                    0, // Начальная скорость отдачи
                    ti.bencode()
            );

            torrentsMap.put(btih, initialTorrent);
            updateListeners(initialTorrent); // Сразу уведомляем UI, что торрент добавлен

            sessionManager.download(ti, saveDir);
            log("Торрент добавлен в сессию: " + ti.name());

        } catch (InterruptedException e) {
            log("Процесс старта загрузки был прерван.");
            Thread.currentThread().interrupt();
        }
    }

    private void setupAlertListener() {
        AlertListener listener = new AlertListener() {
            @Override
            public int[] types() {
                // Слушаем только ключевые события и периодические обновления
                return new int[]{
                        AlertType.ADD_TORRENT.swig(),
                        AlertType.TORRENT_FINISHED.swig(),
                        AlertType.TORRENT_PAUSED.swig(),
                        AlertType.TORRENT_RESUMED.swig(),
                        AlertType.TORRENT_ERROR.swig(),
                        AlertType.STATE_UPDATE.swig(), // Для обновления прогресса
                        AlertType.METADATA_RECEIVED.swig()
                };
            }

            @Override
            public void alert(Alert<?> alert) {
                // STATE_UPDATE не является подклассом TorrentAlert, обрабатываем его отдельно
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

                // Обрабатываем конкретные события
                switch (alert.type()) {
                    case ADD_TORRENT:
                        log("Торрент добавлен: " + th.name());
                        th.resume();
                        break;
                    case TORRENT_FINISHED:
                        log("Торрент завершен: " + th.name());
                        th.pause(); // Ставим на паузу, чтобы остановить раздачу
                        break;
                    case TORRENT_ERROR:
                        log("Ошибка торрента: " + ((TorrentErrorAlert) torrentAlert).error().message());
                        break;
                }

                // Для всех событий обновляем данные и отправляем слушателям
                updateAndNotify(th);
            }
        };

        sessionManager.addListener(listener);
        // Запрашиваем обновления статуса (STATE_UPDATE) каждые 1.5 секунды
        sessionManager.postTorrentUpdates();
        log("AlertListener установлен, обновления статуса запущены.");
    }

    /**
     * Централизованный метод для создания объекта Torrent и уведомления UI.
     * @param th TorrentHandle из алерта или сессии.
     */
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
                return; // Еще нет информации для создания полного объекта Torrent
            }
        }

        // Создаем НОВЫЙ объект с актуальными данными
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

        // Получаем СТАРЫЙ объект из нашей карты
        Torrent oldTorrent = torrentsMap.get(btih);

        // СРАВНИВАЕМ! Отправляем обновление, только если что-то изменилось.
        if (oldTorrent == null || !newTorrent.equals(oldTorrent)) {
            torrentsMap.put(btih, newTorrent); // Сохраняем новое состояние
            updateListeners(newTorrent);       // И только тогда уведомляем UI
            log("Updating UI for " + newTorrent.getName() + " | Progress: " + newTorrent.getProgress() + "%");
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