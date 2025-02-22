package com.example.unemployedavengers.auth;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.unemployedavengers.DAO.IUserDAO;
import com.example.unemployedavengers.R;
import com.example.unemployedavengers.databinding.ProfileBinding;
import com.example.unemployedavengers.implementationDAO.UserDAOImplement;
import com.example.unemployedavengers.models.User;

public class Profile extends Fragment {
    private ProfileBinding binding;
    private IUserDAO userDAO;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_profileFragment_to_dashboardFragment)
        );

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
                            .continueWithTask(next ->{
                                if (!next.isSuccessful() || next.getResult() == null) {
                                    throw next.getException() != null ? next.getException() : new Exception("Failed to retrieve user profile");
                                }
                                User user = next.getResult();
                                return userDAO.changePassword(user,newPassword);
                            }).addOnSuccessListener(result -> {
                                Toast.makeText(getContext(), "User password changed", Toast.LENGTH_LONG).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Please enter a valid password", Toast.LENGTH_SHORT).show();
                }
            });


        });

        binding.btnChangeAvatar.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_homeFragment_to_loginFragment)
        );


    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Prevent memory leaks
        binding = null;
    }


}
