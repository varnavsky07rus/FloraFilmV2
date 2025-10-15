package com.alaka_ala.florafilm.ui.util.coreTorrent.utils;

import com.alaka_ala.florafilm.ui.util.coreTorrent.models.Torrent;
import java.io.*;
import java.util.Map;

public class TorrentSerializer {

    /**
     * Сериализует объект Torrent в byte[]
     */
    public static byte[] serializeTorrent(Map<String, Torrent> torrentsMap) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(torrentsMap);
            return baos.toByteArray();
        }
    }

    /**
     * Десериализует объект Torrent из byte[]
     */
    public static Torrent deserializeTorrent(byte[] serializedData) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (Torrent) ois.readObject();
        }
    }

    /**
     * Сохраняет Torrent в файл
     */
    public static void saveToFile(Map<String, Torrent> torrentsMap, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(torrentsMap);
        }
    }

    /**
     * Загружает Torrent из файла
     */
    public static Torrent loadFromFile(String filePath)
            throws IOException, ClassNotFoundException {
        try (FileInputStream fis = new FileInputStream(filePath);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (Torrent) ois.readObject();
        }
    }
}