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

import com.example.ezchat.databinding.ActivityChatRoomBinding;
import com.example.ezchat.databinding.ActivityChatRoomRecyclerItemBinding;
import com.example.ezchat.models.ChatroomModel;
import com.example.ezchat.models.MessageModel;
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Manages the chat interface for users to send and view messages within a chat room.
 * Automatically creates a new chat room if one doesn't exist.
 */
public class ChatActivity extends AppCompatActivity {

    private ActivityChatRoomBinding binding; // View binding for activity layout
    private String chatRoomId; // ID of the chat room
    private String currentUserPhone; // Current user's phone number
    private String otherUserPhone; // Other participant's phone number
    private List<MessageModel> messageList; // List of messages in the chat
    private MessageAdapter messageAdapter; // Adapter for RecyclerView
    private boolean isChatRoomCreated; // Indicates if the chat room is already created
    private ChatroomModel chatRoom; // ChatroomModel instance for managing chat room
    private PreferenceManager preferenceManager; // Preference manager for shared preferences
    private FirebaseFirestore db; // Firestore instance

    /**
     * Initializes the activity, sets up the chat room interface, and listens for messages.
     *
     * @param savedInstanceState Saved state from a previous instance of this activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        preferenceManager = PreferenceManager.getInstance(getApplicationContext());
        currentUserPhone = preferenceManager.getString(UserModel.FIELD_PHONE);

        if (currentUserPhone == null) {
            Toast.makeText(this, "Unable to fetch current user details.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chatRoom = (ChatroomModel) getIntent().getSerializableExtra("chatRoom");

        if (chatRoom == null || chatRoom.phoneNumbers == null || chatRoom.phoneNumbers.isEmpty()) {
            Toast.makeText(this, "Invalid chat room details.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chatRoomId = chatRoom.chatroomId;
        isChatRoomCreated = !TextUtils.isEmpty(chatRoomId);

        setupRecyclerView();
        setupButtonListeners();

        if (isChatRoomCreated) {
            listenForMessages();
        }
    }

    /**
     * Sets up the RecyclerView to display chat messages.
     */
    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        boolean isTwoUsers = chatRoom.phoneNumbers.size() == 2;
        messageAdapter = new MessageAdapter(this, messageList, currentUserPhone, isTwoUsers);
        binding.chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.chatRecyclerView.setAdapter(messageAdapter);
    }

    /**
     * Sets up click listeners for the send message and back buttons.
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
     * Sends a message and ensures the chat room exists.
     *
     * @param messageText The text of the message to send.
     */
    private void sendMessage(String messageText) {
        if (!isChatRoomCreated) {
            if (TextUtils.isEmpty(chatRoomId)) {
                chatRoomId = db.collection(ChatroomModel.FIELD_COLLECTION_NAME).document().getId();
            }

            chatRoom.createChatRoom(db, chatRoomId, chatRoom.phoneNumbers, currentUserPhone, success -> {
                if (Boolean.TRUE.equals(success)) {
                    isChatRoomCreated = true;
                    addMessageToChatRoom(messageText);
                } else {
                    Toast.makeText(this, "Failed to create chat room", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            addMessageToChatRoom(messageText);
        }
    }

    /**
     * Adds a message to the chat room and updates the UI.
     *
     * @param messageText The text of the message to add.
     */
    private void addMessageToChatRoom(String messageText) {
        chatRoom.addMessageToChatRoom(db, chatRoomId, currentUserPhone, otherUserPhone, messageText, success -> {
            if (Boolean.TRUE.equals(success)) {
                // Add the message to the local list
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
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Listens for real-time updates of messages in the chat room and updates the UI.
     */
    private void listenForMessages() {
        chatRoom.listenForMessages(db, chatRoomId, message -> {
            messageList.add(message);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            binding.chatRecyclerView.scrollToPosition(messageList.size() - 1);
        }, error -> Toast.makeText(this, "Failed to load messages: " + error, Toast.LENGTH_SHORT).show());
    }

    /**
     * RecyclerView adapter for displaying chat messages with different layouts for sent and received messages.
     */
    class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

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
            ActivityChatRoomRecyclerItemBinding binding = ActivityChatRoomRecyclerItemBinding.inflate(LayoutInflater.from(context), parent, false);
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

        class MessageViewHolder extends RecyclerView.ViewHolder {

            private final ActivityChatRoomRecyclerItemBinding binding;

            public MessageViewHolder(@NonNull ActivityChatRoomRecyclerItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            void bindSentMessage(MessageModel message) {
                binding.messageLayoutLeft.setVisibility(View.GONE);
                binding.messageLayoutRight.setVisibility(View.VISIBLE);
                binding.messageTextRight.setText(message.message);
                binding.timestampTextRight.setText(formatTimestamp(message.timestamp));
                binding.usernameTextRight.setVisibility(isTwoUsers ? View.GONE : View.VISIBLE);
                if (!isTwoUsers) binding.usernameTextRight.setText(message.senderPhone);
            }

            void bindReceivedMessage(MessageModel message) {
                binding.messageLayoutRight.setVisibility(View.GONE);
                binding.messageLayoutLeft.setVisibility(View.VISIBLE);
                binding.messageTextLeft.setText(message.message);
                binding.timestampTextLeft.setText(formatTimestamp(message.timestamp));
                binding.usernameTextLeft.setVisibility(isTwoUsers ? View.GONE : View.VISIBLE);
                if (!isTwoUsers) binding.usernameTextLeft.setText(message.senderPhone);
            }

            private String formatTimestamp(com.google.firebase.Timestamp timestamp) {
                if (timestamp == null) return "";
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
                return sdf.format(timestamp.toDate());
            }
        }
    }
}