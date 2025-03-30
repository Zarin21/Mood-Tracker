/**
 * Map is a Fragment that serves as the entry point for navigating between different types of maps.
 * It provides buttons for the user to navigate to the following views:
 * - A personal map view displaying the user's own mood events.
 * - A friends' map view showing mood events shared by friends.
 * - A map view displaying mood events within a 5 km radius of the user.
 *
 * Key Features:
 * - Provides navigation to different map views using buttons.
 * - Handles navigation logic for the personal map, friends' map, and nearby mood events.
 * - Utilizes view binding for easier interaction with UI components.
 *
 * Methods:
 * - onViewCreated: Sets up onClickListeners for the map navigation buttons.
 * - onDestroyView: Nullifies the binding to avoid memory leaks when the view is destroyed.
 */

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
        binding.nearMapButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_mapFragment_to_WithInFiveKm);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Prevent memory leaks by nullifying the binding
        binding = null;
    }
}
