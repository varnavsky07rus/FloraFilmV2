// C:/Users/NATO/StudioProjects/FloraFilmV2/app/src/main/java/com/alaka_ala/florafilm/ui/util/coreTorrent/models/Torrent.java

package com.alaka_ala.florafilm.ui.util.coreTorrent.models;

import java.io.Serializable;
import java.util.Objects;

public class Torrent implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final long size;
    private final String magnet;
    private final String hashBtih;
    private final String pathFile;
    private final int progress;
    private final String state;
    private final byte[] benCode;
    // Новые поля для скорости
    private final int downloadRate;
    private final int uploadRate;

    public Torrent(String name, long size, String magnet, String hashBtih, String pathFile, int progress, String state, int downloadRate, int uploadRate, byte[] benCode) {
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

    // Геттеры для всех полей...
    public String getName() { return name; }
    public long getSize() { return size; }
    public String getMagnet() { return magnet; }
    public String getHashBtih() { return hashBtih; }
    public String getPathFile() { return pathFile; }
    public int getProgress() { return progress; }
    public String getState() { return state; }
    public int getDownloadRate() { return downloadRate; }
    public int getUploadRate() { return uploadRate; }
    public byte[] getBenCode() { return benCode; }

    // Переопределяем equals для удобного сравнения объектов
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Torrent torrent = (Torrent) o;
        return progress == torrent.progress &&
                downloadRate == torrent.downloadRate &&
                uploadRate == torrent.uploadRate &&
                Objects.equals(state, torrent.state) &&
                Objects.equals(hashBtih, torrent.hashBtih);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashBtih, progress, state, downloadRate, uploadRate);
    }
}