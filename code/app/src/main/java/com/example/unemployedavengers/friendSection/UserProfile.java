/**
 * UserProfile Fragment displays the profile of a selected user and allows the current user to follow them.
 *
 * This fragment:
 * - Retrieves the current user's profile and the selected user's profile from the database.
 * - Displays the selected user's username.
 * - Allows the current user to send a follow request to the selected user.
 * - Provides navigation functionality to go back to the previous screen.
 *
 * The follow button is shown if the user exists, and a follow request is sent upon clicking the button.
 */

package com.example.unemployedavengers.friendSection;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.unemployedavengers.DAO.IUserDAO;
import com.example.unemployedavengers.databinding.UserProfileBinding;
import com.example.unemployedavengers.implementationDAO.UserDAOImplement;
import com.example.unemployedavengers.models.User;

public class UserProfile extends Fragment {
    private UserProfileBinding binding;
    private IUserDAO userDAO;
    private User currentUser;
    private User viewedUser;

    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater,
                             @Nullable android.view.ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = UserProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userDAO = new UserDAOImplement();

        String selectedUsername = getArguments() != null ? getArguments().getString("selectedUsername") : null;

        if (selectedUsername == null) {
            Toast.makeText(getContext(), "No user selected", Toast.LENGTH_SHORT).show();
            return;
        }

        userDAO.getCurrentUserProfile().addOnSuccessListener(current -> {
            currentUser = current;

            userDAO.getUserByUsername(selectedUsername).addOnSuccessListener(target -> {
                viewedUser = target;

                if (viewedUser != null) {
                    binding.userUsername.setText(viewedUser.getUsername());
                    binding.followButton.setText("Follow");
                    binding.followButton.setVisibility(View.VISIBLE);
                    setupFollowLogic();
                } else {
                    Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.userBackButton.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );
    }

    private void setupFollowLogic() {
        binding.followButton.setOnClickListener(v -> {
            if (currentUser == null || viewedUser == null) {
                Toast.makeText(getContext(), "Error: Missing user info", Toast.LENGTH_SHORT).show();
                return;
            }

            userDAO.requestFollow(currentUser.getUserId(), viewedUser.getUserId())
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(getContext(), "Follow request sent", Toast.LENGTH_SHORT).show();
                        binding.followButton.setText("Requested");
                        binding.followButton.setEnabled(false);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
