package com.alaka_ala.florafilm.ui.fragments.film.vp_fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class TorrentsFilmFragment extends Fragment {
    private FragmentTorrentsFilmBinding binding;
    private RecyclerView rvTorrentsFilm;
    private int kinopoisk_id;
    private TorrentSessionService torrentService;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTorrentsFilmBinding.inflate(inflater, container, false);
        MainFilmViewModel film = new ViewModelProvider(getActivity()).get(MainFilmViewModel.class);
        torrentService = TorrentSessionService.getInstance();
        kinopoisk_id = film.getKinopoiskId();
        rvTorrentsFilm = binding.rvTorrentsFilm;
        rvTorrentsFilm.setLayoutManager(new LinearLayoutManager(getContext()));

        JacredTor jacredTor = new JacredTor();
        jacredTor.query("kp" + kinopoisk_id, new JacredTor.SearchCallback() {
            @Override
            public void onSuccess(List<JacredTor.JacredData> data) {
                rvTorrentsFilm.setAdapter(new AdapterTorrentsFilm(data));
            }

            @Override
            public void onLoading(int position, int count, int progres) {

            }

            @Override
            public void finish() {

            }

            @Override
            public void onError(String msgError, JacredTor.SearchCallback sc) {

            }
        });




        return binding.getRoot();
    }




    private class AdapterTorrentsFilm extends RecyclerView.Adapter<TorrentItemViewHolder> implements UpdateDataListener {
        public AdapterTorrentsFilm(List<JacredTor.JacredData> data) {
            this.data = data;
            if (torrentSessionService == null) {
                this.torrentSessionService = TorrentSessionService.getInstance();
                torrentSessionService.addListener("AdapterTorrent", this);
            }
        }
        private TorrentSessionService torrentSessionService;
        private final List<JacredTor.JacredData> data;
        public final Map<String, Torrent> torrents = new HashMap<>();
        public final Map<String, Integer> positionHolders = new HashMap<>();

        @NonNull
        @Override
        public TorrentItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_torrent, parent, false);
            return new TorrentItemViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull TorrentItemViewHolder holder, int position) {
            holder.setBtih(MagnetLinkParser.extractBtih(data.get(position).getMagnet()));
            holder.getTorrentName().setText(data.get(position).getName());
            holder.getTorrentSize().setText(formatFileSize(data.get(position).getSize()));
            holder.getButtonStart().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new Thread(() -> {
                        try {
                            if (torrentService == null) return;
                            torrentService.download(kinopoisk_id, data.get(holder.getAbsoluteAdapterPosition()).getMagnet());
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }).start();
                }
            });


            holder.getTorrentProgress().setText(torrents.get(holder.getBtih()) != null ? "" + torrents.get(holder.getBtih()).getProgress() : "0");
            holder.getProgressBarTorrent().setProgress(torrents.get(holder.getBtih()) != null ? torrents.get(holder.getBtih()).getProgress() : 0);
            holder.getTorrentStatus().setText(torrents.get(holder.getBtih()) != null ? torrents.get(holder.getBtih()).getState() : "Не скачан");
            positionHolders.put(holder.getBtih(), holder.getAbsoluteAdapterPosition());
        }

        @Override
        public int getItemCount() {
            if (data == null) return 0;
            return data.size();
        }

        @Override
        public void onUpdatedTorrent(Torrent torrent) {
            torrents.put(torrent.getHashBtih(), torrent);
            if (!positionHolders.containsKey(torrent.getHashBtih())) return;
            notifyDataSetChanged();
        }

        public String formatFileSize(long bytes) {
            if (bytes < 1024) {
                return bytes + " B";
            }

            int unit = 1024;
            String[] units = {"KB", "MB", "GB", "TB"};
            int exp = (int) (Math.log(bytes) / Math.log(unit));
            double size = bytes / Math.pow(unit, exp);

            return String.format("%.1f %s", size, units[exp - 1]);
        }

    }



}