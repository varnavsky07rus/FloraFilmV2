package com.alaka_ala.florafilm.ui.fragments.film.vp_fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentDescriptionFilmBinding;
import com.alaka_ala.florafilm.ui.activities.PlayerExoActivity;
import com.alaka_ala.florafilm.ui.fragments.film.view_model.MainFilmViewModel;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.KinopoiskAPI;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.ItemFilmInfo;
import com.alaka_ala.florafilm.ui.util.local.FavoriteMoviesManager;
import com.alaka_ala.torstream.torrent.LocalHttpServer;
import com.alaka_ala.torstream.torrent.TorrentStreamer;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

public class DescriptionFragment extends Fragment {
    private FragmentDescriptionFilmBinding binding;
    private TextView textViewMainTitleFilm;
    private TextView textViewOriginalTitleFilm;
    private TextView textViewYearFilm;
    private TextView textViewGenreCategoryFilm;
    private TextView textViewRatingKpFilm;
    private TextView textViewRatingImdbFilm;
    private TextView textViewDescriptionFilm;
    private ImageView imageViewPosterFilm;
    private TextView textViewCountryFilm;
    private TextView textVieSloganFilm;
    private FavoriteMoviesManager favoriteMoviesManager;
    private MainFilmViewModel mainFilmViewModel;

    private int kinopoisk_id;

    private ItemFilmInfo itemFilmInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDescriptionFilmBinding.inflate(inflater, container, false);
        mainFilmViewModel = new ViewModelProvider(getActivity()).get(MainFilmViewModel.class);
        favoriteMoviesManager = new FavoriteMoviesManager(getContext());
        setHasOptionsMenu(false);
        kinopoisk_id = mainFilmViewModel.getKinopoiskId();
        textViewMainTitleFilm = binding.textViewMainTitleFilm;
        textViewOriginalTitleFilm = binding.textViewOriginalTitleFilm;
        textViewYearFilm = binding.textViewYearFilm;
        textViewGenreCategoryFilm = binding.textViewGenreCategoryFilm;
        textViewRatingKpFilm = binding.textViewRatingKpFilm;
        textViewRatingImdbFilm = binding.textViewRatingImdbFilm;
        textViewDescriptionFilm = binding.textViewDescriptionFilm;
        imageViewPosterFilm = binding.imageViewPosterFilm;
        textViewCountryFilm = binding.textViewCountryFilm;
        textVieSloganFilm = binding.textVieSloganFilm;

        loadDataDescription();


