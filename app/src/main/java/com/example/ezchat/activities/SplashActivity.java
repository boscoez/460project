package com.example.ezchat.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ezchat.R;
import com.example.ezchat.utils.FirebaseUtil;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    /**
     * Initializes the splash screen activity, waits briefly, then checks login status to navigate accordingly.
     * @param savedInstanceState The saved instance state for the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(() -> {
            if (FirebaseUtil.isLoggedIn()) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginPhoneNumberActivity.class));
            }
            finish();
        }, 1000);
    }
}