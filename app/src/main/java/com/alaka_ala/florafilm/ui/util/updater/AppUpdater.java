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
    private static final int REQUEST_INSTALL_PERMISSION = 1001;

    private final Activity activity;
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_INSTALL_PERMISSION) {
            if (canInstallApk()) {
                proceedWithInstallation();
            } else {
                showErrorDialog("Разрешение на установку не предоставлено");
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
                showErrorDialog("Ошибка проверки обновления");
                return;
            }

            if (latestVersionCode > currentVersionCode) {
                showUpdateDialog(latestVersionCode);
            } else {
                showMessageDialog("У вас последняя версия приложения");
            }
        }
    }

    private void showUpdateDialog(int newVersionCode) {
        new AlertDialog.Builder(activity)
                .setTitle("Доступно обновление")
                .setMessage("Доступна новая версия приложения. Обновить сейчас?")
                .setPositiveButton("Обновить", (dialog, which) -> checkExistingApk(newVersionCode))
                .setNegativeButton("Позже", null)
                .show();
    }

    private void checkExistingApk(int newVersionCode) {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        downloadedApk = new File(downloadsDir, "app-release.apk");

        if (downloadedApk.exists() && downloadedApk.length() > 0) {
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
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.download_progress_dialog, null);
        progressBar = dialogView.findViewById(R.id.progressBar);
        progressText = dialogView.findViewById(R.id.progressText);
        statusText = dialogView.findViewById(R.id.statusText);

        downloadDialog = new AlertDialog.Builder(activity)
                .setView(dialogView)
                .setTitle("Загрузка обновления")
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
        @Override
        protected void onPreExecute() {
            updateStatus("Подготовка к загрузке...");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(APK_URL).openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return false;
                }

                int fileLength = connection.getContentLength();
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                downloadedApk = new File(downloadsDir, "app-release.apk");

                try (InputStream input = connection.getInputStream();
                     FileOutputStream output = new FileOutputStream(downloadedApk)) {

                    byte[] data = new byte[4096];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        if (isCancelled()) {
                            downloadedApk.delete();
                            return false;
                        }
                        total += count;
                        if (fileLength > 0) {
                            publishProgress((int) (total * 100 / fileLength));
                        }
                        output.write(data, 0, count);
                    }
                }
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Download error", e);
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

            if (success && downloadedApk != null && downloadedApk.exists()) {
                showInstallDialog();
            } else {
                showErrorDialog("Ошибка загрузки обновления");
            }
        }
    }

    private void showInstallDialog() {
        new AlertDialog.Builder(activity)
                .setTitle("Установка обновления")
                .setMessage("Обновление загружено. Установить сейчас?")
                .setPositiveButton("Установить", (dialog, which) -> installApk())
                .setNegativeButton("Позже", null)
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
                .setMessage("Для установки обновления разрешите установку из неизвестных источников")
                .setPositiveButton("Настройки", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    intent.setData(Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivityForResult(intent, REQUEST_INSTALL_PERMISSION);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void proceedWithInstallation() {
        if (downloadedApk == null || !downloadedApk.exists()) {
            showErrorDialog("Файл обновления не найден");
            return;
        }

        try {
            Uri apkUri = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                    ? FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", downloadedApk)
                    : Uri.fromFile(downloadedApk);

            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(installIntent);
        } catch (Exception e) {
            Log.e(TAG, "Installation error", e);
            showErrorDialog("Ошибка установки: " + e.getMessage());
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