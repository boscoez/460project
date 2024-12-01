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
import com.example.ezchat.utilities.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * A fragment that allows users to view and update their profile information.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding; // View binding for fragment_profile.xml
    private PreferenceManager preferenceManager; // Preference manager for user data
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

        if(preferenceManager == null) {
            throw new NullPointerException();
        }
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
                        Utilities.showToast(requireContext(), "Failed to load image", Utilities.ToastType.ERROR);
                    }
                }
            }
    );

    /**
     * Fetches the current user's profile data from Firestore using the phone number.
     */
    private void getUserData() {
        setInProgress(true);
        String phoneNumber = preferenceManager.getString(Constants.FIELD_PHONE);

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            setInProgress(false);
            Utilities.showToast(requireContext(), "No user logged in. Please log in again.", Utilities.ToastType.ERROR);
            logoutUser(); // Redirect to login
            return;
        }

        firestore.collection(Constants.USER_COLLECTION)
                .document(phoneNumber)
                .get()
                .addOnCompleteListener(task -> {
                    setInProgress(false);
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        UserModel user = task.getResult().toObject(UserModel.class);

                        if (user != null) {
                            binding.profileUsername.setText(user.getUsername());
                            binding.profilePhone.setText(user.getPhone());
                            binding.profileEmail.setText(user.getEmail());

                            if (user.getProfilePic() != null && !user.getProfilePic().isEmpty()) {
                                Bitmap bitmap = Utilities.decodeImage(user.getProfilePic());
                                binding.profileImageView.setImageBitmap(bitmap);
                                binding.textAddImage.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        Utilities.showToast(requireContext(), "User data not found.", Utilities.ToastType.ERROR);
                    }
                });
    }

    /**
     * Updates the user's profile with the new username, email, password, and profile picture.
     */
    private void updateProfile() {
        String newUsername = binding.profileUsername.getText().toString().trim();
        String newEmail = binding.profileEmail.getText().toString().trim();
        String newPassword = binding.profilePassword.getText().toString().trim();
        String confirmPassword = binding.profileConfirmPassword.getText().toString().trim();

        // Validate username, email, and password fields
        if (newUsername.isEmpty() || newUsername.length() < 3) {
            binding.profileUsername.setError("Username must be at least 3 characters long");
            return;
        }
        if (newEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            binding.profileEmail.setError("Enter a valid email address.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            binding.profileConfirmPassword.setError("Passwords do not match.");
            return;
        }

        setInProgress(true);

        // Update the user's information in Firestore
        UserModel updatedUser = new UserModel(preferenceManager.getString(Constants.FIELD_PHONE), newUsername);
        updatedUser.setEmail(newEmail);
        updatedUser.setFcmToken(preferenceManager.getString(Constants.FIELD_FCM_TOKEN));

        // If the user has selected a profile picture, update it
        if (!encodedImage.isEmpty()) {
            updatedUser.setProfilePic(encodedImage);
        }

        // Set hashed password if provided
        if (!newPassword.isEmpty()) {
            updatedUser.setHashedPassword(newPassword);
        }

        firestore.collection(Constants.USER_COLLECTION)
                .document(updatedUser.getPhone())
                .set(updatedUser)
                .addOnCompleteListener(task -> {
                    setInProgress(false);
                    if (task.isSuccessful()) {
                        // Update preferences as well
                        preferenceManager.putString(Constants.FIELD_USERNAME, newUsername);
                        preferenceManager.putString(Constants.FIELD_EMAIL, newEmail);
                        preferenceManager.putString(Constants.FIELD_PROFILE_PIC, updatedUser.getProfilePic());
                        Utilities.showToast(requireContext(), "Profile updated successfully", Utilities.ToastType.SUCCESS);
                    } else {
                        Utilities.showToast(requireContext(), "Failed to update profile.", Utilities.ToastType.ERROR);
                    }
                });
    }

    /**
     * Logs out the user by clearing their FCM token and preferences.
     */
    private void logoutUser() {
        setInProgress(true);
        FirebaseAuth.getInstance().signOut();

        // Clear preferences
        preferenceManager.clear();

        Intent intent = new Intent(getContext(), SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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