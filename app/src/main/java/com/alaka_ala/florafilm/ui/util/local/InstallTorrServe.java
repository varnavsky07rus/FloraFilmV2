package com.alaka_ala.florafilm.ui.util.local;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.alaka_ala.florafilm.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class InstallTorrServe {

    private static final String TAG = "AppUpdater";
    private static final String APK_URL = "https://github.com/YouROK/TorrServe/releases/download/MatriX.135.Client/TorrServe_MatriX.135.Client-release.apk";

    private final Activity activity;
    private AlertDialog downloadDialog;
    private ProgressBar progressBar;
    private TextView progressText;
    private TextView statusText;
    private File downloadedApk;

    public InstallTorrServe(Activity activity) {
        this.activity = activity;
    }

    public void downloadAndInstall() {
        checkExistingApk();
    }

    private void checkExistingApk() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        downloadedApk = new File(downloadsDir, "TorrServe_MatriX.135.Client-release.apk");

        if (downloadedApk.exists()) {
            showInstallDialog();
        } else {
            startDownload();
        }
    }

    private void startDownload() {
        showDownloadDialog();
        new DownloadApkTask().execute();
    }

    private void showDownloadDialog() {
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.download_progress_dialog, null);

        progressBar = dialogView.findViewById(R.id.progressBar);
        progressText = dialogView.findViewById(R.id.progressText);
        statusText = dialogView.findViewById(R.id.statusText);

        downloadDialog = new MaterialAlertDialogBuilder(activity)
                .setView(dialogView)
                .setTitle("Загрузка приложения")
                .setCancelable(false)
                .setNegativeButton("Отмена", (dialog, which) -> {
                    if (downloadDialog != null) {
                        downloadDialog.dismiss();
                    }
                })
                .show();
    }

    private void updateDownloadProgress(int progress) {
        activity.runOnUiThread(() -> {
            if (progressBar != null) {
                progressBar.setProgress(progress);
            }
            if (progressText != null) {
                progressText.setText(progress + "%");
            }
        });
    }

    private void updateStatus(String status) {
        activity.runOnUiThread(() -> {
            if (statusText != null) {
                statusText.setText(status);
            }
        });
    }

    private class DownloadApkTask extends AsyncTask<Void, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            updateStatus("Подготовка к загрузке...");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL(APK_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage());
                    return false;
                }

                int fileLength = connection.getContentLength();
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                downloadedApk = new File(downloadsDir, "TorrServe_MatriX.135.Client-release.apk");

                InputStream input = connection.getInputStream();
                FileOutputStream output = new FileOutputStream(downloadedApk);

                byte[] data = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        output.close();
                        downloadedApk.delete();
                        return false;
                    }
                    total += count;
                    if (fileLength > 0) {
                        publishProgress((int) (total * 100 / fileLength));
                    }
                    output.write(data, 0, count);
                }

                output.close();
                input.close();
                return true;

            } catch (IOException e) {
                Log.e(TAG, "Error downloading APK", e);
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
            if (downloadDialog != null && downloadDialog.isShowing()) {
                downloadDialog.dismiss();
            }

            if (success) {
                showInstallDialog();
            } else {
                showErrorDialog("Ошибка загрузки приложения. Пожалуйста, попробуйте позже.");
            }
        }
    }

    private void showInstallDialog() {
        new MaterialAlertDialogBuilder(activity)
                .setTitle("Установка приложения")
                .setMessage("Приложение загружено. Установить сейчас?")
                .setPositiveButton("Установить", (dialog, which) -> installApk())
                .setNegativeButton("Позже", null)
                .show();
    }

    private void installApk() {
        if (downloadedApk == null || !downloadedApk.exists()) {
            showErrorDialog("Файл приложения не найден.");
            return;
        }

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri apkUri;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                String authority = activity.getPackageName() + ".fileprovider";
                apkUri = FileProvider.getUriForFile(activity, authority, downloadedApk);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                apkUri = Uri.fromFile(downloadedApk);
            }

            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error installing APK", e);
            showErrorDialog("Не удалось запустить установку приложения.");
        }
    }

    private void showErrorDialog(String message) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle("Ошибка")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}