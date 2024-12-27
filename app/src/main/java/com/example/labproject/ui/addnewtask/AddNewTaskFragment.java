package com.example.labproject.ui.addnewtask;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.labproject.DataBaseHelper;
import com.example.labproject.R;
import com.example.labproject.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddNewTaskFragment extends Fragment {

    private AddNewTaskViewModel mViewModel;
    private DataBaseHelper databaseHelper;

    public static AddNewTaskFragment newInstance() {
        return new AddNewTaskFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_new_task, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AddNewTaskViewModel.class);

        databaseHelper = new DataBaseHelper(getActivity(), "DB", null, 1);

        View view = getView();
        if (view != null) {
            Button addTaskButton = view.findViewById(R.id.button_add_new_task);
            addTaskButton.setOnClickListener(v -> showAddTaskDialog());
        }
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_task_diolog, null);

        builder.setView(dialogView);

        EditText taskTitle = dialogView.findViewById(R.id.edit_task_title);
        EditText taskDescription = dialogView.findViewById(R.id.edit_task_description);
        EditText dueDate = dialogView.findViewById(R.id.edit_task_due_date);
        EditText dueTime = dialogView.findViewById(R.id.edit_task_due_time);
        Spinner prioritySpinner = dialogView.findViewById(R.id.spinner_task_priority);
        Button saveButton = dialogView.findViewById(R.id.button_save);
        Button cancelButton = dialogView.findViewById(R.id.button_cancel);

        final String[] selectedDateTime = {null};
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        dueDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                    (DatePicker view, int year, int month, int dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        dueDate.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth));
                        if (dueTime.getText().toString().isEmpty()) {
                            selectedDateTime[0] = null;
                        } else {
                            selectedDateTime[0] = dateTimeFormat.format(calendar.getTime());
                        }
                    },
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        dueTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                    (TimePicker view, int hourOfDay, int minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        dueTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                        if (dueDate.getText().toString().isEmpty()) {
                            selectedDateTime[0] = null;
                        } else {
                            selectedDateTime[0] = dateTimeFormat.format(calendar.getTime());
                        }
                    },
                    calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        });

        AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(v -> {
            String title = taskTitle.getText().toString().trim();
            String description = taskDescription.getText().toString().trim();
            String priority = prioritySpinner.getSelectedItem().toString();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(selectedDateTime[0])) {
                Toast.makeText(getActivity(), "Please enter all required fields", Toast.LENGTH_SHORT).show();
            } else {
                Task task = new Task();
                task.setTitle(title);
                task.setDescription(description);
                task.setDueDateTime(selectedDateTime[0]);
                task.setPriority(priority);
                task.setUserEmail(getCurrentUserEmail());
                task.setCompleted(false);

                boolean isAdded = databaseHelper.addTask(task);
                if (isAdded) {
                    Toast.makeText(getActivity(), "Task added successfully", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(getView()).navigate(R.id.action_addNewTaskFragment_to_homeFragment);
                } else {
                    Toast.makeText(getActivity(), "Task could not be added", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private String getCurrentUserEmail() {
        return getActivity().getIntent().getStringExtra("email");
    }
}
