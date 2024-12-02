//import package
package com.example.myapplication;

//import statements
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * TaskListActivity displays tasks for a specific date.
 * Includes functionality to mark tasks as completed, delete tasks, and undo deletions.
 */
public class TaskListActivity extends AppCompatActivity {

    private TextView dateTextView;
    private RecyclerView taskRecyclerView;

    private DatabaseReference tasksReference;
    private ArrayList<Task> taskList;
    private TaskAdapter taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        // Initialize Firebase
        tasksReference = FirebaseDatabase.getInstance().getReference("tasks");

        // Initialize Views
        dateTextView = findViewById(R.id.dateTextView);
        taskRecyclerView = findViewById(R.id.taskRecyclerView);

        // Get selected date from intent
        String selectedDate = getIntent().getStringExtra("selectedDate");
        dateTextView.setText("Tasks for: " + selectedDate);

        // Initialize RecyclerView
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList, findViewById(android.R.id.content));
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskRecyclerView.setAdapter(taskAdapter);

        // Fetch tasks for the selected date
        fetchTasksForDate(selectedDate);
    }

    /**
     * Fetch tasks from Firebase for the given date.
     *
     * @param date The selected date for which tasks are to be fetched.
     */
    private void fetchTasksForDate(String date) {
        tasksReference.orderByChild("date").equalTo(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList.clear();
                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);
                    if (task != null) {
                        task.setId(taskSnapshot.getKey());
                        taskList.add(task);
                    }
                }
                taskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database errors
            }
        });
    }
}

//public class Task {
//    public String id; // Unique Id for the task
//    public String title; // Title of the task
//    public String description; // description of the task
//    public String date; // Format: yyyy-MM-dd
//    public long notificationTime; // Time in milliseconds for notification
//
//    //This class is used to encapsulate the data for a single task, including attributes
//    //like title, description, date, and notification time.
//
//    // Default constructor for Firebase
//    public Task() {
//    }
//
//    // Parameterized constructor for convenience
//    public Task(String id, String title, String description, String date, long notificationTime)
//    {
//        this.id = id;
//        this.title = title;
//        this.description = description;
//        this.date = date;
//        this.notificationTime = notificationTime;
//    }
//
//
//    // Set getting and setters (useful for encapsulation)
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//    public String get title() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public String getDate() {
//        return date;
//    }
//
//    public void setDate(String date) {
//        this.date = date;
//    }
//
//    public long getNotificationTime() {
//        return notificationTime;
//    }
//
//    public void setNotificationTime(long notificationTime) {
//        this.notificationTime = notificationTime;
//    }
//}

