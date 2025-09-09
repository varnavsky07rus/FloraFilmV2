package com.alaka_ala.florafilm.ui.fragments.geminiMovie;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentGeminiMovieBinding;
import com.alaka_ala.florafilm.ui.util.adapters.AdapterRecyclerViewItem1;
import com.alaka_ala.florafilm.ui.util.adapters.AdapterRecyclerViewItem2;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.KinopoiskAPI;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.Collection;
import com.alaka_ala.florafilm.ui.util.listeners.MyRecyclerViewItemTouchListener;
import com.alaka_ala.florafilm.ui.util.listeners.MyRecyclerViewScrollListener;

import java.io.IOException;

public class GeminiMovieFragment extends Fragment {
    private FragmentGeminiMovieBinding binding;
    private RecyclerView rvGeminiMovie;
    private GridLayoutManager layoutManager;
    private AdapterRecyclerViewItem2 adapter2;
    private KinopoiskAPI kinopoiskAPI;
    private GeminiMovieViewModel viewModel;

    private int kinopoisk_id = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGeminiMovieBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(GeminiMovieViewModel.class);
        viewModel.initData(requireContext());
        if (getArguments() != null) {
            kinopoisk_id = getArguments().getInt("kinopoisk_id", 0);
        }

        kinopoiskAPI = new KinopoiskAPI(getResources().getString(R.string.api_key_kinopoisk));
        rvGeminiMovie = binding.rvGeminiMovie;
        layoutManager = new GridLayoutManager(getContext(), 3);
        rvGeminiMovie.setLayoutManager(layoutManager);
        adapter2 = new AdapterRecyclerViewItem2();

        rvGeminiMovie.setAdapter(adapter2);

        viewModel.getGeminiMovies().observe(getViewLifecycleOwner(), collection -> {
            adapter2.setCollection(collection.get(kinopoisk_id));
            adapter2.notifyDataSetChanged();
        });

        if (viewModel.getCollectionForMovie(kinopoisk_id) == null) {
            loadMovies();
        }

        rvGeminiMovie.setOnScrollListener(new MyRecyclerViewScrollListener(MyRecyclerViewScrollListener.VERTICAL) {
            @Override
            public void onStart() {

            }

            @Override
            public void onEnd() {
                loadMovies();
            }
        });

        rvGeminiMovie.addOnItemTouchListener(new MyRecyclerViewItemTouchListener(getContext(), rvGeminiMovie, new MyRecyclerViewItemTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView.ViewHolder holder, View view, int position) {
                Bundle bundle = new Bundle();
                if (position <= -1) return;
                bundle.putInt("kinopoisk_id", viewModel.getGeminiMovies().getValue().get(kinopoisk_id).getItems().get(position).getKinopoiskId());
                Navigation.findNavController(view).navigate(R.id.action_geminiMovieFragment_to_mainFilmFragment, bundle);
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
        viewModel.saveData(requireContext());
        binding = null;
    }

    private void loadMovies() {
        if (viewModel.getCollectionForMovie(kinopoisk_id) != null) {
            if (Integer.parseInt(viewModel.getGeminiMovies().getValue().get(kinopoisk_id).getTotal()) == viewModel.getGeminiMovies().getValue().get(kinopoisk_id).getItems().size())
                return;
        }

        Integer page = viewModel.getPage(kinopoisk_id);
        if (page == null) {
            return;
        }

        kinopoiskAPI.getListSimilarFilms(kinopoisk_id, page, new KinopoiskAPI.RequestCallbackCollection() {
            @Override
            public void onSuccess(Collection collection) {
                if (viewModel.getGeminiMovies().getValue() == null) {
                    viewModel.setGeminiMovies(kinopoisk_id, collection);
                } else {
                    viewModel.addAllGeminiMovies(kinopoisk_id, collection);
                }
                viewModel.incrementPage(kinopoisk_id);
            }

            @Override
            public void onFailure(IOException e) {

            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void finish() {

            }
        });
    }
}
