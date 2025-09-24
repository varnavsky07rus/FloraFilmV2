package com.alaka_ala.florafilm.ui.fragments.imgViewer.vpFragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentImageBinding;
import com.alaka_ala.florafilm.ui.util.local.AsyncThreadBuilder;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public class ImageFragment extends Fragment {
    private FragmentImageBinding binding;

    public ImageFragment() {}

    public ImageFragment(String url) {
        this.url = url;
    }
    private String url;
    private PhotoView photo_view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentImageBinding.inflate(inflater, container, false);
        photo_view = binding.photoView;

        if (!url.isEmpty()) {
            Picasso.get()
                    .load(url)
                    .fit() // или .fit() для автоматического подгона под ImageView
                    .centerInside()
                    .into(photo_view);

            // Двойной клик для зума
            photo_view.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    return false;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if (photo_view.getScale() > photo_view.getMinimumScale()) {
                        photo_view.setScale(photo_view.getMinimumScale(), true);
                    } else {
                        photo_view.setScale(photo_view.getMaximumScale(), e.getX(), e.getY(), true);
                    }
                    return true;
                }

                @Override
                public boolean onDoubleTapEvent(MotionEvent e) {
                    return false;
                }
            });
        }


        return binding.getRoot();
    }









}