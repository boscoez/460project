package com.example.ezchat.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezchat.R;
import com.example.ezchat.databinding.ActivityChatCreatorItemBinding;
import com.example.ezchat.databinding.ActivitySearchUserBinding;
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utilities.PreferenceManager;
import com.example.ezchat.utilities.Utilities;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Activity for searching users by username or phone number.
 * Users can select a contact to start a new chat room.
 */
public class SearchUserActivity extends AppCompatActivity {

    private final List<UserModel> userList = new ArrayList<>(); // List of users
    private final Set<UserModel> selectedUsers = new HashSet<>(); // Set of selected users
    private ActivitySearchUserBinding binding; // View binding
    private FirebaseFirestore db; // Firestore instance
    private SearchUserRecyclerAdapter adapter; // Adapter for RecyclerView
    private String currentUserPhone; // Current user's phone number

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivitySearchUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve current user's phone number
        currentUserPhone = getCurrentUserPhone();

        // Set up RecyclerView
        binding.searchUserRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchUserRecyclerAdapter(userList, selectedUsers, this::updateStartChatButtonVisibility);
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

        // Set up the "Start Chat" button
        binding.startChatBtn.setOnClickListener(v -> {
            if (selectedUsers.isEmpty()) {
                Toast.makeText(this, "No users selected.", Toast.LENGTH_SHORT).show();
            } else {
                startChatWithSelectedUsers();
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
                                !user.phone.equals(currentUserPhone)) {
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
     * Starts a chat with the selected users.
     */
    private void startChatWithSelectedUsers() {
        ArrayList<UserModel> selectedUserList = new ArrayList<>(selectedUsers);

        Intent intent = new Intent(this, ChatActivity.class);
        //intent.putParcelableArrayListExtra("selectedUsers", (ArrayList<? extends Parcelable>) selectedUserList); // Pass selected users
        //startActivity(intent);
    }

    /**
     * Updates the visibility of the "Start Chat" button based on selected users.
     */
    private void updateStartChatButtonVisibility() {
        binding.startChatBtn.setVisibility(selectedUsers.isEmpty() ? View.GONE : View.VISIBLE);
    }

    /**
     * Retrieves the current user's phone number from SharedPreferences.
     *
     * @return The phone number of the current user.
     */
    private String getCurrentUserPhone() {
        return getSharedPreferences(PreferenceManager.KEY_PREFERENCE_NAME, MODE_PRIVATE).getString(UserModel.FIELD_PHONE, "");
    }

    /**
     * Adapter for displaying search results in a RecyclerView.
     */
    public static class SearchUserRecyclerAdapter extends RecyclerView.Adapter<SearchUserRecyclerAdapter.UserViewHolder> {

        private final List<UserModel> userList;
        private final Set<UserModel> selectedUsers;
        private final Runnable onSelectionChanged;

        public SearchUserRecyclerAdapter(List<UserModel> userList, Set<UserModel> selectedUsers, Runnable onSelectionChanged) {
            this.userList = userList;
            this.selectedUsers = selectedUsers;
            this.onSelectionChanged = onSelectionChanged;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ActivityChatCreatorItemBinding binding = ActivityChatCreatorItemBinding.inflate(
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

        public class UserViewHolder extends RecyclerView.ViewHolder {

            private final ActivityChatCreatorItemBinding binding;

            public UserViewHolder(ActivityChatCreatorItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            public void bind(UserModel user) {
                binding.userNameText.setText(user.username);
                binding.userPhoneText.setText(user.phone);

                // Load profile picture
                if (user.profilePic != null && !user.profilePic.isEmpty()) {
                    Bitmap bitmap = Utilities.decodeImage(user.profilePic);
                    binding.userProfileImage.setImageBitmap(bitmap);
                } else {
                    binding.userProfileImage.setImageResource(R.drawable.ic_person);
                }

                // Handle checkbox selection
                binding.userCheckBox.setOnCheckedChangeListener(null); // Prevent triggering listener on bind
                binding.userCheckBox.setChecked(selectedUsers.contains(user));
                binding.userCheckBox.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                    if (isChecked) {
                        selectedUsers.add(user);
                    } else {
                        selectedUsers.remove(user);
                    }
                    onSelectionChanged.run();
                });
            }
        }
    }
}