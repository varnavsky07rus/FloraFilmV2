package com.alaka_ala.florafilm.ui.fragments.favorites.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.ui.util.local.FavoriteMoviesManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FavoriteRecyclerViewAdapter extends RecyclerView.Adapter<FavoriteRecyclerViewAdapter.MyViewHolderFavorite> {

    public void setData(List<FavoriteMoviesManager.MovieData> data) {
        this.data = data;
    }
    public void addData(FavoriteMoviesManager.MovieData data){
        this.data.add(data);
    }

    private List<FavoriteMoviesManager.MovieData> data = new ArrayList<>();

    // Нужно доабвить окно с сохраненными фильмами

    @NonNull
    @Override
    public MyViewHolderFavorite onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.rv_item_2, null);
        return new MyViewHolderFavorite(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolderFavorite holder, int position) {
        holder.textViewTitleFilmItem2.setText(data.get(position).getName());
        Picasso.get().load(data.get(position).getUrl()).into(holder.imageViewPosterFilmItem2);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class MyViewHolderFavorite extends RecyclerView.ViewHolder {
        private final ImageView imageViewPosterFilmItem2;
        private final TextView textViewTitleFilmItem2;

        public MyViewHolderFavorite(@NonNull View itemView) {
            super(itemView);
            imageViewPosterFilmItem2 = itemView.findViewById(R.id.imageViewPosterFilmItem2);
            textViewTitleFilmItem2 = itemView.findViewById(R.id.textViewTitleFilmItem2);
        }
    }

}
