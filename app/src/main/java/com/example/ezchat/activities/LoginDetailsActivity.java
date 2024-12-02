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

import com.example.ezchat.databinding.ActivityLoginDetailsBinding;
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utilities.Constants;
import com.example.ezchat.utilities.PreferenceManager;
import com.example.ezchat.utilities.Utilities;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class LoginDetailsActivity extends AppCompatActivity {

    private ActivityLoginDetailsBinding binding;
    private FirebaseFirestore db;
    private PreferenceManager preferenceManager;
    private String phoneNumber;
    private String encodedImage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = PreferenceManager.getInstance(this);
        db = FirebaseFirestore.getInstance();

        phoneNumber = preferenceManager.get(Constants.FIELD_PHONE, "");
        if (phoneNumber.isEmpty()) {
            Log.e(Constants.LOG_TAG_PHONE_NUMBER, "Phone number missing. Redirecting to LoginPhoneNumberActivity.");
            Utilities.navigateToActivity(this, LoginPhoneNumberActivity.class, null);
            finish();
            return;
        }

        Log.d(Constants.LOG_TAG_PHONE_NUMBER, "Phone number retrieved: " + phoneNumber);

        // Check if user already exists in Firestore
        checkIfUserExists();

        binding.loginCodeBtn.setOnClickListener(v -> validateAndRegisterUser());
        binding.layoutImage.setOnClickListener(v -> pickImage());
    }

    /**
     * Checks if a user with the current phone number exists in Firestore.
     */
    private void checkIfUserExists() {
        binding.loginProgressBar.setVisibility(View.VISIBLE);

        db.collection(Constants.COLLECTION_USER)
                .document(phoneNumber)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    binding.loginProgressBar.setVisibility(View.GONE);

                    if (documentSnapshot.exists()) {
                        Log.d(Constants.LOG_TAG_PHONE_NUMBER, "User already exists in Firestore.");
                        UserModel existingUser = documentSnapshot.toObject(UserModel.class);

                        if (existingUser != null) {
                            // Save user data to preferences
                            preferenceManager.set(Constants.FIELD_USERNAME, existingUser.username);
                            preferenceManager.set(Constants.FIELD_EMAIL, existingUser.email);
                            preferenceManager.set(Constants.FIELD_PROFILE_PIC, existingUser.profilePic);

                            // Populate UI with existing user data
                            populateExistingUserData(existingUser);
                        }
                    } else {
                        Log.d(Constants.LOG_TAG_PHONE_NUMBER, "No user found. Proceeding with registration.");
                    }
                })
                .addOnFailureListener(e -> {
                    binding.loginProgressBar.setVisibility(View.GONE);
                    Log.e(Constants.LOG_TAG_PHONE_NUMBER, "Failed to check if user exists: " + e.getMessage());
                    Utilities.showToast(this, "Failed to check user status. Please try again.", Utilities.ToastType.ERROR);
                });
    }

    /**
     * Populates the fields with existing user data and hides non-editable fields.
     */
    private void populateExistingUserData(UserModel user) {
        // Populate the username and profile picture fields
        binding.loginUsername.setText(user.username);
        binding.loginUsername.setEnabled(false); // Make username uneditable

        if (user.profilePic != null && !user.profilePic.isEmpty()) {
            binding.imageProfile.setImageBitmap(Utilities.decodeImage(user.profilePic));
            binding.textAddImage.setVisibility(View.GONE);
        }

        // Hide other fields (email, password, etc.)
        binding.loginEmail.setVisibility(View.GONE);
        binding.loginPassword.setVisibility(View.GONE);
        binding.loginConfirmedPassword.setVisibility(View.GONE);
        binding.loginCodeBtn.setText("Continue");

        // Disable profile picture click
        binding.layoutImage.setOnClickListener(null);

        // Adjust the flow to simply continue to the MainActivity
        binding.loginCodeBtn.setOnClickListener(v -> navigateToMainActivity());
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

    private void validateAndRegisterUser() {
        String username = binding.loginUsername.getText().toString().trim();
        String email = binding.loginEmail.getText().toString().trim();
        String password = binding.loginPassword.getText().toString();
        String confirmPassword = binding.loginConfirmedPassword.getText().toString();

        if (username.isEmpty() || username.length() < 3) {
            showError(binding.loginUsername, "Username must be at least 3 characters.");
            return;
        }

        if (!Utilities.isValidEmail(email)) {
            showError(binding.loginEmail, "Enter a valid email address.");
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            showError(binding.loginPassword, "Password must be at least 6 characters.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError(binding.loginConfirmedPassword, "Passwords do not match.");
            return;
        }

        registerNewUser(username, email, password);
    }

    private void showError(EditText field, String message) {
        field.setError(message);
        field.requestFocus();
    }

    private void registerNewUser(String username, String email, String password) {
        binding.loginProgressBar.setVisibility(View.VISIBLE);
        binding.loginCodeBtn.setVisibility(View.INVISIBLE);

        UserModel newUser = new UserModel(phoneNumber, username);
        newUser.email = email;
        newUser.hashedPassword = Utilities.hashPassword(password);
        newUser.profilePic = encodedImage;

        db.collection(Constants.COLLECTION_USER)
                .document(phoneNumber)
                .set(newUser)
                .addOnSuccessListener(aVoid -> {
                    preferenceManager.set(Constants.FIELD_USERNAME, username);
                    preferenceManager.set(Constants.FIELD_EMAIL, email);
                    preferenceManager.set(Constants.KEY_IS_LOGGED_IN, true);

                    navigateToMainActivity();
                })
                .addOnFailureListener(e -> {
                    binding.loginProgressBar.setVisibility(View.GONE);
                    binding.loginCodeBtn.setVisibility(View.VISIBLE);
                    Log.e(Constants.LOG_TAG_PHONE_NUMBER, "Failed to register user: " + e.getMessage());
                    Utilities.showToast(this, "Failed to register user. Please try again.", Utilities.ToastType.ERROR);
                });
    }

    private void navigateToMainActivity() {
        Utilities.navigateToActivity(LoginDetailsActivity.this, MainActivity.class, null);
        finish();
    }
}