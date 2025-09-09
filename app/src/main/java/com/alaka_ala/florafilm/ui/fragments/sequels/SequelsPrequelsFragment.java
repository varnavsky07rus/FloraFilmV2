package com.alaka_ala.florafilm.ui.fragments.sequels;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentSequelsPrequelsBinding;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.KinopoiskAPI;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.FilmRelation;
import com.alaka_ala.florafilm.ui.util.listeners.MyRecyclerViewItemTouchListener;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class SequelsPrequelsFragment extends Fragment {
    private FragmentSequelsPrequelsBinding binding;
    private int KINOPOISK_ID = 0;

    private AdapterRecyclerViewSequels adapterRecyclerViewSequels;
    private RecyclerView recyclerViewSequels;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSequelsPrequelsBinding.inflate(inflater, container, false);
        Bundle bundle = getArguments();
        if (bundle != null) {
            KINOPOISK_ID = bundle.getInt("kinopoisk_id");
        }

        recyclerViewSequels = binding.recyclerViewSequels;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3, LinearLayoutManager.VERTICAL, false);
        recyclerViewSequels.setLayoutManager(gridLayoutManager);
        recyclerViewSequels.setAdapter(adapterRecyclerViewSequels = new AdapterRecyclerViewSequels());

        recyclerViewSequels.addOnItemTouchListener(new MyRecyclerViewItemTouchListener(getContext(), recyclerViewSequels, new MyRecyclerViewItemTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView.ViewHolder holder, View view, int position) {
                Bundle bundle = new Bundle();
                if (position <= -1) return;
                bundle.putInt("kinopoisk_id", adapterRecyclerViewSequels.getFilmRelation().get(position).getFilmId());
                Navigation.findNavController(view).navigate(R.id.action_sequelsPrequelsFragment_to_mainFilmFragment, bundle);
            }

            @Override
            public void onLongItemClick(RecyclerView.ViewHolder holder, View view, int position) {

            }
        }));

        KinopoiskAPI kinopoiskAPI = new KinopoiskAPI(getResources().getString(R.string.api_key_kinopoisk));
        kinopoiskAPI.getFilmSequelsAndPrequels(KINOPOISK_ID, new KinopoiskAPI.RequestCallbackSequelAndPrequel() {
            @Override
            public void onSuccessRequestPrequel(List<FilmRelation> filmRelation) {
                adapterRecyclerViewSequels.setFilmRelation(filmRelation);
            }

            @Override
            public void onFailureRequestPrequel(IOException e) {
                String[] splitter = e.getMessage().split("\\|");
                String codeResponse = splitter[0].replace("Код ответа: ", "");
                switch (codeResponse) {
                    case "200":
                        showNotFound404("Неизвестная ошибка но код ответа 200");
                        break;
                    case "401":
                        showNotFound404("Пустой или неправильный токен");
                        break;
                    case "404":
                        showNotFound404("Данные отсутствуют");
                        break;
                    case "402":
                        showNotFound404("Превышен лимит запросов(или дневной, или общий)");
                        break;
                    case "429":
                        showNotFound404("Слишком много запросов. Общий лимит - 20 запросов в секунду");
                        break;
                }
                // 200 - Запрос выполнен успешно
                // 401 - Пустой или неправильный токен
                // 404 - Фильм не найден
                // 402 - Превышен лимит запросов(или дневной, или общий)
                // 429 - Слишком много запросов. Общий лимит - 20 запросов в секунду
            }

            @Override
            public void finishRequestPrequel() {
                adapterRecyclerViewSequels.notifyDataSetChanged();
            }
        });


        return binding.getRoot();
    }

    private void showNotFound404(String msg) {
        binding.textView220.setText(msg.isEmpty() ? "Файлы отсутствуют!" : msg);
        binding.rootNotFoundSequels.setVisibility(View.VISIBLE);
        binding.lottieNotFoundSequels.setAnimation(R.raw.not_found);
        binding.lottieNotFoundSequels.playAnimation();
    }


    private static class AdapterRecyclerViewSequels extends RecyclerView.Adapter<AdapterRecyclerViewSequels.MyViewHolder> {


        public List<FilmRelation> getFilmRelation() {
            return filmRelation;
        }

        public void setFilmRelation(List<FilmRelation> filmRelation) {
            this.filmRelation = filmRelation;
        }

        private List<FilmRelation> filmRelation = new ArrayList<>();


        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_2, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            Picasso.get().load(filmRelation.get(position).getPosterUrlPreview()).into(holder.imageViewPosterFilmItem1);
            holder.textViewTitleFilmItem1.setText(
                    filmRelation.get(position).getNameRu().isEmpty()
                            ? filmRelation.get(position).getNameEn()
                            : filmRelation.get(position).getNameRu());
        }

        @Override
        public int getItemCount() {
            return filmRelation.size();
        }

        private static class MyViewHolder extends RecyclerView.ViewHolder {
            private TextView textViewTitleFilmItem1;
            private ImageView imageViewPosterFilmItem1;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewTitleFilmItem1 = itemView.findViewById(R.id.textViewTitleFilmItem2);
                imageViewPosterFilmItem1 = itemView.findViewById(R.id.imageViewPosterFilmItem5);
            }
        }
    }


}