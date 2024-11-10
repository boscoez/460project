package com.example.ezchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezchat.R;
import com.example.ezchat.model.ChatMessageModel;
import com.example.ezchat.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
<<<<<<< HEAD
public class ChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatMessageModel, ChatRecyclerAdapter.ChatModelViewHolder> {
    Context context;
=======

/**
 * This adapter class to bind the chat messages data to a RecyclerView in a chat application
 * More than that the extends Firestore RecyclerAdapter automatic data population from the Firebase Firestore
 */
public class ChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatMessageModel, ChatRecyclerAdapter.ChatModelViewHolder> {
    Context context;

    /**
     * Constructor for ChatRecyclerAdapter
     * @param options  FirestoreRecyclerOptions object containing options to configure firestore data
     * @param context  The context in which the adapter will operate, used for inflating layouts
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    public ChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatMessageModel> options, Context context) {
        super(options);
        this.context = context;
    }
<<<<<<< HEAD
=======

    /**
     * Binds a chat message which to the appropriate the view holder, determining whether the message
     *      should be displaed on the left or right
     * @param holder the view holder where the data should be bound
     * @param position Position of the item in the adapter
     * @param model     ChatMessageModel object containing data for a single chat message.
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    @Override
    protected void onBindViewHolder(@NonNull ChatModelViewHolder holder, int position, @NonNull ChatMessageModel model) {
        if(model.getSenderId().equals(FirebaseUtil.currentUserId())){
            holder.leftChatLayout.setVisibility(View.GONE);
            holder.rightChatLayout.setVisibility(View.VISIBLE);
            holder.rightChatTextview.setText(model.getMessage());
        }else {
            holder.rightChatLayout.setVisibility(View.GONE);
            holder.leftChatLayout.setVisibility(View.VISIBLE);
            holder.leftChatTextview.setText(model.getMessage());
        }
    }
<<<<<<< HEAD
=======

    /**
     *
     * Inflates the layout for each chat message row and returns a new ChatModelViewHolder
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return a new instance of ChatModelViewHolder containing this inflated view.
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    @NonNull
    @Override
    public ChatModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_message_recycler_row, parent, false);
        return new ChatModelViewHolder(view);
    }
<<<<<<< HEAD
=======

    /**
     * ViewHolder class for individual chat message items in the RecyclerView.\
     * Holds references to the views for both left (other user) and right (current user) chat layouts.
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    class ChatModelViewHolder extends RecyclerView.ViewHolder{
        LinearLayout leftChatLayout, rightChatLayout;
        TextView leftChatTextview, rightChatTextview;

<<<<<<< HEAD
=======
        /**
         * Constructor for ChatModelViewHolder.
         * @param itemView The view represneting an individual chat message row
         */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
        public ChatModelViewHolder(@NonNull View itemView){
            super(itemView);

            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
            rightChatTextview = itemView.findViewById(R.id.right_chat_textview);
            leftChatTextview = itemView.findViewById(R.id.left_chat_textview);
        }
    }
}


