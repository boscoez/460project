package com.example.ezchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ezchat.R;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Initializes the splash screen activity, waits briefly, then checks login status to navigate accordingly.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Wait for a short period before navigating
        new Handler().postDelayed(() -> {
            // Check if the user is logged in using FirebaseAuth
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                // If the user is logged in, navigate to MainActivity
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                // If the user is not logged in, navigate to LoginPhoneNumberActivity
                startActivity(new Intent(SplashActivity.this, LoginPhoneNumberActivity.class));
            }
            // Close the splash activity
            finish();
        }, 1000); // Delay for 1 second (1000 ms)
    }
}