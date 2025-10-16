package com.alaka_ala.florafilm.ui.util.coreTorrent.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.alaka_ala.florafilm.ui.util.coreTorrent.models.Torrent;

import java.util.List;

@Dao
public interface TorrentDao {

    /**
     * Вставляет или обновляет торрент. Если торрент с таким же hashBtih уже существует,
     * он будет заменен новыми данными.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(Torrent torrent);

    /**
     * Получает все торренты из базы данных.
     */
    @Query("SELECT * FROM torrents")
    List<Torrent> getAll();

    /**
     * Удаляет торрент из базы данных по его хешу.
     */
    @Query("DELETE FROM torrents WHERE hashBtih = :hash")
    void deleteByHash(String hash);
}