package com.alaka_ala.florafilm.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.airbnb.lottie.LottieAnimationView;
import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentContactBinding;


public class ContactFragment extends Fragment {
    private FragmentContactBinding binding;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentContactBinding.inflate(inflater, container, false);

        LottieAnimationView lottieAnimationView = binding.lottieAnimSupport;
        lottieAnimationView.setAnimation(R.raw.support);
        lottieAnimationView.playAnimation();

        Button buttonContact = binding.btnContact;
        buttonContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String telegramUsername = "Alaka_ala"; // Замените на ваш ник в Telegram
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/" + telegramUsername));
                startActivity(intent);
            }
        });



        return binding.getRoot();
    }
}