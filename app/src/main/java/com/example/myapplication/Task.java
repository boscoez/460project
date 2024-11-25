package com.example.myapplication;

public class Task {
    public String id;
    public String title;
    public String description;
    public String date; // Format: yyyy-MM-dd
    public long notificationTime; // Time in milliseconds for notification

    public Task() {
        // Default constructor for Firebase
    }

    public Task(String id, String title, String description, String date, long notificationTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.notificationTime = notificationTime;
    }
}
