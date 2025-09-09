package com.alaka_ala.florafilm.ui.fragments.geminiMovie;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alaka_ala.florafilm.BuildConfig;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.Collection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

public class GeminiMovieViewModel extends ViewModel {
    // Store a map of collections, keyed by kinopoisk_id
    private final MutableLiveData<Map<Integer, Collection>> geminiMovies = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<Map<Integer, Integer>> page = new MutableLiveData<>(new HashMap<>());

    private final boolean isDebug;
    private final String TAG = "GeminiMovieViewModel";
    private static final String CACHE_DIR_NAME = "cache_gemini";
    private static final String CACHE_FILE_NAME = "geminiMovieViewModelCache";
    private static final String CACHE_FILE_NAME_PAGE = "geminiMovieViewModelCachePage";
    private static final String PREFS_NAME = "GeminiMovieViewModelPrefs";
    private static final String KEY_LAST_CACHE_TIME = "last_cache_time";
    private static final long TWENTY_FOUR_HOURS_IN_MS = 24 * 60 * 60 * 1000L;
    public boolean isInitViewModel = false;

    public GeminiMovieViewModel() {
        this.isDebug = BuildConfig.DEBUG;
    }

    public void initData(Context context) {
        loadData(context);
        isInitViewModel = true;
    }

    public LiveData<Map<Integer, Collection>> getGeminiMovies() {
        return geminiMovies;
    }

    public Collection getCollectionForMovie(int kinopoiskId) {
        if (geminiMovies.getValue() != null) {
            return geminiMovies.getValue().get(kinopoiskId);
        }
        return null;
    }

    public void setGeminiMovies(int kinopoiskId, Collection collection) {
        Map<Integer, Collection> currentMap = geminiMovies.getValue();
        if (currentMap == null) {
            currentMap = new HashMap<>();
        }
        currentMap.put(kinopoiskId, collection);
        geminiMovies.setValue(currentMap);
    }

    public void addAllGeminiMovies(int kinopoiskId, Collection collection) {
        Map<Integer, Collection> currentMap = geminiMovies.getValue();
        if (currentMap != null && currentMap.containsKey(kinopoiskId)) {
            Collection existingCollection = currentMap.get(kinopoiskId);
            if (existingCollection != null) {
                existingCollection.getItems().addAll(collection.getItems());
                geminiMovies.setValue(currentMap); // Notify observers
            }
        } else {
            setGeminiMovies(kinopoiskId, collection);
        }
    }

    public Integer getPage(int kinopoiskId) {
        if (page.getValue() != null && page.getValue().containsKey(kinopoiskId)) {
            return page.getValue().get(kinopoiskId);
        }
        return 1; // Default to page 1
    }

    public void incrementPage(int kinopoiskId) {
        Map<Integer, Integer> currentPageMap = page.getValue();
        if (currentPageMap == null) {
            currentPageMap = new HashMap<>();
        }
        int currentPage = currentPageMap.getOrDefault(kinopoiskId, 1);
        currentPageMap.put(kinopoiskId, currentPage + 1);
        page.setValue(currentPageMap);
    }

    public void saveData(Context context) {
        Map<Integer, Collection> collectionMap = geminiMovies.getValue();
        Map<Integer, Integer> indexLastPage = page.getValue();

        if (collectionMap != null && !collectionMap.isEmpty()) {
            boolean isCollectionSaved = saveCollectionToFile(context, collectionMap);
            if (isCollectionSaved) {
                updateLastCacheTime(context);
            }
        } else {
            if (isDebug) Log.d(TAG, "Нет данных о коллекциях для сохранения в кэш.");
        }

        if (indexLastPage != null && !indexLastPage.isEmpty()) {
            boolean isPageSaved = savePageCollectionToFile(context, indexLastPage);
            if (isPageSaved) {
                updateLastCacheTime(context);
            }
        } else {
            if (isDebug) Log.d(TAG, "Нет данных о страницах для сохранения в кэш.");
        }
    }
    
