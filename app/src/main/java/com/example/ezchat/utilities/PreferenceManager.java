package com.example.ezchat.utilities;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * This class manages the application's shared preferences, providing generic methods
 * to store and retrieve various types of data.
 */
public class PreferenceManager {
    // Preference file name
    public static final String KEY_PREFERENCE_NAME = "chatAppPreferences";

    private static PreferenceManager instance;
    private final SharedPreferences sharedPreferences;

    /**
     * Private constructor to enforce Singleton pattern.
     *
     * @param context The context used to access SharedPreferences.
     */
    private PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(KEY_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Provides the Singleton instance of PreferenceManager.
     *
     * @param context The context used to access SharedPreferences.
     * @return The Singleton instance of PreferenceManager.
     */
    public static synchronized PreferenceManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferenceManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Saves a value of any type in SharedPreferences.
     *
     * @param key   The key under which the value is saved.
     * @param value The value to save (String, Integer, Boolean, Float, Long).
     */
    public void set(String key, Object value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else {
            throw new IllegalArgumentException("Unsupported type for SharedPreferences");
        }

        editor.apply(); // Save the changes asynchronously
    }

    // ** Generic Get Method **

    /**
     * Retrieves a value of any type from SharedPreferences.
     *
     * @param key          The key of the value to retrieve.
     * @param defaultValue The default value to return if the key does not exist.
     * @return The value associated with the key, or the default value if not found.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        Object result;

        if (defaultValue instanceof String) {
            result = sharedPreferences.getString(key, (String) defaultValue);
        } else if (defaultValue instanceof Integer) {
            result = sharedPreferences.getInt(key, (Integer) defaultValue);
        } else if (defaultValue instanceof Boolean) {
            result = sharedPreferences.getBoolean(key, (Boolean) defaultValue);
        } else if (defaultValue instanceof Float) {
            result = sharedPreferences.getFloat(key, (Float) defaultValue);
        } else if (defaultValue instanceof Long) {
            result = sharedPreferences.getLong(key, (Long) defaultValue);
        } else {
            throw new IllegalArgumentException("Unsupported type for SharedPreferences");
        }

        return (T) result;
    }

    // ** Utility Methods **

    /**
     * Removes a specific key from SharedPreferences.
     *
     * @param key The key to remove.
     */
    public void remove(String key) {
        sharedPreferences.edit().remove(key).apply();
    }

    /**
     * Clears all values from SharedPreferences.
     */
    public void clear() {
        sharedPreferences.edit().clear().apply();
    }

    /**
     * Checks if a key exists in SharedPreferences.
     *
     * @param key The key to check.
     * @return True if the key exists, false otherwise.
     */
    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }
}