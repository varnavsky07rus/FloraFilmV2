package com.alaka_ala.florafilm.ui.util.coreTorrent.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Objects;

// 1. Добавляем аннотацию @Entity, указывая имя таблицы
@Entity(tableName = "torrents")
public class Torrent implements Serializable {
    private static final long serialVersionUID = 1L;

    // 2. Указываем, что hashBtih - это уникальный первичный ключ.
    // @NonNull обязателен для первичного ключа.
    @PrimaryKey
    @NonNull
    private final String hashBtih;

    private final String name;
    private final long size;
    private final String magnet;
    private final String pathFile;
    private final int progress;
    private final String state;
    private final byte[] benCode;
    private final int downloadRate;
    private final int uploadRate;

    // Конструктор остается без изменений
    public Torrent(String name, long size, String magnet, @NonNull String hashBtih, String pathFile, int progress, String state, int downloadRate, int uploadRate, byte[] benCode) {
        this.name = name;
        this.size = size;
        this.magnet = magnet;
        this.hashBtih = hashBtih;
        this.pathFile = pathFile;
        this.progress = progress;
        this.state = state;
        this.downloadRate = downloadRate;
        this.uploadRate = uploadRate;
        this.benCode = benCode;
    }

    // Геттеры остаются без изменений
    @NonNull
    public String getHashBtih() { return hashBtih; }
    public String getName() { return name; }
    public long getSize() { return size; }
    public String getMagnet() { return magnet; }
    public String getPathFile() { return pathFile; }
    public int getProgress() { return progress; }
    public String getState() { return state; }
    public int getDownloadRate() { return downloadRate; }
    public int getUploadRate() { return uploadRate; }
    public byte[] getBenCode() { return benCode; }

    // equals и hashCode остаются без изменений
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Torrent torrent = (Torrent) o;
        return progress == torrent.progress &&
                downloadRate == torrent.downloadRate &&
                uploadRate == torrent.uploadRate &&
                Objects.equals(state, torrent.state) &&
                hashBtih.equals(torrent.hashBtih);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashBtih, progress, state, downloadRate, uploadRate);
    }
}