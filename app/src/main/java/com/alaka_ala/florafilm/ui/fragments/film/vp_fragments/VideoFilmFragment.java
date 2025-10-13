package com.alaka_ala.florafilm.ui.fragments.film.vp_fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentVideoFilmBinding;
import com.alaka_ala.florafilm.ui.fragments.film.view_model.MainFilmViewModel;
import com.alaka_ala.florafilm.ui.fragments.settings.SettingsUtils;
import com.alaka_ala.florafilm.ui.util.api.BanCheker;
import com.alaka_ala.florafilm.ui.util.api.EPData;
import com.alaka_ala.florafilm.ui.util.api.collapse.CollapseAPI;
import com.alaka_ala.florafilm.ui.util.api.collapse.HlsProcessor;
import com.alaka_ala.florafilm.ui.util.api.collapse.models.ApiResponse;
import com.alaka_ala.florafilm.ui.util.api.collapse.models.PlayerData;
import com.alaka_ala.florafilm.ui.util.api.hdvb.HDVB;
import com.alaka_ala.florafilm.ui.util.api.hdvb.HDVBSelector;
import com.alaka_ala.florafilm.ui.util.api.hdvb.models.HDVBFilm;
import com.alaka_ala.florafilm.ui.util.api.hdvb.models.HDVBSerial;
import com.alaka_ala.florafilm.ui.util.api.lumex.LumexApi;
import com.alaka_ala.florafilm.ui.util.api.lumex.LumexSelector;
import com.alaka_ala.florafilm.ui.util.api.vibix.Vibix;
import com.alaka_ala.florafilm.ui.util.api.vibix.VibixSelector;

import java.io.IOException;
import java.util.ArrayList;

public class VideoFilmFragment extends Fragment {
    private FragmentVideoFilmBinding binding;
    private MainFilmViewModel mainFilmViewModel;

    public static void addCallbackLoaderData(CallbackLoaderData callbackLoaderData) {
        VideoFilmFragment.callbackLoaderDatas.add(callbackLoaderData);
    }

    private static ArrayList<CallbackLoaderData> callbackLoaderDatas = new ArrayList<>();

    private void onError(String balancer, String err) {
        for (CallbackLoaderData callbackLoaderData : callbackLoaderDatas) {
            callbackLoaderData.error(balancer, err);
        }
    }


    private boolean isNotFountDataVibix = false;
    private boolean isNotFountDataHDVB = false;
    private boolean isNotFoundDataLumex = false;
    private FrameLayout rootNotFound;
    private LottieAnimationView lottieNotFound;

    private RecyclerView recyclerViewRoot;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentVideoFilmBinding.inflate(inflater, container, false);
        mainFilmViewModel = new ViewModelProvider(getActivity()).get(MainFilmViewModel.class);

        rootNotFound = binding.rootNotFound;
        lottieNotFound = binding.lottieNotFound;


        chekBanFilm();
        printNotFoundFile();




