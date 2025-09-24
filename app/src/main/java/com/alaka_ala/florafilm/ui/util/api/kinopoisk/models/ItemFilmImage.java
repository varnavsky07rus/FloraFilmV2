package com.alaka_ala.florafilm.ui.util.api.kinopoisk.models;


import com.google.gson.annotations.SerializedName;
import java.util.List;


public class ItemFilmImage {
    public void setTotal(int total) {
        this.total = total;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    @SerializedName("total")
    private int total;

    @SerializedName("totalPages")
    private int totalPages;

    @SerializedName("items")
    private List<Item> items;

    public int getTotal() {
        return total;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public List<Item> getItems() {
        return items;
    }

    public static class Item {
        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public void setPreviewUrl(String previewUrl) {
            this.previewUrl = previewUrl;
        }

        @SerializedName("imageUrl")
        private String imageUrl;

        @SerializedName("previewUrl")
        private String previewUrl;

        public String getImageUrl() {
            return imageUrl;
        }

        public String getPreviewUrl() {
            return previewUrl;
        }
    }
}
