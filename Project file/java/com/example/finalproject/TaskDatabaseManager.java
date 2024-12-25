package com.example.finalproject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
public class TaskDatabaseManager {

    private SQLiteDatabase db;

    public TaskDatabaseManager(Context context) {
        db = new TaskDatabaseHelper(context).getWritableDatabase(); // Assuming TaskDBHelper handles database creation
    }

    public Cursor getAllTasks() {
        String query = "SELECT * FROM tasks"; // Replace with your actual table name
        return db.rawQuery(query, null);
    }
}
