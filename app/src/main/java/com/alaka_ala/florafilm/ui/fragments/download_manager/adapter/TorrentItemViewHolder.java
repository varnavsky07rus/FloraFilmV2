package com.alaka_ala.florafilm.ui.fragments.download_manager.adapter;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.ui.util.coreTorrent.models.Torrent;
import com.google.android.material.chip.Chip;

public class TorrentItemViewHolder extends RecyclerView.ViewHolder {

    public Torrent torrent;

    public String getBtih() {
        return btih;
    }

    public void setBtih(String btih) {
        this.btih = btih;
    }
    private String btih;

    private final TextView tv_torrent_name;
    private final TextView tv_torrent_status;
    private final TextView tv_torrent_progress;
    private final TextView tv_torrent_speed;
    private final TextView tv_torrent_size;
    private final ProgressBar pb_torrent;
    private final Button buttonStart;
    private final Button buttonRemove;


    public Button getButtonRemove() {
        return buttonRemove;
    }

    public Button getButtonStart() {
        return buttonStart;
    }

    public ProgressBar getProgressBarTorrent() {
        return pb_torrent;
    }

    public TextView getTorrentSize() {
        return tv_torrent_size;
    }

    public TextView getTorrentSpeed() {
        return tv_torrent_speed;
    }

    public TextView getTorrentProgress() {
        return tv_torrent_progress;
    }

    public TextView getTorrentStatus() {
        return tv_torrent_status;
    }

    public TextView getTorrentName() {
        return tv_torrent_name;
    }



    public TorrentItemViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_torrent_name = itemView.findViewById(R.id.tv_torrent_name);
        tv_torrent_status = itemView.findViewById(R.id.tv_torrent_status);
        tv_torrent_progress = itemView.findViewById(R.id.tv_torrent_progress);
        tv_torrent_speed = itemView.findViewById(R.id.tv_torrent_speed);
        tv_torrent_size = itemView.findViewById(R.id.tv_torrent_size);
        pb_torrent = itemView.findViewById(R.id.pb_torrent);
        buttonStart = itemView.findViewById(R.id.buttonStart);
        buttonRemove = itemView.findViewById(R.id.buttonRemove);
    }
}
