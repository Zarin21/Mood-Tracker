/**
 * PasswordReset2 Fragment for the Unemployed Avengers Android application.
 *
 * Purpose:
 * - Manages the second step of the password reset process.
 * - Retrieves the username from the previous fragment.
 * - Validates and confirms the new password.
 * - Updates the user's password securely via the UserDAO.
 * - Navigates back to the login screen upon successful reset.
 * - Displays error messages with a custom Toast notification.
 */

package com.example.unemployedavengers.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.unemployedavengers.DAO.IUserDAO;
import com.example.unemployedavengers.R;
import com.example.unemployedavengers.databinding.PasswordReset2Binding;
import com.example.unemployedavengers.implementationDAO.UserDAOImplement;
import com.google.android.gms.tasks.Task;

public class PasswordReset2 extends Fragment {
    private PasswordReset2Binding binding;
    private IUserDAO userDAO;
    private String userName;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout using view binding
        binding = PasswordReset2Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve the username passed from the previous fragment
        Bundle args = getArguments();
        if (args != null) {
            userName = args.getString("userName");
        }

        // Navigate back to the first password reset screen
        binding.btnPasswordReset2Back.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_passwordReset2Fragment_to_passwordReset1Fragment)
        );

        // Handle the password reset button click
        binding.btnReset.setOnClickListener(v -> {
            // Get and validate the password inputs
            String password = binding.etPassword.getText().toString().trim();
            String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(getContext(), "All fields are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Passwords do not match!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Reset the password using the DAO
            userDAO = new UserDAOImplement();
            Task<Void> resetTask = userDAO.resetPassword(userName, password);
            resetTask.addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Reset Successful!", Toast.LENGTH_SHORT).show();
                // Navigate to the login screen after successful reset
                Navigation.findNavController(v)
                        .navigate(R.id.action_passwordReset2Fragment_to_loginFragment);
            }).addOnFailureListener(e -> {
                // Inflate custom toast layout to display error message
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.custom_toast, null);
                TextView toastText = layout.findViewById(R.id.toastText);
                toastText.setText(e.getMessage());

                Toast toast = new Toast(getContext());
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(layout);
                toast.show();
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
