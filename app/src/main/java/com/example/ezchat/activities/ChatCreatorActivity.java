package com.example.ezchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezchat.R;
import com.example.ezchat.databinding.ActivityChatCreatorBinding;
import com.example.ezchat.databinding.ActivityChatCreatorItemBinding;
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utilities.Constants;
import com.example.ezchat.utilities.PreferenceManager;
import com.example.ezchat.utilities.Utilities;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for selecting users to start a chat.
 */
public class ChatCreatorActivity extends AppCompatActivity {

    private ActivityChatCreatorBinding binding;
    private FirebaseFirestore db;
    private UserAdapter userAdapter;
    private final List<UserModel> userList = new ArrayList<>();
    private final List<String> selectedPhones = new ArrayList<>(); // List of selected phone numbers
    private String currentUserPhone; // Current user's phone number

    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivityChatCreatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firestore, SharedPreferences, and RecyclerView
        db = FirebaseFirestore.getInstance();
        preferenceManager = PreferenceManager.getInstance(getApplicationContext());
        currentUserPhone = preferenceManager.getString(Constants.PREF_KEY_PHONE);

        if (currentUserPhone == null || currentUserPhone.isEmpty()) {
            Utilities.showToast(this, "Failed to load current user.", Utilities.ToastType.ERROR);
            finish();
            return;
        }

        userAdapter = new UserAdapter(userList);
        binding.recyclerviewUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerviewUsers.setAdapter(userAdapter);

        // Fetch users to display in the list
        fetchUsers();

        // Set up Start Chat button
        binding.btnStartChat.setOnClickListener(v -> {
            if (!selectedPhones.isEmpty()) {
                navigateToChat();
            } else {
                Utilities.showToast(this, "Please select at least one user.", Utilities.ToastType.WARNING);
            }
        });

        // Set up Back button
        binding.btnBack.setOnClickListener(v -> onBackPressed());
    }

    /**
     * Fetches all users from Firestore and populates the RecyclerView, excluding the current user.
     */
    private void fetchUsers() {
        db.collection(Constants.USER_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    userList.clear(); // Clear the list before adding users
                    for (DocumentSnapshot document : querySnapshot) {
                        UserModel user = document.toObject(UserModel.class);
                        if (user != null && !user.getPhone().equals(currentUserPhone)) {
                            userList.add(user); // Add only other users
                        }
                    }
                    if (userList.isEmpty()) {
                        Utilities.showToast(this, "No other users available to chat with.", Utilities.ToastType.INFO);
                    }
                    userAdapter.notifyDataSetChanged(); // Notify the adapter of data changes
                })
                .addOnFailureListener(e -> Utilities.showToast(this, "Failed to fetch users: " + e.getMessage(), Utilities.ToastType.ERROR));
    }

    /**
     * Navigates to the ChatActivity, passing selected users for chat creation.
     */
    private void navigateToChat() {
        // Add the current user's phone to the list of selected phones
        if (!selectedPhones.contains(currentUserPhone)) {
            selectedPhones.add(currentUserPhone);
        }

        // Pass selected phones to ChatActivity for message sending and chat creation
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putStringArrayListExtra(Constants.USER_COLLECTION, new ArrayList<>(selectedPhones)); // Pass the selected phones
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    /**
     * Adapter for displaying users in a RecyclerView for chat creation.
     */
    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

        private final List<UserModel> userList;

        UserAdapter(List<UserModel> userList) {
            this.userList = userList;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new UserViewHolder(ActivityChatCreatorItemBinding.inflate(getLayoutInflater(), parent, false));
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

        /**
         * ViewHolder for displaying a single user's details.
         */
        private class UserViewHolder extends RecyclerView.ViewHolder {

            private final ActivityChatCreatorItemBinding itemBinding;

            UserViewHolder(ActivityChatCreatorItemBinding binding) {
                super(binding.getRoot());
                this.itemBinding = binding;
            }

            void bind(UserModel user) {
                itemBinding.userNameText.setText(user.getUsername());
                itemBinding.textviewPhone.setText(user.getPhone());

                // Set a placeholder or user profile picture
                if (user.getProfilePic() != null && !user.getProfilePic().isEmpty()) {
                    itemBinding.roundedviewProfilePic.setImageBitmap(Utilities.decodeImage(user.getProfilePic()));
                } else {
                    itemBinding.roundedviewProfilePic.setImageResource(R.drawable.ic_person);
                }

                // Update the checkbox state
                itemBinding.userCheckBox.setChecked(selectedPhones.contains(user.getPhone()));

                // Handle checkbox toggle
                itemBinding.userCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        if (!selectedPhones.contains(user.getPhone())) {
                            selectedPhones.add(user.getPhone()); // Add to the list of selected phones
                        }
                    } else {
                        selectedPhones.remove(user.getPhone()); // Remove from the list of selected phones
                    }

                    // Enable or disable the Start Chat button based on selection
                    binding.btnStartChat.setEnabled(!selectedPhones.isEmpty());
                });
            }
        }
    }
}