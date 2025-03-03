package com.alaka_ala.florafilm.ui.util.api.vcdn;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.alaka_ala.florafilm.ui.util.api.vcdn.models.DataVCDN;
import com.alaka_ala.florafilm.ui.util.api.vcdn.models.FilmVCDN;
import com.alaka_ala.florafilm.ui.util.api.vcdn.models.SerialVCDN;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VideoCDN {
    private final String MOVIE = "movie";
    private final String TV_SERIES = "tv-series";
    private final String TV_SHOW = "show-tv-series";
    private final String ANIME = "anime";
    private final String ANIME_SERIES = "anime-tv-series";
    private final String API_KEY;
    public static final String USER_AGENT_VCDN = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Mobile Safari/537.36";

    public VideoCDN(String apiKey) {
        API_KEY = apiKey;
    }

    public void parse(int kinoppoiskID, ConnectionVideoCDN cvc) {
        Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                Bundle bundle = msg.getData();
                if (bundle.getBoolean("ok")) {
                    cvc.finishParse((DataVCDN) bundle.getSerializable("dataVCDN"));
                } else {
                    cvc.errorParse(bundle.getString("err"), bundle.getInt("code"));
                }
                return false;
            }
        });


        OkHttpClient client = new OkHttpClient();
        // https://portal.lumex.host/api/short?api_token=Fm4PitEIcN1zUvxT92jer99ybYFf9yHj&kinopoisk_id=1040432
        String urlStr = "https://portal.lumex.host/api/short" + "?api_token=" + API_KEY + "&kinopoisk_id=" + kinoppoiskID;
        Request.Builder bRequest = new Request.Builder();
        bRequest.url(urlStr);
        bRequest.addHeader("User-Agent", USER_AGENT_VCDN);
        Request request = bRequest.build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call calls, IOException e) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("ok", false);
                bundle.putInt("code", 0);
                bundle.putString("err", e.getMessage());
                Message msg = new Message();
                msg.setData(bundle);
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String res = response.body().string();
                    if (!res.isEmpty()) {
                        if (JsonParser.parseString(res).isJsonObject()) {
                            try {
                                JSONObject jsonObject = new JSONObject(res);

                                JSONObject data = jsonObject.getJSONArray("data").getJSONObject(0);
                                int contentId = data.getInt("id");
                                String content_type = data.getString("content_type");
                                String clientId = "XF1ZrOEqc39V";
                                String urlStr = "https://api.lumex.space/content?" +
                                        "clientId=" + clientId +
                                        "&contentType=" + content_type +
                                        "&contentId=" + contentId;
                                OkHttpClient ok = new OkHttpClient();
                                Request.Builder bRequest = new Request.Builder();
                                bRequest.url(urlStr);
                                bRequest.addHeader("User-Agent", USER_AGENT_VCDN);
                                bRequest.addHeader("Origin", "https://p.lumex.space");
                                bRequest.addHeader("Referer", "https://p.lumex.space/");
                                bRequest.addHeader("Access-Control-Allow-Credentials", "true");
                                bRequest.addHeader("Sec-Fetch-Mode", "cors");
                                bRequest.addHeader("Sec-Fetch-Site", "same-site");     // cross-site
                                bRequest.addHeader("Cache-Control", "no-cache");
                                bRequest.addHeader("Pragma", "no-cache");
                                bRequest.addHeader("Priority", "u=1, i");
                                bRequest.addHeader("Sec-Ch-Ua", "\"Not A(Brand\";v=\"8\", \"Chromium\";v=\"132\", \"YaBrowser\";v=\"25.2\", \"Yowser\";v=\"2.5\"");
                                bRequest.addHeader("Sec-Ch-Ua-Mobile", "?1");
                                bRequest.addHeader("Sec-Ch-Ua-Platform", "\"Android\"");
                                bRequest.addHeader("Sec-Fetch-Dest", "empty");
                                Request request = bRequest.build();
                                ok.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        Bundle bundle = new Bundle();
                                        bundle.putBoolean("ok", false);
                                        bundle.putInt("code", 0);
                                        bundle.putString("err", e.getMessage());
                                        Message msg = new Message();
                                        msg.setData(bundle);
                                        handler.sendMessage(msg);
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        if (response.isSuccessful()) {
                                            String res = response.body().string();
                                            if (!res.isEmpty()) {
                                                if (JsonParser.parseString(res).isJsonObject()) {
                                                    JSONObject jsonObject = null;
                                                    try {
                                                        jsonObject = new JSONObject(res);
                                                        String meta = jsonObject.getString("meta");
                                                        JSONObject player = jsonObject.has("player") ? jsonObject.getJSONObject("player") : new JSONObject();
                                                        String content_type = player.has("content_type") ? player.getString("content_type") : "";
                                                        JSONArray media = player.has("media") ? player.getJSONArray("media") : new JSONArray();

                                                        DataVCDN.Builder builderDataVcdn = new DataVCDN.Builder(meta);
                                                        if (content_type.equals(MOVIE)) {
                                                            // Фильмы
                                                            builderDataVcdn.setIsSerial(false);
                                                            FilmVCDN filmVCDN = parseMediaFilm(media);
                                                            builderDataVcdn.setFilmVCDN(filmVCDN);
                                                            DataVCDN dataVCDN = builderDataVcdn.build();
                                                            Bundle bundle = new Bundle();
                                                            bundle.putBoolean("ok", true);
                                                            bundle.putInt("code", response.code());
                                                            bundle.putSerializable("dataVCDN", dataVCDN);
                                                            Message msg = Message.obtain();
                                                            msg.setData(bundle);
                                                            handler.sendMessage(msg);
                                                        } else {
                                                            // Сериалы
                                                            builderDataVcdn.setIsSerial(true);
                                                            SerialVCDN serialVCDN = parseMediaSerial(media);
                                                            builderDataVcdn.setSerialVCDN(serialVCDN);
                                                            DataVCDN dataVCDN = builderDataVcdn.build();
                                                            Bundle bundle = new Bundle();
                                                            bundle.putBoolean("ok", true);
                                                            bundle.putInt("code", response.code());
                                                            bundle.putSerializable("dataVCDN", dataVCDN);
                                                            Message msg = Message.obtain();
                                                            msg.setData(bundle);
                                                            handler.sendMessage(msg);
                                                        }
                                                    } catch (JSONException e) {
                                                        onFailure(call, new IOException(e.getMessage()));
                                                    }
                                                } else {
                                                    onFailure(call, new IOException("API VCDN вернул данные отличительные от JsonObject"));
                                                }
                                            } else {
                                                onFailure(call, new IOException("API VCDN вернул пустой ответ"));
                                            }
                                        } else {
                                            onFailure(call, new IOException(response.message()));
                                        }
                                    }

                                    private FilmVCDN parseMediaFilm(JSONArray m) throws JSONException {
                                        List<FilmVCDN.Media> media = new ArrayList<>();
                                        for (int i = 0; i < m.length(); i++) {
                                            JSONObject mediaItem = m.getJSONObject(i);
                                            int translation_id = mediaItem.getInt("translation_id");
                                            String translation_name = mediaItem.getString("translation_name");
                                            String playlist = mediaItem.getString("playlist");
                                            int max_quality = mediaItem.getInt("max_quality");
                                            FilmVCDN.Media filmVcdnMedia = new FilmVCDN.Media(translation_id, translation_name, playlist, max_quality);
                                            media.add(filmVcdnMedia);
                                        }
                                        return new FilmVCDN(media);
                                    }

                                    private SerialVCDN parseMediaSerial(JSONArray media) throws JSONException {
                                        List<SerialVCDN.Season> seasons = new ArrayList<>();
                                        for (int i = 0; i < media.length(); i++) {

                                            JSONObject season = media.getJSONObject(i);
                                            String season_name = season.getString("season_name");
                                            List<SerialVCDN.Episode> episodes = new ArrayList<>();

                                            JSONArray episods = season.getJSONArray("episodes");
                                            for (int j = 0; j < episods.length(); j++) {
                                                JSONObject episode = episods.getJSONObject(j);
                                                String episodes_name = episode.getString("name");
                                                String poster = episode.getString("poster");
                                                List<SerialVCDN.Media> medias = new ArrayList<>();

                                                JSONArray mediaEpisode = episode.getJSONArray("media");
                                                for (int k = 0; k < mediaEpisode.length(); k++) {
                                                    JSONObject mediaItem = mediaEpisode.getJSONObject(k);
                                                    int translation_id = mediaItem.getInt("translation_id");
                                                    String translation_name = mediaItem.getString("translation_name");
                                                    String playlist = mediaItem.getString("playlist");
                                                    int max_quality = mediaItem.getInt("max_quality");
                                                    SerialVCDN.Media serialVcdnMedia = new SerialVCDN.Media(translation_id, translation_name, playlist, max_quality);
                                                    medias.add(serialVcdnMedia);
                                                }

                                                SerialVCDN.Episode serialVcdnEpisode = new SerialVCDN.Episode(episodes_name, poster, medias);
                                                episodes.add(serialVcdnEpisode);
                                            }
                                            SerialVCDN.Season serialVcdnSeason = new SerialVCDN.Season(season_name, episodes);
                                            seasons.add(serialVcdnSeason);
                                        }
                                        return new SerialVCDN(seasons);
                                    }


                                });


                            } catch (JSONException e) {
                                onFailure(call, new IOException(e.getMessage()));
                            }
                        } else {
                            onFailure(call, new IOException("API VCDN вернул не верные данные"));
                        }
                    } else {
                        onFailure(call, new IOException("API VCDN не вернул данные"));
                    }
                } else {
                    onFailure(call, new IOException(response.message()));
                }

            }
        });


    }



    private static final Queue<String> requestQueue = new LinkedList<>();
    private static boolean isProcessing = false;
    private static String meta = "";
    public static void parsePlaylist(String meta, String playlist, ParserCallback pc) {
        VideoCDN.meta = meta;
        requestQueue.add(playlist);
        // Если обработка не запущена, запускаем её
        if (!isProcessing) {
            processNextRequest(pc);
        }

    }

    private static void processNextRequest(ParserCallback pc) {
        if (requestQueue.isEmpty()) {
            isProcessing = false;
            pc.finishAll(); // Вызываем finish(), когда все запросы выполнены
            return;
        }
        isProcessing = true;
        String episodeToken = requestQueue.poll();
        String urlStr = "https://api.lumex.space" + episodeToken;

        Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                if (msg.getData().getBoolean("ok")) {
                    pc.finishQueue(msg.getData().getString("url"));
                } else {
                    pc.error(msg.getData().getString("err"));
                }
                // После завершения текущего запроса, обрабатываем следующий
                processNextRequest(pc);
                return false;
            }
        });

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient ok = new OkHttpClient();
                Request.Builder b = new Request.Builder();
                b.url(urlStr);
                b.addHeader("User-Agent", USER_AGENT_VCDN);
                b.addHeader("Origin", "https://p.lumex.space");
                b.addHeader("Referer", "https://p.lumex.space/");
                b.addHeader("Baggage", "sentry-environment=production,sentry-release=27d8e62fff5bae16a099ca314ac6c0a2ef025d73,sentry-public_key=7019f7dac1cb65452043137838b207e9,sentry-trace_id=40ef98bda5fc42ecb3b3e4998bd1692b,sentry-sample_rate=0.05,sentry-sampled=false");
                b.addHeader("X-Csrf-Token", meta);
                b.addHeader("Sec-Fetch-Site", "same-site");
                b.addHeader("Sec-Fetch-Mode", "cors");
                b.addHeader("Sec-Fetch-Dest", "empty");
                b.method("POST", RequestBody.create(null, new byte[0]));
                ok.newCall(b.build()).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("ok", false);
                        bundle.putInt("code", 0);
                        bundle.putString("err", e.getMessage());
                        Message msg = new Message();
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String strJson = response.body().string();
                            if (JsonParser.parseString(strJson).isJsonObject()) {
                                try {
                                    JSONObject jsonObject = new JSONObject(strJson);
                                    Bundle bundle = new Bundle();
                                    bundle.putBoolean("ok", true);
                                    bundle.putInt("code", response.code());
                                    bundle.putString("url", "https:" + jsonObject.getString("url"));
                                    Message msg = Message.obtain();
                                    msg.setData(bundle);
                                    handler.sendMessage(msg);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    onFailure(call, new IOException(e.getMessage()));
                                }

                            }
                        } else {
                            onFailure(call, new IOException(response.message()));
                        }
                    }
                });
            }
        });
        thread.start();


    }

    public interface ParserCallback {
        void finishQueue(String url);
        void finishAll();
        void error(String error);
    }

    public interface ConnectionVideoCDN {
        void startParse();

        void finishParse(DataVCDN file);

        void errorParse(String err, int code);
    }


}
