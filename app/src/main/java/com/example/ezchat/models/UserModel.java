package com.example.ezchat.models;

import android.content.Context;
import android.widget.Toast;

import androidx.core.util.Consumer;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a user in the application, containing profile information,
 * contact details, metadata such as creation timestamp, FCM token, and the chat rooms
 * the user is part of. All user-related database actions are implemented here.
 */
public class UserModel implements Serializable {

    // Firestore collection and field constants
    public static final String FIELD_COLLECTION_NAME = "users";
    public static final String FIELD_PHONE = "phone";
    public static final String FIELD_USERNAME = "username";
    public static final String FIELD_CREATED_TIMESTAMP = "createdTimestamp";
    public static final String FIELD_FCM_TOKEN = "fcmToken";
    public static final String FIELD_PROFILE_PIC = "profilePic";
    public static final String FIELD_CHAT_ROOMS = "chatRooms";

    // Public fields for user data
    public String phone;
    public String username;
    public Timestamp createdTimestamp;
    public String fcmToken;
    public String profilePic;
    public List<String> chatRooms;

    /**
     * Default constructor for UserModel.
     */
    public UserModel() {
        this.chatRooms = new ArrayList<>();
    }

    /**
     * Parameterized constructor to initialize the user model with essential fields.
     */
    public UserModel(String phone, String username, Timestamp createdTimestamp,
                     String fcmToken, String profilePic, List<String> chatRooms) {
        this.phone = phone;
        this.username = username;
        this.createdTimestamp = createdTimestamp;
        this.fcmToken = fcmToken;
        this.profilePic = profilePic;
        this.chatRooms = chatRooms != null ? chatRooms : new ArrayList<>();
    }

    /**
     * Checks if the user model has valid data.
     *
     * @return True if all required fields are valid, false otherwise.
     */
    public boolean isValid() {
        return phone != null && !phone.isEmpty() &&
                username != null && !username.isEmpty();
    }

    /**
     * Adds a chat room ID to the user's chat rooms list.
     *
     * @param chatRoomId The chat room ID to add.
     */
    public void addChatRoom(String chatRoomId) {
        if (!chatRooms.contains(chatRoomId)) {
            chatRooms.add(chatRoomId);
        }
    }

    /**
     * Removes a chat room ID from the user's chat rooms list.
     *
     * @param chatRoomId The chat room ID to remove.
     */
    public void removeChatRoom(String chatRoomId) {
        chatRooms.remove(chatRoomId);
    }

