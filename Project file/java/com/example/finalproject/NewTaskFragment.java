package com.example.finalproject;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NewTaskFragment extends Fragment {

    private OnTaskSavedListener listener;

    // UI components
    private EditText etTaskTitle, etTaskDescription, etDueDate,etDueDateNotificaiton;
    private Spinner spinnerPriority;
    private CheckBox cbCompletionStatus,cbReminderStatus;
    private Button btnSaveTask;
    private ImageView ivReminderIcon, ivBackArrow;
    private TimePicker tpDueTime, tpDueTimeNotification;

    private String reminderTime = ""; // Holds reminder time

    public interface OnTaskSavedListener {
        void onTaskSaved(Task task);
    }

    public void setOnTaskSavedListener(OnTaskSavedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_task, container, false);

        // Initialize UI components
        etTaskTitle = view.findViewById(R.id.et_task_title);
        etTaskDescription = view.findViewById(R.id.et_task_description);
        etDueDate = view.findViewById(R.id.et_due_date);
        spinnerPriority = view.findViewById(R.id.spinner_priority);
        cbCompletionStatus = view.findViewById(R.id.cb_completion_status);
        btnSaveTask = view.findViewById(R.id.btn_save_task);
        ivReminderIcon = view.findViewById(R.id.iv_reminder_icon);
        ivBackArrow = view.findViewById(R.id.iv_back_arrow);
        tpDueTime = view.findViewById(R.id.tp_due_time);

        cbReminderStatus = view.findViewById(R.id.reminderr);

        etDueDateNotificaiton = view.findViewById(R.id.et_due_date_Notification);
        tpDueTimeNotification = view.findViewById(R.id.tp_due_time_Notification);

        // Populate spinner and set default priority
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.priority_levels, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(adapter);
        spinnerPriority.setSelection(adapter.getPosition("Medium")); // Default to Medium priority

        // Handle date picker
        etDueDate.setOnClickListener(v -> showDatePickerDialog());

        cbReminderStatus.setOnClickListener(v -> {
            if(cbReminderStatus.isChecked()){
                etDueDateNotificaiton.setVisibility(View.VISIBLE);
                tpDueTimeNotification.setVisibility(View.VISIBLE);
            }
        });

        etDueDateNotificaiton.setOnClickListener(v ->showForNotifcationDatepicker());


        // Back arrow navigation
        ivBackArrow.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigateUp();
        });

        // Save task button
        btnSaveTask.setOnClickListener(v -> saveTask());

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnTaskSavedListener) {
            listener = (OnTaskSavedListener) context;
        }
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    etDueDate.setText(String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear));
                }, year, month, day);
        datePickerDialog.show();
    }
    private void showForNotifcationDatepicker(){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    etDueDateNotificaiton.setText(String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear));
                }, year, month, day);
        datePickerDialog.show();
    }


    private void saveTask() {
        String taskTitle = etTaskTitle.getText().toString().trim();
        String taskDescription = etTaskDescription.getText().toString().trim();
        String dueDate = etDueDate.getText().toString().trim();
        String priority = spinnerPriority.getSelectedItem().toString();
        boolean isCompleted = cbCompletionStatus.isChecked();
        boolean isReminded = cbReminderStatus.isChecked();


        // Validate required fields
        if (taskTitle.isEmpty() || dueDate.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert date to database format
        String formattedDate = convertDateToDatabaseFormat(dueDate);
        if (formattedDate == null) {
            Toast.makeText(getContext(), "Invalid date format.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get due time
        int hour = tpDueTime.getHour();
        int minute = tpDueTime.getMinute();
        String dueTime = String.format("%02d:%02d", hour, minute);

        int ntHour = tpDueTimeNotification.getHour();
        int ntMinute = tpDueTimeNotification.getMinute();
        String notificationTime = String.format("%02d:%02d", ntHour, ntMinute);


        String notifcationDate ="";
        if(etDueDateNotificaiton.getText().toString().isEmpty()){
            notifcationDate = formattedDate;//set it to due's date to not leave it empty.
        }else {
            notifcationDate = convertDateToDatabaseFormat(etDueDateNotificaiton.getText().toString().trim());
        }

        String notifyIn = notifcationDate +" " +notificationTime;


        String currentUserEmail = getCurrentUserEmail();

        Task task = new Task(taskTitle, taskDescription, formattedDate, dueTime, priority, isCompleted, isReminded,notifyIn);
        TaskDatabaseHelper dbHelper = new TaskDatabaseHelper(getContext());

        dbHelper.addTask(task.getTitle(), task.getDescription(), task.getDueDate(),task.getDueTime(),
                isCompleted ? "COMPLETED" : "PENDING", priority, currentUserEmail, -1, isReminded ? "REMINDER" : "NOTREMINDED" ,notifyIn);

        // Notify listener
        if (listener != null) {
            listener.onTaskSaved(task);
        }


        Toast.makeText(getContext(), "Task saved successfully!", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).navigateUp();
    }


    private String getCurrentUserEmail() {
        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(getContext());
        return sharedPrefManager.readString("logged_in_user", "noValue");
    }

    private String convertDateToDatabaseFormat(String date) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            Date parsedDate = inputFormat.parse(date);
            return outputFormat.format(parsedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


}
