package com.example.ezchat.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ezchat.R;
import com.example.ezchat.databinding.ActivityChatRoomBinding;
import com.example.ezchat.utilities.Constants;
import com.example.ezchat.utilities.FirebaseUtil;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChatRoomActivity extends AppCompatActivity {

    private String chatRoomId;
    private String currentUserId;
    private Button deleteChatButton;
    private FirebaseFirestore db;
    private ActivityChatRoomBinding binding; // View Binding

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // Set the root of the layout

        // Get the chat room ID from the intent
        chatRoomId = getIntent().getStringExtra(Constants.EXTRA_CHATROOM_ID);
        currentUserId = FirebaseUtil.currentUserId(); // Get the current user's phone number

        db = FirebaseFirestore.getInstance();

        // Check if the current user is the creator of the chat
        checkIfUserIsCreator();

        // Set up chat messages RecyclerView
        setupRecyclerView();

        // Delete chat room when delete button is clicked
        binding.deleteChatButton.setOnClickListener(v -> deleteChatRoom());

        // Back button to return to the previous screen
        binding.backButton.setOnClickListener(v -> onBackPressed());
    }

    private void checkIfUserIsCreator() {
        db.collection(Constants.KEY_COLLECTION_CHAT)
                .document(chatRoomId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String creatorId = documentSnapshot.getString(Constants.KEY_CREATOR_ID);
                        if (currentUserId.equals(creatorId)) {
                            // Show delete button if the current user is the creator
                            binding.deleteChatButton.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error (e.g., failed to get chat room data)
                    Toast.makeText(ChatRoomActivity.this, "Error loading chat room", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupRecyclerView() {
        // Set up RecyclerView to display chat messages (Use your existing implementation here)
    }

    private void deleteChatRoom() {
        // Delete the chat room from Firestore
        db.collection(Constants.KEY_COLLECTION_CHAT)
                .document(chatRoomId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Successfully deleted chat room, navigate back to previous screen
                    Toast.makeText(ChatRoomActivity.this, "Chat room deleted", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to the previous activity
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    Toast.makeText(ChatRoomActivity.this, "Error deleting chat room", Toast.LENGTH_SHORT).show();
                });
    }
}