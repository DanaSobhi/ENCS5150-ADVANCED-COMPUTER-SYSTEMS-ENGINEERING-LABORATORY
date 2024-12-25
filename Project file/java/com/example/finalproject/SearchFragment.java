package com.example.finalproject;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
public class SearchFragment extends Fragment {

    private EditText startDateEditText, endDateEditText;
    private Button searchButton;
    private RecyclerView searchResultsRecyclerView;
    private TaskDatabaseHelper databaseHelper;
    private TaskAdapter taskAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_search_tasks, container, false);

        // Initialize views
        startDateEditText = rootView.findViewById(R.id.startDateEditText);
        endDateEditText = rootView.findViewById(R.id.endDateEditText);
        searchButton = rootView.findViewById(R.id.searchButton);
        searchResultsRecyclerView = rootView.findViewById(R.id.searchResultsRecyclerView);


        // Set up RecyclerView
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        databaseHelper = new TaskDatabaseHelper(getContext());

        // Initialize adapter with an empty cursor
        taskAdapter = new TaskAdapter(null, getContext(), "search",getParentFragmentManager());
        searchResultsRecyclerView.setAdapter(taskAdapter);

        // Set click listeners for date fields
        startDateEditText.setOnClickListener(v -> showDatePickerDialog(startDateEditText));
        endDateEditText.setOnClickListener(v -> showDatePickerDialog(endDateEditText));

        // Set up search button functionality
        searchButton.setOnClickListener(v -> performSearch());

        ImageView ivBackArrow = rootView.findViewById(R.id.iv_back_arrow);
        ivBackArrow.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigateUp();
        });
        return rootView;
    }

    private void showDatePickerDialog(EditText targetEditText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Show the DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    targetEditText.setText(date);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    @SuppressLint("Range")
    private void performSearch() {
        String startDate = startDateEditText.getText().toString().trim();
        String endDate = endDateEditText.getText().toString().trim();

        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(getContext(), "Please select both start and end dates.", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            dateFormat.parse(startDate);
            dateFormat.parse(endDate);
        } catch (ParseException e) {
            Toast.makeText(getContext(), "Invalid date format. Please use yyyy-MM-dd.", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = databaseHelper.getTasksWithinDateRange(startDate, endDate);

        if (cursor != null) {
            Log.d("SearchFragment", "Cursor count: " + cursor.getCount());

            for (int i = 0; i < cursor.getColumnCount(); i++) {
                Log.d("SearchFragment", "Column: " + cursor.getColumnName(i));
            }

            if (cursor.moveToFirst()) {
                do {
                    Log.d("SearchFragment", "Task: " + cursor.getString(cursor.getColumnIndex(TaskDatabaseHelper.TITLE)));
                } while (cursor.moveToNext());
            }

            if (cursor.getCount() > 0) {
                taskAdapter.swapCursor(cursor);
                Toast.makeText(getContext(), cursor.getCount() + " tasks found.", Toast.LENGTH_SHORT).show();
            } else {
                taskAdapter.swapCursor(null);
                Toast.makeText(getContext(), "No tasks found for the selected range.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("SearchFragment", "Cursor is null.");
            Toast.makeText(getContext(), "Error fetching tasks.", Toast.LENGTH_SHORT).show();
        }
    }
}
