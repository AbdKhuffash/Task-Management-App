package com.example.labproject.ui.allcompletedtasks;

import androidx.lifecycle.ViewModelProvider;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.labproject.DataBaseHelper;
import com.example.labproject.NotificationReceiver;
import com.example.labproject.R;
import com.example.labproject.Task;
import com.example.labproject.TaskAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShowAllCompletedTasks extends Fragment {

    private RecyclerView recyclerView;
    private TextView textNoTasks;
    private TaskAdapter taskAdapter;
    private DataBaseHelper databaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_all_completed_tasks, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_tasks);
        textNoTasks = view.findViewById(R.id.text_no_tasks);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        databaseHelper = new DataBaseHelper(getContext(), "DB", null, 1);

        loadTasks();

        return view;
    }

    private void loadTasks() {
        String userEmail = getActivity().getIntent().getStringExtra("email");

        List<Task> tasksWithHeaders = databaseHelper.getAllCompletedTasksWithHeadersGroupedByDay(userEmail);

        if (tasksWithHeaders.isEmpty()) {
            textNoTasks.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            textNoTasks.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            taskAdapter = new TaskAdapter(getContext(), tasksWithHeaders, new TaskAdapter.TaskActionListener() {
                @Override
                public void onDeleteTask(Task task) {
                    if (task.getDescription() != null) {
                        databaseHelper.deleteTask(task.getTitle(), userEmail);
                        refreshTasks();
                    }
                }

                @Override
                public void onSendEmail(Task task) {
                    if (task.getDescription() != null) {
                        String subject = "Task Details: " + task.getTitle();
                        String message = "Task Details:\n\n" +
                                "Title: " + task.getTitle() + "\n" +
                                "Description: " + task.getDescription() + "\n" +
                                "Due Date: " + (task.getDueDateTime() != null ? task.getDueDateTime() : "No due date specified") + "\n" +
                                "Priority: " + task.getPriority() + "\n" +
                                "Status: " + (task.isCompleted() ? "Completed" : "Incomplete") + "\n\n" +
                                "Shared from Task Manager App.";

                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.setType("message/rfc822");
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                        emailIntent.putExtra(Intent.EXTRA_TEXT, message);

                        try {
                            getContext().startActivity(Intent.createChooser(emailIntent, "Send Email"));
                        } catch (android.content.ActivityNotFoundException e) {
                            Toast.makeText(getContext(), "No email clients installed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onTaskClick(Task task) {
                    if (task.getDescription() != null) {
                        showTaskDetailsDialog(task);
                    }
                }
                @Override
                public void onSetNotification(Task task) {
                    if (task.getDescription() != null) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                            Date date = sdf.parse(task.getDueDateTime());

                            if (date != null) {
                                long triggerAtMillis = date.getTime() - (15 * 60 * 1000);
                                scheduleNotification(getContext(), "Task Reminder", task.getTitle(), triggerAtMillis);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
                        }
                    }
                }



                @Override
                public void onEditTask(Task task) {
                    if (task.getDescription() != null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                        LayoutInflater inflater = getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.dialog_edit_task, null);
                        builder.setView(dialogView);

                        EditText editDescription = dialogView.findViewById(R.id.edit_task_description);
                        EditText editDueDate = dialogView.findViewById(R.id.edit_task_due_date);
                        EditText editDueTime = dialogView.findViewById(R.id.edit_task_due_time);
                        Spinner editPriority = dialogView.findViewById(R.id.spinner_task_priority);
                        CheckBox checkBoxMarkCompleted = dialogView.findViewById(R.id.checkbox_mark_completed);

                        Button saveButton = dialogView.findViewById(R.id.button_save_task);
                        Button cancelButton = dialogView.findViewById(R.id.button_cancel_task);

                        editDescription.setText(task.getDescription());
                        editDueDate.setText(task.getDueDateTime().split(" ")[0]);
                        editDueTime.setText(task.getDueDateTime().split(" ")[1]);
                        String[] priorities = getResources().getStringArray(R.array.priority_levels);
                        for (int i = 0; i < priorities.length; i++) {
                            if (priorities[i].equals(task.getPriority())) {
                                editPriority.setSelection(i);
                                break;
                            }
                        }

                        if (task.isCompleted()) {
                            checkBoxMarkCompleted.setVisibility(View.GONE);
                        }

                        AlertDialog dialog = builder.create();
                        dialog.show();

                        editDueDate.setOnClickListener(v -> {
                            Calendar calendar = Calendar.getInstance();
                            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                                    (view, year, month, dayOfMonth) -> {
                                        editDueDate.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
                                    },
                                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                            datePickerDialog.show();
                        });

                        editDueTime.setOnClickListener(v -> {
                            Calendar calendar = Calendar.getInstance();
                            TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                                    (view, hourOfDay, minute) -> {
                                        editDueTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                            timePickerDialog.show();
                        });

                        saveButton.setOnClickListener(v -> {
                            String updatedDescription = editDescription.getText().toString().trim();
                            String updatedDueDate = editDueDate.getText().toString().trim();
                            String updatedDueTime = editDueTime.getText().toString().trim();
                            String updatedPriority = editPriority.getSelectedItem().toString();
                            boolean markAsCompleted = checkBoxMarkCompleted.isChecked();

                            if (TextUtils.isEmpty(updatedDescription) || TextUtils.isEmpty(updatedDueDate) || TextUtils.isEmpty(updatedDueTime)) {
                                Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
                            } else {

                                task.setDescription(updatedDescription);
                                task.setDueDateTime(updatedDueDate + " " + updatedDueTime);
                                task.setPriority(updatedPriority);
                                if (markAsCompleted) {
                                    task.setCompleted(true);
                                }

                                boolean isUpdated = databaseHelper.updateTask(task);
                                if (isUpdated) {
                                    Toast.makeText(requireContext(), "Task updated successfully", Toast.LENGTH_SHORT).show();
                                    refreshTasks();
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(requireContext(), "Failed to update task", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        cancelButton.setOnClickListener(v -> dialog.dismiss());
                    }
                }
            });

            recyclerView.setAdapter(taskAdapter);
        }
    }


    private void refreshTasks() {
        String userEmail = getActivity().getIntent().getStringExtra("email");
        List<Task> updatedTasks = databaseHelper.getAllCompletedTasksWithHeadersGroupedByDay(userEmail);

        if (taskAdapter != null) {
            taskAdapter.updateTasks(updatedTasks);
        }
    }

    private void showTaskDetailsDialog(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(task.getTitle())
                .setMessage("Description: " + task.getDescription() +
                        "\nDue Date: " + (task.getDueDateTime() != null ? task.getDueDateTime() : "No due date") +
                        "\nPriority: " + task.getPriority())
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void scheduleNotification(Context context, String title, String message, long triggerAtMillis) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("Task Manager", title);
        intent.putExtra("TASK TASK TASKT ATKASKDASASDKASDKASDKASDK", message);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            Toast.makeText(context, "Notification set", Toast.LENGTH_SHORT).show();
        }
    }
}