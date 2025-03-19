package com.example.unemployedavengers.maps;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.unemployedavengers.R;
import com.example.unemployedavengers.databinding.MapBinding;
import com.example.unemployedavengers.models.MoodEvent;

import java.util.ArrayList;

public class Map extends Fragment {
    private ArrayList<MoodEvent> moodEvents;
    private MapBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the view using view binding
        binding = MapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.personalMapButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_mapFragment_to_personalMapFragment);
        });
        binding.friendsMapButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_mapFragment_to_friendsMap);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Prevent memory leaks by nullifying the binding
        binding = null;
    }
}
