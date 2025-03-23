/*
 * Login Fragment for the Unemployed Avengers Android application.
 *
 * This file handles user authentication, including:
 * - Inflating the login layout using view binding.
 * - Validating user input for login credentials.
 * - Authenticating the user via a DAO.
 * - Storing user credentials in SharedPreferences.
 * - Supporting offline login using cached credentials.
 * - Navigating to the Dashboard on successful login.
 * - Providing navigation options to Home and Password Reset screens.
 */

package com.example.unemployedavengers.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Login extends BaseFragment {

    private static final String TAG = "Login";
    private LogInBinding binding;
    private IUserDAO userDAO;
    private boolean isOnline = false;

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

        // Configure Firestore for offline persistence
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.setFirestoreSettings(
                new FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(true)
                        .build()
        );

        // Check network connectivity
        isOnline = isNetworkAvailable();
        updateConnectionStatus();

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

            // Re-check connectivity status before attempting login
            isOnline = isNetworkAvailable();
            updateConnectionStatus();

            if (isOnline) {
                // Online login
                loginOnline(username, password, v);
            } else {
                // Offline login
                loginOffline(username, password, v);
            }
        });

        binding.tvForgotPassword.setOnClickListener(v -> {
            if (!isOnline) {
                showCustomToast("Cannot reset password while offline. Please connect to the internet.");
                return;
            }
            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_passwordReset1Fragment);
        });
    }

    /**
     * Perform online login using Firebase authentication
     */
    private void loginOnline(String username, String password, View v) {
        userDAO.signInUser(username, password)
                .addOnSuccessListener(aVoid -> {
                    if (binding == null || !isValidFragment()) return;

                    Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Username before passing: " + username);

                    // Save login credentials for offline use
                    saveCredentials(username, password);

                    // Retrieve additional user data
                    userDAO.getUserByUsername(username)
                            .addOnSuccessListener(user -> {
                                if (binding == null || !isValidFragment()) return;

                                if (user != null) {
                                    Log.d(TAG, "User ID: " + user.getUserId());
                                    String userID = user.getUserId();

                                    // Save user ID and mark as logged in
                                    SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("userID", userID);
                                    editor.putBoolean("is_logged_in", true);
                                    editor.putLong("last_login_time", System.currentTimeMillis());
                                    Log.d("SharedPreferences", "User ID saved: " + userID);
                                    editor.apply();

                                    // Navigate to dashboard
                                    navigateToDashboard(v);
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (binding == null || !isValidFragment()) return;
                                Log.e(TAG, "Error retrieving user data: " + e.getMessage());
                                showCustomToast("Error retrieving user data");
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

                    showCustomToast(errorMessage);
                });
    }

    /**
     * Perform offline login using cached credentials
     */
    private void loginOffline(String username, String password, View v) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);

        // Get saved credentials
        String savedUsername = sharedPreferences.getString("username", "");
        String savedPasswordHash = sharedPreferences.getString("password_hash", "");

        // Check if we have saved credentials
        if (TextUtils.isEmpty(savedUsername) || TextUtils.isEmpty(savedPasswordHash)) {
            showCustomToast("No saved credentials found. Please connect to the internet for your first login.");
            return;
        }

        // Hash the entered password for comparison
        String currentPasswordHash = hashPassword(password);

        // Verify credentials
        if (username.equals(savedUsername) && currentPasswordHash.equals(savedPasswordHash)) {
            // Successful offline login
            Toast.makeText(getContext(), "Offline login successful!", Toast.LENGTH_SHORT).show();

            // Mark as logged in
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("is_logged_in", true);
            editor.putLong("last_login_time", System.currentTimeMillis());
            editor.apply();

            // Navigate to dashboard
            navigateToDashboard(v);
        } else {
            showCustomToast("Offline authentication failed. Please check your credentials or connect to the internet.");
        }
    }

    /**
     * Save user credentials for offline authentication
     */
    private void saveCredentials(String username, String password) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Store username
        editor.putString("username", username);
        Log.d("SharedPreferences", "Username saved: " + username);

        // Store hashed password (more secure than plain text)
        String passwordHash = hashPassword(password);
        editor.putString("password_hash", passwordHash);

        editor.apply();
    }

    /**
     * Create a hash of the password for secure storage
     */
    private String hashPassword(String password) {
        try {
            // Create SHA-256 hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());

            // Convert to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error hashing password", e);
            // Fallback if hashing fails (not ideal but better than nothing)
            return password + "_salt_for_security";
        }
    }

    /**
     * Navigate to the dashboard screen
     */
    private void navigateToDashboard(View v) {
        try {
            NavController navController = Navigation.findNavController(v);
            // Check if we're already at the dashboard
            if (navController.getCurrentDestination().getId() != R.id.dashboardFragment) {
                navController.navigate(R.id.action_loginFragment_to_dashboardFragment);
            }
        } catch (Exception e) {
            Log.e(TAG, "Navigation error: " + e.getMessage(), e);
        }
    }

    /**
     * Check if the device has an active network connection
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;

        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Update UI to indicate current connection status
     */
    private void updateConnectionStatus() {
        if (binding == null) return;


        if (!isOnline) {
            binding.tvConnectionStatus.setVisibility(View.VISIBLE);
            binding.tvConnectionStatus.setText("Offline Mode - Limited functionality available");
        } else {
            binding.tvConnectionStatus.setVisibility(View.GONE);
        }
    }

    /**
     * Show a custom toast message
     */
    private void showCustomToast(String message) {
        if (getContext() == null) return;

        // Inflate custom toast layout
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, null);
        TextView toastText = layout.findViewById(R.id.toastText);
        toastText.setText(message);

        Toast toast = new Toast(getContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Avoid memory leaks by nullifying the binding
        binding = null;
    }
}