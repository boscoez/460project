package com.example.ezchat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ezchat.utils.FirebaseUtil;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
<<<<<<< HEAD
=======
    /**
     * Initializes the splash screen activity, waits briefly, then checks login status to navigate accordingly.
     *
     * @param savedInstanceState The saved instance state for the activity.
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(() -> {
            if(FirebaseUtil.isLoggedIn()){
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }else {
                startActivity(new Intent(SplashActivity.this, LoginPhoneNumberActivity.class));
            }
            finish();
        }, 1000);
    }
}