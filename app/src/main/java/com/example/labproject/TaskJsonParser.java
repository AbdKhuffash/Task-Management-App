package com.example.labproject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TaskJsonParser {

    public static List<Task> getTasksFromJson(String json) {
        List<Task> tasks = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                Task task = new Task();
                task.setTitle(jsonObject.getString("title"));
                task.setDescription(jsonObject.getString("description"));
                task.setDueDateTime(jsonObject.getString("dueDateTime"));
                task.setPriority(jsonObject.getString("priority"));
                task.setCompleted(jsonObject.getBoolean("isCompleted"));

                tasks.add(task);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return tasks;
    }
}
