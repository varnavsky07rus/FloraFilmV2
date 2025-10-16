package com.alaka_ala.florafilm.ui.fragments.film.vp_fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentTorrentsFilmBinding;
import com.alaka_ala.florafilm.ui.fragments.download_manager.adapter.TorrentItemViewHolder;
import com.alaka_ala.florafilm.ui.fragments.film.view_model.MainFilmViewModel;
import com.alaka_ala.florafilm.ui.util.api.jacred.JacredTor;
import com.alaka_ala.florafilm.ui.util.coreTorrent.TorrentSessionService;
import com.alaka_ala.florafilm.ui.util.coreTorrent.interfaces.UpdateDataListener;
import com.alaka_ala.florafilm.ui.util.coreTorrent.models.Torrent;
import com.alaka_ala.florafilm.ui.util.coreTorrent.utils.MagnetLinkParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TorrentsFilmFragment extends Fragment {
    private FragmentTorrentsFilmBinding binding;
    private RecyclerView rvTorrentsFilm;
    private static int kinopoisk_id;
    // Убираем ссылку на сервис отсюда, она будет в адаптере

    private AdapterTorrentsFilm adapterTorrentsFilm;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTorrentsFilmBinding.inflate(inflater, container, false);
        MainFilmViewModel film = new ViewModelProvider(requireActivity()).get(MainFilmViewModel.class);
        kinopoisk_id = film.getKinopoiskId();

        setupRecyclerView();
        loadTorrents();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        rvTorrentsFilm = binding.rvTorrentsFilm;
        rvTorrentsFilm.setLayoutManager(new LinearLayoutManager(getContext()));
        // Не создаем адаптер здесь, а только после получения данных
    }

    private void loadTorrents() {
        JacredTor jacredTor = new JacredTor();
        jacredTor.query("kp" + kinopoisk_id, new JacredTor.SearchCallback() {
            @Override
            public void onSuccess(List<JacredTor.JacredData> data) {
                if (getContext() == null) return; // Проверка, что фрагмент еще жив
                // Создаем и устанавливаем адаптер
                adapterTorrentsFilm = new AdapterTorrentsFilm(data);
                rvTorrentsFilm.setAdapter(adapterTorrentsFilm);
                // Регистрируем слушатель ПОСЛЕ создания адаптера
                adapterTorrentsFilm.registerListener();
            }

            @Override
            public void onLoading(int position, int count, int progres) {}

            @Override
            public void finish() {}

            @Override
            public void onError(String msgError, JacredTor.SearchCallback sc) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Отписываемся от обновлений, чтобы избежать утечек памяти
        if (adapterTorrentsFilm != null) {
            adapterTorrentsFilm.unregisterListener();
        }
        // Обнуляем ссылки
        if (rvTorrentsFilm != null) {
            rvTorrentsFilm.setAdapter(null);
        }
        adapterTorrentsFilm = null;
        binding = null;
    }

    // Внутренний класс адаптера
    private static class AdapterTorrentsFilm extends RecyclerView.Adapter<TorrentItemViewHolder> implements UpdateDataListener {
        private final List<JacredTor.JacredData> data;
        // Хранит последние актуальные данные для каждого торрента
        private final Map<String, Torrent> torrentsState = new ConcurrentHashMap<>();
        // Хранит активные (видимые на экране) ViewHolder'ы
        private final Map<String, TorrentItemViewHolder> activeHolders = new ConcurrentHashMap<>();
        private final String LISTENER_KEY = "AdapterTorrentsFilm";

        public AdapterTorrentsFilm(List<JacredTor.JacredData> data) {
            this.data = data;
        }

        public void registerListener() {
            TorrentSessionService service = TorrentSessionService.getInstance();
            if (service != null) {
                service.addListener(LISTENER_KEY, this);
            }
        }

        public void unregisterListener() {
            TorrentSessionService service = TorrentSessionService.getInstance();
            if (service != null) {
                service.removeListener(LISTENER_KEY);
            }
            activeHolders.clear(); // Очищаем холдеры при уничтожении
        }

        @NonNull
        @Override
        public TorrentItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_torrent, parent, false);
            return new TorrentItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TorrentItemViewHolder holder, int position) {
            JacredTor.JacredData item = data.get(position);
            String btih = MagnetLinkParser.extractBtih(item.getMagnet());
            holder.setBtih(btih); // Важно для идентификации холдера

            holder.getTorrentName().setText(item.getName());
            holder.getTorrentSize().setText(formatFileSize(item.getSize()));

            holder.getButtonStart().setOnClickListener(view -> {
                new Thread(() -> {
                    TorrentSessionService service = TorrentSessionService.getInstance();
                    if (service == null) return;
                    service.startdl(kinopoisk_id, item.getMagnet());
                }).start();
            });

            // Привязываем актуальные данные из нашего локального хранилища состояний
            updateHolderViews(holder, torrentsState.get(btih));
        }

        /**
         * Вызывается, когда ViewHolder появляется на экране.
         */
        @Override
        public void onViewAttachedToWindow(@NonNull TorrentItemViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            if (holder.getBtih() != null) {
                activeHolders.put(holder.getBtih(), holder);
                // Сразу обновляем вид, как только он появился
                updateHolderViews(holder, torrentsState.get(holder.getBtih()));
            }
        }

        /**
         * Вызывается, когда ViewHolder уходит с экрана (скроллится).
         */
        @Override
        public void onViewDetachedFromWindow(@NonNull TorrentItemViewHolder holder) {
            super.onViewDetachedFromWindow(holder);
            if (holder.getBtih() != null) {
                activeHolders.remove(holder.getBtih()); // Удаляем из активных
            }
        }

        @Override
        public int getItemCount() {
            return data != null ? data.size() : 0;
        }

        @Override
        public void onUpdatedTorrent(Torrent torrent) {
            if (torrent == null || torrent.getHashBtih() == null) return;

            // 1. Сохраняем последнее известное состояние торрента
            torrentsState.put(torrent.getHashBtih(), torrent);

            // 2. Ищем активный (видимый) холдер для этого торрента
            TorrentItemViewHolder holder = activeHolders.get(torrent.getHashBtih());

            // 3. Если холдер найден (т.е. он на экране), обновляем его напрямую
            if (holder != null) {
                new Handler(Looper.getMainLooper()).post(() -> updateHolderViews(holder, torrent));
            }
        }

        /**
         * Централизованный метод для обновления View внутри ViewHolder.
         * @param holder ViewHolder для обновления.
         * @param torrent Данные для отображения.
         */
        @SuppressLint("SetTextI18n")
        private void updateHolderViews(@NonNull TorrentItemViewHolder holder, @Nullable Torrent torrent) {
            if (torrent != null) {
                holder.getTorrentProgress().setText(torrent.getProgress() + "%");
                holder.getProgressBarTorrent().setProgress(torrent.getProgress());
                holder.getTorrentStatus().setText(formatStatus(torrent));
            } else {
                // Состояние по умолчанию, если данных о торренте еще нет
                holder.getTorrentProgress().setText("0%");
                holder.getProgressBarTorrent().setProgress(0);
                holder.getTorrentStatus().setText("Не скачан");
            }
        }

        /**
         * Форматирует статус для отображения, добавляя скорость.
         */
        private String formatStatus(Torrent torrent) {
            String state = torrent.getState();
            if ("DOWNLOADING".equals(state)) {
                return "Загрузка: " + formatFileSize(torrent.getDownloadRate()) + "/с";
            }
            if ("SEEDING".equals(state) || "CHECKING_FILES".equals(state)) {
                return "Раздача: " + formatFileSize(torrent.getUploadRate()) + "/с";
            }
            return state; // Возвращаем как есть для PAUSED, FINISHED и т.д.
        }

        private String formatFileSize(long bytes) {
            if (bytes < 1024) return bytes + " B";
            int unit = 1024;
            String[] units = {"KB", "MB", "GB", "TB"};
            int exp = (int) (Math.log(bytes) / Math.log(unit));
            if (exp < 1) return String.format("%d B", bytes);
            return String.format("%.1f %s", bytes / Math.pow(unit, exp), units[exp - 1]);
        }
    }
}