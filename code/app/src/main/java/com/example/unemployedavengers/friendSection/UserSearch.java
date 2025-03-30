/**
 * UserSearch Fragment allows users to search for other users by username.
 *
 * Key functionalities:
 * - Allows the current user to input a search query (username).
 * - Queries the user database for matching usernames and displays the results.
 * - Allows the user to select a username from the search results to view their profile.
 * - Provides feedback for invalid or failed searches.
 */

package com.example.unemployedavengers.friendSection;

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

public class  UserSearch extends Fragment {
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

            Bundle bundle = new Bundle();
            bundle.putString("selectedUsername", selectedUsername);

            Navigation.findNavController(v).navigate(R.id.action_userSearch_to_userProfile, bundle);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
