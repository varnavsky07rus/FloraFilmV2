package com.alaka_ala.florafilm.ui.fragments.film.common;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class CardFlipPageTransformer implements ViewPager2.PageTransformer {

    @Override
    public void transformPage(@NonNull View page, float position) {
        page.setCameraDistance(20000); // Увеличиваем дистанцию до "камеры", чтобы избежать искажений

        if (position < -1) { // Левая страница вне экрана
            page.setAlpha(0f);

        } else if (position <= 0) { // Страница уходит влево
            page.setAlpha(1f);
            page.setRotationY(90 * Math.abs(position));
            page.setPivotX(page.getWidth());
            page.setPivotY(page.getHeight() / 2f);

        } else if (position <= 1) { // Страница приходит справа
            page.setAlpha(1f);
            page.setRotationY(-90 * Math.abs(position));
            page.setPivotX(0f);
            page.setPivotY(page.getHeight() / 2f);

        } else { // Правая страница вне экрана
            page.setAlpha(0f);
        }
    }
}

