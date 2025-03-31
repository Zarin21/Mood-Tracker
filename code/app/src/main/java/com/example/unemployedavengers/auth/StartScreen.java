/**
 * StartScreen Fragment for the Unemployed Avengers Android application.
 *
 * Purpose:
 * - This fragment serves as the entry point of the application, allowing users to either sign up or log in.
 * - The fragment provides navigation options to direct users to the sign-up screen or the login screen.
 *
 * Design Pattern:
 * - The fragment follows a simple View-based architecture, using Android's Navigation component to handle screen transitions.
 *
 * Outstanding Issues:
 * - There is no validation or error handling for potential navigation failures, such as network issues or navigation actions failing.
 * - A loading indicator could be useful during screen transitions to improve user experience, especially when navigating between multiple fragments.
 * - The buttons are statically linked to specific fragments, which may limit flexibility for future features, like adding guest access or other authentication methods.
 */


package com.example.unemployedavengers.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.unemployedavengers.R;
import com.example.unemployedavengers.databinding.StartScreenBinding;

public class StartScreen extends Fragment {

    private StartScreenBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the view using view binding
        binding = StartScreenBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Navigate to the Sign-Up screen when "Start" button is clicked
        binding.btnStart.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_homeFragment_to_signUpFragment)
        );

        // Navigate to the Login screen when "Login" text is clicked
        binding.tvStartLogin.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_homeFragment_to_loginFragment)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Prevent memory leaks by nullifying the binding
        binding = null;
    }
}
