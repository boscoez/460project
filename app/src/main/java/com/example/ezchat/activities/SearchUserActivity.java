package com.example.ezchat.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezchat.R;
import com.example.ezchat.adapters.SearchUserRecyclerAdapter;
import com.example.ezchat.models.UserModel;
import com.example.ezchat.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

/**
 * Activity for searching users by their username. Displays a search bar and a RecyclerView
 * for showing matching user profiles fetched from Firebase Firestore.
 */
public class SearchUserActivity extends AppCompatActivity {
    // UI Components
    EditText searchInput; // Input field for entering the username to search
    ImageButton searchButton; // Button to trigger the search
    ImageButton backButton; // Button to navigate back to the previous activity
    RecyclerView recyclerView; // RecyclerView to display the search results
    SearchUserRecyclerAdapter adapter; // Adapter for managing search results
    /**
     * Initializes the search user activity, sets up UI components, and adds listeners for
     * the back and search buttons.
     * @param savedInstanceState The saved instance state for the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);
        // Initialize UI components
        searchInput = findViewById(R.id.search_username_input);
        searchButton = findViewById(R.id.search_user_btn);
        backButton = findViewById(R.id.back_btn);
        recyclerView = findViewById(R.id.search_user_recycler_view);
        // Automatically focus on the search input field
        searchInput.requestFocus();
        // Listener for the back button to navigate back
        backButton.setOnClickListener(v -> onBackPressed());
        // Listener for the search button to validate input and trigger search
        searchButton.setOnClickListener(v -> {
            String searchTerm = searchInput.getText().toString();
            // Validate search term
            if (searchTerm.isEmpty() || searchTerm.length() < 3) {
                searchInput.setError("Invalid Username"); // Show error if invalid
                return;
            }
            // Set up the RecyclerView for displaying search results
            setupSearchRecyclerView(searchTerm);
        });
    }
    /**
     * Configures the RecyclerView to display search results for users whose usernames
     * match the search term.
     * @param searchTerm The search term to filter usernames.
     */
    void setupSearchRecyclerView(String searchTerm) {
        // Query Firestore for usernames matching the search term
        Query query = FirebaseUtil.allUserCollectionReference()
                .whereGreaterThanOrEqualTo("username", searchTerm);
        // Set up FirestoreRecyclerOptions with the query
        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(query, UserModel.class)
                .build();
        // Initialize the adapter with options and bind it to the RecyclerView
        adapter = new SearchUserRecyclerAdapter(options, getApplicationContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Set layout manager
        recyclerView.setAdapter(adapter); // Attach adapter to RecyclerView
        adapter.startListening(); // Start listening for Firestore updates
    }
    /**
     * Starts the adapter listening when the activity is started.
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }
    /**
     * Stops the adapter listening when the activity is stopped.
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
    /**
     * Resumes the adapter listening when the activity is resumed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.startListening();
        }
    }
}
