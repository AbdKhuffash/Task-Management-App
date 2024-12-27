package com.example.labproject.ui.importtask;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.labproject.DataBaseHelper;
import com.example.labproject.R;
import com.example.labproject.Task;
import com.example.labproject.ConnectionAsyncTask;

import java.util.List;

public class ImportTaskFragment extends Fragment {

    private DataBaseHelper databaseHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_import_task, container, false);

        databaseHelper = new DataBaseHelper(getContext(), "DB", null, 1);

        Button importTasksButton = view.findViewById(R.id.button_import_tasks);
        importTasksButton.setOnClickListener(v -> importTasks());

        return view;
    }

    private void importTasks() {
        String dummyApiUrl = "https://run.mocky.io/v3/02874ce9-1bc7-48ac-8211-d9440b2df8fd";
        new ConnectionAsyncTask(getActivity(), tasks -> {
            if (tasks != null && !tasks.isEmpty()) {
                for (Task task : tasks) {
                    task.setUserEmail(getActivity().getIntent().getStringExtra("email"));
                    boolean added = databaseHelper.addTask(task);
                    if (!added) {
                        Toast.makeText(getContext(), "Failed to add task: " + task.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                }
                Toast.makeText(getContext(), "Tasks imported successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "No tasks found to import.", Toast.LENGTH_SHORT).show();
            }
        }).execute(dummyApiUrl);
    }
}
