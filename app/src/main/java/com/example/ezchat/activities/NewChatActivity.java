package com.example.ezchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezchat.R;
import com.example.ezchat.activities.ChatRoomActivity;
import com.example.ezchat.databinding.ActivityNewChatBinding;
import com.example.ezchat.databinding.ActivityNewChatRecyclerItemBinding;
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewChatActivity extends AppCompatActivity {

    private ActivityNewChatBinding binding;
    private FirebaseFirestore db;
    private List<UserModel> userList;
    private List<String> selectedUsers;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();
        selectedUsers = new ArrayList<>();

        // Set up RecyclerView
        userAdapter = new UserAdapter(userList);
        binding.usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.usersRecyclerView.setAdapter(userAdapter);

        // Fetch users from Firestore
        fetchUsers();

        // Set up the start chat button
        binding.startChatButton.setOnClickListener(v -> {
            if (!selectedUsers.isEmpty()) {
                createChatRoom(selectedUsers);
            } else {
                Toast.makeText(NewChatActivity.this, "Please select at least one user", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the back button
        binding.backButton.setOnClickListener(v -> onBackPressed());
    }

    /**
     * Fetch users from Firestore.
     */
    private void fetchUsers() {
        db.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        UserModel user = document.toObject(UserModel.class);
                        if (user != null) {
                            userList.add(user);
                        }
                    }
                    userAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(NewChatActivity.this, "Failed to fetch users", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Create a new chat room with the selected users.
     * @param selectedUsers List of selected user phone numbers.
     */
    private void createChatRoom(List<String> selectedUsers) {
        String chatRoomId = db.collection(Constants.KEY_COLLECTION_CHAT).document().getId(); // Generate new chat room ID

        // Retrieve the ID of the user creating the room
        String creatorId = "getCurrentUserId()"; // Replace with your method to get the current user ID

        // Create a chat room data structure with all fields populated
        Map<String, Object> chatRoomData = new HashMap<>();
        chatRoomData.put(Constants.KEY_CHATROOM_ID, chatRoomId);
        chatRoomData.put(Constants.KEY_USER_IDS, selectedUsers);
        chatRoomData.put(Constants.KEY_CREATOR_ID, creatorId);
        chatRoomData.put(Constants.KEY_LAST_MESSAGE, ""); // Empty message initially
        chatRoomData.put(Constants.KEY_LAST_MESSAGE_TIMESTAMP, System.currentTimeMillis());
        chatRoomData.put(Constants.KEY_LAST_MESSAGE_SENDER_ID, null); // No sender yet

        // Add the chat room to Firestore
        db.collection(Constants.KEY_COLLECTION_CHAT)
                .document(chatRoomId)
                .set(chatRoomData)
                .addOnSuccessListener(aVoid -> {
                    // Chat room created successfully, navigate to the chat room
                    Intent intent = new Intent(NewChatActivity.this, ChatRoomActivity.class);
                    intent.putExtra(Constants.KEY_CHATROOM_ID, chatRoomId);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(NewChatActivity.this, "Failed to create chat room", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * User Adapter for the RecyclerView to display users.
     */
    public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

        private final List<UserModel> userList;

        public UserAdapter(List<UserModel> userList) {
            this.userList = userList;
        }

        @Override
        public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_new_chat_recycler_item, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(UserViewHolder holder, int position) {
            holder.bind(userList.get(position));
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }

        public class UserViewHolder extends RecyclerView.ViewHolder {

            private final CheckBox userCheckBox;
            private final TextView userNameTextView;
            private final TextView userPhoneTextView;
            private final ImageView userProfileImageView;

            public UserViewHolder(View itemView) {
                super(itemView);
                userCheckBox = itemView.findViewById(R.id.userCheckBox);
                userNameTextView = itemView.findViewById(R.id.userNameText);
                userPhoneTextView = itemView.findViewById(R.id.userPhoneText);
                userProfileImageView = itemView.findViewById(R.id.userProfileImage);

                // Handle user selection for chat room
                userCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedUsers.add(userPhoneTextView.getText().toString());
                    } else {
                        selectedUsers.remove(userPhoneTextView.getText().toString());
                    }

                    // Enable or disable start chat button based on user selection
                    binding.startChatButton.setEnabled(!selectedUsers.isEmpty());
                });
            }

            public void bind(UserModel user) {
                userNameTextView.setText(user.getUsername());
                userPhoneTextView.setText(user.getPhone());
                // Set profile image (if available)
                userProfileImageView.setImageResource(R.drawable.ic_person); // Placeholder for now
            }
        }
    }
}