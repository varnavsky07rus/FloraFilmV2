package com.alaka_ala.florafilm.ui.util.player;

import android.content.Context;
import android.content.SharedPreferences;

public class PlaybackPositionManager {
    private static final String PREF_NAME = "playback_prefs";
    private static final String KEY_POSITION = "position_";

    private final SharedPreferences sharedPreferences;

    public PlaybackPositionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Сохранение позиции для фильма/серии
    public void savePosition(int mediaId, long position) {
        sharedPreferences.edit()
                .putLong(KEY_POSITION + mediaId, position)
                .apply();
    }

    // Получение сохраненной позиции
    public long getSavedPosition(int mediaId) {
        return sharedPreferences.getLong(KEY_POSITION + mediaId, 0);
    }

    // Очистка данных (например, после просмотра)
    public void clearPosition(int mediaId) {
        sharedPreferences.edit()
                .remove(KEY_POSITION + mediaId)
                .apply();
    }


    public void savePositionEpisode(int mediaId, int indexEpisode, int indexSeason, int indexTranslation, int indexQuality, long position, String balancer) {
        sharedPreferences.edit()
                .putInt("indexEpisode" + mediaId, indexEpisode)
                .putInt("indexSeason" + mediaId, indexSeason)
                .putInt("indexTranslation" + mediaId, indexTranslation)
                .putInt("indexQuality" + mediaId, indexQuality)
                .putLong(KEY_POSITION + mediaId + indexEpisode + indexSeason, position)
                .putString("balancer" + mediaId, balancer)
                .apply();
    }

    public int getSavedIndexEpisode(int mediaId) {
        return sharedPreferences.getInt("indexEpisode" + mediaId, 0);
    }

    public int getSavedIndexSeason(int mediaId) {
        return sharedPreferences.getInt("indexSeason" + mediaId, 0);
    }

    public int getSavedIndexTranslation(int mediaId) {
        return sharedPreferences.getInt("indexTranslation" + mediaId, 0);
    }

    public int getSavedIndexQuality(int mediaId) {
        return sharedPreferences.getInt("indexQuality" + mediaId, 0);
    }

    public long getSavedPositionEpisode(int mediaId, int indexEpisode, int indexSeason) {
        return sharedPreferences.getLong(KEY_POSITION + mediaId + indexEpisode + indexSeason, 0);
    }

    public String getSavedBalancer(int mediaId) {
        return sharedPreferences.getString("balancer" + mediaId, "");
    }






}
