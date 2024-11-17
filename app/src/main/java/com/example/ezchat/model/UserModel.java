package com.example.ezchat.model;

import com.google.firebase.Timestamp;
/**
 * Represents a user in the application, containing profile information,
 * contact details, and metadata such as creation timestamp and FCM token.
 */
public class UserModel {
    private String phone;              // User's phone number
    private String username;           // User's username
    private Timestamp createdTimestamp; // Timestamp indicating when the user account was created
    private String userId;             // Unique identifier for the user
    private String fcmToken;           // Firebase Cloud Messaging token for notifications
    private String profilePicUrl;      // URL to the user's profile picture
    /**
     * Default constructor for UserModel.
     * Required for Firebase Firestore to deserialize user data.
     */
    public UserModel() {
    }
    /**
     * Parameterized constructor to initialize the user model with essential fields.
     * @param phone            The user's phone number.
     * @param username         The user's username.
     * @param createdTimestamp The timestamp indicating when the user account was created.
     * @param userId           The unique identifier for the user.
     */
    public UserModel(String phone, String username, Timestamp createdTimestamp, String userId) {
        this.phone = phone;
        this.username = username;
        this.createdTimestamp = createdTimestamp;
        this.userId = userId;
    }
    /**
     * Gets the user's phone number.
     * @return The phone number.
     */
    public String getPhone() {
        return phone;
    }
    /**
     * Sets the user's phone number.
     * @param phone The phone number to set.
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }
    /**
     * Gets the user's username.
     * @return The username.
     */
    public String getUsername() {
        return username;
    }
    /**
     * Sets the user's username.
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }
    /**
     * Gets the timestamp indicating when the user account was created.
     * @return The creation timestamp.
     */
    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }
    /**
     * Sets the timestamp indicating when the user account was created.
     * @param createdTimestamp The creation timestamp to set.
     */
    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }
    /**
     * Gets the user's unique identifier.
     * @return The user ID.
     */
    public String getUserId() {
        return userId;
    }
    /**
     * Sets the user's unique identifier.
     * @param userId The user ID to set.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
    /**
     * Gets the Firebase Cloud Messaging (FCM) token.
     * @return The FCM token.
     */
    public String getFcmToken() {
        return fcmToken;
    }
    /**
     * Sets the Firebase Cloud Messaging (FCM) token.
     * @param fcmToken The FCM token to set.
     */
    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
    /**
     * Gets the URL to the user's profile picture.
     * @return The profile picture URL.
     */
    public String getProfilePicUrl() {
        return profilePicUrl;
    }
    /**
     * Sets the URL to the user's profile picture.
     * @param profilePicUrl The profile picture URL to set.
     */
    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }
}