        return binding.getRoot();
    }

    private void loadDataDescription() {



        itemFilmInfo = mainFilmViewModel.getItemFilmInfoMap(getContext(), kinopoisk_id);
        if (itemFilmInfo == null) {
            KinopoiskAPI kinopoiskAPI = new KinopoiskAPI(getResources().getString(R.string.api_key_kinopoisk));
            kinopoiskAPI.getInforamationItem(kinopoisk_id, new KinopoiskAPI.RequestCallbackInformationItem() {
                @Override
                public void onSuccessInfoItem(ItemFilmInfo itemFilmInfos) {
                    itemFilmInfo = itemFilmInfos;
                    mainFilmViewModel.addItemFilmInfoMap(getContext(), itemFilmInfos);
                    String title = !itemFilmInfo.getNameRu().equals("null") ? itemFilmInfo.getNameRu() : itemFilmInfo.getNameEn();
                    String subTitle = !itemFilmInfo.getNameOriginal().equals("null") ? itemFilmInfo.getNameOriginal() : "";
                    if (title.equals("null")) {
                        title = itemFilmInfo.getNameOriginal();
                    }
                    textViewMainTitleFilm.setText(title);
                    textViewOriginalTitleFilm.setText(subTitle);
                    textViewYearFilm.setText(itemFilmInfo.getYear());
                    StringBuilder genre = new StringBuilder();
                    for (int i = 0; i < itemFilmInfo.getGenres().size(); i++) {
                        genre.append(itemFilmInfo.getGenres().get(i).getGenre());
                        if (i != itemFilmInfo.getGenres().size() - 1) {
                            genre.append(", ");
                        }
                    }
                    textViewGenreCategoryFilm.setText(genre);
                    textViewRatingKpFilm.setText("Кинопоиск: " + itemFilmInfo.getRatingKinopoisk());
                    textViewRatingImdbFilm.setText("IMDb: " + itemFilmInfo.getRatingImdb());
                    textViewDescriptionFilm.setText(itemFilmInfo.getDescription());
                    StringBuilder country = new StringBuilder();
                    for (int i = 0; i < itemFilmInfo.getCountries().size(); i++) {
                        country.append(itemFilmInfo.getCountries().get(i).getCountry());
                        if (i != itemFilmInfo.getCountries().size() - 1) {
                            country.append(", ");
                        }
                    }
                    textViewCountryFilm.setText(country);
                    String slogan = !itemFilmInfo.getSlogan().equals("null") ? itemFilmInfo.getSlogan() : "";
                    textVieSloganFilm.setText(slogan);
                    itemFilmInfo.getSlogan();
                    Picasso.get().load(itemFilmInfo.getPosterUrl()).into(imageViewPosterFilm);
                    setHasOptionsMenu(true);
                }

                @Override
                public void onFailureInfoItem(IOException e) {

                }
            });
        } else {
            textViewMainTitleFilm.setText(itemFilmInfo.getNameRu());
            textViewOriginalTitleFilm.setText(itemFilmInfo.getNameOriginal().equals("null") ? "" : itemFilmInfo.getNameOriginal());
            textViewYearFilm.setText(itemFilmInfo.getYear());
            StringBuilder genres = new StringBuilder();
            for (int i = 0; i < itemFilmInfo.getGenres().size(); i++) {
                genres.append(itemFilmInfo.getGenres().get(i).getGenre());
                if (i != itemFilmInfo.getGenres().size() - 1) {
                    genres.append(", ");
                }
            }
            textViewGenreCategoryFilm.setText(genres);
            textViewRatingKpFilm.setText("Кинопоиск: " + itemFilmInfo.getRatingKinopoisk());
            textViewRatingImdbFilm.setText("IMDb: " + itemFilmInfo.getRatingImdb());
            textViewDescriptionFilm.setText(itemFilmInfo.getDescription().equals("null") ? "Описание отсутствует" : itemFilmInfo.getDescription());
            StringBuilder counries = new StringBuilder();
            for (int i = 0; i < itemFilmInfo.getCountries().size(); i++) {
                counries.append(itemFilmInfo.getCountries().get(i).getCountry());
                if (i != itemFilmInfo.getCountries().size() - 1) {
                    counries.append(", ");
                }
            }
            textViewCountryFilm.setText(counries);
            textVieSloganFilm.setText(itemFilmInfo.getSlogan().equals("null") ? "" : itemFilmInfo.getSlogan());
            Picasso.get().load(itemFilmInfo.getPosterUrl()).into(imageViewPosterFilm);
            setHasOptionsMenu(true);
        }
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (favoriteMoviesManager.isFavorite(kinopoisk_id)) {
            menu.add("Удалить из избранного").setIcon(R.drawable.round_favorite_24).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        } else {
            menu.add("Добавить в избранное").setIcon(R.drawable.round_favorite_border_24).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getTitle() == null) return super.onOptionsItemSelected(item);
        if (item.getTitle().equals("Добавить в избранное")) {
            //mainFilmViewModel.addToFavorite();
            String posterUrl = itemFilmInfo.getPosterUrl() == null ? itemFilmInfo.getPosterUrlPreview() : itemFilmInfo.getPosterUrl();
            if (posterUrl == null)
                posterUrl = "http://st.kinopoisk.ru/images/film_big/" + kinopoisk_id + ".jpg";
            String nameRu = itemFilmInfo.getNameRu() == null ? itemFilmInfo.getNameOriginal() : itemFilmInfo.getNameRu();
            if (nameRu == null) nameRu = "Отсутствует название";
            favoriteMoviesManager.addFavorite(kinopoisk_id, posterUrl, nameRu);
            item.setIcon(R.drawable.round_favorite_24).setTitle("Удалить из избранного");
        } else if (item.getTitle().equals("Удалить из избранного")) {
            //mainFilmViewModel.removeFromFavorite();
            favoriteMoviesManager.removeFavorite(kinopoisk_id);
            item.setIcon(R.drawable.round_favorite_border_24).setTitle("Добавить в избранное");
        }

        return super.onOptionsItemSelected(item);
    }


}