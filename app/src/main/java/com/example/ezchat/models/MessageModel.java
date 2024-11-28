package com.example.ezchat.models;

import com.google.firebase.Timestamp;

/**
 * The MessageModel class represents a single message exchanged between two users in a chat room.
 * It contains the sender's ID, the receiver's ID, the message content, and the timestamp of when the message was sent.
 */
public class MessageModel {

    private String senderId;         // The ID of the user who sent the message
    private String receiverId;       // The ID of the user receiving the message
    private String message;          // The content of the message
    private Timestamp timestamp;     // Timestamp when the message was sent
    private String status;           // Status of the message (sent, delivered, read)

    /**
     * Default constructor required for Firebase Firestore to deserialize data.
     */
    public MessageModel() {
    }

    /**
     * Constructor to initialize the message model with necessary data.
     *
     * @param senderId   The ID of the user who sent the message.
     * @param receiverId The ID of the user receiving the message.
     * @param message    The content of the message.
     * @param timestamp  The timestamp when the message was sent.
     * @param status     The status of the message (sent, delivered, read).
     */
    public MessageModel(String senderId, String receiverId, String message, Timestamp timestamp, String status) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters and Setters

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}