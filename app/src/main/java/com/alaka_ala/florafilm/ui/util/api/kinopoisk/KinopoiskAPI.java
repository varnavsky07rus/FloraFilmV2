package com.alaka_ala.florafilm.ui.util.api.kinopoisk;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.Collection;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.Country;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.FilmTrailer;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.Genre;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.ItemFilmInfo;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.ListFilmItem;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.ListStaffItem;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.NewsMedia;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.StaffFilmsItem;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.StaffInfo;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.StaffSpouseItem;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Все методы выполняются асинхронно, но результат возвращает в основном потоке
 */
public class KinopoiskAPI {


    private Map<String, String> headers;
    private final String api_key;


    public KinopoiskAPI(String api_key) {
        this.api_key = api_key;
    }

    public interface RequestCallbackListVieos {
        void onSuccessVideo(ArrayList<FilmTrailer> filmTrailers);

        void onFailureVideo(IOException e);

        /**
         * Метод выполнится после завершения всех подключений...
         * Т.е. если вызвав к примеру несколько запросов getList() то каждый из них вызывает finish();
         * однако данный метод ограничивает вызов каждый раз этого метода ограничив его до одного.
         * На каждый вызов есть отдельный метод, onSuccess().
         * Данный метод будет вызываться столько раз, сколько будет вызвано новых запросов.
         * Так же будет вызываться onFailure() на каждый новый запрос
         */
        void finishVideo();
    }

    public interface RequesCallbackNewsMedia {
        void onSuccessNews(ArrayList<NewsMedia> newsMediaList);

        void onFailureNews(IOException e);

        /**
         * Метод выполнится после завершения всех подключений...
         * Т.е. если вызвав к примеру несколько запросов getList() то каждый из них вызывает finish();
         * однако данный метод ограничивает вызов каждый раз этого метода ограничив его до одного.
         * На каждый вызов есть отдельный метод, onSuccess().
         * Данный метод будет вызываться столько раз, сколько будет вызвано новых запросов.
         * Так же будет вызываться onFailure() на каждый новый запрос
         */
        void finishNews();
    }

    public interface RequestCallbackCollection {
        void onSuccess(Collection collection);

        void onFailure(IOException e);

        /**
         * Метод выполнится после завершения всех подключений...
         * Т.е. если вызвав к примеру несколько запросов getList() то каждый из них вызывает finish();
         * однако данный метод ограничивает вызов каждый раз этого метода ограничив его до одного.
         * На каждый вызов есть отдельный метод, onSuccess().
         * Данный метод будет вызываться столько раз, сколько будет вызвано новых запросов.
         * Так же будет вызываться onFailure() на каждый новый запрос
         */
        void finish();
    }

    public interface RequestCallbackInformationItem {
        void onSuccessInfoItem(ItemFilmInfo itemFilmInfo);

        void onFailureInfoItem(IOException e);

        /**
         * Метод выполнится после завершения всех подключений...
         * Т.е. если вызвав к примеру несколько запросов getList() то каждый из них вызывает finish();
         * однако данный метод ограничивает вызов каждый раз этого метода ограничив его до одного.
         * На каждый вызов есть отдельный метод, onSuccess().
         * Данный метод будет вызываться столько раз, сколько будет вызвано новых запросов.
         * Так же будет вызываться onFailure() на каждый новый запрос
         */
        default void finishInfoItem(){};
    }

    public interface RequestCallbackStaffList {
        void onSuccessStaffList(ArrayList<ListStaffItem> listStaffItem);

        void onFailureStaffList(IOException e);

        /**
         * Метод выполнится после завершения всех подключений...
         * Т.е. если вызвав к примеру несколько запросов getList() то каждый из них вызывает finish();
         * однако данный метод ограничивает вызов каждый раз этого метода ограничив его до одного.
         * На каждый вызов есть отдельный метод, onSuccess().
         * Данный метод будет вызываться столько раз, сколько будет вызвано новых запросов.
         * Так же будет вызываться onFailure() на каждый новый запрос
         */
        void finishStafList();
    }

    public interface RequestCallbackInformationStaff {
        void onSuccessInfoStaff(StaffInfo staffInfo);

        void onFailureInfoStaff(IOException e);

        /**
         * Метод выполнится после завершения всех подключений...
         * Т.е. если вызвав к примеру несколько запросов getList() то каждый из них вызывает finish();
         * однако данный метод ограничивает вызов каждый раз этого метода ограничив его до одного.
         * На каждый вызов есть отдельный метод, onSuccess().
         * Данный метод будет вызываться столько раз, сколько будет вызвано новых запросов.
         * Так же будет вызываться onFailure() на каждый новый запрос
         */
        void finishInfoStaff();
    }

    private interface ConntectCallback {
        void onSuccess(String responseJson);

        void onFailure(IOException e);

        void finish();
    }

    // Общее кол-во подключений
    private int previousCountConnections = 0;
    // Предыдущее кол-во подключений
    private int requestId = 0;
    private int countConnections = 0;

