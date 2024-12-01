package com.example.ezchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ezchat.databinding.ActivityLoginPhoneNumberBinding;
import com.example.ezchat.utilities.PreferenceManager;
import com.example.ezchat.utilities.Constants;
import com.example.ezchat.utilities.Utilities;

public class LoginPhoneNumberActivity extends AppCompatActivity {

    private ActivityLoginPhoneNumberBinding binding; // View Binding
    private PreferenceManager preferenceManager; // Shared preferences manager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivityLoginPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize PreferenceManager
        preferenceManager = PreferenceManager.getInstance(getApplicationContext());

        // Set default country to US using Constants
        binding.loginCountrycode.setDefaultCountryUsingNameCode(Constants.DEFAULT_COUNTRY_CODE);
        binding.loginCountrycode.setCountryForNameCode(Constants.DEFAULT_COUNTRY_CODE); // Reflect it in the UI

        // Set up "Send OTP" button click listener
        binding.sendOtpBtn.setOnClickListener(v -> processPhoneNumber());
    }

    /**
     * Processes the phone number entered by the user, validates it,
     * saves it to SharedPreferences, and navigates to LoginOtpActivity.
     */
    private void processPhoneNumber() {
        // Retrieve the phone number and country code
        String countryCode = binding.loginCountrycode.getSelectedCountryCodeWithPlus();
        String mobileNumber = binding.loginMobileNumber.getText().toString().trim();

        // Log the raw input
        Log.d("LoginPhoneNumberActivity", "Raw input: " + mobileNumber);

        // Validate the mobile number
        if (!isValidPhoneNumber(mobileNumber)) {
            Utilities.showToast(getApplicationContext(), Constants.TOAST_INVALID_PHONE, Utilities.ToastType.ERROR);
            Log.d("LoginPhoneNumberActivity", "Invalid phone number entered: " + mobileNumber);
            return;
        }

        // Construct the full phone number
        String phoneNumber = countryCode + mobileNumber;
        Log.d("LoginPhoneNumberActivity", "Constructed phone number: " + phoneNumber);

        // Save the phone number in SharedPreferences
        preferenceManager.putString(Constants.PREF_KEY_PHONE, phoneNumber);
        Log.d("LoginPhoneNumberActivity", "Phone number saved to preferences: " + phoneNumber);

        // Navigate to LoginOtpActivity
        Intent intent = new Intent(LoginPhoneNumberActivity.this, LoginOtpActivity.class);
        intent.putExtra(Constants.PREF_KEY_PHONE, phoneNumber);
        Log.d("LoginPhoneNumberActivity", "Phone number passed to OTP activity: " + phoneNumber);
        startActivity(intent);
    }

    /**
     * Validates the phone number to ensure it meets the minimum length and format requirements.
     *
     * @param mobileNumber The phone number entered by the user.
     * @return True if the phone number is valid, false otherwise.
     */
    private boolean isValidPhoneNumber(String mobileNumber) {
        // Ensure the phone number is not empty and meets the minimum length
        if (mobileNumber == null || mobileNumber.isEmpty() || mobileNumber.length() < 10) {
            binding.loginMobileNumber.setError("Enter a valid phone number (10 digits minimum).");
            return false;
        }
        // Additional validation (e.g., regex) can be added here
        return true;
    }
}