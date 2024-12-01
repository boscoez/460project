package com.example.ezchat.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ezchat.databinding.ActivityLoginUserNameBinding;
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utilities.PreferenceManager;
import com.example.ezchat.utilities.Utilities;
import com.example.ezchat.utilities.Constants;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Handles signing in or signing up a user with their phone number and profile information.
 */
public class LoginUserNameActivity extends AppCompatActivity {

    private ActivityLoginUserNameBinding binding; // View Binding
    private String phoneNumber; // User's phone number
    private UserModel userModel; // User data model
    private PreferenceManager preferenceManager; // Manages preferences
    private String encodedImage = ""; // Encoded profile picture in Base64
    private final FirebaseFirestore db = FirebaseFirestore.getInstance(); // Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferenceManager = PreferenceManager.getInstance(getApplicationContext());
        binding = ActivityLoginUserNameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Retrieve the phone number from the intent
        phoneNumber = getIntent().getStringExtra(Constants.PREF_KEY_PHONE);

        // Check if user already exists
        checkIfUserExists();

        // Set up "Next" button click listener
        binding.loginCodeBtn.setOnClickListener(v -> {
            if (userModel != null) {
                signInUser(); // Existing user flow
            } else {
                signUpNewUser(); // New user flow
            }
        });

        // Set up "Pick Image" button click listener
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    // Image picker result launcher
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        // Compress and encode image
                        Bitmap compressedBitmap = Utilities.compressImage(bitmap);
                        encodedImage = Utilities.encodeImage(compressedBitmap);

                        binding.imageProfile.setImageBitmap(compressedBitmap);
                        binding.textAddImage.setVisibility(View.GONE);

                    } catch (FileNotFoundException e) {
                        Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    /**
     * Checks if a user already exists in Firestore using their phone number.
     */
    private void checkIfUserExists() {
        setInProgress(true);
        db.collection(Constants.USER_COLLECTION)
                .document(phoneNumber)
                .get()
                .addOnCompleteListener(task -> {
                    setInProgress(false);
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        userModel = task.getResult().toObject(UserModel.class);
                        if (userModel != null) {
                            preFillUserData();
                        }
                    } else {
                        userModel = null; // If user does not exist
                    }
                });
    }

    /**
     * Pre-fills the UI fields for existing users.
     */
    private void preFillUserData() {
        binding.layoutImage.setEnabled(false);
        binding.loginUsername.setText(userModel.getUsername());
        binding.loginUsername.setEnabled(false);
        binding.loginEmail.setVisibility(View.GONE);
        binding.loginPassword.setVisibility(View.GONE);
        binding.loginConfirmedPassword.setVisibility(View.GONE);
        binding.createAccountTitle.setVisibility(View.GONE);
        binding.loginCodeBtn.setText("Sign In");
    }

    /**
     * Signs in an existing user by saving their data locally and navigating to the main activity.
     */
    private void signInUser() {
        setInProgress(true);
        saveUserToPreferences(userModel); // Save user data to preferences
        navigateToMainActivity();
        Toast.makeText(this, "Welcome back, " + userModel.getUsername() + "!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Handles the creation of a new user and saves their data to Firestore.
     */
    private void signUpNewUser() {
        String username = binding.loginUsername.getText().toString().trim();
        String email = binding.loginEmail.getText().toString().trim();
        String password = binding.loginPassword.getText().toString().trim();
        String confirmPassword = binding.loginConfirmedPassword.getText().toString().trim();

        if (!validateInputs(username, email, password, confirmPassword)) return;

        setInProgress(true);

        // Retrieve FCM token for the new user
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String fcmToken = task.getResult();
                UserModel newUser = new UserModel(
                        phoneNumber,
                        username
                );
                newUser.setEmail(email); // Set the email
                newUser.setProfilePic(encodedImage); // Set the profile picture
                newUser.setFcmToken(fcmToken); // Set the FCM token
                newUser.setHashedPassword(password); // Store hashed password
                saveUserToFirestore(newUser); // Save the new user to Firestore
            } else {
                setInProgress(false);
                Toast.makeText(this, "Failed to retrieve FCM token.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Validates input fields for username, email, password, and confirmed password.
     */
    private boolean validateInputs(String username, String email, String password, String confirmPassword) {
        if (username.isEmpty() || username.length() < 3) {
            binding.loginUsername.setError("Username must be at least 3 characters.");
            return false;
        }
        if (email.isEmpty() || !email.contains("@")) {
            binding.loginEmail.setError("Please enter a valid email address.");
            return false;
        }
        if (password.isEmpty() || password.length() < 6) {
            binding.loginPassword.setError("Password must be at least 6 characters.");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            binding.loginConfirmedPassword.setError("Passwords do not match.");
            return false;
        }
        return true;
    }

    /**
     * Saves a new user to Firestore.
     */
    private void saveUserToFirestore(UserModel userModel) {
        db.collection(Constants.USER_COLLECTION)
                .document(userModel.getPhone())
                .set(userModel)
                .addOnCompleteListener(task -> {
                    setInProgress(false);
                    if (task.isSuccessful()) {
                        saveUserToPreferences(userModel); // Save user data to preferences
                        navigateToMainActivity();
                        Toast.makeText(this, "Welcome, " + userModel.getUsername() + "!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Saves user data to SharedPreferences for quick access later.
     */
    private void saveUserToPreferences(UserModel userModel) {
        preferenceManager.putString(Constants.PREF_KEY_PHONE, userModel.getPhone());
        preferenceManager.putString(Constants.PREF_KEY_USERNAME, userModel.getUsername());
        preferenceManager.putString(Constants.FIELD_PROFILE_PIC, userModel.getProfilePic());
        preferenceManager.putString(Constants.PREF_KEY_FCM_TOKEN, userModel.getFcmToken());
        preferenceManager.putString(Constants.PREF_KEY_EMAIL, userModel.getEmail()); // Save email
    }

    /**
     * Navigates to the MainActivity after successful sign-in or sign-up.
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /**
     * Toggles the progress state of the activity.
     */
    private void setInProgress(boolean inProgress) {
        binding.loginProgressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        binding.loginCodeBtn.setVisibility(inProgress ? View.GONE : View.VISIBLE);
        binding.loginUsername.setEnabled(!inProgress);
        binding.loginPassword.setEnabled(!inProgress);
        binding.loginConfirmedPassword.setEnabled(!inProgress);
    }
}