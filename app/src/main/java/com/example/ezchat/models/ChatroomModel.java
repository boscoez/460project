package com.example.ezchat.models;

import android.content.Context;
import android.widget.Toast;

import androidx.core.util.Consumer;

import com.example.ezchat.models.MessageModel;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;

/**
 * The ChatroomModel class handles database operations and represents a chat room in the app.
 * It includes methods for creating, deleting, fetching, and updating chat rooms and their messages.
 */
public class ChatroomModel {

    // Firestore keys
    public static final String FIELD_COLLECTION_NAME = "chatrooms";
    public static final String FIELD_CHATROOM_ID = "chatroomId";
    public static final String FIELD_USER_IDS = "userIds";
    public static final String FIELD_CREATOR_ID = "creatorId";
    public static final String FIELD_LAST_MESSAGE = "lastMessage";
    public static final String FIELD_LAST_MESSAGE_TIMESTAMP = "lastMessageTimestamp";
    public static final String FIELD_LAST_MESSAGE_SENDER_ID = "lastMessageSenderId";
    private static final String MESSAGES_COLLECTION = "messages";

    public String chatroomId;
    public List<String> userIds;
    public String creatorId;
    public String lastMessage;
    public Timestamp lastMessageTimestamp;
    public String lastMessageSenderId;

    // Firestore instance
    private final FirebaseFirestore firestore;

