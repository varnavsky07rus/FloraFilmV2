package com.alaka_ala.florafilm.ui.util.listeners;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;

public class MyRecyclerViewItemTouchListener implements RecyclerView.OnItemTouchListener {

    private final GestureDetectorCompat gestureDetector;
    private final OnItemClickListener clickListener;
    private final RecyclerView recyclerView;
    private boolean isLongPress = false;

    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            isLongPress = false;
            return false;
        }
    });

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessage(0);
        }
    };

    public MyRecyclerViewItemTouchListener(Context context, final RecyclerView recyclerView, OnItemClickListener clickListener) {
        this.clickListener = clickListener;
        this.recyclerView = recyclerView;
        gestureDetector = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (!isLongPress) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null) {
                        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(recyclerView.getChildAdapterPosition(child));
                        clickListener.onItemClick(viewHolder, child, recyclerView.getChildAdapterPosition(child));
                    }}
                isLongPress = false;
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                isLongPress = true;
                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (child != null) {
                    RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(recyclerView.getChildAdapterPosition(child));
                    clickListener.onLongItemClick(viewHolder, child, recyclerView.getChildAdapterPosition(child));
                }
                handler.postDelayed(runnable, 1000);
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        return gestureDetector.onTouchEvent(e);
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    public interface OnItemClickListener {
        void onItemClick(RecyclerView.ViewHolder holder, View view, int position);

        void onLongItemClick(RecyclerView.ViewHolder holder, View view, int position);
    }
}
