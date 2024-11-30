package com.example.ezchat.models;

import androidx.core.util.Consumer;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * ChatroomModel handles operations for chat rooms.
 * FirebaseFirestore instance is passed explicitly to avoid initialization issues.
 */
public class ChatroomModel implements Serializable {

    public static final String FIELD_COLLECTION_NAME = "chatrooms";
    public static final String FIELD_CHATROOM_ID = "chatroomId";
    public static final String FIELD_PHONE_NUMBERS = "phoneNumbers";
    public static final String FIELD_CREATOR_PHONE = "creatorPhone";
    public static final String FIELD_LAST_MESSAGE = "lastMessage";
    public static final String FIELD_LAST_MESSAGE_TIMESTAMP = "lastMessageTimestamp";
    public static final String FIELD_LAST_MESSAGE_SENDER_PHONE = "lastMessageSenderPhone";

    private static final String MESSAGES_COLLECTION = "messages";

    public String chatroomId;
    public List<String> phoneNumbers; // List of participants' phone numbers
    public String creatorPhone;
    public String lastMessage;
    public Date lastMessageTimestamp; // Changed from Timestamp to Date
    public String lastMessageSenderPhone;

    public ChatroomModel() {}

    /**
     * Deletes the chat room if it has no messages.
     */
    public void deleteChatRoomIfEmpty(FirebaseFirestore firestore, String chatRoomId, Consumer<Boolean> onComplete) {
        firestore.collection(FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .collection(MESSAGES_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        firestore.collection(FIELD_COLLECTION_NAME)
                                .document(chatRoomId)
                                .delete()
                                .addOnSuccessListener(aVoid -> onComplete.accept(true))
                                .addOnFailureListener(e -> onComplete.accept(false));
                    } else {
                        onComplete.accept(false);
                    }
                })
                .addOnFailureListener(e -> onComplete.accept(false));
    }

    /**
     * Creates a new chat room with the provided details and adds the chat room ID
     * to the `chatRooms` array for all participating users.
     *
     * @param firestore    The Firestore instance to use.
     * @param chatRoomId   The ID of the chat room.
     * @param phoneNumbers The list of phone numbers of participants in the chat room.
     * @param creatorPhone The phone number of the user who created the chat room.
     * @param onComplete   A callback to notify when the operation is complete.
     */
    public void createChatRoom(FirebaseFirestore firestore, String chatRoomId, List<String> phoneNumbers, String creatorPhone, Consumer<Boolean> onComplete) {
        this.chatroomId = chatRoomId;
        this.phoneNumbers = phoneNumbers;
        this.creatorPhone = creatorPhone;

        // Add the chat room to the `chatrooms` collection
        firestore.collection(FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .set(this)
                .addOnSuccessListener(aVoid -> {
                    // Add the chat room ID to each user's `chatRooms` array
                    addChatRoomToUsers(firestore, phoneNumbers, chatRoomId, success -> {
                        if (Boolean.TRUE.equals(success)) {
                            onComplete.accept(true);
                        } else {
                            onComplete.accept(false);
                        }
                    });
                })
                .addOnFailureListener(e -> onComplete.accept(false));
    }

    /**
     * Adds the chat room ID to the `chatRooms` array of each user in the provided phoneNumbers list.
     *
     * @param firestore    The Firestore instance to use.
     * @param phoneNumbers The list of phone numbers of users to update.
     * @param chatRoomId   The ID of the chat room to add.
     * @param onComplete   A callback to notify when the operation is complete.
     */
    private void addChatRoomToUsers(FirebaseFirestore firestore, List<String> phoneNumbers, String chatRoomId, Consumer<Boolean> onComplete) {
        int[] successCount = {0};

        for (String phoneNumber : phoneNumbers) {
            firestore.collection(UserModel.FIELD_COLLECTION_NAME)
                    .document(phoneNumber)
                    .update(FIELD_COLLECTION_NAME, com.google.firebase.firestore.FieldValue.arrayUnion(chatRoomId))
                    .addOnSuccessListener(aVoid -> {
                        successCount[0]++;
                        if (successCount[0] == phoneNumbers.size()) {
                            onComplete.accept(true);
                        }
                    })
                    .addOnFailureListener(e -> onComplete.accept(false));
        }
    }

    /**
     * Adds a message to the chat room's messages collection.
     */
    public void addMessageToChatRoom(FirebaseFirestore firestore, String chatRoomId, String senderPhone, String recipientPhone, String messageText, Consumer<Boolean> onComplete) {
        MessageModel message = new MessageModel(
                senderPhone,
                recipientPhone,
                messageText,
                Timestamp.now(),
                "sent"
        );

        firestore.collection(FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .collection(MESSAGES_COLLECTION)
                .add(message)
                .addOnSuccessListener(aVoid -> {
                    updateChatRoomMetadata(firestore, chatRoomId, message, onComplete);
                })
                .addOnFailureListener(e -> onComplete.accept(false));
    }

    /**
     * Updates the metadata of the chat room (e.g., last message, timestamp, sender phone).
     */
    private void updateChatRoomMetadata(FirebaseFirestore firestore, String chatRoomId, MessageModel message, Consumer<Boolean> onComplete) {
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put(FIELD_LAST_MESSAGE, message.message);
        metadata.put(FIELD_LAST_MESSAGE_TIMESTAMP, message.timestamp.toDate()); // Store as Date
        metadata.put(FIELD_LAST_MESSAGE_SENDER_PHONE, message.senderPhone);

        firestore.collection(FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .update(metadata)
                .addOnSuccessListener(aVoid -> onComplete.accept(true))
                .addOnFailureListener(e -> onComplete.accept(false));
    }

    /**
     * Fetches all messages from the chat room.
     */
    public void fetchMessages(FirebaseFirestore firestore, String chatRoomId, Consumer<QuerySnapshot> onComplete) {
        firestore.collection(FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .collection(MESSAGES_COLLECTION)
                .orderBy(MessageModel.FIELD_TIMESTAMP)
                .get()
                .addOnSuccessListener(onComplete::accept)
                .addOnFailureListener(e -> onComplete.accept(null));
    }

    /**
     * Listens for real-time updates in the chat room messages.
     */
    public void listenForMessages(FirebaseFirestore firestore, String chatRoomId, Consumer<MessageModel> onMessage, Consumer<String> onError) {
        firestore.collection(FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .collection(MESSAGES_COLLECTION)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        onError.accept(error.getMessage());
                        return;
                    }

                    if (snapshots != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                            MessageModel message = doc.toObject(MessageModel.class);
                            onMessage.accept(message);
                        }
                    }
                });
    }
}