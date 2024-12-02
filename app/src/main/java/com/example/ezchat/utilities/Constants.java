package com.example.ezchat.utilities;

/**
 * This class holds all the constants used across the application to ensure consistency
 * in field names, keys, and other shared values.
 */
public class Constants {

    // ** SplashActivity Constants **
    public static final int SPLASH_DELAY = 2000; // 2 seconds delay
    public static final String SPLASH_LOG_TAG = "SPLASH_ACTIVITY"; // Log tag for SplashActivity

    // ** LoginPhoneNumberActivity Constants **
    public static final String LOG_TAG_PHONE_NUMBER = "LOGIN_PHONE_NUMBER_ACTIVITY";
    public static final String ERROR_INVALID_PHONE = "Enter a valid phone number (minimum 10 digits).";

    // ** LoginOtpActivity Constants **
    public static final String LOG_CHATS_FRAGMENT = "CHATS_FRAGMENT";
    public static final int OTP_TIMEOUT_SECONDS = 60; // Timeout duration for OTP verification
    public static final int RESEND_COUNTDOWN_SECONDS = 30; // Countdown duration for resend button
    public static final String ERROR_NO_INTERNET = "No internet connection. Please check your network.";
    public static final String ERROR_OTP_FAILED = "Verification failed. Please try again.";
    public static final String SUCCESS_OTP_VERIFIED = "Verification successful!";
    public static final String INFO_RESEND_CODE = "Resend CODE in %d seconds.";

    // ** Shared Preferences Keys **
    public static final String PREF_KEY_IS_LOGGED_IN = "isLoggedIn"; // Tracks login status
    public static final String PREF_KEY_PHONE = "userPhone"; // Tracks user's phone number
    public static final String PREF_KEY_USERNAME = "username"; // Tracks user's username
    public static final String PREF_KEY_EMAIL = "userEmail"; // Tracks user's email
    public static final String LABEL_YOU = "You"; // Tracks user's FCM token
    public static final String PREF_KEY_PROFILE_PIC = "profilePic"; // Tracks user's profile picture

    // ** Firestore Collection and Field Names **
    public static final String USER_COLLECTION = "users"; // Firestore collection for users
    public static final String CHAT_COLLECTION = "chats"; // Firestore collection for chats
    public static final String CHAT_MODEL = "chatModel"; // Firestore collection for chats
    public static final String MESSAGE_COLLECTION = "messages"; // Firestore collection for messages
    public static final String FIELD_PHONE = "phone"; // Firestore field for user phone
    public static final String FIELD_USERNAME = "username"; // Firestore field for username
    public static final String FIELD_EMAIL = "email"; // Firestore field for email
    public static final String FIELD_LAST_MESSAGE_TIMESTAMP = "lastMessageTimestamp";
    public static final String FIELD_PROFILE_PIC = "profilePic"; // Firestore field for profile picture
    public static final String FIELD_CHAT_ID = "chatId"; // Firestore field for FCM token
    public static final String FIELD_PHONE_NUMBERS = "contacts"; // Firestore field for user's chat rooms
    public static final String FIELD_TASKS = "task"; // Firestore field for FCM token
    public static final String TASKS_COLLECTION = "tasks"; // Firestore field for FCM token
    public static final String FIELD_TIMESTAMP = "timestamp"; // Firestore field for FCM token

    // ** Default Values **
    public static final String DEFAULT_USERNAME = "Guest"; // Default username if not set
    public static final String DEFAULT_COUNTRY_CODE = "US"; // Default country code
    public static final String DEFAULT_COUNTRY_NAME = "United States"; // Default country name

    // ** UI Constants **
    public static final int TOAST_DURATION_LONG = 3500; // Toast duration in milliseconds
    public static final int TOAST_DURATION_SHORT = 2000; // Toast duration in milliseconds
    public static final int PROGRESS_BAR_ANIMATION_DURATION = 500; // Progress bar animation duration

    // ** General Log Tags **
    public static final String LOG_TAG_MAIN_ACTIVITY = "MAIN_ACTIVITY";
    public static final String LOG_TAG_SPLASH = "SPLASH_ACTIVITY";
    public static final String LOG_TAG_PROFILE_FRAGMENT = "PROFILE_FRAGMENT";
    public static final String LOG_TAG_CALENDAR = "CALENDAR_FRAGMENT";

    // ** Error and Info Messages **
    public static final String ERROR_USER_NOT_FOUND = "User not found. Please log in again.";
    public static final String ERROR_PROFILE_UPDATE_FAILED = "Failed to update profile. Please try again.";
    public static final String SUCCESS_PROFILE_UPDATED = "Profile updated successfully!";
    public static final String INFO_WELCOME_BACK = "Welcome back, %s!";
}