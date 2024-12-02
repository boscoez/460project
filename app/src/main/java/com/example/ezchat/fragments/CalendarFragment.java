package com.example.ezchat.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezchat.R;
import com.example.ezchat.utilities.Constants;
import com.example.ezchat.utilities.PreferenceManager;
import com.example.ezchat.utilities.Utilities;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Manages a user's tasks by date, including adding, editing, and deleting tasks.
 */
public class CalendarFragment extends Fragment {

    private FirebaseFirestore firestore;
    private PreferenceManager preferenceManager;
    private String currentUserPhone;
    private HashMap<String, List<String>> tasksByDate;
    private TaskAdapter taskAdapter;
    private String selectedDate;

    private View rootView;
    private CalendarView calendarView;
    private TextView taskHeader;
    private RecyclerView taskRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_calendar, container, false);

        // Initialize Firestore and PreferenceManager
        firestore = FirebaseFirestore.getInstance();
        preferenceManager = PreferenceManager.getInstance(requireContext());
        currentUserPhone = preferenceManager.get(Constants.PREF_KEY_PHONE, "");

        tasksByDate = new HashMap<>();
        selectedDate = getCurrentDate();

        // Bind views
        calendarView = rootView.findViewById(R.id.calendar_view);
        taskHeader = rootView.findViewById(R.id.task_header);
        taskRecyclerView = rootView.findViewById(R.id.task_recycler_view);
        ImageButton addTaskBtn = rootView.findViewById(R.id.add_task_btn);

        // Initialize RecyclerView
        taskAdapter = new TaskAdapter(new ArrayList<>());
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskRecyclerView.setAdapter(taskAdapter);

        // Set header text
        taskHeader.setText(String.format("Tasks for %s", selectedDate));

        // Load tasks for the current date
        loadTasksFromFirestore();

        // Set calendar date change listener
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
            taskHeader.setText(String.format("Tasks for %s", selectedDate));
            loadTasksFromFirestore();
        });

        // Set Add Task button listener
        addTaskBtn.setOnClickListener(v -> showAddTaskDialog());

        return rootView;
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private void loadTasksFromFirestore() {
        if (currentUserPhone.isEmpty()) {
            Utilities.showToast(requireContext(), "User not logged in.", Utilities.ToastType.ERROR);
            return;
        }

        firestore.collection(Constants.USER_COLLECTION)
                .document(currentUserPhone)
                .collection(Constants.TASKS_COLLECTION)
                .document(selectedDate)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> tasks = (List<String>) documentSnapshot.get(Constants.FIELD_TASKS);
                        tasksByDate.put(selectedDate, tasks != null ? tasks : new ArrayList<>());
                        taskAdapter.updateTasks(tasksByDate.get(selectedDate));
                    } else {
                        tasksByDate.put(selectedDate, new ArrayList<>());
                        taskAdapter.updateTasks(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> Log.e(Constants.LOG_TAG_CALENDAR, "Failed to load tasks: " + e.getMessage()));
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Task");

        final EditText taskInput = new EditText(requireContext());
        taskInput.setHint("Enter task details");
        builder.setView(taskInput);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String newTask = taskInput.getText().toString().trim();
            if (!newTask.isEmpty()) {
                tasksByDate.computeIfAbsent(selectedDate, k -> new ArrayList<>()).add(newTask);
                taskAdapter.updateTasks(tasksByDate.get(selectedDate));
                saveTasksToFirestore(selectedDate, tasksByDate.get(selectedDate));
            } else {
                Utilities.showToast(requireContext(), "Task details cannot be empty!", Utilities.ToastType.WARNING);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void saveTasksToFirestore(String date, List<String> tasks) {
        if (currentUserPhone.isEmpty()) {
            Utilities.showToast(requireContext(), "User not logged in.", Utilities.ToastType.ERROR);
            return;
        }

        if (tasks.isEmpty()) {
            firestore.collection(Constants.USER_COLLECTION)
                    .document(currentUserPhone)
                    .collection(Constants.TASKS_COLLECTION)
                    .document(date)
                    .delete()
                    .addOnSuccessListener(aVoid -> Log.d(Constants.LOG_TAG_CALENDAR, "Date removed from Firestore."))
                    .addOnFailureListener(e -> Log.e(Constants.LOG_TAG_CALENDAR, "Failed to remove date from Firestore: " + e.getMessage()));
        } else {
            firestore.collection(Constants.USER_COLLECTION)
                    .document(currentUserPhone)
                    .collection(Constants.TASKS_COLLECTION)
                    .document(date)
                    .set(new HashMap<String, Object>() {{
                        put(Constants.FIELD_TASKS, tasks);
                    }})
                    .addOnSuccessListener(aVoid -> Log.d(Constants.LOG_TAG_CALENDAR, "Task saved successfully!"))
                    .addOnFailureListener(e -> Log.e(Constants.LOG_TAG_CALENDAR, "Failed to save task: " + e.getMessage()));
        }
    }

    private void showEditTaskDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Task");

        final EditText taskInput = new EditText(requireContext());
        String existingTask = tasksByDate.get(selectedDate).get(position);
        taskInput.setText(existingTask);
        builder.setView(taskInput);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String updatedTask = taskInput.getText().toString().trim();
            if (!updatedTask.isEmpty()) {
                tasksByDate.get(selectedDate).set(position, updatedTask);
                taskAdapter.updateTasks(tasksByDate.get(selectedDate));
                saveTasksToFirestore(selectedDate, tasksByDate.get(selectedDate));
            } else {
                Utilities.showToast(requireContext(), "Task details cannot be empty!", Utilities.ToastType.WARNING);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void deleteTask(int position) {
        tasksByDate.get(selectedDate).remove(position);
        taskAdapter.updateTasks(tasksByDate.get(selectedDate));
        saveTasksToFirestore(selectedDate, tasksByDate.get(selectedDate));
        Utilities.showToast(requireContext(), "Task deleted successfully!", Utilities.ToastType.SUCCESS);
    }





    /**
     * Adapter for managing tasks in the RecyclerView.
     */
    private class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

        private final List<String> tasks;

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
            holder.taskText.setText(task);
            holder.editButton.setOnClickListener(v -> showEditTaskDialog(position));
            holder.deleteButton.setOnClickListener(v -> deleteTask(position));
        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }

        void updateTasks(List<String> newTasks) {
            tasks.clear();
            tasks.addAll(newTasks);
            notifyDataSetChanged();
        }

        class TaskViewHolder extends RecyclerView.ViewHolder {
            TextView taskText;
            ImageButton editButton, deleteButton;

            TaskViewHolder(@NonNull View itemView) {
                super(itemView);
                taskText = itemView.findViewById(R.id.task_text);
                editButton = itemView.findViewById(R.id.edit_button);
                deleteButton = itemView.findViewById(R.id.delete_button);
            }
        }
    }
}