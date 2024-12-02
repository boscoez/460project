package com.example.ezchat.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ezchat.R;
import com.example.ezchat.databinding.ActivityLoginPhoneNumberBinding;
import com.example.ezchat.utilities.PreferenceManager;
import com.example.ezchat.utilities.Constants;
import com.example.ezchat.utilities.Utilities;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles user phone number input and saves it in preferences for later use.
 */
public class LoginPhoneNumberActivity extends AppCompatActivity {

    private ActivityLoginPhoneNumberBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding and PreferenceManager
        binding = ActivityLoginPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = PreferenceManager.getInstance(this);

        // Set default country to the United States
        binding.loginCountrycode.setDefaultCountryUsingNameCode(Constants.DEFAULT_COUNTRY_CODE);
        binding.loginCountrycode.setCountryForNameCode(Constants.DEFAULT_COUNTRY_CODE);

        // Set up "Send OTP" button click listener
        binding.sendOtpBtn.setOnClickListener(v -> validateAndProceed());
    }

    /**
     * Validates the user's input and proceeds to the next step if valid.
     */
    private void validateAndProceed() {
        // Get the selected country code and phone number
        String countryCode = binding.loginCountrycode.getSelectedCountryCodeWithPlus();
        String phoneNumber = binding.loginMobileNumber.getText().toString().trim();

        Log.d(Constants.LOG_TAG_PHONE_NUMBER, "User Input - Country Code: " + countryCode + ", Phone: " + phoneNumber);

        // Validate phone number input
        if (phoneNumber.isEmpty() || phoneNumber.length() < 10) {
            Utilities.showToast(this, Constants.ERROR_INVALID_PHONE, Utilities.ToastType.ERROR);
            Log.e(Constants.LOG_TAG_PHONE_NUMBER, "Invalid phone number: " + phoneNumber);
            return;
        }

        // Construct full phone number
        String fullPhoneNumber = countryCode + phoneNumber;
        Log.d(Constants.LOG_TAG_PHONE_NUMBER, "Constructed Phone Number: " + fullPhoneNumber);

        // Save the phone number to preferences
        preferenceManager.set(Constants.PREF_KEY_PHONE, fullPhoneNumber);
        Log.d(Constants.LOG_TAG_PHONE_NUMBER, "Phone number saved to preferences");

        // Navigate to the OTP Activity
        Map<String, String> extras = new HashMap<>();
        extras.put(Constants.PREF_KEY_PHONE, fullPhoneNumber);

        binding.loginProgressBar.setVisibility(View.VISIBLE); // Show progress bar
        Utilities.navigateToActivity(this, LoginOtpActivity.class, extras);
    }
}