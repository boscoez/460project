package com.example.ezchat.models;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a user in the application, containing profile information,
 * contact details, metadata such as creation timestamp, FCM token, and the chat rooms
 * the user is part of.
 */
public class UserModel {

    // Firestore collection and field constants
    public static final String FIELD_COLLECTION_NAME = "users";
    public static final String FIELD_PHONE = "phone";
    public static final String FIELD_USERNAME = "username";
    public static final String FIELD_CREATED_TIMESTAMP = "createdTimestamp";
    public static final String FIELD_FCM_TOKEN = "fcmToken";
    public static final String FIELD_PROFILE_PIC = "profilePic";
    public static final String FIELD_CHAT_ROOMS = "chatRooms";

    // Public fields for direct Firestore access
    public String phone;
    public String username;
    public com.google.firebase.Timestamp createdTimestamp;
    public String fcmToken;
    public String profilePic;
    public List<String> chatRooms;

    private final FirebaseFirestore db; // Firestore instance

    /**
     * Default constructor for UserModel.
     * Required for Firebase Firestore to deserialize user data.
     */
    public UserModel() {
        this.db = FirebaseFirestore.getInstance();
        this.chatRooms = new ArrayList<>();
    }

    /**
     * Parameterized constructor to initialize the user model with essential fields.
     */
    public UserModel(String phone, String username, com.google.firebase.Timestamp createdTimestamp,
                     String fcmToken, String profilePic, List<String> chatRooms) {
        this();
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
     * Fetches the current user's chat rooms from Firestore using their phone number.
     *
     * @param currentPhone The phone number of the current user.
     * @param onComplete   Callback for success (with a list of chat room IDs).
     * @param onFailure    Callback for failure (with error message).
     */
    public void fetchChatRooms(String currentPhone, Consumer<List<String>> onComplete, Consumer<String> onFailure) {
        db.collection(FIELD_COLLECTION_NAME)
                .document(currentPhone)
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
     * Deletes a chat room if the current user is the creator.
     *
     * @param chatRoomId    The ID of the chat room to delete.
     * @param currentPhone  The phone number of the current user.
     * @param context       The context for showing Toast messages.
     * @param onComplete    Callback for success or failure.
     */
    public void deleteChatRoom(String chatRoomId, String currentPhone, Context context, Consumer<Boolean> onComplete) {
        db.collection(ChatroomModel.FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ChatroomModel chatRoom = documentSnapshot.toObject(ChatroomModel.class);
                        if (chatRoom != null) {
                            if (chatRoom.creatorPhone.equals(currentPhone)) {
                                deleteChatRoomAndMessages(chatRoomId, onComplete);
                            } else {
                                onComplete.accept(false);
                                Toast.makeText(context, "You are not the creator of this chat room.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            onComplete.accept(false);
                            Toast.makeText(context, "Chat room details could not be retrieved.", Toast.LENGTH_SHORT).show();
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
     * Deletes the chat room document and its associated messages.
     *
     * @param chatRoomId The ID of the chat room to delete.
     * @param onComplete Callback for success or failure.
     */
    private void deleteChatRoomAndMessages(String chatRoomId, Consumer<Boolean> onComplete) {
        db.collection(ChatroomModel.FIELD_COLLECTION_NAME)
                .document(chatRoomId)
                .collection(ChatroomModel.FIELD_COLLECTION_NAME)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete();
                    }
                    db.collection(ChatroomModel.FIELD_COLLECTION_NAME)
                            .document(chatRoomId)
                            .delete()
                            .addOnSuccessListener(aVoid -> onComplete.accept(true))
                            .addOnFailureListener(e -> onComplete.accept(false));
                })
                .addOnFailureListener(e -> onComplete.accept(false));
    }

    /**
     * Updates the user's chat room list in Firestore.
     *
     * @param currentPhone The phone number of the current user.
     * @param onComplete   Callback for success or failure.
     */
    public void updateChatRooms(String currentPhone, Consumer<Boolean> onComplete) {
        db.collection(FIELD_COLLECTION_NAME)
                .document(currentPhone)
                .update(FIELD_CHAT_ROOMS, this.chatRooms)
                .addOnSuccessListener(aVoid -> onComplete.accept(true))
                .addOnFailureListener(e -> onComplete.accept(false));
    }
}