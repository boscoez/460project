package com.example.ezchat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ezchat.activities.SplashActivity;
import com.example.ezchat.databinding.FragmentProfileBinding;
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utilities.AndroidUtil;
import com.example.ezchat.utilities.FirebaseUtil;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * A Fragment that allows the user to view and update their profile information,
 * including username, phone number, and profile picture.
 */
public class ProfileFragment extends Fragment {

    // View Binding instance
    private FragmentProfileBinding binding;

    private UserModel currentUserModel;      // Model to store the current user details
    private ActivityResultLauncher<Intent> imagePickLauncher; // Launcher for picking images

    // Default constructor
    public ProfileFragment() {
    }

    /**
     * Initializes the fragment and registers the ActivityResultLauncher for image picking.
     * @param savedInstanceState Saved instance state of the fragment.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register image picker result launcher
        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            Uri selectedImageUri = data.getData();
                            try {
                                String base64Image = convertImageToBase64(selectedImageUri);
                                currentUserModel.setProfilePic(base64Image);
                                // Set the selected profile picture in the ImageView
                                AndroidUtil.setProfilePicFromBase64(getContext(), base64Image, binding.profileImageView);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }

    /**
     * Inflates the layout for this fragment and sets up UI components and listeners using View Binding.
     * @param inflater           The LayoutInflater object.
     * @param container          The parent ViewGroup.
     * @param savedInstanceState Saved instance state.
     * @return The inflated View for the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout using View Binding
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Fetch and display user data
        getUserData();

        // Set listener for the update button
        binding.profileUpdateBtn.setOnClickListener(v -> updateBtnClick());

        // Set listener for the logout button
        binding.logoutBtn.setOnClickListener(v -> {
            FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUtil.logout();
                    Intent intent = new Intent(getContext(), SplashActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            });
        });

        // Set listener for profile picture click to launch image picker
        binding.profileImageView.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .cropSquare()
                    .compress(512) // Final image size will be less than 512 KB
                    .maxResultSize(512, 512) // Final image resolution will be less than 512x512
                    .createIntent(intent -> {
                        imagePickLauncher.launch(intent);
                        return null;
                    });
        });

        return view;
    }

    /**
     * Handles the update profile button click. Validates username input and updates
     * the profile picture (if any) before updating Firestore.
     */
    private void updateBtnClick() {
        String newUsername = binding.profileUsername.getText().toString().trim();
        if (newUsername.isEmpty() || newUsername.length() < 3) {
            binding.profileUsername.setError("Username length should be at least 3 characters");
            return;
        }

        currentUserModel.setUsername(newUsername);
        setInProgress(true);

        // Update Firestore directly since profilePic is already set as Base64
        updateToFirestore();
    }

    /**
     * Updates the user profile in Firestore with the current user model data.
     */
    private void updateToFirestore() {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection("users").document(userId);

        docRef.update("profilePic", currentUserModel.getProfilePic(),
                        "username", currentUserModel.getUsername())
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
     * Fetches the current user data from Firestore and populates the UI fields.
     */
    private void getUserData() {
        setInProgress(true);

        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            setInProgress(false);
            if (task.isSuccessful()) {
                currentUserModel = task.getResult().toObject(UserModel.class);
                if (currentUserModel != null) {
                    binding.profileUsername.setText(currentUserModel.getUsername());
                    binding.profilePhone.setText(currentUserModel.getPhone());
                    if (currentUserModel.getProfilePic() != null && !currentUserModel.getProfilePic().isEmpty()) {
                        AndroidUtil.setProfilePicFromBase64(getContext(), currentUserModel.getProfilePic(), binding.profileImageView);
                    } else {
                        binding.profileImageView.setImageResource(R.drawable.ic_person);
                    }
                } else {
                    Toast.makeText(getContext(), "User data is null", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Failed to fetch user data: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Toggles the progress state by showing/hiding the progress bar and update button.
     * @param inProgress Whether the progress state is active.
     */
    private void setInProgress(boolean inProgress) {
        if (inProgress) {
            binding.profileProgressBar.setVisibility(View.VISIBLE);
            binding.profileUpdateBtn.setVisibility(View.GONE);
        } else {
            binding.profileProgressBar.setVisibility(View.GONE);
            binding.profileUpdateBtn.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Converts an image URI to a Base64-encoded string.
     * @param imageUri The URI of the image to convert.
     * @return The Base64-encoded string of the image.
     * @throws IOException If an error occurs during reading the image.
     */
    private String convertImageToBase64(Uri imageUri) throws IOException {
        Bitmap bitmap = AndroidUtil.getBitmapFromUri(getContext(), imageUri);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // Compress the bitmap to JPEG with 100% quality
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        // Encode the byte array to Base64 string
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    /**
     * Cleans up the binding when the view is destroyed to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}