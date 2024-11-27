package com.example.ezchat;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.ezchat.model.UserModel;
import com.example.ezchat.utils.AndroidUtil;
import com.example.ezchat.utils.FirebaseUtil;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

/**
 * A Fragment that allows the user to view and update their profile information,
 * including username, phone number, and profile picture.
 */
public class ProfileFragment extends Fragment {

    // UI components
    ImageView profilePic;            // ImageView to display and update profile picture
    EditText usernameInput;          // EditText for entering/updating the username
    EditText phoneInput;             // EditText for displaying the phone number
    Button updateProfileBtn;         // Button to save profile changes
    ProgressBar progressBar;         // ProgressBar to indicate loading state
    TextView logoutBtn;              // TextView for logout functionality
    UserModel currentUserModel;      // Model to store the current user details
    ActivityResultLauncher<Intent> imagePickLauncher; // Launcher for picking images
    Uri selectedImageUri;            // URI of the selected profile picture

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
                            selectedImageUri = data.getData();
                            // Set the selected profile picture in the ImageView
                            AndroidUtil.setProfilePic(getContext(), selectedImageUri, profilePic);
                        }
                    }
                }
        );
    }
    /**
     * Inflates the layout for this fragment and sets up UI components and listeners.
     * @param inflater           The LayoutInflater object.
     * @param container          The parent ViewGroup.
     * @param savedInstanceState Saved instance state.
     * @return The inflated View for the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        // Initialize UI components
        profilePic = view.findViewById(R.id.profile_image_view);
        usernameInput = view.findViewById(R.id.profile_username);
        phoneInput = view.findViewById(R.id.profile_phone);
        updateProfileBtn = view.findViewById(R.id.profle_update_btn);
        progressBar = view.findViewById(R.id.profile_progress_bar);
        logoutBtn = view.findViewById(R.id.logout_btn);
        // Fetch and display user data
        getUserData();
        // Set listener for the update button
        updateProfileBtn.setOnClickListener(v -> updateBtnClick());
        // Set listener for the logout button
        logoutBtn.setOnClickListener(v -> {
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
        profilePic.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .cropSquare()
                    .compress(512)
                    .maxResultSize(512, 512)
                    .createIntent(intent -> {
                        imagePickLauncher.launch(intent);
                        return null;
                    });
        });
        return view;
    }
    /**
     * Handles the update profile button click. Validates username input and uploads
     * the selected profile picture, if any, before updating Firestore.
     */
    @OptIn(markerClass = UnstableApi.class)
    void updateBtnClick() {
        String newUsername = usernameInput.getText().toString();
        if (newUsername.isEmpty() || newUsername.length() < 3) {
            usernameInput.setError("Username length should be at least 3 chars");
            return;
        }

        currentUserModel.setUsername(newUsername);
        setInProgress(true);

        // If a new profile picture is selected, upload it
        if (selectedImageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                    .child("profile_pics")
                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() + ".jpg");

            storageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        storageRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    currentUserModel.setProfilePicUrl(uri.toString());
                                    updateToFirestore();
                                })
                                .addOnFailureListener(e -> {
                                    setInProgress(false);
                                    Log.e("ProfileFragment", "Failed to get download URL", e);
                                    AndroidUtil.showToast(getContext(), "Failed to get download URL: " + e.getMessage());
                                });
                    })
                    .addOnFailureListener(e -> {
                        setInProgress(false);
                        Log.e("ProfileFragment", "Failed to upload profile picture", e);
                        AndroidUtil.showToast(getContext(), "Failed to upload profile picture: " + e.getMessage());
                    });
        } else {
            // If no new profile picture is selected, update Firestore directly
            updateToFirestore();
        }
    }
    /**
     * Updates the user profile in Firestore with the current user model data.
     */
    @OptIn(markerClass = UnstableApi.class)
    void updateToFirestore() {
        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        docRef.update("profilePicUrl", currentUserModel.getProfilePicUrl(), "username", currentUserModel.getUsername())
                .addOnSuccessListener(aVoid -> {
                    setInProgress(false);
                    AndroidUtil.showToast(getContext(), "Profile updated successfully");
                })
                .addOnFailureListener(e -> {
                    setInProgress(false);
                    AndroidUtil.showToast(getContext(), "Update failed: " + e.getMessage());
                });
    }
    /**
     * Fetches the current user data from Firestore and populates the UI fields.
     */
    void getUserData() {
        setInProgress(true);

        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            setInProgress(false);
            if (task.isSuccessful()) {
                currentUserModel = task.getResult().toObject(UserModel.class);
                if (currentUserModel != null) {
                    usernameInput.setText(currentUserModel.getUsername());
                    phoneInput.setText(currentUserModel.getPhone());
                    if (currentUserModel.getProfilePicUrl() != null && !currentUserModel.getProfilePicUrl().isEmpty()) {
                        Uri uri = Uri.parse(currentUserModel.getProfilePicUrl());
                        AndroidUtil.setProfilePic(getContext(), uri, profilePic);
                    } else {
                        profilePic.setImageResource(R.drawable.ic_person);
                    }
                } else {
                    AndroidUtil.showToast(getContext(), "User data is null");
                }
            } else {
                AndroidUtil.showToast(getContext(), "Failed to fetch user data: " + Objects.requireNonNull(task.getException()).getMessage());
            }
        });
    }
    /**
     * Toggles the progress state by showing/hiding the progress bar and update button.
     * @param inProgress Whether the progress state is active.
     */
    void setInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            updateProfileBtn.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            updateProfileBtn.setVisibility(View.VISIBLE);
        }
    }
}
