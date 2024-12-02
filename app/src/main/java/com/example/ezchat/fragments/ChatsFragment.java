package com.example.ezchat.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezchat.R;
import com.example.ezchat.activities.ChatCreatorActivity;
import com.example.ezchat.activities.ChatActivity;
import com.example.ezchat.models.ChatModel;
import com.example.ezchat.utilities.Constants;
import com.example.ezchat.utilities.PreferenceManager;
import com.example.ezchat.utilities.Utilities;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a list of chats that the user is involved in,
 * along with the last message of each chat.
 */
public class ChatsFragment extends Fragment {

    private RecyclerView chatRecyclerView;
    private TextView noChatsMessage;
    private ProgressBar loadingProgressBar;

    private FirebaseFirestore firestore;
    private ChatsAdapter adapter;
    private List<ChatModel> chatList;
    private PreferenceManager preferenceManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        // Initialize views
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        noChatsMessage = view.findViewById(R.id.noChatsMessage);
        loadingProgressBar = view.findViewById(R.id.chatsLoadingProgressBar);

        // Initialize Firebase Firestore and PreferenceManager
        firestore = FirebaseFirestore.getInstance();
        preferenceManager = PreferenceManager.getInstance(requireContext());

        // Set up RecyclerView
        chatList = new ArrayList<>();
        adapter = new ChatsAdapter(chatList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(adapter);

        // Load chats from Firestore
        loadChats();

        // Set up the FAB for creating a new chat
        setupFabNewChat(view);

        return view;
    }

    /**
     * Fetches chats from Firestore in real-time and updates the UI.
     */
    private void loadChats() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        String currentUserPhone = preferenceManager.get(Constants.FIELD_PHONE, "");

        firestore.collection(Constants.COLLECTION_CHATS)
                .whereArrayContains(Constants.FIELD_PHONE, currentUserPhone)
                .orderBy(Constants.FIELD_LAST_MESSAGE_TIMESTAMP, Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(Constants.LOG_CHATS_FRAGMENT, "Error fetching chats: " + error.getMessage());
                        loadingProgressBar.setVisibility(View.GONE);
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        chatList.clear();
                        for (DocumentSnapshot doc : snapshots) {
                            ChatModel chat = doc.toObject(ChatModel.class);
                            if (chat != null) {
                                chatList.add(chat);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        noChatsMessage.setVisibility(chatList.isEmpty() ? View.VISIBLE : View.GONE);
                    } else {
                        chatList.clear();
                        adapter.notifyDataSetChanged();
                        noChatsMessage.setVisibility(View.VISIBLE);
                    }

                    loadingProgressBar.setVisibility(View.GONE);
                });
    }

    /**
     * Sets up the Floating Action Button (FAB) to open the ChatCreatorActivity.
     */
    private void setupFabNewChat(View view) {
        View fabNewChat = view.findViewById(R.id.fabNewChat);
        fabNewChat.setOnClickListener(v -> {
            String currentUserPhone = preferenceManager.get(Constants.FIELD_PHONE, "");
            if (currentUserPhone.isEmpty()) {
                Log.e(Constants.LOG_CHATS_FRAGMENT, "Current user phone is missing from preferences.");
                return;
            }

            // Navigate to ChatCreatorActivity, passing the current user's phone number
            Utilities.navigateToActivity(
                    requireContext(),
                    ChatCreatorActivity.class,
                    Utilities.createExtras(Constants.FIELD_PHONE, currentUserPhone)
            );
        });
    }

    /**
     * RecyclerView Adapter for displaying a list of chats.
     */
    private class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatViewHolder> {
        private final List<ChatModel> chatList;

        ChatsAdapter(List<ChatModel> chatList) {
            this.chatList = chatList;
        }

        @NonNull
        @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_chats_recycler_item, parent, false);
            return new ChatViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
            ChatModel chat = chatList.get(position);
            holder.bind(chat);
        }

        @Override
        public int getItemCount() {
            return chatList.size();
        }

        /**
         * ViewHolder for individual chat items.
         */
        class ChatViewHolder extends RecyclerView.ViewHolder {
            private final TextView userNameTextView;
            private final TextView lastMessageTextView;
            private final TextView timestampTextView;

            ChatViewHolder(@NonNull View itemView) {
                super(itemView);
                userNameTextView = itemView.findViewById(R.id.textViewUserName);
                lastMessageTextView = itemView.findViewById(R.id.textViewLastMessage);
                timestampTextView = itemView.findViewById(R.id.textViewTimestamp);
            }

            void bind(ChatModel chat) {
                userNameTextView.setText(chat.creatorPhone); // Replace with actual user name or display logic
                lastMessageTextView.setText(chat.lastMessage != null ? chat.lastMessage.message : "No messages yet");
                timestampTextView.setText(chat.lastMessage != null ? Utilities.formatTimestamp(chat.lastMessage.timestamp) : "");

                itemView.setOnClickListener(v -> {
                    // Handle chat click (navigate to chat details or messages activity)
                    Utilities.navigateToActivity(
                            requireContext(),
                            ChatActivity.class,
                            Utilities.createExtras(Constants.FIELD_CHAT_ID, chat.chatId));
                });
            }
        }
    }
}