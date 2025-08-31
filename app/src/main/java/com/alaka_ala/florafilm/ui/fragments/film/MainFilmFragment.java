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

/**
 * Фрагмент, отображающий подробную информацию о фильме с вкладками.
 */
public class MainFilmFragment extends Fragment {
    private FragmentMainFilmBinding binding;
    private ViewPager2 vpFilm;
    private TabLayout tabLayoutFilm;
    private MainFilmViewModel mainFilmViewModel;
    private static ViewPagerListener viewPagerListener;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMainFilmBinding.inflate(inflater, container, false);
        mainFilmViewModel = new ViewModelProvider(getActivity()).get(MainFilmViewModel.class);
        int kinopoisk_id = getArguments().getInt("kinopoisk_id");
        mainFilmViewModel.setKinopoiskId(kinopoisk_id);
        vpFilm = binding.vpFilm;
        tabLayoutFilm = binding.tabLayoutFilm;

        addTabLayout();
        
        ViewPagerFilmAdapter viewPagerFilmAdapter = new ViewPagerFilmAdapter(getChildFragmentManager(), getLifecycle(), getContext());
        vpFilm.setAdapter(viewPagerFilmAdapter);

        if (SettingsUtils.getParamScrollPageEffect(getContext())) {
            // Добавляем эффекты прокрутки страниц
            vpFilm.setPageTransformer(new DepthPageTransformer());
        }

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

        // Максимальное кол-во фрагментов хранящихся в памяти
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

    /**
     * Добавляет вкладки в TabLayout.
     */
    private void addTabLayout() {
        tabLayoutFilm.addTab(tabLayoutFilm.newTab().setText("Описание"));
        tabLayoutFilm.addTab(tabLayoutFilm.newTab().setText("Видео"));
        // Если вкл\откл поиск по торрентам, то нужно показать или скрыть вкладку с торрентами
        if (SettingsUtils.getParamSearchTorrent(getContext())) {
            tabLayoutFilm.addTab(tabLayoutFilm.newTab().setText("Торрент"));
        }
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
}