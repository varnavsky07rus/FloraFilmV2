package com.alaka_ala.florafilm.ui.fragments.actors;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alaka_ala.florafilm.R;
import com.alaka_ala.florafilm.databinding.FragmentActorsBinding;
import com.alaka_ala.florafilm.ui.fragments.actors.films.ActorFilmsFragment;
import com.alaka_ala.florafilm.ui.util.adapters.AdapterRecyclerViewActors;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.KinopoiskAPI;
import com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.ListStaffItem;
import com.alaka_ala.florafilm.ui.util.listeners.MyRecyclerViewItemTouchListener;

import java.io.IOException;
import java.util.ArrayList;

public class ActorsFragment extends Fragment {
    private FragmentActorsBinding binding;
    private ActorsViewModel viewModel;
    private RecyclerView rvActors;
    private KinopoiskAPI kinopoiskAPI;
    private AdapterRecyclerViewActors adapter;
    private ArrayList<ListStaffItem> listStaffItem = new ArrayList<>();
    private int kinopoisk_id = 0;
    private String nameRu;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentActorsBinding.inflate(inflater, container, false);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        kinopoiskAPI = new KinopoiskAPI(getString(R.string.api_key_kinopoisk));
        viewModel = new ViewModelProvider(this).get(ActorsViewModel.class);
        Bundle bundle = getArguments();
        if (bundle != null) {
            kinopoisk_id = bundle.getInt("kinopoisk_id");
            nameRu = bundle.getString("nameRu");
            assert actionBar != null;
            actionBar.setTitle(nameRu);
        } else {
            return binding.getRoot();
        }

        listStaffItem = viewModel.getListStaffItem(getContext(), kinopoisk_id);

        rvActors = binding.rvActors;
        rvActors.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext(), LinearLayoutManager.VERTICAL, false));
        rvActors.setAdapter(adapter = new AdapterRecyclerViewActors(inflater));

        if (listStaffItem != null) {
            adapter.setListActors(listStaffItem);
        } else {
            kinopoiskAPI.getListStaff(kinopoisk_id, new KinopoiskAPI.RequestCallbackStaffList() {
                @Override
                public void onSuccessStaffList(ArrayList<ListStaffItem> listStaffItems) {
                    listStaffItem = listStaffItems;
                    viewModel.addItemFilmInfoMap(getContext(),kinopoisk_id, listStaffItem);
                    adapter.setListActors(listStaffItem);
                }

                @Override
                public void onFailureStaffList(IOException e) {

                }

                @Override
                public void finishStafList() {

                }
            });
        }

        rvActors.addOnItemTouchListener(new MyRecyclerViewItemTouchListener(getContext(), rvActors, new MyRecyclerViewItemTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView.ViewHolder holder, View view, int position) {
                if (listStaffItem == null) return;
                int staffId = listStaffItem.get(position).getStaffId();
                Bundle bundle = new Bundle();
                bundle.putInt("staffId", staffId);
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_actorsFragment_to_actorFilmsFragment, bundle);
            }

            @Override
            public void onLongItemClick(RecyclerView.ViewHolder holder, View view, int position) {

            }
        }));




        return binding.getRoot();
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

}