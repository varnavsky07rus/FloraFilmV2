package com.alaka_ala.florafilm.ui.util.api.lumex;

import android.os.Handler;
import android.os.Looper;

import com.alaka_ala.florafilm.ui.util.api.EPData;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.ItemFilmInfo;
import com.alaka_ala.florafilm.ui.util.api.lumex.models.movie.MoviePlayerResponse;
import com.alaka_ala.florafilm.ui.util.api.lumex.models.movie.MovieResponse;
import com.alaka_ala.florafilm.ui.util.api.lumex.models.serial.SeriesPlayerResponse;
import com.alaka_ala.florafilm.ui.util.api.lumex.models.serial.SeriesResponse;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LumexApi {
    private static final String API_TOKEN = "Fm4PitEIcN1zUvxT92jer99ybYFf9yHj";
    private static final String CLIENT_ID = "elIrHPVlNWOa";
    private ItemFilmInfo filmInfo;

    private Response getRequest(String url) {
        OkHttpClient client = new OkHttpClient()
                .newBuilder()
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .readTimeout(20000, TimeUnit.MILLISECONDS)
                .writeTimeout(20000, TimeUnit.MILLISECONDS)
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "/")
                .addHeader("Origin", "https://p.lumex.space")
                .addHeader("Referer", "https://p.lumex.space/")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 YaBrowser/25.8.0.0 Safari/537.36")
                .addHeader("sec-ch-ua", "\"Not)A;Brand\";v=\"8\", \"Chromium\";v=\"138\", \"YaBrowser\";v=\"25.8\", \"Yowser\";v=\"2.5\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-ch-ua-platform", "Windows")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-site", "same-site")
                .addHeader("x-csrf-token", "4207e3aa340e01ea82d405b4f5911e6ea8dd0047b6f13f5e3468382db28fb1ea97d4a6f0d77f8b44dcb3a07a257bee2e36a38da5543898c0139af564101f06d4")
                .addHeader("Cookie", "x-csrf-token=4207e3aa340e01ea82d405b4f5911e6ea8dd0047b6f13f5e3468382db28fb1ea97d4a6f0d77f8b44dcb3a07a257bee2e36a38da5543898c0139af564101f06d4%7C6cb35fecbfbd8b6414d8a339e7add6472dfe47bea9e253b9c9202b20bb5f593c; x-csrf-token=4207e3aa340e01ea82d405b4f5911e6ea8dd0047b6f13f5e3468382db28fb1ea97d4a6f0d77f8b44dcb3a07a257bee2e36a38da5543898c0139af564101f06d4%7C6cb35fecbfbd8b6414d8a339e7add6472dfe47bea9e253b9c9202b20bb5f593c")
                .build();
        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Response postRequest(String url) {
        if (url.startsWith("/") || !url.startsWith("//")) {
            url = "https://api.lumex.space" + url;
        }
        OkHttpClient client = new OkHttpClient().newBuilder()
                .readTimeout(20000, TimeUnit.MILLISECONDS)
                .writeTimeout(20000, TimeUnit.MILLISECONDS)
                .connectTimeout(20000, TimeUnit.MILLISECONDS)
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader("Accept", "/")
                .addHeader("Origin", "https://p.lumex.space")
                .addHeader("Referer", "https://p.lumex.space/")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 YaBrowser/25.8.0.0 Safari/537.36")
                .addHeader("sec-ch-ua", "\"Not)A;Brand\";v=\"8\", \"Chromium\";v=\"138\", \"YaBrowser\";v=\"25.8\", \"Yowser\";v=\"2.5\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-ch-ua-platform", "Windows")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-site", "same-site")
                .addHeader("x-csrf-token", "4207e3aa340e01ea82d405b4f5911e6ea8dd0047b6f13f5e3468382db28fb1ea97d4a6f0d77f8b44dcb3a07a257bee2e36a38da5543898c0139af564101f06d4")
                .addHeader("Cookie", "x-csrf-token=4207e3aa340e01ea82d405b4f5911e6ea8dd0047b6f13f5e3468382db28fb1ea97d4a6f0d77f8b44dcb3a07a257bee2e36a38da5543898c0139af564101f06d4%7C6cb35fecbfbd8b6414d8a339e7add6472dfe47bea9e253b9c9202b20bb5f593c; x-csrf-token=4207e3aa340e01ea82d405b4f5911e6ea8dd0047b6f13f5e3468382db28fb1ea97d4a6f0d77f8b44dcb3a07a257bee2e36a38da5543898c0139af564101f06d4%7C6cb35fecbfbd8b6414d8a339e7add6472dfe47bea9e253b9c9202b20bb5f593c")
                .build();
        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Все ответы в callback приходят из отдельного потока.
     * Будьте осторожны при обновлении пользовательского интерфейса
     */
    public void getFromKinopoiskId(ItemFilmInfo filmInfo, CallbackLumex callbackLumex) {
        this.filmInfo = filmInfo;
        new Thread(new Runnable() {
            @Override
            public void run() {
                String stringUrl = "https://portal.lumex.host/api/short?api_token=" + API_TOKEN + "&kinopoisk_id=" + filmInfo.getKinopoiskId();
                try {
                    Response getContentId = getRequest(stringUrl);
                    if (getContentId != null) {
                        if (getContentId.body() != null) {
                            String body = getContentId.body().string();
                            // ответ с data, php, result
                            if (filmInfo.isSerial()) {
                                SeriesResponse seriesResponse = new Gson().fromJson(body, SeriesResponse.class);
                                stringUrl = "https://api.lumex.space/content?clientId=" + CLIENT_ID + "&contentType=tv-series&contentId=" + seriesResponse.getData().get(0).getId();
                                Response seriesPlayerResponse = getRequest(stringUrl);
                                if (seriesPlayerResponse != null) {
                                    if (seriesPlayerResponse.body() != null) {
                                        body = seriesPlayerResponse.body().string();
                                        SeriesPlayerResponse seriesResponseValid = new Gson().fromJson(body, SeriesPlayerResponse.class);
                                        EPData.Serial epDataSerial = createEPDataSerial(seriesResponseValid);
                                        callbackLumex.success(null, epDataSerial);
                                    } else {
                                        callbackLumex.error("#5 Ошибка получения данных");
                                    }
                                } else {
                                    callbackLumex.error("#4 Ошибка получения данных");
                                }
                            } else {
                                MovieResponse movieResponse = new Gson().fromJson(body, MovieResponse.class);
                                stringUrl = "https://api.lumex.space/content?clientId=" + CLIENT_ID + "&contentType=movie&contentId=" + movieResponse.getData().get(0).getId();
                                Response moviePlayerResponse = getRequest(stringUrl);
                                if (moviePlayerResponse != null) {
                                    if (moviePlayerResponse.body() != null) {
                                        body = moviePlayerResponse.body().string();
                                        MoviePlayerResponse moviePlayerResponseValid = new Gson().fromJson(body, MoviePlayerResponse.class);
                                        EPData.Film film = createEPDataFilm(moviePlayerResponseValid);
                                        callbackLumex.success(film, null);
                                    } else {
                                        callbackLumex.error("#4 Ошибка получения данных");
                                    }
                                } else {
                                    callbackLumex.error("#3 Ошибка получения данных");

                                }


                            }



                        } else {
                            callbackLumex.error("#2 Ошибка получения данных");

                        }
                    } else {
                        callbackLumex.error("#1 Ошибка получения данных");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    callbackLumex.error(e.getMessage());
                }

            }

            private EPData.Serial createEPDataSerial(SeriesPlayerResponse seriesResponseValid) {
                EPData.Serial.Builder serialBuilder = new EPData.Serial.Builder();
                serialBuilder.setKinopoiskId(seriesResponseValid.getPlayer().getKinopoiskId());
                serialBuilder.addBlock(null);
                ArrayList<EPData.Serial.Season> seasonArrayList = new ArrayList<>();
                for (int i = 0; i < seriesResponseValid.getPlayer().getMedia().size(); i++) {
                    // парсим сезоны
                    EPData.Serial.Season.Builder seasonBuilder = new EPData.Serial.Season.Builder();
                    seasonBuilder.setTitle(seriesResponseValid.getPlayer().getMedia().get(i).getSeasonName());
                    ArrayList<EPData.Serial.Episode> episodeArrayList = new ArrayList<>();
                    for (int j = 0; j < seriesResponseValid.getPlayer().getMedia().get(i).getEpisodes().size(); j++) {
                        // Парсим серии
                        EPData.Serial.Episode.Builder episodeBuilder = new EPData.Serial.Episode.Builder();
                        episodeBuilder.setTitle(seriesResponseValid.getPlayer().getMedia().get(i).getEpisodes().get(j).getName());
                        ArrayList<EPData.Serial.Translations> translationsArrayList = new ArrayList<>();
                        for (int k = 0; k < seriesResponseValid.getPlayer().getMedia().get(i).getEpisodes().get(j).getMedia().size(); k++) {
                            // Парсим озвучку
                            EPData.Serial.Translations.Builder translationsBuilder = new EPData.Serial.Translations.Builder();
                            translationsBuilder.setTitle(seriesResponseValid.getPlayer().getMedia().get(i).getEpisodes().get(j).getMedia().get(k).getTranslationName());
                            List<Map.Entry<String, String>> videoData = new ArrayList<>();
                            videoData.add(new AbstractMap.SimpleEntry<>("HLS", seriesResponseValid.getPlayer().getMedia().get(i).getEpisodes().get(j).getMedia().get(k).getPlaylist()));

                            translationsBuilder.setVideoData(videoData);
                            translationsArrayList.add(translationsBuilder.build());
                        }
                        episodeBuilder.setTranslations(translationsArrayList);
                        episodeArrayList.add(episodeBuilder.build());
                    }
                    seasonBuilder.setEpisodes(episodeArrayList);
                    seasonArrayList.add(seasonBuilder.build());
                }
                serialBuilder.setSeasons(seasonArrayList);
                return serialBuilder.build();
            }


            private EPData.Film createEPDataFilm(MoviePlayerResponse moviePlayerResponse) {
                //String contentType = apiResponseValid.getPlayer().getContentType();
                //if (contentType.equals("movie") || contentType.equals("anime")) {
                EPData.Film.Builder filmBuilder = new EPData.Film.Builder();
                ArrayList<EPData.Film.Translations> translationsArrayList = new ArrayList<>();
                for (int i = 0; i < moviePlayerResponse.getPlayer().getMedia().size(); i++) {
                    // Создание озвучки
                    EPData.Film.Translations.Builder t = new EPData.Film.Translations.Builder();
                    List<Map.Entry<String, String>> videoData = new ArrayList<>();
                    videoData.add(new AbstractMap.SimpleEntry<>("HLS", moviePlayerResponse.getPlayer().getMedia().get(i).getPlaylist()));
                    t.setVideoData(videoData);
                    t.setTitle(moviePlayerResponse.getPlayer().getMedia().get(i).getTranslationName());
                    translationsArrayList.add(t.build());
                }
                filmBuilder.setTranslations(translationsArrayList);
                // TODO: Корректно обработать имя (добавить разные варианты при отсуствуии русского названия)
                filmBuilder.setNameFilm(filmInfo.getNameRu());
                filmBuilder.addBlock(null);
                filmBuilder.setId(String.valueOf(moviePlayerResponse.getPlayer().getKinopoiskId()));
                filmBuilder.setPoster("http://st.kinopoisk.ru/images/film_big/" + moviePlayerResponse.getPlayer().getKinopoiskId() + ".jpg");
                return filmBuilder.build();
                //}
                //return null;
            }


        }).start();

    }


    public static void getHls(String validateUrl, CallbackLumexHls callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = postRequest(validateUrl);
                    if (response != null) {
                        if (response.body() != null) {
                            String body = response.body().string();
                            LumexHLS lumexHLS = new Gson().fromJson(body, LumexHLS.class);
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(() -> callback.success(lumexHLS));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static class LumexHLS {
        public String getUrl() {
            return url;
        }

        private String url;
    }

    public interface CallbackLumexHls {
        void success(LumexHLS lumexHLS);

        void error(String err);
    }


    /**
     * Путем проб и ошибок было решено сделать Callback в который все результаты приходят в отдельном потоке
     * по этому перед тем как изменять пользовательский интерфейс,
     * необходимо это делать через {@link Handler}
     * и при создании указать главный поток {@link Looper#getMainLooper()}
     */
    public interface CallbackLumex {
        void success(EPData.Film film, EPData.Serial serial);

        void error(String err);
    }


}
