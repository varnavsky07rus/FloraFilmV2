package com.alaka_ala.florafilm.core.torrent;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestUtils {

    /**
     * Создает MD5 хеш из входной строки.
     *
     * @param input Строка для хеширования.
     * @return Шестнадцатеричное представление MD5 хеша или null в случае ошибки.
     */
    public static String createMd5Digest(String input) {
        if (input == null) {
            return null;
        }
        try {
            // 1. Получаем экземпляр алгоритма MD5
            MessageDigest digest = MessageDigest.getInstance("MD5");

            // 2. Вычисляем хеш
            byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // 3. Преобразуем в шестнадцатеричную строку
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            // Это исключение крайне маловероятно для стандартного алгоритма "MD5"
            e.printStackTrace();
            return null;
        }
    }

    /**Создает укороченный SHA-256 хеш из входной строки.
     * @param input - Входящая строка
     * @param length - Длина хэша в кол-ве символов.
     *               Т.е. если указать 32 то будет дайджест с 32 символами*/
    public static String createShortSha256Digest(String input, int length) {
        String fullHash = createSha256Digest(input);
        if (fullHash != null && fullHash.length() >= length) {
            return fullHash.substring(0, length);
        }
        return fullHash; // Возвращаем полный хеш, если он короче запрошенной длины (маловероятно)
    }


    /**Создает полный SHA-256 хеш из входной строки.*/
    public static String createSha256Digest(String input) {
        if (input == null) {
            return null;
        }
        try {
            // Получаем экземпляр алгоритма хеширования SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Получаем байты из строки в кодировке UTF-8 и вычисляем хеш
            byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Преобразуем массив байтов в шестнадцатеричную строку
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            // Это исключение маловероятно для стандартного алгоритма "SHA-256",
            // но его обработка является хорошей практикой.
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Вспомогательный метод для преобразования массива байтов в шестнадцатеричную строку.
     */
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
