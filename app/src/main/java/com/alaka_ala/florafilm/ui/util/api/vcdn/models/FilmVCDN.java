package com.alaka_ala.florafilm.ui.util.api.vcdn.models;

import java.io.Serializable;
import java.util.List;

public class FilmVCDN implements Serializable {

    public List<Media> getMedia() {
        return media;
    }

    private final List<Media> media;

    public FilmVCDN(List<Media> media) {
        this.media = media;
    }


    public static class Media implements Serializable {
        private final int translation_id;
        private final String translation_name;
        private final String playlist;
        private final int max_quality;

        public int getMax_quality() {
            return max_quality;
        }

        public String getPlaylist() {
            return playlist;
        }

        public String getTranslation_name() {
            return translation_name;
        }

        public int getTranslation_id() {
            return translation_id;
        }

        public Media(int translation_id, String translation_name, String playlist, int max_quality) {
            this.translation_id = translation_id;
            this.translation_name = translation_name;
            this.playlist = playlist;
            this.max_quality = max_quality;
        }

    }
}