    /**
     * Fetches the current user's chat rooms from Firestore.
     *
     * @param firestore   The Firestore instance to use.
     * @param onComplete  Callback for success (with a list of chat room IDs).
     * @param onFailure   Callback for failure (with error message).
     */
    public void fetchChatRooms(FirebaseFirestore firestore, Consumer<List<String>> onComplete, Consumer<String> onFailure) {
        firestore.collection(FIELD_COLLECTION_NAME)
                .document(phone)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserModel user = documentSnapshot.toObject(UserModel.class);
                        if (user != null && user.chatRooms != null) {
                            onComplete.accept(user.chatRooms);
                        } else {
                            onComplete.accept(new ArrayList<>()); // Empty list if no chat rooms
                        }
                    } else {
                        onFailure.accept("User data not found.");
                    }
                })
                .addOnFailureListener(e -> onFailure.accept("Failed to fetch chat rooms: " + e.getMessage()));
    }

    /**
     * Sends a message to a chat room.
     *
     * @param firestore      The Firestore instance to use.
     * @param chatRoomId     The ID of the chat room.
     * @param recipientPhone The phone number of the recipient.
     * @param messageText    The message text to send.
     * @param onComplete     Callback for success or failure.
     */
    public void sendMessage(FirebaseFirestore firestore, String chatRoomId, String recipientPhone, String messageText, Consumer<Boolean> onComplete) {
        // Create a new message
        MessageModel message = new MessageModel(
                phone, // Sender is the current user
                recipientPhone,
                messageText,
                Timestamp.now(),
                "sent"
        );

        // Add the message to the Firestore subcollection
        firestore.collection(ChatModel.FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(aVoid -> {
                    // Update chat room metadata
                    updateChatRoomMetadata(firestore, chatRoomId, message, onComplete);
                })
                .addOnFailureListener(e -> onComplete.accept(false));
    }

    /**
     * Updates the chat room metadata with the last message details.
     *
     * @param firestore  The Firestore instance to use.
     * @param chatRoomId The ID of the chat room.
     * @param message    The message details to update.
     * @param onComplete Callback for success or failure.
     */
    private void updateChatRoomMetadata(FirebaseFirestore firestore, String chatRoomId, MessageModel message, Consumer<Boolean> onComplete) {
        firestore.collection(ChatModel.FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .update(
                        ChatModel.FIELD_LAST_MESSAGE, message.message,
                        ChatModel.FIELD_LAST_MESSAGE_TIMESTAMP, message.timestamp,
                        ChatModel.FIELD_LAST_MESSAGE_SENDER_PHONE, message.senderPhone
                )
                .addOnSuccessListener(aVoid -> onComplete.accept(true))
                .addOnFailureListener(e -> onComplete.accept(false));
    }

    /**
     * Updates the user's chat room list in Firestore.
     *
     * @param firestore  The Firestore instance to use.
     * @param onComplete Callback for success or failure.
     */
    public void updateChatRooms(FirebaseFirestore firestore, Consumer<Boolean> onComplete) {
        firestore.collection(FIELD_COLLECTION_NAME)
                .document(phone)
                .update(FIELD_CHAT_ROOMS, this.chatRooms)
                .addOnSuccessListener(aVoid -> onComplete.accept(true))
                .addOnFailureListener(e -> onComplete.accept(false));
    }

    /**
     * Deletes a chat room if the current user is the creator.
     *
     * @param firestore     The Firestore instance to use.
     * @param chatRoomId    The ID of the chat room to delete.
     * @param context       The context for showing Toast messages.
     * @param onComplete    Callback for success or failure.
     */
    public void deleteChatRoom(FirebaseFirestore firestore, String chatRoomId, Context context, Consumer<Boolean> onComplete) {
        firestore.collection(ChatModel.FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ChatModel chatRoom = documentSnapshot.toObject(ChatModel.class);
                        if (chatRoom != null && phone.equals(chatRoom.creatorPhone)) {
                            deleteChatRoomAndMessages(firestore, chatRoomId, onComplete);
                        } else {
                            onComplete.accept(false);
                            Toast.makeText(context, "You are not the creator of this chat room.", Toast.LENGTH_SHORT).show();
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
     * Deletes the chat room and its messages.
     */
    private void deleteChatRoomAndMessages(FirebaseFirestore firestore, String chatRoomId, Consumer<Boolean> onComplete) {
        firestore.collection(ChatModel.FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .collection("messages")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete();
                    }
                    firestore.collection(ChatModel.FIELD_COLLECTION_NAME)
                            .document(chatRoomId)
                            .delete()
                            .addOnSuccessListener(aVoid -> onComplete.accept(true))
                            .addOnFailureListener(e -> onComplete.accept(false));
                })
                .addOnFailureListener(e -> onComplete.accept(false));
    }



    /**
     * Fetches the user data from Firestore by their phone number.
     *
     * @param firestore Firestore instance to use.
     * @param phone     The phone number of the user to fetch.
     * @param onSuccess Callback for successful fetch with the user object.
     * @param onFailure Callback for failure with the error message.
     */
    public void fetchUser(FirebaseFirestore firestore, String phone, Consumer<UserModel> onSuccess, Consumer<String> onFailure) {
        firestore.collection(FIELD_COLLECTION_NAME)
                .document(phone)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserModel user = documentSnapshot.toObject(UserModel.class);
                        onSuccess.accept(user);
                    } else {
                        onSuccess.accept(null); // User not found
                    }
                })
                .addOnFailureListener(e -> onFailure.accept(e.getMessage()));
    }

    /**
     * Updates the user's data in Firestore.
     *
     * @param firestore  Firestore instance to use.
     * @param onComplete Callback for success or failure.
     */
    public void updateUser(FirebaseFirestore firestore, Consumer<Boolean> onComplete) {
        if (phone == null || phone.isEmpty()) {
            onComplete.accept(false); // Fail if phone number is missing
            return;
        }

        HashMap<String, Object> updates = new HashMap<>();
        updates.put(FIELD_USERNAME, username);
        updates.put(FIELD_PROFILE_PIC, profilePic);

        firestore.collection(FIELD_COLLECTION_NAME)
                .document(phone)
                .update(updates)
                .addOnSuccessListener(aVoid -> onComplete.accept(true))
                .addOnFailureListener(e -> onComplete.accept(false));
    }

    /**
     * Logs out the user by clearing their FCM token in Firestore.
     *
     * @param firestore  Firestore instance to use.
     * @param onSuccess  Callback for successful logout.
     * @param onFailure  Callback for failure with the error message.
     */
    public void logoutUser(FirebaseFirestore firestore, Runnable onSuccess, Consumer<String> onFailure) {
        if (phone == null || phone.isEmpty()) {
            onFailure.accept("Phone number is missing.");
            return;
        }

        HashMap<String, Object> updates = new HashMap<>();
        updates.put(FIELD_FCM_TOKEN, null); // Clear the FCM token

        firestore.collection(FIELD_COLLECTION_NAME)
                .document(phone)
                .update(updates)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onFailure.accept(e.getMessage()));
    }
}