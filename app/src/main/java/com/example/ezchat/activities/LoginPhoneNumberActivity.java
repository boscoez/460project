package com.example.ezchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ezchat.databinding.ActivityLoginPhoneNumberBinding;
import com.example.ezchat.utilities.PreferenceManager;
import com.example.ezchat.utilities.Constants;
import com.example.ezchat.utilities.Utilities;

/**
 * LoginPhoneNumberActivity handles the collection of the user's phone number.
 * It validates the phone number before passing it to LoginOtpActivity for OTP-based authentication.
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

        // Set default country to US using Constants
        binding.loginCountrycode.setDefaultCountryUsingNameCode(Constants.DEFAULT_COUNTRY_CODE);
        binding.loginCountrycode.setCountryForNameCode(Constants.DEFAULT_COUNTRY_CODE); // Ensure it reflects correctly

        // Set up "Send OTP" button click listener
        binding.sendOtpBtn.setOnClickListener(v -> {
            // Retrieve the phone number and country code
            String countryCode = binding.loginCountrycode.getSelectedCountryCodeWithPlus();
            String mobileNumber = binding.loginMobileNumber.getText().toString().trim();

            if (mobileNumber.isEmpty() || mobileNumber.length() < 10) {
                Utilities.showToast(getApplicationContext(), Constants.TOAST_INVALID_PHONE, Utilities.ToastType.ERROR);
                return;
            }

            // Create the full phone number
            String phoneNumber = countryCode + mobileNumber;

            // Save the phone number in SharedPreferences using Constants
            preferenceManager.putString(Constants.PREF_KEY_PHONE, phoneNumber);

            // Navigate to LoginOtpActivity
            Intent intent = new Intent(LoginPhoneNumberActivity.this, LoginOtpActivity.class);
            intent.putExtra(Constants.PREF_KEY_PHONE, phoneNumber);
            startActivity(intent);
        });
    }
}