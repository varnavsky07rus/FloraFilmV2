package com.alaka_ala.florafilm.ui.fragments.resumeView;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentResumeBottomSheetBinding;
import com.alaka_ala.florafilm.ui.util.local.ResumeLastMovie;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.squareup.picasso.Picasso;


public class ResumeBottomSheetFragment extends BottomSheetDialogFragment {
    public static final String TAG = "TAG_RESUME_BOTTOM_SHEET_FRAGMENT";
    private FragmentResumeBottomSheetBinding binding;

    private ResumeLastMovie resumeLastMovie;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentResumeBottomSheetBinding.inflate(inflater, container, false);

        resumeLastMovie = new ResumeLastMovie(getContext());

        int kinopoiskId = resumeLastMovie.getKinopoiskId();
        String name = resumeLastMovie.getName();
        String imgUrl = resumeLastMovie.getImgUrl();
        binding.textView24.setText(name);
        Picasso.get().load(imgUrl).into(binding.imageView5);
        binding.buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.buttonNext.setEnabled(false);
                Bundle bundle = new Bundle();
                bundle.putInt("kinopoisk_id", kinopoiskId);
                // TODO: Выяснить осталась ли ошибка при переходе. (раньше былаа ссылка с homeFragment на mainFilmFragment а теперь просто mainFilmFragment)
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment_activity_main).navigate(R.id.mainFilmFragment, bundle);
                onDismiss(getDialog());
            }
        });






        return binding.getRoot();
    }


    public static ResumeBottomSheetFragment newInstance() {
        Bundle args = new Bundle();
        ResumeBottomSheetFragment fragment = new ResumeBottomSheetFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        resumeLastMovie.clear();
    }
}