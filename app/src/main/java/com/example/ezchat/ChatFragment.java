package com.example.ezchat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezchat.databinding.FragmentChatBinding;
import com.example.ezchat.databinding.FragmentChatItemBinding;
import com.example.ezchat.models.ChatroomModel;
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;
    private FirebaseFirestore db;
    private ChatRoomAdapter chatRoomAdapter;
    private List<ChatroomModel> chatRoomList;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        chatRoomList = new ArrayList<>();

        // Set up RecyclerView
        chatRoomAdapter = new ChatRoomAdapter(chatRoomList);
        binding.chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.chatRecyclerView.setAdapter(chatRoomAdapter);

        // Fetch chat rooms
        fetchChatRooms();

        // Set up Floating Action Button (FAB) for new chat
        binding.fabNewChat.setOnClickListener(v -> {
            // Handle new chat functionality
            // Open activity for adding a new chat
        });

        return binding.getRoot();
    }

    /**
     * Fetch the chat rooms that the current user is a part of.
     */
    private void fetchChatRooms() {
        String currentUserId = getCurrentUserId(); // Get current user ID

        // Get chat rooms where the current user is one of the participants
        db.collection(Constants.KEY_COLLECTION_CHAT)
                .whereArrayContains(Constants.KEY_USER_IDS, currentUserId)
                .orderBy(Constants.KEY_LAST_MESSAGE_TIMESTAMP, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    chatRoomList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        // Map Firestore data to model (or directly bind to RecyclerView)
                        ChatroomModel chatroom = document.toObject(ChatroomModel.class);
                        if (chatroom != null) {
                            chatRoomList.add(chatroom);
                        }
                    }
                    chatRoomAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    e.printStackTrace();
                });
    }

    /**
     * Helper method to get the current user ID.
     * This could be replaced by Firebase Authentication logic.
     */
    private String getCurrentUserId() {
        return "user123"; // Placeholder for current user ID, replace with actual logic
    }

    // Adapter class to handle chat rooms
    public static class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

        private final List<ChatroomModel> chatRooms;

        public ChatRoomAdapter(List<ChatroomModel> chatRooms) {
            this.chatRooms = chatRooms;
        }

        @Override
        public ChatRoomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            FragmentChatItemBinding itemBinding = FragmentChatItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ChatRoomViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(ChatRoomViewHolder holder, int position) {
            holder.bind(chatRooms.get(position));
        }

        @Override
        public int getItemCount() {
            return chatRooms.size();
        }

        public static class ChatRoomViewHolder extends RecyclerView.ViewHolder {

            private final FragmentChatItemBinding binding;

            public ChatRoomViewHolder(FragmentChatItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            public void bind(ChatroomModel chatRoom) {
                // Get the relevant data for the chat room
                // Assuming chatRoom contains user ids and last message info
                getUserData(chatRoom.getUserIds().get(0), user -> {
                    // Populate the view with data
                    binding.textViewUserName.setText(user.getUsername());
                    binding.textViewPhone.setText(user.getPhone());
                    binding.textViewLastMessage.setText(chatRoom.getLastMessage());
                    binding.textViewTimestamp.setText(chatRoom.getLastMessageTimestamp().toDate().toString());

                    // Set profile image for the chat room (use actual data)
                    binding.imageViewProfilePic.setImageResource(R.drawable.ic_person);
                });

                // Set up click listener to navigate to the chat room activity
                itemView.setOnClickListener(v -> {
                    // Pass the chatroom ID or any other necessary data to the ChatRoomActivity
                    // Start ChatRoomActivity (implement navigation logic here)
                });
            }

            /**
             * Fetch the user data for a given user ID.
             * @param userId The user ID.
             * @param callback Callback to handle user data.
             */
            private void getUserData(String userId, UserCallback callback) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection(Constants.KEY_COLLECTION_USERS)
                        .document(userId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                // Assuming the document contains the user information
                                UserModel user = documentSnapshot.toObject(UserModel.class);
                                callback.onSuccess(user);
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Handle error
                            e.printStackTrace();
                        });
            }

            // Interface for handling user data callback
            public interface UserCallback {
                void onSuccess(UserModel user);
            }
        }
    }
}