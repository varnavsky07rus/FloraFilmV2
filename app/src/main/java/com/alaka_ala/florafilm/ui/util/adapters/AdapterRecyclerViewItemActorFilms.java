package com.alaka_ala.florafilm.ui.util.adapters;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.StaffFilmsItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterRecyclerViewItemActorFilms extends RecyclerView.Adapter<AdapterRecyclerViewItemActorFilms.ViewHolder> {
    @SuppressLint("NotifyDataSetChanged")
    public void addCollection(ArrayList<StaffFilmsItem> collection) {
        if (this.collection == null) {
            this.collection = collection;
        } else {
            this.collection.addAll(collection);
        }
        notifyDataSetChanged();
    }

    private ArrayList<StaffFilmsItem> collection;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.rv_item_1, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String title = !collection.get(position).getNameRu().equals("null") ? collection.get(position).getNameRu() : "";
        if (title.equals("null")) title = collection.get(position).getNameEn();
        if (title.equals("null")) title = "";
        holder.textViewTitleFilmItem1.setText(title);
        Picasso.get().load(collection.get(position).getPosterUrlPreview()).into(holder.imageViewPosterFilmItem1);
        holder.imageViewIsViewedItem1.setVisibility(View.GONE);
        holder.imageViewisFavoriteItem1.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        if (collection == null) return 0;
        return collection.size();
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
