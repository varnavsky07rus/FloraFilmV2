package com.alaka_ala.florafilm.ui.fragments.favorites;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentFavoriteFilmBinding;
import com.alaka_ala.florafilm.ui.fragments.favorites.adapter.FavoriteRecyclerViewAdapter;
import com.alaka_ala.florafilm.ui.util.listeners.MyRecyclerViewItemTouchListener;
import com.alaka_ala.florafilm.ui.util.local.FavoriteMoviesManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class FavoriteFilmFragment extends Fragment {
    private FragmentFavoriteFilmBinding binding;
    private FavoriteMoviesManager favoriteMoviesManager;
    private RecyclerView rvFavoriteFilm;
    private FavoriteRecyclerViewAdapter adapter;
    private SearchView searchViewFavorite;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFavoriteFilmBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        rvFavoriteFilm = binding.rvFavoriteFilm;
        searchViewFavorite = binding.searchViewFavorite;
        favoriteMoviesManager = new FavoriteMoviesManager(getContext());
        rvFavoriteFilm.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new FavoriteRecyclerViewAdapter();
        adapter.setData(favoriteMoviesManager.getFavoriteMovies());
        rvFavoriteFilm.setAdapter(adapter);

        searchViewFavorite.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchFavoriteMovie(s, new SearchCallback() {
                    @Override
                    public void onSearch(List<FavoriteMoviesManager.MovieData> filteredFavorites) {
                        adapter.setData(filteredFavorites);
                        adapter.notifyDataSetChanged();
                    }
                });
                return false;
            }
        });

        searchViewFavorite.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                adapter.setData(favoriteMoviesManager.getFavoriteMovies());
                adapter.notifyDataSetChanged();
                return false;
            }
        });

        rvFavoriteFilm.addOnItemTouchListener(new MyRecyclerViewItemTouchListener(getContext(), rvFavoriteFilm, new MyRecyclerViewItemTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView.ViewHolder holder, View view, int position) {
                Bundle bundle = new Bundle();
                if (position <= -1) return;
                bundle.putInt("kinopoisk_id", favoriteMoviesManager.getFavoriteMovies().get(position).getKinopoiskId());
                Navigation.findNavController(view).navigate(R.id.action_navFavoriteFilmFragment_to_mainFilmFragment, bundle);
            }

            @Override
            public void onLongItemClick(RecyclerView.ViewHolder holder, View view, int position) {

            }
        }));

        return binding.getRoot();
    }


    private void searchFavoriteMovie(String query, SearchCallback callback) {
        Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                if (message.what == 0) {
                    callback.onSearch((List<FavoriteMoviesManager.MovieData>) message.obj);
                }
                return false;
            }
        });
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                List<FavoriteMoviesManager.MovieData> favorites = favoriteMoviesManager.getFavoriteMovies();
                List<FavoriteMoviesManager.MovieData> filteredFavorites = new ArrayList<>();
                Pattern pattern = Pattern.compile(query.toLowerCase(Locale.ROOT), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
                if (favorites != null) {
                    for (FavoriteMoviesManager.MovieData favorite : favorites) {
                        if (pattern.matcher(favorite.getName().toLowerCase(Locale.ROOT)).find()) {
                            filteredFavorites.add(favorite);
                        }
                    }
                    handler.obtainMessage(0, filteredFavorites).sendToTarget();
                }
            }
        });
        thread.start();

    }

    private interface SearchCallback {
        void onSearch(List<FavoriteMoviesManager.MovieData> filteredFavorites);
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add("Поиск избранных").setActionView(searchViewFavorite).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add("Кол-во столбцов").setIcon(R.drawable.baseline_view_column_24);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getTitle() == null) return super.onOptionsItemSelected(item);
        if (item.getTitle().equals("Кол-во столбцов")) {
            MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(getContext()).setTitle("Кол-во столбцов").setItems(new String[]{"2", "3 - default", "4", "5", "6"}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == 0) {
                        rvFavoriteFilm.setLayoutManager(new GridLayoutManager(getContext(), 2));
                    } else if (i == 1) {
                        rvFavoriteFilm.setLayoutManager(new GridLayoutManager(getContext(), 3));
                    } else if (i == 2) {
                        rvFavoriteFilm.setLayoutManager(new GridLayoutManager(getContext(), 4));
                    } else if (i == 3) {
                        rvFavoriteFilm.setLayoutManager(new GridLayoutManager(getContext(), 5));
                    } else if (i == 4) {
                        rvFavoriteFilm.setLayoutManager(new GridLayoutManager(getContext(), 6));
                    }
                }
            }).setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            if (isDarkTheme()) {
                alertDialog.show().getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.white));
            } else {
                alertDialog.show().getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.black));
            }
        }

        return super.onOptionsItemSelected(item);
    }


    private static final String TAG = "FavoriteFilmFragment:Theme Debug";

    // Здесь получаем текущую тему, Светлая или темная : true темная | false светлая
    private boolean isDarkTheme(){
        // Получаем текущий режим темы устройства
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        // Определяем, какая тема используется
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                // Ночной режим выключен, используется светлая тема
                Log.d(TAG, "Current theme: Light");
                return false;
            case Configuration.UI_MODE_NIGHT_YES:
                // Ночной режим включен, используется темная тема
                Log.d(TAG, "Current theme: Dark");
                return true;
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                // Режим темы не определен (редкий случай)
                Log.d(TAG, "Current theme: Undefined");
                return true;
        }
        return true;
    }


}