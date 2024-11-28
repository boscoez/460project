package com.example.ezchat.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ezchat.databinding.ActivityChatRoomBinding;
import com.example.ezchat.models.ChatroomModel;
import com.example.ezchat.models.MessageModel;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * ChatRoomActivity manages the chat interface, allowing users to send and view messages
 * within a chat room. It supports message updates in real-time using Firebase Firestore.
 */
public class ChatRoomActivity extends AppCompatActivity {

    private ActivityChatRoomBinding binding;
    private String chatRoomId; // ID of the chat room
    private String currentUserId; // ID of the current user
    private FirebaseFirestore db; // Firestore instance
    private List<MessageModel> messageList; // List of messages in the chat
    private MessageModel.MessageAdapter messageAdapter; // Adapter for the message RecyclerView
    private boolean isCreator; // Indicates if the current user is the creator of the chat room

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firestore and user/chat data
        db = FirebaseFirestore.getInstance();
        currentUserId = getCurrentUserId();
        chatRoomId = getIntent().getStringExtra(ChatroomModel.FIELD_CHATROOM_ID);

        // Initialize the RecyclerView
        setupRecyclerView();

        // Load chat room details and set up UI
        loadChatRoomDetails();

        // Set up button listeners
        setupButtonListeners();
    }

    /**
     * Sets up the RecyclerView to display chat messages.
     */
    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        messageAdapter = new MessageModel.MessageAdapter(this, messageList, currentUserId);
        binding.chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.chatRecyclerView.setAdapter(messageAdapter);
        listenForMessages();
    }

    /**
     * Loads chat room details to determine if the user is the creator.
     */
    private void loadChatRoomDetails() {
        db.collection(ChatroomModel.FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ChatroomModel chatRoom = documentSnapshot.toObject(ChatroomModel.class);
                        if (chatRoom != null) {
                            isCreator = chatRoom.creatorId.equals(currentUserId);
                            binding.deleteChatButton.setVisibility(isCreator ? View.VISIBLE : View.GONE);
                        }
                    } else {
                        Toast.makeText(this, "Chat room not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading chat room", Toast.LENGTH_SHORT).show());
    }

    /**
     * Listens for new messages in the chat room in real time.
     */
    private void listenForMessages() {
        db.collection(ChatroomModel.FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .collection(MessageModel.FIELD_COLLECTION_NAME)
                .orderBy(MessageModel.FIELD_TIMESTAMP, Query.Direction.ASCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error fetching messages", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (querySnapshot != null) {
                        for (DocumentChange change : querySnapshot.getDocumentChanges()) {
                            if (change.getType() == DocumentChange.Type.ADDED) {
                                MessageModel message = change.getDocument().toObject(MessageModel.class);
                                messageList.add(message);
                                messageAdapter.notifyItemInserted(messageList.size() - 1);
                                binding.chatRecyclerView.scrollToPosition(messageList.size() - 1);
                            }
                        }
                    }
                });
    }

    /**
     * Sets up listeners for the back button, send message button, and delete button.
     */
    private void setupButtonListeners() {
        binding.backButton.setOnClickListener(v -> onBackPressed());

        binding.messageSendButton.setOnClickListener(v -> {
            String messageText = binding.inputMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(messageText)) {
                sendMessage(messageText);
                binding.inputMessage.setText("");
            }
        });

        binding.deleteChatButton.setOnClickListener(v -> deleteChatRoom());
    }

    /**
     * Sends a message to the chat room.
     *
     * @param messageText The text of the message to send.
     */
    private void sendMessage(String messageText) {
        MessageModel message = new MessageModel(
                currentUserId,
                null, // Receiver ID is null for group messages
                messageText,
                com.google.firebase.Timestamp.now(),
                MessageModel.FIELD_STATUS
        );

        db.collection(ChatroomModel.FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .collection(MessageModel.FIELD_COLLECTION_NAME)
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    // Update the last message and timestamp in the chat room
                    db.collection(ChatroomModel.FIELD_COLLECTION_NAME)
                            .document(chatRoomId)
                            .update(
                                    ChatroomModel.FIELD_LAST_MESSAGE, message.message,
                                    ChatroomModel.FIELD_LAST_MESSAGE_TIMESTAMP, message.timestamp,
                                    ChatroomModel.FIELD_LAST_MESSAGE_SENDER_ID, message.senderId
                            );
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show());
    }

    /**
     * Deletes the chat room if the user is the creator.
     */
    private void deleteChatRoom() {
        db.collection(ChatroomModel.FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Chat room deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete chat room", Toast.LENGTH_SHORT).show());
    }

    /**
     * Retrieves the current user ID (e.g., from shared preferences or authentication).
     *
     * @return The current user's ID.
     */
    private String getCurrentUserId() {
        // Implement your logic to retrieve the current user's ID
        return "currentUserId"; // Placeholder value
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}