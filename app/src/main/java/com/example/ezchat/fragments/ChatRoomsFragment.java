package com.example.ezchat.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ezchat.activities.ChatRoomActivity;
import com.example.ezchat.activities.NewChatRoomActivity;
import com.example.ezchat.databinding.FragmentChatRoomsBinding;
import com.example.ezchat.databinding.FragmentChatRoomsRecyclerItemBinding;
import com.example.ezchat.models.ChatroomModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ChatFragment displays all chat rooms the current user is part of.
 * Clicking a chat room navigates to the ChatRoomActivity.
 */
public class ChatRoomsFragment extends Fragment {

    private FragmentChatRoomsBinding binding;
    private List<ChatroomModel> chatRoomList; // List of chat rooms for the current user
    private ChatRoomAdapter chatRoomAdapter; // Adapter for the RecyclerView
    private FirebaseFirestore db; // Firestore instance
    private String currentUserId; // ID of the current user

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatRoomsBinding.inflate(inflater, container, false);

        // Initialize Firestore, user ID, and chat room list
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();
        chatRoomList = new ArrayList<>();

        // Set up RecyclerView
        setupRecyclerView();

        // Fetch chat rooms for the current user
        fetchChatRooms();

        // Set up FAB to create a new chat
        binding.fabNewChat.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), NewChatRoomActivity.class);
            startActivity(intent);
        });

        return binding.getRoot();
    }

    /**
     * Sets up the RecyclerView to display chat rooms.
     */
    private void setupRecyclerView() {
        chatRoomAdapter = new ChatRoomAdapter(chatRoomList, chatRoomId -> {
            // Navigate to ChatRoomActivity on item click
            Intent intent = new Intent(getContext(), ChatRoomActivity.class);
            intent.putExtra(ChatroomModel.FIELD_CHATROOM_ID, chatRoomId);
            startActivity(intent);
        });

        binding.chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.chatRecyclerView.setAdapter(chatRoomAdapter);
    }

    /**
     * Fetches all chat rooms the current user is involved in.
     */
    private void fetchChatRooms() {
        if (currentUserId == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection(ChatroomModel.FIELD_COLLECTION_NAME)
                .whereArrayContains(ChatroomModel.FIELD_USER_IDS, currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    chatRoomList.clear();
                    if (!querySnapshot.isEmpty()) {
                        for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                            ChatroomModel chatRoom = document.toObject(ChatroomModel.class);
                            chatRoomList.add(chatRoom);
                        }
                        chatRoomAdapter.notifyDataSetChanged();
                        binding.noChatsMessage.setVisibility(chatRoomList.isEmpty() ? View.VISIBLE : View.GONE);
                    } else {
                        binding.noChatsMessage.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to fetch chat rooms", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Adapter for displaying chat rooms in a RecyclerView.
     */
    private static class ChatRoomAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

        private final List<ChatroomModel> chatRoomList;
        private final OnChatRoomClickListener listener;

        /**
         * Constructs the ChatRoomAdapter.
         *
         * @param chatRoomList List of chat rooms to display.
         * @param listener     Listener for handling chat room clicks.
         */
        public ChatRoomAdapter(List<ChatroomModel> chatRoomList, OnChatRoomClickListener listener) {
            this.chatRoomList = chatRoomList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            FragmentChatRoomsRecyclerItemBinding binding = FragmentChatRoomsRecyclerItemBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ChatRoomViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
            holder.bind(chatRoomList.get(position), listener);
        }

        @Override
        public int getItemCount() {
            return chatRoomList.size();
        }

        /**
         * Listener interface for handling chat room click events.
         */
        public interface OnChatRoomClickListener {
            void onChatRoomClick(String chatRoomId);
        }

        /**
         * ViewHolder for displaying a single chat room.
         */
        public static class ChatRoomViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {

            private final FragmentChatRoomsRecyclerItemBinding binding;

            /**
             * Constructs the ChatRoomViewHolder.
             *
             * @param binding Binding for the RecyclerView item.
             */
            public ChatRoomViewHolder(@NonNull FragmentChatRoomsRecyclerItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            /**
             * Binds the chat room data to the view.
             *
             * @param chatRoom ChatroomModel instance to bind.
             * @param listener Listener for chat room clicks.
             */
            public void bind(ChatroomModel chatRoom, OnChatRoomClickListener listener) {
                binding.textViewUserName.setText(chatRoom.lastMessageSenderId); // Placeholder for sender's name
                binding.textViewLastMessage.setText(chatRoom.lastMessage);
                binding.textViewTimestamp.setText(formatTimestamp(chatRoom.lastMessageTimestamp));

                // Click listener to navigate to the chat room
                binding.getRoot().setOnClickListener(v -> listener.onChatRoomClick(chatRoom.chatroomId));
            }

            /**
             * Formats a Firestore timestamp into a readable string.
             *
             * @param timestamp Firestore timestamp to format.
             * @return Formatted timestamp string.
             */
            private String formatTimestamp(com.google.firebase.Timestamp timestamp) {
                if (timestamp == null) return "";
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
                return sdf.format(new Date(timestamp.toDate().getTime()));
            }
        }
    }
}