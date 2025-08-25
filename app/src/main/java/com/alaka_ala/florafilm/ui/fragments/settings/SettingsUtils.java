package com.alaka_ala.florafilm.ui.fragments.settings;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsUtils {
    private static final String KEY_PREFERENCES= "KEY_SETTINGS_NAME_PREFERENCES";
    //==============================================================================================
    public static final boolean DEF_SEARCH_MODE_VIBIX = true;
    public static final String KEY_SEARCH_MODE_VIBIX = "KEY_SEARCH_MODE_SERIAL_1x00000";
    //==============================================================================================
    public static final boolean DEF_SEARCH_MODE_HDVB = true;
    public static final String KEY_SEARCH_MODE_HDVB = "KEY_SEARCH_MODE_SERIAL_2x00000";
    // =============================================================================================
    public static final boolean DEF_BETA_VERSION_MODE = false;
    public static final String KEY_BETA_VERSION_MODE = "KEY_BETA_VERSION_MODE_3x00000";
    // =============================================================================================
    public static final boolean DEF_SEARCH_MODE_TORRENT = false;
    public static final String KEY_SEARCH_MODE_TORRENT = "KEY_SEARCH_MODE_TORRENT_4x00000";
    //==============================================================================================
    public static final boolean DEF_EFFECT_SCROLL_PAGE = true;
    public static final String KEY_EFFECT_SCROLL_PAGE = "KEY_EFFECT_SCROLL_PAGE_5x00000";
    //==============================================================================================
    public static final boolean DEF_EFFECT_ANIMATION = true;
    public static final String KEY_EFFECT_ANIMATION = "KEY_EFFECT_ANIMATION_6x00000";
    //==============================================================================================


    /**Взять параметр: Включен или отключен поиск сериалов по VIBIX*/
    public static boolean getParamSearchVIBIX(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(KEY_PREFERENCES, MODE_PRIVATE);
        return preferences.getBoolean(KEY_SEARCH_MODE_VIBIX, DEF_SEARCH_MODE_VIBIX);
    }

    public static void setParamSearchVibix(Context context, boolean param) {
        SharedPreferences preferences = context.getSharedPreferences(KEY_PREFERENCES, MODE_PRIVATE);
        preferences.edit().putBoolean(KEY_SEARCH_MODE_VIBIX, param).apply();
    }


    //==============================================================================================

    public static boolean getParamSeeachHDVB(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(KEY_PREFERENCES, MODE_PRIVATE);
        return preferences.getBoolean(KEY_SEARCH_MODE_HDVB, DEF_SEARCH_MODE_HDVB);
    }

    public static void setParamSearchHDVB(Context context, boolean param) {
        SharedPreferences preferences = context.getSharedPreferences(KEY_PREFERENCES, MODE_PRIVATE);
        preferences.edit().putBoolean(KEY_SEARCH_MODE_HDVB, param).apply();
    }

    //==============================================================================================

    public static boolean getParamBetaVersion(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(KEY_PREFERENCES, MODE_PRIVATE);
        return preferences.getBoolean(KEY_BETA_VERSION_MODE, DEF_BETA_VERSION_MODE);
    }

    public static void setParamBetaVersion(Context context, boolean param) {
        SharedPreferences preferences = context.getSharedPreferences(KEY_PREFERENCES, MODE_PRIVATE);
        preferences.edit().putBoolean(KEY_BETA_VERSION_MODE, param).apply();
    }

    //==============================================================================================

    public static boolean getParamSearchTorrent(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(KEY_PREFERENCES, MODE_PRIVATE);
        return preferences.getBoolean(KEY_SEARCH_MODE_TORRENT, DEF_SEARCH_MODE_TORRENT);
    }

    public static void setParamSearchTorrent(Context context, boolean param) {
        SharedPreferences preferences = context.getSharedPreferences(KEY_PREFERENCES, MODE_PRIVATE);
        preferences.edit().putBoolean(KEY_SEARCH_MODE_TORRENT, param).apply();
    }

    //==============================================================================================
    public static boolean getParamScrollPageEffect(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(KEY_PREFERENCES, MODE_PRIVATE);
        return preferences.getBoolean(KEY_EFFECT_SCROLL_PAGE, DEF_EFFECT_SCROLL_PAGE);
    }

    public static void setParamScrollPageEffect(Context context, boolean param) {
        SharedPreferences preferences = context.getSharedPreferences(KEY_PREFERENCES, MODE_PRIVATE);
        preferences.edit().putBoolean(KEY_EFFECT_SCROLL_PAGE, param).apply();
    }

    //==============================================================================================
    public static boolean getParamPageEffectAnimation(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(KEY_PREFERENCES, MODE_PRIVATE);
        return preferences.getBoolean(KEY_EFFECT_ANIMATION, DEF_EFFECT_ANIMATION);
    }

    public static void setParamPageEffectAnimation(Context context, boolean param) {
        SharedPreferences preferences = context.getSharedPreferences(KEY_PREFERENCES, MODE_PRIVATE);
        preferences.edit().putBoolean(KEY_EFFECT_ANIMATION, param).apply();
    }







}
