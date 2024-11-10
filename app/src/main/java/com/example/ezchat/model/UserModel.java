package com.example.ezchat.model;

import com.google.firebase.Timestamp;

public class UserModel{
    private String phone;
    private String username;
    private Timestamp createdTimestamp;
    private String userId;
    // No-argument constructor
    public UserModel() {
    }
<<<<<<< HEAD
=======

    /**
     * Constructor to initialize a UserModel with specified phone number, username, creation timestamp, and user ID.
     * @param phone                 The phone number of the user.
     * @param username             The username of the user.
     * @param createdTimestamp      The timestamp indicating when the user account was created.
     * @param userId                The unique ID of the user.
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    public UserModel(String phone, String username, Timestamp createdTimestamp, String userId) {
        this.phone = phone;
        this.username = username;
        this.createdTimestamp = createdTimestamp;
        this.userId = userId;
    }
<<<<<<< HEAD

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }
    public String getUserId() {
        return userId;
    }
=======
    /**
     * Retrieves the user's phone number.
     *
     * @return The phone number as a String.
     */
    public String getPhone() {
        return phone;
    }
    /**
     * Sets the user's phone number.
     *
     * @param phone The phone number to set for the user.
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }
    /**
     * Retrieves the user's username.
     *
     * @return The username as a String.
     */
    public String getUsername() {
        return username;
    }
    /**
     * Sets the user's username.
     *
     * @param username The username to set for the user.
     */
    public void setUsername(String username) {
        this.username = username;
    }
    /**
     * Retrieves the timestamp when the user account was created.
     *
     * @return A Timestamp object representing the account creation time.
     */
    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }
    /**
     * Sets the creation timestamp for the user account.
     *
     * @param createdTimestamp The Timestamp object to set as the creation timestamp.
     */
    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }
    /**
     * Retrieves the user's unique ID.
     *
     * @return The user ID as a String.
     */
    public String getUserId() {
        return userId;
    }
    /**
     * Sets the user's unique ID.
     *
     * @param userId The user ID to set for the user.
     */
>>>>>>> 2fa863b40ad565a15776b66eac7d0625c1989002
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
