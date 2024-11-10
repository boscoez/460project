package com.example.ezchat.model;

import com.google.firebase.Timestamp;

<<<<<<< HEAD
=======
/**
 *Model class representing a chat message.
 *Contains fields for the message content, the sender's ID, and the timestamp of the message.
 *Used to deserialize and serialize data to and from Firebase Firestore.
 */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
public class ChatMessageModel {
    private String message;
    private String senderId;
    private Timestamp timestamp;

    // No-arg constructor is needed for Firebase deserialization
    public ChatMessageModel() {}

<<<<<<< HEAD
=======
    /**
     * Constructor to initialize a ChatMessageModel with specified message content, sender ID, and timestamp.
     * @param message        The content of the chat message.
     * @param senderId       The ID of the user who sent the message.
     * @param timestamp     The timestamp indicating when the message was sent.
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    public ChatMessageModel(String message, String senderId, Timestamp timestamp) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

<<<<<<< HEAD
=======
    /** Retrieves the message content.
     * @return The chat message as a String.
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    public String getMessage() {
        return message;
    }

<<<<<<< HEAD
=======
    /**
     * Sets the message content.
     * @param message The chat message content to set.
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    public void setMessage(String message) {
        this.message = message;
    }

<<<<<<< HEAD
=======
    /**
     *   Retrieves the sender ID.
     * @return The ID of the user who sent the message.
     */

>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    public String getSenderId() {
        return senderId;
    }

<<<<<<< HEAD
=======
    /**
     * Sets the sender ID.
     * @param senderId The ID of the user to set as the sender.
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

<<<<<<< HEAD
=======
    /**
     * Retrieves the timestamp of the message.
     * @return  A Timestamp object representing when the message was sent.
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    public Timestamp getTimestamp() {
        return timestamp;
    }

<<<<<<< HEAD
=======
    /**
     * Sets the timestamp of the message.
     * @param timestamp The Timestamp object to set as the message timestamp.
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
