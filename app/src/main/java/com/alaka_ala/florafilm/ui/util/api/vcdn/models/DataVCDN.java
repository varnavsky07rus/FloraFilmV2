package com.alaka_ala.florafilm.ui.util.api.vcdn.models;

import java.io.Serializable;

public class DataVCDN implements Serializable {

    public SerialVCDN getSerialVCDN() {
        return serialVCDN;
    }

    public FilmVCDN getFilmVCDN() {
        return filmVCDN;
    }

    public boolean isSerial() {
        return isSerial;
    }

    public int getKinoppoiskID() {
        return kinoppoiskID;
    }

    public String getMeta() {
        return meta;
    }

    public String getPoster() {
        return poster;
    }

    private final int kinoppoiskID;
    private final String poster;
    private final SerialVCDN serialVCDN;
    private final FilmVCDN filmVCDN;
    private final boolean isSerial;
    private final String meta;




    public DataVCDN(Builder builder) {
        poster = "http://www.kinopoisk.ru/images/film_big/" + builder.kinoppoiskID + ".jpg";
        kinoppoiskID = builder.kinoppoiskID;
        serialVCDN = builder.serialVCDN;
        filmVCDN = builder.filmVCDN;
        isSerial = builder.isSerial;
        meta = builder.meta;
    }


    public static class Builder implements Serializable {
        public Builder(String meta) {
            this.meta = meta;
        }

        private int kinoppoiskID;
        private final String meta;
        private SerialVCDN serialVCDN;
        private FilmVCDN filmVCDN;
        private boolean isSerial;

        public void setIsSerial(boolean serial) {
            isSerial = serial;
        }

        public void setKinoppoiskID(int kinoppoiskID) {
            this.kinoppoiskID = kinoppoiskID;
        }

        public void setFilmVCDN(FilmVCDN filmVCDN) {
            this.filmVCDN = filmVCDN;
        }

        public void setSerialVCDN(SerialVCDN serialVCDN) {
            this.serialVCDN = serialVCDN;
        }

        public DataVCDN build() {
            return new DataVCDN(this);
        }


    }
}
