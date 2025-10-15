package com.alaka_ala.florafilm.ui.util.updater;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.ui.fragments.settings.SettingsUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AppUpdater {
    private static final String TAG = "AppUpdater";
    private static final String APK_URL = "https://github.com/varnavsky07rus/FloraFilmV2/raw/refs/heads/master/app/release/app-release.apk";
    private static final String VERSION_URL = "https://raw.githubusercontent.com/varnavsky07rus/FloraFilmV2/refs/heads/master/app/release/output-metadata.json";
    private static final String APK_URL_BETA_VERSION = "https://github.com/varnavsky07rus/FloraFilmV2/raw/refs/heads/beta/app/release/app-release.apk";
    private static final String VERSION_URL_BETA = "https://raw.githubusercontent.com/varnavsky07rus/FloraFilmV2/refs/heads/beta/app/release/output-metadata.json";
    private static final int REQUEST_INSTALL_PERMISSION = 1001;
    private static final String TEMP_APK_NAME = "update_temp.apk";

    // Constants for SharedPreferences
    private static final String PREFS_NAME = "AppUpdater";
    private static final String KEY_NEW_VERSION_CODE = "new_version_code";
    private static final String KEY_DOWNLOADED_APK_PATH = "downloaded_apk_path";
    private static final String KEY_UPDATE_ACTION = "update_action";


    // Activity нужна для отображения диалогов.
    // Важно: если Activity будет уничтожена во время работы, возможны ошибки.
    private final Activity activity;
    private AlertDialog downloadDialog;
    private ProgressBar progressBar;
    private TextView progressText;
    private TextView statusText;
    private File downloadedApk;
    private int newVersionCode;

    private final boolean isUpdateBetaVersion;
    private final boolean isSilentFindUpdate;

    // Используем ExecutorService для выполнения задач в фоновом потоке
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    // Используем Handler для отправки результатов в основной поток
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public AppUpdater(Activity activity, boolean isSilentFindUpdate) {
        this.activity = activity;
        this.isSilentFindUpdate = isSilentFindUpdate;
        this.isUpdateBetaVersion = SettingsUtils.getParamBetaVersion(activity);
    }

    public boolean isSilentFindUpdate() {
        return isSilentFindUpdate;
    }

    public interface CallbackCheckUpdate {
        void onFinish(boolean isUpdateAvailable);
    }

    /**
     * Запускает проверку наличия обновлений в фоновом потоке.
     */
    public void checkForUpdate(CallbackCheckUpdate cb) {
        executor.execute(() -> {
            try {
                // Код, который раньше был в doInBackground
                PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
                int currentVersionCode = pInfo.versionCode;

                String versionUrl = isUpdateBetaVersion ? VERSION_URL_BETA : VERSION_URL;
                Integer latestVersionCode = getLatestVersionCodeFromServer(versionUrl, currentVersionCode);

                // Возвращаем результат в основной поток
                mainThreadHandler.post(() -> onVersionCheckComplete(latestVersionCode, currentVersionCode, cb));

            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Package info error", e);
                mainThreadHandler.post(() -> onVersionCheckComplete(null, 0, cb));
            }
        });
    }

    private Integer getLatestVersionCodeFromServer(String urlString, int currentVersion) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(urlString)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return currentVersion;  // Возвращаем текущую версию, если ошибка ответа
            }

            String responseBody = response.body().string();
            JSONObject jsonObject = new JSONObject(responseBody);
            return jsonObject.getJSONArray("elements")
                    .getJSONObject(0)
                    .getInt("versionCode");

        } catch (IOException | JSONException e) {
            Log.e(TAG, "Version check error", e);
            return currentVersion;  // Возвращаем текущую версию, если ошибка запроса или парсинга
        }
    }


    /**
     * Выполняется в основном потоке после завершения проверки версии.
     * Аналог onPostExecute из AsyncTask.
     */
    private void onVersionCheckComplete(Integer latestVersionCode, int currentVersionCode, CallbackCheckUpdate cb) {
        if (activity.isFinishing()) return; // Проверка, что Activity еще "жива"

        if (latestVersionCode == null) {
            showErrorDialog("Ошибка поиска обновлений");
            return;
        }

        if (latestVersionCode > currentVersionCode) {
            newVersionCode = latestVersionCode;
            SharedPreferences preferences = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            preferences.edit().putInt(KEY_NEW_VERSION_CODE, newVersionCode).apply();
            showUpdateDialog();
        } else {
            SharedPreferences preferences = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            preferences.edit().putBoolean("upd", false).apply();
            if (!isSilentFindUpdate) {
                showMessageDialog("Обновлений не найдено!");
            }
        }
        // Возвращаем результат в MainActivity
        if (cb != null) cb.onFinish(latestVersionCode > currentVersionCode);
    }

    private void showUpdateDialog() {
        if (!isSilentFindUpdate) {
            MaterialAlertDialogBuilder alertBuilder = new MaterialAlertDialogBuilder(activity)
                    .setTitle("Доступно обновление").setMessage("Доступна новая версия приложения, установить сейчас?")
                    .setPositiveButton("Да", (dialog, which) -> {
                        if (canInstallApk()) {
                            prepareDownload();
                        } else {
                            SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                            prefs.edit().putString(KEY_UPDATE_ACTION, "DOWNLOAD").apply();
                            requestInstallPermission();
                        }
                    })
                    .setNegativeButton("Позже", null);
            AlertDialog alert = alertBuilder.create();
            alert.show();
            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(activity.getResources().getColor(R.color.buttonAlertCancel));
            alert.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(activity.getResources().getColor(R.color.buttonAlertSubmit));


        }
        SharedPreferences preferences = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putBoolean("upd", true).apply();
    }

    public boolean isAvailableUpdate() {
        SharedPreferences preferences = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean("upd", false);
    }

    private void prepareDownload() {
        File tempDir = new File(activity.getCacheDir(), "updates");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        downloadedApk = new File(tempDir, TEMP_APK_NAME);
        if (downloadedApk.exists()) {
            downloadedApk.delete();
        }

        startDownload();
    }

    private void startDownload() {
        showDownloadDialog();
        // Запускаем загрузку в фоновом потоке
        executor.execute(this::downloadApkFile);
    }

    /**
     * Основной метод загрузки, выполняется в фоновом потоке.
     */
    private void downloadApkFile() {
        HttpURLConnection connection = null;
        InputStream input = null;
        OutputStream output = null;
        try {
            mainThreadHandler.post(() -> updateStatus("Подготовка к загрузке..."));

            URL url = new URL(isUpdateBetaVersion ? APK_URL_BETA_VERSION : APK_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                mainThreadHandler.post(() -> onDownloadComplete(false));
                return;
            }

            int fileLength = connection.getContentLength();
            input = new BufferedInputStream(connection.getInputStream());
            output = new FileOutputStream(downloadedApk);

            byte[] data = new byte[4096];
            long total = 0;
            int count;
            mainThreadHandler.post(() -> updateStatus("Загрузка..."));
            while ((count = input.read(data)) != -1) {
                total += count;
                output.write(data, 0, count);

                // Рассчитываем и публикуем прогресс
                if (fileLength > 0) {
                    int progress = (int) (total * 100 / fileLength);
                    mainThreadHandler.post(() -> updateDownloadProgress(progress));
                }
            }
            output.flush();
            mainThreadHandler.post(() -> onDownloadComplete(true));

        } catch (IOException e) {
            Log.e(TAG, "Download error", e);
            mainThreadHandler.post(() -> onDownloadComplete(false));
        } finally {
            try {
                if (output != null) output.close();
                if (input != null) input.close();
            } catch (IOException ignored) {}
            if (connection != null) connection.disconnect();
        }
    }

    /**
     * Выполняется в основном потоке после завершения загрузки.
     * Аналог onPostExecute.
     */
    private void onDownloadComplete(boolean success) {
        if (activity.isFinishing()) return;

        if (downloadDialog != null && downloadDialog.isShowing()) {
            downloadDialog.dismiss();
        }

        if (success && downloadedApk.exists()) {
            SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_DOWNLOADED_APK_PATH, downloadedApk.getAbsolutePath()).apply();
            verifyAndInstall();
        } else {
            showErrorDialog("Ошибка загрузки!");
            cleanupTempFiles();
        }
    }

    @SuppressLint("MissingInflatedId")
    private void showDownloadDialog() {
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.download_progress_dialog, null);
        progressBar = dialogView.findViewById(R.id.progressBar);
        progressText = dialogView.findViewById(R.id.progressText);
        statusText = dialogView.findViewById(R.id.statusText);

        downloadDialog = new MaterialAlertDialogBuilder(activity)
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
        MaterialAlertDialogBuilder alertBuilder = new MaterialAlertDialogBuilder(activity)
                .setTitle("Установка обновления")
                .setMessage("Обновление загружено. Установить сейчас?")
                .setPositiveButton("Установить", (dialog, which) -> installApk())
                .setNegativeButton("Позже", (dialog, which) -> cleanupTempFiles());
        AlertDialog alert = alertBuilder.create();
        alert.show();
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(activity.getResources().getColor(R.color.buttonAlertCancel));
        alert.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(activity.getResources().getColor(R.color.buttonAlertSubmit));
    }

    private void installApk() {
        if (!canInstallApk()) {
            SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_UPDATE_ACTION, "INSTALL").apply();
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
        MaterialAlertDialogBuilder alertBuilder = new MaterialAlertDialogBuilder(activity)
                .setTitle("Требуется разрешение")
                .setMessage("Разрешите установку из неизвестных источников")
                .setPositiveButton("К настройкам", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    intent.setData(Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivityForResult(intent, REQUEST_INSTALL_PERMISSION);
                })
                .setNegativeButton("Отмена", (dialog, which) -> cleanupTempFiles());
        AlertDialog alert = alertBuilder.create();
        alert.show();
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(activity.getResources().getColor(R.color.buttonAlertCancel));
        alert.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(activity.getResources().getColor(R.color.buttonAlertSubmit));
    }

    private void proceedWithInstallation() {
        if (downloadedApk == null || !downloadedApk.exists()) {
            SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String apkPath = prefs.getString(KEY_DOWNLOADED_APK_PATH, null);
            if (apkPath != null) {
                this.downloadedApk = new File(apkPath);
            } else {
                showErrorDialog("Файл обновления не найден");
                return;
            }
        }

        try {
            Uri apkUri = FileProvider.getUriForFile(
                    activity,
                    activity.getPackageName() + ".fileprovider",
                    downloadedApk
            );

            Intent installIntent = getIntentInstall(apkUri);

            activity.startActivity(installIntent);
            new Handler(Looper.getMainLooper()).postDelayed(this::cleanupTempFiles, 100000);
        } catch (Exception e) {
            Log.e(TAG, "Installation error", e);
            showErrorDialog("Ошибка установки: " + e.getMessage());
            cleanupTempFiles();
        }
    }

    @NonNull
    private static Intent getIntentInstall(Uri apkUri) {
        Intent installIntent = new Intent(Intent.ACTION_VIEW); // ACTION_INSTALL_PACKAGE устарел
        installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        installIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        return installIntent;
    }

    private void cleanupTempFiles() {
        // Запускаем очистку тоже в фоне, чтобы не блокировать UI
        executor.execute(() -> {
            try {
                if (downloadedApk != null && downloadedApk.exists()) {
                    downloadedApk.delete();
                }
                SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                prefs.edit()
                        .remove(KEY_NEW_VERSION_CODE)
                        .remove(KEY_DOWNLOADED_APK_PATH)
                        .remove(KEY_UPDATE_ACTION)
                        .apply();
            } catch (Exception e) {
                Log.e(TAG, "Error cleaning temp files", e);
            }
        });
    }

    private void showMessageDialog(String message) {
        MaterialAlertDialogBuilder alertBuilder = new MaterialAlertDialogBuilder(activity)
                .setMessage(message)
                .setPositiveButton("OK", null);
        AlertDialog alert = alertBuilder.create();
        alert.show();
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(activity.getResources().getColor(R.color.buttonAlertSubmit));
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(activity.getResources().getColor(R.color.buttonAlertCancel));
    }

    private void showErrorDialog(String message) {
        MaterialAlertDialogBuilder alertBuilder = new MaterialAlertDialogBuilder(activity)
                .setTitle("Ошибка")
                .setMessage(message)
                .setPositiveButton("OK", null);
        AlertDialog alert = alertBuilder.create();
        alert.show();
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(activity.getResources().getColor(R.color.buttonAlertSubmit));
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(activity.getResources().getColor(R.color.buttonAlertCancel));
    }

    // Метод для обработки результата из Activity
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_INSTALL_PERMISSION) {
            // Всегда перезагружайте состояние, так как процесс мог быть убит.
            SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            this.newVersionCode = prefs.getInt(KEY_NEW_VERSION_CODE, 0);
            String apkPath = prefs.getString(KEY_DOWNLOADED_APK_PATH, null);
            if (apkPath != null) {
                this.downloadedApk = new File(apkPath);
            }

            if (canInstallApk()) {
                String action = prefs.getString(KEY_UPDATE_ACTION, null);
                if ("DOWNLOAD".equals(action)) {
                    prepareDownload();
                } else if ("INSTALL".equals(action)) {
                    proceedWithInstallation();
                }
            } else {
                showErrorDialog("Ошибка установки! Разрешения на установку из неизвестных источников не выданы!");
                cleanupTempFiles(); // Это также очистит преференсы
            }

            // Очистите ключ действия, но пока не остальные.
            prefs.edit().remove(KEY_UPDATE_ACTION).apply();
        }
    }
}