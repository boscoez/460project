package com.example.ezchat.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a message exchanged between users in a chat room.
 */
public class MessageModel implements Serializable {

    public String senderPhone; // Phone number of the sender
    public List<String> receiverPhones; // List of phone numbers of the receivers
    public String message; // The content of the message
    public Date timestamp; // Timestamp of when the message was sent
    public String status; // Status of the message (e.g., sent, delivered, read)

    /**
     * Default constructor.
     */
    public MessageModel() {
        this.receiverPhones = new ArrayList<>();
        this.timestamp = new Date(); // Default timestamp is the current time
        this.status = "sent"; // Default status is "sent"
    }

    /**
     * Constructor for initializing a MessageModel.
     *
     * @param senderPhone   The sender's phone number.
     * @param receiverPhones The receiver's phone numbers.
     * @param message       The message content.
     */
    public MessageModel(String senderPhone, List<String> receiverPhones, String message) {
        this.senderPhone = senderPhone;
        this.receiverPhones = receiverPhones;
        this.message = message;
        this.timestamp = new Date();
        this.status = "sent";
    }
}