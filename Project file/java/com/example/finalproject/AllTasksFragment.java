package com.example.finalproject;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.appcompat.widget.SearchView;  // Correct import
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class AllTasksFragment extends Fragment implements NewTaskFragment.OnTaskSavedListener {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private FloatingActionButton fab;
    private SearchView searchView; // Declare SearchView

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the correct layout with RecyclerView and FAB
        View view = inflater.inflate(R.layout.fragment_view_all_tasks, container, false);

        // Initialize Views
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        recyclerView = view.findViewById(R.id.recycler_view_tasks);
        fab = view.findViewById(R.id.fab);
        searchView = view.findViewById(R.id.search_view); // Initialize SearchView

        // Setup Toolbar
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar);

        // Initialize RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load all tasks initially
        loadTasks(""); // Pass an empty string to load all tasks

        // Floating Action Button click listener
        fab.setOnClickListener(v -> {
            NewTaskFragment newTaskFragment = new NewTaskFragment();
            newTaskFragment.setOnTaskSavedListener(newTask -> {
                loadTasks(""); // Reload all tasks when a new task is saved
            });
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, newTaskFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Handle search input to filter tasks
// Handle search input to filter tasks
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // No action needed when user presses 'Enter'
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter tasks as the user types
                loadTasks(newText);
                return true;
            }
        });

        ImageView ivBackArrow = view.findViewById(R.id.iv_back_arrow);
        ivBackArrow.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigateUp();
        });

        return view;
    }

    private void loadTasks(String query) {
        TaskDatabaseHelper dbHelper = new TaskDatabaseHelper(getContext());
        Cursor cursor;

        // If a search query is provided, filter the tasks by title or description
        if (query.isEmpty()) {
            cursor = dbHelper.getAllTasks(); // Fetch all tasks without filtering
        } else {
            // Fetch tasks that match the search query in the title or description
            cursor = dbHelper.getTasksForTitleOrDescription(query); // Modify to search in title or description
        }

        // Initialize TaskAdapter with the Cursor
        if (cursor != null) {
            taskAdapter = new TaskAdapter(cursor, getContext(), "all",getParentFragmentManager());
            recyclerView.setAdapter(taskAdapter);
        }
    }

    public void onTaskSaved(Task task) {
        loadTasks(""); // Reload tasks when a new task is saved
    }


}
