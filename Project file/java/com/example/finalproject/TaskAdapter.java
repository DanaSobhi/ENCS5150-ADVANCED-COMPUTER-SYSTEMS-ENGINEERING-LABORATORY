package com.example.finalproject;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.Fragment;

import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.OvershootInterpolator;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private Cursor cursor;
    private final Context context;
    private final TaskDatabaseHelper dbHelper;
    private final String currentFragmentType;

    private final FragmentManager fragmentManager;

    public TaskAdapter(Cursor cursor, Context context, String currentFragmentType, FragmentManager fragmentManager) {
        this.cursor = cursor;
        this.context = context;
        this.dbHelper = new TaskDatabaseHelper(context);
        this.currentFragmentType = currentFragmentType;
        this.fragmentManager = fragmentManager;
    }



    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        if (cursor != null && cursor.moveToPosition(position)) {
            // Get column indices
            int titleIndex = cursor.getColumnIndex("TITLE");
            int dueDateIndex = cursor.getColumnIndex("DEADLINE");
            int dueTimeIndex = cursor.getColumnIndex("DUE_TIME");
            int descriptionIndex = cursor.getColumnIndex("DESCRIPTION");
            int statusIndex = cursor.getColumnIndex("STATUS");
            int idIndex = cursor.getColumnIndex("ID");
            int priorityIndex = cursor.getColumnIndex("PRIORITY");
            int reminderIndex = cursor.getColumnIndex("REMINDER");

            int notificationIndex = cursor.getColumnIndex("NOTIFYING");


            if (titleIndex >= 0 && dueDateIndex >= 0 && descriptionIndex >= 0 &&
                    statusIndex >= 0 && idIndex >= 0 && priorityIndex >= 0) {

                // Retrieve data
                String title = cursor.getString(titleIndex);
                String dueDate = cursor.getString(dueDateIndex);
                String dueTime = cursor.getString(dueTimeIndex);
                String description = cursor.getString(descriptionIndex);
                String status = cursor.getString(statusIndex);
                String taskId = cursor.getString(idIndex);
                String priority = cursor.getString(priorityIndex);
                String reminderStatus = cursor.getString(reminderIndex);  // Retrieve reminder status

                String notificationDate = cursor.getString(notificationIndex);  // Retrieve notification date

                // Bind data to views
                holder.titleTextView.setText(title);
                holder.dueDateTextView.setText("Due: " + dueDate+" " +dueTime);
                holder.descriptionTextView.setText(description);
                holder.priorityTextView.setText(" " + priority);

                holder.checkBox.setOnCheckedChangeListener(null);
                holder.checkBox.setChecked("Completed".equals(status));

                // Checkbox listener
                holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    String newStatus = isChecked ? "Completed" : "Pending";
                    updateTaskStatus(taskId, newStatus);
                });

                // Delete button listener
                holder.deleteButton.setOnClickListener(v -> deleteTask(taskId));

                holder.editButton.setOnClickListener(v -> {
                    Log.d("TaskAdapter", "Editing Task ID: " + taskId);

                    // Check if taskId is valid
                    if (taskId == null || taskId.isEmpty()) {
                        Log.e("TaskAdapter", "Error: Task ID is null or empty!");
                        Toast.makeText(context, "Invalid Task ID", Toast.LENGTH_SHORT).show();
                        return; // Exit if task ID is invalid
                    }

                    // Create a Bundle with the task ID
                    Bundle bundle = new Bundle();
                    bundle.putString("task_id", taskId);

                    // Use NavController to navigate to the EditTaskFragment
                    NavController navController = Navigation.findNavController(v);  // Get the NavController
                    navController.navigate(R.id.editTaskFragment, bundle);  // Navigate using the navigation graph and pass the bundle
                });


                // Check if the reminder is set and schedule the notification if necessary
                if ("REMINDER".equals(reminderStatus) && notificationDate != null && !notificationDate.isEmpty()) {
                    // Schedule the reminder based on the notification date stored in the database
                    scheduleReminder(notificationDate, taskId, title, description);
                }

                // Share button listener
                holder.shareButton.setOnClickListener(v -> {
                    shareTaskViaEmail(title, description, dueDate, priority);
                });
            }
        }
    }

    private void shareTaskViaEmail(String title, String description, String dueDate, String priority) {
        String subject = "Task: " + title;
        String body = "Description: " + description + "\nDue Date: " + dueDate + "\nPriority: " + priority;

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);

        try {
            context.startActivity(Intent.createChooser(emailIntent, "Share Task via Email"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "No email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return (cursor != null) ? cursor.getCount() : 0;
    }

    // Update task status in the database
    private void updateTaskStatus(String taskId, String newStatus) {
        dbHelper.updateTaskStatus(taskId, newStatus);
        if ("today".equals(currentFragmentType)) {
            refreshTodayCursor();
        } else {
            refreshCursor();
        }
        checkAllTasksCompleted();
    }

    // Delete task from database
    private void deleteTask(String taskId) {
        dbHelper.deleteTask(taskId);
        if ("today".equals(currentFragmentType)) {
            refreshTodayCursor();
        } else {
            refreshCursor();
        }
    }

    // Refresh the cursor for all tasks
    private void refreshCursor() {
        Cursor newCursor = dbHelper.getAllTasks();
        swapCursor(newCursor);
    }

    // Refresh the cursor for today's tasks
    private void refreshTodayCursor() {
        Cursor newCursor = dbHelper.getTasksForDate(getCurrentDate());
        swapCursor(newCursor);
    }

    // Swap cursor and refresh RecyclerView
    public void swapCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close();
        }
        cursor = newCursor;
        new Handler(Looper.getMainLooper()).post(this::notifyDataSetChanged);
    }

    // Check if all tasks are completed
    private void checkAllTasksCompleted() {
        Cursor allTasksCursor = dbHelper.getAllTasks();
        boolean allCompleted = true;

        if (allTasksCursor != null) {
            while (allTasksCursor.moveToNext()) {
                int statusIndex = allTasksCursor.getColumnIndex("STATUS");
                if (statusIndex >= 0) {
                    String status = allTasksCursor.getString(statusIndex);
                    if (!"Completed".equals(status)) {
                        allCompleted = false;
                        break;
                    }
                }
            }
            allTasksCursor.close();
        }

        if (allCompleted) {
            showAnimatedDialog();
            Toast.makeText(context, "Bravo! You completed all tasks.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAnimatedDialog() {
        // Create a Dialog with the custom style
        Dialog dialog = new Dialog(context, R.style.TransparentDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_cute_picture);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // Find the ImageView in the dialog layout
        ImageView imageView = dialog.findViewById(R.id.cute_picture_view);

        // Set the drawable image
        imageView.setImageResource(R.drawable.goodjob);

        // Animate the ImageView (scale and fade-in)
        imageView.setScaleX(0f);
        imageView.setScaleY(0f);
        imageView.setAlpha(0f);
        imageView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator())
                .withEndAction(() -> {
                    // Automatically dismiss the dialog after the animation ends
                    imageView.postDelayed(dialog::dismiss, 1500);
                })
                .start();

        // Show the Dialog
        dialog.show();
    }

    // Get today's date in YYYY-MM-DD format
    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        return String.format("%d-%d-%d", year, month, day);
    }

    // ViewHolder Class
    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, dueDateTextView, descriptionTextView, priorityTextView;
        CheckBox checkBox;
        ImageView deleteButton, editButton, shareButton;

        public TaskViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.task_title);
            dueDateTextView = itemView.findViewById(R.id.task_due_date);
            descriptionTextView = itemView.findViewById(R.id.task_description);
            priorityTextView = itemView.findViewById(R.id.task_priority);
            checkBox = itemView.findViewById(R.id.task_checkbox);
            deleteButton = itemView.findViewById(R.id.delete_button);
            editButton = itemView.findViewById(R.id.edit_button);
            shareButton = itemView.findViewById(R.id.share_button);
        }
    }
    @SuppressLint("ScheduleExactAlarm")
    private void scheduleReminder(String notificationDateTime, String taskId, String title, String description) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        try {
            Date date = dateFormat.parse(notificationDateTime);
            calendar.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        // Set up the intent and receiver for notification
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("task_title", title);
        intent.putExtra("task_description", description);
        intent.putExtra("task_id", taskId);

        // Create a PendingIntent to trigger the broadcast
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(taskId), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get the AlarmManager system service
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Schedule the alarm to trigger at the specified time
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

}
