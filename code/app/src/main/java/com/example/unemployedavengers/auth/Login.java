package com.example.unemployedavengers.auth;

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
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.unemployedavengers.DAO.IUserDAO;
import com.example.unemployedavengers.R;
import com.example.unemployedavengers.databinding.LogInBinding;
import com.example.unemployedavengers.implementationDAO.UserDAOImplement;

public class Login extends Fragment {

    private LogInBinding binding;
    private IUserDAO userDAO;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = LogInBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userDAO = new UserDAOImplement();

        binding.btnBack.setOnClickListener(v -> {
            Navigation.findNavController(v)
                    .navigate(R.id.action_loginFragment_to_homeFragment);
        });

        // Set up the click listener for the Login button
        binding.btnLogin.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            // Validation
            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            userDAO.signInUser(username, password)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_dashboardFragment);
                    })
                    .addOnFailureListener(e -> {
                        String errorMessage = e.getMessage(); // default message

                        if (errorMessage != null && errorMessage.contains("no user record")) {
                            errorMessage = "No user record, please sign up first or check your user name again.";
                        } else if (errorMessage != null && errorMessage.contains("does not have a password")) {
                            errorMessage = "The password is invalid, check your password or click on \"Forget password\"";
                        }

                        // Inflate custom toast layout to display the full error message
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
            Navigation.findNavController(v)
                    .navigate(R.id.action_loginFragment_to_passwordReset1Fragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Prevent memory leaks by nulling out the binding
        binding = null;
    }
}
