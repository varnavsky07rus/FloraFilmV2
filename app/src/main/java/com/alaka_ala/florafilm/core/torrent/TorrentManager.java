package com.alaka_ala.florafilm.core.torrent;

import android.content.Context;
import android.content.Intent;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.libtorrent4j.AddTorrentParams;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TorrentManager {
    private static TorrentManager instance;
    private final Map<String, TorrentInfo> torrents = new ConcurrentHashMap<>();
    private final MutableLiveData<Map<String, TorrentInfo>> torrentsLiveData = new MutableLiveData<>();

    private TorrentManager() {}

    public static synchronized TorrentManager getInstance() {
        if (instance == null) {
            instance = new TorrentManager();
        }
        return instance;
    }

    public LiveData<Map<String, TorrentInfo>> getTorrentsLiveData() {
        return torrentsLiveData;
    }

    public String addTorrent(Context context, String magnetLink, int kinopoisk_id) {
        String hash = DigestUtils.createMd5Digest(magnetLink), name;
        try {
            AddTorrentParams params = AddTorrentParams.parseMagnetUri(magnetLink);
            name = params.getName();
            name = getName(magnetLink, name, hash);

            if (torrents.containsKey(hash)) {
                return "Already added"; // Already added
            }

            TorrentInfo info = new TorrentInfo(magnetLink, hash, name, 0); // Size is unknown initially
            info.setStatus(TorrentInfo.DownloadStatus.DOWNLOADING);
            torrents.put(hash, info);
            updateLiveData();

            Intent intent = new Intent(context, TorrentService.class);
            intent.setAction(TorrentService.ACTION_ADD_TORRENT);
            intent.putExtra(TorrentService.EXTRA_MAGNET_LINK, magnetLink);
            intent.putExtra(TorrentService.EXTRA_KINOPOISK_ID, kinopoisk_id);
            context.startService(intent);

        } catch (IllegalArgumentException e) {
            // This can be thrown by parseMagnetUri if the link is invalid
            e.printStackTrace();
        }
        return hash;
    }

    private String getName(String magnetLink, String name, String hash) {
        // Если название не получилось взять из параметров то получает из Magnet ссылки
        if (name == null || name.isEmpty()) {
            try {
                // Fallback to parsing 'dn=' parameter manually
                String[] parts = magnetLink.split("&");
                for (String part : parts) {
                    if (part.startsWith("dn=")) {
                        name = URLDecoder.decode(part.substring(3), "UTF-8");
                        break;
                    }
                }
            } catch (UnsupportedEncodingException e) {
                // ignore
            }
        }
        // Если название не получилось взять из параметров и магнет ссылки
        // то преобразовываем магнет ссылку в хэш и присваиваем имени
        if (name == null || name.isEmpty()) {
            name = hash;
        }
        return name;
    }

    public void updateTorrentInfo(TorrentInfo info) {
        torrents.put(DigestUtils.createMd5Digest(info.getMagnetLink()), info);
        updateLiveData();
    }

    private void updateLiveData() {
        torrentsLiveData.postValue(new ConcurrentHashMap<>(torrents));
    }

    /**Возвращает true если файл для фильма существует в кеше*/
    public boolean existFileFromCache(Context context, int kinopoisk_id, String magnetLink){
        File baseDir = context.getCacheDir();
        if (baseDir == null) {
            return false;
        }
        File saveDir = new File(new File(baseDir, "/" + kinopoisk_id), DigestUtils.createMd5Digest(magnetLink));
        return saveDir.exists();
    }

    /**Получает файл фильма из кеша.*/
    public File getFileFromCache(Context context, int kinopoisk_id, String magnetLink) {
        File baseDir = context.getCacheDir();
        if (baseDir == null) {
            return null;
        }
        return new File(new File(baseDir, "/" + kinopoisk_id + "/"), DigestUtils.createMd5Digest(magnetLink));
    }

    /**Удаляет файл фильма вместе с папкой из кеша.*/
    public boolean removeFileFromCache(Context context, int kinopoisk_id) {
        File baseDir = context.getCacheDir();
        if (baseDir == null) {
            return false;
        }
        File saveDir = new File(baseDir, "/" + kinopoisk_id);
        if (!saveDir.exists()) {
            return false;
        }
        return saveDir.delete();
    }
    


}
