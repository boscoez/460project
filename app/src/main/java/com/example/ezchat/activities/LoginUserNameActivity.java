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
import com.example.ezchat.utilities.FirebaseUtil;
import com.example.ezchat.utilities.PreferenceManager;
import com.example.ezchat.utilities.Utilities;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
/**
 * LoginUserNameActivity handles the process of signing in existing users
 * or signing up new users, with the option to select a profile picture.
 */
public class LoginUserNameActivity extends AppCompatActivity {

    private ActivityLoginUserNameBinding binding; // View Binding
    private String phoneNumber;
    private UserModel userModel;
    private PreferenceManager preferenceManager;
    private String encodedImage = ""; // To store the profile picture in Base64 format

    /**
     * Initializes the activity, sets up UI components, and checks if the user exists.
     *
     * @param savedInstanceState The saved instance state for the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase and preferences
        FirebaseApp.initializeApp(this);
        preferenceManager = PreferenceManager.getInstance(getApplicationContext());
        binding = ActivityLoginUserNameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Retrieve the phone number from intent
        phoneNumber = getIntent().getStringExtra(UserModel.FIELD_PHONE);

        // Check if the user already exists in Firestore
        checkIfUserExists();

        // Handle "Let Me In" button click
        binding.loginCodeBtn.setOnClickListener(v -> {
            if (userModel != null) {
                signInUser();
            } else {
                signUpNewUser();
            }
        });

        // Handle profile picture selection
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    /**
     * Launches the image picker and handles the result.
     */
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Get the selected image URI
                    Uri imageUri = result.getData().getData();

                    try {
                        // Open an InputStream to the image
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        // Decode the InputStream into a Bitmap
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        // Compress the image to reduce size
                        Bitmap compressedBitmap = Utilities.compressImage(bitmap);

                        // Set the selected image in the profile ImageView
                        binding.imageProfile.setImageBitmap(compressedBitmap);
                        // Hide the "Add Image" text
                        binding.textAddImage.setVisibility(View.GONE);
                        // Encode the image to Base64
                        encodedImage = Utilities.encodeImage(compressedBitmap);

                    } catch (FileNotFoundException e) {
                        Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
    );

    /**
     * Checks if the user exists in Firestore using their phone number.
     * If the user exists, hides sign-up fields and prepares to sign them in.
     */
    private void checkIfUserExists() {
        setInProgress(true);
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            setInProgress(false);
            if (task.isSuccessful() && task.getResult() != null) {
                userModel = task.getResult().toObject(UserModel.class);
                if (userModel != null) {
                    // User exists, pre-fill username and hide sign-up fields
                    binding.loginUsername.setText(userModel.username);
                    binding.loginUsername.setEnabled(false);
                    binding.loginPassword.setVisibility(View.GONE);
                    binding.loginConfirmedPassword.setVisibility(View.GONE);
                    binding.textSignIn.setVisibility(View.GONE);
                    binding.createAccountTitle.setVisibility(View.GONE);
                    binding.loginCodeBtn.setText("Sign In");
                }
            } else {
                userModel = null; // User does not exist
            }
        });
    }

    /**
     * Signs in an existing user by saving their data to preferences
     * and navigating to the main activity.
     */
    private void signInUser() {
        setInProgress(true);

        // Save user details to preferences
        preferenceManager.putString(UserModel.FIELD_PHONE, userModel.phone);
        preferenceManager.putString(UserModel.FIELD_USERNAME, userModel.username);
        preferenceManager.putString(UserModel.FIELD_USER_ID, userModel.username);

        // Navigate to MainActivity
        Intent intent = new Intent(LoginUserNameActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        Toast.makeText(this, "Welcome back, " + userModel.username + "!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Validates input and signs up a new user by saving their data in Firestore.
     */
    private void signUpNewUser() {
        // Validate username and password fields
        String username = binding.loginUsername.getText().toString().trim();
        String password = binding.loginPassword.getText().toString().trim();
        String confirmPassword = binding.loginConfirmedPassword.getText().toString().trim();

        if (username.isEmpty() || username.length() < 3) {
            binding.loginUsername.setError("Username must be at least 3 characters.");
            return;
        }
        if (password.isEmpty() || password.length() < 6) {
            binding.loginPassword.setError("Password must be at least 6 characters.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            binding.loginConfirmedPassword.setError("Passwords do not match.");
            return;
        }

        setInProgress(true);

        // Fetch FCM token
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String fcmToken = task.getResult();
                String userId = FirebaseUtil.currentUserId();

                // Create a new user model
                UserModel userModel = new UserModel(
                        phoneNumber,
                        username,
                        Timestamp.now(),
                        userId,
                        fcmToken,
                        encodedImage, // Profile picture in Base64 format
                        new ArrayList<>() // No chat rooms initially
                );

                // Save the user to Firestore
                saveUserToFirestore(userModel);
            } else {
                setInProgress(false);
                Toast.makeText(this, "Failed to retrieve FCM token.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Saves a new user to Firestore and navigates to the main activity.
     *
     * @param userModel The user model to save.
     */
    private void saveUserToFirestore(UserModel userModel) {
        // Save the user to Firestore using the UID as the document ID
        FirebaseUtil.currentUserDetails().set(userModel).addOnCompleteListener(task -> {
            setInProgress(false);
            if (task.isSuccessful()) {
                // Save user details locally in SharedPreferences
                saveUserToPreferences(userModel);

                // Navigate to MainActivity
                navigateToMainActivity();

                Toast.makeText(this, "Welcome, " + userModel.username + "!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserToPreferences(UserModel userModel) {
        preferenceManager.putString(UserModel.FIELD_PHONE, userModel.phone);
        preferenceManager.putString(UserModel.FIELD_USERNAME, userModel.username);
        preferenceManager.putString(UserModel.FIELD_USER_ID, userModel.userId);
        preferenceManager.putString(UserModel.FIELD_FCM_TOKEN, userModel.fcmToken);
        if (userModel.profilePic != null) {
            preferenceManager.putString(UserModel.FIELD_PROFILE_PIC, userModel.profilePic);
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /**
     * Toggles the progress UI state.
     * @param inProgress True to show progress, false to hide it.
     */
    private void setInProgress(boolean inProgress) {
        binding.loginProgressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        binding.loginCodeBtn.setVisibility(inProgress ? View.GONE : View.VISIBLE);
        binding.loginUsername.setEnabled(!inProgress);
        binding.loginPassword.setEnabled(!inProgress);
        binding.loginConfirmedPassword.setEnabled(!inProgress);
    }
}