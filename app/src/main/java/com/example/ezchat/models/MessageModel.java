package com.example.ezchat.models;

import com.google.firebase.Timestamp;

/**
 * Represents a single message exchanged between users in a chat room.
 * It includes details such as sender, receiver, message content, timestamp, and message status.
 */
public class MessageModel {

    // Firestore field constants for MessageModel
    public static final String FIELD_COLLECTION_NAME = "messages";
    public static final String FIELD_SENDER_PHONE = "senderPhone";
    public static final String FIELD_RECEIVER_PHONE = "receiverPhone";
    public static final String FIELD_MESSAGE = "message";
    public static final String FIELD_TIMESTAMP = "timestamp";
    public static final String FIELD_STATUS = "status";

    /**
     * The phone number of the user who sent the message.
     */
    public String senderPhone;

    /**
     * The phone number of the user who received the message.
     */
    public String receiverPhone;

    /**
     * The content of the message.
     */
    public String message;

    /**
     * The timestamp when the message was sent.
     */
    public Timestamp timestamp;

    /**
     * The status of the message (e.g., sent, delivered, read).
     */
    public String status;

    /**
     * Default constructor required for Firebase Firestore to deserialize data.
     */
    public MessageModel() {}

    /**
     * Constructs a new MessageModel with the specified sender phone, receiver phone,
     * message content, timestamp, and status.
     *
     * @param senderPhone   The phone number of the user who sent the message.
     * @param receiverPhone The phone number of the user who received the message.
     * @param message       The content of the message.
     * @param timestamp     The timestamp when the message was sent.
     * @param status        The status of the message (e.g., sent, delivered, read).
     */
    public MessageModel(String senderPhone, String receiverPhone, String message, Timestamp timestamp, String status) {
        this.senderPhone = senderPhone;
        this.receiverPhone = receiverPhone;
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
    }
}