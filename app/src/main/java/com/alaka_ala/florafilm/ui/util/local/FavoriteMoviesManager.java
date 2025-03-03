package com.alaka_ala.florafilm.ui.util.local;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoriteMoviesManager {

    private static final String TAG = "FavoriteMoviesManager";
    private static final String FAVORITES_FILE_NAME = "favorite_movies.dat";

    private Map<Integer, MovieData> favoriteMovies;
    private final Context context;

    public FavoriteMoviesManager(Context context) {
        this.context = context;
        this.favoriteMovies = loadFavorites();
        if (this.favoriteMovies == null) {
            this.favoriteMovies = new HashMap<>();
        }
    }

    public void addFavorite(int movieId, String url, String name) {
        MovieData movieData = new MovieData(url, name, true);
        favoriteMovies.put(movieId, movieData);
        saveFavorites();
    }

    public void removeFavorite(int movieId) {
        if (favoriteMovies.containsKey(movieId)) {
            MovieData movieData = favoriteMovies.get(movieId);
            MovieData newMovieData = new MovieData(movieData.getUrl(), movieData.getName(), false);
            favoriteMovies.put(movieId, newMovieData);
            saveFavorites();
        }
    }

    public boolean isFavorite(int movieId) {
        MovieData movieData = favoriteMovies.get(movieId);
        return movieData != null && movieData.isFavorite();
    }

    public List<MovieData> getFavoriteMovies() {
        List<MovieData> favoriteList =new ArrayList<>();
        for (Map.Entry<Integer, MovieData> entry : favoriteMovies.entrySet()) {
            if (entry.getValue().isFavorite()) {
                favoriteList.add(entry.getValue());
            }
        }
        return favoriteList;
    }

    public MovieData getMovieData(int movieId) {
        return favoriteMovies.get(movieId);
    }

    public List<MovieData> getAllMovies() {
        return new ArrayList<>(favoriteMovies.values());
    }

    private void saveFavorites() {
        File cacheDir = context.getCacheDir();
        File favoritesFile = new File(cacheDir, FAVORITES_FILE_NAME);

        try (FileOutputStream fos = new FileOutputStream(favoritesFile);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(favoriteMovies);
            Log.d(TAG, "Favorites saved to cache: " + favoritesFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Error saving favorites to cache", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, MovieData> loadFavorites() {
        File cacheDir = context.getCacheDir();
        File favoritesFile = new File(cacheDir, FAVORITES_FILE_NAME);

        if (!favoritesFile.exists()) {
            return null;
        }

        try (FileInputStream fis = new FileInputStream(favoritesFile);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                Map<Integer, MovieData> loadedFavorites = (Map<Integer, MovieData>) obj;
                Log.d(TAG, "Favorites loaded from cache: " + favoritesFile.getAbsolutePath());
                return loadedFavorites;
            } else {
                Log.e(TAG, "Invalid data format in favorites file");
                return null;
            }
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "Error loading favorites from cache", e);
            return null;
        }
    }

    public void clearFavorites() {
        favoriteMovies.clear();
        saveFavorites();
    }


    public static class MovieData implements Serializable {
        private final String url;
        private final String name;
        private final boolean isFavorite;

        public MovieData(String url, String name, boolean isFavorite) {
            this.url = url;
            this.name = name;
            this.isFavorite = isFavorite;
        }

        public String getUrl(){
            return url;
        }

        public String getName() {
            return name;
        }

        public boolean isFavorite() {
            return isFavorite;
        }
    }
}
