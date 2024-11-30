package com.example.ezchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezchat.R;
import com.example.ezchat.databinding.ActivityNewChatRoomBinding;
import com.example.ezchat.databinding.ActivityNewChatRoomRecyclerItemBinding;
import com.example.ezchat.models.ChatroomModel;
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utilities.PreferenceManager;
import com.example.ezchat.utilities.Utilities;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for selecting users to start a chat room.
 */
public class NewChatRoomActivity extends AppCompatActivity {

    private ActivityNewChatRoomBinding binding;
    private FirebaseFirestore db;
    private UserAdapter userAdapter;
    private final List<UserModel> userList = new ArrayList<>();
    private final List<String> selectedPhones = new ArrayList<>(); // List of selected phone numbers
    private String currentUserPhone; // Current user's phone number

    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivityNewChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firestore, SharedPreferences, and RecyclerView
        db = FirebaseFirestore.getInstance();
        preferenceManager = PreferenceManager.getInstance(getApplicationContext());
        currentUserPhone = preferenceManager.getString(UserModel.FIELD_PHONE);

        if (currentUserPhone == null || currentUserPhone.isEmpty()) {
            Toast.makeText(this, "Failed to load current user.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userAdapter = new UserAdapter(userList);
        binding.usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.usersRecyclerView.setAdapter(userAdapter);

        // Fetch users to display in the list
        fetchUsers();

        // Set up Start Chat button
        binding.startChatButton.setOnClickListener(v -> {
            if (!selectedPhones.isEmpty()) {
                navigateToChatRoom();
            } else {
                Toast.makeText(this, "Please select at least one user.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up Back button
        binding.backButton.setOnClickListener(v -> onBackPressed());
    }

    /**
     * Fetches all users from Firestore and populates the RecyclerView, excluding the current user.
     */
    private void fetchUsers() {
        db.collection(UserModel.FIELD_COLLECTION_NAME)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    userList.clear(); // Clear the list before adding users
                    for (DocumentSnapshot document : querySnapshot) {
                        UserModel user = document.toObject(UserModel.class);
                        if (user != null && !user.phone.equals(currentUserPhone)) {
                            userList.add(user); // Add only other users
                        }
                    }
                    if (userList.isEmpty()) {
                        Toast.makeText(this, "No other users available to chat with.", Toast.LENGTH_SHORT).show();
                    }
                    userAdapter.notifyDataSetChanged(); // Notify the adapter of data changes
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch users: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Navigates to the ChatRoomActivity, passing a `ChatroomModel` object.
     */
    private void navigateToChatRoom() {
        // Add the current user's phone to the list of selected phones
        if (!selectedPhones.contains(currentUserPhone)) {
            selectedPhones.add(currentUserPhone);
        }

        // Create a ChatroomModel object locally
        ChatroomModel chatRoom = new ChatroomModel();
        chatRoom.phoneNumbers = new ArrayList<>(selectedPhones);
        chatRoom.creatorPhone = currentUserPhone;

        // Navigate to ChatRoomActivity with the ChatroomModel object
        Intent intent = new Intent(this, ChatRoomActivity.class);
        intent.putExtra("chatRoom", chatRoom); // Pass ChatroomModel as a Serializable object
        startActivity(intent);
        finish(); // Close this activity to avoid duplicates
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    /**
     * Adapter for displaying users in a RecyclerView for chat room creation.
     */
    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

        private final List<UserModel> userList;

        UserAdapter(List<UserModel> userList) {
            this.userList = userList;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new UserViewHolder(ActivityNewChatRoomRecyclerItemBinding.inflate(getLayoutInflater(), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            UserModel user = userList.get(position);
            holder.bind(user);
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }

        /**
         * ViewHolder for displaying a single user's details.
         */
        private class UserViewHolder extends RecyclerView.ViewHolder {

            private final ActivityNewChatRoomRecyclerItemBinding itemBinding;

            UserViewHolder(ActivityNewChatRoomRecyclerItemBinding binding) {
                super(binding.getRoot());
                this.itemBinding = binding;
            }

            void bind(UserModel user) {
                itemBinding.userNameText.setText(user.username);
                itemBinding.userPhoneText.setText(user.phone);

                // Set a placeholder or user profile picture
                if (user.profilePic != null && !user.profilePic.isEmpty()) {
                    itemBinding.userProfileImage.setImageBitmap(Utilities.getBitmapFromEncodedString(user.profilePic));
                } else {
                    itemBinding.userProfileImage.setImageResource(R.drawable.ic_person);
                }

                // Update the checkbox state
                itemBinding.userCheckBox.setChecked(selectedPhones.contains(user.phone));

                // Handle checkbox toggle
                itemBinding.userCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        if (!selectedPhones.contains(user.phone)) {
                            selectedPhones.add(user.phone); // Add to the list of selected phones
                        }
                    } else {
                        selectedPhones.remove(user.phone); // Remove from the list of selected phones
                    }

                    // Enable or disable the Start Chat button based on selection
                    binding.startChatButton.setEnabled(!selectedPhones.isEmpty());
                });
            }
        }
    }
}