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
import com.alaka_ala.florafilm.ui.fragments.film.vp_adapter.DepthPageTransformer;
import com.alaka_ala.florafilm.ui.fragments.film.vp_adapter.ViewPagerFilmAdapter;
import com.alaka_ala.florafilm.ui.fragments.resumeView.ResumeBottomSheetFragment;
import com.alaka_ala.florafilm.ui.fragments.settings.SettingsUtils;
import com.alaka_ala.florafilm.ui.util.local.FavoriteMoviesManager;
import com.alaka_ala.florafilm.ui.util.local.ResumeLastMovie;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Фрагмент, отображающий подробную информацию о фильме с вкладками.
 */
public class MainFilmFragment extends Fragment {
    private FragmentMainFilmBinding binding;
    private ViewPager2 vpFilm;
    private static ViewPagerListener viewPagerListener;
    private static ViewPagerSetterPage viewPagerSetterPage;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMainFilmBinding.inflate(inflater, container, false);
        MainFilmViewModel mainFilmViewModel = new ViewModelProvider(getActivity()).get(MainFilmViewModel.class);
        int kinopoisk_id = getArguments().getInt("kinopoisk_id");
        mainFilmViewModel.setKinopoiskId(kinopoisk_id);
        vpFilm = binding.vpFilm;
        TabLayout tabLayoutFilm = binding.tabLayoutFilm;

        ViewPagerFilmAdapter viewPagerFilmAdapter = new ViewPagerFilmAdapter(getChildFragmentManager(), getLifecycle(), getContext());
        vpFilm.setAdapter(viewPagerFilmAdapter);

        if (SettingsUtils.getParamScrollPageEffect(getContext())) {
            // Добавляем эффекты прокрутки страниц
            vpFilm.setPageTransformer(new DepthPageTransformer());
        }

        new TabLayoutMediator(tabLayoutFilm, vpFilm, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Описание");
                    break;
                case 1:
                    tab.setText("Видео");
                    break;
                case 2:
                    if (SettingsUtils.getParamSearchTorrent(getContext())) {
                        tab.setText("Торрент");
                    }
                    break;
            }
        }).attach();

        vpFilm.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                onTransitionListener();
                requireActivity().invalidateOptionsMenu();
            }
        });
        onTransitionListener();

        viewPagerSetterPage = new ViewPagerSetterPage() {
            @Override
            public void setPage(int page) {
                vpFilm.setCurrentItem(page);
            }
        };
        

        // Максимальное кол-во фрагментов хранящихся в памяти
        vpFilm.setOffscreenPageLimit(3);

        return binding.getRoot();
    }

    /**
     * Уведомляет слушателя о смене страницы.
     */
    private void onTransitionListener() {
        if (viewPagerListener != null) {
            viewPagerListener.onTransition(vpFilm.getCurrentItem());
        }
    }

    /**
     * Интерфейс для прослушивания переходов ViewPager.
     * Устанавливается в дочернем фрагменте где необходимо получить информацию о текущей странице.
     */
    public interface ViewPagerListener {
        /**
         * Вызывается при смене страницы.
         *
         * @param currentPage Текущая страница.
         */
        void onTransition(int currentPage);
    }


    /**
     * Устанавливает слушателя для ViewPager.
     *
     * @param viewPagerListener Слушатель.
     */
    public static void setViewPagerListener(ViewPagerListener viewPagerListener) {
        MainFilmFragment.viewPagerListener = viewPagerListener;
    }

    private interface ViewPagerSetterPage {
        void setPage(int page);
    }

    public static void setTransition(int page){
        if (viewPagerSetterPage == null) return;
        viewPagerSetterPage.setPage(page);
    }
    


}