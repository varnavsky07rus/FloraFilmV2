package com.alaka_ala.florafilm.ui.fragments.settings;

import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentSettingsBinding;
import com.alaka_ala.florafilm.ui.util.updater.AppUpdater;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container,false);


        MaterialSwitch switch_off_search_vibix = binding.switchOffSearchVibix;
        boolean isActiveSearchSerialVibix = SettingsUtils.getParamSearchVIBIX(getContext());
        switch_off_search_vibix.setChecked(isActiveSearchSerialVibix);
        switch_off_search_vibix.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingsUtils.setParamSearchVibix(getContext(), isChecked);
            }
        });


        MaterialSwitch switch_off_search_hdvb = binding.switchOffSearchHdvb;
        boolean isActiveSearchSerialHdvb = SettingsUtils.getParamSeeachHDVB(getContext());
        switch_off_search_hdvb.setChecked(isActiveSearchSerialHdvb);
        switch_off_search_hdvb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingsUtils.setParamSearchHDVB(getContext(), isChecked);
            }
        });


        Button buttonChekUpdate = binding.buttonChekUpdate;
        buttonChekUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Проверка обновления (вызывать из Activity)
                // Во Fragment:
                try {
                    String versionName = requireContext().getPackageManager()
                            .getPackageInfo(requireContext().getPackageName(), 0)
                            .versionName;
                    new AppUpdater(getContext()).checkForUpdate(versionName);
                } catch (PackageManager.NameNotFoundException e) {
                    throw new RuntimeException(e);
                }

            }
        });







        return binding.getRoot();
    }
}