package com.alaka_ala.florafilm.ui.util.updater;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AppUpdater {
    private static final String APK_URL = "https://github.com/varnavsky07rus/FloraFilmV2/raw/refs/heads/master/app/release/app-release.apk";
    private static final String METADATA_URL = "https://github.com/varnavsky07rus/FloraFilmV2/raw/master/app/release/output-metadata.json";
    private static final String APK_NAME = "FloraFilm_update.apk";

    private Context context;
    private ProgressDialog progressDialog;

    public AppUpdater(Context context) {
        this.context = context;
    }

    public void checkForUpdate(String currentVersion) {
        new CheckUpdateTask().execute(currentVersion);
    }

    private class CheckUpdateTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String currentVersion = params[0];
            try {
                URL url = new URL(METADATA_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                String json = convertStreamToString(inputStream);
                JSONObject metadata = new JSONObject(json);
                JSONObject elements = metadata.getJSONArray("elements").getJSONObject(0);
                String versionName = elements.getString("versionName");

                return !versionName.equals(currentVersion);
            } catch (Exception e) {
                Log.e("AppUpdater", "Update check failed", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean updateAvailable) {
            if (updateAvailable) {
                showUpdateDialog();
            }
        }
    }

    private void showUpdateDialog() {
        new AlertDialog.Builder(context)
                .setTitle("Доступно обновление")
                .setMessage("Хотите скачать и установить новую версию?")
                .setPositiveButton("Обновить", (dialog, which) -> downloadApk())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void downloadApk() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Загрузка обновления...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.show();

        new DownloadApkTask().execute(APK_URL);
    }

    private class DownloadApkTask extends AsyncTask<String, Integer, File> {
        @Override
        protected File doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                int fileLength = connection.getContentLength();
                File outputFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), APK_NAME);

                try (InputStream input = new BufferedInputStream(connection.getInputStream());
                     FileOutputStream output = new FileOutputStream(outputFile)) {

                    byte[] data = new byte[4096];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        if (isCancelled()) {
                            input.close();
                            return null;
                        }
                        total += count;
                        if (fileLength > 0) {
                            publishProgress((int) (total * 100 / fileLength));
                        }
                        output.write(data, 0, count);
                    }
                }
                return outputFile;
            } catch (Exception e) {
                Log.e("AppUpdater", "Download failed", e);
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            progressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(File apkFile) {
            progressDialog.dismiss();
            if (apkFile != null) {
                installApk(apkFile);
            } else {
                Toast.makeText(context, "Ошибка загрузки", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void installApk(File apkFile) {
        try {
            Uri apkUri;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                apkUri = FileProvider.getUriForFile(
                        context,
                        context.getPackageName() + ".provider",
                        apkFile);
            } else {
                apkUri = Uri.fromFile(apkFile);
            }

            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setDataAndType(apkUri, "application/vnd.android.package-archive");
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(install);
        } catch (Exception e) {
            Log.e("AppUpdater", "Installation failed", e);
            Toast.makeText(context, "Ошибка установки", Toast.LENGTH_SHORT).show();
        }
    }

    private String convertStreamToString(InputStream is) {
        try (java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }
}