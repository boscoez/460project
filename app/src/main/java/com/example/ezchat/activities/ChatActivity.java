package com.example.ezchat.activities;

import static com.example.ezchat.utilities.Utilities.formatTimestamp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezchat.R;
import com.example.ezchat.models.ChatModel;
import com.example.ezchat.models.MessageModel;
import com.example.ezchat.utilities.Constants;
import com.example.ezchat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private String chatId; // Chat ID if available
    private ChatModel chatModel; // Chat model if created by ChatCreatorActivity
    private String currentUserPhone;
    private boolean isNewChat = false;

    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private EditText inputMessage;
    private ImageButton btnSendMessage;
    private ProgressBar loadingProgressBar;

    private List<MessageModel> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initializeViews();
        initializeDependencies();
        handleIntent();

        setupListeners();
        if (isNewChat) {
            // For a new chat, wait for the first message to create the chat
            showWaitingForMessage();
        } else {
            // For an existing chat, load messages from Firestore
            loadMessages();
        }
    }

    private void initializeViews() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        inputMessage = findViewById(R.id.inputMessage);
        btnSendMessage = findViewById(R.id.messageSendButton);
        loadingProgressBar = findViewById(R.id.chatLoadingProgressBar);

        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages);
        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void initializeDependencies() {
        preferenceManager = PreferenceManager.getInstance(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        currentUserPhone = preferenceManager.get(Constants.FIELD_PHONE, "");
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(Constants.FIELD_CHAT_ID)) {
            // Case 1: Existing chat
            chatId = intent.getStringExtra(Constants.FIELD_CHAT_ID);
            isNewChat = false;
        } else if (intent.hasExtra(Constants.MODEL_CHAT)) {
            // Case 2: New chat
            chatModel = (ChatModel) intent.getSerializableExtra(Constants.MODEL_CHAT);
            isNewChat = true;
        } else {
            Log.e(TAG, "No valid data passed to ChatActivity.");
            finish();
        }
    }

    private void setupListeners() {
        btnSendMessage.setOnClickListener(v -> {
            String messageContent = inputMessage.getText().toString().trim();
            if (messageContent.isEmpty()) {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            inputMessage.setText("");

            if (isNewChat) {
                createNewChat(messageContent);
            } else {
                sendMessage(messageContent);
            }
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
    }

    private void showWaitingForMessage() {
        btnSendMessage.setEnabled(true);
        loadingProgressBar.setVisibility(View.GONE);
        chatRecyclerView.setVisibility(View.GONE);
    }

    private void createNewChat(String firstMessage) {
        chatId = database.collection(Constants.COLLECTION_CHAT).document().getId();
        chatModel.chatId = chatId;

        MessageModel message = new MessageModel(currentUserPhone, new ArrayList<>(chatModel.phoneNumbers), firstMessage);
        chatModel.lastMessage = message;

        // Add chat and first message to Firestore
        database.collection(Constants.COLLECTION_CHAT)
                .document(chatId)
                .set(chatModel)
                .addOnSuccessListener(aVoid -> sendMessage(firstMessage))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create chat", e);
                    Toast.makeText(this, "Failed to start chat.", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadMessages() {
        loadingProgressBar.setVisibility(View.VISIBLE);

        database.collection(Constants.COLLECTION_MESSAGE)
                .whereEqualTo(Constants.FIELD_CHAT_ID, chatId)
                .orderBy(Constants.FIELD_TIMESTAMP, Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    loadingProgressBar.setVisibility(View.GONE);

                    if (error != null) {
                        Log.e(TAG, "Error loading messages", error);
                        Toast.makeText(this, "Failed to load messages.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        messages.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                            messages.add(doc.toObject(MessageModel.class));
                        }
                        chatAdapter.notifyDataSetChanged();
                        chatRecyclerView.scrollToPosition(messages.size() - 1);
                    }
                });
    }

    private void sendMessage(String content) {
        MessageModel message = new MessageModel(currentUserPhone, new ArrayList<>(chatModel.phoneNumbers), content);

        database.collection(Constants.COLLECTION_MESSAGE)
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Message sent successfully.");
                    chatRecyclerView.scrollToPosition(messages.size() - 1);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to send message", e);
                    Toast.makeText(this, "Failed to send message.", Toast.LENGTH_SHORT).show();
                });
    }

    public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
        private final List<MessageModel> messages;

        public ChatAdapter(List<MessageModel> messages) {
            this.messages = messages;
        }

        @Override
        public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_chat_recycler_item, parent, false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MessageViewHolder holder, int position) {
            MessageModel message = messages.get(position);
            holder.bind(message);
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        public class MessageViewHolder extends RecyclerView.ViewHolder {
            private final TextView messageTextLeft, usernameTextLeft, timestampTextLeft;
            private final TextView messageTextRight, usernameTextRight, timestampTextRight;

            public MessageViewHolder(View itemView) {
                super(itemView);
                messageTextLeft = itemView.findViewById(R.id.messageTextLeft);
                usernameTextLeft = itemView.findViewById(R.id.usernameTextLeft);
                timestampTextLeft = itemView.findViewById(R.id.timestampTextLeft);

                messageTextRight = itemView.findViewById(R.id.messageTextRight);
                usernameTextRight = itemView.findViewById(R.id.usernameTextRight);
                timestampTextRight = itemView.findViewById(R.id.timestampTextRight);
            }

            public void bind(MessageModel message) {
                if (message.senderPhone.equals(preferenceManager.get(Constants.FIELD_PHONE, ""))) {
                    // Sent message
                    messageTextRight.setText(message.message);
                    usernameTextRight.setText(Constants.LABEL_YOU);
                    timestampTextRight.setText(formatTimestamp(message.timestamp));
                    messageTextRight.setVisibility(View.VISIBLE);
                } else {
                    // Received message
                    messageTextLeft.setText(message.message);
                    usernameTextLeft.setText(message.senderPhone);
                    timestampTextLeft.setText(formatTimestamp(message.timestamp));
                    messageTextLeft.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}