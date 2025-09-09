package com.alaka_ala.florafilm.ui.util.updater;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppUpdater {
    private static final String TAG = "AppUpdater";
    private static final String APK_URL = "https://github.com/varnavsky07rus/FloraFilmV2/raw/refs/heads/master/app/release/app-release.apk";
    private static final String VERSION_URL = "https://raw.githubusercontent.com/varnavsky07rus/FloraFilmV2/refs/heads/master/app/release/output-metadata.json";
    private static final String APK_URL_BETA_VERSION = "https://github.com/varnavsky07rus/FloraFilmV2/raw/refs/heads/beta/app/release/app-release.apk";
    private static final String VERSION_URL_BETA = "https://raw.githubusercontent.com/varnavsky07rus/FloraFilmV2/refs/heads/beta/app/release/output-metadata.json";
    private static final int REQUEST_INSTALL_PERMISSION = 1001;
    private static final String TEMP_APK_NAME = "update_temp.apk";

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
                Integer latestVersionCode = getLatestVersionCodeFromServer(versionUrl);

                // Возвращаем результат в основной поток
                mainThreadHandler.post(() -> onVersionCheckComplete(latestVersionCode, currentVersionCode, cb));

            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Package info error", e);
                mainThreadHandler.post(() -> onVersionCheckComplete(null, 0, cb));
            }
        });
    }

    private Integer getLatestVersionCodeFromServer(String urlString) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
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
            showUpdateDialog();
        } else {
            SharedPreferences preferences = activity.getSharedPreferences("AppUpdater", Context.MODE_PRIVATE);
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
            new AlertDialog.Builder(activity)
                    .setTitle("Доступно обновление")
                    .setMessage("Доступна новая версия приложения, установить сейчас?")
                    .setPositiveButton("Да", (dialog, which) -> prepareDownload())
                    .setNegativeButton("Позже", null)
                    .show();
        }
        SharedPreferences preferences = activity.getSharedPreferences("AppUpdater", Context.MODE_PRIVATE);
        preferences.edit().putBoolean("upd", true).apply();
    }

    public boolean isAvailableUpdate() {
        SharedPreferences preferences = activity.getSharedPreferences("AppUpdater", Context.MODE_PRIVATE);
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

    // --- Остальные методы остаются без изменений ---

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
            new android.os.Handler(Looper.getMainLooper()).postDelayed(this::cleanupTempFiles, 100000);
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
            } catch (Exception e) {
                Log.e(TAG, "Error cleaning temp files", e);
            }
        });
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

    // Метод для обработки результата из Activity
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_INSTALL_PERMISSION) {
            if (canInstallApk()) {
                proceedWithInstallation();
            } else {
                showErrorDialog("Ошибка установки! Разрешения на установку из неизвестных источников не выданы!");
            }
        }
    }
}