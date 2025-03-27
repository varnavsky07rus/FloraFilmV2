package com.alaka_ala.florafilm.ui.util.api.collapse;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class HlsProcessor {

    private static final String MAPPING = "DlChEXitLONYRkFjAsnBbymWzSHMqKPgQZpvwerofJTVdIuUcxaG";

    public static void getHls() {
        // Исходный URL
        String iframeUrl = "https://api.embess.ws/embed/movie/255";
        iframeUrl += "?showPreNext=false&sharing=false&noPreview=true&showMenu=false";

        // Получаем HLS-ссылку
        JSONObject jsonObject = null;
        try {
            jsonObject = getJson(iframeUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("JSON: " + jsonObject);


    }

    // Получаем HLS-ссылку из iframe URL
    private static JSONObject getJson(String iframeUrl) throws IOException {
        String content = fetchUrlContent(iframeUrl);
        String jsonString = extractJson(content);
        return extractJSON(jsonString);
    }

    // Обрабатываем HLS-плейлист
    private static List<String> processHlsPlaylist(String hlsUrl) throws IOException {
        String content = fetchUrlContent(hlsUrl);
        String path = hlsUrl.replace("/master.m3u8", "/");
        List<String> playlist = new ArrayList<>();

        for (String line : content.split("\n")) {
            if (!line.startsWith("#") && !line.isEmpty()) {
                playlist.add(transformUrl(path + line));
            } else {
                playlist.add(line);
            }
        }
        return playlist;
    }

    // Преобразуем URL
    private static String transformUrl(String url) {
        try {
            long hours = System.currentTimeMillis() / 1000 / 60 / 60;
            URL parsedUrl = new URL(url);
            String path = parsedUrl.getPath();
            String encoded = Base64.getEncoder().encodeToString((hours + "/" + path).getBytes(StandardCharsets.UTF_8));
            String transformed = applyMapping(encoded);
            return parsedUrl.getProtocol() + "://" + parsedUrl.getHost() + "/x-en-x/" + transformed;
        } catch (Exception e) {
            throw new RuntimeException("Failed to transform URL", e);
        }
    }

    // Применяем кастомное шифрование
    private static String applyMapping(String input) {
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            int index = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".indexOf(c);
            if (index != -1) {
                result.append(MAPPING.charAt(index));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    // Извлекаем JSON из содержимого страницы
    private static String extractJson(String content) {
        String startMarker = "makePlayer({";
        String endMarker = "});";
        int startIndex = content.indexOf(startMarker) + startMarker.length();
        int endIndex = content.indexOf(endMarker, startIndex);
        return "{" + content.substring(startIndex, endIndex) + "}";
    }

    // Извлекаем HLS-ссылку из JSON
    private static JSONObject extractJSON(String jsonString) {
        // Используем Gson для парсинга JSON
        if (jsonString == null) return null;
        String replLongDownload2 = jsonString.replace("30 * 1000", "\"30 * 1000\"");
        JsonObject json = JsonParser.parseString(replLongDownload2).getAsJsonObject();
        JSONObject j = new JSONObject();
        try {
            j = new JSONObject(json.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return j;
    }

    // Загружаем содержимое по URL
    private static String fetchUrlContent(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
}
