package com.alaka_ala.florafilm.ui.fragments.home.vpager.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.alaka_ala.florafilm.ui.fragments.home.vpager.popAll.ViewpagerItemPopularAllFragment;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.Collection;

public class ViewPagerAdapterHome extends FragmentStateAdapter {
    private final Context context;

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    private Collection collection;


    public ViewPagerAdapterHome(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, Context context) {
        super(fragmentManager, lifecycle);
        this.context = context;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return new ViewpagerItemPopularAllFragment(
                collection.getItems().get(position).getPosterUrl(),
                collection.getItems().get(position).getKinopoiskId());
    }

    @Override
    public int getItemCount() {
        if (collection == null) return 0;
        if (collection.getItems() == null) return 0;
        return collection.getItems().size();
    }
}
