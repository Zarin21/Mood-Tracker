package com.example.unemployedavengers.friendSection;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.unemployedavengers.R;
import com.example.unemployedavengers.databinding.FriendsHistoryBinding;

public class FriendsHistory extends Fragment {
    private FriendsHistoryBinding binding;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FriendsHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.addFriendButton.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_friendsHistoryFragment_to_userSearchFragment)
        );


    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Prevent memory leaks
        binding = null;
    }
}
