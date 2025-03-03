package com.alaka_ala.florafilm.ui.util.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EPData {

    public static class Film implements Serializable {
        private String id = "null";
        private String poster = "null";
        private final ArrayList<Translations> translations;
        private final ArrayList<Block> blockList;

        public ArrayList<Translations> getTranslations() {
            return translations;
        }

        public String getPoster() {
            return poster;
        }

        public String getId() {
            return id;
        }


        public Film(Builder builder) {
            this.id = builder.id;
            this.poster = builder.poster;
            this.translations = builder.translations;
            this.blockList = builder.blockList;
        }

        public ArrayList<Block> getBlockList() {
            return blockList;
        }


        public static class Translations implements Serializable {
            private final String title;
            private final List<Map.Entry<String, String>> videoData;

            public String getTitle(){
                return title;
            }
            public List<Map.Entry<String, String>> getVideoData() {
                return videoData;
            }

            public Translations(Translations.Builder builder) {
                this.title = builder.title;
                this.videoData = builder.videoData;
            }

            public static class Builder {
                private String title;
                private List<Map.Entry<String, String>> videoData;
                public void  setTitle(String title) {
                    this.title = title;
                }
                public void setVideoData(List<Map.Entry<String, String>> videoData) {
                    this.videoData = videoData;
                }
                public Translations build(){
                    return new Translations(this);
                }
            }


        }

        public static class Builder {
            private String id;
            private String poster;
            private ArrayList<Translations> translations;
            private final ArrayList<Block> blockList = new ArrayList<>();

            public Builder setId(String id) {
                this.id = id;
                return this;
            }

            public Builder setPoster(String poster) {
                this.poster = poster;
                return this;
            }

            public void setTranslations(ArrayList<Translations> translations) {
                this.translations = translations;
            }

            public void addBlock(Block block) {
                this.blockList.add(block);
            }

            public Film build() {
                return new Film(this);
            }
        }


    }

    public static class Serial implements Serializable {

        private final ArrayList<Season> seasons;

        private final ArrayList<Block> blockList;

        public ArrayList<Season> getSeasons() {
            return seasons;
        }

        public Serial(Builder builder) {
            this.seasons = builder.seasons;
            this.blockList = builder.blockList;
        }

        public ArrayList<Block> getBlockList() {
            return blockList;
        }

        public static class Builder {
            private ArrayList<Season> seasons;
            private final ArrayList<Block> blockList = new ArrayList<>();
            public Builder addBlock(Block block) {
                this.blockList.add(block);
                return this;
            }
            public void setSeasons(ArrayList<Season> seasons) {
                this.seasons = seasons;
            }
            public Serial build() {
                return new Serial(this);
            }
        }

        public static class Season implements Serializable {
            private final String title;
            private final ArrayList<Episode> episodes;
            public Season(Builder builder) {
                this.title = builder.title;
                this.episodes = builder.episodes;
            }
            public ArrayList<Episode> getEpisodes() {
                return episodes;
            }
            public String getTitle() {
                return title;
            }
            public static class Builder {
                private String title;
                private ArrayList<Episode> episodes;
                public Season.Builder setTitle(String title) {
                    this.title = title;
                    return this;
                }
                public void setEpisodes(ArrayList<Episode> episodes) {
                    this.episodes = episodes;
                }
                public Season build() {
                    return new Season(this);
                }
            }
        }

        public static class Episode implements Serializable {
            private final String title;
            private final ArrayList<Translations> translations;
            public Episode(Builder builder) {
                this.title = builder.title;
                this.translations = builder.translations;
            }
            public String getTitle() {
                return title;
            }
            public ArrayList<Translations> getTranslations() {
                return translations;
            }
            public static class Builder {
                private String title;
                private ArrayList<Translations> translations;
                public Episode.Builder setTitle(String title) {
                    this.title = title;
                    return this;
                }
                public void setTranslations(ArrayList<Translations> translations) {
                    this.translations = translations;
                }
                public Episode build() {
                    return new Episode(this);
                }
            }

        }

        public static class Translations implements Serializable {
            private final String title;
            private final List<Map.Entry<String, String>> videoData;

            public String getTitle(){
                return title;
            }

            public List<Map.Entry<String, String>> getVideoData() {
                return videoData;
            }

            public Translations(Builder builder) {
                this.title = builder.title;
                this.videoData = builder.videoData;
            }

            public static class Builder {
                private String title;
                private List<Map.Entry<String, String>> videoData;
                public void  setTitle(String title) {
                    this.title = title;
                }
                public void setVideoData(List<Map.Entry<String, String>> videoData) {
                    this.videoData = videoData;
                }
                public Translations build(){
                    return new Translations(this);
                }
            }


        }
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

}
