package com.alaka_ala.florafilm.ui.util.api.lumex.models.movie;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MoviePlayerResponse {
    public Ads getAds() {
        return ads;
    }

    public Player getPlayer() {
        return player;
    }

    public String getMeta() {
        return meta;
    }

    private Ads ads;
    private Player player;
    private String meta;

    public static class Ads {
        private List<Roll> rolls;
        private Banners banners;

        public static class Roll {
            public String getTagUrl() {
                return tagUrl;
            }

            public String getTimeOffset() {
                return timeOffset;
            }

            @SerializedName("tag_url")
            private String tagUrl;

            @SerializedName("time_offset")
            private String timeOffset; // необязательное поле
        }

        public static class Banners {
            public boolean isPausebanner() {
                return pausebanner;
            }

            public boolean isEndtag() {
                return endtag;
            }

            private boolean pausebanner;
            private boolean endtag;
        }
    }

    public static class Player {
        public String getContentType() {
            return contentType;
        }

        public int getContentId() {
            return contentId;
        }

        public int getKinopoiskId() {
            return kinopoiskId;
        }

        public String getPoster() {
            return poster;
        }

        public List<Media> getMedia() {
            return media;
        }

        @SerializedName("content_type")
        private String contentType;

        @SerializedName("content_id")
        private int contentId;

        @SerializedName("kinopoisk_id")
        private int kinopoiskId;

        private String poster;
        private List<Media> media;

        public static class Media {
            public int getTranslationId() {
                return translationId;
            }

            public String getTranslationName() {
                return translationName;
            }

            public int getMaxQuality() {
                return maxQuality;
            }

            public String getPlaylist() {
                return playlist;
            }

            public List<Track> getTracks() {
                return tracks;
            }

            @SerializedName("translation_id")
            private int translationId;

            @SerializedName("translation_name")
            private String translationName;

            @SerializedName("max_quality")
            private int maxQuality;

            private String playlist;

            private List<Track> tracks;

            public static class Track {
                public String getKind() {
                    return kind;
                }

                public String getSrc() {
                    return src;
                }

                public String getSrLang() {
                    return srLang;
                }

                public String getLabel() {
                    return label;
                }

                private String kind;
                private String src;

                @SerializedName("srlang")
                private String srLang;

                private String label;
            }
        }
    }
}

