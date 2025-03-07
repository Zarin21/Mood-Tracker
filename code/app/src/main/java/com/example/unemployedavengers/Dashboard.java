package com.example.unemployedavengers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.unemployedavengers.databinding.DashboardBinding;

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

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
