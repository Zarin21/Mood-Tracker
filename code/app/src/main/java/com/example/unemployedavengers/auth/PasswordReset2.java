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
import com.example.unemployedavengers.databinding.PasswordReset1Binding;
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
        binding = PasswordReset2Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            userName = args.getString("userName");
        }
        binding.btnPasswordReset2Back.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_passwordReset2Fragment_to_passwordReset1Fragment)
        );

        binding.btnReset.setOnClickListener(v -> {
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
                userDAO = new UserDAOImplement();
                Task<Void> resetTask = userDAO.resetPassword(userName, password);
                resetTask.addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Reset Successful!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(v)
                            .navigate(R.id.action_passwordReset2Fragment_to_loginFragment);
            }).addOnFailureListener(e -> {
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
        // Prevent memory leaks
        binding = null;
    }


}

