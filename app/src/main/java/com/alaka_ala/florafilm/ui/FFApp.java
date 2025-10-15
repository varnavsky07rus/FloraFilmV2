package com.alaka_ala.florafilm.ui;

import android.app.ActivityManager;
import android.app.Application;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.alaka_ala.florafilm.BuildConfig;
import com.alaka_ala.florafilm.ui.util.coreTorrent.TorrentSessionService;
import com.google.firebase.FirebaseApp;

import java.util.List;

import io.appmetrica.analytics.AppMetrica;
import io.appmetrica.analytics.AppMetricaConfig;

public class FFApp extends Application {

    private boolean isFirstLaunch = true;

    @Override
    public void onCreate() {
        super.onCreate();
        // 1. Инициализируем Firebase для ВСЕХ типов сборок
        FirebaseApp.initializeApp(this);
        Log.d("FFApp", "Firebase initialized");

        // 2. Инициализируем AppMetrica только для RELEASE-сборок
        if (!BuildConfig.DEBUG) {
            // Создаем конфигурацию для AppMetrica
            AppMetricaConfig config = AppMetricaConfig.newConfigBuilder("1945eb04-4fda-4a26-9fcc-4d36e0f34551").build();
            // Активируем AppMetrica SDK
            AppMetrica.activate(getApplicationContext(), config);
            Log.d("FFApp", "AppMetrica initialized for RELEASE build.");
        } else {
            Log.d("FFApp", "AppMetrica is DISABLED for DEBUG build.");
        }


        new Handler(getMainLooper()).postDelayed(() -> {
            // Инициализируем Torrent Сессию
            if (TorrentSessionService.getInstance() != null) {
                return;
            }
            Intent intent = new Intent(this, TorrentSessionService.class);
            intent.setAction(TorrentSessionService.ACTION_SERVICE_INITIALIZE);
            startService(intent);
        }, 500);



    }

    public boolean isFirstLaunch() {
        return isFirstLaunch;
    }

    public void setNotFirstLaunch() {
        isFirstLaunch = false;
    }
}
