package com.alaka_ala.florafilm.ui.fragments.film.common;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class SmoothScalePageTransformer implements ViewPager2.PageTransformer {

    private static final float MIN_SCALE = 0.90f;

    @Override
    public void transformPage(@NonNull View page, float position) {
        if (position < -1 || position > 1) {
            // Вне видимости
            page.setScaleX(MIN_SCALE);
            page.setScaleY(MIN_SCALE);
            page.setAlpha(0.9f);
        } else {
            // Интерполяция масштаба: чем ближе к центру — тем больше scale
            float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);
            page.setAlpha(0.9f + 0.1f * (1 - Math.abs(position)));
        }
    }
}
