package com.alaka_ala.florafilm.ui.util.coreMatrix.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.ui.util.coreMatrix.MatrixManager;

public class TorServerService extends Service implements MatrixManager.DownloadCallback {

    private static final String TAG = "TorServerService";
    private static final String CHANNEL_ID = "TorServerServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    public static final String ACTION_STOP_SERVICE = "com.alaka_ala.florafilm.ACTION_STOP_SERVICE";

    private static final int MAX_STATUS_CHECKS = 15; // 15 checks * 2 seconds = 30 seconds timeout
    private static final long CHECK_INTERVAL_MS = 2000; // 2 seconds

    private NotificationManager notificationManager;
    private Handler statusCheckHandler;
    private Runnable statusCheckRunnable;
    private int statusCheckCounter = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP_SERVICE.equals(intent.getAction())) {
            Log.d(TAG, "Received stop action. Stopping service.");
            stopServerAndService();
            return START_NOT_STICKY;
        }

        if (MatrixManager.isServerRunning()) {
            Log.d(TAG, "Server is already running.");
            updateNotification("Сервер запущен");
            return START_NOT_STICKY;
        }

        if (!MatrixManager.isServerDownloaded(this)) {
            Log.d(TAG, "Server not downloaded, starting download.");
            MatrixManager.downloadServer(this, this);
        } else {
            Log.d(TAG, "Server already downloaded, starting process.");
            MatrixManager.startServer(this);
            updateNotification("Запуск сервера...");
            startServerStatusCheck();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onComplete(boolean success, String message) {
        if (success) {
            Log.d(TAG, "Download complete, starting server.");
            updateNotification("Загрузка завершена, запуск сервера...");
            MatrixManager.startServer(this);
            startServerStatusCheck();
        } else {
            Log.e(TAG, "Download failed: " + message);
            updateNotification("Ошибка загрузки: " + message);
        }
    }

    private void startServerStatusCheck() {
        stopServerStatusCheck(); // Ensure no previous checks are running
        statusCheckCounter = 0;
        statusCheckHandler = new Handler();
        statusCheckRunnable = new Runnable() {
            @Override
            public void run() {
                statusCheckCounter++;
                if (MatrixManager.isServerRunning()) {
                    Log.d(TAG, "Server status check: Server is running.");
                    updateNotification("Сервер запущен");
                    stopServerStatusCheck();
                } else if (statusCheckCounter >= MAX_STATUS_CHECKS) {
                    Log.e(TAG, "Server status check: Timeout reached, server failed to start.");
                    updateNotification("Ошибка запуска сервера");
                    stopServerStatusCheck();
                } else {
                    Log.d(TAG, "Server status check: Server not running yet, checking again in " + CHECK_INTERVAL_MS + "ms");
                    statusCheckHandler.postDelayed(this, CHECK_INTERVAL_MS);
                }
            }
        };
        statusCheckHandler.post(statusCheckRunnable);
    }

    private void stopServerStatusCheck() {
        if (statusCheckHandler != null && statusCheckRunnable != null) {
            statusCheckHandler.removeCallbacks(statusCheckRunnable);
            statusCheckHandler = null;
            statusCheckRunnable = null;
        }
    }

    private void stopServerAndService() {
        MatrixManager.stopServer();
        stopServerStatusCheck();
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Ensure everything is cleaned up when the service is destroyed.
        stopServerAndService();
        Log.d(TAG, "TorServerService destroyed.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onProgress(int progress) {
        updateProgressNotification("Downloading TorrServer", progress);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "TorrServer Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT // Changed from LOW to DEFAULT
            );
            notificationManager.createNotificationChannel(serviceChannel);
        }
    }

    private NotificationCompat.Builder getNotificationBuilder(String title, String text) {
        Intent stopIntent = new Intent(this, TorServerService.class);
        stopIntent.setAction(ACTION_STOP_SERVICE);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher) // Replace with your app's icon
                .setOngoing(true)
                .addAction(R.drawable.outline_close_small_24, "Остановить", stopPendingIntent); // Replace with your stop icon
    }

    private void updateNotification(String text) {
        Notification notification = getNotificationBuilder("Статус сервера", text).build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private void updateProgressNotification(String title, int progress) {
        Notification notification = getNotificationBuilder(title, progress + "% downloaded")
                .setProgress(100, progress, false)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }



}
