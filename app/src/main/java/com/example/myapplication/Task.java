package com.example.myapplication;


/**
         Task model class for representing task data.
 **/
public class Task {
    public String id;
    public String description;
    public boolean completed;

    // Default constructor for Firebase
    public Task() {
    }

    // Constructor with parameters
    public Task(String id, String description, boolean completed) {
        this.id = id;
        this.description = description;
        this.completed = completed;
    }

    // Getter for ID
    public String getId() {
        return id;
    }

    // Setter for ID
    public void setId(String id) {
        this.id = id;
    }

    // Getter for description
    public String getDescription() {
        return description;
    }

    // Setter for description
    public void setDescription(String description) {
        this.description = description;
    }

    // Getter for completed status
    public boolean isCompleted() {
        return completed;
    }

    // Setter for completed status
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}

