package com.alaka_ala.florafilm.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.Lottie;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieConfig;
import com.airbnb.lottie.LottieDrawable;
import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.ActivitySplahBinding;
import com.alaka_ala.florafilm.ui.util.api.BanCheker;
import com.alaka_ala.florafilm.ui.util.updater.AppUpdater;
import com.google.android.material.chip.Chip;

public class SplahActivity extends AppCompatActivity {
    private ActivitySplahBinding binding;
    private AppUpdater appUpdater;

    private Chip chipSkipFlashActivity;
    private LinearLayout linearLayoutSkipFlashActivity;

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


        LottieAnimationView lottieAnimationView = binding.lottieAnimLoading;
        lottieAnimationView.setRepeatMode(LottieDrawable.REVERSE);
        lottieAnimationView.setAnimation(R.raw.loading4);

        // Проверка обновления приложения
        appUpdater = new AppUpdater(SplahActivity.this, true);
        appUpdater.checkForUpdate(new AppUpdater.CallbackCheckUpdate() {
            @Override
            public void onFinish(boolean isUpdateAvailable) {
                // Загружаем сразу список заблокированных фильмов и сохраняем в кэш
                BanCheker banCheker = new BanCheker(SplahActivity.this);
                banCheker.loadList(new BanCheker.LoaderCallback() {
                    @Override
                    public void onFinish() {
                        // Запускаем Основное активити
                        skip();
                    }
                });
            }
        });




        linearLayoutSkipFlashActivity = binding.linearLayoutSkipFlashActivity;
        chipSkipFlashActivity = binding.chipSkipFlashActivity;


        skipFlashActivityHandler();


        /*lottieAnimationView.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

            }
        });*/


    }

    private void skip() {
        if (SplahActivity.this.isFinishing()) {
            return;
        }
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        if (!SplahActivity.this.isFinishing()) {
            SplahActivity.this.finish();
        }
    }

    private void skipFlashActivityHandler() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    linearLayoutSkipFlashActivity.setVisibility(LinearLayout.VISIBLE);
                    Animation animVisible = AnimationUtils.loadAnimation(SplahActivity.this, R.anim.anim_visible_ui);
                    linearLayoutSkipFlashActivity.startAnimation(animVisible);
                    animVisible.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            chipSkipFlashActivity.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    skip();
                                }
                            });
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                }
            }
        }, 10000);
    }
}