package com.example.ezchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ezchat.R;
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utilities.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Objects;

/**55
 * Activity for setting up or updating the user's username after phone authentication.
 */
public class LoginUserNameActivity extends AppCompatActivity {
    private EditText usernameInput;
    private Button letMeInBtn;
    private ProgressBar progressBar;
    private String phoneNumber;
    private UserModel userModel;

    /**
     * Initializes the username setup activity, sets up UI components, and retrieves any existing username.
     *
     * @param savedInstanceState The saved instance state for the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase before using any Firebase services
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_login_user_name);

        // Initialize UI components
        usernameInput = findViewById(R.id.login_username);
        letMeInBtn = findViewById(R.id.login_code_btn);
        progressBar = findViewById(R.id.login_progress_bar);

        // Safely retrieve the phone number from the intent
        if (getIntent() != null && getIntent().getExtras() != null) {
            phoneNumber = getIntent().getExtras().getString("phone", "");
        } else {
            phoneNumber = "";
        }

        // Fetch existing user data
        getUsername();

        // Set listener for the "Let Me In" button
        letMeInBtn.setOnClickListener(v -> setUsername());
    }

    /**
     * Validates and sets the username for the user. If a UserModel doesn't already exist, it creates one,
     * saves it in Firebase, and navigates to the MainActivity on success.
     */
    private void setUsername() {
        String username = usernameInput.getText().toString().trim();

        // Validate username
        if (username.isEmpty() || username.length() < 3) {
            usernameInput.setError("Username length must be 3+ characters.");
            return;
        }

        setInProgress(true);

        if (userModel != null) {
            // Existing user: Update username
            userModel.setUsername(username);
            updateUserModel(userModel);
        } else {
            // New user: Create UserModel
            fetchFcmTokenAndCreateUser(username);
        }
    }

    /**
     * Fetches the FCM token and creates a new UserModel.
     *
     * @param username The username to set for the new user.
     */
    private void fetchFcmTokenAndCreateUser(String username) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String fcmToken = task.getResult();
                        // Initialize profilePic as empty or a default Base64 string
                        String profilePic = ""; // You can set a default Base64 string if desired
                        // Initialize chatRooms as an empty list
                        ArrayList<String> chatRooms = new ArrayList<>();

                        // Create a new UserModel
                        userModel = new UserModel(
                                phoneNumber,
                                username,
                                Timestamp.now(),
                                FirebaseUtil.currentUserId(),
                                fcmToken,
                                profilePic,
                                chatRooms
                        );

                        // Save the new user to Firestore
                        updateUserModel(userModel);
                    } else {
                        setInProgress(false);
                        Toast.makeText(LoginUserNameActivity.this,
                                "Failed to retrieve FCM token.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Updates the UserModel in Firestore.
     *
     * @param userModel The UserModel to update.
     */
    private void updateUserModel(UserModel userModel) {
        FirebaseUtil.currentUserDetails().set(userModel)
                .addOnCompleteListener(task -> {
                    setInProgress(false);
                    if (task.isSuccessful()) {
                        // Navigate to MainActivity upon successful update
                        Intent intent = new Intent(LoginUserNameActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        Toast.makeText(LoginUserNameActivity.this,
                                "Welcome, " + userModel.getUsername() + "!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginUserNameActivity.this,
                                "Failed to save user data: " + Objects.requireNonNull(task.getException()).getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Retrieves the existing username from Firebase Firestore if it exists and populates the input field.
     */
    private void getUsername() {
        setInProgress(true);
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            setInProgress(false);
            if (task.isSuccessful()) {
                UserModel fetchedUserModel = task.getResult().toObject(UserModel.class);
                if (fetchedUserModel != null) {
                    this.userModel = fetchedUserModel; // Assign to class member
                    usernameInput.setText(fetchedUserModel.getUsername());
                } else {
                    // UserModel does not exist; likely a new user
                    this.userModel = null;
                }
            } else {
                Toast.makeText(LoginUserNameActivity.this,
                        "Failed to fetch user data: " + Objects.requireNonNull(task.getException()).getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Shows or hides the progress bar and "Let Me In" button based on the inProgress parameter.
     *
     * @param inProgress Boolean indicating if the operation is in progress.
     */
    private void setInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            letMeInBtn.setVisibility(View.GONE);
            usernameInput.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            letMeInBtn.setVisibility(View.VISIBLE);
            usernameInput.setEnabled(true);
        }
    }
}