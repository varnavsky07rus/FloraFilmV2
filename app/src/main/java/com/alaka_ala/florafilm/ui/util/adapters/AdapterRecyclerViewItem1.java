package com.alaka_ala.florafilm.ui.util.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.Collection;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.ListFilmItem;
import com.squareup.picasso.Picasso;

public class AdapterRecyclerViewItem1 extends RecyclerView.Adapter<AdapterRecyclerViewItem1.ViewHolder> {
    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    private Collection collection;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ПРАВИЛЬНЫЙ способ создания View: используется LayoutInflater и передается parent
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // Получаем элемент ОДИН раз, чтобы избежать повторных вызовов
        final ListFilmItem filmItem = collection.getItems().get(position);

        // Логика выбора названия стала чище
        String title = filmItem.getNameRu();
        if (title == null || title.equals("null")) {
            title = filmItem.getNameOriginal();
        }
        if (title == null || title.equals("null")) {
            title = filmItem.getNameEn();
        }
        if (title == null || title.equals("null")) {
            title = "";
        }

        holder.textViewTitleFilmItem1.setText(title);
        Picasso.get().load(filmItem.getPosterUrlPreview()).into(holder.imageViewPosterFilmItem1);
        holder.imageViewIsViewedItem1.setVisibility(View.GONE);
        holder.imageViewisFavoriteItem1.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        if (collection == null || collection.getItems() == null) return 0;
        return collection.getItems().size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageViewPosterFilmItem1;
        private final ImageView imageViewIsViewedItem1;
        private final ImageView imageViewisFavoriteItem1;
        private final TextView textViewTitleFilmItem1;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewPosterFilmItem1 = itemView.findViewById(R.id.imageViewPosterFilmItem1);
            imageViewIsViewedItem1 = itemView.findViewById(R.id.imageViewIsViewedItem1);
            imageViewisFavoriteItem1 = itemView.findViewById(R.id.imageViewisFavoriteItem1);
            textViewTitleFilmItem1 = itemView.findViewById(R.id.textViewTitleFilmItem1);
        }
    }
}
