package com.example.ezchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ezchat.databinding.ActivityLoginOtpBinding;
import com.example.ezchat.utilities.PreferenceManager;
import com.example.ezchat.utilities.Constants;
import com.example.ezchat.utilities.Utilities;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

/**
 * Handles OTP-based authentication for the provided phone number.
 */
public class LoginOtpActivity extends AppCompatActivity {

    private ActivityLoginOtpBinding binding;
    private String phoneNum; // Phone number passed from the previous activity
    private String verificationCode; // Verification code received via SMS
    private FirebaseAuth mAuth; // Firebase Authentication instance
    private PreferenceManager preferenceManager; // Shared preferences manager

    private PhoneAuthProvider.ForceResendingToken resendingToken; // Token for resending OTP
    private final long OTP_TIMEOUT = 60L; // Timeout for OTP verification in seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivityLoginOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Authentication and PreferenceManager
        mAuth = FirebaseAuth.getInstance();
        preferenceManager = PreferenceManager.getInstance(getApplicationContext());

        // Retrieve the phone number from the intent
        phoneNum = getIntent().getStringExtra(Constants.PREF_KEY_PHONE);
        if (phoneNum == null || phoneNum.isEmpty()) {
            Log.e("LoginOtpActivity", "Phone number is missing. Redirecting to LoginPhoneNumberActivity.");
            navigateToPhoneNumberActivity();
            return;
        }
        Log.d("LoginOtpActivity", "Phone number received: " + phoneNum);

        // Send OTP to the phone number
        sendOtp(phoneNum);

        // Set up button click listeners
        binding.loginNextBtn.setOnClickListener(v -> verifyOtp());
        binding.resendOtpTextview.setOnClickListener(v -> resendOtp());
        binding.btnBack.setOnClickListener(v -> onBackPressed());
    }

    /**
     * Sends an OTP to the specified phone number using Firebase PhoneAuthProvider.
     *
     * @param phoneNum The phone number to send the OTP to.
     */
    private void sendOtp(String phoneNum) {
        Log.d("LoginOtpActivity", "Sending OTP to: " + phoneNum);

        binding.resendOtpTextview.setEnabled(false); // Disable resend button during sending

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNum) // Phone number to verify
                .setTimeout(OTP_TIMEOUT, TimeUnit.SECONDS) // Timeout duration
                .setActivity(this) // Activity context
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        Log.d("LoginOtpActivity", "Verification completed automatically.");
                        signInWithCredential(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Log.e("LoginOtpActivity", "Verification failed: " + e.getMessage());
                        Utilities.showToast(getApplicationContext(), "Verification failed. Please try again.", Utilities.ToastType.ERROR);
                        binding.resendOtpTextview.setEnabled(true); // Enable resend button
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        Log.d("LoginOtpActivity", "OTP sent. Verification ID: " + verificationId);
                        verificationCode = verificationId;
                        resendingToken = token;
                        binding.resendOtpTextview.setEnabled(false); // Disable resend initially
                        startResendTimer(); // Start timer for resend button
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    /**
     * Verifies the OTP entered by the user.
     */
    private void verifyOtp() {
        String enteredCode = binding.loginOtp.getText().toString().trim();

        if (enteredCode.isEmpty() || enteredCode.length() < 6) {
            Utilities.showToast(this, "Enter a valid 6-digit OTP.", Utilities.ToastType.WARNING);
            Log.d("LoginOtpActivity", "Invalid OTP entered: " + enteredCode);
            return;
        }

        Log.d("LoginOtpActivity", "Verifying OTP: " + enteredCode);

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, enteredCode);
        signInWithCredential(credential);
    }

    /**
     * Resends the OTP to the phone number.
     */
    private void resendOtp() {
        if (resendingToken == null) {
            Log.e("LoginOtpActivity", "Resending token is null. Cannot resend OTP.");
            return;
        }

        Log.d("LoginOtpActivity", "Resending OTP to: " + phoneNum);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNum)
                .setTimeout(OTP_TIMEOUT, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        Log.d("LoginOtpActivity", "Verification completed automatically.");
                        signInWithCredential(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Log.e("LoginOtpActivity", "Verification failed: " + e.getMessage());
                        Utilities.showToast(getApplicationContext(), "Verification failed. Please try again.", Utilities.ToastType.ERROR);
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        Log.d("LoginOtpActivity", "OTP resent. Verification ID: " + verificationId);
                        verificationCode = verificationId;
                        resendingToken = token;
                        startResendTimer(); // Start timer for resend button
                    }
                })
                .setForceResendingToken(resendingToken) // Use resending token
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    /**
     * Signs in the user with the provided PhoneAuthCredential.
     *
     * @param credential The PhoneAuthCredential obtained after verification.
     */
    private void signInWithCredential(PhoneAuthCredential credential) {
        binding.loginProgressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            binding.loginProgressBar.setVisibility(View.GONE);

            if (task.isSuccessful()) {
                Log.d("LoginOtpActivity", "Sign-in successful.");
                Utilities.showToast(this, "Verification successful!", Utilities.ToastType.SUCCESS);

                // Navigate to LoginUserNameActivity
                Intent intent = new Intent(LoginOtpActivity.this, LoginUserNameActivity.class);
                intent.putExtra(Constants.PREF_KEY_PHONE, phoneNum);
                startActivity(intent);
                finish();
            } else {
                Log.e("LoginOtpActivity", "Sign-in failed: " + task.getException().getMessage());
                Utilities.showToast(this, "Verification failed. Please try again.", Utilities.ToastType.ERROR);
            }
        });
    }

    /**
     * Starts a timer for enabling the "Resend OTP" button.
     */
    private void startResendTimer() {
        binding.resendOtpTextview.setEnabled(false); // Disable resend button initially
        binding.resendOtpTextview.postDelayed(() -> {
            binding.resendOtpTextview.setEnabled(true); // Enable resend button after timeout
        }, TimeUnit.SECONDS.toMillis(OTP_TIMEOUT));
    }

    /**
     * Redirects the user to LoginPhoneNumberActivity if the phone number is missing.
     */
    private void navigateToPhoneNumberActivity() {
        Intent intent = new Intent(this, LoginPhoneNumberActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}