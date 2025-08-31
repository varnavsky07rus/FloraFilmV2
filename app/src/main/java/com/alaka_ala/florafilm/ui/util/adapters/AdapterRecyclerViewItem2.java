package com.alaka_ala.florafilm.ui.util.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.Collection;
import com.squareup.picasso.Picasso;

public class AdapterRecyclerViewItem2 extends RecyclerView.Adapter<AdapterRecyclerViewItem2.ViewHolder> {
    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    private Collection collection;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.rv_item_2, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String title = !collection.getItems().get(position).getNameRu().equals("null") ? collection.getItems().get(position).getNameRu() : collection.getItems().get(position).getNameOriginal();
        if (title.equals("null")) title = collection.getItems().get(position).getNameEn();
        if (title.equals("null")) title = "";
        holder.textViewTitleFilmItem2.setText(title);
        Picasso.get().load(collection.getItems().get(position).getPosterUrlPreview()).into(holder.imageViewPosterFilmItem2);
    }

    @Override
    public int getItemCount() {
        if (collection == null) return 0;
        return collection.getItems().size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageViewPosterFilmItem2;
        private final TextView textViewTitleFilmItem2;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewPosterFilmItem2 = itemView.findViewById(R.id.imageViewPosterFilmItem2);
            textViewTitleFilmItem2 = itemView.findViewById(R.id.textViewTitleFilmItem2);
        }
    }

}
