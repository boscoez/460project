package com.example.ezchat.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
public class FirebaseUtil {
    /**
     *  Retrieves the current user's ID.
     * @return  The unique ID of the currently logged-in user as a String, or null if the user is not logged in.
     */
    public static String currentUserId(){
        return FirebaseAuth.getInstance().getUid();
    }
    /**
     * Checks if a user is currently logged in.
     *
     * @return True if the user is logged in; false otherwise.
     */
    public static boolean isLoggedIn(){
        return currentUserId() != null;
    }
    /**
     * Retrieves a DocumentReference for the currently logged-in user's details in Firestore.
     *
     * @return A DocumentReference pointing to the current user's document in the "users" collection.
     */
    public static DocumentReference currentUserDetails(){
        return FirebaseFirestore.getInstance().collection("users").document(currentUserId());
    }
    /**
     * Retrieves a CollectionReference for all user documents in the "users" collection.
     *
     * @return A CollectionReference pointing to the "users" collection in Firestore.
     */
    public static CollectionReference allUserCollectionReference(){
        return FirebaseFirestore.getInstance().collection("users");
    }
    /**
     * Retrieves a DocumentReference for a specific chatroom based on its chatroom ID.
     *
     * @param chatroomId The unique ID of the chatroom.
     * @return A DocumentReference pointing to the specified chatroom document in the "chatrooms" collection.
     */
    public static DocumentReference getChatroomReference(String chatroomId){
        return FirebaseFirestore.getInstance().collection("chatrooms").document(chatroomId);
    }
    /**
     * Retrieves a CollectionReference for messages within a specific chatroom.
     *
     * @param chatroomId The unique ID of the chatroom.
     * @return A CollectionReference pointing to the "chats" sub-collection within the specified chatroom.
     */
    public static CollectionReference getChatroomMessageReference(String chatroomId){
        return getChatroomReference(chatroomId).collection("chats");
    }
    /**
     * Generates a unique chatroom ID based on two user IDs.
     * Ensures a consistent ordering by hashing and comparing the two IDs, so the chatroom ID is the same
     * regardless of the order in which the user IDs are provided.
     *
     * @param userId1 The first user's unique ID.
     * @param userId2 The second user's unique ID.
     * @return A unique chatroom ID as a String in the format "userId1_userId2" or "userId2_userId1" (ordered alphabetically).
     */
    public static  String getChatroomId(String userId1, String userId2) {
        if(userId1.hashCode() < userId2.hashCode()){
            return userId1+"_"+userId2;
        }else {
            return userId2+"_"+userId1;
        }
    }
}
