package com.alaka_ala.torstream.torrent;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.frostwire.jlibtorrent.AlertListener;
import com.frostwire.jlibtorrent.Entry;
import com.frostwire.jlibtorrent.SessionManager;
import com.frostwire.jlibtorrent.TorrentFlags;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.jlibtorrent.alerts.AddTorrentAlert;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.AlertType;
import com.frostwire.jlibtorrent.alerts.BlockFinishedAlert;
import com.frostwire.jlibtorrent.alerts.DhtErrorAlert;
import com.frostwire.jlibtorrent.alerts.PieceFinishedAlert;
import com.frostwire.jlibtorrent.alerts.StateUpdateAlert;
import com.frostwire.jlibtorrent.alerts.TorrentErrorAlert;
import com.frostwire.jlibtorrent.alerts.TorrentFinishedAlert;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Класс TorrentStreamer предназначен для загрузки торрентов по магнет-ссылке и организации стриминга видео.
 * Использует библиотеку jlibtorrent для работы с торрентами и NanoHTTPD для создания HTTP-сервера.
 */
public class TorrentStreamer {
    // Путь к загружаемому видеофайлу
    private static String videoFilePath;
    // Контекст приложения (для доступа к ресурсам и запуска активностей)
    private Context context;
    // Последовательная загрузка если TRUE
    private boolean isSequentialDownload = false;
    private LocalHttpServer httpServer;


    /**
     * Возвращает контекст приложения.
     *
     * @return Контекст приложения.
     */
    public Context getContext() {
        return context;
    }

