package com.alaka_ala.florafilm.ui.fragments.download_manager.adapter;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.ui.util.coreTorrent.TorrentSessionService;
import com.alaka_ala.florafilm.ui.util.coreTorrent.interfaces.UpdateDataListener;
import com.alaka_ala.florafilm.ui.util.coreTorrent.models.Torrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdapterTorrent extends RecyclerView.Adapter<TorrentItemViewHolder> implements UpdateDataListener {
    private final Map<String, Integer> positionItemFromHash = new HashMap<>();
    private TorrentSessionService torrentSessionService;
    public AdapterTorrent() {
        super();
        if (torrentSessionService == null) {
            this.torrentSessionService = TorrentSessionService.getInstance();
            torrentSessionService.addListener("AdapterTorrent", this);
        }
    }


    @NonNull
    @Override
    public TorrentItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_torrent, parent, false);
        return new TorrentItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TorrentItemViewHolder holder, int position) {
        holder.setBtih("6a9cf7aad29eaffbba4135509525145da673c4ae");
        positionItemFromHash.put("6a9cf7aad29eaffbba4135509525145da673c4ae", position);
    }

    @Override
    public int getItemCount() {
        return 5;
    }

    @Override
    public void onUpdatedTorrent(Torrent torrent) {
        if (torrent == null) return;
        new Handler(Looper.getMainLooper()).post(() -> {
            this.notifyItemChanged(positionItemFromHash.get(torrent.getHashBtih()).intValue());
        });
    }
}
