/**
 * PasswordReset1 Fragment for the Unemployed Avengers Android application.
 *
 * Purpose:
 * - Handles the first step of the password reset process.
 * - Inflates the password reset layout using view binding for efficient view management.
 * - Allows users to navigate back to the login screen.
 * - Verifies if a user exists in the database using the UserDAO interface.
 * - If the user exists, navigates to the next step in the password reset process.
 * - Provides appropriate feedback via Toast notifications on success or failure.

 * Features:
 * - Validates the username entered by the user and checks its existence in the system.
 * - Handles user navigation to the second step of password reset upon successful verification.
 * - Displays error messages if the user does not exist or an error occurs.

 * Design Pattern:
 * - Follows the fragment-based structure for managing the password reset process.
 * - Utilizes View Binding for simplifying UI interactions and avoiding null pointer issues.
 * - Implements navigation between fragments based on user actions.
 *
 * Outstanding Issues:
 * - There is no handling for cases where the user enters an incorrect or invalid username format.
 * - Password reset flow lacks security considerations such as limiting the number of attempts or CAPTCHA implementation.
 * - Could benefit from additional user input validation (e.g., checking for empty fields or username format).
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
