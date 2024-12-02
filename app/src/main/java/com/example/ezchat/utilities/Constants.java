package com.example.ezchat.utilities;

/**
 * This class holds all the constants used across the application to ensure consistency
 * in field names, keys, and other shared values.
 */
public class Constants {
    // ** SplashActivity Constants **
    public static final int SPLASH_DELAY = 2000;
    public static final int TOAST_DURATION_LONG = 3500; // Toast duration in milliseconds
    public static final int TOAST_DURATION_SHORT = 2000; // Toast duration in milliseconds
    public static final int PROGRESS_BAR_ANIMATION_DURATION = 500; // Progress bar animation duration

    public static final String DEFAULT_USERNAME = "Guest";
    public static final String DEFAULT_COUNTRY_CODE = "US";

    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    public static final String LABEL_YOU = "You";


    public static final String LOG_TAG_MAIN_ACTIVITY = "MAIN_ACTIVITY";
    public static final String LOG_TAG_SPLASH = "SPLASH_ACTIVITY";
    public static final String LOG_TAG_PHONE_NUMBER = "LOGIN_PHONE_NUMBER_ACTIVITY";
    public static final String LOG_CHATS_FRAGMENT = "CHATS_FRAGMENT";
    public static final String LOG_TAG_PROFILE_FRAGMENT = "PROFILE_FRAGMENT";
    public static final String LOG_TAG_CALENDAR = "CALENDAR_FRAGMENT";

    public static final String ERROR_INVALID_PHONE = "Enter a valid phone number (minimum 10 digits).";


    public static final String COLLECTION_CHATS = "chats";
    public static final String COLLECTION_MESSAGES = "messages"; // subcolection of chat
    public static final String COLLECTION_PHONES = "contacts";
    public static final String COLLECTION_TASKS = "tasks";
    public static final String COLLECTION_USERS = "users";


    public static final String MODEL_CHAT = "chatModel";
    public static final String FIELD_CHAT_ID = "chatId";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_LAST_MESSAGE = "lastMessage";
    public static final String FIELD_LAST_MESSAGE_TIMESTAMP = "lastMessageTimestamp";
    public static final String FIELD_PHONE = "phone";
    public static final String FIELD_PROFILE_PIC = "profilePic";
    public static final String FIELD_TIMESTAMP = "timestamp";
    public static final String FIELD_USERNAME = "username";
}