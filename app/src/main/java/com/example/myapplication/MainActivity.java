//import package
package com.example.myapplication;

//import statements
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;


//create MainActivity class that extends AppCompactActivity
public class MainActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference tasksRef;
    private RecyclerView recyclerViewTasks;
    private TaskAdapter taskAdapter;

    // Define the variable of CalendarView type
    CalendarView calendarView;

    // TextView type
    TextView date_view;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance();
        tasksRef = database.getReference("tasks");

        // Initialize Views
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        calendarView = findViewById(R.id.calendarView);
        taskAdapter = new TaskAdapter();
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTasks.setAdapter(taskAdapter);

        // By ID we can use each component which id is assign in xml file use findViewById() to get the CalendarView and TextView
        calendarView = (CalendarView)
                findViewById(R.id.calendarView);
        date_view = (TextView)
                findViewById(R.id.date_view);

        // Add Listener in calendar
        calendarView.setOnDateChangeListener(
                        new CalendarView
                                .OnDateChangeListener() {
                            @Override

                            // In this Listener have one method
                            // and in this method we will
                            // get the value of DAYS, MONTH, YEARS
                            public void onSelectedDayChange(
                                    @NonNull CalendarView view,
                                    int year,
                                    int month,
                                    int dayOfMonth)
                            {

                                // Store the value of date with
                                // format in String type Variable
                                // Add 1 in month because month
                                // index is start with 0
                                String Date
                                        = dayOfMonth + "-"
                                        + (month + 1) + "-" + year;

                                // set this date in TextView for Display
                                date_view.setText(Date);
                            }
                        });

//        initialize the database
//        TaskDatabase db = Room.databaseBuilder(getApplicationContext(),
//                TaskDatabase.class, "task_database").build()

        //create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "todo_channel", "To-Do Notifications", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

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

    //Load tasks for the selected date
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

    //create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel channel = new NotificationChannel(
                "todo_channel", "To-Do Notifications", NotificationManager.IMPORTANCE_HIGH);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }
}








