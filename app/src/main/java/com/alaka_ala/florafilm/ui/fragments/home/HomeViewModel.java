package com.alaka_ala.florafilm.ui.fragments.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.Collection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.appmetrica.analytics.impl.S;

public class HomeViewModel extends ViewModel {


    // Фильмы-сериалы
    private final MutableLiveData<Collection> collectionMutableLiveDataPopularAll;
    private final MutableLiveData<Integer> pagePopularAllMutableLiveData;

    // Фильмы
    private final MutableLiveData<Collection> collectionMutableLiveDataMovie;
    private final MutableLiveData<Integer> pageMovieMutableLiveData;

    // Сериалы
    private final MutableLiveData<Collection> collectionMutableLiveDataSerial;
    private final MutableLiveData<Integer> pageSerialMutableLiveData;

    // Мультфильмы
    private final MutableLiveData<Collection> collectionMutableLiveDataAnimations;
    private final MutableLiveData<Integer> pageAnimationsMutableLiveData;

    // Драмы
    private final MutableLiveData<Collection> collectionMutableLiveDataDrama;
    private final MutableLiveData<Integer> pageMutableLiveDataDrama;

    // Детям
    private final MutableLiveData<Collection> collectionMutableLiveDataKids;
    private final MutableLiveData<Integer> pageKidsMutableLiveData;

    private boolean isDebug = true;
    private final String TAG = "HomeViewModel";
    private static final String CACHE_DIR_NAME = "cache";
    private static final String CACHE_FILE_NAME = "homeViewModelCache";
    private static final String CACHE_FILE_NAME_PAGE = "homeViewModelCachePage";
    private static final String PREFS_NAME = "HomeViewModelPrefs";
    private static final String KEY_LAST_CACHE_TIME = "last_cache_time";
    private static final long TWENTY_FOUR_HOURS_IN_MS = 24 * 60 * 60 * 1000L;


    public HomeViewModel() {
        // Фильмы-сериалы
        this.collectionMutableLiveDataPopularAll = new MutableLiveData<>();
        this.pagePopularAllMutableLiveData = new MutableLiveData<>();
        // Фильмы
        this.collectionMutableLiveDataMovie = new MutableLiveData<>();
        this.pageMovieMutableLiveData = new MutableLiveData<>();
        // Сериалы
        this.pageSerialMutableLiveData = new MutableLiveData<>();
        this.collectionMutableLiveDataSerial = new MutableLiveData<>();
        // Мультфильмы
        this.pageAnimationsMutableLiveData = new MutableLiveData<>();
        this.collectionMutableLiveDataAnimations = new MutableLiveData<>();
        // Драмы
        this.pageMutableLiveDataDrama = new MutableLiveData<>();
        this.collectionMutableLiveDataDrama = new MutableLiveData<>();
        // Детям
        this.pageKidsMutableLiveData = new MutableLiveData<>();
        this.collectionMutableLiveDataKids = new MutableLiveData<>();
    }

    public void initData(Context context){
        loadData(context);
    }



    // Фильмы - Movie
    public void addDataCollectionMovie(Collection collection) {
        if (collectionMutableLiveDataMovie.getValue() == null) {
            collectionMutableLiveDataMovie.setValue(collection);
        } else {
            Objects.requireNonNull(collectionMutableLiveDataMovie.getValue()).getItems().addAll(collection.getItems());
        }
    }

    public MutableLiveData<Collection> getCollectionMutableLiveDataMovie() {
        return collectionMutableLiveDataMovie;
    }

    public MutableLiveData<Integer> getPageMovieMutableLiveData() {
        return pageMovieMutableLiveData;
    }


    // PopularAll (Фильмы/Сериалы)
    public void addDataCollectionPopularAll(Collection collection) {
        if (collectionMutableLiveDataPopularAll.getValue() == null) {
            collectionMutableLiveDataPopularAll.setValue(collection);
        } else {
            Objects.requireNonNull(collectionMutableLiveDataPopularAll.getValue()).getItems().addAll(collection.getItems());
        }
    }

    public MutableLiveData<Collection> getCollectionMutableLiveDataPopularAll() {
        return collectionMutableLiveDataPopularAll;
    }

    public MutableLiveData<Integer> getPagePopularAllMutableLiveData() {
        return pagePopularAllMutableLiveData;
    }


    // Сериалы (Сериалы)
    public void addDataCollectionSerial(Collection collection) {
        if (collectionMutableLiveDataSerial.getValue() == null) {
            collectionMutableLiveDataSerial.setValue(collection);
        } else {
            Objects.requireNonNull(collectionMutableLiveDataSerial.getValue()).getItems().addAll(collection.getItems());
        }
    }

    public MutableLiveData<Collection> getCollectionMutableLiveDataSerial() {
        return collectionMutableLiveDataSerial;
    }

    public MutableLiveData<Integer> getPageSerialMutableLiveData() {
        return pageSerialMutableLiveData;
    }


