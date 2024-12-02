package com.example.ezchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ezchat.R;
import com.example.ezchat.utilities.Constants;
import com.example.ezchat.utilities.PreferenceManager;

/**
 * Initializes the splash screen activity, waits briefly, then checks login status to navigate accordingly.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Log.d(Constants.SPLASH_LOG_TAG, "Splash screen loaded.");

        // Initialize PreferenceManager
        PreferenceManager preferenceManager = PreferenceManager.getInstance(this);

        // Check login status after a delay
        new Handler().postDelayed(() -> {
            boolean isLoggedIn = preferenceManager.get(Constants.PREF_KEY_IS_LOGGED_IN, false);
            if (isLoggedIn) {
                Log.d(Constants.SPLASH_LOG_TAG, "User is logged in. Navigating to MainActivity.");
                navigateToMainActivity();
            } else {
                Log.d(Constants.SPLASH_LOG_TAG, "User not logged in. Navigating to LoginPhoneNumberActivity.");
                navigateToLoginActivity();
            }
        }, Constants.SPLASH_DELAY);
    }

    /**
     * Navigates to the MainActivity.
     */
    private void navigateToMainActivity() {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish(); // Prevent going back to SplashActivity
    }

    /**
     * Navigates to the LoginPhoneNumberActivity.
     */
    private void navigateToLoginActivity() {
        startActivity(new Intent(SplashActivity.this, LoginPhoneNumberActivity.class));
        finish(); // Prevent going back to SplashActivity
    }
}