package com.example.finalproject;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditTaskFragment extends Fragment {

    private EditText editTitle, editDescription, editDeadline,editDueTime,editnotification;
    private Spinner spinnerPriority;
    private CheckBox reminderBox;
    private Button btnSave;
    private TaskDatabaseHelper dbHelper;
    private String taskId;
    private String selectedPriority = "Medium"; // Default priority level

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_task, container, false);

        // Initialize database helper and views
        dbHelper = new TaskDatabaseHelper(requireContext());
        editTitle = view.findViewById(R.id.edit_task_title);
        editDescription = view.findViewById(R.id.edit_task_description);
        editDeadline = view.findViewById(R.id.edit_task_deadline);
        spinnerPriority = view.findViewById(R.id.spinner_task_priority);
        btnSave = view.findViewById(R.id.btn_save_task);
        editDueTime = view.findViewById(R.id.task_due_date);
        reminderBox = view.findViewById(R.id.reminderrr);
        editnotification = view.findViewById(R.id.edit_task_notification);


        // Set up Toolbar
        ImageView ivBackArrow = view.findViewById(R.id.iv_back_arrow);
        ivBackArrow.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_editTaskFragment_to_todayFragment);
        });

        // Set up Spinner with priority levels
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.priority_levels, // Define this in `res/values/strings.xml`
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(adapter);

        spinnerPriority.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPriority = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Date picker for deadline
        editDeadline.setOnClickListener(v -> showDatePicker());
        editDueTime.setOnClickListener(v -> showTimePicker());
        editnotification.setOnClickListener(v -> showDateTimePicker());

        // Fetch task details
        Bundle args = getArguments();
        if (args != null) {
            taskId = args.getString("task_id");
            loadTaskDetails(taskId);
        }

        // Save button click
        btnSave.setOnClickListener(v -> saveTaskDetails());

        return view;
    }


    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            editDeadline.setText(sdf.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(){
        Calendar calendar = Calendar.getInstance();

        new TimePickerDialog(requireContext(), (timeView, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            editDueTime.setText(sdf.format(calendar.getTime()));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
    }


    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            new TimePickerDialog(requireContext(), (timeView, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                editnotification.setText(sdf.format(calendar.getTime()));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @SuppressLint("Range")
    private void loadTaskDetails(String id) {
        Cursor cursor = dbHelper.getTaskById(id);
        if (cursor != null && cursor.moveToFirst()) {
            editTitle.setText(cursor.getString(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.TITLE)));
            editDescription.setText(cursor.getString(cursor.getColumnIndexOrThrow("DESCRIPTION")));
            editDeadline.setText(cursor.getString(cursor.getColumnIndexOrThrow("DEADLINE")));
            String priority = cursor.getString(cursor.getColumnIndexOrThrow("PRIORITY"));
            editDueTime.setText(cursor.getString(cursor.getColumnIndexOrThrow("DUE_TIME")));
          /*  "isReminded ? \"REMINDER\" : \"NOTREMINDED\""*/
            String isreminded = cursor.getString(cursor.getColumnIndexOrThrow("REMINDER"));
            reminderBox.setChecked(isreminded.equals("REMINDER"));
            editnotification.setText(cursor.getString(cursor.getColumnIndexOrThrow("NOTIFYING")));




            if (priority != null) {
                int spinnerPosition = ((ArrayAdapter) spinnerPriority.getAdapter()).getPosition(priority);
                spinnerPriority.setSelection(spinnerPosition);
            }
            cursor.close();
        }
    }

    private void saveTaskDetails() {
        String newTitle = editTitle.getText().toString().trim();
        String newDescription = editDescription.getText().toString().trim();
        String newDeadline = editDeadline.getText().toString().trim();
        String newDueTime = editDueTime.getText().toString().trim();
        boolean newReminder = reminderBox.isChecked();
        String newNotification = editnotification.getText().toString().trim();


        if (TextUtils.isEmpty(newTitle)) {
            Toast.makeText(requireContext(), "Title cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        int rowsUpdated = dbHelper.updateTaskDetails(taskId, newTitle, newDescription, newDeadline,newDueTime, selectedPriority,newReminder ? "REMINDER" : "NOTREMINDED",newNotification);
        if (rowsUpdated > 0) {
            Toast.makeText(requireContext(), "Task updated successfully!", Toast.LENGTH_SHORT).show();
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_editTaskFragment_to_todayFragment);
        } else {
            Toast.makeText(requireContext(), "Failed to update task.", Toast.LENGTH_SHORT).show();
        }
    }
}
