package com.alaka_ala.florafilm.ui.util.api.vibix;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.alaka_ala.florafilm.ui.util.api.EPData;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class Vibix {
    public static final String TYPE_CONTENT_FILM = "movie";
    public static final String TYPE_CONTENT_SERIAL = "serial";
    private final String vibix_api_key;
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36";
    public static final Map<String, String> headers = new HashMap<>();
    private int kinopoiskID;

    public Vibix(String vibixApiKey) {
        vibix_api_key = vibixApiKey;
    }

    public interface ConnectionVibix {
        void startParseVibix();

        void finishParseFilmVibix(EPData.Film vibixFilm);

        void finishParseSerialVibix(EPData.Serial vibixSerial);

        void errorParseVibix(IOException e);
    }

    private ConnectionVibix connectionVibix;


    public void parse(int kinopoiskID, ConnectionVibix connectionVibix) {
        this.connectionVibix = connectionVibix;
        this.kinopoiskID = kinopoiskID;
        headers.put("accept", "application/json");
        headers.put("Authorization", vibix_api_key);
        headers.put("X-CSRF-TOKEN", "");
        Handler handlerResponse = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                Bundle bundle = msg.getData();
                if (bundle.getBoolean("ok")) {
                    String responseBody = bundle.getString("responseBody", "");
                    if (responseBody.isEmpty()) {
                        connectionVibix.errorParseVibix(new IOException("Пустой ответ от сервера"));
                    } else {
                        if (JsonParser.parseString(responseBody).isJsonObject()) {
                            try {
                                JSONObject jsonObject = new JSONObject(responseBody);
                                String CURRENT_TYPE_CONTENT = jsonObject.getString("type");
                                String IFRAME_URL = jsonObject.getString("iframe_url");

                                // После получения iframe_url ссылки на плеер, получаем и парсим html страницу
                                parseHTML(IFRAME_URL, CURRENT_TYPE_CONTENT);

                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    }


                } else {
                    connectionVibix.errorParseVibix(new IOException(bundle.getString("error")));
                }
                return false;
            }
        });

        String urlStr = "https://vibix.org/api/v1/publisher/videos/kp/" + kinopoiskID;
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder requestBuilder = new Request.Builder();
        for (String headerKey : headers.keySet()) {
            requestBuilder.addHeader(headerKey, Objects.requireNonNull(headers.get(headerKey)));
        }
        requestBuilder.url(urlStr);
        Request request = requestBuilder.build();
        connectionVibix.startParseVibix();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("ok", false);
                bundle.putString("responseBody", "");
                bundle.putString("error", e.getMessage());
                bundle.putInt("codeResponse", -1);
                Message msg = new Message();
                msg.setData(bundle);
                handlerResponse.sendMessage(msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Bundle bundle = new Bundle();
                    ResponseBody body = response.body();
                    if (body == null) {
                        onFailure(call, new IOException(response.message()));
                        return;
                    }
                    String responseBody = body.string();
                    bundle.putBoolean("ok", true);
                    bundle.putString("responseBody", responseBody);
                    bundle.putString("error", response.message());
                    bundle.putInt("codeResponse", response.code());
                    Message msg = new Message();
                    msg.setData(bundle);
                    handlerResponse.sendMessage(msg);
                } else {
                    onFailure(call, new IOException(response.message()));
                }
            }
        });
    }

    // Здесь получение страницы HTML и ее парсинг
    private void parseHTML(String iframe_url, String typeContent) {
        Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                Bundle bundle = msg.getData();
                if (bundle == null) return false;
                boolean ok = bundle.getBoolean("ok", false);
                if (!ok) return false;
                String typeContent = bundle.getString("type", "");
                if (typeContent.isEmpty()) {
                    connectionVibix.errorParseVibix(new IOException("Пустой тип контента при парсинге"));
                }
                if (typeContent.equals(TYPE_CONTENT_SERIAL)) {
                    EPData.Serial vibixSerial = (EPData.Serial) bundle.getSerializable("data");
                    connectionVibix.finishParseSerialVibix(vibixSerial);
                } else {
                    EPData.Film vibixFilm = (EPData.Film) bundle.getSerializable("data");
                    connectionVibix.finishParseFilmVibix(vibixFilm);
                }
                return false;
            }
        });
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document document = Jsoup.connect(iframe_url).userAgent(USER_AGENT).timeout(5000).get();
                    String htmlStr = document.toString();
                    // Тут необходимо сделать развилку по типу контента, - фильм или сериал
                    if (typeContent.equals(TYPE_CONTENT_FILM)) {
                        parseHTMLFilm(handler, htmlStr);
                    } else if (typeContent.equals(TYPE_CONTENT_SERIAL)) {
                        parseHTMLSerial(handler, htmlStr);
                    }

                } catch (IOException | JSONException e) {
                    if (e instanceof JSONException) {
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("ok", false);
                        bundle.putString("responseBody", "");
                        bundle.putString("error", "Оибка парсинга JSON: " + e.getMessage());
                        bundle.putInt("codeResponse", -1);
                        Message msg = new Message();
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("ok", false);
                        bundle.putString("responseBody", "");
                        bundle.putString("error", "Ошибка парсинга HTML: " + e.getMessage());
                        bundle.putInt("codeResponse", -1);
                        Message msg = new Message();
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }
                }
            }
        });
        thread.start();
    }

    // Парсинг страницы если СЕРИАЛ
    private void parseHTMLSerial(Handler handler, String htmlStr) {
        Pattern pattern = Pattern.compile("\\[.+]");
        Matcher matcher = pattern.matcher(htmlStr);
        if (!matcher.find()) return;
        String jsonStr = matcher.group(0);
        // Теперь удаляем лишнее
        assert jsonStr != null;
        String dell = jsonStr.replace(",file:", "").replace(",poster", "");
        try {
            JSONArray jsonArray = new JSONArray(dell);
            createSerial(handler, jsonArray);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void createSerial(Handler handler, @NonNull JSONArray jsonArray) throws JSONException {
        EPData.Serial vibixSerial;
        ArrayList<EPData.Serial.Season> seasons = new ArrayList<>();

        // Проходим по всем сезонам
        for (int seasonIndex = 0; seasonIndex < jsonArray.length(); seasonIndex++) {
            JSONObject seasonJsonObj = jsonArray.getJSONObject(seasonIndex);
            String title = seasonJsonObj.getString("title"); // "Сезон 1"
            JSONArray folder = seasonJsonObj.getJSONArray("folder");
            ArrayList<EPData.Serial.Episode> episodes = new ArrayList<>();

            // Проходим по всем сериям в сезоне
            for (int episodeIndex = 0; episodeIndex < folder.length(); episodeIndex++) {
                JSONObject episodeJsonObj = folder.getJSONObject(episodeIndex);
                String episodeTitle = episodeJsonObj.getString("title"); // "Серия 1"
                String file = episodeJsonObj.getString("file");

                // Сначала разбиваем на блоки по качеству (480p, 720p, 1080p)
                String[] qualityBlocks = file.split("(?=\\[\\d+p])");

                // Мапа для группировки: Озвучка -> (Качество -> Ссылка)
                Map<String, Map<String, String>> voiceToQualityMap = new HashMap<>();

                for (String block : qualityBlocks) {
                    if (block.isBlank()) continue;

                    // Извлекаем качество (480p, 720p, 1080p)
                    Pattern qualityPattern = Pattern.compile("\\[(\\d+p)]");
                    Matcher qualityMatcher = qualityPattern.matcher(block);
                    String quality = qualityMatcher.find() ? qualityMatcher.group(1) : "unknown";

                    // Извлекаем все озвучки и ссылки в этом блоке качества
                    Pattern voicePattern = Pattern.compile("\\{([^}]+)\\}(https?://[^;,]+)");
                    Matcher voiceMatcher = voicePattern.matcher(block);

                    while (voiceMatcher.find()) {
                        String voice = voiceMatcher.group(1); // "Кравец", "LostFilm"
                        String url = voiceMatcher.group(2).replaceFirst("http", "https");

                        // Добавляем в мапу: если озвучки нет — создаем запись, иначе обновляем
                        if (!voiceToQualityMap.containsKey(voice)) {
                            voiceToQualityMap.put(voice, new HashMap<>());
                        }
                        voiceToQualityMap.get(voice).put(quality, url);
                    }
                }

                // Преобразуем мапу в список Translations
                ArrayList<EPData.Serial.Translations> translations = new ArrayList<>();
                for (Map.Entry<String, Map<String, String>> entry : voiceToQualityMap.entrySet()) {
                    String voice = entry.getKey();
                    Map<String, String> qualityToUrl = entry.getValue();

                    // Создаем список videoData для текущей озвучки
                    List<Map.Entry<String, String>> videoData = new ArrayList<>();
                    for (Map.Entry<String, String> qualityEntry : qualityToUrl.entrySet()) {
                        videoData.add(new AbstractMap.SimpleEntry<>(
                                qualityEntry.getKey(), // "480p", "720p", "1080p"
                                qualityEntry.getValue() // URL
                        ));
                    }

                    // Добавляем озвучку в translations
                    EPData.Serial.Translations.Builder builder = new EPData.Serial.Translations.Builder();
                    builder.setTitle(voice);
                    builder.setVideoData(videoData);
                    translations.add(builder.build());
                }

                // Создаем серию и добавляем в episodes
                EPData.Serial.Episode.Builder episodeBuilder = new EPData.Serial.Episode.Builder();
                episodeBuilder.setTitle(episodeTitle);
                episodeBuilder.setTranslations(translations);
                episodes.add(episodeBuilder.build());
            }

            // Добавляем сезон в seasons
            EPData.Serial.Season.Builder seasonBuilder = new EPData.Serial.Season.Builder();
            seasonBuilder.setTitle(title);
            seasonBuilder.setEpisodes(episodes);
            seasons.add(seasonBuilder.build());
        }

        // Собираем финальный объект Serial и отправляем через Handler
        EPData.Serial.Builder serialBuilder = new EPData.Serial.Builder();
        serialBuilder.setSeasons(seasons);
        vibixSerial = serialBuilder.build();

        Bundle bundle = new Bundle();
        bundle.putBoolean("ok", true);
        bundle.putSerializable("data", vibixSerial);
        bundle.putString("type", TYPE_CONTENT_SERIAL);
        Message message = new Message();
        message.setData(bundle);
        handler.sendMessage(message);
    }

    // Парсинг страницы если ФИЛЬМ
    private void parseHTMLFilm(Handler handler, String htmlStr) throws JSONException {
        // Более гибкое регулярное выражение
        Pattern pattern = Pattern.compile("new Playerjs\\(\\s*(\\{.+?\\})\\s*\\)");
        Matcher matcher = pattern.matcher(htmlStr);
        JSONObject jsonObject = new JSONObject();

        if (matcher.find()) {
            String jsonObjectString = matcher.group(1);
            jsonObject = new JSONObject(jsonObjectString);
            String id = jsonObject.getString("id");
            String poster = jsonObject.getString("poster");
            JSONArray translations = jsonObject.getJSONArray("file");
            ArrayList<EPData.Film.Translations> translationsList = new ArrayList<>();

            for (int translationIndex = 0; translationIndex < translations.length(); translationIndex++) {
                JSONObject translation = translations.getJSONObject(translationIndex);
                List<Map.Entry<String, String>> videoData = new ArrayList<>();
                String title = translation.getString("title");
                String file = translation.getString("file");
                String[] entries = file.split(",");
                for (String entry : entries) {
                    String[] parts = entry.split("]");
                    if (parts.length == 2) {
                        String quality = parts[0].substring(1); // Remove the opening bracket
                        String url = parts[1].replaceFirst("http", "https");
                        videoData.add(new AbstractMap.SimpleEntry<>(quality, url));
                    }
                }
                EPData.Film.Translations.Builder builderTranslations = new EPData.Film.Translations.Builder();
                builderTranslations.setTitle(title);
                builderTranslations.setVideoData(videoData);
                translationsList.add(builderTranslations.build());
            }

            EPData.Film.Builder vibixFilmBuilder = new EPData.Film.Builder();
            vibixFilmBuilder.setId(id);
            vibixFilmBuilder.setPoster(poster);
            vibixFilmBuilder.setTranslations(translationsList);
            EPData.Film vibixFilm = vibixFilmBuilder.build();

            Bundle bundle = new Bundle();
            bundle.putBoolean("ok", true);
            bundle.putSerializable("data", vibixFilm);
            bundle.putString("type", TYPE_CONTENT_FILM);
            Message message = new Message();
            message.setData(bundle);
            handler.sendMessage(message);

            /*pattern = Pattern.compile("\\[(MP4\\s+[0-9]+p)\\](https?:\\/\\/[^\\s,]+\\.mp4)");
            matcher = pattern.matcher(htmlStr);
            JSONArray jsonArrayFilms = getJsonArray(matcher);*/

        } else {
            System.out.println("JSON object not found.");
        }

        Bundle bundle = new Bundle();
        bundle.putString("data", jsonObject.toString());
        Message message = new Message();
        message.setData(bundle);
        handler.sendMessage(message);

    }

    // 13.03.2025 - не помню почему не удалил данный метод) пока что оставлю
    // Extract метод, парсит HTML и возвращает JSONArray ссылок (метод для ФИЛЬМОВ)
    private @NonNull JSONArray getJsonArray(Matcher matcher) throws JSONException {
        JSONArray jsonArrayFilms = new JSONArray();
        while (matcher.find()) {
            JSONObject jsonFilmParsedHTML = new JSONObject();
            String tag = matcher.group(1).replaceAll("MP4", "").replaceAll(" ", "");    // захват групп 1: тег
            String url = matcher.group(2);                                                                                    // захват групп 2: URL
            jsonFilmParsedHTML.put(tag, url);
            jsonArrayFilms.put(jsonFilmParsedHTML);
        }
        return jsonArrayFilms;
    }


}
