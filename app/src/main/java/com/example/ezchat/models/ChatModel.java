package com.example.ezchat.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a chat room with participants and metadata.
 * It contains details such as the chat ID, the participants' phone numbers,
 * the creator's phone number, the last message sent, the creation date, and the messages in the chat.
 */
public class ChatModel implements Serializable {

    public final String chatId; // Unique ID for the chat (final, immutable)
    public final Date createdDate; // Date the chat was created (final, immutable)
    public final String creatorPhone; // Phone number of the creator of the chat (final, immutable)
    public final List<String> phoneNumbers; // List of participant phone numbers (can be modified by adding participants)
    public final List<MessageModel> messages; // List of messages in the chat (can be modified by adding messages)
    public MessageModel lastMessage; // The last message sent in the chat

    /**
     * Constructor for initializing the ChatModel.
     * Throws IllegalArgumentException if any required field is null or empty.
     *
     * @param chatId        The unique ID for the chat.
     * @param phoneNumbers  The list of participant phone numbers.
     * @param creatorPhone  The phone number of the chat creator.
     * @param lastMessage   The last message sent in the chat (can be null if none).
     */
    public ChatModel(String chatId, List<String> phoneNumbers, String creatorPhone, MessageModel lastMessage) {
        this.chatId = requireNonNullOrEmpty(chatId, "Chat ID cannot be null or empty.");
        this.phoneNumbers = new ArrayList<>(requireNonNullOrEmpty(phoneNumbers, "Phone numbers cannot be null or empty."));
        this.creatorPhone = requireNonNullOrEmpty(creatorPhone, "Creator phone cannot be null or empty.");
        this.createdDate = new Date(); // Set the creation date to the current time when the chat is created
        this.lastMessage = lastMessage;
        this.messages = new ArrayList<>(); // Initialize messages list
    }

    /**
     * Add a participant to the chat (phone number).
     */
    public void addParticipant(String phoneNumber) {
        if (!phoneNumbers.contains(phoneNumber)) {
            phoneNumbers.add(phoneNumber);
        }
    }

    /**
     * Add a message to the chat.
     */
    public void addMessage(MessageModel message) {
        if (message != null) {
            messages.add(message);
        }
    }

    /**
     * Utility method to ensure a string or list is not null or empty.
     *
     * @param value          The value to check.
     * @param errorMessage   The error message to throw in case of invalid value.
     * @return The validated value.
     * @throws IllegalArgumentException if the value is null or empty.
     */
    private <T> T requireNonNullOrEmpty(T value, String errorMessage) {
        if (value == null || (value instanceof String && ((String) value).trim().isEmpty()) ||
                (value instanceof List && ((List<?>) value).isEmpty())) {
            throw new IllegalArgumentException(errorMessage);
        }
        return value;
    }
}