package com.example.ezchat;

import android.content.Intent;
import android.os.Bundle;

import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ezchat.databinding.ActivityLoginUserNameBinding;
import com.example.ezchat.model.UserModel;
import com.example.ezchat.utils.Constants;
import com.example.ezchat.utils.FirebaseUtil;
import com.example.ezchat.utils.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;


public class LoginUserNameActivity extends AppCompatActivity {
    private ActivityLoginUserNameBinding binding;

    private PreferenceManager preferenceManager;




    /**
     * Initializes the username setup activity, sets up UI components, and retrieves any existing username.
     *
     * @param savedInstanceState The saved instance state for the activity.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        binding = ActivityLoginUserNameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    private void setListeners() {
        binding.textSignIn.setOnClickListener(v ->onBackPressed());
        binding.loginCodeBtn.setOnClickListener(v->{
            if(isValidateSignUpDetails()) {
                SignUp();
            }
        });

    }

    public void showToast(String message) {

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void SignUp() {
        loading(true);
        //Post to Firebase
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        HashMap<String, String> user = new HashMap<>();
        user.put(Constants.KEY_USERNAME,binding.loginUsername.getText().toString());
        user.put(Constants.KEY_PASSWORD,binding.loginPassword.getText().toString());
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }).addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage());
                });



    }

    private Boolean isValidateSignUpDetails() {


         if(binding.loginUsername.getText().toString().trim().isEmpty()){
            showToast(("Please enter your Email or your Username"));
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.loginUsername.getText().toString()).matches()) {
            showToast(("Please enter Valid Email"));
            return false;
        }else if(binding.loginPassword.getText().toString().trim().isEmpty()) {
            showToast("Please enter your Password");
            return false;
        }else if(binding.loginConfirmedPassword.getText().toString().trim().isEmpty()){
            showToast("Please confirm your Password");
            return false;
        }else if(!binding.loginPassword.getText().toString().equals(binding.loginConfirmedPassword.getText().toString())) {
            showToast("Password not the same");
            return false;
        }else {
            return true;
        }
    }








    private void loading(Boolean isLoading) {
        if(isLoading) {
            binding.textSignIn.setVisibility(View.INVISIBLE);
            binding.loginProgressBar.setVisibility(View.VISIBLE);
        }else {
            binding.loginProgressBar.setVisibility(View.INVISIBLE);
            binding.textSignIn.setVisibility(View.VISIBLE);

        }
    }





}