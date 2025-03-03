package com.alaka_ala.florafilm.ui.util.api.jacred;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class JacredTor {

    private String query;
    private SearchCallback sc;

    public void query(String query, SearchCallback sc) {
        this.sc = sc;
        if (query.isEmpty()) {
            sc.onError("Поисковый запрос пустой", sc);
            return;
        }
        Handler handlerProgress = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                Bundle bundle = msg.getData();
                if (bundle.getBoolean("loading")) {
                    sc.onLoading(bundle.getInt("position"), bundle.getInt("count"), bundle.getBoolean("finish") ? 100 : 0);
                } else {
                    Object objData = bundle.getSerializable("data");
                    if (objData instanceof ArrayList) {
                        ArrayList<JacredData> jacredDataArrayList = (ArrayList<JacredData>) objData;
                        sc.onSuccess(jacredDataArrayList);
                    } else {
                        sc.onError("Ошибка получения данных", sc);
                    }
                    sc.finish();
                }
                return true;
            }
        });
        this.query = query;
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        builder.addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Mobile Safari/537.36");
        Request request = builder.url("https://jacred.xyz/api/v1.0/torrents?search=" + query + "&apikey=null").build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("data", null);
                bundle.putString("error", e.getMessage());
                Message message = new Message();
                message.setData(bundle);
                handlerProgress.sendMessage(message);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                ArrayList<JacredData> jacredDataArrayList = new ArrayList<>();
                String res = response.body().string();
                if (response.isSuccessful() && JsonParser.parseString(res).isJsonArray()) {
                    try {
                        JSONArray jsonArray = new JSONArray(res);
                        int count = jsonArray.length();
                        for (int i = 0; i < count; i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String tracker = jsonObject.has("tracker") ? jsonObject.getString("tracker") : "Неизвестный трекер";
                            String url = jsonObject.has("url") ? jsonObject.getString("url") : "Неизвестный URL";
                            String title = jsonObject.has("title") ? jsonObject.getString("title") : "Неизвестный заголовок";
                            long size = jsonObject.has("size") ? jsonObject.getLong("size") : 0;
                            String sizeName = jsonObject.has("sizeName") ? jsonObject.getString("sizeName") : "Неизвестный размер";
                            String createTime = jsonObject.has("createTime") ? jsonObject.getString("createTime") : "Неизвестная дата создания";
                            int sid = jsonObject.has("sid") ? jsonObject.getInt("sid") : 0;
                            int pir = jsonObject.has("pir") ? jsonObject.getInt("pir") : 0;
                            String magnet = jsonObject.has("magnet") ? jsonObject.getString("magnet") : "Неизвестный magnet";
                            String name = jsonObject.has("name") ? jsonObject.getString("name") : "Неизвестное имя";
                            String originalname = jsonObject.has("originalname") ? jsonObject.getString("originalname") : "Неизвестное оригинальное имя";
                            int relased = jsonObject.has("relased") ? jsonObject.getInt("relased") : 0;
                            String videotype = jsonObject.has("videotype") ? jsonObject.getString("videotype") : "Неизвестный тип видео";
                            int quality = jsonObject.has("quality") ? jsonObject.getInt("quality") : 0;
                            ArrayList<String> voices = new ArrayList<>();
                            if (jsonObject.has("voices")) {
                                JSONArray voicesArray = jsonObject.getJSONArray("voices");
                                for (int ii = 0; ii < voicesArray.length(); ii++) {
                                    voices.add(voicesArray.getString(ii));
                                }
                            }
                            ArrayList<Integer> seasons = new ArrayList<>();
                            if (jsonObject.has("seasons")) {
                                JSONArray seasonsArray = jsonObject.getJSONArray("seasons");
                                for (int ii = 0; ii < seasonsArray.length(); ii++) {
                                    seasons.add(seasonsArray.getInt(ii));
                                }
                            }
                            ArrayList<String> types = new ArrayList<>();
                            if (jsonObject.has("types")) {
                                JSONArray typesArray = jsonObject.getJSONArray("types");
                                for (int ii = 0; ii < typesArray.length(); ii++) {
                                    types.add(typesArray.getString(ii));
                                }
                            }
                            JacredData jacredData = new JacredData(
                                    tracker,
                                    url,
                                    title,
                                    size,
                                    sizeName,
                                    createTime,
                                    sid, pir,
                                    magnet, name,
                                    originalname,
                                    relased,
                                    videotype,
                                    quality,
                                    voices,
                                    seasons,
                                    types);
                            jacredDataArrayList.add(jacredData);

                            Bundle bundle = new Bundle();
                            bundle.putInt("count", count);
                            bundle.putInt("position", i);
                            bundle.putBoolean("finish", i == count - 1);
                            bundle.putBoolean("loading", true);
                            if (i == count - 1) {
                                bundle.putBoolean("loading", false);
                                bundle.putSerializable("data", jacredDataArrayList);
                            }
                            Message message = new Message();
                            message.setData(bundle);
                            handlerProgress.sendMessage(message);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        onFailure(call, new IOException(e));
                    }
                } else {
                    onFailure(call, new IOException("Ошибка ответа сервера поступившие данные некорректны"));
                }
            }
        });


    }

    public String getQuery() {
        return query;
    }

    public SearchCallback getSearchCallback() {
        return sc;
    }

    public interface SearchCallback {
        void onSuccess(List<JacredData> data);

        void onLoading(int position, int count, int progres);

        void finish();

        void onError(String msgError, SearchCallback sc);
    }

    public static class JacredData {
        public JacredData(String tracker, String url, String title, long size, String sizeName, String createTime, int sid, int pir, String magnet, String name, String originalname, int relased, String videotype, int quality, ArrayList<String> voices, ArrayList<Integer> seasons, ArrayList<String> types) {
            this.tracker = tracker;
            this.url = url;
            this.title = title;
            this.size = size;
            this.sizeName = sizeName;
            this.createTime = createTime;
            this.sid = sid;
            this.pir = pir;
            this.magnet = magnet;
            this.name = name;
            this.originalname = originalname;
            this.relased = relased;
            this.videotype = videotype;
            this.quality = quality;
            this.voices = voices;
            this.seasons = seasons;
            this.types = types;
        }

        private final String tracker;
        private final String url;
        private final String title;
        private final long size;
        private final String sizeName;
        private final String createTime;
        private final int sid;
        private final int pir;
        private final String magnet;
        private final String name;
        private final String originalname;
        private final int relased;
        private final String videotype;
        private final int quality;
        private final ArrayList<String> voices;
        private final ArrayList<Integer> seasons;

        public ArrayList<String> getTypes() {
            return types;
        }

        public ArrayList<Integer> getSeasons() {
            return seasons;
        }

        public ArrayList<String> getVoices() {
            return voices;
        }

        public int getQuality() {
            return quality;
        }

        public String getVideotype() {
            return videotype;
        }

        public int getRelased() {
            return relased;
        }

        public String getOriginalname() {
            return originalname;
        }

        public String getName() {
            return name;
        }

        public String getMagnet() {
            return magnet;
        }

        public int getPir() {
            return pir;
        }

        public int getSid() {
            return sid;
        }

        public String getCreateTime() {
            return createTime;
        }

        public String getSizeName() {
            return sizeName;
        }

        public long getSize() {
            return size;
        }

        public String getTitle() {
            return title;
        }

        public String getUrl() {
            return url;
        }

        public String getTracker() {
            return tracker;
        }

        private final ArrayList<String> types;
    }


}
