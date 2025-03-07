package com.example.unemployedavengers;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.navigation.Navigation;

import com.example.unemployedavengers.databinding.DashboardBinding;
import com.example.unemployedavengers.models.MoodEvent;

public class Dashboard extends Fragment {
    private DashboardBinding binding;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.friendsButton.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_dashboardFragment_to_friendsHistoryFragment)
        );
        binding.profileButton.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_dashboardFragment_to_profileFragment)
        );

        //Navigates to the input dialog
        binding.addMoodButton.setOnClickListener(v -> {
            Navigation.findNavController(v)
                    .navigate(R.id.action_dashboardFragment_to_inputDialog);


        });


        getParentFragmentManager().setFragmentResultListener("input_dialog_result", getViewLifecycleOwner(), new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                // Retrieve the MoodEvent object
                MoodEvent moodEvent = (MoodEvent) result.getSerializable("mood_event_key");
                if (moodEvent != null) {
                    Log.d("Dashboard", "Mood event received");
                    //addMoodEvent(moodEvent); // Store the mood event in Firestore
                }
            }
        });

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
