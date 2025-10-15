package com.alaka_ala.florafilm.ui.util.coreTorrent.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5 {

    public static String getMd5Hash(String input) {
        try {
            // Получаем экземпляр MessageDigest для алгоритма Md5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Вычисляем хэш для массива байтов входной строки
            byte[] messageDigest = md.digest(input.getBytes());

            // Преобразуем массив байтов в BigInteger
            BigInteger no = new BigInteger(1, messageDigest);

            // Преобразуем BigInteger в шестнадцатеричную строку
            String hashtext = no.toString(16);

            // Дополняем строку нулями спереди, чтобы она всегда имела длину 32 символа
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            // Это исключение маловероятно, так как Md5 является стандартным алгоритмом.
            // Тем не менее, его нужно обработать.
            throw new RuntimeException(e);
        }
    }

}
