package com.alaka_ala.florafilm.ui.fragments.film.vp_fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentTorrentFilmBinding;
import com.alaka_ala.florafilm.ui.activities.PlayerExoActivity;
import com.alaka_ala.florafilm.ui.fragments.film.MainFilmFragment;
import com.alaka_ala.florafilm.ui.fragments.film.view_model.MainFilmViewModel;
import com.alaka_ala.florafilm.ui.util.api.jacred.JacredTor;
import com.alaka_ala.torstream.torrent.LocalHttpServer;
import com.alaka_ala.torstream.torrent.TorrentStreamer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TorrentFilmFragment extends Fragment {
    private FragmentTorrentFilmBinding binding;
    private JacredTor jacredTor;
    private MainFilmViewModel mainFilmViewModel;
    private int kinopoisk_id;
    private RecyclerView rvTorrentFilm;
    private List<JacredTor.JacredData> data;
    private List<JacredTor.JacredData> dataFinal;
    private SearchView searchView;
    private boolean isCreateMenu = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTorrentFilmBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        MainFilmFragment.setViewPagerListener(new MainFilmFragment.ViewPagerListener() {
            @Override
            public void onTransition(int currentPage) {
                isCreateMenu = currentPage == 2;
            }
        });

        searchView = binding.svTorrents;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                sortByQuery(s);
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                resetFilters();
                return false;
            }
        });

        mainFilmViewModel = new ViewModelProvider(getActivity()).get(MainFilmViewModel.class);
        kinopoisk_id = mainFilmViewModel.getKinopoiskId();
        jacredTor = new JacredTor();

        rvTorrentFilm = binding.rvTorrentFilm;
        rvTorrentFilm.setLayoutManager(new LinearLayoutManager(getContext()));



        jacredTor.query("kp" + kinopoisk_id, new JacredTor.SearchCallback() {
            @Override
            public void onSuccess(List<JacredTor.JacredData> datas) {
                data = datas;
                dataFinal = data;
                rvTorrentFilm.setAdapter(new AdapterTorrentFilm(data));
            }

            @Override
            public void onLoading(int position, int count, int progres) {

            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void finish() {
                if (rvTorrentFilm.getAdapter() != null) {
                    rvTorrentFilm.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String msgError, JacredTor.SearchCallback sc) {

            }
        });


        return binding.getRoot();
    }

    private static class AdapterTorrentFilm extends RecyclerView.Adapter<AdapterTorrentFilm.MyViewHolder> {
        public AdapterTorrentFilm(List<JacredTor.JacredData> data) {
            this.data = data;
        }

        public void setData(List<JacredTor.JacredData> data) {
            this.data = data;
        }

        private List<JacredTor.JacredData> data;

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_torrent_film, parent, false);
            return new MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            JacredTor.JacredData jacredData = data.get(position);
            holder.textViewTitleTorrent.setText(jacredData.getVoices().isEmpty() ?
                    jacredData.getName() + " (" + jacredData.getQuality() + "p)" :
                    jacredData.getName() + " (" + jacredData.getQuality() + "p)" + jacredData.getVoices());
            holder.textViewInformationTorrent.setText(
                    !jacredData.getSeasons().isEmpty() ?
                            jacredData.getCreateTime() + " • " + formatSize(jacredData.getSize()) + " • " + jacredData.getTracker() + " • сезон " + jacredData.getSeasons() :
                            jacredData.getCreateTime() + " • " + formatSize(jacredData.getSize()) + " • " + jacredData.getTracker());
            holder.textViewPeers.setText(String.valueOf(jacredData.getPir()));
            holder.textViewSiders.setText(String.valueOf(jacredData.getSid()));
            holder.itemView.setId(position);
            holder.itemView.findViewById(R.id.materialCardViewItem).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TorrentHelper.openMagnetLink(view.getContext(), jacredData.getMagnet());
                    TorrentStreamer torrentStreamer = new TorrentStreamer();
                    //String magnet = "magnet:?xt=urn:btih:9C38D68035F190F1953F758E5527DDE1C7563B72&tr=http%3A%2F%2Fbt2.t-ru.org%2Fann%3Fmagnet";
                    torrentStreamer.download(holder.itemView.getContext(), jacredData.getMagnet(), true, new TorrentStreamer.DownloadListener() {
                        @Override
                        public void onStartDownload(File filePath) {

                        }

                        @Override
                        public void onDProgress(int indexPiece, int progress, File filePath) {
                            if (filePath.exists() && indexPiece > 5) {
                                LocalHttpServer httpServer = new LocalHttpServer(8080, filePath.getAbsolutePath());
                                try {
                                    httpServer.startServer();
                                    Intent intent = new Intent(holder.itemView.getContext(), PlayerExoActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                } catch (IOException e) {
                                    e.printStackTrace();

                                }
                            }
                        }

                        @Override
                        public void onDownloadFinished(File filePath) {

                        }

                        @Override
                        public void onError(String e) {
                            Context context = holder.itemView.getContext();
                            Toast.makeText(context, e, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        private static class MyViewHolder extends RecyclerView.ViewHolder {
            private final TextView textViewTitleTorrent;
            private final TextView textViewInformationTorrent;
            private final TextView textViewSiders;
            private final TextView textViewPeers;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewTitleTorrent = itemView.findViewById(R.id.textViewTitleTorrent);
                textViewInformationTorrent = itemView.findViewById(R.id.textViewInformationTorrent);
                textViewSiders = itemView.findViewById(R.id.textViewSiders);
                textViewPeers = itemView.findViewById(R.id.textViewPeers);

            }
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (!isCreateMenu) return;
        menu.add("Сортировать по пирам").setIcon(R.drawable.rounded_sort_24);
        menu.add("Сортировать по сидерам").setIcon(R.drawable.rounded_sort_24);
        menu.add("Сначала новые").setTitle("Сначала новые");
        menu.add("Сначала старые").setTitle("Сначала старые");
        menu.add("Сбросить фильтры").setIcon(R.drawable.rounded_filter_alt_off_24);
        menu.add("Поиск").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setActionView(searchView);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getTitle() == null) return super.onOptionsItemSelected(item);
        if (item.getTitle().equals("Сортировать по пирам")) {
            sortByPeer();
        } else if (item.getTitle().equals("Сортировать по сидерам")) {
            sortBySid();
        } else if (item.getTitle().equals("Сбросить фильтры")) {
            resetFilters();
        } else if (item.getTitle().equals("Поиск")) {

        } else if (item.getTitle().equals("Сначала новые")) {
            sortByDate(SortOrder.NEWEST);
        } else if (item.getTitle().equals("Сначала старые")) {
            sortByDate(SortOrder.OLDEST);
        }

        return super.onOptionsItemSelected(item);
    }

    private void sortByPeer() {
        if (data == null) return;
        Thread threadPeer = new Thread(new Runnable() {
            @Override
            public void run() {
                data.sort((o1, o2) -> o2.getPir() - o1.getPir());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rvTorrentFilm.getAdapter().notifyDataSetChanged();
                    }
                });
            }
        });
        threadPeer.start();
    }

    private void sortBySid() {
        if (data == null) return;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                data.sort((o1, o2) -> o2.getSid() - o1.getSid());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rvTorrentFilm.getAdapter().notifyDataSetChanged();
                    }
                });
            }
        });
        thread.start();
    }

    private void sortByQuery(String query) {
        if (query.isEmpty() || data == null) return;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Создаем Pattern вне цикла, чтобы не пересоздавать его каждый раз
                Pattern pattern = Pattern.compile(query.toLowerCase(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);

                // Создаем новый список для хранения элементов, которые нужно удалить
                List<JacredTor.JacredData> itemsToRemove = new ArrayList<>();

                for (JacredTor.JacredData item : data) {
                    // Получаем имя элемента
                    String itemQuality = String.valueOf(item.getQuality()).toLowerCase();
                    String itemVoices = String.valueOf(item.getVoices()).toLowerCase();
                    String itemName = item.getName().toLowerCase();
                    String itemTracker = item.getTracker().toLowerCase();
                    String title = itemName + " (" + itemQuality + "p)" + itemVoices + " " + itemTracker;

                    // Создаем Matcher для текущего имени
                    Matcher matcher = pattern.matcher(title);

                    // Проверяем, есть ли совпадение
                    if (matcher.find()) {
                        // Если совпадения нет, добавляем элемент в список на удаление
                        itemsToRemove.add(item);
                    }
                    AdapterTorrentFilm adapter = (AdapterTorrentFilm) rvTorrentFilm.getAdapter();
                    adapter.setData(itemsToRemove);
                }


                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rvTorrentFilm.getAdapter().notifyDataSetChanged();
                    }
                });
            }
        });
        thread.start();
    }

    public enum SortOrder {
        NEWEST,
        OLDEST
    }

    private void sortByDate(SortOrder sortOrder) {
        if (data == null) return;

        Thread thread = new Thread(() -> {
            Comparator<JacredTor.JacredData> comparator;
            if (sortOrder == SortOrder.NEWEST) {
                // Сортировка по новинкам (от большего года к меньшему)
                comparator = (o1, o2) -> o2.getRelased() - o1.getRelased();
            } else {
                // Сортировка по старым (от меньшего года к большему)
                comparator = (o1, o2) -> o1.getRelased() - o2.getRelased();
            }

            data.sort(comparator);

            getActivity().runOnUiThread(() -> rvTorrentFilm.getAdapter().notifyDataSetChanged());
        });
        thread.start();
    }


    @SuppressLint("NotifyDataSetChanged")
    private void resetFilters() {
        if (dataFinal == null) return;
        data = dataFinal;
        AdapterTorrentFilm adapter = (AdapterTorrentFilm) rvTorrentFilm.getAdapter();
        if (adapter == null) return;
        adapter.setData(data);
        adapter.notifyDataSetChanged();
    }


    public static String formatSize(long size) {
        if (size <= 0) {
            return "0 B";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

}