package com.alaka_ala.florafilm.ui.fragments.download_manager.adapter;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;

public class TorrentItemViewHolder extends RecyclerView.ViewHolder {
    private final TextView torrentName;
    private final TextView torrentStatus;
    private final ProgressBar progressBarTorrent;
    private final TextView torrentProgress;
    private final TextView torrentSpeed;
    private final TextView torrentSize;
    private final TextView seeders;
    private final TextView peers;
    private final LinearLayout llProgress;
    private String btih; // Для идентификации холдера

    public TorrentItemViewHolder(@NonNull View itemView) {
        super(itemView);
        torrentName = itemView.findViewById(R.id.tv_torrent_name);
        torrentStatus = itemView.findViewById(R.id.tv_torrent_status);
        progressBarTorrent = itemView.findViewById(R.id.pb_torrent);
        torrentProgress = itemView.findViewById(R.id.tv_torrent_progress);
        torrentSpeed = itemView.findViewById(R.id.tv_torrent_speed);
        torrentSize = itemView.findViewById(R.id.tv_torrent_size);
        seeders = itemView.findViewById(R.id.tvSeedersUp);
        peers = itemView.findViewById(R.id.tvPeersDown);
        llProgress = itemView.findViewById(R.id.llProgress);
    }

    public LinearLayout getLlProgress() {
        return llProgress;
    }
    public TextView getSeeders() {
        return seeders;
    }
    public TextView getPeers() {
        return peers;
    }
    public TextView getTorrentName() { return torrentName; }
    public TextView getTorrentStatus() { return torrentStatus; }
    public ProgressBar getProgressBarTorrent() { return progressBarTorrent; }
    public TextView getTorrentProgress() { return torrentProgress; }
    public TextView getTorrentSpeed() { return torrentSpeed; }
    public TextView getTorrentSize() { return torrentSize; }
    public String getBtih() { return btih; }
    public void setBtih(String btih) { this.btih = btih; }
}