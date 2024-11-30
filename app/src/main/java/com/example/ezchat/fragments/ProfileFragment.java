package com.example.ezchat.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
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
import com.example.ezchat.utilities.PreferenceManager;
import com.example.ezchat.utilities.Utilities;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * A fragment that allows users to view and update their profile information.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding; // View binding for fragment_profile.xml
    private PreferenceManager preferenceManager; // Preference manager for user data
    private UserModel currentUserModel; // Current user's profile data
    private String encodedImage = ""; // Holds the Base64-encoded profile picture
    private FirebaseFirestore firestore; // Firestore instance

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize PreferenceManager and Firestore
        preferenceManager = PreferenceManager.getInstance(requireContext());
        firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        // Fetch and display user data
        getUserData();

        // Set up the update profile button click listener
        binding.profileUpdateBtn.setOnClickListener(v -> updateProfile());

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
                        Bitmap compressedBitmap = Utilities.compressImage(bitmap);
                        encodedImage = Utilities.encodeImage(compressedBitmap);

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
     * Fetches the current user's profile data from Firestore using the phone number.
     */
    private void getUserData() {
        setInProgress(true);
        String phoneNumber = preferenceManager.getString(UserModel.FIELD_PHONE);

        if (phoneNumber == null) {
            setInProgress(false);
            Toast.makeText(requireContext(), "Failed to fetch user data: No phone number.", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUserModel = new UserModel();
        currentUserModel.fetchUser(firestore, phoneNumber, user -> {
            setInProgress(false);
            if (user != null) {
                currentUserModel = user;
                binding.profileUsername.setText(user.username);
                binding.profilePhone.setText(user.phone);

                if (user.profilePic != null && !user.profilePic.isEmpty()) {
                    Bitmap bitmap = Utilities.decodeImage(user.profilePic);
                    binding.profileImageView.setImageBitmap(bitmap);
                    binding.textAddImage.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(requireContext(), "User data not found.", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            setInProgress(false);
            Toast.makeText(requireContext(), "Failed to fetch user data: " + error, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Updates the user's profile with the new username and profile picture.
     */
    private void updateProfile() {
        String newUsername = binding.profileUsername.getText().toString().trim();

        if (newUsername.isEmpty() || newUsername.length() < 3) {
            binding.profileUsername.setError("Username must be at least 3 characters long");
            return;
        }

        setInProgress(true);
        currentUserModel.username = newUsername;

        if (!encodedImage.isEmpty()) {
            currentUserModel.profilePic = encodedImage;
        }

        currentUserModel.updateUser(firestore, success -> {
            setInProgress(false);
            if (success) {
                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to update profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Logs out the user by clearing their FCM token and preferences.
     */
    private void logoutUser() {
        setInProgress(true);
        currentUserModel.logoutUser(firestore, () -> {
            setInProgress(false);
            preferenceManager.clear();
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(getContext(), SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }, error -> {
            setInProgress(false);
            Toast.makeText(getContext(), "Failed to log out: " + error, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Shows or hides the progress bar during updates.
     */
    private void setInProgress(boolean inProgress) {
        binding.profileProgressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        binding.profileUpdateBtn.setVisibility(inProgress ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}