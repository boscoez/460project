package com.example.ezchat.models;

import java.io.Serializable;
import java.util.List;
import java.util.Date;

/**
 * Represents a message exchanged between users in a chat room.
 * It contains details such as the sender's phone number,
 * a list of receiver phone numbers, the message content,
 * the timestamp of the message, and its status.
 */
public class MessageModel implements Serializable{

    private String senderPhone; // Phone number of the sender
    private transient List<String> receiverPhones; // List of phone numbers of the receivers
    private String message; // The content of the message
    private Date timestamp; // Timestamp of when the message was sent
    private String status; // Status of the message (e.g., sent, delivered, read)

    public MessageModel(){}

    /**
     * Constructor to initialize a new message.
     * Throws IllegalArgumentException if any field is null or empty.
     *
     * @param senderPhone    The phone number of the message sender.
     * @param receiverPhones The list of phone numbers of the receivers.
     * @param message        The content of the message.
     */
    public MessageModel(String senderPhone, List<String> receiverPhones, String message) {
        this.senderPhone = requireNonNullOrEmpty(senderPhone, "Sender phone cannot be null or empty.");
        this.receiverPhones = requireNonNullOrEmpty(receiverPhones, "Receiver phones cannot be null or empty.");
        this.message = requireNonNullOrEmpty(message, "Message cannot be null or empty.");

        // Set the status to "sent" by default
        this.status = "sent";

        // Set the timestamp to the current time when the message is created
        this.timestamp = new Date();
    }

    // Getters

    public String getSenderPhone() {
        return senderPhone;
    }

    public List<String> getReceiverPhones() {
        return receiverPhones;
    }

    public String getMessage() {
        return message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status;
    }

    // Setters (Only for fields that may change)

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = requireNonNullOrEmpty(senderPhone, "Sender phone cannot be null or empty.");
    }

    public void setReceiverPhones(List<String> receiverPhones) {
        this.receiverPhones = requireNonNullOrEmpty(receiverPhones, "Receiver phones cannot be null or empty.");
    }

    public void setMessage(String message) {
        this.message = requireNonNullOrEmpty(message, "Message cannot be null or empty.");
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setStatus(String status) {
        this.status = requireNonNullOrEmpty(status, "Status cannot be null or empty.");
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