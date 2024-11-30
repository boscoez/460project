package com.example.ezchat.utilities;

import com.example.ezchat.models.ChatModel;
import com.example.ezchat.models.UserModel;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.List;

public class FirebaseUtil {

    public static String currentUserId(){
        return FirebaseAuth.getInstance().getUid();
    }

    public static boolean isLoggedIn(){
        if(currentUserId()!=null){
            return true;
        }
        return false;
    }

    public static DocumentReference currentUserDetails(){
        return FirebaseFirestore.getInstance().collection(UserModel.FIELD_COLLECTION_NAME).document(currentUserId());
    }

    public static CollectionReference allUserCollectionReference(){
        return FirebaseFirestore.getInstance().collection(UserModel.FIELD_COLLECTION_NAME);
    }

    public static DocumentReference getChatroomReference(String chatroomId){
        return FirebaseFirestore.getInstance().collection(ChatModel.FIELD_COLLECTION_NAME).document(chatroomId);
    }

    public static CollectionReference getChatroomMessageReference(String chatroomId){
        return getChatroomReference(chatroomId).collection(ChatModel.FIELD_COLLECTION_NAME);
    }

    public static String getChatroomId(String userId1,String userId2){
        if(userId1.hashCode()<userId2.hashCode()){
            return userId1+"_"+userId2;
        }else{
            return userId2+"_"+userId1;
        }
    }

    public static CollectionReference allChatroomCollectionReference(){
        return FirebaseFirestore.getInstance().collection(ChatModel.FIELD_COLLECTION_NAME);
    }

    public static DocumentReference getOtherUserFromChatroom(List<String> userIds){
        if(userIds.get(0).equals(FirebaseUtil.currentUserId())){
            return allUserCollectionReference().document(userIds.get(1));
        }else{
            return allUserCollectionReference().document(userIds.get(0));
        }
    }

    public static String timestampToString(Timestamp timestamp){
        return new SimpleDateFormat("HH:MM").format(timestamp.toDate());
    }

    public static void logout(){
        FirebaseAuth.getInstance().signOut();
    }

    public static StorageReference  getCurrentProfilePicStorageRef(){
        return FirebaseStorage.getInstance().getReference().child(UserModel.FIELD_PROFILE_PIC)
                .child(FirebaseUtil.currentUserId());
    }

    public static StorageReference  getOtherProfilePicStorageRef(String otherUserId){
        return FirebaseStorage.getInstance().getReference().child(UserModel.FIELD_PROFILE_PIC)
                .child(otherUserId);
    }

    /**
     * Gets a reference to the Firestore user document for the given phone number.
     *
     * @param phone The phone number of the user.
     * @return A DocumentReference to the user document.
     */
    public static DocumentReference getUserDetailsByPhone(String phone) {
        return FirebaseFirestore.getInstance().collection(UserModel.FIELD_COLLECTION_NAME).document(phone);
    }

    /**
     * Gets a reference to the current user's Firestore document.
     * This assumes the phone number is stored in preferences.
     *
     * @param phone The phone number of the current user.
     * @return A DocumentReference for the current user.
     */
    public static DocumentReference currentUserDetails(String phone) {
        return getUserDetailsByPhone(phone);
    }

}