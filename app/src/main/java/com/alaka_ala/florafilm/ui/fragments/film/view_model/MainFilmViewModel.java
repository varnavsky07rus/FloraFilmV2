package com.alaka_ala.florafilm.ui.fragments.film.view_model;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alaka_ala.florafilm.ui.util.api.EPData;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.ItemFilmInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MainFilmViewModel extends ViewModel {
    public MainFilmViewModel() {
    }

    private MutableLiveData<Map<Integer, ItemFilmInfo>> itemFilmInfoMap = new MutableLiveData<>();

    public void addItemFilmInfoMap(Context context, ItemFilmInfo itemFilmInfoMap) {
        if (this.itemFilmInfoMap.getValue() == null) {
            Map<Integer, ItemFilmInfo> map = new HashMap<>();
            map.put(itemFilmInfoMap.getKinopoiskId(), itemFilmInfoMap);
            this.itemFilmInfoMap.setValue(map);
            saveItemFilmInfoMap(context, map);
        } else {
            this.itemFilmInfoMap.getValue().put(itemFilmInfoMap.getKinopoiskId(), itemFilmInfoMap);
            saveItemFilmInfoMap(context, this.itemFilmInfoMap.getValue());
        }
    }

    public ItemFilmInfo getItemFilmInfoMap(Context context, int kinopoiskId) {
        // Сначала проверяем загруженный кэш. Не является ли он Null
        if (itemFilmInfoMap.getValue() != null) {
            // Далее провряем есть ли данный фильм в кэше и если есть то возвращаем его
            // если нет то загружаем новые данные
            if (itemFilmInfoMap.getValue().get(kinopoiskId) != null) {
                return itemFilmInfoMap.getValue().get(kinopoiskId);
            }
        }
        // Загружаем из кэша новые данные
        loadItemFilmInfoMap(context);
        // Проверяем что бы загруженные данные не являлись null в противном случае возвращаем null
        if (itemFilmInfoMap.getValue() == null) return null;
        // Если данные не нулевые то проверяем есть ли в них данный фильм, в противном слкчае вернет null
        return itemFilmInfoMap.getValue().get(kinopoiskId);
        // Если данные буду null,
        // то выполнится поиск данных в интернете и после чего данные добавятся в кэш и в массив,
        // тем самым исключая повторный запрос данных из КЭША
    }

    public ItemFilmInfo getCurrentFilmInfo() {
        if (itemFilmInfoMap.getValue() == null) return null;
        return itemFilmInfoMap.getValue().get(getKinopoiskId());
    }


    // Сохраняем текущий ID филмьа
    private final MutableLiveData<Integer> kinopoiskId = new MutableLiveData<>();

    // Устанавилвает текущий ID фильма для получения ID филмьа в другом фрагменте
    public void setKinopoiskId(int kinopoiskId) {
        this.kinopoiskId.setValue(kinopoiskId);
    }

    // Получение текущего ID фильма
    public int getKinopoiskId() {
        if (kinopoiskId.getValue() == null) return 0;
        return kinopoiskId.getValue();
    }


    private static final String CACHE_FILE_NAME = "item_film_info_cache.dat";
    private static final String TAG = "MainFilmViewModel:CacheManager";

    /**
     * После загрузки и добавления {@link ItemFilmInfo} в {@link #itemFilmInfoMap}
     * данный метод вызывается автоматически. Он сохраняет данные в кэш.
     * Но можно принудительно изменить данные вызвав данный метод}
     */
    private void saveItemFilmInfoMap(Context context, Map<Integer, ItemFilmInfo> data) {
        File cacheDir = context.getCacheDir();
        File cacheFile = new File(cacheDir, CACHE_FILE_NAME);

        try (FileOutputStream fos = new FileOutputStream(cacheFile);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(data);
            Log.d(TAG, "ItemFilmInfoMap saved to cache: " + cacheFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Error saving ItemFilmInfoMap to cache", e);
        }
    }

    /**
     * @-WARN: Данный метод необходимо вызывать перед первым созданием ViewModel
     * @- Данный метод загружает данные из кэша.
     * После создания ViewModel необходимо сразу вызвать данный метод
     */
    @SuppressWarnings("unchecked")
    private void loadItemFilmInfoMap(Context context) {
        File cacheDir = context.getCacheDir();
        File cacheFile = new File(cacheDir, CACHE_FILE_NAME);

        if (!cacheFile.exists()) {
            itemFilmInfoMap  = new MutableLiveData<>();
        }

        try (FileInputStream fis = new FileInputStream(cacheFile);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                Map<Integer, ItemFilmInfo> loadedData = (Map<Integer, ItemFilmInfo>) obj;
                Log.d(TAG, "ItemFilmInfoMap loaded fromcache: " + cacheFile.getAbsolutePath());
                itemFilmInfoMap.setValue(loadedData);
            } else {
                Log.e(TAG, "Invalid data format in cache file");
                itemFilmInfoMap  = new MutableLiveData<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "Error loading ItemFilmInfoMap from cache", e);
            itemFilmInfoMap  = new MutableLiveData<>();
        }
    }


    // HDVB
    private final MutableLiveData<EPData.Film> filmMutableLiveDataHDVB = new MutableLiveData<>();

    private final MutableLiveData<EPData.Serial> serialMutableLiveDataHDVB = new MutableLiveData<>();

    public MutableLiveData<EPData.Serial> getSerialMutableLiveDataHDVB() {
        return serialMutableLiveDataHDVB;
    }

    public MutableLiveData<EPData.Film> getFilmMutableLiveDataHDVB() {
        return filmMutableLiveDataHDVB;
    }

    // VIBIX
    private final MutableLiveData<EPData.Film> filmMutableLiveDataVibix = new MutableLiveData<>();
    private final MutableLiveData<EPData.Serial> serialMutableLiveDataVibix = new MutableLiveData<>();
    public MutableLiveData<EPData.Serial> getSerialMutableLiveDataVibix() {
        return serialMutableLiveDataVibix;
    }
    public MutableLiveData<EPData.Film> getFilmMutableLiveDataVibix() {
        return filmMutableLiveDataVibix;
    }

}
