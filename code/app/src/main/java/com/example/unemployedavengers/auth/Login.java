/*
 * Login Fragment for the Unemployed Avengers Android application.
 *
 * This file handles user authentication, including:
 * - Inflating the login layout using view binding.
 * - Validating user input for login credentials.
 * - Authenticating the user via a DAO.
 * - Storing user credentials in SharedPreferences.
 * - Navigating to the Dashboard on successful login.
 * - Providing navigation options to Home and Password Reset screens.
 */

package com.example.unemployedavengers.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.unemployedavengers.BaseFragment;
import com.example.unemployedavengers.DAO.IUserDAO;
import com.example.unemployedavengers.R;
import com.example.unemployedavengers.databinding.LogInBinding;
import com.example.unemployedavengers.implementationDAO.UserDAOImplement;

public class Login extends BaseFragment {

    private LogInBinding binding;
    private IUserDAO userDAO;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the view using view binding
        binding = LogInBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the User Data Access Object
        userDAO = new UserDAOImplement();

        binding.btnBack.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_homeFragment);
        });

        // Set up the click listener for the Login button
        binding.btnLogin.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            // Validate input fields
            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Attempt to sign in the user
            userDAO.signInUser(username, password)
                    .addOnSuccessListener(aVoid -> {
                        if (binding == null || !isValidFragment()) return;

                        Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                        Log.d("Login", "Username before passing: " + username);

                        // Save username in SharedPreferences
                        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("username", username);
                        editor.apply();
                        Log.d("SharedPreferences", "Username saved: " + username);

                        // Retrieve additional user data
                        userDAO.getUserByUsername(username)
                                .addOnSuccessListener(user -> {
                                    if (binding == null || !isValidFragment()) return;

                                    if (user != null) {
                                        Log.d("Login", "User ID: " + user.getUserId());
                                        String userID = user.getUserId();
                                        editor.putString("userID", userID);
                                        Log.d("SharedPreferences", "User ID saved: " + userID);
                                        editor.apply();

                                        // Fixed navigation code
                                        try {
                                            NavController navController = Navigation.findNavController(v);
                                            // Check if we're already at the dashboard
                                            if (navController.getCurrentDestination().getId() != R.id.dashboardFragment) {
                                                navController.navigate(R.id.action_loginFragment_to_dashboardFragment);
                                            }
                                        } catch (Exception e) {
                                            Log.e("Login", "Navigation error: " + e.getMessage(), e);
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (binding == null || !isValidFragment()) return;
                                    Log.e("Login", "Error retrieving user data: " + e.getMessage());
                                    Toast.makeText(getContext(), "Error retrieving user data", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        if (binding == null || !isValidFragment()) return;

                        String errorMessage = e.getMessage();

                        // Customize error messages based on failure reason
                        if (errorMessage != null && errorMessage.contains("no user record")) {
                            errorMessage = "No user record, please sign up first or check your user name again.";
                        } else if (errorMessage != null && errorMessage.contains("does not have a password")) {
                            errorMessage = "The password is invalid, check your password or click on \"Forget password\"";
                        }

                        // Inflate custom toast layout to show the error message
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_toast, null);
                        TextView toastText = layout.findViewById(R.id.toastText);
                        toastText.setText(errorMessage);

                        Toast toast = new Toast(getContext());
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();
                    });
        });

        binding.tvForgotPassword.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_passwordReset1Fragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Avoid memory leaks by nullifying the binding
        binding = null;
    }
}