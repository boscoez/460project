package com.example.ezchat.models;

import com.google.firebase.Timestamp;
import java.util.List;

/**
 * The ChatroomModel class represents a chat room in the application.
 * It holds information about the chat room, including the participants, 
 * the creator of the room, the last message sent, and metadata such as 
 * the timestamp of the last message.
 *
 * It is used for managing chat rooms and their associated data in Firestore.
 */
public class ChatroomModel {

    private String chatroomId;              // Unique identifier for the chat room
    private List<String> userIds;           // List of user IDs (phone numbers) who are part of this chat room
    private String creatorId;               // ID of the user who created the chat room
    private Timestamp lastMessageTimestamp; // Timestamp indicating when the last message was sent
    private String lastMessageSenderId;     // ID of the user who sent the last message
    private String lastMessage;             // The content of the last message
    private List<MessageModel> messages;    // List of messages (this is not directly stored in Firestore, but represented in the model)

    /**
     * Constructor to initialize the ChatroomModel with essential fields.
     *
     * @param chatroomId The unique ID for the chat room.
     * @param userIds The list of user IDs involved in the chat room.
     * @param creatorId The ID of the user who created the chat room.
     * @param lastMessageTimestamp The timestamp of the last message sent in the chat room.
     * @param lastMessageSenderId The ID of the user who sent the last message.
     * @param lastMessage The content of the last message.
     */
    public ChatroomModel(String chatroomId, List<String> userIds, String creatorId,
                         Timestamp lastMessageTimestamp, String lastMessageSenderId, String lastMessage) {
        this.chatroomId = chatroomId;
        this.userIds = userIds;
        this.creatorId = creatorId;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.lastMessageSenderId = lastMessageSenderId;
        this.lastMessage = lastMessage;
    }

    // Getters and Setters

    public String getChatroomId() {
        return chatroomId;
    }

    public void setChatroomId(String chatroomId) {
        this.chatroomId = chatroomId;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public Timestamp getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(Timestamp lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public List<MessageModel> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageModel> messages) {
        this.messages = messages;
    }

}