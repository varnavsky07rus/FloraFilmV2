package com.alaka_ala.florafilm.ui.util.api.lumex.models.movie;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MovieResponse {

    public boolean isResult() {
        return result;
    }

    public double getPhp() {
        return php;
    }

    public List<MovieData> getData() {
        return data;
    }

    @SerializedName("result")
    private boolean result;
    @SerializedName("php")
    private double php;
    @SerializedName("data")
    private List<MovieData> data;

    public static class MovieData {
        public int getId() {
            return id;
        }

        public String getContentType() {
            return contentType;
        }

        public int getKpId() {
            return kpId;
        }

        public String getTitle() {
            return title;
        }

        public String getOrigTitle() {
            return origTitle;
        }

        public String getAdd() {
            return add;
        }

        public String getYear() {
            return year;
        }

        public String getImdbId() {
            return imdbId;
        }

        public String getIframeSrc() {
            return iframeSrc;
        }

        public String getIframe() {
            return iframe;
        }

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
    }

}

