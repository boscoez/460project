package com.example.ezchat.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ezchat.R;
import com.example.ezchat.databinding.ActivityMainBinding;
import com.example.ezchat.fragments.CalendarFragment;
import com.example.ezchat.fragments.ChatCollectionFragment;
import com.example.ezchat.fragments.ProfileFragment;
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utilities.PreferenceManager;
import com.example.ezchat.utilities.Constants;
import com.example.ezchat.utilities.Utilities;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Main activity of the application that serves as the entry point after login.
 * It contains navigation to chat, profile, and calendar sections, and manages Firebase Cloud Messaging (FCM) tokens.
 */
public class MainActivity extends AppCompatActivity {

    private SharedPreferences preferences; // Shared preferences to store user-related data
    private ActivityMainBinding binding;  // View binding for activity_main.xml
    private ChatCollectionFragment chatFragment; // Fragment for chat
    private ProfileFragment profileFragment;    // Fragment for user profile
    private CalendarFragment calendarFragment;

    private PreferenceManager preferenceManager; // Shared preferences manager

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
        preferenceManager = PreferenceManager.getInstance(getApplicationContext());
        preferences = getSharedPreferences(PreferenceManager.KEY_PREFERENCE_NAME, MODE_PRIVATE);

        // Initialize fragments
        chatFragment = new ChatCollectionFragment();
        profileFragment = new ProfileFragment();
        calendarFragment = new CalendarFragment();

        // Set up search button click listener
        binding.mainSearchBtn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SearchUserActivity.class));
        });

        // Set default navigation to calendar
        binding.bottomNavigation.setSelectedItemId(R.id.menu_calendar);

        // Set up bottom navigation item selection listener
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_chat) {
                // Navigate to ChatsFragment
                getSupportFragmentManager().beginTransaction()
                        .replace(binding.mainFrameLayout.getId(), chatFragment).commit();
            } else if (item.getItemId() == R.id.menu_profile) {
                // Navigate to ProfileFragment
                getSupportFragmentManager().beginTransaction()
                        .replace(binding.mainFrameLayout.getId(), profileFragment).commit();
            } else if (item.getItemId() == R.id.menu_calendar) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, calendarFragment).commit();
            }
            return true;
        });

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
                updateFCMTokenInDatabase(token); // Update token in Firestore
            }
        });
    }

    /**
     * Saves the FCM token to SharedPreferences for local storage.
     *
     * @param token The FCM token to be saved.
     */
    private void saveTokenToPreferences(String token) {
        preferenceManager.putString(Constants.FIELD_FCM_TOKEN, token); // Save the token using PreferenceManager
    }

    /**
     * Updates the FCM token in Firestore for the current user.
     * @param token The FCM token to be saved in Firestore.
     */
    private void updateFCMTokenInDatabase(String token) {
        // Get the current user's phone number from SharedPreferences
        String phone = preferenceManager.getString(Constants.FIELD_PHONE);

        if (phone != null) {
            // Update the FCM token in Firestore for the user using their phone number as document ID
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection(Constants.USER_COLLECTION)
                    .document(phone) // Use phone number as document ID
                    .update(Constants.FIELD_FCM_TOKEN, token)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Save the updated token locally in SharedPreferences
                            preferenceManager.putString(Constants.FIELD_FCM_TOKEN, token);
                            Utilities.showToast(this, "Welcome back, " + preferenceManager.getString(Constants.PREF_KEY_USERNAME) + "!", Utilities.ToastType.SUCCESS);
                        }
                    });
        }
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