package com.example.ezchat.fragments;

import android.app.AlertDialog;
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
import com.example.ezchat.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * A fragment for managing a user's tasks by date. Tasks can be added, edited, and deleted, with data stored in Firestore.
 */
public class CalendarFragment extends Fragment {
    private static final String FIELD_COLLECTION_TASKS = "tasks";

    private FragmentCalendarBinding binding; // View Binding
    private FirebaseFirestore firestore;
    private String currentUserPhone; // Stores the current user's phone number
    private HashMap<String, List<String>> tasksByDate; // Stores tasks mapped to their dates
    private TaskAdapter taskAdapter; // Adapter for RecyclerView
    private String selectedDate; // Tracks the currently selected date

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);

        // Initialize Firestore and get the current user's phone number
        firestore = FirebaseFirestore.getInstance();
        currentUserPhone = getCurrentUserPhone();

        // Initialize task storage
        tasksByDate = new HashMap<>();
        selectedDate = getCurrentDate();

        // Set up RecyclerView
        taskAdapter = new TaskAdapter(new ArrayList<>());
        binding.taskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.taskRecyclerView.setAdapter(taskAdapter);

        // Set initial header text
        binding.taskHeader.setText(String.format("Tasks for %s", selectedDate));

        // Load tasks for the current date
        loadTasksFromFirestore();

        // Set calendar date change listener
        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
            binding.taskHeader.setText(String.format("Tasks for %s", selectedDate));
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

    /**
     * Fetches the current user's phone number.
     *
     * @return The current user's phone number or null if not logged in.
     */
    private String getCurrentUserPhone() {
        return FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()
                : null;
    }

    /**
     * Gets the current date in "yyyy-MM-dd" format.
     *
     * @return The current date as a string.
     */
    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    /**
     * Loads tasks from Firestore for the selected date.
     */
    private void loadTasksFromFirestore() {
        if (currentUserPhone == null) {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection(UserModel.FIELD_COLLECTION_NAME)
                .document(currentUserPhone)
                .collection(FIELD_COLLECTION_TASKS)
                .document(selectedDate)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> tasks = (List<String>) documentSnapshot.get("tasks");
                        tasksByDate.put(selectedDate, tasks != null ? tasks : new ArrayList<>());
                        taskAdapter.updateTasks(tasksByDate.get(selectedDate));
                    } else {
                        tasksByDate.put(selectedDate, new ArrayList<>());
                        taskAdapter.updateTasks(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to load tasks: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /**
     * Displays a dialog to add a new task.
     */
    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Task");

        final EditText taskInput = new EditText(requireContext());
        taskInput.setHint("Enter task details");
        builder.setView(taskInput);

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

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    /**
     * Saves tasks to Firestore for the specified date.
     *
     * @param date  The date for which the tasks should be saved or deleted.
     * @param tasks The list of tasks to save.
     */
    private void saveTasksToFirestore(String date, List<String> tasks) {
        if (currentUserPhone == null) {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tasks.isEmpty()) {
            firestore.collection(UserModel.FIELD_COLLECTION_NAME)
                    .document(currentUserPhone)
                    .collection(FIELD_COLLECTION_TASKS)
                    .document(date)
                    .delete()
                    .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "No tasks left for this date. Date removed from Firestore.", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to remove date from Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show());
        } else {
            firestore.collection(UserModel.FIELD_COLLECTION_NAME)
                    .document(currentUserPhone)
                    .collection(FIELD_COLLECTION_TASKS)
                    .document(date)
                    .set(new HashMap<String, Object>() {{
                        put(FIELD_COLLECTION_TASKS, tasks);
                    }})
                    .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "Task saved successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to save task: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    /**
     * Adapter for displaying tasks in a RecyclerView.
     */
    private class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

        private List<String> tasks;

        TaskAdapter(List<String> tasks) {
            this.tasks = tasks;
        }

        @NonNull
        @Override
        public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_calendar_recycler_item, parent, false);
            return new TaskViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
            String task = tasks.get(position);
            holder.taskTextView.setText(task);

            holder.editButton.setOnClickListener(v -> showEditTaskDialog(position));
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

        class TaskViewHolder extends RecyclerView.ViewHolder {
            TextView taskTextView;
            View editButton, deleteButton;

            TaskViewHolder(@NonNull View itemView) {
                super(itemView);
                taskTextView = itemView.findViewById(R.id.task_text);
                editButton = itemView.findViewById(R.id.edit_button);
                deleteButton = itemView.findViewById(R.id.delete_button);
            }
        }

        private void showEditTaskDialog(int taskPosition) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Edit Task");

            final EditText taskInput = new EditText(requireContext());
            String existingTask = tasks.get(taskPosition);
            taskInput.setText(existingTask);
            builder.setView(taskInput);

            builder.setPositiveButton("Save", (dialog, which) -> {
                String updatedTask = taskInput.getText().toString().trim();
                if (!updatedTask.isEmpty()) {
                    tasks.set(taskPosition, updatedTask);
                    notifyItemChanged(taskPosition);
                    saveTasksToFirestore(selectedDate, tasks);
                } else {
                    Toast.makeText(requireContext(), "Task details cannot be empty!", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            builder.show();
        }
    }
}