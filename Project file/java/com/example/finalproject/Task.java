package com.example.finalproject;

import java.util.List;

public class Task {
    private String title;
    private String description; // Description of the task
    private String dueDate; // Due date of the task
    private String dueTime; // Due time of the task
    private String priority; // Priority level of the task
    private boolean isCompleted; // Completion status of the task
    private boolean reminderTime; // remind the user or nah
    private String notificationTimes;// List of notification for time
    private boolean snoozeDuration; // Duration in minutes for snooze //

    // Constructor that accepts all parameters
    public Task(String title, String description, String dueDate, String dueTime, String priority, boolean isCompleted, boolean reminderTime,String notificationTimes) {
        this.title = title;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.priority = priority;
        this.isCompleted = isCompleted;
        this.description = description;
        this.reminderTime = reminderTime;
        this.notificationTimes = notificationTimes;
    }

    public String getNotificationTimes() {
        return notificationTimes; }
    public boolean getSnoozeDuration() {
        return snoozeDuration; }

    // Getters and setters for all fields
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getDueTime() {
        return dueTime;
    }

    public void setDueTime(String dueTime) {
        this.dueTime = dueTime;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public Boolean getReminderTime() {
        return reminderTime;
    }





}
