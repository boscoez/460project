package com.example.ezchat.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ezchat.activities.SplashActivity;
import com.example.ezchat.databinding.FragmentProfileBinding;
import com.example.ezchat.models.ChatroomModel;
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utilities.AndroidUtil;
import com.example.ezchat.utilities.FirebaseUtil;
import com.example.ezchat.utilities.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

/**
 * A fragment that allows users to view and update their profile information.
 * This includes changing their username, phone number, and profile picture.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding; // View binding for fragment_profile.xml
    private SharedPreferences preferences; // SharedPreferences to manage user session
    private UserModel currentUserModel;    // Current user's profile data

    /**
     * Called when the fragment is first created.
     * Initializes SharedPreferences.
     *
     * @param savedInstanceState Previously saved state (if any).
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize SharedPreferences
        preferences = requireContext().getSharedPreferences(PreferenceManager.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Inflates the fragment's layout and sets up event listeners and UI components.
     *
     * @param inflater           The LayoutInflater used to inflate the layout.
     * @param container          The container for the fragment's view.
     * @param savedInstanceState Previously saved state (if any).
     * @return The root view of the fragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Initialize View Binding
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        // Fetch and display user data
        getUserData();

        // Set up update profile button click listener
        binding.profileUpdateBtn.setOnClickListener(v -> updateBtnClick());

        // Set up logout button click listener
        binding.logoutBtn.setOnClickListener(v -> logoutUser());

        return binding.getRoot();
    }

    /**
     * Handles the profile update button click event.
     * Validates user input and updates their profile information in Firestore.
     */
    private void updateBtnClick() {
        String newUsername = binding.profileUsername.getText().toString().trim();
        if (newUsername.isEmpty() || newUsername.length() < 3) {
            binding.profileUsername.setError("Username must be at least 3 characters long");
            return;
        }

        setInProgress(true); // Show progress bar
        currentUserModel.username = newUsername;

        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        FirebaseFirestore.getInstance()
                .collection(UserModel.FIELD_COLLECTION_NAME)
                .document(userId)
                .update(
                        UserModel.FIELD_USERNAME, newUsername,
                        UserModel.FIELD_PROFILE_PIC, currentUserModel.profilePic
                )
                .addOnSuccessListener(aVoid -> {
                    setInProgress(false); // Hide progress bar
                    Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    setInProgress(false); // Hide progress bar
                    Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Logs out the user by clearing their session and redirecting them to the SplashActivity.
     */
    private void logoutUser() {
        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                preferences.edit().clear().apply(); // Clear user session
                FirebaseUtil.logout(); // Firebase logout

                // Redirect to SplashActivity
                Intent intent = new Intent(getContext(), SplashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    /**
     * Fetches the current user's data from Firestore and displays it in the UI.
     */
    private void getUserData() {
        setInProgress(true); // Show progress bar

        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            setInProgress(false); // Hide progress bar
            if (task.isSuccessful() && task.getResult() != null) {
                currentUserModel = task.getResult().toObject(UserModel.class);
                if (currentUserModel != null) {
                    // Populate UI fields
                    binding.profileUsername.setText(currentUserModel.username);
                    binding.profilePhone.setText(currentUserModel.phone);
                    AndroidUtil.setProfilePicFromBase64(getContext(), currentUserModel.profilePic, binding.profileImageView);
                }
            } else {
                Toast.makeText(getContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Toggles the visibility of the progress bar and update button.
     *
     * @param inProgress True to show progress, false to hide it.
     */
    private void setInProgress(boolean inProgress) {
        binding.profileProgressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        binding.profileUpdateBtn.setVisibility(inProgress ? View.GONE : View.VISIBLE);
    }

    /**
     * Cleans up resources to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Release binding resources
    }
}