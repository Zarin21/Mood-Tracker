/**
 * MainActivity - Core activity class serving as the application's navigation hub.
 *
 * Design Pattern:
 * - Implements Single Activity Architecture using Navigation Component
 * - Follows Material Design guidelines for navigation patterns
 * - Uses View Binding for type-safe view references
 *
 * Key Responsibilities:
 * 1. Navigation Management:
 *    - Coordinates fragment transitions via NavController
 *    - Manages bottom navigation visibility based on auth state
 *    - Handles top app bar navigation actions
 *
 * 2. UI State Management:
 *    - Controls visibility of bottom navigation bar
 *    - Manages app bar (toolbar) display and interactions
 *    - Maintains consistent navigation experience
 *
 * 3. Authentication Flow:
 *    - Hides non-essential UI during auth processes
 *    - Preserves navigation state across auth transitions
 *
 * Technical Implementation:
 * - Uses Navigation Component with single NavHostFragment
 * - Implements dual navigation system (top bar + bottom bar)
 * - Leverages View Binding for layout interaction
 * - Follows Material 3 design principles
 *
 * Outstanding Issues/TODOs:
 * 1. No deep link handling implementation
 * 2. Could benefit from navigation state persistence
 * 3. Limited animation support for transitions
 * 4. No proper handling of back stack in auth flows
 * 5. Could add navigation analytics tracking
 *
 * Dependencies:
 * - AndroidX Navigation Component
 * - Material Components Library
 * - View Binding
 *
 * Lifecycle Notes:
 * - Maintains navigation state across configuration changes
 * - Properly handles activity recreation
 * - Manages fragment back stack appropriately
 *
 * @see NavController
 * @see NavHostFragment
 * @see MaterialToolbar
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
                    itemId == R.id.settingsFragment) {
                navController.navigate(itemId);
                return true;
            }else if (itemId == R.id.profileFragmentt){
                navController.navigate(R.id.profileFragment);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
