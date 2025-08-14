package com.alaka_ala.florafilm.ui.util.updater;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.ui.fragments.settings.SettingsUtils;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import io.appmetrica.analytics.impl.S;

public class AppUpdater {
    private static final String TAG = "AppUpdater";
    private static final String APK_URL = "https://github.com/varnavsky07rus/FloraFilmV2/raw/refs/heads/master/app/release/app-release.apk";
    private static final String VERSION_URL = "https://raw.githubusercontent.com/varnavsky07rus/FloraFilmV2/refs/heads/master/app/release/output-metadata.json";
    private static final String APK_URL_BETA_VERSION = "https://github.com/varnavsky07rus/FloraFilmV2/raw/refs/heads/beta/app/release/app-release.apk";
    private static final String VERSION_URL_BETA = "https://raw.githubusercontent.com/varnavsky07rus/FloraFilmV2/refs/heads/beta/app/release/output-metadata.json";
    private static final int REQUEST_INSTALL_PERMISSION = 1001;
    private static final String TEMP_APK_NAME = "update_temp.apk";

    private final Activity activity;
    private AlertDialog downloadDialog;
    private ProgressBar progressBar;
    private TextView progressText;
    private TextView statusText;
    private File downloadedApk;
    private int newVersionCode;

    // Если включено обновление до бета версий то True
    private boolean isUpdateBetaVersion;

    public AppUpdater(Activity activity) {
        this.activity = activity;
        SettingsUtils.getParamBetaVersion(activity.getBaseContext());
    }

