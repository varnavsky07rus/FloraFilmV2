package com.alaka_ala.florafilm.ui.util.api.lumex.models.serial;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

/** Используется для парсинга ответа от сервера Lumex на получение простой информации о сериале*/
public class SeriesResponse {
    private boolean result;
    private double php;
    private List<SeriesData> data;

    public boolean isResult() {
        return result;
    }

    public double getPhp() {
        return php;
    }

    public List<SeriesData> getData() {
        return data;
    }

    public static class SeriesData {
        private int id;

        @SerializedName("content_type")
        private String contentType;

        @SerializedName("kp_id")
        private int kpId;

        private String title;

        @SerializedName("orig_title")
        private String origTitle;

        private String add;
        private String year;

        @SerializedName("imdb_id")
        private String imdbId;

        @SerializedName("iframe_src")
        private String iframeSrc;

        public String getIframe() {
            return iframe;
        }

        public String getIframeSrc() {
            return iframeSrc;
        }

        public String getImdbId() {
            return imdbId;
        }

        public String getYear() {
            return year;
        }

        public String getAdd() {
            return add;
        }

        public String getOrigTitle() {
            return origTitle;
        }

        public String getTitle() {
            return title;
        }

        public int getKpId() {
            return kpId;
        }

        public String getContentType() {
            return contentType;
        }

        public int getId() {
            return id;
        }

        @SerializedName("iframe")
        private String iframe;

        // Геттеры и сеттеры (по желанию)
    }
}

