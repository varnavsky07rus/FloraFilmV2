package com.alaka_ala.florafilm.ui.fragments.collections;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentCollectionBinding;
import com.alaka_ala.florafilm.ui.util.adapters.AdapterRecyclerViewItem1;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.KinopoiskAPI;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.Collection;
import com.alaka_ala.florafilm.ui.util.listeners.MyRecyclerViewScrollListener;

import java.io.IOException;


public class CollectionFragment extends Fragment {
    private FragmentCollectionBinding binding;

    private RecyclerView rvCollection;
    private AdapterRecyclerViewItem1 adapter;

    private KinopoiskAPI kinopoiskAPI;

    private int page = 0;
    private String collectionsName;
    private Collection collections;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCollectionBinding.inflate(inflater, container, false);
        kinopoiskAPI = new KinopoiskAPI(getResources().getString(R.string.api_key_kinopoisk));





        rvCollection = binding.rvCollection;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3, LinearLayoutManager.VERTICAL, false);
        rvCollection.setLayoutManager(gridLayoutManager);
        adapter = new AdapterRecyclerViewItem1();
        rvCollection.setAdapter(adapter);

        LinearLayout llInfo = binding.llInfo;

        collectionsName = getArguments().getString("collection", "");
        if (collectionsName.isEmpty()) {
            llInfo.setVisibility(VISIBLE);
        } else {
            llInfo.setVisibility(INVISIBLE);
        }

        KinopoiskAPI.RequestCallbackCollection requestCallbackCollection = new KinopoiskAPI.RequestCallbackCollection() {
            @Override
            public void onSuccess(Collection collection) {
                if (collections == null) {
                    collections = collection;
                    adapter.setCollection(collections);
                } else {
                    collections.getItems().addAll(collection.getItems());
                }
            }

            @Override
            public void onFailure(IOException e) {
                Toast.makeText(getContext(), "Error! (" + e + ")", Toast.LENGTH_SHORT).show();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void finish() {
                adapter.notifyDataSetChanged();
            }
        };



        switch (collectionsName) {
            case "Фильмы/сериалы":
                kinopoiskAPI.getListTopPopularAll(++page, requestCallbackCollection);
                break;
            case "Фильмы":
                kinopoiskAPI.getListTop250Movies(++page, requestCallbackCollection);
                break;
            case "Сериалы":
                kinopoiskAPI.getListTop250TVShows(++page, requestCallbackCollection);
                break;

        }

        rvCollection.setOnScrollListener(new MyRecyclerViewScrollListener(RecyclerView.VERTICAL) {
            @Override
            public void onStart() {

            }

            @Override
            public void onEnd() {
                switch (collectionsName) {
                    case "Фильмы/сериалы":
                        kinopoiskAPI.getListTopPopularAll(++page, requestCallbackCollection);
                        break;
                    case "Фильмы":
                        kinopoiskAPI.getListTop250Movies(++page, requestCallbackCollection);
                        break;
                    case "Сериалы":
                        kinopoiskAPI.getListTop250TVShows(++page, requestCallbackCollection);
                        break;

                }
            }
        });

        if (getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(collectionsName);
            }
        }

        return binding.getRoot();
    }


}