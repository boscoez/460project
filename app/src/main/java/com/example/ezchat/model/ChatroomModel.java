package com.example.ezchat.model;

import com.google.firebase.Timestamp;

import java.util.List;

<<<<<<< HEAD
=======
/**
 * Model class representing a chatroom.
 * Contains fields for the chatroom ID, user IDs of participants, the timestamp of the last message,
 * and the sender ID of the last message
 * Used to deserialize and serialize data to and from Firebase Firestore.
 *
 */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
public class ChatroomModel {
    String chatroomId;
    List<String> userIds;
    Timestamp lastMessageTimestamp;
    String lastMessageSenderId;

    public ChatroomModel() {

    }

<<<<<<< HEAD
=======
    /**
     * Constructor to initialize a ChatroomModel with specified chatroom ID, user IDs, last message timestamp,
     * and last message sender ID.
     * @param chatroomId                    The unique ID of the chatroom.
     * @param userIds                       A list of user IDs representing participants in the chatroom.
     * @param lastMessageTimestamp          The timestamp of the last message sent in the chatroom.
     * @param lastMessageSenderId           The ID of the user who sent the last message.
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    public ChatroomModel(String chatroomId, List<String> userIds, Timestamp lastMessageTimestamp, String lastMessageSenderId) {
        this.chatroomId = chatroomId;
        this.userIds = userIds;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.lastMessageSenderId = lastMessageSenderId;
    }

<<<<<<< HEAD
=======
    /**
     * Retrieves the chatroom ID.
     * @return  The unique ID of the chatroom as a String.
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    public String getChatroomId() {
        return chatroomId;
    }

<<<<<<< HEAD
=======
    /**
     * Sets the chatroom ID.
     * @param chatroomId    The unique ID to set for the chatroom.
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    public void setChatroomId(String chatroomId) {
        this.chatroomId = chatroomId;
    }

<<<<<<< HEAD
=======
    /**
     * Retrieves the list of user IDs in the chatroom.
     * @return  A List of user IDs as Strings.
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    public List<String> getUserIds() {
        return userIds;
    }

<<<<<<< HEAD
=======
    /**
     * Sets the list of user IDs for the chatroom.
     * @param userIds    A List of user IDs to set for the chatroom.
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

<<<<<<< HEAD
    public Timestamp getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(Timestamp lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

=======
    /**
     * Retrieves the timestamp of the last message in the chatroom.
     * @return  A Timestamp object representing the time of the last message.
     */
    public Timestamp getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }
    /**
     * Sets the timestamp for the last message in the chatroom.
     *
     * @param lastMessageTimestamp A Timestamp object to set as the last message timestamp.
     */
    public void setLastMessageTimestamp(Timestamp lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }
    /**
     * Retrieves the sender ID of the last message.
     *
     * @return The sender ID of the last message as a String.
     */
    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }
    /**
     * Sets the sender ID for the last message in the chatroom.
     *
     * @param lastMessageSenderId The sender ID to set for the last message.
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }
}
