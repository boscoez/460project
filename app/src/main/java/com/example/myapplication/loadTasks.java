package com.example.myapplication;


import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

private void loadTasks(String selectedDate) {
    taskRef.orderByChild("date").equalTo(selectedDate).addValueEventListener(new ValueEventLister())
    {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot){
            List<Task> tasks = new ArrayList<>();
            for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                Task task = taskSnapshot.getValue(Task.class);
                tasks.add(task);
            }
            taskAdapter.setTasks(tasks);
    }

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            loadTasks(selectedDate);
        });
    });
}
