package com.alaka_ala.florafilm.ui.fragments.settings;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alaka_ala.florafilm.databinding.FragmentSettingsBinding;
import com.alaka_ala.florafilm.ui.activities.MainActivity;
import com.alaka_ala.florafilm.ui.util.updater.AppUpdater;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.io.FileUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Фрагмент для отображения настроек приложения.
 */
public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;

    /**
     * Вызывается для создания иерархии представлений, связанной с фрагментом.
     *
     * @param inflater           Объект LayoutInflater, который можно использовать для раздувания любых представлений во фрагменте,
     * @param container          Если не равно null, это родительское представление, к которому будет прикреплен пользовательский интерфейс фрагмента.
     * @param savedInstanceState Если не равно null, этот фрагмент создается заново из предыдущего сохраненного состояния.
     * @return Возвращает представление для пользовательского интерфейса фрагмента.
     */
    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        // Инициализация и настройка переключателя для поиска на Vibix
        MaterialSwitch switch_off_search_vibix = binding.switchOffSearchVibix;
        boolean isActiveSearchSerialVibix = SettingsUtils.getParamSearchVIBIX(getContext());
        switch_off_search_vibix.setChecked(isActiveSearchSerialVibix);
        switch_off_search_vibix.setText(isActiveSearchSerialVibix ? "Выключить поиск Vibix" : "Включить поиск Vibix");
        switch_off_search_vibix.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingsUtils.setParamSearchVibix(getContext(), isChecked);
                switch_off_search_vibix.setText(isChecked ? "Выключить поиск Vibix" : "Включить поиск Vibix");
            }
        });

        // Инициализация и настройка переключателя для поиска на HDVB
        MaterialSwitch switch_off_search_hdvb = binding.switchOffSearchHdvb;
        boolean isActiveSearchSerialHdvb = SettingsUtils.getParamSeeachHDVB(getContext());
        switch_off_search_hdvb.setChecked(isActiveSearchSerialHdvb);
        switch_off_search_hdvb.setText(isActiveSearchSerialHdvb ? "Выключить поиск HDVB" : "Включить поиск HDVB");
        switch_off_search_hdvb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingsUtils.setParamSearchHDVB(getContext(), isChecked);
                switch_off_search_hdvb.setText(isChecked ? "Выключить поиск HDVB" : "Включить поиск HDVB");
            }
        });

        // Инициализация и настройка переключателя для поиска торрентов
        MaterialSwitch switch_off_search_torrents = binding.switchOffSearchTorrents;
        boolean isActiveSearchTorrent = SettingsUtils.getParamSearchTorrent(getContext());
        switch_off_search_torrents.setChecked(isActiveSearchTorrent);
        switch_off_search_torrents.setText(isActiveSearchTorrent ? "Выключить поиск торрентов" : "Включить поиск торрентов");
        switch_off_search_torrents.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingsUtils.setParamSearchTorrent(getContext(), isChecked);
                switch_off_search_torrents.setText(isChecked ? "Выключить поиск торрентов" : "Включить поиск торрентов");
            }
        });

        // Инициализация и настройка кнопки для проверки обновлений
        Button buttonChekUpdate = binding.buttonChekUpdate;
        buttonChekUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUpdater appUpdater = new AppUpdater(getActivity(), false);
                appUpdater.checkForUpdate(null);
            }
        });

        // Отображение имени версии приложения
        TextView textViewVersionName = binding.textViewVersionName;
        textViewVersionName.setText("Текущая: " + getAppVersionName());

        // Инициализация и настройка переключателя для бета-версий
        MaterialSwitch switch_off_beta_version = binding.switchOffBetaVersion;
        boolean isActiveBetaVersion = SettingsUtils.getParamBetaVersion(getContext());
        switch_off_beta_version.setChecked(isActiveBetaVersion);
        switch_off_beta_version.setText(isActiveBetaVersion ? "Выключить бета-версии" : "Включить бета-версии");
        switch_off_beta_version.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switch_off_beta_version.setText(isChecked ? "Выключить бета-версии" : "Включить бета-версии");
                SettingsUtils.setParamBetaVersion(getContext(), isChecked);
            }
        });

        // Инициализация и настройка переключателя для эффекта прокрутки страницы
        MaterialSwitch switch_off_effect_scroll_page = binding.switchOffEffectScrollPage;
        boolean isActiveEffectScrollPage = SettingsUtils.getParamScrollPageEffect(getContext());
        switch_off_effect_scroll_page.setChecked(isActiveEffectScrollPage);
        switch_off_effect_scroll_page.setText(isActiveEffectScrollPage ? "Выключить эффекты прокрутки страницы" : "Включить эффекты прокрутки страницы");
        switch_off_effect_scroll_page.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingsUtils.setParamScrollPageEffect(getContext(), isChecked);
                switch_off_effect_scroll_page.setText(isChecked ? "Выключить эффекты прокрутки страницы" : "Включить эффекты прокрутки");
                Toast.makeText(getContext(), "Неокторые эффекты были принудительно " + (isChecked ? "включены" : "выключены"), Toast.LENGTH_LONG).show();
                requireActivity().recreate();
            }
        });

        // Инициализация и настройка переключателя для эффекта анимации
        MaterialSwitch switch_off_effect_animation = binding.switchOffEffectAnimation;
        boolean isActiveEffectAnimation = SettingsUtils.getParamPageEffectAnimation(getContext());
        switch_off_effect_animation.setChecked(isActiveEffectAnimation);
        switch_off_effect_animation.setText(isActiveEffectAnimation ? "Выключить анимации" : "Включить анимации");
        switch_off_effect_animation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingsUtils.setParamPageEffectAnimation(getContext(), isChecked);
                switch_off_effect_animation.setText(isChecked ? "Выключить анимации" : "Включить анимации");
            }
        });


        // Инициализация и настройка переключателя для включения нового макета описания фильма
        MaterialSwitch switch_off_layout_description_film = binding.switchOffLayoutDescriptionFilm;
        boolean isActiveLayoutDescriptionFilm = SettingsUtils.getParamLayoutDescriptionFilm(getContext());
        switch_off_layout_description_film.setChecked(isActiveLayoutDescriptionFilm);
        switch_off_layout_description_film.setText(isActiveLayoutDescriptionFilm ? "Выключить новый макет описания фильма" : "Включить новый макет описания фильма");
        switch_off_layout_description_film.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingsUtils.setParamLayoutDescriptionFilm(getContext(), isChecked);
                switch_off_layout_description_film.setText(isChecked ? "Выключить новый макет описания фильма" : "Включить новый макет описания фильма");
            }
        });


        // Очистка кэша
        TextView txtSizeCache = binding.txtSizeCache;
        txtSizeCache.setText(SettingsUtils.getSizeCacheApp(getContext()));
        Button btnClearCache = binding.btnClearCache;


        btnClearCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getContext() == null) return;
                new MaterialAlertDialogBuilder(getContext()).setItems(new String[]{"Очистить кэш", "Очистить все данные"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            SettingsUtils.clearCache(getContext(), false);
                            txtSizeCache.setText(SettingsUtils.getSizeCacheApp(getContext()));
                        } else {
                            SettingsUtils.clearCache(getContext(), true);
                            txtSizeCache.setText(SettingsUtils.getSizeCacheApp(getContext()));
                        }
                    }
                }).show();

            }
        });



        if (isNtVersion()) {
            binding.frameLayoutUpdateContainer.setVisibility(View.VISIBLE);
            binding.divider3.setVisibility(View.VISIBLE);
        }


        return binding.getRoot();
    }

    /**
     * Получает имя версии приложения.
     *
     * @return Имя версии приложения или "N/A" в случае ошибки.
     */
    public String getAppVersionName() {
        try {
            String packageName = requireContext().getPackageName();
            PackageInfo pInfo = requireContext().getPackageManager().getPackageInfo(packageName, 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "N/A";
        }
    }

    /** Получение буквенного обозначения версии приложения. Подробнее см. в gradle.app*/
    private boolean isNtVersion() {
        String vName = getAppVersionName();
        Pattern symbolName = Pattern.compile("(?<=\\_)[a-zA-Z]+");
        Matcher matcher = symbolName.matcher(vName);
        if (matcher.find()) {
            vName = matcher.group();
        }
        return vName.equals("nt");
    }


}