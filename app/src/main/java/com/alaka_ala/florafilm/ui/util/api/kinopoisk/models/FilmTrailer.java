package com.alaka_ala.florafilm.ui.util.api.kinopoisk.models;

public class FilmTrailer {

    private final String url;
    private final String name;
    private final String site;


    public String getSite() {
        return site;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    private FilmTrailer(Builder builder) {
        url = builder.url;
        name = builder.name;
        site = builder.site;
    }

    public static class Builder {
        private String url;
        private String name;
        private String site;
        public Builder() {
        }
        public Builder setUrl(String val) {
            url = val;
            return this;
        }

        public Builder setName(String val) {
            name = val;
            return this;
        }
        public Builder setSite(String val) {
            site = val;
            return this;
        }
        public FilmTrailer build() {
            return new FilmTrailer(this);
        }

    }


}
