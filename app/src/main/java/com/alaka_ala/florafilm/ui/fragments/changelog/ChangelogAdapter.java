package com.alaka_ala.florafilm.ui.fragments.changelog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;

import java.util.List;

public class ChangelogAdapter extends RecyclerView.Adapter<ChangelogAdapter.ChangelogViewHolder> {

    private final List<ChangelogItem> changelogItems;

    public ChangelogAdapter(List<ChangelogItem> changelogItems) {
        this.changelogItems = changelogItems;
    }

    @NonNull
    @Override
    public ChangelogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_changelog, parent, false);
        return new ChangelogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChangelogViewHolder holder, int position) {
        ChangelogItem item = changelogItems.get(position);
        holder.versionTextView.setText(item.getVersion());
        holder.dateTextView.setText(item.getDate());
        holder.descriptionTextView.setText(item.getDescription());
    }

    @Override
    public int getItemCount() {
        return changelogItems.size();
    }

    static class ChangelogViewHolder extends RecyclerView.ViewHolder {
        TextView versionTextView;
        TextView dateTextView;
        TextView descriptionTextView;

        public ChangelogViewHolder(@NonNull View itemView) {
            super(itemView);
            versionTextView = itemView.findViewById(R.id.versionTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
        }
    }
}
