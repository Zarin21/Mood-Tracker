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
import com.example.unemployedavengers.databinding.SignUpBinding;
import com.example.unemployedavengers.implementationDAO.UserDAOImplement;
import com.google.android.gms.tasks.Task;

public class SignUp extends Fragment {

    private SignUpBinding binding;
    private IUserDAO userDAO;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SignUpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userDAO = new UserDAOImplement();


        binding.btnBack.setOnClickListener(v -> {
            Navigation.findNavController(v)
                    .navigate(R.id.action_signUpFragment_to_homeFragment);
        });

        binding.btnSignup.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(getContext(), "All fields are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Passwords do not match!", Toast.LENGTH_SHORT).show();
                return;
            }

            Task<Void> signUpTask = userDAO.signUpUser(username, password);
            signUpTask
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Sign up successful!", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(v)
                                .navigate(R.id.action_signUpFragment_to_loginFragment);
                    })
                    .addOnFailureListener(e -> {
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_toast, null);
                        TextView toastText = layout.findViewById(R.id.toastText);

                        // work around
                        String errorMessage = e.getMessage();
                        if (errorMessage != null && errorMessage.contains("email address is")) {
                            errorMessage = "Username already taken. Please choose a different one.";
                        }

                        toastText.setText("Sign up failed: " + errorMessage);

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
        binding = null;
    }
}