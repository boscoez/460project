package com.example.ezchat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ezchat.utils.AndroidUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class LoginOtpActivity extends AppCompatActivity {
    String phoneNum;
    Long timeoutSeconds = 30L; // Example timeout of 60 seconds
    String verificationCode;
    PhoneAuthProvider.ForceResendingToken resendingToken;
    EditText codeInput;
    Button nxtBtn;
    ProgressBar progressBar;
    TextView resendOtpTextView;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    /**
     * Initializes the OTP login activity, sets up UI components, sends OTP to the provided phone number,
     * and sets click listeners for the "Next" and "Resend OTP" buttons.
     *
     * @param savedInstanceState The saved instance state for the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_otp);

        codeInput = findViewById(R.id.login_otp);
        nxtBtn = findViewById(R.id.login_next_btn);
        progressBar = findViewById(R.id.login_progress_bar);
        resendOtpTextView = findViewById(R.id.resend_otp_textview);

        phoneNum = Objects.requireNonNull(getIntent().getExtras()).getString("phone");

        sendOtp(phoneNum, false);

        nxtBtn.setOnClickListener(v -> {
            String enteredCode = codeInput.getText().toString();
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, enteredCode);
            singIn(credential);
        });
        resendOtpTextView.setOnClickListener((v) -> {
            sendOtp(phoneNum, true);
        });
    }
    /**
     * Sends an OTP to the specified phone number. Uses Firebase's PhoneAuthProvider to initiate or resend the OTP code.
     *
     * @param phoneNum The phone number to send the OTP to.
     * @param isResend Boolean indicating if this is a resend request.
     */
    void sendOtp(String phoneNum, boolean isResend){
        startResendTimer();
        setInProgress(true);
        PhoneAuthOptions.Builder builder =
            PhoneAuthOptions.newBuilder(mAuth)
                    .setPhoneNumber(phoneNum)
                    .setTimeout(timeoutSeconds, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                            singIn(phoneAuthCredential);
                            setInProgress(false);
                        }

                        @Override
                        public void onVerificationFailed(@NonNull FirebaseException e) {
                            AndroidUtil.showToast(getApplicationContext(),"Verification Failed");
                            setInProgress(false);
                        }
                        @Override
                        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                            super.onCodeSent(s, forceResendingToken);
                            verificationCode = s;
                            resendingToken = forceResendingToken;
                            AndroidUtil.showToast(getApplicationContext(),"Code sent successfully.");
                            setInProgress(false);
                        }
                    });
        if(isResend){
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build());
        }else{
            PhoneAuthProvider.verifyPhoneNumber(builder.build());
        }
    }
    /**
     * Sets the in-progress state for UI components, showing a progress bar and hiding the "Next" button.
     *
     * @param inProgress Boolean indicating whether to show the progress bar and hide the "Next" button.
     */
    void  setInProgress(boolean inProgress){
        if(inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            nxtBtn.setVisibility(View.GONE);
        }else{
            progressBar.setVisibility(View.GONE);
            nxtBtn.setVisibility(View.VISIBLE);
        }
    }
    /**
     * Signs the user in with the provided PhoneAuthCredential. If successful, navigates to LoginUserNameActivity.
     *
     * @param phoneAuthCredential The credential obtained after OTP verification.
     */
    void singIn(PhoneAuthCredential phoneAuthCredential){
        //login  and go to next activity
        setInProgress(true);
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                setInProgress(false);
                if(task.isSuccessful()){
                    Intent intent = new Intent(LoginOtpActivity.this, LoginUserNameActivity.class);
                    intent.putExtra("phone", phoneNum);
                    startActivity(intent);

                } else {
                    AndroidUtil.showToast(getApplicationContext(),"Code verification failed!");
                }
            }
        });
    }
    /**
     * Starts a timer for the "Resend OTP" button, making it clickable again after the timeout period.
     * Updates the button text to display the countdown.
     */

    void startResendTimer(){
        resendOtpTextView.setEnabled(false);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeoutSeconds--;
                resendOtpTextView.setText("Resend Code in " + timeoutSeconds + " seconds. ");
                if(timeoutSeconds <= 0){
                    timeoutSeconds = 30L;
                    timer.cancel();
                    runOnUiThread(() -> {
                        resendOtpTextView.setEnabled(true);
                    });
                }
            }
        }, 0,1000);

    }
}