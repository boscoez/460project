package com.example.ezchat.models;

import com.google.firebase.Timestamp;

import java.util.List;

/**
 * Represents a user in the application, containing profile information,
 * contact details, metadata such as creation timestamp, FCM token, and the chat rooms
 * the user is part of.
 */
public class UserModel {

    // Firestore collection and field constants
    public static final String FIELD_COLLECTION_NAME = "users";
    public static final String FIELD_PHONE = "phone";
    public static final String FIELD_USERNAME = "username";
    public static final String FIELD_CREATED_TIMESTAMP = "createdTimestamp";
    public static final String FIELD_USER_ID = "userId";
    public static final String FIELD_FCM_TOKEN = "fcmToken";
    public static final String FIELD_PROFILE_PIC = "profilePic";
    public static final String FIELD_CHAT_ROOMS = "chatRooms";

    // Public fields for direct Firestore access
    public String phone;              // User's phone number
    public String username;           // User's username
    public Timestamp createdTimestamp; // Timestamp indicating when the user account was created
    public String userId;             // Unique identifier for the user
    public String fcmToken;           // Firebase Cloud Messaging token for notifications
    public String profilePic;         // Base64-encoded profile picture (bitmap format)
    public List<String> chatRooms;    // List of chat room IDs the user is involved in

    /**
     * Default constructor for UserModel.
     * Required for Firebase Firestore to deserialize user data.
     */
    public UserModel() {
    }

    /**
     * Parameterized constructor to initialize the user model with essential fields.
     *
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

    // Utility Functions

    /**
     * Validates if the required fields for a user are present.
     *
     * @return True if all required fields are non-null, false otherwise.
     */
    public boolean isValid() {
        return phone != null && !phone.isEmpty() &&
                username != null && !username.isEmpty() &&
                userId != null && !userId.isEmpty();
    }

    /**
     * Updates the FCM token for the user.
     *
     * @param fcmToken The new FCM token.
     */
    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    /**
     * Adds a chat room ID to the user's chat rooms list.
     *
     * @param chatRoomId The chat room ID to add.
     */
    public void addChatRoom(String chatRoomId) {
        if (!chatRooms.contains(chatRoomId)) {
            chatRooms.add(chatRoomId);
        }
    }

    /**
     * Removes a chat room ID from the user's chat rooms list.
     *
     * @param chatRoomId The chat room ID to remove.
     */
    public void removeChatRoom(String chatRoomId) {
        chatRooms.remove(chatRoomId);
    }
}