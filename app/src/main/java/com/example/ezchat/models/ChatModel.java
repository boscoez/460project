package com.example.ezchat.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a chat room with participants and metadata.
 */
public class ChatModel implements Serializable {

    public String chatId; // Unique ID for the chat
    public Date createdDate; // Date the chat was created
    public String creatorPhone; // Phone number of the creator of the chat
    public List<String> contacts; // Set of participant phone numbers
    public List<MessageModel> messages; // List of messages in the chat
    public MessageModel lastMessage; // The last message sent in the chat

    /**
     * Default constructor.
     */
    public ChatModel() {
        this.contacts = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.createdDate = new Date();
    }

    /**
     * Constructor for initializing the ChatModel.
     *
     * @param chatId        The unique ID for the chat.
     * @param contacts  The list of participant phone numbers.
     * @param creatorPhone  The phone number of the chat creator.
     * @param lastMessage   The last message sent in the chat (can be null if none).
     */
    public ChatModel(String chatId, List<String> contacts, String creatorPhone, MessageModel lastMessage) {
        this.chatId = chatId;
        this.contacts = contacts;
        this.creatorPhone = creatorPhone;
        this.createdDate = new Date();
        this.lastMessage = lastMessage;
        this.messages = new ArrayList<>();
    }
}