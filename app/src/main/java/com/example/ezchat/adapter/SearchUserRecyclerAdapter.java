package com.example.ezchat.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezchat.ChatActivity;
import com.example.ezchat.R;
import com.example.ezchat.model.UserModel;
import com.example.ezchat.utils.AndroidUtil;
import com.example.ezchat.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

<<<<<<< HEAD
public class SearchUserRecyclerAdapter extends FirestoreRecyclerAdapter<UserModel, SearchUserRecyclerAdapter.UserModelViewHolder> {
    Context context;
=======
/**
 * Adapter class to bind user data to a RecyclerView in a user search feature.
 * Extends the  FirestoreRecyclerAdapter to support automatic data population from Firebase Firestore.
 */
public class SearchUserRecyclerAdapter extends FirestoreRecyclerAdapter<UserModel, SearchUserRecyclerAdapter.UserModelViewHolder> {
    Context context;

    /**
     * Constructor for SearchUserRecyclerAdapter
     * @param options  FirestoreRecyclerOptions object with options to configure Firestore data retrieval.
     * @param context  The context in which the adapter will operate, used for layout inflation and starting activities.
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    public SearchUserRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options, Context context) {
        super(options);
        this.context = context;
    }
<<<<<<< HEAD
=======

    /**
     * Binds a user item to the ViewHolder, displaying user details and setting up an intent to start
     * ChatActivity when a user item is clicked
     * @param holder        The ViewHolder to bind data to.
     * @param position      Position of the item in the adapter.
     * @param model         UserModel object containing data for a single user.
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    @Override
    protected void onBindViewHolder(@NonNull UserModelViewHolder holder, int position, @NonNull UserModel model) {
        // Set username and phone, adding null checks
        holder.usernameText.setText(model.getUsername() != null ? model.getUsername() : "Unknown User");
        holder.phoneText.setText(model.getPhone() != null ? model.getPhone() : "No Phone");

        // Get the current user ID and add a null check before calling equals()
        String currentUserId = FirebaseUtil.currentUserId();
        if (model.getUserId() != null && currentUserId != null && model.getUserId().equals(currentUserId)) {
            holder.usernameText.setText(model.getUsername() + " (Me)");
        }else {
            holder.usernameText.setText(model.getUsername() != null ? model.getUsername() : "Unknown User");
        }

        // Set up item click listener for starting ChatActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            AndroidUtil.passUserModelAsIntent(intent, model);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }
<<<<<<< HEAD
=======

    /**
     * Inflates the layout for each user item row and returns a new UserModelViewHolder
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new instance of UserModelViewHolder containing the inflated view.
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_user_recycler_row, parent, false);
        return new UserModelViewHolder(view);
    }
<<<<<<< HEAD
=======

    /**
     *  ViewHolder class for individual user items in the RecyclerView.
     *  Holds references to the views for displaying a user's name, phone number, and profile picture.
     *
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    static class UserModelViewHolder extends RecyclerView.ViewHolder{
        TextView usernameText;
        TextView phoneText;
        ImageView profilePic;

<<<<<<< HEAD
=======
        /**
         * Constructor for UserModelViewHolder
         * @param itemView The View representing an individual user item row.
         */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
        public UserModelViewHolder(@NonNull View itemView){
            super(itemView);
            usernameText = itemView.findViewById(R.id.user_name_text);
            phoneText = itemView.findViewById(R.id.phone_text);
            profilePic = itemView.findViewById(R.id.profile_pic_image_view);
        }
    }
}


