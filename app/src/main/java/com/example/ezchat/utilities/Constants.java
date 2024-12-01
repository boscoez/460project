package com.example.ezchat.utilities;

/**
 * This class holds all the constants used across the application to ensure consistency
 * in field names, keys, and other shared values.
 */
public class Constants {

    // ** Firestore Collection and Field Constants **
    public static final String USER_COLLECTION = "users";
    public static final String CHAT_COLLECTION = "chat";
    public static final String MESSAGE_COLLECTION = "messages";
    public static final String FIELD_PHONE = "phone";
    public static final String FIELD_USERNAME = "username";
    public static final String FIELD_PROFILE_PIC = "profilePic";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_PASSWORD = "hashedPassword";
    public static final String FIELD_FCM_TOKEN = "fcmToken";
    public static final String FIELD_CHAT_ROOMS = "chatRooms";
    public static final String FIELD_CHAT_ID = "chatId";
    public static final String FIELD_PHONE_NUMBERS = "phoneNumbers";
    public static final String FIELD_CREATOR_PHONE = "creatorPhone";
    public static final String FIELD_LAST_MESSAGE = "lastMessage";
    public static final String FIELD_LAST_MESSAGE_TIMESTAMP = "lastMessageTimestamp";
    public static final String FIELD_LAST_MESSAGE_SENDER_PHONE = "lastMessageSenderPhone";
    public static final String FIELD_CREATED_DATE = "createdDate";

    // ** Shared Preferences Keys **
    public static final String PREF_NAME = "chatAppPreferences";
    public static final String PREF_KEY_PHONE = "userPhone";
    public static final String PREF_KEY_USERNAME = "username";
    public static final String PREF_KEY_EMAIL = "userEmail";
    public static final String PREF_KEY_FCM_TOKEN = "fcmToken";

    // ** OTP Constants **
    public static final String OTP_LENGTH = "6"; // Length of OTP
    public static final String OTP_EXPIRY = "5"; // OTP Expiry Time in minutes

    // ** Country Code Defaults **
    public static final String DEFAULT_COUNTRY_CODE = "US"; // Default country code
    public static final String DEFAULT_COUNTRY_NAME = "United States"; // Default country name

    // ** Other UI Constants (Optional for future use) **
    public static final String BUTTON_TEXT_SEND_OTP = "Send OTP";
    public static final String BUTTON_TEXT_VERIFY_OTP = "Verify OTP";
    public static final String TOAST_INVALID_PHONE = "Please enter a valid phone number";
    public static final String TOAST_INVALID_OTP = "Invalid OTP, please try again";
}