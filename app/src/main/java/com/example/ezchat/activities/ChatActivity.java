package com.example.ezchat.activities;

import static com.example.ezchat.utilities.Utilities.formatTimestamp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezchat.R;
import com.example.ezchat.databinding.ActivityChatBinding;
import com.example.ezchat.databinding.ActivityChatRecyclerItemBinding;
import com.example.ezchat.models.ChatModel;
import com.example.ezchat.models.MessageModel;
import com.example.ezchat.utilities.Constants;
import com.example.ezchat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for managing and displaying chat messages.
 */
public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    // View binding instance
    private ActivityChatBinding binding;

    // Chat and user details
    private String chatId;
    private ChatModel chatModel;
    private String currentUserPhone;
    private boolean isNewChat = false;

    // Firebase and preferences
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;

    // Adapter and message list
    private ChatAdapter chatAdapter;
    private final List<MessageModel> messages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize view binding
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeDependencies();
        handleIntent();
        initializeRecyclerView();
        setupListeners();

        if (isNewChat) {
            showWaitingForMessage();
        } else {
            loadMessages();
        }
    }

    /**
     * Initialize Firebase and PreferenceManager instances.
     */
    private void initializeDependencies() {
        preferenceManager = PreferenceManager.getInstance(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        currentUserPhone = preferenceManager.get(Constants.FIELD_PHONE, "");
    }

    /**
     * Extracts chat information from the Intent.
     */
    private void handleIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(Constants.FIELD_CHAT_ID)) {
            chatId = intent.getStringExtra(Constants.FIELD_CHAT_ID);
            isNewChat = false;
        } else if (intent.hasExtra(Constants.MODEL_CHAT)) {
            chatModel = (ChatModel) intent.getSerializableExtra(Constants.MODEL_CHAT);
            isNewChat = true;
        } else {
            Log.e(TAG, "No valid data passed to ChatActivity.");
            finish();
        }
    }

    /**
     * Setup RecyclerView with the chat adapter.
     */
    private void initializeRecyclerView() {
        chatAdapter = new ChatAdapter(messages);
        binding.chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.chatRecyclerView.setAdapter(chatAdapter);
    }

    /**
     * Setup event listeners for the UI components.
     */
    private void setupListeners() {
        // Send message button
        binding.messageSendButton.setOnClickListener(v -> {
            String messageContent = binding.inputMessage.getText().toString().trim();
            if (messageContent.isEmpty()) {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.inputMessage.setText("");

            if (isNewChat) {
                createNewChat(messageContent);
            } else {
                sendMessage(messageContent);
            }
        });

        // Back button
        binding.btnBack.setOnClickListener(v -> onBackPressed());
    }

    /**
     * Display waiting UI for the first message in a new chat.
     */
    private void showWaitingForMessage() {
        binding.messageSendButton.setEnabled(true);
        binding.chatLoadingProgressBar.setVisibility(View.GONE);
    }

    /**
     * Creates a new chat and sends the first message.
     *
     * @param firstMessage The first message to send.
     */
    private void createNewChat(String firstMessage) {
        binding.chatLoadingProgressBar.setVisibility(View.VISIBLE);

        chatId = database.collection(Constants.COLLECTION_CHATS).document().getId();
        chatModel.chatId = chatId;

        MessageModel message = new MessageModel(currentUserPhone, firstMessage);
        chatModel.lastMessage = message;

        database.collection(Constants.COLLECTION_CHATS)
                .document(chatId)
                .set(chatModel)
                .addOnSuccessListener(aVoid -> {
                    database.collection(Constants.COLLECTION_CHATS)
                            .document(chatId)
                            .collection(Constants.COLLECTION_MESSAGES)
                            .add(message)
                            .addOnSuccessListener(documentReference -> enableRealTimeUpdates())
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to add first message", e);
                                Toast.makeText(this, "Failed to send the first message.", Toast.LENGTH_SHORT).show();
                                binding.chatLoadingProgressBar.setVisibility(View.GONE);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create chat", e);
                    Toast.makeText(this, "Failed to start chat.", Toast.LENGTH_SHORT).show();
                    binding.chatLoadingProgressBar.setVisibility(View.GONE);
                });
    }

    /**
     * Sends a message in the current chat.
     *
     * @param content The message content.
     */
    private void sendMessage(String content) {
        binding.chatLoadingProgressBar.setVisibility(View.VISIBLE);

        MessageModel message = new MessageModel(currentUserPhone, content);

        database.collection(Constants.COLLECTION_CHATS)
                .document(chatId)
                .collection(Constants.COLLECTION_MESSAGES)
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    database.collection(Constants.COLLECTION_CHATS)
                            .document(chatId)
                            .update(Constants.FIELD_LAST_MESSAGE, message);
                    binding.chatLoadingProgressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to send message", e);
                    Toast.makeText(this, "Failed to send message.", Toast.LENGTH_SHORT).show();
                    binding.chatLoadingProgressBar.setVisibility(View.GONE);
                });
    }

    /**
     * Loads messages from Firestore with real-time updates.
     */
    private void loadMessages() {
        enableRealTimeUpdates();
    }

    /**
     * Enable real-time updates for messages in the chat.
     */
    private void enableRealTimeUpdates() {
        database.collection(Constants.COLLECTION_CHATS)
                .document(chatId)
                .collection(Constants.COLLECTION_MESSAGES)
                .orderBy(Constants.FIELD_TIMESTAMP, Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error fetching real-time updates", error);
                        return;
                    }

                    if (value != null) {
                        for (DocumentChange change : value.getDocumentChanges()) {
                            if (change.getType() == DocumentChange.Type.ADDED) {
                                MessageModel message = change.getDocument().toObject(MessageModel.class);
                                messages.add(message);
                                chatAdapter.notifyItemInserted(messages.size() - 1);
                                binding.chatRecyclerView.scrollToPosition(messages.size() - 1);
                            }
                        }
                    }
                });
    }

    /**
     * Adapter for RecyclerView to display chat messages.
     */
    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

        private final List<MessageModel> messages;

        ChatAdapter(List<MessageModel> messages) {
            this.messages = messages;
        }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MessageViewHolder(ActivityChatBinding.inflate(LayoutInflater.from(parent.getContext())));
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            holder.bind(messages.get(position));
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        /**
         * ViewHolder for binding message data.
         */
        class MessageViewHolder extends RecyclerView.ViewHolder {
            ActivityChatRecyclerItemBinding binding = ActivityChatRecyclerItemBinding.inflate(getLayoutInflater());

            MessageViewHolder(ActivityChatBinding binding) { super(binding.getRoot()); }

            void bind(MessageModel message) {
                if (message.senderPhone.equals(currentUserPhone)) {
                    // Sent message
                    binding.messageTextRight.setText(message.message);
                    binding.usernameTextRight.setText("You");
                    binding.timestampTextRight.setText(formatTimestamp(message.timestamp));
                    binding.messageLayoutRight.setVisibility(View.VISIBLE);
                    binding.messageLayoutLeft.setVisibility(View.GONE);
                } else {
                    // Received message
                    binding.messageTextLeft.setText(message.message);
                    binding.usernameTextLeft.setText(message.senderPhone);
                    binding.timestampTextLeft.setText(formatTimestamp(message.timestamp));
                    binding.messageLayoutLeft.setVisibility(View.VISIBLE);
                    binding.messageLayoutRight.setVisibility(View.GONE);
                }
            }
        }
    }
}