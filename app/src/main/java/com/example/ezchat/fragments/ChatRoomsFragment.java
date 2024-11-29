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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fragment to display the list of chat rooms the current user is part of.
 * Users can click on a chat room to navigate to its messages.
 */
public class ChatRoomsFragment extends Fragment {

    private FragmentChatRoomsBinding binding; // View binding for the fragment
    private PreferenceManager preferenceManager; // Preference manager for user data
    private List<ChatroomModel> chatRoomList; // List of chat rooms
    private ChatRoomAdapter chatRoomAdapter; // Adapter for the RecyclerView
    private FirebaseFirestore firestore; // Firestore instance

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatRoomsBinding.inflate(inflater, container, false);

        // Initialize Firebase Firestore and PreferenceManager
        firestore = FirebaseFirestore.getInstance();
        preferenceManager = PreferenceManager.getInstance(requireContext());

        // Set up RecyclerView
        setupRecyclerView();

        // Fetch chat rooms for the current user
        fetchChatRooms();

        // Set up "New Chat" button click listener
        binding.fabNewChat.setOnClickListener(v -> navigateToNewChatRoom());

        return binding.getRoot();
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
     * Fetches chat rooms associated with the current user from Firestore.
     */
    private void fetchChatRooms() {
        String phoneNumber = preferenceManager.getString(UserModel.FIELD_PHONE);

        if (phoneNumber == null) {
            Toast.makeText(requireContext(), "Failed to fetch user details.", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection(UserModel.FIELD_COLLECTION_NAME)
                .document(phoneNumber) // User's phone number as document ID
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserModel user = documentSnapshot.toObject(UserModel.class);
                        if (user != null && user.chatRooms != null && !user.chatRooms.isEmpty()) {
                            loadChatRooms(user.chatRooms);
                        } else {
                            showNoChatRoomsMessage();
                        }
                    } else {
                        showNoChatRoomsMessage();
                    }
                })
                .addOnFailureListener(e -> {
                    showNoChatRoomsMessage();
                    Log.e("ChatRoomsFragment", "Error fetching user chat rooms", e);
                });
    }

    /**
     * Loads the chat rooms from Firestore based on the user's chat room IDs.
     *
     * @param chatRoomIds The list of chat room IDs.
     */
    private void loadChatRooms(List<String> chatRoomIds) {
        for (String chatRoomId : chatRoomIds) {
            firestore.collection(ChatroomModel.FIELD_COLLECTION_NAME)
                    .document(chatRoomId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            ChatroomModel chatRoom = documentSnapshot.toObject(ChatroomModel.class);
                            if (chatRoom != null) {
                                chatRoomList.add(chatRoom);
                                chatRoomAdapter.notifyItemInserted(chatRoomList.size() - 1);
                                binding.noChatsMessage.setVisibility(View.GONE);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("ChatRoomsFragment", "Error loading chat room: " + chatRoomId, e));
        }
    }

    /**
     * Displays a message when no chat rooms are found.
     */
    private void showNoChatRoomsMessage() {
        binding.noChatsMessage.setVisibility(View.VISIBLE);
        chatRoomList.clear();
        chatRoomAdapter.notifyDataSetChanged();
    }

    /**
     * Navigates to the New Chat Room activity.
     */
    private void navigateToNewChatRoom() {
        Intent intent = new Intent(requireContext(), NewChatRoomActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Adapter for displaying chat rooms in a RecyclerView.
     */
    private class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

        private final List<ChatroomModel> chatRooms;

        /**
         * Constructor for ChatRoomAdapter.
         *
         * @param chatRooms List of chat rooms to display.
         */
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

            /**
             * Constructor for ChatRoomViewHolder.
             *
             * @param itemBinding View binding for the RecyclerView item.
             */
            public ChatRoomViewHolder(@NonNull FragmentChatRoomsRecyclerItemBinding itemBinding) {
                super(itemBinding.getRoot());
                this.itemBinding = itemBinding;
            }

            /**
             * Binds the chat room data to the view.
             *
             * @param chatRoom The chat room to bind.
             */
            public void bind(ChatroomModel chatRoom) {
                itemBinding.textViewUserName.setText(chatRoom.lastMessageSenderPhone); // Placeholder for user name
                itemBinding.textViewLastMessage.setText(chatRoom.lastMessage);
                itemBinding.textViewTimestamp.setText(formatTimestamp(chatRoom.lastMessageTimestamp));

                // Handle item click
                itemBinding.getRoot().setOnClickListener(v -> {
                    Intent intent = new Intent(requireContext(), ChatRoomActivity.class);
                    intent.putExtra(ChatroomModel.FIELD_CHATROOM_ID, chatRoom.chatroomId);
                    startActivity(intent);
                });
            }

            /**
             * Formats a Firestore timestamp into a user-friendly date string.
             *
             * @param timestamp The timestamp to format.
             * @return A formatted date string.
             */
            private String formatTimestamp(com.google.firebase.Timestamp timestamp) {
                if (timestamp == null) return "";
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
                return sdf.format(timestamp.toDate());
            }
        }
    }
}