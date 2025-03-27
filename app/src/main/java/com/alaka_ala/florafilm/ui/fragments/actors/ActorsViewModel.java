package com.alaka_ala.florafilm.ui.fragments.actors;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.ItemFilmInfo;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.ListStaffItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



public class ActorsViewModel extends ViewModel {
    private static final String CACHE_FILE_NAME = "item_films_staff_cache.dat";
    private static final String TAG = "ActorsViewModel:CacheManager";
    private MutableLiveData<Map<Integer, ArrayList<ListStaffItem>>> filmsStaffMap = new MutableLiveData<>();



    public void addItemFilmInfoMap(Context context, int kinopoisk_id, ArrayList<ListStaffItem> listStaffItem) {
        if (this.filmsStaffMap.getValue() == null) {
            Map<Integer, ArrayList<ListStaffItem>> map = new HashMap<>();
            map.put(kinopoisk_id, listStaffItem);
            this.filmsStaffMap.setValue(map);
            saveItemFilmInfoMap(context, map);
        } else {
            this.filmsStaffMap.getValue().put(kinopoisk_id, listStaffItem);
            saveItemFilmInfoMap(context, this.filmsStaffMap.getValue());
        }
    }


    public ArrayList<ListStaffItem> getListStaffItem(Context context, int kinopoiskId) {
        // Сначала проверяем загруженный кэш. Не является ли он Null
        if (filmsStaffMap.getValue() != null) {
            // Далее провряем есть ли данный фильм в кэше и если есть то возвращаем его
            // если нет то загружаем новые данные
            if (filmsStaffMap.getValue().get(kinopoiskId) != null) {
                return filmsStaffMap.getValue().get(kinopoiskId);
            }
        }
        // Загружаем из кэша новые данные
        loadItemFilmInfoMap(context);
        // Проверяем что бы загруженные данные не являлись null в противном случае возвращаем null
        if (filmsStaffMap.getValue() == null) return null;
        // Если данные не нулевые то проверяем есть ли в них данный фильм, в противном слкчае вернет null
        return filmsStaffMap.getValue().get(kinopoiskId);
        // Если данные буду null,
        // то выполнится поиск данных в интернете и после чего данные добавятся в кэш и в массив,
        // тем самым исключая повторный запрос данных из КЭША (upd. 22.03.25 4:45 - Имеется ввиду чтение и запись из памяти)
    }


    private void saveItemFilmInfoMap(Context context, Map<Integer, ArrayList<ListStaffItem>> data) {
        File cacheDir = context.getCacheDir();
        File cacheFile = new File(cacheDir, CACHE_FILE_NAME);

        try (FileOutputStream fos = new FileOutputStream(cacheFile);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(data);
            Log.d(TAG, "ArrayList<ListStaffItem> saved to cache: " + cacheFile.getAbsolutePath());
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
            filmsStaffMap = new MutableLiveData<>();
        }

        try (FileInputStream fis = new FileInputStream(cacheFile);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                Map<Integer, ArrayList<ListStaffItem>> loadedData = (Map<Integer, ArrayList<ListStaffItem>>) obj;
                Log.d(TAG, "<ArrayList<ListStaffItem>> loaded from cache: " + cacheFile.getAbsolutePath());
                filmsStaffMap.setValue(loadedData);
            } else {
                Log.e(TAG, "Invalid data format in cache file");
                filmsStaffMap  = new MutableLiveData<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "Error loading <ArrayList<ListStaffItem>> from cache", e);
            filmsStaffMap  = new MutableLiveData<>();
        }
    }



}