    private void updateLastCacheTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_LAST_CACHE_TIME, System.currentTimeMillis());
        editor.apply();
    }


    public void loadData(Context context) {
        File cacheDir = new File(context.getFilesDir(), CACHE_DIR_NAME);
        File cacheFile = new File(cacheDir, CACHE_FILE_NAME);
        File cacheFilePage = new File(cacheDir, CACHE_FILE_NAME_PAGE);

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long lastCacheTime = prefs.getLong(KEY_LAST_CACHE_TIME, 0);
        long currentTime = System.currentTimeMillis();

        if (!cacheFile.exists() || !cacheFilePage.exists()) {
            if (isDebug) Log.d(TAG, "Файлы кэша не найдены.");
            return;
        }

        if (currentTime - lastCacheTime > TWENTY_FOUR_HOURS_IN_MS) {
            if (isDebug) Log.d(TAG, "Кэш устарел. Очистка.");
            cacheFile.delete();
            cacheFilePage.delete();
            prefs.edit().remove(KEY_LAST_CACHE_TIME).apply();
            return;
        }

        Map<Integer, Collection> loadedMap = loadMapFromFile(context);
        Map<Integer, Integer> loadedIndexLastPage = loadPageCollectionFromFile(context);

        if (loadedMap != null) {
            geminiMovies.setValue(loadedMap);
            if (isDebug) Log.d(TAG, "Данные из кэша коллекций успешно загружены в ViewModel.");
        } else {
            if (isDebug) Log.d(TAG, "Кэш коллекций пуст или не удалось его загрузить.");
        }

        if (loadedIndexLastPage != null) {
            page.setValue(loadedIndexLastPage);
            if (isDebug) Log.d(TAG, "Данные из кэша страниц успешно загружены в ViewModel.");
        } else {
            if (isDebug) Log.d(TAG, "Кэш страниц пуст или не удалось его загрузить.");
        }
    }

    private boolean saveCollectionToFile(Context context, Map<Integer, Collection> mapToSave) {
        File cacheDir = new File(context.getFilesDir(), CACHE_DIR_NAME);
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            if (isDebug) Log.e(TAG, "Не удалось создать директорию кэша: " + cacheDir.getAbsolutePath());
            return false;
        }

        File cacheFile = new File(cacheDir, CACHE_FILE_NAME);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cacheFile))) {
            oos.writeObject(mapToSave);
            if (isDebug) Log.d(TAG, "Кэш коллекции успешно сохранен в " + cacheFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            if (isDebug) Log.e(TAG, "Ошибка при сохранении кэша коллекций", e);
            return false;
        }
    }

    private boolean savePageCollectionToFile(Context context, Map<Integer, Integer> mapToSave) {
        File cacheDir = new File(context.getFilesDir(), CACHE_DIR_NAME);
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            if (isDebug) Log.e(TAG, "Не удалось создать директорию кэша: " + cacheDir.getAbsolutePath());
            return false;
        }

        File cacheFile = new File(cacheDir, CACHE_FILE_NAME_PAGE);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cacheFile))) {
            oos.writeObject(mapToSave);
            if (isDebug) Log.d(TAG, "Кэш страниц успешно сохранен в " + cacheFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            if (isDebug) Log.e(TAG, "Ошибка при сохранении кэша страниц", e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, Collection> loadMapFromFile(Context context) {
        File cacheFile = new File(new File(context.getFilesDir(), CACHE_DIR_NAME), CACHE_FILE_NAME);

        if (!cacheFile.exists()) {
            if (isDebug) Log.d(TAG, "Файл кэша коллекций не найден.");
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cacheFile))) {
            Object object = ois.readObject();
            if (isDebug) Log.d(TAG, "Кэш успешно коллекций загружен из " + cacheFile.getAbsolutePath());
            return (Map<Integer, Collection>) object;
        } catch (IOException | ClassNotFoundException e) {
            if (isDebug) Log.e(TAG, "Ошибка при загрузке кэша коллекций ", e);
            cacheFile.delete();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, Integer> loadPageCollectionFromFile(Context context) {
        File cacheFile = new File(new File(context.getFilesDir(), CACHE_DIR_NAME), CACHE_FILE_NAME_PAGE);

        if (!cacheFile.exists()) {
            if (isDebug) Log.d(TAG, "Файл кэша страниц не найден.");
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cacheFile))) {
            Object object = ois.readObject();
            if (isDebug) Log.d(TAG, "Кэш страниц успешно загружен из " + cacheFile.getAbsolutePath());
            return (Map<Integer, Integer>) object;
        } catch (IOException | ClassNotFoundException e) {
            if (isDebug) Log.e(TAG, "Ошибка при загрузке кэша страниц", e);
            cacheFile.delete();
            return null;
        }
    }
}
