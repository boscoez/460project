package com.example.ezchat.utilities;

/**
 * Constants class that holds all the constant values used across the application.
 * This includes keys for SharedPreferences, Firestore collections, user attributes, and other commonly used data.
 */
public class Constants {

    // Firebase Firestore collections
    public static final String KEY_COLLECTION_CHAT = "chat";    // Chatrooms
    public static final String KEY_COLLECTION_USERS = "Users";  // User data
    public static final String KEY_COLLECTION_MESSAGES = "messages"; // Chatroom messages

    public static final String KEY_CREATOR_ID = "creatorId";


    // Firebase Fields for User Data
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_PHONE_NUMBER = "phoneNumber";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PROFILE_PIC_BITMAP = "profilePicBitmap"; // Bitmap image
    public static final String KEY_LAST_MESSAGE_SENDER_ID = "lastMessageSenderId";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_CREATED_TIMESTAMP = "createdTimestamp";
    public static final String KEY_PASSWORD = "password"; // Key for password field
    public static final String KEY_PASSWORD_HASH = "passwordHash"; // Key for storing hashed password (if using hashing)


    // Firestore Fields for Chat Data
    public static final String KEY_CHATROOM_ID = "chatroomId";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_MESSAGE_STATUS = "messageStatus";  // Sent, delivered, read
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_LAST_MESSAGE_TIMESTAMP = "lastMessageTimestamp"; // For sorting chatrooms
    public static final String KEY_USER_IDS = "userIds"; // Group chats
    public static final String KEY_PREFERENCE_NAME = "chatAppPreferences"; // Placeholder image

    public static final String EXTRA_CHATROOM_ID = "chatroomId";


    // Password-related Constants
    public static final String PASSWORD_SALT = "random_salt"; // For hashing passwords (if using a salt for hashing)
    public static final String PASSWORD_HASH_ALGORITHM = "SHA-256"; // Algorithm for hashing passwords
}