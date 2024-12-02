package com.example.ezchat.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ezchat.R;
import com.example.ezchat.activities.SplashActivity;
import com.example.ezchat.databinding.FragmentProfileBinding;
import com.example.ezchat.utilities.Constants;
import com.example.ezchat.utilities.PreferenceManager;
import com.example.ezchat.utilities.Utilities;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Manages user profile view and updates.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore firestore;
    private String encodedImage = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = PreferenceManager.getInstance(requireContext());
        firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        // Fetch and populate user data
        fetchUserData();

        // Set up listeners for actions
        setupListeners();

        return binding.getRoot();
    }

    private void fetchUserData() {
        String phoneNumber = preferenceManager.get(Constants.PREF_KEY_PHONE, "");

        if (phoneNumber.isEmpty()) {
            Utilities.showToast(requireContext(), "No user logged in. Redirecting to login.", Utilities.ToastType.ERROR);
            logoutUser();
            return;
        }

        firestore.collection(Constants.USER_COLLECTION)
                .document(phoneNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        binding.profilePhone.setText(phoneNumber);
                        if (task.getResult().exists()) {
                            populateUserData(task.getResult().getData());
                        }
                    } else {
                        Log.e(Constants.LOG_TAG_PROFILE_FRAGMENT, "Failed to fetch user data.");
                        Utilities.showToast(requireContext(), "Failed to fetch user data.", Utilities.ToastType.ERROR);
                    }
                });
    }

    private void populateUserData(Object data) {
        binding.profileUsername.setText(preferenceManager.get(Constants.PREF_KEY_USERNAME, ""));
        binding.profileEmail.setText(preferenceManager.get(Constants.PREF_KEY_EMAIL, ""));

        String profilePic = preferenceManager.get(Constants.PREF_KEY_PROFILE_PIC, "");
        if (!profilePic.isEmpty()) {
            Bitmap bitmap = Utilities.decodeImage(profilePic);
            binding.profileImageView.setImageBitmap(bitmap);
            binding.textAddImage.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        binding.profileUpdateBtn.setOnClickListener(v -> updateProfile());
        binding.logoutBtn.setOnClickListener(v -> logoutUser());
        binding.profileImageView.setOnClickListener(v -> pickImage());
    }

    private void updateProfile() {
        String username = binding.profileUsername.getText().toString().trim();
        String email = binding.profileEmail.getText().toString().trim();
        String password = binding.profilePassword.getText().toString().trim();
        String confirmPassword = binding.profileConfirmPassword.getText().toString().trim();

        if (username.isEmpty() || username.length() < 3) {
            binding.profileUsername.setError("Username must be at least 3 characters long");
            return;
        }

        if (!email.isEmpty() && !Utilities.isValidEmail(email)) {
            binding.profileEmail.setError("Enter a valid email address.");
            return;
        }

        if (!password.isEmpty() && !password.equals(confirmPassword)) {
            binding.profileConfirmPassword.setError("Passwords do not match.");
            return;
        }

        binding.profileProgressBar.setVisibility(View.VISIBLE);
        binding.profileUpdateBtn.setVisibility(View.GONE);

        String phoneNumber = preferenceManager.get(Constants.PREF_KEY_PHONE, "");
        if (phoneNumber.isEmpty()) return;

        // Prepare updated user data
        HashMap<String, Object> updatedUser = new HashMap<String, Object>();
        updatedUser.put(Constants.FIELD_USERNAME, username);
        updatedUser.put(Constants.FIELD_EMAIL, email);
        if (!encodedImage.isEmpty()) {
            updatedUser.put(Constants.FIELD_PROFILE_PIC, encodedImage);
        }

        firestore.collection(Constants.USER_COLLECTION)
                .document(phoneNumber)
                .update(updatedUser)
                .addOnCompleteListener(task -> {
                    binding.profileProgressBar.setVisibility(View.GONE);
                    binding.profileUpdateBtn.setVisibility(View.VISIBLE);

                    if (task.isSuccessful()) {
                        preferenceManager.set(Constants.PREF_KEY_USERNAME, username);
                        preferenceManager.set(Constants.PREF_KEY_EMAIL, email);
                        if (!encodedImage.isEmpty()) {
                            preferenceManager.set(Constants.PREF_KEY_PROFILE_PIC, encodedImage);
                        }
                        Utilities.showToast(requireContext(), "Profile updated successfully!", Utilities.ToastType.SUCCESS);
                    } else {
                        Log.e(Constants.LOG_TAG_PROFILE_FRAGMENT, "Failed to update profile.");
                        Utilities.showToast(requireContext(), "Failed to update profile.", Utilities.ToastType.ERROR);
                    }
                });
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        preferenceManager.clear();
        Utilities.navigateToActivity(requireContext(), SplashActivity.class, null);
        requireActivity().finish();
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        encodedImage = Utilities.encodeImage(bitmap, 100);
                        binding.profileImageView.setImageBitmap(bitmap);
                        binding.textAddImage.setVisibility(View.GONE);
                    } catch (FileNotFoundException e) {
                        Log.e(Constants.LOG_TAG_PROFILE_FRAGMENT, "Failed to load image: " + e.getMessage());
                        Utilities.showToast(requireContext(), "Failed to load image.", Utilities.ToastType.ERROR);
                    }
                }
            }
    );

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}