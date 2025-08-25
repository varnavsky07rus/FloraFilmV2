package com.alaka_ala.florafilm.ui.fragments.film.vp_adapter;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class DepthPageTransformer implements ViewPager2.PageTransformer {
    private static final float MIN_SCALE = 0.75f;

    public void transformPage(@NonNull View page, float position) {
        int pageWidth = page.getWidth();

        if (position < -1) { // [-Infinity,-1)
            // Эта страница находится далеко слева от экрана.
            page.setAlpha(0f);

        } else if (position <= 0) { // [-1,0]
            // Используется при движении страницы влево.
            page.setAlpha(1f);
            page.setTranslationX(0f);
            page.setTranslationZ(0f);
            page.setScaleX(1f);
            page.setScaleY(1f);

        } else if (position <= 1) { // (0,1]
            // Уменьшаем прозрачность уходящей страницы (от 1 до 0).
            page.setAlpha(1 - position);

            // Смещаем страницу против направления скролла (эффект параллакса).
            page.setTranslationX(pageWidth * -position);
            // Отправляем страницу назад, чтобы следующая была видна поверх нее.
            page.setTranslationZ(-1f);

            // Уменьшаем страницу (между MIN_SCALE и 1).
            float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);

        } else { // (1,+Infinity]
            // Эта страница находится далеко справа от экрана.
            page.setAlpha(0f);
        }
    }
}
