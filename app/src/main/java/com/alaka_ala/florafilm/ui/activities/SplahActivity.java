package com.alaka_ala.florafilm.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.ActivitySplahBinding;
import com.alaka_ala.florafilm.ui.fragments.settings.SettingsUtils;
import com.alaka_ala.florafilm.ui.util.updater.AppUpdater;

public class SplahActivity extends AppCompatActivity {
    private ActivitySplahBinding binding;
    private AppUpdater appUpdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplahBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        appUpdater = new AppUpdater(this, true);
        appUpdater.checkForUpdate();
        boolean isAvaibleUpdate = appUpdater.isAvailableUpdate();


        LottieAnimationView lottieAnimationView = binding.lottieAnimLoading;
        lottieAnimationView.setAnimation(R.raw.loading3);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplahActivity.this, MainActivity.class);
                startActivity(intent);
                SplahActivity.this.finish();
            }
        }, 3000);



        //lottieAnimationView.setRepeatMode(LottieDrawable.REVERSE);
        //lottieAnimationView.setRepeatCount(1);
        //lottieAnimationView.setSpeed(1f);
        //lottieAnimationView.playAnimation();













    }
}