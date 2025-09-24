package com.alaka_ala.florafilm.ui.util.api.collapse;

import androidx.annotation.NonNull;

import com.alaka_ala.florafilm.ui.util.api.collapse.models.ApiResponse;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CollapseAPI extends HlsProcessor {
    public static final String API_TOKEN = "eedefb541aeba871dcfc756e6b31c02e";

    public void getFromKinopoiskId(int kinopoisk_id, CallbackFromKinopoiskId cb) {
        // Для апи есть еще один домен : api.apicollaps.cc
        String urlApiReques = "https://api.bhcesh.me/list?token=" + API_TOKEN + "&kinopoisk_id=" + kinopoisk_id;
        getIframe(urlApiReques, new CallbackCollapseApi() {
            @Override
            public void onSuccess(ApiResponse apiResponse) {
                cb.onSuccess(apiResponse);
            }

            @Override
            public void onFailure(IOException e) {
                cb.onFailure(e);
            }
        });



    }


    private void getIframe(String urlApiReques, CallbackCollapseApi collapseApi) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(urlApiReques)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                collapseApi.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String jsonString = response.body().string();
                        collapseApi.onSuccess(new Gson().fromJson(jsonString, ApiResponse.class));
                    } else {
                        collapseApi.onFailure(new IOException("Пустой ответ от API Collapse"));
                    }
                } else {
                    collapseApi.onFailure(new IOException("Ошибка при выполнении запроса к API Collapse"));
                }
            }
        });
    }

    public interface CallbackCollapseApi {
        void onSuccess(ApiResponse apiResponse);
        void onFailure(IOException e);
    }

    public interface CallbackFromKinopoiskId {
        void onSuccess(ApiResponse apiResponse);
        void onFailure(IOException e);
    }



}
