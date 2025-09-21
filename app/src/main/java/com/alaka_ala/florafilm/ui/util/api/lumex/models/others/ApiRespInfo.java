package com.alaka_ala.florafilm.ui.util.api.lumex.models.others;

import com.alaka_ala.florafilm.ui.util.api.lumex.models.serial.SeriesResponse;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ApiRespInfo {
    @SerializedName("result")
    private boolean result;
    @SerializedName("php")
    private double php;
    @SerializedName("data")
    private List<SeriesResponse.SeriesData> data;

    public boolean isResult() {
        return result;
    }

    public double getPhp() {
        return php;
    }

    public List<SeriesResponse.SeriesData> getData() {
        return data;
    }

    public static class SeriesData {

        @SerializedName("id")
        private int id;

        @SerializedName("content_type")
        private String contentType;

        @SerializedName("kp_id")
        private int kpId;

        @SerializedName("title")
        private String title;

        @SerializedName("orig_title")
        private String origTitle;

        @SerializedName("add")
        private String add;

        @SerializedName("year")
        private String year;

        @SerializedName("imdb_id")
        private String imdbId;

        @SerializedName("iframe_src")
        private String iframeSrc;

        @SerializedName("iframe")
        private String iframe;

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



        // Геттеры и сеттеры (по желанию)
    }
}
