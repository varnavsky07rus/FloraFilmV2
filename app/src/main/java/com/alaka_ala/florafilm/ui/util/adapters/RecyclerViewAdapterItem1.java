package com.alaka_ala.florafilm.ui.util.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.Collection;
import com.squareup.picasso.Picasso;

public class RecyclerViewAdapterItem1 extends RecyclerView.Adapter<RecyclerViewAdapterItem1.ViewHolder> {
    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    private Collection collection;
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.rv_item_1, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String title = !collection.getItems().get(position).getNameRu().equals("null") ? collection.getItems().get(position).getNameRu() : collection.getItems().get(position).getNameOriginal();
        if (title.equals("null")) title = collection.getItems().get(position).getNameEn();
        if (title.equals("null")) title = "";
        holder.textViewTitleFilmItem1.setText(title);
        Picasso.get().load(collection.getItems().get(position).getPosterUrlPreview()).into(holder.imageViewPosterFilmItem1);
        holder.imageViewIsViewedItem1.setVisibility(View.GONE);
        holder.imageViewisFavoriteItem1.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
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
