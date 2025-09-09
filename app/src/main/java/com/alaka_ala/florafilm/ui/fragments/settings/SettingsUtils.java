package com.alaka_ala.florafilm.ui.fragments.settings;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Locale;

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
    public static final boolean DEF_LAYOUT_DESCRIPTION_FILM = true; // Если TRUE то используется новый макет описания фильма
    public static final String KEY_LAYOUT_DESCRIPTION_FILM = "KEY_LAYOUT_DESCRIPTION_FILM_7x00000";


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

    //==============================================================================================
    public static boolean getParamLayoutDescriptionFilm(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(KEY_PREFERENCES, MODE_PRIVATE);
        return preferences.getBoolean(KEY_LAYOUT_DESCRIPTION_FILM, DEF_LAYOUT_DESCRIPTION_FILM);
    }

    public static void setParamLayoutDescriptionFilm(Context context, boolean param) {
        SharedPreferences preferences = context.getSharedPreferences(KEY_PREFERENCES, MODE_PRIVATE);
        preferences.edit().putBoolean(KEY_LAYOUT_DESCRIPTION_FILM, param).apply();
    }

    //==============================================================================================
    
    public static String getSizeCacheApp(Context context) {
        long size = FileUtils.sizeOf(new File(context.getCacheDir().getPath()));
        long sizeFilesDir = FileUtils.sizeOf(context.getFilesDir());
        size += sizeFilesDir;
        return "Занято: " + formatSize(size);
    }

    public static void clearCache(Context context, boolean isDeleteAll) {
        try {
            FileUtils.deleteDirectory(context.getCacheDir());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (isDeleteAll) {
            try {
                FileUtils.deleteDirectory(context.getFilesDir());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String formatSize(long size) {
        if (size <= 0) {
            return "0 B";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
