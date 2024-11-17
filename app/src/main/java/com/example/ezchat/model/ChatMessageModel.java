package com.example.ezchat.model;

import com.google.firebase.Timestamp;


/**
 *Model class representing a chat message.
 *Contains fields for the message content, the sender's ID, and the timestamp of the message.
 *Used to deserialize and serialize data to and from Firebase Firestore.
 */

public class ChatMessageModel {
    private String message;
    private String senderId;
    private Timestamp timestamp;

    // No-arg constructor is needed for Firebase deserialization
    public ChatMessageModel() {}

    /**
     * Constructor to initialize a ChatMessageModel with specified message content, sender ID, and timestamp.
     * @param message        The content of the chat message.
     * @param senderId       The ID of the user who sent the message.
     * @param timestamp     The timestamp indicating when the message was sent.
     */

    public ChatMessageModel(String message, String senderId, Timestamp timestamp) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    /** Retrieves the message content.
     * @return The chat message as a String.
     */

    public String getMessage() {
        return message;
    }

    /**
     * Sets the message content.
     * @param message The chat message content to set.
     */

    public void setMessage(String message) {
        this.message = message;
    }


    /**
     *   Retrieves the sender ID.
     * @return The ID of the user who sent the message.
     */


    public String getSenderId() {
        return senderId;
    }


    /**
     * Sets the sender ID.
     * @param senderId The ID of the user to set as the sender.
     */

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }


    /**
     * Retrieves the timestamp of the message.
     * @return  A Timestamp object representing when the message was sent.
     */

    public Timestamp getTimestamp() {
        return timestamp;
    }


    /**
     * Sets the timestamp of the message.
     * @param timestamp The Timestamp object to set as the message timestamp.
     */

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
