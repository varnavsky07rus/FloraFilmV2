package com.alaka_ala.florafilm.ui.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.ActivityPlayerExoBinding;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.Util;

public class PlayerExoActivity extends AppCompatActivity {
    private ActivityPlayerExoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerExoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        String url = "http://localhost:8080/";
        PlayerView playerExoView = binding.playerExoView;
        // Настройка ExoPlayer для воспроизведения видео
        ExoPlayer exoPlayer = new ExoPlayer.Builder(this).build();
        playerExoView.setPlayer(exoPlayer);
        //DefaultHttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory().setUserAgent(Util.getUserAgent(this, "TorrentStreamer"));
        //HlsMediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri("http://localhost:8080"));
        //exoPlayer.setMediaSource(mediaSource);

        MediaItem mediaItem = MediaItem.fromUri(url);
        exoPlayer.addMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.play();
        System.out.println("ExoPlayer started playing video.");




    }
}