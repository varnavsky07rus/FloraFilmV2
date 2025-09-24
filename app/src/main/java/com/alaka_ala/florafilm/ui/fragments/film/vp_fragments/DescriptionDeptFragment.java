package com.alaka_ala.florafilm.ui.fragments.film.vp_fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentDescriptionDeptBinding;
import com.alaka_ala.florafilm.ui.activities.PlayerExoActivity;
import com.alaka_ala.florafilm.ui.fragments.film.view_model.MainFilmViewModel;
import com.alaka_ala.florafilm.ui.util.api.EPData;
import com.alaka_ala.florafilm.ui.util.api.firebase.DataLikes;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.KinopoiskAPI;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.ItemFilmInfo;
import com.alaka_ala.florafilm.ui.util.local.FavoriteMoviesManager;
import com.alaka_ala.florafilm.ui.util.player.PlaybackPositionManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.squareup.picasso.Picasso;

import java.io.IOException;


public class DescriptionDeptFragment extends Fragment {

    private FragmentDescriptionDeptBinding binding;


    private TextView textViewMainTitleFilm;
    private TextView textViewOriginalTitleFilm;
    private TextView textViewGenreCategoryFilm;
    private TextView textViewRatingKpFilm;
    private TextView textViewDescriptionFilm;
    private ImageView imageViewPosterFilm;
    private TextView textViewCountryFilm;
    private TextView textVieSloganFilm;
    private Chip chipLike;
    private Chip chipDislike;
    private FavoriteMoviesManager favoriteMoviesManager;
    private MainFilmViewModel mainFilmViewModel;
    private Button buttonResumeView;
    private PlaybackPositionManager playbackPositionManager;

    private int kinopoisk_id;

    private ItemFilmInfo itemFilmInfo;


    private boolean isLoadHDVB = false;
    private boolean isLoadVibix = false;
    private boolean isLoadLumex = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDescriptionDeptBinding.inflate(inflater, container, false);
        mainFilmViewModel = new ViewModelProvider(getActivity()).get(MainFilmViewModel.class);
        favoriteMoviesManager = new FavoriteMoviesManager(getContext());
        playbackPositionManager = new PlaybackPositionManager(getContext());
        setHasOptionsMenu(false);
        kinopoisk_id = mainFilmViewModel.getKinopoiskId();

        findViewByBinding();

        buttonResumeView.setEnabled(false);
        loadDataDescription();

        DataLikes dataLikes = getDataLikes();


        chipLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataLikes.addLikeDislike("like");

            }
        });

        chipDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataLikes.addLikeDislike("dislike");
            }
        });

        textViewRatingKpFilm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Рейтинг кинопоиска!", Toast.LENGTH_SHORT).show();
            }
        });

        String balancer = playbackPositionManager.getSavedBalancer(kinopoisk_id);

        VideoFilmFragment.addCallbackLoaderData(new VideoFilmFragment.CallbackLoaderData() {

            @Override
            public void successHDVBFilm(EPData.Film film) {
                isLoadHDVB = true;
                mainFilmViewModel.getFilmMutableLiveDataHDVB().setValue(film);
                if (balancer.equals("HDVB")) {
                    buttonResumeView.setEnabled(true);
                }
            }

            @Override
            public void successVibixFilm(EPData.Film film) {
                isLoadVibix = true;
                mainFilmViewModel.getFilmMutableLiveDataVibix().setValue(film);
                if (balancer.equals("VIBIX")) {
                    buttonResumeView.setEnabled(true);
                }
            }

            @Override
            public void successHDVBSerial(EPData.Serial serial) {
                isLoadHDVB = true;
                mainFilmViewModel.getSerialMutableLiveDataHDVB().setValue(serial);
                if (balancer.equals("HDVB")) {
                    buttonResumeView.setEnabled(true);
                }
            }

            @Override
            public void successVibixSerial(EPData.Serial serial) {
                isLoadVibix = true;
                mainFilmViewModel.getSerialMutableLiveDataVibix().setValue(serial);
                if (balancer.equals("HDVB")) {
                    buttonResumeView.setEnabled(true);
                }
            }

            @Override
            public void successLumexFilm(EPData.Film film) {
                isLoadLumex = true;
                mainFilmViewModel.getFilmMutableLiveDataLUMEX().setValue(film);
                if (balancer.equals("LUMEX")) {
                    buttonResumeView.setEnabled(true);
                }
            }

            @Override
            public void successLumexSerial(EPData.Serial serial) {
                isLoadLumex = true;
                mainFilmViewModel.getSerialMutableLiveDataLUMEX().setValue(serial);
                if (balancer.equals("LUMEX")) {
                    buttonResumeView.setEnabled(true);
                }
            }

            @Override
            public void error(String balancer, String err) {
                /*if (getContext() != null) {
                    Toast.makeText(getContext(), balancer + ": " + err, Toast.LENGTH_SHORT).show();
                }*/
            }
        });





        return binding.getRoot();
    }

    private void findViewByBinding() {
        textViewMainTitleFilm = binding.textViewMainTitleFilm;
        textViewOriginalTitleFilm = binding.textViewOriginalTitleFilm;
        textViewGenreCategoryFilm = binding.chipGenreCategoryFilm;
        textViewRatingKpFilm = binding.chipViewRatingKpFilm;
        textViewDescriptionFilm = binding.textViewDescriptionFilm;
        imageViewPosterFilm = binding.imageViewPosterFilm;
        textViewCountryFilm = binding.chipCountryFilm;
        textVieSloganFilm = binding.textVieSloganFilm;
        buttonResumeView = binding.buttonResumeView;
        chipLike = binding.chipLike;
        chipDislike = binding.chipDislike;
    }

    @NonNull
    private DataLikes getDataLikes() {
        DataLikes dataLikes = new DataLikes(kinopoisk_id);
        dataLikes.getLikeDislike(new DataLikes.ListenerLikeDislike() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onLike(long count) {
                chipLike.setText("" + count);
                String s = "";
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onDislike(long count) {
                chipDislike.setText("" + count);
                String s = "";
            }
        });
        return dataLikes;
    }


    @SuppressLint("SetTextI18n")
    private void loadDataDescription() {

        itemFilmInfo = mainFilmViewModel.getItemFilmInfoMap(getContext(), kinopoisk_id);
        if (itemFilmInfo == null) {
            KinopoiskAPI kinopoiskAPI = new KinopoiskAPI(getResources().getString(R.string.api_key_kinopoisk));
            kinopoiskAPI.getInforamationItem(kinopoisk_id, new KinopoiskAPI.RequestCallbackInformationItem() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccessInfoItem(ItemFilmInfo itemFilmInfos) {
                    itemFilmInfo = itemFilmInfos;
                    mainFilmViewModel.addItemFilmInfoMap(getContext(), itemFilmInfos);
                    String title = !itemFilmInfo.getNameRu().equals("null") ? itemFilmInfo.getNameRu() : itemFilmInfo.getNameEn();
                    String subTitle = !itemFilmInfo.getNameOriginal().equals("null") ? itemFilmInfo.getNameOriginal() : "";
                    if (title.equals("null")) {
                        title = itemFilmInfo.getNameOriginal();
                    }
                    title += " " + itemFilmInfo.getYear();
                    textViewMainTitleFilm.setText(title + " (" + itemFilmInfo.getYear() + ")");
                    textViewOriginalTitleFilm.setText(subTitle);
                    StringBuilder genre = new StringBuilder();
                    for (int i = 0; i < itemFilmInfo.getGenres().size(); i++) {
                        genre.append(itemFilmInfo.getGenres().get(i).getGenre());
                        if (i != itemFilmInfo.getGenres().size() - 1) {
                            genre.append(", ");
                        }
                    }
                    textViewGenreCategoryFilm.setText(genre);
                    textViewRatingKpFilm.setText("☆" + itemFilmInfo.getRatingKinopoisk());
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
                    ImageView cardViewPosterimg = binding.imageViewPosterFilm;
                    cardViewPosterimg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle bundle = new Bundle();
                            bundle.putInt("kinopoisk_id", kinopoisk_id);
                            Navigation.findNavController(v).navigate(R.id.imageViewerFragment, bundle);
                        }
                    });
                    setHasOptionsMenu(true);

                    resumeButtonLogic();


                }

                @Override
                public void onFailureInfoItem(IOException e) {

                }
            });
        } else {
            textViewMainTitleFilm.setText(itemFilmInfo.getNameRu() + " (" + itemFilmInfo.getYear() + ")");
            textViewOriginalTitleFilm.setText(itemFilmInfo.getNameOriginal().equals("null") ? "" : itemFilmInfo.getNameOriginal() + " " + itemFilmInfo.getYear());
            StringBuilder genres = new StringBuilder();
            for (int i = 0; i < itemFilmInfo.getGenres().size(); i++) {
                genres.append(itemFilmInfo.getGenres().get(i).getGenre());
                if (i != itemFilmInfo.getGenres().size() - 1) {
                    genres.append(", ");
                }
            }
            textViewGenreCategoryFilm.setText(genres);
            textViewRatingKpFilm.setText("☆" + itemFilmInfo.getRatingKinopoisk());
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
            ImageView cardViewPosterimg = binding.imageViewPosterFilm;
            cardViewPosterimg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("kinopoisk_id", kinopoisk_id);
                    Navigation.findNavController(v).navigate(R.id.imageViewerFragment, bundle);
                }
            });
            setHasOptionsMenu(true);

            resumeButtonLogic();

        }
    }

    @SuppressLint("SetTextI18n")
    private void resumeButtonLogic() {
        long positionView = playbackPositionManager.getSavedPositionEpisode(
                kinopoisk_id,
                playbackPositionManager.getSavedIndexEpisode(kinopoisk_id),
                playbackPositionManager.getSavedIndexSeason(kinopoisk_id)
        );
        if (positionView != 0) {
            buttonResumeView.setVisibility(View.VISIBLE);
        }
        buttonResumeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String balancer = playbackPositionManager.getSavedBalancer(kinopoisk_id);
                switch (balancer) {
                    case "HDVB":
                        if (!isLoadHDVB) {
                            Toast.makeText(getContext(), "Загрузка данных HDVB", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        break;
                    case "VIBIX":
                        if (!isLoadVibix) {
                            Toast.makeText(getContext(), "Загрузка данных VIBIX", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        break;
                    case "LUMEX":
                        if (!isLoadLumex) {
                            Toast.makeText(getContext(), "Загрузка данных LUMEX", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        break;
                    default:
                        new MaterialAlertDialogBuilder(getContext()).setTitle(balancer).setMessage("Упс. с балансером: " + balancer + " продолжить пока что не получится!\nНо мы это скоро исправим!").show();
                        return;
                }
                Intent intent = new Intent(getActivity(), PlayerExoActivity.class);
                EPData.Builder builderEPData = new EPData.Builder();
                if (itemFilmInfo == null) return;
                if (balancer.equals("HDVB")) {
                    if (itemFilmInfo.isSerial()) {
                        builderEPData.setSerial(mainFilmViewModel.getSerialMutableLiveDataHDVB().getValue());
                    } else {
                        builderEPData.setFilm(mainFilmViewModel.getFilmMutableLiveDataHDVB().getValue());
                    }
                } else if (balancer.equals("LUMEX")) {
                    if (itemFilmInfo.isSerial()) {
                        builderEPData.setSerial(mainFilmViewModel.getSerialMutableLiveDataLUMEX().getValue());
                    } else {
                        builderEPData.setFilm(mainFilmViewModel.getFilmMutableLiveDataLUMEX().getValue());
                    }
                } else {
                    if (itemFilmInfo.isSerial()) {
                        builderEPData.setSerial(mainFilmViewModel.getSerialMutableLiveDataVibix().getValue());
                    } else {
                        builderEPData.setFilm(mainFilmViewModel.getFilmMutableLiveDataVibix().getValue());
                    }
                }
                builderEPData.setIndexTranslation(playbackPositionManager.getSavedIndexTranslation(kinopoisk_id));
                builderEPData.setIndexQuality(playbackPositionManager.getSavedIndexQuality(kinopoisk_id));
                builderEPData.setIndexSeason(playbackPositionManager.getSavedIndexSeason(kinopoisk_id));
                builderEPData.setIndexEpisode(playbackPositionManager.getSavedIndexEpisode(kinopoisk_id));
                builderEPData.setBalancer(balancer);
                builderEPData.setFilmInfo(itemFilmInfo);
                EPData films = builderEPData.build();
                intent.putExtra("epData", films);
                getActivity().startActivity(intent);
            }
        });
        if (itemFilmInfo == null) return;
        if (itemFilmInfo.isSerial()) {
            // Если сериал
            int indexSeason = playbackPositionManager.getSavedIndexSeason(kinopoisk_id);
            int indexEpisode = playbackPositionManager.getSavedIndexEpisode(kinopoisk_id);
            buttonResumeView.setText("Сезон " + (indexSeason + 1) + " • Серия " + (indexEpisode + 1) + " • " + formatTime(positionView));
        } else {
            buttonResumeView.setText("Продолжить: " + formatTime(positionView));
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
        menu.add("Актёры").setIcon(R.drawable.ic_launcher_foreground).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        menu.add("Сиквелы / Приквелы").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        menu.add("Похожие фильмы").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
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
        } else if (item.getTitle().equals("Актёры")) {
            navigateToActors();
        } else if (item.getTitle().equals("Сиквелы / Приквелы")) {
            navigateToSequels();
        } else if (item.getTitle().equals("Похожие фильмы")) {
            navigateToGeminiMovie();
        }

        return super.onOptionsItemSelected(item);
    }

    private void navigateToSequels() {
        Bundle bundle = new Bundle();
        bundle.putInt("kinopoisk_id", kinopoisk_id);
        bundle.putString("title", "Сиквелы / Приквелы");
        Navigation.findNavController(binding.getRoot()).navigate(R.id.sequelsPrequelsFragment, bundle);
    }

    private void navigateToActors(){
        Bundle bundle = new Bundle();
        bundle.putInt("kinopoisk_id", kinopoisk_id);
        bundle.putString("nameRu", (itemFilmInfo.getNameRu().equals("null") ? itemFilmInfo.getNameEn() : itemFilmInfo.getNameRu())
                .equals("null") ? "Актёры" : (itemFilmInfo.getNameRu().equals("null") ? itemFilmInfo.getNameOriginal() : itemFilmInfo.getNameRu()));
        Navigation.findNavController(binding.getRoot()).navigate(R.id.actorsFragment, bundle);
    }

    private void navigateToGeminiMovie() {
        Bundle bundle = new Bundle();
        bundle.putInt("kinopoisk_id", kinopoisk_id);
        bundle.putString("title", "Похожие фильмы");
        Navigation.findNavController(binding.getRoot()).navigate(R.id.geminiMovieFragment, bundle);
    }



    public static String formatTime(long milliseconds) {
        long totalSeconds = milliseconds / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder formattedTime = new StringBuilder();

        if (hours > 0) {
            formattedTime.append(hours).append(" ч. ");
        }
        if (minutes > 0) {
            formattedTime.append(minutes).append(" мин ");
        }
        if (seconds > 0 || formattedTime.length() == 0) { // Если время 0, то покажем "0 сек"
            formattedTime.append(seconds).append(" сек");
        }

        return formattedTime.toString().trim();
    }


    @Override
    public void onResume() {
        super.onResume();
        resumeButtonLogic();
    }
}