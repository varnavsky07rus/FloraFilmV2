package com.alaka_ala.florafilm.ui.fragments.film;

import android.animation.ValueAnimator;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alaka_ala.florafilm.databinding.FragmentMainFilmBinding;
import com.alaka_ala.florafilm.ui.fragments.film.view_model.MainFilmViewModel;
import com.alaka_ala.florafilm.ui.fragments.film.vp_adapter.CardFlipPageTransformer;
import com.alaka_ala.florafilm.ui.fragments.film.vp_adapter.ViewPagerFilmAdapter;
import com.alaka_ala.florafilm.ui.fragments.film.vp_fragments.VideoFilmFragment;
import com.alaka_ala.florafilm.ui.fragments.settings.SettingsUtils;
import com.alaka_ala.florafilm.ui.util.api.EPData;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.HashMap;
import java.util.Map;

/**
 * Фрагмент, отображающий подробную информацию о фильме с вкладками.
 */
public class MainFilmFragment extends Fragment {
    private FragmentMainFilmBinding binding;
    private ViewPager2 vpFilm;
    private static ViewPagerListener viewPagerListener;
    private static ViewPagerSetterPage viewPagerSetterPage;

    private Map<String, Boolean> isLoadingMap = new HashMap<>();

    private MainFilmViewModel mainFilmViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMainFilmBinding.inflate(inflater, container, false);
        mainFilmViewModel = new ViewModelProvider(getActivity()).get(MainFilmViewModel.class);
        int kinopoisk_id = getArguments().getInt("kinopoisk_id");
        mainFilmViewModel.setKinopoiskId(kinopoisk_id);
        vpFilm = binding.vpFilm;
        TabLayout tabLayoutFilm = binding.tabLayoutFilm;

        ViewPagerFilmAdapter viewPagerFilmAdapter = new ViewPagerFilmAdapter(getChildFragmentManager(), getLifecycle(), getContext());
        vpFilm.setAdapter(viewPagerFilmAdapter);

        if (SettingsUtils.getParamScrollPageEffect(getContext())) {
            vpFilm.setPageTransformer(new CardFlipPageTransformer());
        }

        String[] tabTitles = {"Описание", "Видео", "Торрент"};
        new TabLayoutMediator(tabLayoutFilm, vpFilm, (tab, position) -> {
            if (position < tabTitles.length) {
                if (tabTitles[position].equals("Торрент") && !SettingsUtils.getParamSearchTorrent(getContext())) {
                    tab.view.setVisibility(View.GONE);
                } else {
                    tab.setText(tabTitles[position]);
                }
            }
        }).attach();

        vpFilm.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                onTransitionListener();
                requireActivity().invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
        onTransitionListener();

        viewPagerSetterPage = page -> vpFilm.setCurrentItem(page);

        vpFilm.setOffscreenPageLimit(3);

        VideoFilmFragment.addCallbackLoaderData(new VideoFilmFragment.CallbackLoaderData() {
            @Override
            public void successHDVBFilm(EPData.Film film) {
                mainFilmViewModel.getFilmMutableLiveDataHDVB().setValue(film);
                isLoadingMap.put("HDVB", false);
                checkAndHideProgressBar();
            }

            @Override
            public void successVibixFilm(EPData.Film film) {
                mainFilmViewModel.getFilmMutableLiveDataVibix().setValue(film);
                isLoadingMap.put("VIBIX", false);
                checkAndHideProgressBar();
            }

            @Override
            public void successHDVBSerial(EPData.Serial serial) {
                mainFilmViewModel.getSerialMutableLiveDataHDVB().setValue(serial);
                isLoadingMap.put("HDVB", false);
                checkAndHideProgressBar();
            }

            @Override
            public void successVibixSerial(EPData.Serial serial) {
                mainFilmViewModel.getSerialMutableLiveDataVibix().setValue(serial);
                isLoadingMap.put("VIBIX", false);
                checkAndHideProgressBar();
            }

            @Override
            public void successLumexFilm(EPData.Film film) {
                mainFilmViewModel.getFilmMutableLiveDataLUMEX().setValue(film);
                isLoadingMap.put("LUMEX", false);
                checkAndHideProgressBar();
            }

            @Override
            public void successLumexSerial(EPData.Serial serial) {
                mainFilmViewModel.getSerialMutableLiveDataLUMEX().setValue(serial);
                isLoadingMap.put("LUMEX", false);
                checkAndHideProgressBar();
            }

            @Override
            public void error(String balancer, String err) {
                isLoadingMap.put(balancer, false);
                checkAndHideProgressBar();
            }
        });

        initParams();
        showProgressbarLoading();
        checkCachedData();

        return binding.getRoot();
    }

    private void checkCachedData() {
        if (mainFilmViewModel.getFilmMutableLiveDataHDVB().getValue() != null || mainFilmViewModel.getSerialMutableLiveDataHDVB().getValue() != null) {
            isLoadingMap.put("HDVB", false);
        }
        if (mainFilmViewModel.getFilmMutableLiveDataVibix().getValue() != null || mainFilmViewModel.getSerialMutableLiveDataVibix().getValue() != null) {
            isLoadingMap.put("VIBIX", false);
        }
        if (mainFilmViewModel.getFilmMutableLiveDataLUMEX().getValue() != null || mainFilmViewModel.getSerialMutableLiveDataLUMEX().getValue() != null) {
            isLoadingMap.put("LUMEX", false);
        }
        checkAndHideProgressBar();
    }

    private void initParams() {
        if (getContext() == null) return;
        isLoadingMap.put("LUMEX", SettingsUtils.getParamSearchLumex(getContext()));
        isLoadingMap.put("HDVB", SettingsUtils.getParamSeeachHDVB(getContext()));
        isLoadingMap.put("VIBIX", SettingsUtils.getParamSearchVIBIX(getContext()));
    }

    private void showProgressbarLoading() {
        new Handler().postDelayed(() -> {
            boolean anyLoading = false;
            for (Boolean isLoading : isLoadingMap.values()) {
                if (isLoading) {
                    anyLoading = true;
                    break;
                }
            }
            if (anyLoading) {
                showProgressbarLoading();
            } else {
                binding.progressBar4.setVisibility(View.INVISIBLE);
            }
        }, 500);
    }

    private void checkAndHideProgressBar() {
        boolean allLoaded = true;
        for (Map.Entry<String, Boolean> entry : isLoadingMap.entrySet()) {
            if (entry.getValue()) {
                allLoaded = false;
                break;
            }
        }
        if (allLoaded) {
            binding.progressBar4.setVisibility(View.INVISIBLE);
        }
    }

    private ValueAnimator scaleAnimator;

    private void startScaleAnimation(ViewPager2 viewPager2, float fromScale, float toScale, long duration) {
        if (scaleAnimator != null && scaleAnimator.isRunning()) {
            scaleAnimator.cancel();
        }

        scaleAnimator = ValueAnimator.ofFloat(fromScale, toScale);
        scaleAnimator.setDuration(duration);
        scaleAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            for (int i = 0; i < viewPager2.getChildCount(); i++) {
                View child = viewPager2.getChildAt(i);
                child.setScaleX(scale);
                child.setScaleY(scale);
            }
        });
        scaleAnimator.start();
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