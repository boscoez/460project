package com.example.ezchat.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezchat.R;
import com.example.ezchat.databinding.FragmentCalendarBinding;
import com.example.ezchat.utils.FirebaseUtil;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private FirebaseFirestore firestore;
    private HashMap<String, List<String>> tasksByDate;
    private TaskAdapter taskAdapter;
    private String selectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize task storage
        tasksByDate = new HashMap<>();
        selectedDate = getCurrentDate();

        // Set up RecyclerView
        taskAdapter = new TaskAdapter(new ArrayList<>());
        binding.taskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.taskRecyclerView.setAdapter(taskAdapter);

        // Set default header
        binding.taskHeader.setText("Tasks for " + selectedDate);

        // Load tasks
        loadTasksFromFirestore();

        // Handle calendar date change
        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
            binding.taskHeader.setText("Tasks for " + selectedDate);
            loadTasksFromFirestore();
        });

        // Add Task button listener
        binding.addTaskBtn.setOnClickListener(v -> showAddTaskDialog());

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private void loadTasksFromFirestore() {
        firestore.collection("users")
                .document(FirebaseUtil.currentUserId())
                .collection("tasks")
                .document(selectedDate)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> tasks = (List<String>) documentSnapshot.get("tasks");
                        tasksByDate.put(selectedDate, tasks != null ? tasks : new ArrayList<>());
                        taskAdapter.updateTasks(tasksByDate.get(selectedDate));
                        Toast.makeText(requireContext(), "Tasks loaded successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        tasksByDate.put(selectedDate, new ArrayList<>());
                        taskAdapter.updateTasks(new ArrayList<>());
                        Toast.makeText(requireContext(), "No tasks for the selected date.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load tasks: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Task");

        // Create a custom input field
        final EditText taskInput = new EditText(requireContext());
        taskInput.setHint("Enter task details");
        builder.setView(taskInput);

        // Handle Add button
        builder.setPositiveButton("Add", (dialog, which) -> {
            String newTask = taskInput.getText().toString().trim();
            if (!newTask.isEmpty()) {
                List<String> tasks = tasksByDate.getOrDefault(selectedDate, new ArrayList<>());
                tasks.add(newTask);
                tasksByDate.put(selectedDate, tasks);
                taskAdapter.updateTasks(tasks);
                saveTasksToFirestore(selectedDate, tasks);
            } else {
                Toast.makeText(requireContext(), "Task details cannot be empty!", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Cancel button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void saveTasksToFirestore(String date, List<String> tasks) {
        firestore.collection("users")
                .document(FirebaseUtil.currentUserId())
                .collection("tasks")
                .document(date)
                .set(new HashMap<String, Object>() {{
                    put("tasks", tasks);
                }})
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Task saved successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to save task: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showEditTaskDialog(int taskPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Task");

        // Pre-fill the task input with the current task
        final EditText taskInput = new EditText(requireContext());
        taskInput.setText(tasksByDate.get(selectedDate).get(taskPosition));
        builder.setView(taskInput);

        // Handle Save button
        builder.setPositiveButton("Save", (dialog, which) -> {
            String updatedTask = taskInput.getText().toString().trim();
            if (!updatedTask.isEmpty()) {
                List<String> tasks = tasksByDate.get(selectedDate);
                tasks.set(taskPosition, updatedTask);
                tasksByDate.put(selectedDate, tasks);
                taskAdapter.updateTasks(tasks);
                saveTasksToFirestore(selectedDate, tasks);
                Toast.makeText(requireContext(), "Task updated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Task details cannot be empty!", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Cancel button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }



    /**
     * TaskAdapter for managing and displaying task items in the RecyclerView.
     */
    private class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

        private List<String> tasks;

        TaskAdapter(List<String> tasks) {
            this.tasks = tasks;
        }

        @NonNull
        @Override
        public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
            return new TaskViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
            String task = tasks.get(position);
            holder.taskTextView.setText(task);

            // Edit button
            holder.editButton.setOnClickListener(v -> showEditTaskDialog(position));

            // Delete button
            holder.deleteButton.setOnClickListener(v -> {
                tasks.remove(position);
                notifyItemRemoved(position);
                saveTasksToFirestore(selectedDate, tasks);
                Toast.makeText(requireContext(), "Task deleted successfully!", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }

        void updateTasks(List<String> tasks) {
            this.tasks = tasks;
            notifyDataSetChanged();
        }

        /**
         * ViewHolder class for managing individual task views.
         */
        class TaskViewHolder extends RecyclerView.ViewHolder {
            TextView taskTextView;
            View editButton;
            View deleteButton;

            TaskViewHolder(@NonNull View itemView) {
                super(itemView);
                taskTextView = itemView.findViewById(R.id.task_text);
                editButton = itemView.findViewById(R.id.edit_button);
                deleteButton = itemView.findViewById(R.id.delete_button);
            }
        }
    }
}
