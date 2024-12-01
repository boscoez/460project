package com.example.ezchat.activities;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * LoginOtpActivity handles OTP-based authentication for the provided phone number.
 * The phone number is passed from LoginPhoneNumberActivity.
 */
public class LoginOtpActivity extends AppCompatActivity {

    private ActivityLoginOtpBinding binding;
    private String phoneNum; // Phone number passed from LoginPhoneNumberActivity
    private Long timeoutSeconds = 30L; // Timeout for resend OTP timer in seconds
    private String verificationCode; // Verification code for OTP
    private PhoneAuthProvider.ForceResendingToken resendingToken; // Token for resending OTP

    private FirebaseAuth mAuth = FirebaseAuth.getInstance(); // Firebase Authentication instance
    private PreferenceManager preferenceManager; // Shared preferences manager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivityLoginOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Shared Preferences Manager
        preferenceManager = PreferenceManager.getInstance(getApplicationContext());

        // Retrieve the phone number from the intent or SharedPreferences
        phoneNum = getIntent().getStringExtra(Constants.PREF_KEY_PHONE);

        if (phoneNum != null) {
            // Send OTP to the phone number
            sendOtp(phoneNum, false);
        }

        // Set up "Next" button click listener
        binding.loginNextBtn.setOnClickListener(v -> {
            String enteredCode = binding.loginOtp.getText().toString();
            if (enteredCode.isEmpty() || enteredCode.length() < 6) {
                Utilities.showToast(getApplicationContext(), "Enter a valid OTP", Utilities.ToastType.WARNING);
                return;
            }
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, enteredCode);
            signIn(credential); // Verify and sign in the user
        });

        // Set up "Resend OTP" button click listener
        binding.resendOtpTextview.setOnClickListener(v -> sendOtp(phoneNum, true)); // Resend OTP

        // Set up Back button click listener
        binding.btnBack.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    /**
     * Checks if the device is connected to the internet.
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Sends an OTP to the specified phone number. Uses Firebase's PhoneAuthProvider.
     *
     * @param phoneNum The phone number to send the OTP to.
     * @param isResend Boolean indicating if this is a resend request.
     */
    void sendOtp(String phoneNum, boolean isResend) {
        if (!isNetworkAvailable()) {
            Utilities.showToast(getApplicationContext(), "No internet connection. Please try again.", Utilities.ToastType.ERROR);
            return;
        }

        startResendTimer(); // Start the resend timer
        setInProgress(true); // Show the progress bar

        // Build the PhoneAuthOptions
        PhoneAuthOptions.Builder builder = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNum)
                .setTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        signIn(phoneAuthCredential); // Auto verification
                        setInProgress(false);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        setInProgress(false);
                        Utilities.showToast(getApplicationContext(), "Verification Failed: " + e.getMessage(), Utilities.ToastType.ERROR);
                        // Enable retry option
                        binding.resendOtpTextview.setEnabled(true); // Allow resend on failure
                    }

                    @Override
                    public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        verificationCode = s; // Save the verification code
                        resendingToken = forceResendingToken; // Save the resending token
                        Utilities.showToast(getApplicationContext(), "Code sent successfully.", Utilities.ToastType.SUCCESS);
                        setInProgress(false);
                    }
                });

        // Resend or send OTP based on isResend
        if (isResend) {
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build());
        } else {
            PhoneAuthProvider.verifyPhoneNumber(builder.build());
        }
    }

    /**
     * Toggles the progress state of the activity.
     *
     * @param inProgress Boolean indicating whether to show the progress bar.
     */
    void setInProgress(boolean inProgress) {
        if (inProgress) {
            binding.loginOtp.setVisibility(View.GONE);
            binding.loginProgressBar.setVisibility(View.VISIBLE);
            binding.loginNextBtn.setVisibility(View.GONE);
        } else {
            binding.loginOtp.setVisibility(View.VISIBLE);
            binding.loginProgressBar.setVisibility(View.GONE);
            binding.loginNextBtn.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Verifies the OTP and signs in the user.
     *
     * @param phoneAuthCredential The credential obtained after OTP verification.
     */
    void signIn(PhoneAuthCredential phoneAuthCredential) {
        setInProgress(true); // Show progress bar
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(task -> {
            setInProgress(false);
            if (task.isSuccessful()) {
                // Navigate to the next activity
                Intent intent = new Intent(LoginOtpActivity.this, LoginUserNameActivity.class);
                intent.putExtra(Constants.PREF_KEY_PHONE, phoneNum); // Pass the phone number
                startActivity(intent);
            } else {
                Utilities.showToast(getApplicationContext(), "Code verification failed!", Utilities.ToastType.ERROR);
            }
        });
    }

    /**
     * Starts a timer for the "Resend OTP" button, making it clickable again after the timeout period.
     */
    void startResendTimer() {
        binding.resendOtpTextview.setEnabled(false); // Disable the resend button
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeoutSeconds--;
                runOnUiThread(() -> binding.resendOtpTextview.setText("Resend Code in " + timeoutSeconds + " seconds."));
                if (timeoutSeconds <= 0) {
                    timeoutSeconds = 30L;
                    timer.cancel();
                    runOnUiThread(() -> binding.resendOtpTextview.setEnabled(true)); // Enable the button
                }
            }
        }, 0, 1000); // Schedule the task every second
    }
}