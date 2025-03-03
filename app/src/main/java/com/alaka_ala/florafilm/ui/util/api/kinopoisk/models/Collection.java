package com.alaka_ala.florafilm.ui.util.api.kinopoisk.models;

import androidx.annotation.StringDef;

import java.io.Serializable;
import java.util.ArrayList;

public class Collection implements Serializable {

    public static final String TITLE_POPULAR_ALL = "Новинки";
    public static final String TITLE_POPULAR_MOVIES = "Популярные Фильмы";
    public static final String TITLE_TOP_250_TV_SHOWS = "Топ 250 Сериалов";
    public static final String TITLE_TOP_250_MOVIES = "Топ 250 Фильмов";
    public static final String TITLE_VAMPIRE_THEME = "Про вампиров";
    public static final String TITLE_COMICS_THEME = "По комиксам";
    public static final String TITLE_FAMILY = "Семейные фильмы";

    public static final String TITLE_SEARCH = "Поиск";
    public static final String TITLE_SEARCH_FAVORITE = "Поиск по избранным";
    public static final String TITLE_SEARCH_VIEWED = "Поиск по просмотренным";
    public static final String TITLE_SIMILAR = "Похожие фильмы";
    public static final String TITLE_NEWS_MEDIA = "Новости медиа";
    public static final String TITLE_FAVORITE = "Избранное";
    public static final String TITLE_VIEWED = "Просмотренные";
    public static final String TITLE_CATEGORY_GENRE_19 = "Семейные";
    public static final String TITLE_CATEGORY_GENRE_18 = "Мультфильмы";
    public static final String TITLE_CATEGORY_GENRE_17 = "Ужасы";
    public static final String TITLE_CATEGORY_GENRE_14 = "Военные";
    public static final String TITLE_CATEGORY_GENRE_13 = "Комедии";
    public static final String TITLE_CATEGORY_GENRE_12 = "Фэнтези";
    public static final String TITLE_CATEGORY_GENRE_11 = "Боевик";
    public static final String TITLE_CATEGORY_GENRE_10 = "Вестерн";
    public static final String TITLE_CATEGORY_GENRE_7 = "Приключения";
    public static final String TITLE_CATEGORY_GENRE_24 = "Аниме";



    @StringDef({TITLE_POPULAR_ALL,
            TITLE_POPULAR_MOVIES, TITLE_TOP_250_TV_SHOWS,
            TITLE_TOP_250_MOVIES, TITLE_VAMPIRE_THEME,
            TITLE_COMICS_THEME,
            TITLE_FAMILY, TITLE_SEARCH,
            TITLE_SIMILAR, TITLE_NEWS_MEDIA,
            TITLE_FAVORITE, TITLE_SEARCH_FAVORITE,
            TITLE_SEARCH_VIEWED, TITLE_VIEWED,
            TITLE_CATEGORY_GENRE_19, TITLE_CATEGORY_GENRE_18,
            TITLE_CATEGORY_GENRE_17, TITLE_CATEGORY_GENRE_14,
            TITLE_CATEGORY_GENRE_13, TITLE_CATEGORY_GENRE_12,
            TITLE_CATEGORY_GENRE_11, TITLE_CATEGORY_GENRE_10,
            TITLE_CATEGORY_GENRE_7, TITLE_CATEGORY_GENRE_24})
    public @interface TitleCategoryDefs {
    }

    public Collection(String titleCollection, String total, String totalPages, ArrayList<ListFilmItem> items) {
        this.titleCollection = titleCollection;
        this.total = total;
        this.totalPages = totalPages;
        this.items = items;
    }

    public String getTitleCollection() {
        return titleCollection;
    }

    public String getTotal() {
        return total;
    }

    public String getTotalPages() {
        return totalPages;
    }

    public ArrayList<ListFilmItem> getItems() {
        return items;
    }

    private final String titleCollection;
    private final String total;
    private final String totalPages;
    private final ArrayList<ListFilmItem> items;
}
