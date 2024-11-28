
package com.example.ezchat.models;

import com.google.firebase.Timestamp;
import com.google.firebase.Timestamp;
import java.util.List;

/**
 * Represents a user in the application, containing profile information,
 * contact details, metadata such as creation timestamp, FCM token, and the chat rooms
 * the user is part of.
 */
public class UserModel {
    private String phone;              // User's phone number
    private String username;           // User's username
    private Timestamp createdTimestamp; // Timestamp indicating when the user account was created
    private String userId;             // Unique identifier for the user
    private String fcmToken;           // Firebase Cloud Messaging token for notifications
    private String profilePic;         // Base64-encoded profile picture (bitmap format)
    private List<String> chatRooms;    // List of chat room IDs the user is involved in

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
     * @param fcmToken         The Firebase Cloud Messaging (FCM) token.
     * @param profilePic       The Base64-encoded profile picture.
     * @param chatRooms        List of chat rooms the user is part of.
     */
    public UserModel(String phone, String username, Timestamp createdTimestamp, String userId, String fcmToken, String profilePic, List<String> chatRooms) {
        this.phone = phone;
        this.username = username;
        this.createdTimestamp = createdTimestamp;
        this.userId = userId;
        this.fcmToken = fcmToken;
        this.profilePic = profilePic;
        this.chatRooms = chatRooms;
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
     * Gets the Base64-encoded profile picture (bitmap format).
     * @return The Base64-encoded profile picture.
     */
    public String getProfilePic() {
        return profilePic;
    }

    /**
     * Sets the Base64-encoded profile picture (bitmap format).
     * @param profilePic The profile picture to set.
     */
    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    /**
     * Gets the list of chat room IDs the user is part of.
     * @return The list of chat room IDs.
     */
    public List<String> getChatRooms() {
        return chatRooms;
    }

    /**
     * Sets the list of chat room IDs the user is part of.
     * @param chatRooms The chat rooms to set.
     */
    public void setChatRooms(List<String> chatRooms) {
        this.chatRooms = chatRooms;
    }
}