    // Мультфильмы (Мультфильмы)
    public void addDataCollectionAnimations(Collection collection) {
        if (collectionMutableLiveDataAnimations.getValue() == null) {
            collectionMutableLiveDataAnimations.setValue(collection);
        } else {
            Objects.requireNonNull(collectionMutableLiveDataAnimations.getValue()).getItems().addAll(collection.getItems());
        }
    }

    public MutableLiveData<Collection> getCollectionMutableLiveDataAnimations() {
        return collectionMutableLiveDataAnimations;
    }

    public MutableLiveData<Integer> getPageAnimationsMutableLiveData() {
        return pageAnimationsMutableLiveData;
    }


    // Драмы
    public void addDataCollectionDrama(Collection collection) {
        if (collectionMutableLiveDataDrama.getValue() == null) {
            collectionMutableLiveDataDrama.setValue(collection);
        } else {
            Objects.requireNonNull(collectionMutableLiveDataDrama.getValue()).getItems().addAll(collection.getItems());
        }
    }

    public MutableLiveData<Collection> getCollectionMutableLiveDataDrama() {
        return collectionMutableLiveDataDrama;
    }

    public MutableLiveData<Integer> getPageDramaMutableLiveData() {
        return pageMutableLiveDataDrama;
    }

    // Детям
    public void addDataCollectionKids(Collection collection) {
        if (collectionMutableLiveDataKids.getValue() == null) {
            collectionMutableLiveDataKids.setValue(collection);
        } else {
            Objects.requireNonNull(collectionMutableLiveDataKids.getValue()).getItems().addAll(collection.getItems());
        }
    }

    public MutableLiveData<Collection> getCollectionMutableLiveDataKids() {
        return collectionMutableLiveDataKids;
    }

    public MutableLiveData<Integer> getPageKidsMutableLiveData() {
        return pageKidsMutableLiveData;
    }

    // Сохранение данных в кэш приложения
    /**
     * Собирает данные из LiveData и сохраняет их в файл кэша.
     * Файл полностью перезаписывается при каждом вызове.
     *
     * @param context Контекст приложения.
     */
    public void saveData(Context context) {
        Map<String, Collection> collectionMap = new HashMap<>();
        Map<String, Integer> indexLastPage = new HashMap<>();
        // Безопасно собираем данные из LiveData в карту
        if (pagePopularAllMutableLiveData.getValue() != null) {
            indexLastPage.put("pagePopularAll", pagePopularAllMutableLiveData.getValue());
        }
        if (pageMovieMutableLiveData.getValue() != null) {
            indexLastPage.put("pageMovie", pageMovieMutableLiveData.getValue());
        }
        if (pageSerialMutableLiveData.getValue() != null) {
            indexLastPage.put("pageSerial", pageSerialMutableLiveData.getValue());
        }
        if (pageAnimationsMutableLiveData.getValue() != null) {
            indexLastPage.put("pageAnimations", pageAnimationsMutableLiveData.getValue());
        }
        if (pageMutableLiveDataDrama.getValue() != null) {
            indexLastPage.put("pageDrama", pageMutableLiveDataDrama.getValue());
        }
        if (pageKidsMutableLiveData.getValue() != null) {
            indexLastPage.put("pageKids", pageKidsMutableLiveData.getValue());
        }


        // Безопасно собираем данные из LiveData в карту
        if (collectionMutableLiveDataPopularAll.getValue() != null) {
            collectionMap.put("PopularAll", collectionMutableLiveDataPopularAll.getValue());
        }
        if (collectionMutableLiveDataMovie.getValue() != null) {
            collectionMap.put("Movie", collectionMutableLiveDataMovie.getValue());
        }
        if (collectionMutableLiveDataSerial.getValue() != null) {
            collectionMap.put("Serial", collectionMutableLiveDataSerial.getValue());
        }
        if (collectionMutableLiveDataAnimations.getValue() != null) {
            collectionMap.put("Animations", collectionMutableLiveDataAnimations.getValue());
        }
        if (collectionMutableLiveDataDrama.getValue() != null) {
            collectionMap.put("Drama", collectionMutableLiveDataDrama.getValue());
        }
        if (collectionMutableLiveDataKids.getValue() != null) {
            collectionMap.put("Kids", collectionMutableLiveDataKids.getValue());
        }

        if (!collectionMap.isEmpty() && !indexLastPage.isEmpty()) {
            boolean isCollectionSaved = saveCollectionToFile(context, collectionMap);
            boolean isPageSaved = savePageCollectionToFile(context, indexLastPage);

            if(isCollectionSaved && isPageSaved){
                SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(KEY_LAST_CACHE_TIME, System.currentTimeMillis());
                editor.apply();
            }

        } else {
            if (isDebug) Log.d(TAG, "Нет данных для сохранения в кэш.");
        }

    }

    /**
     * Загружает данные из файла кэша и обновляет LiveData.
     *
     * @param context Контекст приложения.
     */
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


        Map<String, Collection> loadedMap = loadMapFromFile(context);
        Map<String, Integer> loadedIndexLastPage = loadPageCollectionFromFile(context);

