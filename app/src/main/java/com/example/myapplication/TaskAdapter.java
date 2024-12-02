
//import package
package com.example.myapplication;

//import statements
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * TaskAdapter is a RecyclerView adapter for displaying tasks.
 * Includes functionality to mark tasks as completed, delete tasks, and undo deletions.
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {


    private final ArrayList<Task> taskList;

    private final View parentView; // Reference to the parent view for Snackbar

    /**
     * Constructor to initialize the task list and parent view.
     *
     * @param taskList  List of tasks to be displayed.
     * @param parentView Parent view for showing the Snackbar.
     */
    public TaskAdapter(ArrayList<Task> taskList, View parentView) {
        this.taskList = taskList;
        this.parentView = parentView;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.taskTextView.setText(task.getDescription());

        // Update UI for completed tasks
        if (task.isCompleted()) {
            holder.taskTextView.setPaintFlags(holder.taskTextView.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            holder.markDoneButton.setEnabled(false);
        } else {
            holder.taskTextView.setPaintFlags(holder.taskTextView.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            holder.markDoneButton.setEnabled(true);
        }

        // Mark as Done Button Click Listener
        holder.markDoneButton.setOnClickListener(v -> markTaskAsDone(task));

        // Delete Button Click Listener with Undo Support
        holder.deleteButton.setOnClickListener(v -> deleteTaskWithUndo(task, position));
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    /**
     * Mark a task as completed and update it in Firebase.
     *
     * @param task The task to be marked as completed.
     */
    private void markTaskAsDone(Task task) {
        DatabaseReference taskRef = FirebaseDatabase.getInstance()
                .getReference("tasks")
                .child(task.getId());

        taskRef.child("completed").setValue(true)
                .addOnSuccessListener(aVoid -> {
                    task.setCompleted(true);
                    notifyDataSetChanged();
                });
    }

    /**
     * Delete a task from Firebase and the RecyclerView, with Undo functionality.
     *
     * @param task     The task to be deleted.
     * @param position The position of the task in the RecyclerView.
     */
    private void deleteTaskWithUndo(Task task, int position) {
        // Remove task from list and update UI
        taskList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, taskList.size());

        // Reference to Firebase
        DatabaseReference taskRef = FirebaseDatabase.getInstance()
                .getReference("tasks")
                .child(task.getId());

        // Temporarily delete from Firebase
        taskRef.removeValue();

        // Show Snackbar for Undo
        Snackbar.make(parentView, "Task deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> {
                    // Re-add task to Firebase and RecyclerView
                    taskList.add(position, task);
                    notifyItemInserted(position);
                    notifyItemRangeChanged(position, taskList.size());

                    taskRef.setValue(task);
                }).show();
    }

    /**
     * ViewHolder for individual task items.
     */
    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView taskTextView;
        Button markDoneButton;
        Button deleteButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTextView = itemView.findViewById(R.id.taskTextView);
            markDoneButton = itemView.findViewById(R.id.markDoneButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}

//public class TaskAdapter extends RecyclerView.Adapter <TaskAdapter.TaskViewHolder> {
//    private List<Task> tasks = new ArrayList<>(); // Holds the task data
//
//    // Method to set tasks and notify the adapter of changes
//    public void setTasks(List<Task> tasks) {
//        this.tasks = tasks;
//        notifyDataSetChanged();
//    }
//
//    @NonNull
//    @Override
//    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        // Inflate the item layout for a task
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_main, parent, false);
//        return new TaskViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
//        // Bind the task data to the view
//        Task task = tasks.get(position);
//        holder.titleTextView.setText(task.title);
//        holder.descriptionTextView.setText(task.description);
//    }
//
//    @Override
//    public int getItemCount() {
//        return tasks.size(); // Return the number of tasks
//    }
//
//    // ViewHolder class to hold item views
//    static class TaskViewHolder extends RecyclerView.ViewHolder {
//
//        //initialize titleTextView and descriptionTextView
//        TextView titleTextView, descriptionTextView;
//
//        //create constructor for TaskViewHolder
//        public TaskViewHolder(@NonNull View itemView) {
//            super(itemView);
//            titleTextView = itemView.findViewById(com.hbb20.R.id.textView_title);
//            descriptionTextView = itemView.findViewById(R.id.recyclerViewTasks);
//        }
//    }
//}
