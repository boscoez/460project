package com.example.myapplication;

import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.LinearLayout;

private void showAddTaskDialog(String selectedDate) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Add Task");

    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);

    EditText titleInput = new EditText(this);
    titleInput.setHint("Task Title");
    layout.addView(titleInput);

    EditText descInput = new EditText(this);
    descInput.setHint("Task Description");
    layout.addView(descInput);

    builder.setView(layout);

    builder.setPositiveButton("Save", (dialog, which) -> {
        String title = titleInput.getText().toString();
        String description = descInput.getText().toString();
        long notificationTime = System.currentTimeMillis() + 1800000; // Example: 30 minutes from now

        String taskId = tasksRef.push().getKey();
        Task newTask = new Task(taskId, title, description, selectedDate, notificationTime);

        tasksRef.child(taskId).setValue(newTask).addOnSuccessListener(unused -> {
            scheduleNotification(newTask);
        });
    });

    builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

    builder.show();
}