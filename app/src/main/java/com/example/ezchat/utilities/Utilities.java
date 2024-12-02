package com.example.ezchat.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.example.ezchat.R;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utilities class for common functions and helpers used across the app.
 */
public class Utilities {

    private static final String LOG_TAG = "UTILITIES";

    /**
     * Enum representing different types of Toast messages.
     */
    public enum ToastType { INFO, WARNING, ERROR, SUCCESS, DEFAULT }

    // ** Toast Helpers **

    /**
     * Displays a Toast message with the default type.
     * @param context The context to use for displaying the Toast.
     * @param message The message to display in the Toast.
     */
    public static void showToast(Context context, String message) {
        showToast(context, message, ToastType.DEFAULT);
    }

    /**
     * Displays a Toast message with the specified type.
     * @param context The context to use for displaying the Toast.
     * @param message The message to display in the Toast.
     * @param type    The type of the message: INFO, WARNING, ERROR, SUCCESS, or DEFAULT.
     */
    public static void showToast(Context context, String message, ToastType type) {
        if (type == null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            return;
        }
        displayCustomToast(context, message, type);
    }

    /**
     * Displays a custom Toast with styling based on the provided ToastType.
     * @param context The context to use for displaying the Toast.
     * @param message The message to display in the Toast.
     * @param type    The type of the message: INFO, WARNING, ERROR, SUCCESS, or DEFAULT.
     */
    private static void displayCustomToast(Context context, String message, ToastType type) {
        try {
            LayoutInflater inflater = LayoutInflater.from(context);
            View layout = inflater.inflate(R.layout.container_custom_toast, null);
            ImageView imageView = layout.findViewById(R.id.imageToast);
            TextView textView = layout.findViewById(R.id.textToast);
            textView.setText(message);

            int iconResId;
            int bgColor;
            int textColor = ContextCompat.getColor(context, R.color.white);

            switch (type) {
                case INFO:
                    iconResId = R.drawable.ic_info;
                    bgColor = R.color.info;
                    break;
                case WARNING:
                    iconResId = R.drawable.ic_warning;
                    bgColor = R.color.warning;
                    break;
                case ERROR:
                    iconResId = R.drawable.ic_error;
                    bgColor = R.color.error;
                    break;
                case SUCCESS:
                    iconResId = R.drawable.ic_success;
                    bgColor = R.color.success;
                    break;
                default:
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    return;
            }

            imageView.setImageResource(iconResId);
            ViewCompat.setBackgroundTintList(layout, ColorStateList.valueOf(ContextCompat.getColor(context, bgColor)));
            textView.setTextColor(textColor);

            Toast toast = new Toast(context);
            toast.setGravity(Gravity.BOTTOM, 0, 100);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error displaying custom toast: " + e.getMessage());
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    // ** Image Utilities **

    /**
     * Decodes a Base64-encoded string into a Bitmap image.
     * @param encodedImage The Base64-encoded image string.
     * @return A Bitmap representation of the decoded image.
     */
    public static Bitmap decodeImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * Compresses a Bitmap image to reduce its size.
     * @param bitmap The original Bitmap image.
     * @return The compressed Bitmap image.
     */
    public static Bitmap compressImage(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    /**
     * Encodes a Bitmap image to a Base64 string after resizing and compressing it.
     * @param bitmap The Bitmap image to encode.
     * @param previewWidth The desired width for the preview image.
     * @return A Base64 encoded string representation of the image.
     */
    public static String encodeImage(Bitmap bitmap, int previewWidth) {
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    // ** Validation Utilities **

    /**
     * Validates whether a given email address is valid.
     *
     * @param email The email to validate.
     * @return {@code true} if the email is valid, {@code false} otherwise.
     */
    public static boolean isValidEmail(String email) {
        final String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    /**
     * Validates whether a given string matches a specified regex pattern.
     * @param input   The input string to validate.
     * @param pattern The regex pattern to match.
     * @return {@code true} if the input matches the pattern, {@code false} otherwise.
     */
    public static boolean isValidPattern(String input, String pattern) {
        return Pattern.compile(pattern).matcher(input).matches();
    }

    // ** UI Helpers **

    /**
     * Changes the tint color of a View's background.
     * @param view  The view whose background tint should be changed.
     * @param color The color to apply as the tint.
     */
    public static void setBackgroundTint(View view, int color) {
        view.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    /**
     * Applies a ripple effect background to a view.
     * @param view  The view to apply the ripple effect to.
     * @param color The ripple color.
     */
    public static void applyRippleEffect(View view, int color) {
        ColorStateList rippleColor = ColorStateList.valueOf(color);
        ViewCompat.setBackgroundTintList(view, rippleColor);
    }


    /**
     * Navigates to the specified activity.
     *
     * @param context   The current context.
     * @param activity  The target activity class.
     * @param extras    Optional extras to pass as key-value pairs.
     */
    public static void navigateToActivity(Context context, Class<?> activity, Map<String, String> extras) {
        Intent intent = new Intent(context, activity);
        if (extras != null) {
            for (Map.Entry<String, String> entry : extras.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }
        context.startActivity(intent);
    }

    /**
     * Hashes a plain text password using SHA-256.
     *
     * @param password The plain text password to hash.
     * @return The hashed password (Base64 encoded).
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.encodeToString(hash, Base64.DEFAULT).trim();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    /**
     * Verifies if a plain text password matches the stored hashed password.
     *
     * @param plainPassword     The plain text password to verify.
     * @param storedHashedPassword The hashed password to compare against.
     * @return {@code true} if the passwords match, {@code false} otherwise.
     */
    public static boolean verifyPassword(String plainPassword, String storedHashedPassword) {
        String hashedInput = hashPassword(plainPassword);
        return hashedInput.equals(storedHashedPassword);
    }

    /**
     * Checks if two passwords match.
     *
     * @param password     The first password.
     * @param confirmPassword The second password to confirm.
     * @return {@code true} if the passwords match, {@code false} otherwise.
     */
    public static boolean doPasswordsMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }

    /**
     * Ensures that a string is neither null nor empty.
     *
     * @param value The string to validate.
     * @param errorMessage The error message to throw if invalid.
     * @return The validated string.
     */
    public static String requireNonNullOrEmpty(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return value;
    }

    /**
     * Formats a timestamp into a user-friendly string.
     *
     * @param timestamp The timestamp to format.
     * @return A formatted string, e.g., "Today 10:30 AM", "Yesterday 5:45 PM", or "12/01/2024".
     */
    public static String formatTimestamp(Date timestamp) {
        if (timestamp == null) return "";

        SimpleDateFormat todayFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

        Date currentDate = new Date();
        long diffInMillis = currentDate.getTime() - timestamp.getTime();
        long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);

        if (diffInDays < 1) {
            return "Today " + todayFormat.format(timestamp);
        } else if (diffInDays == 1) {
            return "Yesterday " + todayFormat.format(timestamp);
        } else {
            return fullDateFormat.format(timestamp);
        }
    }


    /**
     * Creates a map of extras to pass to an Intent.
     *
     * @param key   The key for the extra.
     * @param value The value for the extra.
     * @return A map containing the key-value pair.
     */
    public static Map<String, String> createExtras(String key, String value) {
        Map<String, String> extras = new HashMap<>();
        extras.put(key, value);
        return extras;
    }

    /**
     * Creates a map of extras with multiple key-value pairs.
     *
     * @param pairs Varargs of key-value pairs (key1, value1, key2, value2, ...).
     * @return A map containing all the provided key-value pairs.
     */
    public static Map<String, String> createExtras(String... pairs) {
        Map<String, String> extras = new HashMap<>();
        for (int i = 0; i < pairs.length - 1; i += 2) {
            extras.put(pairs[i], pairs[i + 1]);
        }
        return extras;
    }
}