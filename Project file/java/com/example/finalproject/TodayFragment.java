package com.example.finalproject;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

public class TodayFragment extends Fragment implements NewTaskFragment.OnTaskSavedListener {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private FloatingActionButton fab;
    private EditText searchBar;
    private TaskDatabaseHelper dbHelper;
    private TextView noTasksFoundMessage;  // TextView for "No tasks found"
    private Button upload_API;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today, container, false);

        // Initialize Views
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        recyclerView = view.findViewById(R.id.recycler_view_tasks);
        fab = view.findViewById(R.id.fab);
        searchBar = view.findViewById(R.id.search_bar);
        noTasksFoundMessage = view.findViewById(R.id.no_tasks_found_message); // Initialize TextView
        upload_API = view.findViewById(R.id.button_upload_API);
        // Setup Toolbar
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar);

        // Setup Drawer Toggle
        DrawerLayout drawerLayout = activity.findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                activity,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Database Helper
        dbHelper = new TaskDatabaseHelper(getContext());

        upload_API.setOnClickListener(v ->{
            ApiService.fetchTasks(getContext(), new ApiService.ApiCallback() {
                public void onSuccess(JSONArray tasks) {

                    for (int i = 0; i < tasks.length(); i++) {
                        try {
                            JSONObject task = tasks.getJSONObject(i);
                            dbHelper.addTask(
                                    task.getString("title"),
                                    task.getString("description"),
                                    task.getString("deadline"),
                                    task.getString("dueTime"),
                                    task.getString("status"),
                                    task.getString("priority"),
                                    SharedPrefManager.getInstance(getContext()).readString("logged_in_user", "noValue"), // Current user
                                    task.getInt("categoryId"),
                                    task.getString("reminder"),
                                    task.getString("notificationTime")
                            );

                        } catch (Exception e) {
                            Log.e("HomeLayout", "Error importing task", e);
                        }
                    }

                    Toast.makeText(getContext(), "Tasks imported successfully!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Load today's tasks (sorted by priority)
        loadTodayTasks("");

        // Floating Action Button to add a new task
        fab.setOnClickListener(v -> {
            NewTaskFragment newTaskFragment = new NewTaskFragment();
            newTaskFragment.setOnTaskSavedListener(this);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, newTaskFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Search functionality
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String query = charSequence.toString();
                loadTodayTasks(query); // Reload tasks with sorting and search
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        return view;
    }

    /**
     * Load today's tasks sorted by priority and filtered by the search query
     */
    private void loadTodayTasks(String query) {
        Cursor cursor = dbHelper.getTasksSortedByPriorityWithFilter(getCurrentDate(), query);

        // Check if there are tasks matching the query
        if (cursor != null && cursor.getCount() > 0) {
             taskAdapter = new TaskAdapter(cursor, getContext(), "today", getParentFragmentManager());
            recyclerView.setAdapter(taskAdapter);
            noTasksFoundMessage.setVisibility(View.GONE); // Hide "No tasks found" message
        } else {
            noTasksFoundMessage.setVisibility(View.VISIBLE); // Show "No tasks found" message
        }
    }


    /**
     * Reload tasks after a new task is saved
     */
    @Override
    public void onTaskSaved(Task task) {
        if (task.getDueDate().equals(getCurrentDate())) {
            loadTodayTasks(""); // Reload today's tasks
        }
    }

    /**
     * Get today's date in YYYY-MM-DD format
     */
    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        return String.format("%d-%02d-%02d", year, month, day);
    }
}
