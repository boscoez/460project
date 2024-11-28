package com.example.ezchat.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezchat.NewChatActivity;
import com.example.ezchat.R;
import com.example.ezchat.databinding.SearchUserRecyclerRowBinding;
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utilities.AndroidUtil;
import com.example.ezchat.utilities.Constants;
import com.example.ezchat.utilities.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

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
        // Use binding for populating the data
        //holder.binding.setUser(model);

        // Check if the current user is displayed
        if (model.getUserId().equals(FirebaseUtil.currentUserId())) {
            holder.binding.textViewUsername.setText(model.getUsername() + " (Me)");
        }

        // Fetch and set the user's profile picture using Firebase Storage
        FirebaseUtil.getOtherProfilePicStorageRef(model.getUserId()).getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri uri = task.getResult();
                        AndroidUtil.setProfilePic(context, uri, holder.binding.profilePic);
                    }
                });

        // Set the click listener to open the new chat activity when a user is clicked
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, NewChatActivity.class);
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
        SearchUserRecyclerRowBinding binding = SearchUserRecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UserModelViewHolder(binding);
    }

    /**
     * A ViewHolder that holds references to the views in the RecyclerView row layout.
     * Used to display a single user's details.
     */
    public class UserModelViewHolder extends RecyclerView.ViewHolder {
        private final SearchUserRecyclerRowBinding binding;

        /**
         * Constructor that initializes view references for a single row.
         *
         * @param binding The binding object to access the views in the layout.
         */
        public UserModelViewHolder(SearchUserRecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}