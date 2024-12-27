package com.example.labproject;

import java.time.LocalDateTime;

public class Task {
    private String userEmail;
    private String Title;
    private String Description;
    private String dueDateTime;
    private boolean isCompleted;
    private String Priority;

    public Task() {
    }

    public Task(String userEmail, String title, String description, String dueDateTime, boolean isCompleted, String priority) {
        this.userEmail = userEmail;
        Title = title;
        Description = description;
        this.dueDateTime = dueDateTime;
        this.isCompleted = isCompleted;
        Priority = priority;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getDueDateTime() {
        return dueDateTime;
    }

    public void setDueDateTime(String dueDateTime) {
        this.dueDateTime = dueDateTime;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getPriority() {
        return Priority;
    }

    public void setPriority(String priority) {
        Priority = priority;
    }


}
