package com.example.ezchat.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ezchat.databinding.ActivityLoginPhoneNumberBinding;
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utilities.AndroidUtil;
import com.example.ezchat.utilities.PreferenceManager;

/**
 * LoginPhoneNumberActivity handles the collection of the user's phone number.
 * It passes the phone number to LoginOtpActivity for OTP-based authentication.
 */
public class LoginPhoneNumberActivity extends AppCompatActivity {

    private ActivityLoginPhoneNumberBinding binding; // View Binding
    private PreferenceManager preferenceManager; // Shared preferences manager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivityLoginPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Shared Preference Manager
        preferenceManager = PreferenceManager.getInstance(getApplicationContext());

        // Set default country to US
        binding.loginCountrycode.setDefaultCountryUsingNameCode("US");
        binding.loginCountrycode.setCountryForNameCode("US"); // Ensure it reflects correctly

        // Set up "Send OTP" button click listener
        binding.sendOtpBtn.setOnClickListener(v -> {
            // Retrieve the phone number and country code
            String countryCode = binding.loginCountrycode.getSelectedCountryCodeWithPlus();
            String mobileNumber = binding.loginMobileNumber.getText().toString().trim();

            if (mobileNumber.isEmpty() || mobileNumber.length() < 10) {
                AndroidUtil.showToast(getApplicationContext(), "Please enter a valid mobile number");
                return;
            }

            // Create the full phone number
            String phoneNumber = countryCode + mobileNumber;

            // Save the phone number in SharedPreferences
            preferenceManager.putString(UserModel.FIELD_PHONE, phoneNumber);

            // Navigate to LoginOtpActivity
            Intent intent = new Intent(LoginPhoneNumberActivity.this, LoginOtpActivity.class);
            intent.putExtra(UserModel.FIELD_PHONE, phoneNumber);
            startActivity(intent);
        });
    }
}