    /**
     * Default constructor for ChatroomModel.
     * Initializes the Firestore instance.
     */
    public ChatroomModel() {
        firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Parameterized constructor for ChatroomModel.
     *
     * @param chatroomId           The ID of the chat room.
     * @param userIds              List of user IDs in the chat room.
     * @param creatorId            The ID of the creator of the chat room.
     * @param lastMessage          The content of the last message.
     * @param lastMessageTimestamp The timestamp of the last message.
     * @param lastMessageSenderId  The ID of the user who sent the last message.
     */
    public ChatroomModel(String chatroomId, List<String> userIds, String creatorId, String lastMessage,
                         Timestamp lastMessageTimestamp, String lastMessageSenderId) {
        this();
        this.chatroomId = chatroomId;
        this.userIds = userIds;
        this.creatorId = creatorId;
        this.lastMessage = lastMessage;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.lastMessageSenderId = lastMessageSenderId;
    }

    /**
     * Deletes a chat room and its messages if the current user is the creator.
     *
     * @param chatRoomId    The ID of the chat room to delete.
     * @param currentUserId The ID of the user attempting to delete the chat room.
     * @param context       Context for displaying Toast messages.
     * @param onComplete    Callback to indicate success or failure.
     */
    public void deleteChatRoom(String chatRoomId, String currentUserId, Context context, Consumer<Boolean> onComplete) {
        firestore.collection(FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String creatorId = documentSnapshot.getString(FIELD_CREATOR_ID);

                        if (currentUserId.equals(creatorId)) {
                            deleteChatRoomAndMessages(chatRoomId, context, onComplete);
                        } else {
                            onComplete.accept(false);
                            Toast.makeText(context, "Only the creator can delete this chat room.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        onComplete.accept(false);
                        Toast.makeText(context, "Chat room not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    onComplete.accept(false);
                    Toast.makeText(context, "Failed to fetch chat room details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Deletes a chat room document and its messages sub-collection.
     *
     * @param chatRoomId The ID of the chat room to delete.
     * @param context    Context for displaying Toast messages.
     * @param onComplete Callback to indicate success or failure.
     */
    private void deleteChatRoomAndMessages(String chatRoomId, Context context, Consumer<Boolean> onComplete) {
        CollectionReference messagesRef = firestore.collection(FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .collection(MESSAGES_COLLECTION);

        messagesRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot messageDoc : querySnapshot.getDocuments()) {
                        messageDoc.getReference().delete();
                    }

                    firestore.collection(FIELD_COLLECTION_NAME)
                            .document(chatRoomId)
                            .delete()
                            .addOnSuccessListener(aVoid -> onComplete.accept(true))
                            .addOnFailureListener(e -> {
                                onComplete.accept(false);
                                Toast.makeText(context, "Failed to delete chat room: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    onComplete.accept(false);
                    Toast.makeText(context, "Failed to delete messages: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Creates a new chat room in Firestore.
     *
     * @param chatRoomId The ID of the chat room.
     * @param userIds    List of user IDs in the chat room.
     * @param creatorId  The ID of the creator of the chat room.
     * @param onComplete Callback for success or failure.
     */
    public void createChatRoom(String chatRoomId, List<String> userIds, String creatorId, Consumer<Boolean> onComplete) {
        ChatroomModel chatRoom = new ChatroomModel(chatRoomId, userIds, creatorId, null, null, null);

        firestore.collection(FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .set(chatRoom)
                .addOnSuccessListener(aVoid -> onComplete.accept(true))
                .addOnFailureListener(e -> onComplete.accept(false));
    }

    /**
     * Fetches the details of a chat room from Firestore.
     *
     * @param chatRoomId The ID of the chat room to fetch.
     * @param context    Context for displaying Toast messages.
     * @param onComplete A callback to handle the fetched ChatroomModel or null if not found.
     */
    public void fetchChatRoomDetails(String chatRoomId, Context context, Consumer<ChatroomModel> onComplete) {
        firestore.collection(FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ChatroomModel chatRoom = documentSnapshot.toObject(ChatroomModel.class);
                        onComplete.accept(chatRoom);
                    } else {
                        onComplete.accept(null);
                    }
                })
                .addOnFailureListener(e -> {
                    onComplete.accept(null);
                    Toast.makeText(context, "Failed to fetch chat room details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Fetches all messages for a chat room.
     *
     * @param chatRoomId The ID of the chat room.
     * @param onResult   Callback to return the messages or null in case of failure.
     */
    public void fetchMessages(String chatRoomId, Consumer<QuerySnapshot> onResult) {
        firestore.collection(FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .collection(MESSAGES_COLLECTION)
                .orderBy(MessageModel.FIELD_TIMESTAMP, Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(onResult::accept)
                .addOnFailureListener(e -> onResult.accept(null));
    }

    /**
     * Deletes a chat room if there are no messages.
     *
     * @param chatRoomId The ID of the chat room.
     * @param context    Context for displaying Toast messages.
     * @param onComplete Callback for success or failure.
     */
    public void deleteEmptyChatRoom(String chatRoomId, Context context, Consumer<Boolean> onComplete) {
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
                                .addOnFailureListener(e -> {
                                    onComplete.accept(false);
                                    Toast.makeText(context, "Failed to delete chat room: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        onComplete.accept(false);
                    }
                })
                .addOnFailureListener(e -> {
                    onComplete.accept(false);
                    Toast.makeText(context, "Failed to check messages: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Adds a message to the chatroom's messages collection and updates the chatroom metadata.
     *
     * @param chatRoomId  The ID of the chat room.
     * @param senderId    The ID of the user sending the message.
     * @param messageText The content of the message.
     * @param onComplete  Callback for success or failure.
     */
    public void addMessageToChatRoom(String chatRoomId, String senderId, String messageText, Consumer<Boolean> onComplete) {
        CollectionReference messagesRef = firestore.collection(FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .collection(MESSAGES_COLLECTION);

        String messageId = messagesRef.document().getId();
        MessageModel message = new MessageModel(
                senderId,
                null,
                messageText,
                Timestamp.now(),
                "sent"
        );

        messagesRef.document(messageId)
                .set(message)
                .addOnSuccessListener(aVoid -> updateChatRoomMetadata(chatRoomId, message, onComplete))
                .addOnFailureListener(e -> onComplete.accept(false));
    }

    /**
     * Updates the metadata of the chat room (e.g., last message, timestamp, sender ID).
     *
     * @param chatRoomId The ID of the chat room.
     * @param message    The message to use for updating metadata.
     * @param onComplete Callback for success or failure.
     */
    private void updateChatRoomMetadata(String chatRoomId, MessageModel message, Consumer<Boolean> onComplete) {
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put(FIELD_LAST_MESSAGE, message.message);
        metadata.put(FIELD_LAST_MESSAGE_TIMESTAMP, message.timestamp);
        metadata.put(FIELD_LAST_MESSAGE_SENDER_ID, message.senderId);

        firestore.collection(FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .update(metadata)
                .addOnSuccessListener(aVoid -> onComplete.accept(true))
                .addOnFailureListener(e -> onComplete.accept(false));
    }
}