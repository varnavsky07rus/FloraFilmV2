package com.alaka_ala.florafilm.ui.util.coreMatrix.api;

import com.alaka_ala.florafilm.ui.util.coreMatrix.api.model.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TorrServeApi {

    private static final Gson gson = new Gson();
    private final String baseUrl;
    private final String authToken;

    public static class ApiException extends Exception {
        private final int code;
        public ApiException(String message, int code) {
            super(message);
            this.code = code;
        }
        public int getCode() {
            return code;
        }
    }

    public TorrServeApi(String baseUrl) {
        this(baseUrl, null);
    }

    public TorrServeApi(String baseUrl, String authToken) {
        this.baseUrl = baseUrl;
        this.authToken = authToken;
    }

    // --- Server Operations ---

    public String echo() throws ApiException {
        return makeGetRequest("/echo");
    }

    public void shutdown() throws ApiException {
        makeGetRequest("/shutdown");
    }

    // --- Torrent Operations ---

    public List<TorrentStatus> listTorrents() throws ApiException {
        String resp = makePostRequest("/torrents", gson.toJson(new TorrentRequest("list")));
        Type listType = new TypeToken<List<TorrentStatus>>(){}.getType();
        return gson.fromJson(resp, listType);
    }

    public TorrentStatus addTorrent(String link, String title, String poster, boolean saveToDb) throws ApiException {
        TorrentRequest req = new TorrentRequest("add", link, title, poster, null, null, saveToDb);
        String resp = makePostRequest("/torrents", gson.toJson(req));
        return gson.fromJson(resp, TorrentStatus.class);
    }

    public TorrentStatus getTorrent(String hash) throws ApiException {
        String resp = makePostRequest("/torrents", gson.toJson(new TorrentRequest("get", hash)));
        return gson.fromJson(resp, TorrentStatus.class);
    }

    public void removeTorrent(String hash) throws ApiException {
        makePostRequest("/torrents", gson.toJson(new TorrentRequest("rem", hash)));
    }

    public void dropTorrent(String hash) throws ApiException {
        makePostRequest("/torrents", gson.toJson(new TorrentRequest("drop", hash)));
    }

    // --- Settings Operations ---

    public BTSettings getSettings() throws ApiException {
        String resp = makePostRequest("/settings", gson.toJson(new SettingsRequest("get")));
        return gson.fromJson(resp, BTSettings.class);
    }

    public void setSettings(BTSettings settings) throws ApiException {
        makePostRequest("/settings", gson.toJson(new SettingsRequest("set", settings)));
    }

    public void defaultSettings() throws ApiException {
        makePostRequest("/settings", gson.toJson(new SettingsRequest("def")));
    }

    // --- Viewed Operations ---

    public List<Viewed> listViewed(String hash) throws ApiException {
        String resp = makePostRequest("/viewed", gson.toJson(new ViewedRequest("list", hash)));
        Type listType = new TypeToken<List<Viewed>>(){}.getType();
        return gson.fromJson(resp, listType);
    }

    public void setViewed(String hash, int fileIndex) throws ApiException {
        makePostRequest("/viewed", gson.toJson(new ViewedRequest("set", hash, fileIndex)));
    }

    public void removeViewed(String hash, int fileIndex) throws ApiException {
        makePostRequest("/viewed", gson.toJson(new ViewedRequest("rem", hash, fileIndex)));
    }

    // --- Other Operations ---

    public CacheState getCacheState(String hash) throws ApiException {
        String resp = makePostRequest("/cache", gson.toJson(new CacheRequest("get", hash)));
        return gson.fromJson(resp, CacheState.class);
    }

    public List<TorrentDetails> search(String query) throws ApiException {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            String resp = makeGetRequest("/search?query=" + encodedQuery);
            Type listType = new TypeToken<List<TorrentDetails>>(){}.getType();
            return gson.fromJson(resp, listType);
        } catch (Exception e) {
            throw new ApiException("Failed to encode search query: " + e.getMessage(), -1);
        }
    }

    // --- Network Implementation ---

    private String makeGetRequest(String path) throws ApiException {
        return makeGetRequest(path, 10000); // 10 seconds timeout
    }

    private String makeGetRequest(String path, int timeoutMs) throws ApiException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(baseUrl + path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            if (authToken != null && !authToken.isEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer " + authToken);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                return readResponse(conn.getInputStream());
            } else {
                throw new ApiException(readResponse(conn.getErrorStream()), responseCode);
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), -1);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String makePostRequest(String path, String jsonBody) throws ApiException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(baseUrl + path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            if (authToken != null && !authToken.isEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer " + authToken);
            }

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                return readResponse(conn.getInputStream());
            } else {
                throw new ApiException(readResponse(conn.getErrorStream()), responseCode);
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), -1);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String readResponse(InputStream stream) throws Exception {
        if (stream == null) return "";
        try (BufferedReader in = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
}
