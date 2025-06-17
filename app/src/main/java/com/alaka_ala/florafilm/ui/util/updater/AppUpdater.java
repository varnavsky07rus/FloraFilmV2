package com.alaka_ala.florafilm.ui.util.updater;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
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

import androidx.core.content.FileProvider;

import com.alaka_ala.florafilm.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AppUpdater {

    private static final String TAG = "AppUpdater";
    private static final String APK_URL = "https://github.com/varnavsky07rus/FloraFilmV2/raw/refs/heads/master/app/release/app-release.apk";
    private static final String VERSION_URL = "https://raw.githubusercontent.com/varnavsky07rus/FloraFilmV2/refs/heads/master/app/release/output-metadata.json";

    private Activity activity;
    private AlertDialog downloadDialog;
    private ProgressBar progressBar;
    private TextView progressText;
    private TextView statusText;
    private File downloadedApk;

    public AppUpdater(Activity activity) {
        this.activity = activity;
    }

    public void checkForUpdate() {
        new CheckVersionTask().execute();
    }

    private class CheckVersionTask extends AsyncTask<Void, Void, Integer> {
        private int currentVersionCode;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                currentVersionCode = activity.getPackageManager()
                        .getPackageInfo(activity.getPackageName(), 0)
                        .versionCode;
            } catch (Exception e) {
                Log.e(TAG, "Could not get package info", e);
                cancel(true);
            }
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                URL url = new URL(VERSION_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage());
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
                Log.e(TAG, "Error checking version", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer latestVersionCode) {
            if (latestVersionCode == null) {
                showErrorDialog("Не удалось проверить обновление. Проверьте подключение к интернету.");
                return;
            }

            if (latestVersionCode > currentVersionCode) {
                showUpdateDialog(latestVersionCode);
            } else {
                showMessageDialog("У вас установлена последняя версия приложения.");
            }
        }
    }

    private void showUpdateDialog(int newVersionCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Доступно обновление");
        builder.setMessage("Доступна новая версия приложения. Хотите обновить сейчас?");

        builder.setPositiveButton("Обновить", (dialog, which) -> {
            checkExistingApk(newVersionCode);
        });

        builder.setNegativeButton("Позже", null);
        builder.show();
    }

    private void checkExistingApk(int newVersionCode) {
        // Проверяем, есть ли уже скачанный APK
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        downloadedApk = new File(downloadsDir, "app-release.apk");

        if (downloadedApk.exists()) {
            // Проверяем версию скачанного APK (это упрощенная проверка)
            // В реальном приложении нужно было бы прочитать версию из APK
            showInstallDialog();
        } else {
            startDownload();
        }
    }

    private void startDownload() {
        showDownloadDialog();
        new DownloadApkTask().execute();
    }

    @SuppressLint("MissingInflatedId")
    private void showDownloadDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.download_progress_dialog, null); // Создайте этот layout

        progressBar = dialogView.findViewById(R.id.progressBar);
        progressText = dialogView.findViewById(R.id.progressText);
        statusText = dialogView.findViewById(R.id.statusText);

        builder.setView(dialogView);
        builder.setTitle("Загрузка обновления");
        builder.setCancelable(false);

        downloadDialog = builder.create();
        downloadDialog.show();
    }

    private void updateDownloadProgress(int progress) {
        if (progressBar != null) {
            progressBar.setProgress(progress);
        }
        if (progressText != null) {
            progressText.setText(progress + "%");
        }
    }

    private void updateStatus(String status) {
        if (statusText != null) {
            statusText.setText(status);
        }
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
                downloadedApk = new File(downloadsDir, "app-release.apk");

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
            super.onProgressUpdate(values);
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
                showErrorDialog("Ошибка загрузки обновления. Пожалуйста, попробуйте позже.");
            }
        }
    }

    private void showInstallDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Установка обновления");
        builder.setMessage("Обновление загружено. Установить сейчас?");

        builder.setPositiveButton("Установить", (dialog, which) -> installApk());
        builder.setNegativeButton("Позже", null);
        builder.show();
    }

    private void installApk() {
        if (downloadedApk == null || !downloadedApk.exists()) {
            showErrorDialog("Файл обновления не найден.");
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
            showErrorDialog("Не удалось запустить установку обновления.");
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
                .setTitle("Ошибка")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}