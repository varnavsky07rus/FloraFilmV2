package com.alaka_ala.florafilm.ui.fragments.imgViewer.vpAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.alaka_ala.florafilm.ui.fragments.imgViewer.vpFragments.ImageFragment;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.ItemFilmImage;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerImageAdapter extends FragmentStateAdapter {
    public List<ItemFilmImage.Item> getItems() {
        return items;
    }

    public void addItems(List<ItemFilmImage.Item> items) {
        if (items == null || items.isEmpty()) return;
        if (this.items.isEmpty()) {
            this.items = items;
        } else {
            this.items.addAll(items);
        }
    }

    public void clearItems() {
        this.items.clear();
    }


    private List<ItemFilmImage.Item> items;

    public ViewPagerImageAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
        items = new ArrayList<>();
    }




    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return new ImageFragment(items.get(position).getImageUrl());
    }

    @Override
    public int getItemCount() {
        if (items == null) return 0;
        return items.size();
    }
}
