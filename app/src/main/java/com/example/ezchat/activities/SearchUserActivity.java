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
import com.example.ezchat.utilities.Constants;
import com.example.ezchat.utilities.PreferenceManager;
import com.example.ezchat.utilities.Utilities;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Activity for searching users by username, email, or phone number.
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
        binding.recyclerviewUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchUserRecyclerAdapter(userList, selectedUsers, this::updateStartChatButtonVisibility);
        binding.recyclerviewUsers.setAdapter(adapter);

        // Set up the search button
        binding.btnSearchUsers.setOnClickListener(v -> {
            String query = binding.editTextUsername.getText().toString().trim();
            if (!query.isEmpty()) {
                searchUsers(query);
            } else {
                Toast.makeText(this, "Enter a username, email, or phone number to search.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the "Start Chat" button
        binding.btnStartChat.setOnClickListener(v -> {
            if (selectedUsers.isEmpty()) {
                Toast.makeText(this, "No users selected.", Toast.LENGTH_SHORT).show();
            } else {
                startChatWithSelectedUsers();
            }
        });

        // Back button listener
        binding.btnBack.setOnClickListener(v -> onBackPressed());
    }

    /**
     * Searches for users in Firestore based on username, email, or phone number.
     *
     * @param query The search query entered by the user.
     */
    private void searchUsers(String query) {
        String lowerQuery = query.toLowerCase().replaceAll("\\s", ""); // Normalize query

        db.collection(Constants.USER_COLLECTION)  // Use the correct collection name from Constants
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    userList.clear();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        UserModel user = document.toObject(UserModel.class);

                        // Check if the user's username, phone, or email matches the query
                        if (user != null &&
                                (user.getUsername().toLowerCase().contains(lowerQuery) ||
                                        user.getPhone().replaceAll("\\s", "").toLowerCase().contains(lowerQuery) ||
                                        (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery))) &&
                                !user.getPhone().equals(currentUserPhone)) { // Exclude current user
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
        // Create a list with the phone numbers of the selected users
        List<String> selectedUserPhones = new ArrayList<>();
        for (UserModel user : selectedUsers) {
            selectedUserPhones.add(user.getPhone());
        }

        // Add the current user to the list of participants (phone number)
        selectedUserPhones.add(currentUserPhone);

        // Start the ChatActivity and pass the selected users' phone numbers
        Intent intent = new Intent(SearchUserActivity.this, ChatActivity.class);
        intent.putStringArrayListExtra(Constants.FIELD_PHONE_NUMBERS, (ArrayList<String>) selectedUserPhones); // Pass selected users' phone numbers
        startActivity(intent);
    }

    /**
     * Updates the visibility of the "Start Chat" button based on selected users.
     */
    private void updateStartChatButtonVisibility() {
        binding.btnStartChat.setVisibility(selectedUsers.isEmpty() ? View.GONE : View.VISIBLE);
    }

    /**
     * Retrieves the current user's phone number from SharedPreferences.
     *
     * @return The phone number of the current user.
     */
    private String getCurrentUserPhone() {
        return getSharedPreferences(PreferenceManager.KEY_PREFERENCE_NAME, MODE_PRIVATE)
                .getString(Constants.PREF_KEY_PHONE, "");
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
                binding.userNameText.setText(user.getUsername());
                binding.textviewPhone.setText(user.getPhone());

                // Load profile picture
                if (user.getProfilePic() != null && !user.getProfilePic().isEmpty()) {
                    Bitmap bitmap = Utilities.decodeImage(user.getProfilePic());
                    binding.roundedviewProfilePic.setImageBitmap(bitmap);
                } else {
                    binding.roundedviewProfilePic.setImageResource(R.drawable.ic_person);
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