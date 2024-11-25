package com.example.ezchat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezchat.databinding.FragmentCalendarBinding;
import com.example.ezchat.utils.FirebaseUtil;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * CalendarFragment displays a calendar and allows users to manage tasks for specific dates.
 * Tasks are saved in Firebase Firestore for persistence.
 */
public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;

    // Firestore instance
    private FirebaseFirestore firestore;

    // Data structure to store tasks by date
    private HashMap<String, List<String>> tasksByDate;
    private TaskAdapter taskAdapter;

    private String selectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Initialize View Binding
        binding = FragmentCalendarBinding.inflate(inflater, container, false);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize task storage
        tasksByDate = new HashMap<>();
        selectedDate = getCurrentDate(); // Default to today's date

        // Set up RecyclerView
        taskAdapter = new TaskAdapter(new ArrayList<>());
        binding.taskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.taskRecyclerView.setAdapter(taskAdapter);

        // Set default header to today's date
        binding.taskHeader.setText("Tasks for " + selectedDate);

        // Load tasks for today's date
        loadTasksFromFirestore();

        // Calendar selection listener
        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
            binding.taskHeader.setText("Tasks for " + selectedDate);
            loadTasksFromFirestore();
        });

        // Add Task button listener
        binding.addTaskBtn.setOnClickListener(v -> addTaskForSelectedDate());

        // Back to Profile button listener
        binding.backToProfileBtn.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Avoid memory leaks
    }

    /**
     * Returns the current date as a string in the format yyyy-MM-dd.
     */
    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    /**
     * Loads tasks for the selected date from Firestore and updates the RecyclerView.
     */
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
                    } else {
                        tasksByDate.put(selectedDate, new ArrayList<>());
                        taskAdapter.updateTasks(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }

    /**
     * Adds a new task for the selected date, saves it to Firestore, and updates the task list.
     */
    private void addTaskForSelectedDate() {
        String newTask = "New Task on " + selectedDate;
        List<String> tasks = tasksByDate.getOrDefault(selectedDate, new ArrayList<>());
        tasks.add(newTask);

        // Update local data structure
        tasksByDate.put(selectedDate, tasks);
        taskAdapter.updateTasks(tasks);

        // Save to Firestore
        saveTasksToFirestore(selectedDate, tasks);
    }

    /**
     * Saves the tasks for the given date to Firestore.
     *
     * @param date  The date for which the tasks are being saved.
     * @param tasks The list of tasks to save.
     */
    private void saveTasksToFirestore(String date, List<String> tasks) {
        firestore.collection("users")
                .document(FirebaseUtil.currentUserId())
                .collection("tasks")
                .document(date)
                .set(new HashMap<String, Object>() {{
                    put("tasks", tasks);
                }})
                .addOnSuccessListener(aVoid -> {
                    // Successfully saved tasks
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
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

            TaskViewHolder(@NonNull View itemView) {
                super(itemView);
                taskTextView = itemView.findViewById(R.id.task_text);
            }
        }
    }
}
