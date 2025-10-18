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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TorrentsFilmFragment extends Fragment {
    private FragmentTorrentsFilmBinding binding;
    private RecyclerView rvTorrentsFilm;
    private static int kinopoisk_id;
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
    }

    private void loadTorrents() {
        JacredTor jacredTor = new JacredTor();
        jacredTor.query("kp" + kinopoisk_id, new JacredTor.SearchCallback() {
            @Override
            public void onSuccess(List<JacredTor.JacredData> data) {
                if (getContext() == null) return;
                adapterTorrentsFilm = new AdapterTorrentsFilm(data);
                rvTorrentsFilm.setAdapter(adapterTorrentsFilm);
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
        if (adapterTorrentsFilm != null) {
            adapterTorrentsFilm.unregisterListener();
        }
        if (rvTorrentsFilm != null) {
            rvTorrentsFilm.setAdapter(null);
        }
        adapterTorrentsFilm = null;
        binding = null;
    }

    private static class AdapterTorrentsFilm extends RecyclerView.Adapter<TorrentItemViewHolder> implements UpdateDataListener {
        private final List<JacredTor.JacredData> data;
        private final Map<String, Torrent> torrentsState = new ConcurrentHashMap<>();
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
            activeHolders.clear();
        }

        @NonNull
        @Override
        public TorrentItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_torrent, parent, false);
            view.findViewById(R.id.llSeedersAndPeersState).setVisibility(View.VISIBLE);
            return new TorrentItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TorrentItemViewHolder holder, int position) {
            JacredTor.JacredData item = data.get(position);
            String btih = MagnetLinkParser.extractBtih(item.getMagnet());
            holder.setBtih(btih);

            // Статические данные
            holder.getTorrentName().setText(item.getName() + " (" + formatFileSize(item.getSize()) + ")");
            holder.getTorrentSize().setText("");
            holder.getPeers().setText(String.valueOf(item.getPir()));
            holder.getSeeders().setText(String.valueOf(item.getSid()));

            // Динамические данные
            Torrent currentTorrentState = torrentsState.get(btih);
            updateDynamicViews(holder, currentTorrentState);

            holder.itemView.setOnClickListener(v -> {
                showActionDialog(v.getContext(), item, torrentsState.get(btih));
            });
        }

        @Override
        public void onBindViewHolder(@NonNull TorrentItemViewHolder holder, int position, @NonNull List<Object> payloads) {
            if (payloads.isEmpty()) {
                onBindViewHolder(holder, position);
            } else {
                for (Object payload : payloads) {
                    if (payload instanceof Torrent) {
                        updateDynamicViews(holder, (Torrent) payload);
                    } else if (payload == null) {
                        updateDynamicViews(holder, null);
                    }
                }
            }
        }

        private void showActionDialog(android.content.Context context, JacredTor.JacredData item, @Nullable Torrent currentTorrentState) {
            TorrentSessionService service = TorrentSessionService.getInstance();
            if (service == null) return;

            String btih = MagnetLinkParser.extractBtih(item.getMagnet());
            ArrayList<String> options = new ArrayList<>();

            if (currentTorrentState != null) {
                String state = currentTorrentState.getState();
                if ("PAUSED".equalsIgnoreCase(state) || "FINISHED".equalsIgnoreCase(state)) {
                    options.add("Возобновить");
                } else if ("DOWNLOADING".equalsIgnoreCase(state) ||
                        "SEEDING".equalsIgnoreCase(state) ||
                        "DOWNLOADING_METADATA".equalsIgnoreCase(state) ||
                        "CONNECTING...".equalsIgnoreCase(state) ||
                        "CHECKING_FILES".equalsIgnoreCase(state) ||
                        "ALLOCATING".equalsIgnoreCase(state) ||
                        "CHECKING_RESUME_DATA".equalsIgnoreCase(state)) {
                    options.add("Поставить на паузу");
                }
                options.add("Удалить торрент");
                options.add("Удалить торрент и файлы");
            } else {
                options.add("Скачать");
            }

            new MaterialAlertDialogBuilder(context)
                    .setTitle(item.getName())
                    .setItems(options.toArray(new String[0]), (dialog, which) -> {
                        String selectedOption = options.get(which);
                        switch (selectedOption) {
                            case "Скачать":
                                service.startdl(kinopoisk_id, item.getMagnet());
                                break;
                            case "Поставить на паузу":
                                service.pauseTorrent(btih);
                                updateTorrentStateOptimistically(btih, "PAUSED");
                                break;
                            case "Возобновить":
                                service.resumeTorrent(btih);
                                updateTorrentStateOptimistically(btih, "DOWNLOADING");
                                break;
                            case "Удалить торрент":
                                confirmAndRemove(context, service, btih, false);
                                break;
                            case "Удалить торрент и файлы":
                                confirmAndRemove(context, service, btih, true);
                                break;
                        }
                    })
                    .show();
        }

        private void updateTorrentStateOptimistically(String btih, String newState) {
            new Handler(Looper.getMainLooper()).post(() -> {
                Torrent oldTorrent = torrentsState.get(btih);
                int index = findIndexOfBtih(btih);

                if (index != -1) {
                    Torrent optimisticTorrent = new Torrent(
                            oldTorrent != null ? oldTorrent.getName() : "",
                            oldTorrent != null ? oldTorrent.getSize() : 0,
                            oldTorrent != null ? oldTorrent.getMagnet() : "",
                            btih,
                            oldTorrent != null ? oldTorrent.getPathFile() : "",
                            oldTorrent != null ? oldTorrent.getProgress() : 0,
                            newState,
                            newState.equals("PAUSED") ? 0 : (oldTorrent != null ? oldTorrent.getDownloadRate() : 0),
                            newState.equals("PAUSED") ? 0 : (oldTorrent != null ? oldTorrent.getUploadRate() : 0),
                            oldTorrent != null ? oldTorrent.getBenCode() : new byte[0]
                    );
                    torrentsState.put(btih, optimisticTorrent);
                    notifyItemChanged(index, optimisticTorrent);
                }
            });
        }

        private void confirmAndRemove(android.content.Context context, TorrentSessionService service, String btih, boolean deleteFiles) {
            String message = deleteFiles ? "Вы уверены, что хотите удалить этот торрент вместе со всеми скачанными файлами?" : "Вы уверены, что хотите удалить этот торрент из списка?";
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Подтверждение")
                    .setMessage(message)
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton("Удалить", (dialog, which) -> {
                        service.removeTorrent(btih, deleteFiles);
                        torrentsState.remove(btih);
                        int index = findIndexOfBtih(btih);
                        if (index != -1) {
                            notifyItemChanged(index, null);
                        }
                    })
                    .show();
        }

        @Override
        public void onViewAttachedToWindow(@NonNull TorrentItemViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            if (holder.getBtih() != null) {
                activeHolders.put(holder.getBtih(), holder);
                updateDynamicViews(holder, torrentsState.get(holder.getBtih()));
            }
        }

        @Override
        public void onViewDetachedFromWindow(@NonNull TorrentItemViewHolder holder) {
            super.onViewDetachedFromWindow(holder);
            if (holder.getBtih() != null) {
                activeHolders.remove(holder.getBtih());
            }
        }

        @Override
        public int getItemCount() {
            return data != null ? data.size() : 0;
        }

        @Override
        public void onUpdatedTorrent(Torrent torrent) {
            if (torrent == null || torrent.getHashBtih() == null) return;
            torrentsState.put(torrent.getHashBtih(), torrent);

            int index = findIndexOfBtih(torrent.getHashBtih());

            if (index != -1) {
                new Handler(Looper.getMainLooper()).post(() -> notifyItemChanged(index, torrent));
            }
        }

        private int findIndexOfBtih(String btih) {
            if (btih == null) return -1;
            for (int i = 0; i < data.size(); i++) {
                String itemBtih = MagnetLinkParser.extractBtih(data.get(i).getMagnet());
                if (btih.equals(itemBtih)) {
                    return i;
                }
            }
            return -1;
        }

        @SuppressLint("SetTextI18n")
        private void updateDynamicViews(@NonNull TorrentItemViewHolder holder, @Nullable Torrent torrent) {
            if (torrent != null) {
                String state = torrent.getState();
                boolean showProgress =
                        "DOWNLOADING".equalsIgnoreCase(state) ||
                                "SEEDING".equalsIgnoreCase(state) ||
                                "PAUSED".equalsIgnoreCase(state) ||
                                "FINISHED".equalsIgnoreCase(state) ||
                                "CHECKING_FILES".equalsIgnoreCase(state);
                holder.getLlProgress().setVisibility(showProgress ? View.VISIBLE : View.GONE);

                String speedText = "";
                String statusText;

                if ("DOWNLOADING".equalsIgnoreCase(state)) {
                    statusText = "Загрузка";
                    speedText = formatFileSize(torrent.getDownloadRate()) + "/с";
                } else if ("SEEDING".equalsIgnoreCase(state)) {
                    statusText = "Раздача";
                    speedText = formatFileSize(torrent.getUploadRate()) + "/с";
                } else if ("CHECKING_FILES".equalsIgnoreCase(state)) {
                    statusText = "Проверка";
                } else if ("PAUSED".equalsIgnoreCase(state)) {
                    statusText = "На паузе";
                } else if ("FINISHED".equalsIgnoreCase(state)) {
                    statusText = "Завершен";
                } else if ("DOWNLOADING_METADATA".equalsIgnoreCase(state)) {
                    statusText = "Загрузка метаданных";
                } else if ("CONNECTING...".equalsIgnoreCase(state)) {
                    statusText = "Соединение...";
                } else {
                    statusText = state;
                }

                holder.getTorrentProgress().setText(torrent.getProgress() + "%");
                holder.getProgressBarTorrent().setProgress(torrent.getProgress());
                holder.getTorrentStatus().setText(statusText);
                holder.getTorrentSpeed().setText(speedText);
            } else {
                // Состояние по умолчанию
                holder.getLlProgress().setVisibility(View.GONE);
                holder.getTorrentProgress().setText("");
                holder.getProgressBarTorrent().setProgress(0);
                holder.getTorrentStatus().setText("");
                holder.getTorrentSpeed().setText("");
            }
        }

        private String formatFileSize(long bytes) {
            if (bytes < 1024) return bytes + " B";
            int unit = 1024;
            String[] units = {"КБ", "МБ", "ГБ", "ТБ"};
            int exp = (int) (Math.log(bytes) / Math.log(unit));
            if (exp < 1) return String.format("%d B", bytes);
            return String.format("%.1f %s", bytes / Math.pow(unit, exp), units[exp - 1]);
        }
    }
}