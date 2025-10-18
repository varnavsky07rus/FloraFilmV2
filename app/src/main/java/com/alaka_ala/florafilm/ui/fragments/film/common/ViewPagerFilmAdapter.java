package com.alaka_ala.florafilm.ui.fragments.film.common;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.alaka_ala.florafilm.ui.fragments.film.vp_fragments.DescriptionDeptFragment;
import com.alaka_ala.florafilm.ui.fragments.film.vp_fragments.DescriptionFragment;
import com.alaka_ala.florafilm.ui.fragments.film.vp_fragments.TorrentsFilmFragment;
import com.alaka_ala.florafilm.ui.fragments.film.vp_fragments.VideoFilmFragment;
import com.alaka_ala.florafilm.ui.fragments.settings.SettingsUtils;

public class ViewPagerFilmAdapter extends FragmentStateAdapter {

    private int countFragments;
    private boolean deptDescriptionLayout;

    public ViewPagerFilmAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, Context context, int countItems) {
        super(fragmentManager, lifecycle);
        countFragments = countItems;
        deptDescriptionLayout = SettingsUtils.getParamLayoutDescriptionFilm(context);
    }

    public ViewPagerFilmAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, Context context) {
        super(fragmentManager, lifecycle);
        countFragments = 3;
        deptDescriptionLayout = SettingsUtils.getParamLayoutDescriptionFilm(context);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return deptDescriptionLayout ? new DescriptionFragment() : new DescriptionDeptFragment();
        } else if (position == 1) {
            return new VideoFilmFragment();
        } else if (position == 2) {
            return new TorrentsFilmFragment();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return countFragments;
    }
}
