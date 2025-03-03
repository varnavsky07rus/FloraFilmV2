package com.alaka_ala.florafilm.ui.util.local;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

// Используется на странице торрентов, по умолчанию открывает поддерживаемое приложение либо предлагает на выбор
public class TorrentHelper {
    public static void openMagnetLink(Context context, String magnetLink) {
        // Создаем URI из magnet-ссылки
        Uri magnetUri = Uri.parse(magnetLink);
        // Создаем Intent
        Intent intent = new Intent(Intent.ACTION_VIEW, magnetUri);
        // Проверяем, есть ли приложение, которое может обработать Intent
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            try {
                // Запускаем Intent
                context.startActivity(intent);
            } catch (
                    ActivityNotFoundException e) {// Обрабатываем исключение, если нет приложения для обработки Intent
                Toast.makeText(context, "No application found to open magnet links", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Если нет приложения по умолчанию, показываем средство выбора приложений
            Intent chooser = Intent.createChooser(intent, "Open magnet link with");
            try {
                context.startActivity(chooser);
            } catch (ActivityNotFoundException e) {
                // Обрабатываем исключение, если нет приложения для обработки Intent
                Toast.makeText(context, "No application found to open magnet links", Toast.LENGTH_SHORT).show();
            }
        }
    }
}