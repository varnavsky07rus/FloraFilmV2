package com.alaka_ala.florafilm.ui.util.api;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.Collection;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class BanCheker {
    private final boolean isDebug = true;
    private final String TAG = "BanCheker";
    public static final String CACHE_DIR_NAME = "banlist";
    public static final String CACHE_FILE_NAME = "list.txt";
    public static final String FILE_NAME = "ban_list";
    public static final String FILE_URL_PATH = "https://raw.githubusercontent.com/varnavsky07rus/FloraFilmV2/refs/heads/master/app/src/main/res/raw/" + FILE_NAME;

    public BanCheker(Context context) {
        this.context = context;
    }

    public interface LoaderCallback {
        void onFinish();
    }

    private final Context context;

    public void loadList(LoaderCallback loaderCallback) {
        Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                loaderCallback.onFinish();
                return false;
            }
        });

        new Thread(() -> {
            Map<Integer, Boolean> banList = new HashMap<>();
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url(FILE_URL_PATH)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Ошибка при загрузке бан листа: " + response.code());
                    handler.sendEmptyMessage(1);
                    return;
                }

                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    Log.e(TAG, "Пустое тело ответа");
                    handler.sendEmptyMessage(1);
                    return;
                }

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(responseBody.byteStream()));
                String id;
                while ((id = bufferedReader.readLine()) != null) {
                    String idTrim = id.trim();
                    if (!idTrim.isEmpty()) {
                        banList.put(Integer.parseInt(idTrim), true);
                    }
                }

                if (cacheList(banList)) {
                    Log.d(TAG, "Бан лист успешно загружен");
                    handler.sendEmptyMessage(0);
                } else {
                    handler.sendEmptyMessage(1);
                    Log.e(TAG, "Ошибка при загрузке бан листа");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Ошибка: " + e.getMessage());
                handler.sendEmptyMessage(1);
            }
        }).start();
    }

    // Сохранение данных в кэш приложения
    private boolean cacheList(Map<Integer, Boolean> banList) {
        File cacheDir = new File(context.getFilesDir(), CACHE_DIR_NAME);
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            if (isDebug) Log.e(TAG, "Не удалось создать директорию кэша списка бан листа: " + cacheDir.getAbsolutePath());
            return false;
        }

        File cacheFile = new File(cacheDir, CACHE_FILE_NAME);

        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(cacheFile.toPath()))) {
            oos.writeObject(banList);
            if (isDebug) Log.d(TAG, "Кэш бан листа успешно сохранен в " + cacheFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            if (isDebug) Log.e(TAG, "Ошибка при сохранении кэша бан листа", e);
            return false;
        }
    }

    private Map<Integer, Boolean> loadMapFromFile() {
        File cacheFile = new File(new File(context.getFilesDir(), CACHE_DIR_NAME), CACHE_FILE_NAME);

        if (!cacheFile.exists()) {
            if (isDebug) Log.d(TAG, "Файл кэша бан листа не найден.");
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(cacheFile.toPath()))) {
            Object object = ois.readObject();
            if (isDebug) Log.d(TAG, "Кэш бан листа успешно загружен из " + cacheFile.getAbsolutePath());
            return (Map<Integer, Boolean>) object;
        } catch (IOException | ClassNotFoundException e) {
            if (isDebug) Log.e(TAG, "Ошибка при загрузке кэша бан листа ", e);
            // В случае ошибки поврежденный файл кэша лучше удалить
            cacheFile.delete();
            return null;
        }
    }

    public boolean isBan(int kinopoisk_id) {
        String vName = getAppVersionName();
        Pattern symbolName = Pattern.compile("(?<=\\_)[a-zA-Z]+");
        Matcher matcher = symbolName.matcher(vName);
        if (matcher.find()) {
            vName = matcher.group();
        }
        if (isDebug) Log.d(TAG, "Версия приложения: " + vName);
        if (vName.equals("nt")) {
            return false;
        }
        Map<Integer, Boolean> banList = loadMapFromFile();
        if (banList == null) {
            return false;
        }
        boolean isBan = banList.containsKey(kinopoisk_id);

        if (isBan) {
            createDialog();
        }

        return isBan;
    }
    /**
     * Получает имя версии приложения.
     *
     * @return Имя версии приложения или "N/A" в случае ошибки.
     */

    public String getAppVersionName() {
        try {
            String packageName = context.getPackageName();
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "N/A";
        }
    }


    private void createDialog() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new MaterialAlertDialogBuilder(context).setTitle("Сожалеем :(").setMessage("Фильм заблокирован по требованию правообладателя").create().show();
            }
        }, 100);

    }


}
