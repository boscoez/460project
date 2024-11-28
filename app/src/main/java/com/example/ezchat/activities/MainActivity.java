package com.example.ezchat.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ezchat.R;
import com.example.ezchat.databinding.ActivityMainBinding;
import com.example.ezchat.fragments.ChatRoomsFragment;
import com.example.ezchat.fragments.ProfileFragment;
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utilities.FirebaseUtil;
import com.example.ezchat.utilities.PreferenceManager;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Main activity of the application that serves as the entry point after login.
 * It contains navigation to chat rooms and profile sections, and manages Firebase Cloud Messaging (FCM) tokens.
 */
public class MainActivity extends AppCompatActivity {

    private SharedPreferences preferences; // Shared preferences to store user-related data
    private ActivityMainBinding binding;  // View binding for activity_main.xml
    private ChatRoomsFragment chatRoomFragment; // Fragment for chat rooms
    private ProfileFragment profileFragment;    // Fragment for user profile

    /**
     * Called when the activity is first created.
     * Initializes UI components, sets up navigation, and retrieves the FCM token.
     *
     * @param savedInstanceState Previously saved state (if any).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize SharedPreferences
        preferences = getSharedPreferences(PreferenceManager.KEY_PREFERENCE_NAME, MODE_PRIVATE);

        // Initialize fragments
        chatRoomFragment = new ChatRoomsFragment();
        profileFragment = new ProfileFragment();

        // Set up search button click listener
        binding.mainSearchBtn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SearchUserActivity.class));
        });

        // Set up bottom navigation item selection listener
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_chat) {
                // Navigate to ChatRoomsFragment
                getSupportFragmentManager().beginTransaction()
                        .replace(binding.mainFrameLayout.getId(), chatRoomFragment).commit();
            } else if (item.getItemId() == R.id.menu_profile) {
                // Navigate to ProfileFragment
                getSupportFragmentManager().beginTransaction()
                        .replace(binding.mainFrameLayout.getId(), profileFragment).commit();
            }
            return true;
        });

        // Set default navigation to chat rooms
        binding.bottomNavigation.setSelectedItemId(R.id.menu_chat);

        // Fetch FCM token
        getFCMToken();
    }

    /**
     * Retrieves the Firebase Cloud Messaging (FCM) token for push notifications
     * and saves it to Firebase and local SharedPreferences.
     */
    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                saveTokenToPreferences(token); // Save token locally
                FirebaseUtil.currentUserDetails().update(UserModel.FIELD_FCM_TOKEN, token); // Update token in Firebase
            }
        });
    }

    /**
     * Saves the FCM token to SharedPreferences for local storage.
     *
     * @param token The FCM token to be saved.
     */
    private void saveTokenToPreferences(String token) {
        preferences.edit().putString(UserModel.FIELD_FCM_TOKEN, token).apply();
    }

    /**
     * Cleans up resources to prevent memory leaks.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null; // Avoid memory leaks by releasing binding
    }
}