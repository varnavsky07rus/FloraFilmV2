package com.alaka_ala.florafilm.ui.util.coreTorrent.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.alaka_ala.florafilm.ui.util.coreTorrent.models.Torrent;

@Database(entities = {Torrent.class}, version = 1, exportSchema = false)
public abstract class TorrentDatabase extends RoomDatabase {

    public abstract TorrentDao torrentDao();

    private static volatile TorrentDatabase INSTANCE;

    public static TorrentDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TorrentDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    TorrentDatabase.class, "torrent_database")
                            // ВНИМАНИЕ: allowMainThreadQueries() здесь только для простоты.
                            // В реальном приложении используйте фоновые потоки.
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}