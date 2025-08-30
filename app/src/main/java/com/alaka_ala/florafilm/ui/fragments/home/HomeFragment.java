package com.alaka_ala.florafilm.ui.fragments.home;

import static com.alaka_ala.florafilm.ui.util.api.kinopoisk.KinopoiskAPI.GenreConstants.ANIMATION;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.CompoundButton;
import android.widget.TextView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentHomeBinding;
import com.alaka_ala.florafilm.ui.util.adapters.AdapterRecyclerViewItem1;
import com.alaka_ala.florafilm.ui.util.api.BanCheker;
import com.alaka_ala.florafilm.ui.util.api.collapse.HlsProcessor;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.KinopoiskAPI;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.Collection;
import com.alaka_ala.florafilm.ui.util.listeners.MyRecyclerViewItemTouchListener;
import com.alaka_ala.florafilm.ui.util.listeners.MyRecyclerViewScrollListener;
import com.alaka_ala.florafilm.ui.util.updater.AppUpdater;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;


public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    private int pagePopularAll = 0;
    private int pageMovie = 0;
    private int pageSerial = 0;
    private int pageAnimations = 0;
    private int pageDrama = 0;
    private int pageKids = 0;


    private AdapterRecyclerViewItem1 adapterPopAll;
    private AdapterRecyclerViewItem1 adapterMovie;
    private AdapterRecyclerViewItem1 adapterSerial;
    private AdapterRecyclerViewItem1 adapterAnimations;
    private AdapterRecyclerViewItem1 adapterDrama;
    private AdapterRecyclerViewItem1 adapterKids;


    private RecyclerView recyclerViewPopAll;
    private RecyclerView recyclerViewTitleHomeCategoryMovie;
    private RecyclerView recyclerViewTitleHomeCategorySerial;
    private RecyclerView recyclerViewHomeCategoryAnimations;
    private RecyclerView recyclerViewHomeCategoryDrama;
    private RecyclerView recyclerViewHomeCategoryKids;


    private AppUpdater appUpdater;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        HomeViewModel viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        viewModel.initData(getContext());

        appUpdater = new AppUpdater(getActivity(), true);
        Chip chipUpdApp = binding.chipUpdApp;
        chipUpdApp.setVisibility(appUpdater.isAvailableUpdate() ? View.VISIBLE : View.GONE);
        chipUpdApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_navHomeFragment_to_navSettingsFragment);
            }
        });


        KinopoiskAPI kinopoiskAPI = new KinopoiskAPI(getResources().getString(R.string.api_key_kinopoisk));


        recyclerViewPopAll = binding.fragmentHomeIncludePopularAl.recyclerViewTitleHomeCategory;
        recyclerViewTitleHomeCategoryMovie = binding.fragmentHomeIncludeMovie.recyclerViewTitleHomeCategoryMovie;
        recyclerViewTitleHomeCategorySerial = binding.fragmentHomeIncludeSerial.recyclerViewTitleHomeCategorySerial;
        recyclerViewHomeCategoryAnimations = binding.fragmentHomeIncludeAnimations.recyclerViewTitleHomeCategoryAnimations;
        recyclerViewHomeCategoryDrama = binding.fragmentHomeIncludeDrama.recyclerViewTitleHomeCategoryDrama;
        recyclerViewHomeCategoryKids = binding.fragmentHomeIncludeKids.recyclerViewTitleHomeCategoryKids;


        // Фильмы/сериалы
        TextView textViewTitleHomeCategory = binding.fragmentHomeIncludePopularAl.textViewTitleHomeCategory;
        // Фильмы
        TextView textViewTitleHomeCategoryMovie = binding.fragmentHomeIncludeMovie.textViewTitleHomeCategoryMovie;
        // Сериалы
        TextView textViewTitleHomeCategorySerial = binding.fragmentHomeIncludeSerial.textViewTitleHomeCategorySerial;
        // Мультфильмы
        TextView textViewTitleHomeCategoryAnimations = binding.fragmentHomeIncludeAnimations.textViewTitleHomeCategoryAnimations;
        // Драмы
        TextView textViewTitleHomeCategoryDrama = binding.fragmentHomeIncludeDrama.textViewTitleHomeCategoryDrama;
        // Детям
        TextView textViewTitleHomeCategoryKids = binding.fragmentHomeIncludeKids.textViewTitleHomeCategoryKids;


        // Фильмы/сериалы
        textViewTitleHomeCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("collection", "Фильмы/сериалы");
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_navHomeFragment_to_collectionFragment2, bundle);
            }
        });
        // Фильмы
        textViewTitleHomeCategoryMovie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("collection", "Фильмы");
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_navHomeFragment_to_collectionFragment2, bundle);
            }
        });
        // Сериалы
        textViewTitleHomeCategorySerial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("collection", "Сериалы");
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_navHomeFragment_to_collectionFragment2, bundle);
            }
        });
        // Мультфильмы
        textViewTitleHomeCategoryAnimations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("collection", "Мультфильмы");
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_navHomeFragment_to_collectionFragment2, bundle);
            }
        });
        // Драма
        textViewTitleHomeCategoryDrama.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("collection", "Драма");
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_navHomeFragment_to_collectionFragment2, bundle);
            }
        });
        // Детям
        textViewTitleHomeCategoryKids.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("collection", "Детям");
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_navHomeFragment_to_collectionFragment2, bundle);
            }
        });

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
            viewModel.getCollectionMutableLiveDataMovie().observe(getViewLifecycleOwner(), adapterMovie::setCollection);
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
            viewModel.getCollectionMutableLiveDataSerial().observe(getViewLifecycleOwner(), adapterSerial::setCollection);
            if (recyclerViewTitleHomeCategorySerial.getLayoutManager() == null) {
                recyclerViewTitleHomeCategorySerial.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            }
            if (recyclerViewTitleHomeCategorySerial.getAdapter() == null) {
                recyclerViewTitleHomeCategorySerial.setAdapter(adapterSerial);
            }
            adapterSerial.notifyDataSetChanged();
        }
        // Мультфильмы
        if (viewModel.getPageAnimationsMutableLiveData().getValue() != null) {
            if (adapterAnimations == null) {
                adapterAnimations = new AdapterRecyclerViewItem1();
            }
            viewModel.getCollectionMutableLiveDataAnimations().observe(getViewLifecycleOwner(), adapterAnimations::setCollection);
            if (recyclerViewHomeCategoryAnimations.getLayoutManager() == null) {
                recyclerViewHomeCategoryAnimations.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            }
            if (recyclerViewHomeCategoryAnimations.getAdapter() == null) {
                recyclerViewHomeCategoryAnimations.setAdapter(adapterAnimations);
            }
            adapterAnimations.notifyDataSetChanged();
        }
        // Драмы
        if (viewModel.getPageDramaMutableLiveData().getValue() != null) {
            if (adapterDrama == null) {
                adapterDrama = new AdapterRecyclerViewItem1();
            }
            viewModel.getCollectionMutableLiveDataDrama().observe(getViewLifecycleOwner(), adapterDrama::setCollection);
            if (recyclerViewHomeCategoryDrama.getLayoutManager() == null) {
                recyclerViewHomeCategoryDrama.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            }
            if (recyclerViewHomeCategoryDrama.getAdapter() == null) {
                recyclerViewHomeCategoryDrama.setAdapter(adapterDrama);
            }
            adapterDrama.notifyDataSetChanged();
        }
        // Детям
        if (viewModel.getPageKidsMutableLiveData().getValue() != null) {
            if (adapterKids == null) {
                adapterKids = new AdapterRecyclerViewItem1();
            }
            viewModel.getCollectionMutableLiveDataKids().observe(getViewLifecycleOwner(), adapterKids::setCollection);
            if (recyclerViewHomeCategoryKids.getLayoutManager() == null) {
                recyclerViewHomeCategoryKids.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            }
            if (recyclerViewHomeCategoryKids.getAdapter() == null) {
                recyclerViewHomeCategoryKids.setAdapter(adapterKids);
            }
            adapterKids.notifyDataSetChanged();
        }

        // Подгрузка данных
        KinopoiskAPI.RequestCallbackCollection requestCallbackCollection = new KinopoiskAPI.RequestCallbackCollection() {
            @Override
            public void onSuccess(Collection collection) {
                switch (collection.getTitleCollection()) {
                    case "Популярные фильмы/сериалы":
                        // Новинки (Фильмы/Сериалы)
                        viewModel.addDataCollectionPopularAll(collection);
                        viewModel.getPagePopularAllMutableLiveData().setValue(pagePopularAll);
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
                        if (adapterPopAll != null) {
                            adapterPopAll.notifyDataSetChanged();
                        }
                        break;
                    case "Топ 250 фильмов":
                        // Фильмы
                        viewModel.addDataCollectionMovie(collection);
                        viewModel.getPageMovieMutableLiveData().setValue(pageMovie);
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
                        if (adapterMovie != null) {
                            adapterMovie.notifyDataSetChanged();
                        }
                        break;
                    case "Топ 250 сериалов":
                        // Сериалы
                        viewModel.addDataCollectionSerial(collection);
                        viewModel.getPageSerialMutableLiveData().setValue(pageSerial);
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
                        if (adapterSerial != null) {
                            adapterSerial.notifyDataSetChanged();
                        }
                        break;
                    case "Мультфильм":
                        // Мультфильмы
                        viewModel.addDataCollectionAnimations(collection);
                        viewModel.getPageAnimationsMutableLiveData().setValue(pageAnimations);
                        if (adapterAnimations == null) {
                            adapterAnimations = new AdapterRecyclerViewItem1();
                        }
                        if (recyclerViewHomeCategoryAnimations.getLayoutManager() == null) {
                            recyclerViewHomeCategoryAnimations.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                        }
                        if (recyclerViewHomeCategoryAnimations.getAdapter() == null) {
                            recyclerViewHomeCategoryAnimations.setAdapter(adapterAnimations);
                        }
                        if (getView() != null) {
                            viewModel.getCollectionMutableLiveDataAnimations().observe(getViewLifecycleOwner(), adapterAnimations::setCollection);
                        }
                        if (adapterAnimations != null) {
                            adapterAnimations.notifyDataSetChanged();
                        }
                        break;
                    case "Драма":
                        // Драмы
                        viewModel.addDataCollectionDrama(collection);
                        viewModel.getPageDramaMutableLiveData().setValue(pageDrama);
                        if (adapterDrama == null) {
                            adapterDrama = new AdapterRecyclerViewItem1();
                        }
                        if (recyclerViewHomeCategoryDrama.getLayoutManager() == null) {
                            recyclerViewHomeCategoryDrama.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                        }
                        if (recyclerViewHomeCategoryDrama.getAdapter() == null) {
                            recyclerViewHomeCategoryDrama.setAdapter(adapterDrama);
                        }
                        if (getView() != null) {
                            viewModel.getCollectionMutableLiveDataDrama().observe(getViewLifecycleOwner(), adapterDrama::setCollection);
                        }
                        if (adapterDrama != null) {
                            adapterDrama.notifyDataSetChanged();
                        }
                        break;
                    case "Детский":
                        // Детям
                        viewModel.addDataCollectionKids(collection);
                        viewModel.getPageKidsMutableLiveData().setValue(pageKids);
                        if (adapterKids == null) {
                            adapterKids = new AdapterRecyclerViewItem1();
                        }
                        if (recyclerViewHomeCategoryKids.getLayoutManager() == null) {
                            recyclerViewHomeCategoryKids.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                        }
                        if (recyclerViewHomeCategoryKids.getAdapter() == null) {
                            recyclerViewHomeCategoryKids.setAdapter(adapterKids);
                        }
                        if (getView() != null) {
                            viewModel.getCollectionMutableLiveDataKids().observe(getViewLifecycleOwner(), adapterKids::setCollection);
                        }
                        if (adapterKids != null) {
                            adapterKids.notifyDataSetChanged();
                        }
                        break;
                }
            }

            @Override
            public void onFailure(IOException e) {
                if (getContext() != null)
                    Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void finish() {
                if (getContext() != null) {
                    viewModel.saveData(getContext());
                }
            }
        };

        /*BanCheker banCheker = new BanCheker();
        banCheker.loadList();*/

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
        if (pagePopularAll == 0) kinopoiskAPI.getListTopPopularAll(++pagePopularAll, requestCallbackCollection);



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
        if (pageMovie == 0) kinopoiskAPI.getListTop250Movies(++pageMovie, requestCallbackCollection);



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
        if (pageSerial == 0) kinopoiskAPI.getListTop250TVShows(++pageSerial, requestCallbackCollection);



        // Мультфильмы
        pageAnimations = viewModel.getPageAnimationsMutableLiveData().getValue() == null ? 0 : viewModel.getPageAnimationsMutableLiveData().getValue();
        recyclerViewHomeCategoryAnimations.addOnScrollListener(new MyRecyclerViewScrollListener(MyRecyclerViewScrollListener.HORIZONTAL) {
            @Override
            public void onStart() {

            }

            @Override
            public void onEnd() {
                kinopoiskAPI.getListFromGenre(ANIMATION, ++pageAnimations, requestCallbackCollection);
            }
        });
        recyclerViewHomeCategoryAnimations.addOnItemTouchListener(new MyRecyclerViewItemTouchListener(getContext(), recyclerViewHomeCategoryAnimations, new MyRecyclerViewItemTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView.ViewHolder holder, View view, int position) {
                Bundle bundle = new Bundle();
                if (position <= -1) return;
                bundle.putInt("kinopoisk_id", viewModel.getCollectionMutableLiveDataAnimations().getValue().getItems().get(position).getKinopoiskId());
                Navigation.findNavController(view).navigate(R.id.action_navHomeFragment_to_mainFilmFragment, bundle);
            }

            @Override
            public void onLongItemClick(RecyclerView.ViewHolder holder, View view, int position) {

            }
        }));
        if (pageAnimations == 0) kinopoiskAPI.getListFromGenre(ANIMATION, ++pageAnimations, requestCallbackCollection);


        // Драмы
        pageDrama = viewModel.getPageDramaMutableLiveData().getValue() == null ? 0 : viewModel.getPageDramaMutableLiveData().getValue();
        recyclerViewHomeCategoryDrama.addOnScrollListener(new MyRecyclerViewScrollListener(MyRecyclerViewScrollListener.HORIZONTAL) {
            @Override
            public void onStart() {

            }

            @Override
            public void onEnd() {
                kinopoiskAPI.getListFromGenre(KinopoiskAPI.GenreConstants.DRAMA, ++pageDrama, requestCallbackCollection);
            }
        });
        recyclerViewHomeCategoryDrama.addOnItemTouchListener(new MyRecyclerViewItemTouchListener(getContext(), recyclerViewHomeCategoryDrama, new MyRecyclerViewItemTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView.ViewHolder holder, View view, int position) {
                Bundle bundle = new Bundle();
                if (position <= -1) return;
                bundle.putInt("kinopoisk_id", viewModel.getCollectionMutableLiveDataDrama().getValue().getItems().get(position).getKinopoiskId());
                Navigation.findNavController(view).navigate(R.id.action_navHomeFragment_to_mainFilmFragment, bundle);
            }

            @Override
            public void onLongItemClick(RecyclerView.ViewHolder holder, View view, int position) {

            }
        }));
        if (pageDrama == 0) kinopoiskAPI.getListFromGenre(KinopoiskAPI.GenreConstants.DRAMA, ++pageDrama, requestCallbackCollection);


        // Детям
        pageKids = viewModel.getPageKidsMutableLiveData().getValue() == null ? 0 : viewModel.getPageKidsMutableLiveData().getValue();
        recyclerViewHomeCategoryKids.addOnScrollListener(new MyRecyclerViewScrollListener(MyRecyclerViewScrollListener.HORIZONTAL) {
            @Override
            public void onStart() {

            }

            @Override
            public void onEnd() {
                kinopoiskAPI.getListFromGenre(KinopoiskAPI.GenreConstants.KIDS, ++pageKids, requestCallbackCollection);
            }
        });
        recyclerViewHomeCategoryKids.addOnItemTouchListener(new MyRecyclerViewItemTouchListener(getContext(), recyclerViewHomeCategoryKids, new MyRecyclerViewItemTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView.ViewHolder holder, View view, int position) {
                Bundle bundle = new Bundle();
                if (position <= -1) return;
                bundle.putInt("kinopoisk_id", viewModel.getCollectionMutableLiveDataKids().getValue().getItems().get(position).getKinopoiskId());
                Navigation.findNavController(view).navigate(R.id.action_navHomeFragment_to_mainFilmFragment, bundle);
            }

            @Override
            public void onLongItemClick(RecyclerView.ViewHolder holder, View view, int position) {

            }
        }));
        if (pageKids == 0) kinopoiskAPI.getListFromGenre(KinopoiskAPI.GenreConstants.KIDS, ++pageKids, requestCallbackCollection);

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