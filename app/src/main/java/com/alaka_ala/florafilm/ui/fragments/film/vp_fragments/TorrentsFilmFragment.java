
package com.alaka_ala.florafilm.ui.fragments.film.vp_fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentTorrentsFilmBinding;
import com.alaka_ala.florafilm.ui.activities.PlayerExoActivity;
import com.alaka_ala.florafilm.ui.fragments.film.view_model.MainFilmViewModel;
import com.alaka_ala.florafilm.ui.util.api.EPData;
import com.alaka_ala.florafilm.ui.util.api.jacred.JacredTor;
import com.alaka_ala.florafilm.ui.util.coreMatrix.api.SimpleStreamingApi;
import com.alaka_ala.florafilm.ui.util.coreMatrix.api.TorrServeApi;
import com.alaka_ala.florafilm.ui.util.coreMatrix.api.model.TorrentFileStat;
import com.alaka_ala.florafilm.ui.util.coreMatrix.api.model.TorrentStatus;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import io.appmetrica.analytics.impl.S;

public class TorrentsFilmFragment extends Fragment {
    private FragmentTorrentsFilmBinding binding;
    private RecyclerView rvTorrentsFilm;
    private AdapterTorrentsFilm adapter;
    private static int kinopoisk_id;
    private static MainFilmViewModel mainFilmViewModel;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTorrentsFilmBinding.inflate(inflater, container, false);

        mainFilmViewModel = new ViewModelProvider(requireActivity()).get(MainFilmViewModel.class);
        kinopoisk_id = mainFilmViewModel.getKinopoiskId();

