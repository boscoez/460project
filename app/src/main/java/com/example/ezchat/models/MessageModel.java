package com.example.ezchat.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezchat.databinding.ActivityChatRoomRecyclerItemBinding;
import com.google.firebase.Timestamp;

import java.util.List;

/**
 * Represents a single message exchanged between users in a chat room.
 * It includes details such as sender, receiver, message content, timestamp, and message status.
 */
public class MessageModel {

    // Firestore field constants for MessageModel
    public static final String FIELD_COLLECTION_NAME = "messages";
    public static final String FIELD_SENDER_PHONE = "senderPhone";
    public static final String FIELD_RECEIVER_PHONE = "receiverPhone";
    public static final String FIELD_MESSAGE = "message";
    public static final String FIELD_TIMESTAMP = "timestamp";
    public static final String FIELD_STATUS = "status";

    /**
     * The phone number of the user who sent the message.
     */
    public String senderPhone;

    /**
     * The phone number of the user who received the message.
     */
    public String receiverPhone;

    /**
     * The content of the message.
     */
    public String message;

    /**
     * The timestamp when the message was sent.
     */
    public Timestamp timestamp;

    /**
     * The status of the message (e.g., sent, delivered, read).
     */
    public String status;

    /**
     * Default constructor required for Firebase Firestore to deserialize data.
     */
    public MessageModel() {
    }

    /**
     * Constructs a new MessageModel with the specified sender phone, receiver phone,
     * message content, timestamp, and status.
     *
     * @param senderPhone   The phone number of the user who sent the message.
     * @param receiverPhone The phone number of the user who received the message.
     * @param message       The content of the message.
     * @param timestamp     The timestamp when the message was sent.
     * @param status        The status of the message (e.g., sent, delivered, read).
     */
    public MessageModel(String senderPhone, String receiverPhone, String message, Timestamp timestamp, String status) {
        this.senderPhone = senderPhone;
        this.receiverPhone = receiverPhone;
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
    }

    /**
     * Adapter class for displaying a list of messages in a RecyclerView.
     */
    public static class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

        private final List<MessageModel> messageList; // List of messages to display
        private final String currentUserPhone; // The phone number of the currently logged-in user
        private final Context context; // Context for accessing resources

        /**
         * Constructs a new MessageAdapter.
         *
         * @param context           The context of the activity or fragment.
         * @param messageList       List of messages to display in the RecyclerView.
         * @param currentUserPhone  The phone number of the currently logged-in user.
         */
        public MessageAdapter(Context context, List<MessageModel> messageList, String currentUserPhone) {
            this.context = context;
            this.messageList = messageList;
            this.currentUserPhone = currentUserPhone;
        }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ActivityChatRoomRecyclerItemBinding binding = ActivityChatRoomRecyclerItemBinding.inflate(inflater, parent, false);
            return new MessageViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            MessageModel message = messageList.get(position);
            holder.bind(message);
        }

        @Override
        public int getItemCount() {
            return messageList.size();
        }

        /**
         * ViewHolder class for individual message items.
         */
        public class MessageViewHolder extends RecyclerView.ViewHolder {

            private final ActivityChatRoomRecyclerItemBinding binding;

            /**
             * Constructs a new MessageViewHolder.
             *
             * @param binding The binding for the chat message item layout.
             */
            public MessageViewHolder(@NonNull ActivityChatRoomRecyclerItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            /**
             * Binds the message data to the view, adjusting the layout for sent and received messages.
             *
             * @param message The message to display.
             */
            public void bind(MessageModel message) {
                boolean isSentByCurrentUser = message.senderPhone.equals(currentUserPhone);

                if (isSentByCurrentUser) {
                    // Sent message layout
                    binding.messageLayoutRight.setVisibility(View.VISIBLE);
                    binding.messageLayoutLeft.setVisibility(View.GONE);

                    binding.messageTextRight.setText(message.message);
                    binding.timestampTextRight.setText(formatTimestamp(message.timestamp));
                } else {
                    // Received message layout
                    binding.messageLayoutRight.setVisibility(View.GONE);
                    binding.messageLayoutLeft.setVisibility(View.VISIBLE);

                    binding.messageTextLeft.setText(message.message);
                    binding.timestampTextLeft.setText(formatTimestamp(message.timestamp));
                }
            }

            /**
             * Formats the timestamp into a user-friendly string.
             *
             * @param timestamp The timestamp to format.
             * @return A formatted timestamp string.
             */
            private String formatTimestamp(Timestamp timestamp) {
                if (timestamp == null) return "";
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a");
                return sdf.format(timestamp.toDate());
            }
        }
    }
}