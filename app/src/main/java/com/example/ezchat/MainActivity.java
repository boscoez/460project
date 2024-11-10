package com.example.ezchat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    ImageButton searchButton;
    ChatFragment chatFragment;
    ProfileFragment profileFragment;

    /**
     * Initializes the main activity, sets up UI components, fragments, and bottom navigation functionality.
     *
     * @param savedInstanceState The saved instance state for the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content view first to load the layout
        setContentView(R.layout.activity_main);

        // Initialize fragments
        chatFragment = new ChatFragment();
        profileFragment = new ProfileFragment();

        // Initialize UI elements
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        searchButton = findViewById(R.id.main_search_btn);
        searchButton.setOnClickListener((v) -> {
            startActivity(new Intent(MainActivity.this, SearchUserActivity.class));
        });

        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(menuItem -> {
                int itemId = menuItem.getItemId();

                if (itemId == R.id.menu_chat) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, chatFragment).commit();
                    return true;
                } else if (itemId == R.id.menu_profile) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, profileFragment).commit();
                    return true;
                }
                return false;
            });

            // Set default selected item
            bottomNavigationView.setSelectedItemId(R.id.menu_chat);
        } else {
            Log.e("MainActivity", "BottomNavigationView is null. Check layout ID.");
        }
    }
}
