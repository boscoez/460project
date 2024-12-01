package com.example.ezchat.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezchat.databinding.ActivityChatBinding;
import com.example.ezchat.databinding.ActivityChatRecyclerItemBinding;
import com.example.ezchat.models.ChatModel;
import com.example.ezchat.models.MessageModel;
import com.example.ezchat.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private String chatId; // Unique ID for the chat
    private String currentUserPhone;
    private List<MessageModel> messageList;
    private MessageAdapter messageAdapter;
    private boolean isChatCreated = false; // Flag to check if the chat is created
    private FirebaseFirestore db;
    private ListenerRegistration chatListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize view binding
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firestore and shared preference manager
        db = FirebaseFirestore.getInstance();
        currentUserPhone = getIntent().getStringExtra(Constants.FIELD_PHONE); // Get the current user's phone from intent

        // Validate current user phone number
        if (currentUserPhone == null) {
            Toast.makeText(this, "Unable to fetch current user details.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Retrieve chatId or phone numbers passed via intent
        chatId = getIntent().getStringExtra(Constants.FIELD_CHAT_ID);
        List<String> selectedPhones = getIntent().getStringArrayListExtra(Constants.FIELD_PHONE_NUMBERS);

        // If chatId exists, load the existing chat, else start a new chat
        if (chatId != null) {
            loadChat(chatId);
        } else if (selectedPhones != null && selectedPhones.size() >= 2) {
            startNewChat(selectedPhones); // Will be triggered when the user sends the first message
        } else {
            Toast.makeText(this, "Invalid chat details.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI components and listeners
        setupRecyclerView();
        setupButtonListeners();
    }

    /**
     * Loads an existing chat from Firestore using chatId.
     */
    private void loadChat(String chatId) {
        db.collection(Constants.CHAT_COLLECTION).document(chatId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(ChatActivity.this, "Error loading chat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        ChatModel chat = documentSnapshot.toObject(ChatModel.class);
                        if (chat != null) {
                            messageList.clear();
                            messageList.addAll(chat.messages);
                            messageAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    /**
     * Starts a new chat by adding the current user and the selected users to the chat.
     * Waits for the user to send the first message before creating the chat.
     */
    private void startNewChat(List<String> selectedPhones) {
        chatId = db.collection(Constants.CHAT_COLLECTION).document().getId();  // Generate a new chat ID
        List<String> phoneNumbers = new ArrayList<>(selectedPhones);
        phoneNumbers.add(currentUserPhone); // Add current user to the chat

        // Set up the empty message waiting for user input
        binding.messageSendButton.setOnClickListener(v -> {
            String messageText = binding.inputMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(messageText)) {
                sendMessage(messageText, phoneNumbers); // Send first message when user clicks send
            } else {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sends the first message and creates the chat document in Firestore.
     *
     * @param messageText The text of the message to be sent.
     * @param phoneNumbers The list of phone numbers of participants.
     */
    private void sendMessage(String messageText, List<String> phoneNumbers) {
        // Create the first message
        MessageModel message = new MessageModel(currentUserPhone, phoneNumbers, messageText);

        // Create the chat in Firestore
        ChatModel chat = new ChatModel(chatId, phoneNumbers, currentUserPhone, message);
        db.collection(Constants.CHAT_COLLECTION).document(chatId).set(chat)
                .addOnSuccessListener(aVoid -> {
                    isChatCreated = true; // Set flag that the chat has been created
                    addMessageToChat(message); // Send the first message
                })
                .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Failed to create chat.", Toast.LENGTH_SHORT).show());
    }

    /**
     * Adds a message to the chat and updates the local UI with the new message.
     *
     * @param message The message to be added.
     */
    private void addMessageToChat(MessageModel message) {
        db.collection(Constants.CHAT_COLLECTION).document(chatId)
                .collection(Constants.MESSAGE_COLLECTION).add(message)
                .addOnSuccessListener(documentReference -> {
                    messageList.add(message);
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    binding.chatRecyclerView.scrollToPosition(messageList.size() - 1);
                })
                .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Failed to send message.", Toast.LENGTH_SHORT).show());
    }

    /**
     * Configures the RecyclerView to display chat messages.
     */
    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList, currentUserPhone, true);
        binding.chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.chatRecyclerView.setAdapter(messageAdapter);
    }

    /**
     * Sets up click listeners for UI elements, such as the back button and the send message button.
     */
    private void setupButtonListeners() {
        // Handle back button click
        binding.btnBack.setOnClickListener(v -> onBackPressed());

        // Handle send message button click
        binding.messageSendButton.setOnClickListener(v -> {
            String messageText = binding.inputMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(messageText)) {
                sendMessage(messageText, new ArrayList<>()); // Will be initialized once users are added to the chat
                binding.inputMessage.setText(""); // Clear the input field
            } else {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Adapter for managing and displaying chat messages in the RecyclerView.
     */
    static class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

        private final Context context;
        private final List<MessageModel> messages;
        private final String currentUserPhone;
        private final boolean isTwoUsers;

        public MessageAdapter(Context context, List<MessageModel> messages, String currentUserPhone, boolean isTwoUsers) {
            this.context = context;
            this.messages = messages;
            this.currentUserPhone = currentUserPhone;
            this.isTwoUsers = isTwoUsers;
        }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ActivityChatRecyclerItemBinding binding = ActivityChatRecyclerItemBinding.inflate(LayoutInflater.from(context), parent, false);
            return new MessageViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            MessageModel message = messages.get(position);
            boolean isSentByCurrentUser = message.getSenderPhone().equals(currentUserPhone);

            if (isSentByCurrentUser) {
                holder.bindSentMessage(message);
            } else {
                holder.bindReceivedMessage(message);
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        class MessageViewHolder extends RecyclerView.ViewHolder {

            private final ActivityChatRecyclerItemBinding binding;

            public MessageViewHolder(@NonNull ActivityChatRecyclerItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            void bindSentMessage(MessageModel message) {
                binding.messageLayoutLeft.setVisibility(View.GONE);
                binding.messageLayoutRight.setVisibility(View.VISIBLE);
                binding.messageTextRight.setText(message.getMessage());
                binding.timestampTextRight.setText(formatTimestamp(message.getTimestamp()));
            }

            void bindReceivedMessage(MessageModel message) {
                binding.messageLayoutRight.setVisibility(View.GONE);
                binding.messageLayoutLeft.setVisibility(View.VISIBLE);
                binding.messageTextLeft.setText(message.getMessage());
                binding.timestampTextLeft.setText(formatTimestamp(message.getTimestamp()));
            }

            private String formatTimestamp(Date timestamp) {
                if (timestamp == null) return "";
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
                return sdf.format(timestamp);
            }
        }
    }
}