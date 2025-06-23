package com.alaka_ala.florafilm.ui.util.torrents;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.se_bastiaan.torrentstream.StreamStatus;
import com.github.se_bastiaan.torrentstream.Torrent;
import com.github.se_bastiaan.torrentstream.TorrentOptions;
import com.github.se_bastiaan.torrentstream.TorrentStream;
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.util.MimeTypes;

import org.libtorrent4j.Priority;
import org.libtorrent4j.TorrentHandle;

public class TorrentStreamerV2 {
    private static final long INITIAL_BUFFER_SIZE = 32 * 1024 * 1024; // 32 МБ
    private static final float PLAYBACK_BUFFER_THRESHOLD = 0.15f; // 15% буферизации для старта

    private Context context;
    private ExoPlayer exoPlayer;
    private TorrentStream torrentStream;
    private Torrent currentTorrent;
    private ProgressBar progressBar;

    public TorrentStreamerV2(Context context, ExoPlayer exoPlayer, ProgressBar progressBar) {
        this.context = context;
        this.exoPlayer = exoPlayer;
        this.progressBar = progressBar;
    }

    public void start(String magnet) {
        showLoading(true);

        TorrentOptions.Builder builderOpt = new TorrentOptions.Builder()
                .saveLocation(context.getExternalCacheDir())
                .autoDownload(true);

        torrentStream = TorrentStream.init(builderOpt.build());
        torrentStream.addListener(new TorrentListener() {
            @Override
            public void onStreamPrepared(Torrent torrent) {
                currentTorrent = torrent;
                setInitialBuffer();
            }

            @Override
            public void onStreamStarted(Torrent torrent) {
            }

            @Override
            public void onStreamReady(Torrent torrent) {
                startPlayback(torrent);
            }

            @Override
            public void onStreamProgress(Torrent torrent, StreamStatus status) {
                updateBufferProgress(status.bufferProgress);

                if (status.bufferProgress > PLAYBACK_BUFFER_THRESHOLD &&
                        exoPlayer.getPlaybackState() == Player.STATE_BUFFERING) {
                    exoPlayer.play();
                }
            }

            @Override
            public void onStreamStopped() {

            }

            @Override
            public void onStreamError(Torrent torrent, Exception e) {
                showError("Ошибка загрузки");
            }
        });

        torrentStream.startStream(magnet);
    }

    private void setInitialBuffer() {
        if (currentTorrent == null) return;

        TorrentHandle handle = currentTorrent.getTorrentHandle();
        long pieceSize = handle.torrentFile().pieceLength();
        int piecesToBuffer = (int) (INITIAL_BUFFER_SIZE / pieceSize) + 1;

        for (int i = 0; i < piecesToBuffer; i++) {
            handle.piecePriority(i, Priority.TOP_PRIORITY);
        }
        Log.d("Buffer", "Buffering first " + piecesToBuffer + " pieces (" + (pieceSize * piecesToBuffer / (1024 * 1024)) + " MB)");
    }

    private void startPlayback(Torrent torrent) {
        Uri videoUri = Uri.fromFile(torrent.getVideoFile());
        exoPlayer.setMediaItem(MediaItem.fromUri(videoUri));
        exoPlayer.prepare();
    }

    private void updateBufferProgress(float progress) {
        progressBar.setProgress((int) (progress * 100));
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        showLoading(false);
    }

    public void release() {
        if (torrentStream != null) torrentStream.stopStream();
        exoPlayer.release();
    }
}