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
 * ChatModel handles operations for chats.
 * FirebaseFirestore instance is passed explicitly to avoid initialization issues.
 */
public class ChatModel implements Serializable {

    public static final String KEY_CHAT = "chat";
    public static final String FIELD_COLLECTION_NAME = "chats";
    public static final String FIELD_CHAT_ID = "chatId";
    public static final String FIELD_PHONE_NUMBERS = "phoneNumbers";
    public static final String FIELD_CREATOR_PHONE = "creatorPhone";
    public static final String FIELD_LAST_MESSAGE = "lastMessage";
    public static final String FIELD_LAST_MESSAGE_TIMESTAMP = "lastMessageTimestamp";
    public static final String FIELD_LAST_MESSAGE_SENDER_PHONE = "lastMessageSenderPhone";

    private static final String MESSAGES_COLLECTION = "messages";

    public String chatId;
    public List<String> phoneNumbers; // List of participants' phone numbers
    public String creatorPhone;
    public String lastMessage;
    public Date lastMessageTimestamp; // Changed from Timestamp to Date
    public String lastMessageSenderPhone;

    public ChatModel() {}

    /**
     * Creates a new chat with the provided details and adds the chat ID
     * to the `chat` array for all participating users.
     *
     * @param firestore    The Firestore instance to use.
     * @param chatId   The ID of the chat.
     * @param phoneNumbers The list of phone numbers of participants in the chat.
     * @param creatorPhone The phone number of the user who created the chat.
     * @param onComplete   A callback to notify when the operation is complete.
     */
    public void createChat(FirebaseFirestore firestore, String chatId, List<String> phoneNumbers, String creatorPhone, Consumer<Boolean> onComplete) {
        this.chatId = chatId;
        this.phoneNumbers = phoneNumbers;
        this.creatorPhone = creatorPhone;

        // Add the chat to the `chat` collection
        firestore.collection(FIELD_COLLECTION_NAME)
                .document(chatId)
                .set(this)
                .addOnSuccessListener(aVoid -> {
                    // Add the chat ID to each user's `chat` array
                    addChatToUsers(firestore, phoneNumbers, chatId, success -> {
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
     * Adds the chat ID to the array of each user in the provided phoneNumbers list.
     *
     * @param firestore    The Firestore instance to use.
     * @param phoneNumbers The list of phone numbers of users to update.
     * @param chatId   The ID of the chat to add.
     * @param onComplete   A callback to notify when the operation is complete.
     */
    private void addChatToUsers(FirebaseFirestore firestore, List<String> phoneNumbers, String chatId, Consumer<Boolean> onComplete) {
        int[] successCount = {0};

        for (String phoneNumber : phoneNumbers) {
            firestore.collection(UserModel.FIELD_COLLECTION_NAME)
                    .document(phoneNumber)
                    .update(FIELD_COLLECTION_NAME, com.google.firebase.firestore.FieldValue.arrayUnion(chatId))
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
     * Adds a message to the chat messages collection.
     */
    public void addMessageToChat(FirebaseFirestore firestore, String chatId, String senderPhone, String recipientPhone, String messageText, Consumer<Boolean> onComplete) {
        MessageModel message = new MessageModel(
                senderPhone,
                recipientPhone,
                messageText,
                Timestamp.now(),
                "sent"
        );

        firestore.collection(FIELD_COLLECTION_NAME)
                .document(chatId)
                .collection(MESSAGES_COLLECTION)
                .add(message)
                .addOnSuccessListener(aVoid -> {
                    updateChatMetadata(firestore, chatId, message, onComplete);
                })
                .addOnFailureListener(e -> onComplete.accept(false));
    }

    /**
     * Updates the metadata of the chat (e.g., last message, timestamp, sender phone).
     */
    private void updateChatMetadata(FirebaseFirestore firestore, String chatId, MessageModel message, Consumer<Boolean> onComplete) {
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put(FIELD_LAST_MESSAGE, message.message);
        metadata.put(FIELD_LAST_MESSAGE_TIMESTAMP, message.timestamp.toDate()); // Store as Date
        metadata.put(FIELD_LAST_MESSAGE_SENDER_PHONE, message.senderPhone);

        firestore.collection(FIELD_COLLECTION_NAME)
                .document(chatId)
                .update(metadata)
                .addOnSuccessListener(aVoid -> onComplete.accept(true))
                .addOnFailureListener(e -> onComplete.accept(false));
    }

    /**
     * Fetches all messages from the chat.
     */
    public void fetchMessages(FirebaseFirestore firestore, String chatId, Consumer<QuerySnapshot> onComplete) {
        firestore.collection(FIELD_COLLECTION_NAME)
                .document(chatId)
                .collection(MESSAGES_COLLECTION)
                .orderBy(MessageModel.FIELD_TIMESTAMP)
                .get()
                .addOnSuccessListener(onComplete::accept)
                .addOnFailureListener(e -> onComplete.accept(null));
    }

    /**
     * Listens for real-time updates in the chat messages.
     */
    public void listenForMessages(FirebaseFirestore firestore, String chatId, Consumer<MessageModel> onMessage, Consumer<String> onError) {
        firestore.collection(FIELD_COLLECTION_NAME)
                .document(chatId)
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