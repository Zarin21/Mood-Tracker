/*
 * MainActivity - Entry point and navigation handler for the Unemployed Avengers Android application.
 *
 * This class sets up:
 * - View binding for UI management.
 * - Navigation component to manage fragment transitions.
 * - Bottom navigation visibility and interaction based on user authentication state.
 *
 * Features:
 * - Uses Navigation to manage fragment navigation.
 * - Controls the visibility of the BottomNavigationView based on active fragments.
 *
 */

package com.example.unemployedavengers;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.unemployedavengers.databinding.ActivityMainBinding;
import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavController navController;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up the Navigation Component with the NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        }

        toolbar = findViewById(R.id.top_navigation);
        setSupportActionBar(toolbar);

        // Handle Profile icon click (left navigation icon)
        toolbar.setNavigationOnClickListener(v -> {
            if (navController != null) {
                navController.navigate(R.id.profileFragment);
            }
        });

        // Handle menu item clicks in the top navigation bar (right icons)
        toolbar.setOnMenuItemClickListener(item -> {
            return onOptionsItemSelected(item);
        });

        // Handle bottom navigation item selection
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() != item.getItemId()) {
                navController.navigate(item.getItemId());
            }
            return true;
        });

        // Manage the visibility of the BottomNavigationView and Toolbar based on the active fragment
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.loginFragment ||
                    destination.getId() == R.id.signUpFragment ||
                    destination.getId() == R.id.passwordReset1Fragment ||
                    destination.getId() == R.id.passwordReset2Fragment ||
                    destination.getId() == R.id.homeFragment) {
                // Hide bottom navigation and toolbar in authentication-related fragments
                toolbar.setVisibility(View.GONE);
                binding.bottomNavigation.setVisibility(View.GONE);
            } else {
                // Show bottom navigation and toolbar in other fragments
                toolbar.setVisibility(View.VISIBLE);
                binding.bottomNavigation.setVisibility(View.VISIBLE);
            }
        });
    }

    // Inflates the top navigation menu only for the buttons on the right
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_navigation_menu, menu);
        return true;
    }

    // Directs the buttons to its corresponding fragments
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (navController != null) {
            if (itemId == R.id.notificationsFragment ||
                    itemId == R.id.settingsFragment||itemId==R.id.profileFragment) {
                navController.navigate(itemId);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
