package com.alaka_ala.florafilm.ui.fragments.donate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentDonateBinding;
import com.google.android.material.card.MaterialCardView;

public class DonateFragment extends Fragment {
    private FragmentDonateBinding binding;
    private boolean isAnimEnd = false;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDonateBinding.inflate(inflater, container, false);


        lottieAnim();
        mcvTon();
        mcvUsdt();
        mcvMir();
        mcvSber();

        return binding.getRoot();
    }

    private void mcvUsdt() {
        MaterialCardView mcvUSDT = binding.mcvUSDT;
        mcvUSDT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String usdt = "TNfg3BCWbssu71qyF9CQ86o7Ckdc7BVzpN";
                copyToClipboard(getContext(), usdt);
            }
        });
    }

    private void mcvMir() {
        MaterialCardView mcvMIR = binding.mcvMIR;
        mcvMIR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mir = "2200152995409839";
                copyToClipboard(getContext(), mir);
            }
        });
    }

    private void mcvSber() {
        MaterialCardView mcvSBER = binding.mcvSBER;
        mcvSBER.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://messenger.online.sberbank.ru/sl/YVcckaPoqMj1MDSXe";
                // Создаем Intent для открытия ссылки
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });
    }

    private void mcvTon() {
        MaterialCardView mcvTON = binding.mcvTON;
        mcvTON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String ton = "UQCh_VEadQs-_dHh0XR6WUgTFwx3xlTfhJvmEd_L8LDjxL_E";
                copyToClipboard(getContext(), ton);
            }
        });
    }

    private void lottieAnim() {
        LottieAnimationView lottieAnimationView = binding.lottieDonate;
        lottieAnimationView.setAnimation(R.raw.money);
        lottieAnimationView.setRepeatMode(LottieDrawable.REVERSE);
        lottieAnimationView.setRepeatCount(1);
        lottieAnimationView.setSpeed(1f);
        lottieAnimationView.playAnimation();
        lottieAnimationView.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(@NonNull Animator animation, boolean isReverse) {
                super.onAnimationEnd(animation, isReverse);
                isAnimEnd = true;
            }
        });
        lottieAnimationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAnimEnd) {
                    lottieAnimationView.playAnimation();
                    isAnimEnd = false;
                }
            }
        });
    }

    public void copyToClipboard(Context context, String text) {
        // Получаем ClipboardManager
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        // Создаем ClipData с текстом
        ClipData clipData = ClipData.newPlainText("Copied Text", text);

        // Устанавливаем ClipData в буфер обмена
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(getContext(), "Скопировано: " + text, Toast.LENGTH_SHORT).show();
        }
    }

}