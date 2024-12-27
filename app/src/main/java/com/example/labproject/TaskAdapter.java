package com.example.labproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;



public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private Context context;
    private TaskActionListener actionListener;

    public interface TaskActionListener {
        void onDeleteTask(Task task);
        void onSendEmail(Task task);
        void onEditTask(Task task);
        void onTaskClick(Task task);
        void onSetNotification(Task task);
    }

    public TaskAdapter(Context context, List<Task> taskList, TaskActionListener actionListener) {
        this.context = context;
        this.taskList = taskList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.taskTitle.setText(task.getTitle());

        if (task.getDueDateTime() != null && !task.getDueDateTime().isEmpty()) {
            holder.taskDueDate.setText(task.getDueDateTime());
        } else {
            holder.taskDueDate.setText("No due date specified");
        }

        holder.deleteButton.setOnClickListener(v -> actionListener.onDeleteTask(task));
        holder.sendEmailButton.setOnClickListener(v -> actionListener.onSendEmail(task));
        holder.editButton.setOnClickListener(v -> actionListener.onEditTask(task));
        holder.itemView.setOnClickListener(v -> actionListener.onTaskClick(task));
        holder.notificationButton.setOnClickListener(v -> actionListener.onSetNotification(task));

    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void updateTasks(List<Task> updatedTaskList) {
        this.taskList = updatedTaskList;
        notifyDataSetChanged();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle, taskDueDate, dayHeader;
        ImageButton deleteButton, sendEmailButton, editButton,notificationButton;

        TaskViewHolder(View itemView) {
            super(itemView);
            dayHeader = itemView.findViewById(R.id.text_day_header);
            taskTitle = itemView.findViewById(R.id.text_task_title);
            taskDueDate = itemView.findViewById(R.id.text_task_due_date);
            deleteButton = itemView.findViewById(R.id.button_delete_task);
            sendEmailButton = itemView.findViewById(R.id.button_send_email_task);
            editButton = itemView.findViewById(R.id.button_edit_task);
            notificationButton = itemView.findViewById(R.id.button_set_notification);

        }
    }

}

