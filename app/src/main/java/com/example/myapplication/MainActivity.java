//import package
package com.example.myapplication;

//import statements
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;




    public class MainActivity extends AppCompatActivity {

        private CalendarView calendarView;
        private Button addTaskButton;
        private TextView textView;
        private String selectedDate;

        private FirebaseDatabase firebaseDatabase;
        private DatabaseReference tasksReference;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            // Initialize Firebase
            firebaseDatabase = FirebaseDatabase.getInstance();
            tasksReference = firebaseDatabase.getReference("tasks");


            // Initialize Views
            calendarView = findViewById(R.id.calendarView);
            addTaskButton = findViewById(R.id.addTaskButton);
            textView = findViewById(R.id.selectedDateText);

            // Handle Calendar Date Selection
            calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                textView.setText("Selected Date: " + selectedDate);
            });

            // Add Task Button Click
            addTaskButton.setOnClickListener(v -> showAddTaskDialog());
        }

        private void saveTaskToFirebase(String taskDescription) {
            String taskId = tasksReference.push().getKey();
            HashMap<String, String> taskData = new HashMap<>();
            taskData.put("date", selectedDate);
            taskData.put("description", taskDescription);

            tasksReference.child(taskId).setValue(taskData)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Task added successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to add task.", Toast.LENGTH_SHORT).show());
        }

        private void showAddTaskDialog() {
            if (selectedDate == null) {
                Toast.makeText(this, "Please select a date first!", Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add Task");

            final EditText input = new EditText(this);
            builder.setView(input);

            //add data into firebase console

            builder.setPositiveButton("Add", (dialog, which) -> {
                String taskDescription = input.getText().toString().trim();
                if (!taskDescription.isEmpty()) {
                    saveTaskToFirebase(taskDescription);
                    scheduleNotification(this, taskDescription, selectedDate);
                } else {
                    Toast.makeText(this, "Task cannot be empty!", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        }

        private void scheduleNotification(Context context, String taskDescription, String date) {
            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.putExtra("taskDescription", taskDescription);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Calendar calendar = Calendar.getInstance();
            // Notify at 9 AM
            calendar.set(Calendar.HOUR_OF_DAY, 9);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            NotificationScheduler.schedule(context, pendingIntent, calendar.getTimeInMillis());
        }

    }
