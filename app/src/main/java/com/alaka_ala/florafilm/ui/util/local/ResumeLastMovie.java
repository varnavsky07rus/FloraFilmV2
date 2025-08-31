package com.alaka_ala.florafilm.ui.util.local;

import android.content.Context;
import android.content.SharedPreferences;

public class ResumeLastMovie {
    private final String FILE_ID_LFM = "saveLastFilm";
    private final SharedPreferences preferences;
    private final String KEY_KINOPOISK_ID = "KINOPOISKID";
    private final String KEY_NAME = "NAME";
    private final String KEY_IMG_URL = "IMGURL";
    private final String KEY_FIRST_LAUNCH = "firstLaunch";


    public ResumeLastMovie(Context context) {
        preferences = context.getSharedPreferences(FILE_ID_LFM, Context.MODE_PRIVATE);
    }

    public void saveLastMovie(int kinopoiskId, String name, String imgUrl) {
        preferences.edit().putInt(KEY_KINOPOISK_ID, kinopoiskId).apply();
        preferences.edit().putString(KEY_NAME, name).apply();
        preferences.edit().putString(KEY_IMG_URL, imgUrl).apply();
    }

    public String getName() {
        return preferences.getString(KEY_NAME, "");
    }

    public int getKinopoiskId(){
        return preferences.getInt(KEY_KINOPOISK_ID, 0);
    }

    public String getImgUrl() {
        return preferences.getString(KEY_IMG_URL, "");
    }

    public void clear() {
        preferences.edit().clear().apply();
    }

    public boolean existLastMovie() {
        return preferences.contains(KEY_KINOPOISK_ID) && preferences.contains(KEY_NAME) && preferences.contains(KEY_IMG_URL);
    }

    public boolean isFirstLaunch() {
        return preferences.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    public void setLaunched() {
        preferences.edit().putBoolean(KEY_FIRST_LAUNCH, true).apply();
    }

    public void setNotLaunched() {
        preferences.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
    }
}