    /**
     * Запускает процесс загрузки торрента.
     *
     * @param context Контекст приложения.
     * @param magnet  Магнет-ссылка для загрузки торрента.
     */
    public void download(Context context, String magnet, boolean isSequentialDownload, DownloadListener downloadListener) {
        this.isSequentialDownload = isSequentialDownload;
        Handler handlerMainLooper = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                Bundle bundle = message.getData();
                if (bundle.getBoolean("error")) {
                    downloadListener.onError(bundle.getString("msg"));
                } else {
                    if (bundle.getString("type", "").equals(AlertType.ADD_TORRENT.name())) {
                        downloadListener.onStartDownload(new File(videoFilePath));
                    } else if (bundle.getString("type", "").equals(AlertType.PIECE_FINISHED.name())) {
                        downloadListener.onDProgress(bundle.getInt("indexPieces"), bundle.getInt("progress"), new File(videoFilePath));
                    } else if (bundle.getString("type", "").equals(AlertType.TORRENT_FINISHED.name())) {
                        downloadListener.onDownloadFinished(new File(videoFilePath));
                    }
                }
                return false;
            }
        });

        this.context = context;
        // Запуск загрузки торрента в отдельном потоке
        Thread thread = new Thread(() -> {
            final SessionManager sessionManager = new SessionManager();
            try {
                startdl(context, magnet, sessionManager, handlerMainLooper);
            } catch (InterruptedException e) {
                e.printStackTrace();
                sendHandlerMsg(handlerMainLooper, AlertType.TORRENT_ERROR, 0, 0, e.getMessage());
            }
            // Остановка сессии после завершения загрузки
            sessionManager.stop();
        });
        thread.start();
    }

    /**
     * Запускает загрузку торрента по магнет-ссылке.
     *
     * @param context        Контекст приложения.
     * @param magnetLink     Магнет-ссылка для загрузки торрента.
     * @param sessionManager Менеджер сессии для работы с торрентами.
     * @throws InterruptedException Исключение, если поток был прерван.
     */
    private void startdl(Context context, String magnetLink, SessionManager sessionManager, Handler handlerMainLooper) throws InterruptedException {
        // Получение директории для сохранения загруженных файлов
        File saveDir = context.getExternalCacheDir();
        if (saveDir == null) return;
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }

        // Добавление слушателя событий торрента
        sessionManager.addListener(new AlertListener() {
            private int grade = 0;
            @Override
            public int[] types() {
                return null;
            }
            @Override
            public void alert(Alert<?> alert) {
                AlertType type = alert.type();
                switch (type) {
                    case ADD_TORRENT:
                        // Установка флага последовательной загрузки файлов
                        if (isSequentialDownload) {
                            ((AddTorrentAlert) alert).handle().setFlags(TorrentFlags.SEQUENTIAL_DOWNLOAD);
                        }
                        ((AddTorrentAlert) alert).handle().resume();
                        sendHandlerMsg(handlerMainLooper, type, 0, 0, "Торрент добавлен. Скоро начнется загрузка");
                        break;
                    case PIECE_FINISHED:
                        // Логирование прогресса загрузки
                        int progress = (int) (((PieceFinishedAlert) alert).handle().status().progress() * 100);
                        if (grade < progress / 5) {
                            int index = (int) (((PieceFinishedAlert) alert).pieceIndex());
                            log("index: " + index);
                            grade += 1;
                            sessionManager.downloadRate();
                            log(progress + " %  downloaded");
                            sendHandlerMsg(handlerMainLooper, type, progress, index, progress + " %  downloaded");
                        }
                        System.out.println("PIECE_FINISHED");
                        break;
                    case TORRENT_FINISHED:
                        grade = 0;
                        ((TorrentFinishedAlert) alert).handle().pause();
                        log("TORRENT FINISHED (Торрент загружен)");
                        sendHandlerMsg(handlerMainLooper, type, 100, ((TorrentFinishedAlert) alert).handle().queuePosition(),"TORRENT FINISHED (Торрент загружен)");
                        break;
                    case TORRENT_ERROR:
                        log(((TorrentErrorAlert) alert).what());
                        log("TORRENT ERROR: STATUS = " + ((TorrentErrorAlert) alert).handle().status());
                        sendHandlerMsg(handlerMainLooper, type, 0, 0, "TORRENT ERROR: STATUS = " + ((TorrentErrorAlert) alert).handle().status());
                        break;
                    case BLOCK_FINISHED:
                        progress = (int) (((BlockFinishedAlert) alert).handle().status().progress() * 100);
                        if (grade < progress / 20) {
                            int index = (int) (((BlockFinishedAlert) alert).pieceIndex());
                            log("index: " + index);
                            grade += 1;
                            sessionManager.downloadRate();
                            log(progress + " %  downloaded");
                            sendHandlerMsg(handlerMainLooper, type, progress, index, progress + " %  downloaded");
                        }
                        log("BLOCK FINISH (Загрзука блока завершена)");
                        break;
                    case STATE_UPDATE:
                        log(((StateUpdateAlert) alert).message());
                        sendHandlerMsg(handlerMainLooper, type, 0, 0, ((StateUpdateAlert) alert).message());
                        break;
                    case METADATA_RECEIVED:
                        log("metadata received (Получение метаданных торрента)");
                        sendHandlerMsg(handlerMainLooper, type, 0,0, "metadata received (Получение метаданных торрента)");
                        break;
                    case DHT_ERROR:
                        log("DHT error");
                        log(((DhtErrorAlert) alert).message());
                        sendHandlerMsg(handlerMainLooper, type, 0,0, "DHT error: " + ((DhtErrorAlert) alert).message());
                        break;
                    default:
                        break;
                }
            }
        });

        // Запуск сессии, если она еще не запущена
        if (!sessionManager.isRunning()) {
            sessionManager.start();
        }

        // Обработка магнет-ссылки
        if (magnetLink.startsWith("magnet:?")) {
            // Ожидание подключения к DHT (Distributed Hash Table)
            waitForNodesInDHT(sessionManager);
            // Получение метаданных торрента по магнет-ссылке
            byte[] data = sessionManager.fetchMagnet(magnetLink, 30, true);
            TorrentInfo ti = TorrentInfo.bdecode(data);
            log(Entry.bdecode(data).toString());
            log("is valid ? =" + ti.isValid());


            // Начало загрузки торрента
            sessionManager.download(ti, saveDir);
            log("torrent added with name = " + ti.name());
            videoFilePath = new File(saveDir.getAbsolutePath(), ti.name()).getAbsolutePath();

            // Логирование состояния и прогресса загрузки
            int countPieces = ti.numPieces();
            for (int i = 0; i < countPieces; i++) {
                TimeUnit.SECONDS.sleep(1);
                log(sessionManager.find(ti.infoHash()).status().state() + " state");
                log(sessionManager.find(ti.infoHash()).status().progress() * 100 + " progress");
                sendHandlerMsg(handlerMainLooper, AlertType.STATE_UPDATE, (int) sessionManager.find(ti.infoHash()).status().progress() * 100, sessionManager.find(ti.infoHash()).status().numPieces(), sessionManager.find(ti.infoHash()).status().state() + " state");
            }




        }
    }


    private void sendHandlerMsg(Handler handlerMainLooper, AlertType alertType, int progress, int index, String msg) {
        Bundle bundle = new Bundle();
        bundle.putString("type", alertType.name());
        switch (alertType) {
            case ADD_TORRENT:
                bundle.putBoolean("error", false);
                bundle.putBoolean("isSequentialDownload", isSequentialDownload);
                bundle.putString("msg", msg);
                break;
            case PIECE_FINISHED:
            case TORRENT_FINISHED:
            case BLOCK_FINISHED:
                bundle.putBoolean("error", false);
                bundle.putInt("progress", progress);
                bundle.putInt("indexPieces", index);
                bundle.putString("msg", msg);

                break;

            case TORRENT_ERROR:
            case DHT_ERROR:
                bundle.putBoolean("error", true);
                bundle.putString("msg", msg);
                break;
            case STATE_UPDATE:
            case METADATA_RECEIVED:
                bundle.putBoolean("error", false);
                bundle.putString("msg", msg);
                break;
            default:
                break;
        }
        Message msgg = new Message();
        msgg.setData(bundle);
        handlerMainLooper.sendMessage(msgg);
    }

    /**
     * Ожидает подключения к DHT (Distributed Hash Table).
     *
     * @param s Менеджер сессии.
     * @throws InterruptedException Исключение, если поток был прерван.
     */
    private void waitForNodesInDHT(final SessionManager s) throws InterruptedException {
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

        // Ожидание подключения к DHT в течение 10 секунд
        boolean r = signal.await(1000, TimeUnit.SECONDS);
        if (!r) {
            System.out.println("DHT bootstrap timeout");
            System.exit(0);
        }
    }


    public interface DownloadListener {
        void onStartDownload(File filePath);
        void onDProgress(int indexPiece, int progress, File filePath);
        void onDownloadFinished(File filePath);
        void onError(String err);
    }


    /**
     * Логирует сообщения в консоль.
     *
     * @param s Сообщение для логирования.
     */
    private static void log(String s) {
        Log.d("TorrentStreamer", s);
    }
}
