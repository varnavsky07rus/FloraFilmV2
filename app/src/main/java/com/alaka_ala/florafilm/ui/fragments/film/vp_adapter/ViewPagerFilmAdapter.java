package com.alaka_ala.florafilm.ui.fragments.film.vp_adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.alaka_ala.florafilm.ui.fragments.film.vp_fragments.DescriptionDeptFragment;
import com.alaka_ala.florafilm.ui.fragments.film.vp_fragments.DescriptionFragment;
import com.alaka_ala.florafilm.ui.fragments.film.vp_fragments.TorrentFilmFragment;
import com.alaka_ala.florafilm.ui.fragments.film.vp_fragments.VideoFilmFragment;
import com.alaka_ala.florafilm.ui.fragments.settings.SettingsUtils;

public class ViewPagerFilmAdapter extends FragmentStateAdapter {
    private int countFragments;
    private boolean deptDescriptionLayout;

    public ViewPagerFilmAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, Context context) {
        super(fragmentManager, lifecycle);
        if (SettingsUtils.getParamSearchTorrent(context)) {
            countFragments = 3;
        } else {
            countFragments = 2;
        }
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
            return new TorrentFilmFragment();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return countFragments;
    }
}
