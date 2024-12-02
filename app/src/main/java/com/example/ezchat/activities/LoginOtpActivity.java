package com.example.ezchat.activities;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ezchat.R;
import com.example.ezchat.databinding.ActivityLoginOtpBinding;
import com.example.ezchat.utilities.Constants;
import com.example.ezchat.utilities.PreferenceManager;
import com.example.ezchat.utilities.Utilities;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginOtpActivity extends AppCompatActivity {

    private ActivityLoginOtpBinding binding;
    private String phoneNum; // The phone number passed from the previous activity
    private String verificationId; // Firebase Verification ID
    private FirebaseAuth firebaseAuth; // Firebase Authentication instance
    private PreferenceManager preferenceManager; // Shared Preferences manager
    private int countdownSeconds = 30; // Countdown duration for the resend text

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding and PreferenceManager
        binding = ActivityLoginOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = PreferenceManager.getInstance(this);

        // Initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();

        // Retrieve the phone number from the preferences
        phoneNum = preferenceManager.get(Constants.PREF_KEY_PHONE, "");
        if (phoneNum.isEmpty()) {
            Log.e(Constants.LOG_TAG_PHONE_NUMBER, "Phone number missing. Redirecting to LoginPhoneNumberActivity.");
            Utilities.navigateToActivity(this, LoginPhoneNumberActivity.class, null);
            finish();
            return;
        }

        Log.d(Constants.LOG_TAG_PHONE_NUMBER, "Phone number retrieved: " + phoneNum);

        // Send OTP to the provided phone number
        sendOtp(phoneNum);

        // Set up button click listeners
        binding.loginNextBtn.setOnClickListener(v -> verifyOtp());
        binding.resendOtpTextview.setOnClickListener(v -> resendOtp());
        binding.btnBack.setOnClickListener(v -> {
            Log.d(Constants.LOG_TAG_PHONE_NUMBER, "Back button clicked. Navigating back to LoginPhoneNumberActivity.");
            onBackPressed();
        });
    }

    /**
     * Sends an OTP to the specified phone number using Firebase.
     *
     * @param phoneNumber The phone number to send the OTP to.
     */
    private void sendOtp(String phoneNumber) {
        Log.d(Constants.LOG_TAG_PHONE_NUMBER, "Initiating OTP send to: " + phoneNumber);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        Log.d(Constants.LOG_TAG_PHONE_NUMBER, "Verification completed automatically.");
                        signInWithCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Log.e(Constants.LOG_TAG_PHONE_NUMBER, "Verification failed: " + e.getMessage());
                        Utilities.showToast(LoginOtpActivity.this, "Verification failed. Please try again.", Utilities.ToastType.ERROR);
                    }

                    @Override
                    public void onCodeSent(String id, PhoneAuthProvider.ForceResendingToken token) {
                        Log.d(Constants.LOG_TAG_PHONE_NUMBER, "OTP sent. Verification ID: " + id);
                        verificationId = id;
                        startCountdownTimer();
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    /**
     * Starts a countdown timer with a heartbeat animation for the resend text.
     */
    private void startCountdownTimer() {
        countdownSeconds = 30;
        Log.d(Constants.LOG_TAG_PHONE_NUMBER, "Starting resend countdown timer.");

        new Thread(() -> {
            while (countdownSeconds > 0) {
                runOnUiThread(() -> {
                    String countdownText = "Resend CODE in " + countdownSeconds + " seconds.";
                    binding.resendOtpTextview.setText(countdownText);
                    animateHeartbeat(binding.resendOtpTextview);
                });

                countdownSeconds--;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e(Constants.LOG_TAG_PHONE_NUMBER, "Countdown interrupted: " + e.getMessage());
                }
            }

            runOnUiThread(() -> {
                binding.resendOtpTextview.setText("Resend CODE");
                binding.resendOtpTextview.setEnabled(true);
                Log.d(Constants.LOG_TAG_PHONE_NUMBER, "Countdown complete. Resend enabled.");
            });
        }).start();
    }

    /**
     * Animates the text color of the TextView with a heartbeat effect.
     *
     * @param textView The TextView to animate.
     */
    private void animateHeartbeat(TextView textView) {
        int colorFrom = getColor(R.color.primary);
        int colorTo = getColor(R.color.error);
        ObjectAnimator animator = ObjectAnimator.ofObject(textView, "textColor", new ArgbEvaluator(), colorFrom, colorTo);
        animator.setDuration(500); // 500ms for the heartbeat effect
        animator.setRepeatMode(ObjectAnimator.REVERSE);
        animator.setRepeatCount(1);
        animator.start();
        Log.d(Constants.LOG_TAG_PHONE_NUMBER, "Heartbeat animation applied to countdown text.");
    }

    /**
     * Verifies the OTP entered by the user.
     */
    private void verifyOtp() {
        String enteredCode = binding.loginOtp.getText().toString().trim();
        Log.d(Constants.LOG_TAG_PHONE_NUMBER, "User-entered OTP: " + enteredCode);

        if (enteredCode.isEmpty() || enteredCode.length() < 6) {
            Log.w(Constants.LOG_TAG_PHONE_NUMBER, "Invalid OTP entered.");
            Utilities.showToast(this, "Enter a valid 6-digit OTP.", Utilities.ToastType.WARNING);
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, enteredCode);
        signInWithCredential(credential);
    }

    /**
     * Resends the OTP to the phone number.
     */
    private void resendOtp() {
        Log.d(Constants.LOG_TAG_PHONE_NUMBER, "Resend OTP clicked.");
        countdownSeconds = 30;
        binding.resendOtpTextview.setEnabled(false);
        sendOtp(phoneNum);
    }

    /**
     * Signs in the user with the provided PhoneAuthCredential.
     *
     * @param credential The PhoneAuthCredential obtained after verification.
     */
    private void signInWithCredential(PhoneAuthCredential credential) {
        Log.d(Constants.LOG_TAG_PHONE_NUMBER, "Attempting to sign in with credential.");
        binding.loginProgressBar.setVisibility(View.VISIBLE);

        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            binding.loginProgressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Log.d(Constants.LOG_TAG_PHONE_NUMBER, "Sign-in successful.");
                Utilities.showToast(this, "Verification successful!", Utilities.ToastType.SUCCESS);

                // Navigate to the next activity
                Map<String, String> extras = new HashMap<>();
                extras.put(Constants.PREF_KEY_PHONE, phoneNum);
                Utilities.navigateToActivity(this, LoginUserNameActivity.class, extras);
                finish();
            } else {
                Log.e(Constants.LOG_TAG_PHONE_NUMBER, "Sign-in failed: " + task.getException().getMessage());
                Utilities.showToast(this, "Verification failed. Please try again.", Utilities.ToastType.ERROR);
            }
        });
    }
}