package com.alaka_ala.florafilm.ui.fragments.actors.films;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.StaffFilmsItem;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.StaffInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActorFilmsViewModel extends ViewModel {

    private static final String CACHE_FILE_NAME = "films_staff_cache_v2.dat";
    private static final String TAG = "ActorFilmsViewModel:CacheManager";
    private MutableLiveData<Map<Integer, StaffInfo>> staffInfoCache = new MutableLiveData<>();



    public void saveActorInfo(Context context, int staffId, StaffInfo listStaffItem) {
        if (this.staffInfoCache.getValue() == null) {
            Map<Integer, StaffInfo> map = new HashMap<>();
            map.put(staffId, listStaffItem);
            this.staffInfoCache.setValue(map);
            saveItemFilmInfoMap(context, map);
        } else {
            this.staffInfoCache.getValue().put(staffId, listStaffItem);
            saveItemFilmInfoMap(context, this.staffInfoCache.getValue());
        }
    }


    public StaffInfo getListStaffItem(Context context, int staffId) {
        // Сначала проверяем загруженный кэш. Не является ли он Null
        if (staffInfoCache.getValue() != null) {
            // Далее провряем есть ли данный фильм в кэше и если есть то возвращаем его
            // если нет то загружаем новые данные
            if (staffInfoCache.getValue().get(staffId) != null) {
                return staffInfoCache.getValue().get(staffId);
            }
        }
        // Загружаем из кэша новые данные
        loadItemFilmInfoMap(context);
        // Проверяем что бы загруженные данные не являлись null в противном случае возвращаем null
        if (staffInfoCache.getValue() == null) return null;
        // Если данные не нулевые то проверяем есть ли в них данный фильм, в противном слкчае вернет null
        return staffInfoCache.getValue().get(staffId);
        // Если данные буду null,
        // то выполнится поиск данных в интернете и после чего данные добавятся в кэш и в массив,
        // тем самым исключая повторный запрос данных из КЭША (upd. 22.03.25 4:45 - Имеется ввиду чтение и запись из памяти)
    }


    private void saveItemFilmInfoMap(Context context, Map<Integer, StaffInfo> data) {
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
            staffInfoCache = new MutableLiveData<>();
        }

        try (FileInputStream fis = new FileInputStream(cacheFile);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                Map<Integer, StaffInfo> loadedData = (Map<Integer, StaffInfo>) obj;
                Log.d(TAG, "ArrayList<StaffFilmsItem> loaded from cache: " + cacheFile.getAbsolutePath());
                staffInfoCache.setValue(loadedData);
            } else {
                Log.e(TAG, "Invalid data format in cache file");
                staffInfoCache = new MutableLiveData<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "Error loading ArrayList<StaffFilmsItem> from cache", e);
            staffInfoCache = new MutableLiveData<>();
        }
    }
}
