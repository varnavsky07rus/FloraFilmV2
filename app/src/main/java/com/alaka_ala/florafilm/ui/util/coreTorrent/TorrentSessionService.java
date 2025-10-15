package com.alaka_ala.florafilm.ui.util.coreTorrent;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.alaka_ala.florafilm.BuildConfig;
import com.alaka_ala.florafilm.ui.util.coreTorrent.interfaces.UpdateDataListener;
import com.alaka_ala.florafilm.ui.util.coreTorrent.models.Torrent;
import com.alaka_ala.florafilm.ui.util.coreTorrent.utils.MagnetLinkParser;
import com.frostwire.jlibtorrent.AlertListener;
import com.frostwire.jlibtorrent.Entry;
import com.frostwire.jlibtorrent.SessionManager;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.jlibtorrent.TorrentStatus;
import com.frostwire.jlibtorrent.alerts.AddTorrentAlert;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.AlertType;
import com.frostwire.jlibtorrent.alerts.BlockFinishedAlert;
import com.frostwire.jlibtorrent.alerts.DhtErrorAlert;
import com.frostwire.jlibtorrent.alerts.PieceFinishedAlert;
import com.frostwire.jlibtorrent.alerts.StateUpdateAlert;
import com.frostwire.jlibtorrent.alerts.TorrentErrorAlert;
import com.frostwire.jlibtorrent.alerts.TorrentFinishedAlert;
import com.frostwire.jlibtorrent.swig.torrent_handle;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class TorrentSessionService extends Service {
    public static final String ACTION_SERVICE_INITIALIZE = "com.alaka_ala.florafilm.ui.util.coreTorrent.TorrentSessionService.ACTION_SERVICE_INITIALIZE";
    private static volatile TorrentSessionService instance;
    private final SessionManager sessionManager = new SessionManager();
    private static Map<String, Torrent> torrentsMap = new HashMap<>();
    private static Map<String, TorrentInfo> torrentInfoMap = new HashMap<>();
    private static Map<String, UpdateDataListener> listeners = new HashMap<>();


    // Публичный конструктор (обязательно для Android сервисов)
    public TorrentSessionService() {
        super();
    }


    // Singleton метод для получения экземпляра
    public static TorrentSessionService getInstance() {
        return instance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initializeSession();
        log("TorrentSessionService created");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sessionManager.isRunning()) {
            sessionManager.pause();
            sessionManager.stop();
        }
        instance = null;
        log("TorrentSessionService destroyed");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_SERVICE_INITIALIZE.equals(intent.getAction())) {
            initializeSession();
        }
        return START_NOT_STICKY;
    }


    private void initializeSession() {
        if (!sessionManager.isRunning()) {
            sessionManager.start();
            log("sessionManager initialized");
        }
    }


    public void download(int kinopoisk_id, String magnetLink) throws InterruptedException {
        String magnetBtih = MagnetLinkParser.extractBtih(magnetLink);
        if (torrentsMap.containsKey(magnetBtih)) {
            log("Данный торрент уже добавлен");
            return;
        }
        TorrentInfo ti = null;
        File saveDir = new File(this.getFilesDir(), "tor");
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        File uniqSaveDir = new File(saveDir.getAbsolutePath(), kinopoisk_id + "/" + magnetBtih);
        if (!uniqSaveDir.exists()) {
            uniqSaveDir.mkdirs();
        }

        log("saveDir = " + saveDir.getAbsolutePath());
        AlertListener l = new AlertListener() {
            private int grade = 0;

            @Override
            public int[] types() {
                return new int[]{
                        AlertType.ADD_TORRENT.swig(),
                        AlertType.PIECE_FINISHED.swig(),
                        AlertType.TORRENT_FINISHED.swig(),
                        AlertType.TORRENT_ERROR.swig(),
                        AlertType.BLOCK_FINISHED.swig(),
                        AlertType.TORRENT_PAUSED.swig(),
                        AlertType.STATE_UPDATE.swig(),
                        AlertType.METADATA_RECEIVED.swig(),
                        AlertType.DHT_ERROR.swig()
                };
            }

            private int lastPtogress = 0;

            @Override
            public void alert(Alert<?> alert) {
                AlertType type = alert.type();
                TorrentInfo tiLoc;
                switch (type) {
                    case ADD_TORRENT:
                        ((AddTorrentAlert) alert).handle().resume();
                        tiLoc = torrentInfoMap.get(magnetBtih);
                        updateTorrent(tiLoc, uniqSaveDir);
                        break;
                    case PIECE_FINISHED:
                        int progress = (int) (((PieceFinishedAlert) alert).handle().status().progress() * 100);
                        if (progress > lastPtogress) {
                            if (grade < progress / 20) {
                                int index = (int) (((PieceFinishedAlert) alert).pieceIndex());
                                log("index: " + index);
                                grade += 1;
                                sessionManager.downloadRate();

                            }
                            tiLoc = torrentInfoMap.get(magnetBtih);
                            updateTorrent(tiLoc, uniqSaveDir);
                            log(progress + " % downloaded");
                            lastPtogress = progress;
                        }
                        break;
                    case TORRENT_FINISHED:
                        grade = 0;
                        ((TorrentFinishedAlert) alert).handle().pause();
                        tiLoc = torrentInfoMap.get(magnetBtih);
                        updateTorrent(tiLoc, uniqSaveDir);
                        break;
                    case TORRENT_ERROR:
                        log(((TorrentErrorAlert) alert).what());
                        break;
                    case BLOCK_FINISHED:
                        progress = (int) (((BlockFinishedAlert) alert).handle().status().progress() * 100);
                        if (grade < progress / 20) {
                            System.out.println("HERE: " + progress);
                            grade += 1;
                            sessionManager.downloadRate();
                        }
                        break;
                    case TORRENT_PAUSED:
                        log("torrent paused");
                        tiLoc = torrentInfoMap.get(magnetBtih);
                        updateTorrent(tiLoc, uniqSaveDir);
                        break;
                    case TORRENT_CONFLICT:
                        log("torrent conflict");
                        tiLoc = torrentInfoMap.get(magnetBtih);
                        updateTorrent(tiLoc, uniqSaveDir);
                        break;
                    case STATE_UPDATE:
                        log(((StateUpdateAlert) alert).message());
                        for (TorrentStatus ts : ((StateUpdateAlert) alert).status()) {
                            log("Torrent STATUS: " + ts.state().name());
                        }
                        tiLoc = torrentInfoMap.get(magnetBtih);
                        updateTorrent(tiLoc, uniqSaveDir);
                        break;
                    case METADATA_RECEIVED:
                        log("metadata received");
                        break;
                    case DHT_ERROR:
                        log("dht error");
                        log(((DhtErrorAlert) alert).message());
                        break;
                    default:
                        break;
                }
            }
        };
        sessionManager.addListener(l);
        initializeSession();

        if (magnetLink.startsWith("magnet:?")) {
            waitForNodesInDHT();
            byte[] data = sessionManager.fetchMagnet(magnetLink, 30, new File("/tmp"));
            if (data == null) return;
            ti = TorrentInfo.bdecode(data);
            torrentInfoMap.put(magnetBtih, ti);
            log(Entry.bdecode(data).toString());
            sessionManager.download(ti, uniqSaveDir);
            TorrentHandle th = sessionManager.find(ti.infoHashV1());
            th.resume();

            updateTorrent(ti, uniqSaveDir);

            int i = 0;
            while (i < 20) {
                TimeUnit.SECONDS.sleep(1);
                log(sessionManager.find(ti.infoHashV1()).status().state() + " state");
                log(sessionManager.find(ti.infoHashV1()).status().progress() * 100 + " progress");
                i++;
            }

            log("torrent added with name = " + ti.name());
        } else {
            ti = null;
        }
    }

    private void updateTorrent(TorrentInfo ti, File uniqSaveDir) {
        if (ti == null) {
            log("Невозможно обновить Torrent. Переданный параметр TorrentInfo имеет значение null");
            return;
        }
        TorrentHandle th = sessionManager.find(ti.infoHashV1());
        Torrent torrent = new Torrent(
                ti.name(),
                ti.totalSize(),
                ti.makeMagnetUri(),
                MagnetLinkParser.extractBtih(ti.makeMagnetUri()),
                uniqSaveDir.getAbsolutePath(),
                (int) (th.status().progress() * 100),
                th.status().state().name(), ti.bencode());
        torrentsMap.put(MagnetLinkParser.extractBtih(ti.makeMagnetUri()), torrent);
        notifyDataToListeners(torrent);
    }

    // Получает из списка торренты и отправляет их в обработчик событий
    private void notifyDataToListeners(Torrent torrent) {
        for (Map.Entry<String, UpdateDataListener> entry : listeners.entrySet()) {
            entry.getValue().onUpdatedTorrent(torrent);
            log("Listener " + entry.getKey() + " notified");
        }
    }

    public void addListener(String key, UpdateDataListener listener) {
        if (listeners.containsKey(key)) {
            log("Listener with key " + key + " already exists");
            return;
        }
        listeners.put(key, listener);
    }

    public Map<String, Torrent> getTorrentsMap() {
        return torrentsMap;
    }


    private void waitForNodesInDHT() throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(1);

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                long nodes = sessionManager.stats().dhtNodes();
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
        String tag = TorrentSessionService.class.getSimpleName();
        if (BuildConfig.DEBUG) Log.i(tag, s);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}