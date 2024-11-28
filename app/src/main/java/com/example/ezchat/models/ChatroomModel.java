package com.example.ezchat.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezchat.databinding.FragmentChatRoomsItemBinding;
import com.google.firebase.Timestamp;

import java.util.List;

/**
 * The ChatroomModel class represents a chat room in the application.
 * It includes details about participants, the creator, metadata, and the last message.
 */
public class ChatroomModel {

    /**
     * Firebase Firestore key constants for the ChatroomModel fields.
     */
    public static  final String FIELD_COLLECTION_NAME = "chatroom";
    public static final String FIELD_CHATROOM_ID = "chatroomId";
    public static final String FIELD_USER_IDS = "userIds";
    public static final String FIELD_CREATOR_ID = "creatorId";
    public static final String FIELD_LAST_MESSAGE = "lastMessage";
    public static final String FIELD_LAST_MESSAGE_TIMESTAMP = "lastMessageTimestamp";
    public static final String FIELD_LAST_MESSAGE_SENDER_ID = "lastMessageSenderId";

    /**
     * The unique identifier for the chat room.
     */
    public String chatroomId;

    /**
     * The list of user IDs participating in the chat room.
     */
    public List<String> userIds;

    /**
     * The ID of the user who created the chat room.
     */
    public String creatorId;

    /**
     * The content of the last message in the chat room.
     */
    public String lastMessage;

    /**
     * The timestamp of the last message in the chat room.
     */
    public Timestamp lastMessageTimestamp;

    /**
     * The ID of the user who sent the last message.
     */
    public String lastMessageSenderId;

    /**
     * Default constructor required for Firebase Firestore to deserialize data.
     */
    public ChatroomModel() {
    }

    /**
     * Constructs a new ChatroomModel with the specified fields.
     *
     * @param chatroomId           The unique identifier for the chat room.
     * @param userIds              The list of user IDs in the chat room.
     * @param creatorId            The ID of the user who created the chat room.
     * @param lastMessage          The content of the last message.
     * @param lastMessageTimestamp The timestamp of the last message.
     * @param lastMessageSenderId  The ID of the user who sent the last message.
     */
    public ChatroomModel(String chatroomId, List<String> userIds, String creatorId, String lastMessage,
                         Timestamp lastMessageTimestamp, String lastMessageSenderId) {
        this.chatroomId = chatroomId;
        this.userIds = userIds;
        this.creatorId = creatorId;
        this.lastMessage = lastMessage;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.lastMessageSenderId = lastMessageSenderId;
    }

    /**
     * Adapter class for displaying chat rooms in a RecyclerView.
     */
    public static class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

        private final List<ChatroomModel> chatRoomList; // List of chat rooms to display
        private final Context context; // Context for accessing resources
        private final OnChatRoomClickListener listener; // Listener for handling chat room clicks

        /**
         * Constructs a new ChatRoomAdapter.
         *
         * @param context       The context of the activity or fragment.
         * @param chatRoomList  List of chat rooms to display in the RecyclerView.
         * @param listener      Listener for handling chat room click events.
         */
        public ChatRoomAdapter(Context context, List<ChatroomModel> chatRoomList, OnChatRoomClickListener listener) {
            this.context = context;
            this.chatRoomList = chatRoomList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            FragmentChatRoomsItemBinding binding = FragmentChatRoomsItemBinding.inflate(inflater, parent, false);
            return new ChatRoomViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
            holder.bind(chatRoomList.get(position));
        }

        @Override
        public int getItemCount() {
            return chatRoomList.size();
        }

        /**
         * ViewHolder class for individual chat room items.
         */
        public class ChatRoomViewHolder extends RecyclerView.ViewHolder {

            private final FragmentChatRoomsItemBinding binding;

            /**
             * Constructs a new ChatRoomViewHolder.
             *
             * @param binding The binding for the chat room item layout.
             */
            public ChatRoomViewHolder(@NonNull FragmentChatRoomsItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            /**
             * Binds the chat room data to the view.
             *
             * @param chatRoom The chat room data to display.
             */
            public void bind(ChatroomModel chatRoom) {
                binding.textViewUserName.setText(chatRoom.lastMessageSenderId); // Placeholder, fetch actual user data
                binding.textViewLastMessage.setText(chatRoom.lastMessage);
                binding.textViewTimestamp.setText(formatTimestamp(chatRoom.lastMessageTimestamp));

                // Click listener to handle navigation or actions
                binding.getRoot().setOnClickListener(v -> listener.onChatRoomClick(chatRoom.chatroomId));
            }

            /**
             * Formats the timestamp into a user-friendly string.
             *
             * @param timestamp The timestamp to format.
             * @return A formatted timestamp string.
             */
            private String formatTimestamp(Timestamp timestamp) {
                if (timestamp == null) return "";
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM d, h:mm a");
                return sdf.format(timestamp.toDate());
            }
        }

        /**
         * Listener interface for handling chat room click events.
         */
        public interface OnChatRoomClickListener {
            void onChatRoomClick(String chatRoomId);
        }
    }
}