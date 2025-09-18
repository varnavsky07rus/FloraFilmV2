package com.alaka_ala.florafilm.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.ActivityPlayerExoBinding;
import com.alaka_ala.florafilm.ui.util.api.EPData;
import com.alaka_ala.florafilm.ui.util.api.hdvb.HDVB;
import com.alaka_ala.florafilm.ui.util.api.lumex.LumexApi;
import com.alaka_ala.florafilm.ui.util.local.ResumeLastMovie;
import com.alaka_ala.florafilm.ui.util.player.PlaybackPositionManager;
import com.alaka_ala.florafilm.ui.util.torrents.listeners.TorrentListener;
import com.alaka_ala.florafilm.ui.util.torrents.main.StreamStatus;
import com.alaka_ala.florafilm.ui.util.torrents.main.Torrent;
import com.alaka_ala.florafilm.ui.util.torrents.main.TorrentOptions;
import com.alaka_ala.florafilm.ui.util.torrents.main.TorrentStream;
import com.alaka_ala.florafilm.ui.util.torrents.server.LocalHttpServer;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.appmetrica.analytics.AppMetrica;
import io.appmetrica.analytics.AppMetricaConfig;

public class PlayerExoActivity extends AppCompatActivity {
    private ActivityPlayerExoBinding binding;
    private ExoPlayer exoPlayer;
    private PlaybackPositionManager playbackPositionManager;
    private EPData epData;

