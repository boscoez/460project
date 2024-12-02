package com.example.ezchat.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a user in the chat application.
 *
 * This model includes fields for user profile information, chat room participation,
 * and tasks mapped to specific dates. It is designed for direct serialization
 * and deserialization with Firestore or other storage mechanisms.
 */
public class UserModel implements Serializable {

    /** The phone number of the user (used as a unique identifier in Firestore). */
    public String phone;

    /** The username chosen by the user. */
    public String username;

    /** The Base64-encoded string representing the user's profile picture. */
    public String profilePic;

    /** The email address of the user. */
    public String email;

    /** The hashed version of the user's password for secure storage. */
    public String hashedPassword;

    /** The Firebase Cloud Messaging (FCM) token for push notifications. */
    public String fcmToken;

    /** A set of unique chat room IDs the user participates in. */
    public transient List<String> chats;

    /** A map of tasks, where the key is a date (in "yyyy-MM-dd" format) and the value is a list of tasks for that date. */
    public transient Map<String, List<String>> tasksByDate;

    /**
     * Default constructor required for serialization/deserialization.
     */
    public UserModel() {
        this.chats = new ArrayList<>(); // Initialize an empty set for unique chats
        this.tasksByDate = new HashMap<>(); // Initialize an empty map for tasks
    }

    /**
     * Parameterized constructor for creating a new UserModel.
     *
     * @param phone    The user's phone number.
     * @param username The user's username.
     */
    public UserModel(String phone, String username) {
        this.phone = phone;
        this.username = username;
        this.chats = new ArrayList<>(); // Initialize an empty set for unique chats
        this.tasksByDate = new HashMap<>(); // Initialize an empty map for tasks
    }

    /**
     * Adds a task to a specific date.
     *
     * @param date The date to associate the task with (in "yyyy-MM-dd" format).
     * @param task The task to add.
     */
    public void addTask(String date, String task) {
        tasksByDate.computeIfAbsent(date, k -> new java.util.ArrayList<>()).add(task);
    }

    /**
     * Removes a task from a specific date.
     *
     * @param date The date to remove the task from (in "yyyy-MM-dd" format).
     * @param task The task to remove.
     */
    public void removeTask(String date, String task) {
        if (tasksByDate.containsKey(date)) {
            tasksByDate.get(date).remove(task);
            if (tasksByDate.get(date).isEmpty()) {
                tasksByDate.remove(date); // Remove the date if no tasks are left
            }
        }
    }

    /**
     * Retrieves the list of tasks for a specific date.
     *
     * @param date The date to retrieve tasks for (in "yyyy-MM-dd" format).
     * @return A list of tasks for the given date, or an empty list if none exist.
     */
    public List<String> getTasksForDate(String date) {
        return tasksByDate.getOrDefault(date, new java.util.ArrayList<>());
    }

    /**
     * Adds a chat room ID to the user's chat set.
     *
     * @param chatId The chat room ID to add.
     */
    public void addChat(String chatId) {
        if (chatId != null && !chatId.trim().isEmpty()) {
            this.chats.add(chatId);
        } else {
            throw new IllegalArgumentException("Chat ID cannot be null or empty.");
        }
    }

    /**
     * Removes a chat room ID from the user's chat set.
     *
     * @param chatId The chat room ID to remove.
     * @return True if the chat ID was removed, false otherwise.
     */
    public boolean removeChat(String chatId) {
        return this.chats.remove(chatId);
    }
}