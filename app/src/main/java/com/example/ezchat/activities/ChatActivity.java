package com.example.ezchat.activities;

import android.content.Context;
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
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Activity for managing a chat interface where users can send and view messages.
 * This activity handles the initialization of a chat, sending messages, and listening for updates.
 */
public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding; // Binding for activity_chat.xml
    private String chatId; // Unique ID for the chat
    private String currentUserPhone; // Phone number of the current user
    private String otherUserPhone; // Phone number of the other user in the chat
    private List<MessageModel> messageList; // List of messages displayed in the chat
    private MessageAdapter messageAdapter; // Adapter for displaying chat messages
    private boolean isChatCreated; // Flag to indicate if the chat has been initialized
    private ChatModel chat; // Represents the chat object
    private PreferenceManager preferenceManager; // Manages user preferences
    private FirebaseFirestore db; // Firebase Firestore instance for database operations

    /**
     * Called when the activity is first created.
     * Sets up the chat interface, retrieves chat details, and initializes listeners for messages.
     *
     * @param savedInstanceState Saved state from a previous instance of this activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize view binding
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firestore and shared preference manager
        db = FirebaseFirestore.getInstance();
        preferenceManager = PreferenceManager.getInstance(getApplicationContext());
        currentUserPhone = preferenceManager.getString(UserModel.FIELD_PHONE);

        // Validate current user phone number
        if (currentUserPhone == null) {
            Toast.makeText(this, "Unable to fetch current user details.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Retrieve chat details passed via intent
        chat = (ChatModel) getIntent().getSerializableExtra(ChatModel.FIELD_COLLECTION_NAME);

        // Validate chat details
        if (chat == null || chat.phoneNumbers == null || chat.phoneNumbers.isEmpty()) {
            Toast.makeText(this, "Invalid chat details.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chatId = chat.chatId;
        isChatCreated = !TextUtils.isEmpty(chatId);

        // Initialize UI components and listeners
        setupRecyclerView();
        setupButtonListeners();

        // Start listening for messages if the chat is already created
        if (isChatCreated) {
            listenForMessages();
        }
    }

    /**
     * Configures the RecyclerView to display chat messages.
     * Initializes the MessageAdapter for binding messages to the RecyclerView.
     */
    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        boolean isTwoUsers = chat.phoneNumbers.size() == 2; // Check if the chat involves only two users
        messageAdapter = new MessageAdapter(this, messageList, currentUserPhone, isTwoUsers);
        binding.chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.chatRecyclerView.setAdapter(messageAdapter);
    }

    /**
     * Sets up click listeners for UI elements, such as the back button and the send message button.
     */
    private void setupButtonListeners() {
        // Handle back button click
        binding.backButton.setOnClickListener(v -> onBackPressed());

        // Handle send message button click
        binding.messageSendButton.setOnClickListener(v -> {
            String messageText = binding.inputMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(messageText)) {
                sendMessage(messageText);
                binding.inputMessage.setText(""); // Clear the input field
            } else {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sends a message. If the chat does not exist, it creates a new one before sending.
     *
     * @param messageText The text of the message to be sent.
     */
    private void sendMessage(String messageText) {
        if (!isChatCreated) {
            if (TextUtils.isEmpty(chatId)) {
                // Generate a new chat ID if it doesn't exist
                chatId = db.collection(ChatModel.FIELD_COLLECTION_NAME).document().getId();
            }

            chat.createChat(db, chatId, chat.phoneNumbers, currentUserPhone, success -> {
                if (Boolean.TRUE.equals(success)) {
                    isChatCreated = true;
                    addMessageToChat(messageText);
                } else {
                    Toast.makeText(this, "Failed to create chat.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            addMessageToChat(messageText);
        }
    }

    /**
     * Adds a message to the chat and updates the local UI with the new message.
     *
     * @param messageText The text of the message to be added.
     */
    private void addMessageToChat(String messageText) {
        chat.addMessageToChat(db, chatId, currentUserPhone, otherUserPhone, messageText, success -> {
            if (Boolean.TRUE.equals(success)) {
                // Add the sent message to the local list and notify the adapter
                MessageModel sentMessage = new MessageModel(
                        currentUserPhone,
                        otherUserPhone,
                        messageText,
                        com.google.firebase.Timestamp.now(),
                        "sent"
                );
                messageList.add(sentMessage);
                messageAdapter.notifyItemInserted(messageList.size() - 1);
                binding.chatRecyclerView.scrollToPosition(messageList.size() - 1);
            } else {
                Toast.makeText(this, "Failed to send message.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Listens for real-time updates to messages in the chat.
     * Updates the UI with new messages as they are received.
     */
    private void listenForMessages() {
        chat.listenForMessages(db, chatId, message -> {
            messageList.add(message);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            binding.chatRecyclerView.scrollToPosition(messageList.size() - 1);
        }, error -> Toast.makeText(this, "Failed to load messages: " + error, Toast.LENGTH_SHORT).show());
    }

    /**
     * Adapter for managing and displaying chat messages in the RecyclerView.
     */
    static class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

        private final Context context; // Context for inflating views
        private final List<MessageModel> messages; // List of chat messages
        private final String currentUserPhone; // Phone number of the current user
        private final boolean isTwoUsers; // Indicates if the chat is between two users

        /**
         * Constructs a MessageAdapter for managing chat messages.
         *
         * @param context          The context of the activity.
         * @param messages         The list of messages to display.
         * @param currentUserPhone The phone number of the current user.
         * @param isTwoUsers       Indicates if the chat is a two-user conversation.
         */
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
            boolean isSentByCurrentUser = message.senderPhone.equals(currentUserPhone);

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

        /**
         * ViewHolder class for managing individual message views in the RecyclerView.
         */
        class MessageViewHolder extends RecyclerView.ViewHolder {

            private final ActivityChatRecyclerItemBinding binding; // Binding for message item layout

            public MessageViewHolder(@NonNull ActivityChatRecyclerItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            /**
             * Binds a sent message to the right-aligned layout.
             *
             * @param message The message to be displayed.
             */
            void bindSentMessage(MessageModel message) {
                binding.messageLayoutLeft.setVisibility(View.GONE);
                binding.messageLayoutRight.setVisibility(View.VISIBLE);
                binding.messageTextRight.setText(message.message);
                binding.timestampTextRight.setText(formatTimestamp(message.timestamp));
                binding.usernameTextRight.setVisibility(isTwoUsers ? View.GONE : View.VISIBLE);
                if (!isTwoUsers) binding.usernameTextRight.setText(message.senderPhone);
            }

            /**
             * Binds a received message to the left-aligned layout.
             *
             * @param message The message to be displayed.
             */
            void bindReceivedMessage(MessageModel message) {
                binding.messageLayoutRight.setVisibility(View.GONE);
                binding.messageLayoutLeft.setVisibility(View.VISIBLE);
                binding.messageTextLeft.setText(message.message);
                binding.timestampTextLeft.setText(formatTimestamp(message.timestamp));
                binding.usernameTextLeft.setVisibility(isTwoUsers ? View.GONE : View.VISIBLE);
                if (!isTwoUsers) binding.usernameTextLeft.setText(message.senderPhone);
            }

            /**
             * Formats a Firebase Timestamp into a user-friendly date and time string.
             *
             * @param timestamp The timestamp to format.
             * @return A formatted date and time string.
             */
            private String formatTimestamp(com.google.firebase.Timestamp timestamp) {
                if (timestamp == null) return "";
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
                return sdf.format(timestamp.toDate());
            }
        }
    }
}