package com.alaka_ala.florafilm.ui.util.api.lumex.models.serial;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SeriesPlayerResponse {

    public Ads getAds() {
        return ads;
    }

    public Player getPlayer() {
        return player;
    }

    public String getMeta() {
        return meta;
    }

    @SerializedName("ads")
    private Ads ads;
    @SerializedName("player")
    private Player player;
    @SerializedName("meta")
    private String meta;

    public static class Ads {
        public List<Roll> getRolls() {
            return rolls;
        }

        public Banners getBanners() {
            return banners;
        }

        @SerializedName("rolls")
        private List<Roll> rolls;
        @SerializedName("banners")
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
            private String timeOffset; // поле необязательно
        }

        public static class Banners {
            public boolean isPausebanner() {
                return pausebanner;
            }

            public boolean isEndtag() {
                return endtag;
            }

            @SerializedName("pausebanner")
            private boolean pausebanner;
            @SerializedName("endtag")
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

        public List<Season> getMedia() {
            return media;
        }

        @SerializedName("content_type")
        private String contentType;

        @SerializedName("content_id")
        private int contentId;

        @SerializedName("kinopoisk_id")
        private int kinopoiskId;

        @SerializedName("poster")
        private String poster;

        @SerializedName("media")
        private List<Season> media;

        public static class Season {
            public int getSeasonId() {
                return seasonId;
            }

            public String getSeasonName() {
                return seasonName;
            }

            public List<Episode> getEpisodes() {
                return episodes;
            }

            @SerializedName("season_id")
            private int seasonId;

            @SerializedName("season_name")
            private String seasonName;

            @SerializedName("episodes")
            private List<Episode> episodes;

            public static class Episode {
                public int getEpisodeId() {
                    return episodeId;
                }

                public String getName() {
                    return name;
                }

                public String getPoster() {
                    return poster;
                }

                public List<Translation> getMedia() {
                    return media;
                }

                @SerializedName("episode_id")
                private int episodeId;

                @SerializedName("name")
                private String name;

                @SerializedName("poster")
                private String poster;

                @SerializedName("media")
                private List<Translation> media;

                public static class Translation {
                    public int getTranslationId() {
                        return translationId;
                    }

                    public String getTranslationName() {
                        return translationName;
                    }

                    public String getPlaylist() {
                        return playlist;
                    }

                    public List<Object> getTracks() {
                        return tracks;
                    }

                    public int getMaxQuality() {
                        return maxQuality;
                    }

                    @SerializedName("translation_id")
                    private int translationId;

                    @SerializedName("translation_name")
                    private String translationName;

                    @SerializedName("playlist")
                    private String playlist;

                    @SerializedName("tracks")
                    private List<Object> tracks; // Пустой список, неизвестна структура — можно оставить как Object или создать класс если будет нужна

                    @SerializedName("max_quality")
                    private int maxQuality;
                }
            }
        }
    }
}

