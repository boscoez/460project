package com.example.ezchat.activities;

import android.content.Context;
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
import com.example.ezchat.utilities.Utilities;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for creating a new chat room by selecting users from a list.
 */
public class NewChatRoomActivity extends AppCompatActivity {

    private ActivityNewChatRoomBinding binding;
    private FirebaseFirestore db;
    private UserAdapter userAdapter;
    private final List<UserModel> userList = new ArrayList<>();
    private final List<String> selectedPhones = new ArrayList<>(); // List of selected user phone numbers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivityNewChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firestore and RecyclerView
        db = FirebaseFirestore.getInstance();
        userAdapter = new UserAdapter(userList);
        binding.usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.usersRecyclerView.setAdapter(userAdapter);

        // Fetch users to display in the list
        fetchUsers();

        // Set up Start Chat button
        binding.startChatButton.setOnClickListener(v -> {
            if (!selectedPhones.isEmpty()) {
                createChatRoom();
            } else {
                Toast.makeText(this, "Please select at least one user", Toast.LENGTH_SHORT).show();
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
                    userList.clear();
                    String currentUserPhone = getCurrentUserPhone();
                    for (DocumentSnapshot document : querySnapshot) {
                        UserModel user = document.toObject(UserModel.class);
                        if (user != null && !user.phone.equals(currentUserPhone)) {
                            userList.add(user);
                        }
                    }
                    if (userList.isEmpty()) {
                        Toast.makeText(this, "No other users available", Toast.LENGTH_SHORT).show();
                    }
                    userAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch users: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Creates a new chat room and navigates to the chat room activity.
     */
    private void createChatRoom() {
        String chatRoomId = generateChatRoomId(); // Generate a unique chat room ID
        String currentUserPhone = getCurrentUserPhone();

        // Add the creator's phone number to the selected phones list
        selectedPhones.add(currentUserPhone);

        ChatroomModel chatRoom = new ChatroomModel();
        chatRoom.chatroomId = chatRoomId;
        chatRoom.phoneNumbers = new ArrayList<>(selectedPhones); // Use selected phones
        chatRoom.creatorPhone = currentUserPhone;

        // Create the chat room in Firestore
        db.collection(ChatroomModel.FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .set(chatRoom)
                .addOnSuccessListener(aVoid -> {
                    // Navigate to ChatRoomActivity and pass the selected numbers
                    navigateToChatRoom(chatRoomId, selectedPhones);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to create chat room", Toast.LENGTH_SHORT).show());
    }

    /**
     * Navigates to the ChatRoomActivity after successfully creating a chat room.
     *
     * @param chatRoomId     The ID of the created chat room.
     * @param selectedPhones The list of phone numbers in the chat room.
     */
    private void navigateToChatRoom(String chatRoomId, List<String> selectedPhones) {
        Intent intent = new Intent(this, ChatRoomActivity.class);
        intent.putExtra(ChatroomModel.FIELD_CHATROOM_ID, chatRoomId);

        // Pass the first selected phone (assume one-on-one chat)
        if (selectedPhones.size() > 1) {
            // Exclude the current user's phone and pass only the other user's phone
            String otherUserPhone = selectedPhones.stream()
                    .filter(phone -> !phone.equals(getCurrentUserPhone()))
                    .findFirst()
                    .orElse(null);

            intent.putExtra("otherUserPhone", otherUserPhone);
        }

        startActivity(intent);
        finish(); // Close this activity to avoid duplicates
    }

    /**
     * Generates a unique chat room ID.
     *
     * @return A unique chat room ID.
     */
    private String generateChatRoomId() {
        return db.collection(ChatroomModel.FIELD_COLLECTION_NAME).document().getId();
    }

    /**
     * Retrieves the current user's phone number from SharedPreferences.
     *
     * @return The current user's phone number.
     */
    private String getCurrentUserPhone() {
        return getSharedPreferences("APP_PREFS", MODE_PRIVATE).getString(UserModel.FIELD_PHONE, "");
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

                // Checkbox to select/deselect the user
                itemBinding.userCheckBox.setChecked(selectedPhones.contains(user.phone));
                itemBinding.userCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedPhones.add(user.phone);
                    } else {
                        selectedPhones.remove(user.phone);
                    }
                    binding.startChatButton.setEnabled(!selectedPhones.isEmpty());
                });
            }
        }
    }
}