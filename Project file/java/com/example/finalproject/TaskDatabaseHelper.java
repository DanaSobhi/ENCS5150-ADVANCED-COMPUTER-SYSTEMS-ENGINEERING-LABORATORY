package com.example.finalproject;

import static android.app.DownloadManager.COLUMN_ID;
import static android.app.DownloadManager.COLUMN_STATUS;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.finalproject.SharedPrefManager;

public class TaskDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "task_management_db";
    private static final int DATABASE_VERSION = 1;
    private static final String TASKS_TABLE = "TASKS";
    private static final String TASK_ID = "ID";
    public static final String TITLE = "TITLE";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String DEADLINE = "DEADLINE";
    private static final String DUE_TIME = "DUE_TIME";
    private static final String STATUS = "STATUS";
    private static final String PRIORITY = "PRIORITY";
    private static final String USER_EMAIL = "USER_EMAIL";
    private static final String TASK_CATEGORY_ID = "CATEGORY_ID";
    private static final String TASK_REMINDER = "REMINDER";
    private static final String NOTIFICATION_TIME = "NOTIFYING";
    private SharedPrefManager sharedPrefManager;
    private Context context;


    public TaskDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTasksTable = "CREATE TABLE " + TASKS_TABLE + " ("
                //          +"_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TITLE + " TEXT NOT NULL, "
                + DESCRIPTION + " TEXT, "
                + DEADLINE + " TEXT, "
                + DUE_TIME + " TEXT, "
                + STATUS + " TEXT DEFAULT 'PENDING', "
                + PRIORITY + " TEXT DEFAULT 'MEDIUM', "
                + USER_EMAIL + " TEXT, "
                + TASK_CATEGORY_ID + " INTEGER, "
                + TASK_REMINDER + " TEXT DEFAULT 'REMINDE',"
                + NOTIFICATION_TIME + " TEXT DEFAULT 'NONE'" +")";
        sqLiteDatabase.execSQL(createTasksTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TASKS_TABLE);
        onCreate(sqLiteDatabase);
    }

    public long addTask(String title, String description, String deadline, String dueTime, String status, String priority, String userEmail, int categoryId ,String reminder,String notifcationDue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TITLE, title);
        values.put(DESCRIPTION, description);
        values.put(DEADLINE, deadline);
        values.put(DUE_TIME, dueTime); // Store dueTime
        values.put(STATUS, status);
        values.put(PRIORITY, priority);
        values.put(USER_EMAIL, userEmail);
        values.put(TASK_CATEGORY_ID, categoryId);
        values.put(TASK_REMINDER, reminder);
        values.put(NOTIFICATION_TIME,notifcationDue);
        return db.insert(TASKS_TABLE, null, values);
    }


    public String getCurrentUserEmail() {
        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(context);
        return sharedPrefManager.readString("logged_in_user", "noValue");
    }
    public Cursor getAllTasks() {
        String userEmail = getCurrentUserEmail();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = USER_EMAIL + " = ?";
        String[] selectionArgs = {userEmail};

        // Log the query
        Log.d("TaskDatabaseHelper", "Fetching tasks for user: " + userEmail);

        Cursor cursor = db.query(TASKS_TABLE, null, selection, selectionArgs, null, null, null);

        // Log the cursor content
        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex("TITLE"));
                Log.d("TaskDatabaseHelper", "Retrieved Task: " + title);
            }
            cursor.moveToPosition(-1); // Reset cursor position
        }

        return cursor;
    }


    public Cursor getTasksForDate(String date) {
        String userEmail = getCurrentUserEmail();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = DEADLINE + " = ? AND " + USER_EMAIL + " = ?";
        String[] selectionArgs = {date, userEmail};
        return db.query(TASKS_TABLE, null, selection, selectionArgs, null, null, null);
    }

    public void updateTaskStatus(String taskId, String newStatus) {
        String userEmail = getCurrentUserEmail();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("STATUS", newStatus);
        db.update("TASKS", values, "ID = ? AND " + USER_EMAIL + " = ?", new String[]{taskId, userEmail});
    }

    public void deleteTask(String taskId) {
        String userEmail = getCurrentUserEmail();
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("TASKS", "ID = ? AND " + USER_EMAIL + " = ?", new String[]{taskId, userEmail});
    }
    public Cursor getCompletedTasks() {
        String userEmail = getCurrentUserEmail();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM TASKS WHERE STATUS = 'Completed' AND USER_EMAIL = ?";
        return db.rawQuery(query, new String[]{userEmail});
    }

    public Cursor getTasksWithinDateRange(String startDate, String endDate) {
        String userEmail = getCurrentUserEmail();
        SQLiteDatabase db = this.getReadableDatabase();

        // Validate inputs
        if (startDate == null || startDate.isEmpty()) {
            Log.e("TaskDatabaseHelper", "Start date is null or empty");
            return null;
        }
        if (endDate == null || endDate.isEmpty()) {
            Log.e("TaskDatabaseHelper", "End date is null or empty");
            return null;
        }
        if (userEmail == null || userEmail.isEmpty()) {
            Log.e("TaskDatabaseHelper", "User email is null or empty");
            return null;
        }

        Log.d("TaskDatabaseHelper", "Fetching tasks between " + startDate + " and " + endDate + " for user: " + userEmail);

        // SQLite query to fetch tasks within the date range
        String query = "SELECT * FROM " + TASKS_TABLE +
                " WHERE strftime('%Y-%m-%d', DEADLINE) >= strftime('%Y-%m-%d', ?) " +
                "AND strftime('%Y-%m-%d', DEADLINE) <= strftime('%Y-%m-%d', ?) " +
                "AND " + USER_EMAIL + " = ?";

        try {
            // Execute the query with placeholders replaced
            return db.rawQuery(query, new String[]{startDate, endDate, userEmail});
        } catch (Exception e) {
            Log.e("TaskDatabaseHelper", "Error fetching tasks within date range", e);
            return null;
        }
    }

    public Cursor getTasksForTitle(String date, String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = DEADLINE + " = ? AND " + USER_EMAIL + " = ? AND (" + TITLE + " LIKE ? OR " + DESCRIPTION + " LIKE ?)";
        String[] selectionArgs = {date, getCurrentUserEmail(), "%" + query + "%", "%" + query + "%"};
        return db.query(TASKS_TABLE, null, selection, selectionArgs, null, null, null);
    }


    public Cursor getTasksForTitleOrDescription(String query) {
        String userEmail = getCurrentUserEmail();
        SQLiteDatabase db = this.getReadableDatabase();

        // Perform a case-insensitive search for tasks that match the query in either title or description
        String selection = "(" + TITLE + " LIKE ? OR " + DESCRIPTION + " LIKE ?) AND " + USER_EMAIL + " = ?";
        String[] selectionArgs = {"%" + query + "%", "%" + query + "%", userEmail};

        return db.query(TASKS_TABLE, null, selection, selectionArgs, null, null, null);
    }
    public void updateTasksUserEmail(String oldEmail, String newEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("USER_EMAIL", newEmail);
        db.update("TASKS", values, "USER_EMAIL = ?", new String[]{oldEmail});
    }


    public Cursor getTasksSortedByPriorityWithFilter(String date, String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String userEmail = getCurrentUserEmail();

        // Define the WHERE clause
        String selection = DEADLINE + " = ? AND " + USER_EMAIL + " = ? AND TITLE LIKE ?";
        String[] selectionArgs = {date, userEmail, "%" + query + "%"};

        // Sort tasks by priority order (with Medium as default)
        String orderBy = "CASE PRIORITY " +
                "WHEN 'High' THEN 1 " +
                "WHEN 'Medium' THEN 2 " +
                "WHEN 'Low' THEN 3 END, " + DEADLINE + " ASC"; // Sort by deadline as secondary

        // Execute query
        return db.query(TASKS_TABLE, null, selection, selectionArgs, null, null, orderBy);
    }


    public Cursor getTaskById(String id) {
        String userEmail = getCurrentUserEmail();
        SQLiteDatabase db = this.getReadableDatabase();

        // Define the WHERE clause
        String selection = "ID = ? AND " + USER_EMAIL + " = ?";
        String[] selectionArgs = {id, userEmail};

        // Query the database for the specific task
        return db.query(TASKS_TABLE, null, selection, selectionArgs, null, null, null);
    }
    public int updateTaskDetails(String taskId, String newTitle, String newDescription, String newDeadline,String newDueTime, String newPriority ,String reminder,String newNotificationTime) {
        String userEmail = getCurrentUserEmail();
        SQLiteDatabase db = this.getWritableDatabase();

        // Define the new values for the task
        ContentValues values = new ContentValues();
        values.put(TITLE, newTitle);
        values.put(DESCRIPTION, newDescription);
        values.put(DEADLINE, newDeadline);
        values.put(DUE_TIME, newDueTime);
        values.put(PRIORITY, newPriority);
        values.put(USER_EMAIL, userEmail);
        values.put(TASK_REMINDER,reminder);
        values.put(NOTIFICATION_TIME,newNotificationTime);

        // Define WHERE clause to ensure only the task of the logged-in user gets updated
        String whereClause = "ID = ? AND " + USER_EMAIL + " = ?";
        String[] whereArgs = {taskId, userEmail};

        // Update the task and return the number of rows affected
        return db.update(TASKS_TABLE, values, whereClause, whereArgs);
    }
    public Cursor getTasksMatchingReminderTime(String reminderTime) {
        String userEmail = getCurrentUserEmail();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query tasks where the reminder time matches the due time
        String selection = DUE_TIME + " = ? AND " + USER_EMAIL + " = ?";
        String[] selectionArgs = {reminderTime, userEmail};

        return db.query(TASKS_TABLE, null, selection, selectionArgs, null, null, null);
    }

    public int updateNotificationTime(String taskId, String newNotificationTime) {
        String userEmail = getCurrentUserEmail();
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NOTIFICATION_TIME, newNotificationTime);

        String whereClause = TASK_ID + " = ? AND " + USER_EMAIL + " = ?";
        String[] whereArgs = {taskId, userEmail};

        return db.update(TASKS_TABLE, values, whereClause, whereArgs);
    }
    public int updateTaskReminder(String taskId, String newReminder) {
        String userEmail = getCurrentUserEmail();
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TASK_REMINDER, newReminder);

        String whereClause = TASK_ID + " = ? AND " + USER_EMAIL + " = ?";
        String[] whereArgs = {taskId, userEmail};

        return db.update(TASKS_TABLE, values, whereClause, whereArgs);
    }


}
