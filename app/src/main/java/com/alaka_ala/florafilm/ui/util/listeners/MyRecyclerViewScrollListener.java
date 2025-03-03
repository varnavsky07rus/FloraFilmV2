package com.alaka_ala.florafilm.ui.util.listeners;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class MyRecyclerViewScrollListener extends RecyclerView.OnScrollListener {
    public static final int HORIZONTAL = RecyclerView.HORIZONTAL;
    public static final int VERTICAL = RecyclerView.VERTICAL;

    @IntDef ({HORIZONTAL, VERTICAL})
    public @interface DefsOrientationRecycler {};

    public int getOrientation() {
        return orientation;
    }

    public MyRecyclerViewScrollListener(int orientation) {
        this.orientation = orientation;
    }

    private final int orientation;

    public abstract void onStart();

    public abstract void onEnd();


    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (orientation == VERTICAL) {
            if (!recyclerView.canScrollVertically(1) && dy > 0) {
                // Упираемся в подвал
                onEnd();
            } else if (!recyclerView.canScrollVertically(-1) && dy < 0) {
                // упираемся в потолок
                onStart();
            }
        } else if (orientation == HORIZONTAL) {
            if (!recyclerView.canScrollHorizontally(1) && dx > 0) {
                // Упираемся в подвал
                onEnd();
            } else if (!recyclerView.canScrollHorizontally(-1) && dx < 0) {
                // упираемся в потолок
                onStart();
            }
        }
    }
}
