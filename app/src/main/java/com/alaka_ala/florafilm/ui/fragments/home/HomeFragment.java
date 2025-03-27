package com.alaka_ala.florafilm.ui.fragments.home;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentHomeBinding;
import com.alaka_ala.florafilm.ui.util.adapters.AdapterRecyclerViewItem1;
import com.alaka_ala.florafilm.ui.util.api.collapse.HlsProcessor;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.KinopoiskAPI;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.Collection;
import com.alaka_ala.florafilm.ui.util.listeners.MyRecyclerViewItemTouchListener;
import com.alaka_ala.florafilm.ui.util.listeners.MyRecyclerViewScrollListener;

import java.io.IOException;


public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    private int pagePopularAll = 0;
    private int pageMovie = 0;
    private int pageSerial = 0;

    private AdapterRecyclerViewItem1 adapterPopAll;
    private AdapterRecyclerViewItem1 adapterMovie;
    private AdapterRecyclerViewItem1 adapterSerial;

    private RecyclerView recyclerViewPopAll;
    private RecyclerView recyclerViewTitleHomeCategoryMovie;
    private RecyclerView recyclerViewTitleHomeCategorySerial;




    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        HomeViewModel viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        KinopoiskAPI kinopoiskAPI = new KinopoiskAPI(getResources().getString(R.string.api_key_kinopoisk));


        recyclerViewPopAll = binding.fragmentHomeIncludePopularAl.recyclerViewTitleHomeCategory;
        recyclerViewTitleHomeCategoryMovie = binding.fragmentHomeIncludeMovie.recyclerViewTitleHomeCategoryMovie;
        recyclerViewTitleHomeCategorySerial = binding.fragmentHomeIncludeSerial.recyclerViewTitleHomeCategorySerial;


        // TODO: 20.03.202 6:23 - АКТИВНАЯ ОШИБКА
        //  ########################################################################################
        //  Обнаружена ошибка (Действия вызывающие ошибку (!)
        //  ########################################################################################
        //  Переход во вкладку избранное ->
        //  Переход на любой фильм из избранного ->
        //  смена темы на любую ->
        //  возврат назад до главной ->
        //  ошибка (!)
        //  ########################################################################################
        //  (Описание ошибки:
        //  При создании адаптера на RecyclerView
        //  в адаптер передаются пустые данные
        //  из-за чего происходит ошибка
        //  когда адаптер пытается получить кол-во элементов списка)
        //  #### ВОЗМОЖНО ПРОБЛЕМУ РЕШИЛ ОБРАБОТАВ В АДАПТЕРЕ РАЗМЕР МАССИВА, если null то возвращает 0 элементов списка 22.03.25 02:05
        //  ########################################################################################
        // Фильмы/сериалы
        if (viewModel.getPagePopularAllMutableLiveData().getValue() != null) {
            if (adapterPopAll == null) {
                adapterPopAll = new AdapterRecyclerViewItem1();
            }
            viewModel.getCollectionMutableLiveDataPopularAll().observe(getViewLifecycleOwner(), adapterPopAll::setCollection);
            if (recyclerViewPopAll.getLayoutManager() == null) {
                recyclerViewPopAll.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            }
            if (recyclerViewPopAll.getAdapter() == null) {
                recyclerViewPopAll.setAdapter(adapterPopAll);
            }
            adapterPopAll.notifyDataSetChanged();
        }
        // Фильмы
        if (viewModel.getPageMovieMutableLiveData().getValue() != null) {
            if (adapterMovie == null) {
                adapterMovie = new AdapterRecyclerViewItem1();
            }
            if (recyclerViewTitleHomeCategoryMovie.getLayoutManager() == null) {
                recyclerViewTitleHomeCategoryMovie.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            }
            if (recyclerViewTitleHomeCategoryMovie.getAdapter() == null) {
                recyclerViewTitleHomeCategoryMovie.setAdapter(adapterMovie);
            }
            adapterMovie.notifyDataSetChanged();
        }
        // Сериалы
        if (viewModel.getPageSerialMutableLiveData().getValue() != null) {
            if (adapterSerial == null) {
                adapterSerial = new AdapterRecyclerViewItem1();
            }
            if (recyclerViewTitleHomeCategorySerial.getLayoutManager() == null) {
                recyclerViewTitleHomeCategorySerial.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            }
            if (recyclerViewTitleHomeCategorySerial.getAdapter() == null) {
                recyclerViewTitleHomeCategorySerial.setAdapter(adapterSerial);
            }
            adapterSerial.notifyDataSetChanged();
        }


        KinopoiskAPI.RequestCallbackCollection requestCallbackCollection = new KinopoiskAPI.RequestCallbackCollection() {
            @Override
            public void onSuccess(Collection collection) {
                switch (collection.getTitleCollection()) {
                    case "Популярные фильмы/сериалы":
                        viewModel.addDataCollectionPopularAll(collection);
                        viewModel.getPagePopularAllMutableLiveData().setValue(pagePopularAll);
                        // Новинки (Фильмы/Сериалы)
                        if (adapterPopAll == null) {
                            adapterPopAll = new AdapterRecyclerViewItem1();
                        }
                        if (recyclerViewPopAll.getLayoutManager() == null) {
                            recyclerViewPopAll.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                        }
                        if (recyclerViewPopAll.getAdapter() == null) {
                            recyclerViewPopAll.setAdapter(adapterPopAll);
                        }

                        if (getView() != null) {
                            viewModel.getCollectionMutableLiveDataPopularAll().observe(getViewLifecycleOwner(), adapterPopAll::setCollection);
                        }
                        break;
                    case "Топ 250 фильмов":
                        viewModel.addDataCollectionMovie(collection);
                        viewModel.getPageMovieMutableLiveData().setValue(pageMovie);
                        // Фильмы
                        if (adapterMovie == null) {
                            adapterMovie = new AdapterRecyclerViewItem1();
                        }
                        if (recyclerViewTitleHomeCategoryMovie.getLayoutManager() == null) {
                            recyclerViewTitleHomeCategoryMovie.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                        }
                        if (recyclerViewTitleHomeCategoryMovie.getAdapter() == null) {
                            recyclerViewTitleHomeCategoryMovie.setAdapter(adapterMovie);
                        }

                        if (getView() != null) {
                            viewModel.getCollectionMutableLiveDataMovie().observe(getViewLifecycleOwner(), adapterMovie::setCollection);
                        }
                        break;
                    case "Топ 250 сериалов":
                        viewModel.addDataCollectionSerial(collection);
                        viewModel.getPageSerialMutableLiveData().setValue(pageSerial);
                        // Сериалы
                        if (adapterSerial == null) {
                            adapterSerial = new AdapterRecyclerViewItem1();
                        }
                        if (recyclerViewTitleHomeCategorySerial.getLayoutManager() == null) {
                            recyclerViewTitleHomeCategorySerial.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                        }
                        if (recyclerViewTitleHomeCategorySerial.getAdapter() == null) {
                            recyclerViewTitleHomeCategorySerial.setAdapter(adapterSerial);
                        }
                        if (getView() != null) {
                            viewModel.getCollectionMutableLiveDataSerial().observe(getViewLifecycleOwner(), adapterSerial::setCollection);
                        }
                        break;
                }

            }

            @Override
            public void onFailure(IOException e) {

            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void finish() {
                if (adapterPopAll != null){
                    adapterPopAll.notifyDataSetChanged();
                }
                if (adapterMovie != null){
                    adapterMovie.notifyDataSetChanged();
                }
                if (adapterSerial != null){
                    adapterSerial.notifyDataSetChanged();
                }
            }
        };


        // Фильмы Сериалы
        pagePopularAll = viewModel.getPagePopularAllMutableLiveData().getValue() == null ? 0 : viewModel.getPagePopularAllMutableLiveData().getValue();
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
                if (position <= -1) return;
                bundle.putInt("kinopoisk_id", viewModel.getCollectionMutableLiveDataPopularAll().getValue().getItems().get(position).getKinopoiskId());
                Navigation.findNavController(view).navigate(R.id.action_navHomeFragment_to_mainFilmFragment, bundle);
            }

            @Override
            public void onLongItemClick(RecyclerView.ViewHolder holder, View view, int position) {

            }
        }));
        kinopoiskAPI.getListTopPopularAll(++pagePopularAll, requestCallbackCollection);


        // Фильмы
        pageMovie = viewModel.getPageMovieMutableLiveData().getValue() == null ? 0 : viewModel.getPageMovieMutableLiveData().getValue();
        recyclerViewTitleHomeCategoryMovie.addOnScrollListener(new MyRecyclerViewScrollListener(MyRecyclerViewScrollListener.HORIZONTAL) {
            @Override
            public void onStart() {

            }

            @Override
            public void onEnd() {
                kinopoiskAPI.getListTop250Movies(++pageMovie, requestCallbackCollection);
            }
        });
        recyclerViewTitleHomeCategoryMovie.addOnItemTouchListener(new MyRecyclerViewItemTouchListener(getContext(), recyclerViewTitleHomeCategoryMovie, new MyRecyclerViewItemTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView.ViewHolder holder, View view, int position) {
                Bundle bundle = new Bundle();
                if (position <= -1) return;
                bundle.putInt("kinopoisk_id", viewModel.getCollectionMutableLiveDataMovie().getValue().getItems().get(position).getKinopoiskId());
                Navigation.findNavController(view).navigate(R.id.action_navHomeFragment_to_mainFilmFragment, bundle);
            }

            @Override
            public void onLongItemClick(RecyclerView.ViewHolder holder, View view, int position) {

            }
        }));
        kinopoiskAPI.getListTop250Movies(++pageMovie, requestCallbackCollection);


        // Сериалы
        pageSerial = viewModel.getPageSerialMutableLiveData().getValue() == null ? 0 : viewModel.getPageSerialMutableLiveData().getValue();
        recyclerViewTitleHomeCategorySerial.addOnScrollListener(new MyRecyclerViewScrollListener(MyRecyclerViewScrollListener.HORIZONTAL) {
            @Override
            public void onStart() {

            }

            @Override
            public void onEnd() {
                kinopoiskAPI.getListTop250TVShows(++pageSerial, requestCallbackCollection);
            }
        });
        recyclerViewTitleHomeCategorySerial.addOnItemTouchListener(new MyRecyclerViewItemTouchListener(getContext(), recyclerViewTitleHomeCategorySerial, new MyRecyclerViewItemTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView.ViewHolder holder, View view, int position) {
                Bundle bundle = new Bundle();
                if (position <= -1) return;
                bundle.putInt("kinopoisk_id", viewModel.getCollectionMutableLiveDataSerial().getValue().getItems().get(position).getKinopoiskId());
                Navigation.findNavController(view).navigate(R.id.action_navHomeFragment_to_mainFilmFragment, bundle);
            }

            @Override
            public void onLongItemClick(RecyclerView.ViewHolder holder, View view, int position) {

            }
        }));
        kinopoiskAPI.getListTop250TVShows(++pageSerial, requestCallbackCollection);


        /*Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HlsProcessor.getHls();
            }
        });
        thread.start();*/





        // Inflate the layout for this fragment
        return binding.getRoot();
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add("Избранное").setIcon(getContext().getDrawable(R.drawable.round_favorite_border_24)).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add("Поиск").setIcon(getContext().getDrawable(R.drawable.rounded_search_24)).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (getContext() == null) return super.onOptionsItemSelected(item);
        if (item.getTitle() == null) return super.onOptionsItemSelected(item);
        if (item.getTitle().equals("Поиск")) {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_navHomeFragment_to_searchFragment);
        } else if (item.getTitle().equals("Избранное")) {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_navHomeFragment_to_navFavoriteFilmFragment);
        }
        return super.onOptionsItemSelected(item);
    }



}