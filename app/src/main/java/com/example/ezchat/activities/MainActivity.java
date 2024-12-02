package com.example.ezchat.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.ezchat.R;
import com.example.ezchat.databinding.ActivityMainBinding;
import com.example.ezchat.fragments.CalendarFragment;
import com.example.ezchat.fragments.ChatsFragment;
import com.example.ezchat.fragments.ProfileFragment;
import com.example.ezchat.utilities.Constants;
import com.example.ezchat.utilities.PreferenceManager;
import com.example.ezchat.utilities.Utilities;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize PreferenceManager
        preferenceManager = PreferenceManager.getInstance(this);

        // Log user details for debugging
        logUserDetails();

        // Set up the BottomNavigationView and load the default fragment
        setupBottomNavigation();

        // Handle search button click
        binding.mainSearchBtn.setOnClickListener(v -> {
            Log.d(Constants.LOG_TAG_MAIN_ACTIVITY, "Search button clicked.");
            Utilities.navigateToActivity(this, SearchActivity.class, null);
        });
    }

    /**
     * Logs the current user's details for debugging.
     */
    private void logUserDetails() {
        String phone = preferenceManager.get(Constants.FIELD_PHONE, "N/A");
        String username = preferenceManager.get(Constants.FIELD_USERNAME, "N/A");
        String email = preferenceManager.get(Constants.FIELD_EMAIL, "N/A");

        Log.d(Constants.LOG_TAG_MAIN_ACTIVITY, "User details:");
        Log.d(Constants.LOG_TAG_MAIN_ACTIVITY, "Phone: " + phone);
        Log.d(Constants.LOG_TAG_MAIN_ACTIVITY, "Username: " + username);
        Log.d(Constants.LOG_TAG_MAIN_ACTIVITY, "Email: " + email);
    }

    /**
     * Sets up the BottomNavigationView to navigate between fragments.
     */
    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_profile) {
                Log.d(Constants.LOG_TAG_MAIN_ACTIVITY, "Navigating to ProfileFragment.");
                selectedFragment = new ProfileFragment();
            } else if (item.getItemId() == R.id.nav_chats) {
                Log.d(Constants.LOG_TAG_MAIN_ACTIVITY, "Navigating to ChatsFragment.");
                selectedFragment = new ChatsFragment();
            } else if (item.getItemId() == R.id.nav_calendar) {
                Log.d(Constants.LOG_TAG_MAIN_ACTIVITY, "Navigating to CalendarFragment.");
                selectedFragment = new CalendarFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            } else {
                return false;
            }
        });

        // Load the default fragment (ChatsFragment)
        binding.bottomNavigation.setSelectedItemId(R.id.nav_chats);
    }

    /**
     * Loads the specified fragment into the FrameLayout.
     *
     * @param fragment The fragment to load.
     */
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_frame_layout, fragment)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}