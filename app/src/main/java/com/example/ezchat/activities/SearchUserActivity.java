package com.example.ezchat.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezchat.R;
import com.example.ezchat.databinding.ActivityNewChatRoomRecyclerItemBinding;
import com.example.ezchat.databinding.ActivitySearchUserBinding;
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utilities.Utilities;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for searching users by username or phone number.
 */
public class SearchUserActivity extends AppCompatActivity {

    private final List<UserModel> userList = new ArrayList<>(); // List to hold users
    private ActivitySearchUserBinding binding; // View binding
    private FirebaseFirestore db; // Firestore instance
    private SearchUserRecyclerAdapter adapter; // Adapter for RecyclerView
    private String currentUserId; // ID of the currently logged-in user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivitySearchUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve the current user's ID
        currentUserId = getCurrentUserId();

        // Set up RecyclerView
        binding.searchUserRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchUserRecyclerAdapter(userList);
        binding.searchUserRecyclerView.setAdapter(adapter);

        // Set up the search button
        binding.searchUserBtn.setOnClickListener(v -> {
            String query = binding.searchUsernameInput.getText().toString().trim();
            if (!query.isEmpty()) {
                searchUsers(query);
            } else {
                Toast.makeText(this, "Enter a username or phone number to search.", Toast.LENGTH_SHORT).show();
            }
        });

        // Back button listener
        binding.backBtn.setOnClickListener(v -> onBackPressed());
    }

    /**
     * Searches for users in Firestore based on username or phone number.
     *
     * @param query The search query entered by the user.
     */
    private void searchUsers(String query) {
        String lowerQuery = query.toLowerCase().replaceAll("\\s", ""); // Normalize query

        db.collection(UserModel.FIELD_COLLECTION_NAME)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    userList.clear();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        UserModel user = document.toObject(UserModel.class);

                        if (user != null &&
                                (user.username.toLowerCase().contains(lowerQuery) ||
                                        user.phone.replaceAll("\\s", "").toLowerCase().contains(lowerQuery)) &&
                                !user.userId.equals(currentUserId)) {
                            userList.add(user);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (userList.isEmpty()) {
                        Toast.makeText(this, "No matching users found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error fetching users: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Retrieves the current user's ID.
     *
     * @return The ID of the current user.
     */
    private String getCurrentUserId() {
        // Replace with your preferred method of retrieving the user's ID
        return getSharedPreferences("APP_PREFS", MODE_PRIVATE).getString(UserModel.FIELD_USER_ID, "");
    }

    /**
     * Adapter for displaying search results in a RecyclerView.
     */
    public static class SearchUserRecyclerAdapter extends RecyclerView.Adapter<SearchUserRecyclerAdapter.UserViewHolder> {

        private final List<UserModel> userList;

        public SearchUserRecyclerAdapter(List<UserModel> userList) {
            this.userList = userList;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ActivityNewChatRoomRecyclerItemBinding binding = ActivityNewChatRoomRecyclerItemBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new UserViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            UserModel user = userList.get(position);
            holder.bind(user);
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }

        public static class UserViewHolder extends RecyclerView.ViewHolder {

            private final ActivityNewChatRoomRecyclerItemBinding binding;

            public UserViewHolder(ActivityNewChatRoomRecyclerItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            public void bind(UserModel user) {
                binding.userNameText.setText(user.username);
                binding.userPhoneText.setText(user.phone);

                // Load profile picture if available, otherwise set a placeholder
                if (user.profilePic != null && !user.profilePic.isEmpty()) {
                    Bitmap bitmap = Utilities.getBitmapFromEncodedString(user.profilePic);
                    binding.userProfileImage.setImageBitmap(bitmap);
                } else {
                    binding.userProfileImage.setImageResource(R.drawable.ic_person); // Placeholder
                }

                // Set item click listener
                itemView.setOnClickListener(v -> {
                    Context context = itemView.getContext();
                    Intent intent = new Intent(context, NewChatRoomActivity.class);
                    intent.putExtra(UserModel.FIELD_USER_ID, user.userId);
                    context.startActivity(intent);
                });
            }
        }
    }
}