package com.example.finalproject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiService {

    private static final String API_URL = "https://run.mocky.io/v3/c0f6b349-19e9-476c-b2d3-bb468783b817";

    public interface ApiCallback {
        void onSuccess(JSONArray tasks);
        void onFailure(String errorMessage);
    }

    public static void fetchTasks(Context context, ApiCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(API_URL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    return response.toString();
                } catch (Exception e) {
                    Log.e("ApiService", "Error fetching tasks", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    if (result != null) {
                        JSONArray tasks = new JSONArray(result);
                        callback.onSuccess(tasks);
                    } else {
                        callback.onFailure("Failed to fetch tasks.");
                    }
                } catch (Exception e) {
                    callback.onFailure("Error parsing API response.");
                }
            }
        }.execute();
    }
}
