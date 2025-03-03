package com.alaka_ala.florafilm.ui.util.api.kinopoisk.models;

import java.io.Serializable;

public class NewsMedia implements Serializable {

    public NewsMedia(String titleCategory, int indexItemNews, int kinopoiskId, String imageUrl, String title, String description, String urlPost, String publishedAt) {
        this.titleCategory = titleCategory;
        this.indexItemNews = indexItemNews;
        this.kinopoiskId = kinopoiskId;
        this.imageUrl = imageUrl;
        this.title = title;
        this.description = description;
        this.urlPost = urlPost;
        this.publishedAt = publishedAt;
    }

    private final String titleCategory;
    private final int indexItemNews;
    private final int kinopoiskId;
    private final String imageUrl;
    private final String title;
    private final String description;
    private final String urlPost;
    private final String publishedAt;

    public String getTitleCategory() {
        return titleCategory;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public String getUrlPost() {
        return urlPost;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getKinopoiskId() {
        return kinopoiskId;
    }

    public int getIndexItemNews() {
        return indexItemNews;
    }


}
