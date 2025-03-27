package com.alaka_ala.florafilm.ui.fragments.collections;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentCollectionBinding;
import com.alaka_ala.florafilm.ui.util.adapters.AdapterRecyclerViewItem1;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.KinopoiskAPI;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.Collection;

import java.io.IOException;


public class CollectionFragment extends Fragment {
    private FragmentCollectionBinding binding;

    private RecyclerView rvCollection;
    private AdapterRecyclerViewItem1 adapter;

    private KinopoiskAPI kinopoiskAPI;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCollectionBinding.inflate(inflater, container, false);
        kinopoiskAPI = new KinopoiskAPI(getResources().getString(R.string.api_key_kinopoisk));

        rvCollection = binding.rvCollection;




        kinopoiskAPI.getListFromCountries(1, 67, new KinopoiskAPI.RequestCallbackCollection() {
            @Override
            public void onSuccess(Collection collection) {

            }

            @Override
            public void onFailure(IOException e) {

            }

            @Override
            public void finish() {

            }
        });

        kinopoiskAPI.getListFromGenre(KinopoiskAPI.GenreConstants.ACTION, 1, new KinopoiskAPI.RequestCallbackCollection() {
            @Override
            public void onSuccess(Collection collection) {

            }

            @Override
            public void onFailure(IOException e) {

            }

            @Override
            public void finish() {

            }
        });


        return binding.getRoot();
    }
}