        return binding.getRoot();
    }


    private void chekBanFilm() {
        BanCheker banCheker = new BanCheker(getContext());
        if (!banCheker.isBan(mainFilmViewModel.getKinopoiskId())) {
            if (SettingsUtils.getParamSearchVIBIX(getContext())) {
                if (mainFilmViewModel.getFilmVibix(mainFilmViewModel.getKinopoiskId()) != null) {
                    VibixSelector vibixSelector = new VibixSelector(binding.linearLayoutRoot, mainFilmViewModel.getFilmVibix(mainFilmViewModel.getKinopoiskId()), mainFilmViewModel.getCurrentFilmInfo());
                    vibixSelector.buildSelector(getActivity());
                } else if (mainFilmViewModel.getSerialVibix(mainFilmViewModel.getKinopoiskId()) != null) {
                    VibixSelector vibixSelector = new VibixSelector(binding.linearLayoutRoot, mainFilmViewModel.getSerialVibix(mainFilmViewModel.getKinopoiskId()), mainFilmViewModel.getCurrentFilmInfo());
                    vibixSelector.buildSelector(getActivity());
                } else {
                    parseVibix();
                }
            }
            if (SettingsUtils.getParamSeeachHDVB(getContext())) {
                if (mainFilmViewModel.getFilmHDVB(mainFilmViewModel.getKinopoiskId()) != null) {
                    HDVBSelector hdvbSelector = new HDVBSelector(binding.linearLayoutRoot, mainFilmViewModel.getFilmHDVB(mainFilmViewModel.getKinopoiskId()), mainFilmViewModel.getCurrentFilmInfo());
                    hdvbSelector.buildSelector(getActivity());
                } else if (mainFilmViewModel.getSerialHDVB(mainFilmViewModel.getKinopoiskId()) != null) {
                    HDVBSelector hdvbSelector = new HDVBSelector(binding.linearLayoutRoot, mainFilmViewModel.getSerialHDVB(mainFilmViewModel.getKinopoiskId()), mainFilmViewModel.getCurrentFilmInfo());
                    hdvbSelector.buildSelector(getActivity());
                } else {
                    parseHdvb();
                }
            }
            if (SettingsUtils.getParamSearchLumex(getContext())) {
                if (mainFilmViewModel.getFilmLUMEX(mainFilmViewModel.getKinopoiskId()) != null) {
                    LumexSelector lumexSelector = new LumexSelector(binding.linearLayoutRoot, mainFilmViewModel.getFilmLUMEX(mainFilmViewModel.getKinopoiskId()), mainFilmViewModel.getCurrentFilmInfo());
                    lumexSelector.buildSelector(getActivity());
                } else if (mainFilmViewModel.getSerialLUMEX(mainFilmViewModel.getKinopoiskId()) != null) {
                    LumexSelector lumexSelector = new LumexSelector(binding.linearLayoutRoot, mainFilmViewModel.getSerialLUMEX(mainFilmViewModel.getKinopoiskId()), mainFilmViewModel.getCurrentFilmInfo());
                    lumexSelector.buildSelector(getActivity());
                } else {
                    parseLumex();
                }
            }
        } else {
            isNotFountDataVibix = true;
            isNotFountDataHDVB = true;
            isNotFoundDataLumex = true;
        }
    }

    private void printNotFoundFile() {
        if (getContext() == null) return;
        if (!SettingsUtils.getParamSearchVIBIX(getContext())) {
            isNotFountDataVibix = true;
        }
        if (!SettingsUtils.getParamSeeachHDVB(getContext())) {
            isNotFountDataHDVB = true;
        }
        if (!SettingsUtils.getParamSearchLumex(getContext())) {
            isNotFoundDataLumex = true;
        }
        if (isNotFountDataVibix && isNotFountDataHDVB && isNotFoundDataLumex) {
            rootNotFound.setVisibility(View.VISIBLE);
            lottieNotFound.setAnimation(R.raw.not_found);
            if (SettingsUtils.getParamPageEffectAnimation(getContext())) {
                lottieNotFound.playAnimation();
            }
        } else {
            rootNotFound.setVisibility(View.GONE);
            lottieNotFound.pauseAnimation();
        }
    }

    private void parseHdvb() {

        HDVB hdvb = new HDVB(getResources().getString(R.string.api_key_hdvb));
        hdvb.parse(mainFilmViewModel.getKinopoiskId(), new HDVB.ResultParseCallback() {
            @Override
            public void finish() {
                HDVB.ResultParseCallback.super.finish();
            }

            @Override
            public void film(HDVBFilm film, EPData.Film filmEP) {
                if (filmEP == null) {
                    for (CallbackLoaderData callbackLoaderData : callbackLoaderDatas) {
                        if (callbackLoaderData != null) {
                            error("Фильм не найден");
                            isNotFountDataHDVB = true;
                        }
                    }
                    return;
                }
                HDVBSelector hdvbSelector = new HDVBSelector(binding.linearLayoutRoot, filmEP, mainFilmViewModel.getCurrentFilmInfo());
                hdvbSelector.buildSelector(getActivity());
                for (CallbackLoaderData callbackLoaderData : callbackLoaderDatas) {
                    if (callbackLoaderData != null) {
                        callbackLoaderData.successHDVBFilm(filmEP);
                        isNotFountDataHDVB = false;
                    }
                }

            }

            @Override
            public void serial(HDVBSerial serial, EPData.Serial serialEP) {
                if (serialEP == null) {
                    for (CallbackLoaderData callbackLoaderData : callbackLoaderDatas) {
                        if (callbackLoaderData != null) {
                            error("Сериал не найден");
                            isNotFountDataHDVB = true;
                        }
                    }
                }
                HDVBSelector hdvbSelector = new HDVBSelector(binding.linearLayoutRoot, serialEP, mainFilmViewModel.getCurrentFilmInfo());
                hdvbSelector.buildSelector(getActivity());
                for (CallbackLoaderData callbackLoaderData : callbackLoaderDatas) {
                    if (callbackLoaderData != null) {
                        callbackLoaderData.successHDVBSerial(serialEP);
                        isNotFountDataHDVB = false;
                    }
                }

            }

            @Override
            public void error(String err) {
                onError("HDVB", err);
                isNotFountDataHDVB = true;
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
                for (CallbackLoaderData callbackLoaderData : callbackLoaderDatas) {
                    if (callbackLoaderData != null) {
                        callbackLoaderData.successVibixFilm(vibixFilm);
                        isNotFountDataVibix = false;
                    }
                }
            }

            @Override
            public void finishParseSerialVibix(EPData.Serial vibixSerial) {
                VibixSelector vibixSelector = new VibixSelector(binding.linearLayoutRoot, vibixSerial, mainFilmViewModel.getCurrentFilmInfo());
                vibixSelector.buildSelector(getActivity());
                for (CallbackLoaderData callbackLoaderData : callbackLoaderDatas) {
                    if (callbackLoaderData != null) {
                        callbackLoaderData.successVibixSerial(vibixSerial);
                        isNotFountDataVibix = false;
                    }
                }
            }

            @Override
            public void errorParseVibix(IOException e) {
                onError("VIBIX", e.getMessage());
                isNotFountDataVibix = true;
            }
        });
    }

    private void parseLumex() {
        LumexApi lumexApi = new LumexApi();
        lumexApi.getFromKinopoiskId(mainFilmViewModel.getKinopoiskId(), new LumexApi.CallbackLumex() {
            @Override
            public void success(EPData.Film film, EPData.Serial serial) {
                Handler handler = new Handler(Looper.getMainLooper());
                LumexSelector lumexSelector;
                if (film != null) {
                    lumexSelector = new LumexSelector(binding.linearLayoutRoot, film, mainFilmViewModel.getCurrentFilmInfo());
                } else {
                    lumexSelector = new LumexSelector(binding.linearLayoutRoot, serial, mainFilmViewModel.getCurrentFilmInfo());
                }
                handler.post(() -> lumexSelector.buildSelector(getActivity()));
                if (film == null) {
                    for (CallbackLoaderData callbackLoaderData : callbackLoaderDatas) {
                        if (callbackLoaderData != null) {
                            handler.post(() -> callbackLoaderData.successLumexSerial(serial));
                            isNotFountDataVibix = false;
                        }
                    }
                } else {
                    for (CallbackLoaderData callbackLoaderData : callbackLoaderDatas) {
                        if (callbackLoaderData != null) {
                            handler.post(() -> callbackLoaderData.successLumexFilm(film));
                            isNotFountDataVibix = false;
                        }
                    }
                }

            }

            @Override
            public void error(String err) {
                new Handler(Looper.getMainLooper()).post(() -> onError("LUMEX", err));
                isNotFoundDataLumex = true;
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

        void successLumexFilm(EPData.Film film);

        void successLumexSerial(EPData.Serial serial);


        void error(String balancer, String err);
    }

    @Override
    public void onResume() {
        super.onResume();
        printNotFoundFile();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        callbackLoaderDatas = new ArrayList<>();
    }
}