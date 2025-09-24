package com.alaka_ala.florafilm.ui.util.api.collapse.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PlayerData {

    @SerializedName("poster")
    public final String poster;

    @SerializedName("ui")
    public final Ui ui;

    @SerializedName("playlist")
    public final Playlist playlist;

    public PlayerData(String poster, Ui ui, Playlist playlist) {
        this.poster = poster;
        this.ui = ui;
        this.playlist = playlist;
    }

    public static class Ui {
        @SerializedName("prevNext")
        public final boolean prevNext;

        public Ui(boolean prevNext) {
            this.prevNext = prevNext;
        }
    }

    public static class Playlist {
        @SerializedName("open")
        public final boolean open;

        @SerializedName("ignoreLast")
        public final boolean ignoreLast;

        @SerializedName("autoNext")
        public final boolean autoNext;

        @SerializedName("id")
        public final int id;

        @SerializedName("current")
        public final Current current;

        @SerializedName("seasons")
        public final List<Season> seasons;

        public Playlist(boolean open, boolean ignoreLast, boolean autoNext, int id, Current current, List<Season> seasons) {
            this.open = open;
            this.ignoreLast = ignoreLast;
            this.autoNext = autoNext;
            this.id = id;
            this.current = current;
            this.seasons = seasons;
        }
    }

    public static class Current {
        @SerializedName("season")
        public final int season;

        @SerializedName("episode")
        public final String episode;

        public Current(int season, String episode) {
            this.season = season;
            this.episode = episode;
        }
    }

    public static class Season {
        public int getSeason() {
            return season;
        }

        @SerializedName("season")
        public final int season;

        @SerializedName("blocked")
        public final boolean blocked;

        @SerializedName("episodes")
        public final List<Episode> episodes;

        public Season(int season, boolean blocked, List<Episode> episodes) {
            this.season = season;
            this.blocked = blocked;
            this.episodes = episodes;
        }
    }

    public static class Episode {
        public int getEpisode() {
            return episode;
        }

        @SerializedName("episode")
        public final int episode;

        @SerializedName("id")
        public final int id;

        @SerializedName("videoKey")
        public final int videoKey;

        @SerializedName("dash")
        public final String dash;

        @SerializedName("hls")
        public final String hls;

        @SerializedName("audio")
        public final Audio audio;

        @SerializedName("cc")
        public final List<Cc> cc;

        @SerializedName("duration")
        public final int duration;

        @SerializedName("title")
        public final String title;

        @SerializedName("download")
        public final String download;

        @SerializedName("sections")
        public final List<Section> sections;

        @SerializedName("poster")
        public final String poster;

        @SerializedName("preview")
        public final Preview preview;

        public Episode(int episode, int id, int videoKey, String dash, String hls, Audio audio, List<Cc> cc,
                       int duration, String title, String download, List<Section> sections, String poster, Preview preview) {
            this.episode = episode;
            this.id = id;
            this.videoKey = videoKey;
            this.dash = dash;
            this.hls = hls;
            this.audio = audio;
            this.cc = cc;
            this.duration = duration;
            this.title = title;
            this.download = download;
            this.sections = sections;
            this.poster = poster;
            this.preview = preview;
        }
    }

    public static class Audio {
        @SerializedName("names")
        public final List<String> names;

        @SerializedName("order")
        public final List<Integer> order;

        public Audio(List<String> names, List<Integer> order) {
            this.names = names;
            this.order = order;
        }
    }

    public static class Cc {
        @SerializedName("url")
        public final String url;

        @SerializedName("name")
        public final String name;

        public Cc(String url, String name) {
            this.url = url;
            this.name = name;
        }
    }

    public static class Section {
        @SerializedName("skip")
        public final boolean skip;

        @SerializedName("type")
        public final String type;

        @SerializedName("start")
        public final double start;

        @SerializedName("title")
        public final String title;

        @SerializedName("end")
        public final double end;

        public Section(boolean skip, String type, double start, String title, double end) {
            this.skip = skip;
            this.type = type;
            this.start = start;
            this.title = title;
            this.end = end;
        }
    }

    public static class Preview {
        @SerializedName("src")
        public final String src;

        public Preview(String src) {
            this.src = src;
        }
    }
}
