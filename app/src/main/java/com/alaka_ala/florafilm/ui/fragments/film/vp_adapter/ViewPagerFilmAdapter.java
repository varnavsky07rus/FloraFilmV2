package com.alaka_ala.florafilm.ui.fragments.film.vp_adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.alaka_ala.florafilm.ui.fragments.film.vp_fragments.DescriptionFragment;
import com.alaka_ala.florafilm.ui.fragments.film.vp_fragments.TorrentFilmFragment;
import com.alaka_ala.florafilm.ui.fragments.film.vp_fragments.VideoFilmFragment;

public class ViewPagerFilmAdapter extends FragmentStateAdapter {
    private int countFragments = 3;

    public ViewPagerFilmAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);

    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new DescriptionFragment();
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
