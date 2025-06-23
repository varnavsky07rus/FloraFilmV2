package com.alaka_ala.florafilm.ui.util.api.vibix;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.ui.activities.PlayerExoActivity;
import com.alaka_ala.florafilm.ui.util.api.EPData;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.ItemFilmInfo;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class VibixSelector {

    public interface SelectorListener {
        void onClick(int indexTranslation, int indexSeasons, int indexEpisode, int indexQuality);
    }

    private SelectorListener selectorListener;

    public void setSelectorListener(SelectorListener selectorListener) {
        this.selectorListener = selectorListener;
    }

    private final LinearLayout  root;
    private EPData.Film film = null;
    private EPData.Serial serial = null;
    private final String CURRENT_TYPE_CONTENT;
    private final ItemFilmInfo filmInfo;

    public VibixSelector(LinearLayout root, EPData.Film film, ItemFilmInfo filmInfo) {
        this.filmInfo = filmInfo;
        CURRENT_TYPE_CONTENT = "FILM";
        this.root = root;
        this.film = film;
    }

    public VibixSelector(LinearLayout root, EPData.Serial serial, ItemFilmInfo filmInfo) {
        this.filmInfo = filmInfo;
        CURRENT_TYPE_CONTENT = "SERIAL";
        this.root = root;
        this.serial = serial;
    }

    public EPData.Film getFilm() {
        return film;
    }

    public EPData.Serial getSerial() {
        return serial;
    }

    public String getCurrentTypeContent() {
        return CURRENT_TYPE_CONTENT;
    }

    public LinearLayout getRoot() {
        return root;
    }

    private Activity activity;
    public void buildSelector(Activity activity) {
        this.activity = activity;
        if (CURRENT_TYPE_CONTENT.equals("FILM")) {
            createFilmSelector();
        } else {
            createSerialSelector();
        }
    }

    // Создание фильма
    @SuppressLint("MissingInflatedId")
    private void createFilmSelector() {
        if (film == null) return;

        for (int i = 0; i < film.getTranslations().size(); i++) {
            String titleTranslation = film.getTranslations().get(i).getTitle() + " | [VIBIX]";
            // Корневой элемент View
            View viewTranslation = LayoutInflater.from(root.getContext()).inflate(R.layout.selector_film_item_1, root, false);
            // Задаем название перевода
            TextView textViewTitleFolder = viewTranslation.findViewById(R.id.textViewTitleFolder);
            // Данный LinearLayout нужен для отработки кликов
            LinearLayout linearLayoutTitleClick = viewTranslation.findViewById(R.id.linearLayoutTitleClick);
            linearLayoutTitleClick.setId(i);
            // В данный LinearLayout помещаем все элементы (файлы)
            LinearLayout linearLayoutFf = viewTranslation.findViewById(R.id.linearLayoutFf);

            textViewTitleFolder.setText(titleTranslation);
            linearLayoutTitleClick.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View vTranslation) {
                    if (linearLayoutFf.getChildCount() > 0) {
                        linearLayoutFf.removeAllViews();
                        return;
                    }
                    int sizeVideos = film.getTranslations().get(vTranslation.getId()).getVideoData().size();
                    for (int i = 0; i < sizeVideos; i++) {
                        String titleQuality = film.getTranslations().get(vTranslation.getId()).getVideoData().get(i).getKey();
                        String urlVideo = film.getTranslations().get(vTranslation.getId()).getVideoData().get(i).getValue();

                        // Корневой элемент View
                        View viewVideo = LayoutInflater.from(root.getContext()).inflate(R.layout.selector_film_item_2, root, false);
                        // Задаем название качества
                        TextView textViewTitleFiles = viewVideo.findViewById(R.id.textViewTitleFiles);
                        textViewTitleFiles.setText(titleQuality);
                        // Данный LinearLayout нужен для отработки кликов
                        LinearLayout linearLayoutTitleClick2 = viewVideo.findViewById(R.id.linearLayoutTitleClick2);
                        linearLayoutTitleClick2.setId(i);
                        linearLayoutTitleClick2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View vQuality) {
                                /*Intent intent = new Intent(activity, PlayerWebActivity.class);
                                intent.putExtra("url", urlVideo);
                                intent.putExtra("poster", film.getPoster());
                                activity.startActivity(intent);*/

                                Intent intent = new Intent(activity, PlayerExoActivity.class);

                                EPData.Builder builderEPData = new EPData.Builder();
                                builderEPData.setFilm(film);
                                builderEPData.setIndexTranslation(vTranslation.getId());
                                builderEPData.setIndexQuality(vQuality.getId());
                                builderEPData.setBalancer("VIBIX");
                                builderEPData.setFilmInfo(filmInfo);
                                EPData film = builderEPData.build();

                                intent.putExtra("epData", film);
                                activity.startActivity(intent);


                            }
                        });

                        linearLayoutFf.addView(viewVideo);


                    }
                }
            });


            root.addView(viewTranslation);
        }
    }

    // Создание сериала
    private void createSerialSelector() {
        if (serial == null) return;

        View view = LayoutInflater.from(root.getContext()).inflate(R.layout.layout_film_files, root, false);
        String titleBalancer = "VIBIX";
        TextView txtBalancer = view.findViewById(R.id.textViewTitleBalancer);
        txtBalancer.setText(titleBalancer);
        root.addView(view);
        LinearLayout root = view.findViewById(R.id.linearLayoutRootGroups);
        LinearLayout title = view.findViewById(R.id.linearLayoutTitleFiles);

        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView arrow = view.findViewById(R.id.imageViewArrowFiles);

                arrow.animate().setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        root.setVisibility(root.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                        if (root.getVisibility() == View.VISIBLE) {
                            if (root.getChildCount() == 0) {
                                for (int i = 0; i < serial.getSeasons().size(); i++) {
                                    // Тут парсятся сезоны
                                    String titleSeason = serial.getSeasons().get(i).getTitle();
                                    View viewSeason = LayoutInflater.from(root.getContext()).inflate(R.layout.selector_film_item_1, root, false);
                                    LinearLayout linearLayoutRootEpisode = viewSeason.findViewById(R.id.linearLayoutFf);
                                    TextView textViewTitleFolder = viewSeason.findViewById(R.id.textViewTitleFolder);
                                    textViewTitleFolder.setText(titleSeason);
                                    LinearLayout linearLayoutTitleClick = viewSeason.findViewById(R.id.linearLayoutTitleClick);
                                    linearLayoutTitleClick.setId(i);
                                    linearLayoutTitleClick.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View vSeason) {
                                            if (linearLayoutRootEpisode.getChildCount() > 0) {
                                                linearLayoutRootEpisode.removeAllViews();
                                                return;
                                            }

                                            for (int j = 0; j < serial.getSeasons().get(vSeason.getId()).getEpisodes().size(); j++) {
                                                // Тут парсятся эпизоды
                                                String titleEpisode = serial.getSeasons().get(vSeason.getId()).getEpisodes().get(j).getTitle();
                                                View viewEpisode = LayoutInflater.from(root.getContext()).inflate(R.layout.selector_film_item_1, root, false);
                                                LinearLayout linearLayoutTranslations = viewEpisode.findViewById(R.id.linearLayoutFf);
                                                TextView textViewTitleFolder = viewEpisode.findViewById(R.id.textViewTitleFolder);
                                                textViewTitleFolder.setText(titleEpisode);
                                                LinearLayout linearLayoutTitleClick = viewEpisode.findViewById(R.id.linearLayoutTitleClick);
                                                linearLayoutTitleClick.setId(j);

                                                linearLayoutTitleClick.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View vEpisode) {
                                                        if (linearLayoutTranslations.getChildCount() > 0) {
                                                            linearLayoutTranslations.removeAllViews();
                                                            return;
                                                        }
                                                        for (int k = 0; k < serial.getSeasons().get(vSeason.getId()).getEpisodes().get(vEpisode.getId()).getTranslations().size(); k++) {
                                                            // Тут парсятся переводы
                                                            String titleTranslation = serial.getSeasons().get(vSeason.getId()).getEpisodes().get(vEpisode.getId()).getTranslations().get(k).getTitle();
                                                            View viewTranslation = LayoutInflater.from(root.getContext()).inflate(R.layout.selector_film_item_1, root, false);
                                                            LinearLayout linearLayoutVideos = viewTranslation.findViewById(R.id.linearLayoutFf);
                                                            TextView textViewTitleFolder = viewTranslation.findViewById(R.id.textViewTitleFolder);
                                                            textViewTitleFolder.setText(titleTranslation);
                                                            LinearLayout linearLayoutTitleClick = viewTranslation.findViewById(R.id.linearLayoutTitleClick);
                                                            linearLayoutTitleClick.setId(k);

                                                            linearLayoutTitleClick.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View vTranslation) {
                                                                    if (linearLayoutVideos.getChildCount() > 0) {
                                                                        linearLayoutVideos.removeAllViews();
                                                                        return;
                                                                    }
                                                                    for (int l = 0; l < serial.getSeasons().get(vSeason.getId()).getEpisodes().get(vEpisode.getId()).getTranslations().get(vTranslation.getId()).getVideoData().size(); l++) {
                                                                        // Тут парсятся видео файлы
                                                                        String titleQuality = serial.getSeasons().get(vSeason.getId()).getEpisodes().get(vEpisode.getId()).getTranslations().get(vTranslation.getId()).getVideoData().get(l).getKey();
                                                                        String urlVideo = serial.getSeasons().get(vSeason.getId()).getEpisodes().get(vEpisode.getId()).getTranslations().get(vTranslation.getId()).getVideoData().get(l).getValue();

                                                                        // Корневой элемент View
                                                                        View viewVideo = LayoutInflater.from(root.getContext()).inflate(R.layout.selector_film_item_2, root, false);
                                                                        // Задаем название качества
                                                                        TextView textViewTitleFiles = viewVideo.findViewById(R.id.textViewTitleFiles);
                                                                        textViewTitleFiles.setText(titleQuality);
                                                                        // Данный LinearLayout нужен для отработки кликов
                                                                        LinearLayout linearLayoutTitleClick2 = viewVideo.findViewById(R.id.linearLayoutTitleClick2);
                                                                        linearLayoutTitleClick2.setId(l);

                                                                        linearLayoutTitleClick2.setOnClickListener(new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View vQuality) {
                                                                                Intent intent = new Intent(activity, PlayerExoActivity.class);

                                                                                EPData.Builder builder = new EPData.Builder();
                                                                                builder.setSerial(serial);
                                                                                builder.setIndexTranslation(vTranslation.getId());
                                                                                builder.setIndexSeason(vSeason.getId());
                                                                                builder.setIndexEpisode(vEpisode.getId());
                                                                                builder.setIndexQuality(vQuality.getId());
                                                                                builder.setBalancer("VIBIX");
                                                                                builder.setFilmInfo(filmInfo);
                                                                                intent.putExtra("epData", builder.build());

                                                                                activity.startActivity(intent);
                                                                            }
                                                                        });

                                                                        linearLayoutVideos.addView(viewVideo);
                                                                    }
                                                                }
                                                            });

                                                            // Переводы добавляются в linearLayoutTranslations
                                                            linearLayoutTranslations.addView(viewTranslation);
                                                        }
                                                    }
                                                });

                                                // Епизоды добавляются в linearLayoutRootEpisode
                                                linearLayoutRootEpisode.addView(viewEpisode);
                                            }
                                        }
                                    });

                                    // Сезоны добавляются в Root (linearLayoutRoot)
                                    root.addView(viewSeason);
                                }
                            } else {
                                root.setVisibility(View.VISIBLE);
                            }
                        } else {
                            root.setVisibility(View.GONE);
                        }
                    }
                }).rotation(arrow.getRotation() == 0 ? 90 : 0).start();


            }
        });



    }
}
