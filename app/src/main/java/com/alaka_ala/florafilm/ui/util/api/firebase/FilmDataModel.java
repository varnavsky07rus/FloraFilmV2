package com.alaka_ala.florafilm.ui.util.api.firebase;

import java.util.HashMap;
import java.util.Map;

// Film.java
public class FilmDataModel {

    // Эти поля ДОЛЖНЫ называться так же, как ключи в Firebase
    private String title;
    private long likes_count;
    private long dislikes_count;
    private Map<String, String> user_ratings; // Карта, где ключ - userId, значение - "like" или "dislike"

    // ОБЯЗАТЕЛЬНО: пустой конструктор для Firebase
    public FilmDataModel() {
    }

    // Конструктор для удобного создания объекта вручную (если нужно)
    public FilmDataModel(String title, long likes_count, long dislikes_count) {
        this.title = title;
        this.likes_count = likes_count;
        this.dislikes_count = dislikes_count;
        this.user_ratings = new HashMap<>();
    }

    // ОБЯЗАТЕЛЬНО: геттеры и сеттеры для всех полей
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getLikes_count() {
        return likes_count;
    }

    public void setLikes_count(long likes_count) {
        this.likes_count = likes_count;
    }

    public long getDislikes_count() {
        return dislikes_count;
    }

    public void setDislikes_count(long dislikes_count) {
        this.dislikes_count = dislikes_count;
    }

    public Map<String, String> getUser_ratings() {
        return user_ratings;
    }

    public void setUser_ratings(Map<String, String> user_ratings) {
        this.user_ratings = user_ratings;
    }
}
