package com.alaka_ala.florafilm.ui.util.local;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.webkit.MimeTypeMap;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import java.io.File;

public class ApkInstaller {
    private static long downloadId;
    private static final String APK_URL = "https://github.com/YouROK/TorrServe/releases/download/MatriX.135.Client/TorrServe_MatriX.135.Client-release.apk";
    private static final String FILE_NAME = "TorrServe_MatriX.135.Client-release.apk";
    private static BroadcastReceiver downloadCompleteReceiver;

    public static void downloadAndInstallApk(Context context) {
        // 1. Проверяем разрешения
        if (!checkPermissions(context)) {
            return;
        }

        // 2. Создаем запрос на загрузку
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(APK_URL))
                .setTitle("Установка TorrServe")
                .setDescription("Загрузка...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, FILE_NAME)
                .setMimeType("application/vnd.android.package-archive");

        // 3. Запускаем загрузку
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        downloadId = downloadManager.enqueue(request);

        // 4. Регистрируем BroadcastReceiver с учетом версии Android
        registerDownloadReceiver(context);
    }

    private static boolean checkPermissions(Context context) {
        // Для Android 13+ проверяем разрешение на уведомления
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                    101);
            return false;
        }

        // Для Android 11+ проверяем MANAGE_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                !Environment.isExternalStorageManager()) {
            requestStoragePermission(context);
            return false;
        }

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private static void requestStoragePermission(Context context) {
        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private static void registerDownloadReceiver(Context context) {
        downloadCompleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id == downloadId) {
                    // Небольшая задержка для гарантированной записи файла
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        installApk(context);
                        try {
                            context.unregisterReceiver(this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, 1000);
                }
            }
        };

        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(downloadCompleteReceiver, filter,
                    Context.RECEIVER_NOT_EXPORTED);
        } else {
            ContextCompat.registerReceiver(context, downloadCompleteReceiver, filter, ContextCompat.RECEIVER_EXPORTED);
        }
    }

    private static void installApk(Context context) {
        File apkFile = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                FILE_NAME
        );

        if (!apkFile.exists()) {
            return;
        }

        Uri apkUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".provider",
                apkFile
        );

        Intent installIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        installIntent.setData(apkUri);
        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Явно разрешаем установку из неизвестных источников
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            installIntent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        }

        context.startActivity(installIntent);
    }
}
