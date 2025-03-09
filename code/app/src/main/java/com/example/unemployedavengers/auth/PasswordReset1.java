/*
 * PasswordReset1 Fragment for the Unemployed Avengers Android application.
 *
 * This file handles the first step of the password reset process:
 * - Inflates the password reset layout using view binding.
 * - Provides navigation back to the login screen.
 * - Verifies if a user exists via the UserDAO.
 * - Navigates to the next password reset step if the user exists.
 * - Displays error messages using Toasts.
 */

package com.example.unemployedavengers.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.unemployedavengers.DAO.IUserDAO;
import com.example.unemployedavengers.R;
import com.example.unemployedavengers.databinding.PasswordReset1Binding;
import com.example.unemployedavengers.implementationDAO.UserDAOImplement;

public class PasswordReset1 extends Fragment {
    private PasswordReset1Binding binding;
    private IUserDAO userDAO;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the view using view binding
        binding = PasswordReset1Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Navigate back to Login fragment when back button is clicked
        binding.btnPasswordResetBack.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_passwordReset1Fragment_to_loginFragment)
        );

        // Handle the "Next" button click for password reset process
        binding.btnNext.setOnClickListener(v -> {
            String userName = binding.etUsername.getText().toString().trim();
            userDAO = new UserDAOImplement();

            // Check if the user exists
            userDAO.checkUserExists(userName)
                    .addOnSuccessListener(exists -> {
                        if (exists) {
                            // Navigate to the next password reset step with the username in a bundle
                            Bundle bundle = new Bundle();
                            bundle.putString("userName", userName);
                            Navigation.findNavController(v)
                                    .navigate(R.id.action_passwordReset1Fragment_to_passwordReset2Fragment, bundle);
                        } else {
                            Toast.makeText(getContext(), "User does not exist, please check or sign up first", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Prevent memory leaks by nullifying the binding
        binding = null;
    }
}
