package com.alaka_ala.florafilm.ui.fragments.film.vp_fragments;

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
import android.widget.Toast;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentVideoFilmBinding;
import com.alaka_ala.florafilm.ui.fragments.film.view_model.MainFilmViewModel;
import com.alaka_ala.florafilm.ui.util.api.EPData;
import com.alaka_ala.florafilm.ui.util.api.hdvb.HDVB;
import com.alaka_ala.florafilm.ui.util.api.hdvb.HDVBSelector;
import com.alaka_ala.florafilm.ui.util.api.hdvb.models.HDVBFilm;
import com.alaka_ala.florafilm.ui.util.api.hdvb.models.HDVBSerial;
import com.alaka_ala.florafilm.ui.util.api.vibix.Vibix;
import com.alaka_ala.florafilm.ui.util.api.vibix.VibixSelector;

import java.io.IOException;

public class VideoFilmFragment extends Fragment {
    private FragmentVideoFilmBinding binding;
    private MainFilmViewModel mainFilmViewModel;

    public static void setCallbackLoaderData(CallbackLoaderData callbackLoaderData) {
        VideoFilmFragment.callbackLoaderData = callbackLoaderData;
    }

    private static CallbackLoaderData callbackLoaderData;


    private void onError(String balancer, String err) {
        if (callbackLoaderData == null) return;
        callbackLoaderData.error(balancer, err);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentVideoFilmBinding.inflate(inflater, container, false);
        mainFilmViewModel = new ViewModelProvider(getActivity()).get(MainFilmViewModel.class);

        parseVibix();

        parseHdvb();


        return binding.getRoot();
    }

    private void parseHdvb() {
        HDVB hdvb = new HDVB(getResources().getString(R.string.api_key_hdvb));
        hdvb.parse(mainFilmViewModel.getKinopoiskId(), new HDVB.ResultParseCallback() {
            @Override
            public void film(HDVBFilm film, EPData.Film filmEP) {
                if (filmEP == null) return;
                HDVBSelector hdvbSelector = new HDVBSelector(binding.linearLayoutRoot, filmEP, mainFilmViewModel.getCurrentFilmInfo());
                hdvbSelector.buildSelector(getActivity());
                if (callbackLoaderData != null) {
                    callbackLoaderData.successHDVBFilm(filmEP);
                }
            }

            @Override
            public void serial(HDVBSerial serial, EPData.Serial serialEP) {
                HDVBSelector hdvbSelector = new HDVBSelector(binding.linearLayoutRoot, serialEP, mainFilmViewModel.getCurrentFilmInfo());
                hdvbSelector.buildSelector(getActivity());
                if (callbackLoaderData != null) {
                    callbackLoaderData.successHDVBSerial(serialEP);
                }
            }

            @Override
            public void error(String err) {
                onError("HDVB", err);
            }
        });
    }

    private void parseVibix() {
        Vibix vibix = new Vibix(getResources().getString(R.string.api_key_vibix));
        vibix.parse(mainFilmViewModel.getKinopoiskId(), new Vibix.ConnectionVibix() {
            @Override
            public void startParseVibix() {

            }

            @Override
            public void finishParseFilmVibix(EPData.Film vibixFilm) {
                VibixSelector vibixSelector = new VibixSelector(binding.linearLayoutRoot, vibixFilm, mainFilmViewModel.getCurrentFilmInfo());
                vibixSelector.buildSelector(getActivity());
                if (callbackLoaderData != null) {
                    callbackLoaderData.successVibixFilm(vibixFilm);
                }
            }

            @Override
            public void finishParseSerialVibix(EPData.Serial vibixSerial) {
                VibixSelector vibixSelector = new VibixSelector(binding.linearLayoutRoot, vibixSerial, mainFilmViewModel.getCurrentFilmInfo());
                vibixSelector.buildSelector(getActivity());
                if (callbackLoaderData != null) {
                    callbackLoaderData.successVibixSerial(vibixSerial);
                }
            }

            @Override
            public void errorParseVibix(IOException e) {
                onError("HDVB", e.getMessage());
            }
        });
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    public interface CallbackLoaderData {
        void successHDVBFilm(EPData.Film film);
        void successVibixFilm(EPData.Film film);
        void successHDVBSerial(EPData.Serial serial);
        void successVibixSerial(EPData.Serial serial);
        void error(String balancer, String err);
    }


}