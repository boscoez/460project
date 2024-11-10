package com.example.ezchat;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ezchat.adapter.SearchUserRecyclerAdapter;
import com.example.ezchat.model.UserModel;
import com.example.ezchat.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

public class SearchUserActivity extends AppCompatActivity {
    EditText searchInput;
    ImageButton searchButton;
    ImageButton backButton;
    RecyclerView recyclerView;
    SearchUserRecyclerAdapter adapter;
<<<<<<< HEAD
=======
    /**
     * Initializes the search user activity, sets up UI components, and adds listeners for the back and search buttons.
     *
     * @param savedInstanceState The saved instance state for the activity.
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        searchInput = findViewById(R.id.search_username_input);
        searchButton = findViewById(R.id.search_user_btn);
        backButton =  findViewById(R.id.back_btn);
        recyclerView = findViewById(R.id.search_user_recycler_view);
        searchInput.requestFocus();

        backButton.setOnClickListener(v->{
            onBackPressed();
        });

        searchButton.setOnClickListener(v -> {
            String searchTerm = searchInput.getText().toString();
            if(searchTerm.isEmpty() || searchTerm.length() < 3){
                searchInput.setError("Invalid Username");
                return;
            }
            setupSearchRecyclerView(searchTerm);
        });
    }
<<<<<<< HEAD
=======
    /**
     * Configures the RecyclerView to display search results for users whose usernames match the search term.
     *
     * @param searchTerm The search term to filter usernames.
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    void setupSearchRecyclerView(String searchTerm){

        Query query = FirebaseUtil.allUserCollectionReference()
                .whereGreaterThanOrEqualTo("username", searchTerm);

        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(query, UserModel.class).build();

        adapter = new SearchUserRecyclerAdapter(options, getApplicationContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }
<<<<<<< HEAD
=======
    /**
     * Starts the adapter listening when the activity is started.
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    @Override
    protected void onStart(){
        super.onStart();
        if(adapter != null)
            adapter.startListening();
    }
<<<<<<< HEAD
=======
    /**
     * Stops the adapter listening when the activity is stopped.
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    @Override
    protected void onStop() {
        super.onStop();
        if(adapter != null)
            adapter.stopListening();
    }
<<<<<<< HEAD
=======
    /**
     * Resumes the adapter listening when the activity is resumed.
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    @Override
    protected void onResume() {
        super.onResume();
        if(adapter != null)
            adapter.startListening();
    }
}