package com.example.ezchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezchat.R;
import com.example.ezchat.models.ChatModel;
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utilities.Constants;
import com.example.ezchat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatCreatorActivity extends AppCompatActivity {

    private static final String TAG = "ChatCreatorActivity";
    private RecyclerView recyclerViewUsers;
    private ImageButton btnStartChat;
    private ProgressBar progressBar;
    private UserAdapter userAdapter;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;

    private List<UserModel> userList = new ArrayList<>();
    private Set<String> selectedUserPhones = new HashSet<>();
    private String currentUserPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_creator);

        preferenceManager = PreferenceManager.getInstance(getApplicationContext());
        database = FirebaseFirestore.getInstance();

        currentUserPhone = getIntent().getStringExtra(Constants.FIELD_PHONE);
        if (currentUserPhone == null || currentUserPhone.isEmpty()) {
            Log.e(TAG, "Current user phone is missing.");
            finish();
            return;
        }

        initializeViews();
        loadUsers();
        setupListeners();
    }

    private void initializeViews() {
        recyclerViewUsers = findViewById(R.id.recyclerviewUsers);
        btnStartChat = findViewById(R.id.btnStartChat);
        progressBar = findViewById(R.id.chatCreatorProgressBar);

        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(userList, selectedUserPhones);
        recyclerViewUsers.setAdapter(userAdapter);

        btnStartChat.setEnabled(false); // Disable start button until users are selected
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        database.collection(Constants.COLLECTION_USER)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        userList.clear();
                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : task.getResult()) {
                            UserModel user = document.toObject(UserModel.class);
                            if (!user.phone.equals(currentUserPhone)) { // Exclude the current user
                                userList.add(user);
                            }
                        }
                        userAdapter.notifyDataSetChanged();
                        Log.d(TAG, "Users loaded successfully.");
                    } else {
                        Log.e(TAG, "Error loading users.", task.getException());
                        Toast.makeText(this, "Failed to load users.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupListeners() {
        btnStartChat.setOnClickListener(v -> {
            if (selectedUserPhones.isEmpty()) {
                Toast.makeText(this, "Please select at least one user.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a new chat model
            ChatModel chat = new ChatModel();
            chat.phoneNumbers.add(currentUserPhone); // Add the current user
            chat.phoneNumbers.addAll(selectedUserPhones); // Add selected users
            chat.creatorPhone = currentUserPhone;

            // Pass the chat model to ChatActivity
            Intent intent = new Intent(ChatCreatorActivity.this, ChatActivity.class);
            intent.putExtra(Constants.MODEL_CHAT, chat);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
    }

    // Adapter for RecyclerView
    public static class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        private final List<UserModel> users;
        private final Set<String> selectedPhones;

        public UserAdapter(List<UserModel> users, Set<String> selectedPhones) {
            this.users = users;
            this.selectedPhones = selectedPhones;
        }

        @Override
        public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_chat_creator_item, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(UserViewHolder holder, int position) {
            UserModel user = users.get(position);
            holder.bind(user, selectedPhones);
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        public static class UserViewHolder extends RecyclerView.ViewHolder {
            private final TextView userNameText;
            private final TextView userPhoneText;
            private final RoundedImageView profilePic;
            private final CheckBox userCheckBox;

            public UserViewHolder(View itemView) {
                super(itemView);
                userNameText = itemView.findViewById(R.id.userNameText);
                userPhoneText = itemView.findViewById(R.id.textviewPhone);
                profilePic = itemView.findViewById(R.id.roundedviewProfilePic);
                userCheckBox = itemView.findViewById(R.id.userCheckBox);
            }

            public void bind(UserModel user, Set<String> selectedPhones) {
                userNameText.setText(user.username);
                userPhoneText.setText(user.phone);
                userCheckBox.setChecked(selectedPhones.contains(user.phone));
                userCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedPhones.add(user.phone);
                    } else {
                        selectedPhones.remove(user.phone);
                    }
                });
            }
        }
    }
}