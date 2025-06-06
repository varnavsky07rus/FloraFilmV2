package com.alaka_ala.florafilm.ui.fragments.actors.films;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentActorFilmsBinding;
import com.alaka_ala.florafilm.ui.util.adapters.AdapterRecyclerViewItemActorFilms;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.KinopoiskAPI;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.StaffInfo;
import com.alaka_ala.florafilm.ui.util.listeners.MyRecyclerViewItemTouchListener;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.io.IOException;


public class ActorFilmsFragment extends Fragment {
    private FragmentActorFilmsBinding binding;
    private KinopoiskAPI kinopoiskAPI;
    private RecyclerView rvFilmsActor;
    private AdapterRecyclerViewItemActorFilms adapter;
    private ActorFilmsViewModel viewModel;

    private StaffInfo staffInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentActorFilmsBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(ActorFilmsViewModel.class);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Фильмы Актёра");

        kinopoiskAPI = new KinopoiskAPI(getString(R.string.api_key_kinopoisk));

        int staffId = getArguments().getInt("staffId", 0);
        if (staffId == 0) {
            Snackbar.make(binding.getRoot(), "Ошибка загрузки данных", Snackbar.LENGTH_SHORT).show();
            return binding.getRoot();
        }

        staffInfo = viewModel.getListStaffItem(getContext(), staffId);

        rvFilmsActor = binding.rvFilmsActor;
        rvFilmsActor.setLayoutManager(new GridLayoutManager(getContext(), 3, LinearLayoutManager.VERTICAL, false));
        rvFilmsActor.setAdapter(adapter = new AdapterRecyclerViewItemActorFilms());


        if (staffInfo == null) {
            kinopoiskAPI.getInformationStaff(staffId, new KinopoiskAPI.RequestCallbackInformationStaff() {
                @Override
                public void onSuccessInfoStaff(StaffInfo staffInfos) {
                    staffInfo = staffInfos;
                    viewModel.saveActorInfo(getContext(), staffId, staffInfo);
                    adapter.addCollection(staffInfo.getFilms());
                    setInfoStaff();
                }

                @Override
                public void onFailureInfoStaff(IOException e) {
                    if (getContext() != null) {
                        Snackbar.make(binding.getRoot(), "Ошибка загрузки данных", Snackbar.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void finishInfoStaff() {

                }
            });
        } else {
            adapter.addCollection(staffInfo.getFilms());
        }

        rvFilmsActor.addOnItemTouchListener(new MyRecyclerViewItemTouchListener(getContext(), rvFilmsActor, new MyRecyclerViewItemTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView.ViewHolder holder, View view, int position) {
                if (staffInfo == null) return;
                Bundle bundle = new Bundle();
                if (position <= -1) return;
                bundle.putInt("kinopoisk_id", staffInfo.getFilms().get(position).getFilmId());
                Navigation.findNavController(view).navigate(R.id.action_actorFilmsFragment_to_mainFilmFragment, bundle);
            }

            @Override
            public void onLongItemClick(RecyclerView.ViewHolder holder, View view, int position) {

            }
        }));

        setInfoStaff();


        return binding.getRoot();
    }

    private void setInfoStaff() {
        if (staffInfo != null) {
            ImageView actorImg = binding.actorInfo.imageViewActor;
            Picasso.get().load(staffInfo.getPosterUrl()).into(actorImg);

            TextView textViewNameActor = binding.actorInfo.textViewNameActor;
            textViewNameActor.setText((staffInfo.getNameRu().isEmpty() ? staffInfo.getNameEn() : staffInfo.getNameRu()));

            TextView textViewNameActorEn = binding.actorInfo.textViewNameActorEn;
            textViewNameActorEn.setText(staffInfo.getNameEn());

            TextView textViewRoleActor = binding.actorInfo.textViewRoleActor;
            textViewRoleActor.setText(staffInfo.getProfession());
        }
    }

}