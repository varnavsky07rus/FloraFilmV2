package com.alaka_ala.florafilm.ui.fragments.film;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentMainFilmBinding;
import com.alaka_ala.florafilm.ui.fragments.film.view_model.MainFilmViewModel;
import com.alaka_ala.florafilm.ui.fragments.film.vp_adapter.ViewPagerFilmAdapter;
import com.alaka_ala.florafilm.ui.util.local.FavoriteMoviesManager;
import com.google.android.material.tabs.TabLayout;

public class MainFilmFragment extends Fragment {
    private FragmentMainFilmBinding binding;
    private ViewPager2 vpFilm;
    private TabLayout tabLayoutFilm;
    private MainFilmViewModel mainFilmViewModel;
    private static ViewPagerListener viewPagerListener;

    private int kinopoisk_id;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMainFilmBinding.inflate(inflater, container, false);
        mainFilmViewModel = new ViewModelProvider(getActivity()).get(MainFilmViewModel.class);
        kinopoisk_id = getArguments().getInt("kinopoisk_id");
        mainFilmViewModel.setKinopoiskId(kinopoisk_id);
        vpFilm = binding.vpFilm;
        tabLayoutFilm = binding.tabLayoutFilm;

        ViewPagerFilmAdapter viewPagerFilmAdapter = new ViewPagerFilmAdapter(getChildFragmentManager(), getLifecycle());
        vpFilm.setAdapter(viewPagerFilmAdapter);

        vpFilm.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                onTransitionListener();
                tabLayoutFilm.setScrollPosition(position, positionOffset, true);
                requireActivity().invalidateOptionsMenu();
            }
        });
        onTransitionListener();

        vpFilm.setOffscreenPageLimit(3);
        tabLayoutFilm.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                vpFilm.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        return binding.getRoot();
    }

    private void onTransitionListener() {
        if (viewPagerListener != null) {
            viewPagerListener.onTransition(vpFilm.getCurrentItem());
        }
    }

    public interface ViewPagerListener {
        void onTransition(int currentPage);
    }


    public static void setViewPagerListener(ViewPagerListener viewPagerListener) {
        MainFilmFragment.viewPagerListener = viewPagerListener;
    }





}