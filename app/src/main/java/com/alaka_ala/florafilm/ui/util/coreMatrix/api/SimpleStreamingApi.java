package com.alaka_ala.florafilm.ui.util.coreMatrix.api;

import android.annotation.SuppressLint;

import com.alaka_ala.florafilm.ui.util.coreMatrix.api.model.TorrentDetails;
import com.alaka_ala.florafilm.ui.util.coreMatrix.api.model.TorrentFileStat;
import com.alaka_ala.florafilm.ui.util.coreMatrix.api.model.TorrentStatus;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * High-level API for easy interaction with TorrServe.
 */
public class SimpleStreamingApi {

    private final TorrServeApi api;
    private final String baseUrl;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public static class StreamingException extends Exception {
        public StreamingException(String message) {
            super(message);
        }

        public StreamingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public SimpleStreamingApi(String baseUrl) {
        this.baseUrl = baseUrl;
        this.api = new TorrServeApi(baseUrl);
    }

    /**
     * Starts streaming a torrent from a magnet link or hash.
     *
     * @param link   The magnet link or torrent hash.
     * @param title  An optional title for the torrent.
     * @param poster An optional poster URL for the torrent.
     * @return The status of the newly added torrent.
     * @throws StreamingException if the torrent fails to start.
     */
    public TorrentStatus startStreaming(String link, String title, String poster) throws StreamingException {
        try {
            return api.addTorrent(link, title, poster, true);
        } catch (TorrServeApi.ApiException e) {
            throw new StreamingException("Failed to start streaming: " + e.getMessage(), e);
        }
    }

    /**
     * Stops and removes a torrent from the server.
     *
     * @param torrentHash The hash of the torrent to stop.
     * @throws StreamingException if stopping the torrent fails.
     */
    public void stopStreaming(String torrentHash) throws StreamingException {
        try {
            api.removeTorrent(torrentHash);
        } catch (TorrServeApi.ApiException e) {
            throw new StreamingException("Failed to stop streaming: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the current status of a torrent.
     *
     * @param torrentHash The hash of the torrent.
     * @return The current TorrentStatus.
     * @throws StreamingException if fetching the status fails.
     */
    public TorrentStatus getTorrentStatus(String torrentHash) throws StreamingException {
        try {
            return api.getTorrent(torrentHash);
        } catch (TorrServeApi.ApiException e) {
            throw new StreamingException("Failed to get torrent status: " + e.getMessage(), e);
        }
    }

    /**
     * Searches for torrents using the server's search functionality.
     *
     * @param query The search query.
     * @return A list of search results.
     * @throws StreamingException if the search fails.
     */
    public List<TorrentDetails> search(String query) throws StreamingException {
        try {
            return api.search(query);
        } catch (TorrServeApi.ApiException e) {
            throw new StreamingException("Failed to search: " + e.getMessage(), e);
        }
    }

    /**
     * Asynchronously waits for a torrent to have at least one file and be in a working state.
     *
     * @param torrentHash    The hash of the torrent to wait for.
     * @param timeoutSeconds The maximum time to wait in seconds.
     * @return A CompletableFuture that will complete with the TorrentStatus when ready.
     */
    public CompletableFuture<TorrentStatus> waitForReady(String torrentHash, int timeoutSeconds) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            long timeout = timeoutSeconds * 1000L;

            while (System.currentTimeMillis() - startTime < timeout) {
                try {
                    TorrentStatus status = getTorrentStatus(torrentHash);
                    if (status != null && status.getFileStats() != null && !status.getFileStats().isEmpty()) {
                        return status; // Ready
                    }
                    Thread.sleep(2000); // Wait before next check
                } catch (StreamingException e) {
                    // Ignore and retry
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for torrent to be ready", e);
                }
            }
            throw new RuntimeException("Timeout waiting for torrent to become ready");
        }, executor);
    }

    /**
     * Constructs a direct streaming URL for a specific file within a torrent.
     *
     * @param magnetLink The magnetLink.
     * @param fileIndex   The index of the file in the torrent's file list.
     * @return The full HTTP URL to stream the file.
     */
    @SuppressLint("DefaultLocale")
    public String getFileStreamUrl(String magnetLink, int fileIndex) {
        return String.format("%s/stream/mov.m3u?play=play&link=%s&index=%d", baseUrl, magnetLink, fileIndex);
    }

    /**
     * Constructs a URL to get the M3U playlist for a torrent.
     *
     * @param torrentHash The hash of the torrent.
     * @return The full HTTP URL for the playlist.
     */
    public String getPlaylistUrl(String torrentHash) {
        try {
            return String.format("%s/playlist?hash=%s", baseUrl, URLEncoder.encode(torrentHash, StandardCharsets.UTF_8.toString()));
        } catch (UnsupportedEncodingException e) {
            // Should not happen with UTF-8
            throw new RuntimeException(e);
        }
    }
}
