package com.example.ezchat.adapters;

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
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utils.AndroidUtil;
import com.example.ezchat.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

/**
 * A RecyclerView adapter for displaying a list of users retrieved from Firestore.
 * This adapter binds data from a {@link UserModel} object to the corresponding views
 * in the RecyclerView row layout.
 */
public class SearchUserRecyclerAdapter extends FirestoreRecyclerAdapter<UserModel, SearchUserRecyclerAdapter.UserModelViewHolder> {

    private final Context context;

    /**
     * Constructor for initializing the adapter with Firestore options and a context.
     * @param options FirestoreRecyclerOptions containing the query and the {@link UserModel} class.
     * @param context The context in which the adapter is used, typically an Activity or Fragment.
     */
    public SearchUserRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options, Context context) {
        super(options);
        this.context = context;
    }

    /**
     * Binds data from a {@link UserModel} to the provided {@link UserModelViewHolder}.
     * @param holder   The ViewHolder for the current item.
     * @param position The position of the current item in the list.
     * @param model    The {@link UserModel} object containing user data.
     */
    @Override
    protected void onBindViewHolder(@NonNull UserModelViewHolder holder, int position, @NonNull UserModel model) {
        // Set the username and phone number.
        holder.usernameText.setText(model.getUsername());
        holder.phoneText.setText(model.getPhone());

        // Append "(Me)" if the user ID matches the current user.
        if (model.getUserId().equals(FirebaseUtil.currentUserId())) {
            holder.usernameText.setText(model.getUsername() + " (Me)");
        }

        // Fetch and set the user's profile picture using Firebase Storage.
        FirebaseUtil.getOtherProfilePicStorageRef(model.getUserId()).getDownloadUrl()
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) {
                        Uri uri = t.getResult();
                        AndroidUtil.setProfilePic(context, uri, holder.profilePic);
                    }
                });
        // Set an onClick listener to navigate to the chat activity with the selected user's details.
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            AndroidUtil.passUserModelAsIntent(intent, model);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }
    /**
     * Creates a new {@link UserModelViewHolder} by inflating the row layout.
     * @param parent   The parent ViewGroup into which the new view will be added.
     * @param viewType The view type of the new View.
     * @return A new instance of {@link UserModelViewHolder}.
     */
    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_user_recycler_row, parent, false);
        return new UserModelViewHolder(view);
    }
    /**
     * A ViewHolder that holds references to the views in the RecyclerView row layout.
     * Used to display a single user's details.
     */
    class UserModelViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        TextView phoneText;
        ImageView profilePic;

        /**
         * Constructor that initializes view references for a single row.
         *
         * @param itemView The root view of the RecyclerView row layout.
         */
        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.user_name_text);
            phoneText = itemView.findViewById(R.id.phone_text);
            profilePic = itemView.findViewById(R.id.profile_pic_image_view);
        }
    }
}
