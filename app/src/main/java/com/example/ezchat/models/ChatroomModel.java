package com.example.ezchat.models;

import android.content.Context;
import android.widget.Toast;

import androidx.core.util.Consumer;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;

/**
 * The ChatroomModel class handles database operations and represents a chat room in the app.
 * It uses phone numbers as unique identifiers for users.
 */
public class ChatroomModel {

    // Firestore keys
    public static final String FIELD_COLLECTION_NAME = "chatrooms";
    public static final String FIELD_CHATROOM_ID = "chatroomId";
    public static final String FIELD_PHONE_NUMBERS = "phoneNumbers";
    public static final String FIELD_CREATOR_PHONE = "creatorPhone";
    public static final String FIELD_LAST_MESSAGE = "lastMessage";
    public static final String FIELD_LAST_MESSAGE_TIMESTAMP = "lastMessageTimestamp";
    public static final String FIELD_LAST_MESSAGE_SENDER_PHONE = "lastMessageSenderPhone";
    private static final String MESSAGES_COLLECTION = "messages";

    public String chatroomId;
    public List<String> phoneNumbers; // List of phone numbers in the chat room
    public String creatorPhone; // Phone number of the creator
    public String lastMessage;
    public Timestamp lastMessageTimestamp;
    public String lastMessageSenderPhone; // Phone number of the last message sender

    private final FirebaseFirestore firestore; // Firestore instance

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
     * @param phoneNumbers         List of phone numbers in the chat room.
     * @param creatorPhone         The phone number of the creator of the chat room.
     * @param lastMessage          The content of the last message.
     * @param lastMessageTimestamp The timestamp of the last message.
     * @param lastMessageSenderPhone The phone number of the user who sent the last message.
     */
    public ChatroomModel(String chatroomId, List<String> phoneNumbers, String creatorPhone, String lastMessage,
                         Timestamp lastMessageTimestamp, String lastMessageSenderPhone) {
        this();
        this.chatroomId = chatroomId;
        this.phoneNumbers = phoneNumbers;
        this.creatorPhone = creatorPhone;
        this.lastMessage = lastMessage;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.lastMessageSenderPhone = lastMessageSenderPhone;
    }

    /**
     * Deletes a chat room and its messages if the current user is the creator.
     *
     * @param chatRoomId   The ID of the chat room to delete.
     * @param currentPhone The phone number of the user attempting to delete the chat room.
     * @param context      Context for displaying Toast messages.
     * @param onComplete   Callback to indicate success or failure.
     */
    public void deleteChatRoom(String chatRoomId, String currentPhone, Context context, Consumer<Boolean> onComplete) {
        firestore.collection(FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String creatorPhone = documentSnapshot.getString(FIELD_CREATOR_PHONE);

                        if (currentPhone.equals(creatorPhone)) {
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
     * @param chatRoomId   The ID of the chat room.
     * @param phoneNumbers List of phone numbers in the chat room.
     * @param creatorPhone The phone number of the creator of the chat room.
     * @param onComplete   Callback for success or failure.
     */
    public void createChatRoom(String chatRoomId, List<String> phoneNumbers, String creatorPhone, Consumer<Boolean> onComplete) {
        ChatroomModel chatRoom = new ChatroomModel(chatRoomId, phoneNumbers, creatorPhone, null, null, null);

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
     * @param context    The application context for error messages.
     * @param onComplete A callback to handle the fetched ChatroomModel or null if not found.
     */
    public static void fetchChatRoomDetails(String chatRoomId, Context context, Consumer<ChatroomModel> onComplete) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

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
     * Adds a message to the chat room's messages collection and updates the chat room metadata.
     *
     * @param chatRoomId   The ID of the chat room.
     * @param senderPhone  The phone number of the user sending the message.
     * @param messageText  The content of the message.
     * @param onComplete   Callback for success or failure.
     */
    public void addMessageToChatRoom(String chatRoomId, String senderPhone, String messageText, Consumer<Boolean> onComplete) {
        CollectionReference messagesRef = firestore.collection(FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .collection(MESSAGES_COLLECTION);

        String messageId = messagesRef.document().getId();
        MessageModel message = new MessageModel(
                senderPhone,
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
     * Updates the metadata of the chat room (e.g., last message, timestamp, sender phone).
     *
     * @param chatRoomId The ID of the chat room.
     * @param message    The message to use for updating metadata.
     * @param onComplete Callback for success or failure.
     */
    private void updateChatRoomMetadata(String chatRoomId, MessageModel message, Consumer<Boolean> onComplete) {
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put(FIELD_LAST_MESSAGE, message.message);
        metadata.put(FIELD_LAST_MESSAGE_TIMESTAMP, message.timestamp);
        metadata.put(FIELD_LAST_MESSAGE_SENDER_PHONE, message.senderPhone);

        firestore.collection(FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .update(metadata)
                .addOnSuccessListener(aVoid -> onComplete.accept(true))
                .addOnFailureListener(e -> onComplete.accept(false));
    }
}