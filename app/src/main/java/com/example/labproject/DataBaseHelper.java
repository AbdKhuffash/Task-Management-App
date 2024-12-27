package com.example.labproject;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DataBaseHelper extends SQLiteOpenHelper {

    public DataBaseHelper(Context context, String name,
                          SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE USER (EMAIL TEXT PRIMARY KEY, FIRSTNAME TEXT, LASTNAME TEXT, PASSWORD TEXT)");
        db.execSQL("CREATE TABLE TASK (TASK_ID INTEGER PRIMARY KEY AUTOINCREMENT, USER_EMAIL TEXT, TITLE TEXT, DESCRIPTION TEXT, DUE_DATE_TIME TEXT, PRIORITY TEXT, IS_COMPLETED INTEGER, FOREIGN KEY(USER_EMAIL) REFERENCES USER(EMAIL) , UNIQUE(USER_EMAIL, TITLE))");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
    public boolean checkUsernameExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM USER WHERE EMAIL = ?", new String[]{email});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    public boolean registerUser(User user) {
        if (checkUsernameExists(user.getEmail())) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("EMAIL", user.getEmail());
        values.put("FIRSTNAME", user.getFirstName().toUpperCase());
        values.put("LASTNAME", user.getLastName().toUpperCase());
        values.put("PASSWORD", user.getPassword());

        long result = db.insert("USER", null, values);
        return result != -1;
    }

    public boolean validateUserCredentials(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM USER WHERE EMAIL = ? AND PASSWORD = ?", new String[]{email, password});
        boolean isValid = cursor.moveToFirst();
        cursor.close();
        return isValid;
    }

    public String getUserFullName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String fullName = "";

        try {
            cursor = db.rawQuery("SELECT FIRSTNAME, LASTNAME FROM USER WHERE EMAIL = ?", new String[]{email});
            if (cursor.moveToFirst()) {
                String firstName = cursor.getString(cursor.getColumnIndexOrThrow("FIRSTNAME"));
                String lastName = cursor.getString(cursor.getColumnIndexOrThrow("LASTNAME"));
                fullName = firstName + " " + lastName;
            }
        } catch (Exception e) {
            Log.e("DataBaseHelper", "Error while fetching user full name", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return fullName;
    }

    public boolean updateEmail(String oldEmail, String newEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("EMAIL", newEmail);
        int rows = db.update("USER", values, "EMAIL = ?", new String[]{oldEmail});
        return rows > 0;
    }

    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("PASSWORD", newPassword);
        int rows = db.update("USER", values, "EMAIL = ?", new String[]{email});
        return rows > 0;
    }

    public boolean updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("DESCRIPTION", task.getDescription());
        values.put("DUE_DATE_TIME", task.getDueDateTime());
        values.put("PRIORITY", task.getPriority());
        values.put("IS_COMPLETED", task.isCompleted() ? 1 : 0);

        int rowsAffected = db.update("TASK", values, "TITLE = ? AND USER_EMAIL = ?",
                new String[]{task.getTitle(), task.getUserEmail()});
        return rowsAffected > 0;
    }



    public boolean isTaskTitleUnique(String userEmail, String title) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM TASK WHERE USER_EMAIL = ? AND TITLE = ?",
                new String[]{userEmail, title});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count == 0;
    }


    public List<Task> getAllTasksSortedByDueDate(String userEmail) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT * FROM TASK WHERE USER_EMAIL = ? ORDER BY DUE_DATE_TIME ASC",
                    new String[]{userEmail}
            );

            while (cursor.moveToNext()) {
                @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex("TITLE"));
                @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex("DESCRIPTION"));
                @SuppressLint("Range") String dueDateTime = cursor.getString(cursor.getColumnIndex("DUE_DATE_TIME"));
                @SuppressLint("Range") String priority = cursor.getString(cursor.getColumnIndex("PRIORITY"));
                @SuppressLint("Range") boolean isCompleted = cursor.getInt(cursor.getColumnIndex("IS_COMPLETED")) == 1;

                Log.d("ss","============="+dueDateTime);
                Task task = new Task(userEmail, title, description, dueDateTime, isCompleted, priority);
                taskList.add(task);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return taskList;
    }

    public boolean deleteTask(String taskTitle, String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete("TASK", "TITLE = ? AND USER_EMAIL = ?", new String[]{taskTitle, userEmail});
        return rowsAffected > 0;
    }


    public boolean addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("USER_EMAIL", task.getUserEmail());
        values.put("TITLE", task.getTitle());
        values.put("DESCRIPTION", task.getDescription());
        values.put("DUE_DATE_TIME", task.getDueDateTime().toString());
        values.put("PRIORITY", task.getPriority());
        values.put("IS_COMPLETED", 0);

        long result = db.insert("TASK", null, values);
        return result != -1;
    }

    public List<Task> getTodayTasks(String userEmail) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        try {
            String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            cursor = db.rawQuery(
                    "SELECT * FROM TASK WHERE USER_EMAIL = ? AND DUE_DATE_TIME LIKE ? AND IS_COMPLETED = 0 ORDER BY DUE_DATE_TIME ASC",
                    new String[]{userEmail, todayDate + "%"}
            );

            while (cursor.moveToNext()) {
                @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex("TITLE"));
                @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex("DESCRIPTION"));
                @SuppressLint("Range") String dueDateTime = cursor.getString(cursor.getColumnIndex("DUE_DATE_TIME"));
                @SuppressLint("Range") String priority = cursor.getString(cursor.getColumnIndex("PRIORITY"));
                @SuppressLint("Range") boolean isCompleted = cursor.getInt(cursor.getColumnIndex("IS_COMPLETED")) == 1;

                Task task = new Task(userEmail, title, description, dueDateTime, isCompleted, priority);
                taskList.add(task);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return taskList;
    }



    public List<Task> getAllTasksWithHeadersGroupedByDay(String userEmail) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT * FROM TASK WHERE USER_EMAIL = ? ORDER BY DUE_DATE_TIME ASC",
                    new String[]{userEmail}
            );

            String lastDayHeader = null;
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex("TITLE"));
                @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex("DESCRIPTION"));
                @SuppressLint("Range") String dueDateTime = cursor.getString(cursor.getColumnIndex("DUE_DATE_TIME"));
                @SuppressLint("Range") String priority = cursor.getString(cursor.getColumnIndex("PRIORITY"));
                @SuppressLint("Range") boolean isCompleted = cursor.getInt(cursor.getColumnIndex("IS_COMPLETED")) == 1;

                Task task = new Task(userEmail, title, description, dueDateTime, isCompleted, priority);

                String dayHeader = "Unspecified";
                if (dueDateTime != null && !dueDateTime.isEmpty()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
                    try {
                        Date date = dateFormat.parse(dueDateTime);
                        if (date != null) {
                            dayHeader = dayFormat.format(date);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                if (!dayHeader.equals(lastDayHeader)) {
                    Task headerTask = new Task();
                    headerTask.setTitle(dayHeader);
                    headerTask.setDueDateTime(null);
                    headerTask.setDescription(null);
                    headerTask.setPriority(null);
                    taskList.add(headerTask);
                    lastDayHeader = dayHeader;
                }

                taskList.add(task);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return taskList;
    }

    public List<Task> getAllCompletedTasksWithHeadersGroupedByDay(String userEmail) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT * FROM TASK WHERE USER_EMAIL = ? ORDER BY DUE_DATE_TIME ASC",
                    new String[]{userEmail}
            );

            String lastDayHeader = null;
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex("TITLE"));
                @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex("DESCRIPTION"));
                @SuppressLint("Range") String dueDateTime = cursor.getString(cursor.getColumnIndex("DUE_DATE_TIME"));
                @SuppressLint("Range") String priority = cursor.getString(cursor.getColumnIndex("PRIORITY"));
                @SuppressLint("Range") boolean isCompleted = cursor.getInt(cursor.getColumnIndex("IS_COMPLETED")) == 1;

                Task task = new Task(userEmail, title, description, dueDateTime, isCompleted, priority);

                String dayHeader = "Unspecified";
                if (dueDateTime != null && !dueDateTime.isEmpty()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
                    try {
                        Date date = dateFormat.parse(dueDateTime);
                        if (date != null) {
                            dayHeader = dayFormat.format(date);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                if (!dayHeader.equals(lastDayHeader)) {
                    Task headerTask = new Task();
                    headerTask.setTitle(dayHeader);
                    headerTask.setDueDateTime(null);
                    headerTask.setDescription(null);
                    headerTask.setPriority(null);
                    taskList.add(headerTask);
                    lastDayHeader = dayHeader;
                }
                if(task.isCompleted())
                    taskList.add(task);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return taskList;
    }


    public List<Task> searchTasksByPeriod(String userEmail, String startDate, String endDate) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        try {
            String query = "SELECT * FROM TASK WHERE USER_EMAIL = ? AND DUE_DATE_TIME BETWEEN ? AND ? ORDER BY DUE_DATE_TIME ASC";
            cursor = db.rawQuery(query, new String[]{userEmail, startDate + " 00:00", endDate + " 23:59"});

            while (cursor.moveToNext()) {
                @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex("TITLE"));
                @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex("DESCRIPTION"));
                @SuppressLint("Range") String dueDateTime = cursor.getString(cursor.getColumnIndex("DUE_DATE_TIME"));
                @SuppressLint("Range") String priority = cursor.getString(cursor.getColumnIndex("PRIORITY"));
                @SuppressLint("Range") boolean isCompleted = cursor.getInt(cursor.getColumnIndex("IS_COMPLETED")) == 1;

                Task task = new Task(userEmail, title, description, dueDateTime, isCompleted, priority);
                taskList.add(task);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return taskList;
    }

    public List<Task> searchTasks(String userEmail, String keyword) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        try {
            String query = "SELECT * FROM TASK WHERE USER_EMAIL = ? AND (TITLE LIKE ? OR DESCRIPTION LIKE ?) ORDER BY DUE_DATE_TIME ASC";
            cursor = db.rawQuery(query, new String[]{userEmail, "%" + keyword + "%", "%" + keyword + "%"});

            while (cursor.moveToNext()) {
                @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex("TITLE"));
                @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex("DESCRIPTION"));
                @SuppressLint("Range") String dueDateTime = cursor.getString(cursor.getColumnIndex("DUE_DATE_TIME"));
                @SuppressLint("Range") String priority = cursor.getString(cursor.getColumnIndex("PRIORITY"));
                @SuppressLint("Range") boolean isCompleted = cursor.getInt(cursor.getColumnIndex("IS_COMPLETED")) == 1;

                Task task = new Task(userEmail, title, description, dueDateTime, isCompleted, priority);
                taskList.add(task);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return taskList;
    }


}
