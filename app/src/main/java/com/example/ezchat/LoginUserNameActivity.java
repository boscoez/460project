package com.example.ezchat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ezchat.model.UserModel;
import com.example.ezchat.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import java.util.Objects;

public class LoginUserNameActivity extends AppCompatActivity {
    EditText usernameInput;
    Button letMeInBtn;
    ProgressBar progressBar;
    String phoneNumber;
    UserModel userModel;

    /**
     * Initializes the username setup activity, sets up UI components, and retrieves any existing username.
     *
     * @param savedInstanceState The saved instance state for the activity.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_user_name);

        usernameInput = findViewById(R.id.login_username);
        letMeInBtn = findViewById(R.id.login_code_btn);
        progressBar = findViewById(R.id.login_progress_bar);
        phoneNumber = Objects.requireNonNull(getIntent().getExtras()).getString("phone");
        getUsername();

        letMeInBtn.setOnClickListener((v -> setUsername()));
    }

    /**
     * Validates and sets the username for the user. If a UserModel doesn't already exist, it creates one,
     * saves it in Firebase, and navigates to the MainActivity on success.
     */

    void setUsername() {

        String username = usernameInput.getText().toString();
        if (username.isEmpty() || username.length() < 3) {
            usernameInput.setError("Username length must be 3+ characters. ");
            return;
        }
        setInProgress(true);
        if(userModel != null){
            userModel.setUsername(username);
        } else {
            userModel = new UserModel(phoneNumber, username, Timestamp.now(), FirebaseUtil.currentUserId());
        }

        FirebaseUtil.currentUserDetails().set(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                setInProgress(false);
                if (task.isSuccessful()) {
                    Intent intent = new Intent(LoginUserNameActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * Retrieves the existing username from Firebase Firestore if it exists and populates the input field.
     */

    void getUsername(){
        setInProgress(true);
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            setInProgress(false);
            if(task.isSuccessful()){
                UserModel userModel = task.getResult().toObject(UserModel.class);
                if(userModel != null){
                    usernameInput.setText(userModel.getUsername());
                }
            }
        });
    }

    /**
     * Shows or hides the progress bar and "Let Me In" button based on the inProgress parameter.
     *
     * @param inProgress Boolean indicating if the operation is in progress.
     */

    void  setInProgress(boolean inProgress){
        if(inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            letMeInBtn.setVisibility(View.GONE);
        }else{
            progressBar.setVisibility(View.GONE);
            letMeInBtn.setVisibility(View.VISIBLE);
        }
    }
}