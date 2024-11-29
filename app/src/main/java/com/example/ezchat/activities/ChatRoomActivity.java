package com.example.ezchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ezchat.databinding.ActivityChatRoomBinding;
import com.example.ezchat.models.ChatroomModel;
import com.example.ezchat.models.MessageModel;
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ChatRoomActivity manages the chat interface, allowing users to send and view messages
 * within a chat room. The chat room is created in Firestore when the first message is sent.
 */
public class ChatRoomActivity extends AppCompatActivity {

    private ActivityChatRoomBinding binding;
    private String chatRoomId; // The ID of the chat room
    private String currentUserPhone; // The phone number of the current user
    private String otherUserPhone; // The phone number of the other participant
    private FirebaseFirestore db; // Firestore instance
    private List<MessageModel> messageList; // List of messages in the chat
    private MessageModel.MessageAdapter messageAdapter; // Adapter for RecyclerView
    private boolean isChatRoomCreated; // Tracks if the chat room has been created
    private PreferenceManager preferenceManager; // For accessing shared preferences

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firestore and PreferenceManager
        db = FirebaseFirestore.getInstance();
        preferenceManager = PreferenceManager.getInstance(getApplicationContext());

        // Retrieve current user's phone and other user's phone from intent
        currentUserPhone = preferenceManager.getString(UserModel.FIELD_PHONE);
        otherUserPhone = getIntent().getStringExtra("otherUserPhone");
        chatRoomId = getIntent().getStringExtra(ChatroomModel.FIELD_CHATROOM_ID);

        if (currentUserPhone == null || otherUserPhone == null) {
            Toast.makeText(this, "Invalid user or chat details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize RecyclerView
        setupRecyclerView();

        // Listen for real-time updates if a chat room already exists
        if (!TextUtils.isEmpty(chatRoomId)) {
            listenForMessages();
            isChatRoomCreated = true;
        }

        // Set up button listeners
        setupButtonListeners();
    }

    /**
     * Sets up the RecyclerView to display chat messages.
     */
    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        messageAdapter = new MessageModel.MessageAdapter(this, messageList, currentUserPhone);
        binding.chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.chatRecyclerView.setAdapter(messageAdapter);
    }

    /**
     * Sets up listeners for the send message button.
     */
    private void setupButtonListeners() {
        binding.backButton.setOnClickListener(v -> onBackPressed());

        binding.messageSendButton.setOnClickListener(v -> {
            String messageText = binding.inputMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(messageText)) {
                sendMessage(messageText);
                binding.inputMessage.setText("");
            } else {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sends a message and creates the chat room if it doesn't exist.
     *
     * @param messageText The text of the message to send.
     */
    private void sendMessage(String messageText) {
        if (!isChatRoomCreated) {
            // Create a chat room when the first message is sent
            createChatRoom(messageText);
        } else {
            // Add the message to the existing chat room
            addMessageToChatRoom(messageText);
        }
    }

    /**
     * Creates a new chat room and sends the first message.
     *
     * @param messageText The first message text.
     */
    private void createChatRoom(String messageText) {
        // Generate a new chat room ID
        chatRoomId = db.collection(ChatroomModel.FIELD_COLLECTION_NAME).document().getId();

        ChatroomModel chatRoom = new ChatroomModel();
        chatRoom.chatroomId = chatRoomId;
        chatRoom.phoneNumbers = Arrays.asList(currentUserPhone, otherUserPhone);
        chatRoom.creatorPhone = currentUserPhone;

        db.collection(ChatroomModel.FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .set(chatRoom)
                .addOnSuccessListener(aVoid -> {
                    isChatRoomCreated = true; // Mark the chat room as created
                    listenForMessages(); // Start listening for messages
                    addMessageToChatRoom(messageText); // Send the first message
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to create chat room", Toast.LENGTH_SHORT).show());
    }

    /**
     * Adds a message to the chat room.
     *
     * @param messageText The message text to add.
     */
    private void addMessageToChatRoom(String messageText) {
        if (chatRoomId == null) {
            Toast.makeText(this, "Error: Chat room not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        MessageModel message = new MessageModel(
                currentUserPhone,
                otherUserPhone,
                messageText,
                com.google.firebase.Timestamp.now(),
                "sent"
        );

        db.collection(ChatroomModel.FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .collection(MessageModel.FIELD_COLLECTION_NAME)
                .add(message)
                .addOnSuccessListener(aVoid -> {
                    // Update chat room metadata with the last message
                    updateChatRoomMetadata(message);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show());
    }

    /**
     * Updates the chat room metadata with the last message details.
     *
     * @param message The message to use for metadata update.
     */
    private void updateChatRoomMetadata(MessageModel message) {
        db.collection(ChatroomModel.FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .update(
                        ChatroomModel.FIELD_LAST_MESSAGE, message.message,
                        ChatroomModel.FIELD_LAST_MESSAGE_TIMESTAMP, message.timestamp,
                        ChatroomModel.FIELD_LAST_MESSAGE_SENDER_PHONE, message.senderPhone
                )
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update chat room metadata", Toast.LENGTH_SHORT).show());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}