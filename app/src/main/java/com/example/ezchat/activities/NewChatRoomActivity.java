package com.example.ezchat.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezchat.R;
import com.example.ezchat.databinding.ActivityNewChatMemberBinding;
import com.example.ezchat.databinding.ActivityNewChatRoomBinding;
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utilities.Constants;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for creating a new chat room by selecting users from a list.
 */
public class NewChatRoomActivity extends AppCompatActivity {

    private ActivityNewChatRoomBinding binding; // View Binding instance
    private FirebaseFirestore db; // Firestore database instance
    private final List<UserModel> userList = new ArrayList<>(); // List of all users
    private final List<String> selectedUsers = new ArrayList<>(); // List of selected user IDs
    private UserAdapter userAdapter; // RecyclerView Adapter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivityNewChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firestore and RecyclerView
        db = FirebaseFirestore.getInstance();
        userAdapter = new UserAdapter(userList, this::onUserSelectionChanged);
        binding.usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.usersRecyclerView.setAdapter(userAdapter);

        // Fetch users from Firestore
        fetchUsers();

        // Set up the Start Chat button
        binding.startChatButton.setOnClickListener(v -> {
            if (!selectedUsers.isEmpty()) {
                createChatRoom(selectedUsers);
            } else {
                Toast.makeText(this, "Please select at least one user", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the Back button
        binding.backButton.setOnClickListener(v -> onBackPressed());
    }

    /**
     * Fetches all users from Firestore and populates the RecyclerView.
     */
    private void fetchUsers() {
        db.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        UserModel user = document.toObject(UserModel.class);
                        if (user != null && !user.getPhone().equals(getCurrentUserId())) { // Exclude current user
                            userList.add(user);
                        }
                    }
                    userAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch users", Toast.LENGTH_SHORT).show());
    }

    /**
     * Creates a new chat room with the selected users and adds it to Firestore.
     *
     * @param selectedUsers List of selected user IDs.
     */
    private void createChatRoom(List<String> selectedUsers) {
        String chatRoomId = db.collection(Constants.KEY_COLLECTION_CHAT).document().getId(); // Generate unique ID
        String creatorId = getCurrentUserId(); // Current user ID from preferences

        // Create chat room data
        Map<String, Object> chatRoomData = new HashMap<>();
        chatRoomData.put(Constants.KEY_CHATROOM_ID, chatRoomId);
        chatRoomData.put(Constants.KEY_USER_IDS, selectedUsers);
        chatRoomData.put(Constants.KEY_CREATOR_ID, creatorId);
        chatRoomData.put(Constants.KEY_LAST_MESSAGE, ""); // Initially empty
        chatRoomData.put(Constants.KEY_LAST_MESSAGE_TIMESTAMP, System.currentTimeMillis());
        chatRoomData.put(Constants.KEY_LAST_MESSAGE_SENDER_ID, null);

        // Add chat room to Firestore
        db.collection(Constants.KEY_COLLECTION_CHAT)
                .document(chatRoomId)
                .set(chatRoomData)
                .addOnSuccessListener(aVoid -> {
                    Intent intent = new Intent(this, ChatRoomActivity.class);
                    intent.putExtra(Constants.KEY_CHATROOM_ID, chatRoomId);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to create chat room", Toast.LENGTH_SHORT).show());
    }

    /**
     * Retrieves the current user's ID from SharedPreferences.
     *
     * @return The current user's ID, or an empty string if not found.
     */
    private String getCurrentUserId() {
        return getSharedPreferences(Constants.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE)
                .getString(Constants.KEY_USER_ID, "");
    }

    /**
     * Callback method triggered when user selection changes in the adapter.
     * Enables or disables the Start Chat button based on the selection state.
     *
     * @param isSelected Whether the user is selected or deselected.
     * @param userPhone  The phone number of the user being toggled.
     */
    private void onUserSelectionChanged(boolean isSelected, String userPhone) {
        if (isSelected) {
            selectedUsers.add(userPhone);
        } else {
            selectedUsers.remove(userPhone);
        }
        binding.startChatButton.setEnabled(!selectedUsers.isEmpty());
    }

    // Clean up binding resources to prevent memory leaks
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    /**
     * Adapter for displaying users in a RecyclerView for chat room creation.
     */
    public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

        private final List<UserModel> userList;
        private final UserSelectionListener selectionListener;

        /**
         * Constructor for UserAdapter.
         *
         * @param userList         List of users to display.
         * @param selectionListener Callback for handling user selection changes.
         */
        public UserAdapter(List<UserModel> userList, UserSelectionListener selectionListener) {
            this.userList = userList;
            this.selectionListener = selectionListener;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ActivityNewChatMemberBinding itemBinding = ActivityNewChatMemberBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new UserViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            holder.bind(userList.get(position));
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }

        /**
         * ViewHolder class for displaying individual user items.
         */
        public class UserViewHolder extends RecyclerView.ViewHolder {

            private final ActivityNewChatMemberBinding binding;

            /**
             * Constructor for UserViewHolder.
             *
             * @param binding The binding for the chat member item layout.
             */
            public UserViewHolder(@NonNull ActivityNewChatMemberBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            /**
             * Binds a user's data to the ViewHolder and manages selection.
             *
             * @param user The user to display.
             */
            public void bind(UserModel user) {
                binding.userNameText.setText(user.getUsername());
                binding.userPhoneText.setText(user.getPhone());
                binding.userProfileImage.setImageResource(R.drawable.ic_person); // Placeholder image

                // Handle checkbox state changes
                binding.userCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                        selectionListener.onUserSelectionChanged(isChecked, user.getPhone()));
            }
        }
    }

    /**
     * Functional interface for handling user selection changes.
     */
    @FunctionalInterface
    public interface UserSelectionListener {
        void onUserSelectionChanged(boolean isSelected, String userPhone);
    }
}