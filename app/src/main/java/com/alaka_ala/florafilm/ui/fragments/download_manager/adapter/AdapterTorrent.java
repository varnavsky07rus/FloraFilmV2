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
    // Хранит активные (видимые на экране) ViewHolder'ы для прямого обновления
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

        // Устанавливаем слушатель на весь элемент списка
        holder.itemView.setOnClickListener(v -> {
            showManagementDialog(v.getContext(), torrent);
        });
    }

    private void showManagementDialog(Context context, Torrent torrent) {
        TorrentSessionService service = TorrentSessionService.getInstance();
        if (service == null) return;

        ArrayList<String> options = new ArrayList<>();
        String state = torrent.getState();

        // Динамически формируем список действий
        if ("DOWNLOADING".equals(state) || "SEEDING".equals(state)) {
            options.add("Поставить на паузу");
        } else {
            options.add("Возобновить");
        }
        options.add("Удалить торрент");
        options.add("Удалить торрент и файлы");

        new MaterialAlertDialogBuilder(context).setTitle(torrent.getName()).setItems(options.toArray(new String[0]), (dialog, which) -> {
            String selectedOption = options.get(which);
            String btih = torrent.getHashBtih();

            switch (selectedOption) {
                case "Поставить на паузу":
                    service.pauseTorrent(btih);
                    break;
                case "Возобновить":
                    service.resumeTorrent(btih);
                    break;
                case "Удалить торрент":
                    // Показываем диалог подтверждения
                    confirmAndRemove(context, service, btih, false);
                    break;
                case "Удалить торрент и файлы":
                    // Показываем диалог подтверждения
                    confirmAndRemove(context, service, btih, true);
                    break;
            }
        }).show();
    }

    private void confirmAndRemove(Context context, TorrentSessionService service, String btih, boolean deleteFiles) {
        String message = deleteFiles ? "Вы уверены, что хотите удалить этот торрент вместе со всеми скачанными файлами?" : "Вы уверены, что хотите удалить этот торрент из списка?";
        new MaterialAlertDialogBuilder(context).setTitle("Подтверждение").setMessage(message).setNegativeButton("Отмена", null).setPositiveButton("Удалить", (dialog, which) -> {
            service.removeTorrent(btih, deleteFiles);
            // Удаляем элемент из списка адаптера и уведомляем об этом
            removeTorrentFromList(btih);
        }).show();
    }

    // Метод для удаления торрента из внутреннего списка адаптера
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

        // 1. Обновляем или добавляем торрент в наш локальный список
        boolean found = false;
        for (int i = 0; i < torrents.size(); i++) {
            if (torrents.get(i).getHashBtih().equals(updatedTorrent.getHashBtih())) {
                torrents.set(i, updatedTorrent); // Обновляем существующий
                found = true;
                break;
            }
        }
        if (!found) {
            torrents.add(updatedTorrent); // Добавляем новый
        }

        // 2. Ищем активный (видимый) холдер для этого торрента
        TorrentItemViewHolder holder = activeHolders.get(updatedTorrent.getHashBtih());

        // 3. Если холдер найден (т.е. он на экране), обновляем его напрямую
        if (holder != null) {
            new Handler(Looper.getMainLooper()).post(() -> updateHolderViews(holder, updatedTorrent));
        } else {
            // Если холдер не на экране, просто уведомляем адаптер, что данные изменились.
            // Это нужно, чтобы при скролле отобразились актуальные данные.
            new Handler(Looper.getMainLooper()).post(this::notifyDataSetChanged);
        }
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
    private void updateHolderViews(@NonNull TorrentItemViewHolder holder, @Nullable Torrent torrent) {
        if (torrent == null) return;

        holder.getTorrentName().setText(torrent.getName());
        holder.getTorrentSize().setText(formatFileSize(torrent.getSize()));
        holder.getTorrentProgress().setText(torrent.getProgress() + "%");
        holder.getProgressBarTorrent().setProgress(torrent.getProgress());

        String state = torrent.getState();
        String speedText = "";
        if ("DOWNLOADING".equals(state)) {
            state = "Загрузка";
            speedText = formatFileSize(torrent.getDownloadRate()) + "/с";
        } else if ("SEEDING".equals(state)) {
            state = "Раздача";
            speedText = formatFileSize(torrent.getUploadRate()) + "/с";
        } else if ("CHECKING_FILES".equals(state)) {
            state = "Проверка";
        } else if ("PAUSED".equals(state)) {
            state = "На паузе";
        } else if ("FINISHED".equals(state)) {
            state = "Завершен";
        }

        holder.getTorrentStatus().setText(state);
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