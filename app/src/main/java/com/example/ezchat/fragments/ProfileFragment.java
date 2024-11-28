package com.example.ezchat.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ezchat.activities.SplashActivity;
import com.example.ezchat.databinding.FragmentProfileBinding;
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utilities.FirebaseUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

/**
 * A fragment that allows users to view and update their profile information.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding; // View binding for fragment_profile.xml
    private SharedPreferences preferences; // SharedPreferences to manage user session
    private UserModel currentUserModel;    // Current user's profile data
    private String encodedImage = "";      // Holds the Base64-encoded profile picture

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize SharedPreferences
        preferences = requireContext().getSharedPreferences(UserModel.FIELD_COLLECTION_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        // Fetch and display user data
        getUserData();

        // Set up the update profile button click listener
        binding.profileUpdateBtn.setOnClickListener(v -> updateBtnClick());

        // Set up the logout button click listener
        binding.logoutBtn.setOnClickListener(v -> logoutUser());

        // Handle profile picture selection
        binding.profileImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

        return binding.getRoot();
    }

    /**
     * Launches the image picker and handles the result.
     */
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        // Compress and encode image
                        Bitmap compressedBitmap = compressImage(bitmap);
                        encodedImage = encodeImage(compressedBitmap);

                        // Update UI with selected image
                        binding.profileImageView.setImageBitmap(compressedBitmap);
                        binding.textAddImage.setVisibility(View.GONE);

                    } catch (FileNotFoundException e) {
                        Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    /**
     * Updates the user profile with the new username and profile picture.
     */
    private void updateBtnClick() {
        String newUsername = binding.profileUsername.getText().toString().trim();

        if (newUsername.isEmpty() || newUsername.length() < 3) {
            binding.profileUsername.setError("Username must be at least 3 characters long");
            return;
        }

        setInProgress(true); // Show progress bar
        currentUserModel.username = newUsername;

        if (!encodedImage.isEmpty()) {
            currentUserModel.profilePic = encodedImage;
        }

        String userId = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore.getInstance()
                .collection(UserModel.FIELD_COLLECTION_NAME)
                .document(userId)
                .update(
                        UserModel.FIELD_USERNAME, newUsername,
                        UserModel.FIELD_PROFILE_PIC, currentUserModel.profilePic
                )
                .addOnSuccessListener(aVoid -> {
                    setInProgress(false);
                    Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    setInProgress(false);
                    Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Logs out the user and redirects to the SplashActivity.
     */
    private void logoutUser() {
        String userId = FirebaseAuth.getInstance().getUid(); // Get the current user's ID
        if (userId == null) {
            Toast.makeText(getContext(), "Failed to log out: User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a map to hold the fields to update
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(UserModel.FIELD_FCM_TOKEN, com.google.firebase.firestore.FieldValue.delete());

        // Get the document reference for the current user
        FirebaseFirestore.getInstance()
                .collection(UserModel.FIELD_COLLECTION_NAME)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    // Clear FCM token locally
                    FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Clear all preferences upon successful token removal
                            preferences.edit().clear().apply();
                            FirebaseUtil.logout(); // Log out the user from Firebase Auth

                            // Redirect to the SplashActivity
                            Intent intent = new Intent(getContext(), SplashActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getContext(), "Failed to clear FCM token locally", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle failure in clearing FCM token from Firestore
                    Toast.makeText(getContext(), "Failed to log out: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Fetches the current user's profile data from Firestore.
     */
    private void getUserData() {
        setInProgress(true);

        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            setInProgress(false);
            if (task.isSuccessful() && task.getResult() != null) {
                currentUserModel = task.getResult().toObject(UserModel.class);
                if (currentUserModel != null) {
                    binding.profileUsername.setText(currentUserModel.username);
                    binding.profilePhone.setText(currentUserModel.phone);

                    if (currentUserModel.profilePic != null && !currentUserModel.profilePic.isEmpty()) {
                        Bitmap bitmap = decodeImage(currentUserModel.profilePic);
                        binding.profileImageView.setImageBitmap(bitmap);
                        binding.textAddImage.setVisibility(View.GONE);
                    }
                }
            } else {
                Toast.makeText(getContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Shows or hides the progress bar during updates.
     */
    private void setInProgress(boolean inProgress) {
        binding.profileProgressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        binding.profileUpdateBtn.setVisibility(inProgress ? View.GONE : View.VISIBLE);
    }

    /**
     * Compresses the given bitmap to reduce file size.
     *
     * @param bitmap The bitmap to compress.
     * @return The compressed bitmap.
     */
    private Bitmap compressImage(Bitmap bitmap) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outStream);
        byte[] byteArray = outStream.toByteArray();
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    /**
     * Encodes a bitmap to a Base64 string.
     *
     * @param bitmap The bitmap to encode.
     * @return The Base64-encoded string.
     */
    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
        byte[] byteArray = outStream.toByteArray();
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);
    }

    /**
     * Decodes a Base64 string to a bitmap.
     *
     * @param encodedImage The Base64 string to decode.
     * @return The decoded bitmap.
     */
    private Bitmap decodeImage(String encodedImage) {
        byte[] bytes = android.util.Base64.decode(encodedImage, android.util.Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}