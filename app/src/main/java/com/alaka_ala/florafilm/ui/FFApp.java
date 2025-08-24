package com.alaka_ala.florafilm.ui;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;

public class FFApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Инициализация Firebase
        // FirebaseApp.initializeApp(this);
        FirebaseApp.initializeApp(this);
        Log.d("FFApp", "onCreate: Firebase initialized");
    }
}
