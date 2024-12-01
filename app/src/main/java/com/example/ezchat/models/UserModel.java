package com.example.ezchat.models;

import android.util.Base64;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a user in the chat application.
 * This model contains user profile details such as phone number, username, profile picture,
 * email, hashed password, fcm token, and the list of chat room IDs the user is part of.
 */
public class UserModel {

    private String phone; // The phone number of the user
    private String username; // The username of the user
    private String profilePic; // The profile picture URL or identifier (Base64 encoded)
    private String email; // The email address of the user
    private String hashedPassword; // The hashed password of the user
    private String fcmToken; // The FCM token for push notifications
    private Set<String> chats; // Set of chats the user is part of (unique values)

    public UserModel(){}

    /**
     * Constructor to initialize a new UserModel with phone number and username.
     * Throws IllegalArgumentException if any required field is null or empty.
     *
     * @param phone    The phone number of the user.
     * @param username The username of the user.
     */
    public UserModel(String phone, String username) {
        this.phone = requireNonNullOrEmpty(phone, "Phone number cannot be null or empty.");
        this.username = requireNonNullOrEmpty(username, "Username cannot be null or empty.");

        // Initialize optional fields with null or default values
        this.profilePic = null; // No profile picture initially
        this.email = null; // No email initially
        this.hashedPassword = null; // Password will be set later
        this.fcmToken = null; // No FCM token initially
        this.chats = new HashSet<>(); // Set of chat rooms initially (no duplicates)
    }

    // Getters and Setters

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = requireNonNullOrEmpty(phone, "Phone number cannot be null or empty.");
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = requireNonNullOrEmpty(username, "Username cannot be null or empty.");
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = requireNonNullOrEmpty(email, "Email cannot be null or empty.");
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String password) {
        if (password != null && !password.trim().isEmpty()) {
            this.hashedPassword = hashPassword(password); // Hash the password before storing
        } else {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public Set<String> getChats() {
        return chats;
    }

    public void setChats(Set<String> chats) {
        this.chats = requireNonNullOrEmpty(chats, "Chat room IDs cannot be null or empty.");
    }

    /**
     * Removes a chat room ID from the set of chat room IDs.
     *
     * @param chatRoomId The chat room ID to remove.
     * @return True if the chat room ID was removed, false if it was not found.
     */
    public boolean removeChatRoomId(String chatRoomId) {
        return this.chats.remove(chatRoomId); // Removes the chat room ID if present
    }

    /**
     * Hashes a plain text password using SHA-256 algorithm.
     *
     * @param password The plain text password to hash.
     * @return The hashed password (Base64 encoded).
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.encodeToString(hash, Base64.DEFAULT); // Return Base64 encoded hashed password
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    /**
     * Verifies if a plain text password matches the stored hashed password.
     *
     * @param plainPassword     The plain text password to verify.
     * @param storedHashedPassword The hashed password stored in the system.
     * @return True if the passwords match, false otherwise.
     */
    public static boolean verifyPassword(String plainPassword, String storedHashedPassword) {
        String hashedInput = hashPassword(plainPassword);
        return hashedInput.equals(storedHashedPassword); // Compare hashed values
    }

    /**
     * Utility method to ensure a string is not null or empty.
     *
     * @param value          The value to check.
     * @param errorMessage   The error message to throw in case of invalid value.
     * @return The validated value.
     * @throws IllegalArgumentException if the value is null or empty.
     */
    private <T> T requireNonNullOrEmpty(T value, String errorMessage) {
        if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
            throw new IllegalArgumentException(errorMessage);
        }
        return value;
    }

    // Image encoding and decoding methods (same as before)...
}