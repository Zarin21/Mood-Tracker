/**
 * UserProfile.java
 *
 * This fragment represents the user profile screen within the application.
 * It is responsible for displaying a selected user's profile details, including their username,
 * profile picture, and follow status. Users can send follow requests from this screen.
 *
 * Features:
 * - Fetches the logged-in user's profile and the selected user's profile.
 * - Displays the selected user's profile picture and username.
 * - Handles follow requests and updates the UI accordingly.
 *
 * Outstanding Issues:
 * - Error handling: If fetching user data fails, UI feedback could be improved.
 * - UI performance: Profile picture loading might need optimization for better performance.
 * - Follow status: Consider implementing real-time updates instead of fetching on view load.
 */

package com.example.unemployedavengers.friendSection;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.unemployedavengers.DAO.IUserDAO;
import com.example.unemployedavengers.R;
import com.example.unemployedavengers.databinding.UserProfileBinding;
import com.example.unemployedavengers.implementationDAO.UserDAOImplement;
import com.example.unemployedavengers.models.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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

        ImageView userImage = (ImageView) view.findViewById(R.id.user_profile);

        userDAO.getCurrentUserProfile().addOnSuccessListener(current -> {
            currentUser = current;

            userDAO.getUserByUsername(selectedUsername).addOnSuccessListener(target -> {
                viewedUser = target;

                if (viewedUser != null) {
                    binding.userUsername.setText(viewedUser.getUsername());

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference userDocRef = db.collection("users").document(viewedUser.getUserId());
                    userDocRef.get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String profilePicUrl = documentSnapshot.getString("avatar");
                            if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                                Glide.with(requireContext()).load(profilePicUrl).into(userImage);
                            }
                        }
                    }).addOnFailureListener(e -> {
                        Log.e("CommentAdapter", "Failed to load profile picture", e);
                    });


                    userDAO.getFollowStatus(currentUser.getUserId(), viewedUser.getUserId())
                            .addOnSuccessListener(status -> {
                                switch (status) {
                                    case "following":
                                        binding.followButton.setText("Following");
                                        binding.followButton.setEnabled(false);
                                        break;
                                    case "requested":
                                        binding.followButton.setText("Requested");
                                        binding.followButton.setEnabled(false);
                                        break;
                                    case "none":
                                        binding.followButton.setText("Follow");
                                        binding.followButton.setVisibility(View.VISIBLE);
                                        setupFollowLogic();
                                        break;
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error checking follow status", Toast.LENGTH_SHORT).show();
                            });

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
