package com.example.ezchat.utilities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.ezchat.R;
import com.example.ezchat.models.UserModel;

import java.io.IOException;

/**
 * Utility class for Android-specific operations such as showing toasts,
 * managing intents, and setting profile pictures.
 */
public class AndroidUtil {
    /**
     * Displays a Toast message in the given context.
     * @param context The context in which the toast should be displayed.
     * @param message The message to display in the toast.
     */
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
    /**
     * Passes user information through an Intent by adding it as extras.
     * @param intent The Intent to which user data will be attached.
     * @param model  The UserModel containing user data.
     */
    public static void passUserModelAsIntent(Intent intent, UserModel model) {
        // Attach user details to the intent.
        intent.putExtra("username", model.getUsername());
        intent.putExtra("phone", model.getPhone());
        intent.putExtra("userId", model.getUserId());
        intent.putExtra("fcmToken", model.getFcmToken());
    }
    /**
     * Extracts user information from an Intent and converts it to a UserModel.
     * @param intent The Intent containing user data.
     * @return A UserModel object populated with the extracted data.
     */
    public static UserModel getUserModelFromIntent(Intent intent) {
        UserModel userModel = new UserModel();
        // Extract user details from the intent.
        userModel.setUsername(intent.getStringExtra("username"));
        userModel.setPhone(intent.getStringExtra("phone"));
        userModel.setUserId(intent.getStringExtra("userId"));
        userModel.setFcmToken(intent.getStringExtra("fcmToken"));
        return userModel;
    }
    /**
     * Sets a profile picture in an ImageView using a circular crop transformation.
     * @param context    The context in which the operation is performed.
     * @param imageUri   The URI of the image to be loaded.
     * @param imageView  The ImageView where the profile picture will be displayed.
     */
    public static void setProfilePic(Context context, Uri imageUri, ImageView imageView) {
        // Load the image URI into the ImageView with a circular crop transformation using Glide.
        Glide.with(context)
                .load(imageUri)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView);
    }

    /**
     * Sets the profile picture from a Base64-encoded string.
     * @param context The context.
     * @param base64Image The Base64-encoded image string.
     * @param imageView The ImageView to set the image.
     */
    public static void setProfilePicFromBase64(Context context, String base64Image, ImageView imageView) {
        try {
            byte[] decodedBytes = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            imageView.setImageBitmap(bitmap);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            imageView.setImageResource(R.drawable.ic_person); // Default image
        }
    }

    /**
     * Retrieves a Bitmap from a given URI.
     * @param context The context.
     * @param uri The URI of the image.
     * @return The Bitmap representation of the image.
     * @throws IOException If an error occurs during reading the image.
     */
    public static Bitmap getBitmapFromUri(Context context, Uri uri) throws IOException {
        return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
    }


}
