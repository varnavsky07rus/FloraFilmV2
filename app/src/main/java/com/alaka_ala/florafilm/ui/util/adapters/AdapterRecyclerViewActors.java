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
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.KinopoiskAPI;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.ListStaffItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterRecyclerViewActors extends RecyclerView.Adapter<AdapterRecyclerViewActors.ViewHolder> {
    private ArrayList<ListStaffItem> listStaffItem = new ArrayList<>();

    public AdapterRecyclerViewActors(LayoutInflater layoutInflater) {
        this.layoutInflater = layoutInflater;
    }

    private LayoutInflater layoutInflater;

    @SuppressLint("NotifyDataSetChanged")
    public void setListActors(ArrayList<ListStaffItem> listStaffItem) {
        this.listStaffItem = listStaffItem;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.rv_item_actor, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ListStaffItem listStaffItem = this.listStaffItem.get(position);
        holder.textViewNameActor.setText(!listStaffItem.getNameRu().isEmpty() ? listStaffItem.getNameRu() : listStaffItem.getNameEn());
        holder.textViewNameActorEn.setText(!listStaffItem.getNameEn().isEmpty() ? listStaffItem.getNameEn() : "");
        holder.textViewRoleActor.setText(listStaffItem.getProfessionText().equals("null") ? "" : listStaffItem.getProfessionText());
        Picasso.get().load(listStaffItem.getPosterUrl()).into(holder.imageViewActor);
    }

    @Override
    public int getItemCount() {
        if (listStaffItem == null) {
            return 0;
        }
        return listStaffItem.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewActor;
        private TextView textViewNameActor;
        private TextView textViewNameActorEn;
        private TextView textViewRoleActor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewActor = itemView.findViewById(R.id.imageViewActor);
            textViewNameActor = itemView.findViewById(R.id.textViewNameActor);
            textViewNameActorEn = itemView.findViewById(R.id.textViewNameActorEn);
            textViewRoleActor = itemView.findViewById(R.id.textViewRoleActor);
        }
    }


}
