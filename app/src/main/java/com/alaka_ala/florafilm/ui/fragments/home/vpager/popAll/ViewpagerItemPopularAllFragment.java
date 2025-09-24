package com.alaka_ala.florafilm.ui.fragments.home.vpager.popAll;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentViewpagerItemPopularAllBinding;
import com.squareup.picasso.Picasso;

public class ViewpagerItemPopularAllFragment extends Fragment {
    public ViewpagerItemPopularAllFragment(String urlImage, int kinopoisk_id) {
        this.urlImage = urlImage;
        this.kinopoisk_id = kinopoisk_id;
    }

    public ViewpagerItemPopularAllFragment() {
        this.urlImage = urlImage;
    }

    private String urlImage;
    private int kinopoisk_id;



    private FragmentViewpagerItemPopularAllBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentViewpagerItemPopularAllBinding.inflate(inflater, container, false);
        Picasso.get().load(urlImage).into(binding.imageView13);


        return binding.getRoot();
    }



}