package com.example.finalproject;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.Objects;



public class DataBaseHelper extends android.database.sqlite.SQLiteOpenHelper {
    private Context context;

    public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {

        super(context, name, factory, version);
        this.context = context;
    }

    public DataBaseHelper(Context context) {
        this(context, "default.db", null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE USERS(EMAIL TEXT PRIMARY KEY UNIQUE NOT NULL, PASSWORD TEXT, FIRSTNAME TEXT , LASTNAME TEXT)");
        //    sqLiteDatabase.execSQL("CREATE TABLE USERS(ID INTEGER PRIMARY KEY AUTOINCREMENT, EMAIL TEXT UNIQUE NOT NULL, PASSWORD TEXT, FIRSTNAME TEXT , LASTNAME TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
    }

    public String addUser(Users user) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("EMAIL", user.getEmailAddress());
        contentValues.put("PASSWORD", user.getPassword());
        contentValues.put("FIRSTNAME", user.getFirstName());
        contentValues.put("LASTNAME", user.getLastName());

        try {
            long result = sqLiteDatabase.insertOrThrow("USERS", null, contentValues);
            if (result == -1) {
                return "There's an error, try again.";
            } else {
                return "Added, welcome.";

            }

        } catch (SQLiteConstraintException e) {
            if (Objects.requireNonNull(e.getMessage()).contains("UNIQUE constraint failed")) {
                return "Email already exists; try another Email.";
            }
        }
        return "";
    }

/*
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM USERS WHERE EMAIL = ? AND PASSWORD = ?", new String[]{email, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

*/

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM USERS WHERE EMAIL = ? AND PASSWORD = ?",
                new String[]{email, password}
        );
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();

        if (exists) {
            SharedPrefManager.getInstance(context).writeString("logged_in_user", email);
        }
        return exists;
    }

    public Cursor getUserData(String email) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        String query = "SELECT * FROM USERS WHERE EMAIL = ?";
        return sqLiteDatabase.rawQuery(query, new String[]{email});
    }

    public boolean emailExist(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM USERS WHERE EMAIL = ?", new String[]{email}
        );
        boolean exist = cursor.getCount() > 0;
        cursor.close();
   //     db.close();
        return exist;
    }

    public boolean updateUserField(String currentEmail, String field, String newValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = 0;

        try {
            db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();

            if (field.equals("EMAIL")) {
                if (emailExist(newValue)) {
                    return false;
                }

                contentValues.put("EMAIL", newValue);
                rowsAffected = db.update("USERS", contentValues, "EMAIL = ?", new String[]{currentEmail});

                if (rowsAffected > 0) {
                    try (TaskDatabaseHelper taskDbHelper = new TaskDatabaseHelper(context)) {
                        taskDbHelper.updateTasksUserEmail(currentEmail, newValue);
                    }
                }
            } else if (field.equals("PASSWORD")) {
                contentValues.put("PASSWORD", newValue);
                rowsAffected = db.update("USERS", contentValues, "EMAIL = ?", new String[]{currentEmail});
            }

            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("DatabaseError", "Error updating user field", e);
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
}


/*
    public boolean updateUserField(String currenEmail, String field, String newValue) {
        if(field.equals("EMAIL")) {
            if (emailExist(newValue)) {
                return false;
            }
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(field, newValue);
            int rowsAffected = db.update("USERS", contentValues, "EMAIL = ?", new String[]{currenEmail});
            if (rowsAffected > 0 && field.equals("EMAIL")) {
                TaskDatabaseHelper taskDbHelper = new TaskDatabaseHelper(context);

                taskDbHelper.updateTasksUserEmail(currenEmail, newValue);
            }
        }
        else if (field.equals("PASSWORD")) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(field, newValue);
            int rowsAffected = db.update("USERS", contentValues, "EMAIL = ?", new String[]{currenEmail});
        }
            return rowsAffected > 0;
    }
}
*/