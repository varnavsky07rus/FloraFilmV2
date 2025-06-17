package com.alaka_ala.florafilm.ui.fragments.instructions;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.alaka_ala.florafilm.databinding.FragmentInstructionViewTorrentBinding;
import com.alaka_ala.florafilm.ui.util.local.InstallTorrServe;
import com.squareup.picasso.Picasso;


public class InstructionViewTorrentFragment extends Fragment {
    private FragmentInstructionViewTorrentBinding binding;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInstructionViewTorrentBinding.inflate(inflater, container, false);

        ImageView imageView3 = binding.imageView3;
        Picasso.get().load("https://cs4a0d.4pda.ws/24750223.png").into(imageView3);


        Button button = binding.button;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InstallTorrServe installTorrServe = new InstallTorrServe(getActivity());
                installTorrServe.downloadAndInstall();
            }
        });




        return binding.getRoot();
    }
}