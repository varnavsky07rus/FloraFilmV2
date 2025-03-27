package com.alaka_ala.florafilm.ui.fragments.search;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentSearchBinding;
import com.alaka_ala.florafilm.ui.util.adapters.AdapterRecyclerViewItem1;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.KinopoiskAPI;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.Collection;
import com.alaka_ala.florafilm.ui.util.listeners.MyRecyclerViewItemTouchListener;
import com.alaka_ala.florafilm.ui.util.listeners.MyRecyclerViewScrollListener;

import java.io.IOException;


public class SearchFragment extends Fragment {
    private FragmentSearchBinding binding;
    private SearchView searchView;
    private RecyclerView rvSearch;
    private AdapterRecyclerViewItem1 adapter;
    private KinopoiskAPI kinopoiskAPI;

    private SearchFragmentViewModel viewModel;

    private Collection collection;
    private int page = 0;
    private String query = "";

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(SearchFragmentViewModel.class);
        if (viewModel.getCollection() != null) {
            collection = viewModel.getCollection();
        }
        if (viewModel.getPage().getValue() != null) {
            page = viewModel.getPage().getValue();
        }
        if (viewModel.getQuery().getValue() != null) {
            query = viewModel.getQuery().getValue();
        }
        searchView = binding.searchView;
        searchView.setIconifiedByDefault(false);
        rvSearch = binding.rvSearch;
        kinopoiskAPI = new KinopoiskAPI(getResources().getString(R.string.api_key_kinopoisk));
        rvSearch.setLayoutManager(new GridLayoutManager(getContext(), 3, LinearLayoutManager.VERTICAL, false));

        if (collection != null) {
            adapter = new AdapterRecyclerViewItem1();
            adapter.setCollection(collection);
            rvSearch.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                page = 0;
                viewModel.getPage().setValue(page);
                viewModel.getQuery().setValue(s);
                kinopoiskAPI.getListSearch(s, ++page, new KinopoiskAPI.RequestCallbackCollection() {
                    @Override
                    public void onSuccess(Collection collectns) {
                        collection = collectns;
                    }

                    @Override
                    public void onFailure(IOException e) {
                        e.printStackTrace();
                    }

                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void finish() {
                        viewModel.setCollection(collection);
                        if (adapter == null) {
                            adapter = new AdapterRecyclerViewItem1();
                            adapter.setCollection(collection);
                            rvSearch.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        } else {
                            adapter.setCollection(collection);
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });


        rvSearch.addOnScrollListener(new MyRecyclerViewScrollListener(MyRecyclerViewScrollListener.VERTICAL) {
            @Override
            public void onStart() {

            }

            @Override
            public void onEnd() {
                String query = viewModel.getQuery().getValue();
                kinopoiskAPI.getListSearch(query, ++page, new KinopoiskAPI.RequestCallbackCollection() {
                    @Override
                    public void onSuccess(Collection collectns) {
                        if (collection == null) {
                            collection = collectns;
                        } else {
                            collection.getItems().addAll(collectns.getItems());
                        }
                    }

                    @Override
                    public void onFailure(IOException e) {
                        e.printStackTrace();
                    }

                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void finish() {
                        viewModel.setCollection(collection);
                        viewModel.getPage().setValue(page);
                        if (adapter == null) {
                            adapter = new AdapterRecyclerViewItem1();
                            adapter.setCollection(collection);
                            rvSearch.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        } else {
                            adapter.setCollection(collection);
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        });


        rvSearch.addOnItemTouchListener(new MyRecyclerViewItemTouchListener(getContext(), rvSearch, new MyRecyclerViewItemTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView.ViewHolder holder, View view, int position) {
                Bundle bundle = new Bundle();
                if (position <= -1) return;
                bundle.putInt("kinopoisk_id", collection.getItems().get(position).getKinopoiskId());
                Navigation.findNavController(view).navigate(R.id.action_searchFragment_to_mainFilmFragment, bundle);
            }

            @Override
            public void onLongItemClick(RecyclerView.ViewHolder holder, View view, int position) {

            }
        }));


        return binding.getRoot();
    }


}