    /**
     * Метод для выполнения запроса к серверу.
     * Выполняется асинхронно, но результат возвращает в основном потоке
     */
    private void connect(String url, ConntectCallback callback) {
        countConnections++;
        previousCountConnections = countConnections;
        Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                countConnections--;
                int codeResponse = msg.getData().getInt("codeResponse", 0);
                String error = msg.getData().getString("error", "");
                int requestIdl = msg.getData().getInt("requestId", 0);
                String response = msg.getData().getString("response", "");
                boolean ok = msg.getData().getBoolean("ok", false);

                if (ok && codeResponse == 200) {
                    callback.onSuccess(response);
                } else {
                    callback.onFailure(new IOException("Код ответа: " + codeResponse + " Ошибка: " + response + " | " + error));
                }

                requestId = 0;
                callback.finish();

                return false;
            }
        });

        // Добавление заголовков в запрос
        headers = new HashMap<>();
        headers.put("X-API-KEY", api_key);
        //headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36");
        headers.put("Content-Type", "application/json");
        // Создание запроса
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url);
        for (String headerKey : headers.keySet()) {
            requestBuilder.addHeader(headerKey, headers.get(headerKey));
        }

        Request request = requestBuilder.build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Bundle bundle = new Bundle();
                bundle.putString("error", e.getMessage());
                bundle.putInt("codeResponse", 0);
                bundle.putInt("requestId", ++requestId);
                Message msg = new Message();
                msg.setData(bundle);
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Bundle bundle = new Bundle();
                ResponseBody responseBody = response.body();
                bundle.putBoolean("ok", response.isSuccessful());
                bundle.putInt("codeResponse", response.code());
                bundle.putString("error", response.message());
                if (responseBody != null) {
                    bundle.putString("response", responseBody.string());
                }
                bundle.putInt("requestId", ++requestId);
                Message msg = new Message();
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        });

    }


    private RequestCallbackCollection rcc;
    /**
     * Получение всех популярных фильмов и сериалов
     */
    public void getListTopPopularAll(int page, RequestCallbackCollection rcc) {
        this.rcc = rcc;
        String base_url = "https://kinopoiskapiunofficial.tech/api/v2.2/films/collections?type=TOP_POPULAR_ALL&page=" + page;
        connect(base_url, new ConntectCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    if (!response.isEmpty()) {
                        if (JsonParser.parseString(response).isJsonObject()) {
                            Collection collection = createCollectionClass("Популярные фильмы/сериалы", response);
                            rcc.onSuccess(collection);
                        }
                    } else {
                        onFailure(new IOException("Пустой ответ!"));
                    }

                } catch (JSONException e) {
                    onFailure(new IOException(e.getMessage()));
                }
            }

            @Override
            public void onFailure(IOException e) {
                rcc.onFailure(e);
            }

            @Override
            public void finish() {
                rcc.finish();
            }
        });
    }

    /**
     * Получение всех популярных фильмов (только фильмов)
     */
    public void getListTopPopularMovies(int page, RequestCallbackCollection rcc) {
        String base_url = "https://kinopoiskapiunofficial.tech/api/v2.2/films/collections?type=TOP_POPULAR_MOVIES&page=" + page;
        connect(base_url, new ConntectCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    if (!response.isEmpty()) {
                        if (JsonParser.parseString(response).isJsonObject()) {
                            Collection collection = createCollectionClass("Популярные фильмы", response);
                            rcc.onSuccess(collection);
                        }
                    } else {
                        onFailure(new IOException("Пустой ответ!"));
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(IOException e) {
                rcc.onFailure(e);
            }

            @Override
            public void finish() {
                rcc.finish();
            }
        });
    }

    /**
     * Получение топ 250 сериалов
     */
    public void getListTop250TVShows(int page, RequestCallbackCollection rcc) {
        String base_url = "https://kinopoiskapiunofficial.tech/api/v2.2/films/collections?type=TOP_250_TV_SHOWS&page=" + page;
        connect(base_url, new ConntectCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    if (!response.isEmpty()) {
                        if (JsonParser.parseString(response).isJsonObject()) {
                            Collection collection = createCollectionClass("Топ 250 сериалов", response);
                            rcc.onSuccess(collection);
                        }
                    } else {
                        onFailure(new IOException("Пустой ответ!"));
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(IOException e) {
                rcc.onFailure(e);
            }

            @Override
            public void finish() {
                rcc.finish();
            }
        });
    }

    /**
     * Получение топ 250 фильмов
     */
    public void getListTop250Movies(int page, RequestCallbackCollection rcc) {
        String base_url = "https://kinopoiskapiunofficial.tech/api/v2.2/films/collections?type=TOP_250_MOVIES&page=" + page;
        connect(base_url, new ConntectCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    if (!response.isEmpty()) {
                        if (JsonParser.parseString(response).isJsonObject()) {
                            Collection collection = createCollectionClass("Топ 250 фильмов", response);
                            rcc.onSuccess(collection);
                        }
                    } else {
                        onFailure(new IOException("Пустой ответ!"));
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(IOException e) {
                rcc.onFailure(e);
            }

            @Override
            public void finish() {
                rcc.finish();
            }
        });
    }

    /**
     * Получение списка фильмов/сериалов на тему: Вампиры
     */
    @Deprecated
    public void getListVampireTheme(int page, RequestCallbackCollection rcc) {
        String base_url = "https://kinopoiskapiunofficial.tech/api/v2.2/films/collections?type=VAMPIRE_THEME&page=" + page;
        connect(base_url, new ConntectCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    if (!response.isEmpty()) {
                        if (JsonParser.parseString(response).isJsonObject()) {
                            Collection collection = createCollectionClass("Про вампиров", response);
                            rcc.onSuccess(collection);
                        }
                    } else {
                        onFailure(new IOException("Пустой ответ!"));
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(IOException e) {
                rcc.onFailure(e);
            }

            @Override
            public void finish() {
                rcc.finish();
            }
        });
    }

    /**
     * Получение списка фильмов/сериалов на тему: Комиксы
     */
    public void getListComicsTheme(int page, RequestCallbackCollection rcc) {
        String base_url = "https://kinopoiskapiunofficial.tech/api/v2.2/films/collections?type=COMICS_THEME&page=" + page;
        connect(base_url, new ConntectCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    if (!response.isEmpty()) {
                        if (JsonParser.parseString(response).isJsonObject()) {
                            Collection collection = createCollectionClass("По комиксам", response);
                            rcc.onSuccess(collection);
                        }
                    } else {
                        onFailure(new IOException("Пустой ответ!"));
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(IOException e) {
                rcc.onFailure(e);
            }

            @Override
            public void finish() {
                rcc.finish();
            }
        });
    }

    /**
     * Получение списка фильмов/сериалов на тему: Семейные
     */
    @Deprecated
    public void getListFamily(int page, RequestCallbackCollection rcc) {
        String base_url = "https://kinopoiskapiunofficial.tech/api/v2.2/films?genres=19&order=RATING&type=FILM&ratingFrom=0&ratingTo=10&yearFrom=1000&yearTo=3000&page=" + page;
        connect(base_url, new ConntectCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    if (!response.isEmpty()) {
                        if (JsonParser.parseString(response).isJsonObject()) {
                            Collection collection = createCollectionClass("Семейные", response);
                            rcc.onSuccess(collection);
                        }
                    } else {
                        onFailure(new IOException("Пустой ответ!"));
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(IOException e) {
                rcc.onFailure(e);
            }

            @Override
            public void finish() {
                rcc.finish();
            }
        });
    }

    /**
     * Получение списка похожих фильмов по kinopoisk_id
     */
    public void getListSimilarFilms(int kinopoisk_id, int page, RequestCallbackCollection rcc) {
        String base_url = "https://kinopoiskapiunofficial.tech/api/v2.2/films/" + kinopoisk_id + "/similars" /*+ page;*/;
        connect(base_url, new ConntectCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    if (!response.isEmpty()) {
                        if (JsonParser.parseString(response).isJsonObject()) {
                            Collection collection = createCollectionClass("Похожие фильмы", response);
                            rcc.onSuccess(collection);
                        }
                    } else {
                        onFailure(new IOException("Пустой ответ!"));
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(IOException e) {
                rcc.onFailure(e);
            }

            @Override
            public void finish() {
                rcc.finish();
            }
        });
    }


    /**
     * Получение списка фильмов/сериалов по названию
     */
    public void getListSearch(String query, int page, RequestCallbackCollection rcc) {
        String query_encode = URLEncoder.encode(query);
        String base_url = "https://kinopoiskapiunofficial.tech/api/v2.1/films/search-by-keyword?keyword=" + query_encode + "&page=" + page;
        connect(base_url, new ConntectCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    if (!response.isEmpty()) {
                        if (JsonParser.parseString(response).isJsonObject()) {
                            Collection collection = createCollectionClass("Поиск", response);
                            rcc.onSuccess(collection);
                        }
                    } else {
                        onFailure(new IOException("Пустой ответ!"));
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(IOException e) {
                rcc.onFailure(e);
            }

            @Override
            public void finish() {
                rcc.finish();
            }
        });
    }

    /**
     * Получение списка фильмов/сериалов по стране
     */
    public void getListFromCountries(int page,  int country, RequestCallbackCollection rcc) {
        String base_url = "https://kinopoiskapiunofficial.tech/api/v2.2/films?countries=" + country + "&order=RATING&type=FILM&ratingFrom=0&ratingTo=10&yearFrom=1000&yearTo=3000&page=" + page;
        connect(base_url, new ConntectCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    if (!response.isEmpty()) {
                        if (JsonParser.parseString(response).isJsonObject()) {
                            Collection collection = createCollectionClass("Поиск по странам", response);
                            rcc.onSuccess(collection);
                        }
                    } else {
                        onFailure(new IOException("Пустой ответ!"));
                    }

                } catch (JSONException e) {
                    onFailure(new IOException("Ошибка JSON"));
                }
            }

            @Override
            public void onFailure(IOException e) {
                rcc.onFailure(e);
            }

            @Override
            public void finish() {
                rcc.finish();
            }
        });
    }



    /**
     * Получение списка фильмов/сериалов по жанру
     */
    public void getListFromGenre(@SuppressLint("SupportAnnotationUsage") @GenreConstants.GenreType  int genreId, int page, RequestCallbackCollection rcc) {
        String base_url = "https://kinopoiskapiunofficial.tech/api/v2.2/films?genres=" + genreId + "&order=RATING&type=ALL&ratingFrom=0&ratingTo=10&yearFrom=1000&yearTo=3000&page=" + page;
        connect(base_url, new ConntectCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    if (!response.isEmpty()) {
                        if (JsonParser.parseString(response).isJsonObject()) {
                            Collection collection = createCollectionClass(GenreConstants.getGenreName(genreId), response);
                            rcc.onSuccess(collection);
                        }
                    } else {
                        onFailure(new IOException("Пустой ответ!"));
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(IOException e) {
                rcc.onFailure(e);
            }

            @Override
            public void finish() {
                rcc.finish();
            }
        });
    }


    /**
     * Получение информации о фильме по его kinopoisk_id
     */
    public void getInforamationItem(int kinopoisk_id, RequestCallbackInformationItem rcii) {
        String base_url = "https://kinopoiskapiunofficial.tech/api/v2.2/films/" + kinopoisk_id;
        connect(base_url, new ConntectCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    if (!response.isEmpty()) {
                        if (JsonParser.parseString(response).isJsonObject()) {
                            ItemFilmInfo itemFilmInfo = createItemInfoClass(response);
                            rcii.onSuccessInfoItem(itemFilmInfo);
                        }
                    } else {
                        onFailure(new IOException("Пустой ответ!"));
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(IOException e) {
                rcii.onFailureInfoItem(e);
            }

            @Override
            public void finish() {
                rcii.finishInfoItem();
            }
        });
    }


    /**
     * Получение списка актёров/актрис по kinopoisk_id
     */
    public void getListStaff(int kinopoisk_id, RequestCallbackStaffList rca) {
        String base_url = "https://kinopoiskapiunofficial.tech/api/v1/staff?filmId=" + kinopoisk_id;
        connect(base_url, new ConntectCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    if (!response.isEmpty()) {
                        if (JsonParser.parseString(response).isJsonArray()) {
                            ArrayList<ListStaffItem> listStaffItem = createListStaffClass(response);
                            rca.onSuccessStaffList(listStaffItem);
                        }
                    } else {
                        onFailure(new IOException("Пустой ответ!"));
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(IOException e) {
                rca.onFailureStaffList(e);
            }

            @Override
            public void finish() {
                rca.finishStafList();
            }
        });
    }

    public void getListVieos(int kinopoisk_id, RequestCallbackListVieos rclv) {
        String base_url = "https://kinopoiskapiunofficial.tech/api/v2.2/films/" + kinopoisk_id + "/videos";
        connect(base_url, new ConntectCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    if (!response.isEmpty()) {
                        if (JsonParser.parseString(response).isJsonObject()) {
                            ArrayList<FilmTrailer> filmTrailers = createListVieosClass(response);
                            rclv.onSuccessVideo(filmTrailers);
                        }
                    } else {
                        onFailure(new IOException("Пустой ответ!"));
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(IOException e) {
                rclv.onFailureVideo(e);
            }

            @Override
            public void finish() {
                rclv.finishVideo();
            }
        });
    }

    private ArrayList<FilmTrailer> createListVieosClass(String response) throws JSONException {
        ArrayList<FilmTrailer> filmTrailers = new ArrayList<>();
        if (response.isEmpty()) return filmTrailers;
        JSONObject jsonObjectList = new JSONObject(response);
        if (jsonObjectList.has("items")) {
            JSONArray items = jsonObjectList.getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                String url = "";
                String name = "";
                String site = "";
                if (item.has("url")) {
                    url = item.getString("url");
                }
                if (item.has("name")) {
                    name = item.getString("name");
                }
                if (item.has("site")) {
                    site = item.getString("site");
                }
                filmTrailers.add(new FilmTrailer.Builder().setUrl(url).setName(name).setSite(site).build());
            }
        }

        return filmTrailers;
    }

    /**
     * Получение подробной информации об актёре/актрисе по staffId
     */
    public void getInformationStaff(int staffId, RequestCallbackInformationStaff rcis) {
        String base_url = "https://kinopoiskapiunofficial.tech/api/v1/staff/" + staffId;
        connect(base_url, new ConntectCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    if (!response.isEmpty()) {
                        if (JsonParser.parseString(response).isJsonObject()) {
                            StaffInfo staffInfo = createStaffInfoClass(response);
                            rcis.onSuccessInfoStaff(staffInfo);
                        }
                    } else {
                        onFailure(new IOException("Пустой ответ!"));
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(IOException e) {
                rcis.onFailureInfoStaff(e);
            }

            @Override
            public void finish() {
                rcis.finishInfoStaff();
            }
        });
    }

    public void getListNewsMedia(int page, RequesCallbackNewsMedia rcnm) {
        String base_url = "https://kinopoiskapiunofficial.tech/api/v1/media_posts?page=" + page;
        connect(base_url, new ConntectCallback() {
            @Override
            public void onSuccess(String responseJson) {
                try {
                    ArrayList<NewsMedia> newsMediaList = createNewsDataClass(responseJson);
                    rcnm.onSuccessNews(newsMediaList);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(IOException e) {
                rcnm.onFailureNews(e);
            }

            @Override
            public void finish() {
                rcnm.finishNews();
            }
        });
    }

    private ArrayList<NewsMedia> createNewsDataClass(String responseJson) throws JSONException {
        ArrayList<NewsMedia> newsMediaList = new ArrayList<>();
        int kinopoiskId = 0;
        String imageUrl = "";
        String title = "";
        String description = "";
        String urlPost = "";
        String publishedAt = "";
        if (responseJson.isEmpty()) return new ArrayList<>();
        JSONObject jsonObject = new JSONObject(responseJson);
        JSONArray items = jsonObject.getJSONArray("items");
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            if (item.has("kinopoiskId")) {
                kinopoiskId = item.getInt("kinopoiskId");
            }
            if (item.has("imageUrl")) {
                imageUrl = item.getString("imageUrl");
            }
            if (item.has("title")) {
                title = item.getString("title");
            }
            if (item.has("description")) {
                description = item.getString("description");
            }
            if (item.has("url")) {
                urlPost = item.getString("url");
            }
            if (item.has("publishedAt")) {
                publishedAt = item.getString("publishedAt");
            }
            newsMediaList.add(new NewsMedia("Новости Медиа", i, kinopoiskId, imageUrl, title, description, urlPost, publishedAt));
        }

        return newsMediaList;
    }

    private StaffInfo createStaffInfoClass(String response) throws JSONException {
        JSONObject jsonStaff = new JSONObject(response);
        int personId = 0;
        String webUrl = "";
        String nameRu = "";
        String nameEn = "";
        String sex = "";
        String posterUrl = "";
        int growth = 0;
        String birthday = "";
        String death = "";
        int age = 0;
        String birthplace = "";
        String deathplace = "";
        ArrayList<StaffSpouseItem> spouses = new ArrayList<>();
        int hasAwards = 0;
        String profession = "";
        ArrayList<String> facts = new ArrayList<>();
        ArrayList<StaffFilmsItem> films = new ArrayList<>();

        if (jsonStaff.has("personId")) {
            personId = jsonStaff.getInt("personId");
        }

        if (jsonStaff.has("webUrl")) {
            webUrl = jsonStaff.getString("webUrl");
        }

        if (jsonStaff.has("nameRu")) {
            nameRu = jsonStaff.getString("nameRu");
        }

        if (jsonStaff.has("nameEn")) {
            nameEn = jsonStaff.getString("nameEn");
        }

        if (jsonStaff.has("sex")) {
            sex = jsonStaff.getString("sex");
        }

        if (jsonStaff.has("posterUrl")) {
            posterUrl = jsonStaff.getString("posterUrl");
        }

        if (jsonStaff.has("growth")) {
            growth = jsonStaff.getInt("growth");
        }

        if (jsonStaff.has("birthday")) {
            birthday = jsonStaff.getString("birthday");
        }

        if (jsonStaff.has("death")) {
            death = jsonStaff.getString("death");
        }

        if (jsonStaff.has("age")) {
            age = jsonStaff.getInt("age");
        }

        if (jsonStaff.has("birthplace")) {
            birthplace = jsonStaff.getString("birthplace");
        }

        if (jsonStaff.has("deathplace")) {
            deathplace = jsonStaff.getString("deathplace");
        }

        if (jsonStaff.has("spouses")) {
            JSONArray spousesArray = jsonStaff.getJSONArray("spouses");
            for (int i = 0; i < spousesArray.length(); i++) {
                JSONObject spouse = spousesArray.getJSONObject(i);
                int personIdSpouse = 0;
                String nameSpouse = "";
                boolean divorcedSpouse = false;
                String divorcedReasonSpouse = "";
                String sexSpouse = "";
                int children = 0;
                String webUrlSpouse = "";
                String relationSpouse = "";

                if (spouse.has("personId")) {
                    personIdSpouse = spouse.getInt("personId");
                }
                if (spouse.has("name")) {
                    nameSpouse = spouse.getString("name");
                }
                if (spouse.has("divorced")) {
                    divorcedSpouse = spouse.getBoolean("divorced");
                }
                if (spouse.has("reason")) {
                    divorcedReasonSpouse = spouse.getString("reason");
                }
                if (spouse.has("sex")) {
                    sexSpouse = spouse.getString("sex");
                }
                if (spouse.has("children")) {
                    children = spouse.getInt("children");
                }
                if (spouse.has("webUrl")) {
                    webUrlSpouse = spouse.getString("webUrl");
                }
                if (spouse.has("relation")) {
                    relationSpouse = spouse.getString("relation");
                }
                StaffSpouseItem staffSpouseItem = new StaffSpouseItem(personIdSpouse, webUrlSpouse, nameSpouse, sexSpouse);
                spouses.add(staffSpouseItem);
            }
        }

        if (jsonStaff.has("hasAwards")) {
            hasAwards = jsonStaff.getInt("hasAwards");
        }

        if (jsonStaff.has("profession")) {
            profession = jsonStaff.getString("profession");
        }

        if (jsonStaff.has("facts")) {
            JSONArray factsArray = jsonStaff.getJSONArray("facts");
            for (int i = 0; i < factsArray.length(); i++) {
                facts.add(factsArray.getString(i));
            }
        }

        if (jsonStaff.has("films")) {
            JSONArray filmsArray = jsonStaff.getJSONArray("films");
            for (int i = 0; i < filmsArray.length(); i++) {
                JSONObject film = filmsArray.getJSONObject(i);
                int filmIdI = 0;
                String nameRuI = "";
                String nameEnI = "";
                int ratingI = 0;
                boolean generalI = false;
                String descriptionI = "";
                String professionKeyI = "";

                if (film.has("filmId")) {
                    if (film.get("filmId") instanceof Integer) {
                        filmIdI = film.getInt("filmId");
                    }
                }
                if (film.has("nameRu")) {
                    nameRuI = film.getString("nameRu");
                }
                if (film.has("nameEn")) {
                    nameEnI = film.getString("nameEn");
                }
                if (film.has("rating")) {
                    if (film.get("rating") instanceof Integer) {
                        filmIdI = film.getInt("rating");
                    }
                }
                if (film.has("general")) {
                    if (film.get("general") instanceof Boolean) {
                        generalI = film.getBoolean("general");
                    }
                }
                if (film.has("description")) {
                    descriptionI = film.getString("description");
                }
                if (film.has("professionKey")) {
                    professionKeyI = film.getString("professionKey");
                }

                StaffFilmsItem staffFilmsItem = new StaffFilmsItem(filmIdI, nameRuI, nameEnI, ratingI, generalI, descriptionI, professionKeyI);
                films.add(staffFilmsItem);
            }
        }


        return new StaffInfo(personId, webUrl, nameRu, nameEn, sex, posterUrl, growth, birthday, death, age, birthplace, deathplace, spouses, hasAwards, profession, facts, films);
    }

    private ArrayList<ListStaffItem> createListStaffClass(String response) throws JSONException {
        ArrayList<ListStaffItem> list = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(response);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            int staffId = 0;
            String nameRu = "";
            String nameEn = "";
            String description = "";
            String posterUrl = "";
            String professionText = "";
            String professionKey = "";
            if (jsonObject.has("staffId")) {
                staffId = jsonObject.getInt("staffId");
            }
            if (jsonObject.has("nameRu")) {
                nameRu = jsonObject.getString("nameRu");
            }
            if (jsonObject.has("nameEn")) {
                nameEn = jsonObject.getString("nameEn");
            }
            if (jsonObject.has("description")) {
                description = jsonObject.getString("description");
            }
            if (jsonObject.has("posterUrl")) {
                posterUrl = jsonObject.getString("posterUrl");
            }
            if (jsonObject.has("professionText")) {
                professionText = jsonObject.getString("professionText");
            }
            if (jsonObject.has("professionKey")) {
                professionKey = jsonObject.getString("professionKey");
            }
            list.add(new ListStaffItem(staffId, nameRu, nameEn, description, posterUrl, professionText, professionKey));
        }
        return list;
    }

    private Collection createCollectionClass(String titleCollection, String json) throws JSONException {
        // Создание JSON Объекта на основе ответа от сервера
        JSONObject jsonCollection = new JSONObject(json);
        // преобразование JSON Объекта в новый объект класса Collection
        String total = "0";
        String totalPages = "0";
        ArrayList<ListFilmItem> items = new ArrayList<>();


        if (jsonCollection.has("total")) {
            total = jsonCollection.getString("total");
        }
        if (jsonCollection.has("searchFilmsCountResult")) {
            total = jsonCollection.getString("searchFilmsCountResult");
        }
        // Если total равен 0 то значит фильмов нет и нет смысла дальше парсить
        if (total.equals("0")) {
            return new Collection(titleCollection, total, totalPages, items);
        }
        if (jsonCollection.has("totalPages")) {
            totalPages = jsonCollection.getString("totalPages");
        }
        if (jsonCollection.has("pagesCount")) {
            totalPages = jsonCollection.getString("pagesCount");
        }

        if (jsonCollection.has("items")) {
            JSONArray itemsArray = jsonCollection.getJSONArray("items");
            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject item = itemsArray.getJSONObject(i);

                // парсинг основной информации
                int kinopoiskId = 0;
                int imdbId = 0;
                double ratingKinopoisk = 0;
                int year = 0;
                String nameRu = "null",
                        nameEn = "null",
                        nameOriginal = "null",
                        ratingImdb = "null",
                        type = "null",
                        posterUrl = "null",
                        posterUrlPreview = "null",
                        coverUrl = "null",
                        logoUrl = "null",
                        description = "null",
                        ratingAgeLimits = "null";

                if (item.has("kinopoiskId")) {
                    if (item.get("kinopoiskId") instanceof Integer) {
                        kinopoiskId = item.getInt("kinopoiskId");
                    }
                }
                if (item.has("filmId")) {
                    if (item.get("filmId") instanceof Integer) {
                        kinopoiskId = item.getInt("filmId");
                    }
                }
                if (item.has("imdbId")) {
                    if (item.get("imdbId") instanceof Integer) {
                        imdbId = item.getInt("imdbId");
                    }
                }
                if (item.has("nameRu")) {
                    nameRu = item.getString("nameRu");
                    if (nameRu.equals("null") && item.has("nameEn")) {
                        nameRu = item.getString("nameEn");
                    }
                    if (nameRu.equals("null") && item.has("nameOriginal")) {
                        nameRu = item.getString("nameOriginal");
                    }
                }
                if (item.has("nameEn")) {
                    nameEn = item.getString("nameEn");
                    if (nameEn.equals("null") && item.has("nameRu")) {
                        nameEn = item.getString("nameRu");
                    }
                    if (nameEn.equals("null") && item.has("nameOriginal")) {
                        nameEn = item.getString("nameOriginal");
                    }
                }
                if (item.has("nameOriginal")) {
                    nameOriginal = item.getString("nameOriginal");
                }
                if (item.has("ratingKinopoisk")) {
                    if (item.get("ratingKinopoisk") instanceof Double) {
                        ratingKinopoisk = item.getDouble("ratingKinopoisk");
                    }
                }
                if (item.has("ratingImdb")) {
                    ratingImdb = item.getString("ratingImdb");
                }
                if (item.has("year")) {
                    if (item.get("year") instanceof Integer) {
                        year = item.getInt("year");
                    }
                }
                if (item.has("type")) {
                    type = item.getString("type");
                }
                if (item.has("posterUrl")) {
                    posterUrl = item.getString("posterUrl");
                }
                if (item.has("posterUrlPreview")) {
                    posterUrlPreview = item.getString("posterUrlPreview");
                }
                if (item.has("coverUrl")) {
                    coverUrl = item.getString("coverUrl");
                }
                if (item.has("logoUrl")) {
                    logoUrl = item.getString("logoUrl");
                }
                if (item.has("description")) {
                    description = item.getString("description");
                }
                if (item.has("ratingAgeLimits")) {
                    ratingAgeLimits = item.getString("ratingAgeLimits");
                }

                // парсинг стран
                ArrayList<Country> countries = new ArrayList<>();
                JSONArray jsonArrayCountry = new JSONArray();
                if (item.has("countries")) {
                    jsonArrayCountry = item.getJSONArray("countries");
                }
                for (int j = 0; j < jsonArrayCountry.length(); j++) {
                    String country = jsonArrayCountry.getJSONObject(j).getString("country");
                    countries.add(new Country(country));
                }

                // парсинг Жанров
                ArrayList<Genre> genres = new ArrayList<>();
                JSONArray jsonArrayGenre = new JSONArray();
                if (item.has("genres")) {
                    jsonArrayGenre = item.getJSONArray("genres");
                }
                for (int j = 0; j < jsonArrayGenre.length(); j++) {
                    String genre = jsonArrayGenre.getJSONObject(j).getString("genre");
                    genres.add(new Genre(genre));
                }

                items.add(new ListFilmItem(kinopoiskId, imdbId, nameRu, nameEn, nameOriginal, countries, genres, ratingKinopoisk, ratingImdb, year, type, posterUrl, posterUrlPreview, coverUrl, logoUrl, description, ratingAgeLimits));

            }
            return new Collection(titleCollection, total, totalPages, items);
        } else if (jsonCollection.has("films")) {
            JSONArray itemsArray = jsonCollection.getJSONArray("films");
            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject item = itemsArray.getJSONObject(i);

                // парсинг основной информации
                int kinopoiskId = 0;
                int imdbId = 0;
                double ratingKinopoisk = 0;
                int year = 0;
                String nameRu = "",
                        nameEn = "",
                        nameOriginal = "",
                        ratingImdb = "",
                        type = "",
                        posterUrl = "",
                        posterUrlPreview = "",
                        coverUrl = "",
                        logoUrl = "",
                        description = "",
                        ratingAgeLimits = "";

                if (item.has("kinopoiskId")) {
                    if (item.get("kinopoiskId") instanceof Integer) {
                        kinopoiskId = item.getInt("kinopoiskId");
                    }
                }
                if (item.has("filmId")) {
                    if (item.get("filmId") instanceof Integer) {
                        kinopoiskId = item.getInt("filmId");
                    }
                }
                if (item.has("imdbId")) {
                    if (item.get("imdbId") instanceof Integer) {
                        imdbId = item.getInt("imdbId");
                    }
                }
                if (item.has("nameRu")) {
                    nameRu = item.getString("nameRu");
                }
                if (item.has("nameEn")) {
                    nameEn = item.getString("nameEn");
                }
                if (item.has("nameOriginal")) {
                    nameOriginal = item.getString("nameOriginal");
                }
                if (item.has("ratingKinopoisk")) {
                    if (item.get("ratingKinopoisk") instanceof Double) {
                        ratingKinopoisk = item.getDouble("ratingKinopoisk");
                    }
                }
                if (item.has("ratingImdb")) {
                    ratingImdb = item.getString("ratingImdb");
                }
                if (item.has("year")) {
                    if (item.get("year") instanceof Double) {
                        year = item.getInt("year");
                    }
                }
                if (item.has("type")) {
                    type = item.getString("type");
                }
                if (item.has("posterUrl")) {
                    posterUrl = item.getString("posterUrl");
                }
                if (item.has("posterUrlPreview")) {
                    posterUrlPreview = item.getString("posterUrlPreview");
                }
                if (item.has("coverUrl")) {
                    coverUrl = item.getString("coverUrl");
                }
                if (item.has("logoUrl")) {
                    logoUrl = item.getString("logoUrl");
                }
                if (item.has("description")) {
                    description = item.getString("description");
                }
                if (item.has("ratingAgeLimits")) {
                    ratingAgeLimits = item.getString("ratingAgeLimits");
                }

                // парсинг стран
                ArrayList<Country> countries = new ArrayList<>();
                JSONArray jsonArrayCountry = new JSONArray();
                if (item.has("countries")) {
                    jsonArrayCountry = item.getJSONArray("countries");
                }
                for (int j = 0; j < jsonArrayCountry.length(); j++) {
                    String country = jsonArrayCountry.getJSONObject(j).getString("country");
                    countries.add(new Country(country));
                }

                // парсинг Жанров
                ArrayList<Genre> genres = new ArrayList<>();
                JSONArray jsonArrayGenre = new JSONArray();
                if (item.has("genres")) {
                    jsonArrayGenre = item.getJSONArray("genres");
                }
                for (int j = 0; j < jsonArrayGenre.length(); j++) {
                    String genre = jsonArrayGenre.getJSONObject(j).getString("genre");
                    genres.add(new Genre(genre));
                }

                items.add(new ListFilmItem(kinopoiskId, imdbId, nameRu, nameEn, nameOriginal, countries, genres, ratingKinopoisk, ratingImdb, year, type, posterUrl, posterUrlPreview, coverUrl, logoUrl, description, ratingAgeLimits));

            }
            return new Collection(titleCollection, total, totalPages, items);
        }
        return new Collection(titleCollection, "-2", "-2", new ArrayList<>());
    }

    private ItemFilmInfo createItemInfoClass(String json) throws JSONException {
        // Создание JSON Объекта на основе ответа от сервера
        JSONObject jsonItem = new JSONObject(json);
        // преобразование JSON Объекта в новый объект класса Collection
        int kinopoiskId = 0;
        String kinopoiskHDId = "0";
        String imdbId = "0";
        String nameRu = "0";
        String nameEn = "0";
        String nameOriginal = "0";
        String posterUrl = "0";
        String posterUrlPreview = "0";
        String coverUrl = "0";
        String logoUrl = "0";
        int reviewsCount = 0;
        int ratingGoodReview = 0;
        int ratingGoodReviewVoteCount = 0;
        double ratingKinopoisk = 0D;
        int ratingKinopoiskVoteCount = 0;
        int ratingImdb = 0;
        int ratingImdbVoteCount = 0;
        int ratingFilmCritics = 0;
        int ratingFilmCriticsVoteCount = 0;
        int ratingAwait = 0;
        int ratingAwaitCount = 0;
        int ratingRfCritics = 0;
        int ratingRfCriticsVoteCount = 0;
        String webUrl = "0";
        String year = "0";
        int filmLength = 0;
        String slogan = "0";
        String description = "0";
        String shortDescription = "0";
        String editorAnnotation = "0";
        boolean isTicketsAvailable = false;
        String productionStatus = "0";
        String type = "0";
        String ratingMpaa = "0";
        String ratingAgeLimits = "0";
        ArrayList<Country> countries = new ArrayList<>();
        ArrayList<Genre> genres = new ArrayList<>();
        String startYear = "0";
        String endYear = "0";
        boolean serial = false;
        boolean shortFilm = false;
        boolean completed = false;
        boolean hasImax = false;
        boolean has3D = false;
        boolean lastSync = false;


        if (jsonItem.has("kinopoiskId")) {
            if (jsonItem.get("kinopoiskId") instanceof Integer) {
                kinopoiskId = jsonItem.getInt("kinopoiskId");
            }
        }
        if (jsonItem.has("kinopoiskHDId")) {
            kinopoiskHDId = jsonItem.getString("kinopoiskHDId");
        }
        if (jsonItem.has("imdbId")) {
            imdbId = jsonItem.getString("imdbId");
        }
        if (jsonItem.has("nameRu")) {
            nameRu = jsonItem.getString("nameRu");
        }
        if (jsonItem.has("nameEn")) {
            nameEn = jsonItem.getString("nameEn");
        }
        if (jsonItem.has("nameOriginal")) {
            nameOriginal = jsonItem.getString("nameOriginal");
        }
        if (jsonItem.has("posterUrl")) {
            posterUrl = jsonItem.getString("posterUrl");
        }
        if (jsonItem.has("posterUrlPreview")) {
            posterUrlPreview = jsonItem.getString("posterUrlPreview");
        }
        if (jsonItem.has("coverUrl")) {
            coverUrl = jsonItem.getString("coverUrl");
        }
        if (jsonItem.has("logoUrl")) {
            logoUrl = jsonItem.getString("logoUrl");
        }
        if (jsonItem.has("reviewsCount")) {
            if (jsonItem.get("reviewsCount") instanceof Integer) {
                reviewsCount = jsonItem.getInt("reviewsCount");
            }
        }
        if (jsonItem.has("ratingGoodReview")) {
            if (jsonItem.get("ratingGoodReview") instanceof Integer) {
                ratingGoodReview = jsonItem.getInt("ratingGoodReview");
            }
        }
        if (jsonItem.has("ratingGoodReviewVoteCount")) {
            if (jsonItem.get("ratingGoodReviewVoteCount") instanceof Integer) {
                ratingGoodReviewVoteCount = jsonItem.getInt("ratingGoodReviewVoteCount");
            }
        }
        if (jsonItem.has("ratingKinopoisk")) {
            if (jsonItem.get("ratingKinopoisk") instanceof Double) {
                ratingKinopoisk = jsonItem.getDouble("ratingKinopoisk");
            }
        }
        if (jsonItem.has("ratingKinopoiskVoteCount")) {
            if (jsonItem.get("ratingKinopoiskVoteCount") instanceof Integer) {
                ratingKinopoiskVoteCount = jsonItem.getInt("ratingKinopoiskVoteCount");
            }
        }
        if (jsonItem.has("ratingImdb")) {
            if (jsonItem.get("ratingImdb") instanceof Integer) {
                ratingImdb = jsonItem.getInt("ratingImdb");
            }
        }
        if (jsonItem.has("ratingImdbVoteCount")) {
            if (jsonItem.get("ratingImdbVoteCount") instanceof Integer) {
                ratingImdbVoteCount = jsonItem.getInt("ratingImdbVoteCount");
            }
        }
        if (jsonItem.has("ratingFilmCritics")) {
            if (jsonItem.get("ratingFilmCritics") instanceof Integer) {
                ratingFilmCritics = jsonItem.getInt("ratingFilmCritics");
            }
        }
        if (jsonItem.has("ratingFilmCriticsVoteCount")) {
            if (jsonItem.get("ratingFilmCriticsVoteCount") instanceof Integer) {
                ratingFilmCriticsVoteCount = jsonItem.getInt("ratingFilmCriticsVoteCount");
            }
        }
        if (jsonItem.has("ratingAwait")) {
            if (jsonItem.get("ratingAwait") instanceof Integer) {
                ratingAwait = jsonItem.getInt("ratingAwait");
            }
        }
        if (jsonItem.has("ratingAwaitCount")) {
            if (jsonItem.get("ratingAwaitCount") instanceof Integer) {
                ratingAwaitCount = jsonItem.getInt("ratingAwaitCount");
            }
        }
        if (jsonItem.has("ratingRfCritics")) {
            if (jsonItem.get("ratingRfCritics") instanceof Integer) {
                ratingRfCritics = jsonItem.getInt("ratingRfCritics");
            }
        }
        if (jsonItem.has("ratingRfCriticsVoteCount")) {
            if (jsonItem.get("ratingRfCriticsVoteCount") instanceof Integer) {
                ratingRfCriticsVoteCount = jsonItem.getInt("ratingRfCriticsVoteCount");
            }
        }
        if (jsonItem.has("webUrl")) {
            webUrl = jsonItem.getString("webUrl");
        }
        if (jsonItem.has("year")) {
            year = jsonItem.getString("year");
        }
        if (jsonItem.has("filmLength")) {
            if (jsonItem.get("filmLength") instanceof Integer) {
                filmLength = jsonItem.getInt("filmLength");
            }
        }
        if (jsonItem.has("slogan")) {
            slogan = jsonItem.getString("slogan");
        }
        if (jsonItem.has("description")) {
            description = jsonItem.getString("description");
        }
        if (jsonItem.has("shortDescription")) {
            shortDescription = jsonItem.getString("shortDescription");
        }
        if (jsonItem.has("editorAnnotation")) {
            editorAnnotation = jsonItem.getString("editorAnnotation");
        }
        if (jsonItem.has("isTicketsAvailable")) {
            if (jsonItem.get("isTicketsAvailable") instanceof Boolean) {
                isTicketsAvailable = jsonItem.getBoolean("isTicketsAvailable");
            }
        }
        if (jsonItem.has("productionStatus")) {
            productionStatus = jsonItem.getString("productionStatus");
        }
        if (jsonItem.has("type")) {
            type = jsonItem.getString("type");
        }
        if (jsonItem.has("ratingMpaa")) {
            ratingMpaa = jsonItem.getString("ratingMpaa");
        }
        if (jsonItem.has("ratingAgeLimits")) {
            ratingAgeLimits = jsonItem.getString("ratingAgeLimits");
        }

        if (jsonItem.has("countries")) {
            if (jsonItem.get("countries") instanceof JSONArray) {
                JSONArray jsonArrayCountry = jsonItem.getJSONArray("countries");
                for (int i = 0; i < jsonArrayCountry.length(); i++) {
                    String country = jsonArrayCountry.getJSONObject(i).getString("country");
                    countries.add(new Country(country));
                }
            }
        }

        if (jsonItem.has("genres")) {
            if (jsonItem.get("genres") instanceof JSONArray) {
                JSONArray jsonArrayGenre = jsonItem.getJSONArray("genres");
                for (int i = 0; i < jsonArrayGenre.length(); i++) {
                    String genre = jsonArrayGenre.getJSONObject(i).getString("genre");
                    genres.add(new Genre(genre));
                }
            }
        }

        if (jsonItem.has("startYear")) {
            startYear = jsonItem.getString("startYear");
        }
        if (jsonItem.has("endYear")) {
            endYear = jsonItem.getString("endYear");
        }
        if (jsonItem.has("serial")) {
            if (jsonItem.get("serial") instanceof Boolean) {
                serial = jsonItem.getBoolean("serial");
            }
        }
        if (jsonItem.has("shortFilm")) {
            if (jsonItem.get("shortFilm") instanceof Boolean) {
                shortFilm = jsonItem.getBoolean("shortFilm");
            }
        }
        if (jsonItem.has("completed")) {
            if (jsonItem.get("completed") instanceof Boolean) {
                completed = jsonItem.getBoolean("completed");
            }
        }
        if (jsonItem.has("hasImax")) {
            if (jsonItem.get("hasImax") instanceof Boolean) {
                hasImax = jsonItem.getBoolean("hasImax");
            }
        }
        if (jsonItem.has("has3D")) {
            if (jsonItem.get("has3D") instanceof Boolean) {
                has3D = jsonItem.getBoolean("has3D");
            }
        }
        if (jsonItem.has("lastSync")) {
            if (jsonItem.get("lastSync") instanceof Boolean) {
                lastSync = jsonItem.getBoolean("lastSync");
            }
        }

        return new ItemFilmInfo(
                kinopoiskId,
                kinopoiskHDId,
                imdbId,
                nameRu,
                nameEn,
                nameOriginal,
                posterUrl,
                posterUrlPreview,
                coverUrl,
                logoUrl,
                reviewsCount,
                ratingGoodReview,
                ratingGoodReviewVoteCount,
                ratingKinopoisk,
                ratingKinopoiskVoteCount,
                ratingImdb,
                ratingImdbVoteCount,
                ratingFilmCritics,
                ratingFilmCriticsVoteCount,
                ratingAwait,
                ratingAwaitCount,
                ratingRfCritics,
                ratingRfCriticsVoteCount,
                webUrl,
                year,
                filmLength,
                slogan,
                description,
                shortDescription,
                editorAnnotation,
                isTicketsAvailable,
                productionStatus,
                type,
                ratingMpaa,
                ratingAgeLimits,
                countries,
                genres,
                startYear,
                endYear,
                serial,
                shortFilm,
                completed,
                hasImax,
                has3D,
                lastSync
        );
    }


    public static class GenreConstants {

        public static final int THRILLER = 1;
        public static final int DRAMA = 2;
        public static final int CRIME = 3;
        public static final int ROMANCE = 4;
        public static final int DETECTIVE = 5;
        public static final int SCI_FI = 6;
        public static final int ADVENTURE = 7;
        public static final int BIOGRAPHY = 8;
        public static final int FILM_NOIR = 9;
        public static final int WESTERN = 10;
        public static final int ACTION = 11;
        public static final int FANTASY = 12;
        public static final int COMEDY = 13;
        public static final int WAR = 14;
        public static final int HISTORY = 15;
        public static final int MUSIC = 16;
        public static final int HORROR = 17;
        public static final int ANIMATION = 18;
        public static final int FAMILY = 19;
        public static final int MUSICAL = 20;
        public static final int SPORT = 21;
        public static final int DOCUMENTARY = 22;
        public static final int SHORT_FILM = 23;
        public static final int ANIME = 24;
        public static final int EMPTY = 25;
        public static final int NEWS = 26;
        public static final int CONCERT = 27;
        public static final int ADULT = 28;
        public static final int CEREMONY = 29;
        public static final int REALITY_TV = 30;
        public static final int GAME = 31;
        public static final int TALK_SHOW = 32;
        public static final int KIDS = 33;

        private static final Map<Integer, String> GENRE_MAP = new HashMap<>();
        private static final Map<String, Integer> GENRE_ID_MAP = new HashMap<>();

        static {
            GENRE_MAP.put(THRILLER, "Триллер");
            GENRE_MAP.put(DRAMA, "Драма");
            GENRE_MAP.put(CRIME, "Криминал");
            GENRE_MAP.put(ROMANCE, "Мелодрама");
            GENRE_MAP.put(DETECTIVE, "Детектив");
            GENRE_MAP.put(SCI_FI, "Фантастика");
            GENRE_MAP.put(ADVENTURE, "Приключения");
            GENRE_MAP.put(BIOGRAPHY, "Биография");
            GENRE_MAP.put(FILM_NOIR, "Фильм-нуар");
            GENRE_MAP.put(WESTERN, "Вестерн");
            GENRE_MAP.put(ACTION, "Боевик");
            GENRE_MAP.put(FANTASY, "Фэнтези");
            GENRE_MAP.put(COMEDY, "Комедия");
            GENRE_MAP.put(WAR, "Военный");
            GENRE_MAP.put(HISTORY, "История");
            GENRE_MAP.put(MUSIC, "Музыка");
            GENRE_MAP.put(HORROR, "Ужасы");
            GENRE_MAP.put(ANIMATION, "Мультфильм");
            GENRE_MAP.put(FAMILY, "Семейный");
            GENRE_MAP.put(MUSICAL, "Мюзикл");
            GENRE_MAP.put(SPORT, "Спорт");
            GENRE_MAP.put(DOCUMENTARY, "Документальный");
            GENRE_MAP.put(SHORT_FILM, "Короткометражка");
            GENRE_MAP.put(ANIME, "Аниме");
            GENRE_MAP.put(EMPTY, "Неизвестный жанр");
            GENRE_MAP.put(NEWS, "Новости");
            GENRE_MAP.put(CONCERT, "Концерт");
            GENRE_MAP.put(ADULT, "Для взрослых");
            GENRE_MAP.put(CEREMONY, "Церемония");
            GENRE_MAP.put(REALITY_TV, "Реальное ТВ");
            GENRE_MAP.put(GAME, "Игра");
            GENRE_MAP.put(TALK_SHOW, "Ток-шоу");
            GENRE_MAP.put(KIDS, "Детский");

            for (Map.Entry<Integer, String> entry : GENRE_MAP.entrySet()) {
                GENRE_ID_MAP.put(entry.getValue().toLowerCase(), entry.getKey());
            }
        }

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({THRILLER, DRAMA, CRIME, ROMANCE, DETECTIVE, SCI_FI, ADVENTURE, BIOGRAPHY, FILM_NOIR, WESTERN,
                ACTION, FANTASY, COMEDY, WAR, HISTORY, MUSIC, HORROR, ANIMATION, FAMILY, MUSICAL,
                SPORT, DOCUMENTARY, SHORT_FILM, ANIME, EMPTY, NEWS, CONCERT, ADULT, CEREMONY, REALITY_TV,
                GAME, TALK_SHOW, KIDS})
        public @interface GenreType {}

        public static String getGenreName(@GenreType int genreId) {
            return GENRE_MAP.getOrDefault(genreId, "Неизвестно");
        }

        public static int getGenreId(String genreName) {
            return GENRE_ID_MAP.getOrDefault(genreName.toLowerCase(), EMPTY);
        }
    }

    public static class CountryConstants {

        // Мапа для хранения соответствия id и названия страны
        private static final Map<Integer, String> ID_TO_COUNTRY = new HashMap<>();
        private static final Map<String, Integer> COUNTRY_TO_ID = new HashMap<>();

        // Инициализация мап
        static {
            addCountry(1, "США");
            addCountry(2, "Швейцария");
            addCountry(3, "Франция");
            addCountry(4, "Польша");
            addCountry(5, "Великобритания");
            addCountry(6, "Швеция");
            addCountry(7, "Индия");
            addCountry(8, "Испания");
            addCountry(9, "Германия");
            addCountry(10, "Италия");
            addCountry(11, "Гонконг");
            addCountry(12, "Германия (ФРГ)");
            addCountry(13, "Австралия");
            addCountry(14, "Канада");
            addCountry(15, "Мексика");
            addCountry(16, "Япония");
            addCountry(17, "Дания");
            addCountry(18, "Чехия");
            addCountry(19, "Ирландия");
            addCountry(20, "Люксембург");
            addCountry(21, "Китай");
            addCountry(22, "Норвегия");
            addCountry(23, "Нидерланды");
            addCountry(24, "Аргентина");
            addCountry(25, "Финляндия");
            addCountry(26, "Босния и Герцеговина");
            addCountry(27, "Австрия");
            addCountry(28, "Тайвань");
            addCountry(29, "Новая Зеландия");
            addCountry(30, "Бразилия");
            addCountry(31, "Чехословакия");
            addCountry(32, "Мальта");
            addCountry(33, "СССР");
            addCountry(34, "Россия");
            addCountry(35, "Югославия");
            addCountry(36, "Португалия");
            addCountry(37, "Румыния");
            addCountry(38, "Хорватия");
            addCountry(39, "ЮАР");
            addCountry(40, "Куба");
            addCountry(41, "Колумбия");
            addCountry(42, "Израиль");
            addCountry(43, "Намибия");
            addCountry(44, "Турция");
            addCountry(45, "Бельгия");
            addCountry(46, "Сальвадор");
            addCountry(47, "Исландия");
            addCountry(48, "Венгрия");
            addCountry(49, "Южная Корея");
            addCountry(50, "Лихтенштейн");
            addCountry(51, "Болгария");
            addCountry(52, "Филиппины");
            addCountry(53, "Доминикана");
            addCountry(54, "");
            addCountry(55, "Марокко");
            addCountry(56, "Таиланд");
            addCountry(57, "Кения");
            addCountry(58, "Пакистан");
            addCountry(59, "Иран");
            addCountry(60, "Панама");
            addCountry(61, "Аруба");
            addCountry(62, "Ямайка");
            addCountry(63, "Греция");
            addCountry(64, "Тунис");
            addCountry(65, "Кыргызстан");
            addCountry(66, "Пуэрто-Рико");
            addCountry(67, "Казахстан");
            addCountry(68, "Югославия (ФР)");
            addCountry(69, "Алжир");
            addCountry(70, "Германия (ГДР)");
            addCountry(71, "Сингапур");
            addCountry(72, "Словакия");
            addCountry(73, "Афганистан");
            addCountry(74, "Индонезия");
            addCountry(75, "Перу");
            addCountry(76, "Бермуды");
            addCountry(77, "Монако");
            addCountry(78, "Зимбабве");
            addCountry(79, "Вьетнам");
            addCountry(80, "Антильские острова");
            addCountry(81, "Саудовская Аравия");
            addCountry(82, "Танзания");
            addCountry(83, "Ливия");
            addCountry(84, "Ливан");
            addCountry(85, "Кувейт");
            addCountry(86, "Египет");
            addCountry(87, "Литва");
            addCountry(88, "Венесуэла");
            addCountry(89, "Словения");
            addCountry(90, "Чили");
            addCountry(91, "Багамы");
            addCountry(92, "Эквадор");
            addCountry(93, "Коста-Рика");
            addCountry(94, "Кипр");
            addCountry(95, "Уругвай");
            addCountry(96, "Ирак");
            addCountry(97, "Мартиника");
            addCountry(98, "Эстония");
            addCountry(99, "ОАЭ");
            addCountry(100, "Бангладеш");
            addCountry(101, "Македония");
            addCountry(102, "Гвинея");
            addCountry(103, "Иордания");
            addCountry(104, "Латвия");
            addCountry(105, "Армения");
            addCountry(106, "Украина");
            addCountry(107, "Сирия");
            addCountry(108, "Шри-Ланка");
            addCountry(109, "Нигерия");
            addCountry(110, "Берег Слоновой Кости");
            addCountry(111, "Грузия");
            addCountry(112, "Сенегал");
            addCountry(113, "Монголия");
            addCountry(114, "Габон");
            addCountry(115, "Замбия");
            addCountry(116, "Албания");
            addCountry(117, "Камерун");
            addCountry(118, "Буркина-Фасо");
            addCountry(119, "Узбекистан");
            addCountry(120, "Малайзия");
            addCountry(121, "Сербия");
            addCountry(122, "Гана");
            addCountry(123, "Таджикистан");
            addCountry(124, "Гаити");
            addCountry(125, "Конго (ДРК)");
            addCountry(126, "Гватемала");
            addCountry(127, "Российская империя");
            addCountry(128, "Беларусь");
            addCountry(129, "Молдавия");
            addCountry(130, "Азербайджан");
            addCountry(131, "Палестина");
            addCountry(132, "Оккупированная Палестинская территория");
            addCountry(133, "Северная Корея");
            addCountry(134, "Никарагуа");
            addCountry(135, "Камбоджа");
            addCountry(136, "Ангола");
            addCountry(137, "Сербия и Черногория");
            addCountry(138, "Непал");
            addCountry(139, "Бенин");
            addCountry(140, "Гваделупа");
            addCountry(141, "Гренландия");
            addCountry(142, "Гвинея-Бисау");
            addCountry(143, "Макао");
            addCountry(144, "Парагвай");
            addCountry(145, "Мавритания");
            addCountry(146, "Руанда");
            addCountry(147, "Фарерские острова");
            addCountry(148, "Кот-д’Ивуар");
            addCountry(149, "Гибралтар");
            addCountry(150, "Ботсвана");
            addCountry(151, "Боливия");
            addCountry(152, "Мадагаскар");
            addCountry(153, "Кабо-Верде");
            addCountry(154, "Чад");
            addCountry(155, "Мали");
            addCountry(156, "Фиджи");
            addCountry(157, "Бутан");
            addCountry(158, "Барбадос");
            addCountry(159, "Тринидад и Тобаго");
            addCountry(160, "Мозамбик");
            addCountry(161, "Заир");
            addCountry(162, "Андорра");
            addCountry(163, "Туркменистан");
            addCountry(164, "Гайана");
            addCountry(165, "Корея");
            addCountry(166, "Нигер");
            addCountry(167, "Конго");
            addCountry(168, "Того");
            addCountry(169, "Ватикан");
            addCountry(170, "Черногория");
            addCountry(171, "Бурунди");
            addCountry(172, "Папуа — Новая Гвинея");
            addCountry(173, "Бахрейн");
            addCountry(174, "Гондурас");
            addCountry(175, "Судан");
            addCountry(176, "Эфиопия");
            addCountry(177, "Йемен");
            addCountry(178, "Северный Вьетнам");
            addCountry(179, "Суринам");
            addCountry(180, "Маврикий");
            addCountry(181, "Белиз");
            addCountry(182, "Либерия");
            addCountry(183, "Лесото");
            addCountry(184, "Уганда");
            addCountry(185, "Каймановы острова");
            addCountry(186, "Антигуа и Барбуда");
            addCountry(187, "Западная Сахара");
            addCountry(188, "Сан-Марино");
            addCountry(189, "Гуам");
            addCountry(190, "Косово");
            addCountry(191, "Лаос");
            addCountry(192, "Катар");
            addCountry(193, "Оман");
            addCountry(194, "Американские Виргинские острова");
            addCountry(195, "Сиам");
            addCountry(196, "Сьерра-Леоне");
            addCountry(197, "Эритрея");
            addCountry(198, "Сомали");
            addCountry(199, "Доминика");
            addCountry(200, "Бирма");
            addCountry(201, "Реюньон");
            addCountry(202, "Федеративные Штаты Микронезии");
            addCountry(203, "Самоа");
            addCountry(204, "Американское Самоа");
            addCountry(205, "Свазиленд");
            addCountry(206, "Французская Полинезия");
            addCountry(207, "Мьянма");
            addCountry(208, "Новая Каледония");
            addCountry(209, "Французская Гвиана");
            addCountry(210, "Сент-Винсент и Гренадины");
            addCountry(211, "Малави");
            addCountry(212, "Экваториальная Гвинея");
            addCountry(213, "Коморские острова");
            addCountry(214, "Кирибати");
            addCountry(215, "Тувалу");
            addCountry(216, "Тимор-Лешти");
            addCountry(217, "ЦАР");
            addCountry(218, "Тонга");
            addCountry(219, "Гренада");
            addCountry(220, "Гамбия");
            addCountry(221, "Антарктида");
            addCountry(222, "Острова Кука");
            addCountry(223, "Остров Мэн");
            addCountry(224, "Внешние малые острова США");
            addCountry(225, "Монтсеррат");
            addCountry(226, "Маршалловы острова");
            addCountry(227, "Бруней-Даруссалам");
            addCountry(228, "Сейшельские острова");
            addCountry(229, "Палау");
            addCountry(230, "Сент-Люсия");
            addCountry(231, "Вануату");
            addCountry(232, "Мальдивы");
            addCountry(233, "Босния");
            addCountry(234, "Уоллис и Футуна");
            addCountry(235, "Белоруссия");
            addCountry(236, "Киргизия");
            addCountry(239, "Джибути");
            addCountry(240, "Виргинские острова (США)");
            addCountry(241, "Северная Македония");
            addCountry(242, "Виргинские острова (Великобритания)");
            addCountry(3545269, "Сент-Люсия ");
            addCountry(3781461, "Сент-Китс и Невис");
            addCountry(3985922, "Соломоновы Острова");
            addCountry(4336645, "Виргинские острова");
            addCountry(7801402, "Фолклендские острова");
            addCountry(10842163, "Остров Святой Елены");
            addCountry(32518739, "острова Теркс и Кайкос");
            addCountry(47738117, "Мелкие отдаленные острова США");
            addCountry(65870322, "Сан-Томе и Принсипи");
            addCountry(100999433, "Эсватини");
        }

        // Сюда надо добавить константы для легкого выбора стран
        @IntDef({})
        public @interface Countries {}


        // Метод для добавления страны в мапы
        private static void addCountry(int id, String country) {
            ID_TO_COUNTRY.put(id, country);
            COUNTRY_TO_ID.put(country, id);
        }



        public static final int EMPTY = -1;

        public static String getCountryName(int cId) {
            return ID_TO_COUNTRY.getOrDefault(cId, "Неизвестно");
        }

        public static int getCountryId(String countryName) {
            return COUNTRY_TO_ID.getOrDefault(countryName.toLowerCase(), EMPTY);
        }

    }

}
