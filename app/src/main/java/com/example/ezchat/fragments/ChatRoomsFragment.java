package com.example.ezchat.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezchat.activities.NewChatRoomActivity;
import com.example.ezchat.databinding.FragmentChatRoomsBinding;
import com.example.ezchat.databinding.FragmentChatRoomsRecyclerItemBinding;
import com.example.ezchat.models.ChatroomModel;
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utilities.AndroidUtil;
import com.example.ezchat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * A Fragment that displays all chat rooms the current user is part of.
 * Allows the user to view, select, and create new chat rooms.
 */
public class ChatRoomsFragment extends Fragment {

    private FragmentChatRoomsBinding binding; // View binding
    private FirebaseFirestore db; // Firestore instance
    private ChatRoomAdapter chatRoomAdapter; // Adapter for chat rooms
    private List<ChatroomModel> chatRoomList; // List of chat rooms
    private String currentUserId; // Local user's ID from SharedPreferences

    /**
     * Default constructor for ChatRoomsFragment.
     */
    public ChatRoomsFragment() {
        // Required empty public constructor
    }

    /**
     * Called to create and return the fragment's view hierarchy.
     *
     * @param inflater  The LayoutInflater used to inflate the layout.
     * @param container The parent ViewGroup for the fragment's UI.
     * @param savedInstanceState The saved instance state, if available.
     * @return The root view of the fragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Initialize View Binding
        binding = FragmentChatRoomsBinding.inflate(inflater, container, false);

        // Initialize Firestore and user data
        db = FirebaseFirestore.getInstance();
        chatRoomList = new ArrayList<>();
        currentUserId = getCurrentUserIdFromPreferences();

        // Set up RecyclerView with ChatRoomAdapter
        chatRoomAdapter = new ChatRoomAdapter(chatRoomList, db, currentUserId);
        binding.chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.chatRecyclerView.setAdapter(chatRoomAdapter);

        // Fetch chat rooms
        fetchChatRooms();

        // Set up Floating Action Button (FAB) to start a new chat
        binding.fabNewChat.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NewChatRoomActivity.class);
            startActivity(intent);
        });

        return binding.getRoot();
    }

    /**
     * Fetches all chat rooms the current user is part of from Firestore.
     * Updates the UI to show the chat rooms or a "No chats started" message if none exist.
     */
    private void fetchChatRooms() {
        if (currentUserId == null || currentUserId.isEmpty()) {
            showNoChatsMessage(true);
            return;
        }

        db.collection(ChatroomModel.FIELD_COLLECTION_NAME)
                .whereArrayContains(ChatroomModel.FIELD_LAST_MESSAGE_TIMESTAMP, currentUserId)
                .orderBy(ChatroomModel.FIELD_LAST_MESSAGE_TIMESTAMP, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    chatRoomList.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        showNoChatsMessage(true);
                    } else {
                        showNoChatsMessage(false);
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            ChatroomModel chatroom = document.toObject(ChatroomModel.class);
                            if (chatroom != null) {
                                chatRoomList.add(chatroom);
                            }
                        }
                        chatRoomAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    showNoChatsMessage(true);
                });
    }

    /**
     * Displays or hides the "No chats started" message based on the presence of chat rooms.
     *
     * @param show True to display the message, false to hide it.
     */
    private void showNoChatsMessage(boolean show) {
        binding.noChatsMessage.setVisibility(show ? View.VISIBLE : View.GONE);
        //binding.chatRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    /**
     * Retrieves the current user's ID from SharedPreferences.
     *
     * @return The current user's ID, or null if not found.
     */
    private String getCurrentUserIdFromPreferences() {
        Context context = getContext();
        if (context != null) {
            return context.getSharedPreferences(PreferenceManager.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE)
                    .getString(UserModel.FIELD_USER_ID, null);
        }
        return null;
    }

    /**
     * Cleans up resources when the fragment's view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Release binding reference to avoid memory leaks
    }

    /**
     * Adapter class to handle displaying chat rooms in the RecyclerView.
     */
    public static class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

        private final List<ChatroomModel> chatRooms; // List of chat rooms
        private final FirebaseFirestore db; // Firestore instance
        private final String currentUserId; // ID of the current user

        /**
         * Constructor for ChatRoomAdapter.
         *
         * @param chatRooms    The list of chat rooms to display.
         * @param db           The Firestore database instance.
         * @param currentUserId The current user's ID.
         */
        public ChatRoomAdapter(List<ChatroomModel> chatRooms, FirebaseFirestore db, String currentUserId) {
            this.chatRooms = chatRooms;
            this.db = db;
            this.currentUserId = currentUserId;
        }

        @NonNull
        @Override
        public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            FragmentChatRoomsRecyclerItemBinding itemBinding = FragmentChatRoomsRecyclerItemBinding.inflate(inflater, parent, false);
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
         * ViewHolder class for displaying individual chat rooms.
         */
        public class ChatRoomViewHolder extends RecyclerView.ViewHolder {

            private final FragmentChatRoomsRecyclerItemBinding binding; // View binding for each item

            /**
             * Constructor for ChatRoomViewHolder.
             *
             * @param binding The binding for the chat room item layout.
             */
            public ChatRoomViewHolder(@NonNull FragmentChatRoomsRecyclerItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            /**
             * Binds a chat room to the ViewHolder and populates UI elements with data.
             *
             * @param chatRoom The chat room to bind to the ViewHolder.
             */
            public void bind(ChatroomModel chatRoom) {
                // Identify the other user in the chat room
                String otherUserId = chatRoom.userIds
                        .stream()
                        .filter(id -> !id.equals(currentUserId))
                        .findFirst()
                        .orElse(null);

                if (otherUserId != null) {
                    // Fetch the other user's details from Firestore
                    db.collection(UserModel.FIELD_COLLECTION_NAME)
                            .document(otherUserId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    UserModel user = documentSnapshot.toObject(UserModel.class);
                                    if (user != null) {
                                        // Populate UI with the other user's data
                                        binding.textViewUserName.setText(user.username);
                                        binding.textViewPhone.setText(user.phone);
                                        AndroidUtil.setProfilePicFromBase64(
                                                binding.imageViewProfilePic.getContext(),
                                                user.profilePic,
                                                binding.imageViewProfilePic
                                        );
                                    }
                                }
                            });

                    // Set the last message and timestamp
                    binding.textViewLastMessage.setText(chatRoom.lastMessage);
                    binding.textViewTimestamp.setText(chatRoom.lastMessageTimestamp.toDate().toString());

                    // Navigate to the chat room on item click
                    itemView.setOnClickListener(v -> {
                        // Handle navigation to ChatRoomActivity or similar
                    });
                }
            }
        }
    }
}