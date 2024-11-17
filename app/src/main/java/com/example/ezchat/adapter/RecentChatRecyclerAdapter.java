package com.example.ezchat.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezchat.ChatActivity;
import com.example.ezchat.R;
import com.example.ezchat.model.ChatroomModel;
import com.example.ezchat.model.UserModel;
import com.example.ezchat.utils.AndroidUtil;
import com.example.ezchat.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
/**
 * RecyclerView Adapter for displaying a list of recent chats in a chatroom.
 * Extends FirestoreRecyclerAdapter to handle Firestore-based data binding.
 */
public class RecentChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder> {
    private final Context context;
    /**
     * Constructor to initialize the adapter with Firestore options and context.
     * @param options FirestoreRecyclerOptions for binding chatroom data.
     * @param context Application context for resource access and navigation.
     */
    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatroomModel> options, Context context) {
        super(options);
        this.context = context;
    }
    /**
     * Binds the data of a single chatroom to the corresponding ViewHolder.
     * @param holder   ViewHolder for the chatroom.
     * @param position Position of the chatroom in the list.
     * @param model    ChatroomModel containing the chatroom data.
     */
    @Override
    protected void onBindViewHolder(@NonNull ChatroomModelViewHolder holder, int position, @NonNull ChatroomModel model) {
        // Fetch other user information from the chatroom user IDs.
        FirebaseUtil.getOtherUserFromChatroom(model.getUserIds())
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean lastMessageSentByMe = model.getLastMessageSenderId().equals(FirebaseUtil.currentUserId());

                        // Retrieve the other user's details.
                        UserModel otherUserModel = task.getResult().toObject(UserModel.class);

                        // Fetch and display the other user's profile picture.
                        FirebaseUtil.getOtherProfilePicStorageRef(otherUserModel.getUserId()).getDownloadUrl()
                                .addOnCompleteListener(t -> {
                                    if (t.isSuccessful()) {
                                        Uri uri = t.getResult();
                                        AndroidUtil.setProfilePic(context, uri, holder.profilePic);
                                    }
                                });

                        // Set the other user's username and last message text.
                        holder.usernameText.setText(otherUserModel.getUsername());
                        if (lastMessageSentByMe) {
                            holder.lastMessageText.setText("You : " + model.getLastMessage());
                        } else {
                            holder.lastMessageText.setText(model.getLastMessage());
                        }

                        // Format and display the timestamp of the last message.
                        holder.lastMessageTime.setText(FirebaseUtil.timestampToString(model.getLastMessageTimestamp()));

                        // Set a click listener to navigate to the chat activity.
                        holder.itemView.setOnClickListener(v -> {
                            Intent intent = new Intent(context, ChatActivity.class);
                            AndroidUtil.passUserModelAsIntent(intent, otherUserModel);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        });
                    }
                });
    }
    /**
     * Creates a new ViewHolder for displaying a chatroom item.
     * @param parent   The parent ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new ChatroomModelViewHolder instance.
     */
    @NonNull
    @Override
    public ChatroomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for the recent chat row.
        View view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row, parent, false);
        return new ChatroomModelViewHolder(view);
    }
    /**
     * ViewHolder class for displaying individual chatroom details.
     */
    class ChatroomModelViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;      // TextView for displaying the username.
        TextView lastMessageText;  // TextView for displaying the last message.
        TextView lastMessageTime;  // TextView for displaying the timestamp of the last message.
        ImageView profilePic;      // ImageView for displaying the user's profile picture.
        /**
         * Constructor to initialize the ViewHolder with UI components.
         * @param itemView The root view of the individual chatroom item.
         */
        public ChatroomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.user_name_text);
            lastMessageText = itemView.findViewById(R.id.last_message_text);
            lastMessageTime = itemView.findViewById(R.id.last_message_time_text);
            profilePic = itemView.findViewById(R.id.profile_pic_image_view);
        }
    }
}