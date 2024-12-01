package com.example.ezchat.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ezchat.databinding.ActivityLoginUserNameBinding;
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utilities.PreferenceManager;
import com.example.ezchat.utilities.Utilities;
import com.example.ezchat.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Handles user registration and sign-in with username and profile information.
 */
public class LoginUserNameActivity extends AppCompatActivity {

    private ActivityLoginUserNameBinding binding; // View Binding
    private String phoneNumber; // User's phone number
    private String encodedImage = ""; // Encoded profile picture in Base64
    private UserModel existingUser; // Existing user data if found
    private final FirebaseFirestore db = FirebaseFirestore.getInstance(); // Firestore instance
    private PreferenceManager preferenceManager; // Shared preferences manager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding and PreferenceManager
        binding = ActivityLoginUserNameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = PreferenceManager.getInstance(getApplicationContext());

        // Retrieve phone number from intent
        phoneNumber = getIntent().getStringExtra(Constants.PREF_KEY_PHONE);
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Log.e("LoginUserNameActivity", "Phone number missing. Redirecting to LoginPhoneNumberActivity.");
            navigateToPhoneNumberActivity();
            return;
        }
        Log.d("LoginUserNameActivity", "Phone number received: " + phoneNumber);

        // Check if the user already exists
        checkIfUserExists();

        // Set up button click listeners
        binding.loginCodeBtn.setOnClickListener(v -> {
            if (existingUser != null) {
                signInUser();
            } else {
                registerNewUser();
            }
        });

        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    /**
     * Checks if the user already exists in Firestore using their phone number.
     */
    private void checkIfUserExists() {
        setInProgress(true);

        db.collection(Constants.USER_COLLECTION)
                .document(phoneNumber)
                .get()
                .addOnCompleteListener(task -> {
                    setInProgress(false);

                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        existingUser = task.getResult().toObject(UserModel.class);
                        Log.d("LoginUserNameActivity", "Existing user found: " + existingUser.getUsername());
                        preFillUserData();
                    } else {
                        Log.d("LoginUserNameActivity", "No existing user found for phone number: " + phoneNumber);
                        existingUser = null;
                    }
                });
    }

    /**
     * Pre-fills the UI fields for existing users.
     */
    private void preFillUserData() {
        binding.loginUsername.setText(existingUser.getUsername());
        binding.loginUsername.setEnabled(false);

        if (existingUser.getProfilePic() != null && !existingUser.getProfilePic().isEmpty()) {
            binding.imageProfile.setImageBitmap(Utilities.decodeImage(existingUser.getProfilePic()));
        }

        binding.layoutImage.setEnabled(false);
        binding.loginCodeBtn.setText("Sign In");
    }

    /**
     * Handles signing in an existing user.
     */
    private void signInUser() {
        saveUserToPreferences(existingUser);
        navigateToMainActivity();
    }

    /**
     * Registers a new user by saving their details to Firestore.
     */
    private void registerNewUser() {
        String username = binding.loginUsername.getText().toString().trim();
        if (username.isEmpty() || username.length() < 3) {
            binding.loginUsername.setError("Username must be at least 3 characters long.");
            return;
        }

        setInProgress(true);

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String fcmToken = task.getResult();
                UserModel newUser = new UserModel(phoneNumber, username);
                newUser.setFcmToken(fcmToken);
                newUser.setProfilePic(encodedImage);

                db.collection(Constants.USER_COLLECTION)
                        .document(phoneNumber)
                        .set(newUser)
                        .addOnCompleteListener(saveTask -> {
                            setInProgress(false);
                            if (saveTask.isSuccessful()) {
                                Log.d("LoginUserNameActivity", "New user registered: " + username);
                                saveUserToPreferences(newUser);
                                navigateToMainActivity();
                            } else {
                                Utilities.showToast(this, "Failed to register user. Please try again.", Utilities.ToastType.ERROR);
                                Log.e("LoginUserNameActivity", "User registration failed: " + saveTask.getException().getMessage());
                            }
                        });
            } else {
                setInProgress(false);
                Utilities.showToast(this, "Failed to retrieve FCM token. Please try again.", Utilities.ToastType.ERROR);
                Log.e("LoginUserNameActivity", "FCM token retrieval failed: " + task.getException().getMessage());
            }
        });
    }

    /**
     * Saves user data to SharedPreferences for local access.
     */
    private void saveUserToPreferences(UserModel user) {
        preferenceManager.putString(Constants.PREF_KEY_PHONE, user.getPhone());
        preferenceManager.putString(Constants.PREF_KEY_USERNAME, user.getUsername());
        preferenceManager.putString(Constants.FIELD_PROFILE_PIC, user.getProfilePic());
        preferenceManager.putString(Constants.PREF_KEY_FCM_TOKEN, user.getFcmToken());
        Log.d("LoginUserNameActivity", "User data saved to preferences: " + user.getUsername());
    }

    /**
     * Navigates to MainActivity after successful registration or sign-in.
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /**
     * Redirects the user to LoginPhoneNumberActivity if the phone number is missing.
     */
    private void navigateToPhoneNumberActivity() {
        Intent intent = new Intent(this, LoginPhoneNumberActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /**
     * Toggles the progress state of the activity.
     */
    private void setInProgress(boolean inProgress) {
        binding.loginProgressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        binding.loginCodeBtn.setEnabled(!inProgress);
    }

    /**
     * Launches the image picker and encodes the selected image.
     */
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        encodedImage = Utilities.encodeImage(bitmap);
                        binding.imageProfile.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        Log.e("LoginUserNameActivity", "Image file not found.", e);
                        Utilities.showToast(this, "Failed to load image.", Utilities.ToastType.ERROR);
                    }
                }
            }
    );
}