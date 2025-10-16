package com.alaka_ala.florafilm.ui.fragments.download_manager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.databinding.FragmentDownloadManagerBinding;
import com.alaka_ala.florafilm.ui.fragments.download_manager.adapter.AdapterTorrent;
import com.alaka_ala.florafilm.ui.util.coreTorrent.db.TorrentDatabase;
import com.alaka_ala.florafilm.ui.util.coreTorrent.models.Torrent;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadManagerFragment extends Fragment {
    private FragmentDownloadManagerBinding binding;
    private RecyclerView rvDManger;
    private AdapterTorrent adapterTorrent;
    private TorrentDatabase db;
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDownloadManagerBinding.inflate(inflater, container, false);
        db = TorrentDatabase.getDatabase(getContext());
        setupRecyclerView();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadTorrentsFromDb();
    }

    private void setupRecyclerView() {
        rvDManger = binding.rvDManger;
        rvDManger.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void loadTorrentsFromDb() {
        dbExecutor.execute(() -> {
            // Получаем все торренты из базы данных в фоновом потоке
            List<Torrent> torrents = db.torrentDao().getAll();

            // Возвращаемся в главный поток для обновления UI
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Создаем и устанавливаем адаптер с полученными данными
                    adapterTorrent = new AdapterTorrent(torrents);
                    rvDManger.setAdapter(adapterTorrent);
                    // Регистрируем слушатель, чтобы получать живые обновления
                    adapterTorrent.registerListener();
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // ОБЯЗАТЕЛЬНО отписываемся от обновлений, чтобы избежать утечек памяти
        if (adapterTorrent != null) {
            adapterTorrent.unregisterListener();
        }
        binding = null;
    }
}