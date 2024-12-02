package com.example.ezchat.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ezchat.R;
import com.example.ezchat.databinding.ActivityLoginUserNameBinding;
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utilities.Constants;
import com.example.ezchat.utilities.PreferenceManager;
import com.example.ezchat.utilities.Utilities;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class LoginUserNameActivity extends AppCompatActivity {

    private ActivityLoginUserNameBinding binding;
    private FirebaseFirestore db; // Firestore instance
    private PreferenceManager preferenceManager; // Shared Preferences manager
    private String phoneNumber; // User's phone number from previous activity
    private String encodedImage = ""; // Holds the Base64-encoded profile picture

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding and PreferenceManager
        binding = ActivityLoginUserNameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = PreferenceManager.getInstance(this);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve the phone number from preferences
        phoneNumber = preferenceManager.get(Constants.PREF_KEY_PHONE, "");
        if (phoneNumber.isEmpty()) {
            Log.e(Constants.LOG_TAG_PHONE_NUMBER, "Phone number missing. Redirecting to LoginPhoneNumberActivity.");
            Utilities.navigateToActivity(this, LoginPhoneNumberActivity.class, null);
            finish();
            return;
        }

        Log.d(Constants.LOG_TAG_PHONE_NUMBER, "Phone number retrieved: " + phoneNumber);

        // Set up button click listeners
        binding.loginCodeBtn.setOnClickListener(v -> validateAndRegisterUser());
        binding.layoutImage.setOnClickListener(v -> pickImage());
    }

    /**
     * Launches an intent to pick an image for the profile picture.
     */
    private void pickImage() {
        Log.d(Constants.LOG_TAG_PHONE_NUMBER, "Image picker launched.");
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        imagePickerLauncher.launch(intent);
    }

    /**
     * ActivityResultLauncher for handling image picking result.
     */
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        encodedImage = Utilities.encodeImage(bitmap, 100);
                        binding.imageProfile.setImageBitmap(bitmap);
                        binding.textAddImage.setVisibility(View.GONE);
                        Log.d(Constants.LOG_TAG_PHONE_NUMBER, "Image successfully selected and encoded.");
                    } catch (FileNotFoundException e) {
                        Log.e(Constants.LOG_TAG_PHONE_NUMBER, "Image file not found: " + e.getMessage());
                        Utilities.showToast(this, "Failed to load image.", Utilities.ToastType.ERROR);
                    }
                }
            });

    /**
     * Validates input fields and registers the user.
     */
    private void validateAndRegisterUser() {
        String username = binding.loginUsername.getText().toString().trim();
        String email = binding.loginEmail.getText().toString().trim();
        String password = binding.loginPassword.getText().toString();
        String confirmPassword = binding.loginConfirmedPassword.getText().toString();

        // Validate username
        if (username.isEmpty() || username.length() < 3) {
            showError(binding.loginUsername, "Username must be at least 3 characters.");
            return;
        }

        // Validate email
        if (!Utilities.isValidEmail(email)) {
            showError(binding.loginEmail, "Enter a valid email address.");
            return;
        }

        // Validate password
        if (password.isEmpty() || password.length() < 6) {
            showError(binding.loginPassword, "Password must be at least 6 characters.");
            return;
        }

        // Validate confirm password
        if (!password.equals(confirmPassword)) {
            showError(binding.loginConfirmedPassword, "Passwords do not match.");
            return;
        }

        // Register user
        registerNewUser(username, email, password);
    }

    /**
     * Displays an error on an EditText field.
     *
     * @param field The EditText field to show the error on.
     * @param message The error message to display.
     */
    private void showError(EditText field, String message) {
        field.setError(message);
        field.requestFocus();
    }

    /**
     * Registers a new user in Firestore.
     *
     * @param username The user's username.
     * @param email    The user's email.
     * @param password The user's password (to be hashed).
     */
    private void registerNewUser(String username, String email, String password) {
        binding.loginProgressBar.setVisibility(View.VISIBLE);
        binding.loginCodeBtn.setVisibility(View.INVISIBLE);

        // Create a new user model
        UserModel newUser = new UserModel(phoneNumber, username);
        newUser.email = email;
        newUser.hashedPassword = Utilities.hashPassword(password);
        newUser.profilePic = encodedImage;

        Log.d(Constants.LOG_TAG_PHONE_NUMBER, "Registering new user: " + newUser);

        // Save user to Firestore
        db.collection(Constants.USER_COLLECTION)
                .document(phoneNumber)
                .set(newUser)
                .addOnSuccessListener(aVoid -> {
                    Log.d(Constants.LOG_TAG_PHONE_NUMBER, "User successfully registered in Firestore.");
                    preferenceManager.set(Constants.PREF_KEY_USERNAME, username);
                    preferenceManager.set(Constants.PREF_KEY_EMAIL, email);
                    preferenceManager.set(Constants.PREF_KEY_IS_LOGGED_IN, true);

                    // Navigate to MainActivity
                    Utilities.navigateToActivity(LoginUserNameActivity.this, MainActivity.class, null);
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.loginProgressBar.setVisibility(View.GONE);
                    binding.loginCodeBtn.setVisibility(View.VISIBLE);
                    Log.e(Constants.LOG_TAG_PHONE_NUMBER, "Failed to register user: " + e.getMessage());
                    Utilities.showToast(this, "Failed to register user. Please try again.", Utilities.ToastType.ERROR);
                });
    }
}