    public void checkForUpdate() {
        new CheckVersionTask().execute();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_INSTALL_PERMISSION) {
            if (canInstallApk()) {
                proceedWithInstallation();
            } else {
                showErrorDialog("Ошибка установки! Разрешения на установку из неизвестных источников не выданы!");
            }
        }
    }

    private class CheckVersionTask extends AsyncTask<Void, Void, Integer> {
        private int currentVersionCode;

        @Override
        protected void onPreExecute() {
            try {
                PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
                currentVersionCode = pInfo.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Package info error", e);
                cancel(true);
            }
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(isUpdateBetaVersion ? VERSION_URL_BETA : VERSION_URL).openConnection();
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return null;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
                reader.close();

                JSONObject jsonObject = new JSONObject(json.toString());
                return jsonObject.getJSONArray("elements")
                        .getJSONObject(0)
                        .getInt("versionCode");

            } catch (IOException | JSONException e) {
                Log.e(TAG, "Version check error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer latestVersionCode) {
            if (latestVersionCode == null) {
                showErrorDialog("Ошибка поиска обновлений");
                return;
            }

            if (latestVersionCode > currentVersionCode) {
                newVersionCode = latestVersionCode;
                showUpdateDialog();
            } else {
                showMessageDialog("Обновлений не найдено!");
            }
        }
    }

    private void showUpdateDialog() {
        new AlertDialog.Builder(activity)
                .setTitle("Доступно обновление")
                .setMessage("Доступна новая версия приложения, установить сейчас?")
                .setPositiveButton("Да", (dialog, which) -> prepareDownload())
                .setNegativeButton("Позже", null)
                .show();
    }

    private void prepareDownload() {
        // Создаем временную папку в кэше приложения
        File tempDir = new File(activity.getCacheDir(), "updates");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        // Удаляем предыдущий временный файл, если есть
        downloadedApk = new File(tempDir, TEMP_APK_NAME);
        if (downloadedApk.exists()) {
            downloadedApk.delete();
        }

        startDownload();
    }

    private void startDownload() {
        showDownloadDialog();
        new DownloadApkTask().execute();
    }

    @SuppressLint("MissingInflatedId")
    private void showDownloadDialog() {
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.download_progress_dialog, null);
        progressBar = dialogView.findViewById(R.id.progressBar);
        progressText = dialogView.findViewById(R.id.progressText);
        statusText = dialogView.findViewById(R.id.statusText);

        downloadDialog = new AlertDialog.Builder(activity)
                .setView(dialogView)
                .setTitle("Загрузка обновлений")
                .setCancelable(false)
                .create();
        downloadDialog.show();
    }

    private void updateDownloadProgress(int progress) {
        if (progressBar != null) progressBar.setProgress(progress);
        if (progressText != null) progressText.setText(progress + "%");
    }

    private void updateStatus(String status) {
        if (statusText != null) statusText.setText(status);
    }

    private class DownloadApkTask extends AsyncTask<Void, Integer, Boolean> {
        private volatile boolean isDownloading = true;

        @Override
        protected void onPreExecute() {
            updateStatus("Подготовка к загрузке...");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL(isUpdateBetaVersion ? APK_URL_BETA_VERSION : APK_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return false;
                }

                int fileLength = connection.getContentLength();

                // Запускаем поток для отслеживания прогресса
                new Thread(() -> {
                    while (isDownloading) {
                        try {
                            Thread.sleep(300);
                            if (downloadedApk.exists()) {
                                long downloaded = downloadedApk.length();
                                int progress = (int) (downloaded * 100 / fileLength);
                                publishProgress(progress);
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }).start();

                // Скачиваем файл
                FileUtils.copyURLToFile(url, downloadedApk);
                isDownloading = false;

                return true;
            } catch (IOException e) {
                Log.e(TAG, "Download error", e);
                isDownloading = false;
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            updateDownloadProgress(values[0]);
            updateStatus("Загрузка...");
        }

        @Override
        protected void onPostExecute(Boolean success) {
            isDownloading = false;
            if (downloadDialog != null && downloadDialog.isShowing()) {
                downloadDialog.dismiss();
            }

            if (success && downloadedApk.exists()) {
                verifyAndInstall();
            } else {
                showErrorDialog("Ошибка загрузки!");
                cleanupTempFiles();
            }
        }

        @Override
        protected void onCancelled() {
            isDownloading = false;
            cleanupTempFiles();
            if (downloadDialog != null && downloadDialog.isShowing()) {
                downloadDialog.dismiss();
            }
            showErrorDialog("Загрузка отменена!");
        }
    }

    private void verifyAndInstall() {
        try {
            PackageInfo newPackageInfo = activity.getPackageManager()
                    .getPackageArchiveInfo(downloadedApk.getAbsolutePath(), 0);

            if (newPackageInfo != null && newPackageInfo.versionCode == newVersionCode) {
                showInstallDialog();
            } else {
                showErrorDialog("Загруженный файл поврежден");
                cleanupTempFiles();
            }
        } catch (Exception e) {
            Log.e(TAG, "Version verification error", e);
            showErrorDialog("Ошибка проверки версии файла");
            cleanupTempFiles();
        }
    }

    private void showInstallDialog() {
        new AlertDialog.Builder(activity)
                .setTitle("Установка обновления")
                .setMessage("Обновление загружено. Установить сейчас?")
                .setPositiveButton("Установить", (dialog, which) -> installApk())
                .setNegativeButton("Позже", (dialog, which) -> cleanupTempFiles())
                .show();
    }

    private void installApk() {
        if (!canInstallApk()) {
            requestInstallPermission();
            return;
        }
        proceedWithInstallation();
    }

    private boolean canInstallApk() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
                activity.getPackageManager().canRequestPackageInstalls();
    }

    private void requestInstallPermission() {
        new AlertDialog.Builder(activity)
                .setTitle("Требуется разрешение")
                .setMessage("Разрешите установку из неизвестных источников")
                .setPositiveButton("К настройкам", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    intent.setData(Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivityForResult(intent, REQUEST_INSTALL_PERMISSION);
                })
                .setNegativeButton("Отмена", (dialog, which) -> cleanupTempFiles())
                .show();
    }

    private void proceedWithInstallation() {
        if (downloadedApk == null || !downloadedApk.exists()) {
            showErrorDialog("Файл обновления не найден");
            return;
        }

        try {
            Uri apkUri = FileProvider.getUriForFile(
                    activity,
                    activity.getPackageName() + ".fileprovider",
                    downloadedApk
            );

            Intent installIntent = getIntentInstall(apkUri);

            activity.startActivity(installIntent);

            // Удаляем файл через 100 секунд на всякий случай
            new android.os.Handler().postDelayed(this::cleanupTempFiles, 100000);
        } catch (Exception e) {
            Log.e(TAG, "Installation error", e);
            showErrorDialog("Ошибка установки: " + e.getMessage());
            cleanupTempFiles();
        }
    }

    @NonNull
    private static Intent getIntentInstall(Uri apkUri) {
        Intent installIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        installIntent.setData(apkUri);
        installIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        installIntent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        installIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true);

        // Удаляем файл после установки
        installIntent.putExtra(Intent.EXTRA_ALLOW_REPLACE, true);
        installIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return installIntent;
    }

    private void cleanupTempFiles() {
        try {
            if (downloadedApk != null && downloadedApk.exists()) {
                downloadedApk.delete();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cleaning temp files", e);
        }
    }

    private void showMessageDialog(String message) {
        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(activity)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}