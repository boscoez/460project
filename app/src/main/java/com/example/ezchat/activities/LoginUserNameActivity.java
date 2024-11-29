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

        phoneNumber = getIntent().getStringExtra(UserModel.FIELD_PHONE);

        checkIfUserExists(); // Check if the user already exists

        binding.loginCodeBtn.setOnClickListener(v -> {
            if (userModel != null) {
                signInUser(); // Existing user flow
            } else {
                signUpNewUser(); // New user flow
            }
        });

        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

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
        db.collection(UserModel.FIELD_COLLECTION_NAME)
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
                        userModel = null;
                    }
                });
    }

    /**
     * Pre-fills the UI fields for existing users.
     */
    private void preFillUserData() {
        binding.loginUsername.setText(userModel.username);
        binding.loginUsername.setEnabled(false);
        binding.loginPassword.setVisibility(View.GONE);
        binding.loginConfirmedPassword.setVisibility(View.GONE);
        binding.textSignIn.setVisibility(View.GONE);
        binding.createAccountTitle.setVisibility(View.GONE);
        binding.loginCodeBtn.setText("Sign In");
    }

    /**
     * Signs in an existing user by saving their data locally and navigating to the main activity.
     */
    private void signInUser() {
        setInProgress(true);
        saveUserToPreferences(userModel);
        navigateToMainActivity();
        Toast.makeText(this, "Welcome back, " + userModel.username + "!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Handles the creation of a new user and saves their data to Firestore.
     */
    private void signUpNewUser() {
        String username = binding.loginUsername.getText().toString().trim();
        String password = binding.loginPassword.getText().toString().trim();
        String confirmPassword = binding.loginConfirmedPassword.getText().toString().trim();

        if (!validateInputs(username, password, confirmPassword)) return;

        setInProgress(true);

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String fcmToken = task.getResult();
                UserModel newUser = new UserModel(
                        phoneNumber,
                        username,
                        Timestamp.now(),
                        fcmToken,
                        encodedImage,
                        new ArrayList<>()
                );
                saveUserToFirestore(newUser);
            } else {
                setInProgress(false);
                Toast.makeText(this, "Failed to retrieve FCM token.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInputs(String username, String password, String confirmPassword) {
        if (username.isEmpty() || username.length() < 3) {
            binding.loginUsername.setError("Username must be at least 3 characters.");
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
        db.collection(UserModel.FIELD_COLLECTION_NAME)
                .document(userModel.phone)
                .set(userModel)
                .addOnCompleteListener(task -> {
                    setInProgress(false);
                    if (task.isSuccessful()) {
                        saveUserToPreferences(userModel);
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
        preferenceManager.putString(UserModel.FIELD_PROFILE_PIC, userModel.profilePic);
        preferenceManager.putString(UserModel.FIELD_FCM_TOKEN, userModel.fcmToken);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void setInProgress(boolean inProgress) {
        binding.loginProgressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        binding.loginCodeBtn.setVisibility(inProgress ? View.GONE : View.VISIBLE);
        binding.loginUsername.setEnabled(!inProgress);
        binding.loginPassword.setEnabled(!inProgress);
        binding.loginConfirmedPassword.setEnabled(!inProgress);
    }
}