package com.alaka_ala.florafilm.ui.fragments.imgViewer;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentImageViewerBinding;
import com.alaka_ala.florafilm.ui.fragments.imgViewer.vpAdapter.ViewPagerImageAdapter;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.KinopoiskAPI;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.KinopoiskSiteParser;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.ItemFilmImage;
import com.alaka_ala.florafilm.ui.util.listeners.MyRecyclerViewScrollListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ImageViewerFragment extends Fragment {
    private ActionBar actionBar;
    private ViewPager2 vp2Images;
    private ViewPagerImageAdapter adapter;
    private FragmentImageViewerBinding binding;
    private int currentPage = 0;
    private KinopoiskAPI kinopoiskAPI;
    private List<ItemFilmImage.Item> items = new ArrayList<>();

    private int indexType = 0;
    private final List<String> types = KinopoiskAPI.ImagesTypeConstant.getListImageTypes();
    private KinopoiskSiteParser kinopoiskSiteParser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        assert getArguments() != null;
        setHasOptionsMenu(true);
        int kinopoisk_id = getArguments().getInt("kinopoisk_id");
        int staffId = getArguments().getInt("staffId");

        binding = FragmentImageViewerBinding.inflate(inflater, container, false);
        kinopoiskAPI = new KinopoiskAPI(getResources().getString(R.string.api_key_kinopoisk));
        kinopoiskSiteParser = new KinopoiskSiteParser();
        vp2Images = binding.vp2Images;
        adapter = new ViewPagerImageAdapter(getChildFragmentManager(), getLifecycle());
        vp2Images.setAdapter(adapter);
        vp2Images.setOffscreenPageLimit(3);
        RecyclerView recyclerView = (RecyclerView) vp2Images.getChildAt(0);

        vp2Images.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                // position:
                // 0 — текущая страница (центральная)
                // от -1 до 0 — страницы слева, от 0 до 1 — справа

                float minScale = 0.85f;

                if (position < -1 || position > 1) {
                    // Скрываем страницы, которые далеко за пределами экрана
                    page.setAlpha(0f);
                    page.setScaleX(minScale);
                    page.setScaleY(minScale);
                } else if (position <= 0) {
                    // Слева или центральная страница
                    float scaleFactor = minScale + (1 - minScale) * (1 + position);
                    page.setScaleX(scaleFactor);
                    page.setScaleY(scaleFactor);
                    page.setAlpha(1f);
                    page.setTranslationX(0);
                } else {
                    // Справа
                    float scaleFactor = minScale + (1 - minScale) * (1 - position);
                    page.setScaleX(scaleFactor);
                    page.setScaleY(scaleFactor);
                    page.setAlpha(1f);
                    page.setTranslationX(0);
                }
            }
        });
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        recyclerView.addOnScrollListener(new MyRecyclerViewScrollListener(MyRecyclerViewScrollListener.HORIZONTAL) {
            @Override
            public void onStart() {

            }

            @Override
            public void onEnd() {
                if (kinopoisk_id != 0) {
                    if (KinopoiskAPI.ImagesTypeConstant.getListImageTypes().size() > indexType) {
                        loadDataFilm(kinopoisk_id, KinopoiskAPI.ImagesTypeConstant.getListImageTypes().get(indexType));
                    }
                } else if (staffId != 0) {
                    loadDataStaff(staffId);
                }
            }
        });

        if (kinopoisk_id != 0) {
            loadDataFilm(kinopoisk_id, types.get(indexType));
        } else if (staffId != 0){
            loadDataStaff(staffId);
        }


        return binding.getRoot();
    }

    private void loadDataStaff(int staffId) {
        kinopoiskSiteParser.getStaffPhoto(binding.webView, staffId, new KinopoiskSiteParser.CallbackStaffPhoto() {
            @Override
            public void onSuccess(List<String> listImg) {
                ItemFilmImage itemFilmImage = new ItemFilmImage();
                itemFilmImage.setTotal(listImg.size());
                itemFilmImage.setTotalPages(1);
                List<ItemFilmImage.Item> items = new ArrayList<>();
                for (String url : listImg) {
                    ItemFilmImage.Item item = new ItemFilmImage.Item();
                    item.setImageUrl(url);
                    item.setPreviewUrl(url);
                    items.add(item);
                }
                itemFilmImage.setItems(items);
                adapter.addItems(items);
                int itemCount = adapter.getItemCount();
                adapter.notifyItemRangeInserted(itemCount, items.size());
            }

            @Override
            public void onFailure(IOException e) {

            }
        });
    }


    private void loadDataFilm(int kinopoiskId, String type) {
        actionBar.setTitle(getTypeTitle(indexType));
        currentPage++; // инкремент явно, а не в аргументе
        kinopoiskAPI.getListImage(kinopoiskId, currentPage, type, new KinopoiskAPI.RequestCallbackImagesFilm() {
            @Override
            public void onSuccessImagesFilm(ItemFilmImage itemFilmImage) {
                if (itemFilmImage == null || getContext() == null) return;

                List<ItemFilmImage.Item> items = itemFilmImage.getItems();
                if (items == null || items.isEmpty()) {
                    loadNextTypeIfExists(kinopoiskId);
                    return;
                }

                int startPos = adapter.getItemCount();
                adapter.addItems(items);
                adapter.notifyItemRangeInserted(startPos, items.size());

                actionBar.setTitle(getTypeTitle(indexType));
            }

            @Override
            public void onFailureImagesFilm(IOException e) {
                if (getContext() == null) return;
                new MaterialAlertDialogBuilder(getContext())
                        .setMessage(e != null ? e.getMessage() : "Неизвестная ошибка")
                        .show();
            }

            @Override
            public void finish() {
                // если здесь будет нужна логика завершения — можно добавить
            }
        });
    }

    private void loadNextTypeIfExists(int kinopoiskId) {
        if (KinopoiskAPI.ImagesTypeConstant.getListImageTypes().size() > (indexType + 1)) {
            currentPage = 0;
            indexType++;
            loadDataFilm(kinopoiskId, types.get(indexType));
        }
    }

    private String getTypeTitle(int indexType) {
        switch (KinopoiskAPI.ImagesTypeConstant.getListImageTypes().get(indexType)) {
            case KinopoiskAPI.ImagesTypeConstant.TYPE_POSTER:
                return "Постеры";
            case KinopoiskAPI.ImagesTypeConstant.TYPE_SHOOTING:
                return "Со съемок";
            case KinopoiskAPI.ImagesTypeConstant.TYPE_STILL:
                return "Снимки";
            case KinopoiskAPI.ImagesTypeConstant.TYPE_FAN_ART:
                return "Фанарты";
            case KinopoiskAPI.ImagesTypeConstant.TYPE_CONCEPT:
                return "Концепты";
            case KinopoiskAPI.ImagesTypeConstant.TYPE_COVER:
                return "Обложки";
            case KinopoiskAPI.ImagesTypeConstant.TYPE_PROMO:
                return "Промо";
            case KinopoiskAPI.ImagesTypeConstant.TYPE_WALLPAPER:
                return "Обои";
            case KinopoiskAPI.ImagesTypeConstant.TYPE_SCREENSHOT:
                return "Скриншоты";
        }
        return "Просмотр";
    }


}