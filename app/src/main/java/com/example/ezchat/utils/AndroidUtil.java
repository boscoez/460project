package com.example.ezchat.utils;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.example.ezchat.model.UserModel;

public class AndroidUtil {
    /**
     *  Displays a toast message on the screen.
     * @param context   The context in which the toast should be shown.
     * @param message   The message to display in the toast.
     */
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Adds user details from a UserModel object as extras to an Intent.
     *This enables passing user information between activities.
     *
     * @param intent        The Intent to add user details to.
     * @param model         The UserModel object containing user data to pass.
     */
    public static void passUserModelAsIntent(Intent intent, UserModel model){
        intent.putExtra("username",model.getUsername());
        intent.putExtra("phone",model.getPhone());
        intent.putExtra("userId",model.getUserId());
    }

    /**
     * Retrieves user details from an Intent and returns them as a UserModel object.
     * @param intent        The Intent containing user data as extras.
     * @return          A UserModel object populated with data from the Intent extras.
     */
    public static UserModel getUserModelFromIntent(Intent intent){
        UserModel userModel = new UserModel();
        userModel.setUsername(intent.getStringExtra("username"));
        userModel.setPhone(intent.getStringExtra("phone"));
        userModel.setUserId(intent.getStringExtra("userId"));
        return userModel;
    }
}

