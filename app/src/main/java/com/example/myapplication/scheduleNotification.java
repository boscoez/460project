package com.example.myapplication;

import static android.content.Context.ALARM_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;

private void scheduleNotification(Task task) {
    // Create an Intent to trigger the BroadcastReceiver
    Intent intent = new Intent(this, NotificationReceiver.class);
    // Pass task title
    intent.putExtra("title", task.title);
    // Pass task description
    intent.putExtra("description", task.description);

    // Create a PendingIntent to be triggered by AlarmManager
    PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this,
            // Unique request code using task ID
            task.id.hashCode(),
            intent,

            PendingIntent.FLAG_UPDATE_CURRENT);

    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    alarmManager.setExact(AlarmManager.RTC_WAKEUP, task.notificationTime, pendingIntent);
}
