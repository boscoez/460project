package com.example.ezchat;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ezchat.model.UserModel;
import com.example.ezchat.utils.AndroidUtil;

public class ChatActivity extends AppCompatActivity {
    UserModel otherUser;
    EditText messageInput;
    ImageButton backBtn;
    ImageButton sendMessageBtn;
    TextView otherUsername;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        otherUser = AndroidUtil.getUserModelFromIntent(getIntent());
    }
}