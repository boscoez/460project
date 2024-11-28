package com.example.ezchat.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezchat.R;
import com.example.ezchat.activities.NewChatRoomActivity;
import com.example.ezchat.databinding.SearchUserRecyclerRowBinding;
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utilities.AndroidUtil;
import com.example.ezchat.utilities.FirebaseUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerAdapter for searching and displaying users in a case-insensitive way,
 * with partial query matching on username or phone.
 */
public class SearchUserRecyclerAdapter extends RecyclerView.Adapter<SearchUserRecyclerAdapter.UserModelViewHolder> {

    private final Context context;
    private final FirebaseFirestore db;
    private final List<UserModel> userList;
    private String currentUserId;

    /**
     * Constructor for initializing the adapter.
     *
     * @param context The context in which the adapter is used, typically an Activity or Fragment.
     */
    public SearchUserRecyclerAdapter(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.userList = new ArrayList<>();
        this.currentUserId = FirebaseUtil.currentUserId();
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
                .orderBy(UserModel.FIELD_USERNAME) // Sorting for better UX
                .startAt(lowerQuery) // Match start of fields
                .endAt(lowerQuery + "\uf8ff") // End matching
                .get()
                .addOnSuccessListener(snapshots -> {
                    userList.clear();
                    for (DocumentSnapshot document : snapshots.getDocuments()) {
                        UserModel user = document.toObject(UserModel.class);

                        // Include user if username or phone matches the query
                        if (user != null && (user.username.toLowerCase().contains(lowerQuery)
                                || user.phone.toLowerCase().contains(lowerQuery))
                                && !user.userId.equals(currentUserId)) {
                            userList.add(user);
                        }
                    }
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    AndroidUtil.showToast(context, "Failed to search users: " + e.getMessage());
                });
    }

    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SearchUserRecyclerRowBinding binding = SearchUserRecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UserModelViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserModelViewHolder holder, int position) {
        UserModel model = userList.get(position);

        // Set username, append "(Me)" if the user is the current user
        holder.binding.textViewUsername.setText(
                model.username + (model.userId.equals(currentUserId) ? " (Me)" : "")
        );

        // Fetch and set the user's profile picture
        FirebaseUtil.getOtherProfilePicStorageRef(model.userId).getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Uri uri = task.getResult();
                        AndroidUtil.setProfilePic(context, uri, holder.binding.profilePic);
                    } else {
                        holder.binding.profilePic.setImageResource(R.drawable.ic_person); // Placeholder
                    }
                });

        // Set up click listener to open the NewChatRoomActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, NewChatRoomActivity.class);
            AndroidUtil.passUserModelAsIntent(intent, model);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * ViewHolder class for displaying a single user's details in the RecyclerView.
     */
    public class UserModelViewHolder extends RecyclerView.ViewHolder {
        private final SearchUserRecyclerRowBinding binding;

        public UserModelViewHolder(SearchUserRecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}