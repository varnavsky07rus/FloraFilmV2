package com.alaka_ala.florafilm.ui.fragments.collections;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
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
import com.alaka_ala.florafilm.ui.fragments.home.HomeViewModel;
import com.alaka_ala.florafilm.ui.util.adapters.AdapterRecyclerViewItem1;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.KinopoiskAPI;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.Collection;
import com.alaka_ala.florafilm.ui.util.listeners.MyRecyclerViewItemTouchListener;
import com.alaka_ala.florafilm.ui.util.listeners.MyRecyclerViewScrollListener;

import java.io.IOException;


public class CollectionFragment extends Fragment {
    private FragmentCollectionBinding binding;

    private RecyclerView rvCollection;
    private AdapterRecyclerViewItem1 adapter;

    private KinopoiskAPI kinopoiskAPI;

    private int page = 0;
    private String collectionsName;

    private CollectionsViewModel collectionsViewModel;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCollectionBinding.inflate(inflater, container, false);
        kinopoiskAPI = new KinopoiskAPI(getResources().getString(R.string.api_key_kinopoisk));
        collectionsViewModel = new ViewModelProvider(this).get(CollectionsViewModel.class);





        rvCollection = binding.rvCollection;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3, LinearLayoutManager.VERTICAL, false);
        rvCollection.setLayoutManager(gridLayoutManager);
        adapter = new AdapterRecyclerViewItem1();
        rvCollection.setAdapter(adapter);


        LinearLayout llInfo = binding.llInfo;

        collectionsName = getArguments().getString("collection", "");

        if (collectionsName.isEmpty()) {
            llInfo.setVisibility(VISIBLE);
        }

        KinopoiskAPI.RequestCallbackCollection requestCallbackCollection = new KinopoiskAPI.RequestCallbackCollection() {
            @Override
            public void onSuccess(Collection collection) {
                switch (collectionsName) {
                    case "Фильмы/сериалы":
                        if (collectionsViewModel.getCollectionMutableLiveDataPopularAll().getValue() == null) {
                            collectionsViewModel.getCollectionMutableLiveDataPopularAll().setValue(collection);
                        } else {
                            collectionsViewModel.getCollectionMutableLiveDataPopularAll().getValue().getItems().addAll(collection.getItems());
                        }
                        adapter.setCollection(collectionsViewModel.getCollectionMutableLiveDataPopularAll().getValue());
                        break;
                    case "Фильмы":
                        if (collectionsViewModel.getCollectionMutableLiveDataMovie().getValue() == null) {
                            collectionsViewModel.getCollectionMutableLiveDataMovie().setValue(collection);

                        } else {
                            collectionsViewModel.getCollectionMutableLiveDataMovie().getValue().getItems().addAll(collection.getItems());
                        }
                        adapter.setCollection(collectionsViewModel.getCollectionMutableLiveDataMovie().getValue());
                        break;
                    case "Сериалы":
                        if (collectionsViewModel.getCollectionMutableLiveDataSerial().getValue() == null) {
                            collectionsViewModel.getCollectionMutableLiveDataSerial().setValue(collection);

                        } else {
                            collectionsViewModel.getCollectionMutableLiveDataSerial().getValue().getItems().addAll(collection.getItems());
                        }
                        adapter.setCollection(collectionsViewModel.getCollectionMutableLiveDataSerial().getValue());
                        break;

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

        rvCollection.addOnItemTouchListener(new MyRecyclerViewItemTouchListener(getContext(), rvCollection, new MyRecyclerViewItemTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView.ViewHolder holder, View view, int position) {
                Bundle bundle = new Bundle();
                if (position <= -1) return;
                switch (collectionsName) {
                    case "Фильмы/сериалы":
                        bundle.putInt("kinopoisk_id", collectionsViewModel.getCollectionMutableLiveDataPopularAll().getValue().getItems().get(position).getKinopoiskId());
                        break;
                    case "Фильмы":
                        bundle.putInt("kinopoisk_id", collectionsViewModel.getCollectionMutableLiveDataMovie().getValue().getItems().get(position).getKinopoiskId());
                        break;
                    case "Сериалы":
                        bundle.putInt("kinopoisk_id", collectionsViewModel.getCollectionMutableLiveDataSerial().getValue().getItems().get(position).getKinopoiskId());
                        break;

                }

                Navigation.findNavController(view).navigate(R.id.action_collectionFragment_to_mainFilmFragment, bundle);
            }

            @Override
            public void onLongItemClick(RecyclerView.ViewHolder holder, View view, int position) {

            }
        }));



        return binding.getRoot();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Bundle bundle = new Bundle();
        bundle.putString("collection", collectionsName);
        onSaveInstanceState(bundle);
    }
}