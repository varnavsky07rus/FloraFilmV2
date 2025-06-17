package com.alaka_ala.florafilm.ui.util.updater;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;
import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AppUpdater {
    private final Context context;
    private final OkHttpClient client = new OkHttpClient();
    private final String metadataUrl = "https://raw.githubusercontent.com/varnavsky07rus/FloraFilmV2/refs/heads/master/app/release/output-metadata.json";
    private final String apkUrl = "https://github.com/varnavsky07rus/FloraFilmV2/raw/refs/heads/master/app/release/app-release.apk";

    private static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Mobile Safari/537.36";
    public AppUpdater(Context context) {
        this.context = context;
    }

    public void checkForUpdate(String currentVersion) {
        Request request = new Request.Builder()
                .url(metadataUrl)
                .addHeader("User-Agent", USER_AGENT)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showToast("Ошибка подключения: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    showToast("Ошибка сервера: " + response.code());
                    return;
                }

                try {
                    String jsonData = response.body().string();
                    JSONObject json = new JSONObject(jsonData);
                    JSONArray elements = json.getJSONArray("elements");
                    String latestVersion = elements.getJSONObject(0).getString("versionName");

                    if (!currentVersion.equals(latestVersion)) {
                        showUpdateDialog();
                    } else {
                        showToast("У вас последняя версия");
                    }
                } catch (Exception e) {
                    showToast("Ошибка обработки данных: " + e.getMessage());
                }
            }
        });
    }

    private void showToast(final String message) {
        if (context != null) {
            ((Activity) context).runOnUiThread(() ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
        }
    }

    private void showUpdateDialog() {
        ((Activity) context).runOnUiThread(() ->
                new AlertDialog.Builder(context)
                        .setTitle("Доступно обновление")
                        .setMessage("Хотите обновить приложение?")
                        .setPositiveButton("Обновить", (dialog, which) -> downloadApk())
                        .setNegativeButton("Позже", null)
                        .show());
    }

    private void downloadApk() {
        showToast("Начало загрузки...");

        Request request = new Request.Builder()
                .url(apkUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showToast("Ошибка загрузки: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    showToast("Ошибка загрузки: " + response.code());
                    return;
                }

                try {
                    InputStream inputStream = response.body().byteStream();
                    File apkFile = new File(context.getExternalFilesDir(null), "update.apk");

                    try (FileOutputStream outputStream = new FileOutputStream(apkFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }

                    installApk(apkFile);
                    showToast("Обновление загружено");
                } catch (Exception e) {
                    showToast("Ошибка сохранения: " + e.getMessage());
                }
            }
        });
    }

    private void installApk(File apkFile) {
        Uri apkUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
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
    }
}