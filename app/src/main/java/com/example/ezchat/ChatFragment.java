package com.example.ezchat;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ezchat.adapter.RecentChatRecyclerAdapter;
import com.example.ezchat.model.ChatroomModel;
import com.example.ezchat.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
/**
 * Fragment for displaying a list of recent chats in a RecyclerView.
 * Integrates with Firestore to fetch and display chatroom data in real-time.
 */
public class ChatFragment extends Fragment {
    // UI Components
    RecyclerView recyclerView; // RecyclerView to display recent chats
    RecentChatRecyclerAdapter adapter; // Adapter for managing chat data
    /**
     * Default constructor for ChatFragment.
     */
    public ChatFragment() {
    }
    /**
     * Inflates the layout for this fragment and sets up the RecyclerView.
     * @param inflater           The LayoutInflater object for inflating views in the fragment.
     * @param container          The parent ViewGroup.
     * @param savedInstanceState Saved instance state for the fragment.
     * @return The View for the fragment's UI.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for the fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        // Initialize the RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
        // Set up the RecyclerView to display recent chats
        setupRecyclerView();
        return view;
    }
    /**
     * Configures the RecyclerView with Firestore data for displaying recent chats.
     */
    void setupRecyclerView() {
        // Query Firestore to fetch chatrooms for the current user, ordered by the latest message timestamp
        Query query = FirebaseUtil.allChatroomCollectionReference()
                .whereArrayContains("userIds", FirebaseUtil.currentUserId())
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING);
        // Configure FirestoreRecyclerOptions for the RecentChatRecyclerAdapter
        FirestoreRecyclerOptions<ChatroomModel> options = new FirestoreRecyclerOptions.Builder<ChatroomModel>()
                .setQuery(query, ChatroomModel.class)
                .build();
        // Initialize the adapter and bind it to the RecyclerView
        adapter = new RecentChatRecyclerAdapter(options, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // Set linear layout
        recyclerView.setAdapter(adapter); // Attach the adapter
        adapter.startListening(); // Start listening for Firestore data updates
    }
    /**
     * Starts the adapter listening when the fragment is started.
     */
    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }
    /**
     * Stops the adapter listening when the fragment is stopped.
     */
    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
    /**
     * Notifies the adapter to refresh data when the fragment is resumed.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