        setupRecyclerView();
        loadTorrents();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        rvTorrentsFilm = binding.rvTorrentsFilm;
        adapter = new AdapterTorrentsFilm();
        rvTorrentsFilm.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTorrentsFilm.setAdapter(adapter);
    }

    private void loadTorrents() {
        JacredTor jacredTor = new JacredTor();
        jacredTor.query("kp" + kinopoisk_id, new JacredTor.SearchCallback() {
            @Override
            public void onSuccess(List<JacredTor.JacredData> data) {
                if (getContext() == null) return;
                new Handler(Looper.getMainLooper()).post(() -> {
                    adapter.submitList(data);
                });

            }

            @Override
            public void onLoading(int position, int count, int progres) {}

            @Override
            public void finish() {}

            @Override
            public void onError(String msgError, JacredTor.SearchCallback sc) {

            }
        });

    }

    private static class AdapterTorrentsFilm extends ListAdapter<JacredTor.JacredData, AdapterTorrentsFilm.ViewHolderTorrentsFilm> {



        public AdapterTorrentsFilm() {
            super(new JacredDataDiffCallback());
        }

        @NonNull
        @Override
        public ViewHolderTorrentsFilm onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_torrent, parent, false);
            return new ViewHolderTorrentsFilm(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolderTorrentsFilm holder, int position) {
            JacredTor.JacredData jacredData = getItem(position);
            holder.title.setText(jacredData.getTitle());
            jacredData.getVoices();
            holder.seeders.setText(String.valueOf(jacredData.getSid()));
            holder.peers.setText(String.valueOf(jacredData.getPir()));
            holder.tvSize.setText(formatFileSize(jacredData.getSize()));
            holder.tvTracker.setText(jacredData.getTracker());
            holder.voices = jacredData.getVoices();
            holder.magnet = jacredData.getMagnet();
            holder.btnVisibleVoicer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.linVoices.getVisibility() == View.GONE) {
                        newThreadAddedView(view);
                        holder.linVoices.setVisibility(View.VISIBLE);
                        holder.btnVisibleVoicer.setText("Скрыть");
                    }
                    else {
                        holder.linVoices.setVisibility(View.GONE);
                        holder.btnVisibleVoicer.setText("Озвучки");
                    }
                }

                private void newThreadAddedView(View view) {
                    if (holder.linVoices.getChildCount() > 1) return;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (String voice : holder.voices) {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    TextView tv = new TextView(view.getContext());
                                    tv.setText(voice);
                                    holder.linVoices.addView(tv);
                                });

                            }
                        }
                    }).start();

                }
            });
            holder.chipPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String magnetLink = holder.magnet;
                    String torrentTitle = jacredData.getName(); // Get title from data

                    // All network operations must be on a background thread
                    new Thread(() -> {
                        // 1. Initialize the API
                        SimpleStreamingApi streamingApi = new SimpleStreamingApi("http://127.0.0.1:8090");

                        try {
                            // 2. Add the torrent to the server
                            Log.d("Streaming", "Starting stream for: " + torrentTitle);
                            TorrentStatus status = streamingApi.startStreaming(magnetLink, torrentTitle, null);
                            String torrentHash = status.getHash();
                            Log.d("Streaming", "Torrent added with hash: " + torrentHash);

                            // 3. Asynchronously wait for the torrent to be ready
                            streamingApi.waitForReady(torrentHash, 60).thenAccept(readyStatus -> {
                                // 4. Get the list of all files and filter for video files
                                List<TorrentFileStat> allFiles = readyStatus.getFileStats();
                                List<TorrentFileStat> videoFiles = allFiles.stream()
                                        .filter(this::isVideoFile)
                                        .collect(Collectors.toList());

                                if (videoFiles.isEmpty()) {
                                    Log.e("Streaming", "No video files found in the torrent.");
                                    new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(view.getContext(), "Видеофайлы не найдены", Toast.LENGTH_SHORT).show());
                                    return;
                                }

                                // 5. Decide what to do based on the number of video files
                                if (videoFiles.size() == 1) {
                                    // Only one video file, play it directly
                                    playFile(view, streamingApi, readyStatus.getHash(), videoFiles.get(0));
                                } else {
                                    // Multiple video files, show a selection dialog
                                    showFileDialog(view, streamingApi, readyStatus.getHash(), videoFiles);
                                }

                            }).exceptionally(ex -> {
                                // Handle timeout or other errors during waiting
                                Log.e("Streaming", "Failed to get torrent ready: " + ex.getMessage());
                                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(view.getContext(), "Ошибка подготовки торрента", Toast.LENGTH_SHORT).show());
                                return null;
                            });

                        } catch (SimpleStreamingApi.StreamingException e) {
                            // Handle errors during torrent addition
                            Log.e("Streaming", "Error starting stream: " + e.getMessage());
                            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(view.getContext(), "Ошибка добавления торрента", Toast.LENGTH_SHORT).show());
                        }
                    }).start();
                }

                private void showFileDialog(View view, SimpleStreamingApi streamingApi, String torrentHash, List<TorrentFileStat> videoFiles) {
                    // Create a list of file paths to display in the dialog
                    CharSequence[] filePaths = videoFiles.stream()
                            .map(TorrentFileStat::getPath)
                            .toArray(CharSequence[]::new);

                    // UI operations must be on the main thread
                    new Handler(Looper.getMainLooper()).post(() -> {
                        new MaterialAlertDialogBuilder(view.getContext())
                                .setTitle("Выберите файл для воспроизведения")
                                .setItems(filePaths, (dialog, which) -> {
                                    // User selected a file, play it
                                    TorrentFileStat selectedFile = videoFiles.get(which);
                                    playFile(view, streamingApi, torrentHash, selectedFile);
                                })
                                .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                                .show();
                    });
                }

                private void playFile(View view, SimpleStreamingApi streamingApi, String torrentHash, TorrentFileStat file) {
                    String playbackUrl = streamingApi.getFileStreamUrl(holder.magnet, file.getId());
                    Log.d("Streaming", "Streaming URL for " + file.getPath() + ": " + playbackUrl);
                    EPData.Builder epDataBuilder = new EPData.Builder();
                    epDataBuilder.setFilmInfo(mainFilmViewModel.getCurrentFilmInfo());
                    epDataBuilder.setBalancer("magnet");
                    EPData.Film.Builder filmBuilder = new EPData.Film.Builder();
                    filmBuilder.setNameFilm(jacredData.getName());
                    filmBuilder.addBlock(new EPData.Block("UA"));
                    filmBuilder.setPoster("https://kinopoiskapiunofficial.tech/images/posters/kp" + kinopoisk_id + ".jpg");
                    ArrayList<EPData.Film.Translations> translations = new ArrayList<>();
                    EPData.Film.Translations.Builder builderTrans = new EPData.Film.Translations.Builder();
                    builderTrans.setTitle(jacredData.getVoices().get(0));
                    List<Map.Entry<String, String>> videoData = new ArrayList<>();
                    videoData.add(Map.entry("url", playbackUrl));
                    builderTrans.setVideoData(videoData);
                    translations.add(builderTrans.build());
                    filmBuilder.setTranslations(translations);
                    epDataBuilder.setFilm(filmBuilder.build());

                    // TODO: Implement playback
                    // Pass the 'playbackUrl' to your video player (e.g., ExoPlayer) on the main thread.
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(view.getContext(), "Начинаем воспроизведение: " + file.getPath(), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(view.getContext(), PlayerExoActivity.class);
                        intent.putExtra("epData", epDataBuilder.build());
                        view.getContext().startActivity(intent);
                    });
                }

                private boolean isVideoFile(TorrentFileStat file) {
                    if (file == null || file.getPath() == null) return false;
                    String path = file.getPath().toLowerCase();
                    return path.endsWith(".mkv") || path.endsWith(".mp4") || path.endsWith(".avi") || path.endsWith(".mov") || path.endsWith(".wmv") || path.endsWith(".flv");
                }
            });


        }

        private static class ViewHolderTorrentsFilm extends RecyclerView.ViewHolder {
            private String magnet;
            private ArrayList<String> voices;
            private LinearLayout linVoices;
            private Chip btnVisibleVoicer, chipPlay;
            private TextView seeders, peers, title, tvSize, tvTracker;
            public ViewHolderTorrentsFilm(@NonNull View itemView) {
                super(itemView);
                seeders = itemView.findViewById(R.id.tvSeedersUp);
                peers = itemView.findViewById(R.id.tvPeersDown);
                title = itemView.findViewById(R.id.tv_torrent_name);
                tvSize = itemView.findViewById(R.id.tvSize);
                tvTracker = itemView.findViewById(R.id.tvTracker);
                linVoices = itemView.findViewById(R.id.linVoices);
                btnVisibleVoicer = itemView.findViewById(R.id.btnVisibleVoicer);
                chipPlay = itemView.findViewById(R.id.chipPlay);
            }
        }
    }

    private static class JacredDataDiffCallback extends DiffUtil.ItemCallback<JacredTor.JacredData> {
        @Override
        public boolean areItemsTheSame(@NonNull JacredTor.JacredData oldItem, @NonNull JacredTor.JacredData newItem) {
            return oldItem.getMagnet().equals(newItem.getMagnet());
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull JacredTor.JacredData oldItem, @NonNull JacredTor.JacredData newItem) {
            return Objects.equals(oldItem, newItem);
        }
    }



    /**
     * Форматирует размер файла из байтов (long) в удобочитаемую строку.
     * <p>
     * Метод преобразует числовое значение размера файла в строку с указанием
     * соответствующей единицы измерения (B, KB, MB, GB, TB). Результат
     * округляется до двух знаков после запятой.
     *
     * <h3>Условия использования:</h3>
     * <ul>
     *     <li>Если переданный размер меньше 0, метод вернет "0 B".</li>
     *     <li>Метод использует множитель 1024 для перехода между единицами измерения.</li>
     * </ul>
     *
     * @param size Размер файла в байтах.
     * @return Отформатированная строка, представляющая размер файла (например, "1.23 MB").
     */
    public static String formatFileSize(long size) {
        if (size <= 0) {
            return "0 B";
        }

        // Массив единиц измерения
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};

        // Вычисляем индекс единицы измерения в массиве units
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        // Форматируем число, чтобы оно имело не более двух знаков после запятой
        return new DecimalFormat("#,##0.##")
                .format(size / Math.pow(1024, digitGroups))
                + " " + units[digitGroups];
    }


}
