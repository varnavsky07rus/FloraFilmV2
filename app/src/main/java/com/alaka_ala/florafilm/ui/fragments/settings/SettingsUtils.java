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


}
