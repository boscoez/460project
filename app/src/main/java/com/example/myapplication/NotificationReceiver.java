package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "TASK_NOTIFICATION_CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskDescription = intent.getStringExtra("taskDescription");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Task Notifications", NotificationManager.IMPORTANCE_HIGH);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Task Reminder")
                .setContentText(taskDescription)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }
    }
}

//public class NotificationReceiver extends BroadcastReceiver {
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        // Retrieve task details from the intent
//        String title = intent.getStringExtra("title");
//        String description = intent.getStringExtra("description");
//
//        // Build the notification
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "todo_channel")
//                .setSmallIcon(R.drawable.ic_notification)
//                // Task title
//                .setContentTitle(title)
//                // Task description
//                .setContentText(description)
//                .setPriority(NotificationCompat.PRIORITY_HIGH);
//
//        // Show the notification
//        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
//        manager.notify((int) System.currentTimeMillis(), builder.build());
//    }
//}
