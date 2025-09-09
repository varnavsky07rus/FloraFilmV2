package com.alaka_ala.florafilm.ui;

import android.app.Application;
import android.util.Log;
import com.alaka_ala.florafilm.BuildConfig;
import com.google.firebase.FirebaseApp;

import io.appmetrica.analytics.AppMetrica;
import io.appmetrica.analytics.AppMetricaConfig;

public class FFApp extends Application {

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
    }
}
