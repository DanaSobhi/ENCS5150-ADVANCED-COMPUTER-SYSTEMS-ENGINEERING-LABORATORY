package com.example.finalproject;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CompletedFragment extends Fragment {

    private TaskDatabaseHelper dbHelper;
    private TaskAdapter adapter;
    private RecyclerView recyclerView;

    public CompletedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_completed, container, false);

        // Set up the toolbar with back navigation
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        dbHelper = new TaskDatabaseHelper(getContext());
        recyclerView = view.findViewById(R.id.recyclerViewCompletedTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        refreshCompletedTasks();

        return view;
    }

    private void refreshCompletedTasks() {
        Cursor cursor = dbHelper.getCompletedTasks();
        if (adapter == null) {
            adapter = new TaskAdapter(cursor, getContext(),"all",getParentFragmentManager());
            recyclerView.setAdapter(adapter);
        } else {
            adapter.swapCursor(cursor);
        }
    }
}
