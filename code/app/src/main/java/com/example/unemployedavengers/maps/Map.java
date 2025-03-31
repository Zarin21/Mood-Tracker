/**
 * Map - A Fragment that serves as the entry point for navigating between different types of mood event maps.
 * It allows the user to navigate to the following views:
 * - A personal map showing the user's own mood events.
 * - A friends' map displaying mood events shared by friends.
 * - A map showing mood events within a 5 km radius of the user.
 *
 * Purpose:
 * - Provides a simple interface to navigate between different map views in the application.
 * - Uses buttons for the user to choose which map to view: personal, friends, or nearby mood events.
 * - Ensures smooth navigation using the Navigation component for easy transitions.
 *
 * Key Features:
 * - Buttons for navigating to personal mood events, friends' mood events, and nearby mood events within a 5 km radius.
 * - Uses view binding for efficient and safe UI interaction.
 * - Leverages Android's Navigation component to facilitate clean transitions between fragments.
 *
 * Methods:
 * - onViewCreated: Sets up click listeners for buttons to navigate to different map views.
 * - onDestroyView: Nullifies the binding to avoid memory leaks when the fragment view is destroyed.
 *
 * Known Issues:
 * - There are no known major issues in this fragment. However, navigation relies on predefined actions, so any misconfiguration in the Navigation Graph could lead to navigation failures.
 * - The fragment assumes the user has network access for fetching data related to nearby mood events, but no explicit error handling is provided for offline scenarios.
 *
 * Design Patterns:
 * - This fragment follows the **MVVM** (Model-View-ViewModel) pattern, where navigation and UI updates are managed by the ViewModel, and user interactions are handled via View Binding.
 * - The **Navigation** component is used to handle fragment transitions, which simplifies and centralizes the navigation logic.
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