    private int count_send_event = 0;

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
            return;
        }
        // Создаем LoadControl с увеличенным буфером для стабильности при стриминге торрентов
        DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                        60 * 1000, // Минимальная длительность буфера (1 минута)
                        120 * 1000, // Максимальная длительность буфера (2 минуты)
                        15 * 1000,  // Длительность для начала воспроизведения (15 секунд)
                        30 * 1000   // Длительность для возобновления после ребуферизации (30 секунд)
                )
                .build();

        exoPlayer = new ExoPlayer.Builder(this)
                .setLoadControl(loadControl)
                .build();
        binding.playerExoView.setPlayer(exoPlayer);
        appMetrica();
        updateTitleName();
        preparePlayer();

        resizeMode();

        saveToLastMovie();


    }

    private void saveToLastMovie() {
        Context context = this;
        ResumeLastMovie resumeLastMovie = new ResumeLastMovie(context);
        resumeLastMovie.saveLastMovie(epData.getFilmInfo().getKinopoiskId(), epData.getFilmInfo().getNameRu(), epData.getFilmInfo().getPosterUrl());
        resumeLastMovie.setLaunched();

    }

    private void resizeMode() {
        int[] count = {0};
        ImageView imageViewResizeVideo = binding.playerExoView.findViewById(R.id.imageViewResizeVideo);
        imageViewResizeVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (count[0]) {
                    case 0:
                        binding.playerExoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                        count[0] = 1;
                        break;
                    case 1:
                        binding.playerExoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                        count[0] = 2;
                        break;
                    case 2:
                        binding.playerExoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                        count[0] = 0;
                        break;
                }
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

    private TorrentStream torrentStream;
    private void preparePlayer() {
        ArrayList<MediaItem> mediaItems = new ArrayList<>();

        if (Objects.equals(epData.getTypeContent(), EPData.TYPE_CONTENT_SERIAL) && epData.getBalancer().equals("HDVB")) {
            ArrayList<MediaItem> mediaItemsToken = new ArrayList<>();
            for (int j = 0; j < epData.getSerial().getSeasons().get(epData.getIndexSeason()).getEpisodes().size(); j++) {
                // На некоторые серии нет какой-либо серии в определенной озвучке, для этого необходимо получить дугую озвучку
                String token = getTokenHdvb(j);
                mediaItemsToken.add(new MediaItem.Builder()
                        .setMediaId(String.valueOf(j)) // Индекс серии
                        .setCustomCacheKey(token)
                        .setUri(Uri.EMPTY) // Пока пусто
                        .build());
            }

            // Создали все MediaItem токены и добавили в плеер
            exoPlayer.setMediaItems(mediaItemsToken);

            // Обработчик плеера для постепенной загрузки медиа по токенам
            exoPlayer.addListener(new Player.Listener() {
                @Override
                public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                    // reason 1 = если автоматически переключается на след. серию а reason = 2 если пользователь переключает
                    if (mediaItem != null && exoPlayer.getCurrentMediaItemIndex() != epData.getIndexEpisode() && reason == 1 || reason == 2) {
                        onRequareMediaItem(mediaItemsToken, exoPlayer.getCurrentMediaItemIndex(), epData.getBalancer());

                        playbackPositionManager.savePositionEpisode(
                                epData.getFilmInfo().getKinopoiskId(),
                                exoPlayer.getCurrentMediaItemIndex(),
                                epData.getIndexSeason(),
                                epData.getIndexTranslation(),
                                epData.getIndexQuality(),
                                exoPlayer.getCurrentPosition(),
                                epData.getBalancer());
                    }
                    updateTitleName();
                }
            });

            // Загрузка первой серии
            onRequareMediaItem(mediaItemsToken, epData.getBalancer());

        }
        else if (Objects.equals(epData.getTypeContent(), EPData.TYPE_CONTENT_FILM) && epData.getBalancer().equals("LUMEX")) {
            MediaItem.Builder mediaItemBuilder = new MediaItem.Builder();
            String uriVideoData = epData.getFilm().getTranslations().get(epData.getIndexTranslation()).getVideoData().get(0).getValue();
            LumexApi.getHls(uriVideoData, new LumexApi.CallbackLumexHls() {
                @Override
                public void success(LumexApi.LumexHLS lumexHLS) {
                    mediaItemBuilder.setUri(lumexHLS.getUrl().startsWith("//") ? "https:" + lumexHLS.getUrl() : lumexHLS.getUrl());
                    mediaItems.add(mediaItemBuilder.build());
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
                            playbackPositionManager.
                                    savePositionEpisode(
                                            epData.getFilmInfo().getKinopoiskId(),
                                            exoPlayer.getCurrentMediaItemIndex(),
                                            epData.getIndexSeason(),
                                            epData.getIndexTranslation(),
                                            epData.getIndexQuality(),
                                            exoPlayer.getCurrentPosition(),
                                            epData.getBalancer());
                            updateTitleName();
                        }
                    });
                    exoPlayer.prepare();
                    exoPlayer.play();
                }

                @Override
                public void error(String err) {

                }
            });

        }
        else if (Objects.equals(epData.getTypeContent(), EPData.TYPE_CONTENT_SERIAL) && epData.getBalancer().equals("LUMEX")) {
            ArrayList<MediaItem> mediaItemsToken = new ArrayList<>();
            for (int j = 0; j < epData.getSerial().getSeasons().get(epData.getIndexSeason()).getEpisodes().size(); j++) {
                // На некоторые серии нет какой-либо серии в определенной озвучке, для этого необходимо получить дугую озвучку
                String token = getTokenHdvb(j);
                mediaItemsToken.add(new MediaItem.Builder()
                        .setMediaId(String.valueOf(j)) // Индекс серии
                        .setCustomCacheKey(token)
                        .setUri(Uri.EMPTY) // Пока пусто
                        .build());
            }
            // Создали все MediaItem токены и добавили в плеер
            exoPlayer.setMediaItems(mediaItemsToken);

            // Обработчик плеера для постепенной загрузки медиа по токенам
            exoPlayer.addListener(new Player.Listener() {
                @Override
                public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                    // reason 1 = если автоматически переключается на след. серию а reason = 2 если пользователь переключает
                    if (mediaItem != null && exoPlayer.getCurrentMediaItemIndex() != epData.getIndexEpisode() && reason == 1 || reason == 2) {
                        onRequareMediaItem(mediaItemsToken, exoPlayer.getCurrentMediaItemIndex(), epData.getBalancer());

                        playbackPositionManager.savePositionEpisode(
                                epData.getFilmInfo().getKinopoiskId(),
                                exoPlayer.getCurrentMediaItemIndex(),
                                epData.getIndexSeason(),
                                epData.getIndexTranslation(),
                                epData.getIndexQuality(),
                                exoPlayer.getCurrentPosition(),
                                epData.getBalancer());
                    }
                    updateTitleName();
                }
            });

            // Загружаем первую серию
            onRequareMediaItem(mediaItemsToken, epData.getBalancer());
        }
        else if (Objects.equals(epData.getTypeContent(), EPData.TYPE_CONTENT_FILM) && epData.getBalancer().equals("magnet")) {
            // Запуск торрента и сервера
            String magnet = epData.getFilm().getTranslations().get(0).getVideoData().get(0).getValue();
            TorrentOptions torrentOptions = new TorrentOptions.Builder()
                    .saveLocation(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
                    .removeFilesAfterStop(true)
                    .build();
            torrentStream = TorrentStream.init(torrentOptions);
            torrentStream.addListener(new TorrentListener() {
                @Override
                public void onStreamPrepared(Torrent torrent) {
                    Log.d("Torrent", "Torrent prepared. File: " + torrent.getVideoFile().getName());

                }

                @Override
                public void onStreamStarted(Torrent torrent) {

                }

                @Override
                public void onStreamError(Torrent torrent, Exception e) {
                    e.printStackTrace();
                }

                @Override
                public void onStreamReady(Torrent torrent) {
                    try {
                        // 1. Создаем и запускаем локальный HTTP сервер
                        LocalHttpServer server = new LocalHttpServer(8080, torrent);
                        server.start();

                        // 2. Создаем MediaItem с локальным URL и передаем в ExoPlayer
                        MediaItem mediaItem = new MediaItem.Builder()
                                .setUri("http://127.0.0.1:8080/" + torrent.getVideoFile().getName())
                                .build();

                        exoPlayer.setMediaItem(mediaItem);
                        exoPlayer.prepare();
                        exoPlayer.addAnalyticsListener(new AnalyticsListener() {
                            @Override
                            public void onIsLoadingChanged(EventTime eventTime, boolean isLoading) {
                                AnalyticsListener.super.onIsLoadingChanged(eventTime, isLoading);
                            }

                            @Override
                            public void onIsPlayingChanged(EventTime eventTime, boolean isPlaying) {
                                AnalyticsListener.super.onIsPlayingChanged(eventTime, isPlaying);
                            }

                            @Override
                            public void onPlaybackStateChanged(EventTime eventTime, int state) {
                                AnalyticsListener.super.onPlaybackStateChanged(eventTime, state);
                                if (state == ExoPlayer.STATE_READY) {
                                    exoPlayer.play();
                                }
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                private int steps = 0;
                private int maxSteps = 1500;
                @Override
                public void onStreamProgress(Torrent torrent, StreamStatus status) {
                    // Обработка прогресса загрузки торрента
                    if (steps == maxSteps) {
                        Log.d("Torrent", "\n\n\nTorrent progress: " + status.progress);
                        Log.d("Torrent", "Torrent buffer: " + status.bufferProgress);
                        Log.d("Torrent", "Torrent seeds: " + status.seeds);
                        Log.d("Torrent", "Torrent index piece: " + status.currentPieceDownload);
                        Log.d("Torrent", "==============================================");
                        steps = 0;
                    }
                    steps++;
                }

                @Override
                public void onStreamStopped() {
                    Log.d("Torrent", "Torrent stopped");
                }
            });
            torrentStream.startStream(magnet);

        }
        else if (Objects.equals(epData.getTypeContent(), EPData.TYPE_CONTENT_FILM) && epData.getBalancer().equals("HDVB")) {

            MediaItem.Builder mediaItemBuilder = new MediaItem.Builder();
            String uriVideoData = epData.getFilm().getTranslations().get(epData.getIndexTranslation()).getVideoData().get(epData.getIndexQuality()).getValue();
            mediaItemBuilder.setUri(uriVideoData);
            mediaItems.add(mediaItemBuilder.build());

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
                    playbackPositionManager.
                            savePositionEpisode(
                                    epData.getFilmInfo().getKinopoiskId(),
                                    exoPlayer.getCurrentMediaItemIndex(),
                                    epData.getIndexSeason(),
                                    epData.getIndexTranslation(),
                                    epData.getIndexQuality(),
                                    exoPlayer.getCurrentPosition(),
                                    epData.getBalancer());
                    updateTitleName();
                }
            });

            exoPlayer.prepare();
            exoPlayer.play();
        }
        else if (Objects.equals(epData.getTypeContent(), EPData.TYPE_CONTENT_FILM) && epData.getBalancer().equals("VIBIX")) {

            MediaItem.Builder mediaItemBuilder = new MediaItem.Builder();
            String uriVideoData = replaceIncorrectProtocol(epData.getFilm().getTranslations().get(epData.getIndexTranslation()).getVideoData().get(epData.getIndexQuality()).getValue());
            mediaItemBuilder.setUri(uriVideoData);
            mediaItems.add(mediaItemBuilder.build());

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
                    playbackPositionManager.
                            savePositionEpisode(
                                    epData.getFilmInfo().getKinopoiskId(),
                                    exoPlayer.getCurrentMediaItemIndex(),
                                    epData.getIndexSeason(),
                                    epData.getIndexTranslation(),
                                    epData.getIndexQuality(),
                                    exoPlayer.getCurrentPosition(),
                                    epData.getBalancer());
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
                public void onMediaItemTransition(EventTime eventTime, @Nullable MediaItem mediaItem, int reason) {
                    AnalyticsListener.super.onMediaItemTransition(eventTime, mediaItem, reason);
                    playbackPositionManager.
                            savePositionEpisode(
                                    epData.getFilmInfo().getKinopoiskId(),
                                    exoPlayer.getCurrentMediaItemIndex(),
                                    epData.getIndexSeason(),
                                    epData.getIndexTranslation(),
                                    epData.getIndexQuality(),
                                    exoPlayer.getCurrentPosition(),
                                    epData.getBalancer());
                    updateTitleName();
                }
            });
            exoPlayer.prepare();
            exoPlayer.play();
        }
    }

    private String replaceIncorrectProtocol(String value) {
        return value.replaceFirst(".+:", "https:");
    }

    private String getTokenHdvb(int j) {
        int countTranslationIndexes = (epData.getSerial().getSeasons().get(epData.getIndexSeason()).getEpisodes().get(j).getTranslations().size() - 1);
        int requareTranslationIndex = epData.getIndexTranslation();
        if (requareTranslationIndex > countTranslationIndexes) {

            return epData.getSerial()
                    .getSeasons().get(epData.getIndexSeason())
                    .getEpisodes().get(j)
                    .getTranslations().get(countTranslationIndexes)
                    .getVideoData().get(epData.getIndexQuality()).getValue();
        }

        return epData.getSerial().getSeasons().get(epData.getIndexSeason()).getEpisodes().get(j).getTranslations().get(epData.getIndexTranslation()).getVideoData().get(epData.getIndexQuality()).getValue();
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

    // Воспроизводит сначала то, что требует пользователь
    private void onRequareMediaItem(ArrayList<MediaItem> mediaItemsToken, String balancer) {
        MediaItem mediaItem = exoPlayer.getMediaItemAt(epData.getIndexEpisode());
        Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                String newUri = message.getData().getString("m3u8");
                MediaItem updatedMediaItem = MediaItem.fromUri(newUri);

                mediaItemsToken.remove(epData.getIndexEpisode());
                mediaItemsToken.add(epData.getIndexEpisode(), updatedMediaItem);

                exoPlayer.setMediaItems(
                        mediaItemsToken,
                        epData.getIndexEpisode(),
                        playbackPositionManager.getSavedPositionEpisode(
                                epData.getFilmInfo().getKinopoiskId(),
                                epData.getIndexEpisode(),
                                epData.getIndexSeason()
                        ));
                exoPlayer.prepare();
                exoPlayer.play();
                return false;
            }
        });

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (balancer.equals("LUMEX")) {
                    LumexApi.getHls(String.valueOf(mediaItem.localConfiguration.customCacheKey), new LumexApi.CallbackLumexHls() {
                        @Override
                        public void success(LumexApi.LumexHLS lumexHLS) {
                            Bundle bundle = new Bundle();
                            bundle.putString("m3u8", lumexHLS.getUrl().startsWith("//") ? "https:" + lumexHLS.getUrl() : lumexHLS.getUrl());
                            Message msg = Message.obtain();
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }

                        @Override
                        public void error(String err) {

                        }
                    });
                } else if (balancer.equals("HDVB")) {
                    String newUri = HDVB.getFileSerial(String.valueOf(mediaItem.localConfiguration.customCacheKey));
                    Bundle bundle = new Bundle();
                    bundle.putString("m3u8", newUri);
                    Message msg = Message.obtain();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }

            }
        });
        thread.start();
    }

    // Далее если в спискке есть еще MediaItem (в плеере) то воспроизводит далее и их
    private void onRequareMediaItem(ArrayList<MediaItem> mediaItemsToken, int indexEpisode, String balancer) {
        MediaItem mediaItem = exoPlayer.getMediaItemAt(indexEpisode);
        Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                String newUri = message.getData().getString("m3u8");
                MediaItem updatedMediaItem = MediaItem.fromUri(newUri);

                mediaItemsToken.remove(indexEpisode);
                mediaItemsToken.add(indexEpisode, updatedMediaItem);

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
                return false;
            }
        });

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String newUri;
                if (balancer.equals("LUMEX")) {
                    LumexApi.getHls(String.valueOf(mediaItem.localConfiguration.customCacheKey), new LumexApi.CallbackLumexHls() {
                        @Override
                        public void success(LumexApi.LumexHLS lumexHLS) {
                            Bundle bundle = new Bundle();
                            bundle.putString("m3u8", lumexHLS.getUrl().startsWith("//") ? "https:" + lumexHLS.getUrl() : lumexHLS.getUrl());
                            Message msg = Message.obtain();
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }

                        @Override
                        public void error(String err) {

                        }
                    });
                } else if (balancer.equals("HDVB")) {
                    newUri = HDVB.getFileSerial(String.valueOf(mediaItem.localConfiguration.customCacheKey));
                    Bundle bundle = new Bundle();
                    bundle.putString("m3u8", newUri);
                    Message msg = Message.obtain();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }

            }
        });
        thread.start();

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {

            playbackPositionManager.savePositionEpisode(
                    epData.getFilmInfo().getKinopoiskId(),
                    exoPlayer.getCurrentMediaItemIndex(),
                    epData.getIndexSeason(),
                    epData.getIndexTranslation(),
                    epData.getIndexQuality(),
                    exoPlayer.getCurrentPosition(),
                    epData.getBalancer());

            exoPlayer.stop();
            exoPlayer.release();
        }

        if (torrentStream != null) {
            torrentStream.stopStream();
        }

    }

    private void appMetrica() {
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder("1945eb04-4fda-4a26-9fcc-4d36e0f34551").build();
        // Initializing the AppMetrica SDK.
        AppMetrica.activate(this, config);
        sendEventMetrica();
    }

    private void sendEventMetrica() {
        // С параметрами (например, название фильма и жанр)
        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put("movie_id", epData.getFilmInfo().getKinopoiskId());
        eventParams.put("title", epData.getFilmInfo().getNameRu());
        eventParams.put("genre", epData.getFilmInfo().getGenres());
        eventParams.put("timestamp", System.currentTimeMillis());
        AppMetrica.reportEvent("Старт Просмотра фильма", eventParams);
    }
}
