package com.alaka_ala.florafilm.ui.fragments.film.others;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.alaka_ala.florafilm.R;
import java.util.ArrayList;

public class TreeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_FOLDER = 1;
    private static final int VIEW_TYPE_FILE = 2;
    // Ширина отступа для каждого уровня вложенности (в dp)
    private static final int INDENT_DP = 28;

    private final ArrayList<TreeItem> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onFolderClick(int position);
        void onFileClick(TreeItem.FileItem item);
    }

    public TreeAdapter(ArrayList<TreeItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    // Метод, который нужен для ItemDecoration
    public ArrayList<TreeItem> getItems() {
        return items;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof TreeItem.FolderItem) {
            return VIEW_TYPE_FOLDER;
        } else {
            return VIEW_TYPE_FILE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_FOLDER) {
            View view = inflater.inflate(R.layout.tree_item_folder, parent, false);
            return new FolderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.tree_item_file, parent, false);
            return new FileViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TreeItem item = items.get(position);
        Context context = holder.itemView.getContext();

        // Устанавливаем отступ в зависимости от уровня вложенности
        float indentPx = INDENT_DP * context.getResources().getDisplayMetrics().density;
        int paddingPx = (int) (item.getLevel() * indentPx);
        holder.itemView.setPadding(paddingPx, 0, 0, 0);

        if (holder.getItemViewType() == VIEW_TYPE_FOLDER) {
            FolderViewHolder folderViewHolder = (FolderViewHolder) holder;
            TreeItem.FolderItem folderItem = (TreeItem.FolderItem) item;
            folderViewHolder.title.setText(folderItem.getTitle());
            folderViewHolder.itemView.setOnClickListener(v -> listener.onFolderClick(holder.getAdapterPosition()));
        } else {
            FileViewHolder fileViewHolder = (FileViewHolder) holder;
            TreeItem.FileItem fileItem = (TreeItem.FileItem) item;
            fileViewHolder.title.setText(fileItem.getTitle());
            fileViewHolder.itemView.setOnClickListener(v -> listener.onFileClick(fileItem));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView icon;
        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textViewTitleFolder);
            icon = itemView.findViewById(R.id.imageView6);
        }
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView icon;
        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textViewTitleFiles);
            icon = itemView.findViewById(R.id.imageViewFiles);
        }
    }
}