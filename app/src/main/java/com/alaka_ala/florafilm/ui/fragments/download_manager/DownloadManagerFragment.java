package com.alaka_ala.florafilm.ui.fragments.download_manager;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alaka_ala.florafilm.databinding.FragmentDownloadManagerBinding;
import com.alaka_ala.florafilm.ui.fragments.download_manager.adapter.AdapterTorrent;
import com.alaka_ala.florafilm.ui.util.coreTorrent.TorrentSessionService;

public class DownloadManagerFragment extends Fragment {
    private FragmentDownloadManagerBinding binding;
    private RecyclerView rvDManger;
    private TorrentSessionService torrentService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDownloadManagerBinding.inflate(inflater, container, false);
        rvDManger = binding.rvDManger;
        torrentService = TorrentSessionService.getInstance();

        AdapterTorrent adapterTorrent = new AdapterTorrent();
        rvDManger.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDManger.setAdapter(adapterTorrent);




        String magnetLink = "magnet:?xt=urn:btih:6A9CF7AAD29EAFFBBA4135509525145DA673C4AE&xl=9419574865&dn=Fast.X.2023.MA.WEB-DL.1080p.seleZen.mkv.torrent&tr=http%3A%2F%2Fbt.desol.one%3A2710%2Fannounce&tr=udp%3A%2F%2Fopentor.net%3A6969&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337%2Fannounce&tr=udp%3A%2F%2F9.rarbg.me%3A2730%2Fannounce&tr=udp%3A%2F%2F9.rarbg.to%3A2770%2Fannounce&tr=udp%3A%2F%2Fexodus.desync.com%3A6969%2Fannounce&tr=udp%3A%2F%2Fopen.stealth.si%3A80%2Fannounce&tr=udp%3A%2F%2Fretracker.lanta-net.ru%3A2710%2Fannounce&tr=udp%3A%2F%2Ftracker.moeking.me%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.torrent.eu.org%3A451%2Fannounce&tr=udp%3A%2F%2F9.rarbg.me%3A2770%2Fannounce&tr=udp%3A%2F%2F9.rarbg.to%3A2720%2Fannounce&tr=udp%3A%2F%2F9.rarbg.to%3A2730%2Fannounce&tr=http%3A%2F%2Ftracker.grepler.com%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.dler.com%3A6969%2Fannounce&tr=http%3A%2F%2Fh4.trakx.nibba.trade%3A80%2Fannounce&tr=udp%3A%2F%2Ftracker.bitsearch.to%3A1337%2Fannounce";






        return binding.getRoot();
    }

}