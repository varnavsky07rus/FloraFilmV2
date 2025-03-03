package com.alaka_ala.florafilm.ui.util.api.hdvb.models;

import com.alaka_ala.florafilm.ui.util.api.EPData;

import java.io.Serializable;
import java.util.ArrayList;

public class HDVBFilm implements Serializable {

    public String getTitle_ru() {
        return title_ru;
    }

    public String getTitle_en() {
        return title_en;
    }

    public int getYear() {
        return year;
    }

    public int getKinopoisk_id() {
        return kinopoisk_id;
    }

    public String getTranslator() {
        return translator;
    }

    public String getType() {
        return type;
    }

    public String getIframe_url() {
        return iframe_url;
    }

    public ArrayList<Block> getBlockList() {
        return blockList;
    }

    public String getQuality() {
        return quality;
    }

    public String getTrailer() {
        return trailer;
    }

    public String getAdded_date() {
        return added_date;
    }

    public String getUser_country() {
        return user_country;
    }

    public EPData.Film getHdvbDataFilm() {
        return hdvbDataFilm;
    }

    private final String title_ru;
    private final String title_en;
    private final int year;
    private final int kinopoisk_id;
    private final String translator;
    private final String type;
    private final String iframe_url;
    private final ArrayList<Block> blockList;
    private final String quality;
    private final String trailer;
    private final String added_date;
    private final String user_country;
    private final EPData.Film hdvbDataFilm;

    public HDVBFilm(Builder builder) {
        this.title_ru = builder.title_ru;
        this.title_en = builder.title_en;
        this.year = builder.year;
        this.kinopoisk_id = builder.kinopoisk_id;
        this.translator = builder.translator;
        this.type = builder.type;
        this.iframe_url = builder.iframe_url;
        this.blockList = builder.blockList;
        this.quality = builder.quality;
        this.trailer = builder.trailer;
        this.added_date = builder.added_date;
        this.user_country = builder.user_country;
        this.hdvbDataFilm = builder.hdvbDataFilm;
    }

    public static class Block  implements Serializable {
        public Block(String country) {
            this.country = country;
        }

        public String getCountry() {
            return country;
        }

        private final String country;
    }


    public static class Builder  implements Serializable {
        private String title_ru;
        private String title_en;
        private int year;
        private int kinopoisk_id;
        private String translator;
        private String type;
        private String iframe_url;
        private final ArrayList<Block> blockList = new ArrayList<>();
        private String quality;
        private String trailer;
        private String added_date;
        private String user_country;
        private EPData.Film hdvbDataFilm;

        public void setTitleRu(String title_ru) {
            this.title_ru = title_ru;
        }

        public void setTitleEn(String title_en) {
            this.title_en = title_en;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public void setKinopoiskId(int kinopoisk_id) {
            this.kinopoisk_id = kinopoisk_id;
        }

        public void setTranslator(String translator) {
            this.translator = translator;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setIframeUrl(String iframe_url) {
            this.iframe_url = iframe_url;
        }


        public void addBlock(Block block) {
            this.blockList.add(block);
        }

        public void setQuality(String quality) {
            this.quality = quality;
        }

        public void setTrailer(String trailer) {
            this.trailer = trailer;
        }

        public void setAddedDate(String added_date) {
            this.added_date = added_date;
        }

        public void setUserCountry(String user_country) {
            this.user_country = user_country;
        }

        public void setHdvbDataFilm(EPData.Film hdvbDataFilm) {
            this.hdvbDataFilm = hdvbDataFilm;
        }

        public HDVBFilm build() {
            return new HDVBFilm(this);
        }
    }




}