        if (loadedMap != null && loadedIndexLastPage != null) {
            // Обновляем LiveData загруженными данными
            collectionMutableLiveDataPopularAll.setValue(loadedMap.get("PopularAll"));
            collectionMutableLiveDataMovie.setValue(loadedMap.get("Movie"));
            collectionMutableLiveDataSerial.setValue(loadedMap.get("Serial"));
            collectionMutableLiveDataAnimations.setValue(loadedMap.get("Animations"));
            collectionMutableLiveDataDrama.setValue(loadedMap.get("Drama"));
            collectionMutableLiveDataKids.setValue(loadedMap.get("Kids"));

            pagePopularAllMutableLiveData.setValue(loadedIndexLastPage.get("pagePopularAll"));
            pageMovieMutableLiveData.setValue(loadedIndexLastPage.get("pageMovie"));
            pageSerialMutableLiveData.setValue(loadedIndexLastPage.get("pageSerial"));
            pageAnimationsMutableLiveData.setValue(loadedIndexLastPage.get("pageAnimations"));
            pageMutableLiveDataDrama.setValue(loadedIndexLastPage.get("pageDrama"));
            pageKidsMutableLiveData.setValue(loadedIndexLastPage.get("pageKids"));

            if (isDebug) Log.d(TAG, "Данные из кэша успешно загружены в ViewModel.");
        } else {
            if (isDebug) Log.d(TAG, "Кэш пуст или не удалось его загрузить.");
        }
    }


    /**
     * Вспомогательный метод для сериализации и сохранения карты в файл.
     *
     * @param context   Контекст приложения.
     * @param mapToSave Карта для сохранения.
     * @return true в случае успеха, иначе false.
     */
    private boolean saveCollectionToFile(Context context, Map<String, Collection> mapToSave) {
        File cacheDir = new File(context.getFilesDir(), CACHE_DIR_NAME);
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            if (isDebug) Log.e(TAG, "Не удалось создать директорию кэша: " + cacheDir.getAbsolutePath());
            return false;
        }

        File cacheFile = new File(cacheDir, CACHE_FILE_NAME);

        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(cacheFile.toPath()))) {
            oos.writeObject(mapToSave);
            if (isDebug) Log.d(TAG, "Кэш коллекции успешно сохранен в " + cacheFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            if (isDebug) Log.e(TAG, "Ошибка при сохранении кэша коллекций", e);
            return false;
        }
    }

    private boolean savePageCollectionToFile(Context context, Map<String, Integer> mapToSave) {
        File cacheDir = new File(context.getFilesDir(), CACHE_DIR_NAME);
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            if (isDebug) Log.e(TAG, "Не удалось создать директорию кэша: " + cacheDir.getAbsolutePath());
            return false;
        }

        File cacheFile = new File(cacheDir, CACHE_FILE_NAME_PAGE);

        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(cacheFile.toPath()))) {
            oos.writeObject(mapToSave);
            if (isDebug) Log.d(TAG, "Кэш страниц успешно сохранен в " + cacheFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            if (isDebug) Log.e(TAG, "Ошибка при сохранении кэша страниц", e);
            return false;
        }
    }



    /**
     * Вспомогательный метод для загрузки и десериализации карты из файла.
     *
     * @param context Контекст приложения.
     * @return Загруженная карта или null, если файл не найден или произошла ошибка.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Collection> loadMapFromFile(Context context) {
        File cacheFile = new File(new File(context.getFilesDir(), CACHE_DIR_NAME), CACHE_FILE_NAME);

        if (!cacheFile.exists()) {
            if (isDebug) Log.d(TAG, "Файл кэша коллекций не найден.");
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cacheFile))) {
            Object object = ois.readObject();
            if (isDebug) Log.d(TAG, "Кэш успешно коллекций загружен из " + cacheFile.getAbsolutePath());
            return (Map<String, Collection>) object;
        } catch (IOException | ClassNotFoundException e) {
            if (isDebug) Log.e(TAG, "Ошибка при загрузке кэша коллекций ", e);
            // В случае ошибки поврежденный файл кэша лучше удалить
            cacheFile.delete();
            return null;
        }
    }

    private Map<String, Integer> loadPageCollectionFromFile(Context context) {
        File cacheFile = new File(new File(context.getFilesDir(), CACHE_DIR_NAME), CACHE_FILE_NAME_PAGE);

        if (!cacheFile.exists()) {
            if (isDebug) Log.d(TAG, "Файл кэша страниц не найден.");
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cacheFile))) {
            Object object = ois.readObject();
            if (isDebug) Log.d(TAG, "Кэш страниц успешно загружен из " + cacheFile.getAbsolutePath());
            return (Map<String, Integer>) object;
        } catch (IOException | ClassNotFoundException e) {
            if (isDebug) Log.e(TAG, "Ошибка при загрузке кэша страниц", e);
            // В случае ошибки поврежденный файл кэша лучше удалить
            cacheFile.delete();
            return null;
        }
    }



}
