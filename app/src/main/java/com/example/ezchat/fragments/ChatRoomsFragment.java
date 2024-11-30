package com.example.ezchat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezchat.activities.ChatRoomActivity;
import com.example.ezchat.activities.NewChatRoomActivity;
import com.example.ezchat.databinding.FragmentChatRoomsBinding;
import com.example.ezchat.databinding.FragmentChatRoomsRecyclerItemBinding;
import com.example.ezchat.models.ChatroomModel;
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment to display the current user's chat rooms.
 * Actively listens for changes in the chat rooms in real-time.
 */
public class ChatRoomsFragment extends Fragment {
    private static final String TAG = "ChatRoomsFragment";

    private FragmentChatRoomsBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatroomModel> chatRoomList;
    private ChatRoomAdapter chatRoomAdapter;
    private FirebaseFirestore firestore;
    private ListenerRegistration chatRoomListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatRoomsBinding.inflate(inflater, container, false);

        // Initialize Firestore and PreferenceManager
        firestore = FirebaseFirestore.getInstance();
        preferenceManager = PreferenceManager.getInstance(requireContext());

        // Set up RecyclerView
        setupRecyclerView();

        // Set up "New Chat" button
        binding.fabNewChat.setOnClickListener(v -> navigateToNewChatRoom());

        // Start listening to chat room changes
        listenForChatRoomChanges();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (chatRoomListener != null) {
            chatRoomListener.remove(); // Remove listener to avoid memory leaks
        }
        binding = null;
    }

    /**
     * Sets up the RecyclerView to display chat rooms.
     */
    private void setupRecyclerView() {
        chatRoomList = new ArrayList<>();
        chatRoomAdapter = new ChatRoomAdapter(chatRoomList);
        binding.chatRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.chatRecyclerView.setAdapter(chatRoomAdapter);
    }

    /**
     * Listens for changes to the user's chat rooms in real-time.
     */
    private void listenForChatRoomChanges() {
        String phoneNumber = preferenceManager.getString(UserModel.FIELD_PHONE);

        if (phoneNumber == null) {
            Toast.makeText(requireContext(), "Failed to fetch user details.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Listening for chat room changes for user: " + phoneNumber);

        chatRoomListener = firestore.collection(ChatroomModel.FIELD_COLLECTION_NAME)
                .whereArrayContains("phoneNumbers", phoneNumber) // Filter chat rooms for the current user
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening to chat rooms", error);
                        return;
                    }

                    if (querySnapshot != null) {
                        for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                            ChatroomModel chatRoom = documentChange.getDocument().toObject(ChatroomModel.class);
                            switch (documentChange.getType()) {
                                case ADDED:
                                    // Add new chat room to the list
                                    chatRoomList.add(chatRoom);
                                    chatRoomAdapter.notifyItemInserted(chatRoomList.size() - 1);
                                    Log.d(TAG, "Chat room added: " + chatRoom.chatroomId);
                                    break;

                                case MODIFIED:
                                    // Update an existing chat room in the list
                                    int index = findChatRoomIndex(chatRoom.chatroomId);
                                    if (index != -1) {
                                        chatRoomList.set(index, chatRoom);
                                        chatRoomAdapter.notifyItemChanged(index);
                                        Log.d(TAG, "Chat room modified: " + chatRoom.chatroomId);
                                    }
                                    break;

                                case REMOVED:
                                    // Remove a chat room from the list
                                    index = findChatRoomIndex(chatRoom.chatroomId);
                                    if (index != -1) {
                                        chatRoomList.remove(index);
                                        chatRoomAdapter.notifyItemRemoved(index);
                                        Log.d(TAG, "Chat room removed: " + chatRoom.chatroomId);
                                    }
                                    break;
                            }
                        }

                        // Hide "No Chats" message if there are any chat rooms
                        if (!chatRoomList.isEmpty()) {
                            binding.noChatsMessage.setVisibility(View.GONE);
                        } else {
                            binding.noChatsMessage.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    /**
     * Finds the index of a chat room in the list by its ID.
     *
     * @param chatRoomId The ID of the chat room to find.
     * @return The index of the chat room, or -1 if not found.
     */
    private int findChatRoomIndex(String chatRoomId) {
        for (int i = 0; i < chatRoomList.size(); i++) {
            if (chatRoomList.get(i).chatroomId.equals(chatRoomId)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Navigates to the New Chat Room activity.
     */
    private void navigateToNewChatRoom() {
        Intent intent = new Intent(requireContext(), NewChatRoomActivity.class);
        startActivity(intent);
    }

    /**
     * Adapter for displaying chat rooms in a RecyclerView.
     */
    private class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

        private final List<ChatroomModel> chatRooms;

        public ChatRoomAdapter(List<ChatroomModel> chatRooms) {
            this.chatRooms = chatRooms;
        }

        @NonNull
        @Override
        public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            FragmentChatRoomsRecyclerItemBinding itemBinding = FragmentChatRoomsRecyclerItemBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ChatRoomViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
            holder.bind(chatRooms.get(position));
        }

        @Override
        public int getItemCount() {
            return chatRooms.size();
        }

        /**
         * ViewHolder class for individual chat room items.
         */
        class ChatRoomViewHolder extends RecyclerView.ViewHolder {
            private final FragmentChatRoomsRecyclerItemBinding itemBinding;

            public ChatRoomViewHolder(@NonNull FragmentChatRoomsRecyclerItemBinding itemBinding) {
                super(itemBinding.getRoot());
                this.itemBinding = itemBinding;
            }

            public void bind(ChatroomModel chatRoom) {
                String otherParticipantPhone = chatRoom.phoneNumbers.stream()
                        .filter(phone -> !phone.equals(preferenceManager.getString(UserModel.FIELD_PHONE)))
                        .findFirst()
                        .orElse("Unknown");

                itemBinding.textViewUserName.setText(otherParticipantPhone);
                itemBinding.textViewLastMessage.setText(chatRoom.lastMessage != null ? chatRoom.lastMessage : "No messages yet");
                itemBinding.textViewTimestamp.setText(formatTimestamp(chatRoom.lastMessageTimestamp));

                itemBinding.getRoot().setOnClickListener(v -> {
                    Intent intent = new Intent(requireContext(), ChatRoomActivity.class);
                    intent.putExtra("chatRoom", chatRoom); // Pass the full ChatroomModel object
                    startActivity(intent);
                });
            }

            private String formatTimestamp(Date timestamp) {
                if (timestamp == null) return "Unknown time";
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
                return sdf.format(timestamp);
            }
        }
    }
}