package com.example.finalproject;


import android.content.Intent;
import android.os.Bundle;

import android.view.Menu;

import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.finalproject.databinding.ActivityHomeLayoutBinding;
import com.google.android.material.navigation.NavigationView;



public class HomeLayout extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHomeLayoutBinding binding;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityHomeLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set the Toolbar
        setSupportActionBar(binding.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Create ActionBarDrawerToggle
        toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                binding.toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        // Attach the DrawerListener and sync state
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Define top-level destinations for Navigation
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_today,
                R.id.nav_new_task,
                R.id.nav_all,
                R.id.nav_completed,
                R.id.nav_search,
                R.id.nav_profile,
                R.id.nav_logout)
                .setOpenableLayout(drawer)
                .build();

        // Set up NavController and link it to the NavHostFragment
        Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_home_layout);
        NavController navController = ((NavHostFragment) navHostFragment).getNavController();

        if (navHostFragment instanceof NavHostFragment) {
            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);
        }
            // Load TodayFragment as the default fragment
        navController.navigate(R.id.nav_today); // Navigate to TodayFragment by default

            // Handle item clicks in the NavigationView
        navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_today) {
                    navController.navigate(R.id.nav_today); // Navigate to TodayFragment
                } else if (id == R.id.nav_new_task) {
                    navController.navigate(R.id.newTaskFragment); // Navigate to New Task fragment
                } else if (id == R.id.nav_all) {
                    navController.navigate(R.id.allTasksFragment); // Navigate to All Tasks fragment
                } else if (id == R.id.nav_completed) {
                    loadCompletedFragment(); // Load the Completed fragment
                } else if(id == R.id.nav_search) {
                    navController.navigate(R.id.searchFragment); // Navigate to SearchFragment
                } else if (id == R.id.nav_profile) {
                    navController.navigate((R.id.profileFragment));
                } else if (id == R.id.nav_logout){
                    Toast toast =Toast.makeText(HomeLayout.this,"Logged out", Toast.LENGTH_SHORT);
                    toast.show();
                    SharedPrefManager.getInstance(this).writeString("logged_in_user", null); //clear user data
                    Intent intent = new Intent(HomeLayout.this, MainActivity.class);
                    startActivity(intent);
                    finish();


                }else
                    navController.navigate(R.id.nav_today); // Navigate to TodayFragment


            drawer.closeDrawer(GravityCompat.START);
                return true;
            });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_layout, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home_layout);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = binding.drawerLayout;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Method to load the CompletedFragment
    private void loadCompletedFragment() {
        CompletedFragment completedFragment = new CompletedFragment();

        // Begin the fragment transaction
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace the current fragment with CompletedFragment
        transaction.replace(R.id.nav_host_fragment_content_home_layout, completedFragment);

        // Optionally, add the transaction to the back stack if you want the user to navigate back
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }
}
