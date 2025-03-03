package com.alaka_ala.florafilm.ui.util.api.hdvb;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alaka_ala.florafilm.ui.util.api.EPData;
import com.alaka_ala.florafilm.ui.util.api.hdvb.models.HDVBFilm;
import com.alaka_ala.florafilm.ui.util.api.hdvb.models.HDVBSerial;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HDVB {
    public static final String TYPE_CONTENT_FILM = "movie";
    public static final String TYPE_CONTENT_SERIAL = "serial";
    private static final Map<String, String> headers = new HashMap<>();
    private final String API_KEY;

    private static String X_CSRF_TOKEN = "AALlPbfua1Kxj1K3Ohk$rlmBM-zm3e9ENIgU-sLEuU6K5OWgpdwEG8DyEC7FLwvV";
    private static String ORIGIN = "[url]vb17123filippaaniketos.pw[/url]";
    private static String IFRAME = "";
    private static String FILE;
    private static String HREF;                                                                // всегда актуальный заголовок headers - origin
    private static String CUID;
    private static String REFERER = "[url=vb17123filippaaniketos.pw][/url]";
    private static final String HDVB_API_DOMAIN = "https://apivb.com";


    public HDVB(String apiKey) {
        API_KEY = apiKey;
        createMapHeaders();
    }


    public void parse(int kinopoiskId, ResultParseCallback rpc) {
        connectHDVBApi(kinopoiskId, rpc);
    }

    // Сначала получаем инфу из офицального API
    private void connectHDVBApi(int kinopoiskId, ResultParseCallback rpc) {
        String urlString = HDVB_API_DOMAIN + "/api/videos.json?id_kp=" + kinopoiskId + "&token=" + API_KEY;
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder requestBuilder = new Request.Builder();
        for (String headerKey : headers.keySet()) {
            requestBuilder.addHeader(headerKey, Objects.requireNonNull(headers.get(headerKey)));
        }
        requestBuilder.url(urlString);
        Request request = requestBuilder.build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    if (JsonParser.parseString(body).isJsonArray()) {
                        try {
                            JSONArray jsonArray = new JSONArray(body);


                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            String type = jsonObject.getString("type");
                            if (type.equals(TYPE_CONTENT_FILM)) {
                                parseFilm(jsonArray, rpc);
                            } else if (type.equals(TYPE_CONTENT_SERIAL)) {
                                parseSerial(jsonObject, rpc);
                            }


                        } catch (JSONException ignored) {
                            Log.e("HDVB", "Ошибка парсинга JSON #1");
                        }
                    }
                }
            }
        });


    }


    // Парсинг фильмов
    private void parseFilm(JSONArray film, ResultParseCallback rpc) {
        EPData.Film.Builder hdvbDataFilm = new EPData.Film.Builder();

        ArrayList<EPData.Film.Translations> translations = new ArrayList<>();
        HDVBFilm.Builder builder = new HDVBFilm.Builder();
        for (int i = 0; i < film.length(); i++) {
            try {
                builder.setTitleRu(film.getJSONObject(i).getString("title_ru"));
                builder.setTitleEn(film.getJSONObject(i).getString("title_en"));
                builder.setYear(film.getJSONObject(i).getInt("year"));
                builder.setKinopoiskId(film.getJSONObject(i).getInt("kinopoisk_id"));
                builder.setTranslator(film.getJSONObject(i).getString("translator"));
                builder.setType(film.getJSONObject(i).getString("type"));
                builder.setIframeUrl(film.getJSONObject(i).getString("iframe_url"));
                builder.setQuality(film.getJSONObject(i).getString("quality"));
                builder.setTrailer(film.getJSONObject(i).getString("trailer"));
                builder.setAddedDate(film.getJSONObject(i).getString("added_date"));
                if (film.getJSONObject(i).get("block") instanceof JSONArray) {
                    JSONArray blockArray = film.getJSONObject(i).getJSONArray("block");
                    for (int i2 = 0; i2 < blockArray.length(); i2++) {
                        String country_code = blockArray.getString(i2);
                        HDVBFilm.Block blockBuilder = new HDVBFilm.Block(country_code);
                        builder.addBlock(blockBuilder);
                    }
                }
                IFRAME = film.getJSONObject(i).getString("iframe_url");

                String extractJsonFile = parseHtml(IFRAME);
                if (JsonParser.parseString(extractJsonFile).isJsonObject()) {
                    JSONObject json = new JSONObject(extractJsonFile);
                    X_CSRF_TOKEN = json.getString("key");
                    FILE = json.getString("file");
                    HREF = json.getString("href");
                    ORIGIN = "[url]" + HREF + "[/url]";
                    CUID = json.getString("cuid");


                    // если ответ с getFileFilm() равен коду ошибки
                    // 11 - ошибка связана с запросом к серверу, неверно что-то в заголовках (но это не точно)
                    // 10 -
                    String file = getFileFilm();

                    EPData.Film.Translations.Builder translationsBuilder = new EPData.Film.Translations.Builder();
                    translationsBuilder.setTitle(film.getJSONObject(i).getString("translator"));
                    List<Map.Entry<String, String>> videoData = new ArrayList<>();
                    videoData.add(new AbstractMap.SimpleEntry<>("HLS", file));
                    translationsBuilder.setVideoData(videoData);
                    translations.add(translationsBuilder.build());
                }
                hdvbDataFilm.setPoster(film.getJSONObject(i).getString("poster"));
                hdvbDataFilm.setTranslations(translations);
                builder.setHdvbDataFilm(hdvbDataFilm.build());

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("HDVB", "Ошибка парсинга JSON #1");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("HDVB", "Ошибка парсинга HTML #1");
            }
        }
        Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                rpc.film(builder.build());
                return false;
            }
        });
        handler.sendEmptyMessage(0);

    }

    // Получение ссылки на файл .m3u8 для фильма
    private String getFileFilm() throws IOException {
        createMapHeaders();
        String urlM3u8;
        if (FILE.startsWith("/playlist/") && FILE.endsWith(".txt")) {
            urlM3u8 = IFRAME.replaceAll("/movie.+", "") + FILE;
        } else {
            urlM3u8 = IFRAME.replaceAll("/movie.+", "") + "/playlist/" + FILE.replace("~", "") + ".txt";
        }

        URL url = new URL(urlM3u8);
        HttpURLConnection myURLConnection = (HttpURLConnection) url.openConnection();
        createMapHeaders();
        for (String headerKey : headers.keySet()) {
            myURLConnection.setRequestProperty(headerKey, headers.get(headerKey));
        }
        myURLConnection.setRequestMethod("POST");
        myURLConnection.setConnectTimeout(10000);
        myURLConnection.connect();
        if (myURLConnection.getResponseCode() == 200) {
            // запись ответа
            BufferedReader bufferedReader;
            bufferedReader = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
            String m3u8 = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
            bufferedReader.close();
            return m3u8;
        }
        return "";
    }


    // Парсинг сериалов
    private void parseSerial(JSONObject serial, ResultParseCallback rpc) {
        try {
            HDVBSerial.Builder hdvbSerialBuilder = new HDVBSerial.Builder();
            hdvbSerialBuilder.setTitleRu(serial.getString("title_ru"));
            hdvbSerialBuilder.setTitleEn(serial.getString("title_en"));
            hdvbSerialBuilder.setYear(serial.getInt("year"));
            hdvbSerialBuilder.setKinopoiskId(serial.getInt("kinopoisk_id"));
            hdvbSerialBuilder.setTranslator(serial.getString("translator"));
            hdvbSerialBuilder.setType(serial.getString("type"));
            hdvbSerialBuilder.setIframeUrl(serial.getString("iframe_url"));
            hdvbSerialBuilder.setQuality(serial.getString("quality"));
            hdvbSerialBuilder.setTrailer(serial.getString("trailer"));
            hdvbSerialBuilder.setAddedDate(serial.getString("added_date"));
            if (serial.get("block") instanceof JSONArray) {
                JSONArray blockArray = serial.getJSONArray("block");
                for (int i = 0; i < blockArray.length(); i++) {
                    String country_code = blockArray.getString(i);
                    EPData.Block blockBuilder = new EPData.Block(country_code);
                    hdvbSerialBuilder.addBlock(blockBuilder);
                }
            }
            IFRAME = serial.getString("iframe_url");

            String extractJsonFile = parseHtml(IFRAME);
            if (JsonParser.parseString(extractJsonFile).isJsonObject()) {
                // Создаем JSONObject на основе полученного объекта из HTML и присваевываем данные константам
                JSONObject json = new JSONObject(extractJsonFile);
                X_CSRF_TOKEN = json.getString("key");
                FILE = json.getString("file");
                HREF = json.getString("href");
                ORIGIN = "[url]" + HREF + "[/url]";
                CUID = json.getString("cuid");

                JSONArray seasons = getFilesURLSerial();


                EPData.Serial.Builder hdvbDataSerialBuilder = new EPData.Serial.Builder();
                ArrayList<EPData.Serial.Season> seas = new ArrayList<>();
                for (int i = 0; i < seasons.length(); i++) {
                    JSONObject season = seasons.getJSONObject(i);
                    String title = season.getString("title");
                    JSONArray episodes = season.getJSONArray("files");
                    EPData.Serial.Season.Builder seasonBuilder = new EPData.Serial.Season.Builder();
                    seasonBuilder.setTitle(title);
                    ArrayList<EPData.Serial.Episode> epis = new ArrayList<>();
                    for (int j = 0; j < episodes.length(); j++) {
                        String episode = episodes.getJSONObject(j).getString("title");
                        EPData.Serial.Episode.Builder episodeBuilder = new EPData.Serial.Episode.Builder();
                        episodeBuilder.setTitle(episode);

                        JSONArray translations = episodes.getJSONObject(j).getJSONArray("files");
                        ArrayList<EPData.Serial.Translations> transl = new ArrayList<>();
                        for (int k = 0; k < translations.length(); k++) {
                            Object translationJson = translations.get(k);
                            if (translationJson instanceof JSONArray) continue;
                            String translation = translations.getJSONObject(k).getString("title");
                            String file = translations.getJSONObject(k).getString("file");
                            String text2 = translations.getJSONObject(k).getString("text2");
                            List<Map.Entry<String, String>> videoData = new ArrayList<>();
                            videoData.add(new AbstractMap.SimpleEntry<>("HLS", file));

                            EPData.Serial.Translations.Builder translationsBuilder = new EPData.Serial.Translations.Builder();
                            translationsBuilder.setTitle(translation);
                            translationsBuilder.setVideoData(videoData);
                            transl.add(translationsBuilder.build());
                        }
                        episodeBuilder.setTranslations(transl);
                        epis.add(episodeBuilder.build());

                    }
                    seasonBuilder.setEpisodes(epis);
                    seas.add(seasonBuilder.build());
                }
                hdvbDataSerialBuilder.setSeasons(seas);
                hdvbSerialBuilder.setHdvbDataSerial(hdvbDataSerialBuilder.build());
                // Можно экспортировать
                HDVBSerial hdvbSerial = hdvbSerialBuilder.build();
                Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message msg) {
                        rpc.serial(hdvbSerial);
                        return false;
                    }
                });
                handler.sendEmptyMessage(0);

            }
        } catch (JSONException e) {
            Log.e("HDVB", "Ошибка парсинга JSON #2 | " + e);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("HDVB", "Ошибка парсинга HTML #2 | " + e);
        }
    }

    // получение JSON файла со списком сезонов и серий
    private JSONArray getFilesURLSerial() {
        String urlStr = IFRAME.replaceAll(".com.+", ".com") + FILE + "?key=" + X_CSRF_TOKEN + "&href=" + HREF + "&cuid=" + CUID;
        URL createUrl = null;
        try {
            createUrl = new URL(urlStr);
            HttpURLConnection myURLConnection = null;
            myURLConnection = (HttpURLConnection) createUrl.openConnection();
            createMapHeaders();
            for (String headerKey : headers.keySet()) {
                myURLConnection.setRequestProperty(headerKey, headers.get(headerKey));
            }
            myURLConnection.setRequestMethod("GET");
            //myURLConnection.setConnectTimeout(10000);
            myURLConnection.connect();

            BufferedReader bufferedReader;

            if (myURLConnection.getResponseCode() == 200) {

                // запись ответа
                bufferedReader = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
                String redifiningVarFiles = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
                String redifiningVarFolder = redifiningVarFiles.replaceAll("folder", "files");

                JSONArray jsonArray = new JSONArray(redifiningVarFolder);
                return jsonArray;

            } else {
                bufferedReader = new BufferedReader(new InputStreamReader(myURLConnection.getErrorStream()));
                String response = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));

            }

        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
        return new JSONArray();
    }

    private static final Queue<String> requestQueue = new LinkedList<>();
    private static boolean isProcessing = false;
    public static void getFileSerial(String episodeToken, CallbackSerialGetFile cb) {
        // Добавляем запрос в очередь
        requestQueue.add(episodeToken);

        // Если обработка не запущена, запускаем её
        if (!isProcessing) {
            processNextRequest(cb);
        }
    }
    // Приватный метод предназначен для последовательного парсинга файлов на серии сериалов,
    // то есть можно в цикле вызывать несколько раз данный метод,
    // но выполняться они будут поочередно,
    // последовательно после завершения предыдущего.
    private static void processNextRequest(CallbackSerialGetFile cb) {
        if (requestQueue.isEmpty()) {
            isProcessing = false;
            cb.finish(); // Вызываем finish(), когда все запросы выполнены
            return;
        }

        isProcessing = true;
        String episodeToken = requestQueue.poll();
        String urlM3u8 = "https://" + HREF + "/playlist/" + episodeToken + ".txt";
        createMapHeaders();

        Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                Bundle bundle = msg.getData();
                if (bundle.getBoolean("ok")) {
                    String m3u8 = bundle.getString("file", "");
                    cb.success(m3u8);
                } else {
                    String error = bundle.getString("error", "");
                    cb.error(error);
                }

                // После завершения текущего запроса, обрабатываем следующий
                processNextRequest(cb);
                return false;
            }
        });

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                URL createUrl = null;
                try {
                    createUrl = new URL(urlM3u8);
                    HttpURLConnection myURLConnection = null;
                    myURLConnection = (HttpURLConnection) createUrl.openConnection();
                    for (String headerKey : headers.keySet()) {
                        myURLConnection.setRequestProperty(headerKey, headers.get(headerKey));
                    }
                    myURLConnection.setRequestMethod("GET");
                    myURLConnection.connect();
                    if (myURLConnection.getResponseCode() == 200) {
                        // запись ответа
                        BufferedReader bufferedReader;
                        bufferedReader = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
                        String urlsM3u8 = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
                        sendMessage(urlsM3u8, true, "");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    sendMessage("", false, e.getMessage());
                }
            }

            private void sendMessage(String urlsM3u8, boolean isOk, String error) {
                Bundle bundle = new Bundle();
                if (isOk) {
                    bundle.putBoolean("ok", true);
                    bundle.putString("file", urlsM3u8);
                    bundle.putString("error", "");
                    Message msg = new Message();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                } else {
                    bundle.putBoolean("ok", false);
                    bundle.putString("file", "");
                    bundle.putString("error", error);
                    Message msg = new Message();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            }
        });
        thread.start();
    }

    public interface CallbackSerialGetFile {
        void success(String url);
        void error(String error);
        void finish();
    }


    // Получает HTML страницу и выполняет поиск JSONObject методом extractJson()
    private String parseHtml(String iframe) throws IOException {
        Document html = Jsoup.connect(iframe).headers(headers).get();
        return extractJson(html.toString());

    }

    // Извлекает JSONObject из HTML
    private String extractJson(String html) {
        // Поиск JSONObject фалйа в HTML с помощью регулярного выражения
        Pattern pattern = Pattern.compile("playerConfigs = +(.+);{1}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            // Если JSONObject файл найден
            int countGroup = matcher.groupCount();
            // Проверяем что группы не равны 0
            if (countGroup != 0) {
                // Вырезаем JSONObject из HTML с помощью регулярного выражения
                return matcher.group(0).replaceAll("playerConfigs = ", "").replaceAll(";", "");
            }
        } else {
            // Если не нашло JSONObject файл пробуем другой regex
            pattern = Pattern.compile("playerConfigs = +(.+); {1}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            matcher = pattern.matcher(html);
            if (matcher.find()) {
                // Если JSONObject файл найден
                int countGroup = matcher.groupCount();
                // Проверяем что группы не равны 0
                if (countGroup != 0) {
                    // Вырезаем JSONObject из HTML с помощью регулярного выражения
                    return matcher.group(0).replaceAll("playerConfigs = ", "").replaceAll(";", "");
                }
            } else {
                pattern = Pattern.compile("playerConfigs = +(.+); {3}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                matcher = pattern.matcher(html);
                if (matcher.find()) {
                    // Если JSONObject файл найден
                    int countGroup = matcher.groupCount();
                    // Проверяем что группы не равны 0
                    if (countGroup != 0) {
                        // Вырезаем JSONObject из HTML с помощью регулярного выражения
                        return matcher.group(0).replaceAll("playerConfigs = ", "").replaceAll(";", "");
                    }
                } else {
                    pattern = Pattern.compile("playerConfigs = +(.+);{3}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                    matcher = pattern.matcher(html);
                    if (matcher.find()) {
                        // Если JSONObject файл найден
                        int countGroup = matcher.groupCount();
                        // Проверяем что группы не равны 0
                        if (countGroup != 0) {
                            // Вырезаем JSONObject из HTML с помощью регулярного выражения
                            return matcher.group(0).replaceAll("playerConfigs = ", "").replaceAll(";", "");
                        }
                    } else {
                        pattern = Pattern.compile("playerConfigs = +(.+); {2}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                        matcher = pattern.matcher(html);
                    }
                    if (matcher.find()) {
                        // Если JSONObject файл найден
                        int countGroup = matcher.groupCount();
                        // Проверяем что группы не равны 0
                        if (countGroup != 0) {
                            // Вырезаем JSONObject из HTML с помощью регулярного выражения
                            return matcher.group(0).replaceAll("playerConfigs = ", "").replaceAll(";", "");
                        }
                    } else {
                        pattern = Pattern.compile("playerConfigs = +(.+);{2}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                        matcher = pattern.matcher(html);
                    }
                }
            }
        }

        return "";
    }

    // Создание заголовков
    private static void createMapHeaders() {
        final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36";
        final String ACCEPT = "*/*";
        final String ACCEPT_ENCODING = "text, deflate, br";
        final String ACCEPT_LANGUAGE = "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7";
        final String CONTENT_TYPE = "application/x-www-form-urlencoded";
        final String SEC_CH_UA = "Not A;Brand\";v=\"99\", \"Chromium\";v=\"99\", \"Google Chrome\";v=\"99\"";
        final String SEC_CH_UA_MOBILE = "?1";
        final String SEC_CH_UA_PLATFORM = "Android";
        final String SEC_FETCH_DEST = "empty";
        final String SEC_FETCH_MODE = "cors";
        final String SEC_FETCH_SITE = "same-origin";
        REFERER = "[url=" + IFRAME + "][/url]";

        headers.put("User-Agent", USER_AGENT);
        headers.put("accept", ACCEPT);
        headers.put("accept-encoding", ACCEPT_ENCODING);
        headers.put("accept-language", ACCEPT_LANGUAGE);
        headers.put("content-type", CONTENT_TYPE);
        headers.put("origin", ORIGIN);
        headers.put("referer", REFERER);
        headers.put("sec-ch-ua", SEC_CH_UA);
        headers.put("sec-ch-ua-mobile", SEC_CH_UA_MOBILE);
        headers.put("sec-ch-ua-platform", SEC_CH_UA_PLATFORM);
        headers.put("sec-fetch-dest", SEC_FETCH_DEST);
        headers.put("sec-fetch-mode", SEC_FETCH_MODE);
        headers.put("sec-fetch-site", SEC_FETCH_SITE);
        headers.put("x-csrf-token", X_CSRF_TOKEN);
    }

    public interface ResultParseCallback {
        void serial(HDVBSerial serial);

        void film(HDVBFilm film);

        void error(String err);

    }


}
