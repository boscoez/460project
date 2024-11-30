package com.example.ezchat.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezchat.R;
import com.example.ezchat.activities.ChatCreatorActivity;
import com.example.ezchat.databinding.ActivitySearchUserItemBinding;
import com.example.ezchat.models.UserModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerAdapter for searching and displaying users in a case-insensitive way,
 * with partial query matching on username or phone.
 */
public class SearchUserRecyclerAdapter extends RecyclerView.Adapter<SearchUserRecyclerAdapter.UserModelViewHolder> {

    private final Context context; // Context for adapter
    private final FirebaseFirestore db; // Firestore instance
    private final List<UserModel> userList; // List of users matching the search
    private final String currentUserPhone; // Current user's phone number

    /**
     * Constructor for initializing the adapter.
     *
     * @param context The context in which the adapter is used, typically an Activity or Fragment.
     * @param currentUserPhone The phone number of the currently logged-in user.
     */
    public SearchUserRecyclerAdapter(Context context, String currentUserPhone) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.userList = new ArrayList<>();
        this.currentUserPhone = currentUserPhone;
    }

    /**
     * Performs the search query and updates the RecyclerView data.
     *
     * @param query The user's search input.
     */
    public void searchUsers(String query) {
        String lowerQuery = query.toLowerCase();

        // Firestore query: search username and phone
        db.collection(UserModel.FIELD_COLLECTION_NAME)
                .orderBy(UserModel.FIELD_USERNAME)
                .startAt(lowerQuery)
                .endAt(lowerQuery + "\uf8ff")
                .get()
                .addOnSuccessListener(snapshots -> {
                    userList.clear();
                    for (DocumentSnapshot document : snapshots.getDocuments()) {
                        UserModel user = document.toObject(UserModel.class);

                        // Include user if username or phone matches the query and exclude the current user
                        if (user != null && (user.username.toLowerCase().contains(lowerQuery)
                                || user.phone.toLowerCase().contains(lowerQuery))
                                && !user.phone.equals(currentUserPhone)) {
                            userList.add(user);
                        }
                    }
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Log and notify error
                    e.printStackTrace();
                    notifyError("Failed to search users: " + e.getMessage());
                });
    }

    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ActivitySearchUserItemBinding binding = ActivitySearchUserItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UserModelViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserModelViewHolder holder, int position) {
        UserModel model = userList.get(position);

        // Set the user's name
        holder.binding.textViewUsername.setText(model.username);

        // Set the profile picture or placeholder
        if (model.profilePic != null && !model.profilePic.isEmpty()) {
            Uri uri = Uri.parse(model.profilePic);
            holder.binding.profilePic.setImageURI(uri);
        } else {
            holder.binding.profilePic.setImageResource(R.drawable.ic_person);
        }

        // Set up click listener to open the ChatCreatorActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatCreatorActivity.class);
            intent.putExtra(UserModel.FIELD_PHONE, model.phone); // Pass phone as identifier
            intent.putExtra(UserModel.FIELD_USERNAME, model.username); // Pass username
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * Displays a toast for errors during Firestore operations.
     *
     * @param message The error message to display.
     */
    private void notifyError(String message) {
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    /**
     * ViewHolder class for displaying a single user's details in the RecyclerView.
     */
    public static class UserModelViewHolder extends RecyclerView.ViewHolder {
        private final ActivitySearchUserItemBinding binding;

        public UserModelViewHolder(ActivitySearchUserItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}