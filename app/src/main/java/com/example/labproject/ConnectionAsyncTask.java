package com.example.labproject;

import android.app.Activity;
import android.os.AsyncTask;

import java.util.List;

public class ConnectionAsyncTask extends AsyncTask<String, String, String> {

    private Activity activity;
    private ImportTaskListener listener;

    public interface ImportTaskListener {
        void onTasksImported(List<Task> tasks);
    }

    public ConnectionAsyncTask(Activity activity, ImportTaskListener listener) {
        this.activity = activity;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        return HttpManager.getData(params[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (result != null) {
            List<Task> tasks = TaskJsonParser.getTasksFromJson(result);
            if (listener != null) {
                listener.onTasksImported(tasks);
            }
        }
    }
}

