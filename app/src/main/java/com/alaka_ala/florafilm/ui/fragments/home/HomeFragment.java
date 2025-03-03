package com.alaka_ala.florafilm.ui.fragments.home;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentHomeBinding;
import com.alaka_ala.florafilm.ui.util.adapters.RecyclerViewAdapterItem1;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.KinopoiskAPI;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.Collection;
import com.alaka_ala.florafilm.ui.util.listeners.MyRecyclerViewItemTouchListener;
import com.alaka_ala.florafilm.ui.util.listeners.MyRecyclerViewScrollListener;

import java.io.IOException;


public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    private int pagePopularAll = 0;
    private RecyclerViewAdapterItem1 adapterPopAll;
    private RecyclerView recyclerViewPopAll;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        HomeViewModel viewModel = new ViewModelProvider(this).get(HomeViewModel.class);


        KinopoiskAPI kinopoiskAPI = new KinopoiskAPI(getResources().getString(R.string.api_key_kinopoisk));


        recyclerViewPopAll = binding.fragmentHomeIncludePopularAl.recyclerViewTitleHomeCategory;

        if (viewModel.getPagePopularAllMutableLiveData().getValue() != null) {
            viewModel.getCollectionMutableLiveData().observe(getViewLifecycleOwner(), adapterPopAll::setCollection);
            if (adapterPopAll == null) {
                adapterPopAll = new RecyclerViewAdapterItem1();
            }
            if (recyclerViewPopAll.getLayoutManager() == null) {
                recyclerViewPopAll.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            }
            if (recyclerViewPopAll.getAdapter() == null) {
                recyclerViewPopAll.setAdapter(adapterPopAll);
            }
            adapterPopAll.notifyDataSetChanged();
        }

        pagePopularAll = viewModel.getPagePopularAllMutableLiveData().getValue() == null ? 0 : viewModel.getPagePopularAllMutableLiveData().getValue();
        KinopoiskAPI.RequestCallbackCollection requestCallbackCollection = new KinopoiskAPI.RequestCallbackCollection() {
            @Override
            public void onSuccess(Collection collection) {
                viewModel.addDataCollectionPopularAll(collection);
                viewModel.getPagePopularAllMutableLiveData().setValue(pagePopularAll);
                // Новинки (Фильмы/Сериалы)
                if (adapterPopAll == null) {
                    adapterPopAll = new RecyclerViewAdapterItem1();
                }
                if (recyclerViewPopAll.getLayoutManager() == null) {
                    recyclerViewPopAll.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                }
                if (recyclerViewPopAll.getAdapter() == null) {
                    recyclerViewPopAll.setAdapter(adapterPopAll);
                }

                if (getView() != null) {
                    viewModel.getCollectionMutableLiveData().observe(getViewLifecycleOwner(), adapterPopAll::setCollection);
                }
            }

            @Override
            public void onFailure(IOException e) {

            }

            @Override
            public void finish() {
                if (adapterPopAll != null){
                    adapterPopAll.notifyDataSetChanged();
                }
            }
        };
        recyclerViewPopAll.addOnScrollListener(new MyRecyclerViewScrollListener(MyRecyclerViewScrollListener.HORIZONTAL) {
            @Override
            public void onStart() {

            }

            @Override
            public void onEnd() {
                kinopoiskAPI.getListTopPopularAll(++pagePopularAll, requestCallbackCollection);
            }
        });
        recyclerViewPopAll.addOnItemTouchListener(new MyRecyclerViewItemTouchListener(getContext(), recyclerViewPopAll, new MyRecyclerViewItemTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView.ViewHolder holder, View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putInt("kinopoisk_id", viewModel.getCollectionMutableLiveData().getValue().getItems().get(position).getKinopoiskId());
                Navigation.findNavController(view).navigate(R.id.action_navHomeFragment_to_mainFilmFragment, bundle);
            }

            @Override
            public void onLongItemClick(RecyclerView.ViewHolder holder, View view, int position) {

            }
        }));
        kinopoiskAPI.getListTopPopularAll(++pagePopularAll, requestCallbackCollection);




        // Inflate the layout for this fragment
        return binding.getRoot();
    }


}