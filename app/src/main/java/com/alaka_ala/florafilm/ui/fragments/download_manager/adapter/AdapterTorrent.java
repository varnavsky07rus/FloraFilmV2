package com.alaka_ala.florafilm.ui.fragments.download_manager.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.ui.util.coreTorrent.TorrentSessionService;
import com.alaka_ala.florafilm.ui.util.coreTorrent.interfaces.UpdateDataListener;
import com.alaka_ala.florafilm.ui.util.coreTorrent.models.Torrent;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AdapterTorrent extends RecyclerView.Adapter<TorrentItemViewHolder> implements UpdateDataListener {

    private final List<Torrent> torrents;
    private final Map<String, TorrentItemViewHolder> activeHolders = new ConcurrentHashMap<>();
    private final String LISTENER_KEY = "DownloadManagerAdapter";

    public AdapterTorrent(List<Torrent> initialTorrents) {
        this.torrents = initialTorrents;
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
        return new TorrentItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TorrentItemViewHolder holder, int position) {
        Torrent torrent = torrents.get(position);
        if (torrent == null) return;
        holder.setBtih(torrent.getHashBtih());
        updateHolderViews(holder, torrent);

        holder.itemView.setOnClickListener(v -> {
            Torrent currentTorrent = findTorrentByBtih(torrent.getHashBtih());
            showManagementDialog(v.getContext(), currentTorrent != null ? currentTorrent : torrent);
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
                }
            }
        }
    }

    private Torrent findTorrentByBtih(String btih) {
        for (Torrent t : torrents) {
            if (t.getHashBtih().equals(btih)) {
                return t;
            }
        }
        return null;
    }

    private void showManagementDialog(Context context, Torrent torrent) {
        TorrentSessionService service = TorrentSessionService.getInstance();
        if (service == null) return;

        ArrayList<String> options = new ArrayList<>();
        String state = torrent.getState();

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

        new MaterialAlertDialogBuilder(context).setTitle(torrent.getName()).setItems(options.toArray(new String[0]), (dialog, which) -> {
            String selectedOption = options.get(which);
            String btih = torrent.getHashBtih();

            switch (selectedOption) {
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
        }).show();
    }

    private void updateTorrentStateOptimistically(String btih, String newState) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Torrent oldTorrent = null;
            int torrentIndex = -1;
            for (int i = 0; i < torrents.size(); i++) {
                if (torrents.get(i).getHashBtih().equals(btih)) {
                    oldTorrent = torrents.get(i);
                    torrentIndex = i;
                    break;
                }
            }

            if (oldTorrent != null) {
                Torrent optimisticTorrent = new Torrent(
                        oldTorrent.getName(),
                        oldTorrent.getSize(),
                        oldTorrent.getMagnet(),
                        oldTorrent.getHashBtih(),
                        oldTorrent.getPathFile(),
                        oldTorrent.getProgress(),
                        newState,
                        newState.equals("PAUSED") ? 0 : oldTorrent.getDownloadRate(),
                        newState.equals("PAUSED") ? 0 : oldTorrent.getUploadRate(),
                        oldTorrent.getBenCode()
                );
                torrents.set(torrentIndex, optimisticTorrent);

                TorrentItemViewHolder holder = activeHolders.get(btih);
                if (holder != null) {
                    updateDynamicViews(holder, optimisticTorrent);
                } else {
                    notifyItemChanged(torrentIndex, optimisticTorrent);
                }
            }
        });
    }

    private void confirmAndRemove(Context context, TorrentSessionService service, String btih, boolean deleteFiles) {
        String message = deleteFiles ? "Вы уверены, что хотите удалить этот торрент вместе со всеми скачанными файлами?" : "Вы уверены, что хотите удалить этот торрент из списка?";
        new MaterialAlertDialogBuilder(context).setTitle("Подтверждение").setMessage(message).setNegativeButton("Отмена", null).setPositiveButton("Удалить", (dialog, which) -> {
            service.removeTorrent(btih, deleteFiles);
            removeTorrentFromList(btih);
        }).show();
    }

    private void removeTorrentFromList(String btih) {
        new Handler(Looper.getMainLooper()).post(() -> {
            for (int i = 0; i < torrents.size(); i++) {
                if (torrents.get(i).getHashBtih().equals(btih)) {
                    torrents.remove(i);
                    notifyItemRemoved(i);
                    notifyItemRangeChanged(i, torrents.size());
                    break;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return torrents != null ? torrents.size() : 0;
    }

    @Override
    public void onUpdatedTorrent(Torrent updatedTorrent) {
        if (updatedTorrent == null || updatedTorrent.getHashBtih() == null) return;

        int finalIndex = -1;
        for (int i = 0; i < torrents.size(); i++) {
            if (torrents.get(i).getHashBtih().equals(updatedTorrent.getHashBtih())) {
                torrents.set(i, updatedTorrent);
                finalIndex = i;
                break;
            }
        }

        if (finalIndex == -1) {
            torrents.add(updatedTorrent);
            final int newIndex = torrents.size() - 1;
            new Handler(Looper.getMainLooper()).post(() -> notifyItemInserted(newIndex));
            return;
        }

        TorrentItemViewHolder holder = activeHolders.get(updatedTorrent.getHashBtih());
        final int itemIndex = finalIndex;
        new Handler(Looper.getMainLooper()).post(() -> {
            if (holder != null) {
                updateDynamicViews(holder, updatedTorrent);
            } else {
                notifyItemChanged(itemIndex, updatedTorrent);
            }
        });
    }

    @Override
    public void onViewAttachedToWindow(@NonNull TorrentItemViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder.getBtih() != null) {
            activeHolders.put(holder.getBtih(), holder);
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull TorrentItemViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder.getBtih() != null) {
            activeHolders.remove(holder.getBtih());
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateHolderViews(@NonNull TorrentItemViewHolder holder, @NonNull Torrent torrent) {
        holder.getTorrentName().setText(torrent.getName());
        holder.getTorrentSize().setText(formatFileSize(torrent.getSize()));
        updateDynamicViews(holder, torrent);
    }

    @SuppressLint("SetTextI18n")
    private void updateDynamicViews(@NonNull TorrentItemViewHolder holder, @NonNull Torrent torrent) {
        holder.getTorrentProgress().setText(torrent.getProgress() + "%");
        holder.getProgressBarTorrent().setProgress(torrent.getProgress());

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

        holder.getTorrentStatus().setText(statusText);
        holder.getTorrentSpeed().setText(speedText);
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