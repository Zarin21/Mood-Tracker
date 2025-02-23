package com.example.unemployedavengers.friendSection;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.unemployedavengers.DAO.IUserDAO;
import com.example.unemployedavengers.R;

import com.example.unemployedavengers.databinding.UserSearchBinding;
import com.example.unemployedavengers.implementationDAO.UserDAOImplement;
import com.example.unemployedavengers.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserSearch extends Fragment {
    private UserSearchBinding binding;
    private IUserDAO userDAO;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = UserSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userDAO = new UserDAOImplement();
        binding.searchButton.setOnClickListener(v -> {
            String searchQuery = binding.etUsername.getText().toString().trim();
            if (searchQuery.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a username to search", Toast.LENGTH_SHORT).show();
                return;
            }

            userDAO.searchUsers(searchQuery)
                    .addOnSuccessListener(userList -> {
                        List<String> usernames = new ArrayList<>();
                        for (User user : userList) {
                            usernames.add(user.getUsername());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                getContext(),
                                R.layout.search_item,
                                R.id.username_text,
                                usernames
                        );
                        binding.searchList.setAdapter(adapter);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        binding.searchList.setOnItemClickListener((parent, v, position, id) -> {
            String selectedUsername = (String) parent.getItemAtPosition(position);

            userDAO.getCurrentUserProfile()
                    .addOnSuccessListener(requesterUser -> {
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Follow User?")
                                .setMessage("Do you want to follow " + selectedUsername + "?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    userDAO.getUserByUsername(selectedUsername)
                                            .addOnSuccessListener(targetUser -> {
                                                if (targetUser != null) {
                                                    userDAO.requestFollow(requesterUser.getUserId(), targetUser.getUserId())
                                                            .addOnSuccessListener(aVoid -> {
                                                                Toast.makeText(getContext(), "Follow request sent", Toast.LENGTH_SHORT).show();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(getContext(), "Failed to send request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            });
                                                } else {
                                                    Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(getContext(), "Error retrieving user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                                .show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error retrieving current user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });




    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
