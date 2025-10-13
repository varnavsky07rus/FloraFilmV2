package com.alaka_ala.florafilm.ui.fragments.film.vp_fragments;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.core.torrent.DigestUtils;
import com.alaka_ala.florafilm.core.torrent.TorrentInfo;
import com.alaka_ala.florafilm.core.torrent.TorrentManager;
import com.alaka_ala.florafilm.databinding.FragmentTorrentFilmBinding;
import com.alaka_ala.florafilm.ui.activities.PlayerExoActivity;
import com.alaka_ala.florafilm.ui.fragments.film.MainFilmFragment;
import com.alaka_ala.florafilm.ui.fragments.film.view_model.MainFilmViewModel;
import com.alaka_ala.florafilm.ui.fragments.settings.SettingsUtils;
import com.alaka_ala.florafilm.ui.util.api.EPData;
import com.alaka_ala.florafilm.ui.util.api.jacred.JacredTor;
import com.alaka_ala.florafilm.ui.util.local.TorrentHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TorrentFilmFragment extends Fragment {
    private FragmentTorrentFilmBinding binding;
    private JacredTor jacredTor;
    private MainFilmViewModel mainFilmViewModel;
    private int kinopoisk_id;
    private RecyclerView rvTorrentFilm;
    private List<JacredTor.JacredData> data;
    private List<JacredTor.JacredData> dataFinal;
    private SearchView searchView;
    private boolean isCreateMenu = false;
    private long seekPosition = 0;

    private TorrentManager torrentManager;
    private boolean isNotFoundTorrent = false;
    private LottieAnimationView lottieNotFoundTorrent;
    private FrameLayout rootNotFoundTorrent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTorrentFilmBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        MainFilmFragment.setViewPagerListener(new MainFilmFragment.ViewPagerListener() {
            @Override
            public void onTransition(int currentPage) {
                isCreateMenu = currentPage == 2;
            }


        });

        rootNotFoundTorrent = binding.rootNotFoundTorrent;
        lottieNotFoundTorrent = binding.lottieNotFoundTorrent;

        torrentManager = TorrentManager.getInstance();

        searchView = binding.svTorrents;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                sortByQuery(s);
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                resetFilters();
                return false;
            }
        });

        mainFilmViewModel = new ViewModelProvider(getActivity()).get(MainFilmViewModel.class);
        kinopoisk_id = mainFilmViewModel.getKinopoiskId();
        jacredTor = new JacredTor();

        rvTorrentFilm = binding.rvTorrentFilm;
        rvTorrentFilm.setLayoutManager(new LinearLayoutManager(getContext()));


        jacredTor.query("kp" + kinopoisk_id, new JacredTor.SearchCallback() {
            @Override
            public void onSuccess(List<JacredTor.JacredData> datas) {
                data = datas;
                dataFinal = data;
                rvTorrentFilm.setAdapter(new AdapterTorrentFilm(data));
                rvTorrentFilm.setItemViewCacheSize(50);
                isNotFoundTorrent = data.isEmpty();
                sortBySid();
            }

            @Override
            public void onLoading(int position, int count, int progres) {

            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void finish() {
                if (rvTorrentFilm.getAdapter() != null) {
                    rvTorrentFilm.getAdapter().notifyDataSetChanged();
                }
                printNotFoundTorrent();

            }

            @Override
            public void onError(String msgError, JacredTor.SearchCallback sc) {
                isNotFoundTorrent = true;
            }
        });

        torrentManager.getTorrentsLiveData().observe(getViewLifecycleOwner(), torrentsMap -> {
            // Проверяем, что адаптер и его данные существуют
            if (rvTorrentFilm.getAdapter() == null) {
                return;
            }
            AdapterTorrentFilm adapter = (AdapterTorrentFilm) rvTorrentFilm.getAdapter();
            List<JacredTor.JacredData> currentData = adapter.getData();
            if (currentData == null || currentData.isEmpty()) {
                return;
            }

            // Обновляем прогресс для элементов
            updateProgressBarAdapter(torrentsMap, currentData, adapter);
        });


        return binding.getRoot();
    }

    private static void updateProgressBarAdapter(Map<String, TorrentInfo> torrentsMap, List<JacredTor.JacredData> currentData, AdapterTorrentFilm adapter) {
        for (Map.Entry<String, TorrentInfo> entry : torrentsMap.entrySet()) {
            String hash = entry.getKey();
            TorrentInfo torrentInfo = entry.getValue();

            // Находим индекс элемента в списке адаптера по хешу
            int index = -1;
            for (int i = 0; i < currentData.size(); i++) {
                if (DigestUtils.createMd5Digest(currentData.get(i).getMagnet()).equals(hash)) {
                    index = i;
                    break;
                }
            }

            if (index != -1) {
                // Уведомляем адаптер об изменении конкретного элемента,
                // передавая новый прогресс в качестве payload.
                adapter.notifyItemChanged(index, torrentInfo.getProgress());
            }
        }
    }

    private void printNotFoundTorrent() {
        if (isNotFoundTorrent) {
            rootNotFoundTorrent.setVisibility(View.VISIBLE);
            lottieNotFoundTorrent.setAnimation(R.raw.not_found);
            if (SettingsUtils.getParamPageEffectAnimation(getContext())) {
                lottieNotFoundTorrent.playAnimation();
            }
        } else {
            rootNotFoundTorrent.setVisibility(View.GONE);
            lottieNotFoundTorrent.pauseAnimation();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        printNotFoundTorrent();
    }

    private class AdapterTorrentFilm extends RecyclerView.Adapter<AdapterTorrentFilm.MyViewHolder> {
        public AdapterTorrentFilm(List<JacredTor.JacredData> data) {
            this.data = data;
        }

        public void setData(List<JacredTor.JacredData> data) {
            this.data = data;
        }

        public List<JacredTor.JacredData> getData() {
            return data;
        }

        private List<JacredTor.JacredData> data;

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_torrent_film, parent, false);
            return new MyViewHolder(view);
        }
        
        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            JacredTor.JacredData jacredData = data.get(position);
            holder.textViewTitleTorrent.setText(jacredData.getVoices().isEmpty() ?
                    jacredData.getName() + " (" + jacredData.getQuality() + "p)" :
                    jacredData.getName() + " (" + jacredData.getQuality() + "p)" + jacredData.getVoices());
            holder.textViewInformationTorrent.setText(
                    !jacredData.getSeasons().isEmpty() ?
                            jacredData.getCreateTime() + " • " + formatSize(jacredData.getSize()) + " • " + jacredData.getTracker() + " • сезон " + jacredData.getSeasons() :
                            jacredData.getCreateTime() + " • " + formatSize(jacredData.getSize()) + " • " + jacredData.getTracker());
            holder.textViewPeers.setText(String.valueOf(jacredData.getPir()));
            holder.textViewSiders.setText(String.valueOf(jacredData.getSid()));
            holder.itemView.setId(position);

            boolean isExistFile = torrentManager.existFileFromCache(getContext(), kinopoisk_id, jacredData.getMagnet());
            if (isExistFile) holder.progressBarDownload.setVisibility(View.VISIBLE);

            // Устанавливаем начальный прогресс
            Map<String, TorrentInfo> torrentsMap = torrentManager.getTorrentsLiveData().getValue();
            int progress = 0;
            if (torrentsMap != null) {
                TorrentInfo info = torrentsMap.get(DigestUtils.createMd5Digest(jacredData.getMagnet()));
                if (info != null) {
                    progress = info.getProgress();
                }
            }
            holder.progressBarDownload.setProgress(progress);

            holder.itemView.findViewById(R.id.materialCardViewItem).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //String magnet = "magnet:?xt=urn:btih:9C38D68035F190F1953F758E5527DDE1C7563B72&tr=http%3A%2F%2Fbt2.t-ru.org%2Fann%3Fmagnet";
                    new MaterialAlertDialogBuilder(getContext())
                            .setTitle("Выберите действие")
                            .setItems(new String[]{"Смотреть", "Открыть с помощью", "Скачать"}, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Смотреть
                                    if (i == 0) {

                                        EPData.Film.Builder filmBuilder = new EPData.Film.Builder();

                                        filmBuilder.setNameFilm(jacredData.getName());
                                        filmBuilder.setId(String.valueOf(kinopoisk_id));
                                        filmBuilder.setPoster(mainFilmViewModel.getItemFilmInfoMap(getContext(), kinopoisk_id).getPosterUrl());
                                        EPData.Film.Translations.Builder translationBuilder = new EPData.Film.Translations.Builder();
                                        translationBuilder.setTitle(String.valueOf(jacredData.getVoices()));
                                        List<Map.Entry<String, String>> videoData = new ArrayList<>();
                                        videoData.add(new AbstractMap.SimpleEntry<>("magnet", jacredData.getMagnet()));
                                        translationBuilder.setVideoData(videoData);
                                        ArrayList<EPData.Film.Translations> translationsArrayList = new ArrayList<>();
                                        EPData.Film.Translations translations = translationBuilder.build();
                                        translationsArrayList.add(translations);
                                        filmBuilder.setTranslations(translationsArrayList);


                                        Intent intent = new Intent(getActivity(), PlayerExoActivity.class);
                                        EPData.Builder builderEPData = new EPData.Builder();
                                        builderEPData.setFilm(filmBuilder.build());
                                        builderEPData.setIndexTranslation(0);
                                        builderEPData.setIndexQuality(0);
                                        builderEPData.setBalancer("magnet");
                                        builderEPData.setFilmInfo(mainFilmViewModel.getItemFilmInfoMap(getContext(), kinopoisk_id));
                                        EPData film = builderEPData.build();
                                        intent.putExtra("epData", film);
                                        getActivity().startActivity(intent);


                                    }
                                    // Открыть с помощью
                                    else if (i == 1) {
                                        TorrentHelper.openMagnetLink(view.getContext(), jacredData.getMagnet());
                                    }
                                    // Скачать
                                    else if (i == 2) {
                                        downloadTorrent(kinopoisk_id, jacredData.getMagnet());
                                    }
                                }
                            }).show();
                }
            });
        }

        // 2. Переопределите этот метод для ЧАСТИЧНЫХ обновлений
        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull List<Object> payloads) {
            if (!payloads.isEmpty()) {
                // Если есть payload, мы обновляем только ProgressBar
                Object payload = payloads.get(0);
                if (payload instanceof Integer) {
                    int progress = (Integer) payload;
                    holder.progressBarDownload.setProgress(progress);
                }
            } else {
                // Если payload пуст, вызываем полное связывание
                super.onBindViewHolder(holder, position, payloads);
            }
        }


        @Override
        public int getItemCount() {
            if (data == null) return 0;
            return Math.min(data.size(), 50);
        }

        private class MyViewHolder extends RecyclerView.ViewHolder {
            private final TextView textViewTitleTorrent;
            private final TextView textViewInformationTorrent;
            private final TextView textViewSiders;
            private final TextView textViewPeers;
            private final ProgressBar progressBarDownload;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewTitleTorrent = itemView.findViewById(R.id.textViewTitleTorrent);
                textViewInformationTorrent = itemView.findViewById(R.id.textViewInformationTorrent);
                textViewSiders = itemView.findViewById(R.id.textViewSiders);
                textViewPeers = itemView.findViewById(R.id.textViewPeers);
                progressBarDownload = itemView.findViewById(R.id.progressBarTorrent);
            }
        }
    }

    private void downloadTorrent(int kinopoisk_id, String magnetLink) {
        String currentTorrentHash = torrentManager.addTorrent(getContext(), magnetLink, kinopoisk_id);
        if (currentTorrentHash != null) {
            if (currentTorrentHash.equals("Already added")) {
                Toast.makeText(getContext(), "Дождитесь окончания предыдущей загрузки...", Toast.LENGTH_SHORT).show();
            } else {
                // Загрузка успешно добавлена в очередь
                Toast.makeText(getContext(), "Загрузка добавлена в очередь", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Неверная magnet-ссылка", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (!isCreateMenu) return;
        menu.add("Сортировать по пирам").setIcon(R.drawable.rounded_sort_24);
        menu.add("Сортировать по сидерам").setIcon(R.drawable.rounded_sort_24);
        menu.add("Сначала новые").setTitle("Сначала новые");
        menu.add("Сначала старые").setTitle("Сначала старые");
        menu.add("Сбросить фильтры").setIcon(R.drawable.rounded_filter_alt_off_24);
        menu.add("Поиск").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setActionView(searchView);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getTitle() == null) return super.onOptionsItemSelected(item);
        if (item.getTitle().equals("Сортировать по пирам")) {
            sortByPeer();
        } else if (item.getTitle().equals("Сортировать по сидерам")) {
            sortBySid();
        } else if (item.getTitle().equals("Сбросить фильтры")) {
            resetFilters();
        } else if (item.getTitle().equals("Поиск")) {

        } else if (item.getTitle().equals("Сначала новые")) {
            sortByDate(SortOrder.NEWEST);
        } else if (item.getTitle().equals("Сначала старые")) {
            sortByDate(SortOrder.OLDEST);
        }

        return super.onOptionsItemSelected(item);
    }

    private void sortByPeer() {
        if (data == null) return;
        Thread threadPeer = new Thread(new Runnable() {
            @Override
            public void run() {
                data.sort((o1, o2) -> o2.getPir() - o1.getPir());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rvTorrentFilm.getAdapter().notifyDataSetChanged();
                    }
                });
            }
        });
        threadPeer.start();
    }

    private void sortBySid() {
        if (data == null) return;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                data.sort((o1, o2) -> o2.getSid() - o1.getSid());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rvTorrentFilm.getAdapter().notifyDataSetChanged();
                    }
                });
            }
        });
        thread.start();
    }

    private void sortByQuery(String query) {
        if (query.isEmpty() || data == null) return;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Создаем Pattern вне цикла, чтобы не пересоздавать его каждый раз
                Pattern pattern = Pattern.compile(query.toLowerCase(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);

                // Создаем новый список для хранения элементов, которые нужно удалить
                List<JacredTor.JacredData> itemsToRemove = new ArrayList<>();

                for (JacredTor.JacredData item : data) {
                    // Получаем имя элемента
                    String itemQuality = String.valueOf(item.getQuality()).toLowerCase();
                    String itemVoices = String.valueOf(item.getVoices()).toLowerCase();
                    String itemName = item.getName().toLowerCase();
                    String itemTracker = item.getTracker().toLowerCase();
                    String title = itemName + " (" + itemQuality + "p)" + itemVoices + " " + itemTracker;

                    // Создаем Matcher для текущего имени
                    Matcher matcher = pattern.matcher(title);

                    // Проверяем, есть ли совпадение
                    if (matcher.find()) {
                        // Если совпадения нет, добавляем элемент в список на удаление
                        itemsToRemove.add(item);
                    }
                    AdapterTorrentFilm adapter = (AdapterTorrentFilm) rvTorrentFilm.getAdapter();
                    adapter.setData(itemsToRemove);
                }


                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rvTorrentFilm.getAdapter().notifyDataSetChanged();
                    }
                });
            }
        });
        thread.start();
    }

    public enum SortOrder {
        NEWEST,
        OLDEST
    }

    private void sortByDate(SortOrder sortOrder) {
        if (data == null) return;

        Thread thread = new Thread(() -> {
            Comparator<JacredTor.JacredData> comparator;
            if (sortOrder == SortOrder.NEWEST) {
                // Сортировка по новинкам (от большего года к меньшему)
                comparator = (o1, o2) -> o2.getRelased() - o1.getRelased();
            } else {
                // Сортировка по старым (от меньшего года к большему)
                comparator = (o1, o2) -> o1.getRelased() - o2.getRelased();
            }

            data.sort(comparator);

            getActivity().runOnUiThread(() -> rvTorrentFilm.getAdapter().notifyDataSetChanged());
        });
        thread.start();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void resetFilters() {
        if (dataFinal == null) return;
        data = dataFinal;
        AdapterTorrentFilm adapter = (AdapterTorrentFilm) rvTorrentFilm.getAdapter();
        if (adapter == null) return;
        adapter.setData(data);
        adapter.notifyDataSetChanged();
    }

    public static String formatSize(long size) {
        if (size <= 0) {
            return "0 B";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }




}