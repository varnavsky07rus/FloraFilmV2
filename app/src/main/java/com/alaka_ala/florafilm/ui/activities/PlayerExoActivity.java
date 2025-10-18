package com.alaka_ala.florafilm.ui.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alaka_ala.florafilm.BuildConfig;
import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.ActivityPlayerExoBinding;
import com.alaka_ala.florafilm.ui.util.api.EPData;
import com.alaka_ala.florafilm.ui.util.api.hdvb.HDVB;
import com.alaka_ala.florafilm.ui.util.api.lumex.LumexApi;
import com.alaka_ala.florafilm.ui.util.local.ResumeLastMovie;
import com.alaka_ala.florafilm.ui.util.player.PlaybackPositionManager;
import com.alaka_ala.florafilm.ui.util.player.PlayerGestureListener;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class PlayerExoActivity extends AppCompatActivity {
    private ActivityPlayerExoBinding binding;
    private ExoPlayer exoPlayer;
    private PlaybackPositionManager playbackPositionManager;
    private EPData epData;
    private GestureDetector gestureDetector;
    private PlayerGestureListener gestureListener;

    private int currentResizeMode = 0;

    @SuppressLint({"ClickableViewAccessibility", "CutPasteId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerExoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        playbackPositionManager = new PlaybackPositionManager(this);
        Intent intent = getIntent();
        epData = (EPData) intent.getSerializableExtra("epData");
        if (epData == null) {
            Toast.makeText(this, "Ошибка получения данных!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        exoPlayer = new ExoPlayer.Builder(this).build();
        binding.playerExoView.setPlayer(exoPlayer);

        LinearLayout centerFeedbackLayout = findViewById(R.id.center_feedback_layout);
        ImageView centerFeedbackIcon = findViewById(R.id.center_feedback_icon);
        TextView centerFeedbackText = findViewById(R.id.center_feedback_text);
        ProgressBar centerFeedbackProgress = findViewById(R.id.center_feedback_progress);
        TextView speed2xText = findViewById(R.id.speed_2x_text);

        gestureListener = new PlayerGestureListener(this, exoPlayer, binding.playerExoView,
                centerFeedbackLayout, centerFeedbackIcon, centerFeedbackText, centerFeedbackProgress, speed2xText);
        gestureDetector = new GestureDetector(this, gestureListener);

        binding.playerExoView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            if (event.getAction() == MotionEvent.ACTION_UP) {
                gestureListener.onUp(event);
            }
            return true;
        });

        updateTitleName();
        preparePlayer();

        setupResizeModeToggle();
        saveToLastMovie();
    }

    /** Созранение последнего просмотренного фильма для всплывающего фрагмента для продолжения просмотра */
    private void saveToLastMovie() {
        ResumeLastMovie resumeLastMovie = new ResumeLastMovie(this);
        resumeLastMovie.saveLastMovie(epData.getFilmInfo().getKinopoiskId(), epData.getFilmInfo().getNameRu(), epData.getFilmInfo().getPosterUrl());
        resumeLastMovie.setLaunched();
    }

    private void setupResizeModeToggle() {
        ImageView imageViewResizeVideo = binding.playerExoView.findViewById(R.id.imageViewResizeVideo);
        imageViewResizeVideo.setOnClickListener(view -> {
            currentResizeMode = (currentResizeMode + 1) % 3;
            switch (currentResizeMode) {
                case 0:
                    binding.playerExoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                    break;
                case 1:
                    binding.playerExoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                    break;
                case 2:
                    binding.playerExoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                    break;
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }

    private void preparePlayer() {
        ArrayList<MediaItem> mediaItems = new ArrayList<>();

        if (Objects.equals(epData.getTypeContent(), EPData.TYPE_CONTENT_SERIAL) && epData.getBalancer().equals("HDVB")) {
            ArrayList<MediaItem> mediaItemsToken = new ArrayList<>();
            for (int j = 0; j < epData.getSerial().getSeasons().get(epData.getIndexSeason()).getEpisodes().size(); j++) {
                String token = getTokenHdvb(j);
                mediaItemsToken.add(new MediaItem.Builder()
                        .setMediaId(String.valueOf(j))
                        .setCustomCacheKey(token)
                        .setUri(Uri.EMPTY)
                        .build());
            }
            exoPlayer.setMediaItems(mediaItemsToken);
            exoPlayer.addListener(new Player.Listener() {
                @Override
                public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                    if (mediaItem != null && exoPlayer.getCurrentMediaItemIndex() != epData.getIndexEpisode() && (reason == 1 || reason == 2)) {
                        loadMediaItemByToken(mediaItemsToken, exoPlayer.getCurrentMediaItemIndex(), epData.getBalancer());
                        saveCurrentPlaybackPosition();
                    }
                    updateTitleName();
                }
            });
            loadMediaItemByToken(mediaItemsToken, epData.getIndexEpisode(), epData.getBalancer());

        }
        else if (Objects.equals(epData.getTypeContent(), EPData.TYPE_CONTENT_FILM) && epData.getBalancer().equals("LUMEX")) {
            String uriVideoData = epData.getFilm().getTranslations().get(epData.getIndexTranslation()).getVideoData().get(0).getValue();
            LumexApi.getHls(uriVideoData, new LumexApi.CallbackLumexHls() {
                @Override
                public void success(LumexApi.LumexHLS lumexHLS) {
                    MediaItem mediaItem = new MediaItem.Builder()
                            .setUri(lumexHLS.getUrl().startsWith("//") ? "https:" + lumexHLS.getUrl() : lumexHLS.getUrl())
                            .build();
                    mediaItems.add(mediaItem);
                    exoPlayer.setMediaItems(
                            mediaItems,
                            epData.getIndexEpisode(),
                            playbackPositionManager.getSavedPositionEpisode(
                                    epData.getFilmInfo().getKinopoiskId(),
                                    epData.getIndexEpisode(),
                                    epData.getIndexSeason()
                            ));
                    exoPlayer.addAnalyticsListener(new AnalyticsListener() {
                        @Override
                        public void onMediaItemTransition(@NonNull EventTime eventTime, @Nullable MediaItem mediaItem, int reason) {
                            AnalyticsListener.super.onMediaItemTransition(eventTime, mediaItem, reason);
                            saveCurrentPlaybackPosition();
                            updateTitleName();
                        }
                    });
                    exoPlayer.prepare();
                    exoPlayer.play();
                }

                @Override
                public void error(String err) {
                    Toast.makeText(PlayerExoActivity.this, "Ошибка загрузки видео: " + err, Toast.LENGTH_SHORT).show();
                }
            });

        }
        else if (Objects.equals(epData.getTypeContent(), EPData.TYPE_CONTENT_SERIAL) && epData.getBalancer().equals("LUMEX")) {
            ArrayList<MediaItem> mediaItemsToken = new ArrayList<>();
            for (int j = 0; j < epData.getSerial().getSeasons().get(epData.getIndexSeason()).getEpisodes().size(); j++) {
                String token = getTokenLumex(j);
                mediaItemsToken.add(new MediaItem.Builder()
                        .setMediaId(String.valueOf(j))
                        .setCustomCacheKey(token)
                        .setUri(Uri.EMPTY)
                        .build());
            }
            exoPlayer.setMediaItems(mediaItemsToken);
            exoPlayer.addListener(new Player.Listener() {
                @Override
                public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                    if (mediaItem != null && exoPlayer.getCurrentMediaItemIndex() != epData.getIndexEpisode() && (reason == 1 || reason == 2)) {
                        loadMediaItemByToken(mediaItemsToken, exoPlayer.getCurrentMediaItemIndex(), epData.getBalancer());
                        saveCurrentPlaybackPosition();
                    }
                    updateTitleName();
                }
            });
            loadMediaItemByToken(mediaItemsToken, epData.getIndexEpisode(), epData.getBalancer());

        }
        else if (Objects.equals(epData.getTypeContent(), EPData.TYPE_CONTENT_FILM) && epData.getBalancer().equals("magnet")) {
            String magnet = epData.getFilm().getTranslations().get(0).getVideoData().get(0).getValue();
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(magnet)
                    .build();
            mediaItems.add(mediaItem);
            exoPlayer.setMediaItems(mediaItems, epData.getIndexEpisode(), 0);
            exoPlayer.prepare();
            exoPlayer.play();

        }
        else if (Objects.equals(epData.getTypeContent(), EPData.TYPE_CONTENT_FILM) && epData.getBalancer().equals("HDVB")) {
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(epData.getFilm().getTranslations().get(epData.getIndexTranslation()).getVideoData().get(epData.getIndexQuality()).getValue())
                    .build();
            mediaItems.add(mediaItem);
            exoPlayer.setMediaItems(
                    mediaItems,
                    epData.getIndexEpisode(),
                    playbackPositionManager.getSavedPositionEpisode(
                            epData.getFilmInfo().getKinopoiskId(),
                            epData.getIndexEpisode(),
                            epData.getIndexSeason()
                    ));
            exoPlayer.addAnalyticsListener(new AnalyticsListener() {
                @Override
                public void onMediaItemTransition(@NonNull EventTime eventTime, @Nullable MediaItem mediaItem, int reason) {
                    AnalyticsListener.super.onMediaItemTransition(eventTime, mediaItem, reason);
                    saveCurrentPlaybackPosition();
                    updateTitleName();
                }
            });
            exoPlayer.prepare();
            exoPlayer.play();
        }
        else if (Objects.equals(epData.getTypeContent(), EPData.TYPE_CONTENT_FILM) && epData.getBalancer().equals("VIBIX")) {
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(replaceIncorrectProtocol(epData.getFilm().getTranslations().get(epData.getIndexTranslation()).getVideoData().get(epData.getIndexQuality()).getValue()))
                    .build();
            mediaItems.add(mediaItem);
            exoPlayer.setMediaItems(
                    mediaItems,
                    epData.getIndexEpisode(),
                    playbackPositionManager.getSavedPositionEpisode(
                            epData.getFilmInfo().getKinopoiskId(),
                            epData.getIndexEpisode(),
                            epData.getIndexSeason()
                    ));
            exoPlayer.addAnalyticsListener(new AnalyticsListener() {
                @Override
                public void onMediaItemTransition(@NonNull EventTime eventTime, @Nullable MediaItem mediaItem, int reason) {
                    AnalyticsListener.super.onMediaItemTransition(eventTime, mediaItem, reason);
                    saveCurrentPlaybackPosition();
                    updateTitleName();
                }
            });
            exoPlayer.prepare();
            exoPlayer.play();
        }
        else if (Objects.equals(epData.getTypeContent(), EPData.TYPE_CONTENT_SERIAL)) {
            for (int i = 0; i < epData.getSerial().getSeasons().size(); i++) {
                for (int j = 0; j < epData.getSerial().getSeasons().get(i).getEpisodes().size(); j++) {
                    for (int k = 0; k < epData.getSerial().getSeasons().get(i).getEpisodes().get(j).getTranslations().size(); k++) {
                        String url = epData.getSerial().getSeasons().get(i).getEpisodes().get(j).getTranslations().get(k).getVideoData().get(epData.getIndexQuality()).getValue();
                        MediaItem mediaItem = new MediaItem.Builder().setUri(Uri.parse(url)).build();
                        mediaItems.add(mediaItem);
                    }
                }
            }
            exoPlayer.setMediaItems(
                    mediaItems,
                    epData.getIndexEpisode(),
                    playbackPositionManager.getSavedPositionEpisode(
                            epData.getFilmInfo().getKinopoiskId(),
                            epData.getIndexEpisode(),
                            epData.getIndexSeason()
                    ));
            exoPlayer.addAnalyticsListener(new AnalyticsListener() {
                @Override
                public void onMediaItemTransition(@NonNull EventTime eventTime, @Nullable MediaItem mediaItem, int reason) {
                    AnalyticsListener.super.onMediaItemTransition(eventTime, mediaItem, reason);
                    saveCurrentPlaybackPosition();
                    updateTitleName();
                }
            });
            exoPlayer.prepare();
            exoPlayer.play();
        }
    }

    private void saveCurrentPlaybackPosition() {
        playbackPositionManager.savePositionEpisode(
                epData.getFilmInfo().getKinopoiskId(),
                exoPlayer.getCurrentMediaItemIndex(),
                epData.getIndexSeason(),
                epData.getIndexTranslation(),
                epData.getIndexQuality(),
                exoPlayer.getCurrentPosition(),
                epData.getBalancer());
    }

    private String getTokenHdvb(int episodeIndex) {
        int countTranslationIndexes = (epData.getSerial().getSeasons().get(epData.getIndexSeason()).getEpisodes().get(episodeIndex).getTranslations().size() - 1);
        int requareTranslationIndex = epData.getIndexTranslation();
        if (requareTranslationIndex > countTranslationIndexes) {
            return epData.getSerial()
                    .getSeasons().get(epData.getIndexSeason())
                    .getEpisodes().get(episodeIndex)
                    .getTranslations().get(countTranslationIndexes)
                    .getVideoData().get(epData.getIndexQuality()).getValue();
        }
        return epData.getSerial().getSeasons().get(epData.getIndexSeason()).getEpisodes().get(episodeIndex).getTranslations().get(epData.getIndexTranslation()).getVideoData().get(epData.getIndexQuality()).getValue();
    }

    private String getTokenLumex(int episodeIndex) {
        // Assuming Lumex also uses a similar token fetching logic, adjust if different
        int countTranslationIndexes = (epData.getSerial().getSeasons().get(epData.getIndexSeason()).getEpisodes().get(episodeIndex).getTranslations().size() - 1);
        int requareTranslationIndex = epData.getIndexTranslation();
        if (requareTranslationIndex > countTranslationIndexes) {
            return epData.getSerial()
                    .getSeasons().get(epData.getIndexSeason())
                    .getEpisodes().get(episodeIndex)
                    .getTranslations().get(countTranslationIndexes)
                    .getVideoData().get(epData.getIndexQuality()).getValue();
        }
        return epData.getSerial().getSeasons().get(epData.getIndexSeason()).getEpisodes().get(episodeIndex).getTranslations().get(epData.getIndexTranslation()).getVideoData().get(epData.getIndexQuality()).getValue();
    }

    private void loadMediaItemByToken(ArrayList<MediaItem> mediaItemsToken, int indexEpisode, String balancer) {
        MediaItem mediaItem = exoPlayer.getMediaItemAt(indexEpisode);
        Handler handler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            String uri = null;
            if (balancer.equals("LUMEX")) {
                LumexApi.getHls(String.valueOf(mediaItem.localConfiguration.customCacheKey), new LumexApi.CallbackLumexHls() {
                    @Override
                    public void success(LumexApi.LumexHLS lumexHLS) {
                        String newUri = lumexHLS.getUrl().startsWith("//") ? "https:" + lumexHLS.getUrl() : lumexHLS.getUrl();
                        handler.post(() -> updateMediaItemAndPlay(mediaItemsToken, indexEpisode, newUri));
                    }

                    @Override
                    public void error(String err) {
                        handler.post(() -> Toast.makeText(PlayerExoActivity.this, "Ошибка загрузки HLS Lumex: " + err, Toast.LENGTH_SHORT).show());
                    }
                });
            } else if (balancer.equals("HDVB")) {
                uri = HDVB.getFileSerial(String.valueOf(mediaItem.localConfiguration.customCacheKey));
                String finalUri = uri;
                handler.post(() -> updateMediaItemAndPlay(mediaItemsToken, indexEpisode, finalUri));
            }
        }).start();
    }

    private void updateMediaItemAndPlay(ArrayList<MediaItem> mediaItemsToken, int indexEpisode, String newUri) {
        MediaItem updatedMediaItem = MediaItem.fromUri(newUri);
        mediaItemsToken.set(indexEpisode, updatedMediaItem);

        exoPlayer.setMediaItems(
                mediaItemsToken,
                indexEpisode,
                playbackPositionManager.getSavedPositionEpisode(
                        epData.getFilmInfo().getKinopoiskId(),
                        epData.getIndexEpisode(),
                        epData.getIndexSeason()
                ));
        exoPlayer.prepare();
        exoPlayer.play();
    }

    private String replaceIncorrectProtocol(String value) {
        return value.replaceFirst(".+:", "https:");
    }

    private void updateTitleName() {
        TextView textViewNameFilm = binding.playerExoView.findViewById(R.id.textViewNameFilm);

        String nameFilm =
                epData.getTypeContent().equals(EPData.TYPE_CONTENT_FILM) ?
                        epData.getFilmInfo().getNameRu() :
                        epData.getFilmInfo().getNameRu()
                                + " | сезон " + (epData.getIndexSeason() + 1)
                                + " серия " + (exoPlayer.getCurrentMediaItemIndex() + 1);
        textViewNameFilm.setText(nameFilm);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            saveCurrentPlaybackPosition();
            exoPlayer.stop();
            exoPlayer.release();

        }
    }
}
