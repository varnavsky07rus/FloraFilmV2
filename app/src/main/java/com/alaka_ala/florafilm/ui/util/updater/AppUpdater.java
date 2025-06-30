package com.alaka_ala.florafilm.ui.util.updater;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
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

import androidx.core.content.FileProvider;

import com.alaka_ala.florafilm.R;

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

public class AppUpdater {
    private static final String TAG = "AppUpdater";
    private static final String APK_URL = "https://github.com/varnavsky07rus/FloraFilmV2/raw/refs/heads/master/app/release/app-release.apk";
    private static final String VERSION_URL = "https://raw.githubusercontent.com/varnavsky07rus/FloraFilmV2/refs/heads/master/app/release/output-metadata.json";
    private static final int REQUEST_INSTALL_PERMISSION = 1001;
    private static final String TEMP_APK_NAME = "update_temp.apk";

    private final Activity activity;
    private AlertDialog downloadDialog;
    private ProgressBar progressBar;
    private TextView progressText;
    private TextView statusText;
    private File downloadedApk;
    private int newVersionCode;

    public AppUpdater(Activity activity) {
        this.activity = activity;
    }

    public void checkForUpdate() {
        new CheckVersionTask().execute();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_INSTALL_PERMISSION) {
            if (canInstallApk()) {
                proceedWithInstallation();
            } else {
                showErrorDialog("Install permission not granted");
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
                HttpURLConnection connection = (HttpURLConnection) new URL(VERSION_URL).openConnection();
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
                showErrorDialog("Update check error");
                return;
            }

            if (latestVersionCode > currentVersionCode) {
                newVersionCode = latestVersionCode;
                showUpdateDialog();
            } else {
                showMessageDialog("You have the latest version");
            }
        }
    }

    private void showUpdateDialog() {
        new AlertDialog.Builder(activity)
                .setTitle("Update available")
                .setMessage("New version available. Update now?")
                .setPositiveButton("Update", (dialog, which) -> prepareDownload())
                .setNegativeButton("Later", null)
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
                .setTitle("Downloading update")
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
            updateStatus("Preparing download...");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL(APK_URL);
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
            updateStatus("Downloading...");
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
                showErrorDialog("Download failed");
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
            showErrorDialog("Download cancelled");
        }
    }

    private void verifyAndInstall() {
        try {
            PackageInfo newPackageInfo = activity.getPackageManager()
                    .getPackageArchiveInfo(downloadedApk.getAbsolutePath(), 0);

            if (newPackageInfo != null && newPackageInfo.versionCode == newVersionCode) {
                showInstallDialog();
            } else {
                showErrorDialog("Downloaded file is corrupted");
                cleanupTempFiles();
            }
        } catch (Exception e) {
            Log.e(TAG, "Version verification error", e);
            showErrorDialog("Version check failed");
            cleanupTempFiles();
        }
    }

    private void showInstallDialog() {
        new AlertDialog.Builder(activity)
                .setTitle("Install update")
                .setMessage("Update downloaded. Install now?")
                .setPositiveButton("Install", (dialog, which) -> installApk())
                .setNegativeButton("Later", (dialog, which) -> cleanupTempFiles())
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
                .setTitle("Permission required")
                .setMessage("Allow installation from unknown sources")
                .setPositiveButton("Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    intent.setData(Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivityForResult(intent, REQUEST_INSTALL_PERMISSION);
                })
                .setNegativeButton("Cancel", (dialog, which) -> cleanupTempFiles())
                .show();
    }

    private void proceedWithInstallation() {
        if (downloadedApk == null || !downloadedApk.exists()) {
            showErrorDialog("Update file not found");
            return;
        }

        try {
            Uri apkUri = FileProvider.getUriForFile(
                    activity,
                    activity.getPackageName() + ".provider",
                    downloadedApk
            );

            Intent installIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            installIntent.setData(apkUri);
            installIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            installIntent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
            installIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true);

            // Удаляем файл после установки
            installIntent.putExtra(Intent.EXTRA_ALLOW_REPLACE, true);
            installIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            activity.startActivity(installIntent);

            // Удаляем файл через 10 секунд на всякий случай
            new android.os.Handler().postDelayed(this::cleanupTempFiles, 10000);
        } catch (Exception e) {
            Log.e(TAG, "Installation error", e);
            showErrorDialog("Installation failed: " + e.getMessage());
            cleanupTempFiles();
        }
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