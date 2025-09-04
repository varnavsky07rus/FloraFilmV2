package com.alaka_ala.florafilm.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import com.alaka_ala.florafilm.ui.util.api.BanCheker;
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

        // Проверка обновления приложения
        appUpdater = new AppUpdater(this, true);
        appUpdater.checkForUpdate();
        boolean isAvaibleUpdate = appUpdater.isAvailableUpdate();
        LottieAnimationView lottieAnimationView = binding.lottieAnimLoading;
        lottieAnimationView.setAnimation(R.raw.loading4);



        // Загружаем сразу список заблокированных фильмов и сохраняем в кэш
        BanCheker banCheker = new BanCheker(this);
        banCheker.loadList(new BanCheker.LoaderCallback() {
            @Override
            public void onFinish() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                if (!SplahActivity.this.isFinishing()) {
                    SplahActivity.this.finish();
                }
            }
        });


        /*lottieAnimationView.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

            }
        });*/


    }
}