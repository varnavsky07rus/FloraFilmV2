package com.alaka_ala.florafilm.ui.fragments.imgViewer;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentImageViewerBinding;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;


public class ImageViewerFragment extends Fragment {
    private FragmentImageViewerBinding binding;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentImageViewerBinding.inflate(inflater, container, false);

        String url = getArguments().getString("url","");
        PhotoView photo_view = binding.photoView;

        if (!url.isEmpty()) {
            Picasso.get().load(url).into(photo_view);

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