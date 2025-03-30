/**
 * Profile Fragment for the Unemployed Avengers Android application.
 *
 * Purpose:
 * - Manages user profile interactions, including username changes, password updates, and avatar selection.
 * - Retrieves and displays user profile information.
 * - Provides secure authentication operations using the UserDAO.
 * - Integrates Firebase Storage for handling profile pictures.
 *
 */

package com.example.unemployedavengers.auth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.unemployedavengers.DAO.IUserDAO;
import com.example.unemployedavengers.R;
import com.example.unemployedavengers.databinding.ProfileBinding;
import com.example.unemployedavengers.implementationDAO.UserDAOImplement;
import com.example.unemployedavengers.models.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class Profile extends Fragment {
    private ProfileBinding binding;
    private IUserDAO userDAO;
    private FirebaseStorage storage;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri imageUri;
    private ActivityResultLauncher<String> permissionLauncher;
    private ImageView imagePreview;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout using view binding
        binding = ProfileBinding.inflate(inflater, container, false);
        setupImagePickerLaunchers();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);  // Default to null if not found
        binding.tvProfileUsername.setText(username);

        storage = FirebaseStorage.getInstance(); // Image Storage
        imagePreview = binding.profilePicturePlaceholder;

        // Image Preview
        userDAO = new UserDAOImplement();
        userDAO.getCurrentUserProfile()
                .addOnSuccessListener(user -> {
                    if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                        Glide.with(requireContext())
                                .load(Uri.parse(user.getAvatar()))
                                .into(imagePreview);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load profile image", Toast.LENGTH_SHORT).show();
                });

        // Navigate back to Dashboard when the Back button is clicked
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_profileFragment_to_dashboardFragment)
        );

        // Set up the change username dialog
        binding.btnChangeUsername.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.change_user_name, null);
            EditText etNewUsername = dialogView.findViewById(R.id.etNewUsername);
            Button btnSubmitUsername = dialogView.findViewById(R.id.btnSubmitUsername);
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();

            btnSubmitUsername.setOnClickListener(following -> {
                String newUsername = etNewUsername.getText().toString().trim();
                userDAO = new UserDAOImplement();

                if (!newUsername.isEmpty()) {
                    userDAO.changeUsername(newUsername)
                            .addOnSuccessListener(exists -> {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("username", newUsername);
                                editor.apply();
                                binding.tvProfileUsername.setText(newUsername);
                                Toast.makeText(getContext(), "User name changed to: " + newUsername, Toast.LENGTH_LONG).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Please enter a valid username", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        });

        // Set up the change password dialog
        binding.btnChangePassword.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.change_password, null);
            EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
            Button btnSubmitPassword = dialogView.findViewById(R.id.btnSubmitPassword);
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();
            dialog.show();

            btnSubmitPassword.setOnClickListener(following -> {
                String newPassword = etNewPassword.getText().toString().trim();
                userDAO = new UserDAOImplement();

                if (!newPassword.isEmpty()) {
                    userDAO.getCurrentUserProfile()
                            .continueWithTask(next -> {
                                if (!next.isSuccessful() || next.getResult() == null) {
                                    throw next.getException() != null ? next.getException() : new Exception("Failed to retrieve user profile");
                                }
                                User user = next.getResult();
                                return userDAO.changePassword(user, newPassword);
                            }).addOnSuccessListener(result -> {
                                Toast.makeText(getContext(), "User password changed", Toast.LENGTH_LONG).show();
                            }).addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Please enter a valid password", Toast.LENGTH_SHORT).show();
                }
            });
        });

        setupImagePickerLaunchers();

        // Change avatar button navigates to login screen (as per current app flow)
        binding.btnChangeAvatar.setOnClickListener(v ->
                imagePickerLauncher.launch(new Intent(MediaStore.ACTION_PICK_IMAGES)));
    }

    private void setupImagePickerLaunchers() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();

                        try {
                            int fileSize = requireContext().getContentResolver()
                                    .openInputStream(imageUri)
                                    .available(); // Get file size in bytes

                            if (fileSize > 2 * 1024 * 1024) { // 2MB limit
                                Toast.makeText(getContext(), "File size must be under 2MB", Toast.LENGTH_SHORT).show();
                                imageUri = null; // Reset imageUri
                            } else {
                                imagePreview.setImageURI(imageUri);
                                StorageReference storageRef = storage.getReference();
                                StorageReference imageRef = storageRef.child("avatars/" + UUID.randomUUID() + ".jpg");

                                imageRef.putFile(imageUri)
                                        .addOnSuccessListener(taskSnapshot ->
                                                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                                    userDAO.updateUserAvatar(uri.toString())
                                                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profile picture updated", Toast.LENGTH_SHORT).show())
                                                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update profile picture", Toast.LENGTH_SHORT).show());
                                                }))
                                        .addOnFailureListener(uploadError -> {
                                            Toast.makeText(getContext(), "Image upload failed: " + uploadError.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error checking file size", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                });

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        imagePickerLauncher.launch(intent);
                    } else {
                        Toast.makeText(getContext(), "Permission required", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Prevent memory leaks by nullifying the binding
        binding = null;
    }
}
