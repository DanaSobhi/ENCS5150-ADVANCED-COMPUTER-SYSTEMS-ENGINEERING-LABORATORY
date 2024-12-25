package com.example.finalproject;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "task_reminder_channel";
    private static final String CHANNEL_NAME = "Task Reminders";
    private static final String TASK_REMINDER = "REMINDER";
    private static final String NOTIFICATION_TIME = "NOTIFYING";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get task data from the intent
        String taskTitle = intent.getStringExtra("task_title");
        String taskDescription = intent.getStringExtra("task_description");
        String taskId = intent.getStringExtra("task_id");
        String notificationTime = intent.getStringExtra(NOTIFICATION_TIME); // Time string in "yyyy-MM-dd HH:mm"

        // Check the action
        if ("SNOOZE_ACTION".equals(intent.getAction())) {
            snoozeNotification(context, taskId, taskTitle, taskDescription, notificationTime);
            return;
        } else if ("REMOVE_ACTION".equals(intent.getAction())) {
            removeReminder(context, taskId);
            return;
        }



        // Regular notification handling
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Snooze action
        Intent snoozeIntent = new Intent(context, NotificationReceiver.class);
        snoozeIntent.setAction("SNOOZE_ACTION");
        snoozeIntent.putExtra("task_id", taskId);
        snoozeIntent.putExtra("task_title", taskTitle);
        snoozeIntent.putExtra("task_description", taskDescription);
        snoozeIntent.putExtra(NOTIFICATION_TIME, notificationTime);

        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                Integer.parseInt(taskId),
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Remove action
        Intent removeIntent = new Intent(context, NotificationReceiver.class);
        removeIntent.setAction("REMOVE_ACTION");
        removeIntent.putExtra("task_id", taskId);


        PendingIntent removePendingIntent = PendingIntent.getBroadcast(
                context,
                Integer.parseInt(taskId),
                removeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Create the notification with Snooze and Remove actions
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(taskTitle)
                .setContentText(taskDescription)
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(R.drawable.ic_notification, "Snooze", snoozePendingIntent)
                .addAction(R.drawable.ic_notification, "Remove", removePendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(Integer.parseInt(taskId), builder.build());

    }

    /**
     * Snooze the notification for 10 minutes.
     */
    @SuppressLint("ScheduleExactAlarm")
    private void snoozeNotification(Context context, String taskId, String title, String description, String notificationTime) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long currentTimeMillis = System.currentTimeMillis();
        long snoozeTimeMillis = currentTimeMillis + 10 * 60 * 1000;

        String newNotificationTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(new Date(snoozeTimeMillis));

        TaskDatabaseHelper dbHelper = new TaskDatabaseHelper(context);
        dbHelper.updateNotificationTime(taskId, newNotificationTime);

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("task_id", taskId);
        intent.putExtra("task_title", title);
        intent.putExtra("task_description", description);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                Integer.parseInt(taskId),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );



        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, snoozeTimeMillis, pendingIntent);
        }

        Toast.makeText(context, "Snoozed for 10 minutes", Toast.LENGTH_SHORT).show();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(Integer.parseInt(taskId));
        }


    }


    private void removeReminder(Context context, String taskId) {
        TaskDatabaseHelper dbHelper = new TaskDatabaseHelper(context);
        dbHelper.updateTaskReminder(taskId, "NO");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(Integer.parseInt(taskId));
        }

        Toast.makeText(context, "Reminder removed", Toast.LENGTH_SHORT).show